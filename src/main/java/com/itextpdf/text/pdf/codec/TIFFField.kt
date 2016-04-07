/*
 * Copyright 2003-2012 by Paulo Soares.
 *
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageio.plugins.tiff.TIFFField.java)
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

import java.io.Serializable

/**
 * A class representing a field in a TIFF 6.0 Image File Directory.

 *
 *  The TIFF file format is described in more detail in the
 * comments for the TIFFDescriptor class.

 *
 *  A field in a TIFF Image File Directory (IFD).  A field is defined
 * as a sequence of values of identical data type.  TIFF 6.0 defines
 * 12 data types, which are mapped internally onto the Java data types
 * byte, int, long, float, and double.

 *
 * ** This class is not a committed part of the JAI API.  It may
 * be removed or changed in future releases of JAI.**

 * @see TIFFDirectory
 */
class TIFFField : Any, Comparable<TIFFField>, Serializable {

    /** The tag number.  */
    /**
     * Returns the tag number, between 0 and 65535.
     */
    var tag: Int = 0
        internal set

    /** The tag type.  */
    /**
     * Returns the type of the data stored in the IFD.
     * For a TIFF6.0 file, the value will equal one of the
     * TIFF_ constants defined in this class.  For future
     * revisions of TIFF, higher values are possible.

     */
    var type: Int = 0
        internal set

    /** The number of data items present in the field.  */
    /**
     * Returns the number of elements in the IFD.
     */
    var count: Int = 0
        internal set

    /** The field data.  */
    internal var data: Any

    /** The default constructor.  */
    internal constructor() {
    }

    /**
     * Constructs a TIFFField with arbitrary data.  The data
     * parameter must be an array of a Java type appropriate for the
     * type of the TIFF field.  Since there is no available 32-bit
     * unsigned data type, long is used. The mapping between types is
     * as follows:

     *
     *
     *  TIFF type   Java type
     *
     * TIFF_BYTE      byte
     *
     * TIFF_ASCII     String
     *
     * TIFF_SHORT     char
     *
     * TIFF_LONG      long
     *
     * TIFF_RATIONAL  long[2]
     *
     * TIFF_SBYTE     byte
     *
     * TIFF_UNDEFINED byte
     *
     * TIFF_SSHORT    short
     *
     * TIFF_SLONG     int
     *
     * TIFF_SRATIONAL int[2]
     *
     * TIFF_FLOAT     float
     *
     * TIFF_DOUBLE    double
     *
     */
    constructor(tag: Int, type: Int, count: Int, data: Any) {
        this.tag = tag
        this.type = type
        this.count = count
        this.data = data
    }

    /**
     * Returns the data as an uninterpreted array of bytes.
     * The type of the field must be one of TIFF_BYTE, TIFF_SBYTE,
     * or TIFF_UNDEFINED;

     *
     *  For data in TIFF_BYTE format, the application must take
     * care when promoting the data to longer integral types
     * to avoid sign extension.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_BYTE, TIFF_SBYTE, or TIFF_UNDEFINED.
     */
    val asBytes: ByteArray
        get() = data as ByteArray

    /**
     * Returns TIFF_SHORT data as an array of chars (unsigned 16-bit
     * integers).

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_SHORT.
     */
    val asChars: CharArray
        get() = data as CharArray

    /**
     * Returns TIFF_SSHORT data as an array of shorts (signed 16-bit
     * integers).

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_SSHORT.
     */
    val asShorts: ShortArray
        get() = data as ShortArray

    /**
     * Returns TIFF_SLONG data as an array of ints (signed 32-bit
     * integers).

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_SLONG.
     */
    val asInts: IntArray
        get() = data as IntArray

    /**
     * Returns TIFF_LONG data as an array of longs (signed 64-bit
     * integers).

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_LONG.
     */
    val asLongs: LongArray
        get() = data as LongArray

    /**
     * Returns TIFF_FLOAT data as an array of floats.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_FLOAT.
     */
    val asFloats: FloatArray
        get() = data as FloatArray

    /**
     * Returns TIFF_DOUBLE data as an array of doubles.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_DOUBLE.
     */
    val asDoubles: DoubleArray
        get() = data as DoubleArray

