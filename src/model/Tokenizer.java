package model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	//ignored words
	Set<String> stopwords = new HashSet<String>();
	
	public Tokenizer() {
		//initialize stopwords
		String fileName = "stopwords.txt";
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line = br.readLine();
			while(line != null) {
				stopwords.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find stop words file " + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sanitize(String token) {
		//remove all non-alphabetic, non-hypen characters
		token = token.replaceAll("[^a-zA-Z\\-\\+]", "");
		token = token.toLowerCase();
		return token;
	}
	
	public String[] tokenize(String text) {
		//result
		ArrayList<String> tokens = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("\\S+");
		Matcher matcher = pattern.matcher(text);
		while(matcher.find()) {
			String token = sanitize(matcher.group());
			if(!stopwords.contains(token))
				tokens.add(token);
		}
		
		return tokens.toArray(new String[tokens.size()]);
	}
	
	public static void main(String args[]) {
		String tokens[] = new Tokenizer().tokenize("Here is-a sentence 123 from a web blog.Here is another sentence from a web blog!");
		for(String token : tokens)
			System.out.println(token);
	}
}
