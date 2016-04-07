/*
 * $Id: 9a6134c93ed683d07a4da368220ebdbf9590d48e $
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

import com.itextpdf.text.*
import com.itextpdf.text.List
import com.itextpdf.text.api.WriterOperation
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.TempFileCache
import com.itextpdf.text.pdf.collection.PdfCollection
import com.itextpdf.text.pdf.draw.DrawInterface
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.internal.PdfAnnotationsImp
import com.itextpdf.text.pdf.internal.PdfViewerPreferencesImp

import java.io.IOException
import java.text.DecimalFormat
import java.util.*

/**
 * PdfDocument is the class that is used by PdfWriter
 * to translate a Document into a PDF with different pages.
 *
 * A PdfDocument always listens to a Document
 * and adds the Pdf representation of every Element that is
 * added to the Document.

 * @see com.itextpdf.text.Document

 * @see com.itextpdf.text.DocListener

 * @see PdfWriter

 * @since    2.0.8 (class was package-private before)
 */

class PdfDocument : Document() {

    /**
     * PdfInfo is the PDF InfoDictionary.
     *
     * A document's trailer may contain a reference to an Info dictionary that provides information
     * about the document. This optional dictionary may contain one or more keys, whose values
     * should be strings.
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.10 (page 120-121)
     * @since    2.0.8 (PdfDocument was package-private before)
     */

    class PdfInfo
    /**
     * Construct a PdfInfo-object.
     */
    internal constructor() : PdfDictionary() {

        init {
            addProducer()
            addCreationDate()
        }

        /**
         * Constructs a PdfInfo-object.

         * @param        author        name of the author of the document
         * *
         * @param        title        title of the document
         * *
         * @param        subject        subject of the document
         */

        internal constructor(author: String, title: String, subject: String) : this() {
            addTitle(title)
            addSubject(subject)
            addAuthor(author)
        }

        /**
         * Adds the title of the document.

         * @param    title        the title of the document
         */

        internal fun addTitle(title: String) {
            put(PdfName.TITLE, PdfString(title, PdfObject.TEXT_UNICODE))
        }

        /**
         * Adds the subject to the document.

         * @param    subject        the subject of the document
         */

        internal fun addSubject(subject: String) {
            put(PdfName.SUBJECT, PdfString(subject, PdfObject.TEXT_UNICODE))
        }

        /**
         * Adds some keywords to the document.

         * @param    keywords        the keywords of the document
         */

        internal fun addKeywords(keywords: String) {
            put(PdfName.KEYWORDS, PdfString(keywords, PdfObject.TEXT_UNICODE))
        }

        /**
         * Adds the name of the author to the document.

         * @param    author        the name of the author
         */

        internal fun addAuthor(author: String) {
            put(PdfName.AUTHOR, PdfString(author, PdfObject.TEXT_UNICODE))
        }

        /**
         * Adds the name of the creator to the document.

         * @param    creator        the name of the creator
         */

        internal fun addCreator(creator: String) {
            put(PdfName.CREATOR, PdfString(creator, PdfObject.TEXT_UNICODE))
        }

        /**
         * Adds the name of the producer to the document.
         */

        internal fun addProducer() {
            put(PdfName.PRODUCER, PdfString(Version.getInstance().version))
        }

        /**
         * Adds the date of creation to the document.
         */

        internal fun addCreationDate() {
            val date = PdfDate()
            put(PdfName.CREATIONDATE, date)
            put(PdfName.MODDATE, date)
        }

        internal fun addkey(key: String, value: String) {
            if (key == "Producer" || key == "CreationDate")
                return
            put(PdfName(key), PdfString(value, PdfObject.TEXT_UNICODE))
        }
    }

    /**
     * PdfCatalog is the PDF Catalog-object.
     *
     * The Catalog is a dictionary that is the root node of the document. It contains a reference
     * to the tree of pages contained in the document, a reference to the tree of objects representing
     * the document's outline, a reference to the document's article threads, and the list of named
     * destinations. In addition, the Catalog indicates whether the document's outline or thumbnail
     * page images should be displayed automatically when the document is viewed and whether some location
     * other than the first page should be shown when the document is opened.
     * In this class however, only the reference to the tree of pages is implemented.
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.2 (page 67-71)
     */

