package stone.javatasks.control.executables;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

import stone.javatasks.sudoku.SudokuAsPanel;
import stone.javatasks.sudoku.UpdateAction;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Level;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
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
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;

/**
 * run a trial of sudoku using the tatool framework, logging performance aspects.
 * @author James Stone
 */

public class SudokuTrial extends BlockingAWTExecutable implements
		DescriptivePropertyHolder, Observer {
	
	private String current_user;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	private int trialCounter = 0;
	private boolean success;
	private RegionsContainer regionsContainer;	
	private long startTime;
	private long endTime;
	
	private JPanel holdingPanel;
	private int startingNumOfSpaces;
	
	private IntegerProperty numHelpUsedProperty = new IntegerProperty("numHelp");
	private IntegerProperty numSpacesInGridProperty = new IntegerProperty("spacesToFill");
	
	public int sessionTime = 0;
	public int TimerLimit;
	protected Timer sessionCounter = new Timer(1000, new TimeCounterListener());
	
	
	/*
	 * construct trial
	 */
	public SudokuTrial() {
		holdingPanel = new JPanel();
		holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
		holdingPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		holdingPanel.setBackground(Color.WHITE);
	}
	
    public void update(Observable o, Object arg) {
        switch ((UpdateAction)arg) {
            case WON:
            	endTime = System.nanoTime();
            	endTrial(true);
            	break;
            case QUIT:
            	System.out.println("case QUIT pressed");
            	System.out.println("sessionTime: " + sessionTime);
            	System.out.println("TimerLimit: " + TimerLimit);
            	if (sessionTime > TimerLimit) {
                	endTime = System.nanoTime();
                	endTrial(false);           		
            	}
            	break;
            case ITEM_WITH_HELP:
            	numHelpUsedProperty.setValue(this, numHelpUsedProperty.getValue(this) + 1);
            	break;
            case INCREMENT_HELP:
            	numHelpUsedProperty.setValue(this, numHelpUsedProperty.getValue(this) + 1);
            	break;
        }
    }
    
    public void endTrial(boolean success) {
    	processProperties(success);
    	if (getFinishExecutionLock()) {
    		finishExecution();
    	}
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
		
		holdingPanel.removeAll();
		numHelpUsedProperty.setValue(this, 0);
		startTime = System.nanoTime();
		
		if (trialCounter == 0) {
			Timing.getStartTimeProperty().setValue(this, new Date());
			DefaultVisualTimeoutHandler thisDVTH = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultVisualTimeoutHandler.class);
			thisDVTH.startTimeout(getExecutionContext());
			sessionTime = 0;
			sessionCounter.start();
		}
		
		trialCounter++;
		
		numSpacesInGridProperty.setValue(this, startingNumOfSpaces + 2 * (Level.getLevelProperty().getValueOrDefault(this)));
		
		SudokuAsPanel thisTrialSudo = new SudokuAsPanel(this, (startingNumOfSpaces + 2 * (Level.getLevelProperty().getValueOrDefault(this))));
		thisTrialSudo.setAllBackgrounds(Color.WHITE);
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(thisTrialSudo);
		holdingPanel.add(Box.createVerticalGlue());
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
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
				Misc.getOutcomeProperty(), numSpacesInGridProperty, 
				numHelpUsedProperty };
	}

	/**
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list and set the executable phase
	 * to INIT.
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
		}
	}
	
	private void processProperties(boolean success) {
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);

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
	
	public void setstartingNumOfSpaces(int n) {
		this.startingNumOfSpaces = n;
	}
	public int getstartingNumOfSpaces() {
		return this.startingNumOfSpaces;
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
			System.out.println("listener in action");
			sessionTime++;
		}
	}
	
	
}
