// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Edge;
import rapid.net.Gate;
import rapid.util.Ref;

public class FuzzyPort extends Port {

    private static final Logger LOG = LogManager.getLogger(FuzzyPort.class);

    private final int max;
    private final Gate gate;

    private int value = NO_VALUE;
    private float fuzzyValue = 0.0f;

    public FuzzyPort(String name, int max, int cycle) {
        super(name, new ArrayList<Gate>());

        this.max = max;

        this.gate = Gate.createAddGate(this, name, cycle);  // TODO: check createOrGate()
        gates.add(gate);
    }

    public FuzzyPort(String name, FuzzyPort prototype, int cycle) {
        super(name, new ArrayList<Gate>());
        this.max = prototype.max;

        this.gate = Gate.createAddGate(this, name, cycle);
        gates.add(gate);
    }

    @Override
    public void clearValue(boolean recursive) {
        if (value != NO_VALUE) {
            gate.setStartValue(0.0f);
            value = NO_VALUE;
            fuzzyValue = 0.0f;
        }
    }

    @Override
    public boolean setValue(int value, Queue<Gate> bfp, int cycle) {
        fuzzyValue = calcFuzzyFromValue(value);
        gate.setStartValue(fuzzyValue);
        if (fuzzyValue != 0.0f) {
            bfp.offer(gate);
        }
        return true;
    }

    @Override
    public int getValue(int cycle) {
        return calcValueFromFuzzy(gate.getResult(cycle));
    }

    @Override
    public int estimateValue() {
        return NO_VALUE;    // TODO - not supported yet (is this needed?)
    }

    @Override
    public List<Gate> getGatesByValue(int value) {
        ArrayList<Gate> result = new ArrayList<>();
        if (value != 0) {
            result.add(gate);
        }
        return result;
    }

    @Override
    public void createOutputEdges(Gate toAndGate, Ref<Float> sumWeight, int cycle) {
        final float inputValue = gate.getResult(cycle);
        if (inputValue != 0.0f) {
            final Edge edge = gate.createOut(toAndGate);
            edge.setWeight(1.0f / inputValue);
            toAndGate.addIn(edge);
            if (sumWeight.value == 0.0f) {
                sumWeight.value = 1.0f;
            }
        }
    }

    public float calcFuzzyFromValue(int value) {
        return (float) value / max;
    }

    public int calcValueFromFuzzy(float fuzzyValue) {
        return (int) (fuzzyValue * max + 0.5f);
    }
}
