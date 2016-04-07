/*
 * $Id: bdeb5b49fa967193b6b2b0a2dd37b57ca40272c1 $
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

import com.itextpdf.text.Rectangle

/** Implements form fields.

 * @author Paulo Soares
 */
class PdfFormField : PdfAnnotation {

    /** Holds value of property parent.  */
    /** Getter for property parent.
     * @return Value of property parent.
     */
    var parent: PdfFormField? = null
        protected set

    var kids: ArrayList<PdfFormField>? = null
        protected set

    /**
     * Constructs a new PdfAnnotation of subtype link (Action).
     */

    constructor(writer: PdfWriter, llx: Float, lly: Float, urx: Float, ury: Float, action: PdfAction) : super(writer, llx, lly, urx, ury, action) {
        put(PdfName.TYPE, PdfName.ANNOT)
        put(PdfName.SUBTYPE, PdfName.WIDGET)
        isAnnotation = true
    }

    /** Creates new PdfFormField  */
    protected constructor(writer: PdfWriter) : super(writer, null) {
        isForm = true
        isAnnotation = false
        role = PdfName.FORM
    }

    fun setWidget(rect: Rectangle, highlight: PdfName?) {
        put(PdfName.TYPE, PdfName.ANNOT)
        put(PdfName.SUBTYPE, PdfName.WIDGET)
        put(PdfName.RECT, PdfRectangle(rect))
        isAnnotation = true
        if (highlight != null && highlight != PdfAnnotation.HIGHLIGHT_INVERT)
            put(PdfName.H, highlight)
    }

    fun setButton(flags: Int) {
        put(PdfName.FT, PdfName.BTN)
        if (flags != 0)
            put(PdfName.FF, PdfNumber(flags))
    }

    fun addKid(field: PdfFormField) {
        field.parent = this
        if (kids == null)
            kids = ArrayList<PdfFormField>()
        kids!!.add(field)
    }

    /**
     * ORs together the given flags with the current /Ff value.
     * @param flags flags to be added.
     * *
     * @return the old flag value
     */
    fun setFieldFlags(flags: Int): Int {
        val obj = get(PdfName.FF) as PdfNumber?
        val old: Int
        if (obj == null)
            old = 0
        else
            old = obj.intValue()
        val v = old or flags
        put(PdfName.FF, PdfNumber(v))
        return old
    }

    fun setValueAsString(s: String) {
        put(PdfName.V, PdfString(s, PdfObject.TEXT_UNICODE))
    }

    fun setValueAsName(s: String) {
        put(PdfName.V, PdfName(s))
    }

    fun setValue(sig: PdfSignature) {
        put(PdfName.V, sig)
    }

    /**
     * Sets the rich value for this field.
     * It is suggested that the regular value of this field be set to an
     * equivalent value.  Rich text values are only supported since PDF 1.5,
     * and require that the FF_RV flag be set.  See PDF Reference chapter
     * 12.7.3.4 for details.
     * @param rv HTML markup for the rich value of this field
     * *
     * @since 5.0.6
     */
    fun setRichValue(rv: String) {
        put(PdfName.RV, PdfString(rv))
    }

    fun setDefaultValueAsString(s: String) {
        put(PdfName.DV, PdfString(s, PdfObject.TEXT_UNICODE))
    }

    fun setDefaultValueAsName(s: String) {
        put(PdfName.DV, PdfName(s))
    }

    fun setFieldName(s: String?) {
        if (s != null)
            put(PdfName.T, PdfString(s, PdfObject.TEXT_UNICODE))
    }

    /**
     * The "user name" is the text shown as a tool.
     * @param s user name.
     */
    fun setUserName(s: String) {
        put(PdfName.TU, PdfString(s, PdfObject.TEXT_UNICODE))
    }

    /**
     * The mapping name is the name this field uses when submitting form data.
     * @param s
     */
    fun setMappingName(s: String) {
        put(PdfName.TM, PdfString(s, PdfObject.TEXT_UNICODE))
    }

    /**
     * Sets text alginment for this field
     * @param v  one of the Q_* contstants
     */
    fun setQuadding(v: Int) {
        put(PdfName.Q, PdfNumber(v))
    }

    override fun setUsed() {
        isUsed = true
        if (parent != null)
            put(PdfName.PARENT, parent!!.indirectReference)
        if (kids != null) {
            val array = PdfArray()
            for (k in kids!!.indices)
                array.add(kids!![k].indirectReference)
            put(PdfName.KIDS, array)
        }
        if (templates == null)
            return
        val dic = PdfDictionary()
        for (template in templates!!) {
            mergeResources(dic, template.resources as PdfDictionary)
        }
        put(PdfName.DR, dic)
    }

