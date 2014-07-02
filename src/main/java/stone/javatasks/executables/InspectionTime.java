package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.action.ActionPanel;
import ch.tatool.core.display.swing.action.ActionPanelListener;
import ch.tatool.core.display.swing.action.KeyActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;

/**
 * Classic Inspection time task. Participant is presented with two vert lines
 * joined by a horizontal line. The bottom half of the lines is occluded after 
 * a brief interval and the participant must respond with the shortest line.
 * @author James Stone
 *
 */

public class InspectionTime extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder {
	
	Logger logger = LoggerFactory.getLogger(InspectionTime.class);
	private RegionsContainer regionsContainer;	
	
	private IntegerProperty SoaProperty = new IntegerProperty("SOA");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	
	private JLayeredPane layeredPane;
	private JPanel holderPanel;
	private JLabel displayLabel;
	private JLabel occluderLabel;
	private KeyActionPanel responsePanel;
	
	private Timer timer;
	private TimerTask activateRecall;
	private int initialSOA = 0; //soa, will be initially set by XML//	
	private int currentTrialSOA; //soa for current trial//
	private int additionSOA = 0; //keep track of SOA change due to performance
	private ArrayList<Integer> outcomes;
	private int trialsCorrectSinceChange;
	private int reversals; //number of staircase reversals. As per Nettelbeck and Burns, cease execution after 8 reversals.
	private String direction;
	
	private long startTime;
	private long endTime;
	private Random rand;
	private String current_user;
	
	private ImageIcon left_icon;
	private ImageIcon right_icon;
	private ImageIcon occluder_icon;
	
	private int correctResponse;
	private int givenResponse;
	private int trialCounter = 0;
	
	private IteratedListSelector thisILS;
	
	public InspectionTime() {
		rand = new Random();
		timer = new Timer();
		layeredPane = new JLayeredPane();
		holderPanel = new JPanel();
		displayLabel = new JLabel();
		occluderLabel = new JLabel();
		responsePanel = new KeyActionPanel();
		outcomes = new ArrayList<Integer>();
		trialsCorrectSinceChange = 0;
		reversals = 0;
		direction = "";
		//set some characteristics of these constructed panels//
		//holderPanel.setBackground();
		/*
		BoxLayout layout = new BoxLayout(holderPanel, BowLayout.Y_AXIS);
		holderPanel.setLayout(layout);
		*/
		layeredPane.setPreferredSize(new Dimension(380,580));
		layeredPane.setMaximumSize(new Dimension(380,580));
		layeredPane.setBackground(Color.WHITE);
		layeredPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		layeredPane.setBorder(BorderFactory.createTitledBorder("Which line is the shortest?"));
		
		//holderPanel.setLayout(new BoxLayout(holderPanel, BoxLayout.Y_AXIS));
		
		//holderPanel.add(Box.createVerticalGlue());
		holderPanel.setBackground(Color.WHITE);
		holderPanel.add(layeredPane);
		//holderPanel.add(Box.createVerticalGlue());
		
		//load the stimuli images//
		left_icon = new ImageIcon(getClass().getResource("/stimuli/imgs/inspection_time_left.png"));
		right_icon = new ImageIcon(getClass().getResource("/stimuli/imgs/inspection_time_right.png"));
		occluder_icon = new ImageIcon(getClass().getResource("/stimuli/imgs/it_occluded_2.png"));
		//ImageIcon occluderIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/occluder.png"));
		//occluderLabel.setIcon(occluderIcon);
		
		displayLabel.setBounds(40,20,300,500);
		//occluderLabel.setBounds(40,310,320,250);
		
		//occluderLabel.setOpaque(false);
		//displayLabel.setOpaque(false);
		
		layeredPane.add(displayLabel);
		//layeredPane.add(occluderLabel);
	}
	
