package org.bouncycastle.asn1.ocsp

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive

class OCSPResponseStatus private constructor(
        private val value: ASN1Enumerated) : ASN1Object() {

    /**
     * The OCSPResponseStatus enumeration.
     *
     * OCSPResponseStatus ::= ENUMERATED {
     * successful            (0),  --Response has valid confirmations
     * malformedRequest      (1),  --Illegal confirmation request
     * internalError         (2),  --Internal error in issuer
     * tryLater              (3),  --Try again later
     * --(4) is not used
     * sigRequired           (5),  --Must sign the request
     * unauthorized          (6)   --Request unauthorized
     * }
     *
     */
    constructor(
            value: Int) : this(ASN1Enumerated(value)) {
    }

    fun getValue(): BigInteger {
        return value.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return value
    }

    companion object {
        val SUCCESSFUL = 0
        val MALFORMED_REQUEST = 1
        val INTERNAL_ERROR = 2
        val TRY_LATER = 3
        val SIG_REQUIRED = 5
        val UNAUTHORIZED = 6

        fun getInstance(
                obj: Any?): OCSPResponseStatus? {
            if (obj is OCSPResponseStatus) {
                return obj
            } else if (obj != null) {
                return OCSPResponseStatus(ASN1Enumerated.getInstance(obj))
            }

            return null
        }
    }
}
