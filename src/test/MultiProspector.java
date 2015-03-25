package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MultiProspector {
	/*
	 * Returns summary prospect data over multiple orderings of urls in urlFile
	 */
	public ProspectSummary summarizeProspects(String goldFile, String categoriesFile, String urlFile, int numReorderings) {
		assert numReorderings > 0;
		
		//get prospects 
		String reorderedURLs = "tempURLFile";
		Prospect prospects[] = new Prospect[numReorderings];
		for(int i = 0; i < numReorderings; i++) {
			try {
				LineScrambler.reorderLines(DataLoader.dataDir + urlFile, DataLoader.dataDir + reorderedURLs);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			prospects[i] = new Prospector().prospect(goldFile, categoriesFile, reorderedURLs);
		}
		//delete temp file
		try {
		    Files.delete(Paths.get(DataLoader.dataDir + reorderedURLs));
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
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
		
		return summary;
	}
	
	public static void main(String args[]) {
		System.out.println(new MultiProspector().summarizeProspects("smallGold.txt", "smallCategories.txt", "smallURLs.txt", 30));
	}
}
