import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class ModelTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void createAndRetrieveUser() {
		new User("bob@gmail.com", "secret").save();
		User bob = User.find("byEmail", "bob@gmail.com").first();
		assertNotNull(bob);
		assertEquals("bob@gmail.com", bob.email);
	}

	@Test
	public void tryConnectAsUser() {
		new User("bob@gmail.com", "secret").save();
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNull(User.connect("bob@gmail.com", "badpassword"));
		assertNull(User.connect("tom@gmail.com", "secret"));
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
		new AnnotationRule(bioassayTerm, "SELECT * FROM foo", "identification of the word assay").save();
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
		new User("bob@gmail.com", "secret").save();
		User bob = User.find("byEmail", "bob@gmail.com").first();
		assayRetrieved.setReviewer(bob);
		assertNotNull(assayRetrieved.reviewer);
	}


}
