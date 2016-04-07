package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * The extendedKeyUsage object.
 *
 * extendedKeyUsage ::= SEQUENCE SIZE (1..MAX) OF KeyPurposeId
 *
 */
class ExtendedKeyUsage : ASN1Object {
    internal var usageTable = Hashtable()
    internal var seq: ASN1Sequence

    /**
     * Base constructor, from a single KeyPurposeId.

     * @param usage the keyPurposeId to be included.
     */
    constructor(
            usage: KeyPurposeId) {
        this.seq = DERSequence(usage)

        this.usageTable.put(usage, usage)
    }

    private constructor(
            seq: ASN1Sequence) {
        this.seq = seq

        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Encodable
            if (o.toASN1Primitive() !is ASN1ObjectIdentifier) {
                throw IllegalArgumentException("Only ASN1ObjectIdentifiers allowed in ExtendedKeyUsage.")
            }
            this.usageTable.put(o, o)
        }
    }

    /**
     * Base constructor, from multiple KeyPurposeIds.

     * @param usages an array of KeyPurposeIds.
     */
    constructor(
            usages: Array<KeyPurposeId>) {
        val v = ASN1EncodableVector()

        for (i in usages.indices) {
            v.add(usages[i])
            this.usageTable.put(usages[i], usages[i])
        }

        this.seq = DERSequence(v)
    }


    @Deprecated("use KeyPurposeId[] constructor.")
    constructor(
            usages: Vector<Any>) {
        val v = ASN1EncodableVector()
        val e = usages.elements()

        while (e.hasMoreElements()) {
            val o = KeyPurposeId.getInstance(e.nextElement())

            v.add(o)
            this.usageTable.put(o, o)
        }

        this.seq = DERSequence(v)
    }

    /**
     * Return true if this ExtendedKeyUsage object contains the passed in keyPurposeId.

     * @param keyPurposeId  the KeyPurposeId of interest.
     * *
     * @return true if the keyPurposeId is present, false otherwise.
     */
    fun hasKeyPurposeId(
            keyPurposeId: KeyPurposeId): Boolean {
        return usageTable.get(keyPurposeId) != null
    }

    /**
     * Returns all extended key usages.

     * @return An array with all key purposes.
     */
    val usages: Array<KeyPurposeId>
        get() {
            val temp = arrayOfNulls<KeyPurposeId>(seq.size())

            var i = 0
            val it = seq.objects
            while (it.hasMoreElements()) {
                temp[i++] = KeyPurposeId.getInstance(it.nextElement())
            }
            return temp
        }

    /**
     * Return the number of KeyPurposeIds present in this ExtendedKeyUsage.

     * @return the number of KeyPurposeIds
     */
    fun size(): Int {
        return usageTable.size
    }

    /**
     * Return the ASN.1 primitive form of this object.

     * @return an ASN1Sequence.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        /**
         * Return an ExtendedKeyUsage from the passed in tagged object.

         * @param obj the tagged object containing the ExtendedKeyUsage
         * *
         * @param explicit true if the tagged object should be interpreted as explicitly tagged, false if implicit.
         * *
         * @return the ExtendedKeyUsage contained.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ExtendedKeyUsage {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an ExtendedKeyUsage from the passed in object.

         * @param obj an ExtendedKeyUsage, some form or encoding of one, or null.
         * *
         * @return  an ExtendedKeyUsage object, or null if null is passed in.
         */
        fun getInstance(
                obj: Any?): ExtendedKeyUsage? {
            if (obj is ExtendedKeyUsage) {
                return obj
            } else if (obj != null) {
                return ExtendedKeyUsage(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        /**
         * Retrieve an ExtendedKeyUsage for a passed in Extensions object, if present.

         * @param extensions the extensions object to be examined.
         * *
         * @return  the ExtendedKeyUsage, null if the extension is not present.
         */
        fun fromExtensions(extensions: Extensions): ExtendedKeyUsage {
            return ExtendedKeyUsage.getInstance(extensions.getExtensionParsedValue(Extension.extendedKeyUsage))
        }
    }
}
