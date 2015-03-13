package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import model.Category;

public class Tester {

	public static void main(String args[]) throws FileNotFoundException, IOException {
		ArrayList<Category> gold = DataLoader.loadGold("smallGold.txt", true);
		
	}
}
