package org.junit.internal.builders

import java.util.Arrays

import org.junit.runner.Runner
import org.junit.runners.model.RunnerBuilder

class AllDefaultPossibilitiesBuilder(private val canUseSuiteMethod: Boolean) : RunnerBuilder() {

    @Throws(Throwable::class)
    override fun runnerForClass(testClass: Class<*>): Runner? {
        val builders = Arrays.asList(
                ignoredBuilder(),
                annotatedBuilder(),
                suiteMethodBuilder(),
                junit3Builder(),
                junit4Builder())

        for (each in builders) {
            val runner = each.safeRunnerForClass(testClass)
            if (runner != null) {
                return runner
            }
        }
        return null
    }

    protected fun junit4Builder(): JUnit4Builder {
        return JUnit4Builder()
    }

    protected fun junit3Builder(): JUnit3Builder {
        return JUnit3Builder()
    }

    protected fun annotatedBuilder(): AnnotatedBuilder {
        return AnnotatedBuilder(this)
    }

    protected fun ignoredBuilder(): IgnoredBuilder {
        return IgnoredBuilder()
    }

    protected fun suiteMethodBuilder(): RunnerBuilder {
        if (canUseSuiteMethod) {
            return SuiteMethodBuilder()
        }
        return NullBuilder()
    }
}