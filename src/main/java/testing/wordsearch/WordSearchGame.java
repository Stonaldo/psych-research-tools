package testing.wordsearch;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import stone.javatasks.sudoku.Field;

public class WordSearchGame extends Observable{
	char[][] data;	// Puzzle without random filler characters
	char[][] dataF; // Puzzle with    random filler characters
	
	String[] words;
    
	
	List<String> wordsToFind = new ArrayList<String>();
	
	ArrayList<Field> fieldsGiven = new ArrayList<Field>();
	
	public WordSearchGame(String[] wordList, int size) {
		data = new char[size][size];
		
		for(int i=0; i<data.length; i++)
			for(int j=0; j<data.length; j++)
				data[i][j] = ' ';
		
		for(String word : wordList) {
			add(word, data);
		}
		
		dataF = fill(data);
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		
		ret.append("Puzzle:\n");
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data.length; j++) {
				ret.append(dataF[i][j]);
			}
			ret.append("\n");
		}
		
		ret.append("\n");
		
		ret.append("Solution:\n");
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data.length; j++) {
				ret.append(data[i][j]);
			}
			ret.append("\n");
		}
		
		return ret.toString();
	}
	
	public String getPuzzle() {
		StringBuilder ret = new StringBuilder();
		
		for(int i=0; i<dataF.length; i++) {
			for(int j=0; j<dataF.length; j++) {
				ret.append(dataF[i][j]);
			}
			ret.append("\n");
		}
		
		return ret.toString();
	}
	
	public String getSolution() {
		StringBuilder ret = new StringBuilder();
		
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data.length; j++) {
				ret.append(data[i][j]);
			}
			ret.append("\n");
		}
		
		return ret.toString();
	}
	
	private boolean add(String word, char[][] puzzle) {
		word = word.toUpperCase();
		String originalWord = word;
		
		char[][] origPuzzle = new char[puzzle.length][puzzle.length];
		for(int i=0; i<puzzle.length; i++)
			for(int j=0; j<puzzle.length; j++)
				origPuzzle[i][j] = puzzle[i][j];
		
		for(int tries=0; tries<100; tries++) {
			Random r = new Random();
			
			int orientation = r.nextInt(2); // 0 = Forwards,   1 = Backwards
			if(orientation == 1) word = flip(word);
			
			int direction   = r.nextInt(3); // 0 = Horizontal, 1 = Vertical,  2 = Diagonal
			
			int row			= r.nextInt(puzzle.length - word.length());
			int col			= r.nextInt(puzzle.length - word.length());
			
			int i=0;
			for(i=0; i<word.length(); i++) {
				if(puzzle[row][col] == ' ' || puzzle[row][col] == word.charAt(i)) {
					puzzle[row][col] = word.charAt(i);
					
					if(direction == 0) col++;
					if(direction == 1) row++;
					if(direction == 2) { col++; row++; }
					if(i+1 == word.length()){wordsToFind.add(originalWord);}
				} else {
					for(int j=i; j>0; j--) {
						if(direction == 0) col--;
						if(direction == 1) row--;
						if(direction == 2) { col--; row--; }
						
						puzzle[row][col] = origPuzzle[row][col]; 
					}
					break;
				}
			}
			if(--i > 0) {
				return true;
			}
		}
		return false;
	}
	
	private String flip(String in) {
		StringBuilder ret = new StringBuilder();
		for(int i=in.length()-1; i>=0; i--)
			ret.append(in.charAt(i));
		return ret.toString();
	}
	
	private char[][] fill(char[][] puzzle) {
		char[][] ret = new char[puzzle.length][puzzle.length];
		RandChar r = new RandChar();
		
		for(int i=0; i<ret.length; i++) {
			for(int j=0; j<ret.length; j++) {
				if(puzzle[i][j] != ' ') {
					ret[i][j] = puzzle[i][j];
				} else {
					ret[i][j] = r.nextChar();
				}
			}
		}
		
		return ret;
	}
	
    /**
     * Sets given number on given position in the game.
     *
     * @param x         The x position in the game.
     * @param y         The y position in the game.
     * @param c    		The char to be set.
     */
    public void setChar(int x, int y, char c) {
        dataF[y][x] = c;
    }

    /**
     * Returns number of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      char at given position.
     */
    public char getChar(int x, int y) {
        return dataF[y][x];
    }
    
    public void checkWord() {
    	String wordToCheck = buildWord();
    	System.out.println("wordToCheck: " + wordToCheck);
    	if (wordsToFind.contains(wordToCheck)) {
    		wordsToFind.remove(wordsToFind.indexOf(wordToCheck));
    		setChanged();
    		notifyObservers(UpdateAction.WORD_FOUND);
    	} else {
    		removeFields();
    		setChanged();
    		notifyObservers(UpdateAction.WORD_NOT_FOUND);
    	}
    }
    
    private String buildWord() {
    	StringBuilder s = new StringBuilder();
    	for (int i = 0; i < fieldsGiven.size(); i++) {
    		int x = fieldsGiven.get(i).getFieldX();
    		int y = fieldsGiven.get(i).getFieldY();
    		s.append(getChar(x,y));
    	}
    	return s.toString();
    }
    
    public void addField(Field f) {
    	fieldsGiven.add(f);
    }
    public void removeFields() {
    	fieldsGiven.clear();
    }
    
}
