package org.junit.internal.requests

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder
import org.junit.runner.Request
import org.junit.runner.Runner

class ClassRequest @JvmOverloads constructor(/*
     * We have to use the f prefix, because IntelliJ's JUnit4IdeaTestRunner uses
     * reflection to access this field. See
     * https://github.com/junit-team/junit/issues/960
     */
        private val fTestClass: Class<*>, private val canUseSuiteMethod: Boolean = true) : Request() {
    private val runnerLock = Object()
    @Volatile private var runner: Runner? = null

    override fun getRunner(): Runner {
        if (runner == null) {
            synchronized (runnerLock) {
                if (runner == null) {
                    runner = AllDefaultPossibilitiesBuilder(canUseSuiteMethod).safeRunnerForClass(fTestClass)
                }
            }
        }
        return runner
    }
}