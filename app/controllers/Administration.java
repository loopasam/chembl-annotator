package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jobs.FakeAnnotationsJob;
import jobs.RemoveFakeAnnotationJob;
import jobs.RuleAnnotationJob;
import jobs.SemanticSimplificationJob;
import jobs.TextMatchingAnnotationJob;
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

	//TODO change name of job launchers
	public static void priorityAnnotationJob() {
		new RuleAnnotationJob().now();
		index();
	}

	public static void textminingAnnotationJob(){
		new TextMatchingAnnotationJob().now();
		index();
	}

	public static void removeFakeAnnotations(){
		new RemoveFakeAnnotationJob().now();
		index();
	}

	public static void estimateCountAnnotations(){
		new AnnotationEstimationCount().now();
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
		if(file.exists()){
			Logger.error("Job not started, as there's already a file containing rules (data/rules.txt)." +
					" Please delete the file first and re-launch the job.");
		}else{
			List<AnnotationRule> rules = AnnotationRule.findAll();
			StringBuilder sb = new StringBuilder();
			for (AnnotationRule annotationRule : rules) {
				sb.append(annotationRule.rule + "|" + annotationRule.baoTerm.baoId + "|" + annotationRule.comment + 
						"|" + annotationRule.confidence + "|" + annotationRule.highlight + "\n");
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

	public static void generateFakeAnnotations() {
		new FakeAnnotationsJob().now();
		index();
	}

	public static void generateDummyAnnotation() {			
		Fixtures.delete(Annotation.class);
		Fixtures.delete(AnnotatedAssay.class);

		//create fake annotations
		AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(791527, "CHEMBL1930580", "Fungistatic activity against Saccharomyces cerevisiae ATCC 24657 assessed as cell growth at 100 uM after 48 hrs by spectrophotometric bioassay");			
		AnnotatedAssay assay2 = AnnotatedAssay.createOrRetrieve(673225, "CHEMBL1267020", "Half life of free unbound fraction in serum of methicillin-resistant Staphylococcus aureus infected ddY mouse at 100 mg/kg, ip administered 1 day post infection by paper disk bioassay method");
		AnnotatedAssay assay3 = AnnotatedAssay.createOrRetrieve(11912, "CHEMBL628657", "Ratio of biodistributions in Athymic mice bearing human Tumor (TE671) xenografts in Tumor (T) and blood (B)");

		AnnotationRule rule1 = AnnotationRule.find("byConfidence", 1).first();
		AnnotationRule rule2 = AnnotationRule.find("byConfidence", 5).first();

		assay1.annotate(rule1, true, Reviewer.randomReviewer());
		assay2.annotate(rule1, true, Reviewer.randomReviewer());
		assay2.annotate(rule2, true, Reviewer.randomReviewer());
		assay3.annotate(rule2, true, Reviewer.randomReviewer());
		Logger.info("Dummy rules added.");
		index();
	}

}
