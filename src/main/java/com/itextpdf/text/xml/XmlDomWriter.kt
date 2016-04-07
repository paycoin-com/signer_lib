/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itextpdf.text.xml

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.UnsupportedEncodingException

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.DocumentType
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**

 * @author psoares
 */
class XmlDomWriter {

    /** Print writer.  */
    protected var fOut: PrintWriter

    /** Canonical output.  */
    protected var fCanonical: Boolean = false

    /** Processing XML 1.1 document.  */
    protected var fXML11: Boolean = false

    //
    // Constructors
    //

    /** Default constructor.  */
    constructor() {
    } // <init>()

    constructor(canonical: Boolean) {
        fCanonical = canonical
    } // <init>(boolean)

    //
    // Public methods
    //

    /** Sets whether output is canonical.  */
    fun setCanonical(canonical: Boolean) {
        fCanonical = canonical
    } // setCanonical(boolean)

    /** Sets the output stream for printing.  */
    @Throws(UnsupportedEncodingException::class)
    fun setOutput(stream: OutputStream, encoding: String?) {
        var encoding = encoding

        if (encoding == null) {
            encoding = "UTF8"
        }

        val writer = OutputStreamWriter(stream, encoding)
        fOut = PrintWriter(writer)

    } // setOutput(OutputStream,String)

    /** Sets the output writer.  */
    fun setOutput(writer: java.io.Writer) {

        fOut = if (writer is PrintWriter)
            writer
        else
            PrintWriter(writer)

    } // setOutput(java.io.Writer)

    /** Writes the specified node, recursively.  */
    fun write(node: Node?) {

        // is there anything to do?
        if (node == null) {
            return
        }

        val type = node.nodeType
        when (type) {
            Node.DOCUMENT_NODE -> {
                val document = node as Document?
                fXML11 = false //"1.1".equals(getVersion(document));
                if (!fCanonical) {
                    if (fXML11) {
                        fOut.println("<?xml version=\"1.1\" encoding=\"UTF-8\"?>")
                    } else {
                        fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    }
                    fOut.flush()
                    write(document.getDoctype())
                }
                write(document.getDocumentElement())
            }

            Node.DOCUMENT_TYPE_NODE -> {
                val doctype = node as DocumentType?
                fOut.print("<!DOCTYPE ")
                fOut.print(doctype.getName())
                val publicId = doctype.getPublicId()
                val systemId = doctype.getSystemId()
                if (publicId != null) {
                    fOut.print(" PUBLIC '")
                    fOut.print(publicId)
                    fOut.print("' '")
                    fOut.print(systemId)
                    fOut.print('\'')
                } else if (systemId != null) {
                    fOut.print(" SYSTEM '")
                    fOut.print(systemId)
                    fOut.print('\'')
                }
                val internalSubset = doctype.getInternalSubset()
                if (internalSubset != null) {
                    fOut.println(" [")
                    fOut.print(internalSubset)
                    fOut.print(']')
                }
                fOut.println('>')
            }

            Node.ELEMENT_NODE -> {
                fOut.print('<')
                fOut.print(node.nodeName)
                val attrs = sortAttributes(node.attributes)
                for (i in attrs.indices) {
                    val attr = attrs[i]
                    fOut.print(' ')
                    fOut.print(attr.nodeName)
                    fOut.print("=\"")
                    normalizeAndPrint(attr.nodeValue, true)
                    fOut.print('"')
                }
                fOut.print('>')
                fOut.flush()

                var child: Node? = node.firstChild
                while (child != null) {
                    write(child)
                    child = child.nextSibling
                }
            }

            Node.ENTITY_REFERENCE_NODE -> {
                if (fCanonical) {
                    var child: Node? = node.firstChild
                    while (child != null) {
                        write(child)
                        child = child.nextSibling
                    }
                } else {
                    fOut.print('&')
                    fOut.print(node.nodeName)
                    fOut.print(';')
                    fOut.flush()
                }
            }

            Node.CDATA_SECTION_NODE -> {
                if (fCanonical) {
                    normalizeAndPrint(node.nodeValue, false)
                } else {
                    fOut.print("<![CDATA[")
                    fOut.print(node.nodeValue)
                    fOut.print("]]>")
                }
                fOut.flush()
            }

            Node.TEXT_NODE -> {
                normalizeAndPrint(node.nodeValue, false)
                fOut.flush()
            }

            Node.PROCESSING_INSTRUCTION_NODE -> {
                fOut.print("<?")
                fOut.print(node.nodeName)
                val data = node.nodeValue
                if (data != null && data.length > 0) {
                    fOut.print(' ')
                    fOut.print(data)
                }
                fOut.print("?>")
                fOut.flush()
            }

            Node.COMMENT_NODE -> {
                if (!fCanonical) {
                    fOut.print("<!--")
                    val comment = node.nodeValue
                    if (comment != null && comment.length > 0) {
                        fOut.print(comment)
                    }
                    fOut.print("-->")
                    fOut.flush()
                }
            }
        }

        if (type == Node.ELEMENT_NODE) {
            fOut.print("</")
            fOut.print(node.nodeName)
            fOut.print('>')
            fOut.flush()
        }

    } // write(Node)

