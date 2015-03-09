package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Category {
	private static final int MAX_NUM_TAGS = 25;
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
	private PriorityQueue<Tag> rawTagWeights =
			new PriorityQueue<Tag>(MAX_NUM_TAGS, new Comparator<Tag>() {
				@Override
				public int compare(Tag o1, Tag o2) {
					if(o1.getWeight() > o2.getWeight())
						return 1;
					else if(o1.getWeight() < o2.getWeight())
						return -1;
					else
						return 0;
				}
			});
	private ArrayList<Document> docs = new ArrayList<Document>();

	public Iterator<Tag> tagIterator() {
		return rawTagWeights.iterator();
	}

	/*
	 * Best case running time; O(T*D)
	 * Worst case running time: O(T*D + T*MAX_NUM_TAGS)
	 * 
	 * T = num terms in doc
	 * D = num documents
	 */
	public void addDocument(Document doc) {
		//add terms in doc as tags if their weights are high enough
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
			System.out.println(tag.getTerm() + ":");
			System.out.println("category weight: " + categoryWeight);
			System.out.println("document weight: " + docWeight);
		}
		System.out.println("dot: " + qdotp);
		Double qMagnitude = Math.sqrt(qSqrMagnitude);
		System.out.println("category magnitude: " +qMagnitude);
		System.out.println("document magnitude: " + doc.getMagnitude());
		if(Math.sqrt(qMagnitude) == 0)
			return 0.0;
		return qdotp/(qMagnitude*doc.getMagnitude());
	}

	public void printRawTagWeights() {
		Iterator<Tag> iter = tagIterator();
		while(iter.hasNext()) {
			Tag tag = iter.next();
			System.out.print(tag.getTerm());
			System.out.println(" " + tag.getWeight());
		}
	}

	public static void main(String args[]) throws IOException {
		Category design = new Category();
		Document doc1 = new Indexer().index("http://martinfowler.com/articles/designDead.html");
		Document doc2 = new Indexer().index("http://martinfowler.com/articles/mocksArentStubs.html");

		design.addDocument(doc2);
		System.out.println();
		System.out.println("Category tags");
		design.printRawTagWeights();
		System.out.println("Score: " + design.score(doc1));
	}
}
