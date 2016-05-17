package crema.limitpertime;

import com.google.common.base.Stopwatch;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class MultiLimitPerTimeTest extends BaseLimitPerTimeTest {

    @Override
    protected LimitPerTime ten2000ms() {
        return MultiLimitPerTime.create().add(10, 2000, TimeUnit.MILLISECONDS).build();
    }

    @Override
    protected LimitPerTime ten500ms() {
        return MultiLimitPerTime.create().add(10, 500, TimeUnit.MILLISECONDS).build();
    }

    @Test
    public void testMulti() throws Exception {
        //because the reset on the longest comes after 500ms
        runMulti(450, 15); //before reset
        runMulti(750, 30); //after reset
    }

    private void runMulti(long runFor, int expected) {
        MultiLimitPerTime limitPerTime = MultiLimitPerTime.create()
                .add(2, 10, TimeUnit.MILLISECONDS)
                .add(5, 50, TimeUnit.MILLISECONDS)
                .add(7, 100, TimeUnit.MILLISECONDS)
                .add(15, 500, TimeUnit.MILLISECONDS)
                .build();

        int counter = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (stopwatch.elapsed(TimeUnit.MILLISECONDS) < runFor) {
            if (limitPerTime.consume("foo")) counter++;
        }

        limitPerTime.destroy();
        assertEquals(counter, expected);
    }
}
