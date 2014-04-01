package controllers;

import play.*;
import play.db.jpa.JPA;
import play.mvc.*;

import java.math.BigInteger;
import java.util.*;

import jobs.AnnotateAllAssaysJob;
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

	public static void randomAssay(){
		AnnotatedAssay assay = AnnotatedAssay.find("order by rand()").first();
		render(assay);
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

	public static void curationRandom() {
		AnnotatedAssay assay = AnnotatedAssay.find("needReview", true).first();
		render(assay);
	}

	public static void curationAssay(String chemblid) {
		AnnotatedAssay assay = AnnotatedAssay.find("byChemblId", chemblid).first();
		render(assay);
	}

	public static void star(Long id){
		AnnotatedAssay assay = AnnotatedAssay.findById(id);
		assay.star();
		redirect("Application.curationAssay", assay.chemblId);
	}

}