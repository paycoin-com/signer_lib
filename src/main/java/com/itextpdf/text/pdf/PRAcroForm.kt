/*
 * $Id: b6ad983afb84503f7876a737e0b1b021b2c01038 $
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

import java.util.ArrayList
import java.util.HashMap

/**
 * This class captures an AcroForm on input. Basically, it extends Dictionary
 * by indexing the fields of an AcroForm
 * @author Mark Thompson
 */

class PRAcroForm
/**
 * Constructor
 * @param reader reader of the input file
 */
(internal var reader: PdfReader) : PdfDictionary() {

    /**
     * This class holds the information for a single field
     */
    class FieldInformation internal constructor(fieldName: String, info: PdfDictionary, ref: PRIndirectReference) {
        /**
         * Returns the full name of the field.
         * @return    a String or null
         */
        var name: String
            internal set
        var info: PdfDictionary
            internal set
        var ref: PRIndirectReference
            internal set

        init {
            this.name = fieldName
            this.info = info
            this.ref = ref
        }

        /**
         * Returns the name of the widget annotation (the /NM entry).
         * @return    a String or null (if there's no /NM key)
         */
        val widgetName: String?
            get() {
                val name = info.get(PdfName.NM)
                if (name != null)
                    return name.toString()
                return null
            }
    }

    var fields: ArrayList<FieldInformation>
        internal set
    internal var stack: ArrayList<PdfDictionary>
    internal var fieldByName: HashMap<String, FieldInformation>

    init {
        fields = ArrayList<FieldInformation>()
        fieldByName = HashMap<String, FieldInformation>()
        stack = ArrayList<PdfDictionary>()
    }

    /**
     * Number of fields found
     * @return size
     */
    override fun size(): Int {
        return fields.size
    }

    fun getField(name: String): FieldInformation {
        return fieldByName[name]
    }

    /**
     * Given the title (/T) of a reference, return the associated reference
     * @param name a string containing the path
     * *
     * @return a reference to the field, or null
     */
    fun getRefByName(name: String): PRIndirectReference? {
        val fi = fieldByName[name] ?: return null
        return fi.ref
    }

    /**
     * Read, and comprehend the acroform
     * @param root the document root
     */
    fun readAcroForm(root: PdfDictionary?) {
        if (root == null)
            return
        hashMap = root.hashMap
        pushAttrib(root)
        val fieldlist = PdfReader.getPdfObjectRelease(root.get(PdfName.FIELDS)) as PdfArray?
        if (fieldlist != null) {
            iterateFields(fieldlist, null, null)
        }
    }

    /**
     * After reading, we index all of the fields. Recursive.
     * @param fieldlist An array of fields
     * *
     * @param fieldDict the last field dictionary we encountered (recursively)
     * *
     * @param parentPath the pathname of the field, up to this point or null
     */
    protected fun iterateFields(fieldlist: PdfArray, fieldDict: PRIndirectReference?, parentPath: String?) {
        val it = fieldlist.listIterator()
        while (it.hasNext()) {
            val ref = it.next() as PRIndirectReference
            val dict = PdfReader.getPdfObjectRelease(ref) as PdfDictionary?

            // if we are not a field dictionary, pass our parent's values
            var myFieldDict = fieldDict
            var fullPath: String = parentPath
            val tField = dict.get(PdfName.T) as PdfString?
            val isFieldDict = tField != null

            if (isFieldDict) {
                myFieldDict = ref
                if (parentPath == null) {
                    fullPath = tField!!.toString()
                } else {
                    fullPath = parentPath + '.' + tField!!.toString()
                }
            }

            val kids = dict.get(PdfName.KIDS) as PdfArray?
            if (kids != null) {
                pushAttrib(dict)
                iterateFields(kids, myFieldDict, fullPath)
                stack.removeAt(stack.size - 1)   // pop
            } else {
                // leaf node
                if (myFieldDict != null) {
                    var mergedDict = stack[stack.size - 1]
                    if (isFieldDict)
                        mergedDict = mergeAttrib(mergedDict, dict)

                    mergedDict.put(PdfName.T, PdfString(fullPath))
                    val fi = FieldInformation(fullPath, mergedDict, myFieldDict)
                    fields.add(fi)
                    fieldByName.put(fullPath, fi)
                }
            }
        }
    }

    /**
     * merge field attributes from two dictionaries
     * @param parent one dictionary
     * *
     * @param child the other dictionary
     * *
     * @return a merged dictionary
     */
    protected fun mergeAttrib(parent: PdfDictionary?, child: PdfDictionary): PdfDictionary {
        val targ = PdfDictionary()
        if (parent != null) targ.putAll(parent)

        for (element in child.keys) {
            if (element == PdfName.DR || element == PdfName.DA ||
                    element == PdfName.Q || element == PdfName.FF ||
                    element == PdfName.DV || element == PdfName.V
                    || element == PdfName.FT || element == PdfName.NM
                    || element == PdfName.F) {
                targ.put(element, child.get(element))
            }
        }
        return targ
    }

    /**
     * stack a level of dictionary. Merge in a dictionary from this level
     */
    protected fun pushAttrib(dict: PdfDictionary) {
        var dic: PdfDictionary? = null
        if (!stack.isEmpty()) {
            dic = stack[stack.size - 1]
        }
        dic = mergeAttrib(dic, dict)
        stack.add(dic)
    }
}
