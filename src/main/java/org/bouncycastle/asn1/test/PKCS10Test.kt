package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROutputStream
import org.bouncycastle.asn1.pkcs.CertificationRequest
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.test.SimpleTestResult
import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

class PKCS10Test : Test {
    internal var req1 = Base64.decode(
            "MIHoMIGTAgEAMC4xDjAMBgNVBAMTBVRlc3QyMQ8wDQYDVQQKEwZBbmFUb20xCzAJBgNVBAYTAlNF"
                    + "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALlEt31Tzt2MlcOljvacJgzQVhmlMoqAOgqJ9Pgd3Gux"
                    + "Z7/WcIlgW4QCB7WZT21O1YoghwBhPDMcNGrHei9kHQkCAwEAAaAAMA0GCSqGSIb3DQEBBQUAA0EA"
                    + "NDEI4ecNtJ3uHwGGlitNFq9WxcoZ0djbQJ5hABMotav6gtqlrwKXY2evaIrsNwkJtNdwwH18aQDU"
                    + "KCjOuBL38Q==")

    internal var req2 = Base64.decode(
            "MIIB6TCCAVICAQAwgagxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRQwEgYDVQQH"
                    + "EwtTYW50YSBDbGFyYTEMMAoGA1UEChMDQUJCMVEwTwYDVQQLHEhQAAAAAAAAAG8AAAAAAAAAdwAA"
                    + "AAAAAABlAAAAAAAAAHIAAAAAAAAAIAAAAAAAAABUAAAAAAAAABxIAAAAAAAARAAAAAAAAAAxDTAL"
                    + "BgNVBAMTBGJsdWUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANETRZ+6occCOrFxNhfKIp4C"
                    + "mMkxwhBNb7TnnahpbM9O0r4hrBPcfYuL7u9YX/jN0YNUP+/CiT39HhSe/bikaBPDEyNsl988I8vX"
                    + "piEdgxYq/+LTgGHbjRsRYCkPtmzwBbuBldNF8bV7pu0v4UScSsExmGqqDlX1TbPU8KkPU1iTAgMB"
                    + "AAGgADANBgkqhkiG9w0BAQQFAAOBgQAFbrs9qUwh93CtETk7DeUD5HcdCnxauo1bck44snSV6MZV"
                    + "OCIGaYu1501kmhEvAtVVRr6SEHwimfQDDIjnrWwYsEr/DT6tkTZAbfRd3qUu3iKjT0H0vlUZp0hJ"
                    + "66mINtBM84uZFBfoXiWY8M3FuAnGmvy6ah/dYtJorTxLKiGkew==")

    override fun getName(): String {
        return "PKCS10"
    }

    fun pkcs10Test(
            testName: String,
            req: ByteArray): TestResult {
        try {
            val bIn = ByteArrayInputStream(req)
            val aIn = ASN1InputStream(bIn)

            val r = CertificationRequest(aIn.readObject() as ASN1Sequence)

            val bOut = ByteArrayOutputStream()
            val dOut = DEROutputStream(bOut)

            dOut.writeObject(r.toASN1Primitive())

            val bytes = bOut.toByteArray()

            if (bytes.size != req.size) {
                return SimpleTestResult(false, "$name: $testName failed length test")
            }

            for (i in req.indices) {
                if (bytes[i] != req[i]) {
                    return SimpleTestResult(false, "$name: $testName failed comparison test")
                }
            }
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": Exception - " + testName + " " + e.toString())
        }

        return SimpleTestResult(true, name + ": Okay")
    }

    override fun perform(): TestResult {
        val res = pkcs10Test("basic CR", req1)

        if (!res.isSuccessful) {
            return res
        }

        return pkcs10Test("Universal CR", req2)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = PKCS10Test()

            val result = test.perform()

            println(result)
        }
    }
}
