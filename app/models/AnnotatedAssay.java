package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

	@ManyToOne
	public Reviewer reviewer;

	//TODO that was the problem all along
	@ManyToMany(cascade=CascadeType.MERGE)
	public List<BaoTerm> annotations;

	public int assayId;

	public String chemblId;

	@Type(type = "org.hibernate.type.TextType")
	public String description;

	public boolean starred;

	public boolean needReview;

	public AnnotatedAssay(int assayId, String chemblId, String description) {
		this.assayId = assayId;
		this.description = description;
		this.chemblId = chemblId;
		this.needReview = false;
		this.starred = false;
		this.annotations = new ArrayList<>();
	}

	public AnnotatedAssay setReviewer(Reviewer reviewer) {
		this.reviewer = reviewer;
		this.save();
		return this;
	}
	
	public String toString() {
		return chemblId;
	}

	public static void annotate(int assayId, String chemblId, String description, BaoTerm baoTerm) {
		//Checks if the assay exists already
		AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();

		if(assay == null){
			//If not then create a new one
			assay = new AnnotatedAssay(assayId, chemblId, description);
			assay.annotations.add(baoTerm);
			assay.save();
		}else{
			//If the assay exists already, then update it with a new term only, if not contained already
			if(!assay.annotations.contains(baoTerm)){
				assay.annotations.add(baoTerm);
				assay.save();
			}
		}
	}

}
