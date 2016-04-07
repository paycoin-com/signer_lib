package org.bouncycastle.asn1.x509.qualified

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * The MonetaryValue object.
 *
 * MonetaryValue  ::=  SEQUENCE {
 * currency              Iso4217CurrencyCode,
 * amount               INTEGER,
 * exponent             INTEGER }
 * -- value = amount * 10^exponent
 *
 */
class MonetaryValue : ASN1Object {
    var currency: Iso4217CurrencyCode? = null
        private set
    private var amount: ASN1Integer? = null
    private var exponent: ASN1Integer? = null

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects
        // currency
        currency = Iso4217CurrencyCode.getInstance(e.nextElement())
        // hashAlgorithm
        amount = ASN1Integer.getInstance(e.nextElement())
        // exponent
        exponent = ASN1Integer.getInstance(e.nextElement())
    }

    constructor(
            currency: Iso4217CurrencyCode,
            amount: Int,
            exponent: Int) {
        this.currency = currency
        this.amount = ASN1Integer(amount.toLong())
        this.exponent = ASN1Integer(exponent.toLong())
    }

    fun getAmount(): BigInteger {
        return amount!!.value
    }

    fun getExponent(): BigInteger {
        return exponent!!.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(currency)
        seq.add(amount)
        seq.add(exponent)

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(
                obj: Any?): MonetaryValue? {
            if (obj is MonetaryValue) {
                return obj
            }

            if (obj != null) {
                return MonetaryValue(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
