package rapid.net;

import java.util.function.BiPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import rapid.util.Utils;

public abstract class TestBase {

    private static final Logger LOG = LogManager.getLogger(TestBase.class);

    protected String name;

    protected Network network;

    private long startMillis;
    private int successCount = 0;
    private int failCount = 0;

    protected TestBase(String name) {
        this.name = name;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        network = new Network(this.getClass().getSimpleName());
    }

    @After
    public void tearDown() {
    }

    protected boolean runTest(int[][] inputPattern, int[][] outputPattern) {
        runTest_Start();
        runTest_Learn(inputPattern, outputPattern);
        runTest_Verify(inputPattern, outputPattern, false);
        runTest_Optimize();
        runTest_Verify(inputPattern, outputPattern, true);
        return runTest_Stop();
    }

    protected boolean runTest(int[][] inputPattern, int[][] outputPattern, BiPredicate<Integer[], Integer[]> verifyFunc) {
        runTest_Start();
        runTest_Learn(inputPattern, outputPattern);
        runTest_Verify(inputPattern, verifyFunc, null, false);
        runTest_Optimize();
        runTest_Verify(inputPattern, verifyFunc, outputPattern, true);
        return runTest_Stop();
    }

    protected void runTest_Start() {
        startMillis = System.currentTimeMillis();
    }

    protected void runTest_Learn(int[][] inputPattern, int[][] outputPattern) {
        LOG.debug("---------- PHASE 1: Learning ---------- ");
        for (int i = 0; i < inputPattern.length; i++) {
            network.learn(inputPattern[i], outputPattern[i], null, false);
        }
        LOG.info(name + ": learned   " + network.toString() + "\n" + network.dumpNetworkToString(false));
    }

    protected void runTest_Verify(int[][] inputPattern, int[][] outputPattern, boolean assertIfFailed) {
        LOG.debug("---------- PHASE " + (assertIfFailed ? "4" : "2") + ": Verification ---------- ");
        for (int i = 0; i < inputPattern.length; i++) {
            if (network.verify(inputPattern[i], outputPattern[i], null)) {
                successCount++;
            } else {
                failCount++;
                if (assertIfFailed) {
                    assertTrue("Input is " + Utils.intArrayToString(inputPattern[i]) + ", Output should be " + Utils.intArrayToString(outputPattern[i]) + " but is " + Utils.intArrayToString(network.getOutputValues()), false);
                }
            }
        }
    }

    protected void runTest_Verify(int[][] inputPattern, BiPredicate<Integer[], Integer[]> verifyFunc, int[][] outputPattern, boolean assertIfFailed) {
        LOG.debug("---------- PHASE " + (assertIfFailed ? "4" : "2") + ": Verification ---------- ");
        for (int i = 0; i < inputPattern.length; i++) {
            if (network.verify(inputPattern[i], null, verifyFunc)) {
                successCount++;
            } else {
                failCount++;
                if (assertIfFailed) {
                    assertTrue("Verification of learned data. Input is " + Utils.intArrayToString(inputPattern[i]) + " Output should be " + Utils.intArrayToString(outputPattern[i]) + " but is " + Utils.intArrayToString(network.getOutputValues()), false);
                }
            }
        }
    }

    protected void runTest_Optimize() {
        LOG.debug("---------- PHASE 3: Optimizing ---------- ");
        network.optimizeAll();
        LOG.info(name + ": optimized " + network.toString() + "\n" + network.dumpNetworkToString(false));
    }

    protected boolean runTest_Stop() {
        long stopMillis = System.currentTimeMillis();
        LOG.info(network.toString());
        LOG.info(name + ": Test " + ((0 == failCount) ? "OK" : "FAILED") + " succeeded=" + successCount + " failed=" + failCount + " duration=" + (stopMillis - startMillis) + " msec.");
        assertEquals(name + ": Tests succeeded=" + successCount + " failed=" + failCount + " duration=" + (stopMillis - startMillis) + " msec.", 0, failCount);
        return failCount == 0;
    }

    public static int[][] generateConstantPattern(int from, int to, int value) {
        int[][] pattern = new int[to - from + 1][1];
        for (int index = from; index <= to; index++) {
            pattern[index][0] = value;
        }
        return pattern;
    }

    public static int[][] generateLinearPattern(int from, int to, int factor) {
        int[][] pattern = new int[to - from + 1][1];
        generateLinearPattern(pattern, from, to, factor, 0);
        return pattern;
    }

    public static void generateLinearPattern(int[][] pattern, int from, int to, int factor, int dimensionIndex) {
        if (from <= to) {
            for (int index = from; index <= to; index++) {
                pattern[index][dimensionIndex] = index * factor;
            }
        } else {
            for (int index = from; index >= to; index--) {
                pattern[from-index][dimensionIndex] = index * factor;
            }
        }
    }

    public static int[][] generateLinearThirdsPattern(int from, int to, int factor) {
        int delta1 = factor / 3;
        int delta2 = (int) ((2.0f * factor) / 3.0f + 0.5f);
        int[][] pattern = new int[(to - from + 1) * 3][1];
        for (int index = from; index < to; index++) {
            pattern[index * 3][0] = index * factor;
            pattern[index * 3 + 1][0] = index * factor + delta1;
            pattern[index * 3 + 2][0] = index * factor + delta2;
        }
        pattern[pattern.length - 1][0] = (to - from) * factor;
        return pattern;
    }
}
