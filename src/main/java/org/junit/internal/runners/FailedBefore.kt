package org.junit.internal.runners

import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
internal class FailedBefore : Exception() {
    companion object {
        private val serialVersionUID = 1L
    }
}