package org.junit.experimental.theories.internal

import java.util.Arrays

class ParameterizedAssertionError(targetException: Throwable,
                                  methodName: String, vararg params: Any) : AssertionError(String.format("%s(%s)", methodName, ParameterizedAssertionError.join(", ", *params))) {

    init {
        this.initCause(targetException)
    }

    override fun equals(obj: Any?): Boolean {
        return obj is ParameterizedAssertionError && toString() == obj.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    companion object {
        private val serialVersionUID = 1L

        fun join(delimiter: String, vararg params: Any): String {
            return join(delimiter, Arrays.asList(*params))
        }

        fun join(delimiter: String, values: Collection<Any>): String {
            val sb = StringBuilder()
            val iter = values.iterator()
            while (iter.hasNext()) {
                val next = iter.next()
                sb.append(stringValueOf(next))
                if (iter.hasNext()) {
                    sb.append(delimiter)
                }
            }
            return sb.toString()
        }

        private fun stringValueOf(next: Any): String {
            try {
                return next.toString()
            } catch (e: Throwable) {
                return "[toString failed]"
            }

        }
    }
}