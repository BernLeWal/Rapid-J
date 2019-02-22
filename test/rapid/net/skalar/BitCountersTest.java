// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.skalar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import rapid.net.Layer;
import rapid.net.TestBase;
import rapid.net.port.PortFactory;

public class BitCountersTest extends TestBase {

    private static final Logger LOG = LogManager.getLogger(BitCountersTest.class);

    public BitCountersTest() {
        super("BitCounter");
    }

    public static void main(String[] args) {
        BitCountersTest test = new BitCountersTest();
        test.setUp();
        test.test4BitCounting();
        test.tearDown();
    }
    
    @Test
    public void test1BitCounting() {
        bitCountingTest(1, network.addInput(PortFactory.createOneHot("In", (1 << 1) - 1, network.getCycles())));  // Number consisting of n Bits (0..values-1)
    }

    @Test
    public void test2BitCounting() {
        bitCountingTest(2, network.addInput(PortFactory.createOneHot("In", (1 << 2) - 1, network.getCycles())));
    }

    @Test
    public void test3BitCounting() {
        bitCountingTest(3, network.addInput(PortFactory.createOneHot("In", (1 << 3) - 1, network.getCycles())));
    }

    @Test
    public void test4BitCounting() {
        bitCountingTest(4, network.addInput(PortFactory.createOneHot("In", (1 << 4) - 1, network.getCycles())));
    }

    @Test
    public void test5BitCounting() {
        bitCountingTest(5, network.addInput(PortFactory.createOneHot("In", (1 << 5) - 1, network.getCycles())));
    }

    @Test
    public void test6BitCounting() {
        bitCountingTest(6, network.addInput(PortFactory.createOneHot("In", (1 << 6) - 1, network.getCycles())));
    }

    @Test
    public void test7BitCounting() {
        bitCountingTest(7, network.addInput(PortFactory.createOneHot("In", (1 << 7) - 1, network.getCycles())));
    }

    @Test
    public void test8BitCounting() {
        bitCountingTest(8, network.addInput(PortFactory.createOneHot("In", (1 << 8) - 1, network.getCycles())));    // Number consisting of n Bits (0..values-1)
    }

    private void bitCountingTest(int bits, Layer input) {
        name = (bits + "BitCounter");
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int values = 1 << bits;
        network.addOutput(PortFactory.createOneHot("Out", bits, network.getCycles()));  // number of "1"-Bits

        // create test-patterns and result-putterns
        int[][] inputPattern = new int[values][1]; //{{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}};
        int[][] outputPattern = new int[values][1]; //{{0}, {1}, {1}, {2}, {1}, {2}, {2}, {3}};
        for (int i = 0; i < values; i++) {
            inputPattern[i][0] = i;
            int highBitCount = 0;
            int j = i;
            while (j > 0) {
                if ((j & 1) == 1) {
                    highBitCount++;
                }
                j = j >> 1;
            }
            outputPattern[i][0] = highBitCount;
        }

        runTest(inputPattern, outputPattern);
        network.toGraphML("logs/" + name + ".graphml", false);

        LOG.debug("========== Finished: " + name + " ==========");
    }
}
