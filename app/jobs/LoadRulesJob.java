package jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.AnnotationRule;
import models.BaoTerm;

import org.apache.commons.io.FileUtils;
import org.h2.constant.SysProperties;

import play.Logger;
import play.jobs.Job;

public class LoadRulesJob extends Job {

    @Override
    public void doJob() throws IOException {

        Logger.info("Loading the rules from data/rules.txt");
        File file = new File("data/rules.txt");
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        for (String line : lines) {
            String[] splits = line.split("\\|");
            String rule = splits[0];
            String baoId = splits[1];
            String comment = splits[2];
            int confidence = Integer.parseInt(splits[3]);

            boolean highlight;
            if (splits[4].equals("true")) {
                highlight = true;
            } else {
                highlight = false;
            }

            boolean isFilter;
            if (splits[5].equals("true")) {
                isFilter = true;
            } else {
                isFilter = false;
            }

            BaoTerm term;
            if (!baoId.equals("filter")) {
                term = BaoTerm.find("byBaoId", baoId).first();
            } else {
                term = BaoTerm.find("byBaoId", "BAO_0000015").first();
            }

            term.addAnnotationRule(rule, comment, confidence, highlight, isFilter);
        }
        Logger.info("All rules imported.");
    }
}
