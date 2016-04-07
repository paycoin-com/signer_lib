/*
 * $Id: fcd98643d05d5e36f1b8b942ef8db882487595c8 $
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

import java.io.IOException
import java.util.ArrayList

import com.itextpdf.text.error_messages.MessageLocalization

/**
 * An optional content group is a dictionary representing a collection of graphics
 * that can be made visible or invisible dynamically by users of viewer applications.
 * In iText they are referenced as layers.

 * @author Paulo Soares
 */
class PdfLayer : PdfDictionary, PdfOCG {
    /**
     * Gets the PdfIndirectReference that represents this layer.
     * @return the PdfIndirectReference that represents this layer
     */
    /**
     * Sets the PdfIndirectReference that represents this layer.
     * This can only be done from PdfStamperImp.
     * @param    ref    The reference to the OCG object
     * *
     * @since    2.1.2
     */
    override var ref: PdfIndirectReference
        internal set(ref) {
            this.ref = ref
        }
    /**
     * Gets the children layers.
     * @return the children layers or null if the layer has no children
     */
    var children: ArrayList<PdfLayer>? = null
        protected set
    /**
     * Gets the parent layer.
     * @return the parent layer or null if the layer has no parent
     */
    var parent: PdfLayer? = null
        protected set
    internal var title:

            String
        protected set

    /**
     * Holds value of property on.
     */
    /**
     * Gets the initial visibility of the layer.
     * @return the initial visibility of the layer
     */
    /**
     * Sets the initial visibility of the layer.
     * @param on the initial visibility of the layer
     */
    var isOn = true

    /**
     * Holds value of property onPanel.
     */
    /**
     * Gets the layer visibility in Acrobat's layer panel
     * @return the layer visibility in Acrobat's layer panel
     */
    /**
     * Sets the visibility of the layer in Acrobat's layer panel. If false
     * the layer cannot be directly manipulated by the user. Note that any children layers will
     * also be absent from the panel.
     * @param onPanel the visibility of the layer in Acrobat's layer panel
     */
    var isOnPanel = true

    internal constructor(title: String) {
        this.title = title
    }

    /**
     * Creates a new layer.
     * @param name the name of the layer
     * *
     * @param writer the writer
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(name: String, writer: PdfWriter) : super(PdfName.OCG) {
        setName(name)
        if (writer is PdfStamperImp)
            ref = writer.addToBody(this).indirectReference
        else
            ref = writer.pdfIndirectReference
        writer.registerLayer(this)
    }

    /**
     * Adds a child layer. Nested layers can only have one parent.
     * @param child the child layer
     */
    fun addChild(child: PdfLayer) {
        if (child.parent != null)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.layer.1.already.has.a.parent", child.getAsString(PdfName.NAME).toUnicodeString()))
        child.parent = this
        if (children == null)
            children = ArrayList<PdfLayer>()
        children!!.add(child)
    }

    /**
     * Sets the name of this layer.
     * @param name the name of this layer
     */
    fun setName(name: String) {
        put(PdfName.NAME, PdfString(name, PdfObject.TEXT_UNICODE))
    }

    /**
     * Gets the dictionary representing the layer. It just returns this.
     * @return the dictionary representing the layer
     */
    override val pdfObject: PdfObject
        get() = this

    private val usage: PdfDictionary
        get() {
            var usage: PdfDictionary? = getAsDict(PdfName.USAGE)
            if (usage == null) {
                usage = PdfDictionary()
                put(PdfName.USAGE, usage)
            }
            return usage
        }

