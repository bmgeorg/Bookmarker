package model;

import java.util.Iterator;
import java.util.TreeMap;

public class Document {
	/*
	 * p: the cutoff weight for tags
	 * If a term weight is greater than or equal to p, the term is counted as a tag.
	 * 
	 * term weight is the relative frequency of the term in the document
	 * term weight = term frequency / numTerms * 100
	 */
	private double p = 1;
	private TreeMap<String, Integer> termCounts;
	private TreeMap<String, Double> tags;
	private int numTerms;
	/*
	 * magnitude: the magnitude of the term weight vector in the vector space model
	 */
	private double magnitude;
	
	private void calculateTags() {
		tags = new TreeMap<String, Double>();
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			String term = iter.next();
			Double weight = weightForTerm(term);
			if(weight > p)
				tags.put(term, weight);
		}
	}
	
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
	public Document(TreeMap<String, Integer> wordCounts, int numTerms) {
		this.termCounts = wordCounts;
		this.numTerms = numTerms;
		
		calculateTags();
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
	
	public TreeMap<String, Double> getTags() {
		return tags;
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
	
	public void printTagWeights() {
		Iterator<String> iter = tags.keySet().iterator();
		while(iter.hasNext()) {
			String tag = iter.next();
			System.out.print(tag);
			System.out.println(" " + weightForTerm(tag));
		}
	}
}
