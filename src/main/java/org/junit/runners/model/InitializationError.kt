package org.junit.runners.model

import java.util.Arrays

/**
 * Represents one or more problems encountered while initializing a Runner

 * @since 4.5
 */
class InitializationError
/**
 * Construct a new `InitializationError` with one or more
 * errors `errors` as causes
 */
(/*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
        /**
         * Returns one or more Throwables that led to this initialization error.
         */
        val causes: List<Throwable>) : Exception() {

    constructor(error: Throwable) : this(Arrays.asList(error)) {
    }

    /**
     * Construct a new `InitializationError` with one cause
     * with message `string`
     */
    constructor(string: String) : this(Exception(string)) {
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
