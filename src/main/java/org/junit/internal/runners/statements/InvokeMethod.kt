package org.junit.internal.runners.statements

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class InvokeMethod(private val testMethod: FrameworkMethod, private val target: Any) : Statement() {

    @Throws(Throwable::class)
    override fun evaluate() {
        testMethod.invokeExplosively(target)
    }
}