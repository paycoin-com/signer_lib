package org.bouncycastle.asn1.test

import java.io.IOException
import java.security.SecureRandom

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest


/**
 * X.690 test example
 */
class TagTest : SimpleTest() {
    internal var longTagged = Base64.decode(
            "ZSRzIp8gEEZFRENCQTk4NzY1NDMyMTCfIQwyMDA2MDQwMTEyMzSUCCAFERVz"
                    + "A4kCAHEXGBkalAggBRcYGRqUCCAFZS6QAkRFkQlURUNITklLRVKSBQECAwQF"
                    + "kxAREhMUFRYXGBkalAggBREVcwOJAgBxFxgZGpQIIAUXGBkalAggBWUukAJE"
                    + "RZEJVEVDSE5JS0VSkgUBAgMEBZMQERITFBUWFxgZGpQIIAURFXMDiQIAcRcY"
                    + "GRqUCCAFFxgZGpQIIAVlLpACREWRCVRFQ0hOSUtFUpIFAQIDBAWTEBESExQV"
                    + "FhcYGRqUCCAFERVzA4kCAHEXGBkalAggBRcYGRqUCCAFFxgZGpQIIAUXGBka"
                    + "lAg=")

    internal var longAppSpecificTag = Hex.decode("5F610101")

    override fun getName(): String {
        return "Tag"
    }

    @Throws(IOException::class)
    override fun performTest() {
        var aIn = ASN1InputStream(longTagged)

        var app = aIn.readObject() as DERApplicationSpecific

        aIn = ASN1InputStream(app.contents)

        app = aIn.readObject() as DERApplicationSpecific

        aIn = ASN1InputStream(app.contents)

        var tagged = aIn.readObject() as ASN1TaggedObject

        if (tagged.tagNo != 32) {
            fail("unexpected tag value found - not 32")
        }

        tagged = ASN1Primitive.fromByteArray(tagged.encoded) as ASN1TaggedObject

        if (tagged.tagNo != 32) {
            fail("unexpected tag value found on recode - not 32")
        }

        tagged = aIn.readObject() as ASN1TaggedObject

        if (tagged.tagNo != 33) {
            fail("unexpected tag value found - not 33")
        }

        tagged = ASN1Primitive.fromByteArray(tagged.encoded) as ASN1TaggedObject

        if (tagged.tagNo != 33) {
            fail("unexpected tag value found on recode - not 33")
        }

        aIn = ASN1InputStream(longAppSpecificTag)

        app = aIn.readObject() as DERApplicationSpecific

        if (app.applicationTag != 97) {
            fail("incorrect tag number read")
        }

        app = ASN1Primitive.fromByteArray(app.encoded) as DERApplicationSpecific

        if (app.applicationTag != 97) {
            fail("incorrect tag number read on recode")
        }

        val sr = SecureRandom()
        for (i in 0..99) {
            val testTag = sr.nextInt().ushr(1 + sr.nextInt().ushr(1) % 26)
            app = DERApplicationSpecific(testTag, byteArrayOf(1))
            app = ASN1Primitive.fromByteArray(app.encoded) as DERApplicationSpecific

            if (app.applicationTag != testTag) {
                fail("incorrect tag number read on recode (random test value: $testTag)")
            }
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(TagTest())
        }
    }
}
