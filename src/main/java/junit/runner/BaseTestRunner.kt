package junit.runner

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.text.NumberFormat
import java.util.Properties

import junit.framework.AssertionFailedError
import junit.framework.Test
import junit.framework.TestListener
import junit.framework.TestSuite

/**
 * Base class for all test runners.
 * This class was born live on stage in Sardinia during XP2000.
 */
abstract class BaseTestRunner : TestListener {
    internal var fLoading = true

    /*
    * Implementation of TestListener
    */
    @Synchronized override fun startTest(test: Test) {
        testStarted(test.toString())
    }

    @Synchronized override fun endTest(test: Test) {
        testEnded(test.toString())
    }

    @Synchronized override fun addError(test: Test, e: Throwable) {
        testFailed(TestRunListener.STATUS_ERROR, test, e)
    }

    @Synchronized override fun addFailure(test: Test, e: AssertionFailedError) {
        testFailed(TestRunListener.STATUS_FAILURE, test, e)
    }

    // TestRunListener implementation

    abstract fun testStarted(testName: String)

    abstract fun testEnded(testName: String)

    abstract fun testFailed(status: Int, test: Test, e: Throwable)

    /**
     * Returns the Test corresponding to the given suite. This is
     * a template method, subclasses override runFailed(), clearStatus().
     */
    fun getTest(suiteClassName: String): Test? {
        if (suiteClassName.length <= 0) {
            clearStatus()
            return null
        }
        var testClass: Class<*>? = null
        try {
            testClass = loadSuiteClass(suiteClassName)
        } catch (e: ClassNotFoundException) {
            var clazz: String? = e.message
            if (clazz == null) {
                clazz = suiteClassName
            }
            runFailed("Class not found \"" + clazz + "\"")
            return null
        } catch (e: Exception) {
            runFailed("Error: " + e.toString())
            return null
        }

        var suiteMethod: Method? = null
        try {
            suiteMethod = testClass.getMethod(SUITE_METHODNAME)
        } catch (e: Exception) {
            // try to extract a test suite automatically
            clearStatus()
            return TestSuite(testClass)
        }

        if (!Modifier.isStatic(suiteMethod!!.modifiers)) {
            runFailed("Suite() method must be static")
            return null
        }
        var test: Test? = null
        try {
            test = suiteMethod.invoke(null) as Test // static method
            if (test == null) {
                return test
            }
        } catch (e: InvocationTargetException) {
            runFailed("Failed to invoke suite():" + e.targetException.toString())
            return null
        } catch (e: IllegalAccessException) {
            runFailed("Failed to invoke suite():" + e.toString())
            return null
        }

        clearStatus()
        return test
    }

    /**
     * Returns the formatted string of the elapsed time.
     */
    fun elapsedTimeAsString(runTime: Long): String {
        return NumberFormat.getInstance().format(runTime.toDouble() / 1000)
    }

    /**
     * Processes the command line arguments and
     * returns the name of the suite class to run or null
     */
    protected fun processArguments(args: Array<String>): String {
        var suiteName: String? = null
        var i = 0
        while (i < args.size) {
            if (args[i] == "-noloading") {
                setLoading(false)
            } else if (args[i] == "-nofilterstack") {
                fgFilterStack = false
            } else if (args[i] == "-c") {
                if (args.size > i + 1) {
                    suiteName = extractClassName(args[i + 1])
                } else {
                    println("Missing Test class name")
                }
                i++
            } else {
                suiteName = args[i]
            }
            i++
        }
        return suiteName
    }

    /**
     * Sets the loading behaviour of the test runner
     */
    fun setLoading(enable: Boolean) {
        fLoading = enable
    }

    /**
     * Extract the class name from a String in VA/Java style
     */
    fun extractClassName(className: String): String {
        if (className.startsWith("Default package for")) {
            return className.substring(className.lastIndexOf(".") + 1)
        }
        return className
    }

    /**
     * Override to define how to handle a failed loading of
     * a test suite.
     */
    protected abstract fun runFailed(message: String)

