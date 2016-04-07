package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * OIDs for [RFC 2560](http://tools.ietf.org/html/rfc2560) and [RFC 6960](http://tools.ietf.org/html/rfc6960)
 * Online Certificate Status Protocol - OCSP.
 */
interface OCSPObjectIdentifiers {
    companion object {
        /** OID: 1.3.6.1.5.5.7.48.1  */
        val id_pkix_ocsp = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1")
        /** OID: 1.3.6.1.5.5.7.48.1.1  */
        val id_pkix_ocsp_basic = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.1")

        /** OID: 1.3.6.1.5.5.7.48.1.2  */
        val id_pkix_ocsp_nonce = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.2")
        /** OID: 1.3.6.1.5.5.7.48.1.3  */
        val id_pkix_ocsp_crl = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.3")

        /** OID: 1.3.6.1.5.5.7.48.1.4  */
        val id_pkix_ocsp_response = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.4")
        /** OID: 1.3.6.1.5.5.7.48.1.5  */
        val id_pkix_ocsp_nocheck = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.5")
        /** OID: 1.3.6.1.5.5.7.48.1.6  */
        val id_pkix_ocsp_archive_cutoff = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.6")
        /** OID: 1.3.6.1.5.5.7.48.1.7  */
        val id_pkix_ocsp_service_locator = ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.7")


        val id_pkix_ocsp_pref_sig_algs = id_pkix_ocsp.branch("8")

        val id_pkix_ocsp_extended_revoke = id_pkix_ocsp.branch("9")
    }
}
