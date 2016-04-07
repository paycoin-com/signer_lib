package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652) CMS attribute OID constants.
 * and [RFC 6211](http://tools.ietf.org/html/rfc6211) Algorithm Identifier Protection Attribute.
 *
 * contentType      ::= 1.2.840.113549.1.9.3
 * messageDigest    ::= 1.2.840.113549.1.9.4
 * signingTime      ::= 1.2.840.113549.1.9.5
 * counterSignature ::= 1.2.840.113549.1.9.6

 * contentHint      ::= 1.2.840.113549.1.9.16.2.4
 * cmsAlgorithmProtect := 1.2.840.113549.1.9.52
 *
 */

interface CMSAttributes {
    companion object {
        /** PKCS#9: 1.2.840.113549.1.9.3  */
        val contentType = PKCSObjectIdentifiers.pkcs_9_at_contentType
        /** PKCS#9: 1.2.840.113549.1.9.4  */
        val messageDigest = PKCSObjectIdentifiers.pkcs_9_at_messageDigest
        /** PKCS#9: 1.2.840.113549.1.9.5  */
        val signingTime = PKCSObjectIdentifiers.pkcs_9_at_signingTime
        /** PKCS#9: 1.2.840.113549.1.9.6  */
        val counterSignature = PKCSObjectIdentifiers.pkcs_9_at_counterSignature
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.4 - See [RFC 2634](http://tools.ietf.org/html/rfc2634)  */
        val contentHint = PKCSObjectIdentifiers.id_aa_contentHint

        val cmsAlgorithmProtect = PKCSObjectIdentifiers.id_aa_cmsAlgorithmProtect
    }

}
