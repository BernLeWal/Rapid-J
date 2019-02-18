// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import rapid.net.port.PortFactory;

public class BoxingGameTest extends TestBase {

    private static final Logger LOG = LogManager.getLogger(BoxingGameTest.class);

    public BoxingGameTest() {
        super("BoxingGame");
    }

    @Test
    public void boxingRulesTest() {
        name = "BoxingRules";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();
        network.addInput(PortFactory.createOneHot("InPos", 2, cycle));
        network.addInput(PortFactory.createOneHot("InAct", 1, cycle));
        network.addOutput(PortFactory.createOneHot("OutPos", 2, cycle));
        network.addOutput(PortFactory.createOneHot("OutAct", 1, cycle));

        // create test-patterns and result-putterns
        int[][] inputPattern = new int[][]{
            {0, 0}, {0, 1}, {1, 0}, {1, 1}, {2, 0}, {2, 1},
            {0, 0}, {1, 0}, {2, 0} // this line contains multiple-correct results
        };
        int[][] outputPattern = new int[][]{
            {1, 1}, {0, 0}, {0, 1}, {1, 0}, {0, 1}, {2, 0},
            {2, 1}, {2, 1}, {1, 1} // this line contains multiple-correct results
        };

        runTest(inputPattern, outputPattern, (in, out) -> {
            return checkRules(in, out);
        });
        assertEquals("Number of gates in the hidden-layer.", 11, network.gates.size());

        LOG.debug("========== Finished: " + name + " ==========");
    }

    public boolean checkRules(Integer[] input, Integer[] output) {
        if (input[1] == 0 && output[1] == 1) {
            return (!Objects.equals(input[0], output[0]));
        } else if (input[1] == 1 && output[1] == 0) {
            return (Objects.equals(input[0], output[0]));
        } else {
            return false;
        }
    }

}
