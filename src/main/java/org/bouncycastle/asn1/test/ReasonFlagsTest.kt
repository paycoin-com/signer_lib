package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.x509.ReasonFlags
import org.bouncycastle.util.test.SimpleTest

class ReasonFlagsTest : SimpleTest() {
    override fun getName(): String {
        return "ReasonFlags"
    }

    @Throws(IOException::class)
    override fun performTest() {
        BitStringConstantTester.testFlagValueCorrect(0, ReasonFlags.unused)
        BitStringConstantTester.testFlagValueCorrect(1, ReasonFlags.keyCompromise)
        BitStringConstantTester.testFlagValueCorrect(2, ReasonFlags.cACompromise)
        BitStringConstantTester.testFlagValueCorrect(3, ReasonFlags.affiliationChanged)
        BitStringConstantTester.testFlagValueCorrect(4, ReasonFlags.superseded)
        BitStringConstantTester.testFlagValueCorrect(5, ReasonFlags.cessationOfOperation)
        BitStringConstantTester.testFlagValueCorrect(6, ReasonFlags.certificateHold)
        BitStringConstantTester.testFlagValueCorrect(7, ReasonFlags.privilegeWithdrawn)
        BitStringConstantTester.testFlagValueCorrect(8, ReasonFlags.aACompromise)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(ReasonFlagsTest())
        }
    }
}
