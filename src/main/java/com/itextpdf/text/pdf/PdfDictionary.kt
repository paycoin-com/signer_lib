/*
 * $Id: 43910629170276e8828fe78033bbdcdcfb4ae98d $
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
import java.util.LinkedHashMap

/**
 * PdfDictionary is the Pdf dictionary object.
 *
 * A dictionary is an associative table containing pairs of objects.
 * The first element of each pair is called the key and the second
 * element is called the value.
 * Unlike dictionaries in the PostScript language, a key must be a
 * PdfName.
 * A value can be any kind of PdfObject, including a dictionary.
 * A dictionary is generally used to collect and tie together the attributes
 * of a complex object, with each key-value pair specifying the name and value
 * of an attribute.
 * A dictionary is represented by two left angle brackets (<<), followed by a
 * sequence of key-value pairs, followed by two right angle brackets (>>).
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.6 (page 59-60).
 *

 * @see PdfObject

 * @see PdfName

 * @see BadPdfFormatException
 */
open class PdfDictionary : PdfObject {

    // CLASS VARIABLES

    /** This is the type of this dictionary  */
    private var dictionaryType: PdfName? = null

    /** This is the hashmap that contains all the values and keys of the dictionary  */
    protected var hashMap: LinkedHashMap<PdfName, PdfObject>

    // CONSTRUCTORS

    /**
     * Constructs an empty PdfDictionary-object.
     */
    constructor() : super(PdfObject.DICTIONARY) {
        hashMap = LinkedHashMap<PdfName, PdfObject>()
    }

    constructor(capacity: Int) : super(PdfObject.DICTIONARY) {
        hashMap = LinkedHashMap<PdfName, PdfObject>(capacity)
    }

    /**
     * Constructs a PdfDictionary-object of a certain type.

     * @param type a PdfName
     */
    constructor(type: PdfName) : this() {
        dictionaryType = type
        put(PdfName.TYPE, dictionaryType)
    }

    // METHODS OVERRIDING SOME PDFOBJECT METHODS

    /**
     * Writes the PDF representation of this PdfDictionary as an
     * array of byte to the given OutputStream.

     * @param writer for backwards compatibility
     * *
     * @param os the OutputStream to write the bytes to.
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_OBJECT, this)
        os.write('<')
        os.write('<')
        // loop over all the object-pairs in the HashMap
        var value: PdfObject
        var type = 0
        for (e in hashMap.entries) {
            e.key.toPdf(writer, os)
            value = e.value
            type = value.type()
            if (type != PdfObject.ARRAY && type != PdfObject.DICTIONARY && type != PdfObject.NAME && type != PdfObject.STRING)
                os.write(' ')
            value.toPdf(writer, os)
        }
        os.write('>')
        os.write('>')
    }

    /**
     * Returns a string representation of this PdfDictionary.

     * The string doesn't contain any of the content of this dictionary.
     * Rather the string "dictionary" is returned, possibly followed by the
     * type of this PdfDictionary, if set.

     * @return the string representation of this PdfDictionary
     * *
     * @see com.itextpdf.text.pdf.PdfObject.toString
     */
    override fun toString(): String {
        if (get(PdfName.TYPE) == null)
            return "Dictionary"
        return "Dictionary of type: " + get(PdfName.TYPE)!!
    }

    // DICTIONARY CONTENT METHODS

    /**
     * Associates the specified PdfObject as value with
     * the specified PdfName as key in this map.

     * If the map previously contained a mapping for this key, the
     * old value is replaced. If the value is
     * null or PdfNull the key is deleted.

     * @param key a PdfName
     * *
     * @param object the PdfObject to be associated with the
     * *   key
     */
    fun put(key: PdfName, `object`: PdfObject?) {
        if (`object` == null || `object`.isNull)
            hashMap.remove(key)
        else
            hashMap.put(key, `object`)
    }

    /**
     * Associates the specified PdfObject as value to the
     * specified PdfName as key in this map.

     * If the value is a PdfNull, it is treated just as
     * any other PdfObject. If the value is
     * null however nothing is done.

     * @param key a PdfName
     * *
     * @param value the PdfObject to be associated to the
     * * key
     */
    fun putEx(key: PdfName, value: PdfObject?) {
        if (value == null)
            return
        put(key, value)
    }

    /**
     * Copies all of the mappings from the specified PdfDictionary
     * to this PdfDictionary.

     * These mappings will replace any mappings previously contained in this
     * PdfDictionary.

     * @param dic The PdfDictionary with the mappings to be
     * *   copied over
     */
    fun putAll(dic: PdfDictionary) {
        hashMap.putAll(dic.hashMap)
    }

    /**
     * Removes a PdfObject and its key from the
     * PdfDictionary.

     * @param key a PdfName
     */
    fun remove(key: PdfName) {
        hashMap.remove(key)
    }

    /**
     * Removes all the PdfObjects and its keys from the
     * PdfDictionary.
     * @since 5.0.2
     */
    fun clear() {
        hashMap.clear()
    }

    /**
     * Returns the PdfObject associated to the specified
     * key.

     * @param key a PdfName
     * *
     * @return the PdfObject previously associated to the
     * *   key
     */
    operator fun get(key: PdfName): PdfObject? {
        return hashMap[key]
    }

    /**
     * Returns the PdfObject associated to the specified
     * key, resolving a possible indirect reference to a direct
     * object.

     * This method will never return a PdfIndirectReference
     * object.

     * @param key A key for the PdfObject to be returned
     * *
     * @return A direct PdfObject or null
     */
    fun getDirectObject(key: PdfName): PdfObject? {
        return PdfReader.getPdfObject(get(key))
    }

