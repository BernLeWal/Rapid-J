// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Edge;
import rapid.net.Gate;
import rapid.net.Layer;
import rapid.util.Ref;

public abstract class Port extends Layer implements Portable {

    private static final Logger LOG = LogManager.getLogger(Port.class);

    protected Portable parent = null;

    protected Port(String name, List<Gate> gates) {
        super(name, gates);
    }

    // Structure of portables
    @Override
    public String name() {
        return name;
    }

    @Override
    public Portable getParent() {
        return parent;
    }

    public void setParent(Portable parent) {
        this.parent = parent;
    }

    @Override
    public int getChildCount(boolean recursive) {
        return 0;
    }

    @Override
    public List<Portable> getChildren() {
        return null;    // no children supported
    }

    // Gate handling
    public abstract List<Gate> getGatesByValue(int value);

    @Override
    public void createOutputEdges(Gate toAndGate, Ref<Float> sumWeight, int cycle) {
        for (Gate fromGate : gates) {
            if (fromGate.getResult(cycle) != 0.0f) {
                final Edge edge = fromGate.createOut(toAndGate);
                toAndGate.addIn(edge);
                sumWeight.value++;
            }
        }
    }
}
