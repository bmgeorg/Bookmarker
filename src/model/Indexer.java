package model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jsoup.Jsoup;

public class Indexer {
	private org.jsoup.nodes.Document htmlDoc;
	private Map<String, Double> wordWeights;
	private String[] tokens;
	
	private void addTerm(String term) {
		Double count = 1.0;
		if(wordWeights.containsKey(term)) {
			count = wordWeights.get(term) + 1;
		}
		wordWeights.put(term, count);
	}
	
	private void scaleWeights() {
		double scale = tokens.length;
		Set<String> keys = wordWeights.keySet();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			wordWeights.put(key, wordWeights.get(key)/scale);
		}
	}
	
	public Document index(String url) throws IOException {
		htmlDoc = Jsoup.connect(url).get();
		tokens = new Tokenizer().tokenize(htmlDoc.text());
		wordWeights = new TreeMap<String, Double>();
		for(String token : tokens) {
			addTerm(token);
		}
		scaleWeights();
		Document result = new Document();
		result.setWords(wordWeights);
		return result;
	}
	
	public static void main(String args[]) throws IOException {
		new Indexer().index("http://lipsum.com").printWords();
	}
}