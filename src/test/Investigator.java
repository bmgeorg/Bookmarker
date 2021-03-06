package test;

import java.util.ArrayList;

import model.BookmarkReport;
import model.Bookmarker;
import model.Category;
import model.Document;

/*
 * reports on why a bookmark gets placed in a particular category
 */
public class Investigator {
	public BookmarkReport investigate(String url, String categoriesFile, String urlFile, boolean useCache) {
		//load ore
		ArrayList<Category> categories = DataLoader.loadCategories(categoriesFile);
		ArrayList<Document> docs = DataLoader.loadDocs(urlFile, useCache);

		//bookmark ore
		Bookmarker bookmarker = new Bookmarker();
		bookmarker.addCategories(categories);
		for(int i = 0; i < docs.size(); i++) {
			Document doc = docs.get(i);
			BookmarkReport report = bookmarker.bookmark(doc);
			System.out.print(doc.getBaseURI());
			if(!doc.getBaseURI().equals(url)) {
				System.out.println(" - " + report.categoryReports[0].name);
			} else {
				System.out.println();
				return report;
			}
		}
		
		return null;
	}
	
	public static void main(String args[]) {
		BookmarkReport report = new Investigator().investigate("http://protractor.ninja/", "categories.txt", "urls.txt", true);
		System.out.println(report.toString());
	}
}
