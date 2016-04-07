package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * This class helps to support crossCerfificatePairs in a LDAP directory
 * according RFC 2587

 *
 * crossCertificatePairATTRIBUTE::={
 * WITH SYNTAX   CertificatePair
 * EQUALITY MATCHING RULE certificatePairExactMatch
 * ID joint-iso-ccitt(2) ds(5) attributeType(4) crossCertificatePair(40)}
 *

 *  The forward elements of the crossCertificatePair attribute of a
 * CA's directory entry shall be used to store all, except self-issued
 * certificates issued to this CA. Optionally, the reverse elements of the
 * crossCertificatePair attribute, of a CA's directory entry may contain a
 * subset of certificates issued by this CA to other CAs. When both the forward
 * and the reverse elements are present in a single attribute value, issuer name
 * in one certificate shall match the subject name in the other and vice versa,
 * and the subject public key in one certificate shall be capable of verifying
 * the digital signature on the other certificate and vice versa.

 * When a reverse element is present, the forward element value and the reverse
 * element value need not be stored in the same attribute value; in other words,
 * they can be stored in either a single attribute value or two attribute
 * values.

 *
 * CertificatePair ::= SEQUENCE {
 * forward        [0]    Certificate OPTIONAL,
 * reverse        [1]    Certificate OPTIONAL,
 * -- at least one of the pair shall be present -- }
 *
 */
class CertificatePair : ASN1Object {
    /**
     * @return Returns the forward.
     */
    var forward: Certificate? = null
        private set

    /**
     * @return Returns the reverse.
     */
    var reverse: Certificate? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type CertificatePair:
     *
     * CertificatePair ::= SEQUENCE {
     * forward        [0]    Certificate OPTIONAL,
     * reverse        [1]    Certificate OPTIONAL,
     * -- at least one of the pair shall be present -- }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 1 && seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = ASN1TaggedObject.getInstance(e.nextElement())
            if (o.tagNo == 0) {
                forward = Certificate.getInstance(o, true)
            } else if (o.tagNo == 1) {
                reverse = Certificate.getInstance(o, true)
            } else {
                throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
        }
    }

    /**
     * Constructor from a given details.

     * @param forward Certificates issued to this CA.
     * *
     * @param reverse Certificates issued by this CA to other CAs.
     */
    constructor(forward: Certificate, reverse: Certificate) {
        this.forward = forward
        this.reverse = reverse
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * CertificatePair ::= SEQUENCE {
     * forward        [0]    Certificate OPTIONAL,
     * reverse        [1]    Certificate OPTIONAL,
     * -- at least one of the pair shall be present -- }
     *

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()

        if (forward != null) {
            vec.add(DERTaggedObject(0, forward))
        }
        if (reverse != null) {
            vec.add(DERTaggedObject(1, reverse))
        }

        return DERSequence(vec)
    }

    companion object {

        fun getInstance(obj: Any?): CertificatePair {
            if (obj == null || obj is CertificatePair) {
                return obj as CertificatePair?
            }

            if (obj is ASN1Sequence) {
                return CertificatePair(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
