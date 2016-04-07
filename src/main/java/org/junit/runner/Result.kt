package org.junit.runner

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass
import java.io.ObjectStreamField
import java.io.Serializable
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

/**
 * A `Result` collects and summarizes information from running multiple tests.
 * All tests are counted -- additional information is collected from tests that fail.

 * @since 4.0
 */
class Result : Serializable {
    private val count: AtomicInteger
    private val ignoreCount: AtomicInteger
    private val failures: CopyOnWriteArrayList<Failure>
    private val runTime: AtomicLong
    private val startTime: AtomicLong

    /** Only set during deserialization process.  */
    private var serializedForm: SerializedForm? = null

    constructor() {
        count = AtomicInteger()
        ignoreCount = AtomicInteger()
        failures = CopyOnWriteArrayList<Failure>()
        runTime = AtomicLong()
        startTime = AtomicLong()
    }

    private constructor(serializedForm: SerializedForm) {
        count = serializedForm.fCount
        ignoreCount = serializedForm.fIgnoreCount
        failures = CopyOnWriteArrayList(serializedForm.fFailures)
        runTime = AtomicLong(serializedForm.fRunTime)
        startTime = AtomicLong(serializedForm.fStartTime)
    }

    /**
     * @return the number of tests run
     */
    val runCount: Int
        get() = count.get()

    /**
     * @return the number of tests that failed during the run
     */
    val failureCount: Int
        get() = failures.size

    /**
     * @return the number of milliseconds it took to run the entire suite to run
     */
    fun getRunTime(): Long {
        return runTime.get()
    }

    /**
     * @return the [Failure]s describing tests that failed and the problems they encountered
     */
    fun getFailures(): List<Failure> {
        return failures
    }

    /**
     * @return the number of tests ignored during the run
     */
    fun getIgnoreCount(): Int {
        return ignoreCount.get()
    }

    /**
     * @return `true` if all tests succeeded
     */
    fun wasSuccessful(): Boolean {
        return failureCount == 0
    }

    @Throws(IOException::class)
    private fun writeObject(s: ObjectOutputStream) {
        val serializedForm = SerializedForm(this)
        serializedForm.serialize(s)
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    private fun readObject(s: ObjectInputStream) {
        serializedForm = SerializedForm.deserialize(s)
    }

    private fun readResolve(): Any {
        return Result(serializedForm)
    }

    @RunListener.ThreadSafe
    private inner class Listener : RunListener() {
        @Throws(Exception::class)
        override fun testRunStarted(description: Description) {
            startTime.set(System.currentTimeMillis())
        }

        @Throws(Exception::class)
        override fun testRunFinished(result: Result) {
            val endTime = System.currentTimeMillis()
            runTime.addAndGet(endTime - startTime.get())
        }

        @Throws(Exception::class)
        override fun testFinished(description: Description) {
            count.andIncrement
        }

        @Throws(Exception::class)
        override fun testFailure(failure: Failure) {
            failures.add(failure)
        }

        @Throws(Exception::class)
        override fun testIgnored(description: Description) {
            ignoreCount.andIncrement
        }

        override fun testAssumptionFailure(failure: Failure) {
            // do nothing: same as passing (for 4.5; may change in 4.6)
        }
    }

    /**
     * Internal use only.
     */
    fun createListener(): RunListener {
        return Listener()
    }

    /**
     * Represents the serialized output of `Result`. The fields on this
     * class match the files that `Result` had in JUnit 4.11.
     */
    private class SerializedForm : Serializable {
        private val fCount: AtomicInteger
        private val fIgnoreCount: AtomicInteger
        private val fFailures: List<Failure>
        private val fRunTime: Long
        private val fStartTime: Long

        constructor(result: Result) {
            fCount = result.count
            fIgnoreCount = result.ignoreCount
            fFailures = Collections.synchronizedList(ArrayList(result.failures))
            fRunTime = result.runTime.toLong()
            fStartTime = result.startTime.toLong()
        }

        @SuppressWarnings("unchecked")
        @Throws(IOException::class)
        private constructor(fields: ObjectInputStream.GetField) {
            fCount = fields.get("fCount", null) as AtomicInteger
            fIgnoreCount = fields.get("fIgnoreCount", null) as AtomicInteger
            fFailures = fields.get("fFailures", null) as List<Failure>
            fRunTime = fields.get("fRunTime", 0L)
            fStartTime = fields.get("fStartTime", 0L)
        }

        @Throws(IOException::class)
        fun serialize(s: ObjectOutputStream) {
            val fields = s.putFields()
            fields.put("fCount", fCount)
            fields.put("fIgnoreCount", fIgnoreCount)
            fields.put("fFailures", fFailures)
            fields.put("fRunTime", fRunTime)
            fields.put("fStartTime", fStartTime)
            s.writeFields()
        }

        companion object {
            private val serialVersionUID = 1L

            @Throws(ClassNotFoundException::class, IOException::class)
            fun deserialize(s: ObjectInputStream): SerializedForm {
                val fields = s.readFields()
                return SerializedForm(fields)
            }
        }
    }

    companion object {
        private val serialVersionUID = 1L
        private val serialPersistentFields = ObjectStreamClass.lookup(SerializedForm::class.java).fields
    }
}
