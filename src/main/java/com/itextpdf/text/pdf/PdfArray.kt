/*
 * $Id: e6b9da03a833953c0922cea5074abf4685fefd43 $
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

import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList

/**
 * PdfArray is the PDF Array object.
 *
 * An array is a sequence of PDF objects. An array may contain a mixture of
 * object types.
 * An array is written as a left square bracket ([), followed by a sequence of
 * objects, followed by a right square bracket (]).
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.5 (page 58).

 * @see PdfObject
 */
open class PdfArray : PdfObject, Iterable<PdfObject> {

    // CLASS VARIABLES

    /** this is the actual array of PdfObjects  */
    /**
     * Get the internal arrayList for this PdfArray.  Not Recommended.

     * @return the internal ArrayList.  Naughty Naughty.
     */
    var arrayList: ArrayList<PdfObject>
        protected set

    // constructors

    /**
     * Constructs an empty PdfArray-object.
     */
    constructor() : super(PdfObject.ARRAY) {
        arrayList = ArrayList<PdfObject>()
    }

    constructor(capacity: Int) : super(PdfObject.ARRAY) {
        arrayList = ArrayList<PdfObject>(capacity)
    }

    /**
     * Constructs an PdfArray-object, containing 1
     * PdfObject.

     * @param    object        a PdfObject that has to be added to the array
     */
    constructor(`object`: PdfObject) : super(PdfObject.ARRAY) {
        arrayList = ArrayList<PdfObject>()
        arrayList.add(`object`)
    }

    /**
     * Constructs a PdfArray-object, containing all
     * float values in a specified array.

     * The float values are internally converted to
     * PdfNumber objects.

     * @param values    an array of float values to be added
     */
    constructor(values: FloatArray) : super(PdfObject.ARRAY) {
        arrayList = ArrayList<PdfObject>()
        add(values)
    }

    /**
     * Constructs a PdfArray-object, containing all
     * int values in a specified array.

     * The int values are internally converted to
     * PdfNumber objects.

     * @param values    an array of int values to be added
     */
    constructor(values: IntArray) : super(PdfObject.ARRAY) {
        arrayList = ArrayList<PdfObject>()
        add(values)
    }

    /**
     * Constructs a PdfArray, containing all elements of a
     * specified ArrayList.

     * @param l    an ArrayList with PdfObjects to be
     * *   added to the array
     * *
     * @throws ClassCastException if the ArrayList contains
     * *   something that isn't a PdfObject
     * *
     * @since 2.1.3
     */
    constructor(l: List<PdfObject>) : this() {
        for (element in l)
            add(element)
    }

    /**
     * Constructs an PdfArray-object, containing all
     * PdfObjects in a specified PdfArray.

     * @param array    a PdfArray to be added to the array
     */
    constructor(array: PdfArray) : super(PdfObject.ARRAY) {
        arrayList = ArrayList(array.arrayList)
    }

    // METHODS OVERRIDING SOME PDFOBJECT METHODS

