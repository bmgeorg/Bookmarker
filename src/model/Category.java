package model;

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
	
	public void addDocument(Document doc) {
		docs.add(doc);
		addTagWeights(doc.getTags());
	}
	
	public void printRawTagWeights() {
		Iterator<String> iter = rawTagWeights.keySet().iterator();
		while(iter.hasNext()) {
			String tag = iter.next();
			System.out.print(tag);
			System.out.println(" " + rawTagWeights.get(tag));
		}
	}
}
