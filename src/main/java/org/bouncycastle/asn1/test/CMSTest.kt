package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetStringParser
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1SetParser
import org.bouncycastle.asn1.ASN1StreamParser
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers
import org.bouncycastle.asn1.cms.CompressedData
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.cms.ContentInfoParser
import org.bouncycastle.asn1.cms.EncryptedContentInfoParser
import org.bouncycastle.asn1.cms.EnvelopedData
import org.bouncycastle.asn1.cms.EnvelopedDataParser
import org.bouncycastle.asn1.cms.KEKRecipientInfo
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo
import org.bouncycastle.asn1.cms.RecipientInfo
import org.bouncycastle.asn1.cms.SignedData
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.io.Streams
import org.bouncycastle.util.test.SimpleTestResult
import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

class CMSTest : Test {
    //
    // compressed data object
    //
    internal var compData = Base64.decode(
            "MIAGCyqGSIb3DQEJEAEJoIAwgAIBADANBgsqhkiG9w0BCRADCDCABgkqhkiG9w0BBwGggCSABIIC"
                    + "Hnic7ZRdb9owFIbvK/k/5PqVYPFXGK12YYyboVFASSp1vQtZGiLRACZE49/XHoUW7S/0tXP8Efux"
                    + "fU5ivWnasml72XFb3gb5druui7ytN803M570nii7C5r8tfwR281hy/p/KSM3+jzH5s3+pbQ90xSb"
                    + "P3VT3QbLusnt8WPIuN5vN/vaA2+DulnXTXkXvNTr8j8ouZmkCmGI/UW+ZS/C8zP0bz2dz0zwLt+1"
                    + "UEk2M8mlaxjRMByAhZTj0RGYg4TvogiRASROsZgjpVcJCb1KV6QzQeDJ1XkoQ5Jm+C5PbOHZZGRi"
                    + "v+ORAcshOGeCcdFJyfgFxdtCdEcmOrbinc/+BBMzRThEYpwl+jEBpciSGWQkI0TSlREmD/eOHb2D"
                    + "SGLuESm/iKUFt1y4XHBO2a5oq0IKJKWLS9kUZTA7vC5LSxYmgVL46SIWxIfWBQd6AdrnjLmH94UT"
                    + "vGxVibLqRCtIpp4g2qpdtqK1LiOeolpVK5wVQ5P7+QjZAlrh0cePYTx/gNZuB9Vhndtgujl9T/tg"
                    + "W9ogK+3rnmg3YWygnTuF5GDS+Q/jIVLnCcYZFc6Kk/+c80wKwZjwdZIqDYWRH68MuBQSXLgXYXj2"
                    + "3CAaYOBNJMliTl0X7eV5DnoKIFSKYdj3cRpD/cK/JWTHJRe76MUXnfBW8m7Hd5zhQ4ri2NrVF/WL"
                    + "+kV1/3AGSlJ32bFPd2BsQD8uSzIx6lObkjdz95c0AAAAAAAAAAAAAAAA")

    //
    // enveloped data
    //
    internal var envDataKeyTrns = Base64.decode(
            "MIAGCSqGSIb3DQEHA6CAMIACAQAxgcQwgcECAQAwKjAlMRYwFAYDVQQKEw1Cb3Vu"
                    + "Y3kgQ2FzdGxlMQswCQYDVQQGEwJBVQIBCjANBgkqhkiG9w0BAQEFAASBgC5vdGrB"
                    + "itQSGwifLf3KwPILjaB4WEXgT/IIO1KDzrsbItCJsMA0Smq2y0zptxT0pSRL6JRg"
                    + "NMxLk1ySnrIrvGiEPLMR1zjxlT8yQ6VLX+kEoK43ztd1aaLw0oBfrcXcLN7BEpZ1"
                    + "TIdjlBfXIOx1S88WY1MiYqJJFc3LMwRUaTEDMIAGCSqGSIb3DQEHATAdBglghkgB"
                    + "ZQMEARYEEAfxLMWeaBOTTZQwUq0Y5FuggAQgwOJhL04rjSZCBCSOv5i5XpFfGsOd"
                    + "YSHSqwntGpFqCx4AAAAAAAAAAAAA")

