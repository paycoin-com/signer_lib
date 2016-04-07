package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.util.Arrays

/**
 * [RFC 5084](http://tools.ietf.org/html/rfc5084): CCMParameters object.
 *
 *
 *
 * CCMParameters ::= SEQUENCE {
 * aes-nonce        OCTET STRING, -- recommended size is 12 octets
 * aes-ICVlen       AES-CCM-ICVlen DEFAULT 12 }
 *
 */
class CCMParameters : ASN1Object {
    private var nonce: ByteArray? = null
    var icvLen: Int = 0
        private set

    private constructor(
            seq: ASN1Sequence) {
        this.nonce = ASN1OctetString.getInstance(seq.getObjectAt(0)).octets

        if (seq.size() == 2) {
            this.icvLen = ASN1Integer.getInstance(seq.getObjectAt(1)).value.toInt()
        } else {
            this.icvLen = 12
        }
    }

    constructor(
            nonce: ByteArray,
            icvLen: Int) {
        this.nonce = Arrays.clone(nonce)
        this.icvLen = icvLen
    }

    fun getNonce(): ByteArray {
        return Arrays.clone(nonce)
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(DEROctetString(nonce))

        if (icvLen != 12) {
            v.add(ASN1Integer(icvLen.toLong()))
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an CCMParameters object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [org.bouncycastle.asn1.cms.CCMParameters] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with CCMParameters structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): CCMParameters? {
            if (obj is CCMParameters) {
                return obj
            } else if (obj != null) {
                return CCMParameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
