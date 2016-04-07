package org.bouncycastle.asn1.teletrust

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * TeleTrusT:
 * { iso(1) identifier-organization(3) teleTrust(36) algorithm(3)

 */
interface TeleTrusTObjectIdentifiers {
    companion object {
        /** 1.3.36.3  */
        val teleTrusTAlgorithm = ASN1ObjectIdentifier("1.3.36.3")

        /** 1.3.36.3.2.1  */
        val ripemd160 = teleTrusTAlgorithm.branch("2.1")
        /** 1.3.36.3.2.2  */
        val ripemd128 = teleTrusTAlgorithm.branch("2.2")
        /** 1.3.36.3.2.3  */
        val ripemd256 = teleTrusTAlgorithm.branch("2.3")

        /** 1.3.36.3.3.1  */
        val teleTrusTRSAsignatureAlgorithm = teleTrusTAlgorithm.branch("3.1")

        /** 1.3.36.3.3.1.2  */
        val rsaSignatureWithripemd160 = teleTrusTRSAsignatureAlgorithm.branch("2")
        /** 1.3.36.3.3.1.3  */
        val rsaSignatureWithripemd128 = teleTrusTRSAsignatureAlgorithm.branch("3")
        /** 1.3.36.3.3.1.4  */
        val rsaSignatureWithripemd256 = teleTrusTRSAsignatureAlgorithm.branch("4")

        /** 1.3.36.3.3.2  */
        val ecSign = teleTrusTAlgorithm.branch("3.2")

        /** 1.3.36.3.3.2,1  */
        val ecSignWithSha1 = ecSign.branch("1")
        /** 1.3.36.3.3.2.2  */
        val ecSignWithRipemd160 = ecSign.branch("2")

        /** 1.3.36.3.3.2.8  */
        val ecc_brainpool = teleTrusTAlgorithm.branch("3.2.8")
        /** 1.3.36.3.3.2.8.1  */
        val ellipticCurve = ecc_brainpool.branch("1")
        /** 1.3.36.3.3.2.8.1.1  */
        val versionOne = ellipticCurve.branch("1")

        /** 1.3.36.3.3.2.8.1.1.1  */
        val brainpoolP160r1 = versionOne.branch("1")
        /** 1.3.36.3.3.2.8.1.1.2  */
        val brainpoolP160t1 = versionOne.branch("2")
        /** 1.3.36.3.3.2.8.1.1.3  */
        val brainpoolP192r1 = versionOne.branch("3")
        /** 1.3.36.3.3.2.8.1.1.4  */
        val brainpoolP192t1 = versionOne.branch("4")
        /** 1.3.36.3.3.2.8.1.1.5  */
        val brainpoolP224r1 = versionOne.branch("5")
        /** 1.3.36.3.3.2.8.1.1.6  */
        val brainpoolP224t1 = versionOne.branch("6")
        /** 1.3.36.3.3.2.8.1.1.7  */
        val brainpoolP256r1 = versionOne.branch("7")
        /** 1.3.36.3.3.2.8.1.1.8  */
        val brainpoolP256t1 = versionOne.branch("8")
        /** 1.3.36.3.3.2.8.1.1.9  */
        val brainpoolP320r1 = versionOne.branch("9")
        /** 1.3.36.3.3.2.8.1.1.10  */
        val brainpoolP320t1 = versionOne.branch("10")
        /** 1.3.36.3.3.2.8.1.1.11  */
        val brainpoolP384r1 = versionOne.branch("11")
        /** 1.3.36.3.3.2.8.1.1.12  */
        val brainpoolP384t1 = versionOne.branch("12")
        /** 1.3.36.3.3.2.8.1.1.13  */
        val brainpoolP512r1 = versionOne.branch("13")
        /** 1.3.36.3.3.2.8.1.1.14  */
        val brainpoolP512t1 = versionOne.branch("14")
    }
}
