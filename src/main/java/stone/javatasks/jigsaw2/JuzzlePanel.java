/*
 * A puzzle game written in Java.
 *
 * Please read "http://juzzle.sourceforge.net/juzzle_licence.txt" for copyrights.
 * 
 * The sourcecode is designed and created with
 * Sun J2SDK 1.3 and Microsoft Visual J++ 6.0
 *
 * Juzzle homepage: http://juzzle.sourceforge.net
 *
 * autor: Slawa Weis
 * email: slawaweis@animatronik.net
 *
 */

package stone.javatasks.jigsaw2;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import stone.javatasks.control.executables.JigsawTrial;
import stone.javatasks.jigsaw2.images.JuzzleImages;

/**
 * the main panel for Juzzle, it containt the ControlPanel and the PuzzlePanel
 *
 * @see     org.game.Juzzle.ControlPanel
 * @see     org.game.Juzzle.PuzzlePanel
 */
public class JuzzlePanel extends JDesktopPane
{
 /**
  * current verion of the program
  */
 public static final String version = "0.5";
 /**
  * text as HTML for the about dialog
  */
 public static final String ABOUT_TEXT =
 "<html>" + 
 "Juzzle - a puzzle game powered by Java<br><br>" + 
 "Autor: Slawa Weis<br>" + 
 "Email: <u><font color=#0000FF>slawaweis@animatronik.net</font></u><br>" + 
 "Homepage: <u><font color=#0000FF>http://juzzle.sourceforge.net</font></u><br><br>" + 
 "the most images comes from:<br>" + 
 "<u><font color=#0000FF>http://www.freeimages.co.uk</font></u><br>" + 
 "<u><font color=#0000FF>http://www.freeimages.com</font></u>" + 
 "</html>" 
 ;

 /**
  * counter for the user images. Used for names of the images:<br>
  * <br>
  * user image 1<br>
  * user image 2<br>
  * user image 3<br>
  * ...<br>
  *
  */
 protected static int userImage = 1;

 /**
  * reference to the ScrollPanel of the PuzzlePanel
  */
 protected JScrollPane pzp_jsp = null;
 /**
  * reference to the PuzzlePanel
  */
 protected PuzzlePanel pzp = null;
 /**
  * reference to the Frame of the ControlPanel
  */
 protected JInternalFrame controlFrame = null;
 /**
  * reference to the ControlPanel
  */
 protected ControlPanel controlPanel = null;

 /**
  * reference to the StartDialog
  */
 protected StartDialog startDialog = null;

 /**
  * reference to the callback function for the menu
  */
 protected MenuListener menuListener = new MenuListener();

 /**
  * array with ImageDescription's, from JuzzleImages
  *
  * @see     org.game.Juzzle.ImageDescription
  * @see     org.game.Juzzle.images.JuzzleImages
  */ 
 protected Vector imagesList = null; 
 
 protected JigsawTrial jt;
 
 /**
  * simply constructor
  */
 public JuzzlePanel(JigsawTrial jt)
  {
  super();
  this.jt = jt;
  

  // create the control panel
  controlPanel = new ControlPanel(this);

  // create puzzle panel and the scroller for it
  pzp = new PuzzlePanel(controlPanel);
  pzp_jsp = new JScrollPane(pzp);
  pzp_jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
  pzp_jsp.setOpaque(false);
  pzp_jsp.getViewport().setOpaque(false);

  // set the puzzle panel scroller as default layer, that mean under the ControlPanel frame
  add(pzp_jsp, JDesktopPane.DEFAULT_LAYER);
  addComponentListener(new JuzzlePanelResizer());

  // creates the frame for the ControlPanel
  controlFrame = new JInternalFrame("test", false, false, false, false);
//  controlFrame.setMinimumSize(new Dimension(200, 200));
  controlFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
  controlFrame.getContentPane().add(controlPanel);
  controlFrame.setLocation(10, 10);
  controlFrame.pack();
  add(controlFrame);
  controlFrame.setVisible(true);

  // creates the menu bar for the ControlPanel
  JMenuBar jmb = new JMenuBar();
  JMenu jm = null;  

  jm = new JMenu("Game");
  //jm.add(createMenuItem("New", "new.gif"));
  //jm.add(createMenuItem("Open", "open.gif"));
  //jm.add(createMenuItem("Reset", "reset.gif"));
  //jm.addSeparator();
  //jm.add(createMenuItem("About", "about.gif"));
  //jm.addSeparator();
  jm.add(createMenuItem("Quit", "quit.gif"));
  jmb.add(jm);

  jm = new JMenu("View");
  //jm.add(createMenuItem2("Antialiasing", false));
  jm.add(createMenuItem2("Outline", true));
  jm.add(createMenuItem2("Shadow", true));
  jmb.add(jm);

  controlFrame.setJMenuBar(jmb);
  
  // get the images
  imagesList = JuzzleImages.getImages();
  
  System.out.println("imagesList: " + imagesList);
  Random r = new Random();
  int dims = jt.getStartingNumOfDimensions() + jt.currentLevel;
  
  startGame((ImageDescription)imagesList.get(r.nextInt(imagesList.size())), new Dimension(dims,dims));
  }

