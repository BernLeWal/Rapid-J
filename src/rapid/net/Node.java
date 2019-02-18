package rapid.net;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Node implements Visitable {

    private static final Logger LOG = LogManager.getLogger(Node.class);

    private final String name;
    protected List<Edge> ins;
    protected List<Edge> outs;

    // for the Visitable-Interface
    private int visitId = 0;
    private int visitValue = 0;

    protected Node(String name) {
        this.name = name;
        this.ins = new ArrayList<>();
        this.outs = new ArrayList<>();
    }

    public void addIns(List<Edge> ins) {
        if (ins == null) {
            return;
        }

        for (Edge in : ins) {
            if (in != null) {
                this.ins.add(in);
                in.setTo(this);
            }
        }
    }

    public void addIn(Edge in) {
        if (in != null) {
            this.ins.add(in);
            in.setTo(this);
        }
    }

    public void addOuts(List<Edge> outs) {
        if (outs == null) {
            return;
        }

        for (Edge out : outs) {
            if (out != null) {
                this.outs.add(out);
                out.setFrom(this);
            }
        }
    }

    public void addOut(Edge out) {
        if (out != null) {
            this.outs.add(out);
            out.setFrom(this);
        }
    }

    public List<Edge> getIns() {
        return this.ins;
    }

    public List<Edge> getOuts() {
        return this.outs;
    }

    public Edge getFirstIn() {
        if (ins.size() <= 0) {
            return null;
        }
        return ins.get(0);
    }

    public Edge getFirstOut() {
        if (outs.size() <= 0) {
            return null;
        }
        return outs.get(0);
    }

    public Edge createIn(Node from) {
        Edge edge = new Edge(from, this);
        this.ins.add(edge);
        return edge;
    }

    public Edge createOut(Node to) {
        Edge edge = new Edge(this, to);
        this.outs.add(edge);
        return edge;
    }

    public void remove() {
        LOG.debug(" remove " + toString());
        while (ins.size() > 0) {
            ins.get(ins.size() - 1).remove();
        }
        while (outs.size() > 0) {
            outs.get(outs.size() - 1).remove();
        }
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        Node node = (Node) other;
        if (node.ins.size() != ins.size()) {
            return false;
        }
        if (node.outs.size() != outs.size()) {
            return false;
        }
        // Edges are not directly compared
        return true;
    }    

    //
    // Visitable interface:
    //
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
        return name;
    }

}
