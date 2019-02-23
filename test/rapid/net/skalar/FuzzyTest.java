// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.skalar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import rapid.net.TestBase;
import static org.junit.Assert.*;
import rapid.net.port.PortFactory;

public class FuzzyTest extends TestBase {

    private static final Logger LOG = LogManager.getLogger(FuzzyTest.class);

    public static final int MAX_VALUE = 100;

    public FuzzyTest() {
        super("FuzzySkalar");
    }

    public static void main(String[] args) {
        FuzzyTest test = new FuzzyTest();
        test.setUp();
        test.addTestAplusA();
        test.tearDown();
    }

    @Test
    public void fuzzy_to_oneHot2Test() {
        fuzzy_to_oneHotTest(2);
    }

    @Test
    public void fuzzy_to_oneHot3Test() {
        fuzzy_to_oneHotTest(3);
    }

    @Test
    public void fuzzy_to_oneHot4Test() {
        fuzzy_to_oneHotTest(4);
    }

    @Test
    public void fuzzy_to_oneHot5Test() {
        fuzzy_to_oneHotTest(5);
    }

    @Test
    public void fuzzy_to_oneHot6Test() {
        fuzzy_to_oneHotTest(6);
    }

    @Test
    public void fuzzy_to_oneHot7Test() {
        fuzzy_to_oneHotTest(7);
    }

    @Test
    public void fuzzy_to_oneHot8Test() {
        fuzzy_to_oneHotTest(8);
    }

    @Test
    public void fuzzy_to_oneHot10Test() {
        fuzzy_to_oneHotTest(10);
    }

    @Test
    public void fuzzy_to_oneHotMaxTest() {
        fuzzy_to_oneHotTest(MAX_VALUE);
    }

    protected void fuzzy_to_oneHotTest(int sectors) {
        name = "Fuzzy_to_OneHot" + sectors + "Test";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createFuzzy("In", MAX_VALUE, cycle));
        network.addOutput(PortFactory.createOneHot("Out", sectors - 1, cycle));

        // create test-patterns and result-putterns
        int[][] learnInputPattern = generateLinearPattern(0, sectors - 1, MAX_VALUE / (sectors - 1));
        int[][] learnOutputPattern = generateLinearPattern(0, sectors - 1, 1);

        int delta = (int) ((float) MAX_VALUE / (sectors - 1) / 3);
        int[][] verifyInputPattern = generateLinearThirdsPattern(0, sectors - 1, MAX_VALUE / (sectors - 1));
        int[][] verifyOutputPattern = generateLinearThirdsPattern(0, sectors - 1, 1);
        verifyOutputPattern[1][0] = 1;

        // perform the tests
        runTest_Start();
        runTest_Learn(learnInputPattern, learnOutputPattern);
        runTest_Verify(learnInputPattern, learnOutputPattern, false);
        if (delta > 0) {
            runTest_Verify(verifyInputPattern, verifyOutputPattern, false);
        }
        runTest_Optimize();
        runTest_Verify(learnInputPattern, learnOutputPattern, true);
        if (delta > 0) {
            runTest_Verify(verifyInputPattern, verifyOutputPattern, true);
        }
        runTest_Stop();
        network.toGraphML(name + ".graphml", false);

        assertEquals("Number of gates in the hidden-layer.", sectors - 1, network.getGates().size());

