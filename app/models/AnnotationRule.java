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
	
	public boolean hasPriority;

	//TODO replace name by something more meaningful
	public boolean highlight;

	@ManyToOne
	public BaoTerm baoTerm;

	public AnnotationRule(BaoTerm baoTerm, String rule, String comment, int confidence, boolean highlight) {
		this.rule = rule;
		this.comment = comment;
		this.baoTerm = baoTerm;
		this.confidence = confidence;
		this.highlight = highlight;
		this.hasPriority = false;
	}

	public String toString() {
		return rule;
	}

	//TODO to remove
	public String getSqlQuery() {
		if(this.rule.startsWith("SELECT")){
			return this.rule;
		}else{
			return "SELECT DISTINCT assay_id, description, chembl_id FROM assays WHERE " 
					+ this.rule +	";";
		}
	}

}