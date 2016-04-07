package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.misc.CAST5CBCParameters
import org.bouncycastle.asn1.misc.IDEACBCPar
import org.bouncycastle.asn1.misc.NetscapeCertType
import org.bouncycastle.asn1.misc.NetscapeRevocationURL
import org.bouncycastle.asn1.misc.VerisignCzagExtension
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.test.SimpleTest

class MiscTest : SimpleTest() {
    private fun isSameAs(
            a: ByteArray,
            b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }

        for (i in a.indices) {
            if (a[i] != b[i]) {
                return false
            }
        }

        return true
    }

    @Throws(Exception::class)
    fun shouldFailOnExtraData() {
        // basic construction
        val s1 = DERBitString(ByteArray(0), 0)

        ASN1Primitive.fromByteArray(s1.encoded)

        ASN1Primitive.fromByteArray(BERSequence(s1).encoded)

        try {
            val obj = ASN1Primitive.fromByteArray(Arrays.concatenate(s1.encoded, ByteArray(1)))
            fail("no exception")
        } catch (e: IOException) {
            if ("Extra data detected in stream" != e.message) {
                fail("wrong exception")
            }
        }

    }

    @Throws(Exception::class)
    override fun performTest() {
        val testIv = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        val values = arrayOf<ASN1Encodable>(CAST5CBCParameters(testIv, 128), NetscapeCertType(NetscapeCertType.smime), VerisignCzagExtension(DERIA5String("hello")), IDEACBCPar(testIv), NetscapeRevocationURL(DERIA5String("http://test")))

        val data = Base64.decode("MA4ECAECAwQFBgcIAgIAgAMCBSAWBWhlbGxvMAoECAECAwQFBgcIFgtodHRwOi8vdGVzdA==")

        val bOut = ByteArrayOutputStream()
        val aOut = ASN1OutputStream(bOut)

        for (i in values.indices) {
            aOut.writeObject(values[i])
        }

        val readValues = arrayOfNulls<ASN1Primitive>(values.size)

        if (!isSameAs(bOut.toByteArray(), data)) {
            fail("Failed data check")
        }

        val bIn = ByteArrayInputStream(bOut.toByteArray())
        val aIn = ASN1InputStream(bIn)

        for (i in values.indices) {
            val o = aIn.readObject()
            if (values[i] != o) {
                fail("Failed equality test for " + o)
            }

            if (o.hashCode() != values[i].hashCode()) {
                fail("Failed hashCode test for " + o)
            }
        }

        shouldFailOnExtraData()
    }

    override fun getName(): String {
        return "Misc"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(MiscTest())
        }
    }
}
