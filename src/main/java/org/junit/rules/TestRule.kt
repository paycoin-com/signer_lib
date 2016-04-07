package org.junit.rules

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A TestRule is an alteration in how a test method, or set of test methods,
 * is run and reported.  A [TestRule] may add additional checks that cause
 * a test that would otherwise fail to pass, or it may perform necessary setup or
 * cleanup for tests, or it may observe test execution to report it elsewhere.
 * [TestRule]s can do everything that could be done previously with
 * methods annotated with [org.junit.Before],
 * [org.junit.After], [org.junit.BeforeClass], or
 * [org.junit.AfterClass], but they are more powerful, and more easily
 * shared
 * between projects and classes.

 * The default JUnit test runners for suites and
 * individual test cases recognize [TestRule]s introduced in two different
 * ways.  [org.junit.Rule] annotates method-level
 * [TestRule]s, and [org.junit.ClassRule]
 * annotates class-level [TestRule]s.  See Javadoc for those annotations
 * for more information.

 * Multiple [TestRule]s can be applied to a test or suite execution. The
 * [Statement] that executes the method or suite is passed to each annotated
 * [org.junit.Rule] in turn, and each may return a substitute or modified
 * [Statement], which is passed to the next [org.junit.Rule], if any. For
 * examples of how this can be useful, see these provided TestRules,
 * or write your own:

 *
 *  * [ErrorCollector]: collect multiple errors in one test method
 *  * [ExpectedException]: make flexible assertions about thrown exceptions
 *  * [ExternalResource]: start and stop a server, for example
 *  * [TemporaryFolder]: create fresh files, and delete after test
 *  * [TestName]: remember the test name for use during the method
 *  * [TestWatcher]: add logic at events during method execution
 *  * [Timeout]: cause test to fail after a set time
 *  * [Verifier]: fail test if object state ends up incorrect
 *

 * @since 4.9
 */
interface TestRule {
    /**
     * Modifies the method-running [Statement] to implement this
     * test-running rule.

     * @param base The [Statement] to be modified
     * *
     * @param description A [Description] of the test implemented in `base`
     * *
     * @return a new statement, which may be the same as `base`,
     * *         a wrapper around `base`, or a completely new Statement.
     */
    fun apply(base: Statement, description: Description): Statement
}
