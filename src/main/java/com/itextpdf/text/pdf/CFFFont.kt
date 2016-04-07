/*
 * $Id: 50c5ec9bdef26059eb21f96ced80213bbe4de866 $
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

/*
 * Comments by the original author, Sivan Toledo:
 * I created this class in order to add to iText the ability to utilize
 * OpenType fonts with CFF glyphs (these usually have an .otf extension).
 * The CFF font within the CFF table of the OT font might be either a CID
 * or a Type1 font. (CFF fonts may also contain multiple fonts; I do not
 * know if this is allowed in an OT table). The PDF spec, however, only
 * allow a CID font with an Identity-H or Identity-V encoding. Otherwise,
 * you are limited to an 8-bit encoding.
 * Adobe fonts come in both flavors. That is, the OTFs sometimes have
 * a CID CFF inside (for Japanese fonts), and sometimes a Type1 CFF
 * (virtually all the others, Latin/Greek/Cyrillic). So to easily use
 * all the glyphs in the latter, without creating multiple 8-bit encoding,
 * I wrote this class, whose main purpose is to convert a Type1 font inside
 * a CFF container (which might include other fonts) into a CID CFF font
 * that can be directly embeded in the PDF.
 *
 * Limitations of the current version:
 * 1. It does not extract a single CID font from a CFF that contains that
 *    particular CID along with other fonts. The Adobe Japanese OTF's that
 *    I have only have one font in the CFF table, so these can be
 *    embeded in the PDF as is.
 * 2. It does not yet subset fonts.
 * 3. It may or may not work on CFF fonts that are not within OTF's.
 *    I didn't try that. In any case, that would probably only be
 *    useful for subsetting CID fonts, not for CFF Type1 fonts (I don't
 *    think there are any available.
 * I plan to extend the class to support these three features at some
 * future time.
 */

package com.itextpdf.text.pdf

import java.util.LinkedList

import com.itextpdf.text.ExceptionConverter

