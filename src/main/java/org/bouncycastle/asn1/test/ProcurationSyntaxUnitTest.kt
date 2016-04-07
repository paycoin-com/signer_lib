package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.isismtt.x509.ProcurationSyntax
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.IssuerSerial

class ProcurationSyntaxUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "ProcurationSyntax"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val country = "AU"
        val typeOfSubstitution = DirectoryString("substitution")
        val thirdPerson = GeneralName(X500Name("CN=thirdPerson"))
        val certRef = IssuerSerial(GeneralNames(GeneralName(X500Name("CN=test"))), ASN1Integer(1))

        var procuration: ProcurationSyntax? = ProcurationSyntax(country, typeOfSubstitution, thirdPerson)

        checkConstruction(procuration, country, typeOfSubstitution, thirdPerson, null)

        procuration = ProcurationSyntax(country, typeOfSubstitution, certRef)

        checkConstruction(procuration, country, typeOfSubstitution, null, certRef)

        procuration = ProcurationSyntax(null, typeOfSubstitution, certRef)

        checkConstruction(procuration, null, typeOfSubstitution, null, certRef)

        procuration = ProcurationSyntax(country, null, certRef)

        checkConstruction(procuration, country, null, null, certRef)

        procuration = ProcurationSyntax.getInstance(null)

        if (procuration != null) {
            fail("null getInstance() failed.")
        }

        try {
            ProcurationSyntax.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            procuration: ProcurationSyntax,
            country: String?,
            typeOfSubstitution: DirectoryString?,
            thirdPerson: GeneralName?,
            certRef: IssuerSerial?) {
        var procuration = procuration
        checkValues(procuration, country, typeOfSubstitution, thirdPerson, certRef)

        procuration = ProcurationSyntax.getInstance(procuration)

        checkValues(procuration, country, typeOfSubstitution, thirdPerson, certRef)

        val aIn = ASN1InputStream(procuration.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        procuration = ProcurationSyntax.getInstance(seq)

        checkValues(procuration, country, typeOfSubstitution, thirdPerson, certRef)
    }

    private fun checkValues(
            procuration: ProcurationSyntax,
            country: String,
            typeOfSubstitution: DirectoryString,
            thirdPerson: GeneralName,
            certRef: IssuerSerial) {
        checkOptionalField("country", country, procuration.country)
        checkOptionalField("typeOfSubstitution", typeOfSubstitution, procuration.typeOfSubstitution)
        checkOptionalField("thirdPerson", thirdPerson, procuration.thirdPerson)
        checkOptionalField("certRef", certRef, procuration.certRef)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(ProcurationSyntaxUnitTest())
        }
    }
}
