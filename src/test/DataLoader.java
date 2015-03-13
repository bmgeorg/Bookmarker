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
import java.util.HashMap;

import model.Category;
import model.Document;
import model.Tokenizer;

/*
 * Loads category and bookmark data from a .txt file
 */
public class DataLoader {
	private static final String pathPrefix = "src/dataSet_1/";
	private static HashMap<String, Document> docs = null;

	private static Object getCache(String fileName) throws IOException, ClassNotFoundException {
		String path = pathPrefix + fileName;
		try(FileInputStream f_in = new FileInputStream(path);
				ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
			return obj_in.readObject();
		} catch(FileNotFoundException e) {
			//the file cache was not found
			return null;
		}
	}

	private static void createCache(String fileName, Object obj) throws IOException {
		String path = pathPrefix + fileName;
		try(FileOutputStream f_out = new FileOutputStream(path);
				ObjectOutputStream obj_out = new ObjectOutputStream (f_out)) {
			obj_out.writeObject(obj);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Document getDoc(String url, boolean useCache) throws ClassNotFoundException, IOException {
		//if docs == null, cache hasn't been loaded yet
		if(docs == null)
			docs = (HashMap<String, Document>) getCache("docs.obj");
		//if still null, then cache does not exist
		if(docs == null)
			docs = new HashMap<String, Document>();
		if(useCache) {
			if(docs.containsKey(url))
				return docs.get(url);
		}

		Document doc = new Document(url);
		docs.put(url, doc);
		createCache("docs.obj", docs);
		return doc;
	}


	/*
	 * LOADS MANUALLY LABELED DATA:
	 * 
	 * Params:
	 * fileName:
	 * 	the name of the data file
	 * 	Example: cleanData.txt
	 * useCachedCategories:
	 * 	if true, the method will look for a .obj file to load the data from. The .obj file must be named exactly the
	 * 	same as fileName (including fileName's extension), with .obj appended. If the .obj file could not be found,
	 * 	the method will load data fresh, as if useCachedCategories were equal to false.
	 * 	Example: if fileName is "cleanData.txt", the method will look for a file called "cleanData.txt.obj"
	 * useCachedDocs:
	 * 	if true, the method will look up all documents in a docs.obj cache file instead of indexing docs from Internet.
	 * 	if useCachedCategories is true and the cached categories file exists, useCachedDocs is ignored.
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
	public static ArrayList<Category> loadLabeled(String fileName, boolean useCachedCategories, boolean useCachedDocs) throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<Category> categories;
		if(useCachedCategories) {
			@SuppressWarnings("unchecked")
			ArrayList<Category> cached = (ArrayList<Category>) getCache(fileName+".obj");
			if(cached != null) {
				return cached;
			}
			//proceed as normal because cached version doesn't exist
		}

		categories = new ArrayList<Category>();
		String path = pathPrefix + fileName;
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
						category.addDocument(getDoc(url, useCachedDocs));
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
		createCache(fileName + ".obj", categories);

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
		ArrayList<Category> data = DataLoader.loadLabeled("smallCleanData.txt", false, true);
		printCategoriesAndBookmarks(data);
	}
}
