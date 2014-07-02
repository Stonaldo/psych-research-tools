package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

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
import ch.tatool.core.display.swing.action.InputActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
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
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Display a sequence of strings that the participant must remember and then 
 * recall in the correct order at the end.
 * @author James Stone
 *
 */

public class WordDigitSpan extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder, ExecutionPhaseListener {
	
	Logger logger = LoggerFactory.getLogger(WordDigitSpan.class);
	
	private RegionsContainer regionsContainer;
	private int complexSpan;
	private String current_user;	
	private int StimuliType = 0; //set this using the XML, 1 for digits, 2 for words.//
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	
	//phases of task//
	public enum Phase {
		INIT, MEMO, RECALL
	}
	
	private Phase currentPhase;
	
	//properties of interest//
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	
	//panels//
	private CenteredTextPanel displayPanel;
	private InputActionPanel responsePanel;
	
	//timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private TimerTask startRecallTask;
	private int displayDuration = 1000; //duration string should be displayed in ms//
	private static int interResponseDuration = 1000; //blank screen between recalls//
	
	//stimuli//
	private int[] numbers; //if digit span//
	private String[] words; //if word span//
	private ArrayList<String> wordBank = new ArrayList<String>(); //method will fill this arraylist with words from an external file//
	private String WordBankFileName = "";
	private Random rand;
	
	private int[] simpleSpans = {2,2,2,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6};
	private int[] complexSpans = {2,2,2,2,2,2,3,3,3,4,4,4,5,5,5};
	private ArrayList<Integer> spansList = new ArrayList<Integer>();
	
	private int trialCounter = 0;
	private int memCounter; //counts memoranda presented per trial//
	private int respCounter; //counts responses given//
	private int correctResponseDigits; //if digit span//
	private String correctResponseWord; //if word span//
	private int givenResponseDigits; //if digit span//
	private String givenResponseWord; //if word span//
	
	private long startTime;
	private long endTime;
	
	/*
	 * constructor
	 */
	public WordDigitSpan() {
		displayPanel = new CenteredTextPanel();
		displayPanel.setTextFont(treb);
		displayPanel.setTextColor(fontColor);
		responsePanel = new InputActionPanel();
		if (StimuliType == 1) {
			responsePanel.setTextDocument(2, InputActionPanel.FORMAT_ONLY_DIGITS);
		} else if (StimuliType == 2) {
			//need to add code for words//
			responsePanel.setTextDocument(20, InputActionPanel.FORMAT_ALL);
		} else {
			//add exception handling//
		}
		
		responsePanel.addActionPanelListener(this);
		
		rand = new Random();
		timer = new Timer();

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
		generateStimuli();
		
		//reset stim counter//
		memCounter = 0;
		respCounter = 0;
		
		Result.getResultProperty().setValue(this,null);
		
		//start memo phase//
		currentPhase = Phase.MEMO;
		startMemoPhase();
	}
	
