// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import rapid.net.TestBase;
import rapid.net.port.PortFactory;

/**
 *
 * @author Bernhard
 */
public class VectorTest extends TestBase {

    private static final Logger LOG = LogManager.getLogger(VectorTest.class);

    public static final int MAX_VALUE = 100;
    public static final int MAX_SIZE = 100;

    public VectorTest() {
        super("FuzzyVector");
    }

    @Test
    public void lineTest2() {
        lineTest(2);
    }

    @Test
    public void lineTest4() {
        lineTest(4);
    }

    @Test
    public void lineTest8() {
        lineTest(8);
    }

    @Test
    public void lineTest10() {
        lineTest(10);
    }
    
    @Test
    public void lineTestMax() {
        lineTest(MAX_VALUE);
    }

    protected void lineTest(int sectors) {
        name = "Vector_to_OneHot" + sectors + "Test";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createVector("In", MAX_SIZE, MAX_VALUE, cycle));
        network.addOutput(PortFactory.createOneHot("Out", sectors + 2, cycle));

        // create test-patterns and result-putterns
        int[][] learnInputPattern = new int[sectors + 1 + 2][MAX_SIZE];
        int[][] learnOutputPattern = new int[sectors + 1 + 2][1];
        // generate constant values
        for (int i = 0; i <= sectors; i++) {
            for (int j = 0; j < MAX_SIZE; j++) {
                learnInputPattern[i][j] = MAX_VALUE / sectors * i;
            }
            learnOutputPattern[i][0] = i;
        }
        // generate increasing and decreasing values
        for (int j = 0; j < MAX_SIZE; j++) {
            learnInputPattern[sectors + 1][j] = j * MAX_VALUE / MAX_SIZE;
            learnInputPattern[sectors + 2][j] = (MAX_SIZE - j) * MAX_VALUE / MAX_SIZE;
        }
        learnOutputPattern[sectors + 1][0] = sectors + 1;
        learnOutputPattern[sectors + 2][0] = sectors + 2;

        // perform the tests
        runTest(learnInputPattern, learnOutputPattern);
        network.toGraphML(name + ".graphml", false);

        //assertEquals("Number of gates in the hidden-layer.", sectors - 1, network.getGates().size());
        LOG.debug("========== Finished: " + name + " ==========");
    }

}
