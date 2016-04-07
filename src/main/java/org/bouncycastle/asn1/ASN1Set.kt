package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.util.Arrays

/**
 * ASN.1 `SET` and `SET OF` constructs.
 *
 *
 * Note: This does not know which syntax the set is!
 * (The difference: ordering of SET elements or not ordering.)
 *
 *
 * DER form is always definite form length fields, while
 * BER support uses indefinite form.
 *
 *
 * The CER form support does not exist.
 *
 *
 *
 * X.690
 * 8: Basic encoding rules
 * 8.11 Encoding of a set value
 * **8.11.1** The encoding of a set value shall be constructed
 *
 *
 * **8.11.2** The contents octets shall consist of the complete
 * encoding of a data value from each of the types listed in the
 * ASN.1 definition of the set type, in an order chosen by the sender,
 * unless the type was referenced with the keyword
 * **OPTIONAL** or the keyword **DEFAULT**.
 *
 *
 * **8.11.3** The encoding of a data value may, but need not,
 * be present for a type which was referenced with the keyword
 * **OPTIONAL** or the keyword **DEFAULT**.
 *
 * NOTE  The order of data values in a set value is not significant,
 * and places no constraints on the order during transfer
 *
 * 8.12 Encoding of a set-of value
 * **8.12.1** The encoding of a set-of value shall be constructed.
 *
 *
 * **8.12.2** The text of 8.10.2 applies:
 * *The contents octets shall consist of zero,
 * one or more complete encodings of data values from the type listed in
 * the ASN.1 definition.*
 *
 *
 * **8.12.3** The order of data values need not be preserved by
 * the encoding and subsequent decoding.

 * 9: Canonical encoding rules
 * 9.1 Length forms
 * If the encoding is constructed, it shall employ the indefinite-length form.
 * If the encoding is primitive, it shall include the fewest length octets necessary.
 * [Contrast with 8.1.3.2 b).]
 * 9.3 Set components
 * The encodings of the component values of a set value shall
 * appear in an order determined by their tags as specified
 * in 8.6 of ITU-T Rec. X.680 | ISO/IEC 8824-1.
 * Additionally, for the purposes of determining the order in which
 * components are encoded when one or more component is an untagged
 * choice type, each untagged choice type is ordered as though it
 * has a tag equal to that of the smallest tag in that choice type
 * or any untagged choice types nested within.

 * 10: Distinguished encoding rules
 * 10.1 Length forms
 * The definite form of length encoding shall be used,
 * encoded in the minimum number of octets.
 * [Contrast with 8.1.3.2 b).]
 * 10.3 Set components
 * The encodings of the component values of a set value shall appear
 * in an order determined by their tags as specified
 * in 8.6 of ITU-T Rec. X.680 | ISO/IEC 8824-1.
 *
 * NOTE  Where a component of the set is an untagged choice type,
 * the location of that component in the ordering will depend on
 * the tag of the choice component being encoded.
 *

 * 11: Restrictions on BER employed by both CER and DER
 * 11.5 Set and sequence components with default value
 * The encoding of a set value or sequence value shall not include
 * an encoding for any component value which is equal to
 * its default value.
 * 11.6 Set-of components
 *
 *
 * The encodings of the component values of a set-of value
 * shall appear in ascending order, the encodings being compared
 * as octet strings with the shorter components being padded at
 * their trailing end with 0-octets.
 *
 * NOTE  The padding octets are for comparison purposes only
 * and do not appear in the encodings.
 *
 */
abstract class ASN1Set : ASN1Primitive, org.bouncycastle.util.Iterable<ASN1Encodable> {
    private var set = Vector()
    private var isSorted = false

    protected constructor() {
    }

    /**
     * create a sequence containing one object
     * @param obj object to be added to the SET.
     */
    protected constructor(
            obj: ASN1Encodable) {
        set.addElement(obj)
    }

    /**
     * create a sequence containing a vector of objects.
     * @param v a vector of objects to make up the SET.
     * *
     * @param doSort true if should be sorted DER style, false otherwise.
     */
    protected constructor(
            v: ASN1EncodableVector,
            doSort: Boolean) {
        for (i in 0..v.size() - 1) {
            set.addElement(v.get(i))
        }

        if (doSort) {
            this.sort()
        }
    }

    /*
     * create a sequence containing a vector of objects.
     */
    protected constructor(
            array: Array<ASN1Encodable>,
            doSort: Boolean) {
        for (i in array.indices) {
            set.addElement(array[i])
        }

        if (doSort) {
            this.sort()
        }
    }

    val objects: Enumeration<Any>
        get() = set.elements()

    /**
     * return the object at the set position indicated by index.

     * @param index the set number (starting at zero) of the object
     * *
     * @return the object at the set position indicated by index.
     */
    fun getObjectAt(
            index: Int): ASN1Encodable {
        return set.elementAt(index)
    }

    /**
     * return the number of objects in this set.

     * @return the number of objects in this set.
     */
    fun size(): Int {
        return set.size
    }

    fun toArray(): Array<ASN1Encodable> {
        val values = arrayOfNulls<ASN1Encodable>(this.size())

        for (i in 0..this.size() - 1) {
            values[i] = this.getObjectAt(i)
        }

        return values
    }

