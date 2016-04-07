package org.junit.runner.manipulation

import org.junit.runner.Description
import org.junit.runner.Request

/**
 * The canonical case of filtering is when you want to run a single test method in a class. Rather
 * than introduce runner API just for that one case, JUnit provides a general filtering mechanism.
 * If you want to filter the tests to be run, extend `Filter` and apply an instance of
 * your filter to the [org.junit.runner.Request] before running it (see
 * [org.junit.runner.JUnitCore.run]. Alternatively, apply a `Filter` to
 * a [org.junit.runner.Runner] before running tests (for example, in conjunction with
 * [org.junit.runner.RunWith].

 * @since 4.0
 */
abstract class Filter {


    /**
     * @param description the description of the test to be run
     * *
     * @return `true` if the test should be run
     */
    abstract fun shouldRun(description: Description): Boolean

    /**
     * Returns a textual description of this Filter

     * @return a textual description of this Filter
     */
    abstract fun describe(): String

    /**
     * Invoke with a [org.junit.runner.Runner] to cause all tests it intends to run
     * to first be checked with the filter. Only those that pass the filter will be run.

     * @param child the runner to be filtered by the receiver
     * *
     * @throws NoTestsRemainException if the receiver removes all tests
     */
    @Throws(NoTestsRemainException::class)
    open fun apply(child: Any) {
        if (child !is Filterable) {
            return
        }
        child.filter(this)
    }

    /**
     * Returns a new Filter that accepts the intersection of the tests accepted
     * by this Filter and `second`
     */
    open fun intersect(second: Filter): Filter {
        if (second === this || second === ALL) {
            return this
        }
        val first = this
        return object : Filter() {
            override fun shouldRun(description: Description): Boolean {
                return first.shouldRun(description) && second.shouldRun(description)
            }

            override fun describe(): String {
                return first.describe() + " and " + second.describe()
            }
        }
    }

    companion object {
        /**
         * A null `Filter` that passes all tests through.
         */
        val ALL: Filter = object : Filter() {
            override fun shouldRun(description: Description): Boolean {
                return true
            }

            override fun describe(): String {
                return "all tests"
            }

            @Throws(NoTestsRemainException::class)
            override fun apply(child: Any) {
                // do nothing
            }

            override fun intersect(second: Filter): Filter {
                return second
            }
        }

        /**
         * Returns a `Filter` that only runs the single method described by
         * `desiredDescription`
         */
        fun matchMethodDescription(desiredDescription: Description): Filter {
            return object : Filter() {
                override fun shouldRun(description: Description): Boolean {
                    if (description.isTest) {
                        return desiredDescription == description
                    }

                    // explicitly check if any children want to run
                    for (each in description.children) {
                        if (shouldRun(each)) {
                            return true
                        }
                    }
                    return false
                }

                override fun describe(): String {
                    return String.format("Method %s", desiredDescription.displayName)
                }
            }
        }
    }
}
