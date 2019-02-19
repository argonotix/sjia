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

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * The Java Agent environment setup
 */
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
        System.out.println("Connecting agent");
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

        if (arguments != null && arguments.equals("monitor")) {
            System.out.println("Starting SJIA with monitor");
            new MetricWeb().start();
        } else {
            System.out.println("Starting SJIA without monitor");
        }
    }
}
