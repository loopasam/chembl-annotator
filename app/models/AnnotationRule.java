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

	//Flag to identifying tect-mining rules to be highlighted in the description
    //later on
    public boolean highlight;

    //Flag to indentify the rules used in the filtering phase
    public boolean isFilter;

    @ManyToOne
    public BaoTerm baoTerm;

    public AnnotationRule(BaoTerm baoTerm, String rule, String comment, int confidence, boolean highlight, boolean isFilter) {
        this.rule = rule;
        this.comment = comment;
        this.baoTerm = baoTerm;
        this.confidence = confidence;
        this.highlight = highlight;
        this.isFilter = isFilter;
    }

    @Override
    public String toString() {
        return rule;
    }

    public String getSQLRule() {

        if (this.rule.startsWith("SELECT")) {
            return this.rule + ";";
        } else {
            return "SELECT DISTINCT assay_id, description, chembl_id FROM assays "
                    + "WHERE " + this.rule + ";";
        }
    }

}