    internal class PdfCatalog
    /**
     * Constructs a PdfCatalog.

     * @param        pages        an indirect reference to the root of the document's Pages tree.
     * *
     * @param writer the writer the catalog applies to
     */
    (pages: PdfIndirectReference,
     /** The writer writing the PDF for which we are creating this catalog object.  */
     var writer: PdfWriter) : PdfDictionary(PdfDictionary.CATALOG) {

        init {
            put(PdfName.PAGES, pages)
        }

        /**
         * Adds the names of the named destinations to the catalog.
         * @param localDestinations the local destinations
         * *
         * @param documentLevelJS the javascript used in the document
         * *
         * @param documentFileAttachment    the attached files
         * *
         * @param writer the writer the catalog applies to
         */
        fun addNames(localDestinations: TreeMap<String, Destination>, documentLevelJS: HashMap<String, PdfObject>, documentFileAttachment: HashMap<String, PdfObject>, writer: PdfWriter) {
            if (localDestinations.isEmpty() && documentLevelJS.isEmpty() && documentFileAttachment.isEmpty())
                return
            try {
                val names = PdfDictionary()
                if (!localDestinations.isEmpty()) {
                    val destmap = HashMap<String, PdfObject>()
                    for ((name, dest) in localDestinations) {
                        if (dest.destination == null)
                        //no destination
                            continue
                        destmap.put(name, dest.reference)
                    }
                    if (destmap.size > 0) {
                        names.put(PdfName.DESTS, writer.addToBody(PdfNameTree.writeTree(destmap, writer)).indirectReference)
                    }
                }
                if (!documentLevelJS.isEmpty()) {
                    val tree = PdfNameTree.writeTree(documentLevelJS, writer)
                    names.put(PdfName.JAVASCRIPT, writer.addToBody(tree).indirectReference)
                }
                if (!documentFileAttachment.isEmpty()) {
                    names.put(PdfName.EMBEDDEDFILES, writer.addToBody(PdfNameTree.writeTree(documentFileAttachment, writer)).indirectReference)
                }
                if (names.size() > 0)
                    put(PdfName.NAMES, writer.addToBody(names).indirectReference)
            } catch (e: IOException) {
                throw ExceptionConverter(e)
            }

        }

        /**
         * Adds an open action to the catalog.
         * @param    action    the action that will be triggered upon opening the document
         */
        fun setOpenAction(action: PdfAction) {
            put(PdfName.OPENACTION, action)
        }


        /**
         * Sets the document level additional actions.
         * @param actions   dictionary of actions
         */
        fun setAdditionalActions(actions: PdfDictionary) {
            try {
                put(PdfName.AA, writer.addToBody(actions).indirectReference)
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }
    }

    init {
        addProducer()
        addCreationDate()
    }

    /** The PdfWriter.  */
    protected var writer: PdfWriter? = null

    private val structElements = HashMap<AccessibleElementId, PdfStructureElement>()

    //fields for external caching support
    private var externalCache: TempFileCache? = null
    private val externallyStoredStructElements = HashMap<AccessibleElementId, TempFileCache.ObjectPosition>()
    private val elementsParents = HashMap<AccessibleElementId, AccessibleElementId>()
    private var isToUseExternalCache = false


    protected var openMCDocument = false

    protected var structParentIndices = HashMap<Any, IntArray>()

    protected var markPoints = HashMap<Any, Int>()

    /**
     * Adds a PdfWriter to the PdfDocument.

     * @param writer the PdfWriter that writes everything
     * *                     what is added to this document to an outputstream.
     * *
     * @throws DocumentException on error
     */
    @Throws(DocumentException::class)
    fun addWriter(writer: PdfWriter) {
        if (this.writer == null) {
            this.writer = writer
            annotationsImp = PdfAnnotationsImp(writer)
            return
        }
        throw DocumentException(MessageLocalization.getComposedMessage("you.can.only.add.a.writer.to.a.pdfdocument.once"))
    }

    // LISTENER METHODS START

    //	[L0] ElementListener interface

    /** This is the PdfContentByte object, containing the text.  */
    protected var text: PdfContentByte? = null

    /** This is the PdfContentByte object, containing the borders and other Graphics.  */
    protected var graphics: PdfContentByte

    /** This represents the leading of the lines.  */
    /**
     * Getter for the current leading.
     * @return    the current leading
     * *
     * @since    2.1.2
     */
    /**
     * Setter for the current leading.
     * @param    leading the current leading
     * *
     * @since    2.1.6
     */
    var leading = 0f
        internal set(leading) {
            this.leading = leading
        }

    /** This represents the current alignment of the PDF Elements.  */
    protected var alignment = Element.ALIGN_LEFT

    /** This is the current height of the document.  */
    protected var currentHeight = 0f

    /**
     * Signals that onParagraph is valid (to avoid that a Chapter/Section title is treated as a Paragraph).
     * @since 2.1.2
     */
    protected var isSectionTitle = false

    /** The current active PdfAction when processing an Anchor.  */
    protected var anchorAction: PdfAction? = null

    /**
     * The current tab settings.
     * @return    the current
     * *
     * @since 5.4.0
     */
    /**
     * Getter for the current tab stops.
     * @since    5.4.0
     */
    /**
     * Setter for the current tab stops.
     * @param    tabSettings the current tab settings
     * *
     * @since    5.4.0
     */
    var tabSettings: TabSettings

    /**
     * Signals that the current leading has to be subtracted from a YMark object when positive
     * and save current leading
     * @since 2.1.2
     */
    private val leadingStack = Stack<Float>()

    private var body: PdfBody? = null

    /**
     * Save current @leading
     */
    protected fun pushLeading() {
        leadingStack.push(leading)
    }

    /**
     * Restore @leading from leadingStack
     */
    protected fun popLeading() {
        leading = leadingStack.pop()
        if (leadingStack.size > 0)
            leading = leadingStack.peek()
    }

    /**
     * Signals that an Element was added to the Document.

     * @param element the element to add
     * *
     * @return true if the element was added, false if not.
     * *
     * @throws DocumentException when a document isn't open yet, or has been closed
     */
    @Throws(DocumentException::class)
    override fun add(element: Element): Boolean {
        if (writer != null && writer!!.isPaused) {
            return false
        }
        try {
            if (element.type() != Element.DIV) {
                flushFloatingElements()
            }
            // TODO refactor this uber long switch to State/Strategy or something ...
            when (element.type()) {
            // Information (headers)
                Element.HEADER -> info.addkey((element as Meta).name, element.content)
                Element.TITLE -> info.addTitle((element as Meta).content)
                Element.SUBJECT -> info.addSubject((element as Meta).content)
                Element.KEYWORDS -> info.addKeywords((element as Meta).content)
                Element.AUTHOR -> info.addAuthor((element as Meta).content)
                Element.CREATOR -> info.addCreator((element as Meta).content)
                Element.LANGUAGE -> setLanguage((element as Meta).content)
                Element.PRODUCER -> // you can not change the name of the producer
                    info.addProducer()
                Element.CREATIONDATE -> // you can not set the creation date, only reset it
                    info.addCreationDate()
            // content (text)
                Element.CHUNK -> {
                    // if there isn't a current line available, we make one
                    if (line == null) {
                        carriageReturn()
                    }

                    // we cast the element to a chunk
                    var chunk = PdfChunk(element as Chunk, anchorAction, tabSettings)
                    // we try to add the chunk to the line, until we succeed
                    run {
                        var overflow: PdfChunk
                        while ((overflow = line!!.add(chunk, leading)) != null) {
                            carriageReturn()
                            val newlineSplit = chunk.isNewlineSplit
                            chunk = overflow
                            if (!newlineSplit)
                                chunk.trimFirstSpace()
                        }
                    }

                    isPageEmpty = false

                    if (chunk.isAttribute(Chunk.NEWPAGE)) {
                        newPage()
                    }
                }
                Element.ANCHOR -> {
                    val anchor = element as Anchor
                    val url = anchor.reference
                    leading = anchor.leading
                    pushLeading()
                    if (url != null) {
                        anchorAction = PdfAction(url)
                    }
                    // we process the element
                    element.process(this)
                    anchorAction = null
                    popLeading()
                }
                Element.ANNOTATION -> {
                    if (line == null) {
                        carriageReturn()
                    }
                    val annot = element as Annotation
                    var rect = Rectangle(0f, 0f)
                    if (line != null)
                        rect = Rectangle(annot.llx(indentRight() - line!!.widthLeft()), annot.ury(indentTop() - currentHeight - 20f), annot.urx(indentRight() - line!!.widthLeft() + 20), annot.lly(indentTop() - currentHeight))
                    val an = PdfAnnotationsImp.convertAnnotation(writer, annot, rect)
                    annotationsImp.addPlainAnnotation(an)
                    isPageEmpty = false
                }
                Element.PHRASE -> {
                    val backupTabSettings = tabSettings
                    if ((element as Phrase).tabSettings != null)
                        tabSettings = element.tabSettings
                    // we cast the element to a phrase and set the leading of the document
                    leading = element.totalLeading
                    pushLeading()
                    // we process the element
                    element.process(this)
                    tabSettings = backupTabSettings
                    popLeading()
                }
                Element.PARAGRAPH -> {
                    val backupTabSettings = tabSettings
                    if ((element as Phrase).tabSettings != null)
                        tabSettings = element.tabSettings
                    // we cast the element to a paragraph
                    val paragraph = element as Paragraph
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.openMCBlock(paragraph)
                    }
                    addSpacing(paragraph.getSpacingBefore(), leading, paragraph.font)

                    // we adjust the parameters of the document
                    alignment = paragraph.alignment
                    leading = paragraph.totalLeading
                    pushLeading()
                    carriageReturn()

                    // we don't want to make orphans/widows
                    if (currentHeight + calculateLineHeight() > indentTop() - indentBottom()) {
                        newPage()
                    }

                    indentation.indentLeft += paragraph.getIndentationLeft()
                    indentation.indentRight += paragraph.getIndentationRight()
                    carriageReturn()

                    val pageEvent = writer!!.pageEvent
                    if (pageEvent != null && !isSectionTitle)
                        pageEvent.onParagraph(writer, this, indentTop() - currentHeight)

                    // if a paragraph has to be kept together, we wrap it in a table object
                    if (paragraph.keepTogether) {
                        carriageReturn()
                        val table = PdfPTable(1)
                        table.keepTogether = paragraph.keepTogether
                        table.widthPercentage = 100f
                        val cell = PdfPCell()
                        cell.addElement(paragraph)
                        cell.setBorder(Rectangle.NO_BORDER)
                        cell.setPadding(0f)
                        table.addCell(cell)
                        indentation.indentLeft -= paragraph.getIndentationLeft()
                        indentation.indentRight -= paragraph.getIndentationRight()
                        this.add(table)
                        indentation.indentLeft += paragraph.getIndentationLeft()
                        indentation.indentRight += paragraph.getIndentationRight()
                    } else {
                        line!!.setExtraIndent(paragraph.firstLineIndent)
                        val oldHeight = currentHeight
                        element.process(this)
                        carriageReturn()
                        if (oldHeight != currentHeight || lines!!.size > 0) {
                            addSpacing(paragraph.getSpacingAfter(), paragraph.totalLeading, paragraph.font, true)
                        }
                    }

                    if (pageEvent != null && !isSectionTitle)
                        pageEvent.onParagraphEnd(writer, this, indentTop() - currentHeight)

                    alignment = Element.ALIGN_LEFT
                    if (floatingElements != null && floatingElements!!.size != 0) {
                        flushFloatingElements()
                    }
                    indentation.indentLeft -= paragraph.getIndentationLeft()
                    indentation.indentRight -= paragraph.getIndentationRight()
                    carriageReturn()
                    tabSettings = backupTabSettings
                    popLeading()
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.closeMCBlock(paragraph)
                    }
                }
                Element.SECTION, Element.CHAPTER -> {
                    // Chapters and Sections only differ in their constructor
                    // so we cast both to a Section
                    val section = element as Section
                    val pageEvent = writer!!.pageEvent

                    val hasTitle = section.isNotAddedYet && section.title != null

                    // if the section is a chapter, we begin a new page
                    if (section.isTriggerNewPage) {
                        newPage()
                    }

                    if (hasTitle) {
                        var fith = indentTop() - currentHeight
                        val rotation = pageSize.rotation
                        if (rotation == 90 || rotation == 180)
                            fith = pageSize.height - fith
                        val destination = PdfDestination(PdfDestination.FITH, fith)
                        while (currentOutline.level() >= section.depth) {
                            currentOutline = currentOutline.parent()
                        }
                        val outline = PdfOutline(currentOutline, destination, section.bookmarkTitle, section.isBookmarkOpen)
                        currentOutline = outline
                    }

                    // some values are set
                    carriageReturn()
                    indentation.sectionIndentLeft += section.getIndentationLeft()
                    indentation.sectionIndentRight += section.getIndentationRight()

                    if (section.isNotAddedYet && pageEvent != null)
                        if (element.type() == Element.CHAPTER)
                            pageEvent.onChapter(writer, this, indentTop() - currentHeight, section.title)
                        else
                            pageEvent.onSection(writer, this, indentTop() - currentHeight, section.depth, section.title)

                    // the title of the section (if any has to be printed)
                    if (hasTitle) {
                        isSectionTitle = true
                        add(section.title)
                        isSectionTitle = false
                    }
                    indentation.sectionIndentLeft += section.indentation
                    // we process the section
                    element.process(this)
                    flushLines()
                    // some parameters are set back to normal again
                    indentation.sectionIndentLeft -= section.getIndentationLeft() + section.indentation
                    indentation.sectionIndentRight -= section.getIndentationRight()

                    if (section.isComplete && pageEvent != null)
                        if (element.type() == Element.CHAPTER)
                            pageEvent.onChapterEnd(writer, this, indentTop() - currentHeight)
                        else
                            pageEvent.onSectionEnd(writer, this, indentTop() - currentHeight)
                }
                Element.LIST -> {
                    // we cast the element to a List
                    val list = element as List
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.openMCBlock(list)
                    }
                    if (list.isAlignindent) {
                        list.normalizeIndentation()
                    }
                    // we adjust the document
                    indentation.listIndentLeft += list.getIndentationLeft()
                    indentation.indentRight += list.getIndentationRight()
                    // we process the items in the list
                    element.process(this)

                    // some parameters are set back to normal again
                    indentation.listIndentLeft -= list.getIndentationLeft()
                    indentation.indentRight -= list.getIndentationRight()
                    carriageReturn()
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.closeMCBlock(list)
                    }
                }
                Element.LISTITEM -> {
                    // we cast the element to a ListItem
                    val listItem = element as ListItem
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.openMCBlock(listItem)
                    }

                    addSpacing(listItem.getSpacingBefore(), leading, listItem.font)

                    // we adjust the document
                    alignment = listItem.alignment
                    indentation.listIndentLeft += listItem.getIndentationLeft()
                    indentation.indentRight += listItem.getIndentationRight()
                    leading = listItem.totalLeading
                    pushLeading()
                    carriageReturn()

                    // we prepare the current line to be able to show us the listsymbol
                    line!!.setListItem(listItem)
                    // we process the item
                    element.process(this)
                    addSpacing(listItem.getSpacingAfter(), listItem.totalLeading, listItem.font, true)

