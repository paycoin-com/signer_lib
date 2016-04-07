package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.cmp.CMPObjectIdentifiers
import org.bouncycastle.asn1.cmp.PBMParameter
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * Password-based MAC value for use with POPOSigningKeyInput.
 */
class PKMACValue : ASN1Object {
    var algId: AlgorithmIdentifier? = null
        private set
    var value: DERBitString? = null
        private set

    private constructor(seq: ASN1Sequence) {
        algId = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        value = DERBitString.getInstance(seq.getObjectAt(1))
    }

    /**
     * Creates a new PKMACValue.
     * @param params parameters for password-based MAC
     * *
     * @param value MAC of the DER-encoded SubjectPublicKeyInfo
     */
    constructor(
            params: PBMParameter,
            value: DERBitString) : this(AlgorithmIdentifier(
            CMPObjectIdentifiers.passwordBasedMac, params), value) {
    }

    /**
     * Creates a new PKMACValue.
     * @param aid CMPObjectIdentifiers.passwordBasedMAC, with PBMParameter
     * *
     * @param value MAC of the DER-encoded SubjectPublicKeyInfo
     */
    constructor(
            aid: AlgorithmIdentifier,
            value: DERBitString) {
        this.algId = aid
        this.value = value
    }

    /**
     *
     * PKMACValue ::= SEQUENCE {
     * algId  AlgorithmIdentifier,
     * -- algorithm value shall be PasswordBasedMac 1.2.840.113533.7.66.13
     * -- parameter value is PBMParameter
     * value  BIT STRING }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algId)
        v.add(value)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): PKMACValue? {
            if (o is PKMACValue) {
                return o
            }

            if (o != null) {
                return PKMACValue(ASN1Sequence.getInstance(o))
            }

            return null
        }

        fun getInstance(obj: ASN1TaggedObject, isExplicit: Boolean): PKMACValue {
            return getInstance(ASN1Sequence.getInstance(obj, isExplicit))
        }
    }
}
