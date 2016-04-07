/*
 * $Id: 01af9aa4df3bf23b12128212ddf99398fbee5594 $
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

import com.itextpdf.text.pdf.OutputStreamCounter

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Properties

/**
 * An abstract Writer class for documents.
 *
 * DocWriter is the abstract class of several writers such
 * as PdfWriter and HtmlWriter.
 * A DocWriter can be added as a DocListener
 * to a certain Document by getting an instance (see method
 * getInstance() in the specific writer-classes).
 * Every Element added to the original Document
 * will be written to the OutputStream of the listening
 * DocWriter.

 * @see Document

 * @see DocListener
 */

abstract class DocWriter : DocListener {

    // membervariables

    /** The pageSize.  */
    protected var pageSize: Rectangle

    /** This is the document that has to be written.  */
    protected var document: Document

    /** The outputstream of this writer.  */
    protected var os: OutputStreamCounter

    /** Is the writer open for writing?  */
    protected var open = false

    /** Do we have to pause all writing actions?  */
    /**
     * Checks if writing is paused.

     * @return        true if writing temporarily has to be paused, false otherwise.
     */

    var isPaused = false
        protected set

    /** Closes the stream on document close  */
    /** Checks if the stream is to be closed on document close
     * @return true if the stream is closed on document close
     */
    /** Sets the close state of the stream after document close
     * @param closeStream true if the stream is closed on document close
     */
    var isCloseStream = true

    // constructor

    protected constructor() {
    }

    /**
     * Constructs a DocWriter.

     * @param document  The Document that has to be written
     * *
     * @param os  The OutputStream the writer has to write to.
     */

    protected constructor(document: Document, os: OutputStream) {
        this.document = document
        this.os = OutputStreamCounter(BufferedOutputStream(os))
    }

    // implementation of the DocListener methods

    /**
     * Signals that an Element was added to the Document.
     *
     * This method should be overridden in the specific DocWriter classes
     * derived from this abstract class.

     * @param element A high level object to add
     * *
     * @return  false
     * *
     * @throws  DocumentException when a document isn't open yet, or has been closed
     */

    @Throws(DocumentException::class)
    override fun add(element: Element): Boolean {
        return false
    }

    /**
     * Signals that the Document was opened.
     */

    override fun open() {
        open = true
    }

    /**
     * Sets the pagesize.

     * @param pageSize  the new pagesize
     * *
     * @return  a boolean
     */

    override fun setPageSize(pageSize: Rectangle): Boolean {
        this.pageSize = pageSize
        return true
    }

    /**
     * Sets the margins.
     *
     * This does nothing. Has to be overridden if needed.

     * @param marginLeft    the margin on the left
     * *
     * @param marginRight   the margin on the right
     * *
     * @param marginTop   the margin on the top
     * *
     * @param marginBottom  the margin on the bottom
     * *
     * @return  false
     */

    override fun setMargins(marginLeft: Float, marginRight: Float, marginTop: Float, marginBottom: Float): Boolean {
        return false
    }

    /**
     * Signals that an new page has to be started.
     *
     * This does nothing. Has to be overridden if needed.

     * @return  true if the page was added, false if not.
     */

    override fun newPage(): Boolean {
        if (!open) {
            return false
        }
        return true
    }

    /**
     * Sets the page number to 0.
     *
     * This method should be overridden in the specific DocWriter classes
     * derived from this abstract class if they actually support the use of
     * pagenumbers.
     */

    override fun resetPageCount() {
    }

    /**
     * Sets the page number.
     *
     * This method should be overridden in the specific DocWriter classes
     * derived from this abstract class if they actually support the use of
     * pagenumbers.

     * @param pageN   the new page number
     */

    override fun setPageCount(pageN: Int) {
    }

    /**
     * Signals that the Document was closed and that no other
     * Elements will be added.
     */

