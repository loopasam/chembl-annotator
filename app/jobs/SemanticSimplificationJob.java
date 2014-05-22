package jobs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;

import models.AnnotatedAssay;
import models.Annotation;
import models.BaoTerm;
import play.Logger;
import play.jobs.Job;

public class SemanticSimplificationJob extends Job {

    @Override
    public void doJob() {

        Logger.info("Simplification started...");
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        List<AnnotatedAssay> assays = AnnotatedAssay.findAll();
        int total = assays.size();
        int counter = 0;

        //Identify all the annotations to be removed (ids) and store them somewhere (= list)
        //Iterate over the list, make the modifications and flush regularly
        List<Long> toRemove = new ArrayList<Long>();

        for (AnnotatedAssay annotatedAssay : assays) {
            counter++;
            Logger.info("Assay " + counter + "/" + total);

            if (annotatedAssay.annotations.size() > 1) {

                List<BaoTerm> annotatedTerms = new ArrayList<BaoTerm>();

                for (Annotation annotation : annotatedAssay.annotations) {
                    //Retrieves all the annotated terms not to be removed
                    if (!annotation.toRemove) {
                        annotatedTerms.add(annotation.term);
                    }
                }

                for (Annotation annotation : annotatedAssay.annotations) {
                    if (!annotation.toRemove) {
                        //If the children of an annotated term are
                        //present in the annotated terms, then delete the annotation
                        List<BaoTerm> children = annotation.term.children;
                        for (BaoTerm child : children) {
                            if (annotatedTerms.contains(child) && !toRemove.contains(annotation.id)) {
                                //The annotation should be removed - flag as such to avoid concurrency problems
                                toRemove.add(annotation.id);
                            }
                        }
                    }
                }
            }

        }

        Logger.info("Flaging annotations to be removed: " + toRemove.size());
        int counterToRemove = 0;
        int totalToRemove = toRemove.size();

        for (Long id : toRemove) {
            counterToRemove++;
            Logger.info("Removing: " + counterToRemove + "/" + totalToRemove);
            Annotation annotation = Annotation.findById(id);
            annotation.toRemove = true;
            annotation.save();
            Logger.info("Annotation: " + annotation.term.label + " - " + annotation.assay.chemblId);
            if (counterToRemove % 50 == 0) {
                Annotation.em().flush();
                Annotation.em().clear();
            }
        }

        stopwatch.stop();
        Logger.info("Annotation job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
    }
}
