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
	private double p = .5;
	private TreeMap<String, Integer> termCounts;
	private TreeMap<String, Double> tags;
	private int numTerms;
	
	private void calculateTags() {
		tags = new TreeMap<String, Double>();
		Iterator<String> iter = termCounts.keySet().iterator();
		while(iter.hasNext()) {
			String term = iter.next();
			Double weight = weightForTerm(term);
			if(weight > p)
				tags.put(term, weight);
		}
	}
	
	/*
	 * numTerms = count of all terms in document, not just count of distinct terms in document
	 */
	public Document(TreeMap<String, Integer> wordCounts, int numTerms) {
		this.termCounts = wordCounts;
		this.numTerms = numTerms;
		
		calculateTags();
	}
		
	public Double weightForTerm(String term) {
		if(termCounts.containsKey(term))
			return (100.0 * termCounts.get(term))/numTerms;
		else
			return 0.0;
	}
	
	public void printTermCounts() {
		Iterator<String> iter = termCounts.keySet().iterator();
		while(iter.hasNext()) {
			String term = iter.next();
			System.out.print(term);
			System.out.println(" " + termCounts.get(term));
		}
	}
	
	public void printTermWeights() {
		Iterator<String> iter = termCounts.keySet().iterator();
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
