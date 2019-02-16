package argonotix.agent;

import com.google.common.math.StatsAccumulator;
import spark.Service;

import static spark.Service.ignite;

final class MetricWeb {
    private Service http;

    /**
     * Start the stats server
     */
    void start() {
        http = ignite();
        http.port(45000);

        http.get("/stats", (req, res) -> {
            res.type("application/json");

            StringBuilder json = new StringBuilder();
            synchronized (MetricInterceptor.updateLock) {
                json.append("{\"*\":").append(toString(MetricInterceptor.all));
                MetricInterceptor.routes.forEach((route, acc) -> {
                    json.append(",");
                    json.append('"').append(route).append("\": ").append(toString(acc));
                });
                if (!MetricInterceptor.routes.isEmpty()) {
                    json.deleteCharAt(json.length() - 1);
                }
                json.append("}}");
            }
            return json.toString();
        });
    }

    private static String toString(StatsAccumulator acc) {
        return "{\"max\":" + acc.max() + ",\"min\":" + acc.min() + ",\"avg\":" + acc.mean() + "}";
    }


    /**
     * Stop the stats server
     */
    void stop() {
        http.stop();
    }
}
