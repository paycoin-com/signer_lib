package org.bouncycastle.asn1

import java.math.BigInteger


@Deprecated("Use ASN1Enumerated instead of this.")
class DEREnumerated : ASN1Enumerated {
    /**
     * @param bytes the value of this enumerated as an encoded BigInteger (signed).
     * *
     */
    @Deprecated("use ASN1Enumerated")
    internal constructor(bytes: ByteArray) : super(bytes) {
    }

    /**
     * @param value the value of this enumerated.
     * *
     */
    @Deprecated("use ASN1Enumerated")
    constructor(value: BigInteger) : super(value) {
    }

    /**
     * @param value the value of this enumerated.
     * *
     */
    @Deprecated("use ASN1Enumerated")
    constructor(value: Int) : super(value) {
    }
}
