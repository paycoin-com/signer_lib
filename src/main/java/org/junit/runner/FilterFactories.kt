package org.junit.runner

import org.junit.internal.Classes
import org.junit.runner.FilterFactory.FilterNotCreatedException
import org.junit.runner.manipulation.Filter

/**
 * Utility class whose methods create a [FilterFactory].
 */
internal object FilterFactories {
    /**
     * Creates a [Filter].

     * A filter specification is of the form "package.of.FilterFactory=args-to-filter-factory" or
     * "package.of.FilterFactory".

     * @param request the request that will be filtered
     * *
     * @param filterSpec the filter specification
     * *
     * @throws org.junit.runner.FilterFactory.FilterNotCreatedException
     */
    @Throws(FilterFactory.FilterNotCreatedException::class)
    fun createFilterFromFilterSpec(request: Request, filterSpec: String): Filter {
        val topLevelDescription = request.runner.description
        val tuple: Array<String>

        if (filterSpec.contains("=")) {
            tuple = filterSpec.split("=".toRegex(), 2).toTypedArray()
        } else {
            tuple = arrayOf(filterSpec, "")
        }

        return createFilter(tuple[0], FilterFactoryParams(topLevelDescription, tuple[1]))
    }

    /**
     * Creates a [Filter].

     * @param filterFactoryFqcn The fully qualified class name of the [FilterFactory]
     * *
     * @param params The arguments to the [FilterFactory]
     */
    @Throws(FilterFactory.FilterNotCreatedException::class)
    fun createFilter(filterFactoryFqcn: String, params: FilterFactoryParams): Filter {
        val filterFactory = createFilterFactory(filterFactoryFqcn)

        return filterFactory.createFilter(params)
    }

    /**
     * Creates a [Filter].

     * @param filterFactoryClass The class of the [FilterFactory]
     * *
     * @param params             The arguments to the [FilterFactory]
     */
    @Throws(FilterFactory.FilterNotCreatedException::class)
    fun createFilter(filterFactoryClass: Class<out FilterFactory>, params: FilterFactoryParams): Filter {
        val filterFactory = createFilterFactory(filterFactoryClass)

        return filterFactory.createFilter(params)
    }

    @Throws(FilterNotCreatedException::class)
    fun createFilterFactory(filterFactoryFqcn: String): FilterFactory {
        val filterFactoryClass: Class<out FilterFactory>

        try {
            filterFactoryClass = Classes.getClass(filterFactoryFqcn).asSubclass(FilterFactory::class.java)
        } catch (e: Exception) {
            throw FilterNotCreatedException(e)
        }

        return createFilterFactory(filterFactoryClass)
    }

    @Throws(FilterNotCreatedException::class)
    fun createFilterFactory(filterFactoryClass: Class<out FilterFactory>): FilterFactory {
        try {
            return filterFactoryClass.getConstructor().newInstance()
        } catch (e: Exception) {
            throw FilterNotCreatedException(e)
        }

    }
}
