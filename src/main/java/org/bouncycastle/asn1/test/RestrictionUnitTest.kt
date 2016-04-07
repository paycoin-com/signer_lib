package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.isismtt.x509.Restriction
import org.bouncycastle.asn1.x500.DirectoryString

class RestrictionUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "Restriction"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val res = DirectoryString("test")
        val restriction = Restriction(res.string)

        checkConstruction(restriction, res)

        try {
            Restriction.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            restriction: Restriction,
            res: DirectoryString) {
        var restriction = restriction
        checkValues(restriction, res)

        restriction = Restriction.getInstance(restriction)

        checkValues(restriction, res)

        val aIn = ASN1InputStream(restriction.toASN1Primitive().encoded)

        val str = aIn.readObject() as ASN1String

        restriction = Restriction.getInstance(str)

        checkValues(restriction, res)
    }

    private fun checkValues(
            restriction: Restriction,
            res: DirectoryString) {
        checkMandatoryField("restriction", res, restriction.restriction)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(RestrictionUnitTest())
        }
    }
}
