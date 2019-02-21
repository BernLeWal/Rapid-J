// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Vector (one-dimensional array) of Fuzzy-Values
 * usable as input- or output-node in a neural network.
 * Fuzzy means that the value is not only true (max) or false (0) but also anything between.
 * @author Bernhard
 */
public class FuzzyVector extends PortGroup {

    private static final Logger LOG = LogManager.getLogger(FuzzyVector.class);

    public FuzzyVector(String name, int size, int max, int cycle) {
        super(name, null);

        for (int i = 0; i < size; i++) {
            children.add(new FuzzyPort(name + String.valueOf(i), max, cycle));
        }
    }
}
