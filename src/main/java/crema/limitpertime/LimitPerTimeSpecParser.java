package crema.limitpertime;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Reads a {@link LimitPerTime} specification in String form.
 *
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpecParser {

    private static final Splitter SEMI_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private static final Splitter IN_SPLITTER = Splitter.on(Pattern.compile("in", Pattern.CASE_INSENSITIVE)).trimResults().omitEmptyStrings().limit(2); //limit 2 because mINutes contains that string

    private static final LimitPerTimeSpecParser INSTANCE = new LimitPerTimeSpecParser();
    public static LimitPerTimeSpecParser getInstance() {
        return INSTANCE;
    }
    private LimitPerTimeSpecParser() {
    }

    /**
     * Example syntax: "10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day"
     *
     * <p><pre>Time units are the ones from {@link TimeUnit}, plus these shortcuts are allowed:
     *   ms = milliseconds
     *   s  = seconds
     *   m  = minutes
     *   h  = hours
     *   d  = days
     * </pre></p>
     *
     * <p>The smallest permitted unit is milliseconds.</p>
     *
     * <p>The method works case insensitive.</p>
     *
     * <p>Does not perform logical validation, see {@link #parseSortValidate(String)}.</p>
     *
     * @param s empty is permitted by definition.
     * @return possibly an empty limit if the string contains no definition.
     * @throws IllegalArgumentException on illegal syntax.
     */
    @NotNull
    public List<LimitPerTimeSpec> parse(@NotNull String s) {
        List<String> strings = SEMI_SPLITTER.splitToList(s);
        if (strings.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<LimitPerTimeSpec> ret = new ArrayList<>();
            for (String s1 : strings) {
                List<String> parts = IN_SPLITTER.splitToList(s1);
                if (parts.size() != 2) {
                    throw new IllegalArgumentException("Invalid part: >>>"+s1+"<<<!");
                }
                long limit = Long.parseLong(parts.get(0), 10);

                String part2 = parts.get(1);
                int i = 0;
                for (; i<part2.length(); i++) {
                    if (!Character.isDigit(part2.charAt(i))) {
                        break;
                    }
                }
                int timeAmount = Integer.parseInt(part2.substring(0, i), 10);
                TimeUnit timeUnit = parseTimeUnit(part2.substring(i).trim().toUpperCase(Locale.ENGLISH));

                ret.add(new LimitPerTimeSpec(limit, timeAmount, timeUnit));
            }

            return ret;
        }
    }

    /**
     * Calls {@link #parse(String)} followed by {@link LimitPerTimeSpecs#sortAndValidate}.
     */
    @NotNull
    public List<LimitPerTimeSpec> parseSortValidate(@NotNull String s) {
        return LimitPerTimeSpecs.sortAndValidate( parse(s) );
    }


    /**
     * @throws IllegalArgumentException
     */
    private TimeUnit parseTimeUnit(String upper) {
        switch (upper) {
            case "MS":
            case "MILLISECOND":
            case "MILLISECONDS":
                return TimeUnit.SECONDS;
            case "S":
            case "SECOND":
            case "SECONDS":
                return TimeUnit.SECONDS;
            case "M":
            case "MINUTE":
            case "MINUTES":
                return TimeUnit.MINUTES;
            case "H":
            case "HOUR":
            case "HOURS":
                return TimeUnit.HOURS;
            case "D":
            case "DAY":
            case "DAYS":
                return TimeUnit.DAYS;
            default:
                return TimeUnit.valueOf(upper);
        }
    }

}
