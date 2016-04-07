package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERBitString

/**
 * The KeyUsage object.
 *
 * id-ce-keyUsage OBJECT IDENTIFIER ::=  { id-ce 15 }

 * KeyUsage ::= BIT STRING {
 * digitalSignature        (0),
 * nonRepudiation          (1),
 * keyEncipherment         (2),
 * dataEncipherment        (3),
 * keyAgreement            (4),
 * keyCertSign             (5),
 * cRLSign                 (6),
 * encipherOnly            (7),
 * decipherOnly            (8) }
 *
 */
class KeyUsage : ASN1Object {

    private var bitString: DERBitString? = null

    /**
     * Basic constructor.

     * @param usage - the bitwise OR of the Key Usage flags giving the
     * * allowed uses for the key.
     * * e.g. (KeyUsage.keyEncipherment | KeyUsage.dataEncipherment)
     */
    constructor(
            usage: Int) {
        this.bitString = DERBitString(usage)
    }

    private constructor(
            bitString: DERBitString) {
        this.bitString = bitString
    }

    /**
     * Return true if a given usage bit is set, false otherwise.

     * @param usages combination of usage flags.
     * *
     * @return true if all bits are set, false otherwise.
     */
    fun hasUsages(usages: Int): Boolean {
        return bitString!!.intValue() and usages == usages
    }

    val bytes: ByteArray
        get() = bitString!!.bytes

    val padBits: Int
        get() = bitString!!.padBits

    override fun toString(): String {
        val data = bitString!!.bytes

        if (data.size == 1) {
            return "KeyUsage: 0x" + Integer.toHexString(data[0] and 0xff)
        }
        return "KeyUsage: 0x" + Integer.toHexString(data[1] and 0xff shl 8 or (data[0] and 0xff))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return bitString
    }

    companion object {
        val digitalSignature = 1 shl 7
        val nonRepudiation = 1 shl 6
        val keyEncipherment = 1 shl 5
        val dataEncipherment = 1 shl 4
        val keyAgreement = 1 shl 3
        val keyCertSign = 1 shl 2
        val cRLSign = 1 shl 1
        val encipherOnly = 1 shl 0
        val decipherOnly = 1 shl 15

        fun getInstance(obj: Any?   // needs to be DERBitString for other VMs
        ): KeyUsage? {
            if (obj is KeyUsage) {
                return obj
            } else if (obj != null) {
                return KeyUsage(DERBitString.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): KeyUsage {
            return KeyUsage.getInstance(extensions.getExtensionParsedValue(Extension.keyUsage))
        }
    }
}