    fun parser(): ASN1SetParser {
        val outer = this

        return object : ASN1SetParser {
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

    /**
     * Change current SET object to be encoded as [DERSet].
     * This is part of Distinguished Encoding Rules form serialization.
     */
    internal override fun toDERObject(): ASN1Primitive {
        if (isSorted) {
            val derSet = DERSet()

            derSet.set = this.set

            return derSet
        } else {
            val v = Vector()

            for (i in set.indices) {
                v.addElement(set.elementAt(i))
            }

            val derSet = DERSet()

            derSet.set = v

            derSet.sort()

            return derSet
        }
    }

    /**
     * Change current SET object to be encoded as [DLSet].
     * This is part of Direct Length form serialization.
     */
    internal override fun toDLObject(): ASN1Primitive {
        val derSet = DLSet()

        derSet.set = this.set

        return derSet
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1Set) {
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
        val encObj = e.nextElement() as ASN1Encodable ?: return DERNull.INSTANCE

        // unfortunately null was allowed as a substitute for DER null

        return encObj
    }

    /**
     * return true if a <= b (arrays are assumed padded with zeros).
     */
    private fun lessThanOrEqual(
            a: ByteArray,
            b: ByteArray): Boolean {
        val len = Math.min(a.size, b.size)
        for (i in 0..len - 1) {
            if (a[i] != b[i]) {
                return a[i] and 0xff < b[i] and 0xff
            }
        }
        return len == a.size
    }

    private fun getDEREncoded(
            obj: ASN1Encodable): ByteArray {
        try {
            return obj.toASN1Primitive().getEncoded(ASN1Encoding.DER)
        } catch (e: IOException) {
            throw IllegalArgumentException("cannot encode object added to SET")
        }

    }

    protected fun sort() {
        if (!isSorted) {
            isSorted = true
            if (set.size > 1) {
                var swapped = true
                var lastSwap = set.size - 1

                while (swapped) {
                    var index = 0
                    var swapIndex = 0
                    var a = getDEREncoded(set.elementAt(0) as ASN1Encodable)

                    swapped = false

                    while (index != lastSwap) {
                        val b = getDEREncoded(set.elementAt(index + 1) as ASN1Encodable)

                        if (lessThanOrEqual(a, b)) {
                            a = b
                        } else {
                            val o = set.elementAt(index)

                            set.setElementAt(set.elementAt(index + 1), index)
                            set.setElementAt(o, index + 1)

                            swapped = true
                            swapIndex = index
                        }

                        index++
                    }

                    lastSwap = swapIndex
                }
            }
        }
    }

    internal override val isConstructed: Boolean
        get() = true

    @Throws(IOException::class)
    internal abstract override fun encode(out: ASN1OutputStream)

    override fun toString(): String {
        return set.toString()
    }

    override fun iterator(): Iterator<ASN1Encodable> {
        return Arrays.Iterator(toArray())
    }

    companion object {

        /**
         * return an ASN1Set from the given object.

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1Set instance, or null.
         */
        fun getInstance(
                obj: Any?): ASN1Set {
            if (obj == null || obj is ASN1Set) {
                return obj as ASN1Set?
            } else if (obj is ASN1SetParser) {
                return ASN1Set.getInstance(obj.toASN1Primitive())
            } else if (obj is ByteArray) {
                try {
                    return ASN1Set.getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct set from byte[]: " + e.message)
                }

            } else if (obj is ASN1Encodable) {
                val primitive = obj.toASN1Primitive()

                if (primitive is ASN1Set) {
                    return primitive
                }
            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }

        /**
         * Return an ASN1 set from a tagged object. There is a special
         * case here, if an object appears to have been explicitly tagged on
         * reading but we were expecting it to be implicitly tagged in the
         * normal course of events it indicates that we lost the surrounding
         * set - so we need to add it back (this will happen if the tagged
         * object is a sequence that contains other sequences). If you are
         * dealing with implicitly tagged sets you really **should**
         * be using this method.

         * @param obj the tagged object.
         * *
         * @param explicit true if the object is meant to be explicitly tagged
         * *          false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *          be converted.
         * *
         * @return an ASN1Set instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1Set {
            if (explicit) {
                if (!obj.isExplicit) {
                    throw IllegalArgumentException("object implicit - explicit expected.")
                }

                return obj.`object` as ASN1Set?
            } else {
                //
                // constructed object which appears to be explicitly tagged
                // and it's really implicit means we have to add the
                // surrounding set.
                //
                if (obj.isExplicit) {
                    if (obj is BERTaggedObject) {
                        return BERSet(obj.`object`)
                    } else {
                        return DLSet(obj.`object`)
                    }
                } else {
                    if (obj.`object` is ASN1Set) {
                        return obj.`object` as ASN1Set?
                    }

                    //
                    // in this case the parser returns a sequence, convert it
                    // into a set.
                    //
                    if (obj.`object` is ASN1Sequence) {
                        val s = obj.`object` as ASN1Sequence?

                        if (obj is BERTaggedObject) {
                            return BERSet(s.toArray())
                        } else {
                            return DLSet(s.toArray())
                        }
                    }
                }
            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }
    }
}
