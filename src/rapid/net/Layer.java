// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Layer {

    private static final Logger LOG = LogManager.getLogger(Layer.class);

    public final String name;
    protected final List<Gate> gates;

    protected Layer(String name, List<Gate> gates) {
        this.name = name;
        this.gates = gates;
    }

    public List<Gate> getGates() {
        return this.gates;
    }

    @Override
    public String toString() {
        return name + "{" + gates.size() + " gates}";
    }

    public boolean isAllVisited(int sessionId) {
        for (Gate gate : gates) {
            if (!gate.isVisited(sessionId)) {
                return false;
            }
        }
        return true;
    }

    public int getCommonVisitCount(int sessionId) {
        int result = 0;
        for (Gate gate : gates) {
            int visited = gate.getVisitValue(sessionId);
            if (visited <= 0) {
                return 0;
            }
            if (result == 0) {
                result = visited;
            }
            if (visited != result) {
                return 0;
            }
        }
        return result;
    }

    public boolean isOnlyOneVisited(int sessionId, Gate visitedGate) {
        for (Gate gate : gates) {
            if (gate == visitedGate) {
                if (!gate.isVisited(sessionId)) {
                    return false;
                }
            } else {
                if (gate.isVisited(sessionId)) {
                    return false;
                }
            }
        }
        return true;
    }
}
