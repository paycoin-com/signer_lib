package org.junit.runner

import org.junit.runner.manipulation.Filter

/**
 * Extend this class to create a factory that creates [Filter].
 */
interface FilterFactory {
    /**
     * Creates a [Filter] given a [FilterFactoryParams] argument.

     * @param params Parameters needed to create the [Filter]
     */
    @Throws(FilterNotCreatedException::class)
    fun createFilter(params: FilterFactoryParams): Filter

    /**
     * Exception thrown if the [Filter] cannot be created.
     */
    class FilterNotCreatedException(exception: Exception) : Exception(exception.message, exception)
}
