package stone.javatasks.stacker;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import stone.javatasks.stacker.ButtonController;

public class ButtonPanel extends JPanel {
	
	JButton startGame, resetGame;
	
	public ButtonPanel() {
		super(new BorderLayout());
		startGame = new JButton("New Game");
		resetGame = new JButton("Reset Game");
		startGame.setFocusable(false);
		
		add(startGame, BorderLayout.WEST);
		add(resetGame, BorderLayout.EAST);
	}
	
    public void setController(ButtonController buttonController) {
        startGame.addActionListener(buttonController);
        resetGame.addActionListener(buttonController);
    }
	
}
