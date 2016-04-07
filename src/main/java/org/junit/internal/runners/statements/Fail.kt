package org.junit.internal.runners.statements

import org.junit.runners.model.Statement

class Fail(private val error: Throwable) : Statement() {

    @Throws(Throwable::class)
    override fun evaluate() {
        throw error
    }
}
