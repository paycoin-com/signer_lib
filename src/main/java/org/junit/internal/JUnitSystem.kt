package org.junit.internal

import java.io.PrintStream

interface JUnitSystem {

    /**
     * Will be removed in the next major release
     */
    @Deprecated("")
    fun exit(code: Int)

    fun out(): PrintStream
}
