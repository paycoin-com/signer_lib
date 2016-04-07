package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityInformationAccess

class ServiceLocator private constructor(sequence: ASN1Sequence) : ASN1Object() {
    val issuer: X500Name
    val locator: AuthorityInformationAccess?

    init {
        this.issuer = X500Name.getInstance(sequence.getObjectAt(0))
        if (sequence.size() == 2) {
            this.locator = AuthorityInformationAccess.getInstance(sequence.getObjectAt(1))
        } else {
            this.locator = null

        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ServiceLocator ::= SEQUENCE {
     * issuer    Name,
     * locator   AuthorityInfoAccessSyntax OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(issuer)

        if (locator != null) {
            v.add(locator)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): ServiceLocator? {
            if (obj is ServiceLocator) {
                return obj
            } else if (obj != null) {
                return ServiceLocator(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
