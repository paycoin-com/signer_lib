package org.bouncycastle.asn1.isismtt.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * ISIS-MTT PROFILE: The responder may include this extension in a response to
 * send the hash of the requested certificate to the responder. This hash is
 * cryptographically bound to the certificate and serves as evidence that the
 * certificate is known to the responder (i.e. it has been issued and is present
 * in the directory). Hence, this extension is a means to provide a positive
 * statement of availability as described in T8.[8]. As explained in T13.[1],
 * clients may rely on this information to be able to validate signatures after
 * the expiry of the corresponding certificate. Hence, clients MUST support this
 * extension. If a positive statement of availability is to be delivered, this
 * extension syntax and OID MUST be used.
 *
 * CertHash ::= SEQUENCE {
 * hashAlgorithm AlgorithmIdentifier,
 * certificateHash OCTET STRING
 * }
 *
 */
class CertHash : ASN1Object {

    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var certificateHash: ByteArray? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type CertHash:
     *
     * CertHash ::= SEQUENCE {
     * hashAlgorithm AlgorithmIdentifier,
     * certificateHash OCTET STRING
     * }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        certificateHash = DEROctetString.getInstance(seq.getObjectAt(1)).octets
    }

    /**
     * Constructor from a given details.

     * @param hashAlgorithm   The hash algorithm identifier.
     * *
     * @param certificateHash The hash of the whole DER encoding of the certificate.
     */
    constructor(hashAlgorithm: AlgorithmIdentifier, certificateHash: ByteArray) {
        this.hashAlgorithm = hashAlgorithm
        this.certificateHash = ByteArray(certificateHash.size)
        System.arraycopy(certificateHash, 0, this.certificateHash, 0,
                certificateHash.size)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * CertHash ::= SEQUENCE {
     * hashAlgorithm AlgorithmIdentifier,
     * certificateHash OCTET STRING
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        vec.add(hashAlgorithm)
        vec.add(DEROctetString(certificateHash))
        return DERSequence(vec)
    }

    companion object {

        fun getInstance(obj: Any?): CertHash {
            if (obj == null || obj is CertHash) {
                return obj as CertHash?
            }

            if (obj is ASN1Sequence) {
                return CertHash(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
