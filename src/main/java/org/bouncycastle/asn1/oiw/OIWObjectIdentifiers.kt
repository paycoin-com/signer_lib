package org.bouncycastle.asn1.oiw

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * OIW organization's OIDs:
 *
 *
 * id-SHA1 OBJECT IDENTIFIER ::=
 * {iso(1) identified-organization(3) oiw(14) secsig(3) algorithms(2) 26 }
 */
interface OIWObjectIdentifiers {
    companion object {
        /** OID: 1.3.14.3.2.2  */
        val md4WithRSA = ASN1ObjectIdentifier("1.3.14.3.2.2")
        /** OID: 1.3.14.3.2.3  */
        val md5WithRSA = ASN1ObjectIdentifier("1.3.14.3.2.3")
        /** OID: 1.3.14.3.2.4  */
        val md4WithRSAEncryption = ASN1ObjectIdentifier("1.3.14.3.2.4")

        /** OID: 1.3.14.3.2.6  */
        val desECB = ASN1ObjectIdentifier("1.3.14.3.2.6")
        /** OID: 1.3.14.3.2.7  */
        val desCBC = ASN1ObjectIdentifier("1.3.14.3.2.7")
        /** OID: 1.3.14.3.2.8  */
        val desOFB = ASN1ObjectIdentifier("1.3.14.3.2.8")
        /** OID: 1.3.14.3.2.9  */
        val desCFB = ASN1ObjectIdentifier("1.3.14.3.2.9")

        /** OID: 1.3.14.3.2.17  */
        val desEDE = ASN1ObjectIdentifier("1.3.14.3.2.17")

        /** OID: 1.3.14.3.2.26  */
        val idSHA1 = ASN1ObjectIdentifier("1.3.14.3.2.26")

        /** OID: 1.3.14.3.2.27  */
        val dsaWithSHA1 = ASN1ObjectIdentifier("1.3.14.3.2.27")

        /** OID: 1.3.14.3.2.29  */
        val sha1WithRSA = ASN1ObjectIdentifier("1.3.14.3.2.29")

        /**
         *
         * ElGamal Algorithm OBJECT IDENTIFIER ::=
         * {iso(1) identified-organization(3) oiw(14) dirservsig(7) algorithm(2) encryption(1) 1 }
         *
         * OID: 1.3.14.7.2.1.1
         */
        val elGamalAlgorithm = ASN1ObjectIdentifier("1.3.14.7.2.1.1")
    }

}
