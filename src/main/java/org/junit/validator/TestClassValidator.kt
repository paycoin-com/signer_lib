package org.junit.validator

import org.junit.runners.model.TestClass

/**
 * Validates a single facet of a test class.

 * @since 4.12
 */
interface TestClassValidator {
    /**
     * Validate a single facet of a test class.

     * @param testClass
     * *            the [TestClass] that is validated.
     * *
     * @return the validation errors found by the validator.
     */
    fun validateTestClass(testClass: TestClass): List<Exception>
}
