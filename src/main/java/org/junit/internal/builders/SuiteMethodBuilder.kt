package org.junit.internal.builders

import org.junit.internal.runners.SuiteMethod
import org.junit.runner.Runner
import org.junit.runners.model.RunnerBuilder

class SuiteMethodBuilder : RunnerBuilder() {
    @Throws(Throwable::class)
    override fun runnerForClass(each: Class<*>): Runner? {
        if (hasSuiteMethod(each)) {
            return SuiteMethod(each)
        }
        return null
    }

    fun hasSuiteMethod(testClass: Class<*>): Boolean {
        try {
            testClass.getMethod("suite")
        } catch (e: NoSuchMethodException) {
            return false
        }

        return true
    }
}