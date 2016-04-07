package org.junit.internal.builders

import org.junit.runner.Runner
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.RunnerBuilder

class JUnit4Builder : RunnerBuilder() {
    @Throws(Throwable::class)
    override fun runnerForClass(testClass: Class<*>): Runner {
        return BlockJUnit4ClassRunner(testClass)
    }
}