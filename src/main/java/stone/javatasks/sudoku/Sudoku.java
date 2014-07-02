package stone.javatasks.sudoku;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.UIManager;

import stone.javatasks.control.executables.SudokuTrial;

/**
 * Main class of program.
 *
 * @author Eric Beijer
 */
public class Sudoku extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Game game;

	public Sudoku(SudokuTrial st) {
        super("Sudoku");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        Game game = new Game(1);

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
        game.addObserver(st);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Main entry point of program.
     * 
     * @param args Command line arguments.
     */
	/*
    public static void main(String[] args) {
        // Use System Look and Feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ex) { ex.printStackTrace(); }
        new Sudoku();
    }
    */
}