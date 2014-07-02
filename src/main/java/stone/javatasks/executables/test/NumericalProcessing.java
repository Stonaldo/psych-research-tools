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

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
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
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;

/**
 * Displays an equation and asks the user to assess whether the equation is correct
 * or not.
 * 
 * @author André Locher
 */
public class NumericalProcessing extends BlockingAWTExecutable implements
		ActionPanelListener, DescriptivePropertyHolder {

	Logger logger = LoggerFactory.getLogger(NumericalProcessing.class);

	/** Question panel. */
	private CenteredTextPanel questionPanel;
	private KeyActionPanel actionPanel;

	/** Stimuli. */
	private int itemno = 0;
	private String equation = "";
	private boolean correctEquation;
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private Random rand;

	private RegionsContainer regionsContainer;
	
	/** Variables needed if task should end after a specified taskDuration (in ms). Uncomment if needed. */
	//private Timer timer;
	//private TimerTask taskEnd;
	//private int taskDuration = 2500;
	

	/** Default Constructor. */
	public NumericalProcessing() {
		questionPanel = new CenteredTextPanel();
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		//timer = new Timer(); /** Uncomment if task should end after taskDuration. */
		rand = new Random();
	}

	protected void startExecutionAWT() {
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils
				.getDisplay(context);

		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils
				.getRegionsContainer();

		String equation = initStimulus();

		questionPanel.setTextSize(100);
		questionPanel.setText(equation);
		actionPanel.addKey(KeyEvent.VK_LEFT, "korrekt", 1);
		actionPanel.addKey(KeyEvent.VK_RIGHT, "falsch", 0);

		regionsContainer.setRegionContent(Region.CENTER, questionPanel);
		regionsContainer.setRegionContent(Region.SOUTH, actionPanel);

		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();

		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);

		actionPanel.enableActionPanel();

		// Ends task automatically after taskDuration. Uncomment if needed.		
		/**		taskEnd = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						endTask();
					}
				});
			}
		};
		timer.schedule(taskEnd, taskDuration);
		 */
	}

	/**
	 * Generates the stimuli.
	 */
	private String initStimulus() {
		//String equation = "";
		givenResponse = 3;
		char operator = 0;
		int result = 0;

		int oper = (rand.nextInt(4) + 1);
		int operand1 = (int) (11 * Math.random() + 1);
		int operand2 = (int) (11 * Math.random() + 1);

		correctEquation = rand.nextInt(2) == 1;
		if (correctEquation) {
			correctResponse = 1;
		} else {
			correctResponse = 0;
		}

		// make sure division adds up
		if (oper == 4) {
			int modResult = operand1 % operand2;
			while (modResult != 0) {
				operand2 = (int) (11 * Math.random() + 1);
				modResult = operand1 % operand2;
			}
		}

		switch (oper) {
		case 1:
			operator = '+';
			if (correctEquation) {
				result = operand1 + operand2;
			} else {
				result = operand1 + operand2 + 2;
			}
			equation = String.valueOf(operand1) + operator
					+ String.valueOf(operand2) + "=" + result;
			break;
		case 2:
			operator = '-';
			if (correctEquation) {
				result = operand1 - operand2;
			} else {
				result = operand1 - operand2 - 2;
			}
			equation = String.valueOf(operand1) + operator
					+ String.valueOf(operand2) + "=" + result;
			break;
		case 3:
			operator = '*';
			if (correctEquation) {
				result = operand1 * operand2;
			} else {
				result = operand1 * operand2 + 2;
			}
			equation = String.valueOf(operand1) + operator
					+ String.valueOf(operand2) + "=" + result;
			break;
		case 4:
			operator = '/';
			if (correctEquation) {
				result = operand1 / operand2;
			} else {
				result = operand1 / operand2 + 2;
			}
			equation = String.valueOf(operand1) + operator
					+ String.valueOf(operand2) + "=" + result;
			break;
		}

		itemno = itemno + 1;

		return equation;
	}

	public void actionTriggered(ActionPanel source, Object actionValue) {
		actionPanel.disableActionPanel();

		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);

		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());

		givenResponse = (Integer) actionValue;
		
		endTask(); // Comment if timer is used.
	}

	private void endTask() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);

		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, equation, correctResponse);
		boolean success = correctResponse == givenResponse;
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);

		// set duration time property
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
			if (duration <= 0) {
				duration = 0;
			}
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);

		// create new trial
		Trial currentTrial = getExecutionContext().getExecutionData()
				.addTrial();
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
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty() };
	}
	
	protected void cancelExecutionAWT() {
		//timer.cancel();
    }
}