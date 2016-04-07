/*
 * $Id: f3e5da04b2cd818ee33d2b2841a8f05df588e28f $
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

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

/**
 * A generic Document class.
 *
 * All kinds of Text-elements can be added to a HTMLDocument.
 * The Document signals all the listeners when an element has
 * been added.
 *
 * Remark:
 *
 * Once a document is created you can add some meta information.
 * You can also set the headers/footers.
 * You have to open the document before you can write content.
 * You can only write content (no more meta-formation!) once a document is
 * opened.
 * When you change the header/footer on a certain page, this will be
 * effective starting on the next page.
 * After closing the document, every listener (as well as its
 * OutputStream) is closed too.
 *
 * Example:

 * // creation of the document with a certain size and certain margins
 * Document document = new Document(PageSize.A4, 50, 50, 50, 50);
 *  try {
 * // creation of the different writers
 * HtmlWriter.getInstance(document , System.out);
 * PdfWriter.getInstance(document , new FileOutputStream("text.pdf"));
 * // we add some meta information to the document
 * document.addAuthor("Bruno Lowagie");
 * document.addSubject("This is the result of a Test.");
 * // we open the document for writing
 * document.open();
 * document.add(new Paragraph("Hello world"));
 * } catch(DocumentException de) {
 * System.err.println(de.getMessage());
 * }
 * document.close();
 *

 *
 */

class Document
/**
 * Constructs a new Document -object.

 * @param pageSize
 * *            the pageSize
 * *
 * @param marginLeft
 * *            the margin on the left
 * *
 * @param marginRight
 * *            the margin on the right
 * *
 * @param marginTop
 * *            the margin on the top
 * *
 * @param marginBottom
 * *            the margin on the bottom
 */
