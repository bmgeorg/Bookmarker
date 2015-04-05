package model;

import java.io.IOException;
import java.util.ArrayList;

public class Bookmarker {
	//contains all categories except unsorted
	private ArrayList<Category> categories = new ArrayList<Category>();
	private Category unsorted = new Category("Unsorted");

	public BookmarkReport bookmark(Document doc) {
		CategoryReport[] categoryReports = new CategoryReport[categories.size()];

		if(categories.size() == 0)
			return null;

		for(int i = 0; i < categories.size(); i++) {
			categoryReports[i] = categories.get(i).score(doc);
		}

		//find best score
		int bestIndex = 0;
		for(int i = 1; i < categories.size(); i++) {
			if(categoryReports[i].score >= categoryReports[bestIndex].score)
				bestIndex = i;
		}

		double bestScore = categoryReports[bestIndex].score;
		double multiCategoryThreshold = bestScore*Parameterizer.MULTIPLE_CATEGORY_THRESHOLD;

		//add document to all categories with score above multiCategoryThreshold and UNSORTED_THRESHOLD
		for(int i = 0; i < categories.size(); i++) {
			if(categoryReports[i].score >= multiCategoryThreshold &&
				categoryReports[i].score >= Parameterizer.UNSORTED_THRESHOLD) {
				categories.get(i).addDocument(doc);
			}
		}
		
		//put category in unsorted category if it doesn't match any category
		if(bestScore < Parameterizer.UNSORTED_THRESHOLD) {
			unsorted.addDocument(doc);
		}

		return new BookmarkReport(categoryReports);
	}

	public void addCategories(ArrayList<Category> newCategories) {
		for(Category category : newCategories) {
			addCategory(category);
		}
	}

	public void addCategory(Category newCategory) {
		categories.add(newCategory);
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}

	public Category getUnsortedCategory() {
		return unsorted;
	}

	/* testing */
	public void printCategories() {
		System.out.println("Bookmarker Categories");
		for(int i = 0; i < categories.size(); i++) {
			Category cat = categories.get(i);
			System.out.println(cat.getName());
			cat.printAdjustedTagWeights();
			cat.printDocumentURLs();
			System.out.println();
		}
		System.out.println(unsorted.getName());
		unsorted.printAdjustedTagWeights();
		unsorted.printDocumentURLs();
		System.out.println();
	}

	public static void main(String args[]) throws IOException {
		Bookmarker engine = new Bookmarker();
		Category cat1 = new Category("Linear Programming", "programming", "simplex", "revised", "program");
		Category cat2 = new Category("Design", "design", "user", "elegant", "martin", "fowler");
		Category cat3 = new Category("Programming", "programming", "program", "compile", "Java", "C", "C++", "Objective-C");
		engine.addCategory(cat1);
		engine.addCategory(cat2);
		engine.addCategory(cat3);

		engine.bookmark(new Document("http://martinfowler.com/articles/designDead.html"));
		engine.bookmark(new Document("http://en.wikipedia.org/wiki/Linear_programming"));
		engine.bookmark(new Document("http://en.wikipedia.org/wiki/Revised_simplex_method"));
		engine.bookmark(new Document("http://david.heinemeierhansson.com/2014/tdd-is-dead-long-live-testing.html"));
		engine.bookmark(new Document("http://stackoverflow.com/questions/1154008/any-way-to-declare-an-array-in-line"));
		engine.bookmark(new Document("http://stackoverflow.com/questions/17924999/java-multiple-variable-length-argument"));

		engine.printCategories();
	}
}
