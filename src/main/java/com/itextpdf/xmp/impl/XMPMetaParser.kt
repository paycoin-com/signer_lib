//Copyright (c) 2006, Adobe Systems Incorporated
//All rights reserved.
//
//        Redistribution and use in source and binary forms, with or without
//        modification, are permitted provided that the following conditions are met:
//        1. Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//        2. Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//        3. All advertising materials mentioning features or use of this software
//        must display the following acknowledgement:
//        This product includes software developed by the Adobe Systems Incorporated.
//        4. Neither the name of the Adobe Systems Incorporated nor the
//        names of its contributors may be used to endorse or promote products
//        derived from this software without specific prior written permission.
//
//        THIS SOFTWARE IS PROVIDED BY ADOBE SYSTEMS INCORPORATED ''AS IS'' AND ANY
//        EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//        WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//        DISCLAIMED. IN NO EVENT SHALL ADOBE SYSTEMS INCORPORATED BE LIABLE FOR ANY
//        DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//        (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//        LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//        ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//        (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//        http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

package com.itextpdf.xmp.impl

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.io.UnsupportedEncodingException

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ProcessingInstruction
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.options.ParseOptions


/**
 * This class replaces the `ExpatAdapter.cpp` and does the
 * XML-parsing and fixes the prefix. After the parsing several normalisations
 * are applied to the XMPTree.

 * @since 01.02.2006
 */
object XMPMetaParser {
    /**   */
    private val XMP_RDF = Object()
    /** the DOM Parser Factory, options are set  */
    private val factory = createDocumentBuilderFactory()


    /**
     * Parses the input source into an XMP metadata object, including
     * de-aliasing and normalisation.

     * @param input the input can be an `InputStream`, a `String` or
     * * 			a byte buffer containing the XMP packet.
     * *
     * @param options the parse options
     * *
     * @return Returns the resulting XMP metadata object
     * *
     * @throws XMPException Thrown if parsing or normalisation fails.
     */
    @Throws(XMPException::class)
    fun parse(input: Any, options: ParseOptions?): XMPMeta {
        var options = options
        ParameterAsserts.assertNotNull(input)
        options = if (options != null) options else ParseOptions()

        val document = parseXml(input, options)

        val xmpmetaRequired = options.requireXMPMeta
        var result: Array<Any>? = arrayOfNulls(3)
        result = findRootNode(document, xmpmetaRequired, result)

        if (result != null && result[1] === XMP_RDF) {
            val xmp = ParseRDF.parse(result[0] as Node)
            xmp.packetHeader = result[2] as String

            // Check if the XMP object shall be normalized
            if (!options.omitNormalization) {
                return XMPNormalizer.process(xmp, options)
            } else {
                return xmp
            }
        } else {
            // no appropriate root node found, return empty metadata object
            return XMPMetaImpl()
        }
    }


    /**
     * Parses the raw XML metadata packet considering the parsing options.
     * Latin-1/ISO-8859-1 can be accepted when the input is a byte stream
     * (some old toolkits versions such packets). The stream is
     * then wrapped in another stream that converts Latin-1 to UTF-8.
     *
     *
     * If control characters shall be fixed, a reader is used that fixes the chars to spaces
     * (if the input is a byte stream is has to be read as character stream).
     *
     *
     * Both options reduce the performance of the parser.

     * @param input the input can be an `InputStream`, a `String` or
     * * 			a byte buffer containing the XMP packet.
     * *
     * @param options the parsing options
     * *
     * @return Returns the parsed XML document or an exception.
     * *
     * @throws XMPException Thrown if the parsing fails for different reasons
     */
    @Throws(XMPException::class)
    private fun parseXml(input: Any, options: ParseOptions): Document {
        if (input is InputStream) {
            return parseXmlFromInputStream(input, options)
        } else if (input is ByteArray) {
            return parseXmlFromBytebuffer(ByteBuffer(input), options)
        } else {
            return parseXmlFromString(input as String, options)
        }
    }


