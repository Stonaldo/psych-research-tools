package stone.javatasks.control.executables;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

import stone.javatasks.wordsearch.UpdateAction;
import stone.javatasks.wordsearch.WordSearch;
import stone.javatasks.wordsearch.WordSearchGame;
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
 * run a trial of wordsearch using the tatool framework, logging performance aspects.
 * @author James Stone
 */

public class WordsearchTrial extends BlockingAWTExecutable implements
		DescriptivePropertyHolder, Observer {
	
	private String current_user;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	private boolean success;
	private RegionsContainer regionsContainer;	
	private long startTime;
	private long endTime;
	private int trialCounter = 0;
	
	private IntegerProperty sizeProperty = new IntegerProperty("size");
	
	
	private JPanel holdingPanel;
	private int StartingSizeOfGrid;
	
	int sessionTime = 0;
	protected Timer sessionCounter = new Timer(1000, new TimeCounterListener());
	public int TimerLimit;
	
	//private String base = "src/main/resources/stimuli/wordsearch/";
	private String[] wordLists = new String[20];
	
	/*
	 * construct trial
	 */
	public WordsearchTrial() {
		holdingPanel = new JPanel();
		holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
		holdingPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		holdingPanel.setBackground(Color.WHITE);
		
		for (int i = 0; i < 20; i++) {
			wordLists[i] = "wordlist_" + String.valueOf(i) + ".txt";
		}
	}
	
    public void update(Observable o, Object arg) {
        switch ((UpdateAction)arg) {
            case ALL_WORDS_FOUND:
            	endTime = System.nanoTime();
            	endTrial(true);
            	break;
            case QUIT:
            	if (sessionTime > TimerLimit) {
                	endTime = System.nanoTime();
                	endTrial(false);          		
            	}
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
		
		triggerStartExecution(getExecutionContext());
		
		if (trialCounter == 0) {
			Timing.getStartTimeProperty().setValue(this, new Date());
			DefaultVisualTimeoutHandler thisDVTH = (DefaultVisualTimeoutHandler) ElementUtils.findHandlerInStackByType(getExecutionContext(), DefaultVisualTimeoutHandler.class);
			thisDVTH.startTimeout(getExecutionContext());
			sessionTime = 0;
			sessionCounter.start();
		}
		
		trialCounter++;
		
		holdingPanel.removeAll();
		startTime = System.nanoTime();
		
		String[] list = getList();
		
		sizeProperty.setValue(this, StartingSizeOfGrid + Level.getLevelProperty().getValueOrDefault(this));
		
		WordSearchGame w = new WordSearchGame(list, StartingSizeOfGrid + Level.getLevelProperty().getValueOrDefault(this));
		WordSearch thisTrialWordsearch = new WordSearch(this, w);
		thisTrialWordsearch.setAllBGs(Color.WHITE);
		
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(thisTrialWordsearch);
		holdingPanel.add(Box.createVerticalGlue());
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
	}
	
	public String[] getList() {
		Random r = new Random();
		
		
		
		Scanner s;
		s = new Scanner(new InputStreamReader(this.getClass().getResourceAsStream("/stimuli/wordsearch/" + wordLists[r.nextInt(20)])));
		
		/*
		Scanner stdin = null;
		try {
			stdin = new Scanner(new File(base + wordLists[r.nextInt(20)]));
		} catch(FileNotFoundException e) {
			System.out.println("Couldn't open file \"wordlist.txt\"");
		} */
		
		StringBuilder wordList = new StringBuilder();
		while(s.hasNext()) {
			wordList.append(s.next() + " ");
		}
		wordList.deleteCharAt(wordList.length()-1);
		
		String[] list = wordList.toString().split(" ");
		
		s.close();
		
		return list;
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
				Misc.getOutcomeProperty(), sizeProperty };
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
	
	public void setStartingSizeOfGrid(int n) {
		this.StartingSizeOfGrid = n;
	}
	public int getStartingSizeOfGrid() {
		return this.StartingSizeOfGrid;
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
