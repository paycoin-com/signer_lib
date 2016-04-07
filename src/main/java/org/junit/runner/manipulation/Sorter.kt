package org.junit.runner.manipulation

import java.util.Comparator

import org.junit.runner.Description

/**
 * A `Sorter` orders tests. In general you will not need
 * to use a `Sorter` directly. Instead, use [org.junit.runner.Request.sortWith].

 * @since 4.0
 */
class Sorter
/**
 * Creates a `Sorter` that uses `comparator`
 * to sort tests

 * @param comparator the [Comparator] to use when sorting tests
 */
(private val comparator: Comparator<Description>) : Comparator<Description> {

    /**
     * Sorts the test in `runner` using `comparator`
     */
    fun apply(`object`: Any) {
        if (`object` is Sortable) {
            `object`.sort(this)
        }
    }

    override fun compare(o1: Description, o2: Description): Int {
        return comparator.compare(o1, o2)
    }

    companion object {
        /**
         * NULL is a `Sorter` that leaves elements in an undefined order
         */
        val NULL = Sorter(java.util.Comparator<org.junit.runner.Description> { o1, o2 -> 0 })
    }
}