    /**
     * Parses XML from an [InputStream],
     * fixing the encoding (Latin-1 to UTF-8) and illegal control character optionally.

     * @param stream an `InputStream`
     * *
     * @param options the parsing options
     * *
     * @return Returns an XML DOM-Document.
     * *
     * @throws XMPException Thrown when the parsing fails.
     */
    @Throws(XMPException::class)
    private fun parseXmlFromInputStream(stream: InputStream, options: ParseOptions): Document {
        if (!options.acceptLatin1 && !options.fixControlChars) {
            return parseInputSource(InputSource(stream))
        } else {
            // load stream into bytebuffer
            try {
                val buffer = ByteBuffer(stream)
                return parseXmlFromBytebuffer(buffer, options)
            } catch (e: IOException) {
                throw XMPException("Error reading the XML-file",
                        XMPError.BADSTREAM, e)
            }

        }
    }


    /**
     * Parses XML from a byte buffer,
     * fixing the encoding (Latin-1 to UTF-8) and illegal control character optionally.

     * @param buffer a byte buffer containing the XMP packet
     * *
     * @param options the parsing options
     * *
     * @return Returns an XML DOM-Document.
     * *
     * @throws XMPException Thrown when the parsing fails.
     */
    @Throws(XMPException::class)
    private fun parseXmlFromBytebuffer(buffer: ByteBuffer, options: ParseOptions): Document {
        var buffer = buffer
        var source = InputSource(buffer.byteStream)
        try {
            return parseInputSource(source)
        } catch (e: XMPException) {
            if (e.errorCode == XMPError.BADXML || e.errorCode == XMPError.BADSTREAM) {
                if (options.acceptLatin1) {
                    buffer = Latin1Converter.convert(buffer)
                }

                if (options.fixControlChars) {
                    try {
                        val encoding = buffer.encoding
                        val fixReader = FixASCIIControlsReader(
                                InputStreamReader(
                                        buffer.byteStream, encoding))
                        return parseInputSource(InputSource(fixReader))
                    } catch (e1: UnsupportedEncodingException) {
                        // can normally not happen as the encoding is provided by a util function
                        throw XMPException("Unsupported Encoding",
                                XMPError.INTERNALFAILURE, e)
                    }

                }
                source = InputSource(buffer.byteStream)
                return parseInputSource(source)
            } else {
                throw e
            }
        }

    }


    /**
     * Parses XML from a [String],
     * fixing the illegal control character optionally.

     * @param input a `String` containing the XMP packet
     * *
     * @param options the parsing options
     * *
     * @return Returns an XML DOM-Document.
     * *
     * @throws XMPException Thrown when the parsing fails.
     */
    @Throws(XMPException::class)
    private fun parseXmlFromString(input: String, options: ParseOptions): Document {
        var source = InputSource(StringReader(input))
        try {
            return parseInputSource(source)
        } catch (e: XMPException) {
            if (e.errorCode == XMPError.BADXML && options.fixControlChars) {
                source = InputSource(FixASCIIControlsReader(StringReader(input)))
                return parseInputSource(source)
            } else {
                throw e
            }
        }

    }


    /**
     * Runs the XML-Parser.
     * @param source an `InputSource`
     * *
     * @return Returns an XML DOM-Document.
     * *
     * @throws XMPException Wraps parsing and I/O-exceptions into an XMPException.
     */
    @Throws(XMPException::class)
    private fun parseInputSource(source: InputSource): Document {
        try {
            val builder = factory.newDocumentBuilder()
            builder.setErrorHandler(null)
            return builder.parse(source)
        } catch (e: SAXException) {
            throw XMPException("XML parsing failure", XMPError.BADXML, e)
        } catch (e: ParserConfigurationException) {
            throw XMPException("XML Parser not correctly configured",
                    XMPError.UNKNOWN, e)
        } catch (e: IOException) {
            throw XMPException("Error reading the XML-file", XMPError.BADSTREAM, e)
        }

    }


