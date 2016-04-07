package org.junit.runner

import org.junit.runner.notification.RunNotifier

/**
 * A `Runner` runs tests and notifies a [org.junit.runner.notification.RunNotifier]
 * of significant events as it does so. You will need to subclass `Runner`
 * when using [org.junit.runner.RunWith] to invoke a custom runner. When creating
 * a custom runner, in addition to implementing the abstract methods here you must
 * also provide a constructor that takes as an argument the [Class] containing
 * the tests.

 *
 * The default runner implementation guarantees that the instances of the test case
 * class will be constructed immediately before running the test and that the runner
 * will retain no reference to the test case instances, generally making them
 * available for garbage collection.

 * @see org.junit.runner.Description

 * @see org.junit.runner.RunWith

 * @since 4.0
 */
abstract class Runner : Describable {
    /*
     * (non-Javadoc)
     * @see org.junit.runner.Describable#getDescription()
     */
    abstract override val description: Description

    /**
     * Run the tests for this runner.

     * @param notifier will be notified of events while tests are being run--tests being
     * * started, finishing, and failing
     */
    abstract fun run(notifier: RunNotifier)

    /**
     * @return the number of tests to be run by the receiver
     */
    fun testCount(): Int {
        return description.testCount()
    }
}
