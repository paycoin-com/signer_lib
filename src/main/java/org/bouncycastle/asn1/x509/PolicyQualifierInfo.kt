package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence

/**
 * Policy qualifiers, used in the X509V3 CertificatePolicies
 * extension.

 *
 * PolicyQualifierInfo ::= SEQUENCE {
 * policyQualifierId  PolicyQualifierId,
 * qualifier          ANY DEFINED BY policyQualifierId }

 * PolicyQualifierId ::= OBJECT IDENTIFIER ( id-qt-cps | id-qt-unotice )
 *
 */
class PolicyQualifierInfo : ASN1Object {
    var policyQualifierId: ASN1ObjectIdentifier? = null
        private set
    var qualifier: ASN1Encodable? = null
        private set

    /**
     * Creates a new `PolicyQualifierInfo` instance.

     * @param policyQualifierId a `PolicyQualifierId` value
     * *
     * @param qualifier the qualifier, defined by the above field.
     */
    constructor(
            policyQualifierId: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable) {
        this.policyQualifierId = policyQualifierId
        this.qualifier = qualifier
    }

    /**
     * Creates a new `PolicyQualifierInfo` containing a
     * cPSuri qualifier.

     * @param cps the CPS (certification practice statement) uri as a
     * * `String`.
     */
    constructor(
            cps: String) {
        policyQualifierId = PolicyQualifierId.id_qt_cps
        qualifier = DERIA5String(cps)
    }

    /**
     * Creates a new `PolicyQualifierInfo` instance.

     * @param as `PolicyQualifierInfo` X509 structure
     * * encoded as an ASN1Sequence.
     * *
     */
    @Deprecated("use PolicyQualifierInfo.getInstance()")
    constructor(
            `as`: ASN1Sequence) {
        if (`as`.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + `as`.size())
        }

        policyQualifierId = ASN1ObjectIdentifier.getInstance(`as`.getObjectAt(0))
        qualifier = `as`.getObjectAt(1)
    }

    /**
     * Returns a DER-encodable representation of this instance.

     * @return a `ASN1Primitive` value
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val dev = ASN1EncodableVector()
        dev.add(policyQualifierId)
        dev.add(qualifier)

        return DERSequence(dev)
    }

    companion object {

        fun getInstance(
                obj: Any?): PolicyQualifierInfo? {
            if (obj is PolicyQualifierInfo) {
                return obj
            } else if (obj != null) {
                return PolicyQualifierInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
