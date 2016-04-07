/*
 * $Id: 58ea330eb47b7e047ff1346736d0801278c29035 $
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
package com.itextpdf.text.xml.xmp

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.xml.XmlDomWriter

/**
 * Reads an XMP stream into an org.w3c.dom.Document objects.
 * Allows you to replace the contents of a specific tag.
 * @since 2.1.3
 */
@Deprecated("")
class XmpReader
/**
 * Constructs an XMP reader
 * @param    bytes    the XMP content
 * *
 * @throws ExceptionConverter
 * *
 * @throws IOException
 * *
 * @throws SAXException
 */
@Throws(SAXException::class, IOException::class)
constructor(bytes: ByteArray) {

    private var domDocument: Document? = null

    init {
        try {
            val fact = DocumentBuilderFactory.newInstance()
            fact.isNamespaceAware = true
            val db = fact.newDocumentBuilder()
            val bais = ByteArrayInputStream(bytes)
            domDocument = db.parse(bais)
        } catch (e: ParserConfigurationException) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Replaces the content of a tag.
     * @param    namespaceURI    the URI of the namespace
     * *
     * @param    localName        the tag name
     * *
     * @param    value            the new content for the tag
     * *
     * @return    true if the content was successfully replaced
     * *
     * @since    2.1.6 the return type has changed from void to boolean
     */
    fun replaceNode(namespaceURI: String, localName: String, value: String): Boolean {
        val nodes = domDocument!!.getElementsByTagNameNS(namespaceURI, localName)
        var node: Node
        if (nodes.length == 0)
            return false
        for (i in 0..nodes.length - 1) {
            node = nodes.item(i)
            setNodeText(domDocument, node, value)
        }
        return true
    }

    /**
     * Replaces the content of an attribute in the description tag.
     * @param    namespaceURI    the URI of the namespace
     * *
     * @param    localName        the tag name
     * *
     * @param    value            the new content for the tag
     * *
     * @return    true if the content was successfully replaced
     * *
     * @since    5.0.0 the return type has changed from void to boolean
     */
    fun replaceDescriptionAttribute(namespaceURI: String, localName: String, value: String): Boolean {
        val descNodes = domDocument!!.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description")
        if (descNodes.length == 0) {
            return false
        }
        var node: Node
        for (i in 0..descNodes.length - 1) {
            node = descNodes.item(i)
            val attr = node.attributes.getNamedItemNS(namespaceURI, localName)
            if (attr != null) {
                attr.nodeValue = value
                return true
            }
        }
        return false
    }

    /**
     * Adds a tag.
     * @param    namespaceURI    the URI of the namespace
     * *
     * @param    parent            the tag name of the parent
     * *
     * @param    localName        the name of the tag to add
     * *
     * @param    value            the new content for the tag
     * *
     * @return    true if the content was successfully added
     * *
     * @since    2.1.6
     */
    fun add(parent: String, namespaceURI: String, localName: String, value: String): Boolean {
        val nodes = domDocument!!.getElementsByTagName(parent)
        if (nodes.length == 0)
            return false
        var pNode: Node
        var node: Node
        val prefix: String
        for (i in 0..nodes.length - 1) {
            pNode = nodes.item(i)
            val attrs = pNode.attributes
            for (j in 0..attrs.length - 1) {
                node = attrs.item(j)
                if (namespaceURI == node.nodeValue) {
                    prefix = node.localName
                    node = domDocument!!.createElementNS(namespaceURI, localName)
                    node.setPrefix(prefix)
                    node.appendChild(domDocument!!.createTextNode(value))
                    pNode.appendChild(node)
                    return true
                }
            }
        }
        return false
    }

    /**
     * Sets the text of this node. All the child's node are deleted and a new
     * child text node is created.
     * @param domDocument the Document that contains the node
     * *
     * @param n the Node to add the text to
     * *
     * @param value the text to add
     */
    fun setNodeText(domDocument: Document, n: Node?, value: String): Boolean {
        if (n == null)
            return false
        var nc: Node? = null
        while ((nc = n.firstChild) != null) {
            n.removeChild(nc)
        }
        n.appendChild(domDocument.createTextNode(value))
        return true
    }

    /**
     * Writes the document to a byte array.
     */
    @Throws(IOException::class)
    fun serializeDoc(): ByteArray {
        val xw = XmlDomWriter()
        val fout = ByteArrayOutputStream()
        xw.setOutput(fout, null)
        fout.write(XPACKET_PI_BEGIN.toByteArray(charset("UTF-8")))
        fout.flush()
        val xmpmeta = domDocument!!.getElementsByTagName("x:xmpmeta")
        xw.write(xmpmeta.item(0))
        fout.flush()
        for (i in 0..19) {
            fout.write(EXTRASPACE.toByteArray())
        }
        fout.write(XPACKET_PI_END_W.toByteArray())
        fout.close()
        return fout.toByteArray()
    }

    companion object {


        /** String used to fill the extra space.  */
        val EXTRASPACE = "                                                                                                   \n"

        /**
         * Processing Instruction required at the start of an XMP stream
         */
        val XPACKET_PI_BEGIN = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"

        /**
         * Processing Instruction required at the end of an XMP stream for XMP streams that can be updated
         */
        val XPACKET_PI_END_W = "<?xpacket end=\"w\"?>"
    }
}
