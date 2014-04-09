package jobs;

import java.util.concurrent.TimeUnit;

import models.AnnotatedAssay;
import models.BaoTerm;

import com.google.common.base.Stopwatch;

import play.Logger;
import play.jobs.Job;

public class FakeAnnotationsJob extends Job {

	public static int numberOfFakeAnnotations = 10;

	public void doJob(){
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		stopwatch.start();

		for (int i = 1; i <= numberOfFakeAnnotations; i++) {
			Logger.info("Fake annotation: " + i + "/" + numberOfFakeAnnotations);
			AnnotatedAssay assay = AnnotatedAssay.find("order by rand()").first();
			assay.addFakeAnnotation();
		}
		
		stopwatch.stop();
		Logger.info("Job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

	}

}
