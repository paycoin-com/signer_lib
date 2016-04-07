package org.bouncycastle.asn1.cmp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class ErrorMsgContent : ASN1Object {
    var pkiStatusInfo: PKIStatusInfo? = null
        private set
    var errorCode: ASN1Integer? = null
        private set
    var errorDetails: PKIFreeText? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val en = seq.objects

        pkiStatusInfo = PKIStatusInfo.getInstance(en.nextElement())

        while (en.hasMoreElements()) {
            val o = en.nextElement()

            if (o is ASN1Integer) {
                errorCode = ASN1Integer.getInstance(o)
            } else {
                errorDetails = PKIFreeText.getInstance(o)
            }
        }
    }

    @JvmOverloads constructor(
            pkiStatusInfo: PKIStatusInfo?,
            errorCode: ASN1Integer? = null,
            errorDetails: PKIFreeText? = null) {
        if (pkiStatusInfo == null) {
            throw IllegalArgumentException("'pkiStatusInfo' cannot be null")
        }

        this.pkiStatusInfo = pkiStatusInfo
        this.errorCode = errorCode
        this.errorDetails = errorDetails
    }

    /**
     *
     * ErrorMsgContent ::= SEQUENCE {
     * pKIStatusInfo          PKIStatusInfo,
     * errorCode              INTEGER           OPTIONAL,
     * -- implementation-specific error codes
     * errorDetails           PKIFreeText       OPTIONAL
     * -- implementation-specific error details
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(pkiStatusInfo)
        addOptional(v, errorCode)
        addOptional(v, errorDetails)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(obj)
        }
    }

    companion object {

        fun getInstance(o: Any?): ErrorMsgContent? {
            if (o is ErrorMsgContent) {
                return o
            }

            if (o != null) {
                return ErrorMsgContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
