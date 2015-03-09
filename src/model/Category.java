package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class Category {
	/*
	 * rawTagWeights: non-normalized tag weights
	 * non-normalized tag weight = sum of tag weights from each document
	 * normalized tag weight = non-normalized tag weight / num documents
	 */
	private TreeMap<String, Double> rawTagWeights = new TreeMap<String, Double>();
	private ArrayList<Document> docs = new ArrayList<Document>();
	
	private void addTagWeights(TreeMap<String, Double> tagWeights) {
		Iterator<String> iter = tagWeights.keySet().iterator();
		while(iter.hasNext()) {
			String tag = iter.next();
			addTagWeight(tag, tagWeights.get(tag));
		}
	}
	
	private void addTagWeight(String tag, Double weight) {
		assert weight >= 0;
		assert weight <= 100;
		//sum new weight and old weight
		if(rawTagWeights.containsKey(tag))
			weight += rawTagWeights.get(tag);
		rawTagWeights.put(tag, weight);
	}
	
	public Iterator<String> tagIterator() {
		return rawTagWeights.keySet().iterator();
	}
	
	public void addDocument(Document doc) {
		docs.add(doc);
		addTagWeights(doc.getTags());
	}
	
	public Double weightForTag(String tag) {
		if(rawTagWeights.containsKey(tag))
			return rawTagWeights.get(tag)/docs.size();
		else
			return 0.0;
	}
	
	public Double score(Document doc) {
		//take dot product of category tags and document terms
		//divide by magnitude of category tag weights and magnitude of document term weights
		Iterator<String> iter = tagIterator();
		Double dot = 0.0, squaredMagnitude = 0.0;
		while(iter.hasNext()) {
			String tag = iter.next();
			Double catWeight = weightForTag(tag);
			Double docWeight = doc.weightForTerm(tag);
			dot += catWeight*docWeight;
			squaredMagnitude += catWeight*catWeight;
			System.out.println(tag + ":");
			System.out.println("category weight: " + catWeight);
			System.out.println("document weight: " + docWeight);
		}
		System.out.println("dot: " + dot);
		Double magnitude = Math.sqrt(squaredMagnitude);
		System.out.println("category magnitude: " + magnitude);
		System.out.println("document magnitude: " + doc.getMagnitude());
		if(Math.sqrt(magnitude) == 0)
			return 0.0;
		return dot/(magnitude*doc.getMagnitude());
	}
	
	public void printRawTagWeights() {
		Iterator<String> iter = tagIterator();
		while(iter.hasNext()) {
			String tag = iter.next();
			System.out.print(tag);
			System.out.println(" " + rawTagWeights.get(tag));
		}
	}
	
	public static void main(String args[]) throws IOException {
		Category design = new Category();
		Document doc1 = new Indexer().index("http://en.wikipedia.org/wiki/Revised_simplex_method");
		Document doc2 = new Indexer().index("http://en.wikipedia.org/wiki/Linear_programming");
		System.out.println("Doc1");
		doc1.printTagWeights();
		System.out.println("Doc2");
		doc2.printTagWeights();
		
		design.addDocument(doc1);
		System.out.println("Score: " + design.score(doc2));
	}
}
