package test;


public class MultiProspector {
	/*
	 * Returns summary prospect data over multiple orderings of urls in urlFile
	 */
	public ProspectSummary summarizeProspects(String goldFile, String categoriesFile, String urlFile, int numReorderings, boolean useCache) {
		assert numReorderings > 0;
		
		//get prospects 
		Prospect prospects[] = new Prospect[numReorderings];
		for(int i = 0; i < numReorderings; i++) {
			System.out.println(i);
			prospects[i] = new Prospector().prospect(goldFile, categoriesFile, urlFile, true, useCache);
		}
		
		//calculate summary
		ProspectSummary summary = new ProspectSummary();
		summary.minRecall = prospects[0].totalRecall;
		summary.maxRecall = prospects[0].totalRecall;
		summary.avgRecall = prospects[0].totalRecall;
		summary.minPrecision = prospects[0].totalPrecision;
		summary.maxPrecision = prospects[0].totalPrecision;
		summary.avgPrecision = prospects[0].totalPrecision;
		for(int i = 1; i < prospects.length; i++) {
			if(prospects[i].totalRecall < summary.minRecall)
				summary.minRecall = prospects[i].totalRecall;
			if(prospects[i].totalRecall > summary.maxRecall)
				summary.maxRecall = prospects[i].totalRecall;
			summary.avgRecall += prospects[i].totalRecall;
			
			if(prospects[i].totalPrecision < summary.minPrecision)
				summary.minPrecision = prospects[i].totalPrecision;
			if(prospects[i].totalPrecision > summary.maxPrecision)
				summary.maxPrecision = prospects[i].totalPrecision;
			summary.avgPrecision += prospects[i].totalPrecision;
		}
		summary.avgRecall /= prospects.length;
		summary.avgPrecision /= prospects.length; 
		
		//compute recall standard deviation
		summary.stdDevRecall = 0;
		for(int i = 0; i < prospects.length; i++) {
			summary.stdDevRecall += (summary.avgRecall - prospects[i].totalRecall)*(summary.avgRecall - prospects[i].totalRecall);
		}
		summary.stdDevRecall /= prospects.length;
		summary.stdDevRecall = Math.sqrt(summary.stdDevRecall);
		
		//compute precision standard deviation
		summary.stdDevPrecision = 0;
		for(int i = 0; i < prospects.length; i++) {
			summary.stdDevPrecision += (summary.avgPrecision - prospects[i].totalPrecision)*(summary.avgPrecision - prospects[i].totalPrecision);
		}
		summary.stdDevPrecision /= prospects.length;
		summary.stdDevPrecision = Math.sqrt(summary.stdDevPrecision);
		
		return summary;
	}
	
	public static void main(String args[]) {
		System.out.println(new MultiProspector().summarizeProspects("gold.txt", "categories.txt", "urls.txt", 500, true));
	}
}
