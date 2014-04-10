package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Annotation extends Model implements Comparable<Annotation> {

	@ManyToOne
	public BaoTerm term;

	@ManyToOne
	public AnnotatedAssay assay;

	public int confidence;
	
	public boolean isFake;

	public Annotation(BaoTerm term, AnnotatedAssay assay, int confidence, boolean isFake) {
		this.term = term;
		this.assay = assay;
		this.confidence = confidence;
		this.isFake = isFake;
	}

	@Override
	public int compareTo(Annotation otherAnnotation) {
		return term.label.compareTo(otherAnnotation.term.label);
	}

}
