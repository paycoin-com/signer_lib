//Copyright (c) 2006, Adobe Systems Incorporated
//All rights reserved.
//
//        Redistribution and use in source and binary forms, with or without
//        modification, are permitted provided that the following conditions are met:
//        1. Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//        2. Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//        3. All advertising materials mentioning features or use of this software
//        must display the following acknowledgement:
//        This product includes software developed by the Adobe Systems Incorporated.
//        4. Neither the name of the Adobe Systems Incorporated nor the
//        names of its contributors may be used to endorse or promote products
//        derived from this software without specific prior written permission.
//
//        THIS SOFTWARE IS PROVIDED BY ADOBE SYSTEMS INCORPORATED ''AS IS'' AND ANY
//        EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//        WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//        DISCLAIMED. IN NO EVENT SHALL ADOBE SYSTEMS INCORPORATED BE LIABLE FOR ANY
//        DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//        (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//        LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//        ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//        (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//        http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

package com.itextpdf.xmp.impl


/**
 * A utility class to perform base64 encoding and decoding as specified
 * in RFC-1521. See also RFC 1421.

 * @version     $Revision: 1.4 $
 */
object Base64 {
    /** marker for invalid bytes  */
    private val INVALID: Byte = -1
    /** marker for accepted whitespace bytes  */
    private val WHITESPACE: Byte = -2
    /** marker for an equal symbol  */
    private val EQUAL: Byte = -3

    /**  */
    private val base64 = byteArrayOf('A'.toByte(), 'B'.toByte(), 'C'.toByte(), 'D'.toByte(), //  0 to  3
            'E'.toByte(), 'F'.toByte(), 'G'.toByte(), 'H'.toByte(), //  4 to  7
            'I'.toByte(), 'J'.toByte(), 'K'.toByte(), 'L'.toByte(), //  8 to 11
            'M'.toByte(), 'N'.toByte(), 'O'.toByte(), 'P'.toByte(), // 11 to 15
            'Q'.toByte(), 'R'.toByte(), 'S'.toByte(), 'T'.toByte(), // 16 to 19
            'U'.toByte(), 'V'.toByte(), 'W'.toByte(), 'X'.toByte(), // 20 to 23
            'Y'.toByte(), 'Z'.toByte(), 'a'.toByte(), 'b'.toByte(), // 24 to 27
            'c'.toByte(), 'd'.toByte(), 'e'.toByte(), 'f'.toByte(), // 28 to 31
            'g'.toByte(), 'h'.toByte(), 'i'.toByte(), 'j'.toByte(), // 32 to 35
            'k'.toByte(), 'l'.toByte(), 'm'.toByte(), 'n'.toByte(), // 36 to 39
            'o'.toByte(), 'p'.toByte(), 'q'.toByte(), 'r'.toByte(), // 40 to 43
            's'.toByte(), 't'.toByte(), 'u'.toByte(), 'v'.toByte(), // 44 to 47
            'w'.toByte(), 'x'.toByte(), 'y'.toByte(), 'z'.toByte(), // 48 to 51
            '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(), // 52 to 55
            '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(), // 56 to 59
            '8'.toByte(), '9'.toByte(), '+'.toByte(), '/'.toByte()    // 60 to 63
    )
    /**  */
    private val ascii = ByteArray(255)

    /**  */
    init {
        // not valid bytes
        for (idx in 0..254) {
            ascii[idx] = INVALID
        }
        // valid bytes
        for (idx in base64.indices) {
            ascii[base64[idx]] = idx.toByte()
        }
        // whitespaces
        ascii[0x09] = WHITESPACE
        ascii[0x0A] = WHITESPACE
        ascii[0x0D] = WHITESPACE
        ascii[0x20] = WHITESPACE

        // trailing equals
        ascii[0x3d] = EQUAL
    }


