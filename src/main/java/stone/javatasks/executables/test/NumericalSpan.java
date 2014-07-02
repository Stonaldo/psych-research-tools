/*******************************************************************************
 * Copyright (c) 2011 Michael Ruflin, André Locher, Claudia von Bastian.
 * 
 * This file is part of Tatool.
 * 
 * Tatool is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * Tatool is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tatool. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package stone.javatasks.executables.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Displays a sequence of numbers and requests the user to remember them in the correct order. Afterwards
 * the user has to recall the numbers and enter them one after the other.
 * 
 * @author André Locher
 */
public class NumericalSpan extends BlockingAWTExecutable
		implements ActionPanelListener, DescriptivePropertyHolder,
		ExecutionPhaseListener {

	Logger logger = LoggerFactory.getLogger(NumericalSpan.class);

	private RegionsContainer regionsContainer;

	// executable phases
	public enum Phase {
		INIT, MEMORISATION, RECALL
	}

	private Phase currentPhase;

	// additional properties of interest
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty currentLevelProperty = new IntegerProperty("current Level");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");

	// panels
	private CenteredTextPanel questionPanel;
	private InputActionPanel actionPanel;

	// timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private TimerTask startRecallTask;
	private int displayDuration = 1000; // display duration of memoranda
	private static int interResponseDuration = 500; // blank screen between recalls

	// stimuli generation
	private int startLevel = 1; // span = start level + current level
	private int[] numbers;
	private Random rand;

	// stimuli
	private int trialCounter; // counts the trial
	private int memCounter; // counts the memoranda
	private int respCounter; // counts the responses
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;

	/**
	 * Constructor initiates objects that will be used through the whole
	 * lifespan of this executable.
	 */
	public NumericalSpan() {
		questionPanel = new CenteredTextPanel();
		actionPanel = new InputActionPanel();
		actionPanel.setTextDocument(2, InputActionPanel.FORMAT_ONLY_DIGITS);
		actionPanel.addActionPanelListener(this);

		rand = new Random();
		timer = new Timer();
	}

	/**
	 * Executable start method.
	 */
	protected void startExecutionAWT() {
		
		// initialise environment for executable
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();

		switch (currentPhase) {
		case INIT:
			startInitPhase();
			break;
		case MEMORISATION:
			startMemorisationPhase();
			break;
		case RECALL:
			startRecallPhase();
			break;
		}
	}

	/**
	 * Executes the init phase that includes the initialisation of the stimuli
	 * data and the necessary counters.
	 */
	private void startInitPhase() {
		generateStimuli();

		// reset stimuli counter
		memCounter = 0;
		respCounter = 0;
		
		Result.getResultProperty().setValue(this, null);

		// start memorisation phase for first stimulus
		currentPhase = Phase.MEMORISATION;
		startMemorisationPhase();
	}

	/**
	 * Executes the memorisation phase that includes the display of the stimulus
	 * for a given duration.
	 */
	private void startMemorisationPhase() {
		// String stimulus = stimuliData[memCounter];
		String stimulus = String.valueOf(numbers[memCounter]);
		questionPanel.setTextSize(120);
		questionPanel.setText(stimulus);

		// set phase to recall if this is the last stimulus
		memCounter++;
		if (memCounter == numbers.length) {
			currentPhase = Phase.RECALL;
		}

		// display memoranda only for a given time
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						suspendExecutable(); // suspend task
					}
				});
			}
		};

		regionsContainer.setRegionContent(Region.CENTER, questionPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		timer.schedule(suspendExecutableTask, displayDuration);
	}

	/**
	 * Executes the recall phase that includes the display of the action panel
	 * which allows the user to enter his recalls one after the other.
	 */
	private void startRecallPhase() {
		StringBuilder text = new StringBuilder();
		text.append("Number ");
		text.append(respCounter + 1);
		text.append(":");
		questionPanel.setTextSize(60);
		questionPanel.setText(text.toString());

		actionPanel.clearTextField();

		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();

		regionsContainer.setRegionContent(Region.CENTER, questionPanel);
		regionsContainer.setRegionContent(Region.SOUTH, actionPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);

		actionPanel.enableActionPanel();
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

	/**
	 * Listens to answers given by the user and stores the data as a new trial.
	 */
	public void actionTriggered(ActionPanel source, Object actionValue) {
		try {
			givenResponse = Integer.valueOf((String) actionValue);
		} catch (NumberFormatException e) {
			givenResponse = 0;
		}

		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());

		actionPanel.disableActionPanel();
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);

		// process the properties for this trial
		processProperties();

		// decide whether we have to display another recall stimuli or can
		// finish the executable. if (respCounter < (stimuliData.length - 1)) {
		if (respCounter < (numbers.length - 1)) {
			respCounter++;
			// show next recall stimulus after a little pause
			startRecallTask = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							changeStatusPanelOutcome(null);
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
		int stimulus = numbers[respCounter];
		correctResponse = Integer.valueOf(stimulus);
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, String.valueOf(stimulus), correctResponse);
		boolean success = correctResponse == givenResponse;
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		if (respCounter < (numbers.length - 1)) {
			Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
		} else {
			Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		}
		loadProperty.setValue(this, numbers.length);
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

	/**
	 * Generates the stimuli.
	 */
	private void generateStimuli() {
		int currLevel = Level.getLevelProperty().getValueOrDefault(this);
		currentLevelProperty.setValue(this, currLevel);

		numbers = new int[currLevel + startLevel];
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

	public int getStartLevel() {
		return startLevel;
	}

	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}

}
