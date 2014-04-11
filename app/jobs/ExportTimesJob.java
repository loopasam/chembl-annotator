package jobs;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.AnnotatedAssay;
import models.Reviewer;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class ExportTimesJob extends Job {

	public void doJob() throws IOException {

		Logger.info("Exporting times for all users...");
		//select all the users and put the ids in a list excepts the bot
		List<Reviewer> reviewers = Reviewer.find("email != 'super.cool.bot@gmail.com'").fetch();

		for (Reviewer reviewer : reviewers) {

			File report = new File("data/times/" + reviewer.email + ".csv");
			String reportContent = "";

			List<AnnotatedAssay> assays = AnnotatedAssay.find("reviewer_id = ? and needReview = false order by validationDate", reviewer.id).fetch();

			for (int i = 1; i < assays.size(); i++) {
				AnnotatedAssay old = assays.get(i-1);
				AnnotatedAssay young = assays.get(i);
				long time = (young.validationDate.getTime() - old.validationDate.getTime());
				reportContent += time + "\n";
			}
			
			FileUtils.writeStringToFile(report, reportContent);
		}

		Logger.info("Export done.");
	}

}
