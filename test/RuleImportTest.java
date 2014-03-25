import java.util.concurrent.ExecutionException;

import jobs.LoadBaoJob;
import jobs.LoadRulesJob;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;

public class RuleImportTest extends UnitTest {

		@Before
		public void setup() {
			Fixtures.deleteDatabase();
		}

	@Test
	public void loadTerms() throws InterruptedException, ExecutionException{
		Logger.info("Loading BAO terms...");
		new LoadBaoJob().now().get();

		Logger.info("Import the rules...");
		new LoadRulesJob().now().get();
		//TODO do more test once loaded
	}

}
