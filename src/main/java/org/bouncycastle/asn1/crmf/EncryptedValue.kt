package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class EncryptedValue : ASN1Object {
    var intendedAlg: AlgorithmIdentifier? = null
        private set
    var symmAlg: AlgorithmIdentifier? = null
        private set
    var encSymmKey: DERBitString? = null
        private set
    var keyAlg: AlgorithmIdentifier? = null
        private set
    var valueHint: ASN1OctetString? = null
        private set
    var encValue: DERBitString? = null
        private set

    private constructor(seq: ASN1Sequence) {
        var index = 0
        while (seq.getObjectAt(index) is ASN1TaggedObject) {
            val tObj = seq.getObjectAt(index) as ASN1TaggedObject

            when (tObj.tagNo) {
                0 -> intendedAlg = AlgorithmIdentifier.getInstance(tObj, false)
                1 -> symmAlg = AlgorithmIdentifier.getInstance(tObj, false)
                2 -> encSymmKey = DERBitString.getInstance(tObj, false)
                3 -> keyAlg = AlgorithmIdentifier.getInstance(tObj, false)
                4 -> valueHint = ASN1OctetString.getInstance(tObj, false)
            }
            index++
        }

        encValue = DERBitString.getInstance(seq.getObjectAt(index))
    }

    constructor(
            intendedAlg: AlgorithmIdentifier,
            symmAlg: AlgorithmIdentifier,
            encSymmKey: DERBitString,
            keyAlg: AlgorithmIdentifier,
            valueHint: ASN1OctetString,
            encValue: DERBitString?) {
        if (encValue == null) {
            throw IllegalArgumentException("'encValue' cannot be null")
        }

        this.intendedAlg = intendedAlg
        this.symmAlg = symmAlg
        this.encSymmKey = encSymmKey
        this.keyAlg = keyAlg
        this.valueHint = valueHint
        this.encValue = encValue
    }

    /**
     *
     * EncryptedValue ::= SEQUENCE {
     * intendedAlg   [0] AlgorithmIdentifier  OPTIONAL,
     * -- the intended algorithm for which the value will be used
     * symmAlg       [1] AlgorithmIdentifier  OPTIONAL,
     * -- the symmetric algorithm used to encrypt the value
     * encSymmKey    [2] BIT STRING           OPTIONAL,
     * -- the (encrypted) symmetric key used to encrypt the value
     * keyAlg        [3] AlgorithmIdentifier  OPTIONAL,
     * -- algorithm used to encrypt the symmetric key
     * valueHint     [4] OCTET STRING         OPTIONAL,
     * -- a brief description or identifier of the encValue content
     * -- (may be meaningful only to the sending entity, and used only
     * -- if EncryptedValue might be re-examined by the sending entity
     * -- in the future)
     * encValue       BIT STRING }
     * -- the encrypted value itself
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        addOptional(v, 0, intendedAlg)
        addOptional(v, 1, symmAlg)
        addOptional(v, 2, encSymmKey)
        addOptional(v, 3, keyAlg)
        addOptional(v, 4, valueHint)

        v.add(encValue)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(false, tagNo, obj))
        }
    }

    companion object {

        fun getInstance(o: Any?): EncryptedValue? {
            if (o is EncryptedValue) {
                return o
            } else if (o != null) {
                return EncryptedValue(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
