/*
 *
 * The MIT License (MIT)
 *
 * Copyright (c) $year Christopher Kies
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
import spark.Service;

import java.util.Map;

import static spark.Service.ignite;

/**
 * MetricWeb is the WebApp of the monitoring service for the Agent
 */
final class MetricWeb {
    private Service http;

    /**
     * Start the stats server
     */
    void start() {
        http = ignite();
        http.port(45000);
        http.staticFiles.location("/monitor");

        http.get("/stats", (req, res) -> {
            res.type("application/json");

            StringBuilder json = new StringBuilder("{\"sizes\":{");
            addStats(MetricInterceptor.routeSizes, json);
            json.append("},\"times\":{");
            addStats(MetricInterceptor.routeTimes, json);
            json.append("}}");
            return json.toString();
        });

        System.out.println("Started agent monitor on http://localhost:45000");

    }

    /**
     * Converts the current min/max/avg in a StatsAccumulator to the representation in JSON
     *
     * @param stats the stats to convert
     * @param json  the converted JSON
     */
    private void addStats(Map<String, StatsAccumulator> stats, StringBuilder json) {
        stats.forEach((route, acc) -> {
            json.append('"').append(route).append("\":");
            synchronized (acc) {
                json.append(toString(acc));
            }
            json.append(',');
        });
        if (!stats.isEmpty()) {
            json.deleteCharAt(json.length() - 1);
        }
    }

    /**
     * Generate JSON to represent max,min,avg
     *
     * @param acc the accumulator to gather from
     * @return the JSON
     */
    private static String toString(StatsAccumulator acc) {
        return "{\"max\":" + String.format("%.2f", acc.max()) + ",\"min\":" + String.format("%.2f", acc.min()) + ",\"avg\":" + String.format("%.2f", acc.mean()) + "}";
    }


    /**
     * Stop the stats server
     */
    void stop() {
        http.stop();
    }
}
