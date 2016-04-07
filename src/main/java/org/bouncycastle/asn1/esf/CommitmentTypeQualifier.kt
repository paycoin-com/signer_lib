package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * Commitment type qualifiers, used in the Commitment-Type-Indication attribute (RFC3126).

 *
 * CommitmentTypeQualifier ::= SEQUENCE {
 * commitmentTypeIdentifier  CommitmentTypeIdentifier,
 * qualifier          ANY DEFINED BY commitmentTypeIdentifier OPTIONAL }
 *
 */
class CommitmentTypeQualifier : ASN1Object {
    var commitmentTypeIdentifier: ASN1ObjectIdentifier? = null
        private set
    var qualifier: ASN1Encodable? = null
        private set

    /**
     * Creates a new `CommitmentTypeQualifier` instance.

     * @param commitmentTypeIdentifier a `CommitmentTypeIdentifier` value
     * *
     * @param qualifier the qualifier, defined by the above field.
     */
    @JvmOverloads constructor(
            commitmentTypeIdentifier: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable? = null) {
        this.commitmentTypeIdentifier = commitmentTypeIdentifier
        this.qualifier = qualifier
    }

    /**
     * Creates a new `CommitmentTypeQualifier` instance.

     * @param as `CommitmentTypeQualifier` structure
     * * encoded as an ASN1Sequence.
     */
    private constructor(
            `as`: ASN1Sequence) {
        commitmentTypeIdentifier = `as`.getObjectAt(0) as ASN1ObjectIdentifier

        if (`as`.size() > 1) {
            qualifier = `as`.getObjectAt(1)
        }
    }

    /**
     * Returns a DER-encodable representation of this instance.

     * @return a `ASN1Primitive` value
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val dev = ASN1EncodableVector()
        dev.add(commitmentTypeIdentifier)
        if (qualifier != null) {
            dev.add(qualifier)
        }

        return DERSequence(dev)
    }

    companion object {

        fun getInstance(`as`: Any?): CommitmentTypeQualifier? {
            if (`as` is CommitmentTypeQualifier) {
                return `as`
            } else if (`as` != null) {
                return CommitmentTypeQualifier(ASN1Sequence.getInstance(`as`))
            }

            return null
        }
    }
}
/**
 * Creates a new `CommitmentTypeQualifier` instance.

 * @param commitmentTypeIdentifier a `CommitmentTypeIdentifier` value
 */
