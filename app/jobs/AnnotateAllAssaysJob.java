package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;

import play.Logger;
import play.jobs.Job;

public class AnnotateAllAssaysJob extends Job {

	public void doJob() throws SQLException{
		
		//Foreach term, get rule, fire rule and save the results
		
		List<BaoTerm> terms = BaoTerm.findAll();
		int counter = 0;
		int total = terms.size();
		
		for (BaoTerm baoTerm : terms) {
			counter++;
			Logger.info("Term: " + baoTerm.label + "(" + baoTerm.baoId + ") - " + counter + "/" + total);
			for (AnnotationRule annotationRule : baoTerm.rules) {
				executeRuleAndSaveAssays(annotationRule.rule);
			}
		}
		
		//TODO do not forget to fetch the reminder of the assays
		//Basically add copy all the rows from chembl assays
	}

	private void executeRuleAndSaveAssays(String rule) {
	    Connection c = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        String url = "jdbc:postgresql:chembl_17";
	        Class.forName("org.postgresql.Driver").newInstance();
	        
	        //TODO Get either from local or internal, from config file
	        c = DriverManager.getConnection(url, "postgres", "pouet");
	        
	        String fullRule = "SELECT assay_id, description, chembl_id FROM assays WHERE " + rule + " LIMIT 10";
	        pstmt = c.prepareStatement(fullRule);
	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	int assayId = rs.getInt("assay_id");
	            System.out.println(assayId);
	            String chemblId = rs.getString("chembl_id");
	            System.out.println(chemblId);
	            String description = rs.getString("description");
	            System.out.println(description);

	            //save the object
	            AnnotatedAssay.annotate(assayId, chemblId, description);
	            
	        }

	    }catch(Exception e){
	        e.printStackTrace();
	    } finally {
	    	try { if (rs != null) rs.close(); } catch (Exception e) {};
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
	        try { if (c != null) c.close(); } catch (Exception e) {};
	    }
	}
}
