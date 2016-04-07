package org.bouncycastle.asn1

import java.util.Date

/**
 * DER UTC time object.
 */
class DERUTCTime : ASN1UTCTime {
    internal constructor(bytes: ByteArray) : super(bytes) {
    }

    constructor(time: Date) : super(time) {
    }

    constructor(time: String) : super(time) {
    }

    // TODO: create proper DER encoding.
}
