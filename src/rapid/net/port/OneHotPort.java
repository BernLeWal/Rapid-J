package rapid.net.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Gate;

public class OneHotPort extends Port {

    private static final Logger LOG = LogManager.getLogger(OneHotPort.class);

    private int index = NO_VALUE;

    public OneHotPort(String name, int max, int cycle) {
        super(name, new ArrayList<Gate>());

        for (int i = 0; i <= max; i++) {
            gates.add(Gate.createOrGate(this, name + "." + i, cycle));
        }
    }

    public OneHotPort(String name, OneHotPort prototype, int cycle) {
        super(name, new ArrayList<Gate>());

        for (int i = 0; i <= prototype.gates.size(); i++) {
            gates.add(Gate.createOrGate(this, name + "." + i, cycle));
        }
    }

    protected OneHotPort(String name) {
        super(name, new ArrayList<Gate>());
    }

    @Override
    public void clearValue(boolean recursive) {
        if (index != NO_VALUE) {
            gates.get(index).setStartValue(0.0f);
            index = NO_VALUE;
        }
    }

    @Override
    public boolean setValue(int value, Queue<Gate> bfp, int cycle) {
        if (index == value) {
            if (index != NO_VALUE) {
                bfp.offer(gates.get(index));
            }
            return true;
        }
        clearValue(false);
        if (value != NO_VALUE) {
            Gate gate = gates.get(value);
            gate.setStartValue(1.0f);
            bfp.offer(gate);
            index = value;
        }
        return true;
    }

    @Override
    public int getValue(int cycle) {
        ArrayList<Integer> resultIndex = new ArrayList<>();
        float resultValue = 0.0f;
        for (int i = 0; i < gates.size(); i++) {
            float f = gates.get(i).getResult(cycle);
            if (f != 0.0f) {
                if (f > resultValue) {
                    resultIndex.clear();
                    resultIndex.add(i);
                    resultValue = f;
                } else if (f == resultValue) {
                    resultIndex.add(i);
                }
            }
        }
        switch (resultIndex.size()) {
            case 1: {
                // exatly one result --> return it
                int index = resultIndex.get(0);
                gates.get(index).incHits();
                return index;
            }
            case 0:
                // no result found 
                return NO_VALUE;

            default: {
                // multiple results --> blanace between the possible results
                int index = NO_VALUE;
                double dMin = 0.0;
                StringBuilder sb = new StringBuilder();
                sb.append("multiple outputs: [ ");
                for (Integer i : resultIndex) {
                    Gate g = gates.get(i);
                    double d = (double) g.getHits() / (double) (cycle - g.getCreationCycle());
                    if (dMin == 0.0 || d < dMin) {
                        dMin = d;
                        index = i;
                    }
                    sb.append("{");
                    sb.append(i);
                    sb.append("|");
                    sb.append(d);
                    sb.append("} ");
                }
                sb.append(" ] return output=");
                sb.append(index);
                LOG.info(sb.toString());
                gates.get(index).incHits();
                return index;
            }
        }
    }

    @Override
    public int estimateValue() {
        // return the most likely result
//        int hitCount = 0;
//        int hitIndex = NO_VALUE;
//        for (int i = 0; i < gates.size(); i++) {
//            int hits = gates.get(i).getHits();
//            if (hits > hitCount) {
//                hitCount = hits;
//                hitIndex = i;
//            }
//        }
        return 0;   // treat NO_VALUE as 0-value
    }

    @Override
    public List<Gate> getGatesByValue(int value) {
        ArrayList<Gate> result = new ArrayList<>();
        if (value >= 0 && value < gates.size()) {
            result.add(gates.get(value));
        }
        return result;
    }
}
