package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

class BEROctetStringGenerator : BERGenerator {
    @Throws(IOException::class)
    constructor(out: OutputStream) : super(out) {

        writeBERHeader(BERTags.CONSTRUCTED or BERTags.OCTET_STRING)
    }

    @Throws(IOException::class)
    constructor(
            out: OutputStream,
            tagNo: Int,
            isExplicit: Boolean) : super(out, tagNo, isExplicit) {

        writeBERHeader(BERTags.CONSTRUCTED or BERTags.OCTET_STRING)
    }

    // limit for CER encoding.
    val octetOutputStream: OutputStream
        get() = getOctetOutputStream(ByteArray(1000))

    fun getOctetOutputStream(
            buf: ByteArray): OutputStream {
        return BufferedBEROctetStream(buf)
    }

    private inner class BufferedBEROctetStream internal constructor(
            private val _buf: ByteArray) : OutputStream() {
        private var _off: Int = 0
        private val _derOut: DEROutputStream

        init {
            _off = 0
            _derOut = DEROutputStream(_out)
        }

        @Throws(IOException::class)
        override fun write(
                b: Int) {
            _buf[_off++] = b.toByte()

            if (_off == _buf.size) {
                DEROctetString.encode(_derOut, _buf)
                _off = 0
            }
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray, off: Int, len: Int) {
            var off = off
            var len = len
            while (len > 0) {
                val numToCopy = Math.min(len, _buf.size - _off)
                System.arraycopy(b, off, _buf, _off, numToCopy)

                _off += numToCopy
                if (_off < _buf.size) {
                    break
                }

                DEROctetString.encode(_derOut, _buf)
                _off = 0

                off += numToCopy
                len -= numToCopy
            }
        }

        @Throws(IOException::class)
        override fun close() {
            if (_off != 0) {
                val bytes = ByteArray(_off)
                System.arraycopy(_buf, 0, bytes, 0, _off)

                DEROctetString.encode(_derOut, bytes)
            }

            writeBEREnd()
        }
    }
}
