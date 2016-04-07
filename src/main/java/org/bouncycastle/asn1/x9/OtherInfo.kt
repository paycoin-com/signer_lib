package org.bouncycastle.asn1.x9

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * ASN.1 def for Diffie-Hellman key exchange OtherInfo structure. See
 * RFC 2631, or X9.42, for further details.
 *
 * OtherInfo ::= SEQUENCE {
 * keyInfo KeySpecificInfo,
 * partyAInfo [0] OCTET STRING OPTIONAL,
 * suppPubInfo [2] OCTET STRING
 * }
 *
 */
class OtherInfo : ASN1Object {
    /**
     * Return the key specific info for the KEK/CEK.

     * @return the key specific info.
     */
    var keyInfo: KeySpecificInfo? = null
        private set
    /**
     * PartyA info for key deriviation.

     * @return PartyA info.
     */
    var partyAInfo: ASN1OctetString? = null
        private set
    /**
     * The length of the KEK to be generated as a 4 byte big endian.

     * @return KEK length as a 4 byte big endian in an octet string.
     */
    var suppPubInfo: ASN1OctetString? = null
        private set

    constructor(
            keyInfo: KeySpecificInfo,
            partyAInfo: ASN1OctetString,
            suppPubInfo: ASN1OctetString) {
        this.keyInfo = keyInfo
        this.partyAInfo = partyAInfo
        this.suppPubInfo = suppPubInfo
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        keyInfo = KeySpecificInfo.getInstance(e.nextElement())

        while (e.hasMoreElements()) {
            val o = e.nextElement() as DERTaggedObject

            if (o.tagNo == 0) {
                partyAInfo = o.`object` as ASN1OctetString
            } else if (o.tagNo == 2) {
                suppPubInfo = o.`object` as ASN1OctetString
            }
        }
    }

    /**
     * Return an ASN.1 primitive representation of this object.

     * @return a DERSequence containing the OtherInfo values.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyInfo)

        if (partyAInfo != null) {
            v.add(DERTaggedObject(0, partyAInfo))
        }

        v.add(DERTaggedObject(2, suppPubInfo))

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a OtherInfo object from the passed in object.

         * @param obj an object for conversion or a byte[].
         * *
         * @return a OtherInfo
         */
        fun getInstance(obj: Any?): OtherInfo? {
            if (obj is OtherInfo) {
                return obj
            } else if (obj != null) {
                return OtherInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
