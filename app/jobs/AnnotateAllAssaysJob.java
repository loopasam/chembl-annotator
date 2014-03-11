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

	//TODO clean up here
	public void doJob() throws SQLException{

		List<BaoTerm> terms = BaoTerm.findAll();
		int counter = 0;
		int total = terms.size();

		for (BaoTerm baoTerm : terms) {
			counter++;
			Logger.info("Term: " + baoTerm.label + "(" + baoTerm.baoId + ") - " + counter + "/" + total);
			for (AnnotationRule annotationRule : baoTerm.rules) {
				List<Object[]> results = 
						JPA.em().createNativeQuery("SELECT assay_id, description, chembl_id FROM assays WHERE " 
								+ annotationRule.rule +	";").getResultList();

				Logger.info("Rule: "  + annotationRule.rule + " - Number of assays identified: " + results.size());

				int counterFlush = 0;

				for (Object[] object : results) {
					
					//Wrap in the annotated assay object

					String description = (String) object[1];
					int assayId = (int) object[0];
					String chemblId = (String) object[2];

					//Checks if the assay exists already
					AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();

					if(assay == null){
						//If not then create a new one
						assay = new AnnotatedAssay(assayId, chemblId, description);
						assay.annotations.add(baoTerm);
						assay.save();
					}else{
						//If the assay exists already, then update it with a new term only, if not contained already
						if(!assay.annotations.contains(baoTerm)){
							assay.annotations.add(baoTerm);
							assay.save();
						}
					}

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