    /**
     * Returns TIFF_SRATIONAL data as an array of 2-element arrays of ints.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_SRATIONAL.
     */
    val asSRationals: Array<IntArray>
        get() = data as Array<IntArray>

    /**
     * Returns TIFF_RATIONAL data as an array of 2-element arrays of longs.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_RATTIONAL.
     */
    val asRationals: Array<LongArray>
        get() = data as Array<LongArray>

    /**
     * Returns data in TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT,
     * TIFF_SSHORT, or TIFF_SLONG format as an int.

     *
     *  TIFF_BYTE and TIFF_UNDEFINED data are treated as unsigned;
     * that is, no sign extension will take place and the returned
     * value will be in the range [0, 255].  TIFF_SBYTE data will
     * be returned in the range [-128, 127].

     *
     *  A ClassCastException will be thrown if the field is not of
     * type TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT,
     * TIFF_SSHORT, or TIFF_SLONG.
     */
    fun getAsInt(index: Int): Int {
        when (type) {
            TIFF_BYTE, TIFF_UNDEFINED -> return (data as ByteArray)[index] and 0xff
            TIFF_SBYTE -> return (data as ByteArray)[index].toInt()
            TIFF_SHORT -> return (data as CharArray)[index].toInt() and 0xffff
            TIFF_SSHORT -> return (data as ShortArray)[index].toInt()
            TIFF_SLONG -> return (data as IntArray)[index]
            else -> throw ClassCastException()
        }
    }

    /**
     * Returns data in TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT,
     * TIFF_SSHORT, TIFF_SLONG, or TIFF_LONG format as a long.

     *
     *  TIFF_BYTE and TIFF_UNDEFINED data are treated as unsigned;
     * that is, no sign extension will take place and the returned
     * value will be in the range [0, 255].  TIFF_SBYTE data will
     * be returned in the range [-128, 127].

     *
     *  A ClassCastException will be thrown if the field is not of
     * type TIFF_BYTE, TIFF_SBYTE, TIFF_UNDEFINED, TIFF_SHORT,
     * TIFF_SSHORT, TIFF_SLONG, or TIFF_LONG.
     */
    fun getAsLong(index: Int): Long {
        when (type) {
            TIFF_BYTE, TIFF_UNDEFINED -> return ((data as ByteArray)[index] and 0xff).toLong()
            TIFF_SBYTE -> return (data as ByteArray)[index].toLong()
            TIFF_SHORT -> return ((data as CharArray)[index].toInt() and 0xffff).toLong()
            TIFF_SSHORT -> return (data as ShortArray)[index].toLong()
            TIFF_SLONG -> return (data as IntArray)[index].toLong()
            TIFF_LONG -> return (data as LongArray)[index]
            else -> throw ClassCastException()
        }
    }

    /**
     * Returns data in any numerical format as a float.  Data in
     * TIFF_SRATIONAL or TIFF_RATIONAL format are evaluated by
     * dividing the numerator into the denominator using
     * double-precision arithmetic and then truncating to single
     * precision.  Data in TIFF_SLONG, TIFF_LONG, or TIFF_DOUBLE
     * format may suffer from truncation.

     *
     *  A ClassCastException will be thrown if the field is
     * of type TIFF_UNDEFINED or TIFF_ASCII.
     */
    fun getAsFloat(index: Int): Float {
        when (type) {
            TIFF_BYTE -> return ((data as ByteArray)[index] and 0xff).toFloat()
            TIFF_SBYTE -> return (data as ByteArray)[index].toFloat()
            TIFF_SHORT -> return ((data as CharArray)[index].toInt() and 0xffff).toFloat()
            TIFF_SSHORT -> return (data as ShortArray)[index].toFloat()
            TIFF_SLONG -> return (data as IntArray)[index].toFloat()
            TIFF_LONG -> return (data as LongArray)[index].toFloat()
            TIFF_FLOAT -> return (data as FloatArray)[index]
            TIFF_DOUBLE -> return (data as DoubleArray)[index].toFloat()
            TIFF_SRATIONAL -> {
                val ivalue = getAsSRational(index)
                return (ivalue[0].toDouble() / ivalue[1]).toFloat()
            }
            TIFF_RATIONAL -> {
                val lvalue = getAsRational(index)
                return (lvalue[0].toDouble() / lvalue[1]).toFloat()
            }
            else -> throw ClassCastException()
        }
    }

