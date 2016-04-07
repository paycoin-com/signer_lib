package org.bouncycastle.asn1.kisa

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * Korea Information Security Agency (KISA)
 * ({iso(1) member-body(2) kr(410) kisa(200004)})
 *
 *
 * See [RFC 4010](http://tools.ietf.org/html/rfc4010)
 * Use of the SEED Encryption Algorithm
 * in Cryptographic Message Syntax (CMS),
 * and [RFC 4269](http://tools.ietf.org/html/rfc4269)
 * The SEED Encryption Algorithm
 */
interface KISAObjectIdentifiers {
    companion object {
        /** RFC 4010, 4269: id-seedCBC; OID 1.2.410.200004.1.4  */
        val id_seedCBC = ASN1ObjectIdentifier("1.2.410.200004.1.4")

        /** RFC 4269: id-seedMAC; OID 1.2.410.200004.1.7  */
        val id_seedMAC = ASN1ObjectIdentifier("1.2.410.200004.1.7")

        /** RFC 4269: pbeWithSHA1AndSEED-CBC; OID 1.2.410.200004.1.15  */
        val pbeWithSHA1AndSEED_CBC = ASN1ObjectIdentifier("1.2.410.200004.1.15")

        /** RFC 4010: id-npki-app-cmsSeed-wrap; OID 1.2.410.200004.7.1.1.1  */
        val id_npki_app_cmsSeed_wrap = ASN1ObjectIdentifier("1.2.410.200004.7.1.1.1")

        /** RFC 4010: SeedEncryptionAlgorithmInCMS; OID 1.2.840.113549.1.9.16.0.24  */
        val id_mod_cms_seed = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.0.24")
    }
}
