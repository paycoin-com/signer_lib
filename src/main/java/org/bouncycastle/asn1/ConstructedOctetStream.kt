package org.bouncycastle.asn1

import java.io.IOException
import java.io.InputStream

internal class ConstructedOctetStream(
        private val _parser: ASN1StreamParser) : InputStream() {

    private var _first = true
    private var _currentStream: InputStream? = null

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (_currentStream == null) {
            if (!_first) {
                return -1
            }

            val s = _parser.readObject() as ASN1OctetStringParser? ?: return -1

            _first = false
            _currentStream = s.octetStream
        }

        var totalRead = 0

        while (true) {
            val numRead = _currentStream!!.read(b, off + totalRead, len - totalRead)

            if (numRead >= 0) {
                totalRead += numRead

                if (totalRead == len) {
                    return totalRead
                }
            } else {
                val aos = _parser.readObject() as ASN1OctetStringParser?

                if (aos == null) {
                    _currentStream = null
                    return if (totalRead < 1) -1 else totalRead
                }

                _currentStream = aos.octetStream
            }
        }
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (_currentStream == null) {
            if (!_first) {
                return -1
            }

            val s = _parser.readObject() as ASN1OctetStringParser? ?: return -1

            _first = false
            _currentStream = s.octetStream
        }

        while (true) {
            val b = _currentStream!!.read()

            if (b >= 0) {
                return b
            }

            val s = _parser.readObject() as ASN1OctetStringParser?

            if (s == null) {
                _currentStream = null
                return -1
            }

            _currentStream = s.octetStream
        }
    }
}
