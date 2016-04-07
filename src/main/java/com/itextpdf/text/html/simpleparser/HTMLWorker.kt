/*
 * $Id: cfa0a5d0b749ed16fa38f787c5de845954a4bbe9 $
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
package com.itextpdf.text.html.simpleparser

import java.io.IOException
import java.io.Reader
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.Stack

import com.itextpdf.text.Chunk
import com.itextpdf.text.DocListener
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.FontProvider
import com.itextpdf.text.Image
import com.itextpdf.text.ListItem
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.TextElementArray
import com.itextpdf.text.html.HtmlTags
import com.itextpdf.text.html.HtmlUtilities
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser

/**
 * Old iText class that allows you to convert HTML to PDF.
 * We've completely rewritten HTML to PDF conversion and we made it a separate project named XML Worker.
 */
@Deprecated("")
@Deprecated("since 5.5.2; please switch to XML Worker instead (this is a separate project)")
class HTMLWorker
/**
 * Creates a new instance of HTMLWorker
 * @param document    A class that implements DocListener
 * *
 * @param tags        A map containing the supported tags
 * *
 * @param style        A StyleSheet
 * *
 * @since 5.0.6
 */
@JvmOverloads constructor(
        /**
         * DocListener that will listen to the Elements
         * produced by parsing the HTML.
         * This can be a com.lowagie.text.Document adding
         * the elements to a Document directly, or an
         * HTMLWorker instance strong the objects in a List
         */
        protected var document: DocListener, tags: Map<String, HTMLTagProcessor>? = null, style: StyleSheet? = null) : SimpleXMLDocHandler, DocListener {

    /**
     * The map with all the supported tags.
     * @since 5.0.6
     */
    protected var tags: Map<String, HTMLTagProcessor>

    /** The object defining all the styles.  */
    private var style = StyleSheet()

    init {
        setSupportedTags(tags)
        setStyleSheet(style)
    }

    /**
     * Sets the map with supported tags.
     * @param tags
     * *
     * @since 5.0.6
     */
    fun setSupportedTags(tags: Map<String, HTMLTagProcessor>?) {
        var tags = tags
        if (tags == null)
            tags = HTMLTagProcessors()
        this.tags = tags
    }

    /**
     * Setter for the StyleSheet
     * @param style the StyleSheet
     */
    fun setStyleSheet(style: StyleSheet?) {
        var style = style
        if (style == null)
            style = StyleSheet()
        this.style = style
    }

    /**
     * Parses content read from a java.io.Reader object.
     * @param reader    the content
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun parse(reader: Reader) {
        LOGGER.info("Please note, there is a more extended version of the HTMLWorker available in the iText XMLWorker")
        SimpleXMLParser.parse(this, null, reader, true)
    }

    // state machine

    /**
     * Stack with the Elements that already have been processed.
     * @since iText 5.0.6 (private => protected)
     */
    protected var stack = Stack<Element>()

    /**
     * Keeps the content of the current paragraph
     * @since iText 5.0.6 (private => protected)
     */
    protected var currentParagraph: Paragraph? = null

    /**
     * The current hierarchy chain of tags.
     * @since 5.0.6
     */
    private val chain = ChainedProperties()

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.startDocument
     */
    override fun startDocument() {
        val attrs = HashMap<String, String>()
        style.applyStyle(HtmlTags.BODY, attrs)
        chain.addToChain(HtmlTags.BODY, attrs)
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.startElement
     */
    override fun startElement(tag: String, attrs: MutableMap<String, String>) {
        val htmlTag = tags[tag] ?: return
        // apply the styles to attrs
        style.applyStyle(tag, attrs)
        // deal with the style attribute
        StyleSheet.resolveStyleAttribute(attrs, chain)
        // process the tag
        try {
            htmlTag.startElement(this, tag, attrs)
        } catch (e: DocumentException) {
            throw ExceptionConverter(e)
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.text
     */
    override fun text(content: String) {
        var content = content
        if (isSkipText)
            return
        if (currentParagraph == null) {
            currentParagraph = createParagraph()
        }
        if (!isInsidePRE) {
            // newlines and carriage returns are ignored
            if (content.trim { it <= ' ' }.length == 0 && content.indexOf(' ') < 0) {
                return
            }
            content = HtmlUtilities.eliminateWhiteSpace(content)
        }
        val chunk = createChunk(content)
        currentParagraph!!.add(chunk)
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.endElement
     */
    override fun endElement(tag: String) {
        val htmlTag = tags[tag] ?: return
        // process the tag
        try {
            htmlTag.endElement(this, tag)
        } catch (e: DocumentException) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.endDocument
     */
    override fun endDocument() {
        try {
            // flush the stack
            for (k in stack.indices)
                document.add(stack.elementAt(k))
            // add current paragraph
            if (currentParagraph != null)
                document.add(currentParagraph)
            currentParagraph = null
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    // stack and current paragraph operations

    /**
     * Adds a new line to the currentParagraph.
     * @since 5.0.6
     */
    fun newLine() {
        if (currentParagraph == null) {
            currentParagraph = Paragraph()
        }
        currentParagraph!!.add(createChunk("\n"))
    }

    /**
     * Flushes the current paragraph, indicating that we're starting
     * a new block.
     * If the stack is empty, the paragraph is added to the document.
     * Otherwise the Paragraph is added to the stack.
     * @since 5.0.6
     */
    @Throws(DocumentException::class)
    fun carriageReturn() {
        if (currentParagraph == null)
            return
        if (stack.empty())
            document.add(currentParagraph)
        else {
            val obj = stack.pop()
            if (obj is TextElementArray) {
                obj.add(currentParagraph)
            }
            stack.push(obj)
        }
        currentParagraph = null
    }

    /**
     * Stacks the current paragraph, indicating that we're starting
     * a new span.
     * @since 5.0.6
     */
    fun flushContent() {
        pushToStack(currentParagraph)
        currentParagraph = Paragraph()
    }

    /**
     * Pushes an element to the Stack.
     * @param element
     * *
     * @since 5.0.6
     */
    fun pushToStack(element: Element?) {
        if (element != null)
            stack.push(element)
    }

    /**
     * Updates the chain with a new tag and new attributes.
     * @param tag    the new tag
     * *
     * @param attrs    the corresponding attributes
     * *
     * @since 5.0.6
     */
    fun updateChain(tag: String, attrs: MutableMap<String, String>) {
        chain.addToChain(tag, attrs)
    }

    /**
     * Updates the chain by removing a tag.
     * @param tag    the new tag
     * *
     * @since 5.0.6
     */
    fun updateChain(tag: String) {
        chain.removeChain(tag)
    }

    /**
     * Map containing providers such as a FontProvider or ImageProvider.
     * @since 5.0.6 (renamed from interfaceProps)
     */
    /**
     * Gets the providers
     */
    var interfaceProps: Map<String, Any> = HashMap()
        private set

    /**
     * Setter for the providers.
     * If a FontProvider is added, the ElementFactory is updated.
     * @param providers a Map with different providers
     * *
     * @since 5.0.6
     */
    fun setProviders(providers: Map<String, Any>?) {
        if (providers == null)
            return
        this.interfaceProps = providers
        var ff: FontProvider? = null
        if (providers != null)
            ff = providers[FONT_PROVIDER] as FontProvider
        if (ff != null)
            factory.fontProvider = ff
    }

    // factory that helps create objects

    /**
     * Factory that is able to create iText Element objects.
     * @since 5.0.6
     */
    private val factory = ElementFactory()

    /**
     * Creates a Chunk using the factory.
     * @param content    the content of the chunk
     * *
     * @return    a Chunk with content
     * *
     * @since 5.0.6
     */
    fun createChunk(content: String): Chunk {
        return factory.createChunk(content, chain)
    }

    /**
     * Creates a Paragraph using the factory.
     * @return    a Paragraph without any content
     * *
     * @since 5.0.6
     */
    fun createParagraph(): Paragraph {
        return factory.createParagraph(chain)
    }

    /**
     * Creates a List object.
     * @param tag should be "ol" or "ul"
     * *
     * @return    a List object
     * *
     * @since 5.0.6
     */
    fun createList(tag: String): com.itextpdf.text.List {
        return factory.createList(tag, chain)
    }

    /**
     * Creates a ListItem object.
     * @return a ListItem object
     * *
     * @since 5.0.6
     */
    fun createListItem(): ListItem {
        return factory.createListItem(chain)
    }

    /**
     * Creates a LineSeparator object.
     * @param attrs    properties of the LineSeparator
     * *
     * @return a LineSeparator object
     * *
     * @since 5.0.6
     */
    fun createLineSeparator(attrs: Map<String, String>): LineSeparator {
        return factory.createLineSeparator(attrs, currentParagraph!!.leading / 2)
    }

    /**
     * Creates an Image object.
     * @param attrs properties of the Image
     * *
     * @return an Image object (or null if the Image couldn't be found)
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     * *
     * @since 5.0.6
     */
    @Throws(DocumentException::class, IOException::class)
    fun createImage(attrs: Map<String, String>): Image? {
        val src = attrs[HtmlTags.SRC] ?: return null
        val img = factory.createImage(
                src, attrs, chain, document,
                interfaceProps[IMG_PROVIDER] as ImageProvider,
                interfaceProps[IMG_STORE] as ImageStore,
                interfaceProps[IMG_BASEURL] as String)
        return img
    }

    /**
     * Creates a Cell.
     * @param tag    the tag
     * *
     * @return    a CellWrapper object
     * *
     * @since 5.0.6
     */
    fun createCell(tag: String): CellWrapper {
        return CellWrapper(tag, chain)
    }

    // processing objects

    /**
     * Adds a link to the current paragraph.
     * @since 5.0.6
     */
    fun processLink() {
        if (currentParagraph == null) {
            currentParagraph = Paragraph()
        }
        // The link provider allows you to do additional processing
        val i = interfaceProps[HTMLWorker.LINK_PROVIDER] as LinkProcessor
        if (i == null || !i.process(currentParagraph, chain)) {
            // sets an Anchor for all the Chunks in the current paragraph
            val href = chain.getProperty(HtmlTags.HREF)
            if (href != null) {
                for (ck in currentParagraph!!.chunks) {
                    ck.setAnchor(href)
                }
            }
        }
        // a link should be added to the current paragraph as a phrase
        if (stack.isEmpty()) {
            // no paragraph to add too, 'a' tag is first element
            val tmp = Paragraph(Phrase(currentParagraph))
            currentParagraph = tmp
        } else {
            val tmp = stack.pop() as Paragraph
            tmp.add(Phrase(currentParagraph))
            currentParagraph = tmp
        }
    }

    /**
     * Fetches the List from the Stack and adds it to
     * the TextElementArray on top of the Stack,
     * or to the Document if the Stack is empty.
     * @throws DocumentException
     * *
     * @since 5.0.6
     */
    @Throws(DocumentException::class)
    fun processList() {
        if (stack.empty())
            return
        val obj = stack.pop()
        if (obj !is com.itextpdf.text.List) {
            stack.push(obj)
            return
        }
        if (stack.empty())
            document.add(obj)
        else
            (stack.peek() as TextElementArray).add(obj)
    }

    /**
     * Looks for the List object on the Stack,
     * and adds the ListItem to the List.
     * @throws DocumentException
     * *
     * @since 5.0.6
     */
    @Throws(DocumentException::class)
    fun processListItem() {
        if (stack.empty())
            return
        val obj = stack.pop()
        if (obj !is ListItem) {
            stack.push(obj)
            return
        }
        if (stack.empty()) {
            document.add(obj)
            return
        }
        val list = stack.pop()
        if (list !is com.itextpdf.text.List) {
            stack.push(list)
            return
        }
        list.add(obj)
        obj.adjustListSymbolFont()
        stack.push(list)
    }

    /**
     * Processes an Image.
     * @param img
     * *
     * @param attrs
     * *
     * @throws DocumentException
     * *
     * @since    5.0.6
     */
    @Throws(DocumentException::class)
    fun processImage(img: Image, attrs: Map<String, String>) {
        val processor = interfaceProps[HTMLWorker.IMG_PROCESSOR] as ImageProcessor
        if (processor == null || !processor.process(img, attrs, chain, document)) {
            val align = attrs[HtmlTags.ALIGN]
            if (align != null) {
                carriageReturn()
            }
            if (currentParagraph == null) {
                currentParagraph = createParagraph()
            }
            currentParagraph!!.add(Chunk(img, 0f, 0f, true))
            currentParagraph!!.alignment = HtmlUtilities.alignmentValue(align)
            if (align != null) {
                carriageReturn()
            }
        }
    }

    /**
     * Processes the Table.
     * @throws DocumentException
     * *
     * @since 5.0.6
     */
    @Throws(DocumentException::class)
    fun processTable() {
        val table = stack.pop() as TableWrapper
        val tb = table.createTable()
        tb.isSplitRows = true
        if (stack.empty())
            document.add(tb)
        else
            (stack.peek() as TextElementArray).add(tb)
    }

    /**
     * Gets the TableWrapper from the Stack and adds a new row.
     * @since 5.0.6
     */
    fun processRow() {
        val row = ArrayList<PdfPCell>()
        val cellWidths = ArrayList<Float>()
        var percentage = false
        var width: Float
        var totalWidth = 0f
        var zeroWidth = 0
        var table: TableWrapper? = null
        while (true) {
            val obj = stack.pop()
            if (obj is CellWrapper) {
                width = obj.width
                cellWidths.add(width)
                percentage = percentage or obj.isPercentage
                if (width == 0f) {
                    zeroWidth++
                } else {
                    totalWidth += width
                }
                row.add(obj.cell)
            }
            if (obj is TableWrapper) {
                table = obj
                break
            }
        }
        table!!.addRow(row)
        if (cellWidths.size > 0) {
            // cells come off the stack in reverse, naturally
            totalWidth = 100 - totalWidth
            Collections.reverse(cellWidths)
            val widths = FloatArray(cellWidths.size)
            var hasZero = false
            for (i in widths.indices) {
                widths[i] = cellWidths[i].toFloat()
                if (widths[i] == 0f && percentage && zeroWidth > 0) {
                    widths[i] = totalWidth / zeroWidth
                }
                if (widths[i] == 0f) {
                    hasZero = true
                    break
                }
            }
            if (!hasZero)
                table.setColWidths(widths)
        }
        stack.push(table)
    }

    // state variables and methods

    /** Stack to keep track of table tags.  */
    private val tableState = Stack<BooleanArray>()

    /** Boolean to keep track of TR tags.  */
    /**
     * @return the pendingTR
     * *
     * @since 5.0.6
     */
    /**
     * @param pendingTR the pendingTR to set
     * *
     * @since 5.0.6
     */
    var isPendingTR = false

    /** Boolean to keep track of TD and TH tags  */
    /**
     * @return the pendingTD
     * *
     * @since 5.0.6
     */
    /**
     * @param pendingTD the pendingTD to set
     * *
     * @since 5.0.6
     */
    var isPendingTD = false

    /** Boolean to keep track of LI tags  */
    /**
     * @return the pendingLI
     * *
     * @since 5.0.6
     */
    /**
     * @param pendingLI the pendingLI to set
     * *
     * @since 5.0.6
     */
    var isPendingLI = false

    /**
     * Boolean to keep track of PRE tags
     * @since 5.0.6 renamed from isPRE
     */
    /**
     * @return the insidePRE
     * *
     * @since 5.0.6
     */
    /**
     * @param insidePRE the insidePRE to set
     * *
     * @since 5.0.6
     */
    var isInsidePRE = false

    /**
     * Indicates if text needs to be skipped.
     * @since iText 5.0.6 (private => protected)
     */
    /**
     * @return the skipText
     * *
     * @since 5.0.6
     */
    /**
     * @param skipText the skipText to set
     * *
     * @since 5.0.6
     */
    var isSkipText = false

    /**
     * Pushes the values of pendingTR and pendingTD
     * to a state stack.
     * @since 5.0.6
     */
    fun pushTableState() {
        tableState.push(booleanArrayOf(isPendingTR, isPendingTD))
    }

    /**
     * Pops the values of pendingTR and pendingTD
     * from a state stack.
     * @since 5.0.6
     */
    fun popTableState() {
        val state = tableState.pop()
        isPendingTR = state[0]
        isPendingTD = state[1]
    }

    // static methods to parse HTML to a List of Element objects.

    /** The resulting list of elements.  */
    protected var objectList: MutableList<Element>

    // DocListener interface

    /**
     * @see com.itextpdf.text.ElementListener.add
     */
    @Throws(DocumentException::class)
    override fun add(element: Element): Boolean {
        objectList.add(element)
        return true
    }

    /**
     * @see com.itextpdf.text.DocListener.close
     */
    override fun close() {
    }

    /**
     * @see com.itextpdf.text.DocListener.newPage
     */
    override fun newPage(): Boolean {
        return true
    }

    /**
     * @see com.itextpdf.text.DocListener.open
     */
    override fun open() {
    }

    /**
     * @see com.itextpdf.text.DocListener.resetPageCount
     */
    override fun resetPageCount() {
    }

    /**
     * @see com.itextpdf.text.DocListener.setMarginMirroring
     */
    override fun setMarginMirroring(marginMirroring: Boolean): Boolean {
        return false
    }

    /**
     * @see com.itextpdf.text.DocListener.setMarginMirroring
     * @since    2.1.6
     */
    override fun setMarginMirroringTopBottom(marginMirroring: Boolean): Boolean {
        return false
    }

    /**
     * @see com.itextpdf.text.DocListener.setMargins
     */
    override fun setMargins(marginLeft: Float, marginRight: Float,
                            marginTop: Float, marginBottom: Float): Boolean {
        return true
    }

    /**
     * @see com.itextpdf.text.DocListener.setPageCount
     */
    override fun setPageCount(pageN: Int) {
    }

    /**
     * @see com.itextpdf.text.DocListener.setPageSize
     */
    override fun setPageSize(pageSize: Rectangle): Boolean {
        return true
    }

    // deprecated methods

    /**
     * Sets the providers.
     */
    @Deprecated("")
    @Deprecated("use setProviders() instead")
    fun setInterfaceProps(providers: HashMap<String, Any>) {
        setProviders(providers)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(HTMLWorker::class.java)

        // providers that help find resources such as images and fonts

        /**
         * Key used to store the image provider in the providers map.
         * @since 5.0.6
         */
        val IMG_PROVIDER = "img_provider"

        /**
         * Key used to store the image processor in the providers map.
         * @since 5.0.6
         */
        val IMG_PROCESSOR = "img_interface"

        /**
         * Key used to store the image store in the providers map.
         * @since 5.0.6
         */
        val IMG_STORE = "img_static"

        /**
         * Key used to store the image baseurl provider in the providers map.
         * @since 5.0.6
         */
        val IMG_BASEURL = "img_baseurl"

        /**
         * Key used to store the font provider in the providers map.
         * @since 5.0.6
         */
        val FONT_PROVIDER = "font_factory"

        /**
         * Key used to store the link provider in the providers map.
         * @since 5.0.6
         */
        val LINK_PROVIDER = "alink_interface"

        /**
         * Parses an HTML source to a List of Element objects
         * @param reader    the HTML source
         * *
         * @param style        a StyleSheet object
         * *
         * @param providers    map containing classes with extra info
         * *
         * @return a List of Element objects
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        @JvmOverloads fun parseToList(reader: Reader, style: StyleSheet,
                                      providers: HashMap<String, Any>? = null): List<Element> {
            return parseToList(reader, style, null, providers)
        }

        /**
         * Parses an HTML source to a List of Element objects
         * @param reader    the HTML source
         * *
         * @param style        a StyleSheet object
         * *
         * @param tags        a map containing supported tags and their processors
         * *
         * @param providers    map containing classes with extra info
         * *
         * @return a List of Element objects
         * *
         * @throws IOException
         * *
         * @since 5.0.6
         */
        @Throws(IOException::class)
        fun parseToList(reader: Reader, style: StyleSheet,
                        tags: Map<String, HTMLTagProcessor>?, providers: HashMap<String, Any>): List<Element> {
            val worker = HTMLWorker(null, tags, style)
            worker.document = worker
            worker.setProviders(providers)
            worker.objectList = ArrayList<Element>()
            worker.parse(reader)
            return worker.objectList
        }
    }

}
/**
 * Creates a new instance of HTMLWorker
 * @param document A class that implements DocListener
 */
/**
 * Parses an HTML source to a List of Element objects
 * @param reader    the HTML source
 * *
 * @param style        a StyleSheet object
 * *
 * @return a List of Element objects
 * *
 * @throws IOException
 */
