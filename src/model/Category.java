package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class Category implements Serializable {
	private static final long serialVersionUID = -456116242689353233L;
	private String name;
	private static final int MAX_NUM_TAGS = 10;
	private static final double INITIAL_TAG_WEIGHT = 3;
	/*
	 * rawTagWeights: non-normalized tag weights
	 * non-normalized tag weight = sum of tag weights from each document
	 * normalized tag weight = non-normalized tag weight / num documents
	 * 
	 * We allow up to MAX_NUM_TAGS in rawTagWeights. After that, we only let in a new tag if
	 * it's raw weight is greater than the minimum raw weight in rawTagWeights.
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
	private PriorityQueue<Tag> rawTagWeights =
			new PriorityQueue<Tag>(MAX_NUM_TAGS, new TagComparator());
	private ArrayList<Document> docs = new ArrayList<Document>();

	public Category(String name, String... tags) {
		assert name != null;
		assert tags.length <= MAX_NUM_TAGS;
		this.name = name;

		//collect unique tags
		Set<String> uniqueTags = new HashSet<String>();
		for(int i = 0; i < tags.length; i++)
			uniqueTags.add(tags[i]);
		
		//tokenize name and collect unique name tags
		Set<String> uniqueNameTags = new HashSet<String>();
		String[] nameTags = new Tokenizer().tokenize(name);
		for(int i = 0; i < nameTags.length; i++)
			if(!uniqueTags.contains(nameTags[i]))
				uniqueNameTags.add(nameTags[i]);

		//add tags
		int numToAdd = Math.min(MAX_NUM_TAGS, uniqueTags.size() + uniqueNameTags.size());
		if(numToAdd > 0) {
			double weight = INITIAL_TAG_WEIGHT/numToAdd;
			//the addTag function will automatically filter results if we add more than MAX_NUM_TAGS
			for(String tag : uniqueTags) {
				addTag(tag, weight);
			}
			for(String tag : uniqueNameTags) {
				addTag(tag, weight);
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public ArrayList<Document> getDocs() {
		return docs;
	}

	public Iterator<Tag> tagIterator() {
		return rawTagWeights.iterator();
	}
	
	private void addTag(String term, double weight) {
		//add term as tag iff numTags < MAX_NUM_TAGS or term weight is greater than current minimum tag weight
		if(rawTagWeights.size() < MAX_NUM_TAGS || weight > rawTagWeights.peek().getWeight()) {
			//remove min if already at tag cap
			if(rawTagWeights.size() == MAX_NUM_TAGS)
				rawTagWeights.remove();
			//remove tag if it exists
			rawTagWeights.remove(new Tag(term, 0.0));
			rawTagWeights.add(new Tag(term, weight));
		}
	}

	/*
	 * T = num terms in doc
	 * D = num documents
	 * 
	 * Best case running time; O(T*D)
	 * Worst case running time: O(T*D + T*MAX_NUM_TAGS*log(MAX_NUM_TAGS))
	 */
	public void addDocument(Document doc) {
		//add each term in doc as tag if its cumulative weights is higher than lowest weight of current tag
		Iterator<String> iter = doc.termIterator();
		while(iter.hasNext()) {
			String term = iter.next();
			Double weight = doc.weightForTerm(term);
			//add weights from docs
			for(int i = 0; i < docs.size(); i++)
				weight += docs.get(i).weightForTerm(term);
			addTag(term, weight);
		}

		docs.add(doc);
	}

	/*
	 * Running time: O(MAX_NUM_TAGS)
	 */
	public CategoryReport score(Document doc) {
		/*
		 * Let q be vector of category tag weights
		 * Let p be vector of doc term weights with terms corresponding to the tags in category
		 * Let w be vector of all doc term weights
		 * Let |q| be the magnitude of vector q
		 * Let |p| be the magnitude of vector p
		 * 
		 * score = q (dot product) p / (|q| * |p|)
		 * 
		 * score is in [0, 1]
		 */
		Tag tags[] = new Tag[rawTagWeights.size()];
		double docWeights[] = new double[rawTagWeights.size()];
		
		Iterator<Tag> iter = tagIterator();
		double qdotp = 0.0, qSqrMagnitude = 0.0;
		for(int i = 0; i < rawTagWeights.size(); i++) {
			Tag tag = iter.next();
			double categoryWeight = tag.getWeight();
			double docWeight = doc.weightForTerm(tag.getTerm());
			qdotp += categoryWeight*docWeight;
			qSqrMagnitude += categoryWeight*categoryWeight;
			tags[i] = tag;
			docWeights[i] = docWeight;
		}
		Double qMagnitude = Math.sqrt(qSqrMagnitude);
		double score = 0;
		if(qMagnitude != 0 && doc.getMagnitude() != 0)
			score = qdotp/(qMagnitude*doc.getMagnitude());
		CategoryReport report = new CategoryReport(name, score, tags, docWeights);
		return report;
	}
	
	/* private methods */
	private ArrayList<Tag> getSortedTags() {
		ArrayList<Tag> sortedTags = new ArrayList<Tag>();
		Iterator<Tag> iter = tagIterator();
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
		Iterator<Tag> iter = tagIterator();
		while(iter.hasNext())
			sqrMagnitude += Math.pow(iter.next().getWeight(), 2.0);
		return Math.sqrt(sqrMagnitude);
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
		Document doc1 = new Document("http://martinfowler.com/articles/designDead.html");
		Document doc2 = new Document("http://martinfowler.com/articles/mocksArentStubs.html");
		
		doc1.printTermWeights();
		doc2.printTermWeights();

		design.addDocument(doc2);
		System.out.println();
		System.out.println("Category tags");
		design.printRawTagWeights();
		System.out.println("Score: " + design.score(doc1));
	}
}
