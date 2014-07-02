package testing;

import javax.swing.JFrame;
import javax.swing.UIManager;

import stone.javatasks.sudoku.SudokuAsPanel;

public class TestingSudokuAsPanel extends JFrame{
	
	public TestingSudokuAsPanel() {
		super("Sudoku");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        add(new SudokuAsPanel(10));
		
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
	}
	
	
	

	public static void main(String[] args) {
        // Use System Look and Feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ex) { ex.printStackTrace(); }
        new TestingSudokuAsPanel();		
	}

}
