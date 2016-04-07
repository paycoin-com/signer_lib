package org.bouncycastle.asn1.isismtt.x509

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence

/**
 * Monetary limit for transactions. The QcEuMonetaryLimit QC statement MUST be
 * used in new certificates in place of the extension/attribute MonetaryLimit
 * since January 1, 2004. For the sake of backward compatibility with
 * certificates already in use, components SHOULD support MonetaryLimit (as well
 * as QcEuLimitValue).
 *
 *
 * Indicates a monetary limit within which the certificate holder is authorized
 * to act. (This value DOES NOT express a limit on the liability of the
 * certification authority).
 *
 * MonetaryLimitSyntax ::= SEQUENCE
 * {
 * currency PrintableString (SIZE(3)),
 * amount INTEGER,
 * exponent INTEGER
 * }
 *
 *
 *
 * currency must be the ISO code.
 *
 *
 * value = amount�10*exponent
 */
class MonetaryLimit : ASN1Object {
    internal var currency: DERPrintableString
    internal var amount: ASN1Integer
    internal var exponent: ASN1Integer

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        val e = seq.objects
        currency = DERPrintableString.getInstance(e.nextElement())
        amount = ASN1Integer.getInstance(e.nextElement())
        exponent = ASN1Integer.getInstance(e.nextElement())
    }

    /**
     * Constructor from a given details.
     *
     *
     * value = amount�10^exponent

     * @param currency The currency. Must be the ISO code.
     * *
     * @param amount   The amount
     * *
     * @param exponent The exponent
     */
    constructor(currency: String, amount: Int, exponent: Int) {
        this.currency = DERPrintableString(currency, true)
        this.amount = ASN1Integer(amount.toLong())
        this.exponent = ASN1Integer(exponent.toLong())
    }

    fun getCurrency(): String {
        return currency.string
    }

    fun getAmount(): BigInteger {
        return amount.value
    }

    fun getExponent(): BigInteger {
        return exponent.value
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * MonetaryLimitSyntax ::= SEQUENCE
     * {
     * currency PrintableString (SIZE(3)),
     * amount INTEGER,
     * exponent INTEGER
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(currency)
        seq.add(amount)
        seq.add(exponent)

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(obj: Any?): MonetaryLimit {
            if (obj == null || obj is MonetaryLimit) {
                return obj as MonetaryLimit?
            }

            if (obj is ASN1Sequence) {
                return MonetaryLimit(ASN1Sequence.getInstance(obj))
            }

            throw IllegalArgumentException("unknown object in getInstance")
        }
    }

}