                    // if the last line is justified, it should be aligned to the left
                    if (line!!.hasToBeJustified()) {
                        line!!.resetAlignment()
                    }
                    // some parameters are set back to normal again
                    carriageReturn()
                    indentation.listIndentLeft -= listItem.getIndentationLeft()
                    indentation.indentRight -= listItem.getIndentationRight()
                    popLeading()
                    if (isTagged(writer)) {
                        flushLines()
                        text!!.closeMCBlock(listItem.listBody)
                        text!!.closeMCBlock(listItem)
                    }
                }
                Element.RECTANGLE -> {
                    val rectangle = element as Rectangle
                    graphics.rectangle(rectangle)
                    isPageEmpty = false
                }
                Element.PTABLE -> {
                    val ptable = element as PdfPTable
                    if (ptable.size() <= ptable.headerRows)
                        break //nothing to do

                    // before every table, we add a new line and flush all lines
                    ensureNewLine()
                    flushLines()

                    addPTable(ptable)
                    isPageEmpty = false
                    newLine()
                }
                Element.JPEG, Element.JPEG2000, Element.JBIG2, Element.IMGRAW, Element.IMGTEMPLATE -> {
                    //carriageReturn(); suggestion by Marc Campforts
                    if (isTagged(writer) && !(element as Image).isImgTemplate) {
                        flushLines()
                        text!!.openMCBlock(element)
                    }
                    add(element as Image)
                    if (isTagged(writer) && !element.isImgTemplate) {
                        flushLines()
                        text!!.closeMCBlock(element)
                    }
                }
                Element.YMARK -> {
                    val zh = element as DrawInterface
                    zh.draw(graphics, indentLeft(), indentBottom(), indentRight(), indentTop(), indentTop() - currentHeight - if (leadingStack.size > 0) leading else 0)
                    isPageEmpty = false
                }
                Element.MARKED -> {
                    var mo: MarkedObject?
                    if (element is MarkedSection) {
                        mo = element.title
                        if (mo != null) {
                            mo.process(this)
                        }
                    }
                    mo = element as MarkedObject
                    mo.process(this)
                }
                Element.WRITABLE_DIRECT -> if (null != writer) {
                    (element as WriterOperation).write(writer, this)
                }
                Element.DIV -> {
                    ensureNewLine()
                    flushLines()
                    addDiv(element as PdfDiv)
                    isPageEmpty = false
                }
                Element.BODY -> {
                    body = element as PdfBody
                    graphics.rectangle(body)
                    return false
                }
                else -> return false
            }//newLine();
            lastElementType = element.type()
            return true
        } catch (e: Exception) {
            throw DocumentException(e)
        }

    }

    //	[L1] DocListener interface

    /**
     * Opens the document.
     *
     * You have to open the document before you can begin to add content
     * to the body of the document.
     */
    override fun open() {
        if (!open) {
            super.open()
            writer!!.open()
            rootOutline = PdfOutline(writer)
            currentOutline = rootOutline
        }
        try {
            if (isTagged(writer)) {
                openMCDocument = true
            }
            initPage()
        } catch (de: DocumentException) {
            throw ExceptionConverter(de)
        }

    }

    //	[L2] DocListener interface

    /**
     * Closes the document.
     *
     * Once all the content has been written in the body, you have to close
     * the body. After that nothing can be written to the body anymore.
     */
    override fun close() {
        if (close) {
            return
        }
        try {
            if (isTagged(writer)) {
                flushFloatingElements()
                flushLines()
                writer!!.flushAcroFields()
                writer!!.flushTaggedObjects()
                if (isPageEmpty) {
                    val pageReferenceCount = writer!!.pageReferences.size
                    if (pageReferenceCount > 0 && writer!!.currentPageNumber == pageReferenceCount) {
                        writer!!.pageReferences.removeAt(pageReferenceCount - 1)
                    }
                }
            } else
                writer!!.flushAcroFields()
            if (imageWait != null) {
                newPage()
            }
            endPage()
            if (isTagged(writer)) {
                writer!!.getDirectContent().closeMCBlock(this)
            }
            if (annotationsImp.hasUnusedAnnotations())
                throw RuntimeException(MessageLocalization.getComposedMessage("not.all.annotations.could.be.added.to.the.document.the.document.doesn.t.have.enough.pages"))
            val pageEvent = writer!!.pageEvent
            pageEvent?.onCloseDocument(writer, this)
            super.close()

            writer!!.addLocalDestinations(localDestinations)
            calculateOutlineCount()
            writeOutlines()
        } catch (e: Exception) {
            throw ExceptionConverter.convertException(e)
        }

        writer!!.close()
    }

    //	[L3] DocListener interface
    protected var textEmptySize: Int = 0

    // [C9] Metadata for the page
    /**
     * Use this method to set the XMP Metadata.
     * @param xmpMetadata The xmpMetadata to set.
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun setXmpMetadata(xmpMetadata: ByteArray) {
        val xmp = PdfStream(xmpMetadata)
        xmp.put(PdfName.TYPE, PdfName.METADATA)
        xmp.put(PdfName.SUBTYPE, PdfName.XML)
        val crypto = writer!!.encryption
        if (crypto != null && !crypto.isMetadataEncrypted) {
            val ar = PdfArray()
            ar.add(PdfName.CRYPT)
            xmp.put(PdfName.FILTER, ar)
        }
        writer!!.addPageDictEntry(PdfName.METADATA, writer!!.addToBody(xmp).indirectReference)
    }

    /**
     * Makes a new page and sends it to the PdfWriter.

     * @return true if new page is added
     */
    override fun newPage(): Boolean {
        if (isPageEmpty) {
            setNewPageSizeAndMargins()
            return false
        }
        if (!open || close) {
            throw RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"))
        }

        //we end current page
        val savedMcBlocks = endPage()

        //Added to inform any listeners that we are moving to a new page (added by David Freels)
        super.newPage()

        // the following 2 lines were added by Pelikan Stephan
        indentation.imageIndentLeft = 0f
        indentation.imageIndentRight = 0f

        try {
            if (isTagged(writer)) {
                flushStructureElementsOnNewPage()
                writer!!.getDirectContentUnder().restoreMCBlocks(savedMcBlocks)
            }

            // we initialize the new page
            initPage()

            if (body != null && body!!.getBackgroundColor() != null) {
                graphics.rectangle(body)
            }
        } catch (de: DocumentException) {
            // maybe this never happens, but it's better to check.
            throw ExceptionConverter(de)
        }

        return true
    }

    protected fun endPage(): ArrayList<IAccessibleElement>? {
        if (isPageEmpty) {
            return null
        }

        var savedMcBlocks: ArrayList<IAccessibleElement>? = null

        try {
            flushFloatingElements()
        } catch (de: DocumentException) {
            // maybe this never happens, but it's better to check.
            throw ExceptionConverter(de)
        }

        lastElementType = -1

        val pageEvent = writer!!.pageEvent
        pageEvent?.onEndPage(writer, this)

        try {
            // we flush the arraylist with recently written lines
            flushLines()

            // we prepare the elements of the page dictionary

            // [U1] page size and rotation
            val rotation = pageSize.rotation

            // [C10]
            if (writer!!.isPdfIso) {
                if (thisBoxSize.containsKey("art") && thisBoxSize.containsKey("trim"))
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("only.one.of.artbox.or.trimbox.can.exist.in.the.page"))
                if (!thisBoxSize.containsKey("art") && !thisBoxSize.containsKey("trim")) {
                    if (thisBoxSize.containsKey("crop"))
                        thisBoxSize.put("trim", thisBoxSize["crop"])
                    else
                        thisBoxSize.put("trim", PdfRectangle(pageSize, pageSize.rotation))
                }
            }

            // [M1]
            pageResources.addDefaultColorDiff(writer!!.defaultColorspace)
            if (writer!!.isRgbTransparencyBlending) {
                val dcs = PdfDictionary()
                dcs.put(PdfName.CS, PdfName.DEVICERGB)
                pageResources.addDefaultColorDiff(dcs)
            }
            val resources = pageResources.resources

            // we create the page dictionary

            val page = PdfPage(PdfRectangle(pageSize, rotation), thisBoxSize, resources, rotation)
            if (isTagged(writer)) {
                page.put(PdfName.TABS, PdfName.S)
            } else {
                page.put(PdfName.TABS, writer!!.tabs)
            }
            page.putAll(writer!!.pageDictEntries)
            writer!!.resetPageDictEntries()

            // we complete the page dictionary

            // [U3] page actions: additional actions
            if (pageAA != null) {
                page.put(PdfName.AA, writer!!.addToBody(pageAA).indirectReference)
                pageAA = null
            }

            // [C5] and [C8] we add the annotations
            if (annotationsImp.hasUnusedAnnotations()) {
                val array = annotationsImp.rotateAnnotations(writer, pageSize)
                if (array.size() != 0)
                    page.put(PdfName.ANNOTS, array)
            }

            // [F12] we add tag info
            if (isTagged(writer))
                page.put(PdfName.STRUCTPARENTS, PdfNumber(getStructParentIndex(writer!!.currentPage)))

            if (text!!.size() > textEmptySize || isTagged(writer))
                text!!.endText()
            else
                text = null

            if (isTagged(writer)) {
                savedMcBlocks = writer!!.getDirectContent().saveMCBlocks()
            }
            writer!!.add(page, PdfContents(writer!!.getDirectContentUnder(), graphics, if (!isTagged(writer)) text else null, writer!!.getDirectContent(), pageSize))

            annotationsImp.resetAnnotations()
            writer!!.resetContent()
        } catch (de: DocumentException) {
            // maybe this never happens, but it's better to check.
            throw ExceptionConverter(de)
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

        return savedMcBlocks
    }
    //	[L4] DocListener interface

    /**
     * Sets the pagesize.

     * @param pageSize the new pagesize
     * *
     * @return true if the page size was set
     */
    override fun setPageSize(pageSize: Rectangle): Boolean {
        if (writer != null && writer!!.isPaused) {
            return false
        }
        nextPageSize = Rectangle(pageSize)
        return true
    }

    //	[L5] DocListener interface

    /** margin in x direction starting from the left. Will be valid in the next page  */
    protected var nextMarginLeft: Float = 0.toFloat()

    /** margin in x direction starting from the right. Will be valid in the next page  */
    protected var nextMarginRight: Float = 0.toFloat()

    /** margin in y direction starting from the top. Will be valid in the next page  */
    protected var nextMarginTop: Float = 0.toFloat()

    /** margin in y direction starting from the bottom. Will be valid in the next page  */
    protected var nextMarginBottom: Float = 0.toFloat()

    /**
     * Sets the margins.

     * @param    marginLeft        the margin on the left
     * *
     * @param    marginRight        the margin on the right
     * *
     * @param    marginTop        the margin on the top
     * *
     * @param    marginBottom    the margin on the bottom
     * *
     * @return    a boolean
     */
    override fun setMargins(marginLeft: Float, marginRight: Float, marginTop: Float, marginBottom: Float): Boolean {
        if (writer != null && writer!!.isPaused) {
            return false
        }
        nextMarginLeft = marginLeft
        nextMarginRight = marginRight
        nextMarginTop = marginTop
        nextMarginBottom = marginBottom
        return true
    }

    //	[L6] DocListener interface

    /**
     * @see com.itextpdf.text.DocListener.setMarginMirroring
     */
    override fun setMarginMirroring(MarginMirroring: Boolean): Boolean {
        if (writer != null && writer!!.isPaused) {
            return false
        }
        return super.setMarginMirroring(MarginMirroring)
    }

    /**
     * @see com.itextpdf.text.DocListener.setMarginMirroring
     * @since    2.1.6
     */
    override fun setMarginMirroringTopBottom(MarginMirroringTopBottom: Boolean): Boolean {
        if (writer != null && writer!!.isPaused) {
            return false
        }
        return super.setMarginMirroringTopBottom(MarginMirroringTopBottom)
    }

    //	[L7] DocListener interface

    /**
     * Sets the page number.

     * @param    pageN        the new page number
     */
    override fun setPageCount(pageN: Int) {
        if (writer != null && writer!!.isPaused) {
            return
        }
        super.setPageCount(pageN)
    }

    //	[L8] DocListener interface

    /**
     * Sets the page number to 0.
     */
    override fun resetPageCount() {
        if (writer != null && writer!!.isPaused) {
            return
        }
        super.resetPageCount()
    }

    // DOCLISTENER METHODS END

    /** Signals that OnOpenDocument should be called.  */
    protected var firstPageEvent = true

    /**
     * Initializes a page.
     *
     * If the footer/header is set, it is printed.
     * @throws DocumentException on error
     */
    @Throws(DocumentException::class)
    protected fun initPage() {
        // the pagenumber is incremented
        pageN++

        // initialization of some page objects
        pageResources = PageResources()

        if (isTagged(writer)) {
            graphics = writer!!.getDirectContentUnder().duplicate
            writer!!.getDirectContent().duplicatedFrom = graphics
        } else {
            graphics = PdfContentByte(writer)
        }

        setNewPageSizeAndMargins()
        imageEnd = -1f
        indentation.imageIndentRight = 0f
        indentation.imageIndentLeft = 0f
        indentation.indentBottom = 0f
        indentation.indentTop = 0f
        currentHeight = 0f

        // backgroundcolors, etc...
        thisBoxSize = HashMap(boxSize)
        if (pageSize.backgroundColor != null
                || pageSize.hasBorders()
                || pageSize.borderColor != null) {
            add(pageSize)
        }

        val oldleading = leading
        val oldAlignment = alignment
        isPageEmpty = true
        // if there is an image waiting to be drawn, draw it
        try {
            if (imageWait != null) {
                add(imageWait)
                imageWait = null
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        leading = oldleading
        alignment = oldAlignment
        carriageReturn()

        val pageEvent = writer!!.pageEvent
        if (pageEvent != null) {
            if (firstPageEvent) {
                pageEvent.onOpenDocument(writer, this)
            }
            pageEvent.onStartPage(writer, this)
        }
        firstPageEvent = false
    }

    /** The line that is currently being written.  */
    protected var line: PdfLine? = null
    /** The lines that are written until now.  */
    protected var lines: ArrayList<PdfLine>? = ArrayList()

    /**
     * Adds the current line to the list of lines and also adds an empty line.
     * @throws DocumentException on error
     */
    @Throws(DocumentException::class)
    protected fun newLine() {
        lastElementType = -1
        carriageReturn()
        if (lines != null && !lines!!.isEmpty()) {
            lines!!.add(line)
            currentHeight += line!!.height()
        }
        line = PdfLine(indentLeft(), indentRight(), alignment, leading)
    }

    /**
     * line.height() is usually the same as the leading
     * We should take leading into account if it is not the same as the line.height

     * @return float combined height of the line
     * *
     * @since 5.5.1
     */
    protected fun calculateLineHeight(): Float {
        var tempHeight = line!!.height()

        if (tempHeight != leading) {
            tempHeight += leading
        }

        return tempHeight
    }

    /**
     * If the current line is not empty or null, it is added to the arraylist
     * of lines and a new empty line is added.
     */
    protected fun carriageReturn() {
        // the arraylist with lines may not be null
        if (lines == null) {
            lines = ArrayList<PdfLine>()
        }
        // If the current line is not null or empty
        if (line != null && line!!.size() > 0) {
            // we check if the end of the page is reached (bugfix by Francois Gravel)
            if (currentHeight + calculateLineHeight() > indentTop() - indentBottom()) {
                // if the end of the line is reached, we start a newPage which will flush existing lines
                // then move to next page but before then we need to exclude the current one that does not fit
                // After the new page we add the current line back in
                if (currentHeight != 0f) {
                    val overflowLine = line
                    line = null
                    newPage()
                    line = overflowLine
                    //update left indent because of mirror margins.
                    overflowLine.left = indentLeft()
                }
            }
            currentHeight += line!!.height()
            lines!!.add(line)
            isPageEmpty = false
        }
        if (imageEnd > -1 && currentHeight > imageEnd) {
            imageEnd = -1f
            indentation.imageIndentRight = 0f
            indentation.imageIndentLeft = 0f
        }
        // a new current line is constructed
        line = PdfLine(indentLeft(), indentRight(), alignment, leading)
    }

    /**
     * Gets the current vertical page position.
     * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects
     * *   for elements that do not terminate the lines they've started because those lines will get
     * *   terminated.
     * *
     * @return The current vertical page position.
     */
    fun getVerticalPosition(ensureNewLine: Boolean): Float {
        // ensuring that a new line has been started.
        if (ensureNewLine) {
            ensureNewLine()
        }
        return top() - currentHeight - indentation.indentTop
    }

    /** Holds the type of the last element, that has been added to the document.  */
    protected var lastElementType = -1

    /**
     * Ensures that a new line has been started.
     */
    protected fun ensureNewLine() {
        try {
            if (lastElementType == Element.PHRASE || lastElementType == Element.CHUNK) {
                newLine()
                flushLines()
            }
        } catch (ex: DocumentException) {
            throw ExceptionConverter(ex)
        }

    }

    /**
     * Writes all the lines to the text-object.

     * @return the displacement that was caused
     * *
     * @throws DocumentException on error
     */
    @Throws(DocumentException::class)
    protected fun flushLines(): Float {
        // checks if the ArrayList with the lines is not null
        if (lines == null) {
            return 0f
        }
        // checks if a new Line has to be made.
        if (line != null && line!!.size() > 0) {
            lines!!.add(line)
            line = PdfLine(indentLeft(), indentRight(), alignment, leading)
        }

        // checks if the ArrayList with the lines is empty
        if (lines!!.isEmpty()) {
            return 0f
        }

        // initialization of some parameters
        val currentValues = arrayOfNulls<Any>(2)
        var currentFont: PdfFont? = null
        var displacement = 0f
        val lastBaseFactor = 0
        currentValues[1] = lastBaseFactor
        // looping over all the lines
        for (l in lines!!) {
            val moveTextX = l.indentLeft() - indentLeft() + indentation.indentLeft + indentation.listIndentLeft + indentation.sectionIndentLeft
            text!!.moveText(moveTextX, -l.height())
            // is the line preceded by a symbol?
            l.flush()

            if (l.listSymbol() != null) {
                var lbl: ListLabel? = null
                var symbol: Chunk = l.listSymbol()
                if (isTagged(writer)) {
                    lbl = l.listItem().listLabel
                    graphics.openMCBlock(lbl)
                    symbol = Chunk(symbol)
                    symbol.setRole(null)
                }
                ColumnText.showTextAligned(graphics, Element.ALIGN_LEFT, Phrase(symbol), text!!.xtlm - l.listIndent(), text!!.ytlm, 0f)
                if (lbl != null) {
                    graphics.closeMCBlock(lbl)
                }
            }

            currentValues[0] = currentFont

            if (isTagged(writer) && l.listItem() != null) {
                text!!.openMCBlock(l.listItem().listBody)
            }
            writeLineToContent(l, text, graphics, currentValues, writer!!.spaceCharRatio)

            currentFont = currentValues[0] as PdfFont
            displacement += l.height()
            text!!.moveText(-moveTextX, 0f)

        }
        lines = ArrayList<PdfLine>()
        return displacement
    }

    /**
     * Writes a text line to the document. It takes care of all the attributes.
     *
     * Before entering the line position must have been established and the
     * text argument must be in text object scope (beginText()).
     * @param line the line to be written
     * *
     * @param text the PdfContentByte where the text will be written to
     * *
     * @param graphics the PdfContentByte where the graphics will be written to
     * *
     * @param currentValues the current font and extra spacing values
     * *
     * @param ratio
     * *
     * @throws DocumentException on error
     * *
     * @since 5.0.3 returns a float instead of void
     */
    @Throws(DocumentException::class)
    internal fun writeLineToContent(line: PdfLine, text: PdfContentByte, graphics: PdfContentByte, currentValues: Array<Any>, ratio: Float): Float {
        var currentFont = currentValues[0] as PdfFont
        var lastBaseFactor = (currentValues[1] as Float).toFloat()
        var chunk: PdfChunk
        val numberOfSpaces: Int
        val lineLen: Int
        val isJustified: Boolean
        var hangingCorrection = 0f
        var hScale = 1f
        var lastHScale = java.lang.Float.NaN
        var baseWordSpacing = 0f
        var baseCharacterSpacing = 0f
        var glueWidth = 0f
        var lastX = text.xtlm + line.originalWidth
        numberOfSpaces = line.numberOfSpaces()
        lineLen = line.lineLengthUtf32
        // does the line need to be justified?
        isJustified = line.hasToBeJustified() && (numberOfSpaces != 0 || lineLen > 1)
        val separatorCount = line.separatorCount
        if (separatorCount > 0) {
            glueWidth = line.widthLeft() / separatorCount
        } else if (isJustified && separatorCount == 0) {
            if (line.isNewlineSplit && line.widthLeft() >= lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1)) {
                if (line.isRTL) {
                    text.moveText(line.widthLeft() - lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1), 0f)
                }
                baseWordSpacing = ratio * lastBaseFactor
                baseCharacterSpacing = lastBaseFactor
            } else {
                var width = line.widthLeft()
                val last = line.getChunk(line.size() - 1)
                if (last != null) {
                    val s = last.toString()
                    val c: Char
                    if (s.length > 0 && hangingPunctuation.indexOf((c = s[s.length - 1]).toInt()) >= 0) {
                        val oldWidth = width
                        width += last.font().width(c.toInt()) * 0.4f
                        hangingCorrection = width - oldWidth
                    }
                }
                val baseFactor = width / (ratio * numberOfSpaces + lineLen - 1)
                baseWordSpacing = ratio * baseFactor
                baseCharacterSpacing = baseFactor
                lastBaseFactor = baseFactor
            }
        } else if (line.alignment == Element.ALIGN_LEFT || line.alignment == Element.ALIGN_UNDEFINED) {
            lastX -= line.widthLeft()
        }

        val lastChunkStroke = line.lastStrokeChunk
        var chunkStrokeIdx = 0
        var xMarker = text.xtlm
        val baseXMarker = xMarker
        val yMarker = text.ytlm
        var adjustMatrix = false
        var tabPosition = 0f
        var isMCBlockOpened = false
        // looping over all the chunks in 1 line
        val j = line.iterator()
        while (j.hasNext()) {
            chunk = j.next()
            if (isTagged(writer) && chunk.accessibleElement != null) {
                text.openMCBlock(chunk.accessibleElement)
                isMCBlockOpened = true
            }
            val color = chunk.color()
            var fontSize = chunk.font().size()
            val ascender: Float
            val descender: Float
            if (chunk.isImage) {
                ascender = chunk.height()
                fontSize = chunk.height()
                descender = 0f
            } else {
                ascender = chunk.font().font.getFontDescriptor(BaseFont.ASCENT, fontSize)
                descender = chunk.font().font.getFontDescriptor(BaseFont.DESCENT, fontSize)
            }
            hScale = 1f

            if (chunkStrokeIdx <= lastChunkStroke) {
                var width: Float
                if (isJustified) {
                    width = chunk.getWidthCorrected(baseCharacterSpacing, baseWordSpacing)
                } else {
                    width = chunk.width()
                }
                if (chunk.isStroked) {
                    val nextChunk = line.getChunk(chunkStrokeIdx + 1)
                    if (chunk.isSeparator) {
                        width = glueWidth
                        val sep = chunk.getAttribute(Chunk.SEPARATOR) as Array<Any>
                        val di = sep[0] as DrawInterface
                        val vertical = sep[1] as Boolean
                        if (vertical.booleanValue()) {
                            di.draw(graphics, baseXMarker, yMarker + descender, baseXMarker + line.originalWidth, ascender - descender, yMarker)
                        } else {
                            di.draw(graphics, xMarker, yMarker + descender, xMarker + width, ascender - descender, yMarker)
                        }
                    }
                    if (chunk.isTab) {
                        if (chunk.isAttribute(Chunk.TABSETTINGS)) {
                            val tabStop = chunk.tabStop
                            if (tabStop != null) {
                                tabPosition = tabStop.position + baseXMarker
                                if (tabStop.leader != null)
                                    tabStop.leader.draw(graphics, xMarker, yMarker + descender, tabPosition, ascender - descender, yMarker)
                            } else {
                                tabPosition = xMarker
                            }
                        } else {
                            //Keep deprecated tab logic for backward compatibility...
                            val tab = chunk.getAttribute(Chunk.TAB) as Array<Any>
                            val di = tab[0] as DrawInterface
                            tabPosition = (tab[1] as Float).toFloat() + (tab[3] as Float).toFloat()
                            if (tabPosition > xMarker) {
                                di.draw(graphics, xMarker, yMarker + descender, tabPosition, ascender - descender, yMarker)
                            }
                        }
                        val tmp = xMarker
                        xMarker = tabPosition
                        tabPosition = tmp
                    }
                    if (chunk.isAttribute(Chunk.BACKGROUND)) {
                        val bgr = chunk.getAttribute(Chunk.BACKGROUND) as Array<Any>
                        if (bgr[0] != null) {
                            val inText = graphics.inText
                            if (inText && isTagged(writer)) {
                                graphics.endText()
                            }
                            graphics.saveState()
                            var subtract = lastBaseFactor
                            if (nextChunk != null && nextChunk.isAttribute(Chunk.BACKGROUND)) {
                                subtract = 0f
                            }
                            if (nextChunk == null) {
                                subtract += hangingCorrection
                            }
                            val c = bgr[0] as BaseColor
                            graphics.setColorFill(c)
                            val extra = bgr[1] as FloatArray
                            graphics.rectangle(xMarker - extra[0],
                                    yMarker + descender - extra[1] + chunk.textRise,
                                    width - subtract + extra[0] + extra[2],
                                    ascender - descender + extra[1] + extra[3])
                            graphics.fill()
                            graphics.setGrayFill(0f)
                            graphics.restoreState()
                            if (inText && isTagged(writer)) {
                                graphics.beginText(true)
                            }
                        }
                    }
                    if (chunk.isAttribute(Chunk.UNDERLINE)) {
                        val inText = graphics.inText
                        if (inText && isTagged(writer)) {
                            graphics.endText()
                        }
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.UNDERLINE))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        val unders = chunk.getAttribute(Chunk.UNDERLINE) as Array<Array<Any>>
                        var scolor: BaseColor? = null
                        for (k in unders.indices) {
                            val obj = unders[k]
                            scolor = obj[0] as BaseColor
                            val ps = obj[1] as FloatArray
                            if (scolor == null)
                                scolor = color
                            if (scolor != null)
                                graphics.setColorStroke(scolor)
                            graphics.setLineWidth(ps[0] + chunk.font().size() * ps[1])
                            val shift = ps[2] + chunk.font().size() * ps[3]
                            val cap2 = ps[4].toInt()
                            if (cap2 != 0)
                                graphics.setLineCap(cap2)
                            graphics.moveTo(xMarker, yMarker + shift)
                            graphics.lineTo(xMarker + width - subtract, yMarker + shift)
                            graphics.stroke()
                            if (scolor != null)
                                graphics.resetGrayStroke()
                            if (cap2 != 0)
                                graphics.setLineCap(0)
                        }
                        graphics.setLineWidth(1f)
                        if (inText && isTagged(writer)) {
                            graphics.beginText(true)
                        }
                    }
                    if (chunk.isAttribute(Chunk.ACTION)) {
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.ACTION))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        var annot: PdfAnnotation? = null
                        if (chunk.isImage) {
                            annot = writer!!.createAnnotation(xMarker, yMarker + chunk.imageOffsetY, xMarker + width - subtract, yMarker + chunk.imageHeight + chunk.imageOffsetY, chunk.getAttribute(Chunk.ACTION) as PdfAction, null)
                        } else {
                            annot = writer!!.createAnnotation(xMarker, yMarker + descender + chunk.textRise, xMarker + width - subtract, yMarker + ascender + chunk.textRise, chunk.getAttribute(Chunk.ACTION) as PdfAction, null)
                        }
                        text.addAnnotation(annot, true)
                        if (isTagged(writer) && chunk.accessibleElement != null) {
                            val strucElem = getStructElement(chunk.accessibleElement!!.id)
                            if (strucElem != null) {
                                val structParent = getStructParentIndex(annot)
                                annot!!.put(PdfName.STRUCTPARENT, PdfNumber(structParent))
                                strucElem.setAnnotation(annot, writer!!.currentPage)
                                writer!!.getStructureTreeRoot().setAnnotationMark(structParent, strucElem.reference)
                            }
                        }
                    }
                    if (chunk.isAttribute(Chunk.REMOTEGOTO)) {
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.REMOTEGOTO))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        val obj = chunk.getAttribute(Chunk.REMOTEGOTO) as Array<Any>
                        val filename = obj[0] as String
                        if (obj[1] is String)
                            remoteGoto(filename, obj[1] as String, xMarker, yMarker + descender + chunk.textRise, xMarker + width - subtract, yMarker + ascender + chunk.textRise)
                        else
                            remoteGoto(filename, (obj[1] as Int).toInt(), xMarker, yMarker + descender + chunk.textRise, xMarker + width - subtract, yMarker + ascender + chunk.textRise)
                    }
                    if (chunk.isAttribute(Chunk.LOCALGOTO)) {
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALGOTO))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        localGoto(chunk.getAttribute(Chunk.LOCALGOTO) as String, xMarker, yMarker, xMarker + width - subtract, yMarker + fontSize)
                    }
                    if (chunk.isAttribute(Chunk.LOCALDESTINATION)) {
                        /*float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALDESTINATION))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;*/
                        localDestination(chunk.getAttribute(Chunk.LOCALDESTINATION) as String, PdfDestination(PdfDestination.XYZ, xMarker, yMarker + fontSize, 0f))
                    }
                    if (chunk.isAttribute(Chunk.GENERICTAG)) {
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.GENERICTAG))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        val rect = Rectangle(xMarker, yMarker, xMarker + width - subtract, yMarker + fontSize)
                        val pev = writer!!.pageEvent
                        pev?.onGenericTag(writer, this, rect, chunk.getAttribute(Chunk.GENERICTAG) as String)
                    }
                    if (chunk.isAttribute(Chunk.PDFANNOTATION)) {
                        var subtract = lastBaseFactor
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.PDFANNOTATION))
                            subtract = 0f
                        if (nextChunk == null)
                            subtract += hangingCorrection
                        val annot = PdfFormField.shallowDuplicate(chunk.getAttribute(Chunk.PDFANNOTATION) as PdfAnnotation)
                        annot.put(PdfName.RECT, PdfRectangle(xMarker, yMarker + descender, xMarker + width - subtract, yMarker + ascender))
                        text.addAnnotation(annot, true)
                    }
                    val params = chunk.getAttribute(Chunk.SKEW) as FloatArray
                    val hs = chunk.getAttribute(Chunk.HSCALE) as Float
                    if (params != null || hs != null) {
                        var b = 0f
                        var c = 0f
                        if (params != null) {
                            b = params[0]
                            c = params[1]
                        }
                        if (hs != null)
                            hScale = hs.toFloat()
                        text.setTextMatrix(hScale, b, c, 1f, xMarker, yMarker)
                    }
                    if (!isJustified) {
                        if (chunk.isAttribute(Chunk.WORD_SPACING)) {
                            val ws = chunk.getAttribute(Chunk.WORD_SPACING) as Float
                            text.wordSpacing = ws.toFloat()
                        }

                        if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
                            val cs = chunk.getAttribute(Chunk.CHAR_SPACING) as Float
                            text.characterSpacing = cs.toFloat()
                        }
                    }
                    if (chunk.isImage) {
                        val image = chunk.image
                        width = chunk.imageWidth
                        val matrix = image.matrix(chunk.imageScalePercentage)
                        matrix[Image.CX] = xMarker + chunk.imageOffsetX - matrix[Image.CX]
                        matrix[Image.CY] = yMarker + chunk.imageOffsetY - matrix[Image.CY]
                        graphics.addImage(image, matrix[0].toDouble(), matrix[1].toDouble(), matrix[2].toDouble(), matrix[3].toDouble(), matrix[4].toDouble(), matrix[5].toDouble(), false, isMCBlockOpened)
                        text.moveText(xMarker + lastBaseFactor + chunk.imageWidth - text.xtlm, 0f)
                    }
                }

                xMarker += width
                ++chunkStrokeIdx
            }

            if (!chunk.isImage && chunk.font().compareTo(currentFont) != 0) {
                currentFont = chunk.font()
                text.setFontAndSize(currentFont.font, currentFont.size())
            }
            var rise = 0f
            val textRender = chunk.getAttribute(Chunk.TEXTRENDERMODE) as Array<Any>
            var tr = 0
            var strokeWidth = 1f
            var strokeColor: BaseColor? = null
            val fr = chunk.getAttribute(Chunk.SUBSUPSCRIPT) as Float
            if (textRender != null) {
                tr = (textRender[0] as Int).toInt() and 3
                if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                    text.setTextRenderingMode(tr)
                if (tr == PdfContentByte.TEXT_RENDER_MODE_STROKE || tr == PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE) {
                    strokeWidth = (textRender[1] as Float).toFloat()
                    if (strokeWidth != 1f)
                        text.setLineWidth(strokeWidth)
                    strokeColor = textRender[2] as BaseColor
                    if (strokeColor == null)
                        strokeColor = color
                    if (strokeColor != null)
                        text.setColorStroke(strokeColor)
                }
            }
            if (fr != null)
                rise = fr.toFloat()
            if (color != null)
                text.setColorFill(color)
            if (rise != 0f)
                text.setTextRise(rise)
            if (chunk.isImage) {
                adjustMatrix = true
            } else if (chunk.isHorizontalSeparator) {
                val array = PdfTextArray()
                array.add(-glueWidth * 1000f / chunk.font.size() / hScale)
                text.showText(array)
            } else if (chunk.isTab && tabPosition != xMarker) {
                val array = PdfTextArray()
                array.add((tabPosition - xMarker) * 1000f / chunk.font.size() / hScale)
                text.showText(array)
            } else if (isJustified && numberOfSpaces > 0 && chunk.isSpecialEncoding) {
                if (hScale != lastHScale) {
                    lastHScale = hScale
                    text.wordSpacing = baseWordSpacing / hScale
                    text.characterSpacing = baseCharacterSpacing / hScale + text.characterSpacing
                }
                val s = chunk.toString()
                var idx = s.indexOf(' ')
                if (idx < 0)
                    text.showText(s)
                else {
                    val spaceCorrection = -baseWordSpacing * 1000f / chunk.font.size() / hScale
                    val textArray = PdfTextArray(s.substring(0, idx))
                    var lastIdx = idx
                    while ((idx = s.indexOf(' ', lastIdx + 1)) >= 0) {
                        textArray.add(spaceCorrection)
                        textArray.add(s.substring(lastIdx, idx))
                        lastIdx = idx
                    }
                    textArray.add(spaceCorrection)
                    textArray.add(s.substring(lastIdx))
                    text.showText(textArray)
                }
            } else {
                if (isJustified && hScale != lastHScale) {
                    lastHScale = hScale
                    text.wordSpacing = baseWordSpacing / hScale
                    text.characterSpacing = baseCharacterSpacing / hScale + text.characterSpacing
                }
                text.showText(chunk.toString())
            }// If it is a CJK chunk or Unicode TTF we will have to simulate the
            // space adjustment.

            if (rise != 0f)
                text.setTextRise(0f)
            if (color != null)
                text.resetRGBColorFill()
            if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                text.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL)
            if (strokeColor != null)
                text.resetRGBColorStroke()
            if (strokeWidth != 1f)
                text.setLineWidth(1f)
            if (chunk.isAttribute(Chunk.SKEW) || chunk.isAttribute(Chunk.HSCALE)) {
                adjustMatrix = true
                text.setTextMatrix(xMarker, yMarker)
            }
            if (!isJustified) {
                if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
                    text.characterSpacing = baseCharacterSpacing
                }
                if (chunk.isAttribute(Chunk.WORD_SPACING)) {
                    text.wordSpacing = baseWordSpacing
                }
            }
            if (isTagged(writer) && chunk.accessibleElement != null) {
                text.closeMCBlock(chunk.accessibleElement)
            }

        }
        if (isJustified) {
            text.wordSpacing = 0
            text.characterSpacing = 0
            if (line.isNewlineSplit)
                lastBaseFactor = 0f
        }
        if (adjustMatrix)
            text.moveText(baseXMarker - text.xtlm, 0f)
        currentValues[0] = currentFont
        currentValues[1] = lastBaseFactor
        return lastX
    }

    protected var indentation = Indentation()

    /**
     * @since    2.0.8 (PdfDocument was package-private before)
     */
    class Indentation {

        /** This represents the current indentation of the PDF Elements on the left side.  */
        internal var indentLeft = 0f

        /** Indentation to the left caused by a section.  */
        internal var sectionIndentLeft = 0f

        /** This represents the current indentation of the PDF Elements on the left side.  */
        internal var listIndentLeft = 0f

        /** This is the indentation caused by an image on the left.  */
        internal var imageIndentLeft = 0f

        /** This represents the current indentation of the PDF Elements on the right side.  */
        internal var indentRight = 0f

        /** Indentation to the right caused by a section.  */
        internal var sectionIndentRight = 0f

        /** This is the indentation caused by an image on the right.  */
        internal var imageIndentRight = 0f

        /** This represents the current indentation of the PDF Elements on the top side.  */
        internal var indentTop = 0f

        /** This represents the current indentation of the PDF Elements on the bottom side.  */
        internal var indentBottom = 0f
    }

    /**
     * Gets the indentation on the left side.

     * @return    a margin
     */

    protected fun indentLeft(): Float {
        return left(indentation.indentLeft + indentation.listIndentLeft + indentation.imageIndentLeft + indentation.sectionIndentLeft)
    }

    /**
     * Gets the indentation on the right side.

     * @return    a margin
     */

    protected fun indentRight(): Float {
        return right(indentation.indentRight + indentation.sectionIndentRight + indentation.imageIndentRight)
    }

    /**
     * Gets the indentation on the top side.

     * @return    a margin
     */

    protected fun indentTop(): Float {
        return top(indentation.indentTop)
    }

    /**
     * Gets the indentation on the bottom side.

     * @return    a margin
     */

    internal fun indentBottom(): Float {
        return bottom(indentation.indentBottom)
    }

    /**
     * Adds extra spacing.
     */
    // this method should probably be rewritten
    @JvmOverloads protected fun addSpacing(extraspace: Float, oldleading: Float, f: Font, spacingAfter: Boolean = false) {
        var f = f
        if (extraspace == 0f) {
            return
        }

        if (isPageEmpty) {
            return
        }

        val height = if (spacingAfter) extraspace else calculateLineHeight()

        if (currentHeight + height > indentTop() - indentBottom()) {
            newPage()
            return
        }

        leading = extraspace
        carriageReturn()
        if (f.isUnderlined || f.isStrikethru) {
            f = Font(f)
            var style = f.style
            style = style and Font.UNDERLINE.inv()
            style = style and Font.STRIKETHRU.inv()
            f.style = style
        }
        var space = Chunk(" ", f)
        if (spacingAfter && isPageEmpty) {
            space = Chunk("", f)
        }
        space.process(this)
        carriageReturn()

        leading = oldleading
    }

    //	Info Dictionary and Catalog

    /** some meta information about the Document.  */
    /**
     * Gets the PdfInfo-object.

     * @return    PdfInfo
     */

    internal var info = PdfInfo()
        protected set

    /**
     * Gets the PdfCatalog-object.

     * @param pages an indirect reference to this document pages
     * *
     * @return PdfCatalog
     */

    internal fun getCatalog(pages: PdfIndirectReference): PdfCatalog {
        val catalog = PdfCatalog(pages, writer)

        // [C1] outlines
        if (rootOutline.kids.size > 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES)
            catalog.put(PdfName.OUTLINES, rootOutline.indirectReference())
        }

        // [C2] version
        writer!!.pdfVersion.addToCatalog(catalog)

        // [C3] preferences
        viewerPreferences.addToCatalog(catalog)

        // [C4] pagelabels
        if (pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pageLabels!!.getDictionary(writer))
        }

        // [C5] named objects
        catalog.addNames(localDestinations, documentLevelJS, documentFileAttachment, writer)

        // [C6] actions
        if (openActionName != null) {
            val action = getLocalGotoAction(openActionName)
            catalog.setOpenAction(action)
        } else if (openActionAction != null)
            catalog.setOpenAction(openActionAction)
        if (additionalActions != null) {
            catalog.setAdditionalActions(additionalActions)
        }

        // [C7] portable collections
        if (collection != null) {
            catalog.put(PdfName.COLLECTION, collection)
        }

        // [C8] AcroForm
        if (annotationsImp.hasValidAcroForm()) {
            try {
                catalog.put(PdfName.ACROFORM, writer!!.addToBody(annotationsImp.acroForm).indirectReference)
            } catch (e: IOException) {
                throw ExceptionConverter(e)
            }

        }

        if (language != null) {
            catalog.put(PdfName.LANG, language)
        }

        return catalog
    }

    //	[C1] outlines

    /** This is the root outline of the document.  */
    /**
     * Gets the root outline. All the outlines must be created with a parent.
     * The first level is created with this outline.
     * @return the root outline
     */
    var rootOutline: PdfOutline
        protected set

    /** This is the current PdfOutline in the hierarchy of outlines.  */
    protected var currentOutline: PdfOutline

    /**
     * Adds a named outline to the document .
     * @param outline the outline to be added
     * *
     * @param name the name of this local destination
     */
    internal fun addOutline(outline: PdfOutline, name: String) {
        localDestination(name, outline.pdfDestination)
    }


    /**
     * Updates the count in the outlines.
     */
    internal fun calculateOutlineCount() {
        if (rootOutline.kids.size == 0)
            return
        traverseOutlineCount(rootOutline)
    }

    /**
     * Recursive method to update the count in the outlines.
     */
    internal fun traverseOutlineCount(outline: PdfOutline) {
        val kids = outline.kids
        val parent = outline.parent()
        if (kids.isEmpty()) {
            if (parent != null) {
                parent.count = parent.count + 1
            }
        } else {
            for (k in kids.indices) {
                traverseOutlineCount(kids[k])
            }
            if (parent != null) {
                if (outline.isOpen) {
                    parent.count = outline.count + parent.count + 1
                } else {
                    parent.count = parent.count + 1
                    outline.count = -outline.count
                }
            }
        }
    }

    /**
     * Writes the outline tree to the body of the PDF document.
     */
    @Throws(IOException::class)
    internal fun writeOutlines() {
        if (rootOutline.kids.size == 0)
            return
        outlineTree(rootOutline)
        writer!!.addToBody(rootOutline, rootOutline.indirectReference())
    }

    /**
     * Recursive method used to write outlines.
     */
    @Throws(IOException::class)
    internal fun outlineTree(outline: PdfOutline) {
        outline.setIndirectReference(writer!!.pdfIndirectReference)
        if (outline.parent() != null)
            outline.put(PdfName.PARENT, outline.parent().indirectReference())
        val kids = outline.kids
        val size = kids.size
        for (k in 0..size - 1)
            outlineTree(kids[k])
        for (k in 0..size - 1) {
            if (k > 0)
                kids[k].put(PdfName.PREV, kids[k - 1].indirectReference())
            if (k < size - 1)
                kids[k].put(PdfName.NEXT, kids[k + 1].indirectReference())
        }
        if (size > 0) {
            outline.put(PdfName.FIRST, kids[0].indirectReference())
            outline.put(PdfName.LAST, kids[size - 1].indirectReference())
        }
        for (k in 0..size - 1) {
            val kid = kids[k]
            writer!!.addToBody(kid, kid.indirectReference())
        }
    }

    //  [C3] PdfViewerPreferences interface

    /** Contains the Viewer preferences of this PDF document.  */
    protected var viewerPreferences = PdfViewerPreferencesImp()

    /** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences.setViewerPreferences
     */
    internal fun setViewerPreferences(preferences: Int) {
        this.viewerPreferences.setViewerPreferences(preferences)
    }

    /** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences.addViewerPreference
     */
    internal fun addViewerPreference(key: PdfName, value: PdfObject) {
        this.viewerPreferences.addViewerPreference(key, value)
    }

    //	[C4] Page labels

    /**
     * Sets the page labels
     * @param pageLabels the page labels
     */
    var pageLabels: PdfPageLabels? = null
        internal set(pageLabels) {
            this.pageLabels = pageLabels
        }

    //	[C5] named objects: local destinations, javascript, embedded files

    /**
     * Implements a link to other part of the document. The jump will
     * be made to a local destination with the same name, that must exist.
     * @param name the name for this link
     * *
     * @param llx the lower left x corner of the activation area
     * *
     * @param lly the lower left y corner of the activation area
     * *
     * @param urx the upper right x corner of the activation area
     * *
     * @param ury the upper right y corner of the activation area
     */
    internal fun localGoto(name: String, llx: Float, lly: Float, urx: Float, ury: Float) {
        val action = getLocalGotoAction(name)
        annotationsImp.addPlainAnnotation(writer!!.createAnnotation(llx, lly, urx, ury, action, null))
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * *
     * @param name the name to jump to
     * *
     * @param llx the lower left x corner of the activation area
     * *
     * @param lly the lower left y corner of the activation area
     * *
     * @param urx the upper right x corner of the activation area
     * *
     * @param ury the upper right y corner of the activation area
     */
    internal fun remoteGoto(filename: String, name: String, llx: Float, lly: Float, urx: Float, ury: Float) {
        annotationsImp.addPlainAnnotation(writer!!.createAnnotation(llx, lly, urx, ury, PdfAction(filename, name), null))
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * *
     * @param page the page to jump to
     * *
     * @param llx the lower left x corner of the activation area
     * *
     * @param lly the lower left y corner of the activation area
     * *
     * @param urx the upper right x corner of the activation area
     * *
     * @param ury the upper right y corner of the activation area
     */
    internal fun remoteGoto(filename: String, page: Int, llx: Float, lly: Float, urx: Float, ury: Float) {
        addAnnotation(writer!!.createAnnotation(llx, lly, urx, ury, PdfAction(filename, page), null))
    }

    /** Implements an action in an area.
     * @param action the PdfAction
     * *
     * @param llx the lower left x corner of the activation area
     * *
     * @param lly the lower left y corner of the activation area
     * *
     * @param urx the upper right x corner of the activation area
     * *
     * @param ury the upper right y corner of the activation area
     */
    internal fun setAction(action: PdfAction, llx: Float, lly: Float, urx: Float, ury: Float) {
        addAnnotation(writer!!.createAnnotation(llx, lly, urx, ury, action, null))
    }

    /**
     * Stores the destinations keyed by name. Value is a Destination.
     */
    protected var localDestinations = TreeMap<String, Destination>()

    internal fun getLocalGotoAction(name: String): PdfAction {
        val action: PdfAction
        var dest: Destination? = localDestinations[name]
        if (dest == null)
            dest = Destination()
        if (dest.action == null) {
            if (dest.reference == null) {
                dest.reference = writer!!.pdfIndirectReference
            }
            action = PdfAction(dest.reference)
            dest.action = action
            localDestinations.put(name, dest)
        } else {
            action = dest.action
        }
        return action
    }

    /**
     * The local destination to where a local goto with the same
     * name will jump to.
     * @param name the name of this local destination
     * *
     * @param destination the PdfDestination with the jump coordinates
     * *
     * @return true if the local destination was added,
     * * false if a local destination with the same name
     * * already existed
     */
    internal fun localDestination(name: String, destination: PdfDestination): Boolean {
        var dest: Destination? = localDestinations[name]
        if (dest == null)
            dest = Destination()
        if (dest.destination != null)
            return false
        dest.destination = destination
        localDestinations.put(name, dest)
        if (!destination.hasPage())
            destination.addPage(writer!!.currentPage)
        return true
    }

    /**
     * Stores a list of document level JavaScript actions.
     */
    internal var jsCounter: Int = 0
    internal var documentLevelJS = HashMap<String, PdfObject>()
        protected set

    internal fun addJavaScript(js: PdfAction) {
        if (js.get(PdfName.JS) == null)
            throw RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"))
        try {
            documentLevelJS.put(SIXTEEN_DIGITS.format(jsCounter++.toLong()), writer!!.addToBody(js).indirectReference)
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    internal fun addJavaScript(name: String, js: PdfAction) {
        if (js.get(PdfName.JS) == null)
            throw RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"))
        try {
            documentLevelJS.put(name, writer!!.addToBody(js).indirectReference)
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    internal var documentFileAttachment = HashMap<String, PdfObject>()
        protected set

    @Throws(IOException::class)
    internal fun addFileAttachment(description: String?, fs: PdfFileSpecification) {
        var description = description
        if (description == null) {
            val desc = fs.get(PdfName.DESC) as PdfString?
            if (desc == null) {
                description = ""
            } else {
                description = PdfEncodings.convertToString(desc.bytes, null)
            }
        }
        fs.addDescription(description, true)
        if (description!!.length == 0)
            description = "Unnamed"
        var fn = PdfEncodings.convertToString(PdfString(description, PdfObject.TEXT_UNICODE).bytes, null)
        var k = 0
        while (documentFileAttachment.containsKey(fn)) {
            ++k
            fn = PdfEncodings.convertToString(PdfString(description + " " + k, PdfObject.TEXT_UNICODE).bytes, null)
        }
        documentFileAttachment.put(fn, fs.reference)
    }

    //	[C6] document level actions

    protected var openActionName: String? = null

    internal fun setOpenAction(name: String) {
        openActionName = name
        openActionAction = null
    }

    protected var openActionAction: PdfAction? = null
    internal fun setOpenAction(action: PdfAction) {
        openActionAction = action
        openActionName = null
    }

    protected var additionalActions: PdfDictionary? = null
    internal fun addAdditionalAction(actionType: PdfName, action: PdfAction?) {
        if (additionalActions == null) {
            additionalActions = PdfDictionary()
        }
        if (action == null)
            additionalActions!!.remove(actionType)
        else
            additionalActions!!.put(actionType, action)
        if (additionalActions!!.size() == 0)
            additionalActions = null
    }

    //	[C7] portable collections

    protected var collection: PdfCollection? = null

    /**
     * Sets the collection dictionary.
     * @param collection a dictionary of type PdfCollection
     */
    fun setCollection(collection: PdfCollection) {
        this.collection = collection
    }

    //	[C8] AcroForm

    internal var annotationsImp: PdfAnnotationsImp

    /**
     * Gets the AcroForm object.
     * @return the PdfAcroform object of the PdfDocument
     */
    internal val acroForm: PdfAcroForm
        get() = annotationsImp.acroForm

    internal fun setSigFlags(f: Int) {
        annotationsImp.setSigFlags(f)
    }

    internal fun addCalculationOrder(formField: PdfFormField) {
        annotationsImp.addCalculationOrder(formField)
    }

    internal fun addAnnotation(annot: PdfAnnotation) {
        isPageEmpty = false
        annotationsImp.addAnnotation(annot)
    }

    protected var language: PdfString? = null
    internal fun setLanguage(language: String) {
        this.language = PdfString(language)
    }

    //	[F12] tagged PDF
    //	[U1] page sizes

    /** This is the size of the next page.  */
    protected var nextPageSize: Rectangle? = null

    /** This is the size of the several boxes of the current Page.  */
    protected var thisBoxSize = HashMap<String, PdfRectangle>()

    /** This is the size of the several boxes that will be used in
     * the next page.  */
    protected var boxSize = HashMap<String, PdfRectangle>()

    internal fun setCropBoxSize(crop: Rectangle) {
        setBoxSize("crop", crop)
    }

    internal fun setBoxSize(boxName: String, size: Rectangle?) {
        if (size == null)
            boxSize.remove(boxName)
        else
            boxSize.put(boxName, PdfRectangle(size))
    }

    protected fun setNewPageSizeAndMargins() {
        pageSize = nextPageSize
        if (marginMirroring && pageNumber and 1 == 0) {
            marginRight = nextMarginLeft
            marginLeft = nextMarginRight
        } else {
            marginLeft = nextMarginLeft
            marginRight = nextMarginRight
        }
        if (marginMirroringTopBottom && pageNumber and 1 == 0) {
            marginTop = nextMarginBottom
            marginBottom = nextMarginTop
        } else {
            marginTop = nextMarginTop
            marginBottom = nextMarginBottom
        }
        if (!isTagged(writer)) {
            text = PdfContentByte(writer)
            text!!.reset()
        } else {
            text = graphics
        }
        text!!.beginText()
        // we move to the left/top position of the page
        text!!.moveText(left(), top())
        if (isTagged(writer))
            textEmptySize = text!!.size()
    }

    /**
     * Gives the size of a trim, art, crop or bleed box, or null if not defined.
     * @param boxName crop, trim, art or bleed
     */
    internal fun getBoxSize(boxName: String): Rectangle? {
        val r = thisBoxSize[boxName]
        if (r != null) {
            return r.rectangle
        }
        return null
    }

    //	[U2] empty pages

    /** This checks if the page is empty.  */
    internal var isPageEmpty = true
        get() {
            if (isTagged(writer)) {
                return writer == null || writer!!.getDirectContent().size(false) == 0 && writer!!.getDirectContentUnder().size(false) == 0 && text!!.size(false) - textEmptySize == 0 && (isPageEmpty || writer!!.isPaused)
            } else {
                return writer == null || writer!!.getDirectContent().size() == 0 && writer!!.getDirectContentUnder().size() == 0 && (isPageEmpty || writer!!.isPaused)
            }
        }

    //	[U3] page actions

    /**
     * Sets the display duration for the page (for presentations)
     * @param seconds   the number of seconds to display the page
     */
    internal fun setDuration(seconds: Int) {
        if (seconds > 0)
            writer!!.addPageDictEntry(PdfName.DUR, PdfNumber(seconds))
    }

    /**
     * Sets the transition for the page
     * @param transition   the PdfTransition object
     */
    internal fun setTransition(transition: PdfTransition) {
        writer!!.addPageDictEntry(PdfName.TRANS, transition.transitionDictionary)
    }

    protected var pageAA: PdfDictionary? = null
    internal fun setPageAction(actionType: PdfName, action: PdfAction) {
        if (pageAA == null) {
            pageAA = PdfDictionary()
        }
        pageAA!!.put(actionType, action)
    }

    //	[U8] thumbnail images

    @Throws(PdfException::class, DocumentException::class)
    internal fun setThumbnail(image: Image) {
        writer!!.addPageDictEntry(PdfName.THUMB, writer!!.getImageReference(writer!!.addDirectImageSimple(image)))
    }

    //	[M0] Page resources contain references to fonts, extgstate, images,...

    /** This are the page resources of the current Page.  */
    internal var pageResources:

            PageResources
        protected set

    //	[M3] Images

    /** Holds value of property strictImageSequence.  */
    /** Getter for property strictImageSequence.
     * @return Value of property strictImageSequence.
     */
    /** Setter for property strictImageSequence.
     * @param strictImageSequence New value of property strictImageSequence.
     */
    internal var isStrictImageSequence = false

    /** This is the position where the image ends.  */
    protected var imageEnd = -1f

    /**
     * Method added by Pelikan Stephan
     */
    fun clearTextWrap() {
        var tmpHeight = imageEnd - currentHeight
        if (line != null) {
            tmpHeight += line!!.height()
        }
        if (imageEnd > -1 && tmpHeight > 0) {
            carriageReturn()
            currentHeight += tmpHeight
        }
    }

    fun getStructParentIndex(obj: Any): Int {
        var i: IntArray? = structParentIndices[obj]
        if (i == null) {
            i = intArrayOf(structParentIndices.size, 0)
            structParentIndices.put(obj, i)
        }
        return i[0]
    }

    fun getNextMarkPoint(obj: Any): Int {
        var i: IntArray? = structParentIndices[obj]
        if (i == null) {
            i = intArrayOf(structParentIndices.size, 0)
            structParentIndices.put(obj, i)
        }
        val markPoint = i[1]
        i[1]++
        return markPoint
    }

    fun getStructParentIndexAndNextMarkPoint(obj: Any): IntArray {
        var i: IntArray? = structParentIndices[obj]
        if (i == null) {
            i = intArrayOf(structParentIndices.size, 0)
            structParentIndices.put(obj, i)
        }
        val markPoint = i[1]
        i[1]++
        return intArrayOf(i[0], markPoint)
    }

    /** This is the image that could not be shown on a previous page.  */
    protected var imageWait: Image? = null

    /**
     * Adds an image to the document.
     * @param image the Image to add
     * *
     * @throws PdfException on error
     * *
     * @throws DocumentException on error
     */

    @Throws(PdfException::class, DocumentException::class)
    protected fun add(image: Image) {
        if (image.hasAbsoluteY()) {
            graphics.addImage(image)
            isPageEmpty = false
            return
        }

        // if there isn't enough room for the image on this page, save it for the next page
        if (currentHeight != 0f && indentTop() - currentHeight - image.scaledHeight < indentBottom()) {
            if (!isStrictImageSequence && imageWait == null) {
                imageWait = image
                return
            }
            newPage()
            if (currentHeight != 0f && indentTop() - currentHeight - image.scaledHeight < indentBottom()) {
                imageWait = image
                return
            }
        }
        isPageEmpty = false
        // avoid endless loops
        if (image === imageWait)
            imageWait = null
        val textwrap = image.alignment and Image.TEXTWRAP == Image.TEXTWRAP && image.alignment and Image.MIDDLE != Image.MIDDLE
        val underlying = image.alignment and Image.UNDERLYING == Image.UNDERLYING
        var diff = leading / 2
        if (textwrap) {
            diff += leading
        }
        val lowerleft = indentTop() - currentHeight - image.scaledHeight - diff
        val mt = image.matrix()
        var startPosition = indentLeft() - mt[4]
        if (image.alignment and Image.RIGHT == Image.RIGHT) startPosition = indentRight() - image.scaledWidth - mt[4]
        if (image.alignment and Image.MIDDLE == Image.MIDDLE) startPosition = indentLeft() + (indentRight() - indentLeft() - image.scaledWidth) / 2 - mt[4]
        if (image.hasAbsoluteX()) startPosition = image.absoluteX
        if (textwrap) {
            if (imageEnd < 0 || imageEnd < currentHeight + image.scaledHeight + diff) {
                imageEnd = currentHeight + image.scaledHeight + diff
            }
            if (image.alignment and Image.RIGHT == Image.RIGHT) {
                // indentation suggested by Pelikan Stephan
                indentation.imageIndentRight += image.scaledWidth + image.getIndentationLeft()
            } else {
                // indentation suggested by Pelikan Stephan
                indentation.imageIndentLeft += image.scaledWidth + image.getIndentationRight()
            }
        } else {
            if (image.alignment and Image.RIGHT == Image.RIGHT)
                startPosition -= image.getIndentationRight()
            else if (image.alignment and Image.MIDDLE == Image.MIDDLE)
                startPosition += image.getIndentationLeft() - image.getIndentationRight()
            else
                startPosition += image.getIndentationLeft()
        }
        graphics.addImage(image, mt[0], mt[1], mt[2], mt[3], startPosition, lowerleft - mt[5])
        if (!(textwrap || underlying)) {
            currentHeight += image.scaledHeight + diff
            flushLines()
            text!!.moveText(0f, -(image.scaledHeight + diff))
            newLine()
        }
    }

    //	[M4] Adding a PdfPTable

    /** Adds a PdfPTable to the document.
     * @param ptable the PdfPTable to be added to the document.
     * *
     * @throws DocumentException on error
     */
    @Throws(DocumentException::class)
    internal fun addPTable(ptable: PdfPTable) {
        val ct = ColumnText(if (isTagged(writer)) text else writer!!.getDirectContent())
        ct.runDirection = ptable.runDirection
        // if the table prefers to be on a single page, and it wouldn't
        //fit on the current page, start a new page.
        if (ptable.keepTogether && !fitsPage(ptable, 0f) && currentHeight > 0) {
            newPage()
            if (isTagged(writer)) {
                ct.canvas = text
            }
        }
        if (currentHeight == 0f) {
            ct.isAdjustFirstLine = false
        }
        ct.addElement(ptable)
        val he = ptable.isHeadersInEvent
        ptable.isHeadersInEvent = true
        var loop = 0
        while (true) {
            ct.setSimpleColumn(indentLeft(), indentBottom(), indentRight(), indentTop() - currentHeight)
            val status = ct.go()
            if (status and ColumnText.NO_MORE_TEXT != 0) {
                if (isTagged(writer)) {
                    text!!.setTextMatrix(indentLeft(), ct.yLine)
                } else {
                    text!!.moveText(0f, ct.yLine - indentTop() + currentHeight)
                }
                currentHeight = indentTop() - ct.yLine
                break
            }
            if (indentTop() - currentHeight == ct.yLine)
                ++loop
            else
                loop = 0
            if (loop == 3) {
                throw DocumentException(MessageLocalization.getComposedMessage("infinite.table.loop"))
            }
            currentHeight = indentTop() - ct.yLine
            newPage()
            if (isTagged(writer)) {
                ct.canvas = text
            }
        }
        ptable.isHeadersInEvent = he
    }

    private var floatingElements: ArrayList<Element>? = ArrayList()

    @Throws(DocumentException::class)
    private fun addDiv(div: PdfDiv) {
        if (floatingElements == null) {
            floatingElements = ArrayList<Element>()
        }
        floatingElements!!.add(div)
    }

    @Throws(DocumentException::class)
    private fun flushFloatingElements() {
        if (floatingElements != null && !floatingElements!!.isEmpty()) {
            val cachedFloatingElements = floatingElements
            floatingElements = null
            val fl = FloatLayout(cachedFloatingElements, false)
            var loop = 0
            while (true) {
                val left = indentLeft()
                fl.setSimpleColumn(indentLeft(), indentBottom(), indentRight(), indentTop() - currentHeight)
                try {
                    val status = fl.layout(if (isTagged(writer)) text else writer!!.getDirectContent(), false)
                    if (status and ColumnText.NO_MORE_TEXT != 0) {
                        if (isTagged(writer)) {
                            text!!.setTextMatrix(indentLeft(), fl.yLine)
                        } else {
                            text!!.moveText(0f, fl.yLine - indentTop() + currentHeight)
                        }
                        currentHeight = indentTop() - fl.yLine
                        break
                    }
                } catch (exc: Exception) {
                    return
                }

                if (indentTop() - currentHeight == fl.yLine || isPageEmpty)
                    ++loop
                else {
                    loop = 0
                }
                if (loop == 2) {
                    return
                }
                newPage()
            }
        }
    }

    /**
     * Checks if a PdfPTable fits the current page of the PdfDocument.

     * @param    table    the table that has to be checked
     * *
     * @param    margin    a certain margin
     * *
     * @return    true if the PdfPTable fits the page, false otherwise.
     */

    internal fun fitsPage(table: PdfPTable, margin: Float): Boolean {
        if (!table.isLockedWidth) {
            val totalWidth = (indentRight() - indentLeft()) * table.widthPercentage / 100
            table.totalWidth = totalWidth
        }
        // ensuring that a new line has been started.
        ensureNewLine()
        val spaceNeeded = if (table.isSkipFirstHeader) table.totalHeight - table.headerHeight else table.totalHeight
        return spaceNeeded + (if (currentHeight > 0) table.spacingBefore() else 0f) <= indentTop() - currentHeight - indentBottom() - margin
    }

    private val lastLine: PdfLine?
        get() {
            if (lines!!.size > 0)
                return lines!![lines!!.size - 1]
            else
                return null
        }

    /**
     * @since 5.0.1
     */
    inner class Destination {
        var action: PdfAction? = null
        var reference: PdfIndirectReference? = null
        var destination: PdfDestination? = null
    }

    protected fun useExternalCache(externalCache: TempFileCache) {
        isToUseExternalCache = true
        this.externalCache = externalCache
    }

    protected fun saveStructElement(id: AccessibleElementId, element: PdfStructureElement) {
        structElements.put(id, element)
    }

    @JvmOverloads protected fun getStructElement(id: AccessibleElementId, toSaveFetchedElement: Boolean = true): PdfStructureElement {
        var element: PdfStructureElement? = structElements[id]
        if (isToUseExternalCache && element == null) {
            val pos = externallyStoredStructElements[id]
            if (pos != null) {
                try {
                    element = externalCache!![pos] as PdfStructureElement
                    element.setStructureTreeRoot(writer!!.getStructureTreeRoot())
                    element.setStructureElementParent(getStructElement(elementsParents[element.elementId], toSaveFetchedElement))

                    if (toSaveFetchedElement) {
                        externallyStoredStructElements.remove(id)
                        structElements.put(id, element)
                    }
                } catch (e: IOException) {
                    throw ExceptionConverter(e)
                } catch (e: ClassNotFoundException) {
                    throw ExceptionConverter(e)
                }

            }
        }
        return element
    }

    protected fun flushStructureElementsOnNewPage() {
        if (!isToUseExternalCache)
            return

        val iterator = structElements.entries.iterator()
        var entry: Entry<AccessibleElementId, PdfStructureElement>
        while (iterator.hasNext()) {
            entry = iterator.next()
            if (entry.value.structureType == PdfName.DOCUMENT)
                continue

            try {
                val el = entry.value
                val parentDict = el.getParent()
                var parent: PdfStructureElement? = null
                if (parentDict is PdfStructureElement) {
                    parent = parentDict as PdfStructureElement
                }
                if (parent != null) {
                    elementsParents.put(entry.key, parent.elementId)
                }

                val pos = externalCache!!.put(el)
                externallyStoredStructElements.put(entry.key, pos)
                iterator.remove()
            } catch (e: IOException) {
                throw ExceptionConverter(e)
            }

        }
    }

    fun getStructElements(): Set<AccessibleElementId> {
        val elements = HashSet<AccessibleElementId>()
        elements.addAll(externallyStoredStructElements.keys)
        elements.addAll(structElements.keys)
        return elements
    }

    companion object {

        /** The characters to be applied the hanging punctuation.  */
        internal val hangingPunctuation = ".,;:'"
        protected val SIXTEEN_DIGITS = DecimalFormat("0000000000000000")

        private fun isTagged(writer: PdfWriter?): Boolean {
            return writer != null && writer.isTagged
        }
    }

}// CONSTRUCTING A PdfDocument/PdfWriter INSTANCE
/**
 * Constructs a new PDF document.
 */
/**
 * Calls addSpacing(float, float, Font, boolean (false)).
 */
