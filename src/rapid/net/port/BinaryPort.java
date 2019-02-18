package rapid.net.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Gate;

public class BinaryPort extends Port {

    private static final Logger LOG = LogManager.getLogger(BinaryPort.class);

    private final int bits;

    public BinaryPort(String name, int bits, int cycle) {
        super(name, new ArrayList<Gate>());
        this.bits = bits;

        // normal bits
        for (int i = 0; i < bits; i++) {
            gates.add(Gate.createOrGate(this, this.name + "." + i, cycle));
        }

        // used for 0-value
        gates.add(Gate.createOrGate(this, this.name + ".Null", cycle));
    }

    public BinaryPort(String name, BinaryPort prototype, int cycle) {
        super(name, new ArrayList<Gate>());
        this.bits = prototype.bits;

        // normal bits
        for (int i = 0; i < bits; i++) {
            gates.add(Gate.createOrGate(this, this.name + "." + i, cycle));
        }

        // used for 0-value
        gates.add(Gate.createOrGate(this, this.name + ".Null", cycle));
    }

    @Override
    public void clearValue(boolean recursive) {
        setValue(-1, null, 0);
    }

    @Override
    public boolean setValue(int value, Queue<Gate> bfp, int cycle) {
        int mask = 1;
        for (int i = 0; i < bits; i++) {
            Gate gate = this.gates.get(i);
            if ((value & mask) == mask) {
                gate.setStartValue(1.0f);   // normal bit
                bfp.offer(gate);
            } else {
                gate.setStartValue(0.0f);
            }
            mask = mask << 1;
        }

        // used for 0-value
        Gate gate0 = this.gates.get(bits);
        if (value == 0) {
            gate0.setStartValue(1.0f);
            bfp.offer(gate0);
        } else {
            gate0.setStartValue(0.0f);
        }
        return true;
    }

    @Override
    @Deprecated // only for testing-purposes. Does not work well in separating multiple set bits from not-set bits.
    public int getValue(int cycle) {
        // check if the gate for the '0'-value is already the result
        if (gates.get(bits).getResult(cycle) != 0.0f) {
            return 0;
        }

        // find out the maximum (fuzzy) value
        float maxValue = 0.0f;
        float sumValue = 0.0f;
        for (Gate gate : gates) {
            float f = gate.getResult(cycle);
            if (f > maxValue) {
                maxValue = f;
            }
            sumValue += f;
        }
        if (maxValue == 0.0f) {
            return -1;  // no answer existing
        }
        float triggerValue = maxValue / 2.0f;

        // now check which bits are set
        int mask = 1;
        int resultValue = 0;
        for (int i = 0; i < bits; i++) {
            final float f = this.gates.get(i).getResult(cycle);
            boolean bitValue = /*(f > 1.0f) ||*/ (f > triggerValue);
            if (bitValue) {
                this.gates.get(i).incHits();
                resultValue |= mask;
            }
            mask = mask << 1;
        }
        if (resultValue == 0) {
            return -1;  // no answer existing
        }
        return resultValue;
    }

    @Override
    public int estimateValue() {
        return 0;  // not supported yet. TODO implementation
    }

    @Override
    public List<Gate> getGatesByValue(int value) {
        ArrayList<Gate> result = new ArrayList<>();
        if (value == 0) {
            result.add(gates.get(bits));
        } else {
            int mask = 1;
            for (int i = 0; i < bits; i++) {
                if ((value & mask) == mask) {
                    result.add(gates.get(i));
                }
                mask = mask << 1;
            }
        }

        return result;
    }
}
