package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import model.Category;
import model.Document;
import model.Tokenizer;

/*
 * Loads category and bookmark data from a .txt file
 */
public class DataLoader {
	private static final String pathPrefix = "src/dataSet_";

	private static Object getCache(int dataSet, String fileName) throws IOException, ClassNotFoundException {
		String path = pathPrefix + String.valueOf(dataSet) + "/" + fileName + ".obj";
		try(FileInputStream f_in = new FileInputStream(path);
				ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
			return obj_in.readObject();
		} catch(FileNotFoundException e) {
			//the file cache was not found
			return null;
		}
	}

	public static void createCache(int dataSet, String fileName, Object obj) throws IOException {
		String path = pathPrefix + String.valueOf(dataSet) + "/" + fileName + ".obj";
		try(FileOutputStream f_out = new FileOutputStream(path);
				ObjectOutputStream obj_out = new ObjectOutputStream (f_out)) {
			obj_out.writeObject(obj);
		}
	}


	/*
	 * LOADS MANUALLY LABELED DATA:
	 * 
	 * Params:
	 * dataSet:
	 * 	a positive number referencing package number to use
	 * 	Example: 1 for dataSet_1
	 * fileName:
	 * 	the name of the data file
	 * 	Example: cleanData.txt
	 * useCache:
	 * 	if true, the method will look for a .obj file to load the data from. The .obj file must be amed exactly the
	 * 	same as fileName (including fileName's extension), with .obj appended. If the .obj file could not be found,
	 * 	the method will load data fresh, as if useCache were equal to false.
	 * 	Example: if fileName is "cleanData.txt", the method will look for a file called "cleanData.txt.obj"
	 * 
	 * Purpose:
	 * For each category, loads category name, category tags, and bookmarks into category ArrayList.
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
	public static ArrayList<Category> loadLabeled(int dataSet, String fileName, boolean useCache) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<Category> categories;
		if(useCache) {
			@SuppressWarnings("unchecked")
			ArrayList<Category> cached = (ArrayList<Category>) getCache(dataSet, fileName);
			if(cached != null) {
				return cached;
			}
			//proceed as normal because cached == null => cached version doesn't exist
		}

		categories = new ArrayList<Category>();
		String path = pathPrefix + String.valueOf(dataSet) + "/" + fileName;
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			System.out.println("Loading labeled data");
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
					} catch(org.jsoup.HttpStatusException e) {
						e.printStackTrace();
					} catch(java.net.SocketTimeoutException e) {
						e.printStackTrace();
					} catch(org.jsoup.UnsupportedMimeTypeException e) {
						e.printStackTrace();
					} catch(Exception e) {
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

		//cache data
		createCache(dataSet, fileName, categories);
		
		return categories;
	}

	/* for testing */
	private static  void printCategoriesAndBookmarks(ArrayList<Category> categories) {
		System.out.println("Categories:");
		for(Category cat : categories) {
			System.out.println(cat.getName());
			System.out.println("tags");
			cat.printAdjustedTagWeights();
			System.out.println("urls");
			cat.printDocumentURLs();
			System.out.println();
		}
	}

	public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<Category> data = DataLoader.loadLabeled(1, "smallCleanData.txt", true);
		printCategoriesAndBookmarks(data);
	}
}
