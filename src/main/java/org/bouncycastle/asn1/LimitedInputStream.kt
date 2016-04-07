package org.bouncycastle.asn1

import java.io.InputStream

internal abstract class LimitedInputStream(
        protected val _in: InputStream,
        // TODO: maybe one day this can become more accurate
        val remaining: Int) : InputStream() {

    protected fun setParentEofDetect(on: Boolean) {
        if (_in is IndefiniteLengthInputStream) {
            _in.setEofOn00(on)
        }
    }
}
