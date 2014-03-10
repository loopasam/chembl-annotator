package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class AnnotationRule extends Model {

	public String rule;

	public String comment;

	@ManyToOne
	public BaoTerm baoTerm;

	public AnnotationRule(BaoTerm baoTerm, String rule, String comment) {
		this.rule = rule;
		this.comment = comment;
		this.baoTerm = baoTerm;
	}

	public String toString() {
	    return rule;
	}
}
