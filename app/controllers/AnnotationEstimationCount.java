package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.AnnotatedAssay;
import models.AnnotationRule;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class AnnotationEstimationCount extends Job {

	int counter;
	int totalTextMining;
	int totalPriority;
	int totalGeneric;
	String reportContent = "";

	public void doJob() throws IOException{
		Logger.info("Count numbers of assays matching the rules...");

		//		List<AnnotationRule> textMiningRules = AnnotationRule.find("select r from AnnotationRule r " +
		//				"where r.highlight = true").fetch();
		//		counter = 0;
		//		totalTextMining = textMiningRules.size();
		File report = new File("data/estimate-report-text-mining.txt");
		//
		//		for (AnnotationRule annotationRule : textMiningRules) {
		//
		//			String rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays " +
		//					"WHERE " + annotationRule.rule + " " +
		//					"AND NOT EXISTS (	" +
		//					"SELECT AnnotatedAssay.assayid " +
		//					"FROM AnnotatedAssay " +
		//					"WHERE AnnotatedAssay.assayid = assays.assay_id " +
		//					"AND AnnotatedAssay.reviewer_id IS NOT NULL" +
		//					");";
		//
		//			estimate(rule, totalTextMining, annotationRule);
		//		}
		//
		//		FileUtils.writeStringToFile(report, reportContent);
		//
		//		List<AnnotationRule> priorityRules = AnnotationRule.find("select r from AnnotationRule r " +
		//				"where r.hasPriority = true order by r.confidence desc").fetch();
		//		counter = 0;
		//		reportContent = "";
		//		totalPriority = priorityRules.size();
		//		report = new File("data/estimate-report-priority.txt");
		//
		//		for (AnnotationRule annotationRule : priorityRules) {
		//
		//				String rule = annotationRule.rule + " AND NOT EXISTS (	" +
		//						"SELECT AnnotatedAssay.assayid " +
		//						"FROM AnnotatedAssay " +
		//						"WHERE AnnotatedAssay.assayid = assays.assay_id" +
		//						");";
		//
		//				estimate(rule, totalPriority, annotationRule);			
		//		}
		//		FileUtils.writeStringToFile(report, reportContent);

		List<AnnotationRule> genericRules = AnnotationRule.find("select r from AnnotationRule r " +
				"where r.hasPriority = false and r.highlight = false").fetch();
		counter = 0;
		reportContent = "";
		totalGeneric = genericRules.size();
		report = new File("data/estimate-report-generic.txt");

		for (AnnotationRule annotationRule : genericRules) {

			String rule = "";

			if(annotationRule.rule.startsWith("SELECT")){
				rule = annotationRule.rule;
			}else{
				rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays WHERE " 
						+ annotationRule.rule +	";";
			}

			estimate(rule, totalGeneric, annotationRule);			
		}
		FileUtils.writeStringToFile(report, reportContent);

		Logger.info("Estimation done.");
	}

	private void estimate(String rule, int total, AnnotationRule annotationRule) {
		counter++;
		String termMessage = "Term: " + annotationRule.baoTerm.label + " (" + annotationRule.baoTerm.baoId + ") - " + counter + "/" + total;
		Logger.info(termMessage);
		List<Object[]> results = JPA.em().createNativeQuery(rule).getResultList();

		String ruleMessage = "Rule: "  + annotationRule.rule + " - Number of assays identified: " + results.size();
		reportContent += annotationRule.baoTerm.label + "|" + results.size() + "\n";
		Logger.info(ruleMessage);

	}
}
