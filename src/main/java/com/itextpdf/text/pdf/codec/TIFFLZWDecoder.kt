/*
 * Copyright 2003-2012 by Paulo Soares.
 * 
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageioimpl.plugins.tiff.TIFFLZWDecompressor.java)
 * using the BSD license in a specific wording. In a mail dating from
 * January 23, 2008, Brian Burkhalter (@sun.com) gave us permission
 * to use the code under the following version of the BSD license:
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility.
 */
package com.itextpdf.text.pdf.codec

import com.itextpdf.text.error_messages.MessageLocalization

/**
 * A class for performing LZW decoding.


 */
class TIFFLZWDecoder(internal var w: Int, internal var predictor: Int, internal var samplesPerPixel: Int) {

    internal var stringTable: Array<ByteArray>
    internal var data: ByteArray? = null
    internal var uncompData: ByteArray
    internal var tableIndex: Int = 0
    internal var bitsToGet = 9
    internal var bytePointer: Int = 0
    internal var bitPointer: Int = 0
    internal var dstIndex: Int = 0
    internal var h: Int = 0
    internal var nextData = 0
    internal var nextBits = 0

    internal var andTable = intArrayOf(511, 1023, 2047, 4095)

    /**
     * Method to decode LZW compressed data.

     * @param data            The compressed data.
     * *
     * @param uncompData      Array to return the uncompressed data in.
     * *
     * @param h               The number of rows the compressed data contains.
     */
    fun decode(data: ByteArray, uncompData: ByteArray, h: Int): ByteArray {

        if (data[0] == 0x00.toByte() && data[1] == 0x01.toByte()) {
            throw UnsupportedOperationException(MessageLocalization.getComposedMessage("tiff.5.0.style.lzw.codes.are.not.supported"))
        }

        initializeStringTable()

        this.data = data
        this.h = h
        this.uncompData = uncompData

        // Initialize pointers
        bytePointer = 0
        bitPointer = 0
        dstIndex = 0


        nextData = 0
        nextBits = 0

        var code: Int
        var oldCode = 0
        var string: ByteArray

        while ((code = nextCode) != 257 && dstIndex < uncompData.size) {

            if (code == 256) {

                initializeStringTable()
                code = nextCode

                if (code == 257) {
                    break
                }

                writeString(stringTable[code])
                oldCode = code

            } else {

                if (code < tableIndex) {

                    string = stringTable[code]

                    writeString(string)
                    addStringToTable(stringTable[oldCode], string[0])
                    oldCode = code

                } else {

                    string = stringTable[oldCode]
                    string = composeString(string, string[0])
                    writeString(string)
                    addStringToTable(string)
                    oldCode = code
                }

            }

        }

        // Horizontal Differencing Predictor
        if (predictor == 2) {

            var count: Int
            for (j in 0..h - 1) {

                count = samplesPerPixel * (j * w + 1)

                for (i in samplesPerPixel..w * samplesPerPixel - 1) {

                    uncompData[count] += uncompData[count - samplesPerPixel]
                    count++
                }
            }
        }

        return uncompData
    }


    /**
     * Initialize the string table.
     */
    fun initializeStringTable() {

        stringTable = arrayOfNulls<ByteArray>(4096)

        for (i in 0..255) {
            stringTable[i] = ByteArray(1)
            stringTable[i][0] = i.toByte()
        }

        tableIndex = 258
        bitsToGet = 9
    }

    /**
     * Write out the string just uncompressed.
     */
    fun writeString(string: ByteArray) {
        // Fix for broken tiff files
        var max = uncompData.size - dstIndex
        if (string.size < max)
            max = string.size
        System.arraycopy(string, 0, uncompData, dstIndex, max)
        dstIndex += max
    }

    /**
     * Add a new string to the string table.
     */
    fun addStringToTable(oldString: ByteArray, newString: Byte) {
        val length = oldString.size
        val string = ByteArray(length + 1)
        System.arraycopy(oldString, 0, string, 0, length)
        string[length] = newString

        // Add this new String to the table
        stringTable[tableIndex++] = string

        if (tableIndex == 511) {
            bitsToGet = 10
        } else if (tableIndex == 1023) {
            bitsToGet = 11
        } else if (tableIndex == 2047) {
            bitsToGet = 12
        }
    }

    /**
     * Add a new string to the string table.
     */
    fun addStringToTable(string: ByteArray) {

        // Add this new String to the table
        stringTable[tableIndex++] = string

        if (tableIndex == 511) {
            bitsToGet = 10
        } else if (tableIndex == 1023) {
            bitsToGet = 11
        } else if (tableIndex == 2047) {
            bitsToGet = 12
        }
    }

    /**
     * Append `newString` to the end of `oldString`.
     */
    fun composeString(oldString: ByteArray, newString: Byte): ByteArray {
        val length = oldString.size
        val string = ByteArray(length + 1)
        System.arraycopy(oldString, 0, string, 0, length)
        string[length] = newString

        return string
    }

    // Returns the next 9, 10, 11 or 12 bits
    // Attempt to get the next code. The exception is caught to make
    // this robust to cases wherein the EndOfInformation code has been
    // omitted from a strip. Examples of such cases have been observed
    // in practice.
    // Strip not terminated as expected: return EndOfInformation code.
    val nextCode: Int
        get() {
            try {
                nextData = nextData shl 8 or (data!![bytePointer++] and 0xff)
                nextBits += 8

                if (nextBits < bitsToGet) {
                    nextData = nextData shl 8 or (data!![bytePointer++] and 0xff)
                    nextBits += 8
                }

                val code = nextData shr nextBits - bitsToGet and andTable[bitsToGet - 9]
                nextBits -= bitsToGet

                return code
            } catch (e: ArrayIndexOutOfBoundsException) {
                return 257
            }

        }
}
