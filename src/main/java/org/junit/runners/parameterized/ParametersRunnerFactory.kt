package org.junit.runners.parameterized

import org.junit.runner.Runner
import org.junit.runners.model.InitializationError

/**
 * A `ParameterizedRunnerFactory` creates a runner for a single
 * [TestWithParameters].

 * @since 4.12
 */
interface ParametersRunnerFactory {
    /**
     * Returns a runner for the specified [TestWithParameters].

     * @throws InitializationError
     * *             if the runner could not be created.
     */
    @Throws(InitializationError::class)
    fun createRunnerForTestWithParameters(test: TestWithParameters): Runner
}
