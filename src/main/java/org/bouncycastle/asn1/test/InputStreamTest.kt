package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.util.test.SimpleTest

class InputStreamTest : SimpleTest() {


    override fun getName(): String {
        return "InputStream"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var aIn = ASN1InputStream(outOfBoundsLength)

        try {
            aIn.readObject()
            fail("out of bounds length not detected.")
        } catch (e: IOException) {
            if (!e.message.startsWith("DER length more than 4 bytes")) {
                fail("wrong exception: " + e.message)
            }
        }

        aIn = ASN1InputStream(negativeLength)

        try {
            aIn.readObject()
            fail("negative length not detected.")
        } catch (e: IOException) {
            if (e.message != "corrupted stream - negative length found") {
                fail("wrong exception: " + e.message)
            }
        }

        aIn = ASN1InputStream(outsideLimitLength)

        try {
            aIn.readObject()
            fail("outside limit length not detected.")
        } catch (e: IOException) {
            if (e.message != "corrupted stream - out of bounds length found") {
                fail("wrong exception: " + e.message)
            }
        }

    }

    companion object {
        private val outOfBoundsLength = byteArrayOf(0x30.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte())
        private val negativeLength = byteArrayOf(0x30.toByte(), 0x84.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte())
        private val outsideLimitLength = byteArrayOf(0x30.toByte(), 0x83.toByte(), 0x0f.toByte(), 0xff.toByte(), 0xff.toByte())

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(InputStreamTest())
        }
    }
}
