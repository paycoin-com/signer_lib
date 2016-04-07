package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.util.Arrays

/**
 * ASN.1 `SEQUENCE` and `SEQUENCE OF` constructs.
 *
 *
 * DER form is always definite form length fields, while
 * BER support uses indefinite form.
 *
 *
 * **X.690**
 *
 * **8: Basic encoding rules**
 *
 * **8.9 Encoding of a sequence value **
 * 8.9.1 The encoding of a sequence value shall be constructed.
 *
 *
 * **8.9.2** The contents octets shall consist of the complete
 * encoding of one data value from each of the types listed in
 * the ASN.1 definition of the sequence type, in the order of
 * their appearance in the definition, unless the type was referenced
 * with the keyword **OPTIONAL** or the keyword **DEFAULT**.
 *
 *
 * **8.9.3** The encoding of a data value may, but need not,
 * be present for a type which was referenced with the keyword
 * **OPTIONAL** or the keyword **DEFAULT**.
 * If present, it shall appear in the encoding at the point
 * corresponding to the appearance of the type in the ASN.1 definition.
 *
 *
 * **8.10 Encoding of a sequence-of value **
 *
 *
 * **8.10.1** The encoding of a sequence-of value shall be constructed.
 *
 *
 * **8.10.2** The contents octets shall consist of zero,
 * one or more complete encodings of data values from the type listed in
 * the ASN.1 definition.
 *
 *
 * **8.10.3** The order of the encodings of the data values shall be
 * the same as the order of the data values in the sequence-of value to
 * be encoded.
 *
 *
 * **9: Canonical encoding rules**
 *
 * **9.1 Length forms**
 * If the encoding is constructed, it shall employ the indefinite-length form.
 * If the encoding is primitive, it shall include the fewest length octets necessary.
 * [Contrast with 8.1.3.2 b).]

 *
 * **11: Restrictions on BER employed by both CER and DER**
 *
 * **11.5 Set and sequence components with default value**
 * The encoding of a set value or sequence value shall not include
 * an encoding for any component value which is equal to
 * its default value.
 */
abstract class ASN1Sequence : ASN1Primitive, org.bouncycastle.util.Iterable<ASN1Encodable> {
    protected var seq = Vector()

    /**
     * Create an empty sequence
     */
    protected constructor() {
    }

    /**
     * Create a sequence containing one object
     * @param obj the object to be put in the SEQUENCE.
     */
    protected constructor(
            obj: ASN1Encodable) {
        seq.addElement(obj)
    }

    /**
     * Create a sequence containing a vector of objects.
     * @param v the vector of objects to be put in the SEQUENCE
     */
    protected constructor(
            v: ASN1EncodableVector) {
        for (i in 0..v.size() - 1) {
            seq.addElement(v.get(i))
        }
    }

    /*
     * Create a sequence containing a vector of objects.
     */
    protected constructor(
            array: Array<ASN1Encodable>) {
        for (i in array.indices) {
            seq.addElement(array[i])
        }
    }

    fun toArray(): Array<ASN1Encodable> {
        val values = arrayOfNulls<ASN1Encodable>(this.size())

        for (i in 0..this.size() - 1) {
            values[i] = this.getObjectAt(i)
        }

        return values
    }

    val objects: Enumeration<Any>
        get() = seq.elements()

    fun parser(): ASN1SequenceParser {
        val outer = this

        return object : ASN1SequenceParser {
            private val max = size()

            private var index: Int = 0

            @Throws(IOException::class)
            override fun readObject(): ASN1Encodable? {
                if (index == max) {
                    return null
                }

                val obj = getObjectAt(index++)
                if (obj is ASN1Sequence) {
                    return obj.parser()
                }
                if (obj is ASN1Set) {
                    return obj.parser()
                }

                return obj
            }

            override val loadedObject: ASN1Primitive
                get() = outer

            override fun toASN1Primitive(): ASN1Primitive {
                return outer
            }
        }
    }

    /**
     * Return the object at the sequence position indicated by index.

     * @param index the sequence number (starting at zero) of the object
     * *
     * @return the object at the sequence position indicated by index.
     */
    open fun getObjectAt(
            index: Int): ASN1Encodable {
        return seq.elementAt(index)
    }

