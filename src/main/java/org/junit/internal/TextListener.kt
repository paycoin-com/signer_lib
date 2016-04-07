package org.junit.internal

import java.io.PrintStream
import java.text.NumberFormat

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

class TextListener(/*
      * Internal methods
      */

        private val writer: PrintStream) : RunListener() {

    constructor(system: JUnitSystem) : this(system.out()) {
    }

    override fun testRunFinished(result: Result) {
        printHeader(result.runTime)
        printFailures(result)
        printFooter(result)
    }

    override fun testStarted(description: Description) {
        writer.append('.')
    }

    override fun testFailure(failure: Failure) {
        writer.append('E')
    }

    override fun testIgnored(description: Description) {
        writer.append('I')
    }

    protected fun printHeader(runTime: Long) {
        writer.println()
        writer.println("Time: " + elapsedTimeAsString(runTime))
    }

    protected fun printFailures(result: Result) {
        val failures = result.failures
        if (failures.size == 0) {
            return
        }
        if (failures.size == 1) {
            writer.println("There was " + failures.size + " failure:")
        } else {
            writer.println("There were " + failures.size + " failures:")
        }
        var i = 1
        for (each in failures) {
            printFailure(each, "" + i++)
        }
    }

    protected fun printFailure(each: Failure, prefix: String) {
        writer.println(prefix + ") " + each.testHeader)
        writer.print(each.trace)
    }

    protected fun printFooter(result: Result) {
        if (result.wasSuccessful()) {
            writer.println()
            writer.print("OK")
            writer.println(" (" + result.runCount + " test" + (if (result.runCount == 1) "" else "s") + ")")

        } else {
            writer.println()
            writer.println("FAILURES!!!")
            writer.println("Tests run: " + result.runCount + ",  Failures: " + result.failureCount)
        }
        writer.println()
    }

    /**
     * Returns the formatted string of the elapsed time. Duplicated from
     * BaseTestRunner. Fix it.
     */
    protected fun elapsedTimeAsString(runTime: Long): String {
        return NumberFormat.getInstance().format(runTime.toDouble() / 1000)
    }
}
