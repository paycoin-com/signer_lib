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
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Arrays
import java.util.HashSet

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.options.SerializeOptions


/**
 * Serializes the `XMPMeta`-object using the standard RDF serialization format.
 * The output is written to an `OutputStream`
 * according to the `SerializeOptions`.

 * @since   11.07.2006
 */
class XMPSerializerRDF {

    /** the metadata object to be serialized.  */
    private var xmp: XMPMetaImpl? = null
    /** the output stream to serialize to  */
    private var outputStream: CountOutputStream? = null
    /** this writer is used to do the actual serialization  */
    private var writer: OutputStreamWriter? = null
    /** the stored serialization options  */
    private var options: SerializeOptions? = null
    /** the size of one unicode char, for UTF-8 set to 1
     * (Note: only valid for ASCII chars lower than 0x80),
     * set to 2 in case of UTF-16  */
    private var unicodeSize = 1 // UTF-8
    /** the padding in the XMP Packet, or the length of the complete packet in
     * case of option *exactPacketLength*.  */
    private var padding: Int = 0


    /**
     * The actual serialization.

     * @param xmp the metadata object to be serialized
     * *
     * @param out outputStream the output stream to serialize to
     * *
     * @param options the serialization options
     * *
     * *
     * @throws XMPException If case of wrong options or any other serialization error.
     */
    @Throws(XMPException::class)
    fun serialize(xmp: XMPMeta, out: OutputStream,
                  options: SerializeOptions) {
        try {
            outputStream = CountOutputStream(out)
            writer = OutputStreamWriter(outputStream, options.encoding)

            this.xmp = xmp as XMPMetaImpl
            this.options = options
            this.padding = options.padding

            writer = OutputStreamWriter(outputStream, options.encoding)

            checkOptionsConsistence()

            // serializes the whole packet, but don't write the tail yet 
            // and flush to make sure that the written bytes are calculated correctly
            val tailStr = serializeAsRDF()
            writer!!.flush()

            // adds padding
            addPadding(tailStr.length)

            // writes the tail
            write(tailStr)
            writer!!.flush()

            outputStream!!.close()
        } catch (e: IOException) {
            throw XMPException("Error writing to the OutputStream", XMPError.UNKNOWN)
        }

    }


    /**
     * Calculates the padding according to the options and write it to the stream.
     * @param tailLength the length of the tail string
     * *
     * @throws XMPException thrown if packet size is to small to fit the padding
     * *
     * @throws IOException forwards writer errors
     */
    @Throws(XMPException::class, IOException::class)
    private fun addPadding(tailLength: Int) {
        if (options!!.exactPacketLength) {
            // the string length is equal to the length of the UTF-8 encoding
            val minSize = outputStream!!.bytesWritten + tailLength * unicodeSize
            if (minSize > padding) {
                throw XMPException("Can't fit into specified packet size",
                        XMPError.BADSERIALIZE)
            }
            padding -= minSize    // Now the actual amount of padding to add.
        }

        // fix rest of the padding according to Unicode unit size.
        padding /= unicodeSize

        val newlineLen = options!!.newline.length
        if (padding >= newlineLen) {
            padding -= newlineLen    // Write this newline last.
            while (padding >= 100 + newlineLen) {
                writeChars(100, ' ')
                writeNewline()
                padding -= 100 + newlineLen
            }
            writeChars(padding, ' ')
            writeNewline()
        } else {
            writeChars(padding, ' ')
        }
    }


    /**
     * Checks if the supplied options are consistent.
     * @throws XMPException Thrown if options are conflicting
     */
    @Throws(XMPException::class)
    protected fun checkOptionsConsistence() {
        if (options!!.encodeUTF16BE or options!!.encodeUTF16LE) {
            unicodeSize = 2
        }

        if (options!!.exactPacketLength) {
            if (options!!.omitPacketWrapper or options!!.includeThumbnailPad) {
                throw XMPException("Inconsistent options for exact size serialize",
                        XMPError.BADOPTIONS)
            }
            if (options!!.padding and unicodeSize - 1 != 0) {
                throw XMPException("Exact size must be a multiple of the Unicode element",
                        XMPError.BADOPTIONS)
            }
        } else if (options!!.readOnlyPacket) {
            if (options!!.omitPacketWrapper or options!!.includeThumbnailPad) {
                throw XMPException("Inconsistent options for read-only packet",
                        XMPError.BADOPTIONS)
            }
            padding = 0
        } else if (options!!.omitPacketWrapper) {
            if (options!!.includeThumbnailPad) {
                throw XMPException("Inconsistent options for non-packet serialize",
                        XMPError.BADOPTIONS)
            }
            padding = 0
        } else {
            if (padding == 0) {
                padding = DEFAULT_PAD * unicodeSize
            }

            if (options!!.includeThumbnailPad) {
                if (!xmp!!.doesPropertyExist(XMPConst.NS_XMP, "Thumbnails")) {
                    padding += 10000 * unicodeSize
                }
            }
        }
    }


