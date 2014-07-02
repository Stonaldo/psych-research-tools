package stone.javatasks.sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import stone.javatasks.control.executables.SudokuTrial;

public class SudokuAsPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Game game;
	private SudokuPanel sudokuPanel;
	private ButtonPanel buttonPanel;

	public SudokuAsPanel(SudokuTrial st, int n) {
        super();
        this.setBorder(BorderFactory.createTitledBorder("Sudoku Puzzle"));
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(900,500));

        Game game = new Game(n);

        ButtonController buttonController = new ButtonController(game);
        buttonPanel = new ButtonPanel();
        buttonPanel.setController(buttonController);
        add(buttonPanel, BorderLayout.EAST);

        sudokuPanel = new SudokuPanel();
        SudokuController sudokuController = new SudokuController(sudokuPanel, game);
        sudokuPanel.setGame(game);
        sudokuPanel.setController(sudokuController);
        add(sudokuPanel, BorderLayout.CENTER);

        game.addObserver(buttonPanel);
        game.addObserver(sudokuPanel);
        game.addObserver(st);
        
        setVisible(true);
    }
	
	public SudokuAsPanel(int n) {
        super();
        setLayout(new BorderLayout());

        Game game = new Game(n);

        ButtonController buttonController = new ButtonController(game);
        ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.setController(buttonController);
        add(buttonPanel, BorderLayout.EAST);

        SudokuPanel sudokuPanel = new SudokuPanel();
        SudokuController sudokuController = new SudokuController(sudokuPanel, game);
        sudokuPanel.setGame(game);
        sudokuPanel.setController(sudokuController);
        add(sudokuPanel, BorderLayout.CENTER);

        game.addObserver(buttonPanel);
        game.addObserver(sudokuPanel);
        
        setVisible(true);
    }
	
	public void setAllBackgrounds(Color c) {
		this.setBackground(c);
		buttonPanel.setAllBGs(c);
		sudokuPanel.setAllBGs(c);
	}
}