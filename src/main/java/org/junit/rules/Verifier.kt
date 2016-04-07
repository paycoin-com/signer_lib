package org.junit.rules

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Verifier is a base class for Rules like ErrorCollector, which can turn
 * otherwise passing test methods into failing tests if a verification check is
 * failed

 *
 * public static class ErrorLogVerifier {
 * private ErrorLog errorLog = new ErrorLog();

 * &#064;Rule
 * public Verifier verifier = new Verifier() {
 * &#064;Override public void verify() {
 * assertTrue(errorLog.isEmpty());
 * }
 * }

 * &#064;Test public void testThatMightWriteErrorLog() {
 * // ...
 * }
 * }
 *

 * @since 4.7
 */
abstract class Verifier : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                base.evaluate()
                verify()
            }
        }
    }

    /**
     * Override this to add verification logic. Overrides should throw an
     * exception to indicate that verification failed.
     */
    @Throws(Throwable::class)
    protected open fun verify() {
    }
}
