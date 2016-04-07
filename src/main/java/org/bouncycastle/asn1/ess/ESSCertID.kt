package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.IssuerSerial

class ESSCertID : ASN1Object {
    private var certHash: ASN1OctetString? = null

    var issuerSerial: IssuerSerial? = null
        private set

    /**
     * constructor
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        certHash = ASN1OctetString.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            issuerSerial = IssuerSerial.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(
            hash: ByteArray) {
        certHash = DEROctetString(hash)
    }

    constructor(
            hash: ByteArray,
            issuerSerial: IssuerSerial) {
        this.certHash = DEROctetString(hash)
        this.issuerSerial = issuerSerial
    }

    fun getCertHash(): ByteArray {
        return certHash!!.octets
    }

    /**
     *
     * ESSCertID ::= SEQUENCE {
     * certHash Hash,
     * issuerSerial IssuerSerial OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certHash)

        if (issuerSerial != null) {
            v.add(issuerSerial)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): ESSCertID? {
            if (o is ESSCertID) {
                return o
            } else if (o != null) {
                return ESSCertID(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
