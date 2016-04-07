package org.bouncycastle.asn1

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

internal class IndefiniteLengthInputStream @Throws(IOException::class)
constructor(
        `in`: InputStream,
        limit: Int) : LimitedInputStream(`in`, limit) {
    private var _b1: Int = 0
    private var _b2: Int = 0
    private var _eofReached = false
    private var _eofOn00 = true

    init {

        _b1 = `in`.read()
        _b2 = `in`.read()

        if (_b2 < 0) {
            // Corrupted stream
            throw EOFException()
        }

        checkForEof()
    }

    fun setEofOn00(
            eofOn00: Boolean) {
        _eofOn00 = eofOn00
        checkForEof()
    }

    private fun checkForEof(): Boolean {
        if (!_eofReached && _eofOn00 && _b1 == 0x00 && _b2 == 0x00) {
            _eofReached = true
            setParentEofDetect(true)
        }
        return _eofReached
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        // Only use this optimisation if we aren't checking for 00
        if (_eofOn00 || len < 3) {
            return super.read(b, off, len)
        }

        if (_eofReached) {
            return -1
        }

        val numRead = _in.read(b, off + 2, len - 2)

        if (numRead < 0) {
            // Corrupted stream
            throw EOFException()
        }

        b[off] = _b1.toByte()
        b[off + 1] = _b2.toByte()

        _b1 = _in.read()
        _b2 = _in.read()

        if (_b2 < 0) {
            // Corrupted stream
            throw EOFException()
        }

        return numRead + 2
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (checkForEof()) {
            return -1
        }

        val b = _in.read()

        if (b < 0) {
            // Corrupted stream
            throw EOFException()
        }

        val v = _b1

        _b1 = _b2
        _b2 = b

        return v
    }
}
