package org.junit.rules

import org.junit.Rule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * A MethodRule is an alteration in how a test method is run and reported.
 * Multiple [MethodRule]s can be applied to a test method. The
 * [Statement] that executes the method is passed to each annotated
 * [Rule] in turn, and each may return a substitute or modified
 * [Statement], which is passed to the next [Rule], if any. For
 * examples of how this can be useful, see these provided MethodRules,
 * or write your own:

 *
 *  * [ErrorCollector]: collect multiple errors in one test method
 *  * [ExpectedException]: make flexible assertions about thrown exceptions
 *  * [ExternalResource]: start and stop a server, for example
 *  * [TemporaryFolder]: create fresh files, and delete after test
 *  * [TestName]: remember the test name for use during the method
 *  * [TestWatchman]: add logic at events during method execution
 *  * [Timeout]: cause test to fail after a set time
 *  * [Verifier]: fail test if object state ends up incorrect
 *

 * Note that [MethodRule] has been replaced by [TestRule],
 * which has the added benefit of supporting class rules.

 * @since 4.7
 */
interface MethodRule {
    /**
     * Modifies the method-running [Statement] to implement an additional
     * test-running rule.

     * @param base The [Statement] to be modified
     * *
     * @param method The method to be run
     * *
     * @param target The object on which the method will be run.
     * *
     * @return a new statement, which may be the same as `base`,
     * *         a wrapper around `base`, or a completely new Statement.
     */
    fun apply(base: Statement, method: FrameworkMethod, target: Any): Statement
}
