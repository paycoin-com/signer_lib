package org.junit.experimental.categories

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.Collections
import java.util.HashSet

import org.junit.runner.Description
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.NoTestsRemainException
import org.junit.runners.Suite
import org.junit.runners.model.InitializationError
import org.junit.runners.model.RunnerBuilder
import kotlin.reflect.KClass

/**
 * From a given set of test classes, runs only the classes and methods that are
 * annotated with either the category given with the @IncludeCategory
 * annotation, or a subtype of that category.
 *
 *
 * Note that, for now, annotating suites with `@Category` has no effect.
 * Categories must be annotated on the direct method or class.
 *
 *
 * Example:
 *
 * public interface FastTests {
 * }

 * public interface SlowTests {
 * }

 * public interface SmokeTests
 * }

 * public static class A {
 * &#064;Test
 * public void a() {
 * fail();
 * }

 * &#064;Category(SlowTests.class)
 * &#064;Test
 * public void b() {
 * }

 * &#064;Category({FastTests.class, SmokeTests.class})
 * &#064;Test
 * public void c() {
 * }
 * }

 * &#064;Category({SlowTests.class, FastTests.class})
 * public static class B {
 * &#064;Test
 * public void d() {
 * }
 * }

 * &#064;RunWith(Categories.class)
 * &#064;IncludeCategory(SlowTests.class)
 * &#064;SuiteClasses({A.class, B.class})
 * // Note that Categories is a kind of Suite
 * public static class SlowTestSuite {
 * // Will run A.b and B.d, but not A.a and A.c
 * }
 *
 *
 *
 * Example to run multiple categories:
 *
 * &#064;RunWith(Categories.class)
 * &#064;IncludeCategory({FastTests.class, SmokeTests.class})
 * &#064;SuiteClasses({A.class, B.class})
 * public static class FastOrSmokeTestSuite {
 * // Will run A.c and B.d, but not A.b because it is not any of FastTests or SmokeTests
 * }
 *

 * @version 4.12
 * *
 * @see [Categories at JUnit wiki](https://github.com/junit-team/junit/wiki/Categories)
 */
