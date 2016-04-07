package org.junit.internal.runners

import java.util.Arrays

/**
 * Use the published version:
 * [org.junit.runners.model.InitializationError]
 * This may disappear as soon as 1 April 2009
 */
@Deprecated("")
class InitializationError(/*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
        val causes: List<Throwable>) : Exception() {

    constructor(vararg errors: Throwable) : this(Arrays.asList(*errors)) {
    }

    constructor(string: String) : this(Exception(string)) {
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
