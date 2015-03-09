package model;

import java.io.IOException;
import java.util.TreeMap;

import org.jsoup.Jsoup;

public class Indexer {
	private org.jsoup.nodes.Document htmlDoc;
	private TreeMap<String, Integer> wordCounts;
	private String[] tokens;
	
	private void addTerm(String term) {
		int count = 1;
		if(wordCounts.containsKey(term)) {
			count = wordCounts.get(term) + 1;
		}
		wordCounts.put(term, count);
	}

	
	public Document index(String url) throws IOException {
		htmlDoc = Jsoup.connect(url).get();
		tokens = new Tokenizer().tokenize(htmlDoc.text());
		wordCounts = new TreeMap<String, Integer>();
		for(String token : tokens) {
			addTerm(token);
		}
		return new Document(wordCounts, tokens.length);
	}
	
	public static void main(String args[]) throws IOException {
		new Indexer().index("http://martinfowler.com/articles/designDead.html").printTermWeights();
	}
}