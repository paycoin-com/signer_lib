/*
 * $Id: 256864e55b962ec54c6808f19b448e6aa6a3c3cb $
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

import java.util.ArrayList
import java.util.HashMap

import com.itextpdf.text.api.Indentable
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

/**
 * A Section is a part of a Document containing
 * other Sections, Paragraphs, List
 * and/or Tables.
 *
 * Remark: you can not construct a Section yourself.
 * You will have to ask an instance of Section to the
 * Chapter or Section to which you want to
 * add the new Section.
 *
 * Example:
 *
 * Paragraph title2 = new Paragraph("This is Chapter 2", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC, new Color(0, 0, 255)));
 * Chapter chapter2 = new Chapter(title2, 2);
 * Paragraph someText = new Paragraph("This is some text");
 * chapter2.add(someText);
 * Paragraph title21 = new Paragraph("This is Section 1 in Chapter 2", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new Color(255, 0, 0)));
 * Section section1 = chapter2.addSection(title21);
 * Paragraph someSectionText = new Paragraph("This is some silly paragraph in a chapter and/or section. It contains some text to test the functionality of Chapters and Section.");
 * section1.add(someSectionText);
 * Paragraph title211 = new Paragraph("This is SubSection 1 in Section 1 in Chapter 2", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, new Color(255, 0, 0)));
 * Section section11 = section1.addSection(40, title211, 2);
 * section11.add(someSectionText);
 *
 */

open class Section : ArrayList<Element>, TextElementArray, LargeElement, Indentable, IAccessibleElement {

    // member variables

    /** The title of this section.  */
    /**
     * Returns the title, preceded by a certain number of sectionnumbers.

     * @return    a Paragraph
     */
    // public methods

    /**
     * Sets the title of this section.

     * @param    title    the new title
     */
    var title: Paragraph? = null
        get() = constructTitle(title, numbers, numberDepth, numberStyle)

    /** The bookmark title if different from the content title  */
    protected var bookmarkTitle: String? = null

    /** The number of sectionnumbers that has to be shown before the section title.  */
    /**
     * Returns the numberdepth of this Section.

     * @return    the numberdepth
     */
    /**
     * Sets the depth of the sectionnumbers that will be shown preceding the title.
     *
     * If the numberdepth is 0, the sections will not be numbered. If the numberdepth
     * is 1, the section will be numbered with their own number. If the numberdepth is
     * higher (for instance x > 1), the numbers of x - 1 parents will be shown.

     * @param    numberDepth        the new numberDepth
     */
    var numberDepth: Int = 0

    /**
     * The style for sectionnumbers.
     * @since    iText 2.0.8
     */
    /**
     * Gets the style used for numbering sections.
     * @since    iText 2.0.8
     * *
     * @return    a value corresponding with a numbering style
     */
    /**
     * Sets the style for numbering sections.
     * Possible values are [Section.NUMBERSTYLE_DOTTED]: 1.2.3. (the default)
     * or [Section.NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT]: 1.2.3
     * @param numberStyle the style to use
     * *
     * @since    iText 2.0.8
     */
    var numberStyle = NUMBERSTYLE_DOTTED

    /** The indentation of this section on the left side.  */
    /**
     * Returns the indentation of this Section on the left side.

     * @return    the indentation
     */
    /**
     * Sets the indentation of this Section on the left side.

     * @param    indentation        the indentation
     */
    override var indentationLeft: Float = 0.toFloat()

    /** The indentation of this section on the right side.  */
    /**
     * Returns the indentation of this Section on the right side.

     * @return    the indentation
     */
    /**
     * Sets the indentation of this Section on the right side.

     * @param    indentation        the indentation
     */
    override var indentationRight: Float = 0.toFloat()

    /** The additional indentation of the content of this section.  */
    /**
     * Returns the indentation of the content of this Section.

     * @return    the indentation
     */
    /**
     * Sets the indentation of the content of this Section.

     * @param    indentation        the indentation
     */
    var indentation: Float = 0.toFloat()

