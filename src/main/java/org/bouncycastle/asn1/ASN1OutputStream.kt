package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

/**
 * Stream that produces output based on the default encoding for the passed in objects.
 */
open class ASN1OutputStream(
        private val os: OutputStream) {

    @Throws(IOException::class)
    internal fun writeLength(
            length: Int) {
        if (length > 127) {
            var size = 1
            var `val` = length

            while ((`val` = `val` ushr 8) != 0) {
                size++
            }

            write((size or 0x80).toByte().toInt())

            var i = (size - 1) * 8
            while (i >= 0) {
                write((length shr i).toByte().toInt())
                i -= 8
            }
        } else {
            write(length.toByte().toInt())
        }
    }

    @Throws(IOException::class)
    internal open fun write(b: Int) {
        os.write(b)
    }

    @Throws(IOException::class)
    internal fun write(bytes: ByteArray) {
        os.write(bytes)
    }

    @Throws(IOException::class)
    internal fun write(bytes: ByteArray, off: Int, len: Int) {
        os.write(bytes, off, len)
    }

    @Throws(IOException::class)
    internal fun writeEncoded(
            tag: Int,
            bytes: ByteArray) {
        write(tag)
        writeLength(bytes.size)
        write(bytes)
    }

    @Throws(IOException::class)
    internal fun writeTag(flags: Int, tagNo: Int) {
        var tagNo = tagNo
        if (tagNo < 31) {
            write(flags or tagNo)
        } else {
            write(flags or 0x1f)
            if (tagNo < 128) {
                write(tagNo)
            } else {
                val stack = ByteArray(5)
                var pos = stack.size

                stack[--pos] = (tagNo and 0x7F).toByte()

                do {
                    tagNo = tagNo shr 7
                    stack[--pos] = (tagNo and 0x7F or 0x80).toByte()
                } while (tagNo > 127)

                write(stack, pos, stack.size - pos)
            }
        }
    }

    @Throws(IOException::class)
    internal fun writeEncoded(flags: Int, tagNo: Int, bytes: ByteArray) {
        writeTag(flags, tagNo)
        writeLength(bytes.size)
        write(bytes)
    }

    @Throws(IOException::class)
    protected fun writeNull() {
        os.write(BERTags.NULL)
        os.write(0x00)
    }

    @Throws(IOException::class)
    open fun writeObject(
            obj: ASN1Encodable?) {
        if (obj != null) {
            obj.toASN1Primitive().encode(this)
        } else {
            throw IOException("null object detected")
        }
    }

    @Throws(IOException::class)
    internal fun writeImplicitObject(obj: ASN1Primitive?) {
        if (obj != null) {
            obj.encode(ImplicitOutputStream(os))
        } else {
            throw IOException("null object detected")
        }
    }

    @Throws(IOException::class)
    fun close() {
        os.close()
    }

    @Throws(IOException::class)
    fun flush() {
        os.flush()
    }

    internal open val derSubStream: ASN1OutputStream
        get() = DEROutputStream(os)

    internal open val dlSubStream: ASN1OutputStream
        get() = DLOutputStream(os)

    private inner class ImplicitOutputStream(os: OutputStream) : ASN1OutputStream(os) {
        private var first = true

        @Throws(IOException::class)
        public override fun write(b: Int) {
            if (first) {
                first = false
            } else {
                super.write(b)
            }
        }
    }
}
