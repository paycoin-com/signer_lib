package org.junit.experimental.categories

import java.util.ArrayList

import org.junit.internal.Classes
import org.junit.runner.FilterFactory
import org.junit.runner.FilterFactoryParams
import org.junit.runner.manipulation.Filter

/**
 * Implementation of FilterFactory for Category filtering.
 */
internal abstract class CategoryFilterFactory : FilterFactory {
    /**
     * Creates a [org.junit.experimental.categories.Categories.CategoryFilter] given a
     * [FilterFactoryParams] argument.

     * @param params Parameters needed to create the [Filter]
     */
    @Throws(FilterFactory.FilterNotCreatedException::class)
    override fun createFilter(params: FilterFactoryParams): Filter {
        try {
            return createFilter(parseCategories(params.args))
        } catch (e: ClassNotFoundException) {
            throw FilterFactory.FilterNotCreatedException(e)
        }

    }

    /**
     * Creates a [org.junit.experimental.categories.Categories.CategoryFilter] given an array of classes.

     * @param categories Category classes.
     */
    protected abstract fun createFilter(categories: List<Class<*>>): Filter

    @Throws(ClassNotFoundException::class)
    private fun parseCategories(categories: String): List<Class<*>> {
        val categoryClasses = ArrayList<Class<*>>()

        for (category in categories.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val categoryClass = Classes.getClass(category)

            categoryClasses.add(categoryClass)
        }

        return categoryClasses
    }
}
