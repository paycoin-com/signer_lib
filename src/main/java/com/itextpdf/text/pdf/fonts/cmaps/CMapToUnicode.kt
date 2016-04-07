/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * http://www.fontbox.org

 */
package com.itextpdf.text.pdf.fonts.cmaps

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Utilities
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfString
import java.io.IOException
import java.util.HashMap

import com.itextpdf.text.error_messages.MessageLocalization

/**
 * This class represents a CMap file.

 * @author Ben Litchfield (ben@benlitchfield.com)
 * *
 * @since    2.1.4
 */
class CMapToUnicode : AbstractCMap() {

    private val singleByteMappings = HashMap<Int, String>()
    private val doubleByteMappings = HashMap<Int, String>()

    /**
     * This will tell if this cmap has any one byte mappings.

     * @return true If there are any one byte mappings, false otherwise.
     */
    fun hasOneByteMappings(): Boolean {
        return !singleByteMappings.isEmpty()
    }

    /**
     * This will tell if this cmap has any two byte mappings.

     * @return true If there are any two byte mappings, false otherwise.
     */
    fun hasTwoByteMappings(): Boolean {
        return !doubleByteMappings.isEmpty()
    }

    /**
     * This will perform a lookup into the map.

     * @param code The code used to lookup.
     * *
     * @param offset The offset into the byte array.
     * *
     * @param length The length of the data we are getting.
     * *
     * *
     * @return The string that matches the lookup.
     */
    fun lookup(code: ByteArray, offset: Int, length: Int): String {

        var result: String? = null
        var key: Int? = null
        if (length == 1) {

            key = Integer.valueOf(code[offset] and 0xff)
            result = singleByteMappings[key]
        } else if (length == 2) {
            var intKey = code[offset] and 0xff
            intKey = intKey shl 8
            intKey += code[offset + 1] and 0xff
            key = Integer.valueOf(intKey)

            result = doubleByteMappings[key]
        }

        return result
    }

    @Throws(IOException::class)
    fun createReverseMapping(): Map<Int, Int> {
        val result = HashMap<Int, Int>()
        for (entry in singleByteMappings.entries) {
            result.put(convertToInt(entry.value), entry.key)
        }
        for (entry in doubleByteMappings.entries) {
            result.put(convertToInt(entry.value), entry.key)
        }
        return result
    }

    @Throws(IOException::class)
    fun createDirectMapping(): Map<Int, Int> {
        val result = HashMap<Int, Int>()
        for (entry in singleByteMappings.entries) {
            result.put(entry.key, convertToInt(entry.value))
        }
        for (entry in doubleByteMappings.entries) {
            result.put(entry.key, convertToInt(entry.value))
        }
        return result
    }

    @Throws(IOException::class)
    private fun convertToInt(s: String): Int {
        val b = s.toByteArray(charset("UTF-16BE"))
        var value = 0
        for (i in 0..b.size - 1 - 1) {
            value += b[i] and 0xff
            value = value shl 8
        }
        value += b[b.size - 1] and 0xff
        return value
    }

    internal fun addChar(cid: Int, uni: String) {
        doubleByteMappings.put(Integer.valueOf(cid), uni)
    }

    internal override fun addChar(mark: PdfString, code: PdfObject) {
        try {
            val src = mark.bytes
            val dest = createStringFromBytes(code.bytes)
            if (src.size == 1) {
                singleByteMappings.put(Integer.valueOf(src[0] and 0xff), dest)
            } else if (src.size == 2) {
                var intSrc = src[0] and 0xFF
                intSrc = intSrc shl 8
                intSrc = intSrc or (src[1] and 0xFF)
                doubleByteMappings.put(Integer.valueOf(intSrc), dest)
            } else {
                throw IOException(MessageLocalization.getComposedMessage("mapping.code.should.be.1.or.two.bytes.and.not.1", src.size))
            }
        } catch (ex: Exception) {
            throw ExceptionConverter(ex)
        }

    }

    @Throws(IOException::class)
    private fun createStringFromBytes(bytes: ByteArray): String {
        var retval: String? = null
        if (bytes.size == 1) {
            retval = String(bytes)
        } else {
            retval = String(bytes, "UTF-16BE")
        }
        return retval
    }

    companion object {

        val identity: CMapToUnicode
            get() {
                val uni = CMapToUnicode()
                for (i in 0..65536) {
                    uni.addChar(i, Utilities.convertFromUtf32(i))
                }
                return uni
            }
    }
}
/**
 * Creates a new instance of CMap.
 */
//default constructor
