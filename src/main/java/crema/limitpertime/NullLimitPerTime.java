package crema.limitpertime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Has no limit, accepts all.
 *
 * @author Alexei Arshavin, Optimaize
 */
class NullLimitPerTime implements LimitPerTime {

    private static final NullLimitPerTime INSTANCE = new NullLimitPerTime();
    public static NullLimitPerTime getInstance() {
        return INSTANCE;
    }
    private NullLimitPerTime() {
    }

    @Override
    public boolean consume(@NotNull Object key) {
        return true;
    }

    @Nullable
    @Override
    public LimitPerTimeSpec consumeOrSpec(@NotNull Object key) {
        return null;
    }

    @Override
    public boolean canConsume(@NotNull Object key) {
        return true;
    }

    @Nullable
    @Override
    public LimitPerTimeSpec canConsumeOrSpec(@NotNull Object key) {
        return null;
    }

    @Override
    public void destroy() {
    }

    @NotNull
    @Override
    public List<LimitPerTimeSpec> getSpec() {
        return Collections.emptyList();
    }
}
