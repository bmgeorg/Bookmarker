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
		Iterator<String> iter = tagIterator();
		Double dot = 0.0, a = 0.0, b = 0.0;
		while(iter.hasNext()) {
			String tag = iter.next();
			Double catWeight = weightForTag(tag);
			Double docWeight = doc.weightForTerm(tag);
			dot += catWeight*docWeight;
			a += catWeight*catWeight;
			b += docWeight*docWeight;
			System.out.println(tag + ":");
			System.out.println("category weight: " + catWeight);
			System.out.println("document weight: " + docWeight);
		}
		System.out.println("dot: " + dot);
		System.out.println("category magnitude: " + Math.sqrt(a));
		System.out.println("document magnitude: " + Math.sqrt(b));
		if(Math.sqrt(a) == 0 || Math.sqrt(b) == 0)
			return 0.0;
		return dot/(Math.sqrt(a)*Math.sqrt(b));
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
		Document doc1 = new Indexer().index("http://martinfowler.com/articles/designDead.html");
		Document doc2 = new Indexer().index("http://david.heinemeierhansson.com/2014/tdd-is-dead-long-live-testing.html");
		System.out.println("Doc1");
		doc1.printTagWeights();
		System.out.println("Doc2");
		doc2.printTagWeights();
		
		design.addDocument(doc1);
		System.out.println("Score: " + design.score(doc2));
	}
}
