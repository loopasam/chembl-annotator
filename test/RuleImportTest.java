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

public class RuleImportTest extends UnitTest {

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

		//TODO more test
		Logger.info("Test the rules for errors");

		Connection c = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String url = "jdbc:postgresql://localhost/chembl_17";
		Class.forName("org.postgresql.Driver").newInstance();
		c = DriverManager.getConnection(url, "postgres", "pouet");
		
		List<AnnotationRule> rules = AnnotationRule.findAll();

		//TODO add time control to fail tests
		//helps to identify too consuming rules
		//TODO change the database to the MySQL one
		for (AnnotationRule rule : rules) {
			Logger.info("Testing rule: " + rule);
			try {
				pstmt = c.prepareStatement(rule.getSqlQuery());
				rs = pstmt.executeQuery();
			}catch(Exception e){
				fail("Error while trying to execute the SQL query related to the rule: " +
						rule.rule);
			}
		}
		
		try { if (rs != null) rs.close(); } catch (Exception e) {};
		try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
		try { if (c != null) c.close(); } catch (Exception e) {};
	}
}
