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
import model.DocumentMemento;
import model.Tokenizer;

/*
 * Loads category and bookmark data from a .txt file
 */
public class DataLoader {
	public static final String dataDir = "src/smallDataSet/";
	private static HashMap<String, DocumentMemento> docs = null;

	private static Object deserialize(String fileName) {
		String path = dataDir + fileName;
		try(FileInputStream f_in = new FileInputStream(path);
				ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
			return obj_in.readObject();
		} catch(FileNotFoundException e) {
			//the file cache was not found
			return null;
		} catch(IOException | ClassNotFoundException e) {
			//ClassNotFoundException should never happen
			//if it did, you changed the serialVersionUID for some previously stored object
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	private static void serialize(String fileName, Object obj) {
		String path = dataDir + fileName;
		try(FileOutputStream f_out = new FileOutputStream(path);
				ObjectOutputStream obj_out = new ObjectOutputStream (f_out)) {
			obj_out.writeObject(obj);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * reads lines until it reads a non-blank line,
	 * if line is end of file, returns null
	 * otherwise, makes that line the Category name,
	 * reads the next line and tokenizes it for tags
	 */
	private static Category readCategory(BufferedReader br) {
		String categoryName = null;
		String[] tags = null;

		try {
			//skip blank lines until you read a categoryName
			categoryName = br.readLine();
			while(categoryName != null && categoryName.equals(""))
				categoryName = br.readLine();
			//if end of file
			if(categoryName == null)
				return null;

			//get tags for any category except "Unsorted"
			if(categoryName.equals("Unsorted")) {
				//no tags for unsorted category
				tags = new String[0];
			} else {
				String tagsLine = br.readLine();
				tags = new Tokenizer().tokenize(tagsLine);
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return new Category(categoryName, tags);
	}

	@SuppressWarnings("unchecked")
	public static Document getDoc(String url, boolean loadFromCache) {
		//replace https with http to avoid certification storage issues
		//(?i) is for case-insensitive match
		url = url.trim().replaceAll("(?i)https", "http");

		//if docs == null, cache hasn't been loaded yet
		if(docs == null)
			docs = (HashMap<String, DocumentMemento>) deserialize("docs.obj");
		//if still null, then cache does not exist
		if(docs == null)
			docs = new HashMap<String, DocumentMemento>();
		
		//load doc from docs.obj
		if(loadFromCache) {
			if(docs.containsKey(url)) {
				DocumentMemento memento = docs.get(url);
				return new Document(memento);
			}
		}

		//have to reload document from Internet
		try {
			System.out.println("Loading from network: " + url);
			Document doc = new Document(url);
			docs.put(url, doc.getMemento());
			serialize("docs.obj", docs);
			return doc;
		} catch(Exception e) {
			//could be HttpStatusException or SocketTimeoutException or UnsupportedMimeTypeException or others
			//we don't really care, just skip the url and move on
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Purpose:
	 * Loads newline-separated urls from file, creates ArrayList of Documents from urls
	 * 
	 * Params:
	 * fileName:
	 * 	the name of the data file stored at DataLoader.dataDir
	 * 	Example: urls.txt
	 * useCachedDocs:
	 * 	if true, the method will load documents from a "docs.obj" cache file, loading from Internet if document is unfound
	 * 	in "docs.obj"
	 *	the method stores docs in the docs.obj file
	 * 
	 * File Format:
	 * url1
	 * url2
	 * url3
	 * url4
	 */
	public static ArrayList<Document> loadDocs(String fileName, boolean useCachedDocs) {
		String path = dataDir + fileName;
		ArrayList<Document> result = new ArrayList<Document>();
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line = br.readLine();
			while(line != null) {
				if(!line.equals("")) {
					Document doc = getDoc(line, useCachedDocs);
					if(doc != null)
						result.add(doc);
				}
				line = br.readLine();
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return result;
	}

	/*
	 * Purpose:
	 * Loads category names and tags from a file, creates ArrayList of Categories
	 * 
	 * Params:
	 * fileName:
	 * 	the name of the data file stored at DataLoader.dataDir
	 * 	Example: categories.txt
	 * 
	 * File Format:
	 * category1Name
	 * tag1, tag2, tag3
	 * 
	 * category2Name
	 * tag4, tag5, tag6
	 */
	public static ArrayList<Category> loadCategories(String fileName) {
		String path = dataDir + fileName;
		ArrayList<Category> categories = new ArrayList<Category>();
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			Category category = readCategory(br);
			while(category != null) {
				categories.add(category);
				category = readCategory(br);
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return categories;
	}

	/*
	 * Purpose:
	 * Loads manually-labeled category and bookmark data and creates ArrayList of categories with their respective
	 * bookmarks added.
	 * For each category, the method loads category name, category tags, and bookmarks into category ArrayList.
	 * 
	 * Params:
	 * fileName:
	 * 	the name of the data file stored at DataLoader.dataDir
	 * 	Example: gold.txt
	 * useCachedDocs:
	 * 	if true, the method will look up all documents in a docs.obj cache file instead of loading docs from Internet.
	 *	regardless of useCachedDocs value, the method will store docs in the docs.obj file
	 * File Format:
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
	 * 
	 * Unsorted
	 * url7
	 * url8
	 * url9
	 */
	public static ArrayList<Category> loadGold(String fileName, boolean useCachedDocs) {
		ArrayList<Category> categories = new ArrayList<Category>();
		String path = dataDir + fileName;
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			while(true) {
				Category category = readCategory(br);
				//if end of file
				if(category == null)
					break;
				categories.add(category);

				//read urls
				String url = br.readLine();
				while(url != null && !url.equals("")) {
					Document doc = getDoc(url, useCachedDocs);
					if(doc != null)
						category.addDocument(doc);
					url = br.readLine();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

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
		ArrayList<Category> data = DataLoader.loadGold("gold.txt", true);
		printCategoriesAndBookmarks(data);
	}
}
