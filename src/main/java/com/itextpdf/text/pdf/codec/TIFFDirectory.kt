/*
 * Copyright 2003-2012 by Paulo Soares.
 *
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageio.plugins.tiff.TIFFDirectory.java)
 * using the BSD license in a specific wording. In a mail dating from
 * January 23, 2008, Brian Burkhalter (@sun.com) gave us permission
 * to use the code under the following version of the BSD license:
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All  Rights Reserved.
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

import java.io.EOFException
import java.io.IOException
import java.io.Serializable
import java.util.ArrayList
import java.util.Enumeration
import java.util.Hashtable

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.RandomAccessFileOrArray

/**
 * A class representing an Image File Directory (IFD) from a TIFF 6.0
 * stream.  The TIFF file format is described in more detail in the
 * comments for the TIFFDescriptor class.

 *
 *  A TIFF IFD consists of a set of TIFFField tags.  Methods are
 * provided to query the set of tags and to obtain the raw field
 * array.  In addition, convenience methods are provided for acquiring
 * the values of tags that contain a single value that fits into a
 * byte, int, long, float, or double.

 *
 *  Every TIFF file is made up of one or more public IFDs that are
 * joined in a linked list, rooted in the file header.  A file may
 * also contain so-called private IFDs that are referenced from
 * tag data and do not appear in the main list.

 *
 * ** This class is not a committed part of the JAI API.  It may
 * be removed or changed in future releases of JAI.**

 * @see TIFFField
 */
class TIFFDirectory : Any, Serializable {

    /** A boolean storing the endianness of the stream.  */
    /**
     * Returns a boolean indicating whether the byte order used in the
     * the TIFF file is big-endian (i.e. whether the byte order is from
     * the most significant to the least significant)
     */
    var isBigEndian: Boolean = false
        internal set

    /** The number of entries in the IFD.  */
    /** Returns the number of directory entries.  */
    var numEntries: Int = 0
        internal set

    /** An array of TIFFFields.  */
    /**
     * Returns an array of TIFFFields containing all the fields
     * in this directory.
     */
    var fields: Array<TIFFField>
        internal set

    /** A Hashtable indexing the fields by tag number.  */
    internal var fieldIndex = Hashtable<Int, Int>()

    /** The offset of this IFD.  */
    /**
     * Returns the offset of the IFD corresponding to this
     * `TIFFDirectory`.
     */
    var ifdOffset: Long = 8
        internal set

    /** The offset of the next IFD.  */
    /**
     * Returns the offset of the next IFD after the IFD corresponding to this
     * `TIFFDirectory`.
     */
    var nextIFDOffset: Long = 0
        internal set

    /** The default constructor.  */
    internal constructor() {
    }

