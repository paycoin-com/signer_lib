package org.junit.rules

import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * The `DisableOnDebug` Rule allows you to label certain rules to be
 * disabled when debugging.
 *
 *
 * The most illustrative use case is for tests that make use of the
 * [Timeout] rule, when ran in debug mode the test may terminate on
 * timeout abruptly during debugging. Developers may disable the timeout, or
 * increase the timeout by making a code change on tests that need debugging and
 * remember revert the change afterwards or rules such as [Timeout] that
 * may be disabled during debugging may be wrapped in a `DisableOnDebug`.
 *
 *
 * The important benefit of this feature is that you can disable such rules
 * without any making any modifications to your test class to remove them during
 * debugging.
 *
 *
 * This does nothing to tackle timeouts or time sensitive code under test when
 * debugging and may make this less useful in such circumstances.
 *
 *
 * Example usage:

 *
 * public static class DisableTimeoutOnDebugSampleTest {

 * &#064;Rule
 * public TestRule timeout = new DisableOnDebug(new Timeout(20));

 * &#064;Test
 * public void myTest() {
 * int i = 0;
 * assertEquals(0, i); // suppose you had a break point here to inspect i
 * }
 * }
 *

 * @since 4.12
 */
class DisableOnDebug
/**
 * Visible for testing purposes only.

 * @param rule the rule to disable during debugging
 * *
 * @param inputArguments
 * *            arguments provided to the Java runtime
 */
internal constructor(private val rule: TestRule, inputArguments: List<String>) : TestRule {
    /**
     * Returns `true` if the JVM is in debug mode. This method may be used
     * by test classes to take additional action to disable code paths that
     * interfere with debugging if required.

     * @return `true` if the current JVM is in debug mode, `false`
     * *         otherwise
     */
    val isDebugging: Boolean

    /**
     * Create a `DisableOnDebug` instance with the timeout specified in
     * milliseconds.

     * @param rule to disable during debugging
     */
    constructor(rule: TestRule) : this(rule, ManagementFactory.getRuntimeMXBean().inputArguments) {
    }

    init {
        isDebugging = isDebugging(inputArguments)
    }

    /**
     * @see TestRule.apply
     */
    override fun apply(base: Statement, description: Description): Statement {
        if (isDebugging) {
            return base
        } else {
            return rule.apply(base, description)
        }
    }

    /**
     * Parses arguments passed to the runtime environment for debug flags
     *
     *
     * Options specified in:
     *
     *  *
     * [javase-6](http://docs.oracle.com/javase/6/docs/technotes/guides/jpda/conninv.html#Invocation)
     *  * [javase-7](http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/conninv.html#Invocation)
     *  * [javase-8](http://docs.oracle.com/javase/8/docs/technotes/guides/jpda/conninv.html#Invocation)


     * @param arguments
     * *            the arguments passed to the runtime environment, usually this
     * *            will be [RuntimeMXBean.getInputArguments]
     * *
     * @return true if the current JVM was started in debug mode, false
     * *         otherwise.
     */
    private fun isDebugging(arguments: List<String>): Boolean {
        for (argument in arguments) {
            if ("-Xdebug" == argument) {
                return true
            } else if (argument.startsWith("-agentlib:jdwp")) {
                return true
            }
        }
        return false
    }

}