 /**
  * help function. Creates a menu item and adds the callback function to it.
  *
  * @param name label for menu item
  * @param icon icon for menu item
  * @return the menu item
  */
 protected JMenuItem createMenuItem(String name, String icon)
  {
  JMenuItem jmi = new JMenuItem(name, JuzzleImages.getIcon(icon));
  jmi.addActionListener(menuListener);
  return jmi;
  }

 /**
  * help function. Creates a checked menu item and adds the callback function to it.
  *
  * @param name label for menu item
  * @param set checked or not
  * @return the checked menu item
  */
 protected JCheckBoxMenuItem createMenuItem2(String name, boolean set)
  {
  JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(name, set);
  jmi.addActionListener(menuListener);
  return jmi;
  }

 /**
  * starts a new game and reset the system with new parameters
  */
 public void startGame(ImageDescription id, Dimension d)
  {
    Dimension div = d;
    ImageIcon imageIcon = id.imageIcon;
    Image     image     = imageIcon.getImage();
    BufferedImage bimage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
    bimage.getGraphics().drawImage(image, 0, 0, null);
//  int pxc = 8, pyc = 10;
//  int pxc = 2, pyc = 3;

    // set the game parameters to the PuzzlePanel
    pzp.setGame(bimage, div.width, div.height, id.name);
    controlPanel.setImage(image);
//  controlFrame.setVisible(true);
    pzp.repaint();
  }
 
 public void endThisPuzzle(int timeTaken, String pic, int numPiece) {
	 System.out.println("jt.endTrial() called.");
	 jt.endTrial(timeTaken, pic, numPiece, true);
 }

 /**
  * callback function for the menu items
  */
 public class MenuListener implements ActionListener
 {
  public void actionPerformed(ActionEvent e)
   {
        // reset the current game
   if(e.getActionCommand().equals("Reset"))
          {
          pzp.resetGame();
          }
        // shows the about dialog
   else if(e.getActionCommand().equals("About"))
          {
          JOptionPane.showMessageDialog(JuzzlePanel.this, ABOUT_TEXT);
          }
        // exit the program
   else if(e.getActionCommand().equals("Quit"))
          {
	   		if (jt.getSessionTime() > jt.TimerLimit)
	   			jt.endTrial(controlPanel.gameTime, "userCancelled", 99, false);
          }
        // disable or enable antialiasing
   else if(e.getActionCommand().equals("Antialiasing"))
          {
          pzp.setAntialiasing(((JCheckBoxMenuItem)e.getSource()).isSelected());
          }
        // disable or enable pieces outlines
   else if(e.getActionCommand().equals("Outline"))
          {
          pzp.setOutline(((JCheckBoxMenuItem)e.getSource()).isSelected());
          }
        // disable or enable pieces shadow while dragging
   else if(e.getActionCommand().equals("Shadow"))
          {
          pzp.setShadow(((JCheckBoxMenuItem)e.getSource()).isSelected());
          }
   }
 }

 /**
  * needed to resize the puzzle panel scroller, because the layout for the JDesktopPane is null.
  */
 public class JuzzlePanelResizer extends ComponentAdapter
 {
  public void componentShown(ComponentEvent e)
   {
   pzp_jsp.setSize(getSize());
   pzp_jsp.revalidate();
   }

  public void componentResized(ComponentEvent e)
   {
   pzp_jsp.setSize(getSize());
   pzp_jsp.revalidate();
   }
 }

 /**
  * file filter for image loading. Loads only JPEG and GIF
  */
 public class ImageFileFilter extends javax.swing.filechooser.FileFilter
 {
  /**
   * every directory and files with endings *.jpg, *.jpeg, *.gif are accepted
   *
   * @param f the file
   * @return accepted or not
   */
  public boolean accept(File f)
   {
   return (f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".gif") || f.isDirectory());
   }
  /**
   * return the string to display it in the file chooser dialog
   *
   * @return the description string
   */
  public String getDescription()
   {
   return "Images (*.jpg;*.jpeg;*.gif)";
   }
 }
}
