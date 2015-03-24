package test;

import java.util.ArrayList;
import java.util.Set;

class Prospect {
	double accuracy;
	int numCorrect;
	int totalCount;
	ArrayList<CategoryProspect> prospects;
	
	public String toString() {
		String result = "";
		for(CategoryProspect prospect : prospects) {
			result += prospect.toString() + "\n";
		}
		result += toShortString();
		return result;
	}
	
	public String toShortString() {
		return	"Num correct: " + String.valueOf(numCorrect) + "\n" +
				"Out of: " + String.valueOf(totalCount) + "\n" + 
				"Accuracy: " + String.valueOf(accuracy) + "\n";
	}
}

class CategoryProspect {
	String categoryName;
	double recall;
	double confidence;
	Set<String> tp;
	Set<String> fn;
	Set<String> fp;
	
	public String toString() {
		String result = "";
		result += categoryName + "\n";
		
		//add true positives
		for(String s : tp) {
			result += "[TP] " + s + "\n";
		}
		
		//add false negatives
		for(String s : fn) {
			result += "[FN] " + s + "\n";
		}
		
		//add false positives
		for(String s : fp) {
			result += "[FP] " + s + "\n";
		}

		result += "Recall: " + String.valueOf(recall) + "\n";
		result += "Confidence: " + String.valueOf(confidence) + "\n";
		
		return result;
	}
}