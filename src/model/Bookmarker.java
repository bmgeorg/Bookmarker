package model;

import java.io.IOException;
import java.util.ArrayList;

public class Bookmarker {
	private ArrayList<Category> categories = new ArrayList<Category>();
	
	public void addCategory(Category newCategory) {
		categories.add(newCategory);
	}
	
	public Category bookmark(String url) throws IOException {
		Document doc = new Document(url);
		double bestScore = 0;
		Category bestCategory = null;
		for(int i = 0; i < categories.size(); i++) {
			double score = categories.get(i).score(doc);
			if(score > bestScore) {
				bestCategory = categories.get(i);
				bestScore = score;
			}
		}
		
		if(bestCategory != null) {
			bestCategory.addDocument(doc);
			return bestCategory;
		} else {
			return null;
		}
	}
	
	public void printCategories() {
		System.out.println("Bookmarker Categories");
		for(int i = 0; i < categories.size(); i++) {
			Category cat = categories.get(i);
			System.out.println(cat.getName());
			cat.printDocumentURLs();
			cat.printRawTagWeights();
			System.out.println();
		}
	}
	
	public static void main(String args[]) throws IOException {
		Bookmarker engine = new Bookmarker();
		Category cat1 = new Category("Linear Programming", "programming", "simplex", "revised", "program");
		Category cat2 = new Category("Design", "design", "user", "elegant", "martin", "fowler");
		Category cat3 = new Category("Programming", "programming", "program", "compile", "Java", "C", "C++", "Objective-C");
		engine.addCategory(cat1);
		engine.addCategory(cat2);
		engine.addCategory(cat3);
		
		engine.bookmark("http://martinfowler.com/articles/designDead.html");
		engine.bookmark("http://en.wikipedia.org/wiki/Linear_programming");
		engine.bookmark("http://en.wikipedia.org/wiki/Revised_simplex_method");
		engine.bookmark("http://david.heinemeierhansson.com/2014/tdd-is-dead-long-live-testing.html");
		engine.bookmark("http://stackoverflow.com/questions/1154008/any-way-to-declare-an-array-in-line");
		engine.bookmark("http://stackoverflow.com/questions/17924999/java-multiple-variable-length-argument");
		
		engine.printCategories();
	}
}
