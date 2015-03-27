package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import test.DataLoader;

public class Category implements Serializable {
	private static final long serialVersionUID = -456116242689353233L;
	private String name;
	private static final int MAX_USER_TAGS = 10;
	private static final int MAX_DISCOVERED_TAGS = 10;
	private static final double WEIGHT_FOR_USER_TAGS = 3; //will be scaled down by number of user tags
	private static final double WEIGHT_PER_MATCHED_DOMAIN = 0.01; //not scaled down
	/*
	 * All tags hold non-normalized tag weights. The non-normalized tag weight for a term is simply the sum of
	 * weights for the term from all documents and user tags.
	 */

	/*
	 * holds tags assigned by user to Category
	 * maps terms to Tags
	 * if number of user tags is less than MAX_USER_TAGS, the category name is tokenized and tokens are added
	 * as userTags
	 */
	private Map<String, Tag> userTags = new HashMap<String, Tag>();

	/* 
	 * We allow up to MAX_DISCOVERED_TAGS in discoveredTags. After that, we only let in a new tag if
	 * it's weight is greater than the minimum weight in discoveredTags.
	 * We store rawTagWeights as a min priority queue so we know the minimum element in
	 * constant time. 
	 */
	class TagComparator implements Comparator<Tag>, Serializable {
		private static final long serialVersionUID = -313406206543899133L;
		@Override
		public int compare(Tag o1, Tag o2) {
			if(o1.getWeight() > o2.getWeight())
				return 1;
			else if(o1.getWeight() < o2.getWeight())
				return -1;
			else
				return 0;
		}
	}
	private PriorityQueue<Tag> discoveredTags=
			new PriorityQueue<Tag>(MAX_DISCOVERED_TAGS, new TagComparator());
	/*
	 * For weighting by domain
	 * maps domain name to number of times domain occurs in Category
	 */
	private Map<String, Integer> domains = new HashMap<String, Integer>();
	private ArrayList<Document> docs = new ArrayList<Document>();

	public Category(String name, String... tags) {
		assert name != null;
		assert tags.length <= MAX_USER_TAGS;
		this.name = name;

		Set<String> tempTags = new HashSet<String>();
		//add all tags
		for(int i = 0; i < tags.length; i++)
			tempTags.add(tags[i]);

		//tokenize name and add tags as long as there is room
		String[] nameTags = new Tokenizer().tokenize(name);
		for(int i = 0; i < nameTags.length; i++)
			if(tempTags.size() < MAX_USER_TAGS)
				tempTags.add(nameTags[i]);

		//add tags from tempTags
		double weight = WEIGHT_FOR_USER_TAGS/tempTags.size();
		Iterator<String> iter = tempTags.iterator();
		while(iter.hasNext()) {
			String term = iter.next();
			userTags.put(term, new Tag(term, weight));
		}
	}

	public String getName() {
		return name;
	}

	public ArrayList<Document> getDocs() {
		return docs;
	}

	public Iterator<Tag> getUserTagIterator() {
		return userTags.values().iterator();
	}

	public Iterator<Tag> getDiscoveredTagIterator() {
		return discoveredTags.iterator();
	}

	/*
	 * T = num terms in doc
	 * D = num documents
	 * 
	 * Best case running time; O(T*D)
	 * Worst case running time: O(T*D + T*MAX_DISCOVERED_TAGS*log(MAX_DISCOVERED_TAGS))
	 */
	public void addDocument(Document doc) {
		if(doc.getDomain() != null) {
			addDomain(doc.getDomain());
		}

		Iterator<String> iter = doc.termIterator();
		while(iter.hasNext()) {
			String term = iter.next();
			Double weight = doc.weightForTerm(term);

			//if term is a user tag, update weight
			if(userTags.containsKey(term)) {
				double updatedWeight = userTags.get(term).getWeight() + weight;
				userTags.put(term, new Tag(term, updatedWeight));
			}
			//add to discoveredTags iff its weight is higher than current lowest discovered tag weight 
			else {
				//add weights from docs
				for(int i = 0; i < docs.size(); i++)
					weight += docs.get(i).weightForTerm(term);
				if(discoveredTags.size() < MAX_DISCOVERED_TAGS || weight > discoveredTags.peek().getWeight()) {
					//remove min if already at tag cap
					if(discoveredTags.size() == MAX_DISCOVERED_TAGS)
						discoveredTags.remove();
					//remove tag if it exists
					discoveredTags.remove(new Tag(term, 0.0));
					discoveredTags.add(new Tag(term, weight));
				}
			}
		}

		docs.add(doc);
	}

