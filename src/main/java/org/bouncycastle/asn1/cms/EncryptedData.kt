package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-8) EncryptedData object.
 *
 *
 *
 * EncryptedData ::= SEQUENCE {
 * version CMSVersion,
 * encryptedContentInfo EncryptedContentInfo,
 * unprotectedAttrs [1] IMPLICIT UnprotectedAttributes OPTIONAL }
 *
 */
class EncryptedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var encryptedContentInfo: EncryptedContentInfo? = null
        private set
    var unprotectedAttrs: ASN1Set? = null
        private set

    @JvmOverloads constructor(encInfo: EncryptedContentInfo, unprotectedAttrs: ASN1Set? = null) {
        this.version = ASN1Integer((if (unprotectedAttrs == null) 0 else 2).toLong())
        this.encryptedContentInfo = encInfo
        this.unprotectedAttrs = unprotectedAttrs
    }

    private constructor(seq: ASN1Sequence) {
        this.version = ASN1Integer.getInstance(seq.getObjectAt(0))
        this.encryptedContentInfo = EncryptedContentInfo.getInstance(seq.getObjectAt(1))

        if (seq.size() == 3) {
            this.unprotectedAttrs = ASN1Set.getInstance(seq.getObjectAt(2) as ASN1TaggedObject, false)
        }
    }

    /**
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(encryptedContentInfo)
        if (unprotectedAttrs != null) {
            v.add(BERTaggedObject(false, 1, unprotectedAttrs))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an EncryptedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [EncryptedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(o: Any?): EncryptedData? {
            if (o is EncryptedData) {
                return o
            }

            if (o != null) {
                return EncryptedData(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
