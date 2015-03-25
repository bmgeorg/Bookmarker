package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;

public class Document implements Serializable {
	private static final long serialVersionUID = -2054790569435840707L;
	private String url;
	private HashMap<String, Integer> termCounts;
	//numTerms = count of all terms in document, not just count of distinct terms in document
	private int numTerms;
	//magnitude: the magnitude of the term weight vector in the vector space model
	private double magnitude;
	
	public Document(String url) throws IOException {
		this.url = url;
		//lie and say we are Firefox -- prevents 403 errors, should probably fix eventually
		org.jsoup.nodes.Document htmlDoc = Jsoup.connect(url).userAgent("Mozilla").get();
		String[] tokens = new Tokenizer().tokenize(htmlDoc.text());
		numTerms = tokens.length;

		//count tokens and add to termCounts
		termCounts = new HashMap<String, Integer>();
		for(String token : tokens) {
			addTermCount(token, 1);
		}
		
		
		//weight title
		String[] titleTokens = new Tokenizer().tokenize(htmlDoc.title());
		for(String token : titleTokens) {
			addTermCount(token, 5);
		}
		
		calculateMagnitude();
	}
	
	public String getURL() {
		return url;
	}
	
	public Iterator<String> termIterator() {
		return termCounts.keySet().iterator();
	}
		
	//weight is relative frequency of term among all terms
	//weight is in [0, 1]
	public double weightForTerm(String term) {
		if(termCounts.containsKey(term))
			return (double)termCounts.get(term)/numTerms;
		else
			return 0.0;
	}
	
	public double getMagnitude() {
		return magnitude;
	}
	
	/* private methods */
	private void calculateMagnitude() {
		magnitude = 0;
		Iterator<String> iter = termIterator();
		while(iter.hasNext()) {
			magnitude += Math.pow(weightForTerm(iter.next()), 2);
		}
		magnitude = Math.sqrt(magnitude);
	}
	
	private void addTermCount(String term, int count) {
		if(termCounts.containsKey(term)) {
			count = termCounts.get(term) + count;
		}
		termCounts.put(term, count);
	}
	
	/* testing */
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

	/* overriden methods */
	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		return url.equals(other.url);
	}
}
