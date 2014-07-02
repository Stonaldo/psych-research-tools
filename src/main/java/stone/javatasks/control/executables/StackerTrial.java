package stone.javatasks.control.executables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;

import stone.javatasks.stacker.ButtonController;
import stone.javatasks.stacker.ButtonPanel;
import stone.javatasks.stacker.Stacker;
import ch.tatool.core.data.DataUtils;
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
import ch.tatool.core.element.ExecutionStartHandler;
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;

/**
 * Importing the stacker game into the tatool/java framework, 
 * one game will count as one trial. Then the game can be used as 
 * an incentive, by allowing the children to play a few games 
 * of stacker at the end of every training session.
 * 
 * @author James Stone
 *
 */

public class StackerTrial extends BlockingAWTExecutable implements 
DescriptivePropertyHolder {
	
	private String current_user;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	private boolean success;
	private RegionsContainer regionsContainer;
	private int trialCounter = 0;
	
	private long startTime;
	private long endTime;
	
	private JPanel stackerPanel;
	private JPanel holderPanel;
	
	private JLabel resultText = new JLabel("<html>Press New Game To Start<br />Spacebar to stop the blocks");
	
	private Stacker gamePanel;
	
	public StackerTrial() {
		
		resultText.setFont(treb);
		
		holderPanel = new JPanel();
		holderPanel.setLayout(new GridBagLayout());
		holderPanel.setBackground(Color.WHITE);
		
		stackerPanel = new JPanel();
		stackerPanel.setLayout(new BorderLayout());
		stackerPanel.setBackground(Color.WHITE);
		//stackerPanel.setMaximumSize(new Dimension(420,800));
		
		gamePanel = new Stacker(this);
		
        ButtonController buttonController = new ButtonController(gamePanel);
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.setController(buttonController);
        buttonPanel.setBackground(Color.WHITE);

		stackerPanel.add(resultText, BorderLayout.NORTH);
        stackerPanel.add(buttonPanel, BorderLayout.CENTER);
		stackerPanel.add(gamePanel, BorderLayout.SOUTH);
		
		
		stackerPanel.setFocusable(true);
		stackerPanel.setVisible(true);
		
		holderPanel.add(stackerPanel, new GridBagConstraints());
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
		

		if (trialCounter == 0) {
			DefaultVisualTimeoutHandler thisDVTH = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultVisualTimeoutHandler.class);
			thisDVTH.startTimeout(getExecutionContext());
		}
		
		triggerStartExecution(getExecutionContext());
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		
		regionsContainer.setRegionContent(Region.CENTER, holderPanel);
		
	}
	
	public void endTrial(boolean success, int line) {
		
		endTime = System.nanoTime();
		trialCounter++;
		
		this.success = success;
		
		if (!success)
			resultText.setText("<html>Unlucky! You made it to line " + line + "<br />"
					+ "Try again by pressing new game! </html>");
		
		processProperties();
		if(getFinishExecutionLock()) {
			finishExecution();
		}
	}
	
	private void processProperties() {
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);

		if (getExecutionContext() != null) {
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		}
		
		// set duration time property
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
		}
		
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);
		
		if (success)
			resultText.setText("<html>Congratulations! You win! <br />"
					+ "Time taken: " + (ms/1000) + " seconds</html>");
	}
	
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(), 
				Result.getResultProperty(), Timing.getStartTimeProperty(), 
				Timing.getEndTimeProperty(), Timing.getDurationTimeProperty(), 
				Misc.getOutcomeProperty() };
	}

	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
		}
	}
	
	public void updateText(String s) {
		resultText.setText(s);
	}
	
	private void triggerStartExecution(ExecutionContext context) {
		ExecutionStartHandler handler = (ExecutionStartHandler) ElementUtils.findHandlerInStackByType(context, ExecutionStartHandler.class);
		if (handler != null) {
            handler.startExecution(context);
        }	
	}
	
	protected void cancelExecutionAWT() {
	}
	
	public void setStartTime() {
		this.startTime = System.nanoTime();
	}
	
	public long getStartTime() {
		return this.startTime;
	}
}
