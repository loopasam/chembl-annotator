/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.util.List;
import models.AnnotatedAssay;
import models.Annotation;
import models.AnnotationRule;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

/**
 *
 * @author loopasam
 */
public class FilterAssayJob extends Job {

    public void doJob() throws Exception {
        Logger.info("Filtering job...");

        List<AnnotationRule> rules = AnnotationRule.find("select r from AnnotationRule r "
                + "where r.isFilter = true").fetch();

        int counter = 0;
        int total = rules.size();
        for (AnnotationRule annotationRule : rules) {
            counter++;

            String termMessage = "Rule: " + counter + "/" + total;
            Logger.info(termMessage);

            String rule = "SELECT DISTINCT assay_id, description FROM assays "
                    + "WHERE " + annotationRule.rule + ";";

            List<Object[]> results = JPA.em().createNativeQuery(rule).getResultList();

            String ruleMessage = "Rule: " + annotationRule.rule + " - Number of assays identified: " + results.size();
            Logger.info(ruleMessage);

            int counterFlush = 0;

            for (Object[] result : results) {

                int assayId = (int) result[0];
                AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();

                //Check if the assay exists already, otherwise does nothing
                if (assay != null) {
                    Logger.info("removed: " + assay.chemblId);
                    assay.needReview = false;

                    for (Annotation annotation : assay.annotations) {
                        annotation.toRemove = true;
                        annotation.save();
                    }

                    assay.save();

                }

                counterFlush++;
                if (counterFlush % 100 == 0) {
                    AnnotatedAssay.em().flush();
                    AnnotatedAssay.em().clear();
                }
            }
        }
        
        Logger.info("Filtering done.");

    }

}