	//start method//
	protected void startExecutionAWT() {
		//initialise environment//
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		current_user = context.getExecutionData().getModule().getUserAccount().getName();
		StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		customNamePanel.setProperty("title","User");
		customNamePanel.setProperty("value", current_user);
		
		regionsContainer.setRegionContent(Region.CENTER, holderPanel);
		regionsContainer.setRegionContent(Region.SOUTH, responsePanel);
		
		responsePanel.addKey(KeyEvent.VK_LEFT, "left", 0);
		responsePanel.addKey(KeyEvent.VK_RIGHT, "right", 1);
		
		responsePanel.addActionPanelListener(this);
		
		runTrial();
	}
	
	private void runTrial() {
		if (trialCounter == 0)
			thisILS = (IteratedListSelector) ElementUtils.findHandlerInStackByType(getExecutionContext(), IteratedListSelector.class);
		//decide on stim to present
		int thisDirection = rand.nextInt(2);
		correctResponse = thisDirection;
		setDisplayLabel(thisDirection);
		refreshRegion(Region.CENTER);
		
		currentTrialSOA = initialSOA + additionSOA;
		
		//show the region
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		
		activateRecall = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						occludeStimuli();
						startTime = System.nanoTime();
						regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
						responsePanel.enableActionPanel();
					}
				});
			}
		};		
		timer.schedule(activateRecall, currentTrialSOA);		
	}
	

	
	public void actionTriggered(ActionPanel source, Object actionValue) {
		responsePanel.disableActionPanel();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		givenResponse = (Integer) actionValue;
		
		endTask();
	}
	
	private void endTask() {
		System.out.println("thisILS iterations: " + thisILS.getExecutedIterations());
		trialCounter++;
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, "left or right", correctResponse);
		boolean success = correctResponse == givenResponse;
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		
		//duration time property//
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
			if (duration <= 0) {
				duration = 0;
			}
		}
		long ms = (long) duration/1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		
		//new trial//
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		
		trialNoProperty.setValue(this, trialCounter);
		SoaProperty.setValue(this, currentTrialSOA);
		
		//add all executable properties//
		DataUtils.storeProperties(currentTrial,  this);
		
		if (success) {
			outcomes.add(1);
			trialsCorrectSinceChange += 1;
		} else {
			outcomes.add(0);
			additionSOA += 18;
			if (direction.equals("decrease"))
				reversals += 1;
			direction = "increase";
			trialsCorrectSinceChange = 0;
		}
		
		if (trialsCorrectSinceChange == 3) {
			if (additionSOA > -232)
				additionSOA -= 18;
			if (direction.equals("increase"))
				reversals += 1;
			direction = "decrease";
			trialsCorrectSinceChange = 0;
		}
		
		if (reversals == 8) {
			thisILS.setNumIterations(thisILS.getExecutedIterations());
		}
		
		//finish execution //
		if (getFinishExecutionLock()) {
			finishExecution();
		}
	}
	
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty(),
				trialNoProperty, SoaProperty };
	}
	
	
	public void setDisplayLabel(int direction) {
		//method to set display label to the correct image
		if (direction == 0) { // "left" //
			displayLabel.setIcon(left_icon);
		} else if (direction == 1) { //"right"//
			displayLabel.setIcon(right_icon);
		} else {
			System.err.println("invalid direction value");
		}
	}
	
	public void occludeStimuli() {
		//layeredPane.moveToFront(occluderLabel);
		//layeredPane.moveToBack(displayLabel);
		displayLabel.setIcon(occluder_icon);
		refreshRegion(Region.CENTER);
	}
	
	public void unOccludeStimuli() {
		layeredPane.moveToFront(displayLabel);
		layeredPane.moveToBack(occluderLabel);
		refreshRegion(Region.CENTER);
	}
	
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		holderPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}	

	public void setinitialSOA(int n) {
		this.initialSOA = n;
	}
	public int getinitialSOA() {
		return this.initialSOA;
	}
	
	

}
