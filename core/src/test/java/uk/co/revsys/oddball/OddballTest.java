/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.revsys.oddball;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.revsys.oddball.bins.Bin;
import uk.co.revsys.oddball.bins.BinSet;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.cases.MapCase;
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.oddball.rules.Rule;
import uk.co.revsys.oddball.rules.RuleSet;
import uk.co.revsys.oddball.util.JSONUtil;
import uk.co.revsys.resource.repository.LocalDiskResourceRepository;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class OddballTest {

    ResourceRepository resourceRepository = new LocalDiskResourceRepository(new File("src/test/resources"));

    public OddballTest() {

    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatch() throws Exception {
        String ruleSetName = "Test1.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains("abc-ish"));
        assertFalse(result.getLabel().contains("null"));
    }

    @Test
    public void testAssessCaseSubdirectoryRuleFile() throws Exception {
        String ruleSetName = "testCo/Test1.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("aString"));
        assertTrue(result.getLabel().contains("abc-ish"));
        assertFalse(result.getLabel().contains("null"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
//    @Test
//    public void testAssessCaseMatchPersistentRules() throws Exception {
//        System.out.println("assessCase");
//        String ruleSetName = "Test1persist";
//        Case aCase = new StringCase("abc123");
//        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
//        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
//        assertTrue(result.getLabel().contains("string"));
//        assertTrue(result.getLabel().contains("aString"));
//        assertTrue(result.getLabel().contains("abc-ish"));
//        assertFalse(result.getLabel().contains("null"));
//        instance.ruleSets.get("Test1persist").getPersist().dropCases();
//    }
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddball() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballAddRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "", "b-rule", "b-.*", "added");
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("*odDball*"));
        assertTrue(result.getLabel().contains("b-rule"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballAddIgnoreRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "", "*ignore*", "b-.*", "added");
        System.out.println("#rules");
        System.out.println(instance.ruleSets.get(ruleSetName).getAllRules().size());
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("*odDball*"));
        assertFalse(result.getLabel().contains("*ignore*"));
        assertEquals("{ \"tags\" : [] }", result.getLabel());
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatchPrefixRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.abc-ish"));
        assertFalse(result.getLabel().contains("null"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatchPrefixRuleDefault() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3b.txt";
        Case aCase = new StringCase("abx123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.whoops!"));
        assertFalse(result.getLabel().contains("null"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseMatchPrefixRuleMultimatch() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3a.txt";
        Case aCase = new StringCase("abc123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.abc-ish"));
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("anotherType.alpha"));
        assertFalse(result.getLabel().contains("null"));
        assertTrue(result.getEnrichedCase(ruleSetName, aCase, false, null).contains("\"anotherType\" : \"abc-ish,alpha\""));
    }

    @Test
    public void testAssessCaseMatchPrefixRuleOverride() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3c.txt";
        Case aCase = new StringCase("abcd123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.abcd-ish"));
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("null"));
        assertTrue(result.getEnrichedCase(ruleSetName, aCase, false, null).contains("\"anotherType\" : \"abcd-ish\""));
    }

    @Test
    public void testAssessCaseMatchPrefixRuleOverrideExclusive() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3d.txt";
        Case aCase = new StringCase("abcd123");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(result.getLabel().contains("string"));
        assertTrue(result.getLabel().contains("stringType.spoton"));
        assertFalse(result.getLabel().contains("stringType.aString"));
        assertTrue(result.getLabel().contains("anotherType.abcd-ish"));
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("null"));
        assertTrue(result.getEnrichedCase(ruleSetName, aCase, false, null).contains("\"anotherType\" : \"abcd-ish\""));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballPrefix() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertTrue(result.getLabel().contains("anotherType.odDball"));
        assertFalse(result.getLabel().contains("null"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballPrefixAddRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "Test3.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertTrue(result.getLabel().contains("anotherType.odDball"));
        assertFalse(result.getLabel().contains("null"));
        instance.addExtraRule(ruleSetName, "anotherType.", "b-rule", "b-.*", "added");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("string"));
        assertFalse(result.getLabel().contains("other.string"));
        assertFalse(result.getLabel().contains("anotherType.odDball"));
        assertTrue(result.getLabel().contains("anotherType.b-rule"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballMongo() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";

        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
    }

    @Test
    public void testAssessCaseOddballMongoTagTwice() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo2.txt";

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
    }

    @Test
    public void testAssessCaseOddballMongoRulesFileWithIndex() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo3a.rules"; // rule C is exclusive

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertFalse(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertFalse(result.getLabel().contains("ruleB"));
    }

    @Test
    public void testAssessCaseOddballMongoRulesFileWithIndexAndReplacementRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo3.rules";

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
    }

    @Test
    public void testAssessCaseOddballMongoRulesFile() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo2.rules";

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
    }

    @Test
    public void testAssessCaseOddballMongoRulesFileReplacedTag() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo2b.rules";

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertFalse(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertTrue(result.getLabel().contains("ruleC"));
        assertFalse(result.getLabel().contains("ruleB"));
    }

    @Test
    public void testAssessCaseOddballMongoRulesFileInactiveRule() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo4.rules";

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Case aCase = new MapCase("{\"browser\":\"chromium\", \"platform\":\"windows\"}");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertFalse(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));

        aCase = new MapCase("{\"browser\":\"google\", \"platform\":\"windows\"}");
        result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        assertFalse(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
    }


    
    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballMongoEnrichedCase() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";

        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\", \"time\":\"1415958560342\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getEnrichedCase(ruleSetName, aCase, false, null));
        Map<String, Object> compare = JSONUtil.json2map(result.getEnrichedCase(ruleSetName, aCase, false, null));
        assertTrue(compare.containsKey("case"));
        assertTrue(compare.containsKey("caseTime"));
        assertTrue(compare.containsKey("tags"));
        assertTrue(compare.containsKey("timestamp"));
        assertTrue(compare.containsKey("derived"));
    }

    /**
     * Test of assessCase method, of class Oddball.
     */
    @Test
    public void testAssessCaseOddballMongoOddball() throws Exception {
        System.out.println("assessCase");
        String ruleSetName = "TestMongo.txt";

        Case aCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("*odDball*"));
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCases() throws Exception {
        System.out.println("findCases-1");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        System.out.println("result=" + result.toString());
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesDuplicates() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        int count = 0;
        for (String aCase : cases) {
            System.out.println(aCase);
            count++;
        }

        assertTrue(cases.iterator().hasNext());
        assertEquals(2, count);
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesDuplicatesNoPersist() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        instance.assessCaseOpinion(ruleSetName, null, theCase, RuleSet.NEVERPERSIST, null, null, options);
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        int count = 0;
        for (String aCase : cases) {
            System.out.println(aCase);
            count++;
        }

        assertTrue(cases.iterator().hasNext());
        assertTrue(count == 1);
    }

    @Test
    public void testFindCasesDuplicatesUpdate() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");
        Case theRevisedCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");
        Case theDifferentCase = new MapCase("{\"series\":\"237890\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase); // should be added
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        int count = 0;
        for (String aCase : cases) {
            System.out.println(aCase);
            count++;
        }
        instance.assessCaseOpinion(ruleSetName, null, theRevisedCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"123789\"}", null, options);
        instance.assessCaseOpinion(ruleSetName, null, theDifferentCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"237890\"}", null, options);
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        count = 0;
        for (String aCase : cases) {
            System.out.println(aCase);
            count++;
        }
        assertTrue(cases.iterator().hasNext());
        assertTrue(count == 2);
    }

    @Test
    public void testFindCasesDuplicatesUpdatePlaceholder() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\"}");
        Case theRevisedCase = new MapCase("{\"series\":\"123789\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");
        Case theDifferentCase = new MapCase("{\"series\":\"237890\", \"browser\":\"firefox\", \"platform\":\"android\", \"orientation\":\"landscape\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase); // should be added
        HashMap<String, String> options = new HashMap<String, String>();
        instance.assessCaseOpinion(ruleSetName, null, theRevisedCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"<series>\"}", null, options);
        instance.assessCaseOpinion(ruleSetName, null, theDifferentCase, RuleSet.UPDATEPERSIST, "{\"case.series\":\"<series>\"}", null, options);
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        int count = 0;
        for (String aCase : cases) {
            System.out.println(aCase);
            count++;
        }

        assertTrue(cases.iterator().hasNext());
        assertTrue(count == 2);
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQueryBeforeAssess() throws Exception {
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        String ruleSetName = "TestMongo.txt";
        System.out.println("findCasesBeforeAssess");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", new HashMap<String, String>());
        for (String aCase : cases) {
            System.out.println(aCase);
        }
        assertFalse(cases.iterator().hasNext());
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\"}, \"hashTag\":\"#luckybreak\"}");

        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases2 = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases2) {
            System.out.println(aCase);
            assertTrue(aCase.contains("#luckybreak"));
        }
        assertTrue(cases2.iterator().hasNext());
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQuery() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
        System.out.println("findCases done");
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesWithQuoteQuery() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox type \\\"A\\\"\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", options);
        for (String aCase : cases) {
            System.out.println(aCase);
            System.out.println(JSONUtil.json2map(aCase));
            System.out.println(JSONUtil.map2json(JSONUtil.json2map(aCase)));
        }

        assertTrue(cases.iterator().hasNext());
        System.out.println("findCases done");
    }

    /**
     * Test of findCases method, of class Oddball.
     */
    @Test
    public void testFindCasesQueryWithTransform() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", "testjsont.json");
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    @Test
    public void testFindCasesQueryWithTransformSubdirectory() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "testCo/TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("transformer", "testCo/testjsont.json");
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"AA11\"}", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    @Test
    public void testFindDistinctQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", new HashMap<String, String>());
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("distinct", "case.sessionId");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    @Test
    public void testFindCountQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", new HashMap<String, String>());
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("count", "true");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());
        assertEquals("3",cases.iterator().next());
    }

    @Test
    public void testFindLatestQuery() throws Exception {
        System.out.println("findLatest");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, otherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        assertTrue(cases0.iterator().hasNext());
        options.put("selector", "latest");
        Collection<String> latestCase = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(latestCase);
        assertTrue(latestCase.size() == 1);
    }

    @Test
    public void testFindMaxQuery() throws Exception {
        System.out.println("findLatest");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":100, \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":150, \"sessionId\":\"AA12\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":180, \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        Opinion result3 = instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, otherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        assertTrue(cases0.iterator().hasNext());
        options.put("selector", "max:case.response");
        Collection<String> latestCase = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(latestCase);
        assertTrue(latestCase.size() == 1);
        String first = latestCase.iterator().next();
        assertTrue(first.contains("\"response\" : 180"));
    }

    @Test
    public void testFindMinQuery() throws Exception {
        System.out.println("findLatest");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":100, \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":150, \"sessionId\":\"AA12\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"response\":180, \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        Opinion result3 = instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, otherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        assertTrue(cases0.iterator().hasNext());
        options.put("selector", "min:case.response");
        Collection<String> latestCase = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(latestCase.size() == 1);
        String first = latestCase.iterator().next();
        System.out.println(first);
        assertTrue(first.contains("\"response\" : 100"));
    }


    
    @Test
    public void testFindLatestSkipQuery() throws Exception {
        System.out.println("findLatest");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, otherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        assertTrue(cases0.iterator().hasNext());
        options.put("selector", "latest 2-2");
        Collection<String> latestCase = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(latestCase);
        assertTrue(latestCase.size() == 1);
        options.put("selector", "latest 2-3");
        latestCase = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(latestCase);
        assertTrue(latestCase.size() == 2);
    }

    @Test
    public void testFindRecentQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recent", "5");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("owner", "_all");
        Collection<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.size() == 3);
    }

    @Test
    public void testFindRecentQueryMultipleRuleSets() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        String multiName = "TestMongo.txt+TestMongo.txt";
        Iterable<String> cases0 = instance.findQueryCases(multiName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recent", "5");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("owner", "_all");
        Collection<String> cases = instance.findQueryCases(multiName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.size() == 6);
    }

    @Test
    public void testFindRecentAgoQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recent", "5");
        options.put("ago", "0~");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("owner", "_all");
        Collection<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.size() == 3);
    }

    @Test
    public void testFindDistinctRecentQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recent", "5");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    @Test
    public void testFindDistinctSinceQuery() throws Exception {
        System.out.println("findDistinct");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        long since = new Date().getTime() - 5 * 60 * 1000;
        options.put("since", Long.toString(since));
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
    }

    @Test
    public void testFindDistinctQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertTrue(cases.iterator().hasNext());
        options.put("owner", "Real");
