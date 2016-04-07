package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest
import org.bouncycastle.util.test.TestResult

class BitStringTest : SimpleTest() {
    @Throws(Exception::class)
    private fun testZeroLengthStrings() {
        // basic construction
        val s1 = DERBitString(ByteArray(0), 0)

        // check getBytes()
        s1.bytes

        // check encoding/decoding
        val derBit = ASN1Primitive.fromByteArray(s1.encoded) as DERBitString

        if (!Arrays.areEqual(s1.encoded, Hex.decode("030100"))) {
            fail("zero encoding wrong")
        }

        try {
            DERBitString(null, 1)
            fail("exception not thrown")
        } catch (e: NullPointerException) {
            if ("data cannot be null" != e.message) {
                fail("Unexpected exception")
            }
        }

        try {
            DERBitString(ByteArray(0), 1)
            fail("exception not thrown")
        } catch (e: IllegalArgumentException) {
            if ("zero length data with non-zero pad bits" != e.message) {
                fail("Unexpected exception")
            }
        }

        try {
            DERBitString(ByteArray(1), 8)
            fail("exception not thrown")
        } catch (e: IllegalArgumentException) {
            if ("pad bits cannot be greater than 7 or less than 0" != e.message) {
                fail("Unexpected exception")
            }
        }

        val s2 = DERBitString(0)
        if (!Arrays.areEqual(s1.encoded, s2.encoded)) {
            fail("zero encoding wrong")
        }
    }

    @Throws(Exception::class)
    private fun testRandomPadBits() {
        val test = Hex.decode("030206c0")

        val test1 = Hex.decode("030206f0")
        val test2 = Hex.decode("030206c1")
        val test3 = Hex.decode("030206c7")
        val test4 = Hex.decode("030206d1")

        encodingCheck(test, test1)
        encodingCheck(test, test2)
        encodingCheck(test, test3)
        encodingCheck(test, test4)
    }

    @Throws(IOException::class)
    private fun encodingCheck(derData: ByteArray, dlData: ByteArray) {
        if (Arrays.areEqual(derData, ASN1Primitive.fromByteArray(dlData).encoded)) {
            fail("failed DL check")
        }
        if (!Arrays.areEqual(derData, ASN1Primitive.fromByteArray(dlData).getEncoded(ASN1Encoding.DER))) {
            fail("failed DER check")
        }
    }

    @Throws(Exception::class)
    override fun performTest() {
        var k = KeyUsage(KeyUsage.digitalSignature)
        if (k.bytes[0] != KeyUsage.digitalSignature.toByte() || k.padBits != 7) {
            fail("failed digitalSignature")
        }

        k = KeyUsage(KeyUsage.nonRepudiation)
        if (k.bytes[0] != KeyUsage.nonRepudiation.toByte() || k.padBits != 6) {
            fail("failed nonRepudiation")
        }

        k = KeyUsage(KeyUsage.keyEncipherment)
        if (k.bytes[0] != KeyUsage.keyEncipherment.toByte() || k.padBits != 5) {
            fail("failed keyEncipherment")
        }

        k = KeyUsage(KeyUsage.cRLSign)
        if (k.bytes[0] != KeyUsage.cRLSign.toByte() || k.padBits != 1) {
            fail("failed cRLSign")
        }

        k = KeyUsage(KeyUsage.decipherOnly)
        if (k.bytes[1] != (KeyUsage.decipherOnly shr 8).toByte() || k.padBits != 7) {
            fail("failed decipherOnly")
        }

        // test for zero length bit string
        try {
            ASN1Primitive.fromByteArray(DERBitString(ByteArray(0), 0).encoded)
        } catch (e: IOException) {
            fail(e.toString())
        }

        testRandomPadBits()
        testZeroLengthStrings()
    }

    override fun getName(): String {
        return "BitString"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = BitStringTest()
            val result = test.perform()

            println(result)
        }
    }
}
