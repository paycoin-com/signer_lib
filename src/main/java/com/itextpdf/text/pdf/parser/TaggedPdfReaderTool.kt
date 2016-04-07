/*
 * $Id: 7162beaa605fcf756f2a53c51578e7e7a2796e7c $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, et al.
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
package com.itextpdf.text.pdf.parser

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.*
import com.itextpdf.text.xml.XMLUtil

import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * Converts a tagged PDF document into an XML file.

 * @since 5.0.2
 */
class TaggedPdfReaderTool {

    /** The reader object from which the content streams are read.  */
    protected var reader: PdfReader
    /** The writer object to which the XML will be written  */
    protected var out: PrintWriter

    /**
     * Parses a string with structured content.

     * @param reader
     * *            the PdfReader that has access to the PDF file
     * *
     * @param os
     * *            the OutputStream to which the resulting xml will be written
     * *
     * @param charset
     * *            the charset to encode the data
     * *
     * @since 5.0.5
     */
    @Throws(IOException::class)
    @JvmOverloads fun convertToXml(reader: PdfReader, os: OutputStream, charset: String = "UTF-8") {
        this.reader = reader
        val outs = OutputStreamWriter(os, charset)
        out = PrintWriter(outs)
        // get the StructTreeRoot from the root object
        val catalog = reader.catalog
        val struct = catalog.getAsDict(PdfName.STRUCTTREEROOT) ?: throw IOException(MessageLocalization.getComposedMessage("no.structtreeroot.found"))
        // Inspect the child or children of the StructTreeRoot
        inspectChild(struct.getDirectObject(PdfName.K))
        out.flush()
        out.close()
    }

    /**
     * Inspects a child of a structured element. This can be an array or a
     * dictionary.

     * @param k
     * *            the child to inspect
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun inspectChild(k: PdfObject?) {
        if (k == null)
            return
        if (k is PdfArray)
            inspectChildArray(k as PdfArray?)
        else if (k is PdfDictionary)
            inspectChildDictionary(k as PdfDictionary?)
    }

    /**
     * If the child of a structured element is an array, we need to loop over
     * the elements.

     * @param k
     * *            the child array to inspect
     */
    @Throws(IOException::class)
    fun inspectChildArray(k: PdfArray?) {
        if (k == null)
            return
        for (i in 0..k.size() - 1) {
            inspectChild(k.getDirectObject(i))
        }
    }


    /**
     * If the child of a structured element is a dictionary, we inspect the
     * child; we may also draw a tag.

     * @param k
     * *            the child dictionary to inspect
     */
    @Throws(IOException::class)
    @JvmOverloads fun inspectChildDictionary(k: PdfDictionary?, inspectAttributes: Boolean = false) {
        if (k == null)
            return
        val s = k.getAsName(PdfName.S)
        if (s != null) {
            val tagN = PdfName.decodeName(s.toString())
            val tag = fixTagName(tagN)
            out.print("<")
            out.print(tag)
            if (inspectAttributes) {
                val a = k.getAsDict(PdfName.A)
                if (a != null) {
                    val keys = a.keys
                    for (key in keys) {
                        out.print(' ')
                        var value = a.get(key)
                        value = PdfReader.getPdfObject(value)
                        out.print(xmlName(key))
                        out.print("=\"")
                        out.print(value.toString())
                        out.print("\"")
                    }
                }
            }
            out.print(">")
            val alt = k.get(PdfName.ALT)
            if (alt != null && alt.toString() != null) {
                out.print("<alt><![CDATA[")
                out.print(alt.toString().replace("[\\000]*".toRegex(), ""))
                out.print("]]></alt>")
            }
            val dict = k.getAsDict(PdfName.PG)
            if (dict != null)
                parseTag(tagN, k.getDirectObject(PdfName.K), dict)
            inspectChild(k.getDirectObject(PdfName.K))
            out.print("</")
            out.print(tag)
            out.println(">")
        } else
            inspectChild(k.getDirectObject(PdfName.K))
    }

    protected fun xmlName(name: PdfName): String {
        var xmlName = name.toString().replaceFirst("/".toRegex(), "")
        xmlName = Character.toLowerCase(xmlName[0]) + xmlName.substring(1)
        return xmlName
    }

    private fun fixTagName(tag: String): String {
        val sb = StringBuilder()
        for (k in 0..tag.length - 1) {
            var c = tag[k]
            val nameStart = c == ':'
                    || c >= 'A' && c <= 'Z'
                    || c == '_'
                    || c >= 'a' && c <= 'z'
                    || c >= '\u00c0' && c <= '\u00d6'
                    || c >= '\u00d8' && c <= '\u00f6'
                    || c >= '\u00f8' && c <= '\u02ff'
                    || c >= '\u0370' && c <= '\u037d'
                    || c >= '\u037f' && c <= '\u1fff'
                    || c >= '\u200c' && c <= '\u200d'
                    || c >= '\u2070' && c <= '\u218f'
                    || c >= '\u2c00' && c <= '\u2fef'
                    || c >= '\u3001' && c <= '\ud7ff'
                    || c >= '\uf900' && c <= '\ufdcf'
                    || c >= '\ufdf0' && c <= '\ufffd'
            val nameMiddle = c == '-'
                    || c == '.'
                    || c >= '0' && c <= '9'
                    || c == '\u00b7'
                    || c >= '\u0300' && c <= '\u036f'
                    || c >= '\u203f' && c <= '\u2040'
                    || nameStart
            if (k == 0) {
                if (!nameStart)
                    c = '_'
            } else {
                if (!nameMiddle)
                    c = '-'
            }
            sb.append(c)
        }
        return sb.toString()
    }

    /**
     * Searches for a tag in a page.

     * @param tag
     * *            the name of the tag
     * *
     * @param object
     * *            an identifier to find the marked content
     * *
     * @param page
     * *            a page dictionary
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun parseTag(tag: String, `object`: PdfObject, page: PdfDictionary) {
        // if the identifier is a number, we can extract the content right away
        if (`object` is PdfNumber) {
            val filter = MarkedContentRenderFilter(`object`.intValue())
            val strategy = SimpleTextExtractionStrategy()
            val listener = FilteredTextRenderListener(
                    strategy, filter)
            val processor = PdfContentStreamProcessor(
                    listener)
            processor.processContent(PdfReader.getPageContent(page), page.getAsDict(PdfName.RESOURCES))
            out.print(XMLUtil.escapeXML(listener.resultantText, true))
        } else if (`object` is PdfArray) {
            val n = `object`.size()
            for (i in 0..n - 1) {
                parseTag(tag, `object`.getPdfObject(i), page)
                if (i < n - 1)
                    out.println()
            }
        } else if (`object` is PdfDictionary) {
            parseTag(tag, `object`.getDirectObject(PdfName.MCID), `object`.getAsDict(PdfName.PG))
        }// if the identifier is a dictionary, we get the resources from the
        // dictionary
        // if the identifier is an array, we call the parseTag method
        // recursively
    }

}
/**
 * Parses a string with structured content. The output is done using the
 * current charset.

 * @param reader
 * *            the PdfReader that has access to the PDF file
 * *
 * @param os
 * *            the OutputStream to which the resulting xml will be written
 */
/**
 * If the child of a structured element is a dictionary, we inspect the
 * child; we may also draw a tag.

 * @param k
 * *            the child dictionary to inspect
 */
