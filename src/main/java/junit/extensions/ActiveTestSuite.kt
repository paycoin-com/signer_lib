package junit.extensions

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestResult
import junit.framework.TestSuite

/**
 * A TestSuite for active Tests. It runs each
 * test in a separate thread and waits until all
 * threads have terminated.
 * -- Aarhus Radisson Scandinavian Center 11th floor
 */
class ActiveTestSuite : TestSuite {
    @Volatile private var fActiveTestDeathCount: Int = 0

    constructor() {
    }

    constructor(theClass: Class<out TestCase>) : super(theClass) {
    }

    constructor(name: String) : super(name) {
    }

    constructor(theClass: Class<out TestCase>, name: String) : super(theClass, name) {
    }

    override fun run(result: TestResult) {
        fActiveTestDeathCount = 0
        super.run(result)
        waitUntilFinished()
    }

    override fun runTest(test: Test, result: TestResult) {
        val t = object : Thread() {
            override fun run() {
                try {
                    // inlined due to limitation in VA/Java
                    //ActiveTestSuite.super.runTest(test, result);
                    test.run(result)
                } finally {
                    this@ActiveTestSuite.runFinished()
                }
            }
        }
        t.start()
    }

    @Synchronized internal fun waitUntilFinished() {
        while (fActiveTestDeathCount < testCount()) {
            try {
                wait()
            } catch (e: InterruptedException) {
                return  // ignore
            }

        }
    }

    @Synchronized fun runFinished() {
        fActiveTestDeathCount++
        notifyAll()
    }
}