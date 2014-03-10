package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.JPABase;
import play.db.jpa.Model;

//Example: http://data.bioontology.org/ontologies/BAO/classes/http%3A%2F%2Fwww.bioassayontology.org%2Fbao%23BAO_0000015/children/?apikey=1ed7a808-79d2-4888-a11b-5add5b10bdc2
//Class properties: http://data.bioontology.org/ontologies/BAO/classes/http%3A%2F%2Fwww.bioassayontology.org%2Fbao%23BAO_0000015/?apikey=1ed7a808-79d2-4888-a11b-5add5b10bdc2
//Check also for synonyms and obsolete

@Entity
public class BaoTerm extends Model {

	public String baoUrl;
	public String baoId;
	public String label;

	@Lob
	public String definition;

	@OneToMany(mappedBy="baoTerm", cascade=CascadeType.ALL)
	public List<AnnotationRule> rules;

	@ManyToMany(cascade=CascadeType.ALL)
	public List<BaoTerm> children;

	public BaoTerm(String url, String label, String definition) {
		this.baoUrl = url;
		this.definition = definition;
		this.label = label;
		this.baoId = url.substring(36, 47);
		this.rules = new ArrayList<>();
		this.children = new ArrayList<>();
	}

	public BaoTerm addAnnotationRule(String rule, String description) {
		AnnotationRule annotationRule = new AnnotationRule(this, rule, description).save();
		this.rules.add(annotationRule);
		this.save();
		return this;
	}

	public BaoTerm addChild(String url, String label, String definition) {
		BaoTerm term = BaoTerm.createOrRetrieveTerm(url, label, definition);
		this.children.add(term);
		this.save();
		return this;
	}

	public static BaoTerm createOrRetrieveTerm(String url, String label, String definition) {
		BaoTerm term = BaoTerm.find("byBaoUrl", url).first();
		if(term == null){
			return new BaoTerm(url, label, definition).save();
		}
		return term;
	}


}