    override fun close() {
        open = false
        try {
            os.flush()
            if (isCloseStream)
                os.close()
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

    }

    /**
     * Let the writer know that all writing has to be paused.
     */

    fun pause() {
        isPaused = true
    }

    /**
     * Let the writer know that writing may be resumed.
     */

    fun resume() {
        isPaused = false
    }

    /**
     * Flushes the BufferedOutputStream.
     */

    fun flush() {
        try {
            os.flush()
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

    }

    /**
     * Writes a String to the OutputStream.

     * @param string    the String to write
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun write(string: String) {
        os.write(getISOBytes(string))
    }

    /**
     * Writes a number of tabs.

     * @param   indent  the number of tabs to add
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun addTabs(indent: Int) {
        os.write(NEWLINE.toInt())
        for (i in 0..indent - 1) {
            os.write(TAB.toInt())
        }
    }

    /**
     * Writes a key-value pair to the outputstream.

     * @param   key     the name of an attribute
     * *
     * @param   value   the value of an attribute
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun write(key: String, value: String) {
        os.write(SPACE.toInt())
        write(key)
        os.write(EQUALS.toInt())
        os.write(QUOTE.toInt())
        write(value)
        os.write(QUOTE.toInt())
    }

    /**
     * Writes a starttag to the outputstream.

     * @param   tag     the name of the tag
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun writeStart(tag: String) {
        os.write(LT.toInt())
        write(tag)
    }

    /**
     * Writes an endtag to the outputstream.

     * @param   tag     the name of the tag
     * *
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun writeEnd(tag: String) {
        os.write(LT.toInt())
        os.write(FORWARD.toInt())
        write(tag)
        os.write(GT.toInt())
    }

    /**
     * Writes an endtag to the outputstream.
     * @throws IOException
     */

    @Throws(IOException::class)
    protected fun writeEnd() {
        os.write(SPACE.toInt())
        os.write(FORWARD.toInt())
        os.write(GT.toInt())
    }

    /**
     * Writes the markup attributes of the specified MarkupAttributes
     * object to the OutputStream.
     * @param markup   a Properties collection to write.
     * *
     * @return true, if writing the markup attributes succeeded
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun writeMarkupAttributes(markup: Properties?): Boolean {
        if (markup == null) return false
        val attributeIterator = markup.keys.iterator()
        var name: String
        while (attributeIterator.hasNext()) {
            name = attributeIterator.next().toString()
            write(name, markup.getProperty(name))
        }
        markup.clear()
        return true
    }

    /**
     * This implementation only returns false and doesn't do anything. Perhaps you were looking for the similarly named method of the Document class.

     * @see com.itextpdf.text.DocListener.setMarginMirroring
     */
    override fun setMarginMirroring(MarginMirroring: Boolean): Boolean {
        return false
    }

    /**
     * This implementation only returns false and doesn't do anything. Perhaps you were looking for the similarly named method of the Document class.

     * @see com.itextpdf.text.DocListener.setMarginMirroring
     * @since    2.1.6
     */
    override fun setMarginMirroringTopBottom(MarginMirroring: Boolean): Boolean {
        return false
    }

    companion object {

        /** This is some byte that is often used.  */
        val NEWLINE = '\n'.toByte()

        /** This is some byte that is often used.  */
        val TAB = '\t'.toByte()

        /** This is some byte that is often used.  */
        val LT = '<'.toByte()

        /** This is some byte that is often used.  */
        val SPACE = ' '.toByte()

        /** This is some byte that is often used.  */
        val EQUALS = '='.toByte()

        /** This is some byte that is often used.  */
        val QUOTE = '\"'.toByte()

        /** This is some byte that is often used.  */
        val GT = '>'.toByte()

        /** This is some byte that is often used.  */
        val FORWARD = '/'.toByte()

        // methods

        /** Converts a String into a Byte array
         * according to the ISO-8859-1 codepage.
         * @param text the text to be converted
         * *
         * @return the conversion result
         */

        fun getISOBytes(text: String?): ByteArray? {
            if (text == null)
                return null
            val len = text.length
            val b = ByteArray(len)
            for (k in 0..len - 1)
                b[k] = text[k].toByte()
            return b
        }
    }

}
