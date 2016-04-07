package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class CertificatePolicies : ASN1Object {
    private val policyInformation: Array<PolicyInformation>

    /**
     * Construct a CertificatePolicies object containing one PolicyInformation.

     * @param name the name to be contained.
     */
    constructor(
            name: PolicyInformation) {
        this.policyInformation = arrayOf(name)
    }

    constructor(
            policyInformation: Array<PolicyInformation>) {
        this.policyInformation = policyInformation
    }

    private constructor(
            seq: ASN1Sequence) {
        this.policyInformation = arrayOfNulls<PolicyInformation>(seq.size())

        for (i in 0..seq.size() - 1) {
            policyInformation[i] = PolicyInformation.getInstance(seq.getObjectAt(i))
        }
    }

    fun getPolicyInformation(): Array<PolicyInformation> {
        val tmp = arrayOfNulls<PolicyInformation>(policyInformation.size)

        System.arraycopy(policyInformation, 0, tmp, 0, policyInformation.size)

        return tmp
    }

    fun getPolicyInformation(policyIdentifier: ASN1ObjectIdentifier): PolicyInformation? {
        for (i in policyInformation.indices) {
            if (policyIdentifier == policyInformation[i].policyIdentifier) {
                return policyInformation[i]
            }
        }

        return null
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * CertificatePolicies ::= SEQUENCE SIZE {1..MAX} OF PolicyInformation
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(policyInformation)
    }

    override fun toString(): String {
        val p = StringBuffer()
        for (i in policyInformation.indices) {
            if (p.length != 0) {
                p.append(", ")
            }
            p.append(policyInformation[i])
        }

        return "CertificatePolicies: [$p]"
    }

    companion object {

        fun getInstance(
                obj: Any?): CertificatePolicies? {
            if (obj is CertificatePolicies) {
                return obj
            }

            if (obj != null) {
                return CertificatePolicies(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): CertificatePolicies {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Retrieve a CertificatePolicies for a passed in Extensions object, if present.

         * @param extensions the extensions object to be examined.
         * *
         * @return  the CertificatePolicies, null if the extension is not present.
         */
        fun fromExtensions(extensions: Extensions): CertificatePolicies {
            return CertificatePolicies.getInstance(extensions.getExtensionParsedValue(Extension.certificatePolicies))
        }
    }
}
