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
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

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

    @Type(type = "org.hibernate.type.TextType")
    public String definition;

    @OneToMany(mappedBy = "baoTerm", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<AnnotationRule> rules;

    @ManyToMany(cascade = CascadeType.ALL)
    public List<BaoTerm> children;

    public BaoTerm(String url, String label, String definition) {
        this.baoUrl = url;
        this.definition = definition;
        this.label = label;
        this.baoId = url.substring(36, 47);
        this.rules = new ArrayList<AnnotationRule>();
        this.children = new ArrayList<BaoTerm>();
    }

    public AnnotationRule addAnnotationRule(String rule, String description, int confidence, boolean highlight, boolean isFilter) {
        AnnotationRule annotationRule = new AnnotationRule(this, rule, description, confidence, highlight, isFilter).save();
        this.rules.add(annotationRule);
        this.save();
        return annotationRule;
    }

    public BaoTerm addChild(String url, String label, String definition) {
        BaoTerm term = BaoTerm.createOrRetrieveTerm(url, label, definition);
        this.children.add(term);
        this.save();
        return this;
    }

    public static BaoTerm createOrRetrieveTerm(String url, String label, String definition) {
        BaoTerm term = BaoTerm.find("baoUrl", url).first();
        if (term == null) {
            return new BaoTerm(url, label, definition).save();
        }
        return term;
    }

    public String toString() {
        return label;
    }

}
