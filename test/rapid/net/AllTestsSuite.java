// License: Apache 2.0. See LICENSE file in root directory.
package rapid.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // Skalar tests (see also rapid.net.skalar.SkalarTestSuite)
    rapid.net.skalar.BitCountersTest.class,
    rapid.net.BoxingGameTest.class,
    rapid.net.skalar.FuzzyTest.class,
    
    // Vector tests (see also rapid.net.vector.VetorTestSuite)
    rapid.net.vector.VectorTest.class,
    
    // Matrix tests (see also rapid.net.matrix.MatrixTestSuite)
    rapid.net.matrix.Numbers123Test.class,
})
public class AllTestsSuite {
    
}
