package model;

import java.util.Arrays;
import java.util.Comparator;

public class BookmarkReport {
	public CategoryReport[] categoryReports;
	public BookmarkReport(CategoryReport[] reports) {
		categoryReports = reports;
		
		//sort reports in descending order
		Arrays.sort(categoryReports, new Comparator<CategoryReport>() {
			@Override
			public int compare(CategoryReport o1, CategoryReport o2) {
				if(o1.score < o2.score)
					return 1;
				else if(o1.score > o2.score)
					return -1;
				else
					return 0;
			}
		});
	}
	
	@Override
	public String toString() {
		String result = "";
		for(int i = 0; i < categoryReports.length; i++) {
				result += categoryReports[i].toString() + "\n";
		}
		
		return result;
	}
}