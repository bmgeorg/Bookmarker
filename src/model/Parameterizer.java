package model;

public class Parameterizer {
	/* Document */
	public static double TEXT_WEIGHT= 1;
	public static double TITLE_WEIGHT = 3;
	public static double META_DESCRIPTION_WEIGHT = 1;
	public static double IMAGE_ALT_WEIGHT = 0.5;
	public static double H1_EXTRA_WEIGHT = 1;
	public static double DOMAIN_TOKEN_WEIGHT = 1;
	//the weight of a 2-gram is [WEIGHT]*TWO_GRAM_WEIGHT_MULTIPLIER
	public static double TWO_GRAM_WEIGHT_MULTIPLIER = 2;
	
	/* Category */
	public static int MAX_USER_TAGS = 10;
	public static int MAX_DISCOVERED_TAGS = 10;
	public static double WEIGHT_FOR_USER_TAGS = 3; //will be scaled down by number of user tags
	public static double WEIGHT_PER_MATCHED_DOMAIN = 0.01; //not scaled
	
	/* Bookmarker */
	/*
	 * a constant used for multiple categorization
	 * MULTIPLE_CATEGORY_THRESHOLD is in [0, 1]
	 * add a document to all categories with scores >= MULTIPLE_CATEGORY_THRESHOLD*(maximum category score)
	 */
	public static double MULTIPLE_CATEGORY_THRESHOLD = 0.7;
	public static double UNSORTED_THRESHOLD = 0.01;
}