@JvmOverloads constructor(// membervariables concerning the layout

        /** The size of the page.  */
        protected var pageSize: Rectangle = PageSize.A4, marginLeft: Float = 36f, marginRight: Float = 36f,
        marginTop: Float = 36f, marginBottom: Float = 36f) : DocListener, IAccessibleElement {

    /**
     * The DocListener.
     * @since iText 5.1.0 changed from private to protected
     */
    protected var listeners = ArrayList<DocListener>()

    /** Is the document open or not?  */
    /**
     * Checks if the document is open.

     * @return true if the document is open
     */
    var isOpen: Boolean = false
        protected set

    /** Has the document already been closed?  */
    protected var close: Boolean = false

    /** margin in x direction starting from the left  */
    protected var marginLeft = 0f

    /** margin in x direction starting from the right  */
    protected var marginRight = 0f

    /** margin in y direction starting from the top  */
    protected var marginTop = 0f

    /** margin in y direction starting from the bottom  */
    protected var marginBottom = 0f

    /** mirroring of the left/right margins  */
    protected var marginMirroring = false

    /**
     * mirroring of the top/bottom margins
     * @since    2.1.6
     */
    protected var marginMirroringTopBottom = false

    /** Content of JavaScript onLoad function  */
    /**
     * Gets the JavaScript onLoad command.

     * @return the JavaScript onLoad command
     */

    /**
     * Adds a JavaScript onLoad function to the HTML body tag

     * @param code
     * *            the JavaScript code to be executed on load of the HTML page
     */

    var javaScript_onLoad: String? = null

    /** Content of JavaScript onUnLoad function  */
    /**
     * Gets the JavaScript onUnLoad command.

     * @return the JavaScript onUnLoad command
     */

    /**
     * Adds a JavaScript onUnLoad function to the HTML body tag

     * @param code
     * *            the JavaScript code to be executed on unload of the HTML page
     */

    var javaScript_onUnLoad: String? = null

    /** Style class in HTML body tag  */
    /**
     * Gets the style class of the HTML body tag

     * @return        the style class of the HTML body tag
     */

    /**
     * Adds a style class to the HTML body tag

     * @param htmlStyleClass
     * *            the style class for the HTML body tag
     */

    var htmlStyleClass: String? = null

    // headers, footers

    /** Current pagenumber  */
    /**
     * Returns the current page number.

     * @return the current page number
     */

    var pageNumber = 0
        protected set

    /** This is a chapter number in case ChapterAutoNumber is used.  */
    protected var chapternumber = 0

    override var role = PdfName.DOCUMENT
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()

    init {
        this.marginLeft = marginLeft
        this.marginRight = marginRight
        this.marginTop = marginTop
        this.marginBottom = marginBottom
    }

    // listener methods

    /**
     * Adds a DocListener to the Document.

     * @param listener
     * *            the new DocListener.
     */

    fun addDocListener(listener: DocListener) {
        listeners.add(listener)
        if (listener is IAccessibleElement) {
            listener.role = this.role
            listener.id = this.id
            if (this.accessibleAttributes != null) {
                for (key in this.accessibleAttributes!!.keys)
                    listener.setAccessibleAttribute(key, this.accessibleAttributes!![key])
            }
        }
    }

    /**
     * Removes a DocListener from the Document.

     * @param listener
     * *            the DocListener that has to be removed.
     */

    fun removeDocListener(listener: DocListener) {
        listeners.remove(listener)
    }

    // methods implementing the DocListener interface

    /**
     * Adds an Element to the Document.

     * @param element
     * *            the Element to add
     * *
     * @return true if the element was added, false
     * *          if not
     * *
     * @throws DocumentException
     * *             when a document isn't open yet, or has been closed
     */

    @Throws(DocumentException::class)
    override fun add(element: Element): Boolean {
        if (close) {
            throw DocumentException(MessageLocalization.getComposedMessage("the.document.has.been.closed.you.can.t.add.any.elements"))
        }
        if (!isOpen && element.isContent) {
            throw DocumentException(MessageLocalization.getComposedMessage("the.document.is.not.open.yet.you.can.only.add.meta.information"))
        }
        var success = false
        if (element is ChapterAutoNumber) {
            chapternumber = element.setAutomaticNumber(chapternumber)
        }
        for (listener in listeners) {
            success = success or listener.add(element)
        }
        if (element is LargeElement) {
            if (!element.isComplete)
                element.flushContent()
        }
        return success
    }

    /**
     * Opens the document.
     *
     * Once the document is opened, you can't write any Header- or
     * Meta-information anymore. You have to open the document before you can
     * begin to add content to the body of the document.
     */

    override fun open() {
        if (!close) {
            isOpen = true
        }
        for (listener in listeners) {
            listener.setPageSize(pageSize)
            listener.setMargins(marginLeft, marginRight, marginTop,
                    marginBottom)
            listener.open()
        }
    }

    /**
     * Sets the pagesize.

     * @param pageSize
     * *            the new pagesize
     * *
     * @return    a boolean
     */

    override fun setPageSize(pageSize: Rectangle): Boolean {
        this.pageSize = pageSize
        for (listener in listeners) {
            listener.setPageSize(pageSize)
        }
        return true
    }

    /**
     * Sets the margins.

     * @param marginLeft
     * *            the margin on the left
     * *
     * @param marginRight
     * *            the margin on the right
     * *
     * @param marginTop
     * *            the margin on the top
     * *
     * @param marginBottom
     * *            the margin on the bottom
     * *
     * @return    a boolean
     */

    override fun setMargins(marginLeft: Float, marginRight: Float,
                            marginTop: Float, marginBottom: Float): Boolean {
        this.marginLeft = marginLeft
        this.marginRight = marginRight
        this.marginTop = marginTop
        this.marginBottom = marginBottom
        for (listener in listeners) {
            listener.setMargins(marginLeft, marginRight, marginTop,
                    marginBottom)
        }
        return true
    }

    /**
     * Signals that an new page has to be started.

     * @return true if the page was added, false
     * *         if not.
     */

    override fun newPage(): Boolean {
        if (!isOpen || close) {
            return false
        }
        for (listener in listeners) {
            listener.newPage()
        }
        return true
    }

    /**
     * Sets the page number to 0.
     */

    override fun resetPageCount() {
        pageNumber = 0
        for (listener in listeners) {
            listener.resetPageCount()
        }
    }

    /**
     * Sets the page number.

     * @param pageN
     * *            the new page number
     */

    override fun setPageCount(pageN: Int) {
        this.pageNumber = pageN
        for (listener in listeners) {
            listener.setPageCount(pageN)
        }
    }

    /**
     * Closes the document.
     *
     * Once all the content has been written in the body, you have to close the
     * body. After that nothing can be written to the body anymore.
     */

    override fun close() {
        if (!close) {
            isOpen = false
            close = true
        }
        for (listener in listeners) {
            listener.close()
        }
    }

    // methods concerning the header or some meta information

    /**
     * Adds a user defined header to the document.

     * @param name
     * *            the name of the header
     * *
     * @param content
     * *            the content of the header
     * *
     * @return    true if successful, false otherwise
     */

    fun addHeader(name: String, content: String): Boolean {
        try {
            return add(Header(name, content))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the title to a Document.

     * @param title
     * *            the title
     * *
     * @return    true if successful, false otherwise
     */

    fun addTitle(title: String): Boolean {
        try {
            return add(Meta(Element.TITLE, title))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the subject to a Document.

     * @param subject
     * *            the subject
     * *
     * @return    true if successful, false otherwise
     */

    fun addSubject(subject: String): Boolean {
        try {
            return add(Meta(Element.SUBJECT, subject))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the keywords to a Document.

     * @param keywords
     * *            adds the keywords to the document
     * *
     * @return true if successful, false otherwise
     */

    fun addKeywords(keywords: String): Boolean {
        try {
            return add(Meta(Element.KEYWORDS, keywords))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the author to a Document.

     * @param author
     * *            the name of the author
     * *
     * @return    true if successful, false otherwise
     */

    fun addAuthor(author: String): Boolean {
        try {
            return add(Meta(Element.AUTHOR, author))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the creator to a Document.

     * @param creator
     * *            the name of the creator
     * *
     * @return    true if successful, false otherwise
     */

    fun addCreator(creator: String): Boolean {
        try {
            return add(Meta(Element.CREATOR, creator))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the producer to a Document.

     * @return    true if successful, false otherwise
     */

    fun addProducer(): Boolean {
        try {
            return add(Meta(Element.PRODUCER, Version.instance.getVersion()))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds a language to th document. Required for PDF/UA compatible documents.
     * @param language
     * *
     * @return `true` if successfull, `false` otherwise
     */
    fun addLanguage(language: String): Boolean {
        try {
            return add(Meta(Element.LANGUAGE, language))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    /**
     * Adds the current date and time to a Document.

     * @return    true if successful, false otherwise
     */

    fun addCreationDate(): Boolean {
        try {
            /* bugfix by 'taqua' (Thomas) */
            val sdf = SimpleDateFormat(
                    "EEE MMM dd HH:mm:ss zzz yyyy")
            return add(Meta(Element.CREATIONDATE, sdf.format(Date())))
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    // methods to get the layout of the document.

    /**
     * Returns the left margin.

     * @return    the left margin
     */

    fun leftMargin(): Float {
        return marginLeft
    }

    /**
     * Return the right margin.

     * @return    the right margin
     */

    fun rightMargin(): Float {
        return marginRight
    }

    /**
     * Returns the top margin.

     * @return    the top margin
     */

    fun topMargin(): Float {
        return marginTop
    }

    /**
     * Returns the bottom margin.

     * @return    the bottom margin
     */

    fun bottomMargin(): Float {
        return marginBottom
    }

    /**
     * Returns the lower left x-coordinate.

     * @return    the lower left x-coordinate
     */

    fun left(): Float {
        return pageSize.getLeft(marginLeft)
    }

    /**
     * Returns the upper right x-coordinate.

     * @return    the upper right x-coordinate
     */

    fun right(): Float {
        return pageSize.getRight(marginRight)
    }

    /**
     * Returns the upper right y-coordinate.

     * @return    the upper right y-coordinate
     */

    fun top(): Float {
        return pageSize.getTop(marginTop)
    }

    /**
     * Returns the lower left y-coordinate.

     * @return    the lower left y-coordinate
     */

    fun bottom(): Float {
        return pageSize.getBottom(marginBottom)
    }

    /**
     * Returns the lower left x-coordinate considering a given margin.

     * @param margin
     * *            a margin
     * *
     * @return    the lower left x-coordinate
     */

    fun left(margin: Float): Float {
        return pageSize.getLeft(marginLeft + margin)
    }

    /**
     * Returns the upper right x-coordinate, considering a given margin.

     * @param margin
     * *            a margin
     * *
     * @return    the upper right x-coordinate
     */

    fun right(margin: Float): Float {
        return pageSize.getRight(marginRight + margin)
    }

    /**
     * Returns the upper right y-coordinate, considering a given margin.

     * @param margin
     * *            a margin
     * *
     * @return    the upper right y-coordinate
     */

    fun top(margin: Float): Float {
        return pageSize.getTop(marginTop + margin)
    }

    /**
     * Returns the lower left y-coordinate, considering a given margin.

     * @param margin
     * *            a margin
     * *
     * @return    the lower left y-coordinate
     */

    fun bottom(margin: Float): Float {
        return pageSize.getBottom(marginBottom + margin)
    }

    /**
     * Gets the pagesize.

     * @return the page size
     */

    fun getPageSize(): Rectangle {
        return this.pageSize
    }

    /**
     * Set the margin mirroring. It will mirror right/left margins for odd/even pages.

     * @param marginMirroring
     * *            true to mirror the margins
     * *
     * @return always true
     */
    override fun setMarginMirroring(marginMirroring: Boolean): Boolean {
        this.marginMirroring = marginMirroring
        var listener: DocListener
        for (element in listeners) {
            listener = element
            listener.setMarginMirroring(marginMirroring)
        }
        return true
    }

    /**
     * Set the margin mirroring. It will mirror top/bottom margins for odd/even pages.

     * @param marginMirroringTopBottom
     * *            true to mirror the margins
     * *
     * @return always true
     * *
     * @since    2.1.6
     */
    override fun setMarginMirroringTopBottom(marginMirroringTopBottom: Boolean): Boolean {
        this.marginMirroringTopBottom = marginMirroringTopBottom
        var listener: DocListener
        for (element in listeners) {
            listener = element
            listener.setMarginMirroringTopBottom(marginMirroringTopBottom)
        }
        return true
    }

    /**
     * Gets the margin mirroring flag.

     * @return the margin mirroring flag
     */
    fun isMarginMirroring(): Boolean {
        return marginMirroring
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

        /**
         * Allows the pdf documents to be produced without compression for debugging
         * purposes.
         */
        var compress = true

        /**
         * When true the file access is not done through a memory mapped file. Use it if the file
         * is too big to be mapped in your address space.
         */
        var plainRandomAccess = false

        /** Scales the WMF font size. The default value is 0.86.  */
        var wmfFontCorrection = 0.86f
    }
}// constructor
/**
 * Constructs a new Document -object.
 */
/**
 * Constructs a new Document -object.

 * @param pageSize
 * *            the pageSize
 */
