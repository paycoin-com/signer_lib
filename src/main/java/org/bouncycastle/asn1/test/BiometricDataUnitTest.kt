package org.bouncycastle.asn1.test

import java.security.SecureRandom

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.qualified.BiometricData
import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData
import org.bouncycastle.util.test.SimpleTest

class BiometricDataUnitTest : SimpleTest() {
    override fun getName(): String {
        return "BiometricData"
    }

    private fun generateHash(): ByteArray {
        val rand = SecureRandom()
        val bytes = ByteArray(20)

        rand.nextBytes(bytes)

        return bytes
    }

    @Throws(Exception::class)
    override fun performTest() {
        val dataType = TypeOfBiometricData(TypeOfBiometricData.HANDWRITTEN_SIGNATURE)
        val hashAlgorithm = AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE)
        val dataHash = DEROctetString(generateHash())
        var bd: BiometricData? = BiometricData(dataType, hashAlgorithm, dataHash)

        checkConstruction(bd, dataType, hashAlgorithm, dataHash, null)

        val dataUri = DERIA5String("http://test")

        bd = BiometricData(dataType, hashAlgorithm, dataHash, dataUri)

        checkConstruction(bd, dataType, hashAlgorithm, dataHash, dataUri)

        bd = BiometricData.getInstance(null)

        if (bd != null) {
            fail("null getInstance() failed.")
        }

        try {
            BiometricData.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(Exception::class)
    private fun checkConstruction(
            bd: BiometricData,
            dataType: TypeOfBiometricData,
            hashAlgorithm: AlgorithmIdentifier,
            dataHash: ASN1OctetString,
            dataUri: DERIA5String?) {
        var bd = bd
        checkValues(bd, dataType, hashAlgorithm, dataHash, dataUri)

        bd = BiometricData.getInstance(bd)

        checkValues(bd, dataType, hashAlgorithm, dataHash, dataUri)

        val aIn = ASN1InputStream(bd.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        bd = BiometricData.getInstance(seq)

        checkValues(bd, dataType, hashAlgorithm, dataHash, dataUri)
    }

    private fun checkValues(
            bd: BiometricData,
            dataType: TypeOfBiometricData,
            algID: AlgorithmIdentifier,
            dataHash: ASN1OctetString,
            sourceDataURI: DERIA5String?) {
        if (bd.typeOfBiometricData != dataType) {
            fail("types don't match.")
        }

        if (bd.hashAlgorithm != algID) {
            fail("hash algorithms don't match.")
        }

        if (bd.biometricDataHash != dataHash) {
            fail("hash algorithms don't match.")
        }

        if (sourceDataURI != null) {
            if (bd.sourceDataUri != sourceDataURI) {
                fail("data uris don't match.")
            }
        } else if (bd.sourceDataUri != null) {
            fail("data uri found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(BiometricDataUnitTest())
        }
    }
}
