package org.bouncycastle.asn1

import java.util.Date

/**
 * DER Generalized time object.
 */
class DERGeneralizedTime : ASN1GeneralizedTime {

    internal constructor(bytes: ByteArray) : super(bytes) {
    }

    constructor(time: Date) : super(time) {
    }

    constructor(time: String) : super(time) {
    }

    // TODO: create proper DER encoding.
}
