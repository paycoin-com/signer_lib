/*
 * $Id: 2cbae5fe4b1dc2c74d54dcda09a7e8a99c875de9 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.InvalidPdfException
import com.itextpdf.text.io.RandomAccessSourceFactory

import java.io.IOException

/**

 * @author  Paulo Soares
 */
class PRTokeniser
/**
 * Creates a PRTokeniser for the specified [RandomAccessFileOrArray].
 * The beginning of the file is read to determine the location of the header, and the data source is adjusted
 * as necessary to account for any junk that occurs in the byte source before the header
 * @param file the source
 */
(//TODO: is this really necessary?  Seems like exposing this detail opens us up to all sorts of potential problems
        val file: RandomAccessFileOrArray) {

    private val outBuf = StringBuilder()

    /**
     * Enum representing the possible token types
     * @since 5.0.1
     */
    enum class TokenType {
        NUMBER,
        STRING,
        NAME,
        COMMENT,
        START_ARRAY,
        END_ARRAY,
        START_DIC,
        END_DIC,
        REF,
        OTHER,
        ENDOFFILE
    }

    var tokenType: TokenType
        protected set
    var stringValue: String
        protected set
    var reference: Int = 0
        protected set
    var generation: Int = 0
        protected set
    var isHexString: Boolean = false
        protected set

    @Throws(IOException::class)
    fun seek(pos: Long) {
        file.seek(pos)
    }

    val filePointer: Long
        @Throws(IOException::class)
        get() = file.filePointer

    @Throws(IOException::class)
    fun close() {
        file.close()
    }

    @Throws(IOException::class)
    fun length(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    fun read(): Int {
        return file.read()
    }

    val safeFile: RandomAccessFileOrArray
        get() = RandomAccessFileOrArray(file)

    @Throws(IOException::class)
    fun readString(size: Int): String {
        var size = size
        val buf = StringBuilder()
        var ch: Int
        while (size-- > 0) {
            ch = read()
            if (ch == -1)
                break
            buf.append(ch.toChar())
        }
        return buf.toString()
    }

    fun backOnePosition(ch: Int) {
        if (ch != -1)
            file.pushBack(ch.toByte())
    }

    @Throws(IOException::class)
    fun throwError(error: String) {
        throw InvalidPdfException(MessageLocalization.getComposedMessage("1.at.file.pointer.2", error, file.filePointer.toString()))
    }

    val headerOffset: Int
        @Throws(IOException::class)
        get() {
            val str = readString(1024)
            var idx = str.indexOf("%PDF-")
            if (idx < 0) {
                idx = str.indexOf("%FDF-")
                if (idx < 0)
                    throw InvalidPdfException(MessageLocalization.getComposedMessage("pdf.header.not.found"))
            }

            return idx
        }

    @Throws(IOException::class)
    fun checkPdfHeader(): Char {
        file.seek(0)
        val str = readString(1024)
        val idx = str.indexOf("%PDF-")
        if (idx != 0)
            throw InvalidPdfException(MessageLocalization.getComposedMessage("pdf.header.not.found"))
        return str[7]
    }

    @Throws(IOException::class)
    fun checkFdfHeader() {
        file.seek(0)
        val str = readString(1024)
        val idx = str.indexOf("%FDF-")
        if (idx != 0)
            throw InvalidPdfException(MessageLocalization.getComposedMessage("fdf.header.not.found"))
    }

    // 9 = "startxref".length()
    val startxref: Long
        @Throws(IOException::class)
        get() {
            val arrLength = 1024
            val fileLength = file.length()
            var pos = fileLength - arrLength
            if (pos < 1) pos = 1
            while (pos > 0) {
                file.seek(pos)
                val str = readString(arrLength)
                val idx = str.lastIndexOf("startxref")
                if (idx >= 0) return pos + idx
                pos = pos - arrLength + 9
            }
            throw InvalidPdfException(MessageLocalization.getComposedMessage("pdf.startxref.not.found"))
        }

    @Throws(IOException::class)
    fun nextValidToken() {
        var level = 0
        var n1: String? = null
        var n2: String? = null
        var ptr: Long = 0
        while (nextToken()) {
            if (tokenType == TokenType.COMMENT)
                continue
            when (level) {
                0 -> {
                    if (tokenType != TokenType.NUMBER)
                        return
                    ptr = file.filePointer
                    n1 = stringValue
                    ++level
                }
                1 -> {
                    if (tokenType != TokenType.NUMBER) {
                        file.seek(ptr)
                        tokenType = TokenType.NUMBER
                        stringValue = n1
                        return
                    }
                    n2 = stringValue
                    ++level
                }
                else -> {
                    if (tokenType != TokenType.OTHER || stringValue != "R") {
                        file.seek(ptr)
                        tokenType = TokenType.NUMBER
                        stringValue = n1
                        return
                    }
                    tokenType = TokenType.REF
                    reference = Integer.parseInt(n1)
                    generation = Integer.parseInt(n2)
                    return
                }
            }
        }

        if (level == 1) {
            // if the level 1 check returns EOF, then we are still looking at a number - set the type back to NUMBER
            tokenType = TokenType.NUMBER
        }
        // if we hit here, the file is either corrupt (stream ended unexpectedly),
        // or the last token ended exactly at the end of a stream.  This last
        // case can occur inside an Object Stream.
    }

    @Throws(IOException::class)
    fun nextToken(): Boolean {
        var ch = 0
        do {
            ch = file.read()
        } while (ch != -1 && isWhitespace(ch))
        if (ch == -1) {
            tokenType = TokenType.ENDOFFILE
            return false
        }

        // Note:  We have to initialize stringValue here, after we've looked for the end of the stream,
        // to ensure that we don't lose the value of a token that might end exactly at the end
        // of the stream
        outBuf.setLength(0)
        stringValue = EMPTY

        when (ch) {
            '[' -> tokenType = TokenType.START_ARRAY
            ']' -> tokenType = TokenType.END_ARRAY
            '/' -> {
                outBuf.setLength(0)
                tokenType = TokenType.NAME
                while (true) {
                    ch = file.read()
                    if (delims[ch + 1])
                        break
                    if (ch == '#') {
                        ch = (getHex(file.read()) shl 4) + getHex(file.read())
                    }
                    outBuf.append(ch.toChar())
                }
                backOnePosition(ch)
            }
            '>' -> {
                ch = file.read()
                if (ch != '>')
                    throwError(MessageLocalization.getComposedMessage("greaterthan.not.expected"))
                tokenType = TokenType.END_DIC
            }
            '<' -> {
                var v1 = file.read()
                if (v1 == '<') {
                    tokenType = TokenType.START_DIC
                    break
                }
                outBuf.setLength(0)
                tokenType = TokenType.STRING
                isHexString = true
                var v2 = 0
                while (true) {
                    while (isWhitespace(v1))
                        v1 = file.read()
                    if (v1 == '>')
                        break
                    v1 = getHex(v1)
                    if (v1 < 0)
                        break
                    v2 = file.read()
                    while (isWhitespace(v2))
                        v2 = file.read()
                    if (v2 == '>') {
                        ch = v1 shl 4
                        outBuf.append(ch.toChar())
                        break
                    }
                    v2 = getHex(v2)
                    if (v2 < 0)
                        break
                    ch = (v1 shl 4) + v2
                    outBuf.append(ch.toChar())
                    v1 = file.read()
                }
                if (v1 < 0 || v2 < 0)
                    throwError(MessageLocalization.getComposedMessage("error.reading.string"))
            }
            '%' -> {
                tokenType = TokenType.COMMENT
                do {
                    ch = file.read()
                } while (ch != -1 && ch != '\r' && ch != '\n')
            }
            '(' -> {
                outBuf.setLength(0)
                tokenType = TokenType.STRING
                isHexString = false
                var nesting = 0
                while (true) {
                    ch = file.read()
                    if (ch == -1)
                        break
                    if (ch == '(') {
                        ++nesting
                    } else if (ch == ')') {
                        --nesting
                    } else if (ch == '\\') {
                        var lineBreak = false
                        ch = file.read()
                        when (ch) {
                            'n' -> ch = '\n'
                            'r' -> ch = '\r'
                            't' -> ch = '\t'
                            'b' -> ch = '\b'
                            'f' -> ch = '\f'
                            '(', ')', '\\' -> {
                            }
                            '\r' -> {
                                lineBreak = true
                                ch = file.read()
                                if (ch != '\n')
                                    backOnePosition(ch)
                            }
                            '\n' -> lineBreak = true
                            else -> {
                                if (ch < '0' || ch > '7') {
                                    break
                                }
                                var octal = ch - '0'
                                ch = file.read()
                                if (ch < '0' || ch > '7') {
                                    backOnePosition(ch)
                                    ch = octal
                                    break
                                }
                                octal = (octal shl 3) + ch - '0'
                                ch = file.read()
                                if (ch < '0' || ch > '7') {
                                    backOnePosition(ch)
                                    ch = octal
                                    break
                                }
                                octal = (octal shl 3) + ch - '0'
                                ch = octal and 0xff
                            }
                        }
                        if (lineBreak)
                            continue
                        if (ch < 0)
                            break
                    } else if (ch == '\r') {
                        ch = file.read()
                        if (ch < 0)
                            break
                        if (ch != '\n') {
                            backOnePosition(ch)
                            ch = '\n'
                        }
                    }
                    if (nesting == -1)
                        break
                    outBuf.append(ch.toChar())
                }
                if (ch == -1)
                    throwError(MessageLocalization.getComposedMessage("error.reading.string"))
            }
            else -> {
                outBuf.setLength(0)
                if (ch == '-' || ch == '+' || ch == '.' || ch >= '0' && ch <= '9') {
                    tokenType = TokenType.NUMBER
                    var isReal = false
                    var numberOfMinuses = 0
                    if (ch == '-') {
                        // Take care of number like "--234". If Acrobat can read them so must we.
                        do {
                            ++numberOfMinuses
                            ch = file.read()
                        } while (ch == '-')
                        outBuf.append('-')
                    } else {
                        outBuf.append(ch.toChar())
                        // We don't need to check if the number is real over here
                        // as we need to know that fact only in case if there are any minuses.
                        ch = file.read()
                    }
                    while (ch != -1 && (ch >= '0' && ch <= '9' || ch == '.')) {
                        if (ch == '.')
                            isReal = true
                        outBuf.append(ch.toChar())
                        ch = file.read()
                    }
                    if (numberOfMinuses > 1 && !isReal) {
                        // Numbers of integer type and with more than one minus before them
                        // are interpreted by Acrobat as zero.
                        outBuf.setLength(0)
                        outBuf.append('0')
                    }
                } else {
                    tokenType = TokenType.OTHER
                    do {
                        outBuf.append(ch.toChar())
                        ch = file.read()
                    } while (!delims[ch + 1])
                }
                if (ch != -1)
                    backOnePosition(ch)
            }
        }
        if (outBuf != null)
            stringValue = outBuf.toString()
        return true
    }

    fun longValue(): Long {
        return java.lang.Long.parseLong(stringValue)
    }

    fun intValue(): Int {
        return Integer.parseInt(stringValue)
    }

    /**
     * Reads data into the provided byte[]. Checks on leading whitespace.
     * See [isWhiteSpace(int)][.isWhitespace] or [isWhiteSpace(int, boolean)][.isWhitespace]
     * for a list of whitespace characters.

     * @param input byte[]
     * *
     * @param isNullWhitespace boolean to indicate whether '0' is whitespace or not.
     * *                         If in doubt, use true or overloaded method [readLineSegment(input)][.readLineSegment]
     * *
     * @return boolean
     * *
     * @throws IOException
     * *
     * @since 5.5.1
     */
    @Throws(IOException::class)
    @JvmOverloads fun readLineSegment(input: ByteArray, isNullWhitespace: Boolean = true): Boolean {
        var c = -1
        var eol = false
        var ptr = 0
        val len = input.size
        // ssteward, pdftk-1.10, 040922:
        // skip initial whitespace; added this because PdfReader.rebuildXref()
        // assumes that line provided by readLineSegment does not have init. whitespace;
        if (ptr < len) {
            while (isWhitespace(c = read(), isNullWhitespace))
        }
        while (!eol && ptr < len) {
            when (c) {
                -1, '\n' -> eol = true
                '\r' -> {
                    eol = true
                    val cur = filePointer
                    if (read() != '\n') {
                        seek(cur)
                    }
                }
                else -> input[ptr++] = c.toByte()
            }

            // break loop? do it before we read() again
            if (eol || len <= ptr) {
                break
            } else {
                c = read()
            }
        }
        if (ptr >= len) {
            eol = false
            while (!eol) {
                when (c = read()) {
                    -1, '\n' -> eol = true
                    '\r' -> {
                        eol = true
                        val cur = filePointer
                        if (read() != '\n') {
                            seek(cur)
                        }
                    }
                }
            }
        }

        if (c == -1 && ptr == 0) {
            return false
        }
        if (ptr + 2 <= len) {
            input[ptr++] = ' '.toByte()
            input[ptr] = 'X'.toByte()
        }
        return true
    }

    companion object {

        val delims = booleanArrayOf(true, true, false, false, false, false, false, false, false, false, true, true, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, true, false, false, true, true, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)

        internal val EMPTY = ""

        /**
         * Checks whether a character is a whitespace. Currently checks on the following: '0', '9', '10', '12', '13', '32'.
         * @param ch int
         * *
         * @param isWhitespace boolean
         * *
         * @return boolean
         * *
         * @since 5.5.1
         */
        @JvmOverloads fun isWhitespace(ch: Int, isWhitespace: Boolean = true): Boolean {
            return isWhitespace && ch == 0 || ch == 9 || ch == 10 || ch == 12 || ch == 13 || ch == 32
        }

        fun isDelimiter(ch: Int): Boolean {
            return ch == '(' || ch == ')' || ch == '<' || ch == '>' || ch == '[' || ch == ']' || ch == '/' || ch == '%'
        }

        fun isDelimiterWhitespace(ch: Int): Boolean {
            return delims[ch + 1]
        }

        fun getHex(v: Int): Int {
            if (v >= '0' && v <= '9')
                return v - '0'
            if (v >= 'A' && v <= 'F')
                return v - 'A' + 10
            if (v >= 'a' && v <= 'f')
                return v - 'a' + 10
            return -1
        }

        fun checkObjectStart(line: ByteArray): LongArray? {
            try {
                val tk = PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(line)))
                var num = 0
                var gen = 0
                if (!tk.nextToken() || tk.tokenType != TokenType.NUMBER)
                    return null
                num = tk.intValue()
                if (!tk.nextToken() || tk.tokenType != TokenType.NUMBER)
                    return null
                gen = tk.intValue()
                if (!tk.nextToken())
                    return null
                if (tk.stringValue != "obj")
                    return null
                return longArrayOf(num.toLong(), gen.toLong())
            } catch (ioe: Exception) {
                // empty on purpose
            }

            return null
        }
    }

}
/**
 * Is a certain character a whitespace? Currently checks on the following: '0', '9', '10', '12', '13', '32'.
 * The same as calling [isWhiteSpace(ch, true)][.isWhitespace].
 * @param ch int
 * *
 * @return boolean
 * *
 * @since 5.5.1
 */
/**
 * Reads data into the provided byte[]. Checks on leading whitespace.
 * See [isWhiteSpace(int)][.isWhitespace] or [isWhiteSpace(int, boolean)][.isWhitespace]
 * for a list of whitespace characters.
 * The same as calling [readLineSegment(input, true)][.readLineSegment].

 * @param input byte[]
 * *
 * @return boolean
 * *
 * @throws IOException
 * *
 * @since 5.5.1
 */
