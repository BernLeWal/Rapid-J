// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import java.util.List;
import java.util.Queue;
import rapid.net.Gate;
import rapid.util.Ref;

public interface Portable {

    // Information about myself
    String name();

    // Structure of portables
    Portable getParent();

    List<Portable> getChildren();

    int getChildCount(boolean recursive);

    // Value handling
    public static final int NO_VALUE = -1;

    void clearValue(boolean recursive);

    boolean setValue(int value, Queue<Gate> bfp, int cycle);

    int getValue(int cycle);

    int estimateValue();

    // Gate handling
    public void createOutputEdges(Gate toAndGate, Ref<Float> sumWeight, int cycle);
}