//        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertFalse(cases.iterator().hasNext());
    }

    @Test
    public void testFindDistinctRecentQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
        options.put("recent", "5");
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());

        options.put("owner", "Real");
        options.put("recent", "5");
//        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertFalse(cases.iterator().hasNext());
    }

    @Test
    public void testFindDistinctSinceQueryForOwner() throws Exception {
        System.out.println("findDistinctOwner");
        String ruleSetName = "TestMongo.txt";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"accountId\":\"Trial\"}");
        Case otherCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA12\", \"accountId\":\"Trial\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Opinion result2 = instance.assessCaseOpinion(ruleSetName, null, otherCase);
        instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "Trial");
        long since = new Date().getTime() - 5 * 60 * 1000;
        options.put("since", Long.toString(since));
//        Iterable<String> cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
        options.put("distinct", "case.sessionId");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }
        assertTrue(cases.iterator().hasNext());

        options.put("owner", "Real");
        options.put("recent", "5");
//        cases = instance.findDistinct(ruleSetName, "case.sessionId", options);
//        options.put("distinct", "case.sessionId");
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        for (String aCase : cases) {
            System.out.println(aCase);
        }

        assertFalse(cases.iterator().hasNext());
    }

    @Test
    public void testLoadBins() throws Exception {
        System.out.println("loadBins");
        String binSetName = "TestBins.txt";
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        BinSet binSet = instance.loadBinSet(binSetName, resourceRepository);
        assertTrue(binSet != null);
        assertTrue(binSet.getBins().size() == 3);
        Bin firstBin = binSet.getBins().get("bin1");
        assertEquals("bin1", firstBin.getLabel());
    }

    @Test
    public void testUseBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"IE8\", \"platform\":\"windows\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCaseOpinion(ruleSetName, null, anotherCase);

        BinSet binSet = instance.binSet;
        assertTrue(binSet != null);
        assertTrue(binSet.getBins().size() == 3);
        Bin firstBin = binSet.getBins().get("bin1");
        assertEquals("bin1", firstBin.getLabel());
        String binQuery = firstBin.getBinString();

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, binQuery, options);
        for (String foundCase : cases) {
            System.out.println(foundCase);
        }

        assertTrue(cases.iterator().hasNext());

    }

    @Test
    public void testListBins() throws Exception {

        System.out.println("ListBins");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        BinSet binSet = instance.binSet;
        assertTrue(binSet != null);
        Collection<String> binLabels = binSet.listBinLabels();
        assertTrue(binLabels.size() == 3);
        assertTrue(binLabels.contains("bin1"));
        Collection<String> binLabelsIt = instance.listBinLabels("eCK-1005");
        System.out.println(binLabelsIt);
        assertEquals(7, binLabelsIt.size());
        assertTrue(binLabelsIt.contains("Mybin1"));
    }

    @Test
    public void testFindCasesInBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleC"));
        assertTrue(result.getLabel().contains("ruleB"));
        assertTrue(result.getLabel().contains("ruleA"));
        result = instance.assessCaseOpinion(ruleSetName, null, anotherCase);

        BinSet binSet = instance.binSet;

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("binLabel", "bin1");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases) {
            System.out.println("in bin1:");
            System.out.println(foundCase);
        }

        options.put("binLabel", "bin2");
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases) {
            System.out.println("in bin2:");
            System.out.println(foundCase);
        }

        options.put("binLabel", "bin3");
        cases = instance.findQueryCases(ruleSetName, "{ }", options);
        assertTrue(cases.iterator().hasNext());
        for (String foundCase : cases) {
            System.out.println("in bin3:");
            System.out.println(foundCase);
        }

    }

    @Test
    public void testFindDistictPropertyInBin() throws Exception {

        System.out.println("applyBins");
        String ruleSetName = "TestMongo.txt";
        Case aCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"windows\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");

        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        System.out.println(result.getLabel());
        assertTrue(result.getLabel().contains("ruleB"));
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);

        BinSet binSet = instance.binSet;

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("binLabel", "bin2");
        options.put("owner", "_all");
        Iterable<String> cases = instance.findQueryCases(ruleSetName, "{ }", options);
