package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ess.OtherCertID
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.IssuerSerial

class OtherCertIDUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "OtherCertID"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val algId = AlgorithmIdentifier(ASN1ObjectIdentifier("1.2.2.3"))
        val digest = ByteArray(20)
        val issuerSerial = IssuerSerial(GeneralNames(GeneralName(X500Name("CN=test"))), ASN1Integer(1))

        var certID: OtherCertID? = OtherCertID(algId, digest)

        checkConstruction(certID, algId, digest, null)

        certID = OtherCertID(algId, digest, issuerSerial)

        checkConstruction(certID, algId, digest, issuerSerial)

        certID = OtherCertID.getInstance(null)

        if (certID != null) {
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
            certID: OtherCertID,
            algId: AlgorithmIdentifier,
            digest: ByteArray,
            issuerSerial: IssuerSerial?) {
        var certID = certID
        checkValues(certID, algId, digest, issuerSerial)

        certID = OtherCertID.getInstance(certID)

        checkValues(certID, algId, digest, issuerSerial)

        val aIn = ASN1InputStream(certID.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        certID = OtherCertID.getInstance(seq)

        checkValues(certID, algId, digest, issuerSerial)
    }

    private fun checkValues(
            certID: OtherCertID,
            algId: AlgorithmIdentifier,
            digest: ByteArray,
            issuerSerial: IssuerSerial) {
        checkMandatoryField("algorithmHash", algId, certID.algorithmHash)
        checkMandatoryField("certHash", digest, certID.certHash)

        checkOptionalField("issuerSerial", issuerSerial, certID.issuerSerial)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(OtherCertIDUnitTest())
        }
    }
}
