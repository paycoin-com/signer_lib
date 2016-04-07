package org.bouncycastle.asn1


@Deprecated("use ASN1Boolean")
class DERBoolean : ASN1Boolean {
    /**
     * @param value
     */
    @Deprecated("use getInstance(boolean) method.\n      ")
    constructor(value: Boolean) : super(value) {
    }

    internal constructor(value: ByteArray) : super(value) {
    }
}
