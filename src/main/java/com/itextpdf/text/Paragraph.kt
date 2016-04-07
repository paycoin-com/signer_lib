/*
 * $Id: 2e881f7b93ae556be810cf940ee146470ff6a87e $
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
package com.itextpdf.text

import com.itextpdf.text.api.Indentable
import com.itextpdf.text.api.Spaceable
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.ArrayList
import java.util.HashMap

/**
 * A Paragraph is a series of Chunks and/or Phrases.
 *
 * A Paragraph has the same qualities of a Phrase, but also
 * some additional layout-parameters:
 *
 * the indentation
 * the alignment of the text
 *

 * Example:
 *
 * Paragraph p = new Paragraph("This is a paragraph",
 * FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC, new Color(0, 0, 255)));
 *

 * @see Element

 * @see Phrase

 * @see ListItem
 */

open class Paragraph : Phrase, Indentable, Spaceable, IAccessibleElement {

    // membervariables

    /** The alignment of the text.  */
    // methods to retrieve information

    /**
     * Gets the alignment of this paragraph.

     * @return    alignment
     */
    // setting the membervariables

    /**
     * Sets the alignment of this paragraph.

     * @param    alignment        the new alignment
     */
    var alignment = Element.ALIGN_UNDEFINED

    /** The indentation of this paragraph on the left side.  */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Indentable#getIndentationLeft()
	 */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Indentable#setIndentationLeft(float)
	 */
    override var indentationLeft: Float = 0.toFloat()

    /** The indentation of this paragraph on the right side.  */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Indentable#getIndentationRight()
	 */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Indentable#setIndentationRight(float)
	 */
    override var indentationRight: Float = 0.toFloat()

    /** Holds value of property firstLineIndent.  */
    /**
     * Getter for property firstLineIndent.
     * @return Value of property firstLineIndent.
     */
    /**
     * Setter for property firstLineIndent.
     * @param firstLineIndent New value of property firstLineIndent.
     */
    var firstLineIndent = 0f

    /** The spacing before the paragraph.  */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Spaceable#getSpacingBefore()
	 */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Spaceable#setSpacingBefore(float)
	 */
    override var spacingBefore: Float = 0.toFloat()

    /** The spacing after the paragraph.  */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Spaceable#getSpacingAfter()
	 */
    /* (non-Javadoc)
	 * @see com.itextpdf.text.Spaceable#setSpacingAfter(float)
	 */
    override var spacingAfter: Float = 0.toFloat()

    /** Holds value of property extraParagraphSpace.  */
    /**
     * Getter for property extraParagraphSpace.
     * @return Value of property extraParagraphSpace.
     */
    /**
     * Setter for property extraParagraphSpace.
     * @param extraParagraphSpace New value of property extraParagraphSpace.
     */
    var extraParagraphSpace = 0f

    /** Does the paragraph has to be kept together on 1 page.  */
    /**
     * Checks if this paragraph has to be kept together on one page.

     * @return  true if the paragraph may not be split over 2 pages.
     */
    /**
     * Indicates that the paragraph has to be kept together on one page.

     * @param   keeptogether    true of the paragraph may not be split over 2 pages
     */
    var keepTogether = false

    override var paddingTop: Float = 0.toFloat()

