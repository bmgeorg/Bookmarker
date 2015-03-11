package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Wades through the junk in a Firefox exported bookmark html page to find the urls
 */

public class FirefoxBookmarkSanitizer {
	public static void extractFrom(String inputFile, String outputFile) throws FileNotFoundException, IOException {
		ArrayList<String> urls = new ArrayList<String>();
		
		//read input file
		try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			String line = br.readLine();
			while(line != null) {
				Matcher m = Pattern.compile("(?i)href=\"(.*?)\"").matcher(line);
				while(m.find())
					urls.add(m.group(1));
				line = br.readLine();
			}
		}
		
		//write output file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
			for(String url : urls)
				bw.write(url + "\n");
		}
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		extractFrom("rawFirefoxBookmarks.html", "sanitizedFirefoxBookmarks.txt");
		System.out.println("Done");
	}
}
