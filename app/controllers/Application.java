package controllers;

import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.math.BigInteger;
import java.util.*;

import jobs.FakeAnnotationsJob;
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

	public static void next(){
		flash.keep();
		validation.keep();

		//Get the next assay related to a user
		//Get a random one assigned to the user instead?
		AnnotatedAssay assay = AnnotatedAssay.find("needReview is true and reviewer.email = ?", Security.connected()).first();
		if(assay == null){
			ladder();
		}
		assay(assay.chemblId);
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

		long curatedassays = AnnotatedAssay.count("needReview is false");
		renderArgs.put("curatedassays", curatedassays);

		double percentcurated = curatedassays / (double) annotatedchemblassays * 100.0;
		renderArgs.put("percentcurated", percentcurated);

		int totalInitFake = FakeAnnotationsJob.numberOfFakeAnnotations;
		renderArgs.put("totalInitFake", totalInitFake);

		long fakeNeedReview = Annotation.count("assay.needReview is true and isFake is true");
		renderArgs.put("fakeNeedReview", fakeNeedReview);

		long fakeValidated = Annotation.count("assay.needReview is false and isFake is true");
		renderArgs.put("fakeValidated", fakeValidated);

		double curationQuality = 100.0 * (1 - ((double) fakeValidated / totalInitFake));
		renderArgs.put("curationQuality", curationQuality);

		double qualityConfidence = 100.0 * (1 - ((double) fakeNeedReview / totalInitFake));
		renderArgs.put("qualityConfidence", qualityConfidence);

		render();
	}

	public static void ladder(){
		List<Map> ladder = AnnotatedAssay.find(
				"select new map(r.email as user, r.score as score) " +
						"from Reviewer r where r.email != 'super.cool.bot@gmail.com' " +
						"order by score desc"
				).fetch();

		int annotatedchemblassays = AnnotatedAssay.findAll().size();
		long curatedassays = AnnotatedAssay.count("needReview is false");
		double percentcurated = curatedassays / (double) annotatedchemblassays * 100.0;

		render(ladder, percentcurated);
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
		String message = assay.removeAnnotation(annotationId);
		flash.success(message);
		flash.keep();
		assay(assay.chemblId);
	}

	public static void validate(Long assayId){

		AnnotatedAssay assay = AnnotatedAssay.findById(assayId);

		int numberofFake = assay.getNumberOfFakeAnnotations();

		assay.markAsCurated(numberofFake);

		if(numberofFake > 0 && assay.reviewer.isPlayer){
			validation.addError("curation", GameConstants.MISS_FAKE_ANNOTATION_MESSAGE);
			validation.keep();
		}else{
			flash.success(GameConstants.ASSAY_VALIDATED_MESSAGE);
			flash.keep();
		}

		next();
	}

}