    override var role = PdfName.P
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }

    // constructors

    /**
     * Constructs a Paragraph.
     */
    constructor() : super() {
    }

    /**
     * Constructs a Paragraph with a certain leading.

     * @param    leading        the leading
     */
    constructor(leading: Float) : super(leading) {
    }

    /**
     * Constructs a Paragraph with a certain Chunk.

     * @param    chunk        a Chunk
     */
    constructor(chunk: Chunk) : super(chunk) {
    }

    /**
     * Constructs a Paragraph with a certain Chunk
     * and a certain leading.

     * @param    leading        the leading
     * *
     * @param    chunk        a Chunk
     */
    constructor(leading: Float, chunk: Chunk) : super(leading, chunk) {
    }

    /**
     * Constructs a Paragraph with a certain String.

     * @param    string        a String
     */
    constructor(string: String) : super(string) {
    }

    /**
     * Constructs a Paragraph with a certain String
     * and a certain Font.

     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(string: String, font: Font) : super(string, font) {
    }

    /**
     * Constructs a Paragraph with a certain String
     * and a certain leading.

     * @param    leading        the leading
     * *
     * @param    string        a String
     */
    constructor(leading: Float, string: String) : super(leading, string) {
    }

    /**
     * Constructs a Paragraph with a certain leading, String
     * and Font.

     * @param    leading        the leading
     * *
     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(leading: Float, string: String, font: Font) : super(leading, string, font) {
    }

    /**
     * Constructs a Paragraph with a certain Phrase.

     * @param    phrase        a Phrase
     */
    constructor(phrase: Phrase) : super(phrase) {
        if (phrase is Paragraph) {
            alignment = phrase.alignment
            indentationLeft = phrase.indentationLeft
            indentationRight = phrase.indentationRight
            firstLineIndent = phrase.firstLineIndent
            spacingAfter = phrase.spacingAfter
            spacingBefore = phrase.spacingBefore
            extraParagraphSpace = phrase.extraParagraphSpace
            role = phrase.role
            id = phrase.id
            if (phrase.accessibleAttributes != null)
                accessibleAttributes = HashMap(phrase.accessibleAttributes)
        }
    }

    /**
     * Creates a shallow clone of the Paragraph.
     * @return
     */
    open fun cloneShallow(spacingBefore: Boolean): Paragraph {
        val copy = Paragraph()
        populateProperties(copy, spacingBefore)
        return copy
    }

    protected fun populateProperties(copy: Paragraph, spacingBefore: Boolean) {
        copy.font = font
        copy.alignment = alignment
        copy.setLeading(getLeading(), multipliedLeading)
        copy.indentationLeft = indentationLeft
        copy.indentationRight = indentationRight
        copy.firstLineIndent = firstLineIndent
        copy.spacingAfter = spacingAfter
        if (spacingBefore)
            copy.spacingBefore = spacingBefore
        copy.extraParagraphSpace = extraParagraphSpace
        copy.role = role
        copy.id = id
        if (accessibleAttributes != null)
            copy.accessibleAttributes = HashMap(accessibleAttributes)
        copy.tabSettings = tabSettings
        copy.keepTogether = keepTogether
    }

    /**
     * Breaks this Paragraph up in different parts, separating paragraphs, lists and tables from each other.
     * @return
     */
    fun breakUp(): List<Element> {
        val list = ArrayList<Element>()
        var tmp: Paragraph? = null
        for (e in this) {
            if (e.type() == Element.LIST || e.type() == Element.PTABLE || e.type() == Element.PARAGRAPH) {
                if (tmp != null && tmp.size > 0) {
                    tmp.spacingAfter = 0
                    list.add(tmp)
                    tmp = cloneShallow(false)
                }
                if (list.size == 0) {
                    when (e.type()) {
                        Element.PTABLE -> (e as PdfPTable).spacingBefore = spacingBefore
                        Element.PARAGRAPH -> (e as Paragraph).spacingBefore = spacingBefore
                        Element.LIST -> {
                            val firstItem = (e as List).firstItem
                            if (firstItem != null) {
                                firstItem.spacingBefore = spacingBefore
                            }
                        }
                        else -> {
                        }
                    }
                }
                list.add(e)
            } else {
                if (tmp == null) {
                    tmp = cloneShallow(list.size == 0)
                }
                tmp.add(e)
            }
        }
        if (tmp != null && tmp.size > 0) {
            list.add(tmp)
        }
        if (list.size != 0) {
            val lastElement = list[list.size - 1]
            when (lastElement.type()) {
                Element.PTABLE -> (lastElement as PdfPTable).spacingAfter = spacingAfter
                Element.PARAGRAPH -> (lastElement as Paragraph).spacingAfter = spacingAfter
                Element.LIST -> {
                    val lastItem = (lastElement as List).lastItem
                    if (lastItem != null) {
                        lastItem.spacingAfter = spacingAfter
                    }
                }
                else -> {
                }
            }
        }
        return list
    }

    // implementation of the Element-methods

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.PARAGRAPH
    }

    // methods

    /**
     * Adds an Element to the Paragraph.

     * @param    o the element to add.
     * *
     * @return true is adding the object succeeded
     */
    override fun add(o: Element?): Boolean {
        if (o is List) {
            o.indentationLeft = o.indentationLeft + indentationLeft
            o.indentationRight = indentationRight
            return super.add(o)
        } else if (o is Image) {
            super.addSpecial(o)
            return true
        } else if (o is Paragraph) {
            super.addSpecial(o)
            return true
        }
        return super.add(o)
    }

    // scheduled for removal

    /**
     * Gets the spacing before this paragraph.

     * @return    the spacing
     * *
     */
    @Deprecated("")
    @Deprecated("As of iText 2.1.5, replaced by {@link #getSpacingBefore()},\n      scheduled for removal at 2.3.0")
    fun spacingBefore(): Float {
        return spacingBefore
    }

    /**
     * Gets the spacing after this paragraph.

     * @return    the spacing
     * *
     */
    @Deprecated("")
    @Deprecated("As of iText 2.1.5, replaced by {@link #getSpacingAfter()},\n      scheduled for removal at 2.3.0")
    fun spacingAfter(): Float {
        return spacingAfter
    }

    override fun getAccessibleAttribute(key: PdfName): PdfObject {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    override fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = false

    companion object {

        // constants
        private val serialVersionUID = 7852314969733375514L
    }
}
