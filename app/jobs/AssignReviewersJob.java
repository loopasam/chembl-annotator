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
		
		Reviewer.randomReviewer();

		int totalReviewers = reviewers.size()-1;
		
		for (AnnotatedAssay assay : assays) {
			counter++;
			Logger.info("Assay: " + counter + "/" + total);
			//get random reviewer
			
			Stopwatch swRand = Stopwatch.createUnstarted();
			swRand.start();

			Random rand = new Random();
			int randomNum = rand.nextInt((totalReviewers - 0) + 1);
			
			swRand.stop();
			Logger.info("rand " + swRand.elapsed(TimeUnit.MILLISECONDS) + " millisecs.");
			
			
			
			Stopwatch swset = Stopwatch.createUnstarted();
			swset.start();

			assay.setReviewer(reviewers.get(randomNum));
			
			swset.stop();
			Logger.info("rand " + swset.elapsed(TimeUnit.MILLISECONDS) + " millisecs.");
			
		}

		stopwatch.stop();
		Logger.info("Assignement job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
	}

}