	private void startMemoPhase() {
		
		String stimulus = "";
		
		if (StimuliType == 1) {
			stimulus = String.valueOf(numbers[memCounter]);
		} else if (StimuliType == 2) {
			stimulus = words[memCounter];
		}
		
		displayPanel.setTextSize(120);
		displayPanel.setText(stimulus);
		
		memCounter++; //incremement memCounter as an additional digit/word has been shown//
		
		//if this is is last stim then change to recall phase//
		
		if (StimuliType == 1) {
			if (memCounter == numbers.length) {
				currentPhase = Phase.RECALL;
			}
		} else if (StimuliType == 2) {
			if (memCounter == words.length) {
				currentPhase = Phase.RECALL;
			}
		}
		
		//suspend for specified amount of time, this is presentation time of each digit/word//
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						suspendExecutable(); // suspend task
					}
				});
			}
		};

		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		timer.schedule(suspendExecutableTask, displayDuration);		
	}
	
	private void startRecallPhase() {
		StringBuilder text = new StringBuilder();
		if (StimuliType == 1) {
			text.append("Number ");
		} else if (StimuliType == 2) {
			text.append("word ");
		}
		text.append(respCounter + 1);
		text.append(": ");
		displayPanel.setTextSize(60);
		displayPanel.setText(text.toString());
		
		responsePanel.clearTextField();
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContent(Region.SOUTH, responsePanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);

		responsePanel.enableActionPanel();		
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
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.SUSPENDED);
		}
		// finish the execution and make sure nothing else already did so
		if (getFinishExecutionLock()) {
			finishExecution();
		}
	}
	
	//listens to the user input and responds accordingly//
	public void actionTriggered(ActionPanel source, Object actionValue) {
		if (StimuliType == 1) {
			try {
				givenResponseDigits = Integer.valueOf((String) actionValue);
			} catch (NumberFormatException e) {
				givenResponseDigits = 0;
			}
		} else if (StimuliType == 2) {
				givenResponseWord = (String) actionValue;
		}
		
		//debugging purposes
		List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
		System.out.println("executed iterations: " + ILSS.get(0).getExecutedIterations());

		
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());

		responsePanel.disableActionPanel();
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);

		// process the properties for this trial
		processProperties();
		
		//decide whether we have to display another recall page or if we can finish executable//
		boolean displayAnother = false;
		switch(StimuliType) {
		case 1:
			if (respCounter < numbers.length -1) {displayAnother = true;}
			break;
		case 2:
			if (respCounter < words.length -1) {displayAnother = true;}
			break;
		}
		
		if (displayAnother) {
			respCounter++;
			//show next recall entry after a pause
			startRecallTask = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							//changeStatusPanelOutcome(null);
							startRecallPhase();
						}
					});
				}
			};
			
			regionsContainer.setRegionContentVisibility(Region.CENTER, false);
			timer.schedule(startRecallTask, interResponseDuration);
			
		} else {
			currentPhase = Phase.INIT;
			trialCounter++;
			
			if (getFinishExecutionLock()) {
				finishExecution();
			}
		}
		
	}
	
	private void processProperties() {
		switch (StimuliType) {
		case 1:
			int stimulus_num = numbers[respCounter];
			correctResponseDigits = Integer.valueOf(stimulus_num);
			Question.getResponseProperty().setValue(this, givenResponseDigits);
			Question.setQuestionAnswer(this, String.valueOf(stimulus_num), correctResponseDigits);
			break;
		
		case 2:
			String stimulus_word = words[respCounter];
			correctResponseWord = (String) stimulus_word;
			Question.getResponseProperty().setValue(this, givenResponseWord);
			Question.setQuestionAnswer(this, String.valueOf(stimulus_word), correctResponseWord);
			break;
		}
		
		Boolean success = null;
		if (StimuliType == 1) {
			success = correctResponseDigits == givenResponseDigits;
			loadProperty.setValue(this, numbers.length);
		} else if (StimuliType == 2) {
			success = correctResponseWord.equals(givenResponseWord);
			loadProperty.setValue(this, words.length);
		}
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		switch(StimuliType){
		case 1:
			if (respCounter < (numbers.length - 1)) {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
			} else {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			}
			break;
		
		case 2:
			if (respCounter < (words.length - 1)) {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
			} else {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			}
		}

		
		trialNoProperty.setValue(this, trialCounter + 1);
		
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
	
	//update status panel//
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

	/**
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list and set the executable phase
	 * to INIT.
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
		}
	}
	
	protected void cancelExecutionAWT() {
		timer.cancel();
		currentPhase = Phase.INIT;
    }
	
	public int getStimuliType() {
		return StimuliType;
	}
	
	public void setStimuliType(int StimuliType) {
		this.StimuliType = StimuliType;
	}
	
	public String getWordBankFileName() {
		return WordBankFileName;
	}
	
	public void setWordBankFileName(String WordBankFileName) {
		this.WordBankFileName = WordBankFileName;
	}
	
	public int getcomplexSpan() {
		return this.complexSpan;
	}
	public void setcomplexSpan(int c) {
		this.complexSpan = c;
	}
	
	private ArrayList<String> generateWordBank(String filename) {
		
		Scanner s;
		ArrayList<String> list = new ArrayList<String>();
		String file_location = "src/main/resources/stimuli/word_lists/" + filename;
		
		try {
			s = new Scanner(new File(file_location));
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("list: " + list);
		
		return list;
	}
	
	private void generateStimuli() {
		
		//if first trial then generate trial spans
		if (trialCounter == 0) {
			//compile ArrayList for spans//
			System.out.println("complexSpan: " + complexSpan);
			System.out.println("trialCounter: " + trialCounter);
			System.out.println("stimuliType: " + StimuliType);
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
		
		//take random int from spans to use as list length in this trial, then remove that element from spans//
		//int spanIndice = rand.nextInt(spansList.size());
		int thisTrialSpan = spansList.get(0);
		spansList.remove(0);
		
		switch(StimuliType){
		case 1: //need digits to use as stimuli//
			numbers = new int[thisTrialSpan];
			List<Integer> numberList = new ArrayList<Integer>();
			for (int i = 0; i < numbers.length; i++) {
				int tmpNumber = 10 + rand.nextInt(90);
				if (!numberList.contains(tmpNumber)) {
					numbers[i] = tmpNumber;
					numberList.add(tmpNumber);
				} else {
					i--;
				}
			}
			System.out.println("spansList: " + spansList);
			break;
		case 2: //need words to use as stimuli//	
			//if first trial then need to create word bank
			if (trialCounter == 0) {
				wordBank = generateWordBank(WordBankFileName);
			}
			//build word list for this trial//
			words = new String[thisTrialSpan];
			for (int j = 0; j < words.length; j++) {
				int index = rand.nextInt(wordBank.size());
				words[j] = wordBank.get(index);
				wordBank.remove(index); //remove each word as it is used//
			}

		}
	}
}