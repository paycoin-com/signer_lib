package org.junit.validator

import java.util.Collections.emptyList
import java.util.Collections.singletonList

import org.junit.runners.model.TestClass

/**
 * Validates that a [TestClass] is public.

 * @since 4.12
 */
class PublicClassValidator : TestClassValidator {

    /**
     * Validate that the specified [TestClass] is public.

     * @param testClass the [TestClass] that is validated.
     * *
     * @return an empty list if the class is public or a list with a single
     * *         exception otherwise.
     */
    override fun validateTestClass(testClass: TestClass): List<Exception> {
        if (testClass.isPublic) {
            return NO_VALIDATION_ERRORS
        } else {
            return listOf(Exception("The class "
                    + testClass.name + " is not public."))
        }
    }

    companion object {
        private val NO_VALIDATION_ERRORS = emptyList<Exception>()
    }
}
