/*
 * $Id: 8fabf3fda2246edb14bc54fd075454bc17b033fb $
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
import java.io.OutputStream
import java.util.ArrayList

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph

/**
 * PdfOutline is an object that represents a PDF outline entry.
 *
 * An outline allows a user to access views of a document by name.
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 6.7 (page 104-106)

 * @see PdfDictionary
 */

class PdfOutline : PdfDictionary {

    // membervariables

    /** the PdfIndirectReference of this object  */
    private var reference: PdfIndirectReference? = null

    /** value of the Count-key  */
    internal var count = 0

    /** value of the Parent-key  */
    private var parent: PdfOutline? = null

    /** value of the Destination-key  */
    /**
     * Gets the destination for this outline.
     * @return the destination
     */
    var pdfDestination: PdfDestination? = null
        private set

    /** The PdfAction for this outline.
     */
    private var action: PdfAction? = null

    /**
     * Returns the kids of this outline
     * @return an ArrayList with PdfOutlines
     */
    /**
     * Sets the kids of this outline
     * @param kids
     */
    var kids = ArrayList<PdfOutline>()

    protected var writer: PdfWriter

    /** Holds value of property tag.  */
    /** Getter for property tag.
     * @return Value of property tag.
     */
    /** Setter for property tag.
     * @param tag New value of property tag.
     */
    var tag: String? = null

    /** Holds value of property open.  */
    /** Getter for property open.
     * @return Value of property open.
     */
    /** Setter for property open.
     * @param open New value of property open.
     */
    var isOpen: Boolean = false

    /** Holds value of property color.  */
    /** Getter for property color.
     * @return Value of property color.
     */
    /** Setter for property color.
     * @param color New value of property color.
     */
    var color: BaseColor? = null

    /** Holds value of property style.  */
    /** Getter for property style.
     * @return Value of property style.
     */
    /** Setter for property style.
     * @param style New value of property style.
     */
    var style = 0

    // constructors

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for the outlines object.

     * @param writer The PdfWriter you are adding the outline to
     */

    internal constructor(writer: PdfWriter) : super(PdfDictionary.OUTLINES) {
        isOpen = true
        parent = null
        this.writer = writer
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param action the PdfAction for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, action: PdfAction, title: String, open: Boolean = true) : super() {
        this.action = action
        initOutline(parent, title, open)
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param destination the destination for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, destination: PdfDestination, title: String, open: Boolean = true) : super() {
        this.pdfDestination = destination
        initOutline(parent, title, open)
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param action the PdfAction for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, action: PdfAction, title: PdfString, open: Boolean = true) : this(parent, action, title.toString(), open) {
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param destination the destination for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, destination: PdfDestination, title: PdfString, open: Boolean = true) : this(parent, destination, title.toString(), true) {
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param action the PdfAction for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, action: PdfAction, title: Paragraph, open: Boolean = true) : super() {
        val buf = StringBuffer()
        for (chunk in title.chunks) {
            buf.append(chunk.content)
        }
        this.action = action
        initOutline(parent, buf.toString(), open)
    }

    /**
     * Constructs a PdfOutline.
     *
     * This is the constructor for an outline entry.

     * @param parent the parent of this outline item
     * *
     * @param destination the destination for this outline item
     * *
     * @param title the title of this outline item
     * *
     * @param open true if the children are visible
     */
    @JvmOverloads constructor(parent: PdfOutline, destination: PdfDestination, title: Paragraph, open: Boolean = true) : super() {
        val buf = StringBuffer()
        for (element in title.chunks) {
            buf.append(element.content)
        }
        this.pdfDestination = destination
        initOutline(parent, buf.toString(), open)
    }


    // methods

    /** Helper for the constructors.
     * @param parent the parent outline
     * *
     * @param title the title for this outline
     * *
     * @param open true if the children are visible
     */
    internal fun initOutline(parent: PdfOutline, title: String, open: Boolean) {
        this.isOpen = open
        this.parent = parent
        writer = parent.writer
        put(PdfName.TITLE, PdfString(title, PdfObject.TEXT_UNICODE))
        parent.addKid(this)
        if (pdfDestination != null && !pdfDestination!!.hasPage())
        // bugfix Finn Bock
            setDestinationPage(writer.currentPage)
    }

    /**
     * Sets the indirect reference of this PdfOutline.

     * @param reference the PdfIndirectReference to this outline.
     */

    fun setIndirectReference(reference: PdfIndirectReference) {
        this.reference = reference
    }

    /**
     * Gets the indirect reference of this PdfOutline.

     * @return        the PdfIndirectReference to this outline.
     */

    fun indirectReference(): PdfIndirectReference {
        return reference
    }

    /**
     * Gets the parent of this PdfOutline.

     * @return        the PdfOutline that is the parent of this outline.
     */

    fun parent(): PdfOutline {
        return parent
    }

    /**
     * Set the page of the PdfDestination-object.

     * @param pageReference indirect reference to the page
     * *
     * @return true if this page was set as the PdfDestination-page.
     */

    fun setDestinationPage(pageReference: PdfIndirectReference): Boolean {
        if (pdfDestination == null) {
            return false
        }
        return pdfDestination!!.addPage(pageReference)
    }

    /**
     * returns the level of this outline.

     * @return        a level
     */

    fun level(): Int {
        if (parent == null) {
            return 0
        }
        return parent!!.level() + 1
    }

    /**
     * Returns the PDF representation of this PdfOutline.

     * @param writer the PdfWriter
     * *
     * @param os
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        if (color != null && color != BaseColor.BLACK) {
            put(PdfName.C, PdfArray(floatArrayOf(color!!.red / 255f, color!!.green / 255f, color!!.blue / 255f)))
        }
        var flag = 0
        if (style and Font.BOLD != 0)
            flag = flag or 2
        if (style and Font.ITALIC != 0)
            flag = flag or 1
        if (flag != 0)
            put(PdfName.F, PdfNumber(flag))
        if (parent != null) {
            put(PdfName.PARENT, parent!!.indirectReference())
        }
        if (pdfDestination != null && pdfDestination!!.hasPage()) {
            put(PdfName.DEST, pdfDestination)
        }
        if (action != null)
            put(PdfName.A, action)
        if (count != 0) {
            put(PdfName.COUNT, PdfNumber(count))
        }
        super.toPdf(writer, os)
    }

    /**
     * Adds a kid to the outline
     * @param outline
     */
    fun addKid(outline: PdfOutline) {
        kids.add(outline)
    }

    /**
     * Gets the title of this outline
     * @return the title as a String
     */
    /**
     * Sets the title of this outline
     * @param title
     */
    var title: String
        get() {
            val title = get(PdfName.TITLE) as PdfString?
            return title.toString()
        }
        set(title) = put(PdfName.TITLE, PdfString(title, PdfObject.TEXT_UNICODE))

}
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param action the PdfAction for this outline item
 * *
 * @param title the title of this outline item
 */
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param destination the destination for this outline item
 * *
 * @param title the title of this outline item
 */
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param action the PdfAction for this outline item
 * *
 * @param title the title of this outline item
 */
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param destination the destination for this outline item
 * *
 * @param title the title of this outline item
 */
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param action the PdfAction for this outline item
 * *
 * @param title the title of this outline item
 */
/**
 * Constructs a PdfOutline.
 *
 * This is the constructor for an outline entry. The open mode is
 * true.

 * @param parent the parent of this outline item
 * *
 * @param destination the destination for this outline item
 * *
 * @param title the title of this outline item
 */
