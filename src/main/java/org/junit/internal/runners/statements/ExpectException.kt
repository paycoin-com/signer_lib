package org.junit.internal.runners.statements

import org.junit.internal.AssumptionViolatedException
import org.junit.runners.model.Statement

class ExpectException(private val next: Statement, private val expected: Class<out Throwable>) : Statement() {

    @Throws(Exception::class)
    override fun evaluate() {
        var complete = false
        try {
            next.evaluate()
            complete = true
        } catch (e: AssumptionViolatedException) {
            throw e
        } catch (e: Throwable) {
            if (!expected.isAssignableFrom(e.javaClass)) {
                val message = "Unexpected exception, expected<"
                +expected.name + "> but was<"
                +e.javaClass.name + ">"
                throw Exception(message, e)
            }
        }

        if (complete) {
            throw AssertionError("Expected exception: " + expected.name)
        }
    }
}