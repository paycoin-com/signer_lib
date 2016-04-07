package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface MiscObjectIdentifiers {
    companion object {
        //
        // Netscape
        //       iso/itu(2) joint-assign(16) us(840) uscompany(1) netscape(113730) cert-extensions(1) }
        //
        /** Netscape cert extensions OID base: 2.16.840.1.113730.1   */
        val netscape = ASN1ObjectIdentifier("2.16.840.1.113730.1")
        /** Netscape cert CertType OID: 2.16.840.1.113730.1.1   */
        val netscapeCertType = netscape.branch("1")
        /** Netscape cert BaseURL OID: 2.16.840.1.113730.1.2   */
        val netscapeBaseURL = netscape.branch("2")
        /** Netscape cert RevocationURL OID: 2.16.840.1.113730.1.3   */
        val netscapeRevocationURL = netscape.branch("3")
        /** Netscape cert CARevocationURL OID: 2.16.840.1.113730.1.4   */
        val netscapeCARevocationURL = netscape.branch("4")
        /** Netscape cert RenewalURL OID: 2.16.840.1.113730.1.7   */
        val netscapeRenewalURL = netscape.branch("7")
        /** Netscape cert CApolicyURL OID: 2.16.840.1.113730.1.8   */
        val netscapeCApolicyURL = netscape.branch("8")
        /** Netscape cert SSLServerName OID: 2.16.840.1.113730.1.12   */
        val netscapeSSLServerName = netscape.branch("12")
        /** Netscape cert CertComment OID: 2.16.840.1.113730.1.13   */
        val netscapeCertComment = netscape.branch("13")

        //
        // Verisign
        //       iso/itu(2) joint-assign(16) us(840) uscompany(1) verisign(113733) cert-extensions(1) }
        //
        /** Verisign OID base: 2.16.840.1.113733.1  */
        val verisign = ASN1ObjectIdentifier("2.16.840.1.113733.1")

        /** Verisign CZAG (Country,Zip,Age,Gender) Extension OID: 2.16.840.1.113733.1.6.3  */
        val verisignCzagExtension = verisign.branch("6.3")

        val verisignPrivate_6_9 = verisign.branch("6.9")
        val verisignOnSiteJurisdictionHash = verisign.branch("6.11")
        val verisignBitString_6_13 = verisign.branch("6.13")

        /** Verisign D&amp;B D-U-N-S number Extension OID: 2.16.840.1.113733.1.6.15  */
        val verisignDnbDunsNumber = verisign.branch("6.15")

        val verisignIssStrongCrypto = verisign.branch("8.1")

        //
        // Novell
        //       iso/itu(2) country(16) us(840) organization(1) novell(113719)
        //
        /** Novell OID base: 2.16.840.1.113719  */
        val novell = ASN1ObjectIdentifier("2.16.840.1.113719")
        /** Novell SecurityAttribs OID: 2.16.840.1.113719.1.9.4.1  */
        val novellSecurityAttribs = novell.branch("1.9.4.1")

        //
        // Entrust
        //       iso(1) member-body(16) us(840) nortelnetworks(113533) entrust(7)
        //
        /** NortelNetworks Entrust OID base: 1.2.840.113533.7  */
        val entrust = ASN1ObjectIdentifier("1.2.840.113533.7")
        /** NortelNetworks Entrust VersionExtension OID: 1.2.840.113533.7.65.0  */
        val entrustVersionExtension = entrust.branch("65.0")

        /** cast5CBC OBJECT IDENTIFIER ::= {iso(1) member-body(2) us(840) nt(113533) nsn(7) algorithms(66) 10} SEE RFC 2984  */
        val cast5CBC = entrust.branch("66.10")

        //
        // Ascom
        //
        val as_sys_sec_alg_ideaCBC = ASN1ObjectIdentifier("1.3.6.1.4.1.188.7.1.1.2")

        //
        // Peter Gutmann's Cryptlib
        //
        val cryptlib = ASN1ObjectIdentifier("1.3.6.1.4.1.3029")

        val cryptlib_algorithm = cryptlib.branch("1")
        val cryptlib_algorithm_blowfish_ECB = cryptlib_algorithm.branch("1.1")
        val cryptlib_algorithm_blowfish_CBC = cryptlib_algorithm.branch("1.2")
        val cryptlib_algorithm_blowfish_CFB = cryptlib_algorithm.branch("1.3")
        val cryptlib_algorithm_blowfish_OFB = cryptlib_algorithm.branch("1.4")

        //
        // Blake2b
        //
        val blake2 = ASN1ObjectIdentifier("1.3.6.1.4.1.1722.12.2")

        val id_blake2b160 = blake2.branch("1.5")
        val id_blake2b256 = blake2.branch("1.8")
        val id_blake2b384 = blake2.branch("1.12")
        val id_blake2b512 = blake2.branch("1.16")
    }
}
