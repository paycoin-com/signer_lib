package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface X509ObjectIdentifiers {
    companion object {

        /** Subject RDN components: commonName = 2.5.4.3  */
        val commonName = ASN1ObjectIdentifier("2.5.4.3").intern()
        /** Subject RDN components: countryName = 2.5.4.6  */
        val countryName = ASN1ObjectIdentifier("2.5.4.6").intern()
        /** Subject RDN components: localityName = 2.5.4.7  */
        val localityName = ASN1ObjectIdentifier("2.5.4.7").intern()
        /** Subject RDN components: stateOrProvinceName = 2.5.4.8  */
        val stateOrProvinceName = ASN1ObjectIdentifier("2.5.4.8").intern()
        /** Subject RDN components: organization = 2.5.4.10  */
        val organization = ASN1ObjectIdentifier("2.5.4.10").intern()
        /** Subject RDN components: organizationalUnitName = 2.5.4.11  */
        val organizationalUnitName = ASN1ObjectIdentifier("2.5.4.11").intern()

        /** Subject RDN components: telephone_number = 2.5.4.20  */
        val id_at_telephoneNumber = ASN1ObjectIdentifier("2.5.4.20").intern()
        /** Subject RDN components: name = 2.5.4.41  */
        val id_at_name = ASN1ObjectIdentifier("2.5.4.41").intern()

        /**
         * id-SHA1 OBJECT IDENTIFIER ::=
         * {iso(1) identified-organization(3) oiw(14) secsig(3) algorithms(2) 26 }
         *
         *
         * OID: 1.3.14.3.2.27
         */
        val id_SHA1 = ASN1ObjectIdentifier("1.3.14.3.2.26").intern()

        /**
         * ripemd160 OBJECT IDENTIFIER ::=
         * {iso(1) identified-organization(3) TeleTrust(36) algorithm(3) hashAlgorithm(2) RIPEMD-160(1)}
         *
         *
         * OID: 1.3.36.3.2.1
         */
        val ripemd160 = ASN1ObjectIdentifier("1.3.36.3.2.1").intern()

        /**
         * ripemd160WithRSAEncryption OBJECT IDENTIFIER ::=
         * {iso(1) identified-organization(3) TeleTrust(36) algorithm(3) signatureAlgorithm(3) rsaSignature(1) rsaSignatureWithripemd160(2) }
         *
         *
         * OID: 1.3.36.3.3.1.2
         */
        val ripemd160WithRSAEncryption = ASN1ObjectIdentifier("1.3.36.3.3.1.2").intern()


        /** OID: 2.5.8.1.1   */
        val id_ea_rsa = ASN1ObjectIdentifier("2.5.8.1.1").intern()

        /** id-pkix OID: 1.3.6.1.5.5.7
         */
        val id_pkix = ASN1ObjectIdentifier("1.3.6.1.5.5.7")

        /**
         * private internet extensions; OID = 1.3.6.1.5.5.7.1
         */
        val id_pe = id_pkix.branch("1")

        /**
         * ISO ARC for standard certificate and CRL extensions
         *
         *
         * OID: 2.5.29
         */
        val id_ce = ASN1ObjectIdentifier("2.5.29")

        /** id-pkix OID:         1.3.6.1.5.5.7.48   */
        val id_ad = id_pkix.branch("48")
        /** id-ad-caIssuers OID: 1.3.6.1.5.5.7.48.2   */
        val id_ad_caIssuers = id_ad.branch("2").intern()
        /** id-ad-ocsp OID:      1.3.6.1.5.5.7.48.1   */
        val id_ad_ocsp = id_ad.branch("1").intern()

        /** OID for ocsp uri in AuthorityInformationAccess extension  */
        val ocspAccessMethod = id_ad_ocsp
        /** OID for crl uri in AuthorityInformationAccess extension  */
        val crlAccessMethod = id_ad_caIssuers
    }
}