    /**
     * Writes the (optional) packet header and the outer rdf-tags.
     * @return Returns the packet end processing instraction to be written after the padding.
     * *
     * @throws IOException Forwarded writer exceptions.
     * *
     * @throws XMPException
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeAsRDF(): String {
        var level = 0

        // Write the packet header PI.
        if (!options!!.omitPacketWrapper) {
            writeIndent(level)
            write(PACKET_HEADER)
            writeNewline()
        }

        // Write the x:xmpmeta element's start tag.
        if (!options!!.omitXmpMetaElement) {
            writeIndent(level)
            write(RDF_XMPMETA_START)
            // Note: this flag can only be set by unit tests
            if (!options!!.omitVersionAttribute) {
                write(XMPMetaFactory.getVersionInfo().message)
            }
            write("\">")
            writeNewline()
            level++
        }

        // Write the rdf:RDF start tag.
        writeIndent(level)
        write(RDF_RDF_START)
        writeNewline()

        // Write all of the properties.
        if (options!!.useCanonicalFormat) {
            serializeCanonicalRDFSchemas(level)
        } else {
            serializeCompactRDFSchemas(level)
        }

        // Write the rdf:RDF end tag.
        writeIndent(level)
        write(RDF_RDF_END)
        writeNewline()

        // Write the xmpmeta end tag.
        if (!options!!.omitXmpMetaElement) {
            level--
            writeIndent(level)
            write(RDF_XMPMETA_END)
            writeNewline()
        }
        // Write the packet trailer PI into the tail string as UTF-8.
        var tailStr = ""
        if (!options!!.omitPacketWrapper) {
            level = options!!.baseIndent
            while (level > 0) {
                tailStr += options!!.indent
                level--
            }

            tailStr += PACKET_TRAILER
            tailStr += if (options!!.readOnlyPacket) 'r' else 'w'
            tailStr += PACKET_TRAILER2
        }

        return tailStr
    }


    /**
     * Serializes the metadata in pretty-printed manner.
     * @param level indent level
     * *
     * @throws IOException Forwarded writer exceptions
     * *
     * @throws XMPException
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCanonicalRDFSchemas(level: Int) {
        if (xmp!!.root.childrenLength > 0) {
            startOuterRDFDescription(xmp!!.root, level)

            val it = xmp!!.root.iterateChildren()
            while (it.hasNext()) {
                val currSchema = it.next() as XMPNode
                serializeCanonicalRDFSchema(currSchema, level)
            }

            endOuterRDFDescription(level)
        } else {
            writeIndent(level + 1)
            write(RDF_SCHEMA_START) // Special case an empty XMP object.
            writeTreeName()
            write("/>")
            writeNewline()
        }
    }


    /**
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeTreeName() {
        write('"')
        val name = xmp!!.root.name
        if (name != null) {
            appendNodeValue(name, true)
        }
        write('"')
    }


    /**
     * Serializes the metadata in compact manner.
     * @param level indent level to start with
     * *
     * @throws IOException Forwarded writer exceptions
     * *
     * @throws XMPException
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCompactRDFSchemas(level: Int) {
        // Begin the rdf:Description start tag.
        writeIndent(level + 1)
        write(RDF_SCHEMA_START)
        writeTreeName()

        // Write all necessary xmlns attributes.
        val usedPrefixes = HashSet()
        usedPrefixes.add("xml")
        usedPrefixes.add("rdf")

        run {
            val it = xmp!!.root.iterateChildren()
            while (it.hasNext()) {
                val schema = it.next() as XMPNode
                declareUsedNamespaces(schema, usedPrefixes, level + 3)
            }
        }

        // Write the top level "attrProps" and close the rdf:Description start tag.
        var allAreAttrs = true
        run {
            val it = xmp!!.root.iterateChildren()
            while (it.hasNext()) {
                val schema = it.next() as XMPNode
                allAreAttrs = allAreAttrs and serializeCompactRDFAttrProps(schema, level + 2)
            }
        }

        if (!allAreAttrs) {
            write('>')
            writeNewline()
        } else {
            write("/>")
            writeNewline()
            return    // ! Done if all properties in all schema are written as attributes.
        }

        // Write the remaining properties for each schema.
        val it = xmp!!.root.iterateChildren()
        while (it.hasNext()) {
            val schema = it.next() as XMPNode
            serializeCompactRDFElementProps(schema, level + 2)
        }

        // Write the rdf:Description end tag.
        writeIndent(level + 1)
        write(RDF_SCHEMA_END)
        writeNewline()
    }


    /**
     * Write each of the parent's simple unqualified properties as an attribute. Returns true if all
     * of the properties are written as attributes.

     * @param parentNode the parent property node
     * *
     * @param indent the current indent level
     * *
     * @return Returns true if all properties can be rendered as RDF attribute.
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun serializeCompactRDFAttrProps(parentNode: XMPNode, indent: Int): Boolean {
        var allAreAttrs = true

        val it = parentNode.iterateChildren()
        while (it.hasNext()) {
            val prop = it.next() as XMPNode

            if (canBeRDFAttrProp(prop)) {
                writeNewline()
                writeIndent(indent)
                write(prop.name)
                write("=\"")
                appendNodeValue(prop.value, true)
                write('"')
            } else {
                allAreAttrs = false
            }
        }
        return allAreAttrs
    }


    /**
     * Recursively handles the "value" for a node that must be written as an RDF
     * property element. It does not matter if it is a top level property, a
     * field of a struct, or an item of an array. The indent is that for the
     * property element. The patterns bwlow ignore attribute qualifiers such as
     * xml:lang, they don't affect the output form.

     *

     *
     * &lt;ns:UnqualifiedStructProperty-1
     * ... The fields as attributes, if all are simple and unqualified
     * /&gt;

     * &lt;ns:UnqualifiedStructProperty-2 rdf:parseType=&quot;Resource&quot;&gt;
     * ... The fields as elements, if none are simple and unqualified
     * &lt;/ns:UnqualifiedStructProperty-2&gt;

     * &lt;ns:UnqualifiedStructProperty-3&gt;
     * &lt;rdf:Description
     * ... The simple and unqualified fields as attributes
     * &gt;
     * ... The compound or qualified fields as elements
     * &lt;/rdf:Description&gt;
     * &lt;/ns:UnqualifiedStructProperty-3&gt;

     * &lt;ns:UnqualifiedArrayProperty&gt;
     * &lt;rdf:Bag&gt; or Seq or Alt
     * ... Array items as rdf:li elements, same forms as top level properties
     * &lt;/rdf:Bag&gt;
     * &lt;/ns:UnqualifiedArrayProperty&gt;

     * &lt;ns:QualifiedProperty rdf:parseType=&quot;Resource&quot;&gt;
     * &lt;rdf:value&gt; ... Property &quot;value&quot;
     * following the unqualified forms ... &lt;/rdf:value&gt;
     * ... Qualifiers looking like named struct fields
     * &lt;/ns:QualifiedProperty&gt;
     *

     *

     * *** Consider numbered array items, but has compatibility problems. ***
     * Consider qualified form with rdf:Description and attributes.

     * @param parentNode the parent node
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards writer exceptions
     * *
     * @throws XMPException If qualifier and element fields are mixed.
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCompactRDFElementProps(parentNode: XMPNode, indent: Int) {
        val it = parentNode.iterateChildren()
        while (it.hasNext()) {
            val node = it.next() as XMPNode
            if (canBeRDFAttrProp(node)) {
                continue
            }

            var emitEndTag = true
            var indentEndTag = true

            // Determine the XML element name, write the name part of the start tag. Look over the
            // qualifiers to decide on "normal" versus "rdf:value" form. Emit the attribute
            // qualifiers at the same time.
            var elemName = node.name
            if (XMPConst.ARRAY_ITEM_NAME == elemName) {
                elemName = "rdf:li"
            }

            writeIndent(indent)
            write('<')
            write(elemName)

            var hasGeneralQualifiers = false
            var hasRDFResourceQual = false

            val iq = node.iterateQualifier()
            while (iq.hasNext()) {
                val qualifier = iq.next() as XMPNode
                if (!RDF_ATTR_QUALIFIER.contains(qualifier.name)) {
                    hasGeneralQualifiers = true
                } else {
                    hasRDFResourceQual = "rdf:resource" == qualifier.name
                    write(' ')
                    write(qualifier.name)
                    write("=\"")
                    appendNodeValue(qualifier.value, true)
                    write('"')
                }
            }


            // Process the property according to the standard patterns.
            if (hasGeneralQualifiers) {
                serializeCompactRDFGeneralQualifier(indent, node)
            } else {
                // This node has only attribute qualifiers. Emit as a property element.
                if (!node.options.isCompositeProperty) {
                    val result = serializeCompactRDFSimpleProp(node)
                    emitEndTag = (result[0] as Boolean).booleanValue()
                    indentEndTag = (result[1] as Boolean).booleanValue()
                } else if (node.options.isArray) {
                    serializeCompactRDFArrayProp(node, indent)
                } else {
                    emitEndTag = serializeCompactRDFStructProp(
                            node, indent, hasRDFResourceQual)
                }

            }

            // Emit the property element end tag.
            if (emitEndTag) {
                if (indentEndTag) {
                    writeIndent(indent)
                }
                write("</")
                write(elemName)
                write('>')
                writeNewline()
            }

        }
    }


    /**
     * Serializes a simple property.

     * @param node an XMPNode
     * *
     * @return Returns an array containing the flags emitEndTag and indentEndTag.
     * *
     * @throws IOException Forwards the writer exceptions.
     */
    @Throws(IOException::class)
    private fun serializeCompactRDFSimpleProp(node: XMPNode): Array<Any> {
        // This is a simple property.
        var emitEndTag = java.lang.Boolean.TRUE
        var indentEndTag = java.lang.Boolean.TRUE

        if (node.options.isURI) {
            write(" rdf:resource=\"")
            appendNodeValue(node.value, true)
            write("\"/>")
            writeNewline()
            emitEndTag = java.lang.Boolean.FALSE
        } else if (node.value == null || node.value.length == 0) {
            write("/>")
            writeNewline()
            emitEndTag = java.lang.Boolean.FALSE
        } else {
            write('>')
            appendNodeValue(node.value, false)
            indentEndTag = java.lang.Boolean.FALSE
        }

        return arrayOf(emitEndTag, indentEndTag)
    }


