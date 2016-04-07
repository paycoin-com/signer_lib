package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.X509Extensions
import org.bouncycastle.asn1.x509.X509ExtensionsGenerator
import org.bouncycastle.util.test.SimpleTest

class X509ExtensionsTest : SimpleTest() {

    override fun getName(): String {
        return "X509Extensions"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val gen = X509ExtensionsGenerator()

        gen.addExtension(OID_1, true, ByteArray(20))
        gen.addExtension(OID_2, true, ByteArray(20))

        val ext1 = gen.generate()
        var ext2 = gen.generate()

        if (ext1 != ext2) {
            fail("equals test failed")
        }

        gen.reset()

        gen.addExtension(OID_2, true, ByteArray(20))
        gen.addExtension(OID_1, true, ByteArray(20))

        ext2 = gen.generate()

        if (ext1 == ext2) {
            fail("inequality test failed")
        }

        if (!ext1.equivalent(ext2)) {
            fail("equivalence true failed")
        }

        gen.reset()

        gen.addExtension(OID_1, true, ByteArray(22))
        gen.addExtension(OID_2, true, ByteArray(20))

        ext2 = gen.generate()

        if (ext1 == ext2) {
            fail("inequality 1 failed")
        }

        if (ext1.equivalent(ext2)) {
            fail("non-equivalence 1 failed")
        }

        gen.reset()

        gen.addExtension(OID_3, true, ByteArray(20))
        gen.addExtension(OID_2, true, ByteArray(20))

        ext2 = gen.generate()

        if (ext1 == ext2) {
            fail("inequality 2 failed")
        }

        if (ext1.equivalent(ext2)) {
            fail("non-equivalence 2 failed")
        }

        try {
            gen.addExtension(OID_2, true, ByteArray(20))
            fail("repeated oid")
        } catch (e: IllegalArgumentException) {
            if (e.message != "extension 1.2.2 already added") {
                fail("wrong exception on repeated oid: " + e.message)
            }
        }

    }

    companion object {
        private val OID_2 = ASN1ObjectIdentifier("1.2.2")
        private val OID_3 = ASN1ObjectIdentifier("1.2.3")
        private val OID_1 = ASN1ObjectIdentifier("1.2.1")

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(X509ExtensionsTest())
        }
    }
}