//        cases = instance.findCasesInBin(ruleSetName, "bin2", options);
        assertTrue(cases.iterator().hasNext());
        int count = 0;
        for (String foundCase : cases) {
            System.out.println("in bin2:");
            System.out.println(foundCase);
            count++;
        }
        assertTrue(count == 3);

        options.put("distinct", "case.platform");
        options.put("binLabel", "bin2");
        Iterable<String> platforms = instance.findQueryCases(ruleSetName, "{ }", options);
        assertTrue(platforms.iterator().hasNext());
        count = 0;
        for (String platform : platforms) {
            System.out.println("in bin2:");
            System.out.println(platform);
            count++;
        }
        assertTrue(count == 2);

    }

    @Test
    public void testAddRuleTwice() throws Exception {
        System.out.println("addRuleTwice");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        List<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules) {
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "", "b-rule", "b-.*", "added");
        rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int m = rules.size();
        assertTrue((m - n) == 1);
        instance.addExtraRule(ruleSetName, "", "B-rule", "b-.*", "added");
        rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int l = rules.size();
        assertTrue((l - m) == 0);
    }

    @Test
    public void testFindRules() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test2.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        List<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules) {
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "initial.", "b-rule", "b-.*", "added");
        instance.addExtraRule(ruleSetName, "initial.", "B-rule", "B-.*", "added");
        instance.addExtraRule(ruleSetName, "terminal.", "rule-B", ".*-B", "added");
        instance.addExtraRule(ruleSetName, "initial.", "bb-rule", "bb-.*", "inserted");
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("prefix", "initial");
        options.put("source", "added");
        Iterable<String> foundRules = instance.findRules(ruleSetName, options);
        int count = 0;
        for (String foundRule : foundRules) {
            count++;
            System.out.println("found: " + foundRule);
        }
        assertTrue(count == 2);

    }

    @Test
    public void testShowRules() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test2.txt";
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        HashMap<String, String> options = new HashMap<String, String>();
        String foundRules = instance.showRules(ruleSetName, options);
        System.out.println("found: " + foundRules);

    }


    @Test
    public void testShowRules2() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test3c.txt";
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        HashMap<String, String> options = new HashMap<String, String>();
        String foundRules = instance.showRules(ruleSetName, options);
        System.out.println("found: " + foundRules);

    }


    @Test
    public void testShowRules3() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "TestMongoEvent";
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        HashMap<String, String> options = new HashMap<String, String>();
        String foundRules = instance.showRules(ruleSetName, options);
        System.out.println("found: " + foundRules);

    }


    
    @Test
    public void testSaveRules() throws Exception {
        System.out.println("findRules");
        String ruleSetName = "Test4.txt";
        Case aCase = new StringCase("b-side");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, aCase);
        List<Rule> rules = instance.ruleSets.get(ruleSetName).getAllRules();
        int n = rules.size();
        System.out.println("Rules before");
        System.out.println(n);
        for (Rule rule : rules) {
            System.out.println(rule.toString());
        }
        instance.addExtraRule(ruleSetName, "initial.", "b-rule", "b-.*", "added");
        instance.addExtraRule(ruleSetName, "initial.", "B-rule", "B-.*", "added");
        instance.addExtraRule(ruleSetName, "terminal.", "rule-B", ".*-B", "added");
        instance.addExtraRule(ruleSetName, "initial.", "bb-rule", "bb-.*", "inserted");
        instance.addExtraRule(ruleSetName, "", "bb-rule", "aabb-.*", "added");
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("prefix", null);
        options.put("source", "added");
        Iterable<String> foundRules = instance.saveRules(ruleSetName, options);
        int count = 0;
        for (String foundRule : foundRules) {
            count++;
            System.out.println("found: " + foundRule);
        }
        instance.resourceRepository.delete(new Resource("", "Test4.txt.ALL.added"));
        assertTrue(count == 4);

    }

    @Test
    public void testFindCaseById() throws Exception {
        System.out.println("findCaseById");
        String ruleSetName = "TestMongo.txt";
        String randomSessionId = Integer.toString(new Random().nextInt());
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"" + randomSessionId + "\"}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", new HashMap<String, String>());
        for (String aCase : cases0) {
            System.out.println(aCase);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        Iterable<String> cases1 = instance.findQueryCases(ruleSetName, "{ \"case.sessionId\" : \"" + randomSessionId + "\"}", options);
        String id = "";
        for (String aCase : cases1) {
            System.out.println(aCase);
            int idStart = aCase.indexOf("\"_id\" : \"") + 9;
            int idEnd = aCase.substring(idStart).indexOf("\" , \"") + idStart;
            id = aCase.substring(idStart, idEnd);
            System.out.println("id = " + id);
        }

//        String query = "{ \"_id\" : \"ObjectId(\\\""+id+"\\\")\"}";
//        String query = "{ \"_id\" : \"ObjectId('"+id+"')\"}";
        Iterable<String> cases2 = instance.findCaseById(ruleSetName, id, options);
        for (String aCase : cases2) {
            System.out.println(aCase);
        }
        //assertTrue(1==0);
        assertTrue(cases2.iterator().hasNext());
        System.out.println("findCases done");
    }

    @Test
    public void testFindCasesQueryWithTransformAggregation() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("transformer", "event.json");
        options.put("aggregator", "episode");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        System.out.println(cases0.size());
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0B", episodeMap.get("stateCodes"));
        }
        long future = new Date().getTime() + 1000000;
        options.put("timeOutReference", Long.toString(future));
        System.out.println("tor:" + Long.toString(future));
        Iterable<String> cases1 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases1) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0BX", episodeMap.get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithProcessorChain() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0BX", episodeMap.get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithProcessorChain2() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        System.out.println(cases0.size());
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("A0BX", ((Map<String, Object>) episodeMap.get("case")).get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithProcessorChain3() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"opera\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(cases0);
        assertTrue(cases0.size() == 1);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("?X", ((Map<String, Object>) episodeMap.get("case")).get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithProcessorChain4() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"opera\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}, {\"tagger\":\"TestMongoEpisode\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(cases0);
        assertTrue(cases0.size() == 1);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals(null, ((Map<String, Object>) episodeMap.get("case")).get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithProcessorChain5() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"opera\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        //options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}]");
        //options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}, {\"tagger\":\"TestMongoEpisode\"}]");
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}, {\"retagger\":\"TestMongoEpisode2\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(cases0);
        assertTrue(cases0.size() == 1);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertTrue(null!=((Map<String, Object>) episodeMap.get("case")).get("stateCodes"));
        }
    }

