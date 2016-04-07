package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.text.ParseException
import java.util.Date
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.oiw.ElGamalParameter
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.CRLReason
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.IssuingDistributionPoint
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x509.TBSCertList
import org.bouncycastle.asn1.x509.TBSCertificate
import org.bouncycastle.asn1.x509.Time
import org.bouncycastle.asn1.x509.V1TBSCertificateGenerator
import org.bouncycastle.asn1.x509.V2TBSCertListGenerator
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator
import org.bouncycastle.asn1.x509.X509Extension
import org.bouncycastle.asn1.x509.X509Extensions
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.test.SimpleTest

class GenerationTest : SimpleTest() {
    private val v1Cert = Base64.decode(
            "MIGtAgEBMA0GCSqGSIb3DQEBBAUAMCUxCzAJBgNVBAMMAkFVMRYwFAYDVQQKDA1Cb"
                    + "3VuY3kgQ2FzdGxlMB4XDTcwMDEwMTAwMDAwMVoXDTcwMDEwMTAwMDAxMlowNjELMA"
                    + "kGA1UEAwwCQVUxFjAUBgNVBAoMDUJvdW5jeSBDYXN0bGUxDzANBgNVBAsMBlRlc3Q"
                    + "gMTAaMA0GCSqGSIb3DQEBAQUAAwkAMAYCAQECAQI=")

    private val v3Cert = Base64.decode(
            "MIIBSKADAgECAgECMA0GCSqGSIb3DQEBBAUAMCUxCzAJBgNVBAMMAkFVMRYwFAYD"
                    + "VQQKDA1Cb3VuY3kgQ2FzdGxlMB4XDTcwMDEwMTAwMDAwMVoXDTcwMDEwMTAwMDAw"
                    + "MlowNjELMAkGA1UEAwwCQVUxFjAUBgNVBAoMDUJvdW5jeSBDYXN0bGUxDzANBgNV"
                    + "BAsMBlRlc3QgMjAYMBAGBisOBwIBATAGAgEBAgECAwQAAgEDo4GVMIGSMGEGA1Ud"
                    + "IwEB/wRXMFWAFDZPdpHPzKi7o8EJokkQU2uqCHRRoTqkODA2MQswCQYDVQQDDAJB"
                    + "VTEWMBQGA1UECgwNQm91bmN5IENhc3RsZTEPMA0GA1UECwwGVGVzdCAyggECMCAG"
                    + "A1UdDgEB/wQWBBQ2T3aRz8you6PBCaJJEFNrqgh0UTALBgNVHQ8EBAMCBBA=")

    private val v3CertNullSubject = Base64.decode(
            "MIHGoAMCAQICAQIwDQYJKoZIhvcNAQEEBQAwJTELMAkGA1UEAwwCQVUxFjAUBgNVB"
                    + "AoMDUJvdW5jeSBDYXN0bGUwHhcNNzAwMTAxMDAwMDAxWhcNNzAwMTAxMDAwMDAyWj"
                    + "AAMBgwEAYGKw4HAgEBMAYCAQECAQIDBAACAQOjSjBIMEYGA1UdEQEB/wQ8MDqkODA"
                    + "2MQswCQYDVQQDDAJBVTEWMBQGA1UECgwNQm91bmN5IENhc3RsZTEPMA0GA1UECwwG"
                    + "VGVzdCAy")

    private val v2CertList = Base64.decode(
            "MIIBQwIBATANBgkqhkiG9w0BAQUFADAlMQswCQYDVQQDDAJBVTEWMBQGA1UECgwN" +
                    "Qm91bmN5IENhc3RsZRcNNzAwMTAxMDAwMDAwWhcNNzAwMTAxMDAwMDAyWjAiMCAC" +
                    "AQEXDTcwMDEwMTAwMDAwMVowDDAKBgNVHRUEAwoBCqCBxTCBwjBhBgNVHSMBAf8E" +
                    "VzBVgBQ2T3aRz8you6PBCaJJEFNrqgh0UaE6pDgwNjELMAkGA1UEAwwCQVUxFjAU" +
                    "BgNVBAoMDUJvdW5jeSBDYXN0bGUxDzANBgNVBAsMBlRlc3QgMoIBAjBDBgNVHRIE" +
                    "PDA6pDgwNjELMAkGA1UEAwwCQVUxFjAUBgNVBAoMDUJvdW5jeSBDYXN0bGUxDzAN" +
                    "BgNVBAsMBlRlc3QgMzAKBgNVHRQEAwIBATAMBgNVHRwBAf8EAjAA")

