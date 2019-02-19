/*
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Christopher Kies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

/**
 * MetricInterceptor attaches around all spark.Route instances, adding metrics.
 */
public final class MetricInterceptor {
    static final Map<String, StatsAccumulator> routeTimes = new ConcurrentHashMap<>();
    static final Map<String, StatsAccumulator> routeSizes = new ConcurrentHashMap<>();

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
            final String path = request.pathInfo();

            final long start = System.currentTimeMillis();
            final Object body = callable.call();
            final long time = System.currentTimeMillis() - start;
            final long bodySize = body == null ? 0 : ((String) body).length();

            addTimeHeaders(response, path, time);
            addBodyHeaders(response, path, bodySize);

            return body;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add response headers for time measurements
     *
     * @param response the response to add to
     * @param path     the path of the request
     * @param time     the timestamp
     */
    private static void addTimeHeaders(Response response, String path, long time) {
        final StatsAccumulator times = routeTimes.computeIfAbsent(path, k -> new StatsAccumulator());
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (times) {
            times.add(time);
            response.header("X-metric-time-min", String.format("%.2f", times.min()));
            response.header("X-metric-time-max", String.format("%.2f", times.max()));
            response.header("X-metric-time-avg", String.format("%.2f", times.mean()));
        }
        response.header("X-metric-time", Long.toString(time));
    }

    /**
     * Add response headers for body size measurements
     *
     * @param response the response to add to
     * @param path     the path of the request
     * @param bodySize the response body size
     */
    private static void addBodyHeaders(Response response, String path, long bodySize) {
        final StatsAccumulator bodies = routeSizes.computeIfAbsent(path, k -> new StatsAccumulator());
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (bodies) {
            bodies.add(bodySize);
            response.header("X-metric-body-min", String.format("%.2f", bodies.min()));
            response.header("X-metric-body-max", String.format("%.2f", bodies.max()));
            response.header("X-metric-body-avg", String.format("%.2f", bodies.mean()));
        }
        response.header("X-metric-body", Long.toString(bodySize));
    }
}
