package testing.wordsearch;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class WordSearch extends JFrame {
	
	public WordSearch(WordSearchGame w) {
	    super("WordSearch");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    getContentPane().setLayout(new BorderLayout());
	    
	    WordSearchPanel wsPanel = new WordSearchPanel(w.dataF.length);
	    
	    wsPanel.setGame(w);
	    WordSearchController wsController = new WordSearchController(wsPanel, w);
	    wsPanel.setController(wsController);

	    add(wsPanel, BorderLayout.CENTER);
	    
	    InfoPanel ip = new InfoPanel(w);
	    
	    add(ip, BorderLayout.EAST);

	    w.addObserver(wsPanel);
	    w.addObserver(ip);

	    pack();
	    setLocationRelativeTo(null);
	    setVisible(true);		
	}

}
