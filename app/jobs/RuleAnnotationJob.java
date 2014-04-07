package jobs;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Stopwatch;


import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class RuleAnnotationJob extends Job {

	public void doJob() throws SQLException, IOException{

		Stopwatch stopwatch = Stopwatch.createUnstarted();
		stopwatch.start();

		List<AnnotationRule> rules = AnnotationRule.find("select r from AnnotationRule r " +
				"where highlight = false").fetch();
		
		//TODO change by using the robot user
		Reviewer robot = Reviewer.find("byEmail", "samuel.croset@gmail.com").first();

		//Report init
		File report = new File("data/annotation-report-rules.txt");
		String reportContent = "";

		int counter = 0;
		int total = rules.size();

		for (AnnotationRule annotationRule : rules) {
			counter++;

			String termMessage = "Term: " + annotationRule.baoTerm.label + "(" + annotationRule.baoTerm.baoId + ") - " + counter + "/" + total;
			reportContent += termMessage + "\n";
			Logger.info(termMessage);
			
			String rule = annotationRule.rule + ";";
			
			List<Object[]> results = JPA.em().createNativeQuery(rule).getResultList();

			String ruleMessage = "Rule: "  + annotationRule.rule + " - Number of assays identified: " + results.size();
			Logger.info(ruleMessage);
			reportContent += ruleMessage + "\n";

			int counterFlush = 0;

			for (Object[] object : results) {

				String description = (String) object[1];
				int assayId = (int) object[0];
				String chemblId = (String) object[2];

				AnnotatedAssay assay = AnnotatedAssay.createOrRetrieve(assayId, chemblId, description);
				assay.annotate(annotationRule, false);
				
				//Automatically marked as curated
				assay.setReviewer(robot);

				counterFlush++;
				if (counterFlush%100 == 0) {
					AnnotatedAssay.em().flush();
					AnnotatedAssay.em().clear();
				}
			}
		}

		FileUtils.writeStringToFile(report, reportContent);
		stopwatch.stop();
		Logger.info("Annotation job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
	}

}
