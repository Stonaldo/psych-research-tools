package stone.javatasks.wordsearch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * This class controls all user actions from ButtonPanel.
 *
 * @author Eric Beijer
 */
public class ButtonControllerWS implements ActionListener {
    private WordSearchGame game;

    /**
     * Constructor, sets game.
     *
     * @param game  Game to be set.
     */
    public ButtonControllerWS(WordSearchGame game) {
        this.game = game;
    }

    /**
     * Performs action after user pressed button.
     *
     * @param e ActionEvent.
     */
    public void actionPerformed(ActionEvent e) {
    	JButton butClicked = (JButton) e.getSource();
    	
    	if (butClicked.getText() == "Submit")
    		game.checkWord();

    	if (butClicked.getText() == "QUIT")
    		game.quitTrial();
    }

}