    /**
     * Find the XML node that is the root of the XMP data tree. Generally this
     * will be an outer node, but it could be anywhere if a general XML document
     * is parsed (e.g. SVG). The XML parser counted all rdf:RDF and
     * pxmp:XMP_Packet nodes, and kept a pointer to the last one. If there is
     * more than one possible root use PickBestRoot to choose among them.
     *
     *
     * If there is a root node, try to extract the version of the previous XMP
     * toolkit.
     *
     *
     * Pick the first x:xmpmeta among multiple root candidates. If there aren't
     * any, pick the first bare rdf:RDF if that is allowed. The returned root is
     * the rdf:RDF child if an x:xmpmeta element was chosen. The search is
     * breadth first, so a higher level candiate is chosen over a lower level
     * one that was textually earlier in the serialized XML.

     * @param root the root of the xml document
     * *
     * @param xmpmetaRequired flag if the xmpmeta-tag is still required, might be set
     * * 		initially to `true`, if the parse option "REQUIRE_XMP_META" is set
     * *
     * @param result The result array that is filled during the recursive process.
     * *
     * @return Returns an array that contains the result or `null`.
     * * 		   The array contains:
     * *
     * * 		 * [0] - the rdf:RDF-node
     * * 		 * [1] - an object that is either XMP_RDF or XMP_PLAIN (the latter is decrecated)
     * * 		 * [2] - the body text of the xpacket-instruction.
     * *
     */
    private fun findRootNode(root: Node, xmpmetaRequired: Boolean, result: Array<Any>?): Array<Any>? {
        var root = root
        // Look among this parent's content for x:xapmeta or x:xmpmeta.
        // The recursion for x:xmpmeta is broader than the strictly defined choice,
        // but gives us smaller code.
        val children = root.childNodes
        for (i in 0..children.length - 1) {
            root = children.item(i)
            if (Node.PROCESSING_INSTRUCTION_NODE == root.nodeType && XMPConst.XMP_PI == (root as ProcessingInstruction).target) {
                // Store the processing instructions content
                if (result != null) {
                    result[2] = root.data
                }
            } else if (Node.TEXT_NODE != root.nodeType && Node.PROCESSING_INSTRUCTION_NODE != root.nodeType) {
                val rootNS = root.namespaceURI
                val rootLocal = root.localName
                if ((XMPConst.TAG_XMPMETA == rootLocal || XMPConst.TAG_XAPMETA == rootLocal) && XMPConst.NS_X == rootNS) {
                    // by not passing the RequireXMPMeta-option, the rdf-Node will be valid
                    return findRootNode(root, false, result)
                } else if (!xmpmetaRequired &&
                        "RDF" == rootLocal &&
                        XMPConst.NS_RDF == rootNS) {
                    if (result != null) {
                        result[0] = root
                        result[1] = XMP_RDF
                    }
                    return result
                } else {
                    // continue searching
                    val newResult = findRootNode(root, xmpmetaRequired, result)
                    if (newResult != null) {
                        return newResult
                    } else {
                        continue
                    }
                }
            }
        }

        // no appropriate node has been found
        return null
        //     is extracted here in the C++ Toolkit
    }


    /**
     * @return Creates, configures and returnes the document builder factory for
     * *         the Metadata Parser.
     */
    private fun createDocumentBuilderFactory(): DocumentBuilderFactory {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isIgnoringComments = true

        try {
            // honor System parsing limits, e.g.
            // System.setProperty("entityExpansionLimit", "10");
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

            //Security stuff. Protecting against XEE attacks as described here: https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            // Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks" (see reference below)
            factory.isXIncludeAware = false
            factory.isExpandEntityReferences = false

        } catch (e: Exception) {
            // Ignore IllegalArgumentException and ParserConfigurationException
            // in case the configured XML-Parser does not implement the feature.
        }

        return factory
    }
}
/**
 * Hidden constructor, initialises the SAX parser handler.
 */
// EMPTY