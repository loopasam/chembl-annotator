package jobs;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import models.AnnotatedAssay;
import models.Reviewer;

import com.google.common.base.Stopwatch;

import play.Logger;
import play.jobs.Job;

public class AssignReviewersJob extends Job {

	public void doJob(){
		Logger.info("Assigning assays...");
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		stopwatch.start();

		List<AnnotatedAssay> assays = AnnotatedAssay.find("reviewer is null").fetch();

		int total = assays.size();
		int counter = 0;

		//Get all reviewer except super cool bot
		List<Reviewer> reviewers = Reviewer.find("email != 'super.cool.bot@gmail.com'").fetch();

		int totalReviewers = reviewers.size()-1;
		int counterFlush = 0;
		
		for (AnnotatedAssay assay : assays) {
			counter++;
			Logger.info("Assay: " + counter + "/" + total);
			//get random reviewer
			Random rand = new Random();
			int randomNum = rand.nextInt((totalReviewers - 0) + 1);
			assay.setReviewer(reviewers.get(randomNum));
			
			counterFlush++;
			if (counterFlush%100 == 0) {
				AnnotatedAssay.em().flush();
				AnnotatedAssay.em().clear();
			}

		}

		stopwatch.stop();
		Logger.info("Assignement job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
	}

}
