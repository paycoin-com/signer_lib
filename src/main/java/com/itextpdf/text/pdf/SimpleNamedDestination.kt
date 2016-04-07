/*
 * $Id: 738f783107ba273e359f44512ead0fac3fba5c1e $
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

import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.util.HashMap
import java.util.StringTokenizer

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.xml.simpleparser.IanaEncodings
import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser
import com.itextpdf.text.xml.XMLUtil

/**

 * @author Paulo Soares
 */
class SimpleNamedDestination private constructor() : SimpleXMLDocHandler {

    private var xmlNames: HashMap<String, String>? = null
    private var xmlLast: HashMap<String, String>? = null

    override fun endDocument() {
    }

    override fun endElement(tag: String) {
        if (tag == "Destination") {
            if (xmlLast == null && xmlNames != null)
                return
            else
                throw RuntimeException(MessageLocalization.getComposedMessage("destination.end.tag.out.of.place"))
        }
        if (tag != "Name")
            throw RuntimeException(MessageLocalization.getComposedMessage("invalid.end.tag.1", tag))
        if (xmlLast == null || xmlNames == null)
            throw RuntimeException(MessageLocalization.getComposedMessage("name.end.tag.out.of.place"))
        if (!xmlLast!!.containsKey("Page"))
            throw RuntimeException(MessageLocalization.getComposedMessage("page.attribute.missing"))
        xmlNames!!.put(unEscapeBinaryString(xmlLast!!["Name"]), xmlLast!!["Page"])
        xmlLast = null
    }

    override fun startDocument() {
    }

    override fun startElement(tag: String, h: Map<String, String>) {
        if (xmlNames == null) {
            if (tag == "Destination") {
                xmlNames = HashMap<String, String>()
                return
            } else
                throw RuntimeException(MessageLocalization.getComposedMessage("root.element.is.not.destination"))
        }
        if (tag != "Name")
            throw RuntimeException(MessageLocalization.getComposedMessage("tag.1.not.allowed", tag))
        if (xmlLast != null)
            throw RuntimeException(MessageLocalization.getComposedMessage("nested.tags.are.not.allowed"))
        xmlLast = HashMap(h)
        xmlLast!!.put("Name", "")
    }

    override fun text(str: String) {
        if (xmlLast == null)
            return
        var name = xmlLast!!["Name"]
        name += str
        xmlLast!!.put("Name", name)
    }

