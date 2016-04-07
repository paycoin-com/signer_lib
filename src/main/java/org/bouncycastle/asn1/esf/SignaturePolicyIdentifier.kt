package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1Null
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERNull

class SignaturePolicyIdentifier : ASN1Object {
    val signaturePolicyId: SignaturePolicyId
    var isSignaturePolicyImplied: Boolean = false
        private set

    constructor() {
        this.isSignaturePolicyImplied = true
    }

    constructor(
            signaturePolicyId: SignaturePolicyId) {
        this.signaturePolicyId = signaturePolicyId
        this.isSignaturePolicyImplied = false
    }

    /**
     *
     * SignaturePolicyIdentifier ::= CHOICE{
     * SignaturePolicyId         SignaturePolicyId,
     * SignaturePolicyImplied    SignaturePolicyImplied }

     * SignaturePolicyImplied ::= NULL
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (isSignaturePolicyImplied) {
            return DERNull.INSTANCE
        } else {
            return signaturePolicyId.toASN1Primitive()
        }
    }

    companion object {

        fun getInstance(
                obj: Any?): SignaturePolicyIdentifier? {
            if (obj is SignaturePolicyIdentifier) {
                return obj
            } else if (obj is ASN1Null || ASN1Object.hasEncodedTagValue(obj, BERTags.NULL)) {
                return SignaturePolicyIdentifier()
            } else if (obj != null) {
                return SignaturePolicyIdentifier(SignaturePolicyId.getInstance(obj))
            }

            return null
        }
    }
}