    /**
     * Get all keys that are set.

     * @return true if it is, otherwise false.
     */
    val keys: Set<PdfName>
        get() = hashMap.keys

    /**
     * Returns the number of key-value mappings in this
     * PdfDictionary.

     * @return the number of key-value mappings in this
     * *   PdfDictionary.
     */
    open fun size(): Int {
        return hashMap.size
    }

    /**
     * Returns true if this PdfDictionary contains a
     * mapping for the specified key.

     * @return true if the key is set, otherwise false.
     */
    operator fun contains(key: PdfName): Boolean {
        return hashMap.containsKey(key)
    }

    // DICTIONARY TYPE METHODS

    /**
     * Checks if a Dictionary is of the type FONT.

     * @return true if it is, otherwise false.
     */
    val isFont: Boolean
        get() = checkType(FONT)

    /**
     * Checks if a Dictionary is of the type PAGE.

     * @return true if it is, otherwise false.
     */
    val isPage: Boolean
        get() = checkType(PAGE)

    /**
     * Checks if a Dictionary is of the type PAGES.

     * @return true if it is, otherwise false.
     */
    val isPages: Boolean
        get() = checkType(PAGES)

    /**
     * Checks if a Dictionary is of the type CATALOG.

     * @return true if it is, otherwise false.
     */
    val isCatalog: Boolean
        get() = checkType(CATALOG)

    /**
     * Checks if a Dictionary is of the type OUTLINES.

     * @return true if it is, otherwise false.
     */
    val isOutlineTree: Boolean
        get() = checkType(OUTLINES)

    /**
     * Checks the type of the dictionary.
     * @param type the type you're looking for
     * *
     * @return true if the type of the dictionary corresponds with the type you're looking for
     */
    fun checkType(type: PdfName?): Boolean {
        if (type == null)
            return false
        if (dictionaryType == null)
            dictionaryType = getAsName(PdfName.TYPE)
        return type == dictionaryType
    }

    // OTHER METHODS

    fun merge(other: PdfDictionary) {
        hashMap.putAll(other.hashMap)
    }

    fun mergeDifferent(other: PdfDictionary) {
        for (key in other.hashMap.keys) {
            if (!hashMap.containsKey(key))
                hashMap.put(key, other.hashMap[key])
        }
    }

    // DOWNCASTING GETTERS
    // @author Mark A Storer (2/17/06)

    /**
     * Returns a PdfObject as a PdfDictionary,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfDictionary, it is cast down and returned as
     * such. Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfDictionary object,
     * *   or null
     */
    fun getAsDict(key: PdfName): PdfDictionary {
        var dict: PdfDictionary? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isDictionary)
            dict = orig as PdfDictionary?
        return dict
    }

    /**
     * Returns a PdfObject as a PdfArray,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfArray, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfArray object,
     * *   or null
     */
    fun getAsArray(key: PdfName): PdfArray {
        var array: PdfArray? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isArray)
            array = orig as PdfArray?
        return array
    }

    /**
     * Returns a PdfObject as a PdfStream,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfStream, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfStream object,
     * *   or null
     */
    fun getAsStream(key: PdfName): PdfStream {
        var stream: PdfStream? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isStream)
            stream = orig as PdfStream?
        return stream
    }

    /**
     * Returns a PdfObject as a PdfString,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfString, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfString object,
     * *   or null
     */
    fun getAsString(key: PdfName): PdfString {
        var string: PdfString? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isString)
            string = orig as PdfString?
        return string
    }

    /**
     * Returns a PdfObject as a PdfNumber,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfNumber, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfNumber object,
     * *   or null
     */
    fun getAsNumber(key: PdfName): PdfNumber {
        var number: PdfNumber? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isNumber)
            number = orig as PdfNumber?
        return number
    }

    /**
     * Returns a PdfObject as a PdfName,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfName, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfName object,
     * *   or null
     */
    fun getAsName(key: PdfName): PdfName {
        var name: PdfName? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isName)
            name = orig as PdfName?
        return name
    }

    /**
     * Returns a PdfObject as a PdfBoolean,
     * resolving indirect references.

     * The object associated with the PdfName given is retrieved
     * and resolved to a direct object.
     * If it is a PdfBoolean, it is cast down and returned as such.
     * Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfBoolean object,
     * *   or null
     */
    fun getAsBoolean(key: PdfName): PdfBoolean {
        var bool: PdfBoolean? = null
        val orig = getDirectObject(key)
        if (orig != null && orig.isBoolean)
            bool = orig as PdfBoolean?
        return bool
    }

    /**
     * Returns a PdfObject as a PdfIndirectReference.

     * The object associated with the PdfName given is retrieved
     * If it is a PdfIndirectReference, it is cast down and returned
     * as such. Otherwise null is returned.

     * @param key A PdfName
     * *
     * @return the associated PdfIndirectReference object,
     * *   or null
     */
    fun getAsIndirectObject(key: PdfName): PdfIndirectReference {
        var ref: PdfIndirectReference? = null
        val orig = get(key) // not getDirect this time.
        if (orig != null && orig.isIndirect)
            ref = orig as PdfIndirectReference?
        return ref
    }

    companion object {

        // CONSTANTS

        /** This is a possible type of dictionary  */
        val FONT = PdfName.FONT

        /** This is a possible type of dictionary  */
        val OUTLINES = PdfName.OUTLINES

        /** This is a possible type of dictionary  */
        val PAGE = PdfName.PAGE

        /** This is a possible type of dictionary  */
        val PAGES = PdfName.PAGES

        /** This is a possible type of dictionary  */
        val CATALOG = PdfName.CATALOG
    }
}
