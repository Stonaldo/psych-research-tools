package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stone.javatasks.nback.SpatialNBackPanel;
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

public class SingleNBack extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
ExecutionPhaseListener, ActionPanelListener{

	Logger logger = LoggerFactory.getLogger(DualNBack.class);
	
	//needed
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty currentLevelProperty = new IntegerProperty("currentLevel");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	private IntegerProperty blockProperty = new IntegerProperty("blockno");
	private IntegerProperty thisLocationProperty = new IntegerProperty("thisLocation");

	private int block;
	private int trialsRemainingInBlock;
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	
	private RegionsContainer regionsContainer;
	private KeyActionPanel actionPanel;
	private int trialCounter = 0;
	private Timer timer;
	private int displayDuration = 500;
	private int trialDuration = 3000;
	private Response correctResponse;
	
	private enum Response {
		noMatch, Match
	}
	
	private ArrayList<Integer> stimuli;
	
	private int currentLocation;
	private int targetLocation;
	private Response correctAction;
	private Response givenAction;
	private int errors;
	
	private CenteredTextPanel gridGoesHere;
	private SpatialNBackPanel nbackPanel;
	
	/**
	 * Default constructor for single n back task
	 */
	
	public SingleNBack() {
		System.out.println("running constructor");
		
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){}	
		
		//initialise the panel to add the grid to.
		gridGoesHere = new CenteredTextPanel();
		nbackPanel = new SpatialNBackPanel();
		nbackPanel.setBackground(Color.WHITE);
		timer = new Timer();
		
		activateActionPanel();
		resetTrialSpecifics();
	}
	
	protected void startExecutionAWT() {
		//initialise environment
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		/**
		 * call the method to generate a new set of stims, i.e. the
		 * order of the locations lighting up. 
		 */ 
		initStimulus();
		
		
		gridGoesHere.setLayout(new GridBagLayout());
		gridGoesHere.add(nbackPanel, new GridBagConstraints());
		gridGoesHere.revalidate();
		//place the components where they need to be.
		regionsContainer.setRegionContent(Region.CENTER, gridGoesHere);
		regionsContainer.setRegionContent(Region.SOUTH, actionPanel);
		refreshRegion(Region.CENTER);
		refreshRegion(Region.SOUTH);
		
		activateActionPanel();
		
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
		lightThisStim();
		
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
	
	private void lightThisStim() {
		nbackPanel.light(currentLocation - 1);
		refreshRegion(Region.CENTER);
	}
	
	private void clearStimuli() {
		nbackPanel.unlight(currentLocation - 1);
		refreshRegion(Region.CENTER);
	}
	
	private void initStimulus() {
		
		if (trialCounter == 0) {
			generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 15 + Level.getLevelProperty().getValueOrDefault(this));
		}
		
		if (trialsRemainingInBlock == 0) {
			generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 15 + Level.getLevelProperty().getValueOrDefault(this));
			trialCounter = 0;
		}
		
		correctAction = Response.noMatch;
		givenAction = Response.noMatch;
		

		if(trialCounter > Level.getLevelProperty().getValueOrDefault(this)){
			targetLocation = stimuli.get(trialCounter - Level.getLevelProperty().getValueOrDefault(this));
		} else {
			targetLocation = 0;
		}
		
		currentLocation = stimuli.get(trialCounter);
		
		thisLocationProperty.setValue(this, currentLocation);
		
		if (currentLocation == targetLocation) {
			correctAction = Response.Match;
		}
	}
	
	public void actionTriggered(ActionPanel source, Object actionValue) {
		if (source instanceof KeyActionPanel) {
			givenAction = Response.Match;	
		}
	}
	
	private void activateActionPanel() {
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		actionPanel.addKey(KeyEvent.VK_1, "Match", "match");
		actionPanel.enableActionPanel();
	}
	
	private void resetTrialSpecifics() {
		currentLocation = 0;
		targetLocation = 0;
		correctAction = Response.noMatch;
		givenAction = Response.noMatch;
		stimuli = new ArrayList<Integer>();
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
				loadProperty, trialNoProperty, currentLevelProperty, 
				blockProperty, thisLocationProperty };
	}
	
	protected void cancelExecutionAWT() {
		timer.cancel();
    }

	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			//call the method to generate some stims
			//generateBlockStimuli(Level.getLevelProperty().getValueOrDefault(this), 20 + Level.getLevelProperty().getValueOrDefault(this));
			block = 0;
			trialsRemainingInBlock = 15 + Level.getLevelProperty().getValueOrDefault(this);
		}		
	}
	
	private void endTask() {
		processProperties();
		trialCounter += 1;
		if (getFinishExecutionLock()) {
			trialsRemainingInBlock -= 1;
			if (trialsRemainingInBlock == 0) {
				errors = 0;
			}
			finishExecution();
		}
	}
	
	private void processProperties() {
		
		boolean success;
		success = givenAction == correctAction;
		correctResponse = correctAction;
		Question.getResponseProperty().setValue(this, givenAction);
		Question.setQuestionAnswer(this, null, correctResponse);
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		if (!success)
			errors++;

		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		
		loadProperty.setValue(this, Level.getLevelProperty().getValueOrDefault(this));
		trialNoProperty.setValue(this, trialCounter + 1);
		
		// change feedback status panel
		//changeStatusPanelOutcome(success);
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);
		
		System.out.println("errors: " + errors);
	}
	
	private void generateBlockStimuli(int n, int num) {
		currentLevelProperty.setValue(this,  n);
		//set block property
		block += 1;
		blockProperty.setValue(this, block);
		StatusPanel thisSP = (StatusPanel) StatusRegionUtil.getStatusPanel("block");
		thisSP.setProperty("value", block);
		IteratedListSelector thisILS = (IteratedListSelector) ElementUtils.findHandlerInStackByType(getExecutionContext(), IteratedListSelector.class);
		DefaultPointsAndLevelHandler thisPLH = (DefaultPointsAndLevelHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultPointsAndLevelHandler.class);
		thisILS.setNumIterations(num);

		thisPLH.setSampleSize(num);
		thisPLH.setTrialCounter(0);
		double minThresh = ((((double)num * 2) - 9) / ((double)num * 2) * 100);
		double maxThresh = ((((double)num * 2) - 5) / ((double)num * 2) * 100);
		System.out.println("minThreshold: " + minThresh);
		System.out.println("maxThreshold: " + maxThresh);
		thisPLH.setMinThreshold(minThresh);
		thisPLH.setMaxThreshold(maxThresh);
		trialsRemainingInBlock = num;
		
		stimuli = new ArrayList<Integer>();
		
		ArrayList<Integer> usedIndexes = new ArrayList<Integer>(5);
		ArrayList<Integer> lociMatch = new ArrayList<Integer>(5);
		
		//get the indices where a match will occur
		for (int i = 0; i < 5; i++) {
			int thisInt = getUniqueRandomInt(usedIndexes, n+1, num);
			lociMatch.add(thisInt);
			usedIndexes.add(thisInt);
		}

		//now use these indices to populate the stimuli arrays.
		
		for (int i = 0; i < num; i++) {
			if (lociMatch.contains(i)) { //then this needs to be a match
				stimuli.add(stimuli.get(stimuli.size() - n)); //add the same int that appeared n items ago.
			} else { // no match, so any random int from 1-8 except the int n items ago.
				if (i < n) { //trials before n occurs, so any int is fine.
					stimuli.add(getInt(1,10));
				} else { //need to make sure it is not the item n items ago.
					stimuli.add(getUniqueRandomInt(stimuli.get(stimuli.size() - n), 1, 10));	
				}
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
