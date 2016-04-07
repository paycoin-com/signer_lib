package junit.textui

import java.io.PrintStream
import java.text.NumberFormat
import java.util.Enumeration

import junit.framework.AssertionFailedError
import junit.framework.Test
import junit.framework.TestFailure
import junit.framework.TestListener
import junit.framework.TestResult
import junit.runner.BaseTestRunner

class ResultPrinter(writer: PrintStream) : TestListener {
    var writer: PrintStream
        internal set
    internal var fColumn = 0

    init {
        this.writer = writer
    }

    /* API for use by textui.TestRunner */

    @Synchronized internal fun print(result: TestResult, runTime: Long) {
        printHeader(runTime)
        printErrors(result)
        printFailures(result)
        printFooter(result)
    }

    internal fun printWaitPrompt() {
        writer.println()
        writer.println("<RETURN> to continue")
    }

    /* Internal methods */

    protected fun printHeader(runTime: Long) {
        writer.println()
        writer.println("Time: " + elapsedTimeAsString(runTime))
    }

    protected fun printErrors(result: TestResult) {
        printDefects(result.errors(), result.errorCount(), "error")
    }

    protected fun printFailures(result: TestResult) {
        printDefects(result.failures(), result.failureCount(), "failure")
    }

    protected fun printDefects(booBoos: Enumeration<TestFailure>, count: Int, type: String) {
        if (count == 0) return
        if (count == 1) {
            writer.println("There was $count $type:")
        } else {
            writer.println("There were " + count + " " + type + "s:")
        }
        var i = 1
        while (booBoos.hasMoreElements()) {
            printDefect(booBoos.nextElement(), i)
            i++
        }
    }

    fun printDefect(booBoo: TestFailure, count: Int) {
        // only public for testing purposes
        printDefectHeader(booBoo, count)
        printDefectTrace(booBoo)
    }

    protected fun printDefectHeader(booBoo: TestFailure, count: Int) {
        // I feel like making this a println, then adding a line giving the throwable a chance to print something
        // before we get to the stack trace.
        writer.print(count + ") " + booBoo.failedTest())
    }

    protected fun printDefectTrace(booBoo: TestFailure) {
        writer.print(BaseTestRunner.getFilteredTrace(booBoo.trace()))
    }

    protected fun printFooter(result: TestResult) {
        if (result.wasSuccessful()) {
            writer.println()
            writer.print("OK")
            writer.println(" (" + result.runCount() + " test" + (if (result.runCount() == 1) "" else "s") + ")")

        } else {
            writer.println()
            writer.println("FAILURES!!!")
            writer.println("Tests run: " + result.runCount() +
                    ",  Failures: " + result.failureCount() +
                    ",  Errors: " + result.errorCount())
        }
        writer.println()
    }

    /**
     * Returns the formatted string of the elapsed time.
     * Duplicated from BaseTestRunner. Fix it.
     */
    protected fun elapsedTimeAsString(runTime: Long): String {
        return NumberFormat.getInstance().format(runTime.toDouble() / 1000)
    }

    /**
     * @see junit.framework.TestListener.addError
     */
    override fun addError(test: Test, e: Throwable) {
        writer.print("E")
    }

    /**
     * @see junit.framework.TestListener.addFailure
     */
    override fun addFailure(test: Test, t: AssertionFailedError) {
        writer.print("F")
    }

    /**
     * @see junit.framework.TestListener.endTest
     */
    override fun endTest(test: Test) {
    }

    /**
     * @see junit.framework.TestListener.startTest
     */
    override fun startTest(test: Test) {
        writer.print(".")
        if (fColumn++ >= 40) {
            writer.println()
            fColumn = 0
        }
    }

}
