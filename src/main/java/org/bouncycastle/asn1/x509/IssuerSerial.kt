package org.bouncycastle.asn1.x509

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name

class IssuerSerial : ASN1Object {
    var issuer: GeneralNames
        internal set
    var serial: ASN1Integer
        internal set
    var issuerUID: DERBitString? = null
        internal set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2 && seq.size() != 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        issuer = GeneralNames.getInstance(seq.getObjectAt(0))
        serial = ASN1Integer.getInstance(seq.getObjectAt(1))

        if (seq.size() == 3) {
            issuerUID = DERBitString.getInstance(seq.getObjectAt(2))
        }
    }

    constructor(
            issuer: X500Name,
            serial: BigInteger) : this(GeneralNames(GeneralName(issuer)), ASN1Integer(serial)) {
    }

    constructor(
            issuer: GeneralNames,
            serial: BigInteger) : this(issuer, ASN1Integer(serial)) {
    }

    constructor(
            issuer: GeneralNames,
            serial: ASN1Integer) {
        this.issuer = issuer
        this.serial = serial
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * IssuerSerial  ::=  SEQUENCE {
     * issuer         GeneralNames,
     * serial         CertificateSerialNumber,
     * issuerUID      UniqueIdentifier OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(issuer)
        v.add(serial)

        if (issuerUID != null) {
            v.add(issuerUID)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): IssuerSerial? {
            if (obj is IssuerSerial) {
                return obj
            }

            if (obj != null) {
                return IssuerSerial(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): IssuerSerial {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