    /**
     * Serializes an array property.

     * @param node an XMPNode
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards the writer exceptions.
     * *
     * @throws XMPException If qualifier and element fields are mixed.
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCompactRDFArrayProp(node: XMPNode, indent: Int) {
        // This is an array.
        write('>')
        writeNewline()
        emitRDFArrayTag(node, true, indent + 1)

        if (node.options.isArrayAltText) {
            XMPNodeUtils.normalizeLangArray(node)
        }

        serializeCompactRDFElementProps(node, indent + 2)

        emitRDFArrayTag(node, false, indent + 1)
    }


    /**
     * Serializes a struct property.

     * @param node an XMPNode
     * *
     * @param indent the current indent level
     * *
     * @param hasRDFResourceQual Flag if the element has resource qualifier
     * *
     * @return Returns true if an end flag shall be emitted.
     * *
     * @throws IOException Forwards the writer exceptions.
     * *
     * @throws XMPException If qualifier and element fields are mixed.
     */
    @Throws(XMPException::class, IOException::class)
    private fun serializeCompactRDFStructProp(node: XMPNode, indent: Int,
                                              hasRDFResourceQual: Boolean): Boolean {
        // This must be a struct.
        var hasAttrFields = false
        var hasElemFields = false
        var emitEndTag = true

        val ic = node.iterateChildren()
        while (ic.hasNext()) {
            val field = ic.next() as XMPNode
            if (canBeRDFAttrProp(field)) {
                hasAttrFields = true
            } else {
                hasElemFields = true
            }

            if (hasAttrFields && hasElemFields) {
                break    // No sense looking further.
            }
        }

        if (hasRDFResourceQual && hasElemFields) {
            throw XMPException(
                    "Can't mix rdf:resource qualifier and element fields",
                    XMPError.BADRDF)
        }

        if (!node.hasChildren()) {
            // Catch an empty struct as a special case. The case
            // below would emit an empty
            // XML element, which gets reparsed as a simple property
            // with an empty value.
            write(" rdf:parseType=\"Resource\"/>")
            writeNewline()
            emitEndTag = false

        } else if (!hasElemFields) {
            // All fields can be attributes, use the
            // emptyPropertyElt form.
            serializeCompactRDFAttrProps(node, indent + 1)
            write("/>")
            writeNewline()
            emitEndTag = false

        } else if (!hasAttrFields) {
            // All fields must be elements, use the
            // parseTypeResourcePropertyElt form.
            write(" rdf:parseType=\"Resource\">")
            writeNewline()
            serializeCompactRDFElementProps(node, indent + 1)

        } else {
            // Have a mix of attributes and elements, use an inner rdf:Description.
            write('>')
            writeNewline()
            writeIndent(indent + 1)
            write(RDF_STRUCT_START)
            serializeCompactRDFAttrProps(node, indent + 2)
            write(">")
            writeNewline()
            serializeCompactRDFElementProps(node, indent + 1)
            writeIndent(indent + 1)
            write(RDF_STRUCT_END)
            writeNewline()
        }
        return emitEndTag
    }