    /** false if the bookmark children are not visible  */
    /**
     * Getter for property bookmarkOpen.
     * @return Value of property bookmarkOpen.
     */
    /** Setter for property bookmarkOpen.
     * @param bookmarkOpen false if the bookmark children are not
     * * visible.
     */
    var isBookmarkOpen = true

    /** true if the section has to trigger a new page  */
    /**
     * Getter for property bookmarkOpen.
     * @return Value of property triggerNewPage.
     */
    /**
     * Setter for property triggerNewPage.
     * @param triggerNewPage true if a new page has to be triggered.
     */
    var isTriggerNewPage = false
        get() = isTriggerNewPage && isNotAddedYet

    /** This is the number of subsections.  */
    protected var subsections = 0

    /** This is the complete list of sectionnumbers of this section and the parents of this section.  */
    protected var numbers: ArrayList<Int>? = null

    /**
     * Indicates if the Section will be complete once added to the document.
     * @since    iText 2.0.8
     */
    /**
     * @since    iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.isComplete
     */
    /**
     * @since    iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.setComplete
     */
    override var isComplete = true

    /**
     * Indicates if the Section was added completely to the document.
     * @since    iText 2.0.8
     */
    /**
     * @return return the addedCompletely value
     * *
     * @since    iText 2.0.8
     */
    /**
     * @param addedCompletely true if section was completely added, false otherwise
     * *
     * @since    iText 2.0.8
     */
    protected var isAddedCompletely = false

    /**
     * Indicates if this is the first time the section was added.
     * @since    iText 2.0.8
     */
    /**
     * Indicates if this is the first time the section is added.
     * @since    iText2.0.8
     * *
     * @return    true if the section wasn't added yet
     */
    /**
     * Sets the indication if the section was already added to
     * the document.
     * @since    iText2.0.8
     * *
     * @param notAddedYet
     */
    var isNotAddedYet = true

    // constructors

    /**
     * Constructs a new Section.
     */
    protected constructor() {
        title = Paragraph()
        numberDepth = 1
        title!!.role = PdfName("H" + numberDepth)
    }

    /**
     * Constructs a new Section.

     * @param    title            a Paragraph
     * *
     * @param    numberDepth        the numberDepth
     */
    protected constructor(title: Paragraph?, numberDepth: Int) {
        this.numberDepth = numberDepth
        this.title = title
        if (title != null)
            title.role = PdfName("H" + numberDepth)
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param    listener        the ElementListener
     * *
     * @return    true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            var element: Element
            for (element2 in this) {
                element = element2
                listener.add(element)
            }
            return true
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.SECTION
    }

    /**
     * Checks if this object is a Chapter.

     * @return    true if it is a Chapter,
     * *			false if it is a Section.
     */
    val isChapter: Boolean
        get() = type() == Element.CHAPTER

    /**
     * Checks if this object is a Section.

     * @return    true if it is a Section,
     * *			false if it is a Chapter.
     */
    val isSection: Boolean
        get() = type() == Element.SECTION

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override val chunks: List<Chunk>
        get() {
            val tmp = ArrayList<Chunk>()
            for (element in this) {
                tmp.addAll(element.chunks)
            }
            return tmp
        }

    /**
     * @see com.itextpdf.text.Element.isContent
     * @since    iText 2.0.8
     */
    override val isContent: Boolean
        get() = true

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override val isNestable: Boolean
        get() = false

    // overriding some of the ArrayList-methods

