package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.isismtt.ocsp.RequestedCertificate
import org.bouncycastle.asn1.x509.Certificate
import org.bouncycastle.util.encoders.Base64

class RequestedCertificateUnitTest : ASN1UnitTest() {
    internal var certBytes = Base64.decode(
            "MIIBWzCCAQYCARgwDQYJKoZIhvcNAQEEBQAwODELMAkGA1UEBhMCQVUxDDAKBgNV"
                    + "BAgTA1FMRDEbMBkGA1UEAxMSU1NMZWF5L3JzYSB0ZXN0IENBMB4XDTk1MDYxOTIz"
                    + "MzMxMloXDTk1MDcxNzIzMzMxMlowOjELMAkGA1UEBhMCQVUxDDAKBgNVBAgTA1FM"
                    + "RDEdMBsGA1UEAxMUU1NMZWF5L3JzYSB0ZXN0IGNlcnQwXDANBgkqhkiG9w0BAQEF"
                    + "AANLADBIAkEAqtt6qS5GTxVxGZYWa0/4u+IwHf7p2LNZbcPBp9/OfIcYAXBQn8hO"
                    + "/Re1uwLKXdCjIoaGs4DLdG88rkzfyK5dPQIDAQABMAwGCCqGSIb3DQIFBQADQQAE"
                    + "Wc7EcF8po2/ZO6kNCwK/ICH6DobgLekA5lSLr5EvuioZniZp5lFzAw4+YzPQ7XKJ"
                    + "zl9HYIMxATFyqSiD9jsx")

    override fun getName(): String {
        return "RequestedCertificate"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val type = 1
        val certOctets = ByteArray(20)
        val cert = Certificate.getInstance(certBytes)

        var requested: RequestedCertificate? = RequestedCertificate(type, certOctets)

        checkConstruction(requested, type, certOctets, null)

        requested = RequestedCertificate(cert)

        checkConstruction(requested, RequestedCertificate.certificate, null, cert)

        requested = RequestedCertificate.getInstance(null)

        if (requested != null) {
            fail("null getInstance() failed.")
        }

        try {
            RequestedCertificate.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            requested: RequestedCertificate,
            type: Int,
            certOctets: ByteArray?,
            cert: Certificate?) {
        var requested = requested
        checkValues(requested, type, certOctets, cert)

        requested = RequestedCertificate.getInstance(requested)

        checkValues(requested, type, certOctets, cert)

        val aIn = ASN1InputStream(requested.toASN1Primitive().encoded)

        val obj = aIn.readObject()

        requested = RequestedCertificate.getInstance(obj)

        checkValues(requested, type, certOctets, cert)
    }

    @Throws(IOException::class)
    private fun checkValues(
            requested: RequestedCertificate,
            type: Int,
            certOctets: ByteArray,
            cert: Certificate) {
        checkMandatoryField("certType", type, requested.type)

        if (requested.type == RequestedCertificate.certificate) {
            checkMandatoryField("certificate", cert.encoded, requested.certificateBytes)
        } else {
            checkMandatoryField("certificateOctets", certOctets, requested.certificateBytes)
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(RequestedCertificateUnitTest())
        }
    }
}
