// License: Apache 2.0. See LICENSE file in root directory.
package rapid.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bernhard
 */
public class CsvWriter {

    public final String SEPARATOR = ";";

    private final String filename;
    private FileWriter fw;
    private PrintWriter pw;
    private boolean isFirstColumn = true;

    public CsvWriter(String filename) {
        this.filename = filename;
    }
    
    public boolean open(boolean append) {
        boolean newFile = true;
        if( append && new File(filename).isFile() )
            newFile = false;
        
        try {
            fw = new FileWriter(filename, append);
            pw = new PrintWriter(fw);
        } catch (IOException ex) {
            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
        }      
        return newFile;
    }

    public String getFilename() {
        return filename;
    }

    public void print(String s) {
        if (!isFirstColumn) {
            pw.print(SEPARATOR);
        } else {
            isFirstColumn = false;
        }
        pw.print(s);
    }

    public void print(int i) {
        if (!isFirstColumn) {
            pw.print(SEPARATOR);
        } else {
            isFirstColumn = false;
        }
        pw.print(i);
    }

    public void print(float f) {
        if (!isFirstColumn) {
            pw.print(SEPARATOR);
        } else {
            isFirstColumn = false;
        }
        pw.printf("%5.3f", f);
    }

    public void println() {
        pw.println();
        pw.flush();
        isFirstColumn = true;
    }

    public void close() {
        pw.flush();
        pw.close();
        try {
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
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
