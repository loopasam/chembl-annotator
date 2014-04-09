import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import jobs.SemanticSimplificationJob;

import models.AnnotatedAssay;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class SemanticSimplificationTest extends UnitTest {

	@BeforeClass
	public static void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void simplify() throws InterruptedException, ExecutionException{
		BaoTerm baoTerm1 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]").save();
		AnnotationRule rule1 = baoTerm1.addAnnotationRule("description LIKE '%bioassay%'", "test rule", 1, false);

		baoTerm1.addChild("http://www.bioassayontology.org/bao#BAO_0000014", "cell growth assay", "A set of instructions, [...]");
		BaoTerm term2 = BaoTerm.find("byBaoId", "BAO_0000014").first();
		AnnotationRule rule2 = term2.addAnnotationRule("description LIKE '%cell growth assay%'", "test rule", 1, false);

		AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(4321, "CHEMBL4321", "foo description bar");
		//Create redundant annotation
		assay1.annotate(rule1, false, null);
		assay1.annotate(rule2, false, null);

		AnnotatedAssay assay = AnnotatedAssay.find("byChemblId", "CHEMBL4321").first();
		assertNotNull(assay);
		assertEquals(2, assay.annotations.size());

		List<AnnotatedAssay> assays = AnnotatedAssay.findAll();

		for (AnnotatedAssay annotatedAssay : assays) {
			annotatedAssay.doSemanticSimplification();
		}

		AnnotatedAssay assay2 = AnnotatedAssay.find("byChemblId", "CHEMBL4321").first();
		assertNotNull(assay2);
		assertEquals(1, assay2.annotations.size());
		
	}
}
