package argonotix.agent;

import com.ea.agentloader.AgentLoader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.*;
import static spark.Spark.get;

public class AgentTests {
    private static final long WORK = 50;

    private static MetricWeb web;

    @BeforeClass
    public static void loadAgent() throws InterruptedException {
        AgentLoader.loadAgentClass(MetricAgent.class.getName(), "monitor");
        Thread.sleep(1_500); // wait for agent to attach
        System.out.println("Attached agent");

        web = new MetricWeb();
        web.start();

        get("/null", (req, res) -> null);
        get("/metrix", (req, res) -> {
            Thread.sleep(WORK); // do 'work'
            return "Hello";
        });

        Thread.sleep(1_500); // wait for server to actually start
        System.out.println("Started server");
    }

    @Test
    public void testAgentAgainstNullResponses() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4567/null")
                .build();

        try (Response response = client.newCall(request).execute()) {
            final String body = response.header("X-metric-body");
            assertNotNull(body);
            assertEquals("null should return a 0 length body", 0, Integer.parseInt(body));
        } catch (NumberFormatException | IOException e) {
            fail(e.getMessage()); // any exception here is a failure
        }
    }

    @Test
    public void testAgentWeb() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4567/metrix")
                .build();

        // build up some history
        final int runs = 10;
        for (int i = 0; i < runs; i++) {
            try {
                client.newCall(request).execute();
            } catch (NumberFormatException | IOException e) {
                fail(e.getMessage()); // any exception here is a failure
            }
        }

        try (Response response = client.newCall(new Request.Builder()
                .url("http://localhost:45000/stats")
                .build()).execute()) {
            ResponseBody body = response.body();
            assertNotNull(body);
            assertTrue(body.string().length() > 0);
        } catch (IOException e) {
            fail(e.getMessage()); // any exception here is a failure
        }
    }

    @Test
    public void testAgentMetrics() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4567/metrix")
                .build();

        final int runs = 10;
        for (int i = 0; i < runs; i++) {
            try (Response response = client.newCall(request).execute()) {
                final String time = response.header("X-metric-time");
                final String min = response.header("X-metric-min");
                final String max = response.header("X-metric-min");
                final String avg = response.header("X-metric-avg");
                final String body = response.header("X-metric-body");

                // make sure headers exist
                assertNotNull("time metric should exist", time);
                assertNotNull("max time metric should exist", min);
                assertNotNull("min time metric should exist", max);
                assertNotNull("avg time metric should exist", avg);
                assertNotNull("body size metric should exist", body);

                // length
                assertEquals("Hello".length(), Integer.parseInt(body));

                final double parsedTime = Double.parseDouble(time);
                final double parsedMin = Double.parseDouble(min);
                final double parsedMax = Double.parseDouble(max);
                final double parsedAvg = Double.parseDouble(avg);

                // timing should be more than the artificial limit
                assertTrue(parsedTime >= WORK);
                assertTrue(parsedMax >= WORK);
                assertTrue(parsedMin >= WORK);
                assertTrue(parsedAvg >= WORK);

            } catch (NumberFormatException | IOException e) {
                fail(e.getMessage()); // any exception here is a failure
            }
        }
    }
}