    /**
     * Used by the creating application to store application-specific
     * data associated with this optional content group.
     * @param creator a text string specifying the application that created the group
     * *
     * @param subtype a string defining the type of content controlled by the group. Suggested
     * * values include but are not limited to Artwork, for graphic-design or publishing
     * * applications, and Technical, for technical designs such as building plans or
     * * schematics
     */
    fun setCreatorInfo(creator: String, subtype: String) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.CREATOR, PdfString(creator, PdfObject.TEXT_UNICODE))
        dic.put(PdfName.SUBTYPE, PdfName(subtype))
        usage.put(PdfName.CREATORINFO, dic)
    }

    /**
     * Specifies the language of the content controlled by this
     * optional content group
     * @param lang a language string which specifies a language and possibly a locale
     * * (for example, es-MX represents Mexican Spanish)
     * *
     * @param preferred used by viewer applications when there is a partial match but no exact
     * * match between the system language and the language strings in all usage dictionaries
     */
    fun setLanguage(lang: String, preferred: Boolean) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.LANG, PdfString(lang, PdfObject.TEXT_UNICODE))
        if (preferred)
            dic.put(PdfName.PREFERRED, PdfName.ON)
        usage.put(PdfName.LANGUAGE, dic)
    }

    /**
     * Specifies the recommended state for content in this
     * group when the document (or part of it) is saved by a viewer application to a format
     * that does not support optional content (for example, an earlier version of
     * PDF or a raster image format).
     * @param export the export state
     */
    fun setExport(export: Boolean) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.EXPORTSTATE, if (export) PdfName.ON else PdfName.OFF)
        usage.put(PdfName.EXPORT, dic)
    }

    /**
     * Specifies a range of magnifications at which the content
     * in this optional content group is best viewed.
     * @param min the minimum recommended magnification factors at which the group
     * * should be ON. A negative value will set the default to 0
     * *
     * @param max the maximum recommended magnification factor at which the group
     * * should be ON. A negative value will set the largest possible magnification supported by the
     * * viewer application
     */
    fun setZoom(min: Float, max: Float) {
        if (min <= 0 && max < 0)
            return
        val usage = usage
        val dic = PdfDictionary()
        if (min > 0)
            dic.put(PdfName.MIN_LOWER_CASE, PdfNumber(min))
        if (max >= 0)
            dic.put(PdfName.MAX_LOWER_CASE, PdfNumber(max))
        usage.put(PdfName.ZOOM, dic)
    }

    /**
     * Specifies that the content in this group is intended for
     * use in printing
     * @param subtype a name specifying the kind of content controlled by the group;
     * * for example, Trapping, PrintersMarks and Watermark
     * *
     * @param printstate indicates that the group should be
     * * set to that state when the document is printed from a viewer application
     */
    fun setPrint(subtype: String, printstate: Boolean) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.SUBTYPE, PdfName(subtype))
        dic.put(PdfName.PRINTSTATE, if (printstate) PdfName.ON else PdfName.OFF)
        usage.put(PdfName.PRINT, dic)
    }

    /**
     * Indicates that the group should be set to that state when the
     * document is opened in a viewer application.
     * @param view the view state
     */
    fun setView(view: Boolean) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.VIEWSTATE, if (view) PdfName.ON else PdfName.OFF)
        usage.put(PdfName.VIEW, dic)
    }

    /**
     * Indicates that the group contains a pagination artifact.
     * @param pe one of the following names: "HF" (Header Footer),
     * * "FG" (Foreground), "BG" (Background), or "L" (Logo).
     * *
     * @since 5.0.2
     */
    fun setPageElement(pe: String) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.SUBTYPE, PdfName(pe))
        usage.put(PdfName.PAGEELEMENT, dic)
    }

    /**
     * One of more users for whom this optional content group is primarily intended.
     * @param type should be "Ind" (Individual), "Ttl" (Title), or "Org" (Organization).
     * *
     * @param names one or more names
     * *
     * @since 5.0.2
     */
    fun setUser(type: String, vararg names: String) {
        val usage = usage
        val dic = PdfDictionary()
        dic.put(PdfName.TYPE, PdfName(type))
        val arr = PdfArray()
        for (s in names)
            arr.add(PdfString(s, PdfObject.TEXT_UNICODE))
        usage.put(PdfName.NAME, arr)
        usage.put(PdfName.USER, dic)
    }

    companion object {

        /**
         * Creates a title layer. A title layer is not really a layer but a collection of layers
         * under the same title heading.
         * @param title the title text
         * *
         * @param writer the PdfWriter
         * *
         * @return the title layer
         */
        fun createTitle(title: String?, writer: PdfWriter): PdfLayer {
            if (title == null)
                throw NullPointerException(MessageLocalization.getComposedMessage("title.cannot.be.null"))
            val layer = PdfLayer(title)
            writer.registerLayer(layer)
            return layer
        }
    }

}
