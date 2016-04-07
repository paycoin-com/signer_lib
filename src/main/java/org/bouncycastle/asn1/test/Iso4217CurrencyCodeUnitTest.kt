package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode
import org.bouncycastle.util.test.SimpleTest

class Iso4217CurrencyCodeUnitTest : SimpleTest() {

    override fun getName(): String {
        return "Iso4217CurrencyCode"
    }

    @Throws(Exception::class)
    override fun performTest() {
        //
        // alphabetic
        //
        var cc: Iso4217CurrencyCode? = Iso4217CurrencyCode(ALPHABETIC_CURRENCY_CODE)

        checkNumeric(cc, ALPHABETIC_CURRENCY_CODE)

        cc = Iso4217CurrencyCode.getInstance(cc)

        checkNumeric(cc, ALPHABETIC_CURRENCY_CODE)

        var obj = cc!!.toASN1Primitive()

        cc = Iso4217CurrencyCode.getInstance(obj)

        checkNumeric(cc, ALPHABETIC_CURRENCY_CODE)

        //
        // numeric
        //
        cc = Iso4217CurrencyCode(NUMERIC_CURRENCY_CODE)

        checkNumeric(cc, NUMERIC_CURRENCY_CODE)

        cc = Iso4217CurrencyCode.getInstance(cc)

        checkNumeric(cc, NUMERIC_CURRENCY_CODE)

        obj = cc!!.toASN1Primitive()

        cc = Iso4217CurrencyCode.getInstance(obj)

        checkNumeric(cc, NUMERIC_CURRENCY_CODE)

        cc = Iso4217CurrencyCode.getInstance(null)

        if (cc != null) {
            fail("null getInstance() failed.")
        }

        try {
            Iso4217CurrencyCode.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            Iso4217CurrencyCode("ABCD")

            fail("constructor failed to detect out of range currencycode.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            Iso4217CurrencyCode(0)

            fail("constructor failed to detect out of range small numeric code.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            Iso4217CurrencyCode(1000)

            fail("constructor failed to detect out of range large numeric code.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    private fun checkNumeric(
            cc: Iso4217CurrencyCode,
            code: String) {
        if (!cc.isAlphabetic) {
            fail("non-alphabetic code found when one expected.")
        }

        if (cc.alphabetic != code) {
            fail("string codes don't match.")
        }
    }

    private fun checkNumeric(
            cc: Iso4217CurrencyCode,
            code: Int) {
        if (cc.isAlphabetic) {
            fail("alphabetic code found when one not expected.")
        }

        if (cc.numeric != code) {
            fail("numeric codes don't match.")
        }
    }

    companion object {
        private val ALPHABETIC_CURRENCY_CODE = "AUD"
        private val NUMERIC_CURRENCY_CODE = 1

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(Iso4217CurrencyCodeUnitTest())
        }
    }
}
