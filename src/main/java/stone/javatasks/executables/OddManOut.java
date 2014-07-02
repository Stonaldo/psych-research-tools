package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stone.javatasks.helperclasses.RoundButton;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.LongProperty;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Implements the odd-man-out task whereby the stimulus array is 8 circles 
 * that are equi-distant apart. On any given trial three of the cirles will 
 * light up and the participant must respond with the circle they believe to 
 * be the odd man out (the one that is furthest away from the other two).
 * 
 * @author James Stone
 *
 */

public class OddManOut extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
ExecutionPhaseListener {
	
	Logger logger = LoggerFactory.getLogger(OddManOut.class);
	
	private RegionsContainer regionsContainer;
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	private LongProperty decisionTime = new LongProperty("decisionTime");
	private LongProperty movementTime = new LongProperty("movementTime");
	private int trialCounter = 0;
	
	//panels//
	private JPanel holderPanel;
	private JPanel omoPanel;
	private JPanel homeButtonPanel;
	private JPanel homePanel;
	private JLabel text;
	private JLabel homeText;
	
	//buttons//
	private ArrayList<RoundButton> buttons;
	private RoundButton homeButton;
	
	//timing//
	private Timer timer;
	private TimerTask randomPause;
	private int pauseDuration;
	private Random rand;
	
	private ArrayList<Integer> thisTrialLights;
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private long releaseTime;
	
	private ImageIcon redIcon;
	private ImageIcon blueIcon;
	private ImageIcon greenIcon;
	private ImageIcon yellowIcon;
	private ImageIcon seagreenIcon;
	
	private omoButtonListener omoButtonListener = new omoButtonListener();
	private HomeButtonHandler homeButtonHandler = new HomeButtonHandler();
	
	final Font textFont = new Font("Source Code Pro", 1, 20);
	private String current_user;
	
	public OddManOut() {
		holderPanel = new JPanel();
		holderPanel.setBackground(Color.WHITE);
		holderPanel.setLayout(new GridLayout(3,1));
		homePanel = new JPanel();
		homePanel.setBackground(Color.WHITE);
		homePanel.setLayout(new GridLayout(2,1));
		homeButtonPanel = new JPanel();
		homeButtonPanel.setBackground(Color.WHITE);
		omoPanel = new JPanel();
		omoPanel.setBackground(Color.WHITE);
		omoPanel.setPreferredSize(new Dimension(810,110));
		text = new JLabel("<html><div style=\"text-align: center;\">To start the trial press the home button and keep it pressed. <br>Once the buttons light up you can release the home button <br>and select the odd one out.</html>");
		text.setFont(textFont);
		text.setHorizontalAlignment(JLabel.CENTER);
		homeText = new JLabel("<html><div style=\"text-align: center;\">HOME<html>");
		homeText.setFont(textFont);
		homeText.setHorizontalAlignment(JLabel.CENTER);
		homeText.setVerticalAlignment(JLabel.TOP);
		
		//set icons//
		redIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_red_alpha_100.png"));
		blueIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_blue_alpha_100.png"));
		greenIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_green_alpha_100.png"));
		yellowIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_yellow_alpha_100.png"));
		seagreenIcon = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_seagreen_alpha_100.png"));
		
		homeButton = new RoundButton(yellowIcon);
		homeButton.setPressedIcon(greenIcon);
		homeButtonPanel.add(homeButton);
		homePanel.add(homeButtonPanel);
		homePanel.add(homeText);
		
		holderPanel.add(text);
		holderPanel.add(omoPanel);
		holderPanel.add(homePanel);
		
		rand = new Random();
		timer = new Timer();
	}
	
	
	//start method//
	@SuppressWarnings("serial")
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
		
		buttons = new ArrayList<RoundButton>() {{
			for (int i = 0; i < 8; i++) {
				add(new RoundButton(blueIcon));
			}
		}};
		
		for (RoundButton but : buttons) {
			omoPanel.add(but);
		}
		
		regionsContainer.setRegionContent(Region.CENTER, holderPanel);
		
		runTrial();
	}
	
	private void runTrial() {
		//pick the target for this trial//
		int thisTrialTarget = rand.nextInt(buttons.size());
		correctResponse = thisTrialTarget;
		//call a method to determine the alternate lights to flash//
		int[] thisTrialFoils = determineFoils(thisTrialTarget);
		System.out.println("thisTrialTarget: " + thisTrialTarget);
		thisTrialLights = new ArrayList<Integer>();
		thisTrialLights.add(thisTrialTarget);
		thisTrialLights.add(thisTrialFoils[0]);
		thisTrialLights.add(thisTrialFoils[1]);
		System.out.println("thisTrialLights: " + thisTrialLights);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		homeButton.addMouseListener(homeButtonHandler);
	}
	
	
	private class omoButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			for (RoundButton c : buttons) {
				c.removeActionListener(omoButtonListener);
			}
			
			RoundButton butClicked = (RoundButton)(e.getSource());
			butClicked.setIcon(greenIcon);
			givenResponse = buttons.indexOf(butClicked);
			TimerTask suspendExecutableTask = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							endTime = System.nanoTime();
							endTask();
						}
					});
				}
			};

			timer.schedule(suspendExecutableTask, 250);				
		}
	}
	
	private int[] determineFoils(int target) {
		
		int firstFoil = 99;
		int secondFoil = 99;
		
		boolean m = true;
		
		while (m) {
			//pick two ints that are not the target
			for (int i = 0; i < 1; i++) {
				firstFoil = rand.nextInt(buttons.size());
				System.out.println("firstFoil: " + firstFoil);
				if (firstFoil == target) {
					i--;
				}
			}
			for (int i = 0; i < 1; i++) {
				secondFoil = rand.nextInt(buttons.size());
				System.out.println("secondFoil: " + secondFoil);
				if (secondFoil == target | secondFoil == firstFoil) {
					i--;
				}
			}
			
			//check if these foils are ok by computing distances//
			int foilsDistance = Math.abs(firstFoil - secondFoil);
			int targetFoilOneDistance = Math.abs(target - firstFoil);
			int targetFoilTwoDistance = Math.abs(target - secondFoil);
			
			if (foilsDistance < targetFoilOneDistance & foilsDistance < targetFoilTwoDistance) {
				//then we can accept these foils
				m = false;
			} else {}			
		}
		
		int[] foils = {firstFoil, secondFoil};
		return foils;
	}
	
	
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
		}
	}	
	
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty(),
				decisionTime, movementTime,
		};
	}
	
	private void endTask() {
		trialCounter++;
		processProperties();
		omoPanel.removeAll();
		//finish execution //
		if (getFinishExecutionLock()) {
			finishExecution();
		}		
	}
	
	private void processProperties() {
		homeButton.removeMouseListener(homeButtonHandler);
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, String.valueOf(correctResponse), correctResponse);
		boolean success = correctResponse == givenResponse;
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED); //may be a prob?!//
		
		trialNoProperty.setValue(this, trialCounter + 1);
		
		// set duration time property (from onset of stims to selection)
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		
		// set decision time property (from onset of stims to release of home button)
		long decision = 0;
		if (releaseTime > 0) {
			decision = releaseTime - startTime;
		}
		long ms_decision = (long) decision / 1000000;
		decisionTime.setValue(this, ms_decision);		
		
		// set movement time property (from release of home button to selection)
		long movement = 0;
		if (endTime > 0) {
			movement = endTime - releaseTime;
		}
		long ms_movement = (long) movement / 1000000;
		movementTime.setValue(this, ms_movement);			

		if (getExecutionContext() != null) {
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		}
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);		
		
	}
	
	private class HomeButtonHandler implements MouseListener {

		public void mouseClicked(MouseEvent event) {}
		
		public void mousePressed(MouseEvent event){
			//home button pressed so get random amount to wait
			pauseDuration = rand.nextInt(1000) + 500;
			//remove listener from home button
			
			randomPause = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							for (int x : thisTrialLights) {
								buttons.get(x).setIcon(redIcon);
							}
							
							for (RoundButton x : buttons) {
								x.addActionListener(omoButtonListener);
							}
							startTime = System.nanoTime();
						}
					});
				}
			};

			timer.schedule(randomPause, pauseDuration);
		}
		
		public void mouseReleased(MouseEvent event){
			System.out.println("mouse released");
			timer.cancel();
			timer = new Timer();
			releaseTime = System.nanoTime();
		}
		
		public void mouseEntered(MouseEvent event){}
		
		public void mouseExited(MouseEvent event){}	
		
	}
	
	
}