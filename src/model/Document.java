package model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;

public class Document {
	//the actual base uri of a page (after redirection) retrieved from jsoup doc
	private String baseURI; //example: http://www.stackoverflow.com/questions/how-do-you-do-this.html
	private String domain; //example: stackoverflow.com
	private static final int TWO_GRAM_WEIGHT = 2;
	private static final int TITLE_WEIGHT = 3;
	private static final int TWO_GRAM_TITLE_WEIGHT = 2;
	//numTerms = the sum of all term counts in termCounts
	private int termSum;
	private HashMap<String, Integer> termCounts;
	//magnitude: the magnitude of the term weight vector in the vector space model
	private double magnitude;
	//the jsoup representation of the Document
	//eventually we should probably remove this for memory conservation, but it is useful for testing
	private org.jsoup.nodes.Document jsoupDoc;

	private void setup(org.jsoup.nodes.Document jsoupDoc) {
		this.baseURI = jsoupDoc.baseUri();
		this.domain = getDomain(this.baseURI);
		this.jsoupDoc = jsoupDoc;
		termSum = 0;

		//index body
		String[] tokens = new Tokenizer().tokenize(jsoupDoc.text());
		termSum += tokens.length;
		termCounts = new HashMap<String, Integer>();
		for(String token : tokens) {
			addTermCount(token, 1);
		}
		//add n-grams with n = 2
		if(tokens.length > 2) {
			termSum += tokens.length-1;
			for(int i = 0; i < tokens.length-1; i++) {
				String ngram = tokens[i] + " " + tokens[i+1];
				addTermCount(ngram, TWO_GRAM_WEIGHT);
			}
		}

		//index title
		String[] titleTokens = new Tokenizer().tokenize(jsoupDoc.title());
		termSum += titleTokens.length;
		for(String token : titleTokens) {
			addTermCount(token, TITLE_WEIGHT);
		}
		//add n-grams with n = 2
		if(titleTokens.length > 2) {
			termSum += titleTokens.length-1;
			for(int i = 0; i < titleTokens.length-1; i++) {
				String ngram = titleTokens[i] + " " + titleTokens[i+1];
				addTermCount(ngram, TWO_GRAM_TITLE_WEIGHT);
			}
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
	//load document from memento
	public Document(DocumentMemento memento) {
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(memento.html, memento.baseURI);
		setup(jsoupDoc);
	}
	public String getBaseURI() {
		return baseURI;
	}
	public String getDomain() {
		return domain;
	}
	public DocumentMemento getMemento() {
		return new DocumentMemento(jsoupDoc.html(), baseURI);
	}
	public Iterator<String> termIterator() {
		return termCounts.keySet().iterator();
	}

	//weight is relative frequency of term among all terms
	//weight is in [0, 1]
	public double weightForTerm(String term) {
		if(termCounts.containsKey(term))
			return (double)termCounts.get(term)/termSum;
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
	private String getDomain(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return null;
		}
		String theDomain = uri.getHost();
		return theDomain.startsWith("www.") ? theDomain.substring(4) : theDomain;
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
		Document doc = new Document("http://www.lipsum.com");
		DocumentMemento memento = doc.getMemento();
		Document docFromCache = new Document(memento);
		docFromCache.printTermWeights();
		assert(doc.getBaseURI().equals(docFromCache.getBaseURI()));
	}
}
