package crema.limitpertime;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Combines multiple limits into one.
 *
 * <p>Can contain for example one limit per minute, one per hour and one per day, and only if
 * none is exhausted, then {@link #consume} works.</p>
 *
 * @author Alexei Arshavin, Optimaize
 */
class MultiLimitPerTime implements LimitPerTime {

    private final List<LockableLimitPerTime> timeLimits;

    private MultiLimitPerTime(@NotNull List<LockableLimitPerTime> timeLimits) {
        this.timeLimits = ImmutableList.copyOf(timeLimits);
    }

    static Builder create() {
        return new Builder();
    }
    static class Builder {
        private final List<LockableLimitPerTime> timeLimits = new ArrayList<>();
        public Builder add(long limit, long timeAmount, @NotNull TimeUnit timeUnit) {
            return add(new LimitPerTimeSpec(limit, timeAmount, timeUnit));
        }
        public Builder add(@NotNull LimitPerTimeSpec spec) {
            timeLimits.add(new LockableLimitPerTime(spec));
            return this;
        }
        public MultiLimitPerTime build() {
            return new MultiLimitPerTime(timeLimits);
        }
    }



    @Override
    public boolean consume(@NotNull Object key) {
        return consumeOrSpec(key) == null;
    }

    /**
     * Impl note: method is and must be synchronized.
     */
    @Nullable
    @Override
    public synchronized LimitPerTimeSpec consumeOrSpec(@NotNull Object key) {
        //acquire locks for all:
        for (LockableLimitPerTime timeLimit : timeLimits) {
            timeLimit.lock();
        }

        LimitPerTimeSpec firstLimitReached = null;
        checkAll:
        do {
            //check all: (we need this extra loop, otherwise we'd consume some until one says no.)
            for (LockableLimitPerTime timeLimit : timeLimits) {
                firstLimitReached = timeLimit.lockedCanConsumeOrSpec(key);
                if (firstLimitReached != null) {
                    break checkAll;
                }
            }
            //ok, all accepted, let's do it:
            for (LockableLimitPerTime timeLimit : timeLimits) {
                timeLimit.consume(key); //ignore result because we have checked it already and locked the objects. could assert but nah.
            }
        } while (false);

        //release locks:
        for (LockableLimitPerTime timeLimit : timeLimits) {
            timeLimit.unlock();
        }

        return firstLimitReached;
    }

    @Override
    public boolean canConsume(@NotNull Object key) {
        return canConsumeOrSpec(key) == null;
    }

    /**
     * Impl note: method is and must be synchronized.
     */
    @Nullable @Override
    public synchronized LimitPerTimeSpec canConsumeOrSpec(@NotNull Object key) {
        //acquire locks for all:
        for (LockableLimitPerTime timeLimit : timeLimits) {
            timeLimit.lock();
        }

        LimitPerTimeSpec firstLimitReached = null;
        checkAll:
        do {
            //check all:
            for (LockableLimitPerTime timeLimit : timeLimits) {
                firstLimitReached = timeLimit.lockedCanConsumeOrSpec(key);
                if (firstLimitReached != null) {
                    break checkAll;
                }
            }
        } while (false);

        //release locks:
        for (LockableLimitPerTime timeLimit : timeLimits) {
            timeLimit.unlock();
        }

        return firstLimitReached;
    }

    @Override
    public void destroy() {
        for (LockableLimitPerTime timeLimit : timeLimits) {
            timeLimit.destroy();
        }
    }

    @NotNull @Override
    public List<LimitPerTimeSpec> getSpec() {
        List<LimitPerTimeSpec> ret = new ArrayList<>();
        for (LockableLimitPerTime timeLimit : timeLimits) {
            ret.addAll(timeLimit.getSpec());
        }
        return ret;
    }

}
