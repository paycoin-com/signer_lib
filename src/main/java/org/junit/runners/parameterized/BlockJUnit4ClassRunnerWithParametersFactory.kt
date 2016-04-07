package org.junit.runners.parameterized

import org.junit.runner.Runner
import org.junit.runners.model.InitializationError

/**
 * A [ParametersRunnerFactory] that creates
 * [BlockJUnit4ClassRunnerWithParameters].

 * @since 4.12
 */
class BlockJUnit4ClassRunnerWithParametersFactory : ParametersRunnerFactory {
    @Throws(InitializationError::class)
    override fun createRunnerForTestWithParameters(test: TestWithParameters): Runner {
        return BlockJUnit4ClassRunnerWithParameters(test)
    }
}