        LOG.debug("========== Finished: " + name + " ==========");
    }

    @Test
    public void assignTest1_0() {
        assignTest(MAX_VALUE);
    }

    // TODO: question if the charasteristic(curve) should really shift its peak-point
    @Test
    public void assignTest0_5() {
        assignTest(50);
    }

    @Test
    public void assignTest0_1() {
        assignTest(10);
    }

    @Test
    public void assignTest0_01() {
        assignTest(1);
    }

    protected void assignTest(int learnAtValue) {
        name = "FuzzyAssignTestAt" + learnAtValue;
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createFuzzy("In", MAX_VALUE, cycle));
        network.addOutput(PortFactory.createFuzzy("Out", MAX_VALUE, cycle));

        // create test-patterns and result-putterns
        int[][] learnInputPattern = new int[][]{{learnAtValue}};
        int[][] learnOutputPattern = new int[][]{{learnAtValue}};

        int[][] verifyInputPattern = generateLinearPattern(0, learnAtValue, 1);
        int[][] verifyOutputPattern = generateLinearPattern(0, learnAtValue, 1);

        // perform the tests
        runTest_Start();
        runTest_Learn(learnInputPattern, learnOutputPattern);
        runTest_Verify(learnInputPattern, learnOutputPattern, false);
        runTest_Verify(verifyInputPattern, verifyOutputPattern, false);
        runTest_Optimize();
        runTest_Verify(learnInputPattern, learnOutputPattern, true);
        runTest_Verify(verifyInputPattern, verifyOutputPattern, true);
        runTest_Stop();

        network.toGraphML(name + ".graphml", false);
        assertEquals("Number of gates in the hidden-layer.", 1, network.getGates().size());

        LOG.debug("========== Finished: " + name + " ==========");
    }

    @Test
    public void addTest() {
        addTest("", null, null);
    }

    @Test
    public void addTestAplus0() {   // A+0 = A
        int[][] verifyInputPattern0 = new int[MAX_VALUE + 1][2];
        generateLinearPattern(verifyInputPattern0, 0, MAX_VALUE, 1, 0);
        int[][] verifyOutputPattern = generateLinearPattern(0, MAX_VALUE, 1);

        addTest("A+0", verifyInputPattern0, verifyOutputPattern);
    }

    @Test
    public void addTest0plusA() {   // 0+A = A
        int[][] verifyInputPattern1 = new int[MAX_VALUE + 1][2];
        generateLinearPattern(verifyInputPattern1, 0, MAX_VALUE, 1, 1);
        int[][] verifyOutputPattern = generateLinearPattern(0, MAX_VALUE, 1);

        addTest("0+A", verifyInputPattern1, verifyOutputPattern);
    }

    @Test
    public void addTestAplusA() {   // A+A = 2*A
        int[][] verifyInputPattern2 = new int[MAX_VALUE + 1][2];
        generateLinearPattern(verifyInputPattern2, 0, MAX_VALUE, 1, 0);
        generateLinearPattern(verifyInputPattern2, 0, MAX_VALUE, 1, 1);
        int[][] verifyOutputPattern2 = generateLinearPattern(0, MAX_VALUE, 2);

        addTest("A+A", verifyInputPattern2, verifyOutputPattern2);
    }

    @Test
    public void addTestAplusInvA() {    // A+(1-A) = 1
        int[][] verifyInputPattern2 = new int[MAX_VALUE + 1][2];
        generateLinearPattern(verifyInputPattern2, 0, MAX_VALUE, 1, 0);
        generateLinearPattern(verifyInputPattern2, MAX_VALUE, 0, 1, 1);
        int[][] verifyOutputPattern2 = generateConstantPattern(0, MAX_VALUE, MAX_VALUE);

        addTest("A-A", verifyInputPattern2, verifyOutputPattern2);
    }

    protected void addTest(String name2, int[][] verifyInputPattern, int[][] verifyOutputPattern) {
        this.name = "FuzzyAddTest" + name2;
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createFuzzy("InA", MAX_VALUE, cycle));
        network.addInput(PortFactory.createFuzzy("InB", MAX_VALUE, cycle));
        network.addOutput(PortFactory.createFuzzy("Out", MAX_VALUE, cycle));

        // create test-patterns and result-putterns
        int[][] learnInputPattern = new int[][]{
            {0, MAX_VALUE}, {MAX_VALUE, 0} /*,  {MAX_VALUE,MAX_VALUE}*/};
        int[][] learnOutputPattern = new int[][]{
            {MAX_VALUE}, {MAX_VALUE} /*,    {2*MAX_VALUE}*/};

        // perform the tests
        runTest_Start();
        runTest_Learn(learnInputPattern, learnOutputPattern);
        if (verifyInputPattern == null || verifyOutputPattern == null) {
            runTest_Verify(learnInputPattern, learnOutputPattern, false);
        } else {
            runTest_Verify(verifyInputPattern, verifyOutputPattern, false);
        }
        runTest_Optimize();
        if (verifyInputPattern == null || verifyOutputPattern == null) {
            runTest_Verify(learnInputPattern, learnOutputPattern, true);
        } else {
            runTest_Verify(verifyInputPattern, verifyOutputPattern, true);     // A+0 = A
        }
        runTest_Stop();

        network.toGraphML(name + ".graphml", false);
        assertEquals("Number of gates in the hidden-layer.", 2, network.getGates().size());

        LOG.debug("========== Finished: " + name + " ==========");
    }

    @Test
    public void commonANDTest() {
        this.name = "FuzzyCommonANDTest";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createFuzzy("In1", 1, cycle));
        network.addInput(PortFactory.createFuzzy("In2", 1, cycle));
        network.addInput(PortFactory.createFuzzy("In3", 1, cycle));
        network.addInput(PortFactory.createFuzzy("In4", 1, cycle));
        network.addOutput(PortFactory.createOneHot("Out", 1, cycle));

        // create test-patterns and result-putterns
        int[][] learnInputPattern = new int[][]{{1, 1, 1, 0}, {0, 1, 1, 0}, {0, 1, 1, 1}};
        int[][] learnOutputPattern = new int[][]{{1}, {0}, {1}};

        // perform the tests
        runTest_Start();
        runTest_Learn(learnInputPattern, learnOutputPattern);
        runTest_Verify(learnInputPattern, learnOutputPattern, false);
        runTest_Optimize();
        runTest_Verify(learnInputPattern, learnOutputPattern, true);
        runTest_Stop();

        network.toGraphML(name + ".graphml", false);
        // TODO assertEquals("Number of gates in the hidden-layer.", 3, network.getGates().size());

        LOG.debug("========== Finished: " + name + " ==========");
    }

}