    @Throws(IOException::class)
    private fun tbsV1CertGen() {
        val gen = V1TBSCertificateGenerator()
        val startDate = Date(1000)
        val endDate = Date(12000)

        gen.setSerialNumber(ASN1Integer(1))

        gen.setStartDate(Time(startDate))
        gen.setEndDate(Time(endDate))

        gen.setIssuer(X500Name("CN=AU,O=Bouncy Castle"))
        gen.setSubject(X500Name("CN=AU,O=Bouncy Castle,OU=Test 1"))

        gen.setSignature(AlgorithmIdentifier(PKCSObjectIdentifiers.md5WithRSAEncryption, DERNull.INSTANCE))

        val info = SubjectPublicKeyInfo(AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
                RSAPublicKey(BigInteger.valueOf(1), BigInteger.valueOf(2)))

        gen.setSubjectPublicKeyInfo(info)

        val tbs = gen.generateTBSCertificate()
        var bOut = ByteArrayOutputStream()
        var aOut = ASN1OutputStream(bOut)

        aOut.writeObject(tbs)

        if (!Arrays.areEqual(bOut.toByteArray(), v1Cert)) {
            fail("failed v1 cert generation")
        }

        //
        // read back test
        //
        val aIn = ASN1InputStream(ByteArrayInputStream(v1Cert))
        val o = aIn.readObject()

        bOut = ByteArrayOutputStream()
        aOut = ASN1OutputStream(bOut)

        aOut.writeObject(o)

        if (!Arrays.areEqual(bOut.toByteArray(), v1Cert)) {
            fail("failed v1 cert read back test")
        }
    }

    private fun createAuthorityKeyId(
            info: SubjectPublicKeyInfo,
            name: X500Name,
            sNumber: Int): AuthorityKeyIdentifier {
        val genName = GeneralName(name)
        val v = ASN1EncodableVector()

        v.add(genName)

        return AuthorityKeyIdentifier(
                info, GeneralNames.getInstance(DERSequence(v)), BigInteger.valueOf(sNumber.toLong()))
    }

    @Throws(IOException::class)
    private fun tbsV3CertGen() {
        val gen = V3TBSCertificateGenerator()
        val startDate = Date(1000)
        val endDate = Date(2000)

        gen.setSerialNumber(ASN1Integer(2))

        gen.setStartDate(Time(startDate))
        gen.setEndDate(Time(endDate))

        gen.setIssuer(X500Name("CN=AU,O=Bouncy Castle"))
        gen.setSubject(X500Name("CN=AU,O=Bouncy Castle,OU=Test 2"))

        gen.setSignature(AlgorithmIdentifier(PKCSObjectIdentifiers.md5WithRSAEncryption, DERNull.INSTANCE))

        val info = SubjectPublicKeyInfo(AlgorithmIdentifier(OIWObjectIdentifiers.elGamalAlgorithm, ElGamalParameter(BigInteger.valueOf(1), BigInteger.valueOf(2))), ASN1Integer(3))

        gen.setSubjectPublicKeyInfo(info)

        //
        // add extensions
        //
        val order = Vector()
        val extensions = Hashtable()

        order.addElement(X509Extension.authorityKeyIdentifier)
        order.addElement(X509Extension.subjectKeyIdentifier)
        order.addElement(X509Extension.keyUsage)

        extensions.put(X509Extension.authorityKeyIdentifier, X509Extension(true, DEROctetString(createAuthorityKeyId(info, X500Name("CN=AU,O=Bouncy Castle,OU=Test 2"), 2))))
        extensions.put(X509Extension.subjectKeyIdentifier, X509Extension(true, DEROctetString(SubjectKeyIdentifier(getDigest(info)))))
        extensions.put(X509Extension.keyUsage, X509Extension(false, DEROctetString(KeyUsage(KeyUsage.dataEncipherment))))

        val ex = X509Extensions(order, extensions)

        gen.setExtensions(ex)

        val tbs = gen.generateTBSCertificate()
        var bOut = ByteArrayOutputStream()
        var aOut = ASN1OutputStream(bOut)

        aOut.writeObject(tbs)

        if (!Arrays.areEqual(bOut.toByteArray(), v3Cert)) {
            fail("failed v3 cert generation")
        }

        //
        // read back test
        //
        val aIn = ASN1InputStream(ByteArrayInputStream(v3Cert))
        val o = aIn.readObject()

        bOut = ByteArrayOutputStream()
        aOut = ASN1OutputStream(bOut)

        aOut.writeObject(o)

        if (!Arrays.areEqual(bOut.toByteArray(), v3Cert)) {
            fail("failed v3 cert read back test")
        }
    }

