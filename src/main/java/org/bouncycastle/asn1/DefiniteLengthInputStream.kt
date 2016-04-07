package org.bouncycastle.asn1

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

import org.bouncycastle.util.io.Streams

internal class DefiniteLengthInputStream(
        `in`: InputStream,
        private val _originalLength: Int) : LimitedInputStream(`in`, _originalLength) {
    internal override var remaining: Int = 0
        private set(value: Int) {
            super.remaining = value
        }

    init {

        if (_originalLength < 0) {
            throw IllegalArgumentException("negative lengths not allowed")
        }
        this.remaining = _originalLength

        if (_originalLength == 0) {
            setParentEofDetect(true)
        }
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (remaining == 0) {
            return -1
        }

        val b = _in.read()

        if (b < 0) {
            throw EOFException("DEF length $_originalLength object truncated by $remaining")
        }

        if (--remaining == 0) {
            setParentEofDetect(true)
        }

        return b
    }

    @Throws(IOException::class)
    override fun read(buf: ByteArray, off: Int, len: Int): Int {
        if (remaining == 0) {
            return -1
        }

        val toRead = Math.min(len, remaining)
        val numRead = _in.read(buf, off, toRead)

        if (numRead < 0) {
            throw EOFException("DEF length $_originalLength object truncated by $remaining")
        }

        if ((remaining -= numRead) == 0) {
            setParentEofDetect(true)
        }

        return numRead
    }

    @Throws(IOException::class)
    fun toByteArray(): ByteArray {
        if (remaining == 0) {
            return EMPTY_BYTES
        }

        val bytes = ByteArray(remaining)
        if ((remaining -= Streams.readFully(_in, bytes)) != 0) {
            throw EOFException("DEF length $_originalLength object truncated by $remaining")
        }
        setParentEofDetect(true)
        return bytes
    }

    companion object {
        private val EMPTY_BYTES = ByteArray(0)
    }
}