    internal var envDataKEK = Base64.decode(
            "MIAGCSqGSIb3DQEHA6CAMIACAQIxUqJQAgEEMAcEBQECAwQFMBAGCyqGSIb3DQEJE"
                    + "AMHAgE6BDC7G/HyUPilIrin2Yeajqmj795VoLWETRnZAAFcAiQdoQWyz+oCh6WY/H"
                    + "jHHi+0y+cwgAYJKoZIhvcNAQcBMBQGCCqGSIb3DQMHBAiY3eDBBbF6naCABBiNdzJb"
                    + "/v6+UZB3XXKipxFDUpz9GyjzB+gAAAAAAAAAAAAA")

    internal var envDataNestedNDEF = Base64.decode(
            "MIAGCSqGSIb3DQEHA6CAMIACAQAxge8wgewCAQAwgZUwgY8xKDAmBgNVBAoMH1RoZSBMZWdpb24g"
                    + "b2YgdGhlIEJvdW5jeSBDYXN0bGUxLzAtBgkqhkiG9w0BCQEWIGZlZWRiYWNrLWNyeXB0b0Bib3Vu"
                    + "Y3ljYXN0bGUub3JnMREwDwYDVQQIDAhWaWN0b3JpYTESMBAGA1UEBwwJTWVsYm91cm5lMQswCQYD"
                    + "VQQGEwJBVQIBATANBgkqhkiG9w0BAQEFAARABIXMd8xiTyWDKO/LQfvdGYTPW3I9oSQWwtm4OIaN"
                    + "VINpfY2lfwTvbmE6VXiLKeALC0dMBV8z7DEM9hE0HVmvLDCABgkqhkiG9w0BBwEwHQYJYIZIAWUD"
                    + "BAECBBB32ko6WrVxDTqwUYEpV6IUoIAEggKgS6RowrhNlmWWI13zxD/lryxkZ5oWXPUfNiUxYX/P"
                    + "r5iscW3s8VKJKUpJ4W5SNA7JGL4l/5LmSnJ4Qu/xzxcoH4r4vmt75EDE9p2Ob2Xi1NuSFAZubJFc"
                    + "Zlnp4e05UHKikmoaz0PbiAi277sLQlK2FcVsntTYVT00y8+IwuuQu0ATVqkXC+VhfjV/sK6vQZnw"
                    + "2rQKedZhLB7B4dUkmxCujb/UAq4lgSpLMXg2P6wMimTczXyQxRiZxPeI4ByCENjkafXbfcJft2eD"
                    + "gv1DEDdYM5WrW9Z75b4lmJiOJ/xxDniHCvum7KGXzpK1d1mqTlpzPC2xoz08/MO4lRf5Mb0bYdq6"
                    + "CjMaYqVwGsYryp/2ayX+d8H+JphEG+V9Eg8uPcDoibwhDI4KkoyGHstPw5bxcy7vVFt7LXUdNjJc"
                    + "K1wxaUKEXDGKt9Vj93FnBTLMX0Pc9HpueV5o1ipX34dn/P3HZB9XK8ScbrE38B1VnIgylStnhVFO"
                    + "Cj9s7qSVqI2L+xYHJRHsxaMumIRnmRuOqdXDfIo28EZAnFtQ/b9BziMGVvAW5+A8h8s2oazhSmK2"
                    + "23ftV7uv98ScgE8fCd3PwT1kKJM83ThTYyBzokvMfPYCCvsonMV+kTWXhWcwjYTS4ukrpR452ZdW"
                    + "l3aJqDnzobt5FK4T8OGciOj+1PxYFZyRmCuafm2Dx6o7Et2Tu/T5HYvhdY9jHyqtDl2PXH4CTnVi"
                    + "gA1YOAArjPVmsZVwAM3Ml46uyXXhcsXwQ1X0Tv4D+PSa/id4UQ2cObOw8Cj1eW2GB8iJIZVqkZaU"
                    + "XBexqgWYOIoxjqODSeoZKiBsTK3c+oOUBqBDueY1i55swE2o6dDt95FluX6iyr/q4w2wLt3upY1J"
                    + "YL+TuvZxAKviuAczMS1bAAAAAAAAAAAAAA==")

