package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jobs.ExportTimesJob;
import jobs.FakeAnnotationsJob;
import jobs.FilterAssayJob;
import jobs.LoadBaoJob;
import jobs.LoadRulesJob;
import jobs.RemoveFakeAnnotationJob;
import jobs.RuleAnnotationJob;
import jobs.RuleValidityJob;
import jobs.SemanticSimplificationJob;
import jobs.TextMatchingAnnotationJob;
import models.AnnotatedAssay;
import models.Annotation;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.test.Fixtures;

@Check("admin")
@With(Secure.class)
public class Administration extends Controller {

    @Before
    static void setConnectedUser() {
        if (Security.isConnected()) {
            Reviewer reviewer = Reviewer.find("byEmail", Security.connected()).first();
            renderArgs.put("reviewer", reviewer);
        }
    }

    public static void index() {
        render();
    }

    public static void baoJob() {
        if (BaoTerm.findAll().size() <= 0) {
            new LoadBaoJob().now();
        } else {
            Logger.info("Job not started, as there are some already "
                    + "existing BAO terms in the database. Delete "
                    + "first these terms from the table in order to "
                    + "be able to start the job.");
        }
        index();
    }

    public static void ruleAnnotationJob() {
        new RuleAnnotationJob().now();
        index();
    }

    public static void textminingAnnotationJob() {
        new TextMatchingAnnotationJob().now();
        index();
    }

    public static void removeFakeAnnotations() {
        new RemoveFakeAnnotationJob().now();
        index();
    }

    public static void estimateCountAnnotations() {
        new RuleValidityJob().now();
        index();
    }

    public static void testRules() {
        new RuleValidityJob().now();
        index();
    }

    public static void exportBao() {
        List<BaoTerm> terms = BaoTerm.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("BAO_ID,label\n");
        for (BaoTerm baoTerm : terms) {
            sb.append(baoTerm.baoId + "," + baoTerm.label + "\n");
        }

        String file = sb.toString();
        renderText(file);
    }

    public static void loadRules() {
        if (AnnotationRule.findAll().size() <= 0) {
            new LoadRulesJob().now();
        } else {
            Logger.info("Job not started, as there are some already "
                    + "existing rules in the database. Delete "
                    + "first these rules from the table in order to "
                    + "be able to start the job.");
        }
        index();
    }

    public static void exportRules() throws IOException {
        File file = new File("data/rules.txt");
        if (file.exists()) {
            Logger.error("Job not started, as there's already a file containing rules (data/rules.txt)."
                    + " Please delete the file first and re-launch the job.");
        } else {
            List<AnnotationRule> rules = AnnotationRule.findAll();
            StringBuilder sb = new StringBuilder();
            for (AnnotationRule annotationRule : rules) {

                String baoId;
                if (annotationRule.baoTerm == null) {
                    baoId = "filter";
                } else {
                    baoId = annotationRule.baoTerm.baoId;
                }

                sb.append(annotationRule.rule + "|" + baoId + "|" + annotationRule.comment
                        + "|" + annotationRule.confidence + "|" + annotationRule.highlight + "|" + annotationRule.isFilter + "\n");
            }

            FileUtils.writeStringToFile(file, sb.toString());
        }
        Logger.info("Rules exported.");
        index();
    }

    public static void semanticSimplification() {
        new SemanticSimplificationJob().now();
        index();
    }

    public static void exportTimes() {
        new ExportTimesJob().now();
        index();
    }

    public static void generateFakeAnnotations() {
        new FakeAnnotationsJob().now();
        index();
    }

    public static void filterAssays() {
        new FilterAssayJob().now();
        index();
    }

    public static void generateDummyAnnotation() {
        Fixtures.delete(Annotation.class);
        Fixtures.delete(AnnotatedAssay.class);

        //create fake annotations
        AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(791527, "CHEMBL1930580", "Fungistatic "
                + "activity against Saccharomyces cerevisiae ATCC 24657 assessed as cell growth at 100 "
                + "uM after 48 hrs by spectrophotometric bioassay");
        
        AnnotatedAssay assay2 = AnnotatedAssay.createOrRetrieve(673225, "CHEMBL751881", "Functional "
                + "antagonism at the NMDA receptor-ion channel "
                + "complex was demonstarted by the ability to "
                + "inhibit the binding of the "
                + "channel-blocking agent [3H](+)-MK-801");
        
        AnnotatedAssay assay3 = AnnotatedAssay.createOrRetrieve(774643, "CHEMBL628657", "DRUGMATRIX: Cysteinyl "
                + "leukotriene receptor 1 "
                + "radioligand binding (ligand: [3H]LTD4)");

        AnnotationRule rule1 = AnnotationRule.find("byConfidence", 1).first();
        AnnotationRule rule2 = AnnotationRule.find("byConfidence", 5).first();
        
        //Get rule for binding, protein-small and radioligand
        //Annotate one assay with the three and assign to me, the others
        //to random users.
        AnnotationRule radiobinding = AnnotationRule.find("byRule", "LOWER(description) LIKE '%radioligand binding assay%'").first();
        AnnotationRule proteinSmall = AnnotationRule.find("byRule", "LOWER(description) LIKE '%protein-small molecule interaction assay%'").first();
        AnnotationRule binding = AnnotationRule.find("byRule", "assay_type = 'B'").first();
        
        Reviewer sam  = Reviewer.find("byEmail", "samuel.croset@gmail.com").first();
        
        assay1.annotate(radiobinding, true, sam);
        assay1.annotate(proteinSmall, true, sam);
        assay1.annotate(binding, true, sam);
        
        assay2.annotate(radiobinding, true, sam);
        assay2.annotate(proteinSmall, true, sam);
        assay2.annotate(binding, true, sam);

        assay2.annotate(rule1, true, Reviewer.randomReviewer());
        assay2.annotate(rule2, true, Reviewer.randomReviewer());
        assay3.annotate(rule2, true, Reviewer.randomReviewer());
        Logger.info("Dummy rules added.");
        index();
    }

}
