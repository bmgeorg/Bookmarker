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

public class Tester {
	public void printSummary(ArrayList<Category> granite, ArrayList<Category> gold) {
		assert(granite.size() == gold.size());
		
		//sort lists
		Comparator<Category> catComp = new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};
		Collections.sort(granite, catComp);
		Collections.sort(gold, catComp);
		
		for(int i = 0; i < granite.size(); i++) {
			Category graniteCat = granite.get(i);
			Category goldCat = gold.get(i);
			
			ArrayList<Document> graniteDocs = graniteCat.getDocs();
			ArrayList<Document> goldDocs = goldCat.getDocs();
			
			//extract sets of urls
			Set<String> graniteSet = new HashSet<String>();
			Set<String> goldSet = new HashSet<String>();
			
			for(Document doc : graniteDocs) {
				graniteSet.add(doc.getURL());
			}
			for(Document doc : goldDocs) {
				goldSet.add(doc.getURL());
			}
			
			System.out.println(graniteCat.getName());
			printAandB(graniteSet, goldSet, "[TP] ");
			printAminusB(goldSet, graniteSet, "[FN] ");
			printAminusB(graniteSet, goldSet, "[FP] ");
			System.out.println();
		}		
	}
	public void printAandB(Set<String> a, Set<String> b, String prefix) {
		Iterator<String> iter = a.iterator();
		while(iter.hasNext()) {
			String x = iter.next();
			if(b.contains(x))
				System.out.println(prefix + x);
		}
	}
	public void printAminusB(Set<String> a, Set<String> b, String prefix) {
		Iterator<String> iter = a.iterator();
		while(iter.hasNext()) {
			String x = iter.next();
			if(!b.contains(x))
				System.out.println(prefix + x);
		}
	}

	public void compare(String goldFile, String categoriesFile, String urlFile) {
		ArrayList<Category> gold = DataLoader.loadGold(goldFile, true);

		ArrayList<Category> categories = DataLoader.loadCategories(categoriesFile);
		ArrayList<Document> docs = DataLoader.loadDocs("smallURLs.txt", true);
		Bookmarker bookmarker = new Bookmarker();
		bookmarker.addCategories(categories);
		bookmarker.bookmark(docs);

		ArrayList<Category> granite = bookmarker.getCategories();
		printSummary(granite, gold);
	}

	public static void main(String args[]) {
		new Tester().compare("smallGold.txt", "smallCategories.txt", "smallURLs.txt");
	}
}
