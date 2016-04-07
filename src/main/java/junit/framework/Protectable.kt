package junit.framework

/**
 * A *Protectable* can be run and can throw a Throwable.

 * @see TestResult
 */
interface Protectable {

    /**
     * Run the the following method protected.
     */
    @Throws(Throwable::class)
    fun protect()
}