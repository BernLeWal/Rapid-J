// License: Apache 2.0. See LICENSE file in root directory.
/**
 * EXPERIMENTAL - (currently DEPRECATED)
 * The idea was to use Binary-Ports instead of One-Hot to reduce the amount of necessary
 * output nodes, f.e. 256 for a 8-Bit number.
 * 
 * TODO: To implement this the concept of the calculations would have to be
 * extended to implement unstetic-functions for resulting to a bit-mask.
 */
package rapid.net.skalar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import rapid.net.Layer;
import rapid.net.TestBase;
import rapid.net.port.PortFactory;

public class BinaryBitCountersTest extends TestBase {

    private static final Logger LOG = LogManager.getLogger(BinaryBitCountersTest.class);

    public BinaryBitCountersTest() {
        super("BinaryBitCounter");
    }

    public static void main(String[] args) {
        BinaryBitCountersTest test = new BinaryBitCountersTest();
        test.setUp();
        test.test4BitCounting2Bin();
        test.tearDown();
    }

    @Test
    public void test1BitCounting() {
        bitCountingTest(1, network.addInput(PortFactory.createBinary("In", 1, network.getCycles())), false);
    }

    @Test
    public void test2BitCounting() {
        bitCountingTest(2, network.addInput(PortFactory.createBinary("In", 2, network.getCycles())), false);
    }

    @Test
    public void test3BitCounting() {
        bitCountingTest(3, network.addInput(PortFactory.createBinary("In", 3, network.getCycles())), false);
    }

    @Test
    public void test4BitCounting() {
        bitCountingTest(4, network.addInput(PortFactory.createBinary("In", 4, network.getCycles())), false);
    }

    @Test
    public void test5BitCounting() {
        bitCountingTest(5, network.addInput(PortFactory.createBinary("In", 5, network.getCycles())), false);
    }

    @Test
    public void test6BitCounting() {
        bitCountingTest(6, network.addInput(PortFactory.createBinary("In", 6, network.getCycles())), false);
    }

    @Test
    public void test7BitCounting() {
        bitCountingTest(7, network.addInput(PortFactory.createBinary("In", 7, network.getCycles())), false);
    }

    @Test
    public void test8BitCounting() {
        bitCountingTest(8, network.addInput(PortFactory.createBinary("In", 8, network.getCycles())), false);
    }

    @Test
    public void test1BitCounting2Bin() {
        bitCountingTest(1, network.addInput(PortFactory.createBinary("In", 1, network.getCycles())), true);
    }

    @Test
    public void test2BitCounting2Bin() {
        bitCountingTest(2, network.addInput(PortFactory.createBinary("In", 2, network.getCycles())), true);
    }

    @Test
    public void test3BitCounting2Bin() {
        bitCountingTest(3, network.addInput(PortFactory.createBinary("In", 3, network.getCycles())), true);
    }

    @Test
    public void test4BitCounting2Bin() {
        bitCountingTest(4, network.addInput(PortFactory.createBinary("In", 4, network.getCycles())), true);
    }

    @Test
    public void test5BitCounting2Bin() {
        bitCountingTest(5, network.addInput(PortFactory.createBinary("In", 5, network.getCycles())), true);
    }

    @Test
    public void test6BitCounting2Bin() {
        bitCountingTest(6, network.addInput(PortFactory.createBinary("In", 6, network.getCycles())), true);
    }

    @Test
    public void test7BitCounting2Bin() {
        bitCountingTest(7, network.addInput(PortFactory.createBinary("In", 7, network.getCycles())), true);
    }

    @Test
    public void test8BitCounting2Bin() {
        bitCountingTest(8, network.addInput(PortFactory.createBinary("In", 8, network.getCycles())), true);
    }

    private void bitCountingTest(int bits, Layer input, boolean binaryOut) {
        name = ("Binary" + bits + "BitCounter" + (binaryOut ? "BinaryOut" : ""));
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int values = 1 << bits;
        if (binaryOut) {
            network.addOutput(PortFactory.createBinary("Out", bits, network.getCycles()));  // number of "1"-Bits
        } else {
            network.addOutput(PortFactory.createOneHot("Out", bits, network.getCycles()));  // number of "1"-Bits
        }
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

        LOG.debug("========== Finished: " + name + " ==========");
    }
}
