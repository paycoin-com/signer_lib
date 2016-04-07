package org.junit.internal.builders

import org.junit.internal.runners.JUnit38ClassRunner
import org.junit.runner.Runner
import org.junit.runners.model.RunnerBuilder

class JUnit3Builder : RunnerBuilder() {
    @Throws(Throwable::class)
    override fun runnerForClass(testClass: Class<*>): Runner? {
        if (isPre4Test(testClass)) {
            return JUnit38ClassRunner(testClass)
        }
        return null
    }

    internal fun isPre4Test(testClass: Class<*>): Boolean {
        return junit.framework.TestCase::class.java.isAssignableFrom(testClass)
    }
}