package org.bouncycastle.asn1

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * A parser for ASN.1 streams which also returns, where possible, parsers for the objects it encounters.
 */
class ASN1StreamParser @JvmOverloads constructor(
        private val _in: InputStream,
        private val _limit: Int = StreamUtil.findLimit(_in)) {
    private val tmpBuffers: Array<ByteArray>

    init {

        this.tmpBuffers = arrayOfNulls<ByteArray>(11)
    }

    constructor(
            encoding: ByteArray) : this(ByteArrayInputStream(encoding), encoding.size) {
    }

    @Throws(IOException::class)
    internal fun readIndef(tagValue: Int): ASN1Encodable {
        // Note: INDEF => CONSTRUCTED

        // TODO There are other tags that may be constructed (e.g. BIT_STRING)
        when (tagValue) {
            BERTags.EXTERNAL -> return DERExternalParser(this)
            BERTags.OCTET_STRING -> return BEROctetStringParser(this)
            BERTags.SEQUENCE -> return BERSequenceParser(this)
            BERTags.SET -> return BERSetParser(this)
            else -> throw ASN1Exception("unknown BER object encountered: 0x" + Integer.toHexString(tagValue))
        }
    }

    @Throws(IOException::class)
    internal fun readImplicit(constructed: Boolean, tag: Int): ASN1Encodable {
        if (_in is IndefiniteLengthInputStream) {
            if (!constructed) {
                throw IOException("indefinite-length primitive encoding encountered")
            }

            return readIndef(tag)
        }

        if (constructed) {
            when (tag) {
                BERTags.SET -> return DERSetParser(this)
                BERTags.SEQUENCE -> return DERSequenceParser(this)
                BERTags.OCTET_STRING -> return BEROctetStringParser(this)
            }
        } else {
            when (tag) {
                BERTags.SET -> throw ASN1Exception("sequences must use constructed encoding (see X.690 8.9.1/8.10.1)")
                BERTags.SEQUENCE -> throw ASN1Exception("sets must use constructed encoding (see X.690 8.11.1/8.12.1)")
                BERTags.OCTET_STRING -> return DEROctetStringParser(_in as DefiniteLengthInputStream)
            }
        }

        throw ASN1Exception("implicit tagging not implemented")
    }

    @Throws(IOException::class)
    internal fun readTaggedObject(constructed: Boolean, tag: Int): ASN1Primitive {
        if (!constructed) {
            // Note: !CONSTRUCTED => IMPLICIT
            val defIn = _in as DefiniteLengthInputStream
            return DERTaggedObject(false, tag, DEROctetString(defIn.toByteArray()))
        }

        val v = readVector()

        if (_in is IndefiniteLengthInputStream) {
            return if (v.size() == 1)
                BERTaggedObject(true, tag, v.get(0))
            else
                BERTaggedObject(false, tag, BERFactory.createSequence(v))
        }

        return if (v.size() == 1)
            DERTaggedObject(true, tag, v.get(0))
        else
            DERTaggedObject(false, tag, DERFactory.createSequence(v))
    }

    @Throws(IOException::class)
    fun readObject(): ASN1Encodable? {
        val tag = _in.read()
        if (tag == -1) {
            return null
        }

        //
        // turn of looking for "00" while we resolve the tag
        //
        set00Check(false)

        //
        // calculate tag number
        //
        val tagNo = ASN1InputStream.readTagNumber(_in, tag)

        val isConstructed = tag and BERTags.CONSTRUCTED != 0

        //
        // calculate length
        //
        val length = ASN1InputStream.readLength(_in, _limit)

        if (length < 0)
        // indefinite-length method
        {
            if (!isConstructed) {
                throw IOException("indefinite-length primitive encoding encountered")
            }

            val indIn = IndefiniteLengthInputStream(_in, _limit)
            val sp = ASN1StreamParser(indIn, _limit)

            if (tag and BERTags.APPLICATION != 0) {
                return BERApplicationSpecificParser(tagNo, sp)
            }

            if (tag and BERTags.TAGGED != 0) {
                return BERTaggedObjectParser(true, tagNo, sp)
            }

            return sp.readIndef(tagNo)
        } else {
            val defIn = DefiniteLengthInputStream(_in, length)

            if (tag and BERTags.APPLICATION != 0) {
                return DERApplicationSpecific(isConstructed, tagNo, defIn.toByteArray())
            }

            if (tag and BERTags.TAGGED != 0) {
                return BERTaggedObjectParser(isConstructed, tagNo, ASN1StreamParser(defIn))
            }

            if (isConstructed) {
                // TODO There are other tags that may be constructed (e.g. BIT_STRING)
                when (tagNo) {
                    BERTags.OCTET_STRING -> //
                        // yes, people actually do this...
                        //
                        return BEROctetStringParser(ASN1StreamParser(defIn))
                    BERTags.SEQUENCE -> return DERSequenceParser(ASN1StreamParser(defIn))
                    BERTags.SET -> return DERSetParser(ASN1StreamParser(defIn))
                    BERTags.EXTERNAL -> return DERExternalParser(ASN1StreamParser(defIn))
                    else -> throw IOException("unknown tag $tagNo encountered")
                }
            }

            // Some primitive encodings can be handled by parsers too...
            when (tagNo) {
                BERTags.OCTET_STRING -> return DEROctetStringParser(defIn)
            }

            try {
                return ASN1InputStream.createPrimitiveDERObject(tagNo, defIn, tmpBuffers)
            } catch (e: IllegalArgumentException) {
                throw ASN1Exception("corrupted stream detected", e)
            }

        }
    }

    private fun set00Check(enabled: Boolean) {
        if (_in is IndefiniteLengthInputStream) {
            _in.setEofOn00(enabled)
        }
    }

    @Throws(IOException::class)
    internal fun readVector(): ASN1EncodableVector {
        val v = ASN1EncodableVector()

        var obj: ASN1Encodable
        while ((obj = readObject()) != null) {
            if (obj is InMemoryRepresentable) {
                v.add(obj.loadedObject)
            } else {
                v.add(obj.toASN1Primitive())
            }
        }

        return v
    }
}