    /**
     * Return the number of objects in this sequence.

     * @return the number of objects in this sequence.
     */
    open fun size(): Int {
        return seq.size
    }

    override fun hashCode(): Int {
        val e = this.objects
        var hashCode = size()

        while (e.hasMoreElements()) {
            val o = getNext(e)
            hashCode *= 17

            hashCode = hashCode xor o.hashCode()
        }

        return hashCode
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1Sequence) {
            return false
        }

        if (this.size() != o.size()) {
            return false
        }

        val s1 = this.objects
        val s2 = o.objects

        while (s1.hasMoreElements()) {
            val obj1 = getNext(s1)
            val obj2 = getNext(s2)

            val o1 = obj1.toASN1Primitive()
            val o2 = obj2.toASN1Primitive()

            if (o1 === o2 || o1 == o2) {
                continue
            }

            return false
        }

        return true
    }

    private fun getNext(e: Enumeration<Any>): ASN1Encodable {
        val encObj = e.nextElement() as ASN1Encodable

        return encObj
    }

    /**
     * Change current SEQUENCE object to be encoded as [DERSequence].
     * This is part of Distinguished Encoding Rules form serialization.
     */
    internal override fun toDERObject(): ASN1Primitive {
        val derSeq = DERSequence()

        derSeq.seq = this.seq

        return derSeq
    }

    /**
     * Change current SEQUENCE object to be encoded as [DLSequence].
     * This is part of Direct Length form serialization.
     */
    internal override fun toDLObject(): ASN1Primitive {
        val dlSeq = DLSequence()

        dlSeq.seq = this.seq

        return dlSeq
    }

    internal override val isConstructed: Boolean
        get() = true

    @Throws(IOException::class)
    internal abstract override fun encode(out: ASN1OutputStream)

    override fun toString(): String {
        return seq.toString()
    }

    override fun iterator(): Iterator<ASN1Encodable> {
        return Arrays.Iterator(toArray())
    }

    companion object {

        /**
         * Return an ASN1Sequence from the given object.

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1Sequence instance, or null.
         */
        fun getInstance(
                obj: Any?): ASN1Sequence {
            if (obj == null || obj is ASN1Sequence) {
                return obj as ASN1Sequence?
            } else if (obj is ASN1SequenceParser) {
                return ASN1Sequence.getInstance(obj.toASN1Primitive())
            } else if (obj is ByteArray) {
                try {
                    return ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct sequence from byte[]: " + e.message)
                }

            } else if (obj is ASN1Encodable) {
                val primitive = obj.toASN1Primitive()

                if (primitive is ASN1Sequence) {
                    return primitive
                }
            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }

        /**
         * Return an ASN1 sequence from a tagged object. There is a special
         * case here, if an object appears to have been explicitly tagged on
         * reading but we were expecting it to be implicitly tagged in the
         * normal course of events it indicates that we lost the surrounding
         * sequence - so we need to add it back (this will happen if the tagged
         * object is a sequence that contains other sequences). If you are
         * dealing with implicitly tagged sequences you really **should**
         * be using this method.

         * @param obj the tagged object.
         * *
         * @param explicit true if the object is meant to be explicitly tagged,
         * *          false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *          be converted.
         * *
         * @return an ASN1Sequence instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1Sequence {
            if (explicit) {
                if (!obj.isExplicit) {
                    throw IllegalArgumentException("object implicit - explicit expected.")
                }

                return ASN1Sequence.getInstance(obj.`object`!!.toASN1Primitive())
            } else {
                //
                // constructed object which appears to be explicitly tagged
                // when it should be implicit means we have to add the
                // surrounding sequence.
                //
                if (obj.isExplicit) {
                    if (obj is BERTaggedObject) {
                        return BERSequence(obj.`object`)
                    } else {
                        return DLSequence(obj.`object`)
                    }
                } else {
                    if (obj.`object` is ASN1Sequence) {
                        return obj.`object` as ASN1Sequence?
                    }
                }
            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }
    }
}
