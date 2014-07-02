package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stone.javatasks.helperclasses.GridSpanStimulus;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
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
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.element.handler.pause.PauseHandlerUtil;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * displays a sequence of grid locations that the participants
 * must remember and recall in correct serial order.
 * @author James Stone
 *
 */

public class MatrixSpan extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
		ExecutionPhaseListener {

	Logger logger = LoggerFactory.getLogger(WordDigitSpan.class);
	
	private RegionsContainer regionsContainer;
	
	//phases of task//
	public enum Phase {
		INIT, MEMO, RECALL
	}

	private Phase currentPhase;	
	
	//properties of interest//
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	
	private GridSpanStimulus thisTrialPanel; //this is a JPanel with the grid constructed//
	private JPanel holdingPanel;
	
	//timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private TimerTask startRecallTask;
	private int displayDuration = 1000; //duration string should be displayed in ms//
	
	//stimuli
	private int[] simpleSpans = {2,2,2,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6};
	private int[] complexSpans = {2,2,2,2,2,2,3,3,3,4,4,4,5,5,5};
	private ArrayList<Integer> spansList = new ArrayList<Integer>();
	private ArrayList<Integer> stimuli;
	private int complexSpan;
	private String current_user;	
	
	private int trialCounter;
	private int memCounter; //counts memoranda presented per trial//
	private int respCounter; //counts responses given//
	private ArrayList<Integer> correctResponse; 
	private ArrayList<Integer> givenResponse; 
	
	private long startTime;
	private long endTime;
	
	private Random rand;
	
	//buttons for recall
	private JButton box_1 = new JButton();private JButton box_2 = new JButton();private JButton box_3 = new JButton();private JButton box_4 = new JButton();
	private JButton box_5 = new JButton();private JButton box_6 = new JButton();private JButton box_7 = new JButton();private JButton box_8 = new JButton();
	private JButton box_9 = new JButton();private JButton box_10 = new JButton();private JButton box_11 = new JButton();private JButton box_12 = new JButton();
	private JButton box_13 = new JButton();private JButton box_14 = new JButton();private JButton box_15 = new JButton();private JButton box_16 = new JButton();
	private ArrayList<JButton> buttons;
	private JPanel recallPanel;
	private JPanel tmpPanel;
	private recallButtonListener recallButtonListener = new recallButtonListener();
	
	final Color fillColor = new Color(0,51,102);
	
	public MatrixSpan() {
		
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception e)
		{}
		
		thisTrialPanel = new GridSpanStimulus(4,400);
		tmpPanel = new JPanel();
		tmpPanel.setBorder(BorderFactory.createTitledBorder("Remember the sequence"));
		tmpPanel.add(thisTrialPanel);
		tmpPanel.setBackground(Color.WHITE);
		tmpPanel.setPreferredSize(new Dimension(434,434));
		tmpPanel.setMaximumSize(new Dimension(434,434));		
		
		rand = new Random();
		timer = new Timer();
	}
	
	//start method//
	protected void startExecutionAWT() {
		
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception e)
		{}
		
		//initialise environment//
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		current_user = context.getExecutionData().getModule().getUserAccount().getName();
		StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		customNamePanel.setProperty("title","User");
		customNamePanel.setProperty("value", current_user);
		
		switch(currentPhase) {
		case INIT:
			startInitPhase();
			break;
		case MEMO:
			startMemoPhase();
			break;
		case RECALL:
			startRecallPhase();
			break;
		}
	}
	
	private void startInitPhase() {
		
		holdingPanel = new JPanel();
		holdingPanel.setPreferredSize(new Dimension(800,800));
		holdingPanel.setBackground(Color.WHITE);
		holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
		holdingPanel.setBorder(BorderFactory.createTitledBorder("Remember the sequence"));
		
		generateStimuli();
		
		//reset stim counter//
		memCounter = 0;
		respCounter = 0;
		
		correctResponse = new ArrayList<Integer>();
		givenResponse = new ArrayList<Integer>();
		
		Result.getResultProperty().setValue(this,null);
		
		//start memo phase//
		currentPhase = Phase.MEMO;
		
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(tmpPanel);
		holdingPanel.add(Box.createVerticalGlue());
		
		startMemoPhase();
	}	
	
	private void startMemoPhase() {
		
		thisTrialPanel.fillButton(stimuli.get(memCounter), fillColor);

		//suspend for specified amount of time, this is presentation time of each digit/word//
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						thisTrialPanel.fillButton(stimuli.get(memCounter), Color.WHITE);
						memCounter++; //incremement memCounter as an additional digit/word has been shown//
						//if this is is last stim then change to recall phase//
						if (memCounter == stimuli.size()) {
							currentPhase = Phase.RECALL;		
						}
						
						suspendExecutable(); // suspend task
					}
				});
			}
		};
		
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		timer.schedule(suspendExecutableTask, displayDuration);				
	}
	
	private void startRecallPhase() {
		produceRecallGrid();
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);		
	}
	
	private void processProperties() {
		System.out.println("spansList: " + spansList);
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		correctResponse = stimuli;
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, String.valueOf(correctResponse), correctResponse);
		boolean success = correctResponse.equals(givenResponse);
		loadProperty.setValue(this, stimuli.size());
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED); //may be a prob?!//
		
		trialNoProperty.setValue(this, trialCounter + 1);
		
		// set duration time property
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);

		if (getExecutionContext() != null) {
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		}
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);		
		
	}
	
	
	private void endTask() {
		currentPhase = Phase.INIT;
		trialCounter++;
		System.out.println("trialCounter: " + trialCounter);
		
		holdingPanel.removeAll();
		
		for (JButton jb : buttons) {
			jb.removeActionListener(recallButtonListener);
		}
		
		if (getFinishExecutionLock()) {
			finishExecution();
		}		
	}
	
	/**
	 * Sets the outcome of this executable to SUSPENDED in order for us to be
	 * able to continue where we left after other executables have executed.
	 */
	private void suspendExecutable() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);

		// set outcome in the execution context to SUSPENDED to mark compound
		// element as being suspended in order to return later
		if (getExecutionContext() != null) {
			System.out.println("EXECUTION BEING SUSPENDED");
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.SUSPENDED);
		}
		// finish the execution and make sure nothing else already did so
		if (getFinishExecutionLock()) {
			finishExecution();
		}
	}	
	
	
	
	/**
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list and set the executable phase
	 * to INIT.
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
			trialCounter = 0;
		}
	}	
	
	protected void cancelExecutionAWT() {
		timer.cancel();
		currentPhase = Phase.INIT;
    }	
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 */
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty(),
				loadProperty, trialNoProperty };
	}	
	
	
	private void generateStimuli() {
		
		//if first trial then generate trial spans
		if (trialCounter == 0) {
			//compile ArrayList for spans//
			System.out.println("complexSpan: " + complexSpan);
			System.out.println("trialCounter: " + trialCounter);
			if (complexSpan == 1) {
				for (int i = 0; i < complexSpans.length; i++) {
					spansList.add(complexSpans[i]);
				}			
			} else if (complexSpan == 0) {
				for (int i = 0; i < simpleSpans.length; i++) {
					spansList.add(simpleSpans[i]);
				}
			}
			System.out.println("spansList: " + spansList);			
		}		
		
		
		//need to get a span number
		//then get a random set of ints between 0-15 with no repeats
		//of the same length as the span.
		/*
		int spanIndice = rand.nextInt(spansList.size());
		int thisTrialSpan = spansList.get(spanIndice);
		spansList.remove(spanIndice);
		*/
		int thisTrialSpan = spansList.get(0);
		spansList.remove(0);
		
		stimuli = new ArrayList<Integer>();
		
		for (int i = 0; i < thisTrialSpan; i++) {
			int genNum = rand.nextInt(16);
			if (!stimuli.contains(genNum)) {
				stimuli.add(genNum);
			} else {
				i--;
			}
		}
		
	}
	
	private void produceRecallGrid() {
		//initialise the JButtons we need and add them to an arraylist//
		buttons = new ArrayList<JButton>();
		buttons.add(box_1);buttons.add(box_2);buttons.add(box_3);buttons.add(box_4);
		buttons.add(box_5);buttons.add(box_6);buttons.add(box_7);buttons.add(box_8);
		buttons.add(box_9);buttons.add(box_10);buttons.add(box_11);buttons.add(box_12);
		buttons.add(box_13);buttons.add(box_14);buttons.add(box_15);buttons.add(box_16);
		
		//holdingPanel = new JPanel();
		//holdingPanel.setPreferredSize(new Dimension(800,800));
		//holdingPanel.setBackground(Color.WHITE);
		
		recallPanel = new JPanel();
		recallPanel.setPreferredSize(new Dimension(444,444));
		recallPanel.setMaximumSize(new Dimension(444,444));
		recallPanel.setBackground(Color.WHITE);
		recallPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		recallPanel.setBorder(BorderFactory.createTitledBorder("Click the boxes in the order you were shown"));
		
		for (JButton item : buttons) {
			item.setPreferredSize(new Dimension(100,100));
			item.setBackground(Color.WHITE);
			recallPanel.add(item);
			item.addActionListener(recallButtonListener);
		}
		
		holdingPanel.removeAll();
		holdingPanel.setBorder(BorderFactory.createTitledBorder("Click the boxes in the order you were shown"));
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(recallPanel);
		holdingPanel.add(Box.createVerticalGlue());
	}
	
	private class recallButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//change its color
			System.out.println("recallButtonListener in action...");
			JButton gridClicked = (JButton)(event.getSource());
			gridClicked.setBackground(fillColor);
			givenResponse.add(buttons.indexOf(gridClicked));
			
			respCounter++;
			System.out.println("respCounter: " + respCounter);
			System.out.println("stimuli.size(): " + stimuli.size());
			
			//for debugging
			List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
			System.out.println("executed iterations: " + ILSS.get(0).getExecutedIterations());
			
			
			if (respCounter == stimuli.size()) {
				regionsContainer.setRegionContentVisibility(Region.CENTER, false);
				processProperties();
				endTask();
			}
			
		}	
	}
	
	public int getcomplexSpan() {
		return this.complexSpan;
	}
	public void setcomplexSpan(int c) {
		this.complexSpan = c;
	}
			
	
}
