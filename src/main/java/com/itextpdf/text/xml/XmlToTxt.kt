/*
 * $Id: fc7d3c39734d7e7455d9c1299f6620dd7a7d31e9 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, Balder Van Camp, et al.
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
package com.itextpdf.text.xml

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser

/**
 * This class converts XML into plain text stripping all tags.
 */
class XmlToTxt
/**
 * Creates an instance of XML to TXT.
 */
protected constructor() : SimpleXMLDocHandler {

    /**
     * Buffer that stores all content that is encountered.
     */
    protected var buf: StringBuffer

    init {
        buf = StringBuffer()
    }

    /**
     * @return    the String after parsing.
     */
    override fun toString(): String {
        return buf.toString()
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.startElement
     */
    override fun startElement(tag: String, h: Map<String, String>) {
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.endElement
     */
    override fun endElement(tag: String) {
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.startDocument
     */
    override fun startDocument() {
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.endDocument
     */
    override fun endDocument() {
    }

    /**
     * @see com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler.text
     */
    override fun text(str: String) {
        buf.append(str)
    }

    companion object {

        /**
         * Static method that parses an XML InputStream.
         * @param is    the XML input that needs to be parsed
         * *
         * @return    a String obtained by removing all tags from the XML
         */
        @Throws(IOException::class)
        fun parse(`is`: InputStream): String {
            val handler = XmlToTxt()
            SimpleXMLParser.parse(handler, null, InputStreamReader(`is`), true)
            return handler.toString()
        }
    }
}
