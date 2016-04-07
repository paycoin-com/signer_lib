package org.bouncycastle.asn1.cms

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Certificate
import org.bouncycastle.asn1.x509.X509CertificateStructure
import org.bouncycastle.asn1.x509.X509Name

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-10.2.4): IssuerAndSerialNumber object.
 *
 *
 *
 * IssuerAndSerialNumber ::= SEQUENCE {
 * issuer Name,
 * serialNumber CertificateSerialNumber
 * }

 * CertificateSerialNumber ::= INTEGER  -- See RFC 5280
 *
 */
class IssuerAndSerialNumber : ASN1Object {
    var name: X500Name? = null
        private set
    var serialNumber: ASN1Integer? = null
        private set


    @Deprecated("use getInstance() method.")
    constructor(
            seq: ASN1Sequence) {
        this.name = X500Name.getInstance(seq.getObjectAt(0))
        this.serialNumber = seq.getObjectAt(1) as ASN1Integer
    }

    constructor(
            certificate: Certificate) {
        this.name = certificate.issuer
        this.serialNumber = certificate.serialNumber
    }


    @Deprecated("use constructor taking Certificate")
    constructor(
            certificate: X509CertificateStructure) {
        this.name = certificate.issuer
        this.serialNumber = certificate.serialNumber
    }

    constructor(
            name: X500Name,
            serialNumber: BigInteger) {
        this.name = name
        this.serialNumber = ASN1Integer(serialNumber)
    }


    @Deprecated("use X500Name constructor")
    constructor(
            name: X509Name,
            serialNumber: BigInteger) {
        this.name = X500Name.getInstance(name)
        this.serialNumber = ASN1Integer(serialNumber)
    }


    @Deprecated("use X500Name constructor")
    constructor(
            name: X509Name,
            serialNumber: ASN1Integer) {
        this.name = X500Name.getInstance(name)
        this.serialNumber = serialNumber
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(name)
        v.add(serialNumber)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an IssuerAndSerialNumber object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [IssuerAndSerialNumber] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with IssuerAndSerialNumber structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
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
