import java.util.concurrent.ExecutionException;

import jobs.LoadBaoJob;
import models.BaoTerm;
import models.Reviewer;

import org.junit.Before;
import org.junit.Test;

import play.libs.F.Promise;
import play.test.Fixtures;
import play.test.UnitTest;


public class BaoLoaderJobTest extends UnitTest {
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void loadAndSaveBaoTerms() throws InterruptedException, ExecutionException {
		//TODO can be tested differently.
		new LoadBaoJob().now().get();
		//test more stuff
	}


}
