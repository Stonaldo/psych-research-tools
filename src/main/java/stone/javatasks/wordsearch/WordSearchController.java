package stone.javatasks.wordsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import stone.javatasks.sudoku.Field;

public class WordSearchController implements MouseListener, Observer {
    private WordSearchPanel wsPanel;    // Panel to control.
    private WordSearchGame game;                  // Current Wordsearch game.
    final Color c1 = new Color(16,59,179);
    final Color c2 = new Color(180,194,184);
    private int[] currentlySelectedField;

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
     * Method called when model sends update notification.
     *
     * @param o     The model.
     * @param arg   The UpdateAction.
     */
    public void update(Observable o, Object arg) {
    	switch ((UpdateAction)arg) {
    		case WORD_NOT_FOUND:
    			break;	
    	}
    	
    }
            	
     


     /**
     * @param e MouseEvent.
     */
    public void mousePressed(MouseEvent e) {
        JPanel panel = (JPanel)e.getSource();
        Component component = panel.getComponentAt(e.getPoint());
        if (component instanceof Field) {
        	
        	if (game.fieldsGiven.isEmpty())
        		currentlySelectedField = null;
        	
            Field field = (Field)component;
            int x = field.getFieldX();
            int y = field.getFieldY();
            
            if (currentlySelectedField == null) {
            	currentlySelectedField = new int[2];
                field.setForeground(c2);
                currentlySelectedField[0] = x;
                currentlySelectedField[1] = y;
                game.addField(field); 
                
            } else {
            	if (x == currentlySelectedField[0] | x == currentlySelectedField[0] + 1 | x == currentlySelectedField[0] - 1) {
            		if (y == currentlySelectedField[1] | y == currentlySelectedField[1] + 1 | y == currentlySelectedField[1] - 1) {
                        field.setForeground(c2);
                        currentlySelectedField[0] = x;
                        currentlySelectedField[1] = y;
                        game.addField(field);               			
            		}
            	}
            }
            

        }
    }

    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
}
