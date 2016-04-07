package org.junit.runners.model

import java.util.ArrayList
import java.util.Collections

import org.junit.internal.Throwables

/**
 * Collects multiple `Throwable`s into one exception.

 * @since 4.9
 */
open class MultipleFailureException(errors: List<Throwable>) : Exception() {

    /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
    private val fErrors: List<Throwable>

    init {
        this.fErrors = ArrayList(errors)
    }

    val failures: List<Throwable>
        get() = Collections.unmodifiableList(fErrors)

    override fun getMessage(): String {
        val sb = StringBuilder(
                String.format("There were %d errors:", fErrors.size))
        for (e in fErrors) {
            sb.append(String.format("\n  %s(%s)", e.javaClass.name, e.message))
        }
        return sb.toString()
    }

    companion object {
        private val serialVersionUID = 1L

        /**
         * Asserts that a list of throwables is empty. If it isn't empty,
         * will throw [MultipleFailureException] (if there are
         * multiple throwables in the list) or the first element in the list
         * (if there is only one element).

         * @param errors list to check
         * *
         * @throws Exception or Error if the list is not empty
         */
        @SuppressWarnings("deprecation")
        @Throws(Exception::class)
        fun assertEmpty(errors: List<Throwable>) {
            if (errors.isEmpty()) {
                return
            }
            if (errors.size == 1) {
                throw Throwables.rethrowAsException(errors[0])
            }

            /*
           * Many places in the code are documented to throw
           * org.junit.internal.runners.model.MultipleFailureException.
           * That class now extends this one, so we throw the internal
           * exception in case developers have tests that catch
           * MultipleFailureException.
           */
            throw org.junit.internal.runners.model.MultipleFailureException(errors)
        }
    }
}
