package org.bouncycastle.asn1.crmf

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.GeneralName

class CertId : ASN1Object {
    var issuer: GeneralName? = null
        private set
    var serialNumber: ASN1Integer? = null
        private set

    private constructor(seq: ASN1Sequence) {
        issuer = GeneralName.getInstance(seq.getObjectAt(0))
        serialNumber = ASN1Integer.getInstance(seq.getObjectAt(1))
    }

    constructor(issuer: GeneralName, serialNumber: BigInteger) : this(issuer, ASN1Integer(serialNumber)) {
    }

    constructor(issuer: GeneralName, serialNumber: ASN1Integer) {
        this.issuer = issuer
        this.serialNumber = serialNumber
    }

    /**
     *
     * CertId ::= SEQUENCE {
     * issuer           GeneralName,
     * serialNumber     INTEGER }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(issuer)
        v.add(serialNumber)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertId? {
            if (o is CertId) {
                return o
            }

            if (o != null) {
                return CertId(ASN1Sequence.getInstance(o))
            }

            return null
        }

        fun getInstance(obj: ASN1TaggedObject, isExplicit: Boolean): CertId {
            return getInstance(ASN1Sequence.getInstance(obj, isExplicit))
        }
    }
}