    companion object {

        fun getNamedDestination(reader: PdfReader, fromNames: Boolean): HashMap<String, String> {
            val pages = IntHashtable()
            val numPages = reader.numberOfPages
            for (k in 1..numPages)
                pages.put(reader.getPageOrigRef(k).number, k)
            val names = if (fromNames) reader.namedDestinationFromNames else reader.namedDestinationFromStrings
            val n2 = HashMap<String, String>(names.size)
            for (entry in names.entries) {
                val arr = entry.value as PdfArray
                val s = StringBuffer()
                try {
                    s.append(pages.get(arr.getAsIndirectObject(0).number))
                    s.append(' ').append(arr.getPdfObject(1).toString().substring(1))
                    for (k in 2..arr.size() - 1)
                        s.append(' ').append(arr.getPdfObject(k).toString())
                    n2.put(entry.key, s.toString())
                } catch (e: Exception) {
                }

            }
            return n2
        }

        /**
         * Exports the destinations to XML. The DTD for this XML is:
         *
         *
         *
         * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
         * &lt;!ELEMENT Name (#PCDATA)&gt;
         * &lt;!ATTLIST Name
         * Page CDATA #IMPLIED
         * &gt;
         * &lt;!ELEMENT Destination (Name)*&gt;
         *
         * @param names the names
         * *
         * @param out the export destination. The stream is not closed
         * *
         * @param encoding the encoding according to IANA conventions
         * *
         * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if true,
         * * whatever the encoding
         * *
         * @throws IOException on error
         * *
         * @since 5.0.1 (generic type in signature)
         */
        @Throws(IOException::class)
        fun exportToXML(names: HashMap<String, String>, out: OutputStream, encoding: String, onlyASCII: Boolean) {
            val jenc = IanaEncodings.getJavaEncoding(encoding)
            val wrt = BufferedWriter(OutputStreamWriter(out, jenc))
            exportToXML(names, wrt, encoding, onlyASCII)
        }

        /**
         * Exports the destinations to XML.
         * @param names the names
         * *
         * @param wrt the export destination. The writer is not closed
         * *
         * @param encoding the encoding according to IANA conventions
         * *
         * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if true,
         * * whatever the encoding
         * *
         * @throws IOException on error
         * *
         * @since 5.0.1 (generic type in signature)
         */
        @Throws(IOException::class)
        fun exportToXML(names: HashMap<String, String>, wrt: Writer, encoding: String, onlyASCII: Boolean) {
            wrt.write("<?xml version=\"1.0\" encoding=\"")
            wrt.write(XMLUtil.escapeXML(encoding, onlyASCII))
            wrt.write("\"?>\n<Destination>\n")
            for ((key, value) in names) {
                wrt.write("  <Name Page=\"")
                wrt.write(XMLUtil.escapeXML(value, onlyASCII))
                wrt.write("\">")
                wrt.write(XMLUtil.escapeXML(escapeBinaryString(key), onlyASCII))
                wrt.write("</Name>\n")
            }
            wrt.write("</Destination>\n")
            wrt.flush()
        }

        /**
         * Import the names from XML.
         * @param in the XML source. The stream is not closed
         * *
         * @throws IOException on error
         * *
         * @return the names
         */
        @Throws(IOException::class)
        fun importFromXML(`in`: InputStream): HashMap<String, String> {
            val names = SimpleNamedDestination()
            SimpleXMLParser.parse(names, `in`)
            return names.xmlNames
        }

        /**
         * Import the names from XML.
         * @param in the XML source. The reader is not closed
         * *
         * @throws IOException on error
         * *
         * @return the names
         */
        @Throws(IOException::class)
        fun importFromXML(`in`: Reader): HashMap<String, String> {
            val names = SimpleNamedDestination()
            SimpleXMLParser.parse(names, `in`)
            return names.xmlNames
        }

        internal fun createDestinationArray(value: String, writer: PdfWriter): PdfArray {
            val ar = PdfArray()
            val tk = StringTokenizer(value)
            val n = Integer.parseInt(tk.nextToken())
            ar.add(writer.getPageReference(n))
            if (!tk.hasMoreTokens()) {
                ar.add(PdfName.XYZ)
                ar.add(floatArrayOf(0f, 10000f, 0f))
            } else {
                var fn = tk.nextToken()
                if (fn.startsWith("/"))
                    fn = fn.substring(1)
                ar.add(PdfName(fn))
                var k = 0
                while (k < 4 && tk.hasMoreTokens()) {
                    fn = tk.nextToken()
                    if (fn == "null")
                        ar.add(PdfNull.PDFNULL)
                    else
                        ar.add(PdfNumber(fn))
                    ++k
                }
            }
            return ar
        }

        fun outputNamedDestinationAsNames(names: HashMap<String, String>, writer: PdfWriter): PdfDictionary {
            val dic = PdfDictionary()
            for ((key, value) in names) {
                try {
                    val ar = createDestinationArray(value, writer)
                    val kn = PdfName(key)
                    dic.put(kn, ar)
                } catch (e: Exception) {
                    // empty on purpose
                }

            }
            return dic
        }

        @Throws(IOException::class)
        fun outputNamedDestinationAsStrings(names: HashMap<String, String>, writer: PdfWriter): PdfDictionary {
            val n2 = HashMap<String, PdfObject>(names.size)
            for (entry in names.entries) {
                try {
                    val value = entry.value
                    val ar = createDestinationArray(value, writer)
                    n2.put(entry.key, writer.addToBody(ar).indirectReference)
                } catch (e: Exception) {
                }

            }
            return PdfNameTree.writeTree(n2, writer)
        }

        fun escapeBinaryString(s: String): String {
            val buf = StringBuffer()
            val cc = s.toCharArray()
            val len = cc.size
            for (k in 0..len - 1) {
                val c = cc[k]
                if (c < ' ') {
                    buf.append('\\')
                    val octal = "00" + Integer.toOctalString(c.toInt())
                    buf.append(octal.substring(octal.length - 3))
                } else if (c == '\\')
                    buf.append("\\\\")
                else
                    buf.append(c)
            }
            return buf.toString()
        }

        fun unEscapeBinaryString(s: String): String {
            val buf = StringBuffer()
            val cc = s.toCharArray()
            val len = cc.size
            var k = 0
            while (k < len) {
                var c = cc[k]
                if (c == '\\') {
                    if (++k >= len) {
                        buf.append('\\')
                        break
                    }
                    c = cc[k]
                    if (c >= '0' && c <= '7') {
                        var n = c - '0'
                        ++k
                        var j = 0
                        while (j < 2 && k < len) {
                            c = cc[k]
                            if (c >= '0' && c <= '7') {
                                ++k
                                n = n * 8 + c.toInt() - '0'
                            } else {
                                break
                            }
                            ++j
                        }
                        --k
                        buf.append(n.toChar())
                    } else
                        buf.append(c)
                } else
                    buf.append(c)
                ++k
            }
            return buf.toString()
        }
    }
}
