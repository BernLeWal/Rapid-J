// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Edge implements Visitable {

    private static final Logger LOG = LogManager.getLogger(Edge.class);

    private static int instanceCounter = 0;

    private final String name;
    private Node from;
    private Node to;

    private float weight;
    private float bias;
    private float value = 0.0f;
    private int cycle = 0;

    //
    // interface Visitable:
    //
    private int visitId = 0;
    private int visitValue = 0;

    @Override
    public void visit(int id, int value) {
        this.visitId = id;
        this.visitValue = value;
    }

    @Override
    public void visitIncValue(int id) {
        visit(id, getVisitValue(id) + 1);
    }

    @Override
    public boolean isVisited(int id) {
        return (id == visitId);
    }

    @Override
    public int getVisitValue(int id) {
        if (isVisited(id)) {
            return visitValue;
        } else {
            return 0;
        }
    }

    @Override
    public String name() {
        return name + "(w=" + weight + ", b=" + bias + ")";
    }

    public Edge(Node from, Node to) {
        this.name = "Edge" + (++instanceCounter);
        this.from = from;
        this.to = to;
        this.weight = 1.0f;
        this.bias = 0.0f;
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        LOG.debug(toString() + " set w=" + weight);
        this.weight = weight;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float bias) {
        LOG.debug(toString() + " set b=" + bias);
        this.bias = bias;
    }

    public void setValue(float val, int cycle) {
        this.value = val;
        this.cycle = cycle;
    }

    public float getValue(int cycle) {
        if (this.cycle == cycle) {
            return this.value;
        } else {
            return 0.0f;
        }
    }

    public float getWeightedValue(int cycle) {
        if (this.cycle == cycle) {
            return value * weight + bias;
        } else {
            return 0.0f;
        }
    }

    public void remove() {
        LOG.debug(" remove edge " + toString());
        from.outs.remove(this);
        to.ins.remove(this);
        from = null;
        to = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append((from != null) ? from.name() : "()");
        sb.append("-{w=");
        sb.append(weight);
        sb.append(",b=");
        sb.append(bias);
        sb.append("}->");
        sb.append((to != null) ? to.name() : "()");
        return sb.toString();
    }

    public String dumpEdge() {
        StringBuilder sb = new StringBuilder();
        if (weight != 1.0f) {
            sb.append("*");
            sb.append(weight);
        }
        if (bias > 0.0f) {
            sb.append("+");
            sb.append(bias);
        } else if (bias < 0.0f) {
            sb.append(bias);
        }
        return sb.toString();
    }
}
