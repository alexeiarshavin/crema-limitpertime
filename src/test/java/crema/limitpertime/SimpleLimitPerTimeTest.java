package crema.limitpertime;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class SimpleLimitPerTimeTest extends BaseLimitPerTimeTest {

    @Override
    protected LimitPerTime ten2000ms() {
        return new SimpleLimitPerTime(10, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected LimitPerTime ten500ms() {
        return new SimpleLimitPerTime(10, 500, TimeUnit.MILLISECONDS);
    }

}
