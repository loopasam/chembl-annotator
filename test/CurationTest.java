import models.AnnotatedAssay;
import models.Annotation;
import models.AnnotationRule;
import models.BaoTerm;
import models.Reviewer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class CurationTest extends UnitTest {

	@BeforeClass
	public static void setup() {
		Fixtures.deleteDatabase();

		//Fake automatic annotation process
		new Reviewer("bob@gmail.com", "password").save();

		BaoTerm baoTerm1 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]").save();
		AnnotationRule rule1 = baoTerm1.addAnnotationRule("description LIKE '%bioassay%'", "test rule", 1, false);

		BaoTerm baoTerm2 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000014", "cell growth assay", "A set of instructions, [...]").save();
		AnnotationRule rule2 = baoTerm2.addAnnotationRule("description LIKE '%cell growth assay%'", "test rule", 1, false);

		AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(4321, "CHEMBL4321", "foo description bar");
		assay1.annotate(rule1, false, null);

		AnnotatedAssay assay2 = AnnotatedAssay.createOrRetrieve(1234, "CHEMBL1234", "foo description bar");
		assay2.annotate(rule1, false, null);
		assay2.annotate(rule2, false, null);
	}

	@Test
	public void removeAnnotation() {
		assertEquals(2, AnnotatedAssay.findAll().size());
		AnnotatedAssay assay1 = AnnotatedAssay.find("byChemblId", "CHEMBL4321").first();

		assertEquals(3, Annotation.findAll().size());

		Long id = assay1.annotations.get(0).id;
		assay1.removeAnnotation(id);

		assertEquals(0, assay1.annotations.size());
		assertEquals(2, Annotation.findAll().size());
	}

	@Test
	public void starAssay() {
		AnnotatedAssay assay1 = AnnotatedAssay.find("byChemblId", "CHEMBL4321").first();
		assay1.star();
		assertEquals(true, assay1.starred);
		assay1.star();
		assertEquals(false, assay1.starred);
		assay1.star();
		assay1.star();
		assertEquals(false, assay1.starred);
	}

	@Test
	public void validateAssay() {
		Reviewer reviewer = Reviewer.find("byEmail", "bob@gmail.com").first();
		AnnotatedAssay assay1 = AnnotatedAssay.find("byChemblId", "CHEMBL4321").first();
		assay1.markAsCurated(reviewer);
		assertEquals(false, assay1.needReview);
		assertNotNull(assay1.reviewer);
		assertEquals("bob@gmail.com", assay1.reviewer.email);
	}

}
