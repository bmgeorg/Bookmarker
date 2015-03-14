package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Reorderer {
	/*
	 * Randomly reorganizes the lines in file at path
	 */
	public static void reorderLines(String inFile, String outFile) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		
		//read
		try(BufferedReader br = new BufferedReader(new FileReader(inFile))) {
			String line = br.readLine();
			while(line != null) {
				lines.add(line);
				line = br.readLine();
			}
		}
		
		//randomize
		Random rand = new Random();
		for(int i = 0; i < lines.size(); i++) {
			int j = rand.nextInt(lines.size()-i) + i;
			String istring = lines.get(i);
			lines.set(i, lines.get(j));
			lines.set(j, istring);
		}
		
		//write
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
			for(int i = 0; i < lines.size(); i++)
				bw.write(lines.get(i) + "\n");
		}
	}
	
	public static void main(String args[]) throws IOException {
		reorderLines("src/dataSet_1/smallURLs.txt", "src/dataSet_1/smallURLs.txt");
	}
}
