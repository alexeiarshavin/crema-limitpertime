package crema.limitpertime;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides access to create {@link LimitPerTime} instances.
 *
 * @author Alexei Arshavin, Optimaize
 */
public class LimitsPerTime {

    /**
     * Parses the definition with the {@link LimitPerTimeSpecParser}.
     */
    public static LimitPerTime forString(@NotNull String s) {
        List<LimitPerTimeSpec> parse = LimitPerTimeSpecParser.getInstance().parse(s);
        return multi(parse);
    }

    /**
     * Creates a single limit where everything passes.
     */
    public static LimitPerTime none() {
        return NullLimitPerTime.getInstance();
    }

    /**
     * Creates a single limit.
     * @param timeUnit not smaller than milliseconds.
     */
    public static LimitPerTime single(long limit, long time, TimeUnit timeUnit) {
        return single(new LimitPerTimeSpec(limit, time, timeUnit));
    }
    public static LimitPerTime single(@NotNull LimitPerTimeSpec spec) {
        return new SimpleLimitPerTime(spec);
    }


    /**
     * Parses the definition with the {@link LimitPerTimeSpecParser}.
     */
    public static LimitPerTime multi(@NotNull List<LimitPerTimeSpec> spec) {
        List<LimitPerTimeSpec> sorted = LimitPerTimeSpecs.sortAndValidate(spec);
        MultiLimitPerTime.Builder builder = MultiLimitPerTime.create();
        for (LimitPerTimeSpec data : sorted) {
            builder.add(data);
        }
        return builder.build();
    }

    /**
     * Creates a limit with 1 to n limits in it.
     * If just one then the end result is the same as the {@link #single}.
     * @throws IllegalStateException if none is added. Call LimitsPerTime.none() for that special case manually.
     */
    public static Builder multi() {
        return new Builder();
    }

    public static class Builder {
        private final List<LimitPerTimeSpec> list = new ArrayList<>();

        private Builder() {}

        /**
         * @param timeUnit not smaller than milliseconds.
         */
        public Builder add(long limit, long time, TimeUnit timeUnit) {
            list.add(new LimitPerTimeSpec(limit, time, timeUnit));
            return this;
        }

        public LimitPerTime build() {
            if (list.size()==1) {
                return new SimpleLimitPerTime(list.get(0));
            } else {
                if (list.isEmpty()) {
                    throw new IllegalStateException("Builder is empty, at least one limit is required!");
                }
                return multi(list);
            }
        }

        public int size() {
            return list.size();
        }
    }

}
