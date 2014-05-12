package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.hibernate.Session;
import org.hibernate.annotations.Type;
import org.hibernate.mapping.Array;

import controllers.Security;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

    @ManyToOne
    public Reviewer reviewer;

    @OneToMany(mappedBy = "assay", cascade = CascadeType.ALL)
    public Set<Annotation> annotations;

    public int assayId;

    public String chemblId;

    @Type(type = "org.hibernate.type.TextType")
    public String description;

    public boolean starred;

    public boolean needReview;

    public Date validationDate;

    public AnnotatedAssay(int assayId, String chemblId, String description) {
        this.assayId = assayId;
        this.description = description;
        this.chemblId = chemblId;
        this.needReview = false;
        this.starred = false;
        this.annotations = new TreeSet<Annotation>();
    }

    public AnnotatedAssay setReviewer(Reviewer reviewer) {
        this.reviewer = reviewer;
        this.save();
        return this;
    }

    @Override
    public String toString() {
        return chemblId;
    }

    public static AnnotatedAssay createOrRetrieve(int assayId, String chemblId, String description) {
        AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", assayId).first();
        if (assay == null) {
            return new AnnotatedAssay(assayId, chemblId, description).save();
        }
        return assay;
    }

    public void annotate(AnnotationRule annotationRule, boolean needReview, Reviewer reviewer) {
        //Try to retrieve a potentially existing annotation, for the term to this assay.
        Annotation annotation = Annotation.find("byTermAndAssay", annotationRule.baoTerm, this).first();

        if (annotation == null) {
            //If no annotation with the term, then create a new one and save it.
            Annotation newAnnotation = new Annotation(annotationRule.baoTerm, this, annotationRule.confidence, false).save();
            this.annotations.add(newAnnotation);
        } else {
            //If the annotation exists already, then increase the confidence.
            annotation.confidence += annotationRule.confidence;
            annotation.save();
        }
        this.needReview = needReview;
        this.reviewer = reviewer;
        this.save();
    }

    public String removeAnnotation(Long id) {
        Annotation annotation = Annotation.find("byId", id).first();
        int deltaScore = 0;
        String message = GameConstants.REMOVE_ANNOTATION_MESSAGE;

        if (annotation != null) {
            if (annotation.isFake) {
                deltaScore = GameConstants.REMOVE_FAKE_ANNOTATION_POINTS;
                if (this.reviewer.isPlayer) {
                    message = GameConstants.REMOVE_FAKE_ANNOTATION_MESSAGE;
                }
                this.reviewer.updateScore(deltaScore);
            }

            annotation.toRemove = true;
            annotation.save();
            this.save();
        }

        return message;
    }

    public void star() {
        if (this.starred) {
            this.starred = false;
        } else {
            this.starred = true;
        }
        this.save();
    }

    public void markAsCurated(int numberOfFake) {
        this.validationDate = new Date();

        int deltaScore = 0;

        if (numberOfFake > 0) {
            deltaScore = numberOfFake * GameConstants.MISS_FAKE_ANNOTATION_POINTS;
        } else {
            deltaScore = GameConstants.CORRECT_VALIDATION_POINTS;
        }

        this.reviewer.updateScore(deltaScore);

        this.needReview = false;
        this.save();
    }

    public String getRules() {
        String rules = "";
        boolean isFirst = true;
        for (Annotation annotation : this.annotations) {
            for (AnnotationRule annotationRule : annotation.term.rules) {
                if (annotationRule.highlight) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        rules += ", ";
                    }

                    Pattern p = Pattern.compile("%(.*)%");
                    Matcher m = p.matcher(annotationRule.rule);

                    if (m.find()) {
                        rules += "\"" + m.group(1) + "\"";
                    }
                }
            }
        }
        return rules;
    }

    public void doSemanticSimplification() {
        Set<Annotation> annotations = this.annotations;

        if (annotations.size() > 1) {
            List<BaoTerm> annotatedTerms = new ArrayList<BaoTerm>();

            for (Annotation annotation : annotations) {
                //Retrieves all the annotated terms
                annotatedTerms.add(annotation.term);
            }

            List<Annotation> toRemove = new ArrayList<Annotation>();

            for (Annotation annotation : annotations) {
                //If the children of an annotated term are
                //present in the annotated terms, then delete the annotation

                List<BaoTerm> children = annotation.term.children;
                for (BaoTerm child : children) {
                    if (annotatedTerms.contains(child)) {
                        //The annotation should be removed - flag as such to avoid concurrency problems
                        toRemove.add(annotation);
                    }
                }
            }

            for (Annotation annotationToRemove : toRemove) {
                this.annotations.remove(annotationToRemove);
                annotationToRemove.delete();
                this.save();
            }

        }
    }

    public void addFakeAnnotation() {
        List<BaoTerm> terms = this.getAnnotatedTerms();

        BaoTerm randomTerm = BaoTerm.find("order by rand()").first();;

        while (terms.contains(randomTerm)) {
            randomTerm = BaoTerm.find("order by rand()").first();
        }

        int confidence;
        if (Math.random() > 0.5) {
            confidence = 1;
        } else {
            confidence = 3;
        }
        Annotation newAnnotation = new Annotation(randomTerm, this, confidence, true).save();
        this.annotations.add(newAnnotation);
        this.save();
    }

    private List<BaoTerm> getAnnotatedTerms() {
        List<BaoTerm> terms = new ArrayList<BaoTerm>();
        for (Annotation annotation : this.annotations) {
            terms.add(annotation.term);
        }
        return terms;
    }

    public int getNumberOfFakeAnnotations() {
        int number = 0;
        for (Annotation annotation : this.annotations) {
            if (annotation.isFake && !annotation.toRemove) {
                number++;
            }
        }
        return number;
    }

    public void revert() {
        //The number of points to be removed is to be equal to the maximum
        //of points that can be earned by the assay. This should prevent cheating.
        int delta = -GameConstants.CORRECT_VALIDATION_POINTS;

        this.needReview = true;
        for (Annotation annotation : this.annotations) {
            if (annotation.isFake) {
                delta -= GameConstants.REMOVE_FAKE_ANNOTATION_POINTS;
            }

            if (annotation.toRemove) {
                annotation.toRemove = false;
                annotation.save();
            }
        }
        this.reviewer.updateScore(delta);
        this.save();
    }
}
