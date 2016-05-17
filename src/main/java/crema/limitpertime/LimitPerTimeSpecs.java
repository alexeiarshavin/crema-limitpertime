package crema.limitpertime;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Static utility methods for working with {@link LimitPerTimeSpec}s.
 *
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpecs {

    /**
     * @return Sorted from shortest time to longest time.
     *         Does not modify the input.
     */
    @NotNull
    public static List<LimitPerTimeSpec> sort(@NotNull Collection<LimitPerTimeSpec> in) {
        List<LimitPerTimeSpec> ret = new ArrayList<>(in);
        Collections.sort(ret);
        return ret;
    }

    /**
     * Calls {@link #sort} and then makes sure the items are logical in order.
     *
     * <p><pre>
     * Example: when allowing 5 per 1 minute, then allowing 20 per 1 hour makes sense.
     *          but allowing 3 per 1 day does not - the shorter limits would be useless.
     *          this is a user misconfiguration, a bug, and this method throws with a
     *          meaningful text.
     * </pre></p>
     *
     * <p>Duplicate values are not allowed either. They are user misconfigs also, and could hide
     * a constraint by mistake. Copy paste errors n'stuff.<br>
     * Example: "5 in 60 seconds" and "5 in 1 minute" is a duplicate.</p>
     *
     * @throws IllegalArgumentException if a duplicate definition was found, or a logical error is in the definitions.
     *         A duplicate definition could happen also in the form of 5 seconds vs 5000 milliseconds.
     *         A logical error would be to allow less in a longer time than is already allowed for a shorter time.
     */
    @NotNull
    public static List<LimitPerTimeSpec> sortAndValidate(@NotNull Collection<LimitPerTimeSpec> in) {
        List<LimitPerTimeSpec> sorted = sort(in);
        LimitPerTimeSpec last = null;
        for (LimitPerTimeSpec thisOne : sorted) {
            if (last!=null) {
                if (last.equals(thisOne)) {
                    throw new IllegalArgumentException("Duplicate definitions: "+last+" / "+thisOne);
                }
                if (last.getLimit() > thisOne.getLimit()) {
                    throw new IllegalArgumentException("Longer time limit more restrictive than shorter: "+last+" / "+thisOne);
                }
            }
            last = thisOne;
        }
        return sorted;
    }

    /**
     * Ensures that the {@code spec} does not contain a rule that has a longer time spawn than the given.
     *
     * <p>This is useful to ensure a LimitPerTime is not constructed with a very long one, because the object
     * needs to remember the consumption counters for that long.</p>
     *
     * @param spec must be {@link #sort}ed already
     * @throws RuntimeException if it has a longer one
     */
    public static void restrictMaxTime(@NotNull List<LimitPerTimeSpec> spec, long maxAmount, @NotNull TimeUnit timeUnit) {
        if (spec.isEmpty()) return;
        LimitPerTimeSpec last = spec.get(spec.size() - 1);
        if (last.getTimeAmountInMillis() > timeUnit.toMillis(maxAmount)) {
            throw new RuntimeException("Specification "+last+" is longer than max permitted "+maxAmount+" "+timeUnit+"!");
        }
    }

}
