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

import java.io.IOException
import java.io.PushbackReader
import java.io.Reader


/**
 * @since   22.08.2006
 */
class FixASCIIControlsReader
/**
 * The look-ahead size is 6 at maximum (&amp;#xAB;)
 * @see PushbackReader.PushbackReader
 * @param in a Reader
 */
(`in`: Reader) : PushbackReader(`in`, FixASCIIControlsReader.BUFFER_SIZE) {
    /** the state of the automaton  */
    private var state = STATE_START
    /** the result of the escaping sequence  */
    private var control = 0
    /** count the digits of the sequence  */
    private var digits = 0


    /**
     * @see Reader.read
     */
    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        var readAhead = 0
        var read = 0
        var pos = off
        val readAheadBuffer = CharArray(BUFFER_SIZE)

        var available = true
        while (available && read < len) {
            available = super.read(readAheadBuffer, readAhead, 1) == 1
            if (available) {
                var c = processChar(readAheadBuffer[readAhead])
                if (state == STATE_START) {
                    // replace control chars with space
                    if (Utils.isControlChar(c)) {
                        c = ' '
                    }
                    cbuf[pos++] = c
                    readAhead = 0
                    read++
                } else if (state == STATE_ERROR) {
                    unread(readAheadBuffer, 0, readAhead + 1)
                    readAhead = 0
                } else {
                    readAhead++
                }
            } else if (readAhead > 0) {
                // handles case when file ends within excaped sequence
                unread(readAheadBuffer, 0, readAhead)
                state = STATE_ERROR
                readAhead = 0
                available = true
            }
        }


        return if (read > 0 || available) read else -1
    }


    /**
     * Processes numeric escaped chars to find out if they are a control character.
     * @param ch a char
     * *
     * @return Returns the char directly or as replacement for the escaped sequence.
     */
    private fun processChar(ch: Char): Char {
        when (state) {
            STATE_START -> {
                if (ch == '&') {
                    state = STATE_AMP
                }
                return ch
            }

            STATE_AMP -> {
                if (ch == '#') {
                    state = STATE_HASH
                } else {
                    state = STATE_ERROR
                }
                return ch
            }

            STATE_HASH -> {
                if (ch == 'x') {
                    control = 0
                    digits = 0
                    state = STATE_HEX
                } else if ('0' <= ch && ch <= '9') {
                    control = Character.digit(ch, 10)
                    digits = 1
                    state = STATE_DIG1
                } else {
                    state = STATE_ERROR
                }
                return ch
            }

            STATE_DIG1 -> {
                if ('0' <= ch && ch <= '9') {
                    control = control * 10 + Character.digit(ch, 10)
                    digits++
                    if (digits <= 5) {
                        state = STATE_DIG1
                    } else {
                        state = STATE_ERROR // sequence too long
                    }
                } else if (ch == ';' && Utils.isControlChar(control.toChar())) {
                    state = STATE_START
                    return control.toChar()
                } else {
                    state = STATE_ERROR
                }
                return ch
            }

            STATE_HEX -> {
                if ('0' <= ch && ch <= '9' ||
                        'a' <= ch && ch <= 'f' ||
                        'A' <= ch && ch <= 'F') {
                    control = control * 16 + Character.digit(ch, 16)
                    digits++
                    if (digits <= 4) {
                        state = STATE_HEX
                    } else {
                        state = STATE_ERROR // sequence too long
                    }
                } else if (ch == ';' && Utils.isControlChar(control.toChar())) {
                    state = STATE_START
                    return control.toChar()
                } else {
                    state = STATE_ERROR
                }
                return ch
            }

            STATE_ERROR -> {
                state = STATE_START
                return ch
            }

            else -> // not reachable
                return ch
        }
    }

    companion object {
        /**  */
        private val STATE_START = 0
        /**  */
        private val STATE_AMP = 1
        /**  */
        private val STATE_HASH = 2
        /**  */
        private val STATE_HEX = 3
        /**  */
        private val STATE_DIG1 = 4
        /**  */
        private val STATE_ERROR = 5
        /**  */
        private val BUFFER_SIZE = 8
    }
}
