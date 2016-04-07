package org.bouncycastle.asn1.cryptopro

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class GOST3410PublicKeyAlgParameters : ASN1Object {
    var publicKeyParamSet: ASN1ObjectIdentifier? = null
        private set
    var digestParamSet: ASN1ObjectIdentifier? = null
        private set
    var encryptionParamSet: ASN1ObjectIdentifier? = null
        private set

    constructor(
            publicKeyParamSet: ASN1ObjectIdentifier,
            digestParamSet: ASN1ObjectIdentifier) {
        this.publicKeyParamSet = publicKeyParamSet
        this.digestParamSet = digestParamSet
        this.encryptionParamSet = null
    }

    constructor(
            publicKeyParamSet: ASN1ObjectIdentifier,
            digestParamSet: ASN1ObjectIdentifier,
            encryptionParamSet: ASN1ObjectIdentifier) {
        this.publicKeyParamSet = publicKeyParamSet
        this.digestParamSet = digestParamSet
        this.encryptionParamSet = encryptionParamSet
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        this.publicKeyParamSet = seq.getObjectAt(0) as ASN1ObjectIdentifier
        this.digestParamSet = seq.getObjectAt(1) as ASN1ObjectIdentifier

        if (seq.size() > 2) {
            this.encryptionParamSet = seq.getObjectAt(2) as ASN1ObjectIdentifier
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(publicKeyParamSet)
        v.add(digestParamSet)

        if (encryptionParamSet != null) {
            v.add(encryptionParamSet)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): GOST3410PublicKeyAlgParameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): GOST3410PublicKeyAlgParameters? {
            if (obj is GOST3410PublicKeyAlgParameters) {
                return obj
            }

            if (obj != null) {
                return GOST3410PublicKeyAlgParameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
