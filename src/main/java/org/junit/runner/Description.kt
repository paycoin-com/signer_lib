package org.junit.runner

import java.io.Serializable
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A `Description` describes a test which is to be run or has been run. `Descriptions`
 * can be atomic (a single test) or compound (containing children tests). `Descriptions` are used
 * to provide feedback about the tests that are about to run (for example, the tree view
 * visible in many IDEs) or tests that have been run (for example, the failures view).
 *
 *
 * `Descriptions` are implemented as a single class rather than a Composite because
 * they are entirely informational. They contain no logic aside from counting their tests.
 *
 *
 * In the past, we used the raw [junit.framework.TestCase]s and [junit.framework.TestSuite]s
 * to display the tree of tests. This was no longer viable in JUnit 4 because atomic tests no longer have
 * a superclass below [Object]. We needed a way to pass a class and name together. Description
 * emerged from this.

 * @see org.junit.runner.Request

 * @see org.junit.runner.Runner

 * @since 4.0
 */
class Description private constructor(@Volatile private /* write-once */ var fTestClass: Class<*>?,
                                      /**
                                       * @return a user-understandable label
                                       */
                                      val displayName: String?, private val fUniqueId: Serializable?, vararg annotations: Annotation) : Serializable {

    /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
    private val fChildren = ConcurrentLinkedQueue<Description>()
    private val fAnnotations: Array<Annotation>

    private constructor(clazz: Class<*>, displayName: String, vararg annotations: Annotation) : this(clazz, displayName, displayName, *annotations) {
    }

    init {
        if (displayName == null || displayName.length == 0) {
            throw IllegalArgumentException(
                    "The display name must not be empty.")
        }
        if (fUniqueId == null) {
            throw IllegalArgumentException(
                    "The unique id must not be null.")
        }
        this.fAnnotations = annotations
    }

    /**
     * Add `Description` as a child of the receiver.

     * @param description the soon-to-be child.
     */
    fun addChild(description: Description) {
        fChildren.add(description)
    }

    /**
     * Gets the copy of the children of this `Description`.
     * Returns an empty list if there are no children.
     */
    val children: ArrayList<Description>
        get() = ArrayList(fChildren)

    /**
     * @return `true` if the receiver is a suite
     */
    val isSuite: Boolean
        get() = !isTest

    /**
     * @return `true` if the receiver is an atomic test
     */
    val isTest: Boolean
        get() = fChildren.isEmpty()

    /**
     * @return the total number of atomic tests in the receiver
     */
    fun testCount(): Int {
        if (isTest) {
            return 1
        }
        var result = 0
        for (child in fChildren) {
            result += child.testCount()
        }
        return result
    }

    override fun hashCode(): Int {
        return fUniqueId.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is Description) {
            return false
        }
        return fUniqueId == obj.fUniqueId
    }

    override fun toString(): String {
        return displayName
    }

    /**
     * @return true if this is a description of a Runner that runs no tests
     */
    val isEmpty: Boolean
        get() = equals(EMPTY)

    /**
     * @return a copy of this description, with no children (on the assumption that some of the
     * *         children will be added back)
     */
    fun childlessCopy(): Description {
        return Description(fTestClass, displayName, *fAnnotations)
    }

    /**
     * @return the annotation of type annotationType that is attached to this description node,
     * *         or null if none exists
     */
    fun <T : Annotation> getAnnotation(annotationType: Class<T>): T? {
        for (each in fAnnotations) {
            if (each.annotationType() == annotationType) {
                return annotationType.cast(each)
            }
        }
        return null
    }

    /**
     * @return all of the annotations attached to this description node
     */
    val annotations: Collection<Annotation>
        get() = Arrays.asList(*fAnnotations)

    /**
     * @return If this describes a method invocation,
     * *         the class of the test instance.
     */
    val testClass: Class<*>?
        get() {
            if (fTestClass != null) {
                return fTestClass
            }
            val name = className ?: return null
            try {
                fTestClass = Class.forName(name, false, javaClass.classLoader)
                return fTestClass
            } catch (e: ClassNotFoundException) {
                return null
            }

        }

    /**
     * @return If this describes a method invocation,
     * *         the name of the class of the test instance
     */
    val className: String?
        get() = if (fTestClass != null) fTestClass!!.name else methodAndClassNamePatternGroupOrDefault(2, toString())

    /**
     * @return If this describes a method invocation,
     * *         the name of the method (or null if not)
     */
    val methodName: String
        get() = methodAndClassNamePatternGroupOrDefault(1, null)

    private fun methodAndClassNamePatternGroupOrDefault(group: Int,
                                                        defaultString: String?): String {
        val matcher = METHOD_AND_CLASS_NAME_PATTERN.matcher(toString())
        return if (matcher.matches()) matcher.group(group) else defaultString
    }

    companion object {
        private val serialVersionUID = 1L

        private val METHOD_AND_CLASS_NAME_PATTERN = Pattern.compile("([\\s\\S]*)\\((.*)\\)")

        /**
         * Create a `Description` named `name`.
         * Generally, you will add children to this `Description`.

         * @param name the name of the `Description`
         * *
         * @param annotations meta-data about the test, for downstream interpreters
         * *
         * @return a `Description` named `name`
         */
        fun createSuiteDescription(name: String, vararg annotations: Annotation): Description {
            return Description(null, name, *annotations)
        }

        /**
         * Create a `Description` named `name`.
         * Generally, you will add children to this `Description`.

         * @param name the name of the `Description`
         * *
         * @param uniqueId an arbitrary object used to define uniqueness (in [.equals]
         * *
         * @param annotations meta-data about the test, for downstream interpreters
         * *
         * @return a `Description` named `name`
         */
        fun createSuiteDescription(name: String, uniqueId: Serializable, vararg annotations: Annotation): Description {
            return Description(null, name, uniqueId, *annotations)
        }

        /**
         * Create a `Description` of a single test named `name` in the 'class' named
         * `className`. Generally, this will be a leaf `Description`. This method is a better choice
         * than [.createTestDescription] for test runners whose test cases are not
         * defined in an actual Java `Class`.

         * @param className the class name of the test
         * *
         * @param name the name of the test (a method name for test annotated with [org.junit.Test])
         * *
         * @param annotations meta-data about the test, for downstream interpreters
         * *
         * @return a `Description` named `name`
         */
        fun createTestDescription(className: String, name: String, vararg annotations: Annotation): Description {
            return Description(null, formatDisplayName(name, className), *annotations)
        }

        /**
         * Create a `Description` of a single test named `name` in the class `clazz`.
         * Generally, this will be a leaf `Description`.

         * @param clazz the class of the test
         * *
         * @param name the name of the test (a method name for test annotated with [org.junit.Test])
         * *
         * @param annotations meta-data about the test, for downstream interpreters
         * *
         * @return a `Description` named `name`
         */
        fun createTestDescription(clazz: Class<*>, name: String, vararg annotations: Annotation): Description {
            return Description(clazz, formatDisplayName(name, clazz.name), *annotations)
        }

        /**
         * Create a `Description` of a single test named `name` in the class `clazz`.
         * Generally, this will be a leaf `Description`.
         * (This remains for binary compatibility with clients of JUnit 4.3)

         * @param clazz the class of the test
         * *
         * @param name the name of the test (a method name for test annotated with [org.junit.Test])
         * *
         * @return a `Description` named `name`
         */
        fun createTestDescription(clazz: Class<*>, name: String): Description {
            return Description(clazz, formatDisplayName(name, clazz.name))
        }

        /**
         * Create a `Description` of a single test named `name` in the class `clazz`.
         * Generally, this will be a leaf `Description`.

         * @param name the name of the test (a method name for test annotated with [org.junit.Test])
         * *
         * @return a `Description` named `name`
         */
        fun createTestDescription(className: String, name: String, uniqueId: Serializable): Description {
            return Description(null, formatDisplayName(name, className), uniqueId)
        }

        private fun formatDisplayName(name: String, className: String): String {
            return String.format("%s(%s)", name, className)
        }

        /**
         * Create a `Description` named after `testClass`

         * @param testClass A [Class] containing tests
         * *
         * @return a `Description` of `testClass`
         */
        fun createSuiteDescription(testClass: Class<*>): Description {
            return Description(testClass, testClass.name, *testClass.annotations)
        }

        /**
         * Describes a Runner which runs no tests
         */
        val EMPTY = Description(null, "No Tests")

        /**
         * Describes a step in the test-running mechanism that goes so wrong no
         * other description can be used (for example, an exception thrown from a Runner's
         * constructor
         */
        val TEST_MECHANISM = Description(null, "Test mechanism")
    }
}