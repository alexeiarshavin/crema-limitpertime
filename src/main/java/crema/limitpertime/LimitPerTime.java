package crema.limitpertime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Functionality for enforcing a limit per time.
 *
 * <p>Example use:
 * Want to grant a certain 'user' to access a certain 'resource', but only a limited number of times per
 * time range, eg 10 times in 5 minutes, 100 times in 1 hour, and 1000 times in a day.</p>
 *
 * <p>
 * The first step is to create the limitation definition:<br>
 * <pre><code>
 * LimitPerTime limitPerTime = LimitsPerTime.forString("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");
 * </code></pre>
 * </p>
 *
 * <p>Then you need a key object that identifies your use case. It can be the user's identifier.<br>
 * And with that you see if he's within the limits:<br>
 * <pre><code>
 * if (limitPerTime.consume("username")) {
 *     //ok, it was counted, go ahead
 * }
 * </code></pre>
 * </p>
 *
 * <p>The ones that consumed within the current time interval are automatically cleared
 * at the end of the interval.</p>
 *
 * @author Alexei Arshavin, Optimaize
 */
public interface LimitPerTime {

    /**
     * Returns true if consumed, false if the limit has been reached already in this time interval.
     * @param key Anything to identify that "user" or "use case".
     *            If you have just one, use the same string on each call.
     * @see #consumeOrSpec(Object) it returns more than just boolean.
     */
    boolean consume(@NotNull Object key);

    /**
     * Returns the first time spec that hit the limit, or null if all passed and it was consumed.
     */
    @Nullable
    LimitPerTimeSpec consumeOrSpec(@NotNull Object key);

    /**
     * @see #canConsumeOrSpec(Object) it returns more than just boolean.
     */
    boolean canConsume(@NotNull Object key);

    /**
     * Returns the first time spec that hit the limit, or null if it's consumable.
     */
    @Nullable
    LimitPerTimeSpec canConsumeOrSpec(@NotNull Object key);


    /**
     * Call this when done to free resources.
     */
    void destroy();

    @NotNull
    List<LimitPerTimeSpec> getSpec();

}
