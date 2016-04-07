package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ess.OtherCertID
import org.bouncycastle.asn1.ess.OtherSigningCertificate
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class OtherSigningCertificateUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "OtherSigningCertificate"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val algId = AlgorithmIdentifier(ASN1ObjectIdentifier("1.2.2.3"))
        val digest = ByteArray(20)
        val otherCertID = OtherCertID(algId, digest)

        var otherCert: OtherSigningCertificate? = OtherSigningCertificate(otherCertID)

        checkConstruction(otherCert, otherCertID)

        otherCert = OtherSigningCertificate.getInstance(null)

        if (otherCert != null) {
            fail("null getInstance() failed.")
        }

        try {
            OtherCertID.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            otherCert: OtherSigningCertificate,
            otherCertID: OtherCertID) {
        var otherCert = otherCert
        checkValues(otherCert, otherCertID)

        otherCert = OtherSigningCertificate.getInstance(otherCert)

        checkValues(otherCert, otherCertID)

        val aIn = ASN1InputStream(otherCert.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        otherCert = OtherSigningCertificate.getInstance(seq)

        checkValues(otherCert, otherCertID)
    }

    private fun checkValues(
            otherCert: OtherSigningCertificate,
            otherCertID: OtherCertID) {
        if (otherCert.getCerts().size != 1) {
            fail("getCerts() length wrong")
        }
        checkMandatoryField("getCerts()[0]", otherCertID, otherCert.getCerts()[0])
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(OtherSigningCertificateUnitTest())
        }
    }
}
