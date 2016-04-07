package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.DEROutputStream
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest


/**
 * X.690 test example
 */
class OIDTest : SimpleTest() {
    internal var req1 = Hex.decode("0603813403")
    internal var req2 = Hex.decode("06082A36FFFFFFDD6311")

    override fun getName(): String {
        return "OID"
    }

    @Throws(IOException::class)
    private fun recodeCheck(
            oid: String,
            enc: ByteArray) {
        val bIn = ByteArrayInputStream(enc)
        val aIn = ASN1InputStream(bIn)

        val o = ASN1ObjectIdentifier(oid)
        val encO = aIn.readObject() as ASN1ObjectIdentifier

        if (o != encO) {
            fail("oid ID didn't match", o, encO)
        }

        val bOut = ByteArrayOutputStream()
        val dOut = DEROutputStream(bOut)

        dOut.writeObject(o)

        val bytes = bOut.toByteArray()

        if (bytes.size != enc.size) {
            fail("failed length test")
        }

        for (i in enc.indices) {
            if (bytes[i] != enc[i]) {
                fail("failed comparison test", String(Hex.encode(enc)), String(Hex.encode(bytes)))
            }
        }
    }

    @Throws(IOException::class)
    private fun validOidCheck(
            oid: String) {
        var o = ASN1ObjectIdentifier(oid)
        val bOut = ByteArrayOutputStream()
        val aOut = ASN1OutputStream(bOut)

        aOut.writeObject(o)

        val bIn = ByteArrayInputStream(bOut.toByteArray())
        val aIn = ASN1InputStream(bIn)

        o = aIn.readObject() as ASN1ObjectIdentifier

        if (o.id != oid) {
            fail("failed oid check for " + oid)
        }
    }

    private fun invalidOidCheck(
            oid: String) {
        try {
            ASN1ObjectIdentifier(oid)
            fail("failed to catch bad oid: " + oid)
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    private fun branchCheck(stem: String, branch: String) {
        val expected = stem + "." + branch
        val actual = ASN1ObjectIdentifier(stem).branch(branch).id

        if (expected != actual) {
            fail("failed 'branch' check for $stem/$branch")
        }
    }

    private fun onCheck(stem: String, test: String, expected: Boolean) {
        if (expected != ASN1ObjectIdentifier(test).on(ASN1ObjectIdentifier(stem))) {
            fail("failed 'on' check for $stem/$test")
        }
    }

    @Throws(IOException::class)
    override fun performTest() {
        recodeCheck("2.100.3", req1)
        recodeCheck("1.2.54.34359733987.17", req2)

        validOidCheck(PKCSObjectIdentifiers.pkcs_9_at_contentType.id)
        validOidCheck("0.1")
        validOidCheck("1.1.127.32512.8323072.2130706432.545460846592.139637976727552.35747322042253312.9151314442816847872")
        validOidCheck("1.2.123.12345678901.1.1.1")
        validOidCheck("2.25.196556539987194312349856245628873852187.1")

        invalidOidCheck("0")
        invalidOidCheck("1")
        invalidOidCheck("2")
        invalidOidCheck("3.1")
        invalidOidCheck("..1")
        invalidOidCheck("192.168.1.1")
        invalidOidCheck(".123452")
        invalidOidCheck("1.")
        invalidOidCheck("1.345.23.34..234")
        invalidOidCheck("1.345.23.34.234.")
        invalidOidCheck(".12.345.77.234")
        invalidOidCheck(".12.345.77.234.")
        invalidOidCheck("1.2.3.4.A.5")
        invalidOidCheck("1,2")

        branchCheck("1.1", "2.2")

        onCheck("1.1", "1.1", false)
        onCheck("1.1", "1.2", false)
        onCheck("1.1", "1.2.1", false)
        onCheck("1.1", "2.1", false)
        onCheck("1.1", "1.11", false)
        onCheck("1.12", "1.1.2", false)
        onCheck("1.1", "1.1.1", true)
        onCheck("1.1", "1.1.2", true)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(OIDTest())
        }
    }
}