    /**
     * Serializes the general qualifier.
     * @param node the root node of the subtree
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards all writer exceptions.
     * *
     * @throws XMPException If qualifier and element fields are mixed.
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCompactRDFGeneralQualifier(indent: Int, node: XMPNode) {
        // The node has general qualifiers, ones that can't be
        // attributes on a property element.
        // Emit using the qualified property pseudo-struct form. The
        // value is output by a call
        // to SerializePrettyRDFProperty with emitAsRDFValue set.
        write(" rdf:parseType=\"Resource\">")
        writeNewline()

        serializeCanonicalRDFProperty(node, false, true, indent + 1)

        val iq = node.iterateQualifier()
        while (iq.hasNext()) {
            val qualifier = iq.next() as XMPNode
            serializeCanonicalRDFProperty(qualifier, false, false, indent + 1)
        }
    }


    /**
     * Serializes one schema with all contained properties in pretty-printed
     * manner.
     * Each schema's properties are written to a single
     * rdf:Description element. All of the necessary namespaces are declared in
     * the rdf:Description element. The baseIndent is the base level for the
     * entire serialization, that of the x:xmpmeta element. An xml:lang
     * qualifier is written as an attribute of the property start tag, not by
     * itself forcing the qualified property form.

     *

     *
     * &lt;rdf:Description rdf:about=&quot;TreeName&quot; xmlns:ns=&quot;URI&quot; ... &gt;

     * ... The actual properties of the schema, see SerializePrettyRDFProperty

     * &lt;!-- ns1:Alias is aliased to ns2:Actual --&gt;  ... If alias comments are wanted

     * &lt;/rdf:Description&gt;
     *

     *

     * @param schemaNode a schema node
     * *
     * @param level
     * *
     * @throws IOException Forwarded writer exceptions
     * *
     * @throws XMPException
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCanonicalRDFSchema(schemaNode: XMPNode, level: Int) {
        // Write each of the schema's actual properties.
        val it = schemaNode.iterateChildren()
        while (it.hasNext()) {
            val propNode = it.next() as XMPNode
            serializeCanonicalRDFProperty(propNode, options!!.useCanonicalFormat, false, level + 2)
        }
    }


    /**
     * Writes all used namespaces of the subtree in node to the output.
     * The subtree is recursivly traversed.
     * @param node the root node of the subtree
     * *
     * @param usedPrefixes a set containing currently used prefixes
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards all writer exceptions.
     */
    @Throws(IOException::class)
    private fun declareUsedNamespaces(node: XMPNode, usedPrefixes: MutableSet<Any>, indent: Int) {
        if (node.options.isSchemaNode) {
            // The schema node name is the URI, the value is the prefix.
            val prefix = node.value.substring(0, node.value.length - 1)
            declareNamespace(prefix, node.name, usedPrefixes, indent)
        } else if (node.options.isStruct) {
            val it = node.iterateChildren()
            while (it.hasNext()) {
                val field = it.next() as XMPNode
                declareNamespace(field.name, null, usedPrefixes, indent)
            }
        }

        run {
            val it = node.iterateChildren()
            while (it.hasNext()) {
                val child = it.next() as XMPNode
                declareUsedNamespaces(child, usedPrefixes, indent)
            }
        }

        val it = node.iterateQualifier()
        while (it.hasNext()) {
            val qualifier = it.next() as XMPNode
            declareNamespace(qualifier.name, null, usedPrefixes, indent)
            declareUsedNamespaces(qualifier, usedPrefixes, indent)
        }
    }


