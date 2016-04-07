package org.bouncycastle.asn1.bsi

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * See https://www.bsi.bund.de/cae/servlet/contentblob/471398/publicationFile/30615/BSI-TR-03111_pdf.pdf
 */
interface BSIObjectIdentifiers {
    companion object {
        val bsi_de = ASN1ObjectIdentifier("0.4.0.127.0.7")

        /* 0.4.0.127.0.7.1.1 */
        val id_ecc = bsi_de.branch("1.1")

        /* 0.4.0.127.0.7.1.1.4.1 */
        val ecdsa_plain_signatures = id_ecc.branch("4.1")

        /* 0.4.0.127.0.7.1.1.4.1.1 */
        val ecdsa_plain_SHA1 = ecdsa_plain_signatures.branch("1")

        /* 0.4.0.127.0.7.1.1.4.1.2 */
        val ecdsa_plain_SHA224 = ecdsa_plain_signatures.branch("2")

        /* 0.4.0.127.0.7.1.1.4.1.3 */
        val ecdsa_plain_SHA256 = ecdsa_plain_signatures.branch("3")

        /* 0.4.0.127.0.7.1.1.4.1.4 */
        val ecdsa_plain_SHA384 = ecdsa_plain_signatures.branch("4")

        /* 0.4.0.127.0.7.1.1.4.1.5 */
        val ecdsa_plain_SHA512 = ecdsa_plain_signatures.branch("5")

        /* 0.4.0.127.0.7.1.1.4.1.6 */
        val ecdsa_plain_RIPEMD160 = ecdsa_plain_signatures.branch("6")
    }
}
