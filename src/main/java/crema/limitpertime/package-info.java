/**
 * See the LimitPerTime interface and the unit tests to get started.
 *
 *
 * <h2>ABOUT MEMORY CONSUMPTION</h2>
 *
 * <p>The implementations use a standard Java HashMap to remember the Object key as given in LimitPerTime.consume()
 * plus the counter as a Double for each key ever used, until the time range expires and it is cleared.</p>
 *
 * <p>Having very long time ranges means keeping all those keys and counters for a long time.</p>
 *
 * <p>Hint: use small/cheap Object keys, and try to limit the maximal time.</p>
 *
 *
 *
 * <h2>ABOUT TIMERS</h2>
 *
 * <p>The implementations use old style Java timers for clearing the counters.</p>
 *
 * <p>There is one timer instance per time range definition.</p>
 *
 * <p>The timers are created with the daemon flag, therefore they don't stop your application from exiting normally.</p>
 *
 *
 *
 * <h2>ABOUT TIME UNITS</h2>
 *
 * <p>The smallest permitted time range unit is MILLISECONDS.</p>
 *
 * <p>MICROSECONDS and NANOSECONDS are not supported.</p>
 *
 *
 */
package crema.limitpertime;