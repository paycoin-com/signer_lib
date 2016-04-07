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

import java.util.GregorianCalendar

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPDateTime
import com.itextpdf.xmp.XMPDateTimeFactory
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.XMPUtils
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathSegment
import com.itextpdf.xmp.options.AliasOptions
import com.itextpdf.xmp.options.PropertyOptions


/**
 * Utilities for `XMPNode`.

 * @since   Aug 28, 2006
 */
class XMPNodeUtils
/**
 * Private Constructor
 */
private constructor()// EMPTY
: XMPConst {
    companion object {
        /**  */
        internal val CLT_NO_VALUES = 0
        /**  */
        internal val CLT_SPECIFIC_MATCH = 1
        /**  */
        internal val CLT_SINGLE_GENERIC = 2
        /**  */
        internal val CLT_MULTIPLE_GENERIC = 3
        /**  */
        internal val CLT_XDEFAULT = 4
        /**  */
        internal val CLT_FIRST_ITEM = 5


        /**
         * Find or create a schema node if `createNodes` is false and

         * @param tree the root of the xmp tree.
         * *
         * @param namespaceURI a namespace
         * *
         * @param createNodes a flag indicating if the node shall be created if not found.
         * * 		  *Note:* The namespace must be registered prior to this call.
         * *
         * *
         * @return Returns the schema node if found, `null` otherwise.
         * * 		   Note: If `createNodes` is `true`, it is **always**
         * * 		   returned a valid node.
         * *
         * @throws XMPException An exception is only thrown if an error occurred, not if a
         * *         		node was not found.
         */
        @Throws(XMPException::class)
        internal fun findSchemaNode(tree: XMPNode, namespaceURI: String,
                                    createNodes: Boolean): XMPNode {
            return findSchemaNode(tree, namespaceURI, null, createNodes)
        }


        /**
         * Find or create a schema node if `createNodes` is true.

         * @param tree the root of the xmp tree.
         * *
         * @param namespaceURI a namespace
         * *
         * @param suggestedPrefix If a prefix is suggested, the namespace is allowed to be registered.
         * *
         * @param createNodes a flag indicating if the node shall be created if not found.
         * * 		  *Note:* The namespace must be registered prior to this call.
         * *
         * *
         * @return Returns the schema node if found, `null` otherwise.
         * * 		   Note: If `createNodes` is `true`, it is **always**
         * * 		   returned a valid node.
         * *
         * @throws XMPException An exception is only thrown if an error occurred, not if a
         * *         		node was not found.
         */
        @Throws(XMPException::class)
        internal fun findSchemaNode(tree: XMPNode, namespaceURI: String, suggestedPrefix: String?,
                                    createNodes: Boolean): XMPNode {
            assert(tree.parent == null) // make sure that its the root
            var schemaNode = tree.findChildByName(namespaceURI)

            if (schemaNode == null && createNodes) {
                schemaNode = XMPNode(namespaceURI,
                        PropertyOptions().setSchemaNode(true))
                schemaNode.isImplicit = true

                // only previously registered schema namespaces are allowed in the XMP tree.
                var prefix: String? = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(namespaceURI)
                if (prefix == null) {
                    if (suggestedPrefix != null && suggestedPrefix.length != 0) {
                        prefix = XMPMetaFactory.getSchemaRegistry().registerNamespace(namespaceURI,
                                suggestedPrefix)
                    } else {
                        throw XMPException("Unregistered schema namespace URI",
                                XMPError.BADSCHEMA)
                    }
                }

                schemaNode.value = prefix

                tree.addChild(schemaNode)
            }

            return schemaNode
        }


        /**
         * Find or create a child node under a given parent node. If the parent node is no
         * Returns the found or created child node.

         * @param parent
         * *            the parent node
         * *
         * @param childName
         * *            the node name to find
         * *
         * @param createNodes
         * *            flag, if new nodes shall be created.
         * *
         * @return Returns the found or created node or `null`.
         * *
         * @throws XMPException Thrown if
         */
        @Throws(XMPException::class)
        internal fun findChildNode(parent: XMPNode, childName: String, createNodes: Boolean): XMPNode {
            if (!parent.options.isSchemaNode && !parent.options.isStruct) {
                if (!parent.isImplicit) {
                    throw XMPException("Named children only allowed for schemas and structs",
                            XMPError.BADXPATH)
                } else if (parent.options.isArray) {
                    throw XMPException("Named children not allowed for arrays",
                            XMPError.BADXPATH)
                } else if (createNodes) {
                    parent.options.isStruct = true
                }
            }

            var childNode = parent.findChildByName(childName)

            if (childNode == null && createNodes) {
                val options = PropertyOptions()
                childNode = XMPNode(childName, options)
                childNode.isImplicit = true
                parent.addChild(childNode)
            }

            assert(childNode != null || !createNodes)

            return childNode
        }


        /**
         * Follow an expanded path expression to find or create a node.

         * @param xmpTree the node to begin the search.
         * *
         * @param xpath the complete xpath
         * *
         * @param createNodes flag if nodes shall be created
         * * 			(when called by `setProperty()`)
         * *
         * @param leafOptions the options for the created leaf nodes (only when
         * *			`createNodes == true`).
         * *
         * @return Returns the node if found or created or `null`.
         * *
         * @throws XMPException An exception is only thrown if an error occurred,
         * * 			not if a node was not found.
         */
        @Throws(XMPException::class)
        internal fun findNode(xmpTree: XMPNode, xpath: XMPPath?, createNodes: Boolean,
                              leafOptions: PropertyOptions): XMPNode? {
            // check if xpath is set.
            if (xpath == null || xpath.size() == 0) {
                throw XMPException("Empty XMPPath", XMPError.BADXPATH)
            }

            // Root of implicitly created subtree to possible delete it later. 
            // Valid only if leaf is new.
            var rootImplicitNode: XMPNode? = null
            var currNode: XMPNode? = null

            // resolve schema step
            currNode = findSchemaNode(xmpTree,
                    xpath.getSegment(XMPPath.STEP_SCHEMA).name, createNodes)
            if (currNode == null) {
                return null
            } else if (currNode.isImplicit) {
                currNode.isImplicit = false    // Clear the implicit node bit.
                rootImplicitNode = currNode    // Save the top most implicit node.
            }


            // Now follow the remaining steps of the original XMPPath.
            try {
                for (i in 1..xpath.size() - 1) {
                    currNode = followXPathStep(currNode, xpath.getSegment(i), createNodes)
                    if (currNode == null) {
                        if (createNodes) {
                            // delete implicitly created nodes
                            deleteNode(rootImplicitNode)
                        }
                        return null
                    } else if (currNode.isImplicit) {
                        // clear the implicit node flag
                        currNode.isImplicit = false

                        // if node is an ALIAS (can be only in root step, auto-create array 
                        // when the path has been resolved from a not simple alias type
                        if (i == 1 &&
                                xpath.getSegment(i).isAlias &&
                                xpath.getSegment(i).aliasForm != 0) {
                            currNode.options.setOption(xpath.getSegment(i).aliasForm, true)
                        } else if (i < xpath.size() - 1 &&
                                xpath.getSegment(i).kind == XMPPath.STRUCT_FIELD_STEP &&
                                !currNode.options.isCompositeProperty) {
                            currNode.options.isStruct = true
                        }// "CheckImplicitStruct" in C++

                        if (rootImplicitNode == null) {
                            rootImplicitNode = currNode    // Save the top most implicit node.
                        }
                    }
                }
            } catch (e: XMPException) {
                // if new notes have been created prior to the error, delete them
                if (rootImplicitNode != null) {
                    deleteNode(rootImplicitNode)
                }
                throw e
            }


            if (rootImplicitNode != null) {
                // set options only if a node has been successful created
                currNode!!.options.mergeWith(leafOptions)
                currNode.options = currNode.options
            }

            return currNode
        }


        /**
         * Deletes the the given node and its children from its parent.
         * Takes care about adjusting the flags.
         * @param node the top-most node to delete.
         */
        internal fun deleteNode(node: XMPNode) {
            val parent = node.parent

            if (node.options.isQualifier) {
                // root is qualifier
                parent.removeQualifier(node)
            } else {
                // root is NO qualifier
                parent.removeChild(node)
            }

            // delete empty Schema nodes
            if (!parent.hasChildren() && parent.options.isSchemaNode) {
                parent.parent.removeChild(parent)
            }
        }


        /**
         * This is setting the value of a leaf node.

         * @param node an XMPNode
         * *
         * @param value a value
         */
        internal fun setNodeValue(node: XMPNode, value: Any) {
            val strValue = serializeNodeValue(value)
            if (!(node.options.isQualifier && XMPConst.XML_LANG == node.name)) {
                node.value = strValue
            } else {
                node.value = Utils.normalizeLangValue(strValue)
            }
        }


        /**
         * Verifies the PropertyOptions for consistancy and updates them as needed.
         * If options are `null` they are created with default values.

         * @param options the `PropertyOptions`
         * *
         * @param itemValue the node value to set
         * *
         * @return Returns the updated options.
         * *
         * @throws XMPException If the options are not consistant.
         */
        @Throws(XMPException::class)
        internal fun verifySetOptions(options: PropertyOptions?, itemValue: Any?): PropertyOptions {
            var options = options
            // create empty and fix existing options
            if (options == null) {
                // set default options
                options = PropertyOptions()
            }

            if (options.isArrayAltText) {
                options.isArrayAlternate = true
            }

            if (options.isArrayAlternate) {
                options.isArrayOrdered = true
            }

            if (options.isArrayOrdered) {
                options.isArray = true
            }

            if (options.isCompositeProperty && itemValue != null && itemValue.toString().length > 0) {
                throw XMPException("Structs and arrays can't have values",
                        XMPError.BADOPTIONS)
            }

            options.assertConsistency(options.options)

            return options
        }


        /**
         * Converts the node value to String, apply special conversions for defined
         * types in XMP.

         * @param value
         * *            the node value to set
         * *
         * @return Returns the String representation of the node value.
         */
        internal fun serializeNodeValue(value: Any?): String? {
            val strValue: String?
            if (value == null) {
                strValue = null
            } else if (value is Boolean) {
                strValue = XMPUtils.convertFromBoolean(value.booleanValue())
            } else if (value is Int) {
                strValue = XMPUtils.convertFromInteger(value.toInt())
            } else if (value is Long) {
                strValue = XMPUtils.convertFromLong(value.toLong())
            } else if (value is Double) {
                strValue = XMPUtils.convertFromDouble(value.toDouble())
            } else if (value is XMPDateTime) {
                strValue = XMPUtils.convertFromDate(value as XMPDateTime?)
            } else if (value is GregorianCalendar) {
                val dt = XMPDateTimeFactory.createFromCalendar(value as GregorianCalendar?)
                strValue = XMPUtils.convertFromDate(dt)
            } else if (value is ByteArray) {
                strValue = XMPUtils.encodeBase64(value as ByteArray?)
            } else {
                strValue = value.toString()
            }

            return if (strValue != null) Utils.removeControlChars(strValue) else null
        }


        /**
         * After processing by ExpandXPath, a step can be of these forms:
         *
         *  * qualName - A top level property or struct field.
         *  * [index] - An element of an array.
         *  * [last()] - The last element of an array.
         *  * [qualName="value"] - An element in an array of structs, chosen by a field value.
         *  * [?qualName="value"] - An element in an array, chosen by a qualifier value.
         *  * ?qualName - A general qualifier.
         *
         * Find the appropriate child node, resolving aliases, and optionally creating nodes.

         * @param parentNode the node to start to start from
         * *
         * @param nextStep the xpath segment
         * *
         * @param createNodes
         * *
         * @return returns the found or created XMPPath node
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun followXPathStep(
                parentNode: XMPNode,
                nextStep: XMPPathSegment,
                createNodes: Boolean): XMPNode {
            var nextNode: XMPNode? = null
            var index = 0
            val stepKind = nextStep.kind

            if (stepKind == XMPPath.STRUCT_FIELD_STEP) {
                nextNode = findChildNode(parentNode, nextStep.name, createNodes)
            } else if (stepKind == XMPPath.QUALIFIER_STEP) {
                nextNode = findQualifierNode(
                        parentNode, nextStep.name!!.substring(1), createNodes)
            } else {
                // This is an array indexing step. First get the index, then get the node.

                if (!parentNode.options.isArray) {
                    throw XMPException("Indexing applied to non-array", XMPError.BADXPATH)
                }

                if (stepKind == XMPPath.ARRAY_INDEX_STEP) {
                    index = findIndexedItem(parentNode, nextStep.name, createNodes)
                } else if (stepKind == XMPPath.ARRAY_LAST_STEP) {
                    index = parentNode.childrenLength
                } else if (stepKind == XMPPath.FIELD_SELECTOR_STEP) {
                    val result = Utils.splitNameAndValue(nextStep.name)
                    val fieldName = result[0]
                    val fieldValue = result[1]
                    index = lookupFieldSelector(parentNode, fieldName, fieldValue)
                } else if (stepKind == XMPPath.QUAL_SELECTOR_STEP) {
                    val result = Utils.splitNameAndValue(nextStep.name)
                    val qualName = result[0]
                    val qualValue = result[1]
                    index = lookupQualSelector(
                            parentNode, qualName, qualValue, nextStep.aliasForm)
                } else {
                    throw XMPException("Unknown array indexing step in FollowXPathStep",
                            XMPError.INTERNALFAILURE)
                }

                if (1 <= index && index <= parentNode.childrenLength) {
                    nextNode = parentNode.getChild(index)
                }
            }

            return nextNode
        }


        /**
         * Find or create a qualifier node under a given parent node. Returns a pointer to the
         * qualifier node, and optionally an iterator for the node's position in
         * the parent's vector of qualifiers. The iterator is unchanged if no qualifier node (null)
         * is returned.
         * *Note:* On entry, the qualName parameter must not have the leading '?' from the
         * XMPPath step.

         * @param parent the parent XMPNode
         * *
         * @param qualName the qualifier name
         * *
         * @param createNodes flag if nodes shall be created
         * *
         * @return Returns the qualifier node if found or created, `null` otherwise.
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun findQualifierNode(parent: XMPNode, qualName: String, createNodes: Boolean): XMPNode {
            assert(!qualName.startsWith("?"))

            var qualNode = parent.findQualifierByName(qualName)

            if (qualNode == null && createNodes) {
                qualNode = XMPNode(qualName, null)
                qualNode.isImplicit = true

                parent.addQualifier(qualNode)
            }

            return qualNode
        }


        /**
         * @param arrayNode an array node
         * *
         * @param segment the segment containing the array index
         * *
         * @param createNodes flag if new nodes are allowed to be created.
         * *
         * @return Returns the index or index = -1 if not found
         * *
         * @throws XMPException Throws Exceptions
         */
        @Throws(XMPException::class)
        private fun findIndexedItem(arrayNode: XMPNode, segment: String, createNodes: Boolean): Int {
            var segment = segment
            var index = 0

            try {
                segment = segment.substring(1, segment.length - 1)
                index = Integer.parseInt(segment)
                if (index < 1) {
                    throw XMPException("Array index must be larger than zero",
                            XMPError.BADXPATH)
                }
            } catch (e: NumberFormatException) {
                throw XMPException("Array index not digits.", XMPError.BADXPATH)
            }

            if (createNodes && index == arrayNode.childrenLength + 1) {
                // Append a new last + 1 node.
                val newItem = XMPNode(XMPConst.ARRAY_ITEM_NAME, null)
                newItem.isImplicit = true
                arrayNode.addChild(newItem)
            }

            return index
        }


        /**
         * Searches for a field selector in a node:
         * [fieldName="value] - an element in an array of structs, chosen by a field value.
         * No implicit nodes are created by field selectors.

         * @param arrayNode
         * *
         * @param fieldName
         * *
         * @param fieldValue
         * *
         * @return Returns the index of the field if found, otherwise -1.
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun lookupFieldSelector(arrayNode: XMPNode, fieldName: String, fieldValue: String): Int {
            var result = -1

            var index = 1
            while (index <= arrayNode.childrenLength && result < 0) {
                val currItem = arrayNode.getChild(index)

                if (!currItem.options.isStruct) {
                    throw XMPException("Field selector must be used on array of struct",
                            XMPError.BADXPATH)
                }

                for (f in 1..currItem.childrenLength) {
                    val currField = currItem.getChild(f)
                    if (fieldName != currField.name) {
                        continue
                    }
                    if (fieldValue == currField.value) {
                        result = index
                        break
                    }
                }
                index++
            }

            return result
        }


        /**
         * Searches for a qualifier selector in a node:
         * [?qualName="value"] - an element in an array, chosen by a qualifier value.
         * No implicit nodes are created for qualifier selectors,
         * except for an alias to an x-default item.

         * @param arrayNode an array node
         * *
         * @param qualName the qualifier name
         * *
         * @param qualValue the qualifier value
         * *
         * @param aliasForm in case the qual selector results from an alias,
         * * 		  an x-default node is created if there has not been one.
         * *
         * @return Returns the index of th
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun lookupQualSelector(arrayNode: XMPNode, qualName: String,
                                       qualValue: String, aliasForm: Int): Int {
            var qualValue = qualValue
            if (XMPConst.XML_LANG == qualName) {
                qualValue = Utils.normalizeLangValue(qualValue)
                val index = XMPNodeUtils.lookupLanguageItem(arrayNode, qualValue)
                if (index < 0 && aliasForm and AliasOptions.PROP_ARRAY_ALT_TEXT > 0) {
                    val langNode = XMPNode(XMPConst.ARRAY_ITEM_NAME, null)
                    val xdefault = XMPNode(XMPConst.XML_LANG, XMPConst.X_DEFAULT, null)
                    langNode.addQualifier(xdefault)
                    arrayNode.addChild(1, langNode)
                    return 1
                } else {
                    return index
                }
            } else {
                for (index in 1..arrayNode.childrenLength - 1) {
                    val currItem = arrayNode.getChild(index)

                    val it = currItem.iterateQualifier()
                    while (it.hasNext()) {
                        val qualifier = it.next() as XMPNode
                        if (qualName == qualifier.name && qualValue == qualifier.value) {
                            return index
                        }
                    }
                }
                return -1
            }
        }


        /**
         * Make sure the x-default item is first. Touch up &quot;single value&quot;
         * arrays that have a default plus one real language. This case should have
         * the same value for both items. Older Adobe apps were hardwired to only
         * use the &quot;x-default&quot; item, so we copy that value to the other
         * item.

         * @param arrayNode
         * *            an alt text array node
         */
        internal fun normalizeLangArray(arrayNode: XMPNode) {
            if (!arrayNode.options.isArrayAltText) {
                return
            }

            // check if node with x-default qual is first place
            for (i in 2..arrayNode.childrenLength) {
                val child = arrayNode.getChild(i)
                if (child.hasQualifier() && XMPConst.X_DEFAULT == child.getQualifier(1).value) {
                    // move node to first place
                    try {
                        arrayNode.removeChild(i)
                        arrayNode.addChild(1, child)
                    } catch (e: XMPException) {
                        // cannot occur, because same child is removed before
                        assert(false)
                    }

                    if (i == 2) {
                        arrayNode.getChild(2).value = child.value
                    }
                    break
                }
            }
        }


        /**
         * See if an array is an alt-text array. If so, make sure the x-default item
         * is first.

         * @param arrayNode
         * *            the array node to check if its an alt-text array
         */
        internal fun detectAltText(arrayNode: XMPNode) {
            if (arrayNode.options.isArrayAlternate && arrayNode.hasChildren()) {
                var isAltText = false
                val it = arrayNode.iterateChildren()
                while (it.hasNext()) {
                    val child = it.next() as XMPNode
                    if (child.options.hasLanguage) {
                        isAltText = true
                        break
                    }
                }

                if (isAltText) {
                    arrayNode.options.isArrayAltText = true
                    normalizeLangArray(arrayNode)
                }
            }
        }


        /**
         * Appends a language item to an alt text array.

         * @param arrayNode the language array
         * *
         * @param itemLang the language of the item
         * *
         * @param itemValue the content of the item
         * *
         * @throws XMPException Thrown if a duplicate property is added
         */
        @Throws(XMPException::class)
        internal fun appendLangItem(arrayNode: XMPNode, itemLang: String, itemValue: String) {
            val newItem = XMPNode(XMPConst.ARRAY_ITEM_NAME, itemValue, null)
            val langQual = XMPNode(XMPConst.XML_LANG, itemLang, null)
            newItem.addQualifier(langQual)

            if (XMPConst.X_DEFAULT != langQual.value) {
                arrayNode.addChild(newItem)
            } else {
                arrayNode.addChild(1, newItem)
            }
        }


        /**
         *
         *  1. Look for an exact match with the specific language.
         *  1. If a generic language is given, look for partial matches.
         *  1. Look for an "x-default"-item.
         *  1. Choose the first item.
         *

         * @param arrayNode
         * *            the alt text array node
         * *
         * @param genericLang
         * *            the generic language
         * *
         * @param specificLang
         * *            the specific language
         * *
         * @return Returns the kind of match as an Integer and the found node in an
         * *         array.
         * *
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        internal fun chooseLocalizedText(arrayNode: XMPNode, genericLang: String?, specificLang: String): Array<Any> {
            // See if the array has the right form. Allow empty alt arrays,
            // that is what parsing returns.
            if (!arrayNode.options.isArrayAltText) {
                throw XMPException("Localized text array is not alt-text", XMPError.BADXPATH)
            } else if (!arrayNode.hasChildren()) {
                return arrayOf<Any>(XMPNodeUtils.CLT_NO_VALUES, null)
            }

            var foundGenericMatches = 0
            var resultNode: XMPNode? = null
            var xDefault: XMPNode? = null

            // Look for the first partial match with the generic language.
            val it = arrayNode.iterateChildren()
            while (it.hasNext()) {
                val currItem = it.next() as XMPNode

                // perform some checks on the current item
                if (currItem.options.isCompositeProperty) {
                    throw XMPException("Alt-text array item is not simple", XMPError.BADXPATH)
                } else if (!currItem.hasQualifier() || XMPConst.XML_LANG != currItem.getQualifier(1).name) {
                    throw XMPException("Alt-text array item has no language qualifier",
                            XMPError.BADXPATH)
                }

                val currLang = currItem.getQualifier(1).value

                // Look for an exact match with the specific language.
                if (specificLang == currLang) {
                    return arrayOf(XMPNodeUtils.CLT_SPECIFIC_MATCH, currItem)
                } else if (genericLang != null && currLang.startsWith(genericLang)) {
                    if (resultNode == null) {
                        resultNode = currItem
                    }
                    // ! Don't return/break, need to look for other matches.
                    foundGenericMatches++
                } else if (XMPConst.X_DEFAULT == currLang) {
                    xDefault = currItem
                }
            }

            // evaluate loop
            if (foundGenericMatches == 1) {
                return arrayOf<Any>(XMPNodeUtils.CLT_SINGLE_GENERIC, resultNode)
            } else if (foundGenericMatches > 1) {
                return arrayOf<Any>(XMPNodeUtils.CLT_MULTIPLE_GENERIC, resultNode)
            } else if (xDefault != null) {
                return arrayOf<Any>(XMPNodeUtils.CLT_XDEFAULT, xDefault)
            } else {
                // Everything failed, choose the first item.
                return arrayOf(XMPNodeUtils.CLT_FIRST_ITEM, arrayNode.getChild(1))
            }
        }


        /**
         * Looks for the appropriate language item in a text alternative array.item

         * @param arrayNode
         * *            an array node
         * *
         * @param language
         * *            the requested language
         * *
         * @return Returns the index if the language has been found, -1 otherwise.
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        internal fun lookupLanguageItem(arrayNode: XMPNode, language: String): Int {
            if (!arrayNode.options.isArray) {
                throw XMPException("Language item must be used on array", XMPError.BADXPATH)
            }

            for (index in 1..arrayNode.childrenLength) {
                val child = arrayNode.getChild(index)
                if (!child.hasQualifier() || XMPConst.XML_LANG != child.getQualifier(1).name) {
                    continue
                } else if (language == child.getQualifier(1).value) {
                    return index
                }
            }

            return -1
        }
    }
}
