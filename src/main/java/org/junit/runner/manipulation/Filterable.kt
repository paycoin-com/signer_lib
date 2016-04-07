package org.junit.runner.manipulation

/**
 * Runners that allow filtering should implement this interface. Implement [.filter]
 * to remove tests that don't pass the filter.

 * @since 4.0
 */
interface Filterable {

    /**
     * Remove tests that don't pass the parameter `filter`.

     * @param filter the [Filter] to apply
     * *
     * @throws NoTestsRemainException if all tests are filtered out
     */
    @Throws(NoTestsRemainException::class)
    fun filter(filter: Filter)

}
