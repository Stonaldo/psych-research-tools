package stone.javatasks.wordsearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import stone.javatasks.control.executables.WordsearchTrial;

public class WordSearch extends JPanel {
	
	InfoPanel ip;
	WordSearchPanel wsPanel;
	protected WordsearchTrial wt;
	WordSearchGame w;
	
	public WordSearch(WordsearchTrial wsTrial, WordSearchGame w) {
	    super();
	    this.wt = wsTrial;
	    this.w = w;
	    
	    this.setBorder(BorderFactory.createTitledBorder("Wordsearch"));
	    setLayout(new BorderLayout());
	    setMaximumSize(new Dimension(700,500));

	    wsPanel = new WordSearchPanel(w.dataF.length);
	    
	    wsPanel.setGame(w);
	    WordSearchController wsController = new WordSearchController(wsPanel, w);
	    wsPanel.setController(wsController);

	    add(wsPanel, BorderLayout.CENTER);
	    
	    ip = new InfoPanel(w);
	    
	    add(ip, BorderLayout.EAST);

	    w.addObserver(wsPanel);
	    w.addObserver(ip);
	    w.addObserver(wsTrial);
	}
	
	public void setAllBGs(Color c) {
		this.setBackground(c);
		ip.setAllBGs(c);
		wsPanel.setAllBGs(c);
		wsPanel.removeFieldBorders();
	}

}
