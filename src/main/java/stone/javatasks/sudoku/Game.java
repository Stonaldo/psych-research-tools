package stone.javatasks.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * This class represents a Sudoku game. It contains the solution, the user
 * input, the selected number and methods to check the validation of the user
 * input.
 *
 * @author Eric Beijer
 */
public class Game extends Observable {
    private int[][] solution;       // Generated solution.
    private int[][] game;           // Generated game with user input.
    private boolean[][] check;      // Holder for checking validity of game.
    private int selectedNumber;     // Selected number by user.
    private boolean help;           // Help turned on or off.

    /**
     * Constructor with no specified number of spaces to fill.
     */
    public Game() {
        newGame();
        check = new boolean[9][9];
        help = false;
        /*System.out.println("this is the solution: ");
        print(solution);
        System.out.println("this is the game: ");
        print(game);*/
    }
    
    /**
     * constructor with specified number of spaces to fill.
     */
    public Game(int gaps) {
        newGame(gaps);
        check = new boolean[9][9];
        help = false;
        /*System.out.println("this is the solution: ");
        print(solution);
        System.out.println("this is the game: ");
        print(game);*/
    }
    

    /**
     * Generates a new Sudoku game.<br />
     * All observers will be notified, update action: new game.
     */
    public void newGame() {
    	//System.out.println("----------newGame() called--------------");
    	//System.out.println("------------call generateSolution()--------------");
        solution = generateSolution(new int[9][9], 0);
        //System.out.println("-------------solution generated! ------------------");
        //System.out.println("---------------call generateGame()--------------------");
        game = generateGame(copy(solution));
        //System.out.println("-----------game generated-----------");
        setChanged();
        notifyObservers(UpdateAction.NEW_GAME);
    }

    public void newGame(int gaps) {
    	//System.out.println("----------newGame() called--------------");
    	//System.out.println("------------call generateSolution()--------------");
        solution = generateSolution(new int[9][9], 0);
        //System.out.println("-------------solution generated! ------------------");
        //System.out.println("---------------call generateGame()--------------------");
        game = generateGame(copy(solution), gaps);
        //System.out.println("-----------game generated-----------");
        setChanged();
        notifyObservers(UpdateAction.NEW_GAME);
    }    
    
