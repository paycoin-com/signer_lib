package org.junit.runner

import org.junit.runners.Suite
import org.junit.runners.model.InitializationError
import org.junit.runners.model.RunnerBuilder

/**
 * Represents a strategy for computing runners and suites.
 * WARNING: this class is very likely to undergo serious changes in version 4.8 and
 * beyond.

 * @since 4.6
 */
open class Computer {

    /**
     * Create a suite for `classes`, building Runners with `builder`.
     * Throws an InitializationError if Runner construction fails
     */
    @Throws(InitializationError::class)
    open fun getSuite(builder: RunnerBuilder,
                      classes: Array<Class<*>>): Runner {
        return Suite(object : RunnerBuilder() {
            @Throws(Throwable::class)
            override fun runnerForClass(testClass: Class<*>): Runner {
                return getRunner(builder, testClass)
            }
        }, classes)
    }

    /**
     * Create a single-class runner for `testClass`, using `builder`
     */
    @Throws(Throwable::class)
    protected open fun getRunner(builder: RunnerBuilder, testClass: Class<*>): Runner {
        return builder.runnerForClass(testClass)
    }

    companion object {
        /**
         * Returns a new default computer, which runs tests in serial order
         */
        fun serial(): Computer {
            return Computer()
        }
    }
}
