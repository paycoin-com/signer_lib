package org.bouncycastle.asn1

class ASN1ParsingException : IllegalStateException {
    private val cause: Throwable

    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message) {
        this.cause = cause
    }

    override fun getCause(): Throwable {
        return cause
    }
}
