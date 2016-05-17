package crema.limitpertime;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class LimitsPerTimeTest {

    @Test
    public void testForString() throws Exception {
        LimitPerTime limitPerTime = LimitsPerTime.forString("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");
        assertEquals(limitPerTime.getSpec().size(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testForString_illegal() throws Exception {
        LimitsPerTime.forString("10 in 5 MINUTES; 5 in 1 HOUR");
    }
}