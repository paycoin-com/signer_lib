package junit.framework

import org.junit.runner.Describable
import org.junit.runner.Description

class JUnit4TestCaseFacade internal constructor(private val fDescription: Description) : Test, Describable {

    override fun toString(): String {
        return description.toString()
    }

    override fun countTestCases(): Int {
        return 1
    }

    override fun run(result: TestResult) {
        throw RuntimeException(
                "This test stub created only for informational purposes.")
    }

    override fun getDescription(): Description {
        return fDescription
    }
}