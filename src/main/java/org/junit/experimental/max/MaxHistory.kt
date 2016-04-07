package org.junit.experimental.max

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Comparator
import java.util.HashMap

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

/**
 * Stores a subset of the history of each test:
 *
 *  * Last failure timestamp
 *  * Duration of last execution
 *
 */
class MaxHistory private constructor(private val fHistoryStore: File) : Serializable {

    /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
    private val fDurations = HashMap<String, Long>()
    private val fFailureTimestamps = HashMap<String, Long>()

    @Throws(IOException::class)
    private fun save() {
        val stream = ObjectOutputStream(FileOutputStream(
                fHistoryStore))
        stream.writeObject(this)
        stream.close()
    }

    internal fun getFailureTimestamp(key: Description): Long? {
        return fFailureTimestamps[key.toString()]
    }

    internal fun putTestFailureTimestamp(key: Description, end: Long) {
        fFailureTimestamps.put(key.toString(), end)
    }

    internal fun isNewTest(key: Description): Boolean {
        return !fDurations.containsKey(key.toString())
    }

    internal fun getTestDuration(key: Description): Long? {
        return fDurations[key.toString()]
    }

    internal fun putTestDuration(description: Description, duration: Long) {
        fDurations.put(description.toString(), duration)
    }

    private inner class RememberingListener : RunListener() {
        private val overallStart = System.currentTimeMillis()

        private val starts = HashMap<Description, Long>()

        @Throws(Exception::class)
        override fun testStarted(description: Description) {
            starts.put(description, System.nanoTime()) // Get most accurate
            // possible time
        }

        @Throws(Exception::class)
        override fun testFinished(description: Description) {
            val end = System.nanoTime()
            val start = starts[description]
            putTestDuration(description, end - start)
        }

        @Throws(Exception::class)
        override fun testFailure(failure: Failure) {
            putTestFailureTimestamp(failure.description, overallStart)
        }

        @Throws(Exception::class)
        override fun testRunFinished(result: Result) {
            save()
        }
    }

    private inner class TestComparator : Comparator<Description> {
        override fun compare(o1: Description, o2: Description): Int {
            // Always prefer new tests
            if (isNewTest(o1)) {
                return -1
            }
            if (isNewTest(o2)) {
                return 1
            }
            // Then most recently failed first
            val result = getFailure(o2)!!.compareTo(getFailure(o1))
            return if (result != 0)
                result
            else
                getTestDuration(o1)!!.compareTo(getTestDuration(o2))// Then shorter tests first
        }

        private fun getFailure(key: Description): Long? {
            val result = getFailureTimestamp(key) ?: return 0L // 0 = "never failed (that I know about)"
            return result
        }
    }

    /**
     * @return a listener that will update this history based on the test
     * *         results reported.
     */
    fun listener(): RunListener {
        return RememberingListener()
    }

    /**
     * @return a comparator that ranks tests based on the JUnit Max sorting
     * *         rules, as described in the [MaxCore] class comment.
     */
    fun testComparator(): Comparator<Description> {
        return TestComparator()
    }

    companion object {
        private val serialVersionUID = 1L

        /**
         * Loads a [MaxHistory] from `file`, or generates a new one that
         * will be saved to `file`.
         */
        fun forFolder(file: File): MaxHistory {
            if (file.exists()) {
                try {
                    return readHistory(file)
                } catch (e: CouldNotReadCoreException) {
                    e.printStackTrace()
                    file.delete()
                }

            }
            return MaxHistory(file)
        }

        @Throws(CouldNotReadCoreException::class)
        private fun readHistory(storedResults: File): MaxHistory {
            try {
                val file = FileInputStream(storedResults)
                try {
                    val stream = ObjectInputStream(file)
                    try {
                        return stream.readObject() as MaxHistory
                    } finally {
                        stream.close()
                    }
                } finally {
                    file.close()
                }
            } catch (e: Exception) {
                throw CouldNotReadCoreException(e)
            }

        }
    }
}
