package org.junit.experimental.categories

import java.util.HashSet

import org.junit.experimental.categories.Categories.CategoryFilter
import org.junit.runner.manipulation.Filter

/**
 * [org.junit.runner.FilterFactory] to exclude categories.

 * The [Filter] that is created will filter out tests that are categorized with any of the
 * given categories.

 * Usage from command line:
 * `
 * --filter=org.junit.experimental.categories.ExcludeCategories=pkg.of.Cat1,pkg.of.Cat2
` *

 * Usage from API:
 * `
 * new ExcludeCategories().createFilter(Cat1.class, Cat2.class);
` *
 */
class ExcludeCategories : CategoryFilterFactory() {
    /**
     * Creates a [Filter] which is only passed by tests that are
     * not categorized with any of the specified categories.

     * @param categories Category classes.
     */
    override fun createFilter(categories: List<Class<*>>): Filter {
        return ExcludesAny(categories)
    }

    private class ExcludesAny(categories: Set<Class<*>>) : CategoryFilter(true, null, true, categories) {
        constructor(categories: List<Class<*>>) : this(HashSet(categories)) {
        }

        override fun describe(): String {
            return "excludes " + super.describe()
        }
    }
}
