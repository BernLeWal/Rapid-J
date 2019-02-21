// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net.port;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Bernhard
 */
public class FuzzyMatrix extends PortGroup {

    private static final Logger LOG = LogManager.getLogger(FuzzyMatrix.class);
    private final int cols;
    private final int rows;

    public FuzzyMatrix(String name, int cols, int rows, int max, int cycle) {
        super(name, null);
        this.cols = cols;
        this.rows = rows;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                children.add(new FuzzyPort(name + String.valueOf(x) + "," + String.valueOf(y), max, cycle));
            }
        }
    }
    
    public int getColumns() {
        return cols;
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getIndexByColAndRow(int col, int row) {
        if( col < 0 || col >= cols || row < 0 || row >= rows )
            return -1;  // outside range
        return row * rows + col;
    }
}
