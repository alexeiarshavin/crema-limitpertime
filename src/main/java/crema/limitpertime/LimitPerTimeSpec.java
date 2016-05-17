package crema.limitpertime;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Encapsulates the immutable definition of a limit-per-time constraint.
 *
 * <p>Equals/hashCode are defined on a per per value in milliseconds basis.
 * Therefore "5 in 1 day" is the same as "5 in 24 hours" etc.</p>
 *
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpec implements Comparable<LimitPerTimeSpec> {

    private final long limit;
    private final long timeAmount;
    @NotNull
    private final TimeUnit timeUnit;

    /**
     * @param limit > 0
     * @param timeAmount > 0
     * @param timeUnit not smaller than milliseconds.
     */
    public LimitPerTimeSpec(long limit, long timeAmount, @NotNull TimeUnit timeUnit) {
        Util.checkLimit(limit);
        Util.checkTime(timeAmount);
        Util.checkTimeUnit(timeUnit);

        this.limit = limit;
        this.timeAmount = timeAmount;
        this.timeUnit = timeUnit;
    }

    public long getLimit() {
        return limit;
    }

    public long getTimeAmount() {
        return timeAmount;
    }

    public long getTimeAmountInMillis() {
        return timeUnit.toMillis(timeAmount);
    }

    @NotNull
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public String toString() {
        return "LimitPerTimeSpec{" +
                "limit=" + limit +
                ", timeAmount=" + timeAmount +
                ", timeUnit=" + timeUnit +
                '}';
    }


    /**
     * See class header.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LimitPerTimeSpec that = (LimitPerTimeSpec) o;

        if (limit != that.limit) return false;
        if (getTimeAmountInMillis() != that.getTimeAmountInMillis()) return false;

        return true;
    }
    /**
     * See class header.
     */
    @Override
    public int hashCode() {
        long ms = getTimeAmountInMillis();
        int result = (int) (limit ^ (limit >>> 32));
        result = 31 * result + (int) (ms ^ (ms >>> 32));
        return result;
    }

    /**
     * The one with the smaller time (in milliseconds) comes first.
     * In case of same time, the {@code limit} decides (smaller comes first).
     * If equal then this method returns 0 for equal.
     */
    @Override
    public int compareTo(LimitPerTimeSpec o) {
        int compare = Long.compare(this.getTimeAmountInMillis(), o.getTimeAmountInMillis());
        if (compare!=0) return compare;

        compare = Long.compare(this.limit, o.limit);
        if (compare!=0) return compare;

        return 0;
    }
}
