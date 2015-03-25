package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Category implements Serializable {
	private static final long serialVersionUID = -456116242689353233L;
	private String name;
	private static final int MAX_NUM_TAGS = 10;
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

		String[] nameTokens = new Tokenizer().tokenize(name);
		int numTags = Math.min(tags.length + nameTokens.length, MAX_NUM_TAGS);
		String[] combinedTags = new String[numTags];
		for(int i = 0; i < tags.length; i++)
			combinedTags[i] = tags[i];
		for(int i = 0; i + tags.length < numTags; i++)
			combinedTags[i+tags.length] = nameTokens[i]; 

		//add tag raw weights
		if(numTags > 0) {
			double weight = 100.0/numTags;
			for(String tag : combinedTags) {
				rawTagWeights.add(new Tag(tag, weight));
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

	/*
	 * T = num terms in doc
	 * D = num documents
	 * 
	 * Best case running time; O(T*D)
	 * Worst case running time: O(T*D + T*MAX_NUM_TAGS)
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

		docs.add(doc);
	}

	/*
	 * Running time: O(MAX_NUM_TAGS)
	 */
	public Double score(Document doc) {
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
		Iterator<Tag> iter = tagIterator();
		Double qdotp = 0.0, qSqrMagnitude = 0.0;
		while(iter.hasNext()) {
			Tag tag = iter.next();
			Double categoryWeight = tag.getWeight();
			Double docWeight = doc.weightForTerm(tag.getTerm());
			qdotp += categoryWeight*docWeight;
			qSqrMagnitude += categoryWeight*categoryWeight;
		}
		Double qMagnitude = Math.sqrt(qSqrMagnitude);
		if(Math.sqrt(qMagnitude) == 0)
			return 0.0;
		return qdotp/(qMagnitude*doc.getMagnitude());
	}

	public void printDocumentURLs() {
		for(int i = 0; i < docs.size(); i++) {
			System.out.println(docs.get(i).getURL());
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

	public static void main(String args[]) throws IOException {
		Category design = new Category("Design", new String[0]);
		Document doc1 = new Document("http://martinfowler.com/articles/designDead.html");
		Document doc2 = new Document("http://martinfowler.com/articles/mocksArentStubs.html");

		design.addDocument(doc2);
		System.out.println();
		System.out.println("Category tags");
		design.printRawTagWeights();
		System.out.println("Score: " + design.score(doc1));
	}
}
