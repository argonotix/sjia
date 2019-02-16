package argonotix.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public final class MetricAgent {
    /**
     * Called from EA Agent Loader (for tests)
     *
     * @param arguments       agent arguments from commandline
     * @param instrumentation java instrumentation class
     */
    public static void agentmain(String arguments, Instrumentation instrumentation) {
        installAgent(arguments, instrumentation, new MetricInterceptor());
    }

    /**
     * Called when running as a JavaAgent from the command line
     *
     * @param arguments       agent arguments from commandline
     * @param instrumentation java instrumentation class
     */
    public static void premain(String arguments, Instrumentation instrumentation) {
        installAgent(arguments, instrumentation, new MetricInterceptor());
    }

    /**
     * Install the metric agent to the routes in the JVM
     *
     * @param instrumentation java instrumentation class
     */
    private static void installAgent(
            final String arguments,
            final Instrumentation instrumentation,
            final MetricInterceptor interceptor
    ) {
        if (arguments != null && arguments.equals("monitor")) {
            new MetricWeb();
        }
        try {
            new AgentBuilder.Default()
                    .type(ElementMatchers.isSubTypeOf(Class.forName("spark.Route")))
                    .transform(
                            (builder, type, classLoader, module) -> builder
                                    .method(ElementMatchers.named("handle"))
                                    .intercept(MethodDelegation.to(interceptor))
                    ).installOn(instrumentation);
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to find spark route class!");
            System.exit(1);
        }
    }
}
