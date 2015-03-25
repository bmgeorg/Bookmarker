package model;

import java.io.Serializable;

/*
 * a serializable memento of Document for caching Documents
 */
public class DocumentMemento implements Serializable {
	private static final long serialVersionUID = -9223348160074525072L;
	String baseURI;
	String html;
	DocumentMemento(String html, String baseURI) {
		this.html = html;
		this.baseURI = baseURI;
	}
}