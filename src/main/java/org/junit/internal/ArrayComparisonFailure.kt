package org.junit.internal

import java.util.ArrayList

import org.junit.Assert

/**
 * Thrown when two array elements differ

 * @see Assert.assertArrayEquals
 */
class ArrayComparisonFailure
/**
 * Construct a new `ArrayComparisonFailure` with an error text and the array's
 * dimension that was not equal

 * @param cause the exception that caused the array's content to fail the assertion test
 * *
 * @param index the array position of the objects that are not equal.
 * *
 * @see Assert.assertArrayEquals
 */
(private val fMessage: String?, cause: AssertionError, index: Int) : AssertionError() {

    /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
    private val fIndices = ArrayList<Int>()

    init {
        initCause(cause)
        addDimension(index)
    }

    fun addDimension(index: Int) {
        fIndices.add(0, index)
    }

    override fun getMessage(): String {
        val sb = StringBuilder()
        if (fMessage != null) {
            sb.append(fMessage)
        }
        sb.append("arrays first differed at element ")
        for (each in fIndices) {
            sb.append("[")
            sb.append(each)
            sb.append("]")
        }
        sb.append("; ")
        sb.append(cause.message)
        return sb.toString()
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return message
    }

    companion object {

        private val serialVersionUID = 1L
    }
}
