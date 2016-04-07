package org.junit.internal.runners.statements

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class RunBefores(private val next: Statement, private val befores: List<FrameworkMethod>, private val target: Any) : Statement() {

    @Throws(Throwable::class)
    override fun evaluate() {
        for (before in befores) {
            before.invokeExplosively(target)
        }
        next.evaluate()
    }
}