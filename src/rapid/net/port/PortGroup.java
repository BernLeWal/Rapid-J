package rapid.net.port;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Gate;
import rapid.util.Ref;

public class PortGroup implements Portable {

    private static final Logger LOG = LogManager.getLogger(PortGroup.class);

    public final String name;
    protected Portable parent = null;
    protected List<Portable> children;

    public PortGroup(String name, Portable[] childPorts) {
        this.name = name;
        this.children = new ArrayList<>();

        if (childPorts != null) {
            for (Portable child : childPorts) {
                this.children.add(child);
            }
        }
    }

    public PortGroup(String name, PortGroup prototype, int cycle) {
        this.name = name;
        this.children = new ArrayList<>();

        for (Portable child : prototype.children) {
            children.add(PortFactory.createByPrototype(child.name() + "'", child, cycle));
        }

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
    public List<Portable> getChildren() {
        return children;
    }

    @Override
    public int getChildCount(boolean recursive) {
        int count = children.size();
        if (recursive) {
            for (Portable child : children) {
                count += child.getChildCount(recursive);
            }
        }
        return count;
    }

    // Value handling - no direct values in Port-Groups
    @Override
    public void clearValue(boolean recursive) {
        if (recursive) {
            for (Portable child : children) {
                child.clearValue(true);
            }
        }
    }

    @Override
    public boolean setValue(int value, Queue<Gate> bfp, int cycle) {
        return false; // no direct values in Port-Groups
    }

    @Override
    public int getValue(int cycle) {
        return NO_VALUE;
    }

    @Override
    public int estimateValue() {
        return NO_VALUE;
    }

    // Gate handling
    @Override
    public void createOutputEdges(Gate toAndGate, Ref<Float> sumWeight, int cycle) {
        Iterator<Portable> itChild = children.iterator();
        while (itChild.hasNext()) {
            itChild.next().createOutputEdges(toAndGate, sumWeight, cycle);
        }
    }
}
