package org.junit.rules

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A base class for Rules (like TemporaryFolder) that set up an external
 * resource before a test (a file, socket, server, database connection, etc.),
 * and guarantee to tear it down afterward:

 *
 * public static class UsesExternalResource {
 * Server myServer= new Server();

 * &#064;Rule
 * public ExternalResource resource= new ExternalResource() {
 * &#064;Override
 * protected void before() throws Throwable {
 * myServer.connect();
 * };

 * &#064;Override
 * protected void after() {
 * myServer.disconnect();
 * };
 * };

 * &#064;Test
 * public void testFoo() {
 * new Client().run(myServer);
 * }
 * }
 *

 * @since 4.7
 */
abstract class ExternalResource : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return statement(base)
    }

    private fun statement(base: Statement): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                before()
                try {
                    base.evaluate()
                } finally {
                    after()
                }
            }
        }
    }

    /**
     * Override to set up your specific external resource.

     * @throws Throwable if setup fails (which will disable `after`
     */
    @Throws(Throwable::class)
    protected open fun before() {
        // do nothing
    }

    /**
     * Override to tear down your specific external resource.
     */
    protected open fun after() {
        // do nothing
    }
}
