package controllers;

import java.util.List;

import jobs.AnnotateAllAssaysJob;
import jobs.LoadBaoJob;
import jobs.LoadRulesJob;
import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

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


}
