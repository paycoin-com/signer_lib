package org.junit.internal.builders

import org.junit.Ignore
import org.junit.runner.Runner
import org.junit.runners.model.RunnerBuilder

class IgnoredBuilder : RunnerBuilder() {
    override fun runnerForClass(testClass: Class<*>): Runner? {
        if (testClass.getAnnotation(Ignore::class.java) != null) {
            return IgnoredClassRunner(testClass)
        }
        return null
    }
}