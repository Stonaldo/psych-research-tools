package stone.javatasks.stacker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import stone.javatasks.control.executables.StackerTrial;

/**
 * 
 * @author James Stone
 *
 */

public class Stacker extends JPanel implements KeyListener {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int iteration;
	private double time;
	private int last;
	static int m = 10;
	static int n = 20;
	JLabel[][] b;
	static int[] length = { 5, 5 };;
	private int layer;
	private int[] deltax;
	private boolean press;
	private boolean forward;
	private boolean start;
	private GoThread gameTask;
	
	public boolean newGameFlag = true;
	
	
	protected StackerTrial st;
	
	/**
	 * Resets all the variables to their initial state ready for a new game to start.
	 * 
	 * @param args None
	 */
	private void initVarsForNewGame() {
		System.out.println("initVarsForNewGame method called!");
		this.iteration = 1;
		this.time = 200;
		this.last = 0;
		this.layer = 19;
		this.deltax = new int[2];
		this.press = false;
		this.forward = true;
		this.start = true;
		length[0] = 5;
		length[1] = 5;
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				this.b[j][i].setBackground(Color.white);
			}
		}
	}

	/**
	 * Creates a JPanel object that displays the GUI of the Stacker game. 
	 */
	public Stacker(StackerTrial st) {
		super();
		
		this.st = st;
		
		setLayout(new GridLayout(n, m));
		
		this.b = new JLabel[m][n];
	  
		for (int y = 0; y < n; y++) {
			for (int x = 0; x < m; x++) {
				this.b[x][y] = new JLabel(" ");
				this.b[x][y].setBackground(Color.white);
				add(this.b[x][y]);
				this.b[x][y].setEnabled(true);
				this.b[x][y].setOpaque(true);
				this.b[x][y].setBorder(BorderFactory.createLineBorder(Color.GRAY));
				this.b[x][y].setPreferredSize(new Dimension(40, 30));
				this.b[x][y].setMaximumSize(new Dimension(40, 30));
			}
		}
		
		setFocusable(true);
		addKeyListener(this);
	}
	
	/**
	 * Runs a game of Stacker. Handles the movement of the blocks, reacts to spacebar presses, 
	 * and ends the game when the player wins/loses.
	 */
	public void go() {
		
		if (this.newGameFlag)
			initVarsForNewGame();
			this.newGameFlag = false;
		
		requestFocusInWindow();
		
		int tmp = 0;
		Component temporaryLostComponent = null;
		
		do {
			if (forward)
				forward();
			else
				back();
			
			if (deltax[1] == 10 - length[1])
				forward = false;
			else if (deltax[1] == 0) {
				forward = true;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
						draw();
				}
			});
			
			try {
				Thread.sleep((long)time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} while (!press);
		
		if (layer > 12)
			time = 150 - (this.iteration * this.iteration * 2 - this.iteration);
		else
			time -= 2.2;
		
		this.iteration += 1;
		layer -= 1;
		press = false;
		tmp = check();
		length[0] = length[1];
		length[1] = tmp;
		
		if ((layer == -1) && (length[1] > 0)) {
			st.endTrial(true, 18-layer);
			gameTask.stop();
		}
		
		if (length[1] <= 0) {
			st.endTrial(false, 18-layer);
			//JOptionPane.showMessageDialog(temporaryLostComponent, "Game over! You reached line " + (18 - layer) + "!");
			//System.exit(0); //if you want the program to exit after every game.
			gameTask.stop();
		}
		
		last = deltax[1];
		start = false;
		go();
	}
	
	public int check() {
    
		if (start)
			return length[1];
    
		if (last < deltax[1]) {
			if (deltax[1] + length[1] - 1 <= last + length[0] - 1) {
				return length[1];
			}
			return length[1] - Math.abs(deltax[1] + length[1] - (last + length[0]));
		}
		
		if (last > deltax[1]) {
			return length[1] - Math.abs(deltax[1] - last);
		}
		
		return length[1];
	}

  	public void forward() {
  		//System.out.println("forward method called");
  		deltax[0] = deltax[1];
  		deltax[1] += 1;
  	}

  	public void back() {
  		//System.out.println("back method called");
  		deltax[0] = deltax[1];
  		deltax[1] -= 1;
  	}

  	/**
  	 * Uses the current values of deltax/length/layer to update the backgrounds 
  	 * of the appropriate grids.
  	 */
  	public void draw() {
  		
  		//System.out.println("draw method called");
  		
  		System.out.println("deltax[0]: " + deltax[0]);
  		System.out.println("deltax[1]: " + deltax[1]);
  		
  		for (int x = 0; x < length[1]; x++) {
  			this.b[(x + deltax[0])][layer].setBackground(Color.white);
  		}

  		for (int x = 0; x < length[1]; x++) {
  			this.b[(x + deltax[1])][layer].setBackground(Color.BLUE);
  			//this.b[(x + deltax[1])][layer].grabFocus();
  			//this.b[(x + deltax[1])][layer].repaint();
  			//this.b[(x + deltax[1])][layer].revalidate();
  			//System.out.println("this.b[(x + deltax[1])][layer]: " + this.b[(x + deltax[1])][layer].getBackground());
  		}
  	}

  	public void keyPressed(KeyEvent e) {
  		if (e.getKeyCode() == 32) {//spacebar pressed
  			press = true;
  			System.out.println("keyboard pressed");
  		}
  	}

  	public void keyReleased(KeyEvent arg0) {}

  	public void keyTyped(KeyEvent arg0) {}

  	
  	public void runGame() {
  		st.updateText("<html>Spacebar to stop the blocks<br />Get as high as you can!</html");
  		st.setStartTime();
  		this.grabFocus();
		gameTask = new GoThread();
		gameTask.start();  		
  	}
  	
  	public void resetGame() {
  		initVarsForNewGame();
  		gameTask.stop();
  	}
  	
  	public class GoThread extends Thread {
  		public void run() {
  			newGameFlag = true;
  			go();
  		}
  	}

}

