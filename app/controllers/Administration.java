package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jobs.AnnotateAllAssaysJob;
import jobs.LoadBaoJob;
import jobs.LoadRulesJob;
import models.AnnotatedAssay;
import models.Annotation;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;
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
		if(Security.isConnected()) {
			Reviewer reviewer = Reviewer.find("byEmail", Security.connected()).first();
			renderArgs.put("reviewer", reviewer);
		}
	}

	public static void index() {
		render();
	}

	public static void baoJob() {
		if(BaoTerm.findAll().size() <= 0){
			new LoadBaoJob().now();
		}else{
			Logger.info("Job not started, as there are some already " +
					"existing BAO terms in the database. Delete " +
					"first these terms from the table in order to " +
					"be able to start the job.");
		}	
		index();
	}

	public static void annotationJob() {
		if(AnnotatedAssay.findAll().size() <= 0){
			new AnnotateAllAssaysJob().now();
		}else{
			Logger.info("Job not started, as there are some already " +
					"existing annotated assays in the database. Delete " +
					"first these terms from the table in order to " +
					"be able to start the job.");
		}
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
		if(AnnotationRule.findAll().size() <= 0){
			new LoadRulesJob().now();
		}else{
			Logger.info("Job not started, as there are some already " +
					"existing rules in the database. Delete " +
					"first these rules from the table in order to " +
					"be able to start the job.");
		}
		index();
	}

	public static void exportRules() throws IOException{
		File file = new File("data/rules.txt");
		List<AnnotationRule> rules = AnnotationRule.findAll();
		StringBuilder sb = new StringBuilder();
		for (AnnotationRule annotationRule : rules) {
			sb.append(annotationRule.rule + "|" + annotationRule.baoTerm.baoId + "|" + annotationRule.comment + 
					"|" + annotationRule.confidence + "|" + annotationRule.highlight + "\n");
		}
		FileUtils.writeStringToFile(file, sb.toString());
		index();
	}

	public static void generateDummyAnnotation() {			
			Fixtures.delete(Annotation.class);
			Fixtures.delete(AnnotatedAssay.class);
			
			//create fake annotations
			AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(791527, "CHEMBL1930580", "Fungistatic activity against Saccharomyces cerevisiae ATCC 24657 assessed as cell growth at 100 uM after 48 hrs by spectrophotometric bioassay");			
			AnnotatedAssay assay2 = AnnotatedAssay.createOrRetrieve(673225, "CHEMBL1267020", "Half life of free unbound fraction in serum of methicillin-resistant Staphylococcus aureus infected ddY mouse at 100 mg/kg, ip administered 1 day post infection by paper disk bioassay method");
			AnnotatedAssay assay3 = AnnotatedAssay.createOrRetrieve(11912, "CHEMBL628657", "Ratio of biodistributions in Athymic mice bearing human Tumor (TE671) xenografts in Tumor (T) and blood (B)");
						
			assay1.needReview = true;
			assay1.save();
			assay2.needReview = true;
			assay2.save();
			assay3.needReview = true;
			assay3.save();
			
			AnnotationRule rule1 = AnnotationRule.find("byConfidence", 1).first();
			AnnotationRule rule2 = AnnotationRule.find("byConfidence", 5).first();
			
			assay1.annotate(rule1);
			assay2.annotate(rule1);
			assay2.annotate(rule2);
			assay3.annotate(rule2);
			Logger.info("Dummy rules added.");
		index();
	}

}
