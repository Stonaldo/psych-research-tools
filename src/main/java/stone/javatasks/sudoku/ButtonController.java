package stone.javatasks.sudoku;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

/**
 * This class controls all user actions from ButtonPanel.
 *
 * @author Eric Beijer
 */
public class ButtonController implements ActionListener {
    private Game game;

    /**
     * Constructor, sets game.
     *
     * @param game  Game to be set.
     */
    public ButtonController(Game game) {
        this.game = game;
    }

    /**
     * Performs action after user pressed button.
     *
     * @param e ActionEvent.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("New"))
            game.newGame();
        else if (e.getActionCommand().equals("Check"))
            game.checkGame();
        else if (e.getActionCommand().equals("Exit"))
            game.tryEnd();
        else if (e.getActionCommand().equals("Help on"))
            game.setHelp(((JCheckBox)e.getSource()).isSelected());
        else
            game.setSelectedNumber(Integer.parseInt(e.getActionCommand()));
    }
}