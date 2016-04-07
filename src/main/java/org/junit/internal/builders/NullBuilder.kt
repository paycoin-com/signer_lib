package org.junit.internal.builders

import org.junit.runner.Runner
import org.junit.runners.model.RunnerBuilder

class NullBuilder : RunnerBuilder() {
    @Throws(Throwable::class)
    override fun runnerForClass(each: Class<*>): Runner? {
        return null
    }
}