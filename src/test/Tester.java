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
	private void printSummary(ArrayList<Category> granite, ArrayList<Category> gold) {
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
		
		int totalTP = 0;
		int totalNum = 0;
		
		for(int i = 0; i < granite.size(); i++) {
			ArrayList<Document> graniteDocs = granite.get(i).getDocs();
			ArrayList<Document> goldDocs = gold.get(i).getDocs();
			
			//extract sets of urls
			Set<String> graniteSet = new HashSet<String>();
			Set<String> goldSet = new HashSet<String>();
			
			for(Document doc : graniteDocs) {
				graniteSet.add(doc.getURL());
			}
			for(Document doc : goldDocs) {
				goldSet.add(doc.getURL());
			}
			
			Set<String> tp = intersect(graniteSet, goldSet);
			Set<String> fn = minus(goldSet, graniteSet);
			Set<String> fp = minus(graniteSet, goldSet);
			
			totalTP += tp.size();
			totalNum += graniteSet.size();
			
			double recall = 100.0 * tp.size()/(tp.size() + fn.size());
			double confidence = 100.0 * tp.size()/(tp.size() + fp.size());
			
			System.out.println(granite.get(i).getName());
			System.out.println("Recall: " + String.valueOf(recall));
			System.out.println("Confidence: " + String.valueOf(confidence));
			printSet(tp, "[TP] ");
			printSet(fn, "[FN] ");
			printSet(fp, "[FP] ");
			System.out.println();
		}
		System.out.println();
		double accuracy = 100.0 * totalTP / totalNum;
		System.out.println("Num correct: " + String.valueOf(totalTP));
		System.out.println("Out of: " + String.valueOf(totalNum));
		System.out.println("Accuracy: " + String.valueOf(accuracy));
	}
	public void printSet(Set<String> set, String prefix) {
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			System.out.println(prefix + iter.next());
		}
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
