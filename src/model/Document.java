package model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class Document {
	//the actual base uri of a page (after redirection) retrieved from jsoup doc
	private String baseURI; //example: http://www.stackoverflow.com/questions/how-do-you-do-this.html
	private String domain; //example: stackoverflow.com
	private static final double TEXT_WEIGHT= 1;
	private static final double TITLE_WEIGHT = 3;
	private static final double META_DESCRIPTION_WEIGHT = 1;
	private static final double TWO_GRAM_WEIGHT_MULTIPLIER = 2; //the weight of a 2-gram is [WEIGHT]*TWO_GRAM_WEIGHT_MULTIPLIER
	//totalWeight = the sum of weights for all terms in rawTermWeights
	private double totalWeight;
	//the non-normalized weight for a term; will be normalized by 1/totalWeight
	private HashMap<String, Double> rawTermWeights = new HashMap<String, Double>();
	//magnitude: the magnitude of the term weight vector in the vector space model
	private double magnitude;
	//the jsoup representation of the Document
	//eventually we should probably remove this for memory conservation, but it is useful for testing
	private org.jsoup.nodes.Document jsoupDoc;
	private Tokenizer tokenizer = new Tokenizer();

	private void setup(org.jsoup.nodes.Document jsoupDoc) {
		this.baseURI = jsoupDoc.baseUri();
		this.domain = extractURL(this.baseURI);
		this.jsoupDoc = jsoupDoc;
		totalWeight = 0;

		index(jsoupDoc.text(), TEXT_WEIGHT);
		index(jsoupDoc.title(), TITLE_WEIGHT);
		//index meta description
		/*Elements descriptionMetaTags = jsoupDoc.select("meta[name=description]");
		if(descriptionMetaTags.size() > 0) {
			String metaDescription = descriptionMetaTags.first().attr("content");
			index(metaDescription, META_DESCRIPTION_WEIGHT);
		}*/

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
		return rawTermWeights.keySet().iterator();
	}

	//weight is proportion of term's weight in totalWeight
	//weight is in [0, 1]
	public double weightForTerm(String term) {
		if(rawTermWeights.containsKey(term))
			return rawTermWeights.get(term)/totalWeight;
		else
			return 0.0;
	}

	public double getMagnitude() {
		return magnitude;
	}

	/* private methods */
	private void index(String text, double weight) {
		index(text, 1, weight);
		index(text, 2, weight*TWO_GRAM_WEIGHT_MULTIPLIER);
	}
	private void index(String text, int gramSize, double weight) {
		if(text == null || text.equals("")) {
			return;
		}
		String[] tokens = tokenizer.tokenize(text);
		if(tokens.length < gramSize) {
			return;
		}
		
		int numGrams = tokens.length-(gramSize-1);
		totalWeight += numGrams*weight;
		for(int i = 0; i < numGrams; i++) {
			//create gram
			String gram = tokens[i];
			for(int j = 1; j < gramSize; j++) {
				gram += " " + tokens[i+j];
			}
			addTermWeight(gram, weight);
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

	private void addTermWeight(String term, double weight) {
		if(rawTermWeights.containsKey(term)) {
			weight += rawTermWeights.get(term);
		}
		rawTermWeights.put(term, weight);
	}
	private String extractURL(String url) {
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
		Document doc = new Document("http://www.helloworld.com");
		doc.printTermWeights();
		System.out.println("Done");

		/*DocumentMemento memento = doc.getMemento();
		Document docFromCache = new Document(memento);
		docFromCache.printTermWeights();
		assert(doc.getBaseURI().equals(docFromCache.getBaseURI()));*/
	}
}
