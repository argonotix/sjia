package argonotix.agent;

import com.google.common.math.StatsAccumulator;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public final class MetricInterceptor {
    static final Map<String, StatsAccumulator> routes = new ConcurrentHashMap<>();
    static final StatsAccumulator all = new StatsAccumulator();
    static final Object updateLock = new Object();

    /**
     * Get or create a statistic accumulator
     *
     * @param path the path for stats
     * @return the stats accumulator
     */
    private static StatsAccumulator getOrCreate(String path) {
        StatsAccumulator accum = routes.get(path);
        if (accum == null) {
            accum = new StatsAccumulator();
            routes.put(path, accum);
        }
        return accum;
    }

    /**
     * Intercept the routes before the method call and perform instrumentation on route
     *
     * @param callable the 'handle' method of the route
     * @param request  the request
     * @param response the response
     * @return the result of the handle method (the response body)
     */
    @RuntimeType
    public Object intercept(
            final @SuperCall Callable<Object> callable,
            final @Argument(0) Request request,
            final @Argument(1) Response response
    ) {
        try {
            final long start = System.currentTimeMillis();
            final Object call = callable.call();
            final long end = System.currentTimeMillis();
            final long time = end - start;

            final String path = request.pathInfo();
            final StatsAccumulator accum = getOrCreate(path);
            synchronized (updateLock) {
                all.add(time);
                accum.add(time);
                response.header("X-metric-time", Long.toString(time));
                response.header("X-metric-min", Double.toString(accum.min()));
                response.header("X-metric-max", Double.toString(accum.max()));
                response.header("X-metric-avg", Double.toString(accum.mean()));
                response.header("X-metric-body", call == null ? "0" : Integer.toString(((String) call).length()));
            }


            return call;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
