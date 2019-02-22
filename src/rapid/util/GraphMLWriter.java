// License: Apache 2.0. See LICENSE file in root directory.
package rapid.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphMLWriter {

    private final String filename;
    private FileWriter fw;
    private PrintWriter pw;

    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_INT = "int";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_STRING = "string";
    
    public static final String DATA_NAME = "label";
    public static final String DATA_LAYER = "partition";
    public static final String DATA_PARENT = "parent";
    public static final String DATA_VALUE = "value";
    
    public static final String EDGEDATA_NAME = "label";
    public static final String DATA_WEIGHT = "weight";
    public static final String DATA_BIAS = "bias";
    public static final String EDGEDATA_VALUE = "value";

    public GraphMLWriter(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void open() {
        try {
            fw = new FileWriter(filename);
            pw = new PrintWriter(fw);
        } catch (IOException ex) {
            Logger.getLogger(GraphMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  ");
        pw.println("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        pw.println("    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
    }

    public void defNodeData(String id, String name, String type) {
        pw.println("  <key id=\"" + id + "\" for=\"node\" attr.name=\"" + name + "\" attr.type=\"" + type + "\"/>");
    }

    public void defEdgeData(String id, String name, String type) {
        pw.println("  <key id=\"" + id + "\" for=\"edge\" attr.name=\"" + name + "\" attr.type=\"" + type + "\"/>");
    }

    public void beginGraph(String name, boolean directed) {
        pw.println("<graph id=\"" + name + "\" edgedefault=\"" + (directed ? "directed" : "undirected") + "\">");
    }

    public GraphMLWriter beginNode(String id) {
        pw.println("  <node id=\"" + id + "\">");
        return this;
    }

    public GraphMLWriter endNode() {
        pw.println("  </node>");
        return this;
    }

    public GraphMLWriter beginEdge(String id, String sourceId, String targetId) {
        pw.println("  <edge id=\"" + id + "\" source=\"" + sourceId + "\" target=\"" + targetId + "\">");
        return this;
    }

    public GraphMLWriter endEdge() {
        pw.println("  </edge>");
        return this;
    }

    public GraphMLWriter data(String id, String value) {
        pw.println("    <data key=\"" + id + "\">" + value + "</data>");
        return this;
    }

    public GraphMLWriter data(String id, int value) {
        pw.printf("    <data key=\"%s\">%d</data>\n", id, value);
        return this;
    }
    
    public GraphMLWriter data(String id, float value) {
        pw.printf(Locale.US, "    <data key=\"%s\">%f</data>\n", id, value);
        return this;
    }
    
    public void endGraph() {
        pw.println("</graph>");
    }

    public void close() {
        pw.println("</graphml>");
        pw.flush();
        pw.close();
        try {
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(GraphMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
            pw = null;
            fw = null;
        } finally {
            super.finalize();
        }
    }
}
