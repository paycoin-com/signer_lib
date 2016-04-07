package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.GeneralName

class SinglePubInfo private constructor(seq: ASN1Sequence) : ASN1Object() {
    private val pubMethod: ASN1Integer
    var pubLocation: GeneralName? = null
        private set

    init {
        pubMethod = ASN1Integer.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            pubLocation = GeneralName.getInstance(seq.getObjectAt(1))
        }
    }

    /**
     *
     * SinglePubInfo ::= SEQUENCE {
     * pubMethod    INTEGER {
     * dontCare    (0),
     * x500        (1),
     * web         (2),
     * ldap        (3) },
     * pubLocation  GeneralName OPTIONAL }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(pubMethod)

        if (pubLocation != null) {
            v.add(pubLocation)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): SinglePubInfo? {
            if (o is SinglePubInfo) {
                return o
            }

            if (o != null) {
                return SinglePubInfo(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