    /**
     * Adds a Paragraph, List or Table
     * to this Section.

     * @param    index    index at which the specified element is to be inserted
     * *
     * @param    element    an element of type Paragraph, List or Table=
     * *
     * @throws    ClassCastException if the object is not a Paragraph, List or Table
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    override fun add(index: Int, element: Element?) {
        if (isAddedCompletely) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("this.largeelement.has.already.been.added.to.the.document"))
        }
        try {
            if (element!!.isNestable) {
                super.add(index, element)
            } else {
                throw ClassCastException(MessageLocalization.getComposedMessage("you.can.t.add.a.1.to.a.section", element.javaClass.name))
            }
        } catch (cce: ClassCastException) {
            throw ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.message))
        }

    }

    /**
     * Adds a Paragraph, List, Table or another Section
     * to this Section.

     * @param    element   an element of type Paragraph, List, Table or another Section
     * *
     * @return    a boolean
     * *
     * @throws    ClassCastException if the object is not a Paragraph, List, Table or Section
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    override fun add(element: Element?): Boolean {
        if (isAddedCompletely) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("this.largeelement.has.already.been.added.to.the.document"))
        }
        try {
            if (element!!.type() == Element.SECTION) {
                val section = element as Section?
                section.setNumbers(++subsections, numbers)
                return super.add(section)
            } else if (element is MarkedSection && (element as MarkedObject).element!!.type() == Element.SECTION) {
                val section = element.element as Section?
                section.setNumbers(++subsections, numbers)
                return super.add(element)
            } else if (element.isNestable) {
                return super.add(element)
            } else {
                throw ClassCastException(MessageLocalization.getComposedMessage("you.can.t.add.a.1.to.a.section", element.javaClass.name))
            }
        } catch (cce: ClassCastException) {
            throw ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.message))
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
    override fun addAll(collection: Collection<Element>): Boolean {
        if (collection.size == 0)
            return false
        for (element in collection) {
            this.add(element)
        }
        return true
    }

    // methods that return a Section

    /**
     * Creates a Section, adds it to this Section and returns it.

     * @param    indentation    the indentation of the new section
     * *
     * @param    title        the title of the new section
     * *
     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    @JvmOverloads fun addSection(indentation: Float, title: Paragraph, numberDepth: Int = numberDepth + 1): Section {
        if (isAddedCompletely) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("this.largeelement.has.already.been.added.to.the.document"))
        }
        val section = Section(title, numberDepth)
        section.indentation = indentation
        add(section)
        return section
    }

    /**
     * Creates a Section, add it to this Section and returns it.

     * @param    title        the title of the new section
     * *
     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    fun addSection(title: Paragraph, numberDepth: Int): Section {
        return addSection(0f, title, numberDepth)
    }

    /**
     * Adds a marked section. For use in class MarkedSection only!
     * @return the MarkedSection
     */
    protected fun addMarkedSection(): MarkedSection {
        val section = MarkedSection(Section(null, numberDepth + 1))
        add(section)
        return section
    }

    /**
     * Creates a Section, adds it to this Section and returns it.

     * @param    title        the title of the new section
     * *
     * @return  a new Section object
     */
    open fun addSection(title: Paragraph): Section {
        return addSection(0f, title, numberDepth + 1)
    }

    /**
     * Adds a Section to this Section and returns it.

     * @param    indentation    the indentation of the new section
     * *
     * @param    title        the title of the new section
     * *
     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    fun addSection(indentation: Float, title: String, numberDepth: Int): Section {
        return addSection(indentation, Paragraph(title), numberDepth)
    }

    /**
     * Adds a Section to this Section and returns it.

     * @param    title        the title of the new section
     * *
     * @param    numberDepth    the numberDepth of the section
     * *
     * @return  a new Section object
     */
    fun addSection(title: String, numberDepth: Int): Section {
        return addSection(Paragraph(title), numberDepth)
    }

    /**
     * Adds a Section to this Section and returns it.

     * @param    indentation    the indentation of the new section
     * *
     * @param    title        the title of the new section
     * *
     * @return  a new Section object
     */
    fun addSection(indentation: Float, title: String): Section {
        return addSection(indentation, Paragraph(title))
    }

    /**
     * Adds a Section to this Section and returns it.

     * @param    title        the title of the new section
     * *
     * @return  a new Section object
     */
    open fun addSection(title: String): Section {
        return addSection(Paragraph(title))
    }

    /**
     * Sets the bookmark title. The bookmark title is the same as the section title but
     * can be changed with this method.
     * @param bookmarkTitle the bookmark title
     */
    fun setBookmarkTitle(bookmarkTitle: String) {
        this.bookmarkTitle = bookmarkTitle
    }

