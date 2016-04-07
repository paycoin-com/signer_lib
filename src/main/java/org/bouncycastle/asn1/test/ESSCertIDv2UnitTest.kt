package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ess.ESSCertIDv2
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class ESSCertIDv2UnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "ESSCertIDv2"
    }

    @Throws(Exception::class)
    override fun performTest() {
        // check getInstance on default algorithm.
        val digest = ByteArray(256)
        val essCertIdv2 = ESSCertIDv2(AlgorithmIdentifier(
                NISTObjectIdentifiers.id_sha256), digest)
        val asn1Object = essCertIdv2.toASN1Primitive()

        ESSCertIDv2.getInstance(asn1Object)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(ESSCertIDv2UnitTest())
        }
    }
}