    //
    // signed data
    //
    internal var signedData = Base64.decode(
            "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAaCA"
                    + "JIAEDEhlbGxvIFdvcmxkIQAAAAAAAKCCBGIwggINMIIBdqADAgECAgEBMA0GCSqG"
                    + "SIb3DQEBBAUAMCUxFjAUBgNVBAoTDUJvdW5jeSBDYXN0bGUxCzAJBgNVBAYTAkFV"
                    + "MB4XDTA0MTAyNDA0MzA1OFoXDTA1MDIwMTA0MzA1OFowJTEWMBQGA1UEChMNQm91"
                    + "bmN5IENhc3RsZTELMAkGA1UEBhMCQVUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJ"
                    + "AoGBAJj3OAshAOgDmPcYZ1jdNSuhOHRH9VhC/PG17FdiInVGc2ulJhEifEQga/uq"
                    + "ZCpSd1nHsJUZKm9k1bVneWzC0941i9Znfxgb2jnXXsa5kwB2KEVESrOWsRjSRtnY"
                    + "iLgqBG0rzpaMn5A5ntu7N0406EesBhe19cjZAageEHGZDbufAgMBAAGjTTBLMB0G"
                    + "A1UdDgQWBBR/iHNKOo6f4ByWFFywRNZ65XSr1jAfBgNVHSMEGDAWgBR/iHNKOo6f"
                    + "4ByWFFywRNZ65XSr1jAJBgNVHRMEAjAAMA0GCSqGSIb3DQEBBAUAA4GBAFMJJ7QO"
                    + "pHo30bnlQ4Ny3PCnK+Se+Gw3TpaYGp84+a8fGD9Dme78G6NEsgvpFGTyoLxvJ4CB"
                    + "84Kzys+1p2HdXzoZiyXAer5S4IwptE3TxxFwKyj28cRrM6dK47DDyXUkV0qwBAMN"
                    + "luwnk/no4K7ilzN2MZk5l7wXyNa9yJ6CHW6dMIICTTCCAbagAwIBAgIBAjANBgkq"
                    + "hkiG9w0BAQQFADAlMRYwFAYDVQQKEw1Cb3VuY3kgQ2FzdGxlMQswCQYDVQQGEwJB"
                    + "VTAeFw0wNDEwMjQwNDMwNTlaFw0wNTAyMDEwNDMwNTlaMGUxGDAWBgNVBAMTD0Vy"
                    + "aWMgSC4gRWNoaWRuYTEkMCIGCSqGSIb3DQEJARYVZXJpY0Bib3VuY3ljYXN0bGUu"
                    + "b3JnMRYwFAYDVQQKEw1Cb3VuY3kgQ2FzdGxlMQswCQYDVQQGEwJBVTCBnzANBgkq"
                    + "hkiG9w0BAQEFAAOBjQAwgYkCgYEAm+5CnGU6W45iUpCsaGkn5gDruZv3j/o7N6ag"
                    + "mRZhikaLG2JF6ECaX13iioVJfmzBsPKxAACWwuTXCoSSXG8viK/qpSHwJpfQHYEh"
                    + "tcC0CxIqlnltv3KQAGwh/PdwpSPvSNnkQBGvtFq++9gnXDBbynfP8b2L2Eis0X9U"
                    + "2y6gFiMCAwEAAaNNMEswHQYDVR0OBBYEFEAmOksnF66FoQm6IQBVN66vJo1TMB8G"
                    + "A1UdIwQYMBaAFH+Ic0o6jp/gHJYUXLBE1nrldKvWMAkGA1UdEwQCMAAwDQYJKoZI"
                    + "hvcNAQEEBQADgYEAEeIjvNkKMPU/ZYCu1TqjGZPEqi+glntg2hC/CF0oGyHFpMuG"
                    + "tMepF3puW+uzKM1s61ar3ahidp3XFhr/GEU/XxK24AolI3yFgxP8PRgUWmQizTQX"
                    + "pWUmhlsBe1uIKVEfNAzCgtYfJQ8HJIKsUCcdWeCKVKs4jRionsek1rozkPExggEv"
                    + "MIIBKwIBATAqMCUxFjAUBgNVBAoTDUJvdW5jeSBDYXN0bGUxCzAJBgNVBAYTAkFV"
                    + "AgECMAkGBSsOAwIaBQCgXTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqG"
                    + "SIb3DQEJBTEPFw0wNDEwMjQwNDMwNTlaMCMGCSqGSIb3DQEJBDEWBBQu973mCM5U"
                    + "BOl9XwQvlfifHCMocTANBgkqhkiG9w0BAQEFAASBgGHbe3/jcZu6b/erRhc3PEji"
                    + "MUO8mEIRiNYBr5/vFNhkry8TrGfOpI45m7gu1MS0/vdas7ykvidl/sNZfO0GphEI"
                    + "UaIjMRT3U6yuTWF4aLpatJbbRsIepJO/B2kdIAbV5SCbZgVDJIPOR2qnruHN2wLF"
                    + "a+fEv4J8wQ8Xwvk0C8iMAAAAAAAA")

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

