package org.junit.rules

import org.junit.internal.AssumptionViolatedException
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * TestWatchman is a base class for Rules that take note of the testing
 * action, without modifying it. For example, this class will keep a log of each
 * passing and failing test:

 *
 * public static class WatchmanTest {
 * private static String watchedLog;

 * &#064;Rule
 * public MethodRule watchman= new TestWatchman() {
 * &#064;Override
 * public void failed(Throwable e, FrameworkMethod method) {
 * watchedLog+= method.getName() + &quot; &quot; + e.getClass().getSimpleName()
 * + &quot;\n&quot;;
 * }

 * &#064;Override
 * public void succeeded(FrameworkMethod method) {
 * watchedLog+= method.getName() + &quot; &quot; + &quot;success!\n&quot;;
 * }
 * };

 * &#064;Test
 * public void fails() {
 * fail();
 * }

 * &#064;Test
 * public void succeeds() {
 * }
 * }
 *

 * @since 4.7
 * *
 */
@Deprecated("")
@Deprecated("Use {@link TestWatcher} (which implements {@link TestRule}) instead.")
class TestWatchman : MethodRule {
    override fun apply(base: Statement, method: FrameworkMethod,
                       target: Any): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                starting(method)
                try {
                    base.evaluate()
                    succeeded(method)
                } catch (e: AssumptionViolatedException) {
                    throw e
                } catch (e: Throwable) {
                    failed(e, method)
                    throw e
                } finally {
                    finished(method)
                }
            }
        }
    }

    /**
     * Invoked when a test method succeeds
     */
    fun succeeded(method: FrameworkMethod) {
    }

    /**
     * Invoked when a test method fails
     */
    fun failed(e: Throwable, method: FrameworkMethod) {
    }

    /**
     * Invoked when a test method is about to start
     */
    fun starting(method: FrameworkMethod) {
    }


    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    fun finished(method: FrameworkMethod) {
    }
}
