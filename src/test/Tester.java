package test;

import java.io.IOException;
import java.util.ArrayList;

import model.Bookmarker;
import model.Category;
import model.Document;

public class Tester {
	public static void main(String args[]) throws IOException {
		ArrayList<Category> gold = DataLoader.loadGold("smallGold.txt", true);
		
		ArrayList<Category> categories = DataLoader.loadCategories("smallCategories.txt");
		ArrayList<Document> docs = DataLoader.loadDocs("smallURLs.txt", true);
		Bookmarker bookmarker = new Bookmarker();
		bookmarker.addCategories(categories);
		bookmarker.bookmark(docs);
		
		bookmarker.printCategories();
	}
}
