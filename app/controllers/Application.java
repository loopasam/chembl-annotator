package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import jobs.AnnotateAllAssaysJob;
import jobs.LoadBaoJob;

import models.*;

public class Application extends Controller {

	public static void index() {
		render();
	}

	public static void jobs() {
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
		jobs();
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
		jobs();
	}
	
	public static void assay(String chemblid) {
		AnnotatedAssay assay = AnnotatedAssay.find("byChemblId", chemblid).first();		
		render(assay);
	}


}