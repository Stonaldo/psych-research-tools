/*******************************************************************************
* 
* 
*
*
*
*
*
/*******************************************************************************/

package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Level;
import ch.tatool.core.data.LongProperty;
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
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.ExecutionStartHandler;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Runs the working memory period task as described initially in Towse et al.(2005)
 * and used as part of the training regimen in the authors PhD research. 
 * The task works by not manipulating difficulty in terms of increased span (ie by 
 * increasing TBR items but by manipulating processing requirements between 
 * presentation of TBR items by changing the complexity of operations required. 
 
 * @author James Stone
 */

public class WorkingMemoryPeriod extends BlockingAWTExecutable implements 
ActionPanelListener, DescriptivePropertyHolder, ExecutionPhaseListener {

	Logger logger = LoggerFactory.getLogger(WorkingMemoryPeriod.class);
	
	private int numOperations = 4;

	private FlowLayout qpFlow;
	private CenteredTextPanel questionPanel;
	private CenteredTextPanel responseDisplayPanel;
	private KeyActionPanel actionPanel;
	private ArrayList<Integer> correctResponse;
	private ArrayList<Integer> opResponse = new ArrayList<Integer>();
	private ArrayList<Integer> givenResponse;
	
	private long[] operation_startTimes = {0,0,0,0,0};
	private long[] operation_endTimes = {0,0,0,0,0};
	private long trial_startTime;
	private long trial_endTime;
	private long recall_startTime;
	private long recall_endTime;
	private Timer timer;
	
	private String question = "";
	private String responseStringBase = "Response: ";
	private String responseString = "";
	private int currentOperationToDisplay = 0;
	private int block;
	private List<String> stimsThisTrial;
	
	private Random rnd = new Random();

	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty blockProperty = new IntegerProperty("block");
	private IntegerProperty trialnoProperty = new IntegerProperty("trial");
	private LongProperty op1_rt_property = new LongProperty("op1_rt");
	private LongProperty op2_rt_property = new LongProperty("op2_rt");
	private LongProperty op3_rt_property = new LongProperty("op3_rt");
	private LongProperty op4_rt_property = new LongProperty("op4_rt");
	private LongProperty op5_rt_property = new LongProperty("op5_rt");
	private LongProperty recall_rt_property = new LongProperty("recall_rt");

	private RegionsContainer regionsContainer;
	
	private List<String> stimulus;
	
	//for collecting trial response
	private JButton num0 = new JButton("0");
	private JButton num1 = new JButton("1");
	private JButton num2 = new JButton("2");
	private JButton num3 = new JButton("3");
	private JButton num4 = new JButton("4");
	private JButton num5 = new JButton("5");
	private JButton num6 = new JButton("6");
	private JButton num7 = new JButton("7");
	private JButton num8 = new JButton("8");
	private JButton num9 = new JButton("9");
	//to reset trial response if clicked wrong box
	private JButton reset = new JButton("reset");
	private ArrayList<JButton> respButtons;
	
	private String current_user;
	
	private int numBlocks = 3;
	private int numTrialsPerBlock = 6;
	private int overallTrialCounter = 0;
	private int blockTrialCounter = 0;
	
	public WorkingMemoryPeriod() {	
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e)
		{}
		
		questionPanel = new CenteredTextPanel();
		responseDisplayPanel = new CenteredTextPanel();
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		timer = new Timer();
		
		givenResponse = new ArrayList<Integer>();
		correctResponse = new ArrayList<Integer>();
		
		NumberHandlerClass responseButtonHandler = new NumberHandlerClass();
		ResetHandlerClass resetButtonHandler = new ResetHandlerClass();
		
		final Font numFont = new Font("Open Sans",1,24);
		final Font responseFont = new Font("Source Code Pro", 1, 36);
		final Font resetFont = new Font("Open Sans", 1, 20);
		
		//wrap this up into for loops by adding the buttons to an array//
		respButtons = new ArrayList<JButton>();
		respButtons.add(num0); respButtons.add(num1); respButtons.add(num2); 
		respButtons.add(num3); respButtons.add(num4); respButtons.add(num5);
		respButtons.add(num6); respButtons.add(num7); respButtons.add(num8); respButtons.add(num9);
		for (int i = 0; i < respButtons.size(); i++) {
			respButtons.get(i).setPreferredSize(new Dimension(100,100));
			respButtons.get(i).setBackground(Color.LIGHT_GRAY);
			respButtons.get(i).setFont(numFont);
			respButtons.get(i).addActionListener(responseButtonHandler);
		}

		reset.addActionListener(resetButtonHandler);
		reset.setPreferredSize(new Dimension(200,100));
		reset.setFont(resetFont);
		
		responseDisplayPanel.setTextFont(responseFont);
		
	}

	protected void startExecutionAWT() {
		
		// initialise environment for executable
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		current_user = context.getExecutionData().getModule().getUserAccount().getName();
		StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		customNamePanel.setProperty("title","User");
		customNamePanel.setProperty("value", current_user);
		
		if (overallTrialCounter == 0) {
            DefaultVisualTimeoutHandler handler2 = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(context, DefaultVisualTimeoutHandler.class);
    		if (handler2 != null) {
    			handler2.startTimeout(context);
    		}
		}

		triggerStartExecution(getExecutionContext());
		//make sure the response array is clear, won't be if this is not the first trial.
		//Could do this here or at the end of execution, doesn't matter which imo.		
		correctResponse.clear();
		givenResponse.clear();
		
		stimulus = initStimulus();

		//reset layout
		questionPanel.setLayout(new GridLayout());
		
		actionPanel.addKey(KeyEvent.VK_0, "0", 0);
		actionPanel.addKey(KeyEvent.VK_1, "1", 1);
		actionPanel.addKey(KeyEvent.VK_2, "2", 2);
		actionPanel.addKey(KeyEvent.VK_3, "3", 3);
		actionPanel.addKey(KeyEvent.VK_4, "4", 4);
		actionPanel.addKey(KeyEvent.VK_5, "5", 5);
		actionPanel.addKey(KeyEvent.VK_6, "6", 6);
		actionPanel.addKey(KeyEvent.VK_7, "7", 7);
		actionPanel.addKey(KeyEvent.VK_8, "8", 8);
		actionPanel.addKey(KeyEvent.VK_9, "9", 9);
		
		regionsContainer.setRegionContent(Region.CENTER, questionPanel);
		regionsContainer.setRegionContent(Region.SOUTH, responseDisplayPanel);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		trial_startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		
		//Display operations and collect response
		displayOperation(stimulus.get(currentOperationToDisplay));
	}

	
	private void displayOperation(String operation) {
		questionPanel.setTextSize(90);
		if (Level.getLevelProperty().getValueOrDefault(this) > 5)
			questionPanel.setTextSize(60);
		if (Level.getLevelProperty().getValueOrDefault(this) > 10)
			questionPanel.setTextSize(40);
		
		questionPanel.setText(operation);
		actionPanel.enableActionPanel();
		operation_startTimes[currentOperationToDisplay] = System.nanoTime();
	}
	
	private void processResponse(int resp) {
		operation_endTimes[currentOperationToDisplay] = System.nanoTime();
		opResponse.add(resp);
		currentOperationToDisplay += 1;
		if (currentOperationToDisplay < numOperations) {
			displayOperation(stimulus.get(currentOperationToDisplay));
		} else {
			currentOperationToDisplay = 0;
			getTBRresponse();
		}
	}
	
	private void getTBRresponse() {
		
		//set text to empty otherwise the third operation will persist in view. 
		questionPanel.setText("");
		
		qpFlow = new FlowLayout();
		qpFlow.setVgap(questionPanel.getHeight() / 2);
		questionPanel.setLayout(qpFlow);
		
		for (int i = 0; i < respButtons.size(); i++) {
			questionPanel.add(respButtons.get(i));
		}
		
		questionPanel.add(reset);
		
		responseDisplayPanel.setText(getHtmlString(responseStringBase));

		questionPanel.revalidate();
		recall_startTime = System.nanoTime();
	}
	
	//respond to the button clicks as a response
	private class NumberHandlerClass implements ActionListener {
		public void actionPerformed(ActionEvent event){
			JButton butClicked = (JButton)(event.getSource());
			givenResponse.add(JButtonToNumber(butClicked));
			updateDisplayedResponseString(butClicked.getText());
			if (givenResponse.size() == numOperations) {
				for (int i = 0; i < respButtons.size(); i++) {
					questionPanel.remove(respButtons.get(i));
				}
				questionPanel.remove(reset);
				responseDisplayPanel.setText("");
				responseString = "";
				endTask();
			}
		}
	}
	
	private class ResetHandlerClass implements ActionListener {
		public void actionPerformed(ActionEvent event){
			resetDisplayedResponseString();
		}
	}
	
	
	private int JButtonToNumber(JButton but) {
		int numClicked;
		numClicked = Integer.valueOf(but.getText());
		
		return numClicked;
	}
	
	private void updateDisplayedResponseString(String toAppend) {
		responseString += toAppend + " ";
		responseDisplayPanel.setText(getHtmlString(responseStringBase + responseString));
	}
	
	private void resetDisplayedResponseString() {
		responseString = "";
		responseDisplayPanel.setText(getHtmlString(responseStringBase + responseString));
		givenResponse.clear();
	}
	
	/**
	 * Generates the stimuli.
	 */
	private List<String> initStimulus() {
		//set block property here as this method is called at start of any given trial
		//set block property
		List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
		
		
		/*
		if (overallTrialCounter == 0) {
			ILSS.get(1).setNumIterations(numBlocks);
		}
		
		if (blockTrialCounter == 0) {
			ILSS.get(0).setNumIterations(numTrialsPerBlock);
		}
		*/
		
		
		//block = ILSS.get(1).getExecutedIterations();
		//blockProperty.setValue(this, block);
		//StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		//thisSP.setProperty("value", block);
		
		String stim1 = buildOperation();
		String stim2 = buildOperation();
		String stim3 = buildOperation();
		
		//arrange stimuli
		String stimulusOne = getHtmlString(stim1);
		String stimulusTwo = getHtmlString(stim2);
		String stimulusThree = getHtmlString(stim3);

		List<String> stimulus = new ArrayList<String>();
		/*stimulus = Arrays.asList(stimulusOne, stimulusTwo, stimulusThree);*/
		stimulus.add(stimulusOne); stimulus.add(stimulusTwo); stimulus.add(stimulusThree);
		
		if (numOperations > 3) {
			String stim4 = buildOperation();
			String stimulusFour = getHtmlString(stim4);
			stimulus.add(stimulusFour);
		}
		
		if (numOperations > 4) {
			String stim5 = buildOperation();
			String stimulusFive = getHtmlString(stim5);
			stimulus.add(stimulusFive);
		}

		return stimulus;
	}
	
	private int getStartNum() {
		return rnd.nextInt(9) + 1; //will generate random num between 0-8, so add 1 to make it 1-9.
	}
	
	private int getOperand(int currentNum){
		int newOperand;
		if(currentNum == 5) {
			newOperand = rnd.nextInt(4) + 1;
		} else if (currentNum < 5) {
			newOperand = rnd.nextInt(9 - currentNum) + 1;
		} else if (currentNum > 5) {
			newOperand = rnd.nextInt(currentNum - 1) + 1;
		} else {return -1;}
		return newOperand;
	}
	
	private String buildOperation() {
		int answerToOperation = 0;
		String thisStim = "";
		int startNum = getStartNum();
		//update String and answer as we go
		thisStim = thisStim.concat(Integer.toString(startNum));
		answerToOperation += startNum;
		for (int i = 0; i < Level.getLevelProperty().getValueOrDefault(this); i++) {
			int operand = getOperand(answerToOperation);
			//need to see if we are going to add or subtract it
			if (rnd.nextInt(2) == 0) {
				//try add first
				if ((answerToOperation + operand) <= 9) {
					thisStim = thisStim.concat("+" + Integer.toString(operand));
					answerToOperation += operand;
				} else if ((answerToOperation - operand) >= 1){
					thisStim = thisStim.concat("-" + Integer.toString(operand));
					answerToOperation -= operand;
				} else {buildOperation();} //something has gone wrong, start again
			} else {
				//try subtract first
				if ((answerToOperation - operand) >= 1) {
					thisStim = thisStim.concat("-" + Integer.toString(operand));
					answerToOperation -= operand;
				} else if ((answerToOperation + operand) <= 9) {
					thisStim = thisStim.concat("+" + Integer.toString(operand));
					answerToOperation += operand;
				} else {buildOperation();} //something has gone wrong, start again			
			}			
		}
		correctResponse.add(answerToOperation);
		return thisStim;
	}

	private String getHtmlString(String operation) {
		String htmlString = "";
		htmlString = "<html><font color='#101740'>" + operation + "</font></html>";
		return htmlString;
	}

	public void actionTriggered(ActionPanel source, Object actionValue) {
		actionPanel.disableActionPanel();
		processResponse((Integer) actionValue);
	}

	private void endTask() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		overallTrialCounter++;
		blockTrialCounter++;
		
		if (blockTrialCounter == numTrialsPerBlock) {
			blockTrialCounter = 0;
		}
		
		recall_endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		trial_endTime = System.nanoTime();
		
		
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, question, correctResponse);
		boolean success = correctResponse.equals(givenResponse);
		//System.out.println("correctResponse: " + correctResponse);
		//System.out.println("givenResponse: " + givenResponse);
		//System.out.println("success: " + success);
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);

		// set duration time property
		long duration = 0;
		if (trial_endTime > 0) {
			duration = trial_endTime - trial_startTime;
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		//set other RT properties
		long op1rt = ((long) operation_endTimes[0] - (long)operation_startTimes[0]) / 1000000;
		long op2rt = ((long) operation_endTimes[1] - (long)operation_startTimes[1]) / 1000000;
		long op3rt = ((long) operation_endTimes[2] - (long)operation_startTimes[2]) / 1000000;
		long op4rt;
		long op5rt;
		if (numOperations > 3) {
			op4rt = ((long) operation_endTimes[3] - (long)operation_startTimes[3]) / 1000000;
		} else {
			op4rt = 0;
		}
		if (numOperations > 4) {
			op5rt = ((long) operation_endTimes[4] - (long)operation_startTimes[4]) / 1000000;
		} else {
			op5rt = 0;
		}
		
		long recallRT = ((long) recall_endTime - (long) recall_startTime) / 1000000;
		
		op1_rt_property.setValue(this, op1rt);
		op2_rt_property.setValue(this, op2rt);
		op3_rt_property.setValue(this, op3rt);
		op4_rt_property.setValue(this, op4rt);
		op5_rt_property.setValue(this, op5rt);
		recall_rt_property.setValue(this, recallRT);
		loadProperty.setValue(this, Level.getLevelProperty().getValueOrDefault(this));
		trialnoProperty.setValue(this, overallTrialCounter);
		blockProperty.setValue(this, block);

		// create new trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());

		// add all executable properties to the current trial
		DataUtils.storeProperties(currentTrial, this);

		// finish the execution and make sure nobody else already did so
		if (getFinishExecutionLock()) {
			finishExecution();
		}
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
				loadProperty, blockProperty, trialnoProperty, recall_rt_property,
				op1_rt_property, op2_rt_property, op3_rt_property, op4_rt_property, 
				op5_rt_property};
	}


	/**
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list.
	 */
	public void processExecutionPhase(ExecutionContext context) {}
	
	protected void cancelExecutionAWT() {
		timer.cancel();
    }
	
	private void triggerStartExecution(ExecutionContext context) {
		ExecutionStartHandler handler = (ExecutionStartHandler) ElementUtils.findHandlerInStackByType(context, ExecutionStartHandler.class);
		if (handler != null) {
            handler.startExecution(context);
        }	
	}
}
