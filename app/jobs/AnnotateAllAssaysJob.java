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

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class AnnotateAllAssaysJob extends Job {

	public void doJob() throws SQLException, IOException{
		
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		stopwatch.start();

		List<BaoTerm> terms = BaoTerm.findAll();
		
		//TODO comment for full scale
		terms = BaoTerm.find("byBaoId", "BAO_0000015").fetch();
		
		//Report init
		File report = new File("data/annotation-report.txt");
		String reportContent = "";
		
		int counter = 0;
		int total = terms.size();

		for (BaoTerm baoTerm : terms) {
			counter++;
			
			String termMessage = "Term: " + baoTerm.label + "(" + baoTerm.baoId + ") - " + counter + "/" + total;
			reportContent += termMessage + "\n";
			Logger.info(termMessage);
			
			for (AnnotationRule annotationRule : baoTerm.rules) {
								
				String rule;
				
				if(annotationRule.rule.startsWith("SELECT")){
					rule = annotationRule.rule;
				}else{
					rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays WHERE " 
							+ annotationRule.rule +	";";
				}

				List<Object[]> results = 
						JPA.em().createNativeQuery(rule).getResultList();

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
		}
		FileUtils.writeStringToFile(report, reportContent);
		stopwatch.stop();
		Logger.info("Annotation job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
	}

}
