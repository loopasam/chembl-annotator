package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

	@ManyToOne
	public Reviewer reviewer;

	@OneToMany(mappedBy="assay", cascade=CascadeType.ALL)
	public List<Annotation> annotations;

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

	public static AnnotatedAssay createOrRetrieve(int assayId, String chemblId, String description) {
		AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();
		if(assay == null){
			return new AnnotatedAssay(assayId, chemblId, description).save();
		}
		return assay;
	}

	public void annotate(AnnotationRule annotationRule) {
		//Try to retrieve a potentially existing annotation, for the term to this assay.
		Annotation annotation = Annotation.find("byTermAndAssay", annotationRule.baoTerm, this).first();

		if(annotation == null){
			//If no annotation with the term, then create a new one and save it.
			Annotation newAnnotation = new Annotation(annotationRule.baoTerm, this, annotationRule.confidence).save();
			this.annotations.add(newAnnotation);
		}else{
			//If the annotation exists already, then increase the confidence.
			annotation.confidence += annotationRule.confidence;
			annotation.save();
		}
		this.save();
	}

	public void removeAnnotation(Long id) {
		Annotation annotation = Annotation.find("byId", id).first();
		this.annotations.remove(annotation);
		annotation.delete();
		this.save();
	}
}
