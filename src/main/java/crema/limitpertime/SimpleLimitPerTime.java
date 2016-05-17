package crema.limitpertime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Simple impl with just one limit, using Java synchronization.
 *
 * <p>Implementation detail:
 * Using a Timer with TimerTask.
 * All access is synchronized. That may not be the most performing, but does the job in a simple and safe way.
 * </p>
 *
 * @author Alexei Arshavin, Optimaize
 */
class SimpleLimitPerTime implements LimitPerTime {

    @NotNull
    private final LimitPerTimeSpec spec;
    private final Map<Object,Long> counters;
    private final Timer timer;

    protected SimpleLimitPerTime(long limit, long timeAmount, @NotNull TimeUnit timeUnit) {
        this(new LimitPerTimeSpec(limit, timeAmount, timeUnit));
    }

    /**
     */
    protected SimpleLimitPerTime(@NotNull LimitPerTimeSpec spec) {
        this.spec = spec;
        this.counters = new HashMap<>();

        this.timer = new Timer("Timer-SimpleLimitPerTime", true);
        long millis = spec.getTimeAmountInMillis();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refill();
            }
        }, millis, millis);
    }

    @Override
    public synchronized boolean consume(@NotNull Object key) {
        Long counter = counters.get(key);
        if (counter==null) {
            counters.put(key, spec.getLimit() - 1);
            return true;
        } else {
            if (counter >= 1) {
                counters.put(key, counter - 1);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override @Nullable
    public LimitPerTimeSpec consumeOrSpec(@NotNull Object key) {
        if (consume(key)) {
            return null;
        } else {
            return spec;
        }
    }

    @Override
    public boolean canConsume(@NotNull Object key) {
        Long counter = counters.get(key);
        if (counter==null) {
            return true;
        } else {
            return counter >= 1;
        }
    }

    @Override @Nullable
    public LimitPerTimeSpec canConsumeOrSpec(@NotNull Object key) {
        if (canConsume(key)) {
            return null;
        } else {
            return spec;
        }
    }

    @Override
    public void destroy() {
        timer.cancel();
        counters.clear();
    }

    @NotNull @Override
    public List<LimitPerTimeSpec> getSpec() {
        return Collections.singletonList(spec);
    }

    /**
     * Overwriting to ensure that destroy() is executed. Otherwise the Timer thread lives on.
     */
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    private synchronized void refill() {
        counters.clear();
    }

}
