package junit.framework

/**
 * Thrown when an assert equals for Strings failed.

 * Inspired by a patch from Alex Chaffee mailto:alex@purpletech.com
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
(message: String,
 /**
  * Gets the expected string value

  * @return the expected string value
  */
 val expected: String,
 /**
  * Gets the actual string value

  * @return the actual string value
  */
 val actual: String) : AssertionFailedError(message) {

    /**
     * Returns "..." in place of common prefix and "..." in
     * place of common suffix between expected and actual.

     * @see Throwable.getMessage
     */
    override fun getMessage(): String {
        return ComparisonCompactor(MAX_CONTEXT_LENGTH, expected, actual).compact(super.message)
    }

    companion object {
        private val MAX_CONTEXT_LENGTH = 20
        private val serialVersionUID = 1L
    }
}