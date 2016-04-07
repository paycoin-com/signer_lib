/*
 * $Id: 56a2df5cf770c315d5045bb28789d6f791e66238 $
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

import com.itextpdf.text.pdf.interfaces.IPdfStructureElement

import java.io.IOException
import java.util.HashMap

/**
 * The structure tree root corresponds to the highest hierarchy level in a tagged PDF.
 * @author Paulo Soares
 */
class PdfStructureTreeRoot
/** Creates a new instance of PdfStructureTreeRoot  */
internal constructor(
        /**
         * Holds value of property writer.
         */
        /**
         * Gets the writer.
         * @return the writer
         */
        val writer: PdfWriter) : PdfDictionary(PdfName.STRUCTTREEROOT), IPdfStructureElement {

    private val parentTree = HashMap<Int, PdfObject>()
    /**
     * Gets the reference this object will be written to.
     * @return the reference this object will be written to
     * *
     * @since    2.1.6 method removed in 2.1.5, but restored in 2.1.6
     */
    val reference: PdfIndirectReference
    private var classMap: PdfDictionary? = null
    protected var classes: HashMap<PdfName, PdfObject>? = null
    private var numTree: HashMap<Int, PdfIndirectReference>? = null
    private var idTreeMap: HashMap<String, PdfObject>? = null

    init {
        reference = writer.pdfIndirectReference
    }

    @Throws(IOException::class)
    private fun createNumTree() {
        if (numTree != null) return
        numTree = HashMap<Int, PdfIndirectReference>()
        for (i in parentTree.keys) {
            val obj = parentTree[i]
            if (obj.isArray) {
                val ar = obj as PdfArray
                numTree!!.put(i, writer.addToBody(ar).indirectReference)
            } else if (obj is PdfIndirectReference) {
                numTree!!.put(i, obj)
            }
        }
    }

    /**
     * Maps the user tags to the standard tags. The mapping will allow a standard application to make some sense of the tagged
     * document whatever the user tags may be.
     * @param used the user tag
     * *
     * @param standard the standard tag
     */
    fun mapRole(used: PdfName, standard: PdfName) {
        var rm: PdfDictionary? = get(PdfName.ROLEMAP) as PdfDictionary?
        if (rm == null) {
            rm = PdfDictionary()
            put(PdfName.ROLEMAP, rm)
        }
        rm.put(used, standard)
    }

    fun mapClass(name: PdfName, `object`: PdfObject) {
        if (classMap == null) {
            classMap = PdfDictionary()
            classes = HashMap<PdfName, PdfObject>()
        }
        classes!!.put(name, `object`)
    }

    internal fun putIDTree(record: String, reference: PdfObject) {
        if (idTreeMap == null)
            idTreeMap = HashMap<String, PdfObject>()
        idTreeMap!!.put(record, reference)
    }

    fun getMappedClass(name: PdfName): PdfObject? {
        if (classes == null)
            return null
        return classes!![name]
    }

    @Throws(IOException::class)
    fun getNumTree(): HashMap<Int, PdfIndirectReference> {
        if (numTree == null) createNumTree()
        return numTree
    }

    internal fun setPageMark(page: Int, struc: PdfIndirectReference) {
        val i = Integer.valueOf(page)
        var ar: PdfArray? = parentTree[i] as PdfArray
        if (ar == null) {
            ar = PdfArray()
            parentTree.put(i, ar)
        }
        ar.add(struc)
    }

    internal fun setAnnotationMark(structParentIndex: Int, struc: PdfIndirectReference) {
        parentTree.put(Integer.valueOf(structParentIndex), struc)
    }

    @Throws(IOException::class)
    internal fun buildTree() {
        createNumTree()
        val dicTree = PdfNumberTree.writeTree(numTree, writer)
        if (dicTree != null)
            put(PdfName.PARENTTREE, writer.addToBody(dicTree).indirectReference)
        if (classMap != null && !classes!!.isEmpty()) {
            for (entry in classes!!.entries) {
                val value = entry.value
                if (value.isDictionary)
                    classMap!!.put(entry.key, writer.addToBody(value).indirectReference)
                else if (value.isArray) {
                    val newArray = PdfArray()
                    val array = value as PdfArray
                    for (i in 0..array.size() - 1) {
                        if (array.getPdfObject(i).isDictionary)
                            newArray.add(writer.addToBody(array.getAsDict(i)).indirectReference)
                    }
                    classMap!!.put(entry.key, newArray)
                }
            }
            put(PdfName.CLASSMAP, writer.addToBody(classMap).indirectReference)
        }
        if (idTreeMap != null && !idTreeMap!!.isEmpty()) {
            val dic = PdfNameTree.writeTree(idTreeMap, writer)
            this.put(PdfName.IDTREE, dic)
        }
        writer.addToBody(this, reference)
    }

    /**
     * Gets the first entarance of attribute.
     * @returns PdfObject
     * *
     * @since 5.3.4
     */
    fun getAttribute(name: PdfName): PdfObject? {
        val attr = getAsDict(PdfName.A)
        if (attr != null) {
            if (attr.contains(name))
                return attr.get(name)
        }
        return null
    }

    /**
     * Sets the attribute value.
     * @since 5.3.4
     */
    fun setAttribute(name: PdfName, obj: PdfObject) {
        var attr: PdfDictionary? = getAsDict(PdfName.A)
        if (attr == null) {
            attr = PdfDictionary()
            put(PdfName.A, attr)
        }
        attr.put(name, obj)
    }
}
