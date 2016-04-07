package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERGeneralizedTime
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.cms.RecipientKeyIdentifier
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute
import org.bouncycastle.asn1.smime.SMIMECapability
import org.bouncycastle.asn1.smime.SMIMECapabilityVector
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.test.SimpleTestResult
import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

class SMIMETest : Test {
    internal var attrBytes = Base64.decode("MDQGCSqGSIb3DQEJDzEnMCUwCgYIKoZIhvcNAwcwDgYIKoZIhvcNAwICAgCAMAcGBSsOAwIH")
    internal var prefBytes = Base64.decode("MCwGCyqGSIb3DQEJEAILMR2hGwQIAAAAAAAAAAAYDzIwMDcwMzE1MTczNzI5Wg==")

    private fun isSameAs(
            a: ByteArray,
            b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }

        for (i in a.indices) {
            if (a[i] != b[i]) {
                return false
            }
        }

        return true
    }

    override fun perform(): TestResult {
        val caps = SMIMECapabilityVector()

        caps.addCapability(SMIMECapability.dES_EDE3_CBC)
        caps.addCapability(SMIMECapability.rC2_CBC, 128)
        caps.addCapability(SMIMECapability.dES_CBC)

        val attr = SMIMECapabilitiesAttribute(caps)

        val pref = SMIMEEncryptionKeyPreferenceAttribute(
                RecipientKeyIdentifier(DEROctetString(ByteArray(8)), DERGeneralizedTime("20070315173729Z"), null))

        try {
            if (!isSameAs(attr.encoded, attrBytes)) {
                return SimpleTestResult(false, name + ": Failed attr data check")
            }

            var bIn = ByteArrayInputStream(attrBytes)
            var aIn = ASN1InputStream(bIn)

            var o = aIn.readObject()
            if (attr != o) {
                return SimpleTestResult(false, name + ": Failed equality test for attr")
            }

            if (!isSameAs(pref.getEncoded(), prefBytes)) {
                return SimpleTestResult(false, name + ": Failed attr data check")
            }

            bIn = ByteArrayInputStream(prefBytes)
            aIn = ASN1InputStream(bIn)

            o = aIn.readObject()
            if (pref != o) {
                return SimpleTestResult(false, name + ": Failed equality test for pref")
            }

            return SimpleTestResult(true, name + ": Okay")
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": Failed - exception " + e.toString(), e)
        }

    }

    override fun getName(): String {
        return "SMIME"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = SMIMETest()
            val result = test.perform()

            println(result)
        }
    }
}
