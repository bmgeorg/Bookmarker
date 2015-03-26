package model;

public class BookmarkReport {
	int chosenCategoryIndex;
	CategoryReport[] categoryReports;
	
	@Override
	public String toString() {
		String result = "";
		result += "Chosen Category\n\n";
		result += categoryReports[chosenCategoryIndex].toString() + "\n";
		result += "Other Categories\n\n";
		for(int i = 0; i < categoryReports.length; i++) {
			if(i != chosenCategoryIndex) {
				result += categoryReports[i].toString() + "\n";
			}
		}
		
		return result;
	}
}