    /** Returns a sorted list of attributes.  */
    protected fun sortAttributes(attrs: NamedNodeMap?): Array<Attr> {

        val len = if (attrs != null) attrs.length else 0
        val array = arrayOfNulls<Attr>(len)
        for (i in 0..len - 1) {
            array[i] = attrs!!.item(i) as Attr
        }
        for (i in 0..len - 1 - 1) {
            var name = array[i].getNodeName()
            var index = i
            for (j in i + 1..len - 1) {
                val curName = array[j].getNodeName()
                if (curName.compareTo(name) < 0) {
                    name = curName
                    index = j
                }
            }
            if (index != i) {
                val temp = array[i]
                array[i] = array[index]
                array[index] = temp
            }
        }

        return array

    } // sortAttributes(NamedNodeMap):Attr[]

    //
    // Protected methods
    //

    /** Normalizes and prints the given string.  */
    protected fun normalizeAndPrint(s: String?, isAttValue: Boolean) {

        val len = if (s != null) s.length else 0
        for (i in 0..len - 1) {
            val c = s!![i]
            normalizeAndPrint(c, isAttValue)
        }

    } // normalizeAndPrint(String,boolean)

    /** Normalizes and print the given character.  */
    protected fun normalizeAndPrint(c: Char, isAttValue: Boolean) {

        when (c) {
            '<' -> {
                fOut.print("&lt;")
            }
            '>' -> {
                fOut.print("&gt;")
            }
            '&' -> {
                fOut.print("&amp;")
            }
            '"' -> {
                // A '"' that appears in character data
                // does not need to be escaped.
                if (isAttValue) {
                    fOut.print("&quot;")
                } else {
                    fOut.print("\"")
                }
            }
            '\r' -> {
                // If CR is part of the document's content, it
                // must not be printed as a literal otherwise
                // it would be normalized to LF when the document
                // is reparsed.
                fOut.print("&#xD;")
            }
            '\n' -> {
                run {
                    if (fCanonical) {
                        fOut.print("&#xA;")
                        break
                    } // else, default print char
                }
                run { // In XML 1.1, control chars in the ranges [#x1-#x1F, #x7F-#x9F] must be escaped.
                    //
                    // Escape space characters that would be normalized to #x20 in attribute values
                    // when the document is reparsed.
                    //
                    // Escape NEL (0x85) and LSEP (0x2028) that appear in content
                    // if the document is XML 1.1, since they would be normalized to LF
                    // when the document is reparsed.
                    if (fXML11 && (c.toInt() >= 0x01 && c.toInt() <= 0x1F && c.toInt() != 0x09 && c.toInt() != 0x0A
                            || c.toInt() >= 0x7F && c.toInt() <= 0x9F || c.toInt() == 0x2028) || isAttValue && (c.toInt() == 0x09 || c.toInt() == 0x0A)) {
                        fOut.print("&#x")
                        fOut.print(Integer.toHexString(c.toInt()).toUpperCase())
                        fOut.print(";")
                    } else {
                        fOut.print(c)
                    }
                }
            }
            else -> {
                if (fXML11 && (c.toInt() >= 0x01 && c.toInt() <= 0x1F && c.toInt() != 0x09 && c.toInt() != 0x0A || c.toInt() >= 0x7F && c.toInt() <= 0x9F || c.toInt() == 0x2028) || isAttValue && (c.toInt() == 0x09 || c.toInt() == 0x0A)) {
                    fOut.print("&#x")
                    fOut.print(Integer.toHexString(c.toInt()).toUpperCase())
                    fOut.print(";")
                } else {
                    fOut.print(c)
                }
            }
        }
    } // normalizeAndPrint(char,boolean)

    /** Extracts the XML version from the Document.  */
    //    protected String getVersion(Document document) {
    //        if (document == null) {
    //            return null;
    //        }
    //        String version = null;
    //        Method getXMLVersion = null;
    //        try {
    //            getXMLVersion = document.getClass().getMethod("getXmlVersion", new Class[]{});
    //            // If Document class implements DOM L3, this method will exist.
    //            if (getXMLVersion != null) {
    //                version = (String) getXMLVersion.invoke(document, (Object[]) null);
    //            }
    //        } catch (Exception e) {
    //            // Either this locator object doesn't have
    //            // this method, or we're on an old JDK.
    //        }
    //        return version;
    //    } // getVersion(Document)
}
