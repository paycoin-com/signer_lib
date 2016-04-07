package org.bouncycastle.asn1.ntt

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * From [RFC 3657](http://tools.ietf.org/html/rfc3657)
 * Use of the Camellia Encryption Algorithm
 * in Cryptographic Message Syntax (CMS)
 */
interface NTTObjectIdentifiers {
    companion object {
        /** id-camellia128-cbc; OID 1.2.392.200011.61.1.1.1.2  */
        val id_camellia128_cbc = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.2")
        /** id-camellia192-cbc; OID 1.2.392.200011.61.1.1.1.3  */
        val id_camellia192_cbc = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.3")
        /** id-camellia256-cbc; OID 1.2.392.200011.61.1.1.1.4  */
        val id_camellia256_cbc = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.4")

        /** id-camellia128-wrap; OID 1.2.392.200011.61.1.1.3.2  */
        val id_camellia128_wrap = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.2")
        /** id-camellia192-wrap; OID 1.2.392.200011.61.1.1.3.3  */
        val id_camellia192_wrap = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.3")
        /** id-camellia256-wrap; OID 1.2.392.200011.61.1.1.3.4  */
        val id_camellia256_wrap = ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.4")
    }
}