//    @Test
//    public void testFindCasesQueryWithProcessorChainIncrementor6() throws Exception {
//        System.out.println("findCases");
//        String ruleSetName = "TestMongoEvent";
//        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
//        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
//        Case yetAnotherCase = new MapCase("{\"browser\":\"opera\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\"}");
//        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
//        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
//        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
//        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
//        HashMap<String, String> options = new HashMap<String, String>();
//        options.put("owner", "_all");
//        long future = new Date().getTime() + 1000000;
//        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\"}, {\"tagger\":\"TestMongoEpisode\"}]");
//        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
//        System.out.println(cases0);
//        assertTrue(cases0.size() == 1);
//        for (String aCase : cases0) {
//            System.out.println(aCase);
//            Map episodeMap = JSONUtil.json2map(aCase);
//            assertEquals(null, ((Map<String, Object>) episodeMap.get("case")).get("stateCodes"));
//        }
//    }

    @Test
    public void testFindCasesQueryWithProcessorChainRevertResults() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\", \"userId\":\"ABC123\", \"accountId\":\"ABC123\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\", \"userId\":\"ABC123\", \"accountId\":\"ABC123\"}");
        Case yetAnotherCase = new MapCase("{\"browser\":\"opera\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"owner\":\"ABC123\", \"userId\":\"ABC123\", \"accountId\":\"ABC123\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        instance.assessCaseOpinion(ruleSetName, null, yetAnotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"transformer\":\"event.json\"},{\"filter\":\"{'browser':'opera'}\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}, {\"tagger\":\"TestMongoEpisode\", \"results\":\"revert\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(cases0);
        assertTrue(cases0.size() == 1);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
            assertEquals("?X", episodeMap.get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithRetrieverProcessorChain() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        options.put("ruleSet", ruleSetName);
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"retriever\":\"caseRetriever\"},{\"transformer\":\"event.json\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        System.out.println(cases0.size());
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
//            assertEquals("A0BX", episodeMap.get("stateCodes")); //todo fixthis
        }
    }

    @Test
    public void testFindCasesQueryWithRetrieverProcessorChain2() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"retriever\":\"caseRetriever\", \"ruleSet\":\"TestMongoEvent\"},{\"transformer\":\"event.json\"},{\"aggregator\":\"episode\", \"timeOutReference\":\"" + Long.toString(future) + "\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
