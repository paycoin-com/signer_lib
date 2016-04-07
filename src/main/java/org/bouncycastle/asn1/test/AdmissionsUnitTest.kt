package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.isismtt.x509.Admissions
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.GeneralName

class AdmissionsUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "Admissions"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val name = GeneralName(X500Name("CN=hello world"))
        val auth = NamingAuthority(ASN1ObjectIdentifier("1.2.3"), "url", DirectoryString("fred"))
        var admissions: Admissions? = Admissions(name, auth, arrayOfNulls<ProfessionInfo>(0))

        checkConstruction(admissions, name, auth)

        admissions = Admissions.getInstance(null)

        if (admissions != null) {
            fail("null getInstance() failed.")
        }

        try {
            Admissions.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            admissions: Admissions,
            name: GeneralName,
            auth: NamingAuthority) {
        var admissions = admissions
        checkValues(admissions, name, auth)

        admissions = Admissions.getInstance(admissions)

        checkValues(admissions, name, auth)

        val aIn = ASN1InputStream(admissions.toASN1Primitive().encoded)

        val info = aIn.readObject() as ASN1Sequence

        admissions = Admissions.getInstance(info)

        checkValues(admissions, name, auth)
    }

    private fun checkValues(
            admissions: Admissions,
            name: GeneralName,
            auth: NamingAuthority) {
        checkMandatoryField("admissionAuthority", name, admissions.admissionAuthority)
        checkMandatoryField("namingAuthority", auth, admissions.namingAuthority)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(AdmissionsUnitTest())
        }
    }
}
