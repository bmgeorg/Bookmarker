package test;


public class MultiProspector {
	/*
	 * Returns summary prospect data over multiple orderings of urls in urlFile
	 */
	public ProspectSummary summarizeProspects(String goldFile, String categoriesFile, String urlFile, int numReorderings) {
		assert numReorderings > 0;
		
		//get prospects 
		Prospect prospects[] = new Prospect[numReorderings];
		for(int i = 0; i < numReorderings; i++) {
			prospects[i] = new Prospector().prospect(goldFile, categoriesFile, urlFile, true);
		}
		
		//calculate summary
		ProspectSummary summary = new ProspectSummary();
		summary.minAccuracy = prospects[0].accuracy;
		summary.maxAccuracy = prospects[0].accuracy;
		summary.avgAccuracy = prospects[0].accuracy;
		for(int i = 1; i < prospects.length; i++) {
			if(prospects[i].accuracy < summary.minAccuracy)
				summary.minAccuracy = prospects[i].accuracy;
			if(prospects[i].accuracy > summary.maxAccuracy)
				summary.maxAccuracy = prospects[i].accuracy;
			summary.avgAccuracy += prospects[i].accuracy;
		}
		summary.avgAccuracy = summary.avgAccuracy/prospects.length;
		summary.stdDeviation = 0;
		for(int i = 0; i < prospects.length; i++) {
			summary.stdDeviation += (summary.avgAccuracy - prospects[i].accuracy)*(summary.avgAccuracy - prospects[i].accuracy);
		}
		summary.stdDeviation /= prospects.length;
		summary.stdDeviation = Math.sqrt(summary.stdDeviation);
		
		return summary;
	}
	
	public static void main(String args[]) {
		System.out.println(new MultiProspector().summarizeProspects("smallGold.txt", "smallCategories.txt", "smallURLs.txt", 500));
	}
}
