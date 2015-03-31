package model;

public class CategoryReport {
	public String name;
	public double score;
	public Tag tags[];
	public double docWeights[];
	
	public CategoryReport(String name, double score, Tag tags[], double docWeights[]) {
		this.name = name;
		this.score = score;
		this.tags = tags;
		this.docWeights = docWeights;
		
		//sort tags and docweights by tag weight
		//selection sort
		for(int i = 0; i < tags.length; i++) {
			int max = i;
			for(int j = i+1; j < tags.length; j++) {
				if(tags[j].getWeight() > tags[max].getWeight()) {
					max = j;
				}
			}
			
			//swap tags
			Tag temp = tags[i];
			tags[i] = tags[max];
			tags[max] = temp;
			
			//swap docWeights
			double temp2 = docWeights[i];
			docWeights[i] = docWeights[max];
			docWeights[max] = temp2;
		}
	}
	
	@Override
	public String toString() {
		String result = "";
		result += name + "\n";
		result += String.format("Score: %f\n", score);
		result += String.format("%-20scategory\tdocument\n", "");
		for(int i = 0; i < tags.length; i++) {
			result += String.format("%-20s%f\t%f\n", tags[i].getTerm(), tags[i].getWeight(), docWeights[i]);
		}
		return result;
	}
}