    /**
     * Writes one namespace declaration to the output.
     * @param prefix a namespace prefix (without colon) or a complete qname (when namespace == null)
     * *
     * @param namespace the a namespace
     * *
     * @param usedPrefixes a set containing currently used prefixes
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards all writer exceptions.
     */
    @Throws(IOException::class)
    private fun declareNamespace(prefix: String, namespace: String?, usedPrefixes: MutableSet<Any>, indent: Int) {
        var prefix = prefix
        var namespace = namespace
        if (namespace == null) {
            // prefix contains qname, extract prefix and lookup namespace with prefix
            val qname = QName(prefix)
            if (qname.hasPrefix()) {
                prefix = qname.prefix
                // add colon for lookup
                namespace = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(prefix + ":")
                // prefix w/o colon
                declareNamespace(prefix, namespace, usedPrefixes, indent)
            } else {
                return
            }
        }

        if (!usedPrefixes.contains(prefix)) {
            writeNewline()
            writeIndent(indent)
            write("xmlns:")
            write(prefix)
            write("=\"")
            write(namespace)
            write('"')
            usedPrefixes.add(prefix)
        }
    }


    /**
     * Start the outer rdf:Description element, including all needed xmlns attributes.
     * Leave the element open so that the compact form can add property attributes.

     * @throws IOException If the writing to
     */
    @Throws(IOException::class)
    private fun startOuterRDFDescription(schemaNode: XMPNode, level: Int) {
        writeIndent(level + 1)
        write(RDF_SCHEMA_START)
        writeTreeName()

        val usedPrefixes = HashSet()
        usedPrefixes.add("xml")
        usedPrefixes.add("rdf")

        declareUsedNamespaces(schemaNode, usedPrefixes, level + 3)

        write('>')
        writeNewline()
    }


