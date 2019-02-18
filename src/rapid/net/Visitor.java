package rapid.net;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Visitor {

    private static final Logger LOG = LogManager.getLogger(Visitor.class);

    private static int s_visitSessionId = 0;

    public final int sessionId;
    public final LinkedList<LinkedList<Visitable>> paths;

    public Visitor() {
        this.sessionId = ++s_visitSessionId;
        this.paths = new LinkedList<>();
    }

    public int visitBackwardDSF(Node startNode, int depth) {
        this.paths.add(new LinkedList<>());
        int count = visitBackwardDSF(0, startNode, depth);
        if (count > 1) {
            dumpPaths();
        }
        return count;
    }

    public static boolean equalsBackwardDSF(Node ref, Node other, int depth) {
        if (ref == null || other == null) {
            return false;
        }
        if (!ref.equals(other)) {
            return false;
        }
        if( ref.getIns().isEmpty() && ref!=other )
            return false;

        if (depth > 0) {
            Iterator itRef = ref.getIns().iterator();
            Iterator itOther = other.getIns().iterator();
            while( itRef.hasNext() && itOther.hasNext() ) {
                Edge edgeRef = (Edge)itRef.next();
                Edge edgeOther = (Edge)itOther.next();
                
                if( !equalsBackwardDSF(edgeRef.getFrom(), edgeOther.getFrom(), depth-1 ) )
                    return false;
            }
        }

        return true;
    }

    private int visitBackwardDSF(int count, Node startNode, int depth) {
        if (startNode == null) {
            return count;
        }
        if (depth <= 0) {
            return count;
        }

        LinkedList<Visitable> path = this.paths.getLast();
        startNode.visitIncValue(sessionId);
        count++;
        path.add(startNode);
        depth--;

        if (depth > 0) {
            boolean first = true;
            LinkedList<Visitable> backupChain = null;
            for (Edge in : startNode.getIns()) {
                in.visitIncValue(sessionId);
                count++;
                if (first) {
                    backupChain = (LinkedList<Visitable>) path.clone();
                    first = false;
                } else {
                    path = (LinkedList<Visitable>) backupChain.clone();
                    paths.add(path);
                }
                path.add(in);
                visitBackwardDSF(count, in.getFrom(), depth - 1);
            }
            depth--;
        }

        return count;
    }

    public void dumpPaths() {
        for (LinkedList<Visitable> path : paths) {
            dumpPath(path);
        }
    }

    public void dumpPath(LinkedList<Visitable> path) {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        boolean first = true;

        Iterator<Visitable> it = path.descendingIterator();
        while (it.hasNext()) {
            Visitable v = it.next();
            if (first) {
                first = false;
            } else {
                sb.append(" => ");
            }
            sb.append(v.name());
            sb.append("{");
            sb.append(v.getVisitValue(sessionId));
            sb.append("}");
        }
        LOG.debug(sb.toString());
    }
}
