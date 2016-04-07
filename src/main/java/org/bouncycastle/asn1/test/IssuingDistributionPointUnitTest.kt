package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.DistributionPointName
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.IssuingDistributionPoint
import org.bouncycastle.asn1.x509.ReasonFlags
import org.bouncycastle.util.test.SimpleTest

class IssuingDistributionPointUnitTest : SimpleTest() {
    override fun getName(): String {
        return "IssuingDistributionPoint"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val name = DistributionPointName(
                GeneralNames(GeneralName(X500Name("cn=test"))))
        val reasonFlags = ReasonFlags(ReasonFlags.cACompromise)

        checkPoint(6, name, true, true, reasonFlags, true, true)

        checkPoint(2, name, false, false, reasonFlags, false, false)

        checkPoint(0, null, false, false, null, false, false)

        try {
            IssuingDistributionPoint.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkPoint(
            size: Int,
            distributionPoint: DistributionPointName?,
            onlyContainsUserCerts: Boolean,
            onlyContainsCACerts: Boolean,
            onlySomeReasons: ReasonFlags?,
            indirectCRL: Boolean,
            onlyContainsAttributeCerts: Boolean) {
        var point = IssuingDistributionPoint(distributionPoint, onlyContainsUserCerts, onlyContainsCACerts, onlySomeReasons, indirectCRL, onlyContainsAttributeCerts)

        checkValues(point, distributionPoint, onlyContainsUserCerts, onlyContainsCACerts, onlySomeReasons, indirectCRL, onlyContainsAttributeCerts)

        val seq = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(point.encoded))

        if (seq.size() != size) {
            fail("size mismatch")
        }

        point = IssuingDistributionPoint.getInstance(seq)

        checkValues(point, distributionPoint, onlyContainsUserCerts, onlyContainsCACerts, onlySomeReasons, indirectCRL, onlyContainsAttributeCerts)
    }

    private fun checkValues(point: IssuingDistributionPoint, distributionPoint: DistributionPointName, onlyContainsUserCerts: Boolean, onlyContainsCACerts: Boolean, onlySomeReasons: ReasonFlags, indirectCRL: Boolean, onlyContainsAttributeCerts: Boolean) {
        if (point.onlyContainsUserCerts() != onlyContainsUserCerts) {
            fail("mismatch on onlyContainsUserCerts")
        }

        if (point.onlyContainsCACerts() != onlyContainsCACerts) {
            fail("mismatch on onlyContainsCACerts")
        }

        if (point.isIndirectCRL != indirectCRL) {
            fail("mismatch on indirectCRL")
        }

        if (point.onlyContainsAttributeCerts() != onlyContainsAttributeCerts) {
            fail("mismatch on onlyContainsAttributeCerts")
        }

        if (!isEquiv(onlySomeReasons, point.onlySomeReasons)) {
            fail("mismatch on onlySomeReasons")
        }

        if (!isEquiv(distributionPoint, point.distributionPoint)) {
            fail("mismatch on distributionPoint")
        }
    }

    private fun isEquiv(o1: Any?, o2: Any?): Boolean {
        if (o1 == null) {
            return o2 == null
        }

        return o1 == o2
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(IssuingDistributionPointUnitTest())
        }
    }
}