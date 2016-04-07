package org.bouncycastle.asn1.pkcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.X509Name

class IssuerAndSerialNumber : ASN1Object {
    var name: X500Name
        internal set
    var certificateSerialNumber: ASN1Integer
        internal set

    private constructor(
            seq: ASN1Sequence) {
        this.name = X500Name.getInstance(seq.getObjectAt(0))
        this.certificateSerialNumber = seq.getObjectAt(1) as ASN1Integer
    }

    constructor(
            name: X509Name,
            certSerialNumber: BigInteger) {
        this.name = X500Name.getInstance(name.toASN1Primitive())
        this.certificateSerialNumber = ASN1Integer(certSerialNumber)
    }

    constructor(
            name: X509Name,
            certSerialNumber: ASN1Integer) {
        this.name = X500Name.getInstance(name.toASN1Primitive())
        this.certificateSerialNumber = certSerialNumber
    }

    constructor(
            name: X500Name,
            certSerialNumber: BigInteger) {
        this.name = name
        this.certificateSerialNumber = ASN1Integer(certSerialNumber)
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(name)
        v.add(certificateSerialNumber)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): IssuerAndSerialNumber? {
            if (obj is IssuerAndSerialNumber) {
                return obj
            } else if (obj != null) {
                return IssuerAndSerialNumber(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
