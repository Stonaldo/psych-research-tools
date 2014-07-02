package testing.wordsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import stone.javatasks.sudoku.Field;

public class WordSearchController implements MouseListener {
    private WordSearchPanel wsPanel;    // Panel to control.
    private WordSearchGame game;                  // Current Wordsearch game.
    final Color c1 = new Color(16,59,179);
    final Color c2 = new Color(180,194,184);

    /**
     * Constructor, sets game.
     *
     * @param game  Game to be set.
     */
    public WordSearchController(WordSearchPanel wsPanel, WordSearchGame game) {
        this.wsPanel = wsPanel;
        this.game = game;
    }


     /**
     * @param e MouseEvent.
     */
    public void mousePressed(MouseEvent e) {
        JPanel panel = (JPanel)e.getSource();
        Component component = panel.getComponentAt(e.getPoint());
        if (component instanceof Field) {
            Field field = (Field)component;
            int x = field.getFieldX();
            int y = field.getFieldY();
            
            field.setForeground(c1);
            game.addField(field);
        }
    }

    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
}
