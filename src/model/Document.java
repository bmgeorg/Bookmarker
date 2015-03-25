package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;

public class Document implements Serializable {
	private static final long serialVersionUID = -2054790569435840707L;
	//the actual base uri of a page (after redirection) retrieved from jsoup doc
	private String baseURI;
	private HashMap<String, Integer> termCounts;
	//numTerms = count of all terms in document, not just count of distinct terms in document
	private int numTerms;
	//magnitude: the magnitude of the term weight vector in the vector space model
	private double magnitude;
	//the jsoup representation of the Document
	//eventually we should probably remove this for memory conservation, but it is useful for testing
	private org.jsoup.nodes.Document jsoupDoc;
	
	private void setup(org.jsoup.nodes.Document jsoupDoc) {
		this.baseURI = jsoupDoc.baseUri();
		this.jsoupDoc = jsoupDoc;

		String[] tokens = new Tokenizer().tokenize(jsoupDoc.text());
		numTerms = tokens.length;

		//count tokens and add to termCounts
		termCounts = new HashMap<String, Integer>();
		for(String token : tokens) {
			addTermCount(token, 1);
		}
		
		//weight title
		String[] titleTokens = new Tokenizer().tokenize(jsoupDoc.title());
		for(String token : titleTokens) {
			addTermCount(token, 5);
		}
		
		calculateMagnitude();
	}
	
	//load document from network
	//Note: url may not be the same as Document.baseURI() if url gets redirected
	public Document(String url) throws IOException {
		//load page
		//lie and say we are Firefox -- prevents 403 errors, should probably fix eventually
		org.jsoup.nodes.Document jsoupDoc = Jsoup.connect(url).userAgent("Mozilla").get();
		setup(jsoupDoc);
	}
	
	//the actual base uri of a page (after redirection) retrieved from jsoup doc
	//load document from cached htmlDoc
	public Document(String html, String baseURI) {
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, baseURI);
		setup(jsoupDoc);
	}
	
	public String getBaseURI() {
		return baseURI;
	}
	
	public String getHTML() {
		return jsoupDoc.html();
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
		return baseURI.hashCode();
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
		return baseURI.equals(other.baseURI);
	}
	
	public static void main(String args[]) throws IOException {
		Document doc = new Document("http://www.facebook.com");
		String html = doc.getHTML();
		String baseURI = doc.getBaseURI();
		Document docFromCache = new Document(html, baseURI);
		System.out.println(html);
		System.out.println(docFromCache.getHTML());
		assert(doc.getBaseURI().equals(docFromCache.getBaseURI()));
	}
}
