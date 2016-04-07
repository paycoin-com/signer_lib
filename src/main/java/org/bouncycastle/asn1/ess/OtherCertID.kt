package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.DigestInfo
import org.bouncycastle.asn1.x509.IssuerSerial

class OtherCertID : ASN1Object {
    private var otherCertHash: ASN1Encodable? = null
    var issuerSerial: IssuerSerial? = null
        private set

    /**
     * constructor
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        if (seq.getObjectAt(0).toASN1Primitive() is ASN1OctetString) {
            otherCertHash = ASN1OctetString.getInstance(seq.getObjectAt(0))
        } else {
            otherCertHash = DigestInfo.getInstance(seq.getObjectAt(0))

        }

        if (seq.size() > 1) {
            issuerSerial = IssuerSerial.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(
            algId: AlgorithmIdentifier,
            digest: ByteArray) {
        this.otherCertHash = DigestInfo(algId, digest)
    }

    constructor(
            algId: AlgorithmIdentifier,
            digest: ByteArray,
            issuerSerial: IssuerSerial) {
        this.otherCertHash = DigestInfo(algId, digest)
        this.issuerSerial = issuerSerial
    }

    // SHA-1
    val algorithmHash: AlgorithmIdentifier
        get() {
            if (otherCertHash!!.toASN1Primitive() is ASN1OctetString) {
                return AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)
            } else {
                return DigestInfo.getInstance(otherCertHash)!!.algorithmId
            }
        }

    // SHA-1
    val certHash: ByteArray
        get() {
            if (otherCertHash!!.toASN1Primitive() is ASN1OctetString) {
                return (otherCertHash!!.toASN1Primitive() as ASN1OctetString).octets
            } else {
                return DigestInfo.getInstance(otherCertHash)!!.digest
            }
        }

    /**
     *
     * OtherCertID ::= SEQUENCE {
     * otherCertHash    OtherHash,
     * issuerSerial     IssuerSerial OPTIONAL }

     * OtherHash ::= CHOICE {
     * sha1Hash     OCTET STRING,
     * otherHash    OtherHashAlgAndValue }

     * OtherHashAlgAndValue ::= SEQUENCE {
     * hashAlgorithm    AlgorithmIdentifier,
     * hashValue        OCTET STRING }

     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(otherCertHash)

        if (issuerSerial != null) {
            v.add(issuerSerial)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): OtherCertID? {
            if (o is OtherCertID) {
                return o
            } else if (o != null) {
                return OtherCertID(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
