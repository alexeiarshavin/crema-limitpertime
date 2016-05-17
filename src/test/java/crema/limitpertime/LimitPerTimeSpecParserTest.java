package crema.limitpertime;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpecParserTest {

    @Test
    public void testParse() throws Exception {
        List<LimitPerTimeSpec> spec = LimitPerTimeSpecParser.getInstance().parse("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");
        assertEquals(spec.size(), 3);
    }
}