    /**
     * Write the  end tag.
     */
    @Throws(IOException::class)
    private fun endOuterRDFDescription(level: Int) {
        writeIndent(level + 1)
        write(RDF_SCHEMA_END)
        writeNewline()
    }


    /**
     * Recursively handles the "value" for a node. It does not matter if it is a
     * top level property, a field of a struct, or an item of an array. The
     * indent is that for the property element. An xml:lang qualifier is written
     * as an attribute of the property start tag, not by itself forcing the
     * qualified property form. The patterns below mostly ignore attribute
     * qualifiers like xml:lang. Except for the one struct case, attribute
     * qualifiers don't affect the output form.

     *

     *
     * &lt;ns:UnqualifiedSimpleProperty&gt;value&lt;/ns:UnqualifiedSimpleProperty&gt;

     * &lt;ns:UnqualifiedStructProperty&gt; (If no rdf:resource qualifier)
     * &lt;rdf:Description&gt;
     * ... Fields, same forms as top level properties
     * &lt;/rdf:Description&gt;
     * &lt;/ns:UnqualifiedStructProperty&gt;

     * &lt;ns:ResourceStructProperty rdf:resource=&quot;URI&quot;
     * ... Fields as attributes
     * &gt;

     * &lt;ns:UnqualifiedArrayProperty&gt;
     * &lt;rdf:Bag&gt; or Seq or Alt
     * ... Array items as rdf:li elements, same forms as top level properties
     * &lt;/rdf:Bag&gt;
     * &lt;/ns:UnqualifiedArrayProperty&gt;

     * &lt;ns:QualifiedProperty&gt;
     * &lt;rdf:Description&gt;
     * &lt;rdf:value&gt; ... Property &quot;value&quot; following the unqualified
     * forms ... &lt;/rdf:value&gt;
     * ... Qualifiers looking like named struct fields
     * &lt;/rdf:Description&gt;
     * &lt;/ns:QualifiedProperty&gt;
     *

     *

     * @param node the property node
     * *
     * @param emitAsRDFValue property shall be rendered as attribute rather than tag
     * *
     * @param useCanonicalRDF use canonical form with inner description tag or
     * * 		  the compact form with rdf:ParseType=&quot;resource&quot; attribute.
     * *
     * @param indent the current indent level
     * *
     * @throws IOException Forwards all writer exceptions.
     * *
     * @throws XMPException If &quot;rdf:resource&quot; and general qualifiers are mixed.
     */
    @Throws(IOException::class, XMPException::class)
    private fun serializeCanonicalRDFProperty(
            node: XMPNode, useCanonicalRDF: Boolean, emitAsRDFValue: Boolean, indent: Int) {
        var indent = indent
        var emitEndTag = true
        var indentEndTag = true

        // Determine the XML element name. Open the start tag with the name and
        // attribute qualifiers.

        var elemName = node.name
        if (emitAsRDFValue) {
            elemName = "rdf:value"
        } else if (XMPConst.ARRAY_ITEM_NAME == elemName) {
            elemName = "rdf:li"
        }

        writeIndent(indent)
        write('<')
        write(elemName)

        var hasGeneralQualifiers = false
        var hasRDFResourceQual = false

        run {
            val it = node.iterateQualifier()
            while (it.hasNext()) {
                val qualifier = it.next() as XMPNode
                if (!RDF_ATTR_QUALIFIER.contains(qualifier.name)) {
                    hasGeneralQualifiers = true
                } else {
                    hasRDFResourceQual = "rdf:resource" == qualifier.name
                    if (!emitAsRDFValue) {
                        write(' ')
                        write(qualifier.name)
                        write("=\"")
                        appendNodeValue(qualifier.value, true)
                        write('"')
                    }
                }
            }
        }

        // Process the property according to the standard patterns.

        if (hasGeneralQualifiers && !emitAsRDFValue) {
            // This node has general, non-attribute, qualifiers. Emit using the
            // qualified property form.
            // ! The value is output by a recursive call ON THE SAME NODE with
            // emitAsRDFValue set.

            if (hasRDFResourceQual) {
                throw XMPException("Can't mix rdf:resource and general qualifiers",
                        XMPError.BADRDF)
            }

            // Change serialization to canonical format with inner rdf:Description-tag
            // depending on option
            if (useCanonicalRDF) {
                write(">")
                writeNewline()

                indent++
                writeIndent(indent)
                write(RDF_STRUCT_START)
                write(">")
            } else {
                write(" rdf:parseType=\"Resource\">")
            }
            writeNewline()

            serializeCanonicalRDFProperty(node, useCanonicalRDF, true, indent + 1)

            val it = node.iterateQualifier()
            while (it.hasNext()) {
                val qualifier = it.next() as XMPNode
                if (!RDF_ATTR_QUALIFIER.contains(qualifier.name)) {
                    serializeCanonicalRDFProperty(qualifier, useCanonicalRDF, false, indent + 1)
                }
            }

            if (useCanonicalRDF) {
                writeIndent(indent)
                write(RDF_STRUCT_END)
                writeNewline()
                indent--
            }
        } else {
            // This node has no general qualifiers. Emit using an unqualified form.

            if (!node.options.isCompositeProperty) {
                // This is a simple property.

                if (node.options.isURI) {
                    write(" rdf:resource=\"")
                    appendNodeValue(node.value, true)
                    write("\"/>")
                    writeNewline()
                    emitEndTag = false
                } else if (node.value == null || "" == node.value) {
                    write("/>")
                    writeNewline()
                    emitEndTag = false
                } else {
                    write('>')
                    appendNodeValue(node.value, false)
                    indentEndTag = false
                }
            } else if (node.options.isArray) {
                // This is an array.
                write('>')
                writeNewline()
                emitRDFArrayTag(node, true, indent + 1)
                if (node.options.isArrayAltText) {
                    XMPNodeUtils.normalizeLangArray(node)
                }
                val it = node.iterateChildren()
                while (it.hasNext()) {
                    val child = it.next() as XMPNode
                    serializeCanonicalRDFProperty(child, useCanonicalRDF, false, indent + 2)
                }
                emitRDFArrayTag(node, false, indent + 1)


            } else if (!hasRDFResourceQual) {
                // This is a "normal" struct, use the rdf:parseType="Resource" form.
                if (!node.hasChildren()) {
                    // Change serialization to canonical format with inner rdf:Description-tag
                    // if option is set
                    if (useCanonicalRDF) {
                        write(">")
                        writeNewline()
                        writeIndent(indent + 1)
                        write(RDF_EMPTY_STRUCT)
                    } else {
                        write(" rdf:parseType=\"Resource\"/>")
                        emitEndTag = false
                    }
                    writeNewline()
                } else {
                    // Change serialization to canonical format with inner rdf:Description-tag
                    // if option is set
                    if (useCanonicalRDF) {
                        write(">")
                        writeNewline()
                        indent++
                        writeIndent(indent)
                        write(RDF_STRUCT_START)
                        write(">")
                    } else {
                        write(" rdf:parseType=\"Resource\">")
                    }
                    writeNewline()

                    val it = node.iterateChildren()
                    while (it.hasNext()) {
                        val child = it.next() as XMPNode
                        serializeCanonicalRDFProperty(child, useCanonicalRDF, false, indent + 1)
                    }

                    if (useCanonicalRDF) {
                        writeIndent(indent)
                        write(RDF_STRUCT_END)
                        writeNewline()
                        indent--
                    }
                }
            } else {
                // This is a struct with an rdf:resource attribute, use the
                // "empty property element" form.
                val it = node.iterateChildren()
                while (it.hasNext()) {
                    val child = it.next() as XMPNode
                    if (!canBeRDFAttrProp(child)) {
                        throw XMPException("Can't mix rdf:resource and complex fields",
                                XMPError.BADRDF)
                    }
                    writeNewline()
                    writeIndent(indent + 1)
                    write(' ')
                    write(child.name)
                    write("=\"")
                    appendNodeValue(child.value, true)
                    write('"')
                }
                write("/>")
                writeNewline()
                emitEndTag = false
            }
        }

        // Emit the property element end tag.
        if (emitEndTag) {
            if (indentEndTag) {
                writeIndent(indent)
            }
            write("</")
            write(elemName)
            write('>')
            writeNewline()
        }
    }


