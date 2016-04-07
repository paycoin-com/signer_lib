package org.junit.internal

import java.io.PrintStream

class RealSystem : JUnitSystem {

    /**
     * Will be removed in the next major release
     */
    @Deprecated("")
    override fun exit(code: Int) {
        System.exit(code)
    }

    override fun out(): PrintStream {
        return System.out
    }

}
