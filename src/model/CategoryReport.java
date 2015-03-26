package model;

public class CategoryReport {
	String name;
	double score;
	Tag tags[];
	double docWeights[];
	
	@Override
	public String toString() {
		String result = "";
		result += name + "\n";
		result += String.format("Score: %f\n", score);
		result += String.format("%-20scategory\tdocument\n", "tag");
		for(int i = 0; i < tags.length; i++) {
			result += String.format("%-20s%f\t%f\n", tags[i].getTerm(), tags[i].getWeight(), docWeights[i]);
		}
		return result;
	}
}
