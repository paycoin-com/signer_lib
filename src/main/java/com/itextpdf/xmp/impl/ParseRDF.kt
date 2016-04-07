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

import java.util.ArrayList

import org.w3c.dom.Attr
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.XMPSchemaRegistry
import com.itextpdf.xmp.options.PropertyOptions


/**
 * Parser for "normal" XML serialisation of RDF.

 * @since   14.07.2006
 */
class ParseRDF : XMPError, XMPConst {
    companion object {
        /**  */
        val RDFTERM_OTHER = 0
        /** Start of coreSyntaxTerms.  */
        val RDFTERM_RDF = 1
        /**  */
        val RDFTERM_ID = 2
        /**  */
        val RDFTERM_ABOUT = 3
        /**  */
        val RDFTERM_PARSE_TYPE = 4
        /**  */
        val RDFTERM_RESOURCE = 5
        /**  */
        val RDFTERM_NODE_ID = 6
        /** End of coreSyntaxTerms  */
        val RDFTERM_DATATYPE = 7
        /** Start of additions for syntax Terms.  */
        val RDFTERM_DESCRIPTION = 8
        /** End of of additions for syntaxTerms.  */
        val RDFTERM_LI = 9
        /** Start of oldTerms.  */
        val RDFTERM_ABOUT_EACH = 10
        /**  */
        val RDFTERM_ABOUT_EACH_PREFIX = 11
        /** End of oldTerms.  */
        val RDFTERM_BAG_ID = 12
        /**  */
        val RDFTERM_FIRST_CORE = RDFTERM_RDF
        /**  */
        val RDFTERM_LAST_CORE = RDFTERM_DATATYPE
        /** ! Yes, the syntax terms include the core terms.  */
        val RDFTERM_FIRST_SYNTAX = RDFTERM_FIRST_CORE
        /**  */
        val RDFTERM_LAST_SYNTAX = RDFTERM_LI
        /**  */
        val RDFTERM_FIRST_OLD = RDFTERM_ABOUT_EACH
        /**  */
        val RDFTERM_LAST_OLD = RDFTERM_BAG_ID

        /** this prefix is used for default namespaces  */
        val DEFAULT_PREFIX = "_dflt"


        /**
         * The main parsing method. The XML tree is walked through from the root node and and XMP tree
         * is created. This is a raw parse, the normalisation of the XMP tree happens outside.

         * @param xmlRoot the XML root node
         * *
         * @return Returns an XMP metadata object (not normalized)
         * *
         * @throws XMPException Occurs if the parsing fails for any reason.
         */
        @Throws(XMPException::class)
        internal fun parse(xmlRoot: Node): XMPMetaImpl {
            val xmp = XMPMetaImpl()
            rdf_RDF(xmp, xmlRoot)
            return xmp
        }


        /**
         * Each of these parsing methods is responsible for recognizing an RDF
         * syntax production and adding the appropriate structure to the XMP tree.
         * They simply return for success, failures will throw an exception.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param rdfRdfNode the top-level xml node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        internal fun rdf_RDF(xmp: XMPMetaImpl, rdfRdfNode: Node) {
            if (rdfRdfNode.hasAttributes()) {
                rdf_NodeElementList(xmp, xmp.root, rdfRdfNode)
            } else {
                throw XMPException("Invalid attributes of rdf:RDF element", XMPError.BADRDF)
            }
        }


        /**
         * 7.2.10 nodeElementList
         * ws* ( nodeElement ws* )*

         * Note: this method is only called from the rdf:RDF-node (top level)
         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param rdfRdfNode the top-level xml node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_NodeElementList(xmp: XMPMetaImpl, xmpParent: XMPNode, rdfRdfNode: Node) {
            for (i in 0..rdfRdfNode.childNodes.length - 1) {
                val child = rdfRdfNode.childNodes.item(i)
                // filter whitespaces (and all text nodes)
                if (!isWhitespaceNode(child)) {
                    rdf_NodeElement(xmp, xmpParent, child, true)
                }
            }
        }


        /**
         * 7.2.5 nodeElementURIs
         * anyURI - ( coreSyntaxTerms | rdf:li | oldTerms )

         * 7.2.11 nodeElement
         * start-element ( URI == nodeElementURIs,
         * attributes == set ( ( idAttr | nodeIdAttr | aboutAttr )?, propertyAttr* ) )
         * propertyEltList
         * end-element()

         * A node element URI is rdf:Description or anything else that is not an RDF
         * term.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_NodeElement(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlNode: Node,
                                    isTopLevel: Boolean) {
            val nodeTerm = getRDFTermKind(xmlNode)
            if (nodeTerm != RDFTERM_DESCRIPTION && nodeTerm != RDFTERM_OTHER) {
                throw XMPException("Node element must be rdf:Description or typed node",
                        XMPError.BADRDF)
            } else if (isTopLevel && nodeTerm == RDFTERM_OTHER) {
                throw XMPException("Top level typed node not allowed", XMPError.BADXMP)
            } else {
                rdf_NodeElementAttrs(xmp, xmpParent, xmlNode, isTopLevel)
                rdf_PropertyElementList(xmp, xmpParent, xmlNode, isTopLevel)
            }

        }


        /**

         * 7.2.7 propertyAttributeURIs
         * anyURI - ( coreSyntaxTerms | rdf:Description | rdf:li | oldTerms )

         * 7.2.11 nodeElement
         * start-element ( URI == nodeElementURIs,
         * attributes == set ( ( idAttr | nodeIdAttr | aboutAttr )?, propertyAttr* ) )
         * propertyEltList
         * end-element()

         * Process the attribute list for an RDF node element. A property attribute URI is
         * anything other than an RDF term. The rdf:ID and rdf:nodeID attributes are simply ignored,
         * as are rdf:about attributes on inner nodes.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_NodeElementAttrs(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlNode: Node,
                                         isTopLevel: Boolean) {
            // Used to detect attributes that are mutually exclusive.
            var exclusiveAttrs = 0

            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)

                // quick hack, ns declarations do not appear in C++
                // ignore "ID" without namespace
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue
                }

                val attrTerm = getRDFTermKind(attribute)

                when (attrTerm) {
                    RDFTERM_ID, RDFTERM_NODE_ID, RDFTERM_ABOUT -> {
                        if (exclusiveAttrs > 0) {
                            throw XMPException("Mutally exclusive about, ID, nodeID attributes",
                                    XMPError.BADRDF)
                        }

                        exclusiveAttrs++

                        if (isTopLevel && attrTerm == RDFTERM_ABOUT) {
                            // This is the rdf:about attribute on a top level node. Set
                            // the XMP tree name if
                            // it doesn't have a name yet. Make sure this name matches
                            // the XMP tree name.
                            if (xmpParent.name != null && xmpParent.name.length > 0) {
                                if (xmpParent.name != attribute.nodeValue) {
                                    throw XMPException("Mismatched top level rdf:about values",
                                            XMPError.BADXMP)
                                }
                            } else {
                                xmpParent.name = attribute.nodeValue
                            }
                        }
                    }

                    RDFTERM_OTHER -> addChildNode(xmp, xmpParent, attribute, attribute.nodeValue, isTopLevel)

                    else -> throw XMPException("Invalid nodeElement attribute", XMPError.BADRDF)
                }

            }
        }


        /**
         * 7.2.13 propertyEltList
         * ws* ( propertyElt ws* )*

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlParent the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_PropertyElementList(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlParent: Node,
                                            isTopLevel: Boolean) {
            for (i in 0..xmlParent.childNodes.length - 1) {
                val currChild = xmlParent.childNodes.item(i)
                if (isWhitespaceNode(currChild)) {
                    continue
                } else if (currChild.nodeType != Node.ELEMENT_NODE) {
                    throw XMPException("Expected property element node not found", XMPError.BADRDF)
                } else {
                    rdf_PropertyElement(xmp, xmpParent, currChild, isTopLevel)
                }
            }
        }


        /**
         * 7.2.14 propertyElt

         * resourcePropertyElt | literalPropertyElt | parseTypeLiteralPropertyElt |
         * parseTypeResourcePropertyElt | parseTypeCollectionPropertyElt |
         * parseTypeOtherPropertyElt | emptyPropertyElt

         * 7.2.15 resourcePropertyElt
         * start-element ( URI == propertyElementURIs, attributes == set ( idAttr? ) )
         * ws* nodeElement ws*
         * end-element()

         * 7.2.16 literalPropertyElt
         * start-element (
         * URI == propertyElementURIs, attributes == set ( idAttr?, datatypeAttr?) )
         * text()
         * end-element()

         * 7.2.17 parseTypeLiteralPropertyElt
         * start-element (
         * URI == propertyElementURIs, attributes == set ( idAttr?, parseLiteral ) )
         * literal
         * end-element()

         * 7.2.18 parseTypeResourcePropertyElt
         * start-element (
         * URI == propertyElementURIs, attributes == set ( idAttr?, parseResource ) )
         * propertyEltList
         * end-element()

         * 7.2.19 parseTypeCollectionPropertyElt
         * start-element (
         * URI == propertyElementURIs, attributes == set ( idAttr?, parseCollection ) )
         * nodeElementList
         * end-element()

         * 7.2.20 parseTypeOtherPropertyElt
         * start-element ( URI == propertyElementURIs, attributes == set ( idAttr?, parseOther ) )
         * propertyEltList
         * end-element()

         * 7.2.21 emptyPropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set ( idAttr?, ( resourceAttr | nodeIdAttr )?, propertyAttr* ) )
         * end-element()

         * The various property element forms are not distinguished by the XML element name,
         * but by their attributes for the most part. The exceptions are resourcePropertyElt and
         * literalPropertyElt. They are distinguished by their XML element content.

         * NOTE: The RDF syntax does not explicitly include the xml:lang attribute although it can
         * appear in many of these. We have to allow for it in the attibute counts below.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_PropertyElement(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlNode: Node,
                                        isTopLevel: Boolean) {
            val nodeTerm = getRDFTermKind(xmlNode)
            if (!isPropertyElementName(nodeTerm)) {
                throw XMPException("Invalid property element name", XMPError.BADRDF)
            }

            // remove the namespace-definitions from the list
            val attributes = xmlNode.attributes
            var nsAttrs: MutableList<Any>? = null
            for (i in 0..attributes.length - 1) {
                val attribute = attributes.item(i)
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    if (nsAttrs == null) {
                        nsAttrs = ArrayList()
                    }
                    nsAttrs.add(attribute.nodeName)
                }
            }
            if (nsAttrs != null) {
                val it = nsAttrs.iterator()
                while (it.hasNext()) {
                    val ns = it.next() as String
                    attributes.removeNamedItem(ns)
                }
            }


            if (attributes.length > 3) {
                // Only an emptyPropertyElt can have more than 3 attributes.
                rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
            } else {
                // Look through the attributes for one that isn't rdf:ID or xml:lang, 
                // it will usually tell what we should be dealing with. 
                // The called routines must verify their specific syntax!

                for (i in 0..attributes.length - 1) {
                    val attribute = attributes.item(i)
                    val attrLocal = attribute.localName
                    val attrNS = attribute.namespaceURI
                    val attrValue = attribute.nodeValue
                    if (!(XMPConst.XML_LANG == attribute.nodeName && !("ID" == attrLocal && XMPConst.NS_RDF == attrNS))) {
                        if ("datatype" == attrLocal && XMPConst.NS_RDF == attrNS) {
                            rdf_LiteralPropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                        } else if (!("parseType" == attrLocal && XMPConst.NS_RDF == attrNS)) {
                            rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                        } else if ("Literal" == attrValue) {
                            rdf_ParseTypeLiteralPropertyElement()
                        } else if ("Resource" == attrValue) {
                            rdf_ParseTypeResourcePropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                        } else if ("Collection" == attrValue) {
                            rdf_ParseTypeCollectionPropertyElement()
                        } else {
                            rdf_ParseTypeOtherPropertyElement()
                        }

                        return
                    }
                }

                // Only rdf:ID and xml:lang, could be a resourcePropertyElt, a literalPropertyElt, 
                // or an emptyPropertyElt. Look at the child XML nodes to decide which.

                if (xmlNode.hasChildNodes()) {
                    for (i in 0..xmlNode.childNodes.length - 1) {
                        val currChild = xmlNode.childNodes.item(i)
                        if (currChild.nodeType != Node.TEXT_NODE) {
                            rdf_ResourcePropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                            return
                        }
                    }

                    rdf_LiteralPropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                } else {
                    rdf_EmptyPropertyElement(xmp, xmpParent, xmlNode, isTopLevel)
                }
            }
        }


        /**
         * 7.2.15 resourcePropertyElt
         * start-element ( URI == propertyElementURIs, attributes == set ( idAttr? ) )
         * ws* nodeElement ws*
         * end-element()

         * This handles structs using an rdf:Description node,
         * arrays using rdf:Bag/Seq/Alt, and typedNodes. It also catches and cleans up qualified
         * properties written with rdf:Description and rdf:value.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_ResourcePropertyElement(xmp: XMPMetaImpl, xmpParent: XMPNode,
                                                xmlNode: Node, isTopLevel: Boolean) {
            if (isTopLevel && "iX:changes" == xmlNode.nodeName) {
                // Strip old "punchcard" chaff which has on the prefix "iX:".
                return
            }

            val newCompound = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel)

            // walk through the attributes
            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue
                }

                val attrLocal = attribute.localName
                val attrNS = attribute.namespaceURI
                if (XMPConst.XML_LANG == attribute.nodeName) {
                    addQualifierNode(newCompound, XMPConst.XML_LANG, attribute.nodeValue)
                } else if ("ID" == attrLocal && XMPConst.NS_RDF == attrNS) {
                    continue    // Ignore all rdf:ID attributes.
                } else {
                    throw XMPException(
                            "Invalid attribute for resource property element", XMPError.BADRDF)
                }
            }

            // walk through the children

            var currChild: Node? = null
            var found = false
            var i: Int
            i = 0
            while (i < xmlNode.childNodes.length) {
                currChild = xmlNode.childNodes.item(i)
                if (!isWhitespaceNode(currChild)) {
                    if (currChild!!.nodeType == Node.ELEMENT_NODE && !found) {
                        val isRDF = XMPConst.NS_RDF == currChild.namespaceURI
                        val childLocal = currChild.localName

                        if (isRDF && "Bag" == childLocal) {
                            newCompound.options.isArray = true
                        } else if (isRDF && "Seq" == childLocal) {
                            newCompound.options.setArray(true).isArrayOrdered = true
                        } else if (isRDF && "Alt" == childLocal) {
                            newCompound.options.setArray(true).setArrayOrdered(true).isArrayAlternate = true
                        } else {
                            newCompound.options.isStruct = true
                            if (!isRDF && "Description" != childLocal) {
                                var typeName: String? = currChild.namespaceURI ?: throw XMPException(
                                        "All XML elements must be in a namespace", XMPError.BADXMP)
                                typeName += ':' + childLocal
                                addQualifierNode(newCompound, "rdf:type", typeName)
                            }
                        }

                        rdf_NodeElement(xmp, newCompound, currChild, false)

                        if (newCompound.hasValueChild) {
                            fixupQualifiedNode(newCompound)
                        } else if (newCompound.options.isArrayAlternate) {
                            XMPNodeUtils.detectAltText(newCompound)
                        }

                        found = true
                    } else if (found) {
                        // found second child element
                        throw XMPException(
                                "Invalid child of resource property element", XMPError.BADRDF)
                    } else {
                        throw XMPException(
                                "Children of resource property element must be XML elements", XMPError.BADRDF)
                    }
                }
                i++
            }

            if (!found) {
                // didn't found any child elements
                throw XMPException("Missing child of resource property element", XMPError.BADRDF)
            }
        }


        /**
         * 7.2.16 literalPropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set ( idAttr?, datatypeAttr?) )
         * text()
         * end-element()

         * Add a leaf node with the text value and qualifiers for the attributes.
         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_LiteralPropertyElement(xmp: XMPMetaImpl, xmpParent: XMPNode,
                                               xmlNode: Node, isTopLevel: Boolean) {
            val newChild = addChildNode(xmp, xmpParent, xmlNode, null, isTopLevel)

            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue
                }

                val attrNS = attribute.namespaceURI
                val attrLocal = attribute.localName
                if (XMPConst.XML_LANG == attribute.nodeName) {
                    addQualifierNode(newChild, XMPConst.XML_LANG, attribute.nodeValue)
                } else if (XMPConst.NS_RDF == attrNS && ("ID" == attrLocal || "datatype" == attrLocal)) {
                    continue    // Ignore all rdf:ID and rdf:datatype attributes.
                } else {
                    throw XMPException(
                            "Invalid attribute for literal property element", XMPError.BADRDF)
                }
            }
            var textValue = ""
            for (i in 0..xmlNode.childNodes.length - 1) {
                val child = xmlNode.childNodes.item(i)
                if (child.nodeType == Node.TEXT_NODE) {
                    textValue += child.nodeValue
                } else {
                    throw XMPException("Invalid child of literal property element", XMPError.BADRDF)
                }
            }
            newChild.value = textValue
        }


        /**
         * 7.2.17 parseTypeLiteralPropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set ( idAttr?, parseLiteral ) )
         * literal
         * end-element()

         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_ParseTypeLiteralPropertyElement() {
            throw XMPException("ParseTypeLiteral property element not allowed", XMPError.BADXMP)
        }


        /**
         * 7.2.18 parseTypeResourcePropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set ( idAttr?, parseResource ) )
         * propertyEltList
         * end-element()

         * Add a new struct node with a qualifier for the possible rdf:ID attribute.
         * Then process the XML child nodes to get the struct fields.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_ParseTypeResourcePropertyElement(xmp: XMPMetaImpl, xmpParent: XMPNode,
                                                         xmlNode: Node, isTopLevel: Boolean) {
            val newStruct = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel)

            newStruct.options.isStruct = true

            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue
                }

                val attrLocal = attribute.localName
                val attrNS = attribute.namespaceURI
                if (XMPConst.XML_LANG == attribute.nodeName) {
                    addQualifierNode(newStruct, XMPConst.XML_LANG, attribute.nodeValue)
                } else if (XMPConst.NS_RDF == attrNS && ("ID" == attrLocal || "parseType" == attrLocal)) {
                    continue    // The caller ensured the value is "Resource".
                    // Ignore all rdf:ID attributes.
                } else {
                    throw XMPException("Invalid attribute for ParseTypeResource property element",
                            XMPError.BADRDF)
                }
            }

            rdf_PropertyElementList(xmp, newStruct, xmlNode, false)

            if (newStruct.hasValueChild) {
                fixupQualifiedNode(newStruct)
            }
        }


        /**
         * 7.2.19 parseTypeCollectionPropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set ( idAttr?, parseCollection ) )
         * nodeElementList
         * end-element()

         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_ParseTypeCollectionPropertyElement() {
            throw XMPException("ParseTypeCollection property element not allowed", XMPError.BADXMP)
        }


        /**
         * 7.2.20 parseTypeOtherPropertyElt
         * start-element ( URI == propertyElementURIs, attributes == set ( idAttr?, parseOther ) )
         * propertyEltList
         * end-element()

         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_ParseTypeOtherPropertyElement() {
            throw XMPException("ParseTypeOther property element not allowed", XMPError.BADXMP)
        }


        /**
         * 7.2.21 emptyPropertyElt
         * start-element ( URI == propertyElementURIs,
         * attributes == set (
         * idAttr?, ( resourceAttr | nodeIdAttr )?, propertyAttr* ) )
         * end-element()

         *
         *
         *
         *

         * An emptyPropertyElt is an element with no contained content, just a possibly empty set of
         * attributes. An emptyPropertyElt can represent three special cases of simple XMP properties: a
         * simple property with an empty value (ns:Prop1), a simple property whose value is a URI
         * (ns:Prop2), or a simple property with simple qualifiers (ns:Prop3).
         * An emptyPropertyElt can also represent an XMP struct whose fields are all simple and
         * unqualified (ns:Prop4).

         * It is an error to use both rdf:value and rdf:resource - that can lead to invalid  RDF in the
         * verbose form written using a literalPropertyElt.

         * The XMP mapping for an emptyPropertyElt is a bit different from generic RDF, partly for
         * design reasons and partly for historical reasons. The XMP mapping rules are:
         *
         *  1.  If there is an rdf:value attribute then this is a simple property
         * with a text value.
         * All other attributes are qualifiers.
         *  1.  If there is an rdf:resource attribute then this is a simple property
         * with a URI value.
         * All other attributes are qualifiers.
         *  1.  If there are no attributes other than xml:lang, rdf:ID, or rdf:nodeID
         * then this is a simple
         * property with an empty value.
         *  1.  Otherwise this is a struct, the attributes other than xml:lang, rdf:ID,
         * or rdf:nodeID are fields.
         *

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun rdf_EmptyPropertyElement(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlNode: Node,
                                             isTopLevel: Boolean) {
            var hasPropertyAttrs = false
            var hasResourceAttr = false
            var hasNodeIDAttr = false
            var hasValueAttr = false

            var valueNode: Node? = null    // ! Can come from rdf:value or rdf:resource.

            if (xmlNode.hasChildNodes()) {
                throw XMPException(
                        "Nested content not allowed with rdf:resource or property attributes",
                        XMPError.BADRDF)
            }

            // First figure out what XMP this maps to and remember the XML node for a simple value.
            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)
                if ("xmlns" == attribute.prefix || attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue
                }

                val attrTerm = getRDFTermKind(attribute)

                when (attrTerm) {
                    RDFTERM_ID -> {
                    }

                    RDFTERM_RESOURCE -> {
                        if (hasNodeIDAttr) {
                            throw XMPException(
                                    "Empty property element can't have both rdf:resource and rdf:nodeID",
                                    XMPError.BADRDF)
                        } else if (hasValueAttr) {
                            throw XMPException(
                                    "Empty property element can't have both rdf:value and rdf:resource",
                                    XMPError.BADXMP)
                        }

                        hasResourceAttr = true
                        if (!hasValueAttr) {
                            valueNode = attribute
                        }
                    }

                    RDFTERM_NODE_ID -> {
                        if (hasResourceAttr) {
                            throw XMPException(
                                    "Empty property element can't have both rdf:resource and rdf:nodeID",
                                    XMPError.BADRDF)
                        }
                        hasNodeIDAttr = true
                    }

                    RDFTERM_OTHER -> if ("value" == attribute.localName && XMPConst.NS_RDF == attribute.namespaceURI) {
                        if (hasResourceAttr) {
                            throw XMPException(
                                    "Empty property element can't have both rdf:value and rdf:resource",
                                    XMPError.BADXMP)
                        }
                        hasValueAttr = true
                        valueNode = attribute
                    } else if (XMPConst.XML_LANG != attribute.nodeName) {
                        hasPropertyAttrs = true
                    }

                    else -> throw XMPException("Unrecognized attribute of empty property element",
                            XMPError.BADRDF)
                }// Nothing to do.
            }

            // Create the right kind of child node and visit the attributes again 
            // to add the fields or qualifiers.
            // ! Because of implementation vagaries, 
            //   the xmpParent is the tree root for top level properties.
            // ! The schema is found, created if necessary, by addChildNode.

            val childNode = addChildNode(xmp, xmpParent, xmlNode, "", isTopLevel)
            var childIsStruct = false

            if (hasValueAttr || hasResourceAttr) {
                childNode.value = if (valueNode != null) valueNode.nodeValue else ""
                if (!hasValueAttr) {
                    // ! Might have both rdf:value and rdf:resource.
                    childNode.options.isURI = true
                }
            } else if (hasPropertyAttrs) {
                childNode.options.isStruct = true
                childIsStruct = true
            }

            for (i in 0..xmlNode.attributes.length - 1) {
                val attribute = xmlNode.attributes.item(i)
                if (attribute === valueNode ||
                        "xmlns" == attribute.prefix ||
                        attribute.prefix == null && "xmlns" == attribute.nodeName) {
                    continue    // Skip the rdf:value or rdf:resource attribute holding the value.
                }

                val attrTerm = getRDFTermKind(attribute)

                when (attrTerm) {
                    RDFTERM_ID, RDFTERM_NODE_ID -> {
                    }

                    RDFTERM_RESOURCE -> addQualifierNode(childNode, "rdf:resource", attribute.nodeValue)

                    RDFTERM_OTHER -> if (!childIsStruct) {
                        addQualifierNode(
                                childNode, attribute.nodeName, attribute.nodeValue)
                    } else if (XMPConst.XML_LANG == attribute.nodeName) {
                        addQualifierNode(childNode, XMPConst.XML_LANG, attribute.nodeValue)
                    } else {
                        addChildNode(xmp, childNode, attribute, attribute.nodeValue, false)
                    }

                    else -> throw XMPException("Unrecognized attribute of empty property element",
                            XMPError.BADRDF)
                }// Ignore all rdf:ID and rdf:nodeID attributes.

            }
        }


        /**
         * Adds a child node.

         * @param xmp the xmp metadata object that is generated
         * *
         * @param xmpParent the parent xmp node
         * *
         * @param xmlNode the currently processed XML node
         * *
         * @param value Node value
         * *
         * @param isTopLevel Flag if the node is a top-level node
         * *
         * @return Returns the newly created child node.
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun addChildNode(xmp: XMPMetaImpl, xmpParent: XMPNode, xmlNode: Node,
                                 value: String?, isTopLevel: Boolean): XMPNode {
            var xmpParent = xmpParent
            val registry = XMPMetaFactory.getSchemaRegistry()
            var namespace: String? = xmlNode.namespaceURI
            val childName: String
            if (namespace != null) {
                if (XMPConst.NS_DC_DEPRECATED == namespace) {
                    // Fix a legacy DC namespace
                    namespace = XMPConst.NS_DC
                }

                var prefix: String? = registry.getNamespacePrefix(namespace)
                if (prefix == null) {
                    prefix = if (xmlNode.prefix != null) xmlNode.prefix else DEFAULT_PREFIX
                    prefix = registry.registerNamespace(namespace, prefix)
                }
                childName = prefix!! + xmlNode.localName
            } else {
                throw XMPException(
                        "XML namespace required for all elements and attributes", XMPError.BADRDF)
            }


            // create schema node if not already there
            val childOptions = PropertyOptions()
            var isAlias = false
            if (isTopLevel) {
                // Lookup the schema node, adjust the XMP parent pointer.
                // Incoming parent must be the tree root.
                val schemaNode = XMPNodeUtils.findSchemaNode(xmp.root, namespace,
                        DEFAULT_PREFIX, true)
                schemaNode.isImplicit = false    // Clear the implicit node bit.
                // need runtime check for proper 32 bit code.
                xmpParent = schemaNode

                // If this is an alias set the alias flag in the node 
                // and the hasAliases flag in the tree.
                if (registry.findAlias(childName) != null) {
                    isAlias = true
                    xmp.root.hasAliases = true
                    schemaNode.hasAliases = true
                }
            }


            // Make sure that this is not a duplicate of a named node.
            val isArrayItem = "rdf:li" == childName
            val isValueNode = "rdf:value" == childName

            // Create XMP node and so some checks
            val newChild = XMPNode(
                    childName, value, childOptions)
            newChild.isAlias = isAlias

            // Add the new child to the XMP parent node, a value node first.
            if (!isValueNode) {
                xmpParent.addChild(newChild)
            } else {
                xmpParent.addChild(1, newChild)
            }


            if (isValueNode) {
                if (isTopLevel || !xmpParent.options.isStruct) {
                    throw XMPException("Misplaced rdf:value element", XMPError.BADRDF)
                }
                xmpParent.hasValueChild = true
            }

            if (isArrayItem) {
                if (!xmpParent.options.isArray) {
                    throw XMPException("Misplaced rdf:li element", XMPError.BADRDF)
                }
                newChild.name = XMPConst.ARRAY_ITEM_NAME
            }

            return newChild
        }


        /**
         * Adds a qualifier node.

         * @param xmpParent the parent xmp node
         * *
         * @param name the name of the qualifier which has to be
         * * 		QName including the **default prefix**
         * *
         * @param value the value of the qualifier
         * *
         * @return Returns the newly created child node.
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun addQualifierNode(xmpParent: XMPNode, name: String, value: String): XMPNode {
            val isLang = XMPConst.XML_LANG == name

            var newQual: XMPNode? = null

            // normalize value of language qualifiers
            newQual = XMPNode(name, if (isLang) Utils.normalizeLangValue(value) else value, null)
            xmpParent.addQualifier(newQual)

            return newQual
        }


        /**
         * The parent is an RDF pseudo-struct containing an rdf:value field. Fix the
         * XMP data model. The rdf:value node must be the first child, the other
         * children are qualifiers. The form, value, and children of the rdf:value
         * node are the real ones. The rdf:value node's qualifiers must be added to
         * the others.

         * @param xmpParent the parent xmp node
         * *
         * @throws XMPException thown on parsing errors
         */
        @Throws(XMPException::class)
        private fun fixupQualifiedNode(xmpParent: XMPNode) {
            assert(xmpParent.options.isStruct && xmpParent.hasChildren())

            val valueNode = xmpParent.getChild(1)
            assert("rdf:value" == valueNode.name)

            // Move the qualifiers on the value node to the parent. 
            // Make sure an xml:lang qualifier stays at the front.
            // Check for duplicate names between the value node's qualifiers and the parent's children. 
            // The parent's children are about to become qualifiers. Check here, between the groups. 
            // Intra-group duplicates are caught by XMPNode#addChild(...).
            if (valueNode.options.hasLanguage) {
                if (xmpParent.options.hasLanguage) {
                    throw XMPException("Redundant xml:lang for rdf:value element",
                            XMPError.BADXMP)
                }
                val langQual = valueNode.getQualifier(1)
                valueNode.removeQualifier(langQual)
                xmpParent.addQualifier(langQual)
            }

            // Start the remaining copy after the xml:lang qualifier.		
            for (i in 1..valueNode.qualifierLength) {
                val qualifier = valueNode.getQualifier(i)
                xmpParent.addQualifier(qualifier)
            }


            // Change the parent's other children into qualifiers. 
            // This loop starts at 1, child 0 is the rdf:value node.
            for (i in 2..xmpParent.childrenLength) {
                val qualifier = xmpParent.getChild(i)
                xmpParent.addQualifier(qualifier)
            }

            // Move the options and value last, other checks need the parent's original options. 
            // Move the value node's children to be the parent's children.
            assert(xmpParent.options.isStruct || xmpParent.hasValueChild)

            xmpParent.hasValueChild = false
            xmpParent.options.isStruct = false
            xmpParent.options.mergeWith(valueNode.options)
            xmpParent.value = valueNode.value

            xmpParent.removeChildren()
            val it = valueNode.iterateChildren()
            while (it.hasNext()) {
                val child = it.next() as XMPNode
                xmpParent.addChild(child)
            }
        }


        /**
         * Checks if the node is a white space.
         * @param node an XML-node
         * *
         * @return Returns whether the node is a whitespace node,
         * * 		i.e. a text node that contains only whitespaces.
         */
        private fun isWhitespaceNode(node: Node): Boolean {
            if (node.nodeType != Node.TEXT_NODE) {
                return false
            }

            val value = node.nodeValue
            for (i in 0..value.length - 1) {
                if (!Character.isWhitespace(value[i])) {
                    return false
                }
            }

            return true
        }


        /**
         * 7.2.6 propertyElementURIs
         * anyURI - ( coreSyntaxTerms | rdf:Description | oldTerms )

         * @param term the term id
         * *
         * @return Return true if the term is a property element name.
         */
        private fun isPropertyElementName(term: Int): Boolean {
            if (term == RDFTERM_DESCRIPTION || isOldTerm(term)) {
                return false
            } else {
                return !isCoreSyntaxTerm(term)
            }
        }


        /**
         * 7.2.4 oldTerms
         * rdf:aboutEach | rdf:aboutEachPrefix | rdf:bagID

         * @param term the term id
         * *
         * @return Returns true if the term is an old term.
         */
        private fun isOldTerm(term: Int): Boolean {
            return RDFTERM_FIRST_OLD <= term && term <= RDFTERM_LAST_OLD
        }


        /**
         * 7.2.2 coreSyntaxTerms
         * rdf:RDF | rdf:ID | rdf:about | rdf:parseType | rdf:resource | rdf:nodeID |
         * rdf:datatype

         * @param term the term id
         * *
         * @return Return true if the term is a core syntax term
         */
        private fun isCoreSyntaxTerm(term: Int): Boolean {
            return RDFTERM_FIRST_CORE <= term && term <= RDFTERM_LAST_CORE
        }


        /**
         * Determines the ID for a certain RDF Term.
         * Arranged to hopefully minimize the parse time for large XMP.

         * @param node an XML node
         * *
         * @return Returns the term ID.
         */
        private fun getRDFTermKind(node: Node): Int {
            val localName = node.localName
            var namespace: String? = node.namespaceURI

            if (namespace == null &&
                    ("about" == localName || "ID" == localName) &&
                    node is Attr &&
                    XMPConst.NS_RDF == node.ownerElement.namespaceURI) {
                namespace = XMPConst.NS_RDF
            }

            if (XMPConst.NS_RDF == namespace) {
                if ("li" == localName) {
                    return RDFTERM_LI
                } else if ("parseType" == localName) {
                    return RDFTERM_PARSE_TYPE
                } else if ("Description" == localName) {
                    return RDFTERM_DESCRIPTION
                } else if ("about" == localName) {
                    return RDFTERM_ABOUT
                } else if ("resource" == localName) {
                    return RDFTERM_RESOURCE
                } else if ("RDF" == localName) {
                    return RDFTERM_RDF
                } else if ("ID" == localName) {
                    return RDFTERM_ID
                } else if ("nodeID" == localName) {
                    return RDFTERM_NODE_ID
                } else if ("datatype" == localName) {
                    return RDFTERM_DATATYPE
                } else if ("aboutEach" == localName) {
                    return RDFTERM_ABOUT_EACH
                } else if ("aboutEachPrefix" == localName) {
                    return RDFTERM_ABOUT_EACH_PREFIX
                } else if ("bagID" == localName) {
                    return RDFTERM_BAG_ID
                }
            }

            return RDFTERM_OTHER
        }
    }
}