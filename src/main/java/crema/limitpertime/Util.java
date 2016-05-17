package crema.limitpertime;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexei Arshavin, Optimaize
 */
class Util {

    static void checkTime(long timeAmount) {
        if (timeAmount < 1) {
            throw new IllegalArgumentException("Time must be >=1 but was: "+timeAmount+"!");
        }
    }

    static void checkLimit(long limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >=1 but was: "+limit+"!");
        }
    }

    static void checkTimeUnit(TimeUnit timeUnit) {
        if (timeUnit==TimeUnit.NANOSECONDS || timeUnit==TimeUnit.MICROSECONDS) {
            throw new IllegalArgumentException("Milliseconds is the lowest permitted time unit!");
        }
    }

}
