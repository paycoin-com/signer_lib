package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.Target
import org.bouncycastle.asn1.x509.TargetInformation
import org.bouncycastle.asn1.x509.Targets
import org.bouncycastle.util.test.SimpleTest

class TargetInformationTest : SimpleTest() {

    override fun getName(): String {
        return "TargetInformation"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val targets = arrayOfNulls<Target>(2)
        val targetName = Target(Target.targetName, GeneralName(GeneralName.dNSName, "www.test.com"))
        val targetGroup = Target(Target.targetGroup, GeneralName(GeneralName.directoryName, "o=Test, ou=Test"))
        targets[0] = targetName
        targets[1] = targetGroup
        val targetss = Targets(targets)
        val targetInformation1 = TargetInformation(targetss)
        // use an Target array
        val targetInformation2 = TargetInformation(targets)
        // targetInformation1 and targetInformation2 must have same
        // encoding.
        if (targetInformation1 != targetInformation2) {
            fail("targetInformation1 and targetInformation2 should have the same encoding.")
        }
        val targetInformation3 = TargetInformation.getInstance(targetInformation1)
        val targetInformation4 = TargetInformation.getInstance(targetInformation2)
        if (targetInformation3 != targetInformation4) {
            fail("targetInformation3 and targetInformation4 should have the same encoding.")
        }
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            SimpleTest.runTest(TargetInformationTest())
        }
    }
}

