package test;

import java.util.ArrayList;
import java.util.Set;

class Prospect {
	double totalRecall;
	double totalPrecision;
	int numCorrect;
	int numOre;
	int numGold;
	ArrayList<CategoryProspect> prospects;
	
	@Override
	public String toString() {
		String result = "";
		for(CategoryProspect prospect : prospects) {
			result += prospect.toString() + "\n";
		}
		result += String.format("Num correct: %d\n", numCorrect);
		result += String.format("Num ore: %d\n", numOre);
		result += String.format("Num gold: %d\n", numGold);
		result += String.format("Total recall: %.2f\n", totalRecall);
		result += String.format("Total precision: %.2f\n", totalPrecision);
		return result;
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
	double avgRecall;
	double minRecall;
	double maxRecall;
	double stdDevRecall;
	
	double avgPrecision;
	double minPrecision;
	double maxPrecision;
	double stdDevPrecision;
	
	@Override
	public String toString() {
		String result = "";
		
		result += String.format("Average total recall: %.2f\n", avgRecall);
		result += String.format("Standard deviation: %.2f\n", stdDevRecall);
		result += String.format("Min total recall: %.2f\n", minRecall);
		result += String.format("Max total recall: %.2f\n", maxRecall);
		result += "\n";
		
		result += String.format("Average total precision: %.2f\n", avgPrecision);
		result += String.format("Standard deviation: %.2f\n", stdDevPrecision);
		result += String.format("Min total precision: %.2f\n", minPrecision);
		result += String.format("Max total precision: %.2f\n", maxPrecision);
		
		return result;
	}
}