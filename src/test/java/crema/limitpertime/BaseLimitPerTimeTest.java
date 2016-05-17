package crema.limitpertime;

import com.google.common.base.Stopwatch;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Provides some common tests for all three implementations, and some shared testing code.
 *
 * @author Alexei Arshavin, Optimaize
 */
public abstract class BaseLimitPerTimeTest {

    /**
     * Allows 10 requests every 2000ms.
     */
    protected abstract LimitPerTime ten2000ms();
    /**
     * Allows 10 requests every 500ms.
     */
    protected abstract LimitPerTime ten500ms();

    @Test
    public void testSingleThreaded() throws Exception {
        testSingleThreaded(ten2000ms());
    }

    @Test
    public void testMultipleKeys() throws Exception {
        testMultipleKeys(ten2000ms());
    }

    @Test
    public void testTiming() throws Exception {
        testTiming(ten2000ms());
    }

    @Test
    public void aBitOfStressTest() throws Exception {
        aBitOfStressTest(ten500ms());
    }


    @Test
    public void exhaustOneButAllowOther() throws Exception {
        LimitPerTime limitPerTime = ten2000ms();
        for (int i=0; i<10; i++) {
            assertTrue(limitPerTime.consume("first"));
        }
        assertFalse(limitPerTime.consume("first"));
        assertTrue(limitPerTime.consume("second"));

        LimitPerTimeSpec specFromConsume = limitPerTime.consumeOrSpec("first");
        expectTen2000Ms(specFromConsume);
        LimitPerTimeSpec specFromCanConsume = limitPerTime.canConsumeOrSpec("first");
        expectTen2000Ms(specFromCanConsume);
    }
    private void expectTen2000Ms(LimitPerTimeSpec specFromConsume) {
        assertTrue(specFromConsume != null);
        assertEquals(specFromConsume.getLimit(), 10);
        assertEquals(specFromConsume.getTimeAmount(), 2000);
        assertEquals(specFromConsume.getTimeUnit(), TimeUnit.MILLISECONDS);
    }

    @Test
    public void testCanConsume() throws Exception {
        LimitPerTime limitPerTime = ten2000ms();

        //because we never consume we can ask 1000 times even though only 10 consumptions are permitted.
        for (int i=0; i<1000; i++) {
            assertTrue(limitPerTime.canConsume("foo"));
        }

        //then consume 9
        for (int i=0; i<9; i++) {
            assertTrue(limitPerTime.consume("foo"));
        }

        //one is left, we can still ask as often as we want:
        for (int i=0; i<1000; i++) {
            assertTrue(limitPerTime.canConsume("foo"));
        }

        //then consume 1
        assertTrue(limitPerTime.consume("foo"));

        //can't consume anymore
        assertFalse(limitPerTime.canConsume("foo"));

        //and next consumption fails
        assertFalse(limitPerTime.consume("foo"));
    }


    protected void testSingleThreaded(LimitPerTime limitPerTime) throws Exception {
        //trying to consume 20 when only 10 are allowed in this time
        int counter = 0;
        for (int i=0; i<20; i++) {
            boolean consumed = limitPerTime.consume("foo");
            if (consumed) counter++;
        }
        limitPerTime.destroy();
        assertEquals(counter, 10);
    }


    protected void testMultipleKeys(LimitPerTime limitPerTime) throws Exception {
        //trying to consume 20 when only 10 are allowed in this time
        int counter = 0;
        for (int i=0; i<20; i++) {
            for (int j=1; j<=5; j++) {
                String s = "key-"+j;
                boolean consumed = limitPerTime.consume(s);
                if (consumed) counter++;
            }
        }
        limitPerTime.destroy();
        assertEquals(counter, 50);
    }

    protected void testTiming(LimitPerTime limitPerTime) throws Exception {
        //consuming 20 must take a bit more than 2000ms
        Stopwatch stopwatch = Stopwatch.createStarted();
        int counter = 0;
        while (counter < 20) {
            boolean consumed = limitPerTime.consume("foo");
            if (consumed) counter++;
        }
        limitPerTime.destroy();
        assertEquals(counter, 20);
        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS))
                .isGreaterThanOrEqualTo(1950) //theoretically 2000, but I had 1999 when testing. because it's 2 different clocks (Stopwatch vs. Timer).
                .isLessThanOrEqualTo(2500); //should be just a bit above 2000.
    }

    protected void aBitOfStressTest(LimitPerTime limitPerTime) throws Exception {
        int counter = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (stopwatch.elapsed(TimeUnit.MILLISECONDS) < 3000) {
            for (int i=0; i<1000; i++) {
                String s = "key-"+i;
                boolean consumed = limitPerTime.consume(s);
                if (consumed) counter++;
            }
        }

        limitPerTime.destroy();
        assertThat(counter).isGreaterThanOrEqualTo(10001);
    }

}
