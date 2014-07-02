package stone.javatasks.control.executables;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Timer;

import stone.javatasks.jigsaw2.JuzzlePanel;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Level;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.StringProperty;
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
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;

/**
 * run a trial of the jigsaw puzzle within the tatool framework
 * using the current level to determine number of pieces.
 * 
 * @author stonej
 *
 */

public class JigsawTrial extends BlockingAWTExecutable implements 
		DescriptivePropertyHolder {
	
	private String current_user;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	private int trialCounter = 0;
	public int currentLevel;
	private boolean success;
	private RegionsContainer regionsContainer;	
	private long startTime;
	private long endTime;
	
	private IntegerProperty puzzleTimeProperty = new IntegerProperty("puzzleTime");
	private StringProperty picProperty = new StringProperty("pic");
	private IntegerProperty numPiecesProperty = new IntegerProperty("numPieces");
	
	private int StartingNumOfDimensions;
	
	JuzzlePanel thisTrialPanel;
	
	int sessionTime = 0;
	protected Timer sessionCounter = new Timer(1000, new TimeCounterListener());
	public int TimerLimit;
	
	
	public JigsawTrial() {
		this.currentLevel = Level.getLevelProperty().getValueOrDefault(this);
	}
	
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
		
		triggerStartExecution(getExecutionContext());
		
		if (trialCounter == 0) {
			Timing.getStartTimeProperty().setValue(this, new Date());
			DefaultVisualTimeoutHandler thisDVTH = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultVisualTimeoutHandler.class);
			thisDVTH.startTimeout(getExecutionContext());
			sessionTime = 0;
			sessionCounter.start();
		}
		
		trialCounter++;
		
		startTime = System.nanoTime();
		
		currentLevel = Level.getLevelProperty().getValueOrDefault(this);
		
		thisTrialPanel = new JuzzlePanel(this);
		thisTrialPanel.revalidate();
		regionsContainer.setRegionContent(Region.CENTER, thisTrialPanel);
	}
	

	
	public void endTrial(int timeTaken, String pic, int numPiece, boolean success) {
		//System.out.println("endTrial() running");
		processProperties(timeTaken, pic, numPiece, success);
		if(getFinishExecutionLock()) {
			finishExecution();
		}
	}
	
	private void processProperties(int timeTaken, String pic, int numPiece, boolean success) {
		
		puzzleTimeProperty.setValue(this, timeTaken);
		picProperty.setValue(this, pic);
		numPiecesProperty.setValue(this, numPiece);
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);

		// set duration time property
		/*long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
		}
		
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		*/

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
				Result.getResultProperty(), Timing.getStartTimeProperty(), 
				Timing.getEndTimeProperty(), Timing.getDurationTimeProperty(), 
				Misc.getOutcomeProperty(), puzzleTimeProperty, numPiecesProperty,
				picProperty };
	}	
	
	public void setStartingNumOfDimensions(int n) {
		this.StartingNumOfDimensions = n;
	}
	
	public int getStartingNumOfDimensions() {
		return this.StartingNumOfDimensions;
	}
	
	public void setTimerLimit(int n) {
		this.TimerLimit = n;
	}
	
	public int getTimerLimit() {
		return this.TimerLimit;
	}
	
	public int getSessionTime() {
		return this.sessionTime;
	}
	
	protected class TimeCounterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			sessionTime++;
		}
	}
	
	
	private void triggerStartExecution(ExecutionContext context) {
		ExecutionStartHandler handler = (ExecutionStartHandler) ElementUtils.findHandlerInStackByType(context, ExecutionStartHandler.class);
		if (handler != null) {
            handler.startExecution(context);
        }	
	}

}
