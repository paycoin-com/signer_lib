package junit.extensions

import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestResult

/**
 * A Decorator for Tests. Use TestDecorator as the base class for defining new
 * test decorators. Test decorator subclasses can be introduced to add behaviour
 * before or after a test is run.
 */
open class TestDecorator(test: Test) : Assert(), Test {
    var test: Test
        protected set

    init {
        this.test = test
    }

    /**
     * The basic run behaviour.
     */
    fun basicRun(result: TestResult) {
        test.run(result)
    }

    override fun countTestCases(): Int {
        return test.countTestCases()
    }

    override fun run(result: TestResult) {
        basicRun(result)
    }

    override fun toString(): String {
        return test.toString()
    }
}