/*******************************************************************************
* v1.1:
* - JButtons instead of JPanels as colour selectors and grid selectors. Much
* crisper visuals and also deal with the mouse interaction better than the panels
* with a mouse listener.
* - Implemented the mechanics much better, proper waits where necessary such as 
* when displaying memoranda and when showing what the participant has selected for 
* recall.
/*******************************************************************************/

package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stone.javatasks.helperclasses.recallCorsiGrid;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Level;
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
import ch.tatool.core.element.ExecutionStartHandler;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.element.handler.pause.PauseHandlerUtil;
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Runs the colour corsi task used in the authors PhD research. 
 * The user is presented with a 3x3 grid that lights up in a sequence 
 * in a similar manner to the original colour corsi task. However 
 * the task differs in that the squares light up in one of four different 
 * colours and the participant must also remember the colour of each grid.
 
 * @author James Stone
 */
 
public class ColourCorsi extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
ExecutionPhaseListener {
	Logger logger = LoggerFactory.getLogger(ColourCorsi.class);
	//Components we will need to display and manipulate throughout this procedure
	//panels to form grid
	private JButton square_1 = new JButton();
	private JButton square_2 = new JButton();
	private JButton square_3 = new JButton();
	private JButton square_4 = new JButton();
	private JButton square_5 = new JButton();
	private JButton square_6 = new JButton();
	private JButton square_7 = new JButton();
	private JButton square_8 = new JButton();
	private JButton square_9 = new JButton();
	//panels to use as colour selectors in recall phase.
	private JButton blueBox = new JButton();
	private JButton redBox = new JButton();
	private JButton greenBox = new JButton();
	private JButton yellowBox = new JButton();
	//create instances of mousehandlers now so that different functions can add/remove them.
	private HandlerClassGrid mouseHandlerGrid = new HandlerClassGrid();
	private HandlerClassCols colourHandler = new HandlerClassCols();
	private HandlerClassReset mouseHandlerReset = new HandlerClassReset();
	//Use to create array of the the 9 grids to allow efficient code of operations on all grids with loops.
	private ArrayList<JButton> grids;
	private ArrayList<JButton> colBoxes;
	//Setup some components and variables that the class functions can use/alter
	private JPanel colorHolder = new JPanel();
	private JLabel promptText;
	private ArrayList<Integer> correctResponse;
	private ArrayList<Integer> givenResponse;
	private ArrayList<Integer> givenGrids = new ArrayList<Integer>();
	private ArrayList<Integer> givenCols = new ArrayList<Integer>();
	private long startTime;
	private long endTime;
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private int displayDuration = 1000;
	private int currLevel;
	private int startLevel = 1;
	//stimuli based variables.
	private List<Color> colorList = Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW);
	private ArrayList<Integer> lociStimuli; //boxes to highlight
	private ArrayList<Integer> colStimuli; //colours to use to highlight the boxes
	private ArrayList<recallCorsiGrid> recallGridList;
	private int presCounter; //counts how many presented items
	private Color activeColor = null;
	private int trialCounter = 0;
	//define regions container here as multiple functions will need to play with it. 
	private RegionsContainer regionsContainer;
	// additional properties of interest
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty currentLevelProperty = new IntegerProperty("current Level");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	//private IntegerProperty blockProperty = new IntegerProperty("block");
	private JPanel holderPanel;
	private JPanel corsiPanel;
	private JPanel recallReminderPanel;
	private JButton resetRecall;
	private Border defaultButtonBorder;
	private Dimension thisScreenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	private IntegerProperty numTrialsAcrossSessions = new IntegerProperty("numTrialsAcrossSessions");
	private String current_user;
	// executable phases
	public enum Phase {
		INIT, MEMORISATION, RECALL
	}
	private Phase currentPhase;
		
	public ColourCorsi() {
		
		//panels to use in displaying task materials
		holderPanel = new JPanel();
		holderPanel.setBackground(Color.WHITE);
		holderPanel.setLayout(new FlowLayout());
		
		corsiPanel = new JPanel();
		corsiPanel.setBackground(Color.WHITE);
		corsiPanel.setPreferredSize(new Dimension(320,320));
		corsiPanel.setMaximumSize(new Dimension(320,320));
		corsiPanel.setLayout(new GridLayout(3,3,5,5));
		
		recallReminderPanel = new JPanel();
		recallReminderPanel.setBackground(Color.WHITE);
		
		timer = new Timer();
		
		//font to use
		final Font promptFont = new Font("Source Code Pro", 1, 24);
		
		promptText = new JLabel("Presentation");
		promptText.setFont(promptFont);
		promptText.setForeground(Color.LIGHT_GRAY);
		
		//create reset button for use later
		resetRecall = new JButton("Reset");
		resetRecall.setPreferredSize(new Dimension(100,40));
		
		//add the grid to display stims
		addGridAndButtons();
		
		//This will initialise the arrays for the first trial, and on subsequent trials replace
		//previous trials information with clear arrays. 
		correctResponse = new ArrayList<Integer>();
		defaultButtonBorder = blueBox.getBorder();
	}
	
	
	protected void startExecutionAWT() {	
		//initialise environment
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		current_user = context.getExecutionData().getModule().getUserAccount().getName();
		StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		customNamePanel.setProperty("title","User");
		customNamePanel.setProperty("value", current_user);
		
		regionsContainer.setRegionContent(Region.CENTER, holderPanel);
		regionsContainer.setRegionContent(Region.SOUTH, colorHolder);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		
		triggerStartExecution(getExecutionContext());
		
		if (trialCounter == 0) {
            DefaultVisualTimeoutHandler handler2 = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(context, DefaultVisualTimeoutHandler.class);
    		if (handler2 != null) {
    			handler2.startTimeout(context);
    		}
		}
		
		resetGridBackgrounds();
		
		@SuppressWarnings("unchecked")
		List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
		
		//if (trialCounter > 0) {
			//System.out.println("ILSS.get(0).getNumIterations(): " + ILSS.get(0).getNumIterations());
			//System.out.println("ILSS.get(0).getExecutedIterations(): " + ILSS.get(0).getExecutedIterations());
		//}
		
		//execution occurs multiple times in one trial due to the nature of the tatool framework,
		//this block uses the current value of 'currentPhase' to determine what code to execute.
		switch (currentPhase) {
		case INIT:
			runIntPhase();
			break;
		case MEMORISATION:
			runMemoPhase();
			break;
		case RECALL:
			runRecallPhase();
			break;
		}
	}

	
	
	private void runIntPhase() {
		
		setTrialSpecifics();
		
		toggleColourButtons(false);
		
		//clear responses for new trial
		lociStimuli = new ArrayList<Integer>();
		colStimuli = new ArrayList<Integer>();
		givenGrids.clear();
		givenCols.clear();
		presCounter = 0;
		
		//this ensures that the feedback section only displays at the end of a trial
		//rather the end of each execution phase. 
		Result.getResultProperty().setValue(this, null);

		initStimuli();	
		
		//set start time of whole trial property value
		Timing.getStartTimeProperty().setValue(this, new Date());
		
		//move to memorisation phase now that stimuli is generated for the trial
		currentPhase = Phase.MEMORISATION;
		runMemoPhase();
	}

	private void runMemoPhase() {
		//when displaying memoranda the inter-execution time is set to 1000ms. 
		resetGridBackgrounds();		
		
		grids.get(lociStimuli.get(presCounter)).setBackground(colorList.get(colStimuli.get(presCounter)));
		
		//increment the count of presented memoranda
		presCounter++;
		//if all memoranda for trial shown move to recall
		if (presCounter == currLevel) {
			currentPhase = Phase.RECALL;
		}
		
		//display memoranda for specific duration
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (getFinishExecutionLock()) {
							resetGridBackgrounds();
							finishExecution();
						}
					}
				});
			}
		};

		timer.schedule(suspendExecutableTask, displayDuration);		

	}

	
	private void runRecallPhase() {
		resetGridBackgrounds();
		promptText.setText("Recall");
		
		//activate buttons
		toggleColourButtons(true);
		
		//run method to create recall boxes
		if (givenGrids.size() < 1) {
			addRecallBoxes(currLevel);
		}
		
		PauseHandlerUtil.setCurrentInterElementPauseDuration(getExecutionContext(), 0);
		addMouseListeners();
		startTime = System.nanoTime();
	}
	
	private void addMouseListeners() {
		for (int i=0; i < 9; i++) {
			grids.get(i).addActionListener(mouseHandlerGrid);
			resetRecall.addActionListener(mouseHandlerReset);
		}
	}
	
	private void removeMouseListeners() {
		for (int i=0; i < 9; i++) {
			grids.get(i).removeActionListener(mouseHandlerGrid);
			resetRecall.removeActionListener(mouseHandlerReset);;
		}	
	}
	
	//respond to the button clicks
	
	private class HandlerClassReset implements ActionListener {
		public void actionPerformed(ActionEvent event){
			resetRecallGridBackgrounds();
		}
	}
	
	private class HandlerClassCols implements ActionListener {
		public void actionPerformed(ActionEvent event){
			for (JButton j : colBoxes)
				j.setBorder(defaultButtonBorder);
			JButton butClicked = (JButton)(event.getSource());
			butClicked.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
			activeColor = butClicked.getBackground();
		}
	}
	
	private class HandlerClassGrid implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (activeColor != null) {
				removeMouseListeners();
				JButton gridClicked = (JButton)(event.getSource());
				gridClicked.setBackground(activeColor);
				
				givenCols.add(colorList.indexOf(activeColor));
				givenGrids.add(grids.indexOf(gridClicked));
				
				//set recall box to monitor input
				recallGridList.get(givenGrids.size() -1).setOneButtonBackground(grids.indexOf(gridClicked), activeColor);

				gridClicked.revalidate();	

				activeColor = null;
				
				for (JButton j : colBoxes) {
					j.setBorder(defaultButtonBorder);			
				}
				colorHolder.revalidate();

				
				suspendExecutableTask = new TimerTask() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (getFinishExecutionLock()) {
									if (givenGrids.size() == currLevel){
										endTime = System.nanoTime();
										promptText.setText("Presentation");
										processProperties();
										currentPhase = Phase.INIT;
										resetGridBackgrounds();
										finishExecution();
									} else {
										finishExecution();
									}
								}
							}
						});
					}
				};
	
				timer.schedule(suspendExecutableTask, 500);				
			}
		}
	}

	private void initStimuli() {

		//System.out.println("running init stimuli");
		
		lociStimuli = new ArrayList<Integer>();
		colStimuli = new ArrayList<Integer>();
		
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) {
			nums.add(i);
		}
		
		Random randomGenerator = new Random();
		
		for (int i = 0; i < currLevel; i++) {
			
			if (currLevel > 9) {
				int colINT = randomGenerator.nextInt(4);
				int lociINT = randomGenerator.nextInt(9);
				lociStimuli.add(lociINT);
				colStimuli.add(colINT);
			} else {
				int colINT = randomGenerator.nextInt(4);
				int numIndice = randomGenerator.nextInt(nums.size());
				int lociINT = nums.get(numIndice);
				nums.remove(numIndice);
				
				lociStimuli.add(lociINT);
				colStimuli.add(colINT);
			}
			
		}
		//System.out.println("lociStimuli: " + lociStimuli);
	}

	private void addGridAndButtons(){

		JPanel holdingContainer;
		holdingContainer = new JPanel();
		holdingContainer.setBackground(Color.WHITE);
		holdingContainer.setLayout(new BoxLayout(holdingContainer, BoxLayout.PAGE_AXIS));
		//holdingContainer.setPreferredSize(new Dimension(thisScreenSize.width, thisScreenSize.height * 2/3));
		
		grids = new ArrayList<JButton>();
		grids.add(square_1); grids.add(square_2); grids.add(square_3); 
		grids.add(square_4); grids.add(square_5); grids.add(square_6); 
		grids.add(square_7); grids.add(square_8); grids.add(square_9);
		colBoxes = new ArrayList<JButton>();
		colBoxes.add(blueBox); colBoxes.add(redBox); colBoxes.add(greenBox); colBoxes.add(yellowBox);
		
		for (int i = 0; i < 9; i++) {
			grids.get(i).setPreferredSize(new Dimension(100,100));
			grids.get(i).setBackground(Color.LIGHT_GRAY);
			corsiPanel.add(grids.get(i));
		}
		
		promptText.setPreferredSize(new Dimension(thisScreenSize.width, 50));
		
		promptText.setAlignmentX(Component.CENTER_ALIGNMENT);
		corsiPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		holdingContainer.add(promptText);
		//holdingContainer.add(Box.createVerticalGlue());
		holdingContainer.add(corsiPanel);
		//holdingContainer.add(Box.createVerticalGlue());

		holderPanel.add(holdingContainer);
		holderPanel.add(recallReminderPanel);
		
		colorHolder = new JPanel();
		colorHolder.setLayout(new FlowLayout());
		
		for (int i = 0; i < 4; i++) {
			colBoxes.get(i).setBackground(colorList.get(i));
			colBoxes.get(i).setPreferredSize(new Dimension(60,60));
			colBoxes.get(i).addActionListener(colourHandler);
			colorHolder.add(colBoxes.get(i));
		}
		
		colorHolder.revalidate();
	}
	
	private void addRecallBoxes(int numBoxes) {
		
		recallGridList = new ArrayList<recallCorsiGrid>();
		
		recallCorsiGrid recGrid_1 = new recallCorsiGrid();
		recallGridList.add(recGrid_1);
		
		if (numBoxes > 1) {recallCorsiGrid recGrid_2 = new recallCorsiGrid(); recallGridList.add(recGrid_2);}
		if (numBoxes > 2) {recallCorsiGrid recGrid_3 = new recallCorsiGrid(); recallGridList.add(recGrid_3);}
		if (numBoxes > 3) {recallCorsiGrid recGrid_4 = new recallCorsiGrid(); recallGridList.add(recGrid_4);}
		if (numBoxes > 4) {recallCorsiGrid recGrid_5 = new recallCorsiGrid(); recallGridList.add(recGrid_5);}
		if (numBoxes > 5) {recallCorsiGrid recGrid_6 = new recallCorsiGrid(); recallGridList.add(recGrid_6);}
		if (numBoxes > 6) {recallCorsiGrid recGrid_7 = new recallCorsiGrid(); recallGridList.add(recGrid_7);}
		if (numBoxes > 7) {recallCorsiGrid recGrid_8 = new recallCorsiGrid(); recallGridList.add(recGrid_8);}
		if (numBoxes > 8) {recallCorsiGrid recGrid_9 = new recallCorsiGrid(); recallGridList.add(recGrid_9);}
		if (numBoxes > 9) {recallCorsiGrid recGrid_10 = new recallCorsiGrid(); recallGridList.add(recGrid_10);}
		if (numBoxes > 10) {recallCorsiGrid recGrid_11 = new recallCorsiGrid(); recallGridList.add(recGrid_11);}
		if (numBoxes > 11) {recallCorsiGrid recGrid_12 = new recallCorsiGrid(); recallGridList.add(recGrid_12);}
		if (numBoxes > 12) {recallCorsiGrid recGrid_13 = new recallCorsiGrid(); recallGridList.add(recGrid_13);}
		if (numBoxes > 13) {recallCorsiGrid recGrid_14 = new recallCorsiGrid(); recallGridList.add(recGrid_14);}
		if (numBoxes > 14) {recallCorsiGrid recGrid_15 = new recallCorsiGrid(); recallGridList.add(recGrid_15);}
		if (numBoxes > 15) {recallCorsiGrid recGrid_16 = new recallCorsiGrid(); recallGridList.add(recGrid_16);}
		
	
		for (int j = 0; j < recallGridList.size(); j++) {
			recallGridList.get(j).setBorderText(Integer.toString(j + 1));
			recallReminderPanel.add(recallGridList.get(j));
		}
		
		recallReminderPanel.add(resetRecall);
	}

	
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
		}
	}
	
	private void resetGridBackgrounds(){
		for (int i = 0; i < 9; i++) {
			grids.get(i).setBackground(Color.LIGHT_GRAY);
			grids.get(i).revalidate();
			refreshRegion(Region.CENTER);
		}
	}
	
	private void resetRecallGridBackgrounds() {
		for (int k = 0; k < givenGrids.size(); k++) {
			recallGridList.get(k).setOneButtonBackground(givenGrids.get(k), Color.LIGHT_GRAY);
		}
		givenGrids.clear();
		givenCols.clear();
	}
	
	private void toggleColourButtons(boolean b) {
		for (JButton j : colBoxes) {
			j.setEnabled(b);
		}
	}
	
	private void setTrialSpecifics() {
		//set block property here as this method is called at start of any given trial
		@SuppressWarnings("unchecked")
		List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
		//int block = ILSS.get(2).getExecutedIterations();
		//blockProperty.setValue(this, block);
		//StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		//thisSP.setProperty("value", block);
		
		trialCounter++;
		
		//set level
		currLevel = Level.getLevelProperty().getValueOrDefault(this);
		
		//update custom status panel
		//StatusPanel customLevelPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		//customLevelPanel.setProperty("title", "Level");
		//customLevelPanel.setProperty("value", currLevel);
		
		//set num iterations to execute this trial
		ILSS.get(0).setNumIterations(currLevel * 2);
	}
	
	/**
	 * Update the status panel from within the executable.
	 * 
	 * @param value to set the status panel to
	 */
	private void changeStatusPanelOutcome(Boolean value) {
        StatusPanel panelFeedback = StatusRegionUtil.getStatusPanel(StatusPanel.STATUS_PANEL_OUTCOME);
        if (panelFeedback != null) {
        	if (value == null) {
        		panelFeedback.reset();
        	} else {
        		panelFeedback.setProperty(StatusPanel.PROPERTY_VALUE, value);
        	}
        } 
	}
	
	private void processProperties() {
		
		//good place to remove recall boxes ready for next trial
		for (int j = 0; j < recallGridList.size(); j++) {
			recallReminderPanel.remove(recallGridList.get(j));
			recallReminderPanel.remove(resetRecall);
		}
		
		Timing.getEndTimeProperty().setValue(this, new Date());
		
		PauseHandlerUtil.setCurrentInterElementPauseDuration(getExecutionContext(), 1000);
		
		//set overall trial number property to persist over sessions
		int numTrials = numTrialsAcrossSessions.getValue(getExecutionContext().getExecutionData().getModule(), this, 0);
		numTrialsAcrossSessions.setValue(getExecutionContext().getExecutionData().getModule(), this, numTrials + 1);
		
		//System.out.println("numTrialsAcrossSessions property value: " + numTrialsAcrossSessions.getValue(getExecutionContext().getExecutionData().getModule(), this, 0));

		boolean success = (lociStimuli.equals(givenGrids) && colStimuli.equals(givenCols));
		correctResponse = lociStimuli;
		correctResponse.addAll(colStimuli);
		givenResponse = new ArrayList<Integer>();
		givenResponse.addAll(givenGrids);
		givenResponse.addAll(givenCols);
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, null, correctResponse);
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		
		loadProperty.setValue(this, currLevel);
		trialNoProperty.setValue(this, trialCounter);
		
		// change feedback status panel
		changeStatusPanelOutcome(success);

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
				loadProperty, trialNoProperty, currentLevelProperty };
	}
	
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		holderPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}
	
	public int getStartLevel() {
		return startLevel;
	}

	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}
	
	private void triggerStartExecution(ExecutionContext context) {
		ExecutionStartHandler handler = (ExecutionStartHandler) ElementUtils.findHandlerInStackByType(context, ExecutionStartHandler.class);
		if (handler != null) {
            handler.startExecution(context); 
        }	
	}
		
}