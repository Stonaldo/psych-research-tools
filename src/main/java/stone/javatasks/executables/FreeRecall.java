package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;


/**
 * Free recall task, will present stims one at a time to participants during pres phase.
 * At recall phase the participant will have a certain amount of time to input the items
 * they can recall from the list they were presented.
 * @author James Stone
 *
 */

public class FreeRecall extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder {
	
	Logger logger = LoggerFactory.getLogger(FreeRecall.class);
	
	private IntegerProperty numRecalledProperty = new IntegerProperty("numRecalled");
	
	private RegionsContainer regionsContainer;
	
	//panels//
	private CenteredTextPanel displayPanel;
	private InputActionPanel responsePanel;
	
	//timing//
	private Timer timer; //to use at recall phase so that we can terminate execution after time limit//
	private TimerTask suspendExecutableTask; //
	private TimerTask isiBlankTask; 
	private TimerTask recallTimerTask;
	
	private int presentationDuration = 0; //set from XML
	private int isi = 0; //set from XML
	private String WordPoolFileName = ""; //set from XML
	private int recallTimeLimit = 0; //set from XML
	private int numWordsToPresent = 0; //set from XML//
	
	
	private ArrayList<String> wordPool; //build from txt file//
	private ArrayList<String> wordsPresented; //words selected for this trial//
	private ArrayList<String> wordsGiven; //words submitted in recall phase of this trial//
	
	private int trialCounter = 0;
	private int presCounter = 0;
	private StringBuilder recallScreenText;
	private String htmlOpen = "<HTML>";
	private String htmlClose = "</HTML>";
	private Random rand;
	
	final Color fontColor = new Color(0,51,102);
	private String current_user;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	
	/*
	 * constructor
	 */
	public FreeRecall() {
		displayPanel = new CenteredTextPanel();
		responsePanel = new InputActionPanel();
		
		responsePanel.setTextDocument(20, InputActionPanel.FORMAT_ALL);
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
		
		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContent(Region.SOUTH, responsePanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER,  true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		runTrial();
	}
	
	private void runTrial(){
		
		trialCounter++;
		//update custom status panel
		StatusPanel customLevelPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom2");
		customLevelPanel.setProperty("title", "Trial");
		customLevelPanel.setProperty("value", trialCounter);		
		
		wordsPresented = generateTrialStimuli();
		
		setStimsToDisplay();
	}
	
	private ArrayList<String> generateTrialStimuli(){
		if (trialCounter == 1) { //if first trial then we must gen the word pool to select from.
			generateWordPool();
		}
		ArrayList<String> words = new ArrayList<String>();
		//reset wordsGiven and presCounter//
		wordsGiven = new ArrayList<String>();
		presCounter = 0;
		
		for (int i=0; i < numWordsToPresent; i++) {
			int wordIndex = rand.nextInt(wordPool.size());
			//add the word to the current list and then remove from wordPool so it doesn't get used again//
			words.add(wordPool.get(wordIndex));
			wordPool.remove(wordIndex);			
		}
		
		return words;
	}
	
	private void generateWordPool() {
		Scanner s;
		wordPool = new ArrayList<String>();
		String file_location = "/stimuli/word_lists/" + WordPoolFileName;
		
		//s = new Scanner(new File(file_location));
		s = new Scanner(new InputStreamReader(this.getClass().getResourceAsStream(file_location)));
		while (s.hasNext()) {
			wordPool.add(s.next());
		}
		s.close();
	}
	
	private void setStimsToDisplay() {
		String stimulus = wordsPresented.get(presCounter);
		
		displayPanel.setTextFont(treb);
		displayPanel.setTextColor(fontColor);
		displayPanel.setText(stimulus);
		
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						//enter code
						presCounter++;
						displayPanel.setText("");
						refreshRegion(Region.CENTER);
						
						isiBlankTask = new TimerTask() {
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										if (presCounter == numWordsToPresent) {
											//then move on to recall
											recallPhase();
										} else {
											//show another word
											setStimsToDisplay();
										}
									}
								});
							}
						};
						
						timer.schedule(isiBlankTask, isi);
					}
				});
			}
		};
		
		
		timer.schedule(suspendExecutableTask, presentationDuration);
	}
	
	
	private void recallPhase() {
		
		recallScreenText = new StringBuilder();
		recallScreenText.append("Enter the words you can remember:");
		displayPanel.setText(recallScreenText.toString());
		responsePanel.clearTextField();
		
		recallTimerTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						//enter code to end task.
						regionsContainer.setRegionContentVisibility(Region.CENTER, false);
						regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
						processProperties();
						if (getFinishExecutionLock()) {
							finishExecution();
						}
					}
				});
			}
		};
		
		responsePanel.enableActionPanel();
		
		timer.schedule(recallTimerTask, recallTimeLimit);
	}
	
	
	public void actionTriggered(ActionPanel source, Object actionValue) {
		System.out.println("(String) actionValue: " + (String) actionValue);
		wordsGiven.add((String) actionValue);
		responsePanel.clearTextField();
		recallScreenText.append("<br>" + (String) actionValue);
		displayPanel.setText(htmlOpen + recallScreenText.toString() + htmlClose);
	}
	
	
	private void processProperties() {
		//calculate number successfully recalled
		int numRecalled = calcNumRecalled();
		numRecalledProperty.setValue(this, numRecalled);
		
		Question.getQuestionProperty().setValue(this, String.valueOf(wordsPresented));
		Question.getResponseProperty().setValue(this, wordsGiven);
		
		Result.getResultProperty().setValue(this, true);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		
		DataUtils.storeProperties(currentTrial, this);
	}
	
	private int calcNumRecalled() {
		int j = 0;
		for (int i=0; i < wordsGiven.size(); i++) {
			if (wordsPresented.contains(wordsGiven.get(i))) {
				j++;
			}
		}
		return j;
	}
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 */
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getResponseProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				numRecalledProperty };
	}

	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		regionsContainer.setRegionContentVisibility(reg, true);
	}		
	
	public void setpresentationDuration(int presentationDuration) {
		this.presentationDuration = presentationDuration;
	}
	public int getpresentationDuration() {
		return presentationDuration;
	}
	
	public void setisi(int isi) {
		this.isi = isi;
	}
	public int getisi(){
		return isi;
	}
	
	public void setWordPoolFileName(String WordPoolFileName) {
		this.WordPoolFileName = WordPoolFileName;
	}
	public String getWordPoolFileName() {
		return WordPoolFileName;
	}
	
	public void setrecallTimeLimit(int recallTimeLimit) {
		this.recallTimeLimit = recallTimeLimit;
	}
	public int getrecallTimeLimit() {
		return recallTimeLimit;
	}
	public void setnumWordsToPresent(int numWordsToPresent) {
		this.numWordsToPresent = numWordsToPresent;
	}
	public int getnumWordsToPresent() {
		return numWordsToPresent;
	}
	

	
	
	
	
	
	
	
	
	
	

}



