    private fun compressionTest(): TestResult {
        try {
            val aIn = ASN1InputStream(ByteArrayInputStream(compData))

            var info = ContentInfo.getInstance(aIn.readObject())
            var data = CompressedData.getInstance(info.content)

            data = CompressedData(data.compressionAlgorithmIdentifier, data.encapContentInfo)
            info = ContentInfo(CMSObjectIdentifiers.compressedData, data)

            val bOut = ByteArrayOutputStream()
            val aOut = ASN1OutputStream(bOut)

            aOut.writeObject(info)

            if (!isSameAs(bOut.toByteArray(), compData)) {
                return SimpleTestResult(false, name + ": CMS compression failed to re-encode")
            }

            return SimpleTestResult(true, name + ": Okay")
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": CMS compression failed - " + e.toString(), e)
        }

    }

    private fun envelopedTest(): TestResult {
        try {
            //
            // Key trans
            //
            var aIn = ASN1InputStream(ByteArrayInputStream(envDataKeyTrns))

            var info = ContentInfo.getInstance(aIn.readObject())
            var envData = EnvelopedData.getInstance(info.content)
            var s: ASN1Set = envData.recipientInfos

            if (s.size() != 1) {
                return SimpleTestResult(false, name + ": CMS KeyTrans enveloped, wrong number of recipients")
            }

            var recip = RecipientInfo.getInstance(s.getObjectAt(0))

            if (recip.getInfo() is KeyTransRecipientInfo) {
                var inf = KeyTransRecipientInfo.getInstance(recip.getInfo())

                inf = KeyTransRecipientInfo(inf.recipientIdentifier, inf.keyEncryptionAlgorithm, inf.encryptedKey)

                s = DERSet(RecipientInfo(inf))
            } else {
                return SimpleTestResult(false, name + ": CMS KeyTrans enveloped, wrong recipient type")
            }

            var bOut = ByteArrayOutputStream()
            var aOut = ASN1OutputStream(bOut)

            envData = EnvelopedData(envData.originatorInfo, s, envData.encryptedContentInfo, envData.unprotectedAttrs)
            info = ContentInfo(CMSObjectIdentifiers.envelopedData, envData)

            aOut.writeObject(info)

            if (!isSameAs(bOut.toByteArray(), envDataKeyTrns)) {
                return SimpleTestResult(false, name + ": CMS KeyTrans enveloped failed to re-encode")
            }

            //
            // KEK
            //
            aIn = ASN1InputStream(ByteArrayInputStream(envDataKEK))

            info = ContentInfo.getInstance(aIn.readObject())
            envData = EnvelopedData.getInstance(info.content)
            s = envData.recipientInfos

            if (s.size() != 1) {
                return SimpleTestResult(false, name + ": CMS KEK enveloped, wrong number of recipients")
            }

            recip = RecipientInfo.getInstance(s.getObjectAt(0))

            if (recip.getInfo() is KEKRecipientInfo) {
                var inf = KEKRecipientInfo.getInstance(recip.getInfo())

                inf = KEKRecipientInfo(inf.kekid, inf.keyEncryptionAlgorithm, inf.encryptedKey)

                s = DERSet(RecipientInfo(inf))
            } else {
                return SimpleTestResult(false, name + ": CMS KEK enveloped, wrong recipient type")
            }

            bOut = ByteArrayOutputStream()
            aOut = ASN1OutputStream(bOut)

            envData = EnvelopedData(envData.originatorInfo, s, envData.encryptedContentInfo, envData.unprotectedAttrs)
            info = ContentInfo(CMSObjectIdentifiers.envelopedData, envData)

            aOut.writeObject(info)

            if (!isSameAs(bOut.toByteArray(), envDataKEK)) {
                println(String(Base64.encode(bOut.toByteArray())))
                return SimpleTestResult(false, name + ": CMS KEK enveloped failed to re-encode")
            }

            // Nested NDEF problem
            val asn1In = ASN1StreamParser(ByteArrayInputStream(envDataNestedNDEF))
            val ci = ContentInfoParser(asn1In.readObject() as ASN1SequenceParser)
            val ed = EnvelopedDataParser(ci.getContent(BERTags.SEQUENCE) as ASN1SequenceParser?)
            ed.version
            ed.originatorInfo
            ed.recipientInfos.toASN1Primitive()
            val eci = ed.encryptedContentInfo
            eci.contentType
            eci.contentEncryptionAlgorithm

            val dataIn = (eci.getEncryptedContent(BERTags.OCTET_STRING) as ASN1OctetStringParser).octetStream
            Streams.drain(dataIn)
            dataIn.close()

            // Test data doesn't have unprotected attrs, bug was being thrown by this call
            val upa = ed.unprotectedAttrs
            upa?.toASN1Primitive()

            return SimpleTestResult(true, name + ": Okay")
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": CMS enveloped failed - " + e.toString(), e)
        }

    }

    private fun signedTest(): TestResult {
        try {
            val aIn = ASN1InputStream(ByteArrayInputStream(signedData))

            var info = ContentInfo.getInstance(aIn.readObject())
            var sData = SignedData.getInstance(info.content)

            val bOut = ByteArrayOutputStream()
            val aOut = ASN1OutputStream(bOut)

            sData = SignedData(sData.digestAlgorithms, sData.encapContentInfo, sData.certificates, sData.getCRLs(), sData.signerInfos)
            info = ContentInfo(CMSObjectIdentifiers.signedData, sData)

            aOut.writeObject(info)

            if (!isSameAs(bOut.toByteArray(), signedData)) {
                return SimpleTestResult(false, name + ": CMS signed failed to re-encode")
            }

            return SimpleTestResult(true, name + ": Okay")
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": CMS signed failed - " + e.toString(), e)
        }

    }

    override fun perform(): TestResult {
        var res = compressionTest()

        if (!res.isSuccessful) {
            return res
        }

        res = envelopedTest()
        if (!res.isSuccessful) {
            return res
        }

        return signedTest()
    }

    override fun getName(): String {
        return "CMS"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = CMSTest()
            val result = test.perform()

            println(result)
        }
    }
}