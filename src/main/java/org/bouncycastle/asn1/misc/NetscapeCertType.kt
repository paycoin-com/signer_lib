package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.DERBitString

/**
 * The NetscapeCertType object.
 *
 * NetscapeCertType ::= BIT STRING {
 * SSLClient               (0),
 * SSLServer               (1),
 * S/MIME                  (2),
 * Object Signing          (3),
 * Reserved                (4),
 * SSL CA                  (5),
 * S/MIME CA               (6),
 * Object Signing CA       (7) }
 *
 */
class NetscapeCertType : DERBitString {

    /**
     * Basic constructor.

     * @param usage - the bitwise OR of the Key Usage flags giving the
     * * allowed uses for the key.
     * * e.g. (X509NetscapeCertType.sslCA | X509NetscapeCertType.smimeCA)
     */
    constructor(
            usage: Int) : super(ASN1BitString.getBytes(usage), ASN1BitString.getPadBits(usage)) {
    }

    constructor(
            usage: DERBitString) : super(usage.bytes, usage.getPadBits()) {
    }

    override fun toString(): String {
        return "NetscapeCertType: 0x" + Integer.toHexString(data[0] and 0xff)
    }

    companion object {
        val sslClient = 1 shl 7
        val sslServer = 1 shl 6
        val smime = 1 shl 5
        val objectSigning = 1 shl 4
        val reserved = 1 shl 3
        val sslCA = 1 shl 2
        val smimeCA = 1 shl 1
        val objectSigningCA = 1 shl 0
    }
}
