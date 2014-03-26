import org.junit.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import play.test.*;
import models.*;

public class ModelTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void createAndRetrieveUser() {
		new Reviewer("bob@gmail.com", "secret").save();
		Reviewer bob = Reviewer.find("byEmail", "bob@gmail.com").first();
		assertNotNull(bob);
		assertEquals("bob@gmail.com", bob.email);
	}

	@Test
	public void tryConnectAsUser() {
		new Reviewer("bob@gmail.com", "secret").save();
		assertNotNull(Reviewer.connect("bob@gmail.com", "secret"));
		assertNull(Reviewer.connect("bob@gmail.com", "badpassword"));
		assertNull(Reviewer.connect("tom@gmail.com", "secret"));
	}

	@Test
	public void createAndRetrieveBaoTerm() {
		new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]").save();
		BaoTerm bioassay = BaoTerm.find("byBaoId", "BAO_0000015").first();
		assertNotNull(bioassay);
		assertEquals("bioassay", bioassay.label);
	}

	@Test
	public void createAndRetrieveRules() {
		new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]").save();
		BaoTerm bioassayTerm = BaoTerm.find("byBaoId", "BAO_0000015").first();
		new AnnotationRule(bioassayTerm, "SELECT * FROM foo", "identification of the word assay", 1, true).save();
		AnnotationRule bioassayRs = AnnotationRule.find("byBaoTerm", bioassayTerm).first();	
		assertNotNull(bioassayRs);
		//Relationship not maintained this way
		assertEquals(0, bioassayTerm.rules.size());
	}

	@Test
	public void createAndRetrieveAndMaintainRules() {
		new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]").save();
		BaoTerm bioassayTerm = BaoTerm.find("byBaoId", "BAO_0000015").first();
		bioassayTerm.addAnnotationRule("SELECT * FROM foo", "identification of the word assay");

		AnnotationRule bioassayRs = AnnotationRule.find("byBaoTerm", bioassayTerm).first();	
		assertNotNull(bioassayRs);
		//Relationship maintained this way
		assertEquals(1, bioassayTerm.rules.size());
	}

	@Test
	public void BaoTermHierarchy() {
		BaoTerm bioassayTerm = BaoTerm.createOrRetrieveTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "A set of instructions, [...]");
		assertEquals("bioassay", bioassayTerm.label);
		assertNotNull(BaoTerm.find("byBaoId", "BAO_0000015").first());
		bioassayTerm.addChild("http://www.bioassayontology.org/bao#BAO_0000014", "cell based assay", "A set of [...]");
		BaoTerm cellBasedTerm = BaoTerm.find("byBaoId", "BAO_0000014").first();
		assertNotNull(cellBasedTerm);
		assertEquals(1, bioassayTerm.children.size());
		assertEquals(0, cellBasedTerm.children.size());
		bioassayTerm.addChild("http://www.bioassayontology.org/bao#BAO_0000013", "cell growth assay", "A set of [...]");
		assertEquals(2, bioassayTerm.children.size());
		cellBasedTerm.addChild("http://www.bioassayontology.org/bao#BAO_0000012", "micro assay", "A set of [...]");
		assertEquals(1, cellBasedTerm.children.size());
		bioassayTerm.addChild("http://www.bioassayontology.org/bao#BAO_0000012", "micro assay", "A set of [...]");
		assertEquals(3, bioassayTerm.children.size());
	}

	@Test
	public void annotatedAssayTest() {
		new AnnotatedAssay(2, "CHEMBL201", "balbalab description").save();
		AnnotatedAssay assayRetrieved = AnnotatedAssay.find("byChemblId", "CHEMBL201").first();
		assertNotNull(assayRetrieved);
		assertNull(assayRetrieved.reviewer);
		new Reviewer("bob@gmail.com", "secret").save();
		Reviewer bob = Reviewer.find("byEmail", "bob@gmail.com").first();
		assayRetrieved.setReviewer(bob);
		assertNotNull(assayRetrieved.reviewer);
	}

	@Test
	public void annotationTest(){
		
		//Create a BAO term and add an annotation rule:
		BaoTerm term = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000015", "bioassay", "def").save();
		term.addAnnotationRule("description LIKE '%binding%'", "test rule");

		AnnotatedAssay assay = new AnnotatedAssay(1234, "CHEMBL1234", "foo description").save();
		new Annotation(term, assay, 2).save();
		
		Annotation annotation = Annotation.find("byAssay", assay).first();
		assertNotNull(annotation);
		
		Annotation annotation2 = Annotation.find("byTerm", term).first();
		assertNotNull(annotation2);
		
		AnnotatedAssay.createOrRetrieve(1234, "CHEMBL1234", "foo description");
		assertEquals(1, AnnotatedAssay.findAll().size());
		
		AnnotatedAssay assay1 = AnnotatedAssay.createOrRetrieve(4321, "CHEMBL4321", "foo description bar");
		assertEquals(2, AnnotatedAssay.findAll().size());

		BaoTerm term1 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000014", "binding assay", "def").save();
		term1.addAnnotationRule("description LIKE '%foo bar%'", "test rule", 2, false);
		
		//creates an annotation on the assay
		assay1.annotate(term1.rules.get(0));
		assertEquals(2, Annotation.findAll().size());
		assertEquals(1, assay1.annotations.size());
		assertEquals(2, assay1.annotations.get(0).confidence);
		
		//increases confidence as the assay is already annotated with the term
		assay1.annotate(term1.rules.get(0));
		assertEquals(2, Annotation.findAll().size());
		assertEquals(1, assay1.annotations.size());
		assertEquals(4, assay1.annotations.get(0).confidence);
		
		//Increases the confidence as the assay is already annotated with the term, even if the rule is different
		term1.addAnnotationRule("description LIKE '%fox bar%'", "test rule", 3, false);
		assay1.annotate(term1.rules.get(1));
		assertEquals(1, assay1.annotations.size());
		assertEquals(7, assay1.annotations.get(0).confidence);
		
		//Create a second term to put two annotations on the assay
		BaoTerm term2 = new BaoTerm("http://www.bioassayontology.org/bao#BAO_0000013", "radioligand assay", "def").save();
		term2.addAnnotationRule("description LIKE '%radioligand foo%'", "test rule", 1, false);
		assay1.annotate(term2.rules.get(0));
		assertEquals(2, assay1.annotations.size());
	}

}
