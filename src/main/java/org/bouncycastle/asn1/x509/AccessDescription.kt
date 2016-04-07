package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * The AccessDescription object.
 *
 * AccessDescription  ::=  SEQUENCE {
 * accessMethod          OBJECT IDENTIFIER,
 * accessLocation        GeneralName  }
 *
 */
class AccessDescription : ASN1Object {

    /**

     * @return the access method.
     */
    var accessMethod: ASN1ObjectIdentifier? = null
        internal set
    /**

     * @return the access location
     */
    var accessLocation: GeneralName? = null
        internal set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("wrong number of elements in sequence")
        }

        accessMethod = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        accessLocation = GeneralName.getInstance(seq.getObjectAt(1))
    }

    /**
     * create an AccessDescription with the oid and location provided.
     */
    constructor(
            oid: ASN1ObjectIdentifier,
            location: GeneralName) {
        accessMethod = oid
        accessLocation = location
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val accessDescription = ASN1EncodableVector()

        accessDescription.add(accessMethod)
        accessDescription.add(accessLocation)

        return DERSequence(accessDescription)
    }

    override fun toString(): String {
        return "AccessDescription: Oid(" + this.accessMethod!!.id + ")"
    }

    companion object {
        val id_ad_caIssuers = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.2")

        val id_ad_ocsp = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1")

        fun getInstance(
                obj: Any?): AccessDescription? {
            if (obj is AccessDescription) {
                return obj
            } else if (obj != null) {
                return AccessDescription(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