    @Throws(IOException::class)
    private fun tbsV3CertGenWithNullSubject() {
        val gen = V3TBSCertificateGenerator()
        val startDate = Date(1000)
        val endDate = Date(2000)

        gen.setSerialNumber(ASN1Integer(2))

        gen.setStartDate(Time(startDate))
        gen.setEndDate(Time(endDate))

        gen.setIssuer(X500Name("CN=AU,O=Bouncy Castle"))

        gen.setSignature(AlgorithmIdentifier(PKCSObjectIdentifiers.md5WithRSAEncryption, DERNull.INSTANCE))

        val info = SubjectPublicKeyInfo(AlgorithmIdentifier(OIWObjectIdentifiers.elGamalAlgorithm, ElGamalParameter(BigInteger.valueOf(1), BigInteger.valueOf(2))), ASN1Integer(3))

        gen.setSubjectPublicKeyInfo(info)

        try {
            gen.generateTBSCertificate()
            fail("null subject not caught!")
        } catch (e: IllegalStateException) {
            if (e.message != "not all mandatory fields set in V3 TBScertificate generator") {
                fail("unexpected exception", e)
            }
        }

        //
        // add extensions
        //
        val order = Vector()
        val extensions = Hashtable()

        order.addElement(X509Extension.subjectAlternativeName)

        extensions.put(X509Extension.subjectAlternativeName, X509Extension(true, DEROctetString(GeneralNames(GeneralName(X500Name("CN=AU,O=Bouncy Castle,OU=Test 2"))))))

        val ex = X509Extensions(order, extensions)

        gen.setExtensions(ex)

        val tbs = gen.generateTBSCertificate()
        var bOut = ByteArrayOutputStream()
        var aOut = ASN1OutputStream(bOut)

        aOut.writeObject(tbs)

        if (!Arrays.areEqual(bOut.toByteArray(), v3CertNullSubject)) {
            fail("failed v3 null sub cert generation")
        }

        //
        // read back test
        //
        val aIn = ASN1InputStream(ByteArrayInputStream(v3CertNullSubject))
        val o = aIn.readObject()

        bOut = ByteArrayOutputStream()
        aOut = ASN1OutputStream(bOut)

        aOut.writeObject(o)

        if (!Arrays.areEqual(bOut.toByteArray(), v3CertNullSubject)) {
            fail("failed v3 null sub cert read back test")
        }
    }

    @Throws(IOException::class)
    private fun tbsV2CertListGen() {
        val gen = V2TBSCertListGenerator()

        gen.setIssuer(X500Name("CN=AU,O=Bouncy Castle"))

        gen.addCRLEntry(ASN1Integer(1), Time(Date(1000)), CRLReason.aACompromise)

        gen.setNextUpdate(Time(Date(2000)))

        gen.setThisUpdate(Time(Date(500)))

        gen.setSignature(AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption, DERNull.INSTANCE))