    /**
     * Writes the array start and end tags.

     * @param arrayNode an array node
     * *
     * @param isStartTag flag if its the start or end tag
     * *
     * @param indent the current indent level
     * *
     * @throws IOException forwards writer exceptions
     */
    @Throws(IOException::class)
    private fun emitRDFArrayTag(arrayNode: XMPNode, isStartTag: Boolean, indent: Int) {
        if (isStartTag || arrayNode.hasChildren()) {
            writeIndent(indent)
            write(if (isStartTag) "<rdf:" else "</rdf:")

            if (arrayNode.options.isArrayAlternate) {
                write("Alt")
            } else if (arrayNode.options.isArrayOrdered) {
                write("Seq")
            } else {
                write("Bag")
            }

            if (isStartTag && !arrayNode.hasChildren()) {
                write("/>")
            } else {
                write(">")
            }

            writeNewline()
        }
    }


    /**
     * Serializes the node value in XML encoding. Its used for tag bodies and
     * attributes. *Note:* The attribute is always limited by quotes,
     * thats why `&amp;apos;` is never serialized. *Note:*
     * Control chars are written unescaped, but if the user uses others than tab, LF
     * and CR the resulting XML will become invalid.

     * @param value the value of the node
     * *
     * @param forAttribute flag if value is an attribute value
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun appendNodeValue(value: String?, forAttribute: Boolean) {
        var value = value
        if (value == null) {
            value = ""
        }
        write(Utils.escapeXML(value, forAttribute, true))
    }


    /**
     * A node can be serialized as RDF-Attribute, if it meets the following conditions:
     *
     *  * is not array item
     *  * don't has qualifier
     *  * is no URI
     *  * is no composite property
     *

     * @param node an XMPNode
     * *
     * @return Returns true if the node serialized as RDF-Attribute
     */
    private fun canBeRDFAttrProp(node: XMPNode): Boolean {
        return !node.hasQualifier() &&
                !node.options.isURI &&
                !node.options.isCompositeProperty &&
                !node.options.containsOneOf(PropertyOptions.SEPARATE_NODE) &&
                XMPConst.ARRAY_ITEM_NAME != node.name
    }


