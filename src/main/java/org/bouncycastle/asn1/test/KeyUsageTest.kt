package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.util.test.SimpleTest

class KeyUsageTest : SimpleTest() {
    override fun getName(): String {
        return "KeyUsage"
    }

    @Throws(IOException::class)
    override fun performTest() {
        BitStringConstantTester.testFlagValueCorrect(0, KeyUsage.digitalSignature)
        BitStringConstantTester.testFlagValueCorrect(1, KeyUsage.nonRepudiation)
        BitStringConstantTester.testFlagValueCorrect(2, KeyUsage.keyEncipherment)
        BitStringConstantTester.testFlagValueCorrect(3, KeyUsage.dataEncipherment)
        BitStringConstantTester.testFlagValueCorrect(4, KeyUsage.keyAgreement)
        BitStringConstantTester.testFlagValueCorrect(5, KeyUsage.keyCertSign)
        BitStringConstantTester.testFlagValueCorrect(6, KeyUsage.cRLSign)
        BitStringConstantTester.testFlagValueCorrect(7, KeyUsage.encipherOnly)
        BitStringConstantTester.testFlagValueCorrect(8, KeyUsage.decipherOnly)

        if (!KeyUsage(KeyUsage.keyCertSign).hasUsages(KeyUsage.keyCertSign)) {
            fail("usages bit test failed 1")
        }

        if (KeyUsage(KeyUsage.cRLSign).hasUsages(KeyUsage.keyCertSign)) {
            fail("usages bit test failed 2")
        }

        if (!KeyUsage(KeyUsage.cRLSign or KeyUsage.decipherOnly).hasUsages(KeyUsage.cRLSign or KeyUsage.decipherOnly)) {
            fail("usages bit test failed 3")
        }

        if (KeyUsage(KeyUsage.cRLSign or KeyUsage.decipherOnly).hasUsages(KeyUsage.cRLSign or KeyUsage.decipherOnly or KeyUsage.keyCertSign)) {
            fail("usages bit test failed 4")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(KeyUsageTest())
        }
    }
}
