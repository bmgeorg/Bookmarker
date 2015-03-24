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

	class ProspectSummary {
		double accuracy;
		int numCorrect;
		int totalCount;
		ArrayList<CategoryProspect> prospects;
		
		public String toString() {
			String result = "";
			for(CategoryProspect prospect : prospects) {
				result += prospect.toString() + "\n";
			}
			result += toShortString();
			return result;
		}
		
		public String toShortString() {
			return	"Num correct: " + String.valueOf(numCorrect) + "\n" +
					"Out of: " + String.valueOf(totalCount) + "\n" + 
					"Accuracy: " + String.valueOf(accuracy) + "\n";
		}
	}

	class CategoryProspect {
		String categoryName;
		double recall;
		double confidence;
		Set<String> tp;
		Set<String> fn;
		Set<String> fp;
		
		public String toString() {
			String result = "";
			result += categoryName + "\n";
			
			//add true positives
			for(String s : tp) {
				result += "[TP] " + s + "\n";
			}
			
			//add false negatives
			for(String s : fn) {
				result += "[FN] " + s + "\n";
			}
			
			//add false positives
			for(String s : fp) {
				result += "[FP] " + s + "\n";
			}

			result += "Recall: " + String.valueOf(recall) + "\n";
			result += "Confidence: " + String.valueOf(confidence) + "\n";
			
			return result;
		}
	}

	private ProspectSummary prospect(ArrayList<Category> ore, ArrayList<Category> gold) {
		ProspectSummary summary = new ProspectSummary(); 
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

	public void prospect(String goldFile, String categoriesFile, String urlFile) {
		ArrayList<Category> gold = DataLoader.loadGold(goldFile, true);

		ArrayList<Category> categories = DataLoader.loadCategories(categoriesFile);
		ArrayList<Document> docs = DataLoader.loadDocs("smallURLs.txt", true);
		Bookmarker bookmarker = new Bookmarker();
		bookmarker.addCategories(categories);
		bookmarker.bookmark(docs);

		ArrayList<Category> ore = bookmarker.getCategories();

		//sort lists
		Comparator<Category> catComp = new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};
		Collections.sort(ore, catComp);
		Collections.sort(gold, catComp);

		ProspectSummary summary = prospect(ore, gold);
		System.out.println(summary.toString());
	}

	public static void main(String args[]) {
		new Prospector().prospect("smallGold.txt", "smallCategories.txt", "smallURLs.txt");
	}
}
