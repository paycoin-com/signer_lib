package org.junit.internal.runners.statements

import java.util.ArrayList

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.MultipleFailureException
import org.junit.runners.model.Statement

class RunAfters(private val next: Statement, private val afters: List<FrameworkMethod>, private val target: Any) : Statement() {

    @Throws(Throwable::class)
    override fun evaluate() {
        val errors = ArrayList<Throwable>()
        try {
            next.evaluate()
        } catch (e: Throwable) {
            errors.add(e)
        } finally {
            for (each in afters) {
                try {
                    each.invokeExplosively(target)
                } catch (e: Throwable) {
                    errors.add(e)
                }

            }
        }
        MultipleFailureException.assertEmpty(errors)
    }
}