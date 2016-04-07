package org.bouncycastle.asn1

internal object BERFactory {
    val EMPTY_SEQUENCE = BERSequence()
    val EMPTY_SET = BERSet()

    fun createSequence(v: ASN1EncodableVector): BERSequence {
        return if (v.size() < 1) EMPTY_SEQUENCE else BERSequence(v)
    }

    fun createSet(v: ASN1EncodableVector): BERSet {
        return if (v.size() < 1) EMPTY_SET else BERSet(v)
    }
}
