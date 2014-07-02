package stone.javatasks.sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * This class draws the button panel and reacts to updates from the model.
 *
 * @author Eric Beijer
 */
public class ButtonPanel extends JPanel implements Observer {
    JButton btnNew, btnCheck, btnExit;   // Used buttons.
    JCheckBox cbHelp;               // Used check box.
    ButtonGroup bgNumbers;          // Group for grouping the toggle buttons.
    JToggleButton[] btnNumbers;     // Used toggle buttons.
    JLabel gameStatus;
    
    JPanel pnlAlign;
    JPanel pnlOptions;
    JPanel pnlNumbers;
    JPanel pnlNumbersHelp;
    JPanel pnlNumbersNumbers;
    
    JLabel labHelp;
    int numToDisableHelp;
    int numHelped;
    
    final Font treb = new Font("Trebuchet MS", 1, 26);

    /**
     * Constructs the panel and arranges all components.
     */
    public ButtonPanel() {
        super(new BorderLayout());
        
        this.numToDisableHelp = 5;
        this.numHelped = 0;

        pnlAlign = new JPanel();
        pnlAlign.setLayout(new BoxLayout(pnlAlign, BoxLayout.PAGE_AXIS));
        add(pnlAlign, BorderLayout.NORTH);

        pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEADING));
        pnlOptions.setBorder(BorderFactory.createTitledBorder(" Options "));
        pnlAlign.add(pnlOptions);

        btnNew = new JButton("New");
        btnNew.setFocusable(false);
        //pnlOptions.add(btnNew);

        btnCheck = new JButton("Check");
        btnCheck.setFocusable(false);
        pnlOptions.add(btnCheck);

        btnExit = new JButton("Exit");
        btnExit.setFocusable(false);
        pnlOptions.add(btnExit);

        pnlNumbers = new JPanel();
        pnlNumbers.setLayout(new BoxLayout(pnlNumbers, BoxLayout.PAGE_AXIS));
        pnlNumbers.setBorder(BorderFactory.createTitledBorder(" Numbers "));
        pnlAlign.add(pnlNumbers);

        pnlNumbersHelp = new JPanel(new FlowLayout(FlowLayout.LEADING));
        pnlNumbers.add(pnlNumbersHelp);

        cbHelp = new JCheckBox("Help on", false);
        cbHelp.setFocusable(false);
        pnlNumbersHelp.add(cbHelp);
        
        labHelp = new JLabel("(remaining: " + (numToDisableHelp - numHelped) + ")");
        pnlNumbersHelp.add(labHelp);

        pnlNumbersNumbers = new JPanel(new FlowLayout(FlowLayout.LEADING));
        pnlNumbers.add(pnlNumbersNumbers);

        bgNumbers = new ButtonGroup();
        btnNumbers = new JToggleButton[9];
        for (int i = 0; i < 9; i++) {
            btnNumbers[i] = new JToggleButton("" + (i + 1));
            btnNumbers[i].setPreferredSize(new Dimension(40, 40));
            btnNumbers[i].setFocusable(false);
            bgNumbers.add(btnNumbers[i]);
            pnlNumbersNumbers.add(btnNumbers[i]);
        }
        
        gameStatus = new JLabel("Playing");
        gameStatus.setFont(treb);
        gameStatus.setHorizontalAlignment(SwingConstants.CENTER);
        add(gameStatus, BorderLayout.SOUTH);
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
            case CHECK:
                bgNumbers.clearSelection();
                break;
            case WON:
            	gameStatus.setText("You Win!");
            	break;
            case ERRORS:
            	gameStatus.setText("Uh oh, that's not right");
            case ITEM_WITH_HELP:
            	this.numHelped += 1;
            	labHelp.setText("(remaining: " + (this.numToDisableHelp - this.numHelped) + ")");
            	if (this.numHelped == this.numToDisableHelp)
            		disableTheHelp();
            case INCREMENT_HELP:
            	this.numHelped += 1;
            	labHelp.setText("(remaining: " + (this.numToDisableHelp - this.numHelped) + ")");
            	if (this.numHelped == this.numToDisableHelp)
            		disableTheHelp();
        }
    }
    
    /**
     * Method to disable the help
     */
    public void disableTheHelp() {
		System.out.println("reached limit, help should disable");
		cbHelp.doClick();
		cbHelp.setEnabled(false);    	
    }

    /**
     * Adds controller to all components.
     *
     * @param buttonController  Controller which controls all user actions.
     */
    public void setController(ButtonController buttonController) {
        btnNew.addActionListener(buttonController);
        btnCheck.addActionListener(buttonController);
        btnExit.addActionListener(buttonController);
        cbHelp.addActionListener(buttonController);
        for (int i = 0; i < 9; i++)
            btnNumbers[i].addActionListener(buttonController);
    }
    
    public void setAllBGs(Color c) {
    	this.setBackground(c);
        pnlAlign.setBackground(c);
        pnlOptions.setBackground(c);
        pnlNumbers.setBackground(c);
        pnlNumbersHelp.setBackground(c);
        pnlNumbersNumbers.setBackground(c);
    }
}