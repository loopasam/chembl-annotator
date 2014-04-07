package controllers;

import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.math.BigInteger;
import java.util.*;

import jobs.RuleAnnotationJob;
import jobs.LoadBaoJob;

import models.*;

@With(Secure.class)
public class Application extends Controller {

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

	public static void assay(String chemblid) {
		AnnotatedAssay assay = AnnotatedAssay.find("byChemblId", chemblid).first();
		render(assay);
	}

	public static void random(){
		flash.keep();
		validation.keep();
		AnnotatedAssay assay = AnnotatedAssay.find("needReview is true order by rand() limit 1").first();
		if(assay == null){
			stats();
		}
		assay(assay.chemblId);
	}
	
	public static void pass() {
		flash.error("-2 points for passing.");
		validation.addError("curation", "-2pts for passing");
		validation.keep();
		flash.keep();
		random();
	}
	
	public static void starred(){
		List<Object> assays = AnnotatedAssay.find("starred", true).fetch();
		render(assays);
	}

	public static void stats(){
		int chemblassays = Integer.parseInt(JPA.em().createNativeQuery("SELECT COUNT(*) FROM assays").getResultList().get(0).toString());
		renderArgs.put("chemblassays", chemblassays);

		int annotatedchemblassays = AnnotatedAssay.findAll().size();
		renderArgs.put("annotatedchemblassays", annotatedchemblassays);

		double percentannotated = annotatedchemblassays / (double) chemblassays * 100.0;
		renderArgs.put("percentannotated", percentannotated);

		long curatedassays = AnnotatedAssay.count("reviewer is not null");
		renderArgs.put("curatedassays", curatedassays);

		double percentcurated = curatedassays / (double) annotatedchemblassays * 100.0;
		renderArgs.put("percentcurated", percentcurated);

		List<Map> contributors = AnnotatedAssay.find(
				"select new map(r.email as user, count(a.id) as na) from AnnotatedAssay a join a.reviewer as r group by r.email order by na desc"
				).fetch();

		renderArgs.put("contributors", contributors);

		render();
	}

	public static void switchTheme(String url){
		Reviewer reviewer = Reviewer.find("byEmail", Security.connected()).first();
		if(reviewer.coolTheme){
			reviewer.coolTheme = false;
		}else{
			reviewer.coolTheme = true;	
		}
		reviewer.save();
		redirect(url);
	}

	public static void star(Long id){
		AnnotatedAssay assay = AnnotatedAssay.findById(id);
		assay.star();
		assay(assay.chemblId);
	}

	public static void removeAnnotation(Long assayId, Long annotationId){
		AnnotatedAssay assay = AnnotatedAssay.findById(assayId);
		assay.removeAnnotation(annotationId);
		flash.success("Annotation successfully removed.");
		flash.keep();
		assay(assay.chemblId);
	}

	public static void validate(Long assayId){
		AnnotatedAssay assay = AnnotatedAssay.findById(assayId);
		if(assay.annotations.size() > 1){
			//Doesn't pass the validation - send back
			validation.addError("curation", "Please remove some BAO terms before validating. The assay can be have either one or no annotation.");
			validation.keep();
			assay(assay.chemblId);
		}
		flash.success("Assay successfully validated!");
		flash.keep();
		Reviewer reviewer = Reviewer.find("byEmail", Security.connected()).first();
		assay.markAsCurated(reviewer);
		random();
	}

}