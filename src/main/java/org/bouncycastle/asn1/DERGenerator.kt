package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Basic class for streaming DER encoding generators.
 */
abstract class DERGenerator : ASN1Generator {
    private var _tagged = false
    private val _isExplicit: Boolean
    private val _tagNo: Int

    protected constructor(
            out: OutputStream) : super(out) {
    }

    /**
     * Create a DER encoding generator for a tagged object.

     * @param out the output stream to encode objects to.
     * *
     * @param tagNo the tag number to head the output stream with.
     * *
     * @param isExplicit true if the tagging should be explicit, false otherwise.
     */
    constructor(
            out: OutputStream,
            tagNo: Int,
            isExplicit: Boolean) : super(out) {

        _tagged = true
        _isExplicit = isExplicit
        _tagNo = tagNo
    }

    @Throws(IOException::class)
    private fun writeLength(
            out: OutputStream,
            length: Int) {
        if (length > 127) {
            var size = 1
            var `val` = length

            while ((`val` = `val` ushr 8) != 0) {
                size++
            }

            out.write((size or 0x80).toByte().toInt())

            var i = (size - 1) * 8
            while (i >= 0) {
                out.write((length shr i).toByte().toInt())
                i -= 8
            }
        } else {
            out.write(length.toByte().toInt())
        }
    }

    @Throws(IOException::class)
    internal fun writeDEREncoded(
            out: OutputStream,
            tag: Int,
            bytes: ByteArray) {
        out.write(tag)
        writeLength(out, bytes.size)
        out.write(bytes)
    }

    @Throws(IOException::class)
    internal fun writeDEREncoded(
            tag: Int,
            bytes: ByteArray) {
        if (_tagged) {
            val tagNum = _tagNo or BERTags.TAGGED

            if (_isExplicit) {
                val newTag = _tagNo or BERTags.CONSTRUCTED or BERTags.TAGGED

                val bOut = ByteArrayOutputStream()

                writeDEREncoded(bOut, tag, bytes)

                writeDEREncoded(_out, newTag, bOut.toByteArray())
            } else {
                if (tag and BERTags.CONSTRUCTED != 0) {
                    writeDEREncoded(_out, tagNum or BERTags.CONSTRUCTED, bytes)
                } else {
                    writeDEREncoded(_out, tagNum, bytes)
                }
            }
        } else {
            writeDEREncoded(_out, tag, bytes)
        }
    }
}