class Categories @Throws(InitializationError::class)
constructor(klass: Class<*>, builder: RunnerBuilder) : Suite(klass, builder) {

    @Retention(RetentionPolicy.RUNTIME)
    annotation class IncludeCategory(
            /**
             * Determines the tests to run that are annotated with categories specified in
             * the value of this annotation or their subtypes unless excluded with [ExcludeCategory].
             */
            vararg val value: KClass<*> = arrayOf(),
            /**
             * If true, runs tests annotated with *any* of the categories in
             * [IncludeCategory.value]. Otherwise, runs tests only if annotated with *all* of the categories.
             */
            val matchAny: Boolean = true)

    @Retention(RetentionPolicy.RUNTIME)
    annotation class ExcludeCategory(
            /**
             * Determines the tests which do not run if they are annotated with categories specified in the
             * value of this annotation or their subtypes regardless of being included in [IncludeCategory.value].
             */
            vararg val value: KClass<*> = arrayOf(),
            /**
             * If true, the tests annotated with *any* of the categories in [ExcludeCategory.value]
             * do not run. Otherwise, the tests do not run if and only if annotated with *all* categories.
             */
            val matchAny: Boolean = true)

    open class CategoryFilter protected constructor(private val includedAny: Boolean, includes: Set<Class<*>>,
                                                    private val excludedAny: Boolean, excludes: Set<Class<*>>) : Filter() {
        private val included: Set<Class<*>>
        private val excluded: Set<Class<*>>

        init {
            included = copyAndRefine(includes)
            excluded = copyAndRefine(excludes)
        }

        /**
         * @see .toString
         */
        override fun describe(): String {
            return toString()
        }

        /**
         * Returns string in the form &quot;[included categories] - [excluded categories]&quot;, where both
         * sets have comma separated names of categories.

         * @return string representation for the relative complement of excluded categories set
         * * in the set of included categories. Examples:
         * *
         * *   *  &quot;categories [all]&quot; for all included categories and no excluded ones;
         * *   *  &quot;categories [all] - [A, B]&quot; for all included categories and given excluded ones;
         * *   *  &quot;categories [A, B] - [C, D]&quot; for given included categories and given excluded ones.
         * *
         * *
         * @see Class.toString
         */
        override fun toString(): String {
            val description = StringBuilder("categories ").append(if (included.isEmpty()) "[all]" else included)
            if (!excluded.isEmpty()) {
                description.append(" - ").append(excluded)
            }
            return description.toString()
        }

        override fun shouldRun(description: Description): Boolean {
            if (hasCorrectCategoryAnnotation(description)) {
                return true
            }

            for (each in description.children) {
                if (shouldRun(each)) {
                    return true
                }
            }

            return false
        }

        private fun hasCorrectCategoryAnnotation(description: Description): Boolean {
            val childCategories = categories(description)

            // If a child has no categories, immediately return.
            if (childCategories.isEmpty()) {
                return included.isEmpty()
            }

            if (!excluded.isEmpty()) {
                if (excludedAny) {
                    if (matchesAnyParentCategories(childCategories, excluded)) {
                        return false
                    }
                } else {
                    if (matchesAllParentCategories(childCategories, excluded)) {
                        return false
                    }
                }
            }

            if (included.isEmpty()) {
                // Couldn't be excluded, and with no suite's included categories treated as should run.
                return true
            } else {
                if (includedAny) {
                    return matchesAnyParentCategories(childCategories, included)
                } else {
                    return matchesAllParentCategories(childCategories, included)
                }
            }
        }

        /**
         * @return true if at least one (any) parent category match a child, otherwise false.
         * * If empty parentCategories, returns false.
         */
        private fun matchesAnyParentCategories(childCategories: Set<Class<*>>, parentCategories: Set<Class<*>>): Boolean {
            for (parentCategory in parentCategories) {
                if (hasAssignableTo(childCategories, parentCategory)) {
                    return true
                }
            }
            return false
        }

        /**
         * @return false if at least one parent category does not match children, otherwise true.
         * * If empty parentCategories, returns true.
         */
        private fun matchesAllParentCategories(childCategories: Set<Class<*>>, parentCategories: Set<Class<*>>): Boolean {
            for (parentCategory in parentCategories) {
                if (!hasAssignableTo(childCategories, parentCategory)) {
                    return false
                }
            }
            return true
        }

        companion object {

            fun include(matchAny: Boolean, vararg categories: Class<*>): CategoryFilter {
                if (hasNull(*categories)) {
                    throw NullPointerException("has null category")
                }
                return categoryFilter(matchAny, createSet(*categories), true, null)
            }

            fun include(category: Class<*>): CategoryFilter {
                return include(true, category)
            }

            fun include(vararg categories: Class<*>): CategoryFilter {
                return include(true, *categories)
            }

            fun exclude(matchAny: Boolean, vararg categories: Class<*>): CategoryFilter {
                if (hasNull(*categories)) {
                    throw NullPointerException("has null category")
                }
                return categoryFilter(true, null, matchAny, createSet(*categories))
            }

            fun exclude(category: Class<*>): CategoryFilter {
                return exclude(true, category)
            }

            fun exclude(vararg categories: Class<*>): CategoryFilter {
                return exclude(true, *categories)
            }

            fun categoryFilter(matchAnyInclusions: Boolean, inclusions: Set<Class<*>>?,
                               matchAnyExclusions: Boolean, exclusions: Set<Class<*>>?): CategoryFilter {
                return CategoryFilter(matchAnyInclusions, inclusions, matchAnyExclusions, exclusions)
            }

            private fun categories(description: Description): Set<Class<*>> {
                val categories = HashSet<Class<*>>()
                Collections.addAll(categories, *directCategories(description))
                Collections.addAll(categories, *directCategories(parentDescription(description)))
                return categories
            }

            private fun parentDescription(description: Description): Description? {
                val testClass = description.testClass
                return if (testClass == null) null else Description.createSuiteDescription(testClass)
            }

            private fun directCategories(description: Description?): Array<Class<*>> {
                if (description == null) {
                    return arrayOfNulls(0)
                }

                val annotation = description.getAnnotation(Category::class.java)
                return if (annotation == null) arrayOfNulls<Class<*>>(0) else annotation.value()
            }

            private fun copyAndRefine(classes: Set<Class<*>>?): Set<Class<*>> {
                val c = HashSet<Class<*>>()
                if (classes != null) {
                    c.addAll(classes)
                }
                c.remove(null)
                return c
            }

            private fun hasNull(vararg classes: Class<*>): Boolean {
                if (classes == null) return false
                for (clazz in classes) {
                    if (clazz == null) {
                        return true
                    }
                }
                return false
            }
        }
    }

    init {
        try {
            val included = getIncludedCategory(klass)
            val excluded = getExcludedCategory(klass)
            val isAnyIncluded = isAnyIncluded(klass)
            val isAnyExcluded = isAnyExcluded(klass)

            filter(CategoryFilter.categoryFilter(isAnyIncluded, included, isAnyExcluded, excluded))
        } catch (e: NoTestsRemainException) {
            throw InitializationError(e)
        }

        assertNoCategorizedDescendentsOfUncategorizeableParents(description)
    }

    companion object {

        private fun getIncludedCategory(klass: Class<*>): Set<Class<*>> {
            val annotation = klass.getAnnotation(IncludeCategory::class.java)
            return createSet(*annotation?.value())
        }

        private fun isAnyIncluded(klass: Class<*>): Boolean {
            val annotation = klass.getAnnotation(IncludeCategory::class.java)
            return annotation == null || annotation.matchAny()
        }

        private fun getExcludedCategory(klass: Class<*>): Set<Class<*>> {
            val annotation = klass.getAnnotation(ExcludeCategory::class.java)
            return createSet(*annotation?.value())
        }

        private fun isAnyExcluded(klass: Class<*>): Boolean {
            val annotation = klass.getAnnotation(ExcludeCategory::class.java)
            return annotation == null || annotation.matchAny()
        }

        @Throws(InitializationError::class)
        private fun assertNoCategorizedDescendentsOfUncategorizeableParents(description: Description) {
            if (!canHaveCategorizedChildren(description)) {
                assertNoDescendantsHaveCategoryAnnotations(description)
            }
            for (each in description.children) {
                assertNoCategorizedDescendentsOfUncategorizeableParents(each)
            }
        }

        @Throws(InitializationError::class)
        private fun assertNoDescendantsHaveCategoryAnnotations(description: Description) {
            for (each in description.children) {
                if (each.getAnnotation(Category::class.java) != null) {
                    throw InitializationError("Category annotations on Parameterized classes are not supported on individual methods.")
                }
                assertNoDescendantsHaveCategoryAnnotations(each)
            }
        }

        // If children have names like [0], our current magical category code can't determine their parentage.
        private fun canHaveCategorizedChildren(description: Description): Boolean {
            for (each in description.children) {
                if (each.testClass == null) {
                    return false
                }
            }
            return true
        }

        private fun hasAssignableTo(assigns: Set<Class<*>>, to: Class<*>): Boolean {
            for (from in assigns) {
                if (to.isAssignableFrom(from)) {
                    return true
                }
            }
            return false
        }

        private fun createSet(vararg t: Class<*>): Set<Class<*>> {
            val set = HashSet<Class<*>>()
            if (t != null) {
                Collections.addAll(set, *t)
            }
            return set
        }
    }
}
