package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.EnglishSnowballStemmerFactory;
import org.tartarus.snowball.util.StemmerException;

//stateless
public class Tokenizer {
	//ignored words
	Set<String> stopwords = new HashSet<String>();
	EnglishSnowballStemmerFactory stemmer = EnglishSnowballStemmerFactory.getInstance();


	public Tokenizer() {
		//initialize stopwords
		String fileName = "stopwords.txt";
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line = br.readLine();
			while(line != null) {
				stopwords.add(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String sanitize(String text) {
		//remove all non-alphabetic, non-hypen, non + characters
		text = text.replaceAll("[^a-zA-Z\\-\\+]", " ");
		text = text.toLowerCase();
		return text;
	}

	public String[] tokenize(String text) {
		text = sanitize(text);
		ArrayList<String> tokens = new ArrayList<String>();


		Pattern pattern = Pattern.compile("\\S+");
		Matcher matcher = pattern.matcher(text);
		while(matcher.find()) {
			String token = matcher.group();
			if(!stopwords.contains(token)) {
				//stem token before adding
				try {
					tokens.add(stemmer.process(token));
				} catch(StemmerException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		return tokens.toArray(new String[tokens.size()]);
	}

	public static void main(String args[]) {
		String tokens[] = new Tokenizer().tokenize("Here is-a sentence 123 from a web blog.Here is another sentence from a web blog!");
		for(String token : tokens)
			System.out.println(token);
	}
}