open class CFFFont(
        /**
         * A random Access File or an array
         */
        protected var buf: RandomAccessFileOrArray) {

    //private String[] strings;
    fun getString(sid: Char): String? {
        if (sid.toInt() < standardStrings.size) return standardStrings[sid]
        if (sid.toInt() >= standardStrings.size + stringOffsets.size - 1) return null
        val j = sid.toInt() - standardStrings.size
        //java.lang.System.err.println("going for "+j);
        val p = position
        seek(stringOffsets[j])
        val s = StringBuffer()
        for (k in stringOffsets[j]..stringOffsets[j + 1] - 1) {
            s.append(card8)
        }
        seek(p)
        return s.toString()
    }

    internal val card8: Char
        get() {
            try {
                val i = buf.readByte()
                return (i and 0xff).toChar()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    internal val card16: Char
        get() {
            try {
                return buf.readChar()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    internal fun getOffset(offSize: Int): Int {
        var offset = 0
        for (i in 0..offSize - 1) {
            offset *= 256
            offset += card8.toInt()
        }
        return offset
    }

    internal fun seek(offset: Int) {
        try {
            buf.seek(offset.toLong())
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    internal val short: Short
        get() {
            try {
                return buf.readShort()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    internal val int: Int
        get() {
            try {
                return buf.readInt()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    internal val position: Int
        get() {
            try {
                return buf.filePointer.toInt()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }
    internal var nextIndexOffset: Int = 0
    // read the offsets in the next index
    // data structure, convert to global
    // offsets, and return them.
    // Sets the nextIndexOffset.
    internal fun getIndex(nextIndexOffset: Int): IntArray {
        var nextIndexOffset = nextIndexOffset
        val count: Int
        val indexOffSize: Int

        seek(nextIndexOffset)
        count = card16.toInt()
        val offsets = IntArray(count + 1)

        if (count == 0) {
            offsets[0] = -1
            nextIndexOffset += 2 // TODO death store to local var .. should this be this.nextIndexOffset ?
            return offsets
        }

        indexOffSize = card8.toInt()

        for (j in 0..count) {
            //nextIndexOffset = ofset to relative segment
            offsets[j] = nextIndexOffset
            //2-> count in the index header. 1->offset size in index header
            +2 + 1
            //offset array size * offset size
            +(count + 1) * indexOffSize //???zero <-> one base
            -1 // read object offset relative to object array base
            +getOffset(indexOffSize)
        }
        //nextIndexOffset = offsets[count];
        return offsets
    }

    protected var key: String? = null
    protected var args = arrayOfNulls<Any>(48)
    protected var arg_count = 0

    protected fun getDictItem() {
        for (i in 0..arg_count - 1) args[i] = null
        arg_count = 0
        key = null
        var gotKey = false

        while (!gotKey) {
            val b0 = card8
            if (b0.toInt() == 29) {
                val item = int
                args[arg_count] = Integer.valueOf(item)
                arg_count++
                //System.err.println(item+" ");
                continue
            }
            if (b0.toInt() == 28) {
                val item = short
                args[arg_count] = Integer.valueOf(item.toInt())
                arg_count++
                //System.err.println(item+" ");
                continue
            }
            if (b0.toInt() >= 32 && b0.toInt() <= 246) {
                val item = (b0.toInt() - 139).toByte()
                args[arg_count] = Integer.valueOf(item.toInt())
                arg_count++
                //System.err.println(item+" ");
                continue
            }
            if (b0.toInt() >= 247 && b0.toInt() <= 250) {
                val b1 = card8
                val item = ((b0.toInt() - 247) * 256 + b1.toInt() + 108).toShort()
                args[arg_count] = Integer.valueOf(item.toInt())
                arg_count++
                //System.err.println(item+" ");
                continue
            }
            if (b0.toInt() >= 251 && b0.toInt() <= 254) {
                val b1 = card8
                val item = (-(b0.toInt() - 251) * 256 - b1.toInt() - 108).toShort()
                args[arg_count] = Integer.valueOf(item.toInt())
                arg_count++
                //System.err.println(item+" ");
                continue
            }
            if (b0.toInt() == 30) {
                val item = StringBuilder("")
                var done = false
                var buffer: Char = 0.toChar()
                var avail: Byte = 0
                var nibble = 0
                while (!done) {
                    // get a nibble
                    if (avail.toInt() == 0) {
                        buffer = card8
                        avail = 2
                    }
                    if (avail.toInt() == 1) {
                        nibble = buffer.toInt() / 16
                        avail--
                    }
                    if (avail.toInt() == 2) {
                        nibble = buffer.toInt() % 16
                        avail--
                    }
                    when (nibble) {
                        0xa -> item.append(".")
                        0xb -> item.append("E")
                        0xc -> item.append("E-")
                        0xe -> item.append("-")
                        0xf -> done = true
                        else -> if (nibble >= 0 && nibble <= 9)
                            item.append(nibble.toString())
                        else {
                            item.append("<NIBBLE ERROR: ").append(nibble).append('>')
                            done = true
                        }
                    }
                }
                args[arg_count] = item.toString()
                arg_count++
                //System.err.println(" real=["+item+"]");
                continue
            }
            if (b0.toInt() <= 21) {
                gotKey = true
                if (b0.toInt() != 12)
                    key = operatorNames[b0]
                else
                    key = operatorNames[32 + card8.toInt()]
                //for (int i=0; i<arg_count; i++)
                //  System.err.print(args[i].toString()+" ");
                //System.err.println(key+" ;");
                continue
            }
        }
    }

    /** List items for the linked list that builds the new CID font.
     */

    protected abstract class Item {
        protected var myOffset = -1
        /** remember the current offset and increment by item's size in bytes.  */
        open fun increment(currentOffset: IntArray) {
            myOffset = currentOffset[0]
        }

        /** Emit the byte stream for this item.  */
        open fun emit(buffer: ByteArray) {
        }

        /** Fix up cross references to this item (applies only to markers).  */
        open fun xref() {
        }
    }

    protected abstract class OffsetItem : Item() {
        var value: Int = 0
        /** set the value of an offset item that was initially unknown.
         * It will be fixed up latex by a call to xref on some marker.
         */
        fun set(offset: Int) {
            this.value = offset
        }
    }


    /** A range item.
     */

    protected class RangeItem(private val buf: RandomAccessFileOrArray, var offset: Int, var length: Int) : Item() {
        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += length
        }

        override fun emit(buffer: ByteArray) {
            //System.err.println("range emit offset "+offset+" size="+length);
            try {
                buf.seek(offset.toLong())
                for (i in myOffset..myOffset + length - 1)
                    buffer[i] = buf.readByte()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

            //System.err.println("finished range emit");
        }
    }

    /** An index-offset item for the list.
     * The size denotes the required size in the CFF. A positive
     * value means that we need a specific size in bytes (for offset arrays)
     * and a negative value means that this is a dict item that uses a
     * variable-size representation.
     */
    protected class IndexOffsetItem : OffsetItem {
        val size: Int

        constructor(size: Int, value: Int) {
            this.size = size
            this.value = value
        }

        constructor(size: Int) {
            this.size = size
        }

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += size
        }

        override fun emit(buffer: ByteArray) {
            var i = 0
            when (size) {
                4 -> {
                    buffer[myOffset + i] = (value.ushr(24) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(16) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(8) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(0) and 0xff).toByte()
                    i++
                }
                3 -> {
                    buffer[myOffset + i] = (value.ushr(16) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(8) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(0) and 0xff).toByte()
                    i++
                }
                2 -> {
                    buffer[myOffset + i] = (value.ushr(8) and 0xff).toByte()
                    i++
                    buffer[myOffset + i] = (value.ushr(0) and 0xff).toByte()
                    i++
                }
                1 -> {
                    buffer[myOffset + i] = (value.ushr(0) and 0xff).toByte()
                    i++
                }
            }
            /*
            int mask = 0xff;
            for (int i=size-1; i>=0; i--) {
                buffer[myOffset+i] = (byte) (value & mask);
                mask <<= 8;
            }
             */
        }
    }

    protected class IndexBaseItem : Item()

    protected class IndexMarkerItem(private val offItem: OffsetItem, private val indexBase: IndexBaseItem) : Item() {
        override fun xref() {
            //System.err.println("index marker item, base="+indexBase.myOffset+" my="+this.myOffset);
            offItem.set(this.myOffset - indexBase.myOffset + 1)
        }
    }

    /**
     * TODO To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    protected class SubrMarkerItem(private val offItem: OffsetItem, private val indexBase: IndexBaseItem) : Item() {
        override fun xref() {
            //System.err.println("index marker item, base="+indexBase.myOffset+" my="+this.myOffset);
            offItem.set(this.myOffset - indexBase.myOffset)
        }
    }


    /** an unknown offset in a dictionary for the list.
     * We will fix up the offset later; for now, assume it's large.
     */
    protected class DictOffsetItem : OffsetItem() {
        val size: Int

        init {
            this.size = 5
        }

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += size
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            if (size == 5) {
                buffer[myOffset] = 29
                buffer[myOffset + 1] = (value.ushr(24) and 0xff).toByte()
                buffer[myOffset + 2] = (value.ushr(16) and 0xff).toByte()
                buffer[myOffset + 3] = (value.ushr(8) and 0xff).toByte()
                buffer[myOffset + 4] = (value.ushr(0) and 0xff).toByte()
            }
        }
    }

    /** Card24 item.
     */

    protected class UInt24Item(var value: Int) : Item() {

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += 3
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            buffer[myOffset + 0] = (value.ushr(16) and 0xff).toByte()
            buffer[myOffset + 1] = (value.ushr(8) and 0xff).toByte()
            buffer[myOffset + 2] = (value.ushr(0) and 0xff).toByte()
        }
    }

    /** Card32 item.
     */

    protected class UInt32Item(var value: Int) : Item() {

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += 4
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            buffer[myOffset + 0] = (value.ushr(24) and 0xff).toByte()
            buffer[myOffset + 1] = (value.ushr(16) and 0xff).toByte()
            buffer[myOffset + 2] = (value.ushr(8) and 0xff).toByte()
            buffer[myOffset + 3] = (value.ushr(0) and 0xff).toByte()
        }
    }

    /** A SID or Card16 item.
     */

    protected class UInt16Item(var value: Char) : Item() {

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += 2
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            buffer[myOffset + 0] = (value.toInt().ushr(8) and 0xff).toByte()
            buffer[myOffset + 1] = (value.toInt().ushr(0) and 0xff).toByte()
        }
    }

    /** A Card8 item.
     */

    protected class UInt8Item(var value: Char) : Item() {

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += 1
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            buffer[myOffset + 0] = (value.toInt().ushr(0) and 0xff).toByte()
        }
    }

    protected class StringItem(var s: String) : Item() {

        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += s.length
        }

        override fun emit(buffer: ByteArray) {
            for (i in 0..s.length - 1)
                buffer[myOffset + i] = (s[i].toInt() and 0xff).toByte()
        }
    }


    /** A dictionary number on the list.
     * This implementation is inefficient: it doesn't use the variable-length
     * representation.
     */

    protected class DictNumberItem(val value: Int) : Item() {
        var size = 5
        override fun increment(currentOffset: IntArray) {
            super.increment(currentOffset)
            currentOffset[0] += size
        }

        // this is incomplete!
        override fun emit(buffer: ByteArray) {
            if (size == 5) {
                buffer[myOffset] = 29
                buffer[myOffset + 1] = (value.ushr(24) and 0xff).toByte()
                buffer[myOffset + 2] = (value.ushr(16) and 0xff).toByte()
                buffer[myOffset + 3] = (value.ushr(8) and 0xff).toByte()
                buffer[myOffset + 4] = (value.ushr(0) and 0xff).toByte()
            }
        }
    }

    /** An offset-marker item for the list.
     * It is used to mark an offset and to set the offset list item.
     */

    protected class MarkerItem(internal var p: OffsetItem) : Item() {
        override fun xref() {
            p.set(this.myOffset)
        }
    }

    /** a utility that creates a range item for an entire index

     * @param indexOffset where the index is
     * *
     * @return a range item representing the entire index
     */

    protected fun getEntireIndexRange(indexOffset: Int): RangeItem {
        seek(indexOffset)
        val count = card16.toInt()
        if (count == 0) {
            return RangeItem(buf, indexOffset, 2)
        } else {
            val indexOffSize = card8.toInt()
            seek(indexOffset + 2 + 1 + count * indexOffSize)
            val size = getOffset(indexOffSize) - 1
            return RangeItem(buf, indexOffset,
                    2 + 1 + (count + 1) * indexOffSize + size)
        }
    }


    /** get a single CID font. The PDF architecture (1.4)
     * supports 16-bit strings only with CID CFF fonts, not
     * in Type-1 CFF fonts, so we convert the font to CID if
     * it is in the Type-1 format.
     * Two other tasks that we need to do are to select
     * only a single font from the CFF package (this again is
     * a PDF restriction) and to subset the CharStrings glyph
     * description.
     */


    fun getCID(fontName: String): ByteArray? //throws java.io.FileNotFoundException
    {
        var j: Int
        j = 0
        while (j < fonts.size) {
            if (fontName == fonts[j].name) break
            j++
        }
        if (j == fonts.size) return null

        val l = LinkedList<Item>()

        // copy the header

        seek(0)

        val major = card8.toInt()
        val minor = card8.toInt()
        val hdrSize = card8.toInt()
        val offSize = card8.toInt()
        nextIndexOffset = hdrSize

        l.addLast(RangeItem(buf, 0, hdrSize))

        var nglyphs = -1
        var nstrings = -1
        if (!fonts[j].isCID) {
            // count the glyphs
            seek(fonts[j].charstringsOffset)
            nglyphs = card16.toInt()
            seek(stringIndexOffset)
            nstrings = card16.toInt() + standardStrings.size
            //System.err.println("number of glyphs = "+nglyphs);
        }

        // create a name index

        l.addLast(UInt16Item(1.toChar())) // count
        l.addLast(UInt8Item(1.toChar())) // offSize
        l.addLast(UInt8Item(1.toChar())) // first offset
        l.addLast(UInt8Item((1 + fonts[j].name.length).toChar()))
        l.addLast(StringItem(fonts[j].name))

        // create the topdict Index


        l.addLast(UInt16Item(1.toChar())) // count
        l.addLast(UInt8Item(2.toChar())) // offSize
        l.addLast(UInt16Item(1.toChar())) // first offset
        val topdictIndex1Ref = IndexOffsetItem(2)
        l.addLast(topdictIndex1Ref)
        val topdictBase = IndexBaseItem()
        l.addLast(topdictBase)

        /*
        int maxTopdictLen = (topdictOffsets[j+1]-topdictOffsets[j])
                            + 9*2 // at most 9 new keys
                            + 8*5 // 8 new integer arguments
                            + 3*2;// 3 new SID arguments
         */

        //int    topdictNext = 0;
        //byte[] topdict = new byte[maxTopdictLen];

        val charsetRef = DictOffsetItem()
        val charstringsRef = DictOffsetItem()
        val fdarrayRef = DictOffsetItem()
        val fdselectRef = DictOffsetItem()

        if (!fonts[j].isCID) {
            // create a ROS key
            l.addLast(DictNumberItem(nstrings))
            l.addLast(DictNumberItem(nstrings + 1))
            l.addLast(DictNumberItem(0))
            l.addLast(UInt8Item(12.toChar()))
            l.addLast(UInt8Item(30.toChar()))
            // create a CIDCount key
            l.addLast(DictNumberItem(nglyphs))
            l.addLast(UInt8Item(12.toChar()))
            l.addLast(UInt8Item(34.toChar()))
            // What about UIDBase (12,35)? Don't know what is it.
            // I don't think we need FontName; the font I looked at didn't have it.
        }

        // create an FDArray key
        l.addLast(fdarrayRef)
        l.addLast(UInt8Item(12.toChar()))
        l.addLast(UInt8Item(36.toChar()))
        // create an FDSelect key
        l.addLast(fdselectRef)
        l.addLast(UInt8Item(12.toChar()))
        l.addLast(UInt8Item(37.toChar()))
        // create an charset key
        l.addLast(charsetRef)
        l.addLast(UInt8Item(15.toChar()))
        // create a CharStrings key
        l.addLast(charstringsRef)
        l.addLast(UInt8Item(17.toChar()))

        seek(topdictOffsets[j])
        while (position < topdictOffsets[j + 1]) {
            val p1 = position
            getDictItem()
            val p2 = position
            if (key === "Encoding"
                    || key === "Private"
                    || key === "FDSelect"
                    || key === "FDArray"
                    || key === "charset"
                    || key === "CharStrings") {
                // just drop them
            } else {
                l.add(RangeItem(buf, p1, p2 - p1))
            }
        }

        l.addLast(IndexMarkerItem(topdictIndex1Ref, topdictBase))

        // Copy the string index and append new strings.
        // We need 3 more strings: Registry, Ordering, and a FontName for one FD.
        // The total length is at most "Adobe"+"Identity"+63 = 76

        if (fonts[j].isCID) {
            l.addLast(getEntireIndexRange(stringIndexOffset))
        } else {
            var fdFontName = fonts[j].name + "-OneRange"
            if (fdFontName.length > 127)
                fdFontName = fdFontName.substring(0, 127)
            val extraStrings = "Adobe" + "Identity" + fdFontName

            val origStringsLen = stringOffsets[stringOffsets.size - 1] - stringOffsets[0]
            val stringsBaseOffset = stringOffsets[0] - 1

            val stringsIndexOffSize: Byte
            if (origStringsLen + extraStrings.length <= 0xff)
                stringsIndexOffSize = 1
            else if (origStringsLen + extraStrings.length <= 0xffff)
                stringsIndexOffSize = 2
            else if (origStringsLen + extraStrings.length <= 0xffffff)
                stringsIndexOffSize = 3
            else
                stringsIndexOffSize = 4

            l.addLast(UInt16Item((stringOffsets.size - 1 + 3).toChar())) // count
            l.addLast(UInt8Item(stringsIndexOffSize.toChar())) // offSize
            for (stringOffset in stringOffsets)
                l.addLast(IndexOffsetItem(stringsIndexOffSize.toInt(),
                        stringOffset - stringsBaseOffset))
            var currentStringsOffset = stringOffsets[stringOffsets.size - 1] - stringsBaseOffset
            //l.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
            currentStringsOffset += "Adobe".length
            l.addLast(IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))
            currentStringsOffset += "Identity".length
            l.addLast(IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))
            currentStringsOffset += fdFontName.length
            l.addLast(IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))

            l.addLast(RangeItem(buf, stringOffsets[0], origStringsLen))
            l.addLast(StringItem(extraStrings))
        }

        // copy the global subroutine index

        l.addLast(getEntireIndexRange(gsubrIndexOffset))

        // deal with fdarray, fdselect, and the font descriptors

        if (fonts[j].isCID) {
            // copy the FDArray, FDSelect, charset
        } else {
            // create FDSelect
            l.addLast(MarkerItem(fdselectRef))
            l.addLast(UInt8Item(3.toChar())) // format identifier
            l.addLast(UInt16Item(1.toChar())) // nRanges

            l.addLast(UInt16Item(0.toChar())) // Range[0].firstGlyph
            l.addLast(UInt8Item(0.toChar())) // Range[0].fd

            l.addLast(UInt16Item(nglyphs.toChar())) // sentinel

            // recreate a new charset
            // This format is suitable only for fonts without subsetting

            l.addLast(MarkerItem(charsetRef))
            l.addLast(UInt8Item(2.toChar())) // format identifier

            l.addLast(UInt16Item(1.toChar())) // first glyph in range (ignore .notdef)
            l.addLast(UInt16Item((nglyphs - 1).toChar())) // nLeft
            // now all are covered, the data structure is complete.

            // create a font dict index (fdarray)

            l.addLast(MarkerItem(fdarrayRef))
            l.addLast(UInt16Item(1.toChar()))
            l.addLast(UInt8Item(1.toChar())) // offSize
            l.addLast(UInt8Item(1.toChar())) // first offset

            val privateIndex1Ref = IndexOffsetItem(1)
            l.addLast(privateIndex1Ref)
            val privateBase = IndexBaseItem()
            l.addLast(privateBase)

            // looking at the PS that acrobat generates from a PDF with
            // a CFF opentype font embedded with an identity-H encoding,
            // it seems that it does not need a FontName.
            //l.addLast(new DictNumberItem((standardStrings.length+(stringOffsets.length-1)+2)));
            //l.addLast(new UInt8Item((char)12));
            //l.addLast(new UInt8Item((char)38)); // FontName

            l.addLast(DictNumberItem(fonts[j].privateLength))
            val privateRef = DictOffsetItem()
            l.addLast(privateRef)
            l.addLast(UInt8Item(18.toChar())) // Private

            l.addLast(IndexMarkerItem(privateIndex1Ref, privateBase))

            // copy the private index & local subroutines

            l.addLast(MarkerItem(privateRef))
            // copy the private dict and the local subroutines.
            // the length of the private dict seems to NOT include
            // the local subroutines.
            l.addLast(RangeItem(buf, fonts[j].privateOffset, fonts[j].privateLength))
            if (fonts[j].privateSubrs >= 0) {
                //System.err.println("has subrs="+fonts[j].privateSubrs+" ,len="+fonts[j].privateLength);
                l.addLast(getEntireIndexRange(fonts[j].privateSubrs))
            }
        }

        // copy the charstring index

        l.addLast(MarkerItem(charstringsRef))
        l.addLast(getEntireIndexRange(fonts[j].charstringsOffset))

        // now create the new CFF font

        val currentOffset = IntArray(1)
        currentOffset[0] = 0

        var listIter = l.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.increment(currentOffset)
        }

        listIter = l.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.xref()
        }

        val size = currentOffset[0]
        val b = ByteArray(size)

        listIter = l.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.emit(b)
        }

        return b
    }


    fun isCID(fontName: String): Boolean {
        var j: Int
        j = 0
        while (j < fonts.size) {
            if (fontName == fonts[j].name) return fonts[j].isCID
            j++
        }
        return false
    }

    fun exists(fontName: String): Boolean {
        var j: Int
        j = 0
        while (j < fonts.size) {
            if (fontName == fonts[j].name) return true
            j++
        }
        return false
    }


    val names: Array<String>
        get() {
            val names = arrayOfNulls<String>(fonts.size)
            for (i in fonts.indices)
                names[i] = fonts[i].name
            return names
        }
    private val offSize: Int

    protected var nameIndexOffset: Int = 0
    protected var topdictIndexOffset: Int = 0
    protected var stringIndexOffset: Int = 0
    protected var gsubrIndexOffset: Int = 0
    protected var nameOffsets: IntArray
    protected var topdictOffsets: IntArray
    protected var stringOffsets: IntArray
    protected var gsubrOffsets: IntArray

    /**
     * TODO Changed from private to protected by Ygal&Oren
     */
    protected inner class Font {
        var name: String
        var fullName: String
        var isCID = false
        var privateOffset = -1 // only if not CID
        var privateLength = -1 // only if not CID
        var privateSubrs = -1
        var charstringsOffset = -1
        var encodingOffset = -1
        var charsetOffset = -1
        var fdarrayOffset = -1 // only if CID
        var fdselectOffset = -1 // only if CID
        var fdprivateOffsets: IntArray
        var fdprivateLengths: IntArray
        var fdprivateSubrs: IntArray

        // Added by Oren & Ygal
        var nglyphs: Int = 0
        var nstrings: Int = 0
        var CharsetLength: Int = 0
        var charstringsOffsets: IntArray
        var charset: IntArray
        var FDSelect: IntArray
        var FDSelectLength: Int = 0
        var FDSelectFormat: Int = 0
        var CharstringType = 2
        var FDArrayCount: Int = 0
        var FDArrayOffsize: Int = 0
        var FDArrayOffsets: IntArray
        var PrivateSubrsOffset: IntArray
        var PrivateSubrsOffsetsArray: Array<IntArray>
        var SubrsOffsets: IntArray
    }

    // Changed from private to protected by Ygal&Oren
    protected var fonts: Array<Font>

    init {
        seek(0)

        val major: Int
        val minor: Int
        major = card8.toInt()
        minor = card8.toInt()

        //System.err.println("CFF Major-Minor = "+major+"-"+minor);

        val hdrSize = card8.toInt()

        offSize = card8.toInt()

        //System.err.println("offSize = "+offSize);

        //int count, indexOffSize, indexOffset, nextOffset;

        nameIndexOffset = hdrSize
        nameOffsets = getIndex(nameIndexOffset)
        topdictIndexOffset = nameOffsets[nameOffsets.size - 1]
        topdictOffsets = getIndex(topdictIndexOffset)
        stringIndexOffset = topdictOffsets[topdictOffsets.size - 1]
        stringOffsets = getIndex(stringIndexOffset)
        gsubrIndexOffset = stringOffsets[stringOffsets.size - 1]
        gsubrOffsets = getIndex(gsubrIndexOffset)

        fonts = arrayOfNulls<Font>(nameOffsets.size - 1)

        // now get the name index

        /*
        names             = new String[nfonts];
        privateOffset     = new int[nfonts];
        charsetOffset     = new int[nfonts];
        encodingOffset    = new int[nfonts];
        charstringsOffset = new int[nfonts];
        fdarrayOffset     = new int[nfonts];
        fdselectOffset    = new int[nfonts];
         */

        for (j in 0..nameOffsets.size - 1 - 1) {
            fonts[j] = Font()
            seek(nameOffsets[j])
            fonts[j].name = ""
            for (k in nameOffsets[j]..nameOffsets[j + 1] - 1) {
                fonts[j].name += card8
            }
            //System.err.println("name["+j+"]=<"+fonts[j].name+">");
        }

        // string index

        //strings = new String[stringOffsets.length-1];
        /*
        System.err.println("std strings = "+standardStrings.length);
        System.err.println("fnt strings = "+(stringOffsets.length-1));
        for (char j=0; j<standardStrings.length+(stringOffsets.length-1); j++) {
            //seek(stringOffsets[j]);
            //strings[j] = "";
            //for (int k=stringOffsets[j]; k<stringOffsets[j+1]; k++) {
            //	strings[j] += (char)getCard8();
            //}
            System.err.println("j="+(int)j+" <? "+(standardStrings.length+(stringOffsets.length-1)));
            System.err.println("strings["+(int)j+"]=<"+getString(j)+">");
        }
         */

        // top dict

        for (j in 0..topdictOffsets.size - 1 - 1) {
            seek(topdictOffsets[j])
            while (position < topdictOffsets[j + 1]) {
                getDictItem()
                if (key === "FullName") {
                    //System.err.println("getting fullname sid = "+((Integer)args[0]).intValue());
                    fonts[j].fullName = getString((args[0] as Int).toInt().toChar())
                    //System.err.println("got it");
                } else if (key === "ROS")
                    fonts[j].isCID = true
                else if (key === "Private") {
                    fonts[j].privateLength = (args[0] as Int).toInt()
                    fonts[j].privateOffset = (args[1] as Int).toInt()
                } else if (key === "charset") {
                    fonts[j].charsetOffset = (args[0] as Int).toInt()

                } else if (key === "CharStrings") {
                    fonts[j].charstringsOffset = (args[0] as Int).toInt()
                    //System.err.println("charstrings "+fonts[j].charstringsOffset);
                    // Added by Oren & Ygal
                    val p = position
                    fonts[j].charstringsOffsets = getIndex(fonts[j].charstringsOffset)
                    seek(p)
                } else if (key === "FDArray")
                    fonts[j].fdarrayOffset = (args[0] as Int).toInt()
                else if (key === "FDSelect")
                    fonts[j].fdselectOffset = (args[0] as Int).toInt()
                else if (key === "CharstringType")
                    fonts[j].CharstringType = (args[0] as Int).toInt()//                else if (key=="Encoding"){
                //                    int encOffset = ((Integer)args[0]).intValue();
                //                    if (encOffset > 0) {
                //                        fonts[j].encodingOffset = encOffset;
                //                        ReadEncoding(fonts[j].encodingOffset);
                //                    }
                //                }
            }

            // private dict
            if (fonts[j].privateOffset >= 0) {
                //System.err.println("PRIVATE::");
                seek(fonts[j].privateOffset)
                while (position < fonts[j].privateOffset + fonts[j].privateLength) {
                    getDictItem()
                    if (key === "Subrs")
                    //Add the private offset to the lsubrs since the offset is
                    // relative to the beginning of the PrivateDict
                        fonts[j].privateSubrs = (args[0] as Int).toInt() + fonts[j].privateOffset
                }
            }

            // fdarray index
            if (fonts[j].fdarrayOffset >= 0) {
                val fdarrayOffsets = getIndex(fonts[j].fdarrayOffset)

                fonts[j].fdprivateOffsets = IntArray(fdarrayOffsets.size - 1)
                fonts[j].fdprivateLengths = IntArray(fdarrayOffsets.size - 1)

                //System.err.println("FD Font::");

                for (k in 0..fdarrayOffsets.size - 1 - 1) {
                    seek(fdarrayOffsets[k])
                    while (position < fdarrayOffsets[k + 1]) {
                        getDictItem()
                        if (key === "Private") {
                            fonts[j].fdprivateLengths[k] = (args[0] as Int).toInt()
                            fonts[j].fdprivateOffsets[k] = (args[1] as Int).toInt()
                        }
                    }
                }
            }
        }
        //System.err.println("CFF: done");
    }//System.err.println("CFF: nStdString = "+standardStrings.length);

    // ADDED BY Oren & Ygal

    internal fun ReadEncoding(nextIndexOffset: Int) {
        val format: Int
        seek(nextIndexOffset)
        format = card8.toInt()
    }

    companion object {

        internal val operatorNames = arrayOf("version", "Notice", "FullName", "FamilyName", "Weight", "FontBBox", "BlueValues", "OtherBlues", "FamilyBlues", "FamilyOtherBlues", "StdHW", "StdVW", "UNKNOWN_12", "UniqueID", "XUID", "charset", "Encoding", "CharStrings", "Private", "Subrs", "defaultWidthX", "nominalWidthX", "UNKNOWN_22", "UNKNOWN_23", "UNKNOWN_24", "UNKNOWN_25", "UNKNOWN_26", "UNKNOWN_27", "UNKNOWN_28", "UNKNOWN_29", "UNKNOWN_30", "UNKNOWN_31", "Copyright", "isFixedPitch", "ItalicAngle", "UnderlinePosition", "UnderlineThickness", "PaintType", "CharstringType", "FontMatrix", "StrokeWidth", "BlueScale", "BlueShift", "BlueFuzz", "StemSnapH", "StemSnapV", "ForceBold", "UNKNOWN_12_15", "UNKNOWN_12_16", "LanguageGroup", "ExpansionFactor", "initialRandomSeed", "SyntheticBase", "PostScript", "BaseFontName", "BaseFontBlend", "UNKNOWN_12_24", "UNKNOWN_12_25", "UNKNOWN_12_26", "UNKNOWN_12_27", "UNKNOWN_12_28", "UNKNOWN_12_29", "ROS", "CIDFontVersion", "CIDFontRevision", "CIDFontType", "CIDCount", "UIDBase", "FDArray", "FDSelect", "FontName")

        internal val standardStrings = arrayOf(// Automatically generated from Appendix A of the CFF specification; do
                // not edit. Size should be 391.
                ".notdef", "space", "exclam", "quotedbl", "numbersign", "dollar", "percent", "ampersand", "quoteright", "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen", "period", "slash", "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less", "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash", "bracketright", "asciicircum", "underscore", "quoteleft", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar", "braceright", "asciitilde", "exclamdown", "cent", "sterling", "fraction", "yen", "florin", "section", "currency", "quotesingle", "quotedblleft", "guillemotleft", "guilsinglleft", "guilsinglright", "fi", "fl", "endash", "dagger", "daggerdbl", "periodcentered", "paragraph", "bullet", "quotesinglbase", "quotedblbase", "quotedblright", "guillemotright", "ellipsis", "perthousand", "questiondown", "grave", "acute", "circumflex", "tilde", "macron", "breve", "dotaccent", "dieresis", "ring", "cedilla", "hungarumlaut", "ogonek", "caron", "emdash", "AE", "ordfeminine", "Lslash", "Oslash", "OE", "ordmasculine", "ae", "dotlessi", "lslash", "oslash", "oe", "germandbls", "onesuperior", "logicalnot", "mu", "trademark", "Eth", "onehalf", "plusminus", "Thorn", "onequarter", "divide", "brokenbar", "degree", "thorn", "threequarters", "twosuperior", "registered", "minus", "eth", "multiply", "threesuperior", "copyright", "Aacute", "Acircumflex", "Adieresis", "Agrave", "Aring", "Atilde", "Ccedilla", "Eacute", "Ecircumflex", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis", "Igrave", "Ntilde", "Oacute", "Ocircumflex", "Odieresis", "Ograve", "Otilde", "Scaron", "Uacute", "Ucircumflex", "Udieresis", "Ugrave", "Yacute", "Ydieresis", "Zcaron", "aacute", "acircumflex", "adieresis", "agrave", "aring", "atilde", "ccedilla", "eacute", "ecircumflex", "edieresis", "egrave", "iacute", "icircumflex", "idieresis", "igrave", "ntilde", "oacute", "ocircumflex", "odieresis", "ograve", "otilde", "scaron", "uacute", "ucircumflex", "udieresis", "ugrave", "yacute", "ydieresis", "zcaron", "exclamsmall", "Hungarumlautsmall", "dollaroldstyle", "dollarsuperior", "ampersandsmall", "Acutesmall", "parenleftsuperior", "parenrightsuperior", "twodotenleader", "onedotenleader", "zerooldstyle", "oneoldstyle", "twooldstyle", "threeoldstyle", "fouroldstyle", "fiveoldstyle", "sixoldstyle", "sevenoldstyle", "eightoldstyle", "nineoldstyle", "commasuperior", "threequartersemdash", "periodsuperior", "questionsmall", "asuperior", "bsuperior", "centsuperior", "dsuperior", "esuperior", "isuperior", "lsuperior", "msuperior", "nsuperior", "osuperior", "rsuperior", "ssuperior", "tsuperior", "ff", "ffi", "ffl", "parenleftinferior", "parenrightinferior", "Circumflexsmall", "hyphensuperior", "Gravesmall", "Asmall", "Bsmall", "Csmall", "Dsmall", "Esmall", "Fsmall", "Gsmall", "Hsmall", "Ismall", "Jsmall", "Ksmall", "Lsmall", "Msmall", "Nsmall", "Osmall", "Psmall", "Qsmall", "Rsmall", "Ssmall", "Tsmall", "Usmall", "Vsmall", "Wsmall", "Xsmall", "Ysmall", "Zsmall", "colonmonetary", "onefitted", "rupiah", "Tildesmall", "exclamdownsmall", "centoldstyle", "Lslashsmall", "Scaronsmall", "Zcaronsmall", "Dieresissmall", "Brevesmall", "Caronsmall", "Dotaccentsmall", "Macronsmall", "figuredash", "hypheninferior", "Ogoneksmall", "Ringsmall", "Cedillasmall", "questiondownsmall", "oneeighth", "threeeighths", "fiveeighths", "seveneighths", "onethird", "twothirds", "zerosuperior", "foursuperior", "fivesuperior", "sixsuperior", "sevensuperior", "eightsuperior", "ninesuperior", "zeroinferior", "oneinferior", "twoinferior", "threeinferior", "fourinferior", "fiveinferior", "sixinferior", "seveninferior", "eightinferior", "nineinferior", "centinferior", "dollarinferior", "periodinferior", "commainferior", "Agravesmall", "Aacutesmall", "Acircumflexsmall", "Atildesmall", "Adieresissmall", "Aringsmall", "AEsmall", "Ccedillasmall", "Egravesmall", "Eacutesmall", "Ecircumflexsmall", "Edieresissmall", "Igravesmall", "Iacutesmall", "Icircumflexsmall", "Idieresissmall", "Ethsmall", "Ntildesmall", "Ogravesmall", "Oacutesmall", "Ocircumflexsmall", "Otildesmall", "Odieresissmall", "OEsmall", "Oslashsmall", "Ugravesmall", "Uacutesmall", "Ucircumflexsmall", "Udieresissmall", "Yacutesmall", "Thornsmall", "Ydieresissmall", "001.000", "001.001", "001.002", "001.003", "Black", "Bold", "Book", "Light", "Medium", "Regular", "Roman", "Semibold")
    }
}
