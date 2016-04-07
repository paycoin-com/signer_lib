package org.junit

/**
 * Thrown when an [assertEquals(String, String)][org.junit.Assert.assertEquals] fails.
 * Create and throw a `ComparisonFailure` manually if you want to show users the
 * difference between two complex strings.
 *
 *
 * Inspired by a patch from Alex Chaffee (alex@purpletech.com)

 * @since 4.0
 */
class ComparisonFailure
/**
 * Constructs a comparison failure.

 * @param message the identifying message or null
 * *
 * @param expected the expected string value
 * *
 * @param actual the actual string value
 */
(message: String, /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
 /**
  * Returns the expected string value

  * @return the expected string value
  */
 val expected: String,
 /**
  * Returns the actual string value

  * @return the actual string value
  */
 val actual: String) : AssertionError(message) {

    /**
     * Returns "..." in place of common prefix and "..." in place of common suffix between expected and actual.

     * @see Throwable.getMessage
     */
    override fun getMessage(): String {
        return ComparisonCompactor(MAX_CONTEXT_LENGTH, expected, actual).compact(super.message)
    }

    private class ComparisonCompactor
    /**
     * @param contextLength the maximum length of context surrounding the difference between the compared strings.
     * * When context length is exceeded, the prefixes and suffixes are compacted.
     * *
     * @param expected the expected string value
     * *
     * @param actual the actual string value
     */
    (
            /**
             * The maximum length for `expected` and `actual` strings to show. When
             * `contextLength` is exceeded, the Strings are shortened.
             */
            private val contextLength: Int, private val expected: String?, private val actual: String?) {

        fun compact(message: String): String {
            if (expected == null || actual == null || expected == actual) {
                return Assert.format(message, expected, actual)
            } else {
                val extractor = DiffExtractor()
                val compactedPrefix = extractor.compactPrefix()
                val compactedSuffix = extractor.compactSuffix()
                return Assert.format(message,
                        compactedPrefix + extractor.expectedDiff() + compactedSuffix,
                        compactedPrefix + extractor.actualDiff() + compactedSuffix)
            }
        }

        private fun sharedPrefix(): String {
            val end = Math.min(expected!!.length, actual!!.length)
            for (i in 0..end - 1) {
                if (expected[i] != actual[i]) {
                    return expected.substring(0, i)
                }
            }
            return expected.substring(0, end)
        }

        private fun sharedSuffix(prefix: String): String {
            var suffixLength = 0
            val maxSuffixLength = Math.min(expected!!.length - prefix.length,
                    actual!!.length - prefix.length) - 1
            while (suffixLength <= maxSuffixLength) {
                if (expected[expected.length - 1 - suffixLength] != actual[actual.length - 1 - suffixLength]) {
                    break
                }
                suffixLength++
            }
            return expected.substring(expected.length - suffixLength)
        }

        private inner class DiffExtractor
        /**
         * Can not be instantiated outside [org.junit.ComparisonFailure.ComparisonCompactor].
         */
        private constructor() {
            private val sharedPrefix: String
            private val sharedSuffix: String

            init {
                sharedPrefix = sharedPrefix()
                sharedSuffix = sharedSuffix(sharedPrefix)
            }

            fun expectedDiff(): String {
                return extractDiff(expected)
            }

            fun actualDiff(): String {
                return extractDiff(actual)
            }

            fun compactPrefix(): String {
                if (sharedPrefix.length <= contextLength) {
                    return sharedPrefix
                }
                return ELLIPSIS + sharedPrefix.substring(sharedPrefix.length - contextLength)
            }

            fun compactSuffix(): String {
                if (sharedSuffix.length <= contextLength) {
                    return sharedSuffix
                }
                return sharedSuffix.substring(0, contextLength) + ELLIPSIS
            }

            private fun extractDiff(source: String): String {
                return DIFF_START + source.substring(sharedPrefix.length, source.length - sharedSuffix.length)
                +DIFF_END
            }
        }

        companion object {
            private val ELLIPSIS = "..."
            private val DIFF_END = "]"
            private val DIFF_START = "["
        }
    }

    companion object {
        /**
         * The maximum length for expected and actual strings. If it is exceeded, the strings should be shortened.

         * @see ComparisonCompactor
         */
        private val MAX_CONTEXT_LENGTH = 20
        private val serialVersionUID = 1L
    }
}
