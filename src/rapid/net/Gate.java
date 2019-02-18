// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Gate extends Node {

    private static final Logger LOG = LogManager.getLogger(Gate.class);

    public enum Operation {
        AND("AND", (values) -> {
            float f = 1.0f;
            for (float val : values) {
                f = f * val;
            }
            return (f <= 1.0f) ? f : 1.0f / f;
        }),
        MUL("MUL", (values) -> {
            float f = 1.0f;
            for (float val : values) {
                f = f * val;
            }
            return f;
        }),
        
        OR("OR", (values) -> {
            float f = 0.0f;
            int count = 0;
            for (float val : values) {
                if (val != 0.0f) {
                    f = f + val;
                    count++;
                }
            }
            return (count > 0) ? (f / count) : 0.0f;
        }),
        ADD("ADD", (values) -> {
            float f = 0.0f;
            for (float val : values) {
                f = f + val;
            }
            return f;
        });

        private final String symbol;
        private final Function<List<Float>, Float> op;

        Operation(String symbol, Function<List<Float>, Float> op) {
            this.symbol = symbol;
            this.op = op;
        }

        @Override
        public String toString() {
            return symbol;
        }

        public float apply(List<Float> values) {
            return op.apply(values);
        }
    }

    public final Layer parent;
    private final Operation operation;
    private final int creationCycle;

    private int hits = 0;
    private float startValue = 0.0f;
    private float result;
    private int resultCycle;

    protected Gate(Layer parent, String name, Operation operation, int creationCycle) {
        super(name);
        this.parent = parent;
        this.operation = operation;
        this.creationCycle = creationCycle;

        ins = new ArrayList<>();
        outs = new ArrayList<>();
    }

    public Operation getOperation() {
        return this.operation;
    }

    public int getCreationCycle() {
        return this.creationCycle;
    }

    public int getHits() {
        return this.hits;
    }

    public void incHits() {
        this.hits++;
    }

    public void setStartValue(float value) {
        this.startValue = value;
    }

    public float getStartValue() {
        return this.startValue;
    }

    public float getResult(int cycle) {
        if (resultCycle == cycle) {
            return result;
        } else {
            return 0.0f;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        if (this.operation != ((Gate) other).operation) {
            return false;
        }
        return true;
    }

    @Override
    public void remove() {
        super.remove();
        parent.getGates().remove(this);
    }

    public boolean propagate(int cycle) {
        if (cycle == resultCycle) {
            return false;   // aready propagated, do not need to repeat
        }
        if (ins.isEmpty()) {
            result = startValue;  // if there are no inputs, then use the startValue instead.
        } else {
            // calculate the result-value
            List<Float> vals = new LinkedList<>();
            for (Edge in : ins) {
                vals.add(in.getWeightedValue(cycle));
            }
            result = operation.apply(vals);
            hits++;
        }
        resultCycle = cycle;

        // push result to the outgoing-edges
        for (Edge out : outs) {
            out.setValue(result, resultCycle);
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        if (!ins.isEmpty()) {
            sb.append(" (");
            boolean addSeparator = false;
            for (Edge in : ins) {
                if (addSeparator) {
                    sb.append(" ");
                    sb.append(operation.toString());
                    sb.append(" ");
                } else {
                    addSeparator = true;
                }
                sb.append(in.getFrom().name());
            }
            sb.append(")");
        }
        sb.append(" ==> ");
        sb.append(outs.get(0).getTo().name());
        return sb.toString();
    }

    public String dumpReverse(int depth, boolean showValue) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ins.size(); i++) {
            Edge in = ins.get(i);
            final Gate fromGate = (Gate) in.getFrom();
            if (!fromGate.ins.isEmpty()) {
                sb.append("(");
                sb.append(fromGate.dumpReverse(depth + 1, showValue));
                sb.append(")");
                sb.append(in.dumpEdge());
                if (showValue) {
                    sb.append("{");
                    sb.append(fromGate.result);
                    sb.append("|");
                    sb.append(fromGate.resultCycle);
                    sb.append("}");
                }
            } else {
                sb.append(fromGate.name());
                sb.append(in.dumpEdge());
                if (showValue) {
                    sb.append("{");
                    sb.append(fromGate.result);
                    sb.append("|");
                    sb.append(fromGate.resultCycle);
                    sb.append("}");
                }
            }
            if (i < (ins.size() - 1)) {
                sb.append(" ");
                sb.append(operation.toString());
                sb.append(" ");
            }
        }

        if (depth == 0) {
            sb.append(" => ");
            sb.append(name());
            if (showValue) {
                sb.append("{");
                sb.append(result);
                sb.append("|");
                sb.append(resultCycle);
                sb.append("}");
            }
        }
        return sb.toString();
    }

    //
    // Factory functions:
    //
    public static Gate createAndGate(Layer parent, String name, int cycle) {
        return new Gate(parent, name, Operation.AND, cycle);
    }

    public static Gate createMulGate(Layer parent, String name, int cycle) {
        return new Gate(parent, name, Operation.MUL, cycle);
    }
    
    public static Gate createOrGate(Layer parent, String name, int cycle) {
        return new Gate(parent, name, Operation.OR, cycle);
    }
    
    public static Gate createAddGate(Layer parent, String name, int cycle) {
        return new Gate(parent, name, Operation.ADD, cycle);
    }
}