    /**
     * Encode the given byte[].

     * @param src the source string.
     * *
     * @param lineFeed a linefeed is added after `linefeed` characters;
     * *            must be dividable by four; 0 means no linefeeds
     * *
     * @return the base64-encoded data.
     */
    @JvmOverloads fun encode(src: ByteArray, lineFeed: Int = 0): ByteArray {
        var lineFeed = lineFeed
        // linefeed must be dividable by 4
        lineFeed = lineFeed / 4 * 4
        if (lineFeed < 0) {
            lineFeed = 0
        }

        // determine code length
        var codeLength = (src.size + 2) / 3 * 4
        if (lineFeed > 0) {
            codeLength += (codeLength - 1) / lineFeed
        }

        val dst = ByteArray(codeLength)
        var bits24: Int
        var bits6: Int
        //
        // Do 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
        //
        var didx = 0
        var sidx = 0
        var lf = 0
        while (sidx + 3 <= src.size) {
            bits24 = src[sidx++] and 0xFF shl 16
            bits24 = bits24 or (src[sidx++] and 0xFF shl 8)
            bits24 = bits24 or (src[sidx++] and 0xFF shl 0)
            bits6 = bits24 and 0x00FC0000 shr 18
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x0003F000 shr 12
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x00000FC0 shr 6
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x0000003F
            dst[didx++] = base64[bits6]

            lf += 4
            if (didx < codeLength && lineFeed > 0 && lf % lineFeed == 0) {
                dst[didx++] = 0x0A
            }
        }
        if (src.size - sidx == 2) {
            bits24 = src[sidx] and 0xFF shl 16
            bits24 = bits24 or (src[sidx + 1] and 0xFF shl 8)
            bits6 = bits24 and 0x00FC0000 shr 18
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x0003F000 shr 12
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x00000FC0 shr 6
            dst[didx++] = base64[bits6]
            dst[didx++] = '='.toByte()
        } else if (src.size - sidx == 1) {
            bits24 = src[sidx] and 0xFF shl 16
            bits6 = bits24 and 0x00FC0000 shr 18
            dst[didx++] = base64[bits6]
            bits6 = bits24 and 0x0003F000 shr 12
            dst[didx++] = base64[bits6]
            dst[didx++] = '='.toByte()
            dst[didx++] = '='.toByte()
        }
        return dst
    }


    /**
     * Encode the given string.
     * @param src the source string.
     * *
     * @return the base64-encoded string.
     */
    fun encode(src: String): String {
        return String(encode(src.toByteArray()))
    }


    /**
     * Decode the given byte[].

     * @param src
     * *            the base64-encoded data.
     * *
     * @return the decoded data.
     * *
     * @throws IllegalArgumentException Thrown if the base 64 strings contains non-valid characters,
     * * 		beside the bas64 chars, LF, CR, tab and space are accepted.
     */
    @Throws(IllegalArgumentException::class)
    fun decode(src: ByteArray): ByteArray {
        //
        // Do ascii printable to 0-63 conversion.
        //
        var sidx: Int
        var srcLen = 0
        sidx = 0
        while (sidx < src.size) {
            val `val` = ascii[src[sidx]]
            if (`val` >= 0) {
                src[srcLen++] = `val`
            } else if (`val` == INVALID) {
                throw IllegalArgumentException("Invalid base 64 string")
            }
            sidx++
        }

        //
        // Trim any padding.
        //
        while (srcLen > 0 && src[srcLen - 1] == EQUAL) {
            srcLen--
        }
        val dst = ByteArray(srcLen * 3 / 4)

        //
        // Do 4-byte to 3-byte conversion.
        //
        var didx: Int
        sidx = 0
        didx = 0
        while (didx < dst.size - 2) {
            dst[didx] = (src[sidx] shl 2 and 0xFF or (src[sidx + 1].ushr(4) and 0x03)).toByte()
            dst[didx + 1] = (src[sidx + 1] shl 4 and 0xFF or (src[sidx + 2].ushr(2) and 0x0F)).toByte()
            dst[didx + 2] = (src[sidx + 2] shl 6 and 0xFF or (src[sidx + 3] and 0x3F)).toByte()
            sidx += 4
            didx += 3
        }
        if (didx < dst.size) {
            dst[didx] = (src[sidx] shl 2 and 0xFF or (src[sidx + 1].ushr(4) and 0x03)).toByte()
        }
        if (++didx < dst.size) {
            dst[didx] = (src[sidx + 1] shl 4 and 0xFF or (src[sidx + 2].ushr(2) and 0x0F)).toByte()
        }
        return dst
    }


    /**
     * Decode the given string.

     * @param src the base64-encoded string.
     * *
     * @return the decoded string.
     */
    fun decode(src: String): String {
        return String(decode(src.toByteArray()))
    }
}
/**
 * Encode the given byte[].

 * @param src the source string.
 * *
 * @return the base64-encoded data.
 */
