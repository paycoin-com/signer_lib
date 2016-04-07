package org.junit.internal

/**
 * Miscellaneous functions dealing with `Throwable`.

 * @author kcooney@google.com (Kevin Cooney)
 * *
 * @since 4.12
 */
object Throwables {

    /**
     * Rethrows the given `Throwable`, allowing the caller to
     * declare that it throws `Exception`. This is useful when
     * your callers have nothing reasonable they can do when a
     * `Throwable` is thrown. This is declared to return `Exception`
     * so it can be used in a `throw` clause:
     *
     * try {
     * doSomething();
     * } catch (Throwable e} {
     * throw Throwables.rethrowAsException(e);
     * }
     * doSomethingLater();
     *

     * @param e exception to rethrow
     * *
     * @return does not return anything
     * *
     * @since 4.12
     */
    @Throws(Exception::class)
    fun rethrowAsException(e: Throwable): Exception? {
        Throwables.rethrow<Exception>(e)
        return null // we never get here
    }

    @SuppressWarnings("unchecked")
    @Throws(T::class)
    private fun <T : Throwable> rethrow(e: Throwable) {
        throw e as T
    }
}
