package jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.AnnotatedAssay;
import models.AnnotationRule;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class RuleValidityJob extends Job {

	int counter;
	int totalTextMatching;
	int totalRules;

	public void doJob() throws IOException{
		Logger.info("Count numbers of assays matching the rules...");

		List<AnnotationRule> textMiningRules = AnnotationRule.find("select r from AnnotationRule r " +
				"where r.highlight = true").fetch();
		counter = 0;
		totalTextMatching = textMiningRules.size();

		for (AnnotationRule annotationRule : textMiningRules) {
			String rule = "SELECT DISTINCT assay_id, description, chembl_id FROM assays " +
					"WHERE " + annotationRule.rule + ";";
			estimate(rule, totalTextMatching, annotationRule);
		}


		List<AnnotationRule> rules = AnnotationRule.find("select r from AnnotationRule r " +
				"where r.highlight = false").fetch();
		counter = 0;
		totalRules = rules.size();
		for (AnnotationRule annotationRule : rules) {
			String rule = annotationRule.rule + ";";
			estimate(rule, totalRules, annotationRule);			
		}
		Logger.info("All rules checked done.");
	}

	private void estimate(String rule, int total, AnnotationRule annotationRule) {
		counter++;
		String termMessage = "Term: " + annotationRule.baoTerm.label + " (" + annotationRule.baoTerm.baoId + ") - " + counter + "/" + total;
		Logger.info(termMessage);
		List<Object[]> results = JPA.em().createNativeQuery(rule).getResultList();
		String ruleMessage = "Rule: "  + annotationRule.rule + " - Number of assays identified: " + results.size();
		Logger.info(ruleMessage);

	}
}
