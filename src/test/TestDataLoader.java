package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import model.Category;
import model.Document;
import model.Tokenizer;

/*
 * Loads manually-classified category and bookmark data from a .txt file
 */
public class TestDataLoader {
	private ArrayList<Category> categories;

	/*
	 * For each category,
	 * loads category name, category tags, and bookmarks in category.
	 * 
	 * Expects category data in the following format:
	 * category1Name
	 * tag1, tag2, tag3
	 * url1
	 * url2
	 * url3
	 * 
	 * category2Name
	 * tag4, tag5, tag6
	 * url4
	 * url5
	 * url6
	 */
	public void load(String filePath) throws FileNotFoundException, IOException {
		categories = new ArrayList<Category>();

		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			while(true) {
				//get category name
				String categoryName = br.readLine();
				//skip as many blank lines as necessary
				while(categoryName != null && categoryName.trim().equals(""))
					categoryName = br.readLine();
				//if end of file
				if(categoryName == null)
					break;
				categoryName = categoryName.trim();
				
				System.out.println(categoryName);
				
				//get tags
				String tagsLine = br.readLine();
				//if end of file
				if(tagsLine == null) {
					categories.add(new Category(categoryName, ""));
					break;
				}
				String[] tags = new Tokenizer().tokenize(tagsLine);
				for(String tag :tags)
					System.out.println(tag);
				Category category = new Category(categoryName, tags);
				categories.add(category);
				
				//get urls
				String url = br.readLine();
				while(url != null && !url.trim().equals("")) {
					System.out.println(url);
					//replace https with http to avoid certification storage issues
					//(?i) is for case-insensitive match
					url = url.trim().replaceAll("(?i)https", "http");
					try {
					category.addDocument(new Document(url));
					} catch (org.jsoup.HttpStatusException e) {
						e.printStackTrace();
					} catch (java.net.SocketTimeoutException e) {
						e.printStackTrace();
					} catch (org.jsoup.UnsupportedMimeTypeException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					url = br.readLine();
				}
				System.out.println();
				//if end of file
				if(url == null)
					break;
			}
		}
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}

	public void printCategoriesAndBookmarks() {
		System.out.println("Categories:");
		for(Category cat : categories) {
			System.out.println(cat.getName());
			cat.printDocumentURLs();
			System.out.println();
		}
	}

	public static void main(String args[]) throws FileNotFoundException, IOException {
		TestDataLoader loader = new TestDataLoader();
		loader.load("dummyCategorizedBookmarks.txt");
		loader.printCategoriesAndBookmarks();
	}
}
