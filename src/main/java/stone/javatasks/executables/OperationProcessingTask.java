package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Font;
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


/**Displays a mathematical operation on screen and asks the user to indicate if the 
 * answer given is correct or false. Can be used as a processing element to WM tasks.
 * @author James Stone
 */

public class OperationProcessingTask extends BlockingAWTExecutable implements 
	ActionPanelListener, DescriptivePropertyHolder {
	
	Logger logger = LoggerFactory.getLogger(OperationProcessingTask.class);
	
	//panels//
	private CenteredTextPanel operationPanel; //for displaying operation//
	private KeyActionPanel actionPanel; //for displaying options and collecting responses//
	
	//stimuli variables//
	private int itemno = 0;
	private String equation = "";
	private boolean correctEquation; //should the generated equation be correct or incorrect?//
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private Random rand;
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	
	/*variables controlling timer. If no answer given within a certain 
	 * time then execution moves on. Depends on methodology as to whether 
	 * this would be required. If not required, then comment out these 
	 * variables and all other timer related code below (will be noted)
	 */
	/*private Timer timer;
	private TimerTask taskEnd;
	private int taskDuration = 10000; //time limit for giving an answer (in ms)//
	*/
	
	private RegionsContainer regionsContainer;
	
	//init constructor//
	public OperationProcessingTask() {
		operationPanel = new CenteredTextPanel();
		operationPanel.setTextFont(treb);
		operationPanel.setTextColor(fontColor);
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		//timer = new Timer(); //comment or uncomment depending on timer requirement//
		rand = new Random();
	}
	
	protected void startExecutionAWT() {
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		String equation = generateEquation(); //call the generateEquation method and set the result to variable 'equation'//
		
		operationPanel.setTextSize(120);
		operationPanel.setText(equation);	
		
		actionPanel.addKey(KeyEvent.VK_LEFT, "Correct", 1);
		actionPanel.addKey(KeyEvent.VK_RIGHT, "False", 0);
		
		regionsContainer.setRegionContent(Region.CENTER, operationPanel);
		regionsContainer.setRegionContent(Region.SOUTH,  actionPanel);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		actionPanel.enableActionPanel(); 
		
		/*At this point the equation is displayed and the action panel has been 
		 * configured and enabled to allow the participant to respond. There is code
		 * to move execution along when an answer is given within the action handler.
		 * Alternatively the timer will be used to continue the program if no response 
		 * is given after the set duration.
		 * 
		 */
		/*
		taskEnd = new TimerTask() {
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
	
	//Generate an operation to use as stimulus//
	private String generateEquation() {
		givenResponse = 3;
		char operator = 0;
		int result = 0;
		
		int oper = (rand.nextInt(4) + 1); //get random number from 1-4, which dictates whether +-*/ operation//
		int operand1 = (int) (11 * Math.random() + 1);
		int operand2 = (int) (11 * Math.random() + 1);
		
		correctEquation = rand.nextInt(2) == 1; //decide if this is a trial where the operation should be correct or incorrect//
		if (correctEquation) {
			correctResponse = 1;
		} else {
			correctResponse = 0;
		}
		
		//make sure division between operands yields an integer//
		if (oper == 4) {
			int modResult = operand1 % operand2;
			while (modResult != 0) {
				operand2 = (int) (11 * Math.random() + 1);
				modResult = operand1 % operand2;
			}
		}
		
		//need a different set of instructions depending on operator chosen so a switch statement is useful//
		switch(oper) {
		
		case 1:
			operator = '+';
			if (correctEquation) {
				result = operand1 + operand2;
			} else {
				result = operand1 + operand2 + (rand.nextInt(9) + 1); //add some noise if it is supposed to be wrong//
			}
			break;
			
		case 2:
			operator = '-';
			if (correctEquation) {
				result = operand1 - operand2;
			} else {
				result = operand1 - operand2 - (rand.nextInt(9) + 1); //add some noise if it is supposed to be wrong//
			}
			break;
			
		case 3:
			operator = 'x';
			if (correctEquation) {
				result = operand1 * operand2;
			} else {
				result = (operand1 * operand2) + (rand.nextInt(9) + 1); //add some noise if it is supposed to be wrong//
			}
			break;
			
		case 4:
			operator = '/';
			if (correctEquation) {
				result = operand1 / operand2;
			} else {
				result = operand1 / operand2 - (rand.nextInt(9) + 1); //add some noise if it is supposed to be wrong//
			}
			break;
		}
		
		equation = String.valueOf(operand1) + operator + String.valueOf(operand2) + '=' + result;
		
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
		
		endTask();
	}
	
	private void endTask() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this,  equation, correctResponse);
		boolean success = correctResponse == givenResponse;
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		
		//duration time property//
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
			if (duration <= 0) {
				duration = 0;
			}
		}
		long ms = (long) duration/1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		
		//new trial//
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		
		//add all executable properties//
		DataUtils.storeProperties(currentTrial,  this);
		
		//finish execution //
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
