package model;

import java.io.IOException;

import org.jsoup.Jsoup;

public class Indexer {
	public static Document index(String url) throws IOException {
		System.out.println(Jsoup.connect(url).get().text());
		return null;
	}

	public static void main(String args[]) throws IOException { 
		Indexer.index("http://lipsum.org");
	}
}