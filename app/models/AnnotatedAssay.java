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

	//TODO put @Lob for production
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


}