    /**
     * Writes the PDF representation of this PdfArray as an array
     * of byte to the specified OutputStream.

     * @param writer for backwards compatibility
     * *
     * @param os the OutputStream to write the bytes to.
     */
    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_OBJECT, this)
        os.write('[')

        val i = arrayList.iterator()
        var `object`: PdfObject?
        var type = 0
        if (i.hasNext()) {
            `object` = i.next()
            if (`object` == null)
                `object` = PdfNull.PDFNULL
            `object`!!.toPdf(writer, os)
        }
        while (i.hasNext()) {
            `object` = i.next()
            if (`object` == null)
                `object` = PdfNull.PDFNULL
            type = `object`!!.type()
            if (type != PdfObject.ARRAY && type != PdfObject.DICTIONARY && type != PdfObject.NAME && type != PdfObject.STRING)
                os.write(' ')
            `object`.toPdf(writer, os)
        }
        os.write(']')
    }

    /**
     * Returns a string representation of this PdfArray.

     * The string representation consists of a list of all
     * PdfObjects contained in this PdfArray,
     * enclosed in square brackets ("[]"). Adjacent elements are separated
     * by the characters ", " (comma and space).

     * @return the string representation of this PdfArray
     */
    override fun toString(): String {
        return arrayList.toString()
    }

    // ARRAY CONTENT METHODS

    /**
     * Overwrites a specified location of the array, returning the previous
     * value

     * @param idx The index of the element to be overwritten
     * *
     * @param obj new value for the specified index
     * *
     * @throws IndexOutOfBoundsException if the specified position doesn't exist
     * *
     * @return the previous value
     * *
     * @since 2.1.5
     */
    operator fun set(idx: Int, obj: PdfObject): PdfObject {
        return arrayList.set(idx, obj)
    }

    /**
     * Remove the element at the specified position from the array.

     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).

     * @param idx The index of the element to be removed.
     * *
     * @throws IndexOutOfBoundsException the specified position doesn't exist
     * *
     * @since 2.1.5
     */
    fun remove(idx: Int): PdfObject {
        return arrayList.removeAt(idx)
    }

    /**
     * Returns the number of entries in the array.

     * @return        the size of the ArrayList
     */
    fun size(): Int {
        return arrayList.size
    }

    /**
     * Returns true if the array is empty.

     * @return true if the array is empty
     * *
     * @since 2.1.5
     */
    val isEmpty: Boolean
        get() = arrayList.isEmpty()

    /**
     * Adds a PdfObject to the end of the PdfArray.

     * The PdfObject will be the last element.

     * @param object PdfObject to add
     * *
     * @return always true
     */
    open fun add(`object`: PdfObject): Boolean {
        return arrayList.add(`object`)
    }

    /**
     * Adds an array of float values to end of the
     * PdfArray.

     * The values will be the last elements.
     * The float values are internally converted to
     * PdfNumber objects.

     * @param values An array of float values to add
     * *
     * @return always true
     */
    open fun add(values: FloatArray): Boolean {
        for (k in values.indices)
            arrayList.add(PdfNumber(values[k]))
        return true
    }

    /**
     * Adds an array of int values to end of the PdfArray.

     * The values will be the last elements.
     * The int values are internally converted to
     * PdfNumber objects.

     * @param values An array of int values to add
     * *
     * @return always true
     */
    open fun add(values: IntArray): Boolean {
        for (k in values.indices)
            arrayList.add(PdfNumber(values[k]))
        return true
    }

    /**
     * Inserts the specified element at the specified position.

     * Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).

     * @param index The index at which the specified element is to be inserted
     * *
     * @param element The element to be inserted
     * *
     * @throws IndexOutOfBoundsException if the specified index is larger than the
     * *   last position currently set, plus 1.
     * *
     * @since 2.1.5
     */
    open fun add(index: Int, element: PdfObject) {
        arrayList.add(index, element)
    }

    /**
     * Inserts a PdfObject at the beginning of the
     * PdfArray.

     * The PdfObject will be the first element, any other elements
     * will be shifted to the right (adds one to their indices).

     * @param object The PdfObject to add
     */
    open fun addFirst(`object`: PdfObject) {
        arrayList.add(0, `object`)
    }

    /**
     * Checks if the PdfArray already contains a certain
     * PdfObject.

     * @param object The PdfObject to check
     * *
     * @return true
     */
    operator fun contains(`object`: PdfObject): Boolean {
        return arrayList.contains(`object`)
    }

    /**
     * Returns the list iterator for the array.

     * @return a ListIterator
     */
    fun listIterator(): ListIterator<PdfObject> {
        return arrayList.listIterator()
    }

    /**
     * Returns the PdfObject with the specified index.

     * A possible indirect references is not resolved, so the returned
     * PdfObject may be either a direct object or an indirect
     * reference, depending on how the object is stored in the
     * PdfArray.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return A PdfObject
     */
    fun getPdfObject(idx: Int): PdfObject {
        return arrayList[idx]
    }

    /**
     * Returns the PdfObject with the specified index, resolving
     * a possible indirect reference to a direct object.

     * Thus this method will never return a PdfIndirectReference
     * object.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return A direct PdfObject or null
     */
    fun getDirectObject(idx: Int): PdfObject? {
        return PdfReader.getPdfObject(getPdfObject(idx))
    }

    // DOWNCASTING GETTERS
    // @author Mark A Storer (2/17/06)

    /**
     * Returns a PdfObject as a PdfDictionary,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolvedto a direct object.
     * If it is a PdfDictionary, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfDictionary object,
     * *   or null
     */
    fun getAsDict(idx: Int): PdfDictionary {
        var dict: PdfDictionary? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isDictionary)
            dict = orig as PdfDictionary?
        return dict
    }

    /**
     * Returns a PdfObject as a PdfArray,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfArray, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfArray object,
     * *   or null
     */
    fun getAsArray(idx: Int): PdfArray {
        var array: PdfArray? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isArray)
            array = orig as PdfArray?
        return array
    }

    /**
     * Returns a PdfObject as a PdfStream,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfStream, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfStream object,
     * *   or null
     */
    fun getAsStream(idx: Int): PdfStream {
        var stream: PdfStream? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isStream)
            stream = orig as PdfStream?
        return stream
    }

    /**
     * Returns a PdfObject as a PdfString,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfString, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfString object,
     * *   or null
     */
    fun getAsString(idx: Int): PdfString {
        var string: PdfString? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isString)
            string = orig as PdfString?
        return string
    }

    /**
     * Returns a PdfObject as a PdfNumber,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfNumber, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfNumber object,
     * *   or null
     */
    fun getAsNumber(idx: Int): PdfNumber {
        var number: PdfNumber? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isNumber)
            number = orig as PdfNumber?
        return number
    }

    /**
     * Returns a PdfObject as a PdfName,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfName, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfName object,
     * *   or null
     */
    fun getAsName(idx: Int): PdfName {
        var name: PdfName? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isName)
            name = orig as PdfName?
        return name
    }

    /**
     * Returns a PdfObject as a PdfBoolean,
     * resolving indirect references.

     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a PdfBoolean, it is cast down and returned as
     * such. Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfBoolean object,
     * *   or null
     */
    fun getAsBoolean(idx: Int): PdfBoolean {
        var bool: PdfBoolean? = null
        val orig = getDirectObject(idx)
        if (orig != null && orig.isBoolean)
            bool = orig as PdfBoolean?
        return bool
    }

    /**
     * Returns a PdfObject as a PdfIndirectReference.

     * The object corresponding to the specified index is retrieved.
     * If it is a PdfIndirectReference, it is cast down and
     * returned as such. Otherwise null is returned.

     * @param idx The index of the PdfObject to be returned
     * *
     * @return the corresponding PdfIndirectReference object,
     * *   or null
     */
    fun getAsIndirectObject(idx: Int): PdfIndirectReference {
        var ref: PdfIndirectReference? = null
        val orig = getPdfObject(idx) // not getDirect this time.
        if (orig is PdfIndirectReference)
            ref = orig
        return ref
    }

    /**
     * @return an iterator that iterates over the [PdfObject]s in this PdfArray.
     */
    override fun iterator(): Iterator<PdfObject> {
        return arrayList.iterator()
    }

    /**

     * @return this PdfArray's values as a long[]
     * *
     * @since 5.3.5
     */
    fun asLongArray(): LongArray {
        val rslt = LongArray(size())
        for (k in rslt.indices) {
            rslt[k] = getAsNumber(k).longValue()
        }
        return rslt
    }

    /**

     * @return this PdfArray's values as a double[]
     * *
     * @since 5.5.6
     */
    fun asDoubleArray(): DoubleArray {
        val rslt = DoubleArray(size())
        for (k in rslt.indices) {
            rslt[k] = getAsNumber(k).doubleValue()
        }
        return rslt
    }
}
