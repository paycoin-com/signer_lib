package junit.framework

import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.Enumeration
import java.util.Vector

import org.junit.internal.MethodSorter

/**
 * A `TestSuite` is a `Composite` of Tests.
 * It runs a collection of test cases. Here is an example using
 * the dynamic test definition.
 *
 * TestSuite suite= new TestSuite();
 * suite.addTest(new MathTest("testAdd"));
 * suite.addTest(new MathTest("testDivideByZero"));
 *
 *
 *
 * Alternatively, a TestSuite can extract the tests to be run automatically.
 * To do so you pass the class of your TestCase class to the
 * TestSuite constructor.
 *
 * TestSuite suite= new TestSuite(MathTest.class);
 *
 *
 *
 * This constructor creates a suite with all the methods
 * starting with "test" that take no arguments.
 *
 *
 * A final option is to do the same for a large array of test classes.
 *
 * Class[] testClasses = { MathTest.class, AnotherTest.class }
 * TestSuite suite= new TestSuite(testClasses);
 *

 * @see Test
 */
open class TestSuite : Test {

    /**
     * Returns the name of the suite. Not all
     * test suites have a name and this method
     * can return null.
     */
    /**
     * Sets the name of the suite.

     * @param name the name to set
     */
    var name: String? = null

    private val fTests = Vector<Test>(10) // Cannot convert this to List because it is used directly by some test runners

    /**
     * Constructs an empty TestSuite.
     */
    constructor() {
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     * Parts of this method were written at 2337 meters in the Hueffihuette,
     * Kanton Uri
     */
    constructor(theClass: Class<*>) {
        addTestsFromTestCase(theClass)
    }

    private fun addTestsFromTestCase(theClass: Class<*>) {
        name = theClass.name
        try {
            getTestConstructor(theClass) // Avoid generating multiple error messages
        } catch (e: NoSuchMethodException) {
            addTest(warning("Class " + theClass.name + " has no public constructor TestCase(String name) or TestCase()"))
            return
        }

        if (!Modifier.isPublic(theClass.modifiers)) {
            addTest(warning("Class " + theClass.name + " is not public"))
            return
        }

        var superClass = theClass
        val names = ArrayList<String>()
        while (Test::class.java.isAssignableFrom(superClass)) {
            for (each in MethodSorter.getDeclaredMethods(superClass)) {
                addTestMethod(each, names, theClass)
            }
            superClass = superClass.superclass
        }
        if (fTests.size == 0) {
            addTest(warning("No tests found in " + theClass.name))
        }
    }

    /**
     * Constructs a TestSuite from the given class with the given name.

     * @see TestSuite.TestSuite
     */
    constructor(theClass: Class<out TestCase>, name: String) : this(theClass) {
        name = name
    }

    /**
     * Constructs an empty TestSuite.
     */
    constructor(name: String) {
        name = name
    }

    /**
     * Constructs a TestSuite from the given array of classes.

     * @param classes [TestCase]s
     */
    constructor(vararg classes: Class<*>) {
        for (each in classes) {
            addTest(testCaseForClass(each))
        }
    }

    private fun testCaseForClass(each: Class<*>): Test {
        if (TestCase::class.java.isAssignableFrom(each)) {
            return TestSuite(each.asSubclass(TestCase::class.java))
        } else {
            return warning(each.canonicalName + " does not extend TestCase")
        }
    }

    /**
     * Constructs a TestSuite from the given array of classes with the given name.

     * @see TestSuite.TestSuite
     */
    constructor(classes: Array<Class<out TestCase>>, name: String) : this(*classes) {
        name = name
    }

    /**
     * Adds a test to the suite.
     */
    fun addTest(test: Test) {
        fTests.add(test)
    }

    /**
     * Adds the tests from the given class to the suite
     */
    fun addTestSuite(testClass: Class<out TestCase>) {
        addTest(TestSuite(testClass))
    }

    /**
     * Counts the number of test cases that will be run by this test.
     */
    override fun countTestCases(): Int {
        var count = 0
        for (each in fTests) {
            count += each.countTestCases()
        }
        return count
    }

    /**
     * Runs the tests and collects their result in a TestResult.
     */
    override fun run(result: TestResult) {
        for (each in fTests) {
            if (result.shouldStop()) {
                break
            }
            runTest(each, result)
        }
    }

    open fun runTest(test: Test, result: TestResult) {
        test.run(result)
    }

    /**
     * Returns the test at the given index
     */
    fun testAt(index: Int): Test {
        return fTests[index]
    }

    /**
     * Returns the number of tests in this suite
     */
    fun testCount(): Int {
        return fTests.size
    }

    /**
     * Returns the tests as an enumeration
     */
    fun tests(): Enumeration<Test> {
        return fTests.elements()
    }

    /**
     */
    override fun toString(): String {
        if (name != null) {
            return name
        }
        return super.toString()
    }

    private fun addTestMethod(m: Method, names: MutableList<String>, theClass: Class<*>) {
        val name = m.name
        if (names.contains(name)) {
            return
        }
        if (!isPublicTestMethod(m)) {
            if (isTestMethod(m)) {
                addTest(warning("Test method isn't public: " + m.name + "(" + theClass.canonicalName + ")"))
            }
            return
        }
        names.add(name)
        addTest(createTest(theClass, name))
    }

    private fun isPublicTestMethod(m: Method): Boolean {
        return isTestMethod(m) && Modifier.isPublic(m.modifiers)
    }

    private fun isTestMethod(m: Method): Boolean {
        return m.parameterTypes.size == 0 &&
                m.name.startsWith("test") &&
                m.returnType == Void.TYPE
    }

    companion object {

        /**
         * ...as the moon sets over the early morning Merlin, Oregon
         * mountains, our intrepid adventurers type...
         */
        fun createTest(theClass: Class<*>, name: String): Test {
            val constructor: Constructor<*>
            try {
                constructor = getTestConstructor(theClass)
            } catch (e: NoSuchMethodException) {
                return warning("Class " + theClass.name + " has no public constructor TestCase(String name) or TestCase()")
            }

            val test: Any
            try {
                if (constructor.parameterTypes.size == 0) {
                    test = constructor.newInstance(*arrayOfNulls<Any>(0))
                    if (test is TestCase) {
                        test.name = name
                    }
                } else {
                    test = constructor.newInstance(*arrayOf<Any>(name))
                }
            } catch (e: InstantiationException) {
                return warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")")
            } catch (e: InvocationTargetException) {
                return warning("Exception in constructor: " + name + " (" + exceptionToString(e.targetException) + ")")
            } catch (e: IllegalAccessException) {
                return warning("Cannot access test case: " + name + " (" + exceptionToString(e) + ")")
            }

            return test as Test
        }

        /**
         * Gets a constructor which takes a single String as
         * its argument or a no arg constructor.
         */
        @Throws(NoSuchMethodException::class)
        fun getTestConstructor(theClass: Class<*>): Constructor<*> {
            try {
                return theClass.getConstructor(String::class.java)
            } catch (e: NoSuchMethodException) {
                // fall through
            }

            return theClass.getConstructor()
        }

        /**
         * Returns a test which will fail and log a warning message.
         */
        fun warning(message: String): Test {
            return object : TestCase("warning") {
                override fun runTest() {
                    TestCase.fail(message)
                }
            }
        }

        /**
         * Converts the stack trace into a string
         */
        private fun exceptionToString(e: Throwable): String {
            val stringWriter = StringWriter()
            val writer = PrintWriter(stringWriter)
            e.printStackTrace(writer)
            return stringWriter.toString()
        }
    }
}
