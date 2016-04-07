package org.bouncycastle.asn1.test

import java.io.IOException
import java.util.Random

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.icao.DataGroupHash
import org.bouncycastle.asn1.icao.LDSSecurityObject
import org.bouncycastle.asn1.icao.LDSVersionInfo
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.util.test.SimpleTest

class LDSSecurityObjectUnitTest : SimpleTest() {
    override fun getName(): String {
        return "LDSSecurityObject"
    }

    private fun generateHash(): ByteArray {
        val rand = Random()
        val bytes = ByteArray(20)

        for (i in bytes.indices) {
            bytes[i] = rand.nextInt().toByte()
        }

        return bytes
    }

    @Throws(Exception::class)
    override fun performTest() {
        val algoId = AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)
        val datas = arrayOfNulls<DataGroupHash>(2)

        datas[0] = DataGroupHash(1, DEROctetString(generateHash()))
        datas[1] = DataGroupHash(2, DEROctetString(generateHash()))

        var so = LDSSecurityObject(algoId, datas)

        checkConstruction(so, algoId, datas)

        val versionInfo = LDSVersionInfo("Hello", "world")

        so = LDSSecurityObject(algoId, datas, versionInfo)

        checkConstruction(so, algoId, datas, versionInfo)

        try {
            LDSSecurityObject.getInstance(null)
        } catch (e: Exception) {
            fail("getInstance() failed to handle null.")
        }

        try {
            LDSSecurityObject.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            val v = ASN1EncodableVector()

            LDSSecurityObject.getInstance(DERSequence(v))

            fail("constructor failed to detect empty sequence.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            LDSSecurityObject(algoId, arrayOfNulls<DataGroupHash>(1))

            fail("constructor failed to detect small DataGroupHash array.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            LDSSecurityObject(algoId, arrayOfNulls<DataGroupHash>(LDSSecurityObject.ub_DataGroups + 1))

            fail("constructor failed to out of bounds DataGroupHash array.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            so: LDSSecurityObject,
            digestAlgorithmIdentifier: AlgorithmIdentifier,
            datagroupHash: Array<DataGroupHash>) {
        var so = so
        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, null)

        so = LDSSecurityObject.getInstance(so)

        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, null)

        val aIn = ASN1InputStream(so.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        so = LDSSecurityObject.getInstance(seq)

        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, null)
    }

    @Throws(IOException::class)
    private fun checkConstruction(
            so: LDSSecurityObject,
            digestAlgorithmIdentifier: AlgorithmIdentifier,
            datagroupHash: Array<DataGroupHash>,
            versionInfo: LDSVersionInfo) {
        var so = so
        if (so.getVersion() != 1) {
            fail("version number not 1")
        }

        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, versionInfo)

        so = LDSSecurityObject.getInstance(so)

        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, versionInfo)

        val aIn = ASN1InputStream(so.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        so = LDSSecurityObject.getInstance(seq)

        checkStatement(so, digestAlgorithmIdentifier, datagroupHash, versionInfo)
    }

    private fun checkStatement(
            so: LDSSecurityObject,
            digestAlgorithmIdentifier: AlgorithmIdentifier?,
            datagroupHash: Array<DataGroupHash>?,
            versionInfo: LDSVersionInfo?) {
        if (digestAlgorithmIdentifier != null) {
            if (!so.digestAlgorithmIdentifier!!.equals(digestAlgorithmIdentifier)) {
                fail("ids don't match.")
            }
        } else if (so.digestAlgorithmIdentifier != null) {
            fail("digest algorithm Id found when none expected.")
        }

        if (datagroupHash != null) {
            val datas = so.datagroupHash

            for (i in datas.indices) {
                if (datagroupHash[i] != datas[i]) {
                    fail("name registration authorities don't match.")
                }
            }
        } else if (so.datagroupHash != null) {
            fail("data hash groups found when none expected.")
        }

        if (versionInfo != null) {
            if (versionInfo != so.versionInfo) {
                fail("versionInfo doesn't match")
            }
        } else if (so.versionInfo != null) {
            fail("version info found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(LDSSecurityObjectUnitTest())
        }
    }
}
