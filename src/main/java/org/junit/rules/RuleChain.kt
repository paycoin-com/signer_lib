package org.junit.rules

import java.util.ArrayList
import java.util.Collections

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * The RuleChain rule allows ordering of TestRules. You create a
 * `RuleChain` with [.outerRule] and subsequent calls of
 * [.around]:

 *
 * public static class UseRuleChain {
 * &#064;Rule
 * public RuleChain chain= RuleChain
 * .outerRule(new LoggingRule("outer rule")
 * .around(new LoggingRule("middle rule")
 * .around(new LoggingRule("inner rule");

 * &#064;Test
 * public void example() {
 * assertTrue(true);
 * }
 * }
 *

 * writes the log

 *
 * starting outer rule
 * starting middle rule
 * starting inner rule
 * finished inner rule
 * finished middle rule
 * finished outer rule
 *

 * @since 4.10
 */
class RuleChain private constructor(private val rulesStartingWithInnerMost: List<TestRule>) : TestRule {

    /**
     * Create a new `RuleChain`, which encloses the `nextRule` with
     * the rules of the current `RuleChain`.

     * @param enclosedRule the rule to enclose.
     * *
     * @return a new `RuleChain`.
     */
    fun around(enclosedRule: TestRule): RuleChain {
        val rulesOfNewChain = ArrayList<TestRule>()
        rulesOfNewChain.add(enclosedRule)
        rulesOfNewChain.addAll(rulesStartingWithInnerMost)
        return RuleChain(rulesOfNewChain)
    }

    /**
     * {@inheritDoc}
     */
    override fun apply(base: Statement, description: Description): Statement {
        var base = base
        for (each in rulesStartingWithInnerMost) {
            base = each.apply(base, description)
        }
        return base
    }

    companion object {
        private val EMPTY_CHAIN = RuleChain(
                emptyList<TestRule>())

        /**
         * Returns a `RuleChain` without a [TestRule]. This method may
         * be the starting point of a `RuleChain`.

         * @return a `RuleChain` without a [TestRule].
         */
        fun emptyRuleChain(): RuleChain {
            return EMPTY_CHAIN
        }

        /**
         * Returns a `RuleChain` with a single [TestRule]. This method
         * is the usual starting point of a `RuleChain`.

         * @param outerRule the outer rule of the `RuleChain`.
         * *
         * @return a `RuleChain` with a single [TestRule].
         */
        fun outerRule(outerRule: TestRule): RuleChain {
            return emptyRuleChain().around(outerRule)
        }
    }
}