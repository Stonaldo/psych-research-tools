package testing.wordsearch;
import java.util.Scanner;
import java.io.*;

public class SampleClient {
	public static void main(String[] args) {
		Scanner stdin = null;
		try {
			stdin = new Scanner(new File("src/main/java/testing/wordsearch/wordlist.txt"));
		} catch(FileNotFoundException e) {
			System.out.println("Couldn't open file \"wordlist.txt\"");
		}
		
		StringBuilder wordList = new StringBuilder();
		while(stdin.hasNext()) {
			wordList.append(stdin.next() + " ");
		}
		wordList.deleteCharAt(wordList.length()-1);
		
		String[] list = wordList.toString().split(" ");
		
		WordSearchGame w = new WordSearchGame(list, 10);
		
		System.out.println(w);
		
		new WordSearch(w);
		
	}
}