	/*
	 * Running time: O(MAX_USER_TAGS + MAX_DISCOVERED_TAGS)
	 */
	public CategoryReport score(Document doc) {
		/*
		 * Let q be vector of category tag weights
		 * Let p be vector of doc term weights corresponding to tags
		 * Let w be vector of all doc term weights
		 * Let |q| be the magnitude of vector q
		 * Let |p| be the magnitude of vector p
		 * 
		 * score = q (dot product) p / (|q| * |w|)
		 * 
		 * score is in [0, 1]
		 */

		//for returning in CategoryReport
		Tag tags[] = new Tag[userTags.size() + discoveredTags.size()];
		//for returning in CategoryReport
		double docWeights[] = new double[userTags.size() + discoveredTags.size()];

		double qdotp = 0.0, qSqrMagnitude = 0.0, pSqrMagnitude = doc.getMagnitude()*doc.getMagnitude();
		Iterator<Tag> iter = getUserTagIterator();
		double categoryWeight;
		double docWeight;
		Tag tag;
		//Score domain name matches
		String domain = doc.getDomain();
		if(domain != null && domains.containsKey(domain)) {
			double addedWeight = WEIGHT_PER_MATCHED_DOMAIN*domains.get(domain);
			pSqrMagnitude += addedWeight;
			qSqrMagnitude += addedWeight;
			qdotp += addedWeight*addedWeight;
		}

		//Score over user tags
		for(int i = 0; i < userTags.size(); i++) {
			tag = iter.next();
			categoryWeight = tag.getWeight();
			docWeight = doc.weightForTerm(tag.getTerm());
			qdotp += categoryWeight*docWeight;
			qSqrMagnitude += categoryWeight*categoryWeight;
			tags[i] = tag;
			docWeights[i] = docWeight;
		}
		//Score over discovered tags
		iter = getDiscoveredTagIterator();
		for(int i = userTags.size(); i - userTags.size() < discoveredTags.size(); i++) {
			//same code as above for user tags, too cumbersome to refactor to a method
			tag = iter.next();
			categoryWeight = tag.getWeight();
			docWeight = doc.weightForTerm(tag.getTerm());
			qdotp += categoryWeight*docWeight;
			qSqrMagnitude += categoryWeight*categoryWeight;
			tags[i] = tag;
			docWeights[i] = docWeight;
		}

		double qMagnitude = Math.sqrt(qSqrMagnitude);
		double pMagnitude = Math.sqrt(pSqrMagnitude);
		double score = 0;
		if(qMagnitude != 0 && doc.getMagnitude() != 0)
			score = qdotp/(qMagnitude*pMagnitude);
		CategoryReport report = new CategoryReport(name, score, tags, docWeights);
		return report;
	}

	/* private methods */
	private ArrayList<Tag> getSortedTags() {
		ArrayList<Tag> sortedTags = new ArrayList<Tag>();
		Iterator<Tag> iter = getUserTagIterator();
		while(iter.hasNext())
			sortedTags.add(iter.next());
		iter = getDiscoveredTagIterator();
		while(iter.hasNext())
			sortedTags.add(iter.next());
		Collections.sort(sortedTags, new Comparator<Tag>() {
			@Override
			public int compare(Tag o1, Tag o2) {
				if(o1.getWeight() > o2.getWeight())
					return -1;
				else if(o1.getWeight() < o2.getWeight())
					return 1;
				return 0;
			}
		});
		return sortedTags;
	}

	//expensive because the magnitude changes often and is not cached
	private double getMagnitude() {
		double sqrMagnitude = 0;
		Iterator<Tag> iter = getUserTagIterator();
		while(iter.hasNext())
			sqrMagnitude += Math.pow(iter.next().getWeight(), 2.0);
		iter = getDiscoveredTagIterator();
		while(iter.hasNext())
			sqrMagnitude += Math.pow(iter.next().getWeight(), 2.0);
		return Math.sqrt(sqrMagnitude);
	}
	private void addDomain(String domain) {
		int count = 1;
		if(domains.containsKey(domain)) {
			count += domains.get(domain);
		}
		domains.put(domain, count);
	}

	/* testing */
	public void printDocumentURLs() {
		for(int i = 0; i < docs.size(); i++) {
			System.out.println(docs.get(i).getBaseURI());
		}
	}

	public void printRawTagWeights() {
		ArrayList<Tag> tags = getSortedTags();
		for(Tag tag : tags) {
			System.out.println(tag.getTerm() + ": " + tag.getWeight());
		}
	}

	public void printAdjustedTagWeights() {
		double mag = getMagnitude();
		ArrayList<Tag> tags = getSortedTags();
		for(Tag tag : tags) {
			System.out.println(tag.getTerm() + ": " + tag.getWeight()/mag);
		}
	}

	public static void main(String args[]) throws IOException {
		Category design = new Category("Design");
		Document doc1 = DataLoader.getDoc("http://martinfowler.com/articles/designDead.html", true);
		Document doc2 = DataLoader.getDoc("http://martinfowler.com/articles/mocksArentStubs.html", true);

		design.printRawTagWeights();

		design.addDocument(doc2);
		System.out.println();
		System.out.println("Category tags");
		design.printRawTagWeights();
		System.out.println("Score: " + design.score(doc1));
	}
}
