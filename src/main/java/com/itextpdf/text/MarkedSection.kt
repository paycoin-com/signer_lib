/*
 * $Id: 9b14ac14c5e032b4291db367587f134183003e54 $
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

/**
 * Wrapper that allows to add properties to a Chapter/Section object.
 * Before iText 1.5 every 'basic building block' implemented the MarkupAttributes interface.
 * By setting attributes, you could add markup to the corresponding XML and/or HTML tag.
 * This functionality was hardly used by anyone, so it was removed, and replaced by
 * the MarkedObject functionality.

 */

@Deprecated("")
@Deprecated("since 5.5.9. This class is no longer used.")
class MarkedSection
/**
 * Creates a MarkedObject with a Section or Chapter object.
 * @param section    the marked section
 */
(section: Section) : MarkedObject(), Indentable {

    /** This is the title of this section.  */
    protected var title: MarkedObject? = null

    init {
        if (section.title != null) {
            title = MarkedObject(section.title)
            section.title = null
        }
        this.element = section
    }

    /**
     * Adds a Paragraph, List or Table
     * to this Section.

     * @param    index    index at which the specified element is to be inserted
     * *
     * @param    o    an object of type Paragraph, List or Table=
     * *
     * @throws    ClassCastException if the object is not a Paragraph, List or Table
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    fun add(index: Int, o: Element) {
        (element as Section).add(index, o)
    }

    /**
     * Adds a Paragraph, List, Table or another Section
     * to this Section.

     * @param    o    an object of type Paragraph, List, Table or another Section
     * *
     * @return    a boolean
     * *
     * @throws    ClassCastException if the object is not a Paragraph, List, Table or Section
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    fun add(o: Element): Boolean {
        return (element as Section).add(o)
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param       listener        an ElementListener
     * *
     * @return true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            var element: Element
            val i = (this.element as Section).iterator()
            while (i.hasNext()) {
                element = i.next()
                listener.add(element)
            }
            return true
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Adds a collection of Elements
     * to this Section.

     * @param    collection    a collection of Paragraphs, Lists and/or Tables
     * *
     * @return    true if the action succeeded, false if not.
     * *
     * @throws    ClassCastException if one of the objects isn't a Paragraph, List, Table
     */
    fun addAll(collection: Collection<Element>): Boolean {
        return (element as Section).addAll(collection)
    }

    /**
     * Creates a Section, adds it to this Section and returns it.

     * @param    indentation    the indentation of the new section
     * *
     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    fun addSection(indentation: Float, numberDepth: Int): MarkedSection {
        val section = (element as Section).addMarkedSection()
        section.setIndentation(indentation)
        section.setNumberDepth(numberDepth)
        return section
    }

    /**
     * Creates a Section, adds it to this Section and returns it.

     * @param    indentation    the indentation of the new section
     * *
     * @return  a new Section object
     */
    fun addSection(indentation: Float): MarkedSection {
        val section = (element as Section).addMarkedSection()
        section.setIndentation(indentation)
        return section
    }

    /**
     * Creates a Section, add it to this Section and returns it.

     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    fun addSection(numberDepth: Int): MarkedSection {
        val section = (element as Section).addMarkedSection()
        section.setNumberDepth(numberDepth)
        return section
    }

    /**
     * Creates a Section, adds it to this Section and returns it.

     * @return  a new Section object
     */
    fun addSection(): MarkedSection {
        return (element as Section).addMarkedSection()
    }

    // public methods

    /**
     * Sets the title of this section.

     * @param    title    the new title
     */
    fun setTitle(title: MarkedObject) {
        if (title.element is Paragraph)
            this.title = title
    }

    /**
     * Gets the title of this MarkedSection.
     * @return    a MarkObject with a Paragraph containing the title of a Section
     * *
     * @since    iText 2.0.8
     */
    fun getTitle(): MarkedObject {
        val result = Section.constructTitle(title!!.element as Paragraph?, (element as Section).numbers, (element as Section).numberDepth, (element as Section).numberStyle)
        val mo = MarkedObject(result)
        mo.markupAttributes = title!!.markupAttributes
        return mo
    }

    /**
     * Sets the depth of the sectionnumbers that will be shown preceding the title.
     *
     * If the numberdepth is 0, the sections will not be numbered. If the numberdepth
     * is 1, the section will be numbered with their own number. If the numberdepth is
     * higher (for instance x > 1), the numbers of x - 1 parents will be shown.

     * @param    numberDepth        the new numberDepth
     */
    fun setNumberDepth(numberDepth: Int) {
        (element as Section).numberDepth = numberDepth
    }

    /**
     * Sets the indentation of the content of this Section.

     * @param    indentation        the indentation
     */
    fun setIndentation(indentation: Float) {
        (element as Section).indentation = indentation
    }

    /** Setter for property bookmarkOpen.
     * @param bookmarkOpen false if the bookmark children are not
     * * visible.
     */
    fun setBookmarkOpen(bookmarkOpen: Boolean) {
        (element as Section).isBookmarkOpen = bookmarkOpen
    }

    /**
     * Setter for property triggerNewPage.
     * @param triggerNewPage true if a new page has to be triggered.
     */
    fun setTriggerNewPage(triggerNewPage: Boolean) {
        (element as Section).isTriggerNewPage = triggerNewPage
    }

    /**
     * Sets the bookmark title. The bookmark title is the same as the section title but
     * can be changed with this method.
     * @param bookmarkTitle the bookmark title
     */
    fun setBookmarkTitle(bookmarkTitle: String) {
        (element as Section).setBookmarkTitle(bookmarkTitle)
    }

    /**
     * Adds a new page to the section.
     * @since    2.1.1
     */
    fun newPage() {
        (element as Section).newPage()
    }

    /* (non-Javadoc)
	 * @see com.itextpdf.text.api.Indentable#getIndentationLeft()
	 */
    /**
     * Sets the indentation of this Section on the left side.

     * @param    indentation        the indentation
     */
    override var indentationLeft: Float
        get() = (element as Section).indentationLeft
        set(indentation) {
            (element as Section).indentationLeft = indentation
        }

    /* (non-Javadoc)
	 * @see com.itextpdf.text.api.Indentable#getIndentationRight()
	 */
    /**
     * Sets the indentation of this Section on the right side.

     * @param    indentation        the indentation
     */
    override var indentationRight: Float
        get() = (element as Section).indentationRight
        set(indentation) {
            (element as Section).indentationRight = indentation
        }
}
