package crema.limitpertime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Uses a lock instead of Java's synchronization.
 *
 * <p>This allows the external to also acquire the lock. This is used by the {@link MultiLimitPerTime} class
 * to work on multiple limits in an atomic way.</p>
 *
 * @author Alexei Arshavin, Optimaize
 */
class LockableLimitPerTime implements LimitPerTime {

    @NotNull
    private final LimitPerTimeSpec spec;
    private final Map<Object,Long> counters;
    private final ReentrantLock lock;
    private final Timer timer;

    public LockableLimitPerTime(long limit, long timeAmount, @NotNull TimeUnit timeUnit) {
        this(new LimitPerTimeSpec(limit, timeAmount, timeUnit));
    }
    public LockableLimitPerTime(@NotNull LimitPerTimeSpec spec) {
        this.spec = spec;
        this.counters = new HashMap<>();
        this.lock = new ReentrantLock();

        this.timer = new Timer("Timer-LockableLimitPerTime", true);
        long millis = spec.getTimeAmountInMillis();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refill();
            }
        }, millis, millis);
    }

    /**
     */
    @Override
    public boolean consume(@NotNull Object key) {
        lock.lock();
        try {
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
        } finally{
            lock.unlock();
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

    /**
     * Call this to see, before calling {@link #doConsume}.
     * You must have acquired the {@link #lock()} before!
     * @param key Same as for {@link #consume}
     * @return true if {@link #consume} would have worked at that time of calling.
     * @throws IllegalStateException if not {@link #lock locked}.
     */
    public boolean lockedCanConsume(Object key) {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Lock must be acquired by current thread!");
        }
        return canConsume(key);
    }

    /**
     * Uses {@link #lockedCanConsume} so look there, and then returns like {@link #canConsumeOrSpec}.
     */
    @Nullable
    public LimitPerTimeSpec lockedCanConsumeOrSpec(Object key) {
        if (lockedCanConsume(key)) {
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

    /**
     * Call this after checking with {@link #lockedCanConsume}.
     * @param key Same as for {@link #consume}
     * @throws IllegalStateException if not {@link #lock locked} or not {@link #lockedCanConsume}.
     */
    public void doConsume(Object key) {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Lock must be acquired by current thread!");
        }
        Long counter = counters.get(key);
        if (counter==null) {
            counters.put(key, spec.getLimit() - 1);
        } else {
            if (counter >= 1) {
                counters.put(key, counter - 1);
            } else {
                throw new IllegalStateException("Cannot consume, check with canConsume() first!");
            }
        }
    }

    /**
     * Gives you a frozen consistent state.
     * Wile you hold the lock, no one else can {@link #consume}, {@link #lockedCanConsume} or {@link #doConsume}.
     * Also, the limit is not reset (the Timer is locked out also).
     */
    public void lock() {
        lock.lock();
    }

    /**
     * You must call this when you're done after you had acquired the {@link #lock}.
     */
    public void unlock() {
        lock.unlock();
    }

    /**
     */
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

    private void refill() {
        lock.lock();
        try {
            counters.clear();
        } finally{
            lock.unlock();
        }
    }

}
