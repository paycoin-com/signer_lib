package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.qualified.SemanticsInformation
import org.bouncycastle.util.test.SimpleTest

class SemanticsInformationUnitTest : SimpleTest() {
    override fun getName(): String {
        return "SemanticsInformation"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val statementId = ASN1ObjectIdentifier("1.1")
        var mv: SemanticsInformation? = SemanticsInformation(statementId)

        checkConstruction(mv, statementId, null)

        val names = arrayOfNulls<GeneralName>(2)

        names[0] = GeneralName(GeneralName.rfc822Name, "test@test.org")
        names[1] = GeneralName(X500Name("cn=test"))

        mv = SemanticsInformation(statementId, names)

        checkConstruction(mv, statementId, names)

        mv = SemanticsInformation(names)

        checkConstruction(mv, null, names)

        mv = SemanticsInformation.getInstance(null)

        if (mv != null) {
            fail("null getInstance() failed.")
        }

        try {
            SemanticsInformation.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            val v = ASN1EncodableVector()

            SemanticsInformation.getInstance(DERSequence(v))

            fail("constructor failed to detect empty sequence.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(Exception::class)
    private fun checkConstruction(
            mv: SemanticsInformation,
            semanticsIdentifier: ASN1ObjectIdentifier?,
            names: Array<GeneralName>?) {
        var mv = mv
        checkStatement(mv, semanticsIdentifier, names)

        mv = SemanticsInformation.getInstance(mv)

        checkStatement(mv, semanticsIdentifier, names)

        val aIn = ASN1InputStream(mv.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        mv = SemanticsInformation.getInstance(seq)

        checkStatement(mv, semanticsIdentifier, names)
    }

    private fun checkStatement(
            si: SemanticsInformation,
            id: ASN1ObjectIdentifier?,
            names: Array<GeneralName>?) {
        if (id != null) {
            if (si.semanticsIdentifier != id) {
                fail("ids don't match.")
            }
        } else if (si.semanticsIdentifier != null) {
            fail("statementId found when none expected.")
        }

        if (names != null) {
            val siNames = si.nameRegistrationAuthorities

            for (i in siNames.indices) {
                if (names[i] != siNames[i]) {
                    fail("name registration authorities don't match.")
                }
            }
        } else if (si.nameRegistrationAuthorities != null) {
            fail("name registration authorities found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(SemanticsInformationUnitTest())
        }
    }
}