    /**
     * Checks user input agains the solution and puts it into a check matrix.<br />
     * All observers will be notified, update action: check.
     */
    public void checkGame() {
        selectedNumber = 0;
        int correctSpaces = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++){
                check[y][x] = game[y][x] == solution[y][x];
            	if (game[y][x] == solution[y][x]) {correctSpaces += 1;}
            }
        }
        setChanged();
        notifyObservers(UpdateAction.CHECK);
        
        if (correctSpaces == 81) {
        	setChanged();
        	notifyObservers(UpdateAction.WON);
        } else {
        	setChanged();
        	notifyObservers(UpdateAction.ERRORS);
        }
    }

    /**
     * Sets help turned on or off.<br />
     * All observers will be notified, update action: help.
     * 
     * @param help True for help on, false for help off.
     */
    public void setHelp(boolean help) {
    	System.out.println("setHelp() called." + "this.help = " + this.help + "help var = " + help);
        this.help = help;
        setChanged();
        notifyObservers(UpdateAction.HELP);
        if (help == true) {
        	setChanged();
        	notifyObservers(UpdateAction.INCREMENT_HELP);
        }
    }

    /**
     * Sets selected number to user input.<br />
     * All observers will be notified, update action: selected number.
     *
     * @param selectedNumber    Number selected by user.
     */
    public void setSelectedNumber(int selectedNumber) {
        this.selectedNumber = selectedNumber;
        setChanged();
        notifyObservers(UpdateAction.SELECTED_NUMBER);
        if (this.help == true) {
        	setChanged();
        	notifyObservers(UpdateAction.INCREMENT_HELP);
        }
    }

    /**
     * Returns number selected user.
     *
     * @return  Number selected by user.
     */
    public int getSelectedNumber() {
        return selectedNumber;
    }

    /**
     * Returns whether help is turned on or off.
     *
     * @return True if help is turned on, false if help is turned off.
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns whether selected number is candidate at given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if selected number on given position is candidate,
     *              false otherwise.
     */
    public boolean isSelectedNumberCandidate(int x, int y) {
        return game[y][x] == 0 && isPossibleX(game, y, selectedNumber)
                && isPossibleY(game, x, selectedNumber) && isPossibleBlock(game, x, y, selectedNumber);
    }

    /**
     * Sets given number on given position in the game.
     *
     * @param x         The x position in the game.
     * @param y         The y position in the game.
     * @param number    The number to be set.
     */
    public void setNumber(int x, int y, int number) {
    	System.out.println("this.help: " + this.help);
        game[y][x] = number;
        if (this.help) {
        	System.out.println("is this running?");
        	setChanged();
        	notifyObservers(UpdateAction.ITEM_WITH_HELP);
        }
    }

    /**
     * Returns number of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      Number of given position.
     */
    public int getNumber(int x, int y) {
        return game[y][x];
    }

    /**
     * Returns whether user input is valid of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if user input of given position is valid, false
     *              otherwise.
     */
    public boolean isCheckValid(int x, int y) {
        return check[y][x];
    }

    /**
     * Returns whether given number is candidate on x axis for given game.
     *
     * @param game      Game to check.
     * @param y         Position of x axis to check.
     * @param number    Number to check.
     * @return          True if number is candidate on x axis, false otherwise.
     */
    private boolean isPossibleX(int[][] game, int y, int number) {
        for (int x = 0; x < 9; x++) {
            if (game[y][x] == number)
                return false;
        }
        return true;
    }

    /**
     * Returns whether given number is candidate on y axis for given game.
     *
     * @param game      Game to check.
     * @param x         Position of y axis to check.
     * @param number    Number to check.
     * @return          True if number is candidate on y axis, false otherwise.
     */
    private boolean isPossibleY(int[][] game, int x, int number) {
        for (int y = 0; y < 9; y++) {
            if (game[y][x] == number)
                return false;
        }
        return true;
    }

    /**
     * Returns whether given number is candidate in block for given game.
     *
     * @param game      Game to check.
     * @param x         Position of number on x axis in game to check.
     * @param y         Position of number on y axis in game to check.
     * @param number    Number to check.
     * @return          True if number is candidate in block, false otherwise.
     */
    private boolean isPossibleBlock(int[][] game, int x, int y, int number) {
        int x1 = x < 3 ? 0 : x < 6 ? 3 : 6;
        int y1 = y < 3 ? 0 : y < 6 ? 3 : 6;
        for (int yy = y1; yy < y1 + 3; yy++) {
            for (int xx = x1; xx < x1 + 3; xx++) {
                if (game[yy][xx] == number)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns next posible number from list for given position or -1 when list
     * is empty.
     *
     * @param game      Game to check.
     * @param x         X position in game.
     * @param y         Y position in game.
     * @param numbers   List of remaining numbers.
     * @return          Next possible number for position in game or -1 when
     *                  list is empty.
     */
    private int getNextPossibleNumber(int[][] game, int x, int y, List<Integer> numbers) {
        while (numbers.size() > 0) {
        	//System.out.println("getNextPossibleNumber numbers : " + numbers);
            int number = numbers.remove(0);
            //System.out.println("getNextPossibleNumber number: " + number);
            if (isPossibleX(game, y, number) && isPossibleY(game, x, number) && isPossibleBlock(game, x, y, number)) // check all 3 elements of validity in sudoku. must pass all of these conditions.
                return number;
        }
        return -1;
    }

    /**
     * Generates Sudoku game solution.
     *
     * @param game      Game to fill, user should pass 'new int[9][9]'.
     * @param index     Current index, user should pass 0.
     * @return          Sudoku game solution.
     */
    private int[][] generateSolution(int[][] game, int index) {
    	//System.out.println("---------------generateSolution() called, " + "Index = " + index + "-----");
    	
        if (index > 80)
            return game; //this can end the method.

        int x = index % 9; //column index
        int y = index / 9; //row index

        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 1; i <= 9; i++) numbers.add(i);
        Collections.shuffle(numbers);

        while (numbers.size() > 0) {
        	//System.out.println("started a while loop iteration! " + "x = " + x + " Y = " + y);
        	//System.out.println("numbers list 1: " + numbers);
            int number = getNextPossibleNumber(game, x, y, numbers);
            //System.out.println("number: " + number);
            //System.out.println("numbers list 2: " + numbers);
            if (number == -1)
                return null;

            game[y][x] = number;
            int[][] tmpGame = generateSolution(game, index + 1); //recursive, when filled a slot it moves on to next slot by calling itself, but can backtrack if gets stuck. 
            if (tmpGame != null)
                return tmpGame; //i presume that the program should never really get here?
            game[y][x] = 0;
        }
        return null;
    }

    /**
     * Generates Sudoku game from solution.
     *
     * @param game      Game to be generated, user should pass a solution.
     * @return          Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game) {
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < 81; i++)
            positions.add(i);
        Collections.shuffle(positions);
        return generateGame(game, positions);
    }

    /**
     * Generates Sudoku game from solution, user should use the other
     * generateGame method. This method simple removes a number at a position.
     * If the game isn't anymore valid after this action, the game will be
     * brought back to previous state.
     *
     * @param game          Game to be generated.
     * @param positions     List of remaining positions to clear.
     * @return              Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game, List<Integer> positions) {
        while (positions.size() > 0) {
            int position = positions.remove(0);
            int x = position % 9;
            int y = position / 9;
            int temp = game[y][x];
            game[y][x] = 0;

            if (!isValid(game))
                game[y][x] = temp;
        }

        return game;
    }

    /**
     * Checks whether given game is valid.
     *
     * @param game      Game to check.
     * @return          True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game) {
        return isValid(game, 0, new int[] { 0 });
    }

    /**
     * Checks whether given game is valid, user should use the other isValid
     * method. There may only be one solution.
     *
     * @param game                  Game to check.
     * @param index                 Current index to check.
     * @param numberOfSolutions     Number of found solutions. Int[] instead of
     *                              int because of pass by reference.
     * @return                      True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game, int index, int[] numberOfSolutions) {
        if (index > 80)
            return ++numberOfSolutions[0] == 1;

        int x = index % 9;
        int y = index / 9;

        if (game[y][x] == 0) {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 1; i <= 9; i++)
                numbers.add(i);

            while (numbers.size() > 0) {
                int number = getNextPossibleNumber(game, x, y, numbers);
                if (number == -1)
                    break;
                game[y][x] = number;

                if (!isValid(game, index + 1, numberOfSolutions)) {
                    game[y][x] = 0;
                    return false;
                }
                game[y][x] = 0;
            }
        } else if (!isValid(game, index + 1, numberOfSolutions))
            return false;

        return true;
    }

    /**
     * Copies a game.
     *
     * @param game      Game to be copied.
     * @return          Copy of given game.
     */
    private int[][] copy(int[][] game) {
        int[][] copy = new int[9][9];
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                copy[y][x] = game[y][x];
        }
        return copy;
    }

    /*
     * Prints given game to console. Used for debug.
     *
     * @param game  Game to be printed.
     */
    private void print(int[][] game) {
        System.out.println();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                System.out.print(" " + game[y][x]);
            System.out.println();
        }
    }
    
    /*added functionality by @author James Stone
     * want to be able to produce different difficulty games by manipulating how
     * many grids come pre-filled during game creation. The more grids already filled
     * the easier it should be to complete the puzzle.
     */
    
    /**
     * Generates Sudoku game from solution, with specified number of omissions from solution 
     * allowing manipulation of difficulty on a continuous(ish) scale.
     *
     * @param game		Game to be generated, user should pass a solution.
     * @param omissions		number of gaps in the sudoku puzzle you want.	
     * @return          Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game, int omissions) {
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < 81; i++)
            positions.add(i);
        Collections.shuffle(positions);
        
        int[][] tmpGame = generateGame(game, positions);
        //work out how many spaces have been placed in this game.
        int countSpaces = 0; //hold number of gaps found
        List<Integer> positionOfSpaces = new ArrayList<Integer>();
        
        for (int i = 0; i < 81; i++) {
            int x = i % 9;
            int y = i / 9;
        	if (tmpGame[y][x] == 0) {
        		countSpaces += 1;
        		positionOfSpaces.add(i);
        	}
        }
        //work out how many more gaps need to be filled to reach difficulty we want based on omissions
        int gapsToFill = countSpaces - omissions;
        Collections.shuffle(positionOfSpaces); //randomise the positions then take the first 'gapsToFill' 
        //and set them to the matched value from the solution
        while (gapsToFill > 0) {
        	int i = positionOfSpaces.remove(0);
            int x = i % 9;
            int y = i / 9;
            tmpGame[y][x] = solution[y][x];
            gapsToFill -= 1;
        }
        
        return tmpGame;
        
    }
    
    public void tryEnd() {
    	setChanged();
    	notifyObservers(UpdateAction.QUIT);
    }
}