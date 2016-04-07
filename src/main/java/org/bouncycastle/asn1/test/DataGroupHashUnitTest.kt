package org.bouncycastle.asn1.test

import java.io.IOException
import java.util.Random

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.icao.DataGroupHash
import org.bouncycastle.util.test.SimpleTest

class DataGroupHashUnitTest : SimpleTest() {
    override fun getName(): String {
        return "DataGroupHash"
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
        val dataGroupNumber = 1
        val dataHash = DEROctetString(generateHash())
        val dg = DataGroupHash(dataGroupNumber, dataHash)

        checkConstruction(dg, dataGroupNumber, dataHash)

        try {
            DataGroupHash.getInstance(null)
        } catch (e: Exception) {
            fail("getInstance() failed to handle null.")
        }

        try {
            DataGroupHash.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            dg: DataGroupHash,
            dataGroupNumber: Int,
            dataGroupHashValue: ASN1OctetString) {
        var dg = dg
        checkValues(dg, dataGroupNumber, dataGroupHashValue)

        dg = DataGroupHash.getInstance(dg)

        checkValues(dg, dataGroupNumber, dataGroupHashValue)

        val aIn = ASN1InputStream(dg.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        dg = DataGroupHash.getInstance(seq)

        checkValues(dg, dataGroupNumber, dataGroupHashValue)
    }

    private fun checkValues(
            dg: DataGroupHash,
            dataGroupNumber: Int,
            dataGroupHashValue: ASN1OctetString) {
        if (dg.getDataGroupNumber() != dataGroupNumber) {
            fail("group number don't match.")
        }

        if (!dg.dataGroupHashValue.equals(dataGroupHashValue)) {
            fail("hash value don't match.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(DataGroupHashUnitTest())
        }
    }
}
