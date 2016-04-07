package org.hamcrest

import java.io.IOException

/**
 * A [Description] that is stored as a string.
 */
class StringDescription @JvmOverloads constructor(private val out: Appendable = StringBuilder()) : BaseDescription() {

    override fun append(str: String) {
        try {
            out.append(str)
        } catch (e: IOException) {
            throw RuntimeException("Could not write description", e)
        }

    }

    override fun append(c: Char) {
        try {
            out.append(c)
        } catch (e: IOException) {
            throw RuntimeException("Could not write description", e)
        }

    }

    /**
     * Returns the description as a string.
     */
    override fun toString(): String {
        return out.toString()
    }

    companion object {

        /**
         * Return the description of a [SelfDescribing] object as a String.

         * @param selfDescribing
         * *   The object to be described.
         * *
         * @return
         * *   The description of the object.
         */
        fun toString(selfDescribing: SelfDescribing): String {
            return StringDescription().appendDescriptionOf(selfDescribing).toString()
        }

        /**
         * Alias for [.toString].
         */
        fun asString(selfDescribing: SelfDescribing): String {
            return toString(selfDescribing)
        }
    }
}
