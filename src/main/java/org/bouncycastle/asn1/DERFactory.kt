package org.bouncycastle.asn1

internal object DERFactory {
    val EMPTY_SEQUENCE: ASN1Sequence = DERSequence()
    val EMPTY_SET: ASN1Set = DERSet()

    fun createSequence(v: ASN1EncodableVector): ASN1Sequence {
        return if (v.size() < 1) EMPTY_SEQUENCE else DLSequence(v)
    }

    fun createSet(v: ASN1EncodableVector): ASN1Set {
        return if (v.size() < 1) EMPTY_SET else DLSet(v)
    }
}
