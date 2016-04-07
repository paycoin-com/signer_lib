package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.isismtt.ocsp.CertHash
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class CertHashUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "CertHash"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val algId = AlgorithmIdentifier(ASN1ObjectIdentifier("1.2.2.3"))
        val digest = ByteArray(20)

        var certID: CertHash? = CertHash(algId, digest)

        checkConstruction(certID, algId, digest)

        certID = CertHash.getInstance(null)

        if (certID != null) {
            fail("null getInstance() failed.")
        }

        try {
            CertHash.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            certHash: CertHash,
            algId: AlgorithmIdentifier,
            digest: ByteArray) {
        var certHash = certHash
        checkValues(certHash, algId, digest)

        certHash = CertHash.getInstance(certHash)

        checkValues(certHash, algId, digest)

        val aIn = ASN1InputStream(certHash.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        certHash = CertHash.getInstance(seq)

        checkValues(certHash, algId, digest)
    }

    private fun checkValues(
            certHash: CertHash,
            algId: AlgorithmIdentifier,
            digest: ByteArray) {
        checkMandatoryField("algorithmHash", algId, certHash.hashAlgorithm)

        checkMandatoryField("certificateHash", digest, certHash.certificateHash)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(CertHashUnitTest())
        }
    }
}
