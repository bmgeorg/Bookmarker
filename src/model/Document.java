package model;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Document {
	private Map<String, Double> words;
	
	public Document() {
	}

	public Map<String, Double> getWords() {
		return words;
	}
	
	public void setWords(Map<String, Double> words) {
		this.words = words;
	}
	
	public void printWords() {
		Set<String> keys = words.keySet();
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext()) {
			String term = iter.next();
			System.out.print(term);
			System.out.println(" " + words.get(term));
		}
	}
}
