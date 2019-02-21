// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

/**
 * Factory-Class to create the nodes for inputs and outputs to the neural network.
 * Remarks: These nodes are called "Port". Different types of ports are existing.
 * @author Bernhard
 */
public class PortFactory {

    // static factory methods:
    
    /**
     * Creates an instance of a OneHotPort usable as input- or output-node in the neural network.
     * One-Hot means that form a amount of possibilities (represented by numbers 0 to max), 
     * one possibility is the "hot" result. It is a node for classification/catogrization.
     * @param name the name of the node
     * @param max number of possibilities, from 1 to max. (it is 0 when none of the possibilities is "hot")
     * @param cycle stores the cycle (timestamp) of creation, just for monitoring purposes 
     *              read cycle from network.getCycles()
     * @return the new instance of the OneHotPort
     */
    public static OneHotPort createOneHot(String name, int max, int cycle) {
        return new OneHotPort(name, max, cycle);
    }

    /**
     * Creates an instance of a MapToOneHotPort usable as input- or output-node in a neural network.
     * One-Hot means that form a amount of possibilities one possibility is the "hot" result. 
     * It is a node for classification/catogrization.
     
     * Every possibilities is represented by a name which is mapped to a number (see OneHotPort),
     * which have to be initialized after createion of the MapToOneHotPort-instance 
     * by using .createItem(..) method.
     * 
     * @param name the name of the node
     * @return the new instance of the MapToOneHotPort
     */
    public static MapToOneHotPort createMapToOneHot(String name) {
        return new MapToOneHotPort(name);
    }

    /**
     * Creates an instance of a FuzzyPort usable as input- or output-node in a neural network.
     * Fuzzy means that the value is not only true (max) or false (0) but also anything between.
     * @param name the name of the node
     * @param max the maximum value for this node
     * @param cycle stores the cycle (timestamp) of creation, just for monitoring purposes 
     *              read cycle from network.getCycles()
     * @return the new instance of the FuzzyPort
     */
    public static FuzzyPort createFuzzy(String name, int max, int cycle) {
        return new FuzzyPort(name, max, cycle);
    }
    
    /**
     * Creates an instance of a Vector (one-dimensional array) of Fuzzy-Values
     * usable as input- or output-node in a neural network.
     * Fuzzy means that the value is not only true (max) or false (0) but also anything between.
     * @param name the name of the node
     * @param size the size of the vector (the number of elements in the 1-D array)
     * @param max the maximum value per element
     * @param cycle stores the cycle (timestamp) of creation, just for monitoring purposes 
     *              read cycle from network.getCycles()
     * @return the new instance of the FuzzyVector
     */
    public static FuzzyVector createVector(String name, int size, int max, int cycle) {
        return new FuzzyVector(name, size, max, cycle);
    }

        /**
     * Creates an instance of a Matrix (two-dimensional array) of Fuzzy-Values
     * usable as input- or output-node in a neural network.
     * Fuzzy means that the value is not only true (max) or false (0) but also anything between.
     * @param name the name of the node
     * @param cols the number of columns of the matrix
     * @param rows the number of rows of the matrix
     * @param max the maximum value per element
     * @param cycle stores the cycle (timestamp) of creation, just for monitoring purposes 
     *              read cycle from network.getCycles()
     * @return the new instance of the FuzzyVector
     */
    public static FuzzyMatrix createMatrix(String name, int cols, int rows, int max, int cycle) {
        return new FuzzyMatrix(name, cols, rows, max, cycle);
    }
    
    /**
     * EXPERIMENTAL (currently deprecated)
     * Binary-Ports represent the possibilities via bit-masks, that is by using of only
     * 8 nodes it could represent 256 possibilities which leads to a big decrease of
     * the number of nodes in the network.
     * 
     * @param name the name of the node
     * @param bits the number of bits needed for representing the nodes
     * @param cycle stores the cycle (timestamp) of creation, just for monitoring purposes 
     *              read cycle from network.getCycles()
     * @return the new instance of the BinaryPort
     * @deprecated TODO: To implement this the concept of the calculations would have to be
     *              extended to implement unstetic-functions for resulting to a bit-mask.
     */
    @Deprecated
    public static BinaryPort createBinary(String name, int bits, int cycle) {
        return new BinaryPort("b" + name, bits, cycle);
    }

    /**
     * Creates an instance of a port which accepts a stream of ports (-values).
     * Stream means, that the time (cycle) information of the data is important, too.
     * @param name the name of the node
     * @param prototype the prototype for the child-node, the nodes of which the streams consist.
     * @return the new instance of the PortStream
     */
    public static PortStream createStream(String name, Portable[] prototype) {
        return new PortStream(name, prototype);
    }
    
    public static PortGroup createGroup(String name, Portable[] childPorts) {
        return new PortGroup(name, childPorts);
    }



    /**
     * Create new instance by a prototype depending on the prototype-class.
     * @param name
     * @param prototype
     * @param cycle
     * @return 
     */
    public static Portable createByPrototype(String name, Portable prototype, int cycle) {
        if (prototype instanceof OneHotPort) {
            return new OneHotPort(name, (OneHotPort) prototype, cycle);
        } else if (prototype instanceof MapToOneHotPort) {
            return new MapToOneHotPort(name, (MapToOneHotPort) prototype);
        } else if (prototype instanceof FuzzyPort) {
            return new FuzzyPort(name, (FuzzyPort) prototype, cycle);
        } else if (prototype instanceof BinaryPort) {
            return new BinaryPort(name, (BinaryPort) prototype, cycle);
        }
        return null;
    }
}
