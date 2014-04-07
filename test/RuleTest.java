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
		assertEquals(false, annotationRule.hasPriority);
		BaoTerm term = BaoTerm.find("byLabel", "nuclear membrane potential assay").first();
		assertNotNull(term);
		assertEquals(term, annotationRule.baoTerm);
		
		AnnotationRule rule = AnnotationRule.find("byComment", "Look for the activity type GI50").first();
		assertNotNull(rule);
		assertEquals(true, rule.hasPriority);
		assertEquals(false, rule.highlight);

		//TODO more test - has to be done over mysql local
		//Test also for priority

		//TODO test the number of annotation rules per term - should two highlight at least

		//		Logger.info("Test the rules for errors");
		//		Connection c = null;
		//		PreparedStatement pstmt = null;
		//		ResultSet rs = null;
		//		String url = "jdbc:postgresql://localhost/chembl_17";
		//		Class.forName("org.postgresql.Driver").newInstance();
		//		c = DriverManager.getConnection(url, "postgres", "pouet");
		//		
		//		List<AnnotationRule> rules = AnnotationRule.findAll();
		//
		//TODO change the database to the MySQL one
		//		for (AnnotationRule rule : rules) {
		//			Logger.info("Testing rule: " + rule);
		//			try {
		//				pstmt = c.prepareStatement(rule.getSqlQuery());
		//				rs = pstmt.executeQuery();
		//			}catch(Exception e){
		//				fail("Error while trying to execute the SQL query related to the rule: " +
		//						rule.rule);
		//			}
		//		}

		//		try { if (rs != null) rs.close(); } catch (Exception e) {};
		//		try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
		//		try { if (c != null) c.close(); } catch (Exception e) {};
	}
}
