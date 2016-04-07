package junit.extensions

import junit.framework.Test
import junit.framework.TestResult

/**
 * A Decorator that runs a test repeatedly.
 */
class RepeatedTest(test: Test, private val fTimesRepeat: Int) : TestDecorator(test) {

    init {
        if (fTimesRepeat < 0) {
            throw IllegalArgumentException("Repetition count must be >= 0")
        }
    }

    override fun countTestCases(): Int {
        return super.countTestCases() * fTimesRepeat
    }

    override fun run(result: TestResult) {
        for (i in 0..fTimesRepeat - 1) {
            if (result.shouldStop()) {
                break
            }
            super.run(result)
        }
    }

    override fun toString(): String {
        return super.toString() + "(repeated)"
    }
}