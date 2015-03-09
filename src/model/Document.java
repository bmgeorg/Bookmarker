package model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;

public class Document {
	private String url;
	private Map<String, Integer> termCounts;
	/*
	 * numTerms = count of all terms in document, not just count of distinct terms in document
	 */
	private int numTerms;
	/*
	 * magnitude: the magnitude of the term weight vector in the vector space model
	 */
	private double magnitude;
	
	public Document(String url) throws IOException {
		this.url = url;
		org.jsoup.nodes.Document htmlDoc = Jsoup.connect(url).get();
		String[] tokens = new Tokenizer().tokenize(htmlDoc.text());

		//count tokens and add to termCounts
		termCounts = new HashMap<String, Integer>();
		for(String token : tokens) {
			int count = 1;
			if(termCounts.containsKey(token)) {
				count = termCounts.get(token) + 1;
			}
			termCounts.put(token, count);
		}
		numTerms = tokens.length;
		
		calculateMagnitude();
	}
	
	public String getURL() {
		return url;
	}
	
	private void calculateMagnitude() {
		magnitude = 0;
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			magnitude += Math.pow(weightForTerm(iter.next()), 2);
		}
		magnitude = Math.sqrt(magnitude);
	}
	
	public Iterator<String> termIterator() {
		return termCounts.keySet().iterator();
	}
		
	public Double weightForTerm(String term) {
		if(termCounts.containsKey(term))
			return (100.0 * termCounts.get(term))/numTerms;
		else
			return 0.0;
	}
	
	public Double getMagnitude() {
		return magnitude;
	}
	
	public void printTermCounts() {
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			String term = iter.next();
			System.out.print(term);
			System.out.println(" " + termCounts.get(term));
		}
	}
	
	public void printTermWeights() {
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			String term = iter.next();
			System.out.print(term);
			System.out.println(" " + weightForTerm(term));
		}
	}
}
