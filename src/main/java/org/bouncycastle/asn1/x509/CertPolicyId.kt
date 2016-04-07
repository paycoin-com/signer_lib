package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive


/**
 * CertPolicyId, used in the CertificatePolicies and PolicyMappings
 * X509V3 Extensions.

 *
 * CertPolicyId ::= OBJECT IDENTIFIER
 *
 */
/**
 * CertPolicyId, used in the CertificatePolicies and PolicyMappings
 * X509V3 Extensions.

 *
 * CertPolicyId ::= OBJECT IDENTIFIER
 *
 */
class CertPolicyId private constructor(private val id: ASN1ObjectIdentifier) : ASN1Object() {

    fun getId(): String {
        return id.id
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return id
    }

    companion object {

        fun getInstance(o: Any?): CertPolicyId? {
            if (o is CertPolicyId) {
                return o
            } else if (o != null) {
                return CertPolicyId(ASN1ObjectIdentifier.getInstance(o))
            }

            return null
        }
    }
}
