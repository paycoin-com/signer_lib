package org.hamcrest

import java.lang.String.valueOf

import java.util.Arrays

import org.hamcrest.internal.ArrayIterator
import org.hamcrest.internal.SelfDescribingValueIterator

/**
 * A [Description] that is stored as a string.
 */
abstract class BaseDescription : Description {

    override fun appendText(text: String): Description {
        append(text)
        return this
    }

    override fun appendDescriptionOf(value: SelfDescribing): Description {
        value.describeTo(this)
        return this
    }

    override fun appendValue(value: Any?): Description {
        if (value == null) {
            append("null")
        } else if (value is String) {
            toJavaSyntax(value as String?)
        } else if (value is Char) {
            append('"')
            toJavaSyntax(value as Char?)
            append('"')
        } else if (value is Short) {
            append('<')
            append(descriptionOf(value))
            append("s>")
        } else if (value is Long) {
            append('<')
            append(descriptionOf(value))
            append("L>")
        } else if (value is Float) {
            append('<')
            append(descriptionOf(value))
            append("F>")
        } else if (value.javaClass.isArray) {
            appendValueList<Any>("[", ", ", "]", ArrayIterator(value))
        } else {
            append('<')
            append(descriptionOf(value))
            append('>')
        }
        return this
    }

    private fun descriptionOf(value: Any): String {
        try {
            return value.toString()
        } catch (e: Exception) {
            return value.javaClass.name + "@" + Integer.toHexString(value.hashCode())
        }

    }

    override fun <T> appendValueList(start: String, separator: String, end: String, vararg values: T): Description {
        return appendValueList(start, separator, end, Arrays.asList(*values))
    }

    override fun <T> appendValueList(start: String, separator: String, end: String, values: Iterable<T>): Description {
        return appendValueList<T>(start, separator, end, values.iterator())
    }

    private fun <T> appendValueList(start: String, separator: String, end: String, values: MutableIterator<T>): Description {
        return appendList(start, separator, end, SelfDescribingValueIterator(values))
    }

    override fun appendList(start: String, separator: String, end: String, values: Iterable<SelfDescribing>): Description {
        return appendList(start, separator, end, values.iterator())
    }

    private fun appendList(start: String, separator: String, end: String, i: Iterator<SelfDescribing>): Description {
        var separate = false

        append(start)
        while (i.hasNext()) {
            if (separate) append(separator)
            appendDescriptionOf(i.next())
            separate = true
        }
        append(end)

        return this
    }

    /**
     * Append the String str to the description.
     * The default implementation passes every character to [.append].
     * Override in subclasses to provide an efficient implementation.
     */
    protected open fun append(str: String) {
        for (i in 0..str.length - 1) {
            append(str[i])
        }
    }

    /**
     * Append the char c to the description.
     */
    protected abstract fun append(c: Char)

    private fun toJavaSyntax(unformatted: String) {
        append('"')
        for (i in 0..unformatted.length - 1) {
            toJavaSyntax(unformatted[i])
        }
        append('"')
    }

    private fun toJavaSyntax(ch: Char) {
        when (ch) {
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
}
