package org.bouncycastle.asn1

import java.io.IOException

/**
 * ASN.1 TaggedObject - in ASN.1 notation this is any object preceded by
 * a [n] where n is some number - these are assumed to follow the construction
 * rules (as with sequences).
 */
abstract class ASN1TaggedObject
/**
 * Create a tagged object with the style given by the value of explicit.
 *
 *
 * If the object implements ASN1Choice the tag style will always be changed
 * to explicit in accordance with the ASN.1 encoding rules.
 *
 * @param explicit true if the object is explicitly tagged.
 * *
 * @param tagNo the tag number for this object.
 * *
 * @param obj the tagged object.
 */
(
        explicit: Boolean,
        tagNo: Int,
        obj: ASN1Encodable) : ASN1Primitive(), ASN1TaggedObjectParser {
    override var tagNo: Int = 0
        internal set(value: Int) {
            super.tagNo = value
        }
    var isEmpty = false
        internal set
    /**
     * return whether or not the object may be explicitly tagged.
     *
     *
     * Note: if the object has been read from an input stream, the only
     * time you can be sure if isExplicit is returning the true state of
     * affairs is if it returns false. An implicitly tagged object may appear
     * to be explicitly tagged, so you need to understand the context under
     * which the reading was done as well, see getObject below.
     */
    var isExplicit = true
        internal set
    internal var obj: ASN1Encodable? = null

    init {
        if (obj is ASN1Choice) {
            this.isExplicit = true
        } else {
            this.isExplicit = explicit
        }

        this.tagNo = tagNo

        if (this.isExplicit) {
            this.obj = obj
        } else {
            val prim = obj.toASN1Primitive()

            if (prim is ASN1Set) {
                val s: ASN1Set? = null
            }

            this.obj = obj
        }
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1TaggedObject) {
            return false
        }

        if (tagNo != o.tagNo || isEmpty != o.isEmpty || isExplicit != o.isExplicit) {
            return false
        }

        if (obj == null) {
            if (o.obj != null) {
                return false
            }
        } else {
            if (obj!!.toASN1Primitive() != o.obj!!.toASN1Primitive()) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var code = tagNo

        // TODO: actually this is wrong - the problem is that a re-encoded
        // object may end up with a different hashCode due to implicit
        // tagging. As implicit tagging is ambiguous if a sequence is involved
        // it seems the only correct method for both equals and hashCode is to
        // compare the encodings...
        if (obj != null) {
            code = code xor obj!!.hashCode()
        }

        return code
    }

    /**
     * return whatever was following the tag.
     *
     *
     * Note: tagged objects are generally context dependent if you're
     * trying to extract a tagged object you should be going via the
     * appropriate getInstance method.
     */
    val `object`: ASN1Primitive?
        get() {
            if (obj != null) {
                return obj!!.toASN1Primitive()
            }

            return null
        }

    /**
     * Return the object held in this tagged object as a parser assuming it has
     * the type of the passed in tag. If the object doesn't have a parser
     * associated with it, the base object is returned.
     */
    @Throws(IOException::class)
    override fun getObjectParser(
            tag: Int,
            isExplicit: Boolean): ASN1Encodable {
        when (tag) {
            BERTags.SET -> return ASN1Set.getInstance(this, isExplicit).parser()
            BERTags.SEQUENCE -> return ASN1Sequence.getInstance(this, isExplicit).parser()
            BERTags.OCTET_STRING -> return ASN1OctetString.getInstance(this, isExplicit).parser()
        }

        if (isExplicit) {
            return `object`
        }

        throw ASN1Exception("implicit tagging not implemented for tag: " + tag)
    }

    override val loadedObject: ASN1Primitive
        get() = this.toASN1Primitive()

    internal override fun toDERObject(): ASN1Primitive {
        return DERTaggedObject(isExplicit, tagNo, obj)
    }

    internal override fun toDLObject(): ASN1Primitive {
        return DLTaggedObject(isExplicit, tagNo, obj)
    }

    @Throws(IOException::class)
    internal abstract override fun encode(out: ASN1OutputStream)

    override fun toString(): String {
        return "[$tagNo]$obj"
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1TaggedObject {
            if (explicit) {
                return obj.`object` as ASN1TaggedObject?
            }

            throw IllegalArgumentException("implicitly tagged tagged object")
        }

        fun getInstance(
                obj: Any?): ASN1TaggedObject {
            if (obj == null || obj is ASN1TaggedObject) {
                return obj as ASN1TaggedObject?
            } else if (obj is ByteArray) {
                try {
                    return ASN1TaggedObject.getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct tagged object from byte[]: " + e.message)
                }

            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }
    }
}