    /**
     * Constructs a TIFFDirectory from a SeekableStream.
     * The directory parameter specifies which directory to read from
     * the linked list present in the stream; directory 0 is normally
     * read but it is possible to store multiple images in a single
     * TIFF file by maintaining multiple directories.

     * @param stream a SeekableStream to read from.
     * *
     * @param directory the index of the directory to read.
     */
    @Throws(IOException::class)
    constructor(stream: RandomAccessFileOrArray, directory: Int) {

        val global_save_offset = stream.filePointer
        var ifd_offset: Long

        // Read the TIFF header
        stream.seek(0L)
        val endian = stream.readUnsignedShort()
        if (!isValidEndianTag(endian)) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("bad.endianness.tag.not.0x4949.or.0x4d4d"))
        }
        isBigEndian = endian == 0x4d4d

        val magic = readUnsignedShort(stream)
        if (magic != 42) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("bad.magic.number.should.be.42"))
        }

        // Get the initial ifd offset as an unsigned int (using a long)
        ifd_offset = readUnsignedInt(stream)

        for (i in 0..directory - 1) {
            if (ifd_offset == 0L) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("directory.number.too.large"))
            }

            stream.seek(ifd_offset)
            val entries = readUnsignedShort(stream)
            stream.skip((12 * entries).toLong())

            ifd_offset = readUnsignedInt(stream)
        }

        stream.seek(ifd_offset)
        initialize(stream)
        stream.seek(global_save_offset)
    }

    /**
     * Constructs a TIFFDirectory by reading a SeekableStream.
     * The ifd_offset parameter specifies the stream offset from which
     * to begin reading; this mechanism is sometimes used to store
     * private IFDs within a TIFF file that are not part of the normal
     * sequence of IFDs.

     * @param stream a SeekableStream to read from.
     * *
     * @param ifd_offset the long byte offset of the directory.
     * *
     * @param directory the index of the directory to read beyond the
     * *        one at the current stream offset; zero indicates the IFD
     * *        at the current offset.
     */
    @Throws(IOException::class)
    constructor(stream: RandomAccessFileOrArray, ifd_offset: Long, directory: Int) {
        var ifd_offset = ifd_offset

        val global_save_offset = stream.filePointer
        stream.seek(0L)
        val endian = stream.readUnsignedShort()
        if (!isValidEndianTag(endian)) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("bad.endianness.tag.not.0x4949.or.0x4d4d"))
        }
        isBigEndian = endian == 0x4d4d

        // Seek to the first IFD.
        stream.seek(ifd_offset)

        // Seek to desired IFD if necessary.
        var dirNum = 0
        while (dirNum < directory) {
            // Get the number of fields in the current IFD.
            val numEntries = readUnsignedShort(stream)

            // Skip to the next IFD offset value field.
            stream.seek(ifd_offset + 12 * numEntries)

            // Read the offset to the next IFD beyond this one.
            ifd_offset = readUnsignedInt(stream)

            // Seek to the next IFD.
            stream.seek(ifd_offset)

            // Increment the directory.
            dirNum++
        }

        initialize(stream)
        stream.seek(global_save_offset)
    }

    @Throws(IOException::class)
    private fun initialize(stream: RandomAccessFileOrArray) {
        var nextTagOffset = 0L
        val maxOffset = stream.length()
        var i: Int
        var j: Int

        ifdOffset = stream.filePointer

        numEntries = readUnsignedShort(stream)
        fields = arrayOfNulls<TIFFField>(numEntries)

        i = 0
        while (i < numEntries && nextTagOffset < maxOffset) {
            val tag = readUnsignedShort(stream)
            val type = readUnsignedShort(stream)
            var count = readUnsignedInt(stream).toInt()
            var processTag = true

            // The place to return to to read the next tag
            nextTagOffset = stream.filePointer + 4

            try {
                // If the tag data can't fit in 4 bytes, the next 4 bytes
                // contain the starting offset of the data
                if (count * sizeOfType[type] > 4) {
                    val valueOffset = readUnsignedInt(stream)

                    // bounds check offset for EOF
                    if (valueOffset < maxOffset) {
                        stream.seek(valueOffset)
                    } else {
                        // bad offset pointer .. skip tag
                        processTag = false
                    }
                }
            } catch (ae: ArrayIndexOutOfBoundsException) {
                // if the data type is unknown we should skip this TIFF Field
                processTag = false
            }

            if (processTag) {
                fieldIndex.put(Integer.valueOf(tag), Integer.valueOf(i))
                var obj: Any? = null

                when (type) {
                    TIFFField.TIFF_BYTE, TIFFField.TIFF_SBYTE, TIFFField.TIFF_UNDEFINED, TIFFField.TIFF_ASCII -> {
                        val bvalues = ByteArray(count)
                        stream.readFully(bvalues, 0, count)

                        if (type == TIFFField.TIFF_ASCII) {

                            // Can be multiple strings
                            var index = 0
                            var prevIndex = 0
                            val v = ArrayList<String>()

                            while (index < count) {

                                while (index < count && bvalues[index++].toInt() != 0)

                                // When we encountered zero, means one string has ended
                                    v.add(String(bvalues, prevIndex,
                                            index - prevIndex))
                                prevIndex = index
                            }

                            count = v.size
                            val strings = arrayOfNulls<String>(count)
                            for (c in 0..count - 1) {
                                strings[c] = v[c]
                            }

                            obj = strings
                        } else {
                            obj = bvalues
                        }
                    }

                    TIFFField.TIFF_SHORT -> {
                        val cvalues = CharArray(count)
                        j = 0
                        while (j < count) {
                            cvalues[j] = readUnsignedShort(stream).toChar()
                            j++
                        }
                        obj = cvalues
                    }

                    TIFFField.TIFF_LONG -> {
                        val lvalues = LongArray(count)
                        j = 0
                        while (j < count) {
                            lvalues[j] = readUnsignedInt(stream)
                            j++
                        }
                        obj = lvalues
                    }

                    TIFFField.TIFF_RATIONAL -> {
                        val llvalues = Array(count) { LongArray(2) }
                        j = 0
                        while (j < count) {
                            llvalues[j][0] = readUnsignedInt(stream)
                            llvalues[j][1] = readUnsignedInt(stream)
                            j++
                        }
                        obj = llvalues
                    }

                    TIFFField.TIFF_SSHORT -> {
                        val svalues = ShortArray(count)
                        j = 0
                        while (j < count) {
                            svalues[j] = readShort(stream)
                            j++
                        }
                        obj = svalues
                    }

                    TIFFField.TIFF_SLONG -> {
                        val ivalues = IntArray(count)
                        j = 0
                        while (j < count) {
                            ivalues[j] = readInt(stream)
                            j++
                        }
                        obj = ivalues
                    }

                    TIFFField.TIFF_SRATIONAL -> {
                        val iivalues = Array(count) { IntArray(2) }
                        j = 0
                        while (j < count) {
                            iivalues[j][0] = readInt(stream)
                            iivalues[j][1] = readInt(stream)
                            j++
                        }
                        obj = iivalues
                    }

                    TIFFField.TIFF_FLOAT -> {
                        val fvalues = FloatArray(count)
                        j = 0
                        while (j < count) {
                            fvalues[j] = readFloat(stream)
                            j++
                        }
                        obj = fvalues
                    }

                    TIFFField.TIFF_DOUBLE -> {
                        val dvalues = DoubleArray(count)
                        j = 0
                        while (j < count) {
                            dvalues[j] = readDouble(stream)
                            j++
                        }
                        obj = dvalues
                    }

                    else -> {
                    }
                }

                fields[i] = TIFFField(tag, type, count, obj)
            }

            stream.seek(nextTagOffset)
            i++
        }

        // Read the offset of the next IFD.
        try {
            nextIFDOffset = readUnsignedInt(stream)
        } catch (e: Exception) {
            // broken tiffs may not have this pointer
            nextIFDOffset = 0
        }

    }

    /**
     * Returns the value of a given tag as a TIFFField,
     * or null if the tag is not present.
     */
    fun getField(tag: Int): TIFFField? {
        val i = fieldIndex[Integer.valueOf(tag)]
        if (i == null) {
            return null
        } else {
            return fields[i.toInt()]
        }
    }

    /**
     * Returns true if a tag appears in the directory.
     */
    fun isTagPresent(tag: Int): Boolean {
        return fieldIndex.containsKey(Integer.valueOf(tag))
    }

    /**
     * Returns an ordered array of ints indicating the tag
     * values.
     */
    val tags: IntArray
        get() {
            val tags = IntArray(fieldIndex.size)
            val e = fieldIndex.keys()
            var i = 0

            while (e.hasMoreElements()) {
                tags[i++] = e.nextElement().toInt()
            }

            return tags
        }

    /**
     * Returns the value of a particular index of a given tag as a
     * byte.  The caller is responsible for ensuring that the tag is
     * present and has type TIFFField.TIFF_SBYTE, TIFF_BYTE, or
     * TIFF_UNDEFINED.
     */
    @JvmOverloads fun getFieldAsByte(tag: Int, index: Int = 0): Byte {
        val i = fieldIndex[Integer.valueOf(tag)]
        val b = fields[i!!.toInt()].asBytes
        return b[index]
    }

    /**
     * Returns the value of a particular index of a given tag as a
     * long.  The caller is responsible for ensuring that the tag is
     * present and has type TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED,
     * TIFF_SHORT, TIFF_SSHORT, TIFF_SLONG or TIFF_LONG.
     */
    @JvmOverloads fun getFieldAsLong(tag: Int, index: Int = 0): Long {
        val i = fieldIndex[Integer.valueOf(tag)]
        return fields[i!!.toInt()].getAsLong(index)
    }

    /**
     * Returns the value of a particular index of a given tag as a
     * float.  The caller is responsible for ensuring that the tag is
     * present and has numeric type (all but TIFF_UNDEFINED and
     * TIFF_ASCII).
     */
    @JvmOverloads fun getFieldAsFloat(tag: Int, index: Int = 0): Float {
        val i = fieldIndex[Integer.valueOf(tag)]
        return fields[i!!.toInt()].getAsFloat(index)
    }

    /**
     * Returns the value of a particular index of a given tag as a
     * double.  The caller is responsible for ensuring that the tag is
     * present and has numeric type (all but TIFF_UNDEFINED and
     * TIFF_ASCII).
     */
    @JvmOverloads fun getFieldAsDouble(tag: Int, index: Int = 0): Double {
        val i = fieldIndex[Integer.valueOf(tag)]
        return fields[i!!.toInt()].getAsDouble(index)
    }

    // Methods to read primitive data types from the stream

    @Throws(IOException::class)
    private fun readShort(stream: RandomAccessFileOrArray): Short {
        if (isBigEndian) {
            return stream.readShort()
        } else {
            return stream.readShortLE()
        }
    }

    @Throws(IOException::class)
    private fun readUnsignedShort(stream: RandomAccessFileOrArray): Int {
        if (isBigEndian) {
            return stream.readUnsignedShort()
        } else {
            return stream.readUnsignedShortLE()
        }
    }

    @Throws(IOException::class)
    private fun readInt(stream: RandomAccessFileOrArray): Int {
        if (isBigEndian) {
            return stream.readInt()
        } else {
            return stream.readIntLE()
        }
    }

    @Throws(IOException::class)
    private fun readUnsignedInt(stream: RandomAccessFileOrArray): Long {
        if (isBigEndian) {
            return stream.readUnsignedInt()
        } else {
            return stream.readUnsignedIntLE()
        }
    }

    @Throws(IOException::class)
    private fun readLong(stream: RandomAccessFileOrArray): Long {
        if (isBigEndian) {
            return stream.readLong()
        } else {
            return stream.readLongLE()
        }
    }

    @Throws(IOException::class)
    private fun readFloat(stream: RandomAccessFileOrArray): Float {
        if (isBigEndian) {
            return stream.readFloat()
        } else {
            return stream.readFloatLE()
        }
    }

    @Throws(IOException::class)
    private fun readDouble(stream: RandomAccessFileOrArray): Double {
        if (isBigEndian) {
            return stream.readDouble()
        } else {
            return stream.readDoubleLE()
        }
    }

    companion object {

        private val serialVersionUID = -168636766193675380L

        private fun isValidEndianTag(endian: Int): Boolean {
            return endian == 0x4949 || endian == 0x4d4d
        }

        private val sizeOfType = intArrayOf(0, //  0 = n/a
                1, //  1 = byte
                1, //  2 = ascii
                2, //  3 = short
                4, //  4 = long
                8, //  5 = rational
                1, //  6 = sbyte
                1, //  7 = undefined
                2, //  8 = sshort
                4, //  9 = slong
                8, // 10 = srational
                4, // 11 = float
                8  // 12 = double
        )

        @Throws(IOException::class)
        private fun readUnsignedShort(stream: RandomAccessFileOrArray,
                                      isBigEndian: Boolean): Int {
            if (isBigEndian) {
                return stream.readUnsignedShort()
            } else {
                return stream.readUnsignedShortLE()
            }
        }

        @Throws(IOException::class)
        private fun readUnsignedInt(stream: RandomAccessFileOrArray,
                                    isBigEndian: Boolean): Long {
            if (isBigEndian) {
                return stream.readUnsignedInt()
            } else {
                return stream.readUnsignedIntLE()
            }
        }

        // Utilities

        /**
         * Returns the number of image directories (subimages) stored in a
         * given TIFF file, represented by a `SeekableStream`.
         */
        @Throws(IOException::class)
        fun getNumDirectories(stream: RandomAccessFileOrArray): Int {
            val pointer = stream.filePointer // Save stream pointer

            stream.seek(0L)
            val endian = stream.readUnsignedShort()
            if (!isValidEndianTag(endian)) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("bad.endianness.tag.not.0x4949.or.0x4d4d"))
            }
            val isBigEndian = endian == 0x4d4d
            val magic = readUnsignedShort(stream, isBigEndian)
            if (magic != 42) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("bad.magic.number.should.be.42"))
            }

            stream.seek(4L)
            var offset = readUnsignedInt(stream, isBigEndian)

            var numDirectories = 0
            while (offset != 0L) {
                ++numDirectories

                // EOFException means IFD was probably not properly terminated.
                try {
                    stream.seek(offset)
                    val entries = readUnsignedShort(stream, isBigEndian)
                    stream.skip((12 * entries).toLong())
                    offset = readUnsignedInt(stream, isBigEndian)
                } catch (eof: EOFException) {
                    numDirectories--
                    break
                }

            }

            stream.seek(pointer) // Reset stream pointer
            return numDirectories
        }
    }
}
/**
 * Returns the value of index 0 of a given tag as a
 * byte.  The caller is responsible for ensuring that the tag is
 * present and has  type TIFFField.TIFF_SBYTE, TIFF_BYTE, or
 * TIFF_UNDEFINED.
 */
/**
 * Returns the value of index 0 of a given tag as a
 * long.  The caller is responsible for ensuring that the tag is
 * present and has type TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED,
 * TIFF_SHORT, TIFF_SSHORT, TIFF_SLONG or TIFF_LONG.
 */
/**
 * Returns the value of index 0 of a given tag as a float.  The
 * caller is responsible for ensuring that the tag is present and
 * has numeric type (all but TIFF_UNDEFINED and TIFF_ASCII).
 */
/**
 * Returns the value of index 0 of a given tag as a double.  The
 * caller is responsible for ensuring that the tag is present and
 * has numeric type (all but TIFF_UNDEFINED and TIFF_ASCII).
 */
