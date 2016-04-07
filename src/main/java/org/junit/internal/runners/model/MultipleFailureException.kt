package org.junit.internal.runners.model

@Deprecated("")
class MultipleFailureException(errors: List<Throwable>) : org.junit.runners.model.MultipleFailureException(errors) {
    companion object {
        private val serialVersionUID = 1L
    }
}
