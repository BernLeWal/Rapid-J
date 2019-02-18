package rapid.net.skalar;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    rapid.net.skalar.BinaryBitCountersTest.class, 
    rapid.net.skalar.BitCountersTest.class,
    rapid.net.BoxingGameTest.class,
    rapid.net.skalar.FuzzyTest.class,
})
public class SkalarTestSuite {
    
}
