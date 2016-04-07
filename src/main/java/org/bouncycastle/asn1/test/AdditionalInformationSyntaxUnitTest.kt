package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.isismtt.x509.AdditionalInformationSyntax
import org.bouncycastle.asn1.x500.DirectoryString

class AdditionalInformationSyntaxUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "AdditionalInformationSyntax"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val syntax = AdditionalInformationSyntax("hello world")

        checkConstruction(syntax, DirectoryString("hello world"))

        try {
            AdditionalInformationSyntax.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            syntax: AdditionalInformationSyntax,
            information: DirectoryString) {
        var syntax = syntax
        checkValues(syntax, information)

        syntax = AdditionalInformationSyntax.getInstance(syntax)

        checkValues(syntax, information)

        val aIn = ASN1InputStream(syntax.toASN1Primitive().encoded)

        val info = aIn.readObject() as ASN1String

        syntax = AdditionalInformationSyntax.getInstance(info)

        checkValues(syntax, information)
    }

    private fun checkValues(
            syntax: AdditionalInformationSyntax,
            information: DirectoryString) {
        checkMandatoryField("information", information, syntax.information)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(AdditionalInformationSyntaxUnitTest())
        }
    }
}
