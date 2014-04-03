import java.util.concurrent.ExecutionException;

import jobs.LoadBaoJob;
import models.BaoTerm;
import models.Reviewer;

import org.junit.Before;
import org.junit.Test;

import play.libs.F.Promise;
import play.test.Fixtures;
import play.test.UnitTest;


public class BaoTest extends UnitTest {
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void loadAndSaveBaoTerms() throws InterruptedException, ExecutionException {
		new LoadBaoJob().now().get();
		assertEquals(92, BaoTerm.findAll().size());
		BaoTerm bioassay = BaoTerm.find("byBaoId", "BAO_0000015").first();
		assertNotNull(bioassay);
		assertEquals("http://www.bioassayontology.org/bao#BAO_0000015", bioassay.baoUrl);
		assertEquals("bioassay", bioassay.label);
		assertEquals("A set of instructions, methodology, " +
				"operations, required reagents, instruments to " +
				"carrie out experiments for the purpose of testing " +
				"the effect of a perturbing agent in a biological " +
				"model system, measuring one or multiple effect(s) " +
				"of the agent facilitated by an assay design method " +
				"translate the perturbation into a detectable signal " +
				"to arrive at one or multiple endpoint(s) that quantify or " +
				"qualify the extent of the perturbation.  " +
				"Bioassay is described by multiple bioassay " +
				"components: assay format, biology (biological participants " +
				"in various role and processes), design method, physical " +
				"detection method / technology, screened entity, and endpoint. " +
				"Bioassay includes one or multiple measure groups to describe panel, " +
				"profiling, multiparametric (or multiplexed) assays (assays that measure " +
				"more than one effect of the perturbagen on " +
				"the system that is screened).", bioassay.definition);
		
		assertEquals(33, bioassay.children.size());
	}
}
