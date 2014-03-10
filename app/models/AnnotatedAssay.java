package models;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

	@ManyToOne
	public User reviewer;
		
	public int assayId;
	
	public String chemblId;
	
	@Lob
	public String description;
	
	public boolean starred;
	
	public boolean needReview;
	
	public AnnotatedAssay(int assayId, String chemblId, String description) {
		this.assayId = assayId;
		this.description = description;
		this.chemblId = chemblId;
		this.needReview = false;
		this.starred = false;
	}

	public AnnotatedAssay setReviewer(User reviewer) {
		this.reviewer = reviewer;
		this.save();
		return this;
	}

	public static void annotate(int assayId, String chemblId, String description) {
		//TODO here
		//retrieves the assay by assayIds
		//if exists, then a new BAO term is added to the list
		//if doesn't exit, create a new assay with the BAO term
		//new AnnotatedAssay(assayId, chemblId, description)
	}

}
