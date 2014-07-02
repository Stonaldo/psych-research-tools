/*******************************************************************************
 * Executable for a mental arithmetic task
 * @author James Stone
 */

package stone.javatasks.executables;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.LongProperty;
import ch.tatool.core.data.Misc;
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
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

public class MentalArithmetic extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
ExecutionPhaseListener, ActionPanelListener {
	
	Logger logger = LoggerFactory.getLogger(MentalArithmetic.class);
	
	//needed
	private IntegerProperty blockProperty = new IntegerProperty("block");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	private LongProperty trial_responseTime = new LongProperty("trial_rt");
	
	private RegionsContainer regionsContainer;
	private KeyActionPanel actionPanel;
	private CenteredTextPanel questionPanel;
	private int trialCounter = 0;
	private String block;
	private Timer timer;
	private String correctResponse;
	private String thisTrialResponse = "";
	private String thisTrialStim;
	private String rawThisTrialStim;
	
	private String thisTrialOperation;
	private static String STIMULI_PATH = "/stimuli/";
	private String stimuliFile;
	private List<String[]> stimuliList;
	
	private String[] numbers = {"0","1","2","3","4","5","6","7","8","9"};
	private ArrayList<String> nums;
	
	private long trial_startTime;
	private long trial_endTime;
	
	private final Font numFont = new Font("Open Sans",1,36);
	
	public MentalArithmetic(){
		System.out.println("running constructor");
		
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){}	
		
		//initialise the panel to add the grid to.
		questionPanel = new CenteredTextPanel();
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		timer = new Timer();	
		
		nums = new ArrayList<String>();
		for (String x : numbers) {
			nums.add(x);
		}
		

	}
	
	
	protected void startExecutionAWT() {	
		//initialise environment
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		thisSP.setProperty("value", block);
		
		initStimulus();
		
		activateActionPanel();

		regionsContainer.setRegionContent(Region.CENTER, questionPanel);
		refreshRegion(Region.CENTER);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		
		if (trialCounter == 0) {
			initDVTH();
		}
		
		beginTrial();
		
	}	
	
	private void beginTrial() {
		questionPanel.setFont(numFont);
		questionPanel.setTextSize(150);
		questionPanel.setText(thisTrialStim);
		trial_startTime = System.nanoTime();
		refreshRegion(Region.CENTER);
	}
	
	private void initDVTH() {
		DefaultVisualTimeoutHandler thisDVTH = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultVisualTimeoutHandler.class);
		thisDVTH.startTimeout(getExecutionContext());
		StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		thisSP.setProperty("value", block);
	}
	
	private void clearStimuli() {}
	
	public void actionTriggered(ActionPanel source, Object actionValue) {
		if (nums.contains((String) actionValue)) {
			refreshThisTrialStim((String) actionValue);
		} else if (actionValue == "delete") {
			refreshThisTrialStim();
		} else if (actionValue == "submitted") {
			actionPanel.disableActionPanel();
			trial_endTime = System.nanoTime();
			endTask();
		}
	}	
	
	private void activateActionPanel() {
		actionPanel.addKey(KeyEvent.VK_0, "0", "0");
		actionPanel.addKey(KeyEvent.VK_1, "1", "1");
		actionPanel.addKey(KeyEvent.VK_2, "2", "2");
		actionPanel.addKey(KeyEvent.VK_3, "3", "3");
		actionPanel.addKey(KeyEvent.VK_4, "4", "4");
		actionPanel.addKey(KeyEvent.VK_5, "5", "5");
		actionPanel.addKey(KeyEvent.VK_6, "6", "6");
		actionPanel.addKey(KeyEvent.VK_7, "7", "7");
		actionPanel.addKey(KeyEvent.VK_8, "8", "8");
		actionPanel.addKey(KeyEvent.VK_9, "9", "9");
		actionPanel.addKey(KeyEvent.VK_BACK_SPACE, "delete", "delete");
		actionPanel.addKey(KeyEvent.VK_ENTER, "Submit Answer", "submitted");
		
		actionPanel.enableActionPanel();
	}
	
	private void initStimulus() {
		if (stimuliList.size() == 0) {
			//initTimer();
			readInputData();
			//start timeout
		}
		
		String[] currentItem = stimuliList.get(trialCounter);
		
		thisTrialResponse = "";
		correctResponse = currentItem[1];
		rawThisTrialStim = currentItem[0] + " = " ;
		thisTrialStim = getHtmlString(rawThisTrialStim);
	}
	
	/*
	private void initTimer() {
		StatusPanel thisTimerPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("timer");
		thisTimerPanel.setProperty("minValue", 0);
		thisTimerPanel.setProperty("maxValue", 1000);
		thisTimerPanel.setProperty("value", 500);
		thisTimerPanel.setEnabled(true);
	}
	*/
	
	private void refreshThisTrialStim(String x) {
		thisTrialResponse += x;
		thisTrialStim = getHtmlString(rawThisTrialStim + thisTrialResponse);
		refreshRegion(Region.CENTER);
	}
	
	private void refreshThisTrialStim() {
		if (thisTrialResponse.length() == 0) {
		} else {
			thisTrialResponse = thisTrialResponse.substring(0, thisTrialResponse.length() - 1);
			thisTrialStim = getHtmlString(rawThisTrialStim + thisTrialResponse);
			refreshRegion(Region.CENTER);
		}
	}
	
	private String getHtmlString(String operation) {
		String htmlString = "";
		htmlString = "<html><font color='#101740'>" + operation + "</font></html>";
		return htmlString;
	}
	
	private void refreshRegion(Region reg) {
		questionPanel.setText(thisTrialStim);
		regionsContainer.setRegionContentVisibility(reg, false);
		questionPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}	
	
	/**
	 * Reads the stimuli file CSV and stores the stimuli in an ArrayList
	 */
	private void readInputData() {
		stimuliList = new ArrayList<String[]>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(new InputStreamReader(this.getClass()
					.getResourceAsStream(STIMULI_PATH + stimuliFile),
					"ISO-8859-1"), '\t');
		} catch (UnsupportedEncodingException e1) {
			logger.error(e1.getMessage(), e1);
		}

		try {
			stimuliList = reader.readAll();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			readInputData();
		}
	}
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 */
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty(),
				trialNoProperty, trial_responseTime};
	}
	
	protected void cancelExecutionAWT() {
		timer.cancel();
    }

	/**
	 * Allows to set the stimuli file as a property in the module xml file.
	 * 
	 * @param stimuliFile
	 */
	public void setStimuliFile(String stimuliFile) {
		this.stimuliFile = stimuliFile;
	}

	public String getStimuliFile() {
		return stimuliFile;
	}
	
	/**
	 * Allows to set the block as a property in the module xml file.
	 * 
	 * @param block
	 */
	
	public void setBlock(String block) {
		this.block = block;
	}
	
	public String getBlock(String block) {
		return block;
	}
	
	private void endTask() {
		processProperties();
		trialCounter += 1;
		if (getFinishExecutionLock()) {
			finishExecution();
		}		
	}
	
	private void processProperties() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		Question.getResponseProperty().setValue(this, thisTrialResponse);
		Question.setQuestionAnswer(this, rawThisTrialStim, correctResponse);
		boolean success = correctResponse.equals(thisTrialResponse);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);

		// set duration time property
		long duration = 0;
		if (trial_endTime > 0) {
			duration = trial_endTime - trial_startTime;
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		trial_responseTime.setValue(this, ms);
		
		trialNoProperty.setValue(this, trialCounter);

		// create new trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());

		// add all executable properties to the current trial
		DataUtils.storeProperties(currentTrial, this);		
	}	
}