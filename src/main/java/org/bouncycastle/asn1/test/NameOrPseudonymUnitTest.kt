package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x509.sigi.NameOrPseudonym

class NameOrPseudonymUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "NameOrPseudonym"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val pseudonym = "pseudonym"
        val surname = DirectoryString("surname")
        val givenName = DERSequence(DirectoryString("givenName"))

        var id: NameOrPseudonym? = NameOrPseudonym(pseudonym)

        checkConstruction(id, pseudonym, null, null)

        id = NameOrPseudonym(surname, givenName)

        checkConstruction(id, null, surname, givenName)

        id = NameOrPseudonym.getInstance(null)

        if (id != null) {
            fail("null getInstance() failed.")
        }

        try {
            NameOrPseudonym.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            id: NameOrPseudonym,
            pseudonym: String?,
            surname: DirectoryString?,
            givenName: ASN1Sequence?) {
        var id = id
        checkValues(id, pseudonym, surname, givenName)

        id = NameOrPseudonym.getInstance(id)

        checkValues(id, pseudonym, surname, givenName)

        val aIn = ASN1InputStream(id.toASN1Primitive().encoded)

        if (surname != null) {
            val seq = aIn.readObject() as ASN1Sequence

            id = NameOrPseudonym.getInstance(seq)
        } else {
            val s = aIn.readObject() as ASN1String

            id = NameOrPseudonym.getInstance(s)
        }

        checkValues(id, pseudonym, surname, givenName)
    }

    private fun checkValues(
            id: NameOrPseudonym,
            pseudonym: String,
            surname: DirectoryString?,
            givenName: ASN1Sequence) {

        if (surname != null) {
            checkMandatoryField("surname", surname, id.surname)
            checkMandatoryField("givenName", givenName, DERSequence(id.givenName[0]))
        } else {
            checkOptionalField("pseudonym", DirectoryString(pseudonym), id.pseudonym)
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(NameOrPseudonymUnitTest())
        }
    }
}
