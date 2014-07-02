package testing.wordsearch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

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
    	game.checkWord();
    }

}
