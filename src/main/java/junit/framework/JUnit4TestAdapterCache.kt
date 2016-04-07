package junit.framework

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier

class JUnit4TestAdapterCache : HashMap<Description, Test>() {

    fun asTest(description: Description): Test {
        if (description.isSuite) {
            return createTest(description)
        } else {
            if (!containsKey(description)) {
                put(description, createTest(description))
            }
            return get(description)
        }
    }

    internal fun createTest(description: Description): Test {
        if (description.isTest) {
            return JUnit4TestCaseFacade(description)
        } else {
            val suite = TestSuite(description.displayName)
            for (child in description.children) {
                suite.addTest(asTest(child))
            }
            return suite
        }
    }

    fun getNotifier(result: TestResult, adapter: JUnit4TestAdapter): RunNotifier {
        val notifier = RunNotifier()
        notifier.addListener(object : RunListener() {
            @Throws(Exception::class)
            override fun testFailure(failure: Failure) {
                result.addError(asTest(failure.description), failure.exception)
            }

            @Throws(Exception::class)
            override fun testFinished(description: Description) {
                result.endTest(asTest(description))
            }

            @Throws(Exception::class)
            override fun testStarted(description: Description) {
                result.startTest(asTest(description))
            }
        })
        return notifier
    }

    fun asTestList(description: Description): List<Test> {
        if (description.isTest) {
            return Arrays.asList(asTest(description))
        } else {
            val returnThis = ArrayList<Test>()
            for (child in description.children) {
                returnThis.add(asTest(child))
            }
            return returnThis
        }
    }

    companion object {
        private val serialVersionUID = 1L
        val default = JUnit4TestAdapterCache()
    }

}