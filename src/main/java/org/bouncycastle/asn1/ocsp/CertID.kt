package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class CertID : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier
        internal set
    var issuerNameHash: ASN1OctetString
        internal set
    var issuerKeyHash: ASN1OctetString
        internal set
    var serialNumber: ASN1Integer
        internal set

    constructor(
            hashAlgorithm: AlgorithmIdentifier,
            issuerNameHash: ASN1OctetString,
            issuerKeyHash: ASN1OctetString,
            serialNumber: ASN1Integer) {
        this.hashAlgorithm = hashAlgorithm
        this.issuerNameHash = issuerNameHash
        this.issuerKeyHash = issuerKeyHash
        this.serialNumber = serialNumber
    }

    private constructor(
            seq: ASN1Sequence) {
        hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        issuerNameHash = seq.getObjectAt(1) as ASN1OctetString
        issuerKeyHash = seq.getObjectAt(2) as ASN1OctetString
        serialNumber = seq.getObjectAt(3) as ASN1Integer
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * CertID          ::=     SEQUENCE {
     * hashAlgorithm       AlgorithmIdentifier,
     * issuerNameHash      OCTET STRING, -- Hash of Issuer's DN
     * issuerKeyHash       OCTET STRING, -- Hash of Issuers public key
     * serialNumber        CertificateSerialNumber }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(hashAlgorithm)
        v.add(issuerNameHash)
        v.add(issuerKeyHash)
        v.add(serialNumber)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): CertID {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): CertID? {
            if (obj is CertID) {
                return obj
            } else if (obj != null) {
                return CertID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
