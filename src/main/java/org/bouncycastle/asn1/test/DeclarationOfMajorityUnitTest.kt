package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.isismtt.x509.DeclarationOfMajority

class DeclarationOfMajorityUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "DeclarationOfMajority"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val dateOfBirth = ASN1GeneralizedTime("20070315173729Z")
        var decl: DeclarationOfMajority? = DeclarationOfMajority(dateOfBirth)

        checkConstruction(decl, DeclarationOfMajority.dateOfBirth, dateOfBirth, -1)

        decl = DeclarationOfMajority(6)

        checkConstruction(decl, DeclarationOfMajority.notYoungerThan, null, 6)

        decl = DeclarationOfMajority.getInstance(null)

        if (decl != null) {
            fail("null getInstance() failed.")
        }

        try {
            DeclarationOfMajority.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            decl: DeclarationOfMajority,
            type: Int,
            dateOfBirth: ASN1GeneralizedTime?,
            notYoungerThan: Int) {
        var decl = decl
        checkValues(decl, type, dateOfBirth, notYoungerThan)

        decl = DeclarationOfMajority.getInstance(decl)

        checkValues(decl, type, dateOfBirth, notYoungerThan)

        val aIn = ASN1InputStream(decl.toASN1Primitive().encoded)

        val info = aIn.readObject() as DERTaggedObject

        decl = DeclarationOfMajority.getInstance(info)

        checkValues(decl, type, dateOfBirth, notYoungerThan)
    }

    private fun checkValues(
            decl: DeclarationOfMajority,
            type: Int,
            dateOfBirth: ASN1GeneralizedTime,
            notYoungerThan: Int) {
        checkMandatoryField("type", type, decl.type)
        checkOptionalField("dateOfBirth", dateOfBirth, decl.getDateOfBirth())
        if (notYoungerThan != -1 && notYoungerThan != decl.notYoungerThan()) {
            fail("notYoungerThan mismatch")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(DeclarationOfMajorityUnitTest())
        }
    }
}