    companion object {

        val FF_READ_ONLY = 1
        val FF_REQUIRED = 2
        val FF_NO_EXPORT = 4
        val FF_NO_TOGGLE_TO_OFF = 16384
        val FF_RADIO = 32768
        val FF_PUSHBUTTON = 65536
        val FF_MULTILINE = 4096
        val FF_PASSWORD = 8192
        val FF_COMBO = 131072
        val FF_EDIT = 262144
        val FF_FILESELECT = 1048576
        val FF_MULTISELECT = 2097152
        val FF_DONOTSPELLCHECK = 4194304
        val FF_DONOTSCROLL = 8388608
        val FF_COMB = 16777216
        val FF_RADIOSINUNISON = 1 shl 25
        /**
         * Allows text fields to support rich text.
         * @since 5.0.6
         */
        val FF_RICHTEXT = 1 shl 25
        val Q_LEFT = 0
        val Q_CENTER = 1
        val Q_RIGHT = 2
        val MK_NO_ICON = 0
        val MK_NO_CAPTION = 1
        val MK_CAPTION_BELOW = 2
        val MK_CAPTION_ABOVE = 3
        val MK_CAPTION_RIGHT = 4
        val MK_CAPTION_LEFT = 5
        val MK_CAPTION_OVERLAID = 6
        val IF_SCALE_ALWAYS = PdfName.A
        val IF_SCALE_BIGGER = PdfName.B
        val IF_SCALE_SMALLER = PdfName.S
        val IF_SCALE_NEVER = PdfName.N
        val IF_SCALE_ANAMORPHIC = PdfName.A
        val IF_SCALE_PROPORTIONAL = PdfName.P
        val MULTILINE = true
        val SINGLELINE = false
        val PLAINTEXT = false
        val PASSWORD = true
        internal var mergeTarget = arrayOf(PdfName.FONT, PdfName.XOBJECT, PdfName.COLORSPACE, PdfName.PATTERN)

        fun createEmpty(writer: PdfWriter): PdfFormField {
            val field = PdfFormField(writer)
            return field
        }

        protected fun createButton(writer: PdfWriter, flags: Int): PdfFormField {
            val field = PdfFormField(writer)
            field.setButton(flags)
            return field
        }

        fun createPushButton(writer: PdfWriter): PdfFormField {
            return createButton(writer, FF_PUSHBUTTON)
        }

        fun createCheckBox(writer: PdfWriter): PdfFormField {
            return createButton(writer, 0)
        }

        fun createRadioButton(writer: PdfWriter, noToggleToOff: Boolean): PdfFormField {
            return createButton(writer, FF_RADIO + if (noToggleToOff) FF_NO_TOGGLE_TO_OFF else 0)
        }

        fun createTextField(writer: PdfWriter, multiline: Boolean, password: Boolean, maxLen: Int): PdfFormField {
            val field = PdfFormField(writer)
            field.put(PdfName.FT, PdfName.TX)
            var flags = if (multiline) FF_MULTILINE else 0
            flags += if (password) FF_PASSWORD else 0
            field.put(PdfName.FF, PdfNumber(flags))
            if (maxLen > 0)
                field.put(PdfName.MAXLEN, PdfNumber(maxLen))
            return field
        }

        protected fun createChoice(writer: PdfWriter, flags: Int, options: PdfArray, topIndex: Int): PdfFormField {
            val field = PdfFormField(writer)
            field.put(PdfName.FT, PdfName.CH)
            field.put(PdfName.FF, PdfNumber(flags))
            field.put(PdfName.OPT, options)
            if (topIndex > 0)
                field.put(PdfName.TI, PdfNumber(topIndex))
            return field
        }

        fun createList(writer: PdfWriter, options: Array<String>, topIndex: Int): PdfFormField {
            return createChoice(writer, 0, processOptions(options), topIndex)
        }

        fun createList(writer: PdfWriter, options: Array<Array<String>>, topIndex: Int): PdfFormField {
            return createChoice(writer, 0, processOptions(options), topIndex)
        }

        fun createCombo(writer: PdfWriter, edit: Boolean, options: Array<String>, topIndex: Int): PdfFormField {
            return createChoice(writer, FF_COMBO + if (edit) FF_EDIT else 0, processOptions(options), topIndex)
        }

        fun createCombo(writer: PdfWriter, edit: Boolean, options: Array<Array<String>>, topIndex: Int): PdfFormField {
            return createChoice(writer, FF_COMBO + if (edit) FF_EDIT else 0, processOptions(options), topIndex)
        }

        protected fun processOptions(options: Array<String>): PdfArray {
            val array = PdfArray()
            for (k in options.indices) {
                array.add(PdfString(options[k], PdfObject.TEXT_UNICODE))
            }
            return array
        }

        protected fun processOptions(options: Array<Array<String>>): PdfArray {
            val array = PdfArray()
            for (k in options.indices) {
                val subOption = options[k]
                val ar2 = PdfArray(PdfString(subOption[0], PdfObject.TEXT_UNICODE))
                ar2.add(PdfString(subOption[1], PdfObject.TEXT_UNICODE))
                array.add(ar2)
            }
            return array
        }

        fun createSignature(writer: PdfWriter): PdfFormField {
            val field = PdfFormField(writer)
            field.put(PdfName.FT, PdfName.SIG)
            return field
        }

        @JvmOverloads internal fun mergeResources(result: PdfDictionary, source: PdfDictionary, writer: PdfStamperImp? = null) {
            var dic: PdfDictionary? = null
            var res: PdfDictionary? = null
            var target: PdfName? = null
            for (k in mergeTarget.indices) {
                target = mergeTarget[k]
                val pdfDict = source.getAsDict(target)
                if ((dic = pdfDict) != null) {
                    if ((res = PdfReader.getPdfObject(result.get(target), result) as PdfDictionary?) == null) {
                        res = PdfDictionary()
                    }
                    res!!.mergeDifferent(dic)
                    result.put(target, res)
                    writer?.markUsed(res)
                }
            }
        }

        fun shallowDuplicate(annot: PdfAnnotation): PdfAnnotation {
            val dup: PdfAnnotation
            if (annot.isForm) {
                dup = PdfFormField(annot.writer)
                val srcField = annot as PdfFormField
                dup.parent = srcField.parent
                dup.kids = srcField.kids
            } else
                dup = annot.writer.createAnnotation(null, annot.get(PdfName.SUBTYPE) as PdfName?)
            dup.merge(annot)
            dup.isForm = annot.isForm
            dup.isAnnotation = annot.isAnnotation
            dup.templates = annot.templates
            return dup
        }
    }
}
