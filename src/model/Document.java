package model;

import java.util.Iterator;
import java.util.Map;

public class Document {
	private Map<String, Integer> termCounts;
	private int numTerms;
	/*
	 * magnitude: the magnitude of the term weight vector in the vector space model
	 */
	private double magnitude;
	
	private void calculateMagnitude() {
		magnitude = 0;
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			magnitude += Math.pow(weightForTerm(iter.next()), 2);
		}
		magnitude = Math.sqrt(magnitude);
	}
	
	/*
	 * numTerms = count of all terms in document, not just count of distinct terms in document
	 */
	public Document(Map<String, Integer> wordCounts, int numTerms) {
		this.termCounts = wordCounts;
		this.numTerms = numTerms;
		
		calculateMagnitude();
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
