package controllers;

import play.*;
import play.mvc.*;

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
		AnnotatedAssay assay = AnnotatedAssay.find("order by random()").first();
		render(assay);
	}
	
	public static void starred(){
		List<Object> assays = AnnotatedAssay.find("starred", true).fetch();
		render(assays);
	}

	public static void stats(){
		render();
	}

}