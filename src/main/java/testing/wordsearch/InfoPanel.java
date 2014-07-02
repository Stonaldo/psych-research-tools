package testing.wordsearch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/*
 * this class builds and handles a panel which displays info and allows a user
 * to submit the word they have selected.
 */

public class InfoPanel extends JPanel implements Observer{
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	
	JButton subButton;
	JLabel words;
	JLabel status;
	WordSearchGame game;
	TimerTask resetText;
	Timer timer;
	int displayNotificationDuration;
	
	public InfoPanel(WordSearchGame game) {
		super(new BorderLayout());
		this.game = game;
		timer = new Timer();
		displayNotificationDuration = 2000;
		
		subButton = new JButton("Submit");
		subButton.setPreferredSize(new Dimension(200,50));
		add(subButton, BorderLayout.NORTH);
		
		ButtonControllerWS buttonController = new ButtonControllerWS(game);
		subButton.addActionListener(buttonController);
		
		words = new JLabel();
		words.setText(buildWordsToFindList());
		add(words, BorderLayout.CENTER);
		
		status = new JLabel("Playing");
		status.setFont(treb);
		status.setHorizontalAlignment(SwingConstants.CENTER);
		add(status, BorderLayout.SOUTH);
	}
	
	public String buildWordsToFindList() {
		StringBuilder s = new StringBuilder();
		s.append("<HTML>");
		for (int i = 0; i < game.wordsToFind.size(); i++) {
			s.append(game.wordsToFind.get(i) + "<br>");
		}
		s.append("</HTML>");
		System.out.println("wordsToFind: " + game.wordsToFind);
		return s.toString();
	}

	
    /**
     * Method called when model sends update notification.
     *
     * @param o     The model.
     * @param arg   The UpdateAction.
     */
    public void update(Observable o, Object arg) {
        switch ((UpdateAction)arg) {
            case WORD_FOUND:
            	status.setText("Word Found");
            	words.setText(buildWordsToFindList());
            	
        		//display memoranda for specific duration
        		resetText = new TimerTask() {
        			public void run() {
        				SwingUtilities.invokeLater(new Runnable() {
        					public void run() {
        						status.setText("Playing");
        					}
        				});
        			}
        		};            	
            	
        		timer.schedule(resetText, displayNotificationDuration);		
                break;
            case WORD_NOT_FOUND:
            	status.setText("invalid word");
            	
        		//display memoranda for specific duration
        		resetText = new TimerTask() {
        			public void run() {
        				SwingUtilities.invokeLater(new Runnable() {
        					public void run() {
        						status.setText("Playing");
        					}
        				});
        			}
        		};            	
            	
            	timer.schedule(resetText, displayNotificationDuration);	
            	break;
        }
    }

}
