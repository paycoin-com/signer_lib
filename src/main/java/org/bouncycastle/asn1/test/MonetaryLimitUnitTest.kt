package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.isismtt.x509.MonetaryLimit

class MonetaryLimitUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "MonetaryLimit"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val currency = "AUD"
        val amount = 1
        val exponent = 2

        var limit: MonetaryLimit? = MonetaryLimit(currency, amount, exponent)

        checkConstruction(limit, currency, amount, exponent)

        limit = MonetaryLimit.getInstance(null)

        if (limit != null) {
            fail("null getInstance() failed.")
        }

        try {
            MonetaryLimit.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            limit: MonetaryLimit,
            currency: String,
            amount: Int,
            exponent: Int) {
        var limit = limit
        checkValues(limit, currency, amount, exponent)

        limit = MonetaryLimit.getInstance(limit)

        checkValues(limit, currency, amount, exponent)

        val aIn = ASN1InputStream(limit.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        limit = MonetaryLimit.getInstance(seq)

        checkValues(limit, currency, amount, exponent)
    }

    private fun checkValues(
            limit: MonetaryLimit,
            currency: String,
            amount: Int,
            exponent: Int) {
        checkMandatoryField("currency", currency, limit.getCurrency())
        checkMandatoryField("amount", amount, limit.getAmount().intValue())
        checkMandatoryField("exponent", exponent, limit.getExponent().intValue())
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(MonetaryLimitUnitTest())
        }
    }
}
