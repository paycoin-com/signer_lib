package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.icao.CscaMasterList
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.io.Streams
import org.bouncycastle.util.test.SimpleTest

class CscaMasterListTest : SimpleTest() {
    override fun getName(): String {
        return "CscaMasterList"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val input = getInput("masterlist-content.data")
        val parsedList = CscaMasterList.getInstance(ASN1Primitive.fromByteArray(input))

        if (parsedList.certStructs.size != 3) {
            fail("Cert structure parsing failed: incorrect length")
        }

        val output = parsedList.getEncoded()
        if (!Arrays.areEqual(input, output)) {
            fail("Encoding failed after parse")
        }
    }

    @Throws(IOException::class)
    private fun getInput(name: String): ByteArray {
        return Streams.readAll(javaClass.getResourceAsStream(name))
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(CscaMasterListTest())
        }
    }
}
