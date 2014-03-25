package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import play.db.jpa.Model;

@Entity
public class AnnotationRule extends Model {

	@Type(type = "org.hibernate.type.TextType")
	public String rule;

	public String comment;

	public int confidence;

	public boolean highlight;

	@ManyToOne
	public BaoTerm baoTerm;

	public AnnotationRule(BaoTerm baoTerm, String rule, String comment, int confidence, boolean highlight) {
		this.rule = rule;
		this.comment = comment;
		this.baoTerm = baoTerm;
		this.confidence = confidence;
		this.highlight = highlight;
	}

	public String toString() {
		return rule;
	}
}
