package jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.AnnotatedAssay;
import models.Annotation;
import models.BaoTerm;
import play.Logger;
import play.jobs.Job;

public class SemanticSimplificationJob extends Job {

	public void doJob(){
		
		Logger.info("Simplification started...");

		List<AnnotatedAssay> assays = AnnotatedAssay.findAll();

		int total = assays.size();
		int counter = 0;

		for (AnnotatedAssay annotatedAssay : assays) {
			counter++;
			//TODO uncomment
			//Logger.info("Assay " + counter + "/" + total);
			annotatedAssay.doSemanticSimplification();
		}
		
		Logger.info("Job done...");
	}

}
