package jobs;

import groovy.ui.text.FindReplaceUtility;

import java.util.List;

import models.AnnotatedAssay;
import models.Annotation;
import play.Logger;
import play.jobs.Job;

public class RemoveFakeAnnotationJob extends Job {
	
	public void doJob(){
		Logger.info("Removing fake annotations...");
		List<Annotation> annotations = Annotation.find("isFake is true and assay.needReview is false").fetch();
		Logger.info("There was still " + annotations.size() + " still present " +
				"(starting from " + FakeAnnotationsJob.numberOfFakeAnnotations + ").");
		
		for (Annotation annotation : annotations) {
			Long assayId = annotation.assay.id;
			AnnotatedAssay assay = AnnotatedAssay.findById(assayId);
			assay.removeAnnotation(annotation.id);
		}
		
		Logger.info("Fake annotations removed.");
	}

}
