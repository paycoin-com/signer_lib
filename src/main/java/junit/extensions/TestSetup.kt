package junit.extensions

import junit.framework.Protectable
import junit.framework.Test
import junit.framework.TestResult

/**
 * A Decorator to set up and tear down additional fixture state. Subclass
 * TestSetup and insert it into your tests when you want to set up additional
 * state once before the tests are run.
 */
open class TestSetup(test: Test) : TestDecorator(test) {

    override fun run(result: TestResult) {
        val p = Protectable {
            setUp()
            basicRun(result)
            tearDown()
        }
        result.runProtected(this, p)
    }

    /**
     * Sets up the fixture. Override to set up additional fixture state.
     */
    @Throws(Exception::class)
    protected open fun setUp() {
    }

    /**
     * Tears down the fixture. Override to tear down the additional fixture
     * state.
     */
    @Throws(Exception::class)
    protected open fun tearDown() {
    }
}