package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode
import org.bouncycastle.asn1.x509.qualified.MonetaryValue
import org.bouncycastle.util.test.SimpleTest

class MonetaryValueUnitTest : SimpleTest() {

    override fun getName(): String {
        return "MonetaryValue"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var mv: MonetaryValue? = MonetaryValue(Iso4217CurrencyCode(CURRENCY_CODE), TEST_AMOUNT, ZERO_EXPONENT)

        checkValues(mv, TEST_AMOUNT, ZERO_EXPONENT)

        mv = MonetaryValue.getInstance(mv)

        checkValues(mv, TEST_AMOUNT, ZERO_EXPONENT)

        val aIn = ASN1InputStream(mv!!.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        mv = MonetaryValue.getInstance(seq)

        checkValues(mv, TEST_AMOUNT, ZERO_EXPONENT)

        mv = MonetaryValue.getInstance(null)

        if (mv != null) {
            fail("null getInstance() failed.")
        }

        try {
            MonetaryValue.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    private fun checkValues(
            mv: MonetaryValue,
            amount: Int,
            exponent: Int) {
        if (mv.amount.toInt() != amount) {
            fail("amounts don't match.")
        }

        if (mv.exponent.toInt() != exponent) {
            fail("exponents don't match.")
        }

        val cc = mv.currency

        if (cc.alphabetic != CURRENCY_CODE) {
            fail("currency code wrong")
        }
    }

    companion object {
        private val TEST_AMOUNT = 100
        private val ZERO_EXPONENT = 0

        private val CURRENCY_CODE = "AUD"

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(MonetaryValueUnitTest())
        }
    }
}
