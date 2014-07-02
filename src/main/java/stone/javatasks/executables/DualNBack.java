/*******************************************************************************
 * Executable that runs the dual nback task as described by Jaeggi et al. (2008).
 * @author James Stone
 */

package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Level;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.StringProperty;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.action.ActionPanel;
import ch.tatool.core.display.swing.action.ActionPanelListener;
import ch.tatool.core.display.swing.action.InputActionPanel;
import ch.tatool.core.display.swing.action.KeyActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.element.handler.score.DefaultPointsAndLevelHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

public class DualNBack extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
ExecutionPhaseListener, ActionPanelListener {
	
	Logger logger = LoggerFactory.getLogger(DualNBack.class);
	
	//needed
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty currentLevelProperty = new IntegerProperty("current Level");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	private StringProperty typeProperty = new StringProperty("type");
	private IntegerProperty blockProperty = new IntegerProperty("blockno");
	private IntegerProperty thisGridProperty = new IntegerProperty("thisGrid");
	private IntegerProperty thisLetterProperty = new IntegerProperty("thisLetter");
	
	private int block;
	private int trialsRemainingInBlock;
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	
	private RegionsContainer regionsContainer;
	private KeyActionPanel actionPanel;
	//private InputActionPanel responsePanel;
	private int trialCounter = 0;
	private int startLevel;
	private Timer timer;
	private int displayDuration = 500;
	private int trialDuration = 3000;
	private Response correctResponse;
	
	private ArrayList<Integer> stimuli;
	private String type = "";
	private ArrayList<Integer> stimuliListGrids;
	private ArrayList<Integer> stimuliListAudio;
	
	private int currentSpatial;
	private int currentAuditory;
	private int targetSpatial;
	private int targetAuditory;
	private Response correctSpatialAction;
	private Response correctAuditoryAction;
	private Response givenSpatialAction;
	private Response givenAuditoryAction;
	private int gridErrors;
	private int letterErrors;
	private boolean startLevelNeeded = true;
	private boolean thisTrialAddExecution = false;
	
	private enum Response {
		noMatch, Match
	}

	private CenteredTextPanel gridGoesHere;
	
	//For grid GUI
	private JButton square_1 = new JButton();
	private JButton square_2 = new JButton();
	private JButton square_3 = new JButton();
	private JButton square_4 = new JButton();
	private JButton square_5 = new JButton();
	private JButton square_6 = new JButton();
	private JButton square_7 = new JButton();
	private JButton square_8 = new JButton();
	private JButton square_9 = new JButton();	
	private ArrayList<JButton> grids;
	
	//for audio stims
	private String C = "/letters/c.wav";
	private String H = "/letters/h.wav";
	private String K = "/letters/k.wav";
	private String L = "/letters/l.wav";
	private String Q = "/letters/q.wav";
	private String R = "/letters/r.wav";
	private String S = "/letters/s.wav";
	private String T = "/letters/t.wav";
	private ArrayList<String> audioFiles;
	private URL songPath;
	private AudioInputStream audioIn;
	private Clip letterToPlay;
	
	public DualNBack(){
		System.out.println("running constructor");
		
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){}	
		
		//initialise the panel to add the grid to.
		gridGoesHere = new CenteredTextPanel();
		timer = new Timer();		
		//add the grid to display stims
		addGridAndButtons();
		
		activateActionPanel();
		
		//initialise or reset the stored responses
		resetTrialSpecifics();	
	}
	
	/*protected void selectStartLevel() {
		System.out.println("running selectStartLevel");
		startLevelNeeded = false;
		thisTrialAddExecution = true;
		CenteredTextPanel sentencePanel = new CenteredTextPanel();
		sentencePanel.setTextFont(treb);
		sentencePanel.setTextColor(fontColor);
		sentencePanel.setText("What level were you up to on the previous version?");
		InputActionPanel responsePanel = new InputActionPanel();
		responsePanel.setTextDocument(1, InputActionPanel.FORMAT_ONLY_DIGITS);
		regionsContainer.setRegionContent(Region.CENTER, sentencePanel);
		regionsContainer.setRegionContent(Region.SOUTH, responsePanel);
		refreshRegion(Region.CENTER);
		refreshRegion(Region.SOUTH);		
		responsePanel.addActionPanelListener(this);
		responsePanel.enableActionPanel();
	}*/

	
	protected void startExecutionAWT() {
		//initialise environment
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		/*System.out.println("getExecutionContext().getExecutionData().getModuleSession().getIndex(): " + getExecutionContext().getExecutionData().getModuleSession().getIndex());
		if (getExecutionContext().getExecutionData().getModuleSession().getIndex() == 1 & trialCounter == 0 & startLevelNeeded) {
			currentLevelProperty.setValue(this, 1);
			Level.setLevelProperty(currentLevelProperty);
			selectStartLevel();
		} else {}*/

		stimuli = initStimulus();

		regionsContainer.setRegionContent(Region.CENTER, gridGoesHere);
		regionsContainer.setRegionContent(Region.SOUTH, actionPanel);
		refreshRegion(Region.CENTER);
		refreshRegion(Region.SOUTH);
		
		activateActionPanel();
		
		updateLociPanel(null);
		updateAudioPanel(null);
		
		if (trialCounter == 0) {
			// then introduce a pause before the stimuli plays
			TimerTask delayPreTrial = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							runTrial();
						}
					});
				}
			};
			timer.schedule(delayPreTrial, 3000);			
		} else {
			runTrial();
		}
		
	}
	
	private void runTrial() {
		playStim();
		
		// stimuli display timer task
		TimerTask taskStimuli = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						clearStimuli();
					}
				});
			}
		};
		timer.schedule(taskStimuli, displayDuration);

		TimerTask taskEnd = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						actionPanel.disableActionPanel();
						endTask();
					}
				});
			}
		};
		timer.schedule(taskEnd, trialDuration);		
	}
	
	private void playStim() {
		grids.get(currentSpatial- 1).setBackground(Color.BLACK);
		refreshRegion(Region.CENTER);
		playLetterSound(audioFiles.get(currentAuditory - 1));
	}
	
	private void clearStimuli() {
		grids.get(currentSpatial - 1).setBackground(Color.LIGHT_GRAY);
		refreshRegion(Region.CENTER);
	}
	
	public void actionTriggered(ActionPanel source, Object actionValue) {
		if (source instanceof InputActionPanel) {
			currentLevelProperty.setValue(this, (Integer.valueOf((String) actionValue)));
			Level.setLevelProperty(currentLevelProperty);
			regionsContainer.removeRegionContent(Region.CENTER);
			regionsContainer.removeRegionContent(Region.SOUTH);
			refreshRegion(Region.CENTER);
			refreshRegion(Region.SOUTH);
			if (getFinishExecutionLock()) {
				finishExecution();
			}
		} else if (source instanceof KeyActionPanel) {
			if ((String) actionValue == "grid") {
				givenSpatialAction = Response.Match;
			} else if ((String) actionValue == "letter") {
				givenAuditoryAction = Response.Match;
			}		
		}
	}	

	private void playLetterSound(String filename) {
		songPath = this.getClass().getResource(filename);
		try {
			audioIn = AudioSystem.getAudioInputStream(songPath);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			letterToPlay = AudioSystem.getClip();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		try {
			letterToPlay.open(audioIn);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		letterToPlay.start();
	}
	
	private void activateActionPanel() {
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		actionPanel.addKey(KeyEvent.VK_1, "Grid match", "grid");
		actionPanel.addKey(KeyEvent.VK_0, "Letter Match", "letter");
		actionPanel.enableActionPanel();
	}
	
	private void resetTrialSpecifics() {
		currentSpatial = 0;
		currentAuditory = 0;
		targetSpatial = 0;
		targetAuditory = 0;
		correctSpatialAction = Response.noMatch;
		correctAuditoryAction = Response.noMatch;
		givenSpatialAction = Response.noMatch;
		givenAuditoryAction = Response.noMatch;
		stimuli = new ArrayList<Integer>();
	}
	
	private ArrayList<Integer> initStimulus() {
		/*System.out.println("running initStimulus()");
		System.out.println("trialCounter: " + trialCounter);
		
		StatusPanel levelPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("level");
		levelPanel.setProperty("value", Level.getLevelProperty().getValueOrDefault(this));*/
		
		if (trialCounter == 0) {
			generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 20 + Level.getLevelProperty().getValueOrDefault(this));
		}
		
		if (trialsRemainingInBlock == 0) {
			generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 20 + Level.getLevelProperty().getValueOrDefault(this));
			trialCounter = 0;
		}
		
		correctSpatialAction = Response.noMatch;
		correctAuditoryAction = Response.noMatch;
		givenSpatialAction = Response.noMatch;
		givenAuditoryAction = Response.noMatch;
		
		List<Integer> currentItem = Arrays.asList(stimuliListGrids.get(trialCounter), stimuliListAudio.get(trialCounter));
		

		if(trialCounter > Level.getLevelProperty().getValueOrDefault(this)){
			List<Integer> targetItem = Arrays.asList(stimuliListGrids.get(trialCounter - Level.getLevelProperty().getValueOrDefault(this)), stimuliListAudio.get(trialCounter - Level.getLevelProperty().getValueOrDefault(this)));
			targetSpatial = targetItem.get(0);
			targetAuditory = targetItem.get(1);
		} else {
			targetSpatial = 0;
			targetAuditory = 0;
		}
		
		currentSpatial = currentItem.get(0);
		currentAuditory = currentItem.get(1);
		
		thisGridProperty.setValue(this, currentSpatial);
		thisLetterProperty.setValue(this, currentAuditory);
		
		if (currentSpatial == targetSpatial) {
			correctSpatialAction = Response.Match;
		}
		if (currentAuditory == targetAuditory) {
			correctAuditoryAction = Response.Match;
		}	
		
		stimuli.add(currentSpatial); 
		stimuli.add(currentAuditory);
		
		return stimuli;
	}
	
	private void addGridAndButtons(){
		JPanel holdingContainer;
		holdingContainer = new JPanel();
		holdingContainer.setPreferredSize(new Dimension(340,340));
		holdingContainer.setBackground(Color.WHITE);
		holdingContainer.setBorder(BorderFactory.createTitledBorder(""));
		
		grids = new ArrayList<JButton>();
		grids.add(square_1); grids.add(square_2); grids.add(square_3); 
		grids.add(square_4); grids.add(square_5); grids.add(square_6); 
		grids.add(square_7); grids.add(square_8); grids.add(square_9);
		
		for (int i = 0; i < 9; i++) {
			grids.get(i).setPreferredSize(new Dimension(100,100));
			grids.get(i).setBackground(Color.LIGHT_GRAY);
			holdingContainer.add(grids.get(i));
		}
		Font fixation = new Font("Open Sans",1,58);
		grids.remove(square_5);
		square_5.setBackground(Color.WHITE);
		square_5.setForeground(Color.DARK_GRAY);
		square_5.setFont(fixation);
		square_5.setContentAreaFilled(false);
		square_5.setText("+");

		//change layout of centre panel so it is flexible

		gridGoesHere.setLayout(new GridBagLayout());
		gridGoesHere.add(holdingContainer, new GridBagConstraints());
		gridGoesHere.revalidate();
		
		audioFiles = new ArrayList<String>();
		audioFiles.add(C); audioFiles.add(H); audioFiles.add(K); audioFiles.add(L); 
		audioFiles.add(Q); audioFiles.add(R); audioFiles.add(S); audioFiles.add(T);
	}
	
	public int getStartLevel() {
		return startLevel;
	}
	
	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}	
	
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		gridGoesHere.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
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
				typeProperty, loadProperty, trialNoProperty, currentLevelProperty, 
				blockProperty, thisGridProperty, thisLetterProperty };
	}
	
	protected void cancelExecutionAWT() {
		timer.cancel();
    }

	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			//call the method to generate some stims
			//generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 20 + Level.getLevelProperty().getValueOrDefault(this));
			block = 0;
			trialsRemainingInBlock = 20 + Level.getLevelProperty().getValueOrDefault(this);
		}		
	}
	
	private void endTask() {
		type = "spatial";
		typeProperty.setValue(this, type);
		processProperties();
		type = "auditory";
		typeProperty.setValue(this, type);
		processProperties();
		trialCounter += 1;
		if (getFinishExecutionLock()) {
			trialsRemainingInBlock -= 1;
			if (trialsRemainingInBlock == 0) {
				/*if (gridErrors < 3 & letterErrors < 3) {
					currentLevelProperty.setValue(this, currentLevelProperty.getValueOrDefault(this) + 1);
					Level.setLevelProperty(currentLevelProperty);
					UserFeedbackLevelListener thisUFLL = (UserFeedbackLevelListener) ElementUtils.findHandlerInStackByType(getExecutionContext(), UserFeedbackLevelListener.class);
					thisUFLL.insertLevelChangeInformationElement(getExecutionContext(), 2);						
				} else if (gridErrors > 5 | letterErrors > 5) {
					if (currentLevelProperty.getValueOrDefault(this) > 1) {
						currentLevelProperty.setValue(this, currentLevelProperty.getValueOrDefault(this) - 1);
						Level.setLevelProperty(currentLevelProperty);
						UserFeedbackLevelListener thisUFLL = (UserFeedbackLevelListener) ElementUtils.findHandlerInStackByType(getExecutionContext(), UserFeedbackLevelListener.class);
						thisUFLL.insertLevelChangeInformationElement(getExecutionContext(), 0);							
					}
				}*/
				gridErrors = 0;
				letterErrors = 0;
			}
			finishExecution();
		}
	}
	
	private void processProperties() {

		int typeInt;
		if (type=="spatial") {
			typeInt = 1; //spatial record
		} else {
			typeInt = 2; // auditory record
		}
		
		boolean success;
		
		switch (typeInt) {
		
		case 1:
			success = givenSpatialAction == correctSpatialAction;
			correctResponse = correctSpatialAction;
			Question.getResponseProperty().setValue(this, givenSpatialAction);
			Question.setQuestionAnswer(this, null, correctResponse);
			Points.setZeroOneMinMaxPoints(this);
			Points.setZeroOnePoints(this, success);
			Result.getResultProperty().setValue(this, success);
			
			
			//update feedback panel
			updateLociPanel(success);
			if (!success)
				gridErrors++;

			Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			
			break;
			
		case 2:
			success = givenAuditoryAction == correctAuditoryAction;
			correctResponse = correctAuditoryAction;
			Question.getResponseProperty().setValue(this, givenAuditoryAction);
			Question.setQuestionAnswer(this, null, correctResponse);
			Points.setZeroOneMinMaxPoints(this);
			Points.setZeroOnePoints(this, success);
			Result.getResultProperty().setValue(this, success);
			
			//update feedback panel
			updateAudioPanel(success);
			if (!success)
				letterErrors++;

			Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			
			break;
			
		}
		
		loadProperty.setValue(this, Level.getLevelProperty().getValueOrDefault(this));
		trialNoProperty.setValue(this, trialCounter + 1);
		
		// change feedback status panel
		//changeStatusPanelOutcome(success);
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);
		
		System.out.println("GridErrors: " + gridErrors);
		System.out.println("letterErrors: " + letterErrors);
	}
	
	private void generateBlockStimuli(int n, int num) {
		
		//set block property
		block += 1;
		blockProperty.setValue(this, block);
		StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		thisSP.setProperty("value", block);
		IteratedListSelector thisILS = (IteratedListSelector) ElementUtils.findHandlerInStackByType(getExecutionContext(), IteratedListSelector.class);
		DefaultPointsAndLevelHandler thisPLH = (DefaultPointsAndLevelHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultPointsAndLevelHandler.class);
		thisILS.setNumIterations(num);
		/*if (thisTrialAddExecution) {
			thisILS.setNumIterations(num + 1);
			thisTrialAddExecution = false;
		}*/
		thisPLH.setSampleSize(num * 2);
		thisPLH.setTrialCounter(0);
		double minThresh = ((((double)num * 2) - 9) / ((double)num * 2) * 100);
		double maxThresh = ((((double)num * 2) - 5) / ((double)num * 2) * 100);
		System.out.println("minThreshold: " + minThresh);
		System.out.println("maxThreshold: " + maxThresh);
		thisPLH.setMinThreshold(minThresh);
		thisPLH.setMaxThreshold(maxThresh);
		trialsRemainingInBlock = num;
		
		stimuliListGrids = new ArrayList<Integer>();
		stimuliListAudio = new ArrayList<Integer>();
		
		ArrayList<Integer> usedIndexes = new ArrayList<Integer>(10);
		ArrayList<Integer> gridMatch = new ArrayList<Integer>(6);
		ArrayList<Integer> audioMatch = new ArrayList<Integer>(6);
		
		//get the indices where a match will occur for the grids
		for (int i = 0; i < 4; i++) {
			int thisInt = getUniqueRandomInt(usedIndexes, n+1, num);
			gridMatch.add(thisInt);
			usedIndexes.add(thisInt);
		}

		//get the indices where a match will occur for the audio
		for (int i = 0; i < 4; i++) {
			int thisInt = getUniqueRandomInt(usedIndexes, n+1, num);
			audioMatch.add(thisInt);
			usedIndexes.add(thisInt);
		}

		//and finally get the indices where a match will occur for both
		for (int i = 0; i < 2; i++) {
			int thisInt = getUniqueRandomInt(usedIndexes, n+1, num);
			gridMatch.add(thisInt);
			audioMatch.add(thisInt);
			usedIndexes.add(thisInt);
		}

		
		
		//now use these indices to populate the stimuli arrays.
		
		//grids
		for (int i = 0; i < num; i++) {
			if (gridMatch.contains(i)) { //then this needs to be a match
				stimuliListGrids.add(stimuliListGrids.get(stimuliListGrids.size() - n)); //add the same int that appeared n items ago.
			} else { // no match, so any random int from 1-8 except the int n items ago.
				if (i < n) { //trials before n occurs, so any int is fine.
					stimuliListGrids.add(getInt(1,9));
				} else { //need to make sure it is not the item n items ago.
					stimuliListGrids.add(getUniqueRandomInt(stimuliListGrids.get(stimuliListGrids.size() - n), 1, 9));	
				}
			}
		}		
		
		//audio
		for (int i = 0; i < num; i++) {
			if (audioMatch.contains(i)) { //then this needs to be a match
				stimuliListAudio.add(stimuliListAudio.get(stimuliListAudio.size() - n)); //add the same int that appeared n items ago.
			} else { // no match, so any random int from 1-8 except the int n items ago.
				if (i < n) { //trials before n occurs, so any int is fine.
					stimuliListAudio.add(getInt(1,9));
				} else { //need to make sure it is not the item n items ago.
					stimuliListAudio.add(getUniqueRandomInt(stimuliListAudio.get(stimuliListAudio.size() - n), 1, 9));					
				}
			}
		}
	}

	
	private void updateLociPanel (Boolean gridResult) {
		StatusPanel dnbGridPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("dnbLoci");
		
		if (dnbGridPanel != null) {
			if (gridResult == null) {
				dnbGridPanel.reset();
			} else {
				dnbGridPanel.setProperty(StatusPanel.PROPERTY_VALUE, gridResult);
			}
		}
	}
	
	private void updateAudioPanel (Boolean audioResult) {
		StatusPanel dnbAudioPanel = (StatusPanel) StatusRegionUtil.getStatusPanel("dnbAudio");

		if (dnbAudioPanel != null) {
			if (audioResult == null) {
				dnbAudioPanel.reset();
			} else {
				dnbAudioPanel.setProperty(StatusPanel.PROPERTY_VALUE, audioResult);
			}
		}		
	}
	
	private int getUniqueRandomInt(ArrayList<Integer> array, int min, int max) {
		int thisInt = 0;
		boolean foundUnique = false;
		while (foundUnique == false) {
			thisInt = getInt(min, max);
			if (array.contains(thisInt) == false) {
				foundUnique = true;
			}
		}
		return thisInt;
	}
	
	private int getUniqueRandomInt(int exclude, int min, int max) {
		int thisInt = 0;
		boolean foundUnique = false;
		while (foundUnique == false) {
			thisInt = getInt(min, max);
			if (thisInt != exclude) {
				foundUnique = true;
			}
		}
		return thisInt;
	}
	
	private int getInt(int min, int max) {
		Random rnd = new Random();
		int newInt = rnd.nextInt((max - min)) + min;
		return newInt;
	}
}