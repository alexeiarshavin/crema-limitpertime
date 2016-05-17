package crema.limitpertime;

import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexei Arshavin, Optimaize
 */
public class LimitPerTimeSpecsTest {

    @Test
    public void restrictMaxTime() throws Exception {
        List<LimitPerTimeSpec> spec = LimitPerTimeSpecParser.getInstance().parseSortValidate("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");
        LimitPerTimeSpecs.restrictMaxTime(spec, 1, TimeUnit.DAYS);
        LimitPerTimeSpecs.restrictMaxTime(spec, 24, TimeUnit.HOURS);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void restrictMaxTime_over() throws Exception {
        List<LimitPerTimeSpec> spec = LimitPerTimeSpecParser.getInstance().parseSortValidate("10 in 5 MINUTES; 100 in 1 HOUR; 1000 in 1day");
        LimitPerTimeSpecs.restrictMaxTime(spec, 23, TimeUnit.HOURS);
    }

}