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
		for(Document doc : docs) {
			System.out.println(doc.getBaseURI());
			if(doc.getBaseURI().equals(url))
				return bookmarker.bookmark(doc);
			else
				bookmarker.bookmark(doc);
		}
		
		return null;
	}
	
	public static void main(String args[]) {
		BookmarkReport report = new Investigator().investigate("https://blog.nelhage.com/2010/08/write-yourself-an-strace-in-70-lines-of-code/", "categories.txt", "urls.txt", true);
		System.out.println(report.toString());
	}
}
