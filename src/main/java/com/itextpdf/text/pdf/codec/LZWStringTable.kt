/*
 * Copyright 2003-2012 by Paulo Soares.
 *
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageio.plugins.tiff.TIFFDirectory.java)
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
 *
 * $Revision$
 * $Date$
 * $State: Exp $
 */

package com.itextpdf.text.pdf.codec

import java.io.PrintStream

/**
 * General purpose LZW String Table.
 * Extracted from GIFEncoder by Adam Doppelt
 * Comments added by Robin Luiten
 * `expandCode` added by Robin Luiten
 * The strLen_ table to give quick access to the lenght of an expanded
 * code for use by the `expandCode` method added by Robin.
 * @since 5.0.2
 */
class LZWStringTable {

    internal var strChr_: ByteArray        // after predecessor character
    internal var strNxt_: ShortArray        // predecessor string 
    internal var strHsh_: ShortArray        // hash table to find  predecessor + char pairs
    internal var numStrings_: Short = 0        // next code if adding new prestring + char

    /**
     * each entry corresponds to a code and contains the length of data
     * that the code expands to when decoded.
     */
    internal var strLen_: IntArray

    init {
        strChr_ = ByteArray(MAXSTR)
        strNxt_ = ShortArray(MAXSTR)
        strLen_ = IntArray(MAXSTR)
        strHsh_ = ShortArray(HASHSIZE)
    }

    /**
     * @param index value of -1 indicates no predecessor [used in initialization]
     * *
     * @param b the byte [character] to add to the string store which follows
     * * the predecessor string specified the index.
     * *
     * @return 0xFFFF if no space in table left for addition of predecessor
     * * index and byte b. Else return the code allocated for combination index + b.
     */
    fun AddCharString(index: Short, b: Byte): Int {
        var hshidx: Int

        if (numStrings_ >= MAXSTR)
        // if used up all codes
        {
            return 0xFFFF
        }

        hshidx = Hash(index, b)
        while (strHsh_[hshidx] != HASH_FREE)
            hshidx = (hshidx + HASHSTEP) % HASHSIZE

        strHsh_[hshidx] = numStrings_
        strChr_[numStrings_] = b
        if (index == HASH_FREE) {
            strNxt_[numStrings_] = NEXT_FIRST
            strLen_[numStrings_] = 1
        } else {
            strNxt_[numStrings_] = index
            strLen_[numStrings_] = strLen_[index] + 1
        }

        return numStrings_++.toInt()    // return the code and inc for next code
    }

    /**
     * @param index index to prefix string
     * *
     * @param b the character that follws the index prefix
     * *
     * @return b if param index is HASH_FREE. Else return the code
     * * for this prefix and byte successor
     */
    fun FindCharString(index: Short, b: Byte): Short {
        var hshidx: Int
        var nxtidx: Int

        if (index == HASH_FREE)
            return (b and 0xFF).toShort()    // Rob fixed used to sign extend

        hshidx = Hash(index, b)
        while ((nxtidx = strHsh_[hshidx].toInt()) != HASH_FREE.toInt())
        // search
        {
            if (strNxt_[nxtidx] == index && strChr_[nxtidx] == b)
                return nxtidx.toShort()
            hshidx = (hshidx + HASHSTEP) % HASHSIZE
        }

        return 0xFFFF.toShort()
    }

    /**
     * @param codesize the size of code to be preallocated for the
     * * string store.
     */
    fun ClearTable(codesize: Int) {
        numStrings_ = 0

        for (q in 0..HASHSIZE - 1)
            strHsh_[q] = HASH_FREE

        val w = (1 shl codesize) + RES_CODES
        for (q in 0..w - 1)
            AddCharString(0xFFFF.toShort(), q.toByte())    // init with no prefix
    }

    /**
     * If expanded data doesn't fit into array only what will fit is written
     * to buf and the return value indicates how much of the expanded code has
     * been written to the buf. The next call to expandCode() should be with
     * the same code and have the skip parameter set the negated value of the
     * previous return. Successive negative return values should be negated and
     * added together for next skip parameter value with same code.

     * @param buf buffer to place expanded data into
     * *
     * @param offset offset to place expanded data
     * *
     * @param code the code to expand to the byte array it represents.
     * * PRECONDITION This code must already be in the LZSS
     * *
     * @param skipHead is the number of bytes at the start of the expanded code to
     * * be skipped before data is written to buf. It is possible that skipHead is
     * * equal to codeLen.
     * *
     * @return the length of data expanded into buf. If the expanded code is longer
     * * than space left in buf then the value returned is a negative number which when
     * * negated is equal to the number of bytes that were used of the code being expanded.
     * * This negative value also indicates the buffer is full.
     */
    fun expandCode(buf: ByteArray, offset: Int, code: Short, skipHead: Int): Int {
        var code = code
        var skipHead = skipHead
        if (offset == -2) {
            if (skipHead == 1) skipHead = 0
        }
        if (code == 0xFFFF.toShort() || // just in case
                skipHead == strLen_[code])
        // DONE no more unpacked
            return 0

        val expandLen: Int                            // how much data we are actually expanding
        val codeLen = strLen_[code] - skipHead    // length of expanded code left
        val bufSpace = buf.size - offset        // how much space left
        if (bufSpace > codeLen)
            expandLen = codeLen                // only got this many to unpack
        else
            expandLen = bufSpace

        var skipTail = codeLen - expandLen        // only > 0 if codeLen > bufSpace [left overs]

        var idx = offset + expandLen            // initialise to exclusive end address of buffer area

        // NOTE: data unpacks in reverse direction and we are placing the
        // unpacked data directly into the array in the correct location.
        while (idx > offset && code != 0xFFFF.toShort()) {
            if (--skipTail < 0)
            // skip required of expanded data
            {
                buf[--idx] = strChr_[code]
            }
            code = strNxt_[code]                // to predecessor code
        }

        if (codeLen > expandLen)
            return -expandLen                    // indicate what part of codeLen used
        else
            return expandLen                    // indicate length of dat unpacked
    }

    fun dump(out: PrintStream) {
        var i: Int
        i = 258
        while (i < numStrings_) {
            out.println(" strNxt_[" + i + "] = " + strNxt_[i]
                    + " strChr_ " + Integer.toHexString(strChr_[i] and 0xFF)
                    + " strLen_ " + Integer.toHexString(strLen_[i]))
            ++i
        }
    }

    companion object {
        /** codesize + Reserved Codes  */
        private val RES_CODES = 2

        private val HASH_FREE = 0xFFFF.toShort()
        private val NEXT_FIRST = 0xFFFF.toShort()

        private val MAXBITS = 12
        private val MAXSTR = 1 shl MAXBITS

        private val HASHSIZE: Short = 9973
        private val HASHSTEP: Short = 2039

        fun Hash(index: Short, lastbyte: Byte): Int {
            return (((lastbyte shl 8).toShort() xor index).toInt() and 0xFFFF) % HASHSIZE
        }
    }
}
/**
 * Constructor allocate memory for string store data
 */
