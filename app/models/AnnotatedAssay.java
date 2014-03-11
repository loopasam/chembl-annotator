package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

	@ManyToOne
	public Reviewer reviewer;

	@ManyToMany(cascade=CascadeType.ALL)
	public List<BaoTerm> annotations;

	public int assayId;

	public String chemblId;

	@Lob
	public String description;

	public boolean starred;

	public boolean needReview;

	public AnnotatedAssay(int assayId, String chemblId, String description, BaoTerm baoTerm) {
		this.assayId = assayId;
		this.description = description;
		this.chemblId = chemblId;
		this.needReview = false;
		this.starred = false;
		this.annotations = new ArrayList<>();
		this.annotations.add(baoTerm);
	}

	public AnnotatedAssay setReviewer(Reviewer reviewer) {
		this.reviewer = reviewer;
		this.save();
		return this;
	}

	public static void annotate(int assayId, String chemblId, String description, BaoTerm baoTerm) {
		AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();
		if(assay == null){
			assay = new AnnotatedAssay(assayId, chemblId, description, baoTerm).save();
		}else{
			if(!assay.annotations.contains(baoTerm)){
				assay.annotations.add(baoTerm);
				assay.save();
			}
		}

	}

}
