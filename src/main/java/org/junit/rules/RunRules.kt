package org.junit.rules

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Runs a collection of rules on a statement.

 * @since 4.9
 */
class RunRules(base: Statement, rules: Iterable<TestRule>, description: Description) : Statement() {
    private val statement: Statement

    init {
        statement = applyAll(base, rules, description)
    }

    @Throws(Throwable::class)
    override fun evaluate() {
        statement.evaluate()
    }

    private fun applyAll(result: Statement, rules: Iterable<TestRule>,
                         description: Description): Statement {
        var result = result
        for (each in rules) {
            result = each.apply(result, description)
        }
        return result
    }
}
