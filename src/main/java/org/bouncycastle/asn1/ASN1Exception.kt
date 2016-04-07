package org.bouncycastle.asn1

import java.io.IOException

class ASN1Exception : IOException {
    private val cause: Throwable

    internal constructor(message: String) : super(message) {
    }

    internal constructor(message: String, cause: Throwable) : super(message) {
        this.cause = cause
    }

    override fun getCause(): Throwable {
        return cause
    }
}
