package test;

import java.util.ArrayList;
import java.util.Set;

class Prospect {
	double accuracy;
	int numCorrect;
	int totalCount;
	ArrayList<CategoryProspect> prospects;
	
	@Override
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
				String.format("Accuracy: %.2f\n", accuracy);
	}
}

class CategoryProspect {
	String categoryName;
	double recall;
	double confidence;
	Set<String> tp;
	Set<String> fn;
	Set<String> fp;
	
	@Override
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

		result += String.format("Recall: %.2f\n", recall);
		result += String.format("Confidence: %.2f\n", confidence);
		
		return result;
	}
}

class ProspectSummary {
	double avgAccuracy;
	double minAccuracy;
	double maxAccuracy;
	double stdDeviation;
	
	@Override
	public String toString() {
		String result = "";
		result += String.format("Average accuracy: %.2f\n", avgAccuracy);
		result += String.format("Standard deviation: %.2f\n", stdDeviation);
		result += String.format("Min accuracy: %.2f\n", minAccuracy);
		result += String.format("Max accuracy: %.2f\n", maxAccuracy);
		return result;
	}
}