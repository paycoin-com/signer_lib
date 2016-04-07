package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.ess.ContentHints

class ContentHintsUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "ContentHints"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val contentDescription = DERUTF8String("Description")
        val contentType = ASN1ObjectIdentifier("1.2.2.3")

        var hints: ContentHints? = ContentHints(contentType)

        checkConstruction(hints, contentType, null)

        hints = ContentHints(contentType, contentDescription)

        checkConstruction(hints, contentType, contentDescription)

        hints = ContentHints.getInstance(null)

        if (hints != null) {
            fail("null getInstance() failed.")
        }

        try {
            ContentHints.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            hints: ContentHints,
            contentType: ASN1ObjectIdentifier,
            description: DERUTF8String?) {
        var hints = hints
        checkValues(hints, contentType, description)

        hints = ContentHints.getInstance(hints)

        checkValues(hints, contentType, description)

        val aIn = ASN1InputStream(hints.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        hints = ContentHints.getInstance(seq)

        checkValues(hints, contentType, description)
    }

    private fun checkValues(
            hints: ContentHints,
            contentType: ASN1ObjectIdentifier,
            description: DERUTF8String) {
        checkMandatoryField("contentType", contentType, hints.contentType)
        checkOptionalField("description", description, hints.contentDescription)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(ContentHintsUnitTest())
        }
    }
}
