package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax
import org.bouncycastle.asn1.isismtt.x509.Admissions
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.GeneralName

class AdmissionSyntaxUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "AdmissionSyntax"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val name = GeneralName(X500Name("CN=hello world"))
        val admissions = DERSequence(
                Admissions(name,
                        NamingAuthority(ASN1ObjectIdentifier("1.2.3"), "url", DirectoryString("fred")),
                        arrayOfNulls<ProfessionInfo>(0)))
        var syntax: AdmissionSyntax? = AdmissionSyntax(name, admissions)

        checkConstruction(syntax, name, admissions)

        syntax = AdmissionSyntax.getInstance(null)

        if (syntax != null) {
            fail("null getInstance() failed.")
        }

        try {
            AdmissionSyntax.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            syntax: AdmissionSyntax,
            authority: GeneralName,
            admissions: ASN1Sequence) {
        var syntax = syntax
        checkValues(syntax, authority, admissions)

        syntax = AdmissionSyntax.getInstance(syntax)

        checkValues(syntax, authority, admissions)

        val aIn = ASN1InputStream(syntax.toASN1Primitive().encoded)

        val info = aIn.readObject() as ASN1Sequence

        syntax = AdmissionSyntax.getInstance(info)

        checkValues(syntax, authority, admissions)
    }

    private fun checkValues(
            syntax: AdmissionSyntax,
            authority: GeneralName,
            admissions: ASN1Sequence) {
        checkMandatoryField("admissionAuthority", authority, syntax.admissionAuthority)

        val adm = syntax.getContentsOfAdmissions()

        if (adm.size != 1 || adm[0] != admissions.getObjectAt(0)) {
            fail("admissions check failed")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(AdmissionSyntaxUnitTest())
        }
    }
}
