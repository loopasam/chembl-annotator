package jobs;

import java.util.concurrent.TimeUnit;

import models.AnnotatedAssay;
import models.BaoTerm;
import models.Utils;

import com.google.common.base.Stopwatch;

import play.Logger;
import play.jobs.Job;

public class FakeAnnotationsJob extends Job {

    public static int numberOfFakeAnnotations = Integer.parseInt((String) play.Play.configuration.get("application.fakeAnnotations"));

    @Override
    public void doJob() {
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        int counterFlush = 0;

        for (int i = 1; i <= numberOfFakeAnnotations; i++) {
            Logger.info("Fake annotation: " + i + "/" + numberOfFakeAnnotations);
            AnnotatedAssay assay = AnnotatedAssay.find("order by rand()").first();
            assay.addFakeAnnotation();

            counterFlush++;
            if (counterFlush % 50 == 0) {
                AnnotatedAssay.em().flush();
                AnnotatedAssay.em().clear();
            }
        }

        stopwatch.stop();
        Utils.emailAdmin("FakeAnnotationsJob completed", "Job done in "
                + "" + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes and generating " + numberOfFakeAnnotations
                + " fake annotations.");
        Logger.info("Job done in " + stopwatch.elapsed(TimeUnit.MINUTES) + " minutes.");

    }

}
