package testing.wordsearch;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import stone.javatasks.sudoku.Field;

/**
 * this class will take a generated wordsearch of char[][] and construct
 * a jpanel to display the wordsearch in an interactive format.
 * 
 * @author stonej
 *
 */

public class WordSearchPanel extends JPanel implements Observer {
	
	private Field[][] fields; //array of fields
	private JPanel[][] panels; //panels to hold the fields
	private int size;
	private int clicks;
	final Color c2 = new Color(180,194,184);
	
	/*
	 * construct an overall panel, containing the subpanels, which in turn hold the fields.
	 */
	
	public WordSearchPanel(int size) {
		super(new GridLayout(size, size));
		this.size = size;
		
		panels = new JPanel[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
            	panels[y][x] = new JPanel();
                panels[y][x].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                add(panels[y][x]);
            }
        }
        
        fields = new Field[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fields[y][x] = new Field(x, y);
                panels[y][x].add(fields[y][x]);
            }
        }
        
	}
	
    /**
     * Method called when model sends update notification.
     *
     * @param o     The model.
     * @param arg   The UpdateAction.
     */
    public void update(Observable o, Object arg) {
        switch ((UpdateAction)arg) {
            case NEW_GAME:
                setGame((WordSearchGame)o);
                break;
            case WORD_FOUND:
            	updatePanelCorrect((WordSearchGame)o);
            	break;
            case WORD_NOT_FOUND:
            	updatePanelIncorrect((WordSearchGame)o);
            	break;
        }
    }
    
    public void updatePanelCorrect(WordSearchGame wsg) {
    	for (Field f: wsg.fieldsGiven) {
    		f.setBackground(c2);
    		f.setForeground(Color.BLACK);
    	}
    	wsg.fieldsGiven.clear();
    }
    
    public void updatePanelIncorrect(WordSearchGame wsg) {
    	for (Field f: wsg.fieldsGiven) {
    		f.setBackground(Color.WHITE);
    		f.setForeground(Color.BLACK);
    	}
    	wsg.fieldsGiven.clear();
    }
    
    /**
     * Sets the fields corresponding to given game.
     *
     * @param game  Game to be set.
     */
    public void setGame(WordSearchGame game) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fields[y][x].setBackground(Color.WHITE);
                fields[y][x].setChar(game.getChar(x, y));
            }
        }
    }
    
    /**
     * Adds controller to all sub panels.
     *
     * @param WordSearchController  Controller which controls all user actions.
     */
    public void setController(WordSearchController wsController) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++)
                panels[y][x].addMouseListener(wsController);
        }
    }
	
}
