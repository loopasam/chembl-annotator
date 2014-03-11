import java.util.concurrent.ExecutionException;

import jobs.AnnotateAllAssaysJob;
import jobs.LoadBaoJob;

import models.AnnotatedAssay;
import models.BaoTerm;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class AnnotateAssaysTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}


	@Test
	public void annotateAssay() throws InterruptedException, ExecutionException {
		BaoTerm term = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "def").save();
		term.addAnnotationRule("description ILIKE '%binding%'", "test rule");
		
		//Creates a new AnnotatedAssay
		AnnotatedAssay.annotate(1234, "CHEMBL1234", "foo description", term);
		AnnotatedAssay assay = AnnotatedAssay.find("byAssayId", 1234).first();
		assertNotNull(assay);
		assertEquals(1, assay.annotations.size());
		
		//Add a term - no new assay
		BaoTerm term2 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000014", "cell growth assay", "def").save();
		AnnotatedAssay.annotate(1234, "CHEMBL1234", "foo description", term2);
		assertEquals(2, assay.annotations.size());
		assertEquals(1, AnnotatedAssay.findAll().size());
		
		//Re-annotate with the same term, should not add it again
		AnnotatedAssay.annotate(1234, "CHEMBL1234", "foo description", term2);
		assertEquals(2, assay.annotations.size());
		assertEquals(1, AnnotatedAssay.findAll().size());
	}


}