    /**
     * Returns the loaded Class for a suite name.
     */
    @Throws(ClassNotFoundException::class)
    protected fun loadSuiteClass(suiteClassName: String): Class<*> {
        return Class.forName(suiteClassName)
    }

    /**
     * Clears the status message.
     */
    protected fun clearStatus() {
        // Belongs in the GUI TestRunner class
    }

    protected fun useReloadingTestSuiteLoader(): Boolean {
        return getPreference("loading") == "true" && fLoading
    }

    companion object {
        val SUITE_METHODNAME = "suite"

        protected var preferences: Properties? = null
            get() {
                if (preferences == null) {
                    preferences = Properties()
                    preferences!!.put("loading", "true")
                    preferences!!.put("filterstack", "true")
                    readPreferences()
                }
                return preferences
            }
        internal var fgMaxMessageLength = 500
        internal var fgFilterStack = true

        @Throws(IOException::class)
        fun savePreferences() {
            val fos = FileOutputStream(preferencesFile)
            try {
                preferences.store(fos, "")
            } finally {
                fos.close()
            }
        }

        fun setPreference(key: String, value: String) {
            preferences.put(key, value)
        }

        /**
         * Truncates a String to the maximum length.
         */
        fun truncate(s: String): String {
            var s = s
            if (fgMaxMessageLength != -1 && s.length > fgMaxMessageLength) {
                s = s.substring(0, fgMaxMessageLength) + "..."
            }
            return s
        }

        private val preferencesFile: File
            get() {
                val home = System.getProperty("user.home")
                return File(home, "junit.properties")
            }

        private fun readPreferences() {
            var `is`: InputStream? = null
            try {
                `is` = FileInputStream(preferencesFile)
                preferences = Properties(preferences)
                preferences.load(`is`)
            } catch (ignored: IOException) {
            } finally {
                try {
                    if (`is` != null) {
                        `is`.close()
                    }
                } catch (e1: IOException) {
                }

            }
        }

        fun getPreference(key: String): String? {
            return preferences.getProperty(key)
        }

        fun getPreference(key: String, dflt: Int): Int {
            val value = getPreference(key)
            var intValue = dflt
            if (value == null) {
                return intValue
            }
            try {
                intValue = Integer.parseInt(value)
            } catch (ne: NumberFormatException) {
            }

            return intValue
        }

        /**
         * Returns a filtered stack trace
         */
        fun getFilteredTrace(e: Throwable): String {
            val stringWriter = StringWriter()
            val writer = PrintWriter(stringWriter)
            e.printStackTrace(writer)
            val trace = stringWriter.toString()
            return BaseTestRunner.getFilteredTrace(trace)
        }

        /**
         * Filters stack frames from internal JUnit classes
         */
        fun getFilteredTrace(stack: String): String {
            if (showStackRaw()) {
                return stack
            }

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            val sr = StringReader(stack)
            val br = BufferedReader(sr)

            var line: String
            try {
                while ((line = br.readLine()) != null) {
                    if (!filterLine(line)) {
                        pw.println(line)
                    }
                }
            } catch (IOException: Exception) {
                return stack // return the stack unfiltered
            }

            return sw.toString()
        }

        protected fun showStackRaw(): Boolean {
            return getPreference("filterstack") != "true" || fgFilterStack == false
        }

        internal fun filterLine(line: String): Boolean {
            val patterns = arrayOf("junit.framework.TestCase", "junit.framework.TestResult", "junit.framework.TestSuite", "junit.framework.Assert.", // don't filter AssertionFailure
                    "junit.swingui.TestRunner", "junit.awtui.TestRunner", "junit.textui.TestRunner", "java.lang.reflect.Method.invoke(")
            for (i in patterns.indices) {
                if (line.indexOf(patterns[i]) > 0) {
                    return true
                }
            }
            return false
        }

        init {
            fgMaxMessageLength = getPreference("maxmessage", fgMaxMessageLength)
        }
    }

}
