package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;


import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class AnnotateAllAssaysJob extends Job {

	public void doJob() throws SQLException{

		List<BaoTerm> terms = BaoTerm.findAll();
		
		//TODO comment for full scale
		terms = BaoTerm.find("byBaoId", "BAO_0000015").fetch();
		
		int counter = 0;
		int total = terms.size();

		for (BaoTerm baoTerm : terms) {
			counter++;
			Logger.info("Term: " + baoTerm.label + "(" + baoTerm.baoId + ") - " + counter + "/" + total);
			for (AnnotationRule annotationRule : baoTerm.rules) {
								
				//TODO check how the query is formed, depending on the highlight flag
				//or if there's a SELECT statement
				String rule;
				
				if(annotationRule.rule.startsWith("SELECT")){
					rule = annotationRule.rule;
				}else{
					rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays WHERE " 
							+ annotationRule.rule +	";";
				}
				System.out.println(rule);

				List<Object[]> results = 
						JPA.em().createNativeQuery(rule).getResultList();

				Logger.info("Rule: "  + annotationRule.rule + " - Number of assays identified: " + results.size());

				int counterFlush = 0;

				for (Object[] object : results) {
					
					String description = (String) object[1];
					int assayId = (int) object[0];
					String chemblId = (String) object[2];

					AnnotatedAssay.annotate(assayId, chemblId, description, baoTerm);
					
					counterFlush++;
					if (counterFlush%100 == 0) {
						AnnotatedAssay.em().flush();
						AnnotatedAssay.em().clear();
					}
				}
			}
		}
		
		Logger.info("Annotation job done.");
	}

}
