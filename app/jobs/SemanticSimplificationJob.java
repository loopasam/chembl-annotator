package jobs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import models.AnnotatedAssay;
import models.Annotation;
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

        for (AnnotatedAssay annotatedAssay : assays) {
            counter++;
            Logger.info("Assay " + counter + "/" + total);

            annotatedAssay.doSemanticSimplification();
            if (counter % 100 == 0) {
                Annotation.em().flush();
                Annotation.em().clear();
            }

        }

        stopwatch.stop();
        Logger.info("Annotation job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");
    }
}
