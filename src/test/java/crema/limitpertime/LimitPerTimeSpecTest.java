package crema.limitpertime;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpecTest {

    @Test
    public void testConstructAndGet() throws Exception {
        LimitPerTimeSpec spec = new LimitPerTimeSpec(5, 10, TimeUnit.SECONDS);
        assertEquals(spec.getLimit(), 5);
        assertEquals(spec.getTimeAmount(), 10);
        assertEquals(spec.getTimeUnit(), TimeUnit.SECONDS);
        assertEquals(spec.getTimeAmountInMillis(), 10*1000);
    }

    @Test
    public void testEquals() throws Exception {
        LimitPerTimeSpec spec1 = new LimitPerTimeSpec(5, 10, TimeUnit.SECONDS);
        LimitPerTimeSpec spec2 = new LimitPerTimeSpec(5, 10*1000, TimeUnit.MILLISECONDS);
        assertEquals(spec1, spec2);

        LimitPerTimeSpec spec3 = new LimitPerTimeSpec(5, 10, TimeUnit.SECONDS);
        LimitPerTimeSpec spec4 = new LimitPerTimeSpec(6, 10*1000, TimeUnit.MILLISECONDS);
        assertNotEquals(spec3, spec4);
    }

    @Test
    public void testCompare() throws Exception {
        LimitPerTimeSpec spec1 = new LimitPerTimeSpec(5, 10, TimeUnit.SECONDS);
        LimitPerTimeSpec spec2 = new LimitPerTimeSpec(5, 10*1000, TimeUnit.MILLISECONDS);
        assertTrue(spec1.compareTo(spec2) == 0);
        assertTrue(spec2.compareTo(spec1) == 0);

        LimitPerTimeSpec spec3 = new LimitPerTimeSpec(5, 10, TimeUnit.SECONDS);
        LimitPerTimeSpec spec4 = new LimitPerTimeSpec(5, 10, TimeUnit.MINUTES);
        assertTrue(spec3.compareTo(spec4) < 0);
        assertTrue(spec4.compareTo(spec3) > 0);
    }



    @Test(expectedExceptions = IllegalArgumentException.class)
    public void limitTooSmall() throws Exception {
        new LimitPerTimeSpec(0, 10, TimeUnit.MICROSECONDS);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void timeAmountTooSmall() throws Exception {
        new LimitPerTimeSpec(5, 0, TimeUnit.MICROSECONDS);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void timeUnitTooSmall() throws Exception {
        new LimitPerTimeSpec(5, 10, TimeUnit.MICROSECONDS);
    }
}