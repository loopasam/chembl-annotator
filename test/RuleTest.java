import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jobs.LoadBaoJob;
import jobs.LoadRulesJob;

import models.AnnotationRule;
import models.BaoTerm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;

public class RuleTest extends UnitTest {

	@BeforeClass
	public static void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void loadTerms() throws Exception {

		Logger.info("Loading BAO terms...");
		new LoadBaoJob().now().get();

		Logger.info("Import the rules...");
		new LoadRulesJob().now().get();

		assertEquals(219, AnnotationRule.findAll().size());

		AnnotationRule annotationRule = AnnotationRule.find("byRule", "LOWER(description) LIKE '%nuclear membrane potential assay%'").first();
		assertNotNull(annotationRule);
		assertEquals("text-mining on BAO label", annotationRule.comment);
		assertEquals(3, annotationRule.confidence);
		assertEquals(true, annotationRule.highlight);
		BaoTerm term = BaoTerm.find("byLabel", "nuclear membrane potential assay").first();
		assertNotNull(term);
		assertEquals(term, annotationRule.baoTerm);
		
		AnnotationRule rule = AnnotationRule.find("byComment", "Look for the activity type GI50").first();
		assertNotNull(rule);
		assertEquals(false, rule.highlight);
	}
}