        //
        // extensions
        //
        val info = SubjectPublicKeyInfo(AlgorithmIdentifier(OIWObjectIdentifiers.elGamalAlgorithm, ElGamalParameter(BigInteger.valueOf(1), BigInteger.valueOf(2))), ASN1Integer(3))

        val extGen = ExtensionsGenerator()

        extGen.addExtension(Extension.authorityKeyIdentifier, true, createAuthorityKeyId(info, X500Name("CN=AU,O=Bouncy Castle,OU=Test 2"), 2))
        extGen.addExtension(Extension.issuerAlternativeName, false, GeneralNames(GeneralName(X500Name("CN=AU,O=Bouncy Castle,OU=Test 3"))))
        extGen.addExtension(Extension.cRLNumber, false, ASN1Integer(1))
        extGen.addExtension(Extension.issuingDistributionPoint, true, IssuingDistributionPoint.getInstance(DERSequence()))

        val ex = extGen.generate()

        gen.setExtensions(ex)

        val tbs = gen.generateTBSCertList()
        var bOut = ByteArrayOutputStream()
        var aOut = ASN1OutputStream(bOut)

        aOut.writeObject(tbs)

        if (!Arrays.areEqual(bOut.toByteArray(), v2CertList)) {
            println(String(Base64.encode(bOut.toByteArray())))
            fail("failed v2 cert list generation")
        }

        //
        // read back test
        //
        val aIn = ASN1InputStream(ByteArrayInputStream(v2CertList))
        val o = aIn.readObject()

        bOut = ByteArrayOutputStream()
        aOut = ASN1OutputStream(bOut)

        aOut.writeObject(o)

        if (!Arrays.areEqual(bOut.toByteArray(), v2CertList)) {
            fail("failed v2 cert list read back test")
        }

        //
        // check we can add a custom reason
        //
        gen.addCRLEntry(ASN1Integer(1), Time(Date(1000)), CRLReason.aACompromise)

        //
        // check invalidity date
        gen.addCRLEntry(ASN1Integer(2), Time(Date(1000)), CRLReason.affiliationChanged, ASN1GeneralizedTime(Date(2000)))

        val crl = gen.generateTBSCertList()

        val entries = crl.revokedCertificates
        for (i in entries.indices) {
            val entry = entries[i]

            if (entry.userCertificate == ASN1Integer(1)) {
                val extensions = entry.extensions
                val ext = extensions.getExtension(Extension.reasonCode)

                val r = CRLReason.getInstance(ext.parsedValue)

                if (r.value.toInt() != CRLReason.aACompromise) {
                    fail("reason code mismatch")
                }
            } else if (entry.userCertificate == ASN1Integer(2)) {
                val extensions = entry.extensions
                var ext = extensions.getExtension(Extension.reasonCode)

                val r = CRLReason.getInstance(ext.parsedValue)

                if (r.value.toInt() != CRLReason.affiliationChanged) {
                    fail("reason code mismatch")
                }

                ext = extensions.getExtension(Extension.invalidityDate)

                val t = ASN1GeneralizedTime.getInstance(ext.parsedValue)

                try {
                    if (t.date != Date(2000)) {
                        fail("invalidity date mismatch")
                    }
                } catch (e: ParseException) {
                    fail("can't parse date", e)
                }

            }
        }
    }

    @Throws(Exception::class)
    override fun performTest() {
        tbsV1CertGen()
        tbsV3CertGen()
        tbsV3CertGenWithNullSubject()
        tbsV2CertListGen()
    }

    override fun getName(): String {
        return "Generation"
    }

    companion object {

        private fun getDigest(spki: SubjectPublicKeyInfo): ByteArray {
            val digest = SHA1Digest()
            val resBuf = ByteArray(digest.digestSize)

            val bytes = spki.publicKeyData.bytes
            digest.update(bytes, 0, bytes.size)
            digest.doFinal(resBuf, 0)
            return resBuf
        }

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(GenerationTest())
        }
    }
}
