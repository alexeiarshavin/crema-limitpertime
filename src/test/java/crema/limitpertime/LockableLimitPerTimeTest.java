package crema.limitpertime;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class LockableLimitPerTimeTest extends BaseLimitPerTimeTest {

    @Override
    protected LimitPerTime ten2000ms() {
        return new LockableLimitPerTime(10, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected LimitPerTime ten500ms() {
        return new LockableLimitPerTime(10, 500, TimeUnit.MILLISECONDS);
    }



    @Test
    public void testLock() throws Exception {
        LockableLimitPerTime limitPerTime = new LockableLimitPerTime(10, 500, TimeUnit.MILLISECONDS);
        limitPerTime.lock();
        for (int i=0; i<10; i++) {
            assertTrue(limitPerTime.lockedCanConsume("foo"));
            limitPerTime.consume("foo");
        }
        assertFalse(limitPerTime.lockedCanConsume("foo"));
        limitPerTime.unlock();
    }

    @Test
    public void testTimerWaits() throws Exception {
        LockableLimitPerTime limitPerTime = new LockableLimitPerTime(10, 200, TimeUnit.MILLISECONDS);
        limitPerTime.lock();
        for (int i=0; i<10; i++) {
            limitPerTime.consume("foo");
        }
        assertFalse(limitPerTime.lockedCanConsume("foo"));

        //now we wait while holding the lock. the timer kicks in twice, but has to wait.
        Thread.sleep(450);
        //even though time passed, we are still maxed out.
        assertFalse(limitPerTime.lockedCanConsume("foo"));

        //now we unlock, sleep a tiny bit, and then expect that the timer did its jobs and reset the limits.
        limitPerTime.unlock();
        Thread.sleep(10);
        assertTrue(limitPerTime.consume("foo"));
    }
}
