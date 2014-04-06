package jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Stopwatch;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class AnnotateTextMining extends Job {

	public void doJob() throws IOException{

		Stopwatch stopwatch = Stopwatch.createUnstarted();
		stopwatch.start();

		List<AnnotationRule> rules = AnnotationRule.find("select r from AnnotationRule r " +
				"where r.highlight = true").fetch();

		//Report init
		File report = new File("data/annotation-report-high-priority.txt");
		String reportContent = "";

		int counter = 0;
		int total = rules.size();

		for (AnnotationRule annotationRule : rules) {
			counter++;

			String termMessage = "Term: " + annotationRule.baoTerm.label + "(" + annotationRule.baoTerm.baoId + ") - " + counter + "/" + total;
			reportContent += termMessage + "\n";
			Logger.info(termMessage);

			String rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays " +
					"WHERE " + annotationRule.rule + " " +
					"AND NOT EXISTS (	" +
					"SELECT AnnotatedAssay.assayid " +
					"FROM AnnotatedAssay " +
					"WHERE AnnotatedAssay.assayid = assays.assay_id " +
					"AND AnnotatedAssay.reviewer_id IS NOT NULL" +
					");";

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
				assay.annotate(annotationRule);

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
