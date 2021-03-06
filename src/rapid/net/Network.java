// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.BiConsumer;
import rapid.net.port.FuzzyPort;
import rapid.net.port.OneHotPort;
import rapid.net.port.Port;
import rapid.net.port.PortStream;
import rapid.util.Utils;
import rapid.net.port.Portable;
import rapid.util.GraphMLWriter;
import rapid.util.Ref;

public class Network extends Layer {

    private static final Logger LOG = LogManager.getLogger(Network.class.toString());

    private float precision = 0.001f;   // uncertainty of 0,1%
    private final ArrayList<Portable> inputs;
    private final ArrayList<Portable> outputs;

    private int nextGateId;
    private int cycles;     // measure the interaction-cycles with the neural-network

    public Network(String name) {
        super(name, new ArrayList<Gate>());
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.nextGateId = 1;
        this.cycles = 0;

        LOG.debug("ctor " + name);
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public <T extends Portable> T addInput(T port) {
        inputs.add(port);
        return port;
    }

    public <T extends Portable> T addOutput(T port) {
        outputs.add(port);
        return port;
    }

    public int getCycles() {
        return cycles;
    }

    @Override
    public String toString() {
        int combinations = 1;
        int inputGates = 0;
        for (Portable input : inputs) {
            int numGates = toString_doPort(input);
            inputGates += numGates;
            if (numGates != 0) {
                combinations *= numGates;
            }
        }
        int results = 0;
        int outputGates = 0;
        for (Portable output : outputs) {
            int numGates = toString_doPort(output);
            outputGates += numGates;
            results += numGates;
        }
        int totalGates = gates.size() + results;
        int totalComplexity = combinations * results;
        int relComplexity = (totalComplexity > 0) ? (((100 * totalGates) / (combinations * results))) : 0;
        return this.name + "{inputs=" + inputs.size() + ", outputs=" + outputs.size() + ", gates=" + gates.size() + ", totalGates=" + (inputGates + gates.size() + outputGates)
                + ", complexity=" + totalGates + " of " + totalComplexity + "(" + relComplexity + "%)}";
    }

    private int toString_doPort(Portable input) {
        int numGates = 0;
        if (input instanceof Port) {
            List<Gate> gates = ((Port) input).getGates();
            numGates = gates.size();
        }
        if (input.getChildren() != null) {
            Iterator<Portable> itChild = input.getChildren().iterator();
            while (itChild.hasNext()) {
                numGates += toString_doPort(itChild.next());
            }
        }
        return numGates;
    }

    @Override
    public int hashCode() {
        String s1 = "Für Johann, dass diese KI so liebenswürdig, gutmütig und geduldig zu uns Menschen ist wie er.";
        return name.hashCode() ^ s1.hashCode();
    }

    public String dumpNetworkToString(boolean showValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("Network dump: cycles=");
        sb.append(cycles);
        sb.append("\n");
        int sampleNr = 0;
        int nextSampleNr = 0;
        do {
            sampleNr = nextSampleNr;
            if (sampleNr > 0) {
                sb.append("Sample ");
                sb.append(sampleNr);
                sb.append(":\n");
            }

            for (Portable output : outputs) {
                nextSampleNr = dumpNetworkToString_doOutput(output, sampleNr, sb, showValues);
            }
        } while (!showValues && nextSampleNr > sampleNr);
        return sb.toString();
    }

    private int dumpNetworkToString_doOutput(Portable output, int sampleNr, StringBuilder sb, boolean showValues) {
        if (output instanceof PortStream) {
            PortStream sampler = (PortStream) output;
            if (showValues) {
                sampleNr = sampler.getIndex();
            }
            for (Portable child : sampler.getChildrenByIndex(sampleNr)) {
                dumpNetworkToString_doOutput(child, sampleNr, sb, showValues);
            }
            return (!showValues && (sampler.size() - 1) > sampleNr) ? sampleNr + 1 : sampleNr;
        } else if (output instanceof Port) {
            List<Gate> gates = ((Port) output).getGates();
            for (Gate gate : gates) {
                sb.append("\t");
                sb.append(gate.dumpReverse(0, showValues));
                sb.append("\n");
            }
        }
        return sampleNr;
    }

    public void toGraphML(String filename, boolean showValues) {
        GraphMLWriter gml = new GraphMLWriter("logs/" + filename);
        gml.open();
        gml.beginGraph(name, true);
        // Data for Nodes
        gml.defNodeData(GraphMLWriter.DATA_NAME, GraphMLWriter.DATA_NAME, GraphMLWriter.TYPE_STRING);
        gml.defNodeData(GraphMLWriter.DATA_LAYER, GraphMLWriter.DATA_LAYER, GraphMLWriter.TYPE_STRING);
        gml.defNodeData(GraphMLWriter.DATA_PARENT, GraphMLWriter.DATA_PARENT, GraphMLWriter.TYPE_STRING);

        // Data for Edges:
        gml.defEdgeData(GraphMLWriter.EDGEDATA_NAME, GraphMLWriter.EDGEDATA_NAME, GraphMLWriter.TYPE_STRING);
        gml.defEdgeData(GraphMLWriter.DATA_WEIGHT, GraphMLWriter.DATA_WEIGHT, GraphMLWriter.TYPE_FLOAT);
        gml.defEdgeData(GraphMLWriter.DATA_BIAS, GraphMLWriter.DATA_BIAS, GraphMLWriter.TYPE_FLOAT);

        if (showValues) {
            gml.defNodeData(GraphMLWriter.DATA_VALUE, GraphMLWriter.DATA_VALUE, GraphMLWriter.TYPE_FLOAT);
            gml.defEdgeData(GraphMLWriter.EDGEDATA_VALUE, GraphMLWriter.EDGEDATA_VALUE, GraphMLWriter.TYPE_FLOAT);
        }

        for (Portable input : inputs) {
            toGraphML_doPort(input, gml, "input", showValues);
        }
        for (Gate gate : gates) {
            gate.toGraphML(gml, "hidden", showValues, cycles);
        }
        for (Portable output : outputs) {
            toGraphML_doPort(output, gml, "output", showValues);
        }
        gml.endGraph();
        gml.close();
    }

    private void toGraphML_doPort(Portable input, GraphMLWriter gml, String layer, boolean showValues) {
        if (input instanceof Port) {
            List<Gate> gates = ((Port) input).getGates();
            for (Gate gate : gates) {
                gate.toGraphML(gml, layer, showValues, cycles);
            }
        }
        if (input.getChildren() != null) {
            Iterator<Portable> itChild = input.getChildren().iterator();
            while (itChild.hasNext()) {
                toGraphML_doPort(itChild.next(), gml, layer, showValues);
            }
        }
    }

    public void clearPortValues() {
        for (Portable input : inputs) {
            input.clearValue(true);
        }
        for (Portable output : outputs) {
            output.clearValue(true);
        }
    }

    private void prepareValues(List<Portable> ports) {
        // prepare output-values
        Iterator<Portable> itPort = ports.iterator();
        while (itPort.hasNext()) {
            prepareValues_doPort(itPort.next());
        }
    }

    private void prepareValues_doPort(Portable port) {
        if (port instanceof PortStream) {
            PortStream sampler = (PortStream) port;
            sampler.next(cycles);
        }
        if (port.getChildren() != null) {
            Iterator<Portable> itChild = port.getChildren().iterator();
            while (itChild.hasNext()) {
                prepareValues_doPort(itChild.next());
            }
        }
    }

    protected void setInputValues(int[] values, BiConsumer<Queue<Gate>, Integer> setterFunc, Queue<Gate> bfp) {
        LOG.debug(name + ": set input=[" + Utils.intArrayToString(values) + "] cycle=" + cycles);
        if (setterFunc != null) {
            setterFunc.accept(bfp, cycles);
        }
        int valueIndex = 0;
        Iterator<Portable> itInput = inputs.iterator();
        while (valueIndex < values.length && itInput.hasNext()) {
            valueIndex = setInputValues_doInput(itInput.next(), values, valueIndex, bfp);
        }
    }

    private int setInputValues_doInput(Portable input, int[] values, int valueIndex, Queue<Gate> bfp) {
        if (input.setValue(values[valueIndex], bfp, cycles)) {
            valueIndex++;
        }
        if (input.getChildren() != null) {
            Iterator<Portable> itChild = input.getChildren().iterator();
            while (itChild.hasNext()) {
                valueIndex = setInputValues_doInput(itChild.next(), values, valueIndex, bfp);
            }
        }
        return valueIndex;
    }

    public int[] getOutputValues() {
        int resultSize = 0;
        for (Portable output : outputs) {
            int count = output.getChildCount(true);
            resultSize += (count > 0) ? count : 1;
        }
        int[] result = new int[resultSize];

        int resultIndex = 0;
        Iterator<Portable> itOutput = outputs.iterator();
        while (resultIndex < result.length && itOutput.hasNext()) {
            resultIndex = getOutputValues_doOutput(itOutput.next(), result, resultIndex);
        }
        LOG.debug(name + ": get output=[" + Utils.intArrayToString(result) + "] cycle=" + cycles);
        return result;
    }

    private int getOutputValues_doOutput(Portable output, int[] result, int resultIndex) {
        result[resultIndex] = output.getValue(cycles);
        if (output instanceof Port) {
            if (result[resultIndex] == Portable.NO_VALUE) {
                result[resultIndex] = output.estimateValue();
            }
            resultIndex++;
        }
        if (output.getChildren() != null) {
            Iterator<Portable> itChild = output.getChildren().iterator();
            while (itChild.hasNext()) {
                resultIndex = getOutputValues_doOutput(itChild.next(), result, resultIndex);
            }
        }
        return resultIndex;
    }

    public int[] query(int[] inputPattern, BiConsumer<Queue<Gate>, Integer> setterFunc) {
        // propagate test-pattern
        cycles++;
        LinkedList<Gate> bfp = new LinkedList<>();
        prepareValues(inputs);
        setInputValues(inputPattern, setterFunc, bfp);
        prepareValues(outputs);
        propagateBFS(bfp);
        return getOutputValues();
    }

    public boolean learn(int[] inputPattern, int[] outputPattern, BiConsumer<Queue<Gate>, Integer> setterFunc, boolean optimize) {
        // fill network with current values
        query(inputPattern, setterFunc);
        // insert/learn (and automatically optimizeGate) result-pattern
        if (insert(outputPattern, optimize) > 0) {
            // verify if the learned input-pattern already results to the output-pattern
            int[] outputValues = query(inputPattern, setterFunc);
            for (int j = 0; j < outputValues.length; j++) {
                final int sollValue = outputPattern[j];
                final int istValue = outputValues[j];
                if (sollValue != istValue) {
                    Portable output = outputs.get(j);
                    LOG.debug("ATTENTION - Verification of data failed! Input=" + Utils.intArrayToString(inputPattern) + " " + ((Layer) output).name + " should be " + sollValue + " but is " + istValue);
                    LOG.debug(toString() + "\n" + dumpNetworkToString(true));
                    return correct(output, sollValue, istValue);
                }
            }
            return true;
        } else {
            return false;   // there was nothing new to learn
        }
    }

    private boolean correct(Portable output, final int sollValue, final int istValue) {
        if (output instanceof Port) {
            Port outputPort = (Port)output;
            List<Gate> sollGates = outputPort.getGatesByValue(sollValue);
            List<Gate> istGates = outputPort.getGatesByValue(istValue);
            if( sollGates.size()==1 && istGates.size()==1 ) {
                Gate sollGate = sollGates.get(0);
                float sollResult = sollGate.getResult(cycles);
                float istResult = istGates.get(0).getResult(cycles);
                float correction = (istResult / sollResult) * (1.f + 10.f*precision);
                Iterator it = sollGate.getIns().iterator();
                while( it.hasNext() ) {
                    Edge edge = (Edge)it.next();
                    edge.setWeight( edge.getWeight()*correction );
                }
                LOG.info("Corrected weight-values for inputs to " + output.name() + " by factor " + correction);
                return true;
            }
        }
        return false;
    }

    public boolean verify(int[] inputValues, int[] outputValues, BiConsumer<Queue<Gate>, Integer> setterFunc) {
        int[] mlOutputValues = query(inputValues, setterFunc);
        // compare with result-pattern
        boolean success = true;
        for (int j = 0; j < outputValues.length; j++) {
            if (outputValues[j] != mlOutputValues[j]) {
                LOG.error("FAILED - Verification of data failed! Input=" + Utils.intArrayToString(inputValues) + " " + ((Layer) outputs.get(j)).name + " should be " + outputValues[j] + " but is " + mlOutputValues[j]);
                LOG.info(toString() + "\n" + dumpNetworkToString(true));
                success = false;
            }
        }
        return success;
    }

    public boolean verify(int[] inputValues, BiConsumer<Queue<Gate>, Integer> setterFunc, BiPredicate<Integer[], Integer[]> verifyFunc) {
        Integer[] inArr = Arrays.stream(inputValues).boxed().toArray(Integer[]::new);
        int[] mlOutputValues = query(inputValues, setterFunc);
        Integer[] outArr = Arrays.stream(mlOutputValues).boxed().toArray(Integer[]::new);
        // compare with result-pattern
        if (!verifyFunc.test(inArr, outArr)) {
            LOG.error("FAILED - Verification of data failed! Input=" + Utils.intArrayToString(inputValues) + " Output=" + Utils.intArrayToString(mlOutputValues));
            LOG.info(toString() + "\n" + dumpNetworkToString(true));
            return false;
        } else {
            return true;
        }
    }

    private int propagateBFS(Queue<Gate> bfp) {
        // Breadth-first propagation
        //LOG.debug(name + ": breadth-first propagation started...");
        int count = 0;
        Gate gate = bfp.poll();
        while (gate != null) {
            if (gate.propagate(cycles)) {
                count++;
                for (Edge out : gate.getOuts()) {
                    if (0.0f != out.getValue(cycles)) {
                        bfp.offer((Gate) out.getTo());
                    }
                }
            }

            gate = bfp.poll();  // get next gate from the head
        }
        LOG.debug(name + ": breadth-first propagation finished: propagated " + count + " gates.");
        return count;
    }

    private int insert(int[] outputValues, boolean optimize) {
        LOG.debug(toString() + " insert() started...");
        int valueIndex = 0;
        Iterator<Portable> itOutput = outputs.iterator();
        Ref<Integer> insertedGates = new Ref<>(0);
        while (valueIndex < outputValues.length && itOutput.hasNext()) {
            valueIndex = insert_doOutput(itOutput.next(), outputValues, valueIndex, optimize, insertedGates);
        }
        LOG.debug(toString() + "\n" + dumpNetworkToString(false));
        LOG.debug(toString() + " insert() finished. added gates=" + insertedGates.value);
        return insertedGates.value;
    }

    private int insert_doOutput(Portable output, int[] outputValues, int valueIndex, boolean optimize, Ref<Integer> insertedGates) {
        if (output instanceof Port) {
            Port outputPort = (Port) output;
            int outputValue = outputValues[valueIndex];
            if (outputPort.getValue(cycles) != outputValue) {
                List<Gate> outputGates = outputPort.getGatesByValue(outputValue);
                for (Gate outputGate : outputGates) {
                    Gate andGate = insertAndGate(outputGate);
                    if (andGate != null) {
                        insertedGates.value++;
                        if (outputPort instanceof FuzzyPort) {
                            final Edge edge = andGate.getOuts().get(0);
                            final float fuzzyOutputValue = ((FuzzyPort) outputPort).calcFuzzyFromValue(outputValue);
                            edge.setWeight(edge.getWeight() * fuzzyOutputValue);
                        }
                        if (optimize) {
                            optimizeGate(outputGate);
                        }
                    }
                }
            }
            valueIndex++;
        }
        // TODO: optimation multiple-gates check on add-gate

        if (output.getChildren() != null) {
            Iterator<Portable> itChild = output.getChildren().iterator();
            while (itChild.hasNext()) {
                valueIndex = insert_doOutput(itChild.next(), outputValues, valueIndex, optimize, insertedGates);
            }
        }
        return valueIndex;
    }

    private Gate insertAndGate(Gate outputOrGate) {
        Gate andGate = Gate.createAndGate(this, "AndGate" + (nextGateId++), cycles);

        Ref<Float> sumWeight = new Ref<>(0.0f);
        for (Portable input : inputs) {
            input.createOutputEdges(andGate, sumWeight, cycles);
        }

        if (sumWeight.value > 0.0f) {
            Edge andToOrEdge = outputOrGate.createIn(andGate);
            andToOrEdge.setWeight(sumWeight.value);
            andGate.addOut(andToOrEdge);

            gates.add(andGate);
            LOG.debug(andGate);
            return andGate;
        } else {
            return null;
        }
    }

    public int optimizeAll() {
        int count = 0;
        for (Portable output : outputs) {
            count += optimizaAll_doOutput(output);
        }
        return count;
    }

    private int optimizaAll_doOutput(Portable output) {
        int count = optimizeAll(output);
        if (output.getChildren() != null) {
            Iterator<Portable> itChild = output.getChildren().iterator();
            while (itChild.hasNext()) {
                count += optimizaAll_doOutput(itChild.next());
            }
        }
        return count;
    }

    public int optimizeAll(Portable output) {
        int count = 0;
        if (output instanceof Port) {
            List<Gate> gates = ((Port) output).getGates();
            for (Gate dstGate : gates) {
                count += optimizeGate(dstGate);
            }
        }
        return count;
    }

    protected int optimizeGate(Gate dstGate) {
        LOG.debug(toString() + " optimizing " + dstGate.name());
        int count = 0;
        if (dstGate.getOperation() == Gate.Operation.OR) {
            count += optimizeRemoveInputPermutations(dstGate);
            count += optimizeRemoveDuplicateInputGates(dstGate);
        }
        return count;
    }

    private int optimizeRemoveInputPermutations(Gate orGate) {
        Visitor result = new Visitor();
        result.visitBackwardDSF(orGate, 5);
        LOG.debug(toString() + " checking permutations on " + orGate.name());

        HashSet<Layer> permutatedInputs = new HashSet<>();
        for (LinkedList<Visitable> path : result.paths) {
            Visitable input = path.getLast();
            if (input instanceof Gate) {
                Gate gate = (Gate) input;
                if (gate.parent instanceof OneHotPort) {
                    LOG.debug("   " + input.name()
                            + " visitedCount=" + gate.getVisitValue(result.sessionId)
                            + " parent=" + gate.parent.name + " gates=" + gate.parent.getGates().size()
                            + (gate.parent.isAllVisited(result.sessionId) ? " allVisited" : "")
                            + (gate.parent.isOnlyOneVisited(result.sessionId, gate) ? " onlyThisGateVisited" : "")
                    );

                    if (gate.parent.getCommonVisitCount(result.sessionId) > 0) {
                        permutatedInputs.add(gate.parent);
                    } else if (!gate.parent.isOnlyOneVisited(result.sessionId, gate)) {
                        LOG.debug(toString() + " no permutations found. (entry-graph not fully visited)");
                        return 0;
                    }
                }
            }
        }
        if (permutatedInputs.size() <= 0) {
            LOG.debug(toString() + " no permutations found. (no permutated inputs)");
            return 0;
        }
        LOG.debug(toString() + " permutations found!");

        int removeCount = 0;
        for (Layer permutatedInput : permutatedInputs) {
            for (LinkedList<Visitable> path : result.paths) {
                Gate inputGate = (Gate) path.pollLast();
                Edge edge = (Edge) path.pollLast();
                if (inputGate.parent == permutatedInput) {
                    LOG.info(toString() + " removed permutated edge: " + edge.toString());
                    edge.remove();
                    removeCount++;
                } else {
                    edge.setWeight(edge.getWeight() / permutatedInput.getGates().size());
                }
            }
        }
        return removeCount;
    }

    private int optimizeRemoveDuplicateInputGates(Gate orGate) {
        if (orGate == null || orGate.getIns().size() <= 1) {
            return 0;   // no duplicates possible
        }
        LOG.debug(toString() + " checking duplicate-input-paths on " + orGate.name());

        int count = 0;
        Iterator it = orGate.getIns().iterator();
        Edge edgeRef = (Edge) it.next();
        Node nodeRef = edgeRef.getFrom();
        LOG.debug(" search duplicate for " + nodeRef.toString());
        ArrayList<Node> removeNodes = new ArrayList<>();
        float removedWeights = 0;
        while (it.hasNext()) {
            Edge edgeOther = (Edge) it.next();
            Node nodeOther = edgeOther.getFrom();
            if (Visitor.equalsBackwardDSF(nodeRef, nodeOther, 3)) {
                removeNodes.add(nodeOther);
                removedWeights += edgeOther.getWeight();
            }
        }

        if (removeNodes.size() <= 0) {
            LOG.debug(toString() + " no duplicate-input-paths found.");
            return 0;
        }
        LOG.debug(toString() + " duplicate-input-paths found!");

        if (!removeNodes.isEmpty()) {
            for (Node removeNode : removeNodes) {
                LOG.info(toString() + " removed duplicate node: " + removeNode.toString());
                removeNode.remove();
            }
            edgeRef.setWeight(edgeRef.getWeight() + removedWeights);
        }

        return count;
    }
}
