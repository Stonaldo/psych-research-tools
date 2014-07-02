package stone.javatasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stone.javatasks.helperclasses.GridSpanStimulus;
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
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;

/** Displays a 8x8 grid with a pattern fill.
 * Participant must make a judgement on whether or not the pattern
 * is symmetrical.
 * 
 * @author James Stone
 *
 */

public class SymmetryProcessingTask extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder {
	
	Logger logger = LoggerFactory.getLogger(OperationProcessingTask.class);
	
	//panels//
	private GridSpanStimulus symmetryPanel; //for displaying grid pattern//
	private JPanel holdingPanel;
	private KeyActionPanel actionPanel; //for displaying options and collecting responses//	
	
	//stimuli variables//
	private int itemno = 0;
	private boolean correctAnswer; //should the generated pattern be symmetrical or not?//
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private Random rand;
	private int minimumSquaresToFill = 0; //overwritten by XML value//
	private int maximumSquaresToFill = 0; //overwritten by XML value//
	private int gridSize = 0; //overwritten by XML //
	
	private RegionsContainer regionsContainer;	
	
	
	//init constructor//
	public SymmetryProcessingTask() {
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		rand = new Random();
	}
	
	protected void startExecutionAWT() {
		
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception e)
		{}
				
		
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		generateGrid(); //call the generateGrid method which will fill symmetryPanel with a pattern//	
		
		actionPanel.addKey(KeyEvent.VK_LEFT, "Symmetrical", 1);
		actionPanel.addKey(KeyEvent.VK_RIGHT, "Non-Symmetrical", 0);
		
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContent(Region.SOUTH,  actionPanel);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		actionPanel.enableActionPanel(); 
	}
	
	private void generateGrid() {
		holdingPanel = new JPanel();
		holdingPanel.setPreferredSize(new Dimension(800,800));
		holdingPanel.setBackground(Color.WHITE);
		holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
		symmetryPanel = new GridSpanStimulus(8, gridSize);
		//should it be symmetrical?//
		correctAnswer = rand.nextInt(2) == 1; //if evaluates to true then display symmetrical else non-symm//
		if (correctAnswer) {correctResponse = 1;} else {correctResponse = 0;}
		//how many grids to fill?//
		int numberOfSquaresToFill = rand.nextInt(maximumSquaresToFill - minimumSquaresToFill) + minimumSquaresToFill + 1;
		
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(symmetryPanel);
		holdingPanel.add(Box.createVerticalGlue());
		symmetryPanel.fillListButtons(symmetryPanel.getPattern(correctAnswer, numberOfSquaresToFill), Color.BLACK);
		
		itemno++;
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
		Question.setQuestionAnswer(this, "symmetry_judgement", correctResponse);
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
	
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		holdingPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}

	public int getminimumSquaresToFill() {
		return this.minimumSquaresToFill;
	}
	public void setminimumSquaresToFill(int n) {
		this.minimumSquaresToFill = n;
	}
	public int getmaximumSquaresToFill() {
		return this.maximumSquaresToFill;
	}
	public void setmaximumSquaresToFill(int n) {
		this.maximumSquaresToFill = n;
	}
	public int getgridSize() {
		return this.gridSize;
	}
	public void setgridSize(int n) {
		this.gridSize = n;
	}
}
