    /**
     * Writes indents and automatically includes the baseindend from the options.
     * @param times number of indents to write
     * *
     * @throws IOException forwards exception
     */
    @Throws(IOException::class)
    private fun writeIndent(times: Int) {
        for (i in options!!.baseIndent + times downTo 1) {
            writer!!.write(options!!.indent)
        }
    }


    /**
     * Writes a char to the output.
     * @param c a char
     * *
     * @throws IOException forwards writer exceptions
     */
    @Throws(IOException::class)
    private fun write(c: Int) {
        writer!!.write(c)
    }


    /**
     * Writes a String to the output.
     * @param str a String
     * *
     * @throws IOException forwards writer exceptions
     */
    @Throws(IOException::class)
    private fun write(str: String) {
        writer!!.write(str)
    }


    /**
     * Writes an amount of chars, mostly spaces
     * @param number number of chars
     * *
     * @param c a char
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeChars(number: Int, c: Char) {
        var number = number
        while (number > 0) {
            writer!!.write(c.toInt())
            number--
        }
    }


    /**
     * Writes a newline according to the options.
     * @throws IOException Forwards exception
     */
    @Throws(IOException::class)
    private fun writeNewline() {
        writer!!.write(options!!.newline)
    }

    companion object {
        /** default padding  */
        private val DEFAULT_PAD = 2048
        /**  */
        private val PACKET_HEADER = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>"
        /** The w/r is missing inbetween  */
        private val PACKET_TRAILER = "<?xpacket end=\""
        /**  */
        private val PACKET_TRAILER2 = "\"?>"
        /**  */
        private val RDF_XMPMETA_START = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\""
        /**  */
        private val RDF_XMPMETA_END = "</x:xmpmeta>"
        /**  */
        private val RDF_RDF_START = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
        /**  */
        private val RDF_RDF_END = "</rdf:RDF>"

        /**  */
        private val RDF_SCHEMA_START = "<rdf:Description rdf:about="
        /**  */
        private val RDF_SCHEMA_END = "</rdf:Description>"
        /**  */
        private val RDF_STRUCT_START = "<rdf:Description"
        /**  */
        private val RDF_STRUCT_END = "</rdf:Description>"
        /**  */
        private val RDF_EMPTY_STRUCT = "<rdf:Description/>"
        /** a set of all rdf attribute qualifier  */
        internal val RDF_ATTR_QUALIFIER: Set<Any> = HashSet(Arrays.asList(*arrayOf(XMPConst.XML_LANG, "rdf:resource", "rdf:ID", "rdf:bagID", "rdf:nodeID")))
    }
}