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

	public void doJob() throws IOException {

		Logger.info("Loading the rules from data/rules.csv");
		File file = new File("data/rules.csv");
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		for (String line : lines) {
			System.out.println(line);
			String[] splits = line.replaceAll("\"", "").split("\\|");
			String rule = splits[0];
			String baoId = splits[1];
			String comment = splits[2];
			int confidence = Integer.parseInt(splits[3]);
			boolean highlight;
			if(splits[4].equals("t")){
				highlight = true;	
			}else{
				highlight = false;
			}

			BaoTerm term = BaoTerm.find("byBaoId", baoId).first();
			term.addAnnotationRule(rule, comment, confidence, highlight);
		}
		Logger.info("All rules imported.");
	}
}
