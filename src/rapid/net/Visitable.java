// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

public interface Visitable {

    public void visit(int id, int value);

    public void visitIncValue(int id);

    public boolean isVisited(int id);

    public int getVisitValue(int id);

    public String name();
}
