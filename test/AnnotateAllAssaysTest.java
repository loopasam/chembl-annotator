import java.util.concurrent.ExecutionException;

import jobs.AnnotateAllAssaysJob;
import jobs.LoadBaoJob;

import models.AnnotatedAssay;
import models.BaoTerm;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class AnnotateAllAssaysTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.delete(AnnotatedAssay.class);
	}


	@Test
	public void loadAndSaveBaoTerms() throws InterruptedException, ExecutionException {
		BaoTerm term = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "def").save();
		term.addAnnotationRule("description ILIKE '%binding%'", "test rule");
		new AnnotateAllAssaysJob().now().get();
		//test more stuff
	}


}
