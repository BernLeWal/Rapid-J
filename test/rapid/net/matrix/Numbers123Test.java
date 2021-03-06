// License: Apache 2.0. See LICENSE file in root directory.
/**
 * HOWTO - Build and run your first neuronal-network
 * Whole source is implemented as NUnit Test-Class.
 * Follow the steps marked by "STEP XXX:" !
 */
package rapid.net.matrix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import rapid.net.TestBase;
import rapid.net.port.PortFactory;

// STEP 1: create a new class and derive it from TestBase
/**
 * This sample class introduces what do do to implement a simple neural-network
 * which does a image recognition of the number 1, 2 or 3.
 *
 * @author Bernhard
 */
public class Numbers123Test extends TestBase {

    private static final Logger LOG = LogManager.getLogger(Numbers123Test.class);

    // STEP 2: pass a name for your test-class to the constructor of TestBase.
    /**
     * Create a new test-case.
     */
    public Numbers123Test() {
        super("Numbers123");
    }

    // STEP 3: Define learn- and test-data
    /**
     * Data for learning (the numbers 1,2 and 3) 
     * and testing (here just 3 forms of the number 1).
     */
    private static final int[][] LEARN_DATA_IN = {
        {0, 0, 1, 0, // 4x5 bitmap of the       X 
         0, 1, 1, 0, // number "1"             XX
         1, 0, 1, 0, //                       X X
         0, 0, 1, 0, //                         X
         0, 0, 1, 0}, //                        X

        {0, 1, 1, 0, // 4x5 bitmap of the      XX 
         1, 0, 0, 1, // number "2"            X  X
         0, 0, 1, 0, //                         X
         0, 1, 0, 0, //                        X 
         1, 1, 1, 1}, //                      XXXX

        {0, 1, 1, 0, // 4x5 bitmap of the      XX 
         1, 0, 0, 1, // number "3"            X  X
         0, 0, 1, 1, //                         XX
         1, 0, 0, 1, //                       X  X
         0, 1, 1, 0}, //                       XX 
    };
    private static final int[][] LEARN_DATA_OUT = {
        {1},    // first learn-pattern is a "1"
        {2},    // second learn-pattern is a "2"
        {3}     // third learn-pattern is a "3"
    };

    private static final int[][] TEST_DATA_IN = {
        {0, 0, 1, 0, // 4x5 bitmap of the       X 
         0, 1, 1, 0, // number "1"             XX
         1, 0, 1, 0, // (the original)        X X
         0, 0, 1, 0, //                         X
         0, 0, 1, 0}, //                        X

        {0, 0, 1, 0, // 4x5 bitmap of the       X 
         0, 1, 1, 0, // number "1"             XX
         0, 0, 1, 0, // (smaller than orig)     X
         0, 0, 1, 0, //                         X
         0, 0, 0, 0}, //                         

        {0, 0, 1, 0, // 4x5 bitmap of the       X 
         0, 0, 1, 0, // number "1"              X
         0, 0, 1, 0, // (a missing line)        X
         0, 0, 1, 0, //                         X
         0, 0, 1, 0}, //                        X
    };
    private static final int[][] TEST_DATA_OUT = {
        {1},    // first learn-pattern is a "1"
        {1},    // second learn-pattern is a "1"
        {1}     // third learn-pattern is a "1"
    };
    
    // STEP 4: create the first test-method as follows
    /**
     * A test-method to build and run the neural-network and evaluate the
     * results.
     */
    @Test
    public void numbers123Recognition() {
        name = "Numbers123Rec";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        // STEP 5: create the input- and output-nodes of the network, called ports.
        int cycle = network.getCycles();    // returns the current "timestamp", to show when the nodes where created
        network.addInput( PortFactory.createMatrix("In", 4, 5, 1, cycle) );
        network.addOutput( PortFactory.createOneHot("Out", 3, cycle) );
        
        // STEP 6: apply the patterns to learn, run and verify the tests
        name = "Numbers123Rec-LEARN";
        runTest( LEARN_DATA_IN, LEARN_DATA_OUT );
        
        // STEP 7: apply the patterns to verify if the network finds the real results on test data
        name = "Numbers123Rec-TEST";
        runVerify( TEST_DATA_IN, TEST_DATA_OUT );
        
        // export the neural-network to GraphML to show in a viewer, f.e. Gephi
        network.toGraphML(name + ".graphml", true);
        
        LOG.debug("========== Finished: " + name + " ==========");
    }
    
    @Test 
    public void numbers123LearnAll() {
        name = "Numbers123LearnAll";
        LOG.info("========== Started: " + name + " ==========");

        LOG.debug("---------- PHASE 0: Initialisation ---------- ");
        int cycle = network.getCycles();    
        network.addInput( PortFactory.createMatrix("In", 4, 5, 1, cycle) );
        network.addOutput( PortFactory.createOneHot("Out", 3, cycle) );
        
        runTest( LEARN_DATA_IN, LEARN_DATA_OUT );
        runTest( TEST_DATA_IN, TEST_DATA_OUT );
        
        network.toGraphML(name + ".graphml", true);
        LOG.debug("========== Finished: " + name + " ==========");
    }
}