//            assertEquals("A0BX", episodeMap.get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithSummaryProcessorChain() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processorChain", "[{\"aggregator\":\"summary\", \"summaryDefinition\":\"testSummaryDef.json\"}, {\"transformer\":\"TestSummaryXform.json\"}]");
        //options.put("processorChain", "[{\"aggregator\":\"summary\", \"summaryDefinition\":\"testSummaryDef.json\"}]");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        for (String aCase : cases0) {
            System.out.println(aCase);
            assertTrue(aCase.contains("\"duration\":3600000"));
        }
    }

    @Test
    public void testFindCasesQueryWithRetrieverProcessor() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
//            assertEquals("A0BX", episodeMap.get("stateCodes"));
        }
    }

    @Test
    public void testFindCasesQueryWithRetrieverProcessor2() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor_2.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() == 0);
    }


    @Test
    public void testFindCasesQueryWithRetrieverProcessor3() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor_3.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() == 0);
    }

    @Test
    public void testFindCasesQueryWithRetrieverProcessor4() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"declaredId\":\"alf@alpha.com\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        result = instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor_4.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        System.out.println(cases0.iterator().next().toString());
        assertTrue(cases0.size() > 0);
    }

   @Test
    public void testFindCasesQueryWithRetrieverProcessor5() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\", \"declaredId\":\"alf@alpha.com\", \"sub\":{\"subA\":\"A\", \"subB\":\"B\"}}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        result = instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor_5.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String casex : cases0){
            System.out.println(casex.toString());
        }
        assertTrue(cases0.size() > 0);
    }


    
    @Test
    public void testFindCasesQueryWithNestedProcessor() throws Exception {
        System.out.println("findCases");
        String ruleSetName = "TestMongoEvent";
        Case theCase = new MapCase("{\"browser\":\"firefox\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Case anotherCase = new MapCase("{\"browser\":\"chrome\", \"platform\":\"android\", \"sessionId\":\"AA11\"}");
        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        instance.assessCaseOpinion(ruleSetName, null, anotherCase);
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("owner", "_all");
        options.put("retriever", "none");
        long future = new Date().getTime() + 1000000;
        options.put("processor", "processor_nested.script");
        Collection<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        assertTrue(cases0.size() > 0);
        for (String aCase : cases0) {
            System.out.println(aCase);
            Map episodeMap = JSONUtil.json2map(aCase);
//            assertEquals("A0BX", episodeMap.get("stateCodes")); //todo fixthis!
        }
    }


    
    @Test
    public void testFindCasesMultipleQuery() throws Exception {
        System.out.println("findCasesMultiple");
        String ruleSetName = "Test1burst";
        Case theCase = new MapCase("{\"id\": \"123\", \"scripts\": [\"{async=false, defer=false, src=http://dev.echo-central.com/libraries.js, type=text/javascript}\",\"{async=true, defer=true, src=http://script.echo-central.com/wonderbar.js, type=text/javascript}\"]}");

        Oddball instance = new Oddball(resourceRepository, "TestBins.txt");
        HashMap<String, String> options = new HashMap<String, String>();
        Opinion result = instance.assessCaseOpinion(ruleSetName, null, theCase);
        System.out.println(result.getLabel());
        options.put("owner", "_all");
        Iterable<String> cases0 = instance.findQueryCases(ruleSetName, "{}", options);
        for (String aCase : cases0) {
            System.out.println(aCase);
            assertTrue(aCase.contains("Libraries") || aCase.contains("Wonderbar"));
        }
        // assertTrue(false);

    }

}
