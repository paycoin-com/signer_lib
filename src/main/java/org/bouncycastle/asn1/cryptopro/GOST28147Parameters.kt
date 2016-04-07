package org.bouncycastle.asn1.cryptopro

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * ASN.1 algorithm identifier parameters for GOST-28147
 */
class GOST28147Parameters
@Deprecated("use the getInstance() method. This constructor will vanish!")
constructor(
        seq: ASN1Sequence) : ASN1Object() {
    private val iv: ASN1OctetString
    /**
     * Return the OID representing the sBox to use.

     * @return the sBox OID.
     */
    val encryptionParamSet: ASN1ObjectIdentifier

    init {
        val e = seq.objects

        iv = e.nextElement() as ASN1OctetString
        encryptionParamSet = e.nextElement() as ASN1ObjectIdentifier
    }

    /**
     *
     * Gost28147-89-Parameters ::=
     * SEQUENCE {
     * iv                   Gost28147-89-IV,
     * encryptionParamSet   OBJECT IDENTIFIER
     * }

     * Gost28147-89-IV ::= OCTET STRING (SIZE (8))
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(iv)
        v.add(encryptionParamSet)

        return DERSequence(v)
    }

    /**
     * Return the initialisation vector to use.

     * @return the IV.
     */
    fun getIV(): ByteArray {
        return iv.octets
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): GOST28147Parameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): GOST28147Parameters? {
            if (obj is GOST28147Parameters) {
                return obj
            }

            if (obj != null) {
                return GOST28147Parameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
