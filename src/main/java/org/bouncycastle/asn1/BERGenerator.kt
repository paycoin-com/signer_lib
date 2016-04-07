package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

/**
 * Base class for generators for indefinite-length structures.
 */
open class BERGenerator : ASN1Generator {
    private var _tagged = false
    private val _isExplicit: Boolean
    private val _tagNo: Int

    protected constructor(
            out: OutputStream) : super(out) {
    }

    protected constructor(
            out: OutputStream,
            tagNo: Int,
            isExplicit: Boolean) : super(out) {

        _tagged = true
        _isExplicit = isExplicit
        _tagNo = tagNo
    }

    override val rawOutputStream: OutputStream
        get() = _out

    @Throws(IOException::class)
    private fun writeHdr(
            tag: Int) {
        _out.write(tag)
        _out.write(0x80)
    }

    @Throws(IOException::class)
    protected fun writeBERHeader(
            tag: Int) {
        if (_tagged) {
            val tagNum = _tagNo or BERTags.TAGGED

            if (_isExplicit) {
                writeHdr(tagNum or BERTags.CONSTRUCTED)
                writeHdr(tag)
            } else {
                if (tag and BERTags.CONSTRUCTED != 0) {
                    writeHdr(tagNum or BERTags.CONSTRUCTED)
                } else {
                    writeHdr(tagNum)
                }
            }
        } else {
            writeHdr(tag)
        }
    }

    @Throws(IOException::class)
    protected fun writeBEREnd() {
        _out.write(0x00)
        _out.write(0x00)

        if (_tagged && _isExplicit)
        // write extra end for tag header
        {
            _out.write(0x00)
            _out.write(0x00)
        }
    }
}