    /**
     * Gets the bookmark title.
     * @return the bookmark title
     */
    val bookmarkTitle: Paragraph
        get() {
            if (bookmarkTitle == null)
                return title
            else
                return Paragraph(bookmarkTitle)
        }

    /**
     * Changes the Chapter number.
     * @param number the new number
     */
    fun setChapterNumber(number: Int) {
        numbers!![numbers!!.size - 1] = Integer.valueOf(number)
        var s: Any
        val i = iterator()
        while (i.hasNext()) {
            s = i.next()
            if (s is Section) {
                s.setChapterNumber(number)
            }
        }
    }

    /**
     * Returns the depth of this section.

     * @return    the depth
     */
    val depth: Int
        get() = numbers!!.size

    // private methods

    /**
     * Sets the number of this section.

     * @param    number        the number of this section
     * *
     * @param    numbers        an ArrayList, containing the numbers of the Parent
     */
    private fun setNumbers(number: Int, numbers: ArrayList<Int>) {
        this.numbers = ArrayList<Int>()
        this.numbers!!.add(Integer.valueOf(number))
        this.numbers!!.addAll(numbers)
    }

    /**
     * @since    iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.flushContent
     */
    override fun flushContent() {
        isNotAddedYet = false
        title = null
        var element: Element
        val i = iterator()
        while (i.hasNext()) {
            element = i.next()
            if (element is Section) {
                if (!element.isComplete && size == 1) {
                    element.flushContent()
                    return
                } else {
                    element.isAddedCompletely = true
                }
            }
            i.remove()
        }
    }

    /**
     * Adds a new page to the section.
     * @since    2.1.1
     */
    fun newPage() {
        this.add(Chunk.NEXTPAGE)
    }

    override fun getAccessibleAttribute(key: PdfName): PdfObject {
        return title!!.getAccessibleAttribute(key)
    }

    override fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        title!!.setAccessibleAttribute(key, value)
    }

    override val accessibleAttributes: HashMap<PdfName, PdfObject>
        get() = title!!.accessibleAttributes

    override var role: PdfName
        get() = title!!.role
        set(role) {
            title!!.role = role
        }

    override var id: AccessibleElementId
        get() = title!!.id
        set(id) {
            title!!.id = id
        }

    override val isInline: Boolean
        get() = false

    companion object {
        // constant
        /**
         * A possible number style. The default number style: "1.2.3."
         * @since    iText 2.0.8
         */
        val NUMBERSTYLE_DOTTED = 0
        /**
         * A possible number style. For instance: "1.2.3"
         * @since    iText 2.0.8
         */
        val NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT = 1

        /** A serial version uid.  */
        private val serialVersionUID = 3324172577544748043L

        /**
         * Constructs a Paragraph that will be used as title for a Section or Chapter.
         * @param    title    the title of the section
         * *
         * @param    numbers    a list of sectionnumbers
         * *
         * @param    numberDepth    how many numbers have to be shown
         * *
         * @param    numberStyle    the numbering style
         * *
         * @return    a Paragraph object
         * *
         * @since    iText 2.0.8
         */
        fun constructTitle(title: Paragraph?, numbers: ArrayList<Int>, numberDepth: Int, numberStyle: Int): Paragraph? {
            if (title == null) {
                return null
            }

            val depth = Math.min(numbers.size, numberDepth)
            if (depth < 1) {
                return title
            }
            val buf = StringBuffer(" ")
            for (i in 0..depth - 1) {
                buf.insert(0, ".")
                buf.insert(0, numbers[i].toInt())
            }
            if (numberStyle == NUMBERSTYLE_DOTTED_WITHOUT_FINAL_DOT) {
                buf.deleteCharAt(buf.length - 2)
            }
            val result = Paragraph(title)

            result.add(0, Chunk(buf.toString(), title.font))
            return result
        }
    }
}
/**
 * Creates a Section, adds it to this Section and returns it.

 * @param    indentation    the indentation of the new section
 * *
 * @param    title        the title of the new section
 * *
 * @return  a new Section object
 */
