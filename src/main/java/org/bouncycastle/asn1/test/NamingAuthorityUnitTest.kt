package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority
import org.bouncycastle.asn1.x500.DirectoryString

class NamingAuthorityUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "NamingAuthority"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val namingAuthorityID = ASN1ObjectIdentifier("1.2.3")
        val namingAuthorityURL = "url"
        val namingAuthorityText = DirectoryString("text")

        var auth: NamingAuthority? = NamingAuthority(namingAuthorityID, namingAuthorityURL, namingAuthorityText)

        checkConstruction(auth, namingAuthorityID, namingAuthorityURL, namingAuthorityText)

        auth = NamingAuthority(null, namingAuthorityURL, namingAuthorityText)

        checkConstruction(auth, null, namingAuthorityURL, namingAuthorityText)

        auth = NamingAuthority(namingAuthorityID, null, namingAuthorityText)

        checkConstruction(auth, namingAuthorityID, null, namingAuthorityText)

        auth = NamingAuthority(namingAuthorityID, namingAuthorityURL, null)

        checkConstruction(auth, namingAuthorityID, namingAuthorityURL, null)

        auth = NamingAuthority.getInstance(null)

        if (auth != null) {
            fail("null getInstance() failed.")
        }

        try {
            NamingAuthority.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            auth: NamingAuthority,
            namingAuthorityID: ASN1ObjectIdentifier?,
            namingAuthorityURL: String?,
            namingAuthorityText: DirectoryString?) {
        var auth = auth
        checkValues(auth, namingAuthorityID, namingAuthorityURL, namingAuthorityText)

        auth = NamingAuthority.getInstance(auth)

        checkValues(auth, namingAuthorityID, namingAuthorityURL, namingAuthorityText)

        val aIn = ASN1InputStream(auth.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        auth = NamingAuthority.getInstance(seq)

        checkValues(auth, namingAuthorityID, namingAuthorityURL, namingAuthorityText)
    }

    private fun checkValues(
            auth: NamingAuthority,
            namingAuthorityId: ASN1ObjectIdentifier,
            namingAuthorityURL: String,
            namingAuthorityText: DirectoryString) {
        checkOptionalField("namingAuthorityId", namingAuthorityId, auth.namingAuthorityId)
        checkOptionalField("namingAuthorityURL", namingAuthorityURL, auth.namingAuthorityUrl)
        checkOptionalField("namingAuthorityText", namingAuthorityText, auth.namingAuthorityText)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(NamingAuthorityUnitTest())
        }
    }
}
