package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.IssuerSerial

class ESSCertIDv2 : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var certHash: ByteArray? = null
        private set
    var issuerSerial: IssuerSerial? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        var count = 0

        if (seq.getObjectAt(0) is ASN1OctetString) {
            // Default value
            this.hashAlgorithm = DEFAULT_ALG_ID
        } else {
            this.hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(count++).toASN1Primitive())
        }

        this.certHash = ASN1OctetString.getInstance(seq.getObjectAt(count++).toASN1Primitive()).octets

        if (seq.size() > count) {
            this.issuerSerial = IssuerSerial.getInstance(seq.getObjectAt(count))
        }
    }

    constructor(
            certHash: ByteArray) : this(null, certHash, null) {
    }

    constructor(
            certHash: ByteArray,
            issuerSerial: IssuerSerial) : this(null, certHash, issuerSerial) {
    }

    @JvmOverloads constructor(
            algId: AlgorithmIdentifier?,
            certHash: ByteArray,
            issuerSerial: IssuerSerial? = null) {
        if (algId == null) {
            // Default value
            this.hashAlgorithm = DEFAULT_ALG_ID
        } else {
            this.hashAlgorithm = algId
        }

        this.certHash = certHash
        this.issuerSerial = issuerSerial
    }

    /**
     *
     * ESSCertIDv2 ::=  SEQUENCE {
     * hashAlgorithm     AlgorithmIdentifier
     * DEFAULT {algorithm id-sha256},
     * certHash          Hash,
     * issuerSerial      IssuerSerial OPTIONAL
     * }

     * Hash ::= OCTET STRING

     * IssuerSerial ::= SEQUENCE {
     * issuer         GeneralNames,
     * serialNumber   CertificateSerialNumber
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (hashAlgorithm != DEFAULT_ALG_ID) {
            v.add(hashAlgorithm)
        }

        v.add(DEROctetString(certHash).toASN1Primitive())

        if (issuerSerial != null) {
            v.add(issuerSerial)
        }

        return DERSequence(v)
    }

    companion object {
        private val DEFAULT_ALG_ID = AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)

        fun getInstance(
                o: Any?): ESSCertIDv2? {
            if (o is ESSCertIDv2) {
                return o
            } else if (o != null) {
                return ESSCertIDv2(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }

}
