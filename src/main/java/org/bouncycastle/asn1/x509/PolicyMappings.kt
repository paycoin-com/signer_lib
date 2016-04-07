package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Hashtable

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * PolicyMappings V3 extension, described in RFC3280.
 *
 * PolicyMappings ::= SEQUENCE SIZE (1..MAX) OF SEQUENCE {
 * issuerDomainPolicy      CertPolicyId,
 * subjectDomainPolicy     CertPolicyId }
 *

 * @see [RFC 3280, section 4.2.1.6](http://www.faqs.org/rfc/rfc3280.txt)
 */
class PolicyMappings : ASN1Object {
    internal var seq: ASN1Sequence? = null

    /**
     * Creates a new `PolicyMappings` instance.

     * @param seq an `ASN1Sequence` constructed as specified
     * *            in RFC 3280
     */
    private constructor(seq: ASN1Sequence) {
        this.seq = seq
    }

    /**
     * Creates a new `PolicyMappings` instance.

     * @param mappings a `HashMap` value that maps
     * *                 `String` oids
     * *                 to other `String` oids.
     * *
     */
    @Deprecated("use CertPolicyId constructors.")
    constructor(mappings: Hashtable<Any, Any>) {
        val dev = ASN1EncodableVector()
        val it = mappings.keys()

        while (it.hasMoreElements()) {
            val idp = it.nextElement() as String
            val sdp = mappings[idp] as String
            val dv = ASN1EncodableVector()
            dv.add(ASN1ObjectIdentifier(idp))
            dv.add(ASN1ObjectIdentifier(sdp))
            dev.add(DERSequence(dv))
        }

        seq = DERSequence(dev)
    }

    constructor(issuerDomainPolicy: CertPolicyId, subjectDomainPolicy: CertPolicyId) {
        val dv = ASN1EncodableVector()
        dv.add(issuerDomainPolicy)
        dv.add(subjectDomainPolicy)

        seq = DERSequence(DERSequence(dv))
    }

    constructor(issuerDomainPolicy: Array<CertPolicyId>, subjectDomainPolicy: Array<CertPolicyId>) {
        val dev = ASN1EncodableVector()

        for (i in issuerDomainPolicy.indices) {
            val dv = ASN1EncodableVector()
            dv.add(issuerDomainPolicy[i])
            dv.add(subjectDomainPolicy[i])
            dev.add(DERSequence(dv))
        }

        seq = DERSequence(dev)
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        fun getInstance(obj: Any?): PolicyMappings? {
            if (obj is PolicyMappings) {
                return obj
            }
            if (obj != null) {
                return PolicyMappings(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
