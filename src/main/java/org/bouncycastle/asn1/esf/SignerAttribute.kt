package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Attribute
import org.bouncycastle.asn1.x509.AttributeCertificate


class SignerAttribute : ASN1Object {
    /**
     * Return the sequence of choices - the array elements will either be of
     * type Attribute[] or AttributeCertificate depending on what tag was used.

     * @return array of choices.
     */
    var values: Array<Any>? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        var index = 0
        values = arrayOfNulls<Any>(seq.size())

        val e = seq.objects
        while (e.hasMoreElements()) {
            val taggedObject = ASN1TaggedObject.getInstance(e.nextElement())

            if (taggedObject.tagNo == 0) {
                val attrs = ASN1Sequence.getInstance(taggedObject, true)
                val attributes = arrayOfNulls<Attribute>(attrs.size())

                for (i in attributes.indices) {
                    attributes[i] = Attribute.getInstance(attrs.getObjectAt(i))
                }
                values[index] = attributes
            } else if (taggedObject.tagNo == 1) {
                values[index] = AttributeCertificate.getInstance(ASN1Sequence.getInstance(taggedObject, true))
            } else {
                throw IllegalArgumentException("illegal tag: " + taggedObject.tagNo)
            }
            index++
        }
    }

    constructor(
            claimedAttributes: Array<Attribute>) {
        this.values = arrayOfNulls<Any>(1)
        this.values[0] = claimedAttributes
    }

    constructor(
            certifiedAttributes: AttributeCertificate) {
        this.values = arrayOfNulls<Any>(1)
        this.values[0] = certifiedAttributes
    }

    /**

     *
     * SignerAttribute ::= SEQUENCE OF CHOICE {
     * claimedAttributes   [0] ClaimedAttributes,
     * certifiedAttributes [1] CertifiedAttributes }

     * ClaimedAttributes ::= SEQUENCE OF Attribute
     * CertifiedAttributes ::= AttributeCertificate -- as defined in RFC 3281: see clause 4.1.
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        for (i in values!!.indices) {
            if (values!![i] is Array<Attribute>) {
                v.add(DERTaggedObject(0, DERSequence(values!![i] as Array<Attribute>)))
            } else {
                v.add(DERTaggedObject(1, values!![i] as AttributeCertificate))
            }
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): SignerAttribute? {
            if (o is SignerAttribute) {
                return o
            } else if (o != null) {
                return SignerAttribute(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
