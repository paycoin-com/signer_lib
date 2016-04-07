package org.bouncycastle.asn1.icao

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence

class LDSVersionInfo : ASN1Object {
    private var ldsVersion: DERPrintableString? = null
    private var unicodeVersion: DERPrintableString? = null

    constructor(ldsVersion: String, unicodeVersion: String) {
        this.ldsVersion = DERPrintableString(ldsVersion)
        this.unicodeVersion = DERPrintableString(unicodeVersion)
    }

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("sequence wrong size for LDSVersionInfo")
        }

        this.ldsVersion = DERPrintableString.getInstance(seq.getObjectAt(0))
        this.unicodeVersion = DERPrintableString.getInstance(seq.getObjectAt(1))
    }

    fun getLdsVersion(): String {
        return ldsVersion!!.string
    }

    fun getUnicodeVersion(): String {
        return unicodeVersion!!.string
    }

    /**
     *
     * LDSVersionInfo ::= SEQUENCE {
     * ldsVersion PRINTABLE STRING
     * unicodeVersion PRINTABLE STRING
     * }
     *
     * @return a DERSequence representing the value in this object.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ldsVersion)
        v.add(unicodeVersion)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): LDSVersionInfo? {
            if (obj is LDSVersionInfo) {
                return obj
            } else if (obj != null) {
                return LDSVersionInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
