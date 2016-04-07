package org.bouncycastle.asn1

import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

import org.bouncycastle.util.io.Streams

/**
 * a general purpose ASN.1 decoder - note: this class differs from the
 * others in that it returns null after it has read the last object in
 * the stream. If an ASN.1 NULL is encountered a DER/BER Null object is
 * returned.
 */
class ASN1InputStream
/**
 * Create an ASN1InputStream where no DER object will be longer than limit, and constructed
 * objects such as sequences will be parsed lazily.

 * @param input stream containing ASN.1 encoded data.
 * *
 * @param limit maximum size of a DER encoded object.
 * *
 * @param lazyEvaluate true if parsing inside constructed objects can be delayed.
 */
@JvmOverloads constructor(
        input: InputStream,
        internal val limit: Int,
        private val lazyEvaluate: Boolean = false) : FilterInputStream(input), BERTags {

    private val tmpBuffers: Array<ByteArray>

    constructor(
            `is`: InputStream) : this(`is`, StreamUtil.findLimit(`is`)) {
    }

    /**
     * Create an ASN1InputStream based on the input byte array. The length of DER objects in
     * the stream is automatically limited to the length of the input array.

     * @param input array containing ASN.1 encoded data.
     */
    constructor(
            input: ByteArray) : this(ByteArrayInputStream(input), input.size) {
    }

    /**
     * Create an ASN1InputStream based on the input byte array. The length of DER objects in
     * the stream is automatically limited to the length of the input array.

     * @param input array containing ASN.1 encoded data.
     * *
     * @param lazyEvaluate true if parsing inside constructed objects can be delayed.
     */
    constructor(
            input: ByteArray,
            lazyEvaluate: Boolean) : this(ByteArrayInputStream(input), input.size, lazyEvaluate) {
    }

    /**
     * Create an ASN1InputStream where no DER object will be longer than limit, and constructed
     * objects such as sequences will be parsed lazily.

     * @param input stream containing ASN.1 encoded data.
     * *
     * @param lazyEvaluate true if parsing inside constructed objects can be delayed.
     */
    constructor(
            input: InputStream,
            lazyEvaluate: Boolean) : this(input, StreamUtil.findLimit(input), lazyEvaluate) {
    }

    init {
        this.tmpBuffers = arrayOfNulls<ByteArray>(11)
    }

    @Throws(IOException::class)
    protected fun readLength(): Int {
        return readLength(this, limit)
    }

    @Throws(IOException::class)
    protected fun readFully(
            bytes: ByteArray) {
        if (Streams.readFully(this, bytes) != bytes.size) {
            throw EOFException("EOF encountered in middle of object")
        }
    }

    /**
     * build an object given its tag and the number of bytes to construct it from.

     * @param tag the full tag details.
     * *
     * @param tagNo the tagNo defined.
     * *
     * @param length the length of the object.
     * *
     * @return the resulting primitive.
     * *
     * @throws java.io.IOException on processing exception.
     */
    @Throws(IOException::class)
    protected fun buildObject(
            tag: Int,
            tagNo: Int,
            length: Int): ASN1Primitive {
        val isConstructed = tag and BERTags.CONSTRUCTED != 0

        val defIn = DefiniteLengthInputStream(this, length)

        if (tag and BERTags.APPLICATION != 0) {
            return DERApplicationSpecific(isConstructed, tagNo, defIn.toByteArray())
        }

        if (tag and BERTags.TAGGED != 0) {
            return ASN1StreamParser(defIn).readTaggedObject(isConstructed, tagNo)
        }

        if (isConstructed) {
            // TODO There are other tags that may be constructed (e.g. BIT_STRING)
            when (tagNo) {
                BERTags.OCTET_STRING -> {
                    //
                    // yes, people actually do this...
                    //
                    val v = buildDEREncodableVector(defIn)
                    val strings = arrayOfNulls<ASN1OctetString>(v.size())

                    for (i in strings.indices) {
                        strings[i] = v.get(i) as ASN1OctetString
                    }

                    return BEROctetString(strings)
                }
                BERTags.SEQUENCE -> {
                    if (lazyEvaluate) {
                        return LazyEncodedSequence(defIn.toByteArray())
                    } else {
                        return DERFactory.createSequence(buildDEREncodableVector(defIn))
                    }
                    return DERFactory.createSet(buildDEREncodableVector(defIn))
                }
                BERTags.SET -> return DERFactory.createSet(buildDEREncodableVector(defIn))
                BERTags.EXTERNAL -> return DERExternal(buildDEREncodableVector(defIn))
                else -> throw IOException("unknown tag $tagNo encountered")
            }
        }

        return createPrimitiveDERObject(tagNo, defIn, tmpBuffers)
    }

    @Throws(IOException::class)
    internal fun buildEncodableVector(): ASN1EncodableVector {
        val v = ASN1EncodableVector()
        var o: ASN1Primitive

        while ((o = readObject()) != null) {
            v.add(o)
        }

        return v
    }

    @Throws(IOException::class)
    internal fun buildDEREncodableVector(
            dIn: DefiniteLengthInputStream): ASN1EncodableVector {
        return ASN1InputStream(dIn).buildEncodableVector()
    }

    @Throws(IOException::class)
    fun readObject(): ASN1Primitive? {
        val tag = read()
        if (tag <= 0) {
            if (tag == 0) {
                throw IOException("unexpected end-of-contents marker")
            }

            return null
        }

        //
        // calculate tag number
        //
        val tagNo = readTagNumber(this, tag)

        val isConstructed = tag and BERTags.CONSTRUCTED != 0

        //
        // calculate length
        //
        val length = readLength()

        if (length < 0)
        // indefinite-length method
        {
            if (!isConstructed) {
                throw IOException("indefinite-length primitive encoding encountered")
            }

            val indIn = IndefiniteLengthInputStream(this, limit)
            val sp = ASN1StreamParser(indIn, limit)

            if (tag and BERTags.APPLICATION != 0) {
                return BERApplicationSpecificParser(tagNo, sp).loadedObject
            }

            if (tag and BERTags.TAGGED != 0) {
                return BERTaggedObjectParser(true, tagNo, sp).loadedObject
            }

            // TODO There are other tags that may be constructed (e.g. BIT_STRING)
            when (tagNo) {
                BERTags.OCTET_STRING -> return BEROctetStringParser(sp).loadedObject
                BERTags.SEQUENCE -> return BERSequenceParser(sp).loadedObject
                BERTags.SET -> return BERSetParser(sp).loadedObject
                BERTags.EXTERNAL -> return DERExternalParser(sp).loadedObject
                else -> throw IOException("unknown BER object encountered")
            }
        } else {
            try {
                return buildObject(tag, tagNo, length)
            } catch (e: IllegalArgumentException) {
                throw ASN1Exception("corrupted stream detected", e)
            }

        }
    }

    companion object {

        @Throws(IOException::class)
        internal fun readTagNumber(s: InputStream, tag: Int): Int {
            var tagNo = tag and 0x1f

            //
            // with tagged object tag number is bottom 5 bits, or stored at the start of the content
            //
            if (tagNo == 0x1f) {
                tagNo = 0

                var b = s.read()

                // X.690-0207 8.1.2.4.2
                // "c) bits 7 to 1 of the first subsequent octet shall not all be zero."
                if (b and 0x7f == 0)
                // Note: -1 will pass
                {
                    throw IOException("corrupted stream - invalid high tag number found")
                }

                while (b >= 0 && b and 0x80 != 0) {
                    tagNo = tagNo or (b and 0x7f)
                    tagNo = tagNo shl 7
                    b = s.read()
                }

                if (b < 0) {
                    throw EOFException("EOF found inside tag value.")
                }

                tagNo = tagNo or (b and 0x7f)
            }

            return tagNo
        }

        @Throws(IOException::class)
        internal fun readLength(s: InputStream, limit: Int): Int {
            var length = s.read()
            if (length < 0) {
                throw EOFException("EOF found when length expected")
            }

            if (length == 0x80) {
                return -1      // indefinite-length encoding
            }

            if (length > 127) {
                val size = length and 0x7f

                // Note: The invalid long form "0xff" (see X.690 8.1.3.5c) will be caught here
                if (size > 4) {
                    throw IOException("DER length more than 4 bytes: " + size)
                }

                length = 0
                for (i in 0..size - 1) {
                    val next = s.read()

                    if (next < 0) {
                        throw EOFException("EOF found reading length")
                    }

                    length = (length shl 8) + next
                }

                if (length < 0) {
                    throw IOException("corrupted stream - negative length found")
                }

                if (length >= limit)
                // after all we must have read at least 1 byte
                {
                    throw IOException("corrupted stream - out of bounds length found")
                }
            }

            return length
        }

        @Throws(IOException::class)
        private fun getBuffer(defIn: DefiniteLengthInputStream, tmpBuffers: Array<ByteArray>): ByteArray {
            val len = defIn.remaining
            if (defIn.remaining < tmpBuffers.size) {
                var buf: ByteArray? = tmpBuffers[len]

                if (buf == null) {
                    buf = tmpBuffers[len] = ByteArray(len)
                }

                Streams.readFully(defIn, buf)

                return buf
            } else {
                return defIn.toByteArray()
            }
        }

        @Throws(IOException::class)
        private fun getBMPCharBuffer(defIn: DefiniteLengthInputStream): CharArray {
            val len = defIn.remaining / 2
            val buf = CharArray(len)
            var totalRead = 0
            while (totalRead < len) {
                val ch1 = defIn.read()
                if (ch1 < 0) {
                    break
                }
                val ch2 = defIn.read()
                if (ch2 < 0) {
                    break
                }
                buf[totalRead++] = (ch1 shl 8 or (ch2 and 0xff)).toChar()
            }

            return buf
        }

        @Throws(IOException::class)
        internal fun createPrimitiveDERObject(
                tagNo: Int,
                defIn: DefiniteLengthInputStream,
                tmpBuffers: Array<ByteArray>): ASN1Primitive {
            when (tagNo) {
                BERTags.BIT_STRING -> return ASN1BitString.fromInputStream(defIn.remaining, defIn)
                BERTags.BMP_STRING -> return DERBMPString(getBMPCharBuffer(defIn))
                BERTags.BOOLEAN -> return ASN1Boolean.fromOctetString(getBuffer(defIn, tmpBuffers))
                BERTags.ENUMERATED -> return ASN1Enumerated.fromOctetString(getBuffer(defIn, tmpBuffers))
                BERTags.GENERALIZED_TIME -> return ASN1GeneralizedTime(defIn.toByteArray())
                BERTags.GENERAL_STRING -> return DERGeneralString(defIn.toByteArray())
                BERTags.IA5_STRING -> return DERIA5String(defIn.toByteArray())
                BERTags.INTEGER -> return ASN1Integer(defIn.toByteArray(), false)
                BERTags.NULL -> return DERNull.INSTANCE   // actual content is ignored (enforce 0 length?)
                BERTags.NUMERIC_STRING -> return DERNumericString(defIn.toByteArray())
                BERTags.OBJECT_IDENTIFIER -> return ASN1ObjectIdentifier.fromOctetString(getBuffer(defIn, tmpBuffers))
                BERTags.OCTET_STRING -> return DEROctetString(defIn.toByteArray())
                BERTags.PRINTABLE_STRING -> return DERPrintableString(defIn.toByteArray())
                BERTags.T61_STRING -> return DERT61String(defIn.toByteArray())
                BERTags.UNIVERSAL_STRING -> return DERUniversalString(defIn.toByteArray())
                BERTags.UTC_TIME -> return ASN1UTCTime(defIn.toByteArray())
                BERTags.UTF8_STRING -> return DERUTF8String(defIn.toByteArray())
                BERTags.VISIBLE_STRING -> return DERVisibleString(defIn.toByteArray())
                BERTags.GRAPHIC_STRING -> return DERGraphicString(defIn.toByteArray())
                BERTags.VIDEOTEX_STRING -> return DERVideotexString(defIn.toByteArray())
                else -> throw IOException("unknown tag $tagNo encountered")
            }
        }
    }
}
/**
 * Create an ASN1InputStream where no DER object will be longer than limit.

 * @param input stream containing ASN.1 encoded data.
 * *
 * @param limit maximum size of a DER encoded object.
 */