    /**
     * Returns data in any numerical format as a float.  Data in
     * TIFF_SRATIONAL or TIFF_RATIONAL format are evaluated by
     * dividing the numerator into the denominator using
     * double-precision arithmetic.

     *
     *  A ClassCastException will be thrown if the field is of
     * type TIFF_UNDEFINED or TIFF_ASCII.
     */
    fun getAsDouble(index: Int): Double {
        when (type) {
            TIFF_BYTE -> return ((data as ByteArray)[index] and 0xff).toDouble()
            TIFF_SBYTE -> return (data as ByteArray)[index].toDouble()
            TIFF_SHORT -> return ((data as CharArray)[index].toInt() and 0xffff).toDouble()
            TIFF_SSHORT -> return (data as ShortArray)[index].toDouble()
            TIFF_SLONG -> return (data as IntArray)[index].toDouble()
            TIFF_LONG -> return (data as LongArray)[index].toDouble()
            TIFF_FLOAT -> return (data as FloatArray)[index].toDouble()
            TIFF_DOUBLE -> return (data as DoubleArray)[index]
            TIFF_SRATIONAL -> {
                val ivalue = getAsSRational(index)
                return ivalue[0].toDouble() / ivalue[1]
            }
            TIFF_RATIONAL -> {
                val lvalue = getAsRational(index)
                return lvalue[0].toDouble() / lvalue[1]
            }
            else -> throw ClassCastException()
        }
    }

    /**
     * Returns a TIFF_ASCII data item as a String.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_ASCII.
     */
    fun getAsString(index: Int): String {
        return (data as Array<String>)[index]
    }

    /**
     * Returns a TIFF_SRATIONAL data item as a two-element array
     * of ints.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_SRATIONAL.
     */
    fun getAsSRational(index: Int): IntArray {
        return (data as Array<IntArray>)[index]
    }

    /**
     * Returns a TIFF_RATIONAL data item as a two-element array
     * of ints.

     *
     *  A ClassCastException will be thrown if the field is not
     * of type TIFF_RATIONAL.
     */
    fun getAsRational(index: Int): LongArray {
        if (type == TIFF_LONG)
            return asLongs
        return (data as Array<LongArray>)[index]
    }

    /**
     * Compares this `TIFFField` with another
     * `TIFFField` by comparing the tags.

     *
     * **Note: this class has a natural ordering that is inconsistent
     * with `equals()`.**

     * @throws IllegalArgumentException if the parameter is `null`.
     */
    override fun compareTo(o: TIFFField): Int {
        if (o == null) {
            throw IllegalArgumentException()
        }

        val oTag = o.tag

        if (tag < oTag) {
            return -1
        } else if (tag > oTag) {
            return 1
        } else {
            return 0
        }
    }

    companion object {

        private val serialVersionUID = 9088332901412823834L

        /** Flag for 8 bit unsigned integers.  */
        val TIFF_BYTE = 1

        /** Flag for null-terminated ASCII strings.  */
        val TIFF_ASCII = 2

        /** Flag for 16 bit unsigned integers.  */
        val TIFF_SHORT = 3

        /** Flag for 32 bit unsigned integers.  */
        val TIFF_LONG = 4

        /** Flag for pairs of 32 bit unsigned integers.  */
        val TIFF_RATIONAL = 5

        /** Flag for 8 bit signed integers.  */
        val TIFF_SBYTE = 6

        /** Flag for 8 bit uninterpreted bytes.  */
        val TIFF_UNDEFINED = 7

        /** Flag for 16 bit signed integers.  */
        val TIFF_SSHORT = 8

        /** Flag for 32 bit signed integers.  */
        val TIFF_SLONG = 9

        /** Flag for pairs of 32 bit signed integers.  */
        val TIFF_SRATIONAL = 10

        /** Flag for 32 bit IEEE floats.  */
        val TIFF_FLOAT = 11

        /** Flag for 64 bit IEEE doubles.  */
        val TIFF_DOUBLE = 12
    }
}
