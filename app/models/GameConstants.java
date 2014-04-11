package models;

public class GameConstants {
	
	//Removing a fake annotation brings a fair amount of points
	public static final int REMOVE_FAKE_ANNOTATION_POINTS = 10;
	
	//Missing a fake annotation is highly penalised
	public static final int MISS_FAKE_ANNOTATION_POINTS = -20;
	
	//Grinding the points by validating a lot of assays
	public static final int CORRECT_VALIDATION_POINTS = 1;
	
	public static final String REMOVE_FAKE_ANNOTATION_MESSAGE = "Fake annotation correctly identified!";
	
	public static final String REMOVE_ANNOTATION_MESSAGE = "Annotation removed";
	
	public static final String MISS_FAKE_ANNOTATION_MESSAGE = "Some fake annotation(s) were still present!";
	
	public static final String ASSAY_VALIDATED_MESSAGE = "Assay successfully validated";
	
}
