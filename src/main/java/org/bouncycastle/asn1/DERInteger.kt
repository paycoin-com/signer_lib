package org.bouncycastle.asn1

import java.math.BigInteger


@Deprecated("Use ASN1Integer instead of this,")
class DERInteger : ASN1Integer {
    /**
     * Constructor from a byte array containing a signed representation of the number.

     * @param bytes a byte array containing the signed number.A copy is made of the byte array.
     */
    constructor(bytes: ByteArray) : super(bytes, true) {
    }

    constructor(value: BigInteger) : super(value) {
    }

    constructor(value: Long) : super(value) {
    }
}
