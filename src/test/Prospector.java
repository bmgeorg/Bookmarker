package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.Bookmarker;
import model.Category;
import model.Document;

public class Prospector {
	
	/*
	 * The only public method in Prospector
	 * 
	 * goldFile holds manually categorized urls
	 * categoriesFile holds categories and tags (see format in DataLoader loadCategories())
	 * urlFile holds urls (see format in DataLoader loadDocs())
	 * if reorderURLs is true, the method will randomly reorder urls from urlFile before bookmarking the urls -
	 * important because order generally matters when bookmarking
	 * 
	 * Uses Bookmarker object to categorize urls from urlFile into the categories in categoriesFile
	 * Compares results from Bookmarker with gold data from goldFile
	 * Returns summary of comparison in a Prospect object
	 */
	public Prospect prospect(String goldFile, String categoriesFile, String urlFile, boolean reorderURLs, boolean useCache) {		
		//load gold
		ArrayList<Category> gold = DataLoader.loadGold(goldFile, useCache);

		//load ore
		ArrayList<Category> categories = DataLoader.loadCategories(categoriesFile);
		ArrayList<Document> docs = DataLoader.loadDocs(urlFile, useCache);
		//reorder urls
		if(reorderURLs) {
			Collections.shuffle(docs);
		}
		
		//bookmark ore
		Bookmarker bookmarker = new Bookmarker();
		bookmarker.addCategories(categories);
		for(Document doc : docs) {
			bookmarker.bookmark(doc);
		}

		ArrayList<Category> ore = bookmarker.getCategories();

		//sort ore and gold categories to be in same order
		Comparator<Category> catComp = new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};
		Collections.sort(ore, catComp);
		Collections.sort(gold, catComp);

		return prospect(ore, gold);
	}

	private Prospect prospect(ArrayList<Category> ore, ArrayList<Category> gold) {
		Prospect summary = new Prospect(); 
		summary.numCorrect = 0;
		summary.totalCount = 0;
		summary.prospects = new ArrayList<CategoryProspect>();

		for(int i = 0; i < ore.size(); i++) {
			ArrayList<Document> oreDocs = ore.get(i).getDocs();
			ArrayList<Document> goldDocs = gold.get(i).getDocs();

			//extract urls from docs
			Set<String> oreSet = new HashSet<String>();
			Set<String> goldSet = new HashSet<String>();

			for(Document doc : oreDocs) {
				oreSet.add(doc.getURL());
			}
			for(Document doc : goldDocs) {
				goldSet.add(doc.getURL());
			}

			CategoryProspect prospect = new CategoryProspect();
			prospect.tp = intersect(oreSet, goldSet);
			prospect.fn = minus(goldSet, oreSet);
			prospect.fp = minus(oreSet, goldSet);
			prospect.categoryName = ore.get(i).getName();
			prospect.confidence = 100.0 * prospect.tp.size()/(prospect.tp.size() + prospect.fp.size());
			prospect.recall = 100.0 * prospect.tp.size()/(prospect.tp.size() + prospect.fn.size());
			summary.prospects.add(prospect);

			summary.numCorrect += prospect.tp.size();
			summary.totalCount += oreSet.size();
		}
		
		summary.accuracy = (double)summary.numCorrect/summary.totalCount;
		return summary;
	}
	private Set<String> intersect(Set<String> a, Set<String> b) {
		Set<String> result = new HashSet<String>();
		Iterator<String> iter = a.iterator();
		while(iter.hasNext()) {
			String x = iter.next();
			if(b.contains(x))
				result.add(x);
		}
		return result;
	}
	private Set<String> minus(Set<String> a, Set<String> b) {
		Set<String> result = new HashSet<String>();
		Iterator<String> iter = a.iterator();
		while(iter.hasNext()) {
			String x = iter.next();
			if(!b.contains(x))
				result.add(x);
		}
		return result;
	}

	public static void main(String args[]) {
		Prospect prospect = new Prospector().prospect("gold.txt", "categories.txt", "urls.txt", false, true);
		System.out.println(prospect);
	}
}
