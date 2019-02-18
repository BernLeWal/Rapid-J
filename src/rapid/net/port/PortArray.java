// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Gate;

public class PortArray extends PortGroup {

    private static final Logger LOG = LogManager.getLogger(PortArray.class);

    private int index = 0;
    private final ArrayList<List<Portable>> childrenByIndex;
    private boolean isCleared = true;
    private final Portable[] prototypes;

    public PortArray(String name, Portable[] prototypes) {
        super(name, prototypes);
        this.prototypes = prototypes;

        childrenByIndex = new ArrayList<>();
        childrenByIndex.add(children);
    }

    public int getIndex() {
        return index;
    }
    
    public boolean setIndex(int index) {
        if( index < size() ) {
            children = childrenByIndex.get(index);
            this.index = index;
            return true;
        }
        return false;
    }

    public int size() {
        return childrenByIndex.size();
    }
    
    public List<Portable> getChildrenByIndex(int index) {
        return childrenByIndex.get(index);
    }

    private List<Portable> createAndAddChildren(int cycle) {
        ArrayList<Portable> newChildren = new ArrayList<>();
        if (prototypes.length == 1) {
            newChildren.add(PortFactory.createByPrototype(name + "[" + size() + "]", prototypes[0], cycle));
        } else if (prototypes.length > 1) {
            for (Portable prototype : prototypes) {
                newChildren.add(PortFactory.createByPrototype(prototype.name() + "[" + size() + "]", prototype, cycle));
            }
        }
        childrenByIndex.add(newChildren);
        return newChildren;
    }

    @Override
    public void clearValue(boolean recursive) {
        index = 0;
        isCleared = true;
        if (recursive) {
            for (List<Portable> childs : childrenByIndex) {
                for (Portable child : childs) {
                    child.clearValue(true);
                }
            }
        }
    }

    @Override
    public boolean setValue(int value, Queue<Gate> bfp, int cycle) {
        return false; // no direct values in Port-Arrays
    }

    @Override
    public int getValue(int cycle) {
        return NO_VALUE;
    }

    public void first() {
        // Note: the child(ren) for the first index is always generated at the ctor!
        clearValue(true);
        isCleared = false;
        children = childrenByIndex.get(index);
    }

    public void next(int cycle) {
        if (isCleared) {
            first();
            return;
        }
        index++;
        if (index >= size()) {
            children = createAndAddChildren(cycle);
        } else {
            children = childrenByIndex.get(index);
        }
    }
}
