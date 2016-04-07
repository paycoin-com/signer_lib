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

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.XMPUtils
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathParser
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.properties.XMPAliasInfo


/**
 * @since 11.08.2006
 */
class XMPUtilsImpl
/**
 * Private constructor, as
 */
private constructor()// EMPTY
: XMPConst {
    companion object {
        /**  */
        private val UCK_NORMAL = 0
        /**  */
        private val UCK_SPACE = 1
        /**  */
        private val UCK_COMMA = 2
        /**  */
        private val UCK_SEMICOLON = 3
        /**  */
        private val UCK_QUOTE = 4
        /**  */
        private val UCK_CONTROL = 5


        /**
         * @see XMPUtils.catenateArrayItems
         * @param xmp
         * *            The XMP object containing the array to be catenated.
         * *
         * @param schemaNS
         * *            The schema namespace URI for the array. Must not be null or
         * *            the empty string.
         * *
         * @param arrayName
         * *            The name of the array. May be a general path expression, must
         * *            not be null or the empty string. Each item in the array must
         * *            be a simple string value.
         * *
         * @param separator
         * *            The string to be used to separate the items in the catenated
         * *            string. Defaults to &quot;; &quot;, ASCII semicolon and space
         * *            (U+003B, U+0020).
         * *
         * @param quotes
         * *            The characters to be used as quotes around array items that
         * *            contain a separator. Defaults to &apos;&quot;&apos;
         * *
         * @param allowCommas
         * *            Option flag to control the catenation.
         * *
         * @return Returns the string containing the catenated array items.
         * *
         * @throws XMPException
         * *             Forwards the Exceptions from the metadata processing
         */
        @Throws(XMPException::class)
        fun catenateArrayItems(xmp: XMPMeta, schemaNS: String, arrayName: String,
                               separator: String?, quotes: String?, allowCommas: Boolean): String {
            var separator = separator
            var quotes = quotes
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertArrayName(arrayName)
            ParameterAsserts.assertImplementation(xmp)
            if (separator == null || separator.length == 0) {
                separator = "; "
            }
            if (quotes == null || quotes.length == 0) {
                quotes = "\""
            }

            val xmpImpl = xmp as XMPMetaImpl
            var arrayNode: XMPNode? = null
            var currItem: XMPNode? = null

            // Return an empty result if the array does not exist, 
            // hurl if it isn't the right form.
            val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)
            arrayNode = XMPNodeUtils.findNode(xmpImpl.root, arrayPath, false, null)
            if (arrayNode == null) {
                return ""
            } else if (!arrayNode.options.isArray || arrayNode.options.isArrayAlternate) {
                throw XMPException("Named property must be non-alternate array", XMPError.BADPARAM)
            }

            // Make sure the separator is OK.
            checkSeparator(separator)
            // Make sure the open and close quotes are a legitimate pair.
            val openQuote = quotes[0]
            val closeQuote = checkQuotes(quotes, openQuote)

            // Build the result, quoting the array items, adding separators.
            // Hurl if any item isn't simple.

            val catinatedString = StringBuffer()

            val it = arrayNode.iterateChildren()
            while (it.hasNext()) {
                currItem = it.next() as XMPNode
                if (currItem.options.isCompositeProperty) {
                    throw XMPException("Array items must be simple", XMPError.BADPARAM)
                }
                val str = applyQuotes(currItem.value, openQuote, closeQuote, allowCommas)

                catinatedString.append(str)
                if (it.hasNext()) {
                    catinatedString.append(separator)
                }
            }

            return catinatedString.toString()
        }


        /**
         * see [XMPUtils.separateArrayItems]

         * @param xmp
         * *            The XMP object containing the array to be updated.
         * *
         * @param schemaNS
         * *            The schema namespace URI for the array. Must not be null or
         * *            the empty string.
         * *
         * @param arrayName
         * *            The name of the array. May be a general path expression, must
         * *            not be null or the empty string. Each item in the array must
         * *            be a simple string value.
         * *
         * @param catedStr
         * *            The string to be separated into the array items.
         * *
         * @param arrayOptions
         * *            Option flags to control the separation.
         * *
         * @param preserveCommas
         * *            Flag if commas shall be preserved
         * *
         * *
         * @throws XMPException
         * *             Forwards the Exceptions from the metadata processing
         */
        @Throws(XMPException::class)
        fun separateArrayItems(xmp: XMPMeta, schemaNS: String, arrayName: String,
                               catedStr: String?, arrayOptions: PropertyOptions, preserveCommas: Boolean) {
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertArrayName(arrayName)
            if (catedStr == null) {
                throw XMPException("Parameter must not be null", XMPError.BADPARAM)
            }
            ParameterAsserts.assertImplementation(xmp)
            val xmpImpl = xmp as XMPMetaImpl

            // Keep a zero value, has special meaning below.
            val arrayNode = separateFindCreateArray(schemaNS, arrayName, arrayOptions, xmpImpl)

            // Extract the item values one at a time, until the whole input string is done.
            var itemValue: String
            var itemStart: Int
            var itemEnd: Int
            var nextKind = UCK_NORMAL
            var charKind = UCK_NORMAL
            var ch: Char = 0.toChar()
            var nextChar: Char = 0.toChar()

            itemEnd = 0
            val endPos = catedStr.length
            while (itemEnd < endPos) {
                // Skip any leading spaces and separation characters. Always skip commas here.
                // They can be kept when within a value, but not when alone between values.
                itemStart = itemEnd
                while (itemStart < endPos) {
                    ch = catedStr[itemStart]
                    charKind = classifyCharacter(ch)
                    if (charKind == UCK_NORMAL || charKind == UCK_QUOTE) {
                        break
                    }
                    itemStart++
                }
                if (itemStart >= endPos) {
                    break
                }

                if (charKind != UCK_QUOTE) {
                    // This is not a quoted value. Scan for the end, create an array
                    // item from the substring.
                    itemEnd = itemStart
                    while (itemEnd < endPos) {
                        ch = catedStr[itemEnd]
                        charKind = classifyCharacter(ch)

                        if (charKind == UCK_NORMAL || charKind == UCK_QUOTE ||
                                charKind == UCK_COMMA && preserveCommas) {
                            itemEnd++
                            continue
                        } else if (charKind != UCK_SPACE) {
                            break
                        } else if (itemEnd + 1 < endPos) {
                            ch = catedStr[itemEnd + 1]
                            nextKind = classifyCharacter(ch)
                            if (nextKind == UCK_NORMAL || nextKind == UCK_QUOTE ||
                                    nextKind == UCK_COMMA && preserveCommas) {
                                continue
                            }
                        }

                        // Anything left?
                        break // Have multiple spaces, or a space followed by a
                        itemEnd++
                        // separator.
                    }
                    itemValue = catedStr.substring(itemStart, itemEnd)
                } else {
                    // Accumulate quoted values into a local string, undoubling
                    // internal quotes that
                    // match the surrounding quotes. Do not undouble "unmatching"
                    // quotes.

                    val openQuote = ch
                    val closeQuote = getClosingQuote(openQuote)

                    itemStart++ // Skip the opening quote;
                    itemValue = ""

                    itemEnd = itemStart
                    while (itemEnd < endPos) {
                        ch = catedStr[itemEnd]
                        charKind = classifyCharacter(ch)

                        if (charKind != UCK_QUOTE || !isSurroundingQuote(ch, openQuote, closeQuote)) {
                            // This is not a matching quote, just append it to the
                            // item value.
                            itemValue += ch
                        } else {
                            // This is a "matching" quote. Is it doubled, or the
                            // final closing quote?
                            // Tolerate various edge cases like undoubled opening
                            // (non-closing) quotes,
                            // or end of input.

                            if (itemEnd + 1 < endPos) {
                                nextChar = catedStr[itemEnd + 1]
                                nextKind = classifyCharacter(nextChar)
                            } else {
                                nextKind = UCK_SEMICOLON
                                nextChar = 0x3B.toChar()
                            }

                            if (ch == nextChar) {
                                // This is doubled, copy it and skip the double.
                                itemValue += ch
                                // Loop will add in charSize.
                                itemEnd++
                            } else if (!isClosingingQuote(ch, openQuote, closeQuote)) {
                                // This is an undoubled, non-closing quote, copy it.
                                itemValue += ch
                            } else {
                                // This is an undoubled closing quote, skip it and
                                // exit the loop.
                                itemEnd++
                                break
                            }
                        }
                        itemEnd++
                    }
                }

                // Add the separated item to the array. 
                // Keep a matching old value in case it had separators.
                var foundIndex = -1
                for (oldChild in 1..arrayNode.childrenLength) {
                    if (itemValue == arrayNode.getChild(oldChild).value) {
                        foundIndex = oldChild
                        break
                    }
                }

                var newItem: XMPNode? = null
                if (foundIndex < 0) {
                    newItem = XMPNode(XMPConst.ARRAY_ITEM_NAME, itemValue, null)
                    arrayNode.addChild(newItem)
                }
            }
        }


        /**
         * Utility to find or create the array used by `separateArrayItems()`.
         * @param schemaNS a the namespace fo the array
         * *
         * @param arrayName the name of the array
         * *
         * @param arrayOptions the options for the array if newly created
         * *
         * @param xmp the xmp object
         * *
         * @return Returns the array node.
         * *
         * @throws XMPException Forwards exceptions
         */
        @Throws(XMPException::class)
        private fun separateFindCreateArray(schemaNS: String, arrayName: String,
                                            arrayOptions: PropertyOptions, xmp: XMPMetaImpl): XMPNode {
            var arrayOptions = arrayOptions
            arrayOptions = XMPNodeUtils.verifySetOptions(arrayOptions, null)
            if (!arrayOptions.isOnlyArrayOptions) {
                throw XMPException("Options can only provide array form", XMPError.BADOPTIONS)
            }

            // Find the array node, make sure it is OK. Move the current children
            // aside, to be readded later if kept.
            val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)
            var arrayNode = XMPNodeUtils.findNode(xmp.root, arrayPath, false, null)
            if (arrayNode != null) {
                // The array exists, make sure the form is compatible. Zero
                // arrayForm means take what exists.
                val arrayForm = arrayNode.options
                if (!arrayForm.isArray || arrayForm.isArrayAlternate) {
                    throw XMPException("Named property must be non-alternate array",
                            XMPError.BADXPATH)
                }
                if (arrayOptions.equalArrayTypes(arrayForm)) {
                    throw XMPException("Mismatch of specified and existing array form",
                            XMPError.BADXPATH) // *** Right error?
                }
            } else {
                // The array does not exist, try to create it.
                // don't modify the options handed into the method
                arrayNode = XMPNodeUtils.findNode(xmp.root, arrayPath, true, arrayOptions.setArray(true))
                if (arrayNode == null) {
                    throw XMPException("Failed to create named array", XMPError.BADXPATH)
                }
            }
            return arrayNode
        }


        /**
         * @see XMPUtils.removeProperties
         * @param xmp
         * *            The XMP object containing the properties to be removed.
         * *
         * *
         * @param schemaNS
         * *            Optional schema namespace URI for the properties to be
         * *            removed.
         * *
         * *
         * @param propName
         * *            Optional path expression for the property to be removed.
         * *
         * *
         * @param doAllProperties
         * *            Option flag to control the deletion: do internal properties in
         * *            addition to external properties.
         * *
         * @param includeAliases
         * *            Option flag to control the deletion: Include aliases in the
         * *            "named schema" case above.
         * *
         * @throws XMPException If metadata processing fails
         */
        @Throws(XMPException::class)
        fun removeProperties(xmp: XMPMeta, schemaNS: String?, propName: String?,
                             doAllProperties: Boolean, includeAliases: Boolean) {
            ParameterAsserts.assertImplementation(xmp)
            val xmpImpl = xmp as XMPMetaImpl

            if (propName != null && propName.length > 0) {
                // Remove just the one indicated property. This might be an alias,
                // the named schema might not actually exist. So don't lookup the
                // schema node.

                if (schemaNS == null || schemaNS.length == 0) {
                    throw XMPException("Property name requires schema namespace",
                            XMPError.BADPARAM)
                }

                val expPath = XMPPathParser.expandXPath(schemaNS, propName)

                val propNode = XMPNodeUtils.findNode(xmpImpl.root, expPath, false, null)
                if (propNode != null) {
                    if (doAllProperties || !Utils.isInternalProperty(expPath.getSegment(XMPPath.STEP_SCHEMA).name, expPath.getSegment(XMPPath.STEP_ROOT_PROP).name)) {
                        val parent = propNode.parent
                        parent.removeChild(propNode)
                        if (parent.options.isSchemaNode && !parent.hasChildren()) {
                            // remove empty schema node
                            parent.parent.removeChild(parent)
                        }

                    }
                }
            } else if (schemaNS != null && schemaNS.length > 0) {

                // Remove all properties from the named schema. Optionally include
                // aliases, in which case
                // there might not be an actual schema node.

                // XMP_NodePtrPos schemaPos;
                val schemaNode = XMPNodeUtils.findSchemaNode(xmpImpl.root, schemaNS, false)
                if (schemaNode != null) {
                    if (removeSchemaChildren(schemaNode, doAllProperties)) {
                        xmpImpl.root.removeChild(schemaNode)
                    }
                }

                if (includeAliases) {
                    // We're removing the aliases also. Look them up by their
                    // namespace prefix.
                    // But that takes more code and the extra speed isn't worth it.
                    // Lookup the XMP node
                    // from the alias, to make sure the actual exists.

                    val aliases = XMPMetaFactory.getSchemaRegistry().findAliases(schemaNS)
                    for (i in aliases.indices) {
                        val info = aliases[i]
                        val path = XMPPathParser.expandXPath(info.namespace, info.propName)
                        val actualProp = XMPNodeUtils.findNode(xmpImpl.root, path, false, null)
                        if (actualProp != null) {
                            val parent = actualProp.parent
                            parent.removeChild(actualProp)
                        }
                    }
                }
            } else {
                // Remove all appropriate properties from all schema. In this case
                // we don't have to be
                // concerned with aliases, they are handled implicitly from the
                // actual properties.
                val it = xmpImpl.root.iterateChildren()
                while (it.hasNext()) {
                    val schema = it.next() as XMPNode
                    if (removeSchemaChildren(schema, doAllProperties)) {
                        it.remove()
                    }
                }
            }
        }


        /**
         * @see XMPUtils.appendProperties
         * @param source The source XMP object.
         * *
         * @param destination The destination XMP object.
         * *
         * @param doAllProperties Do internal properties in addition to external properties.
         * *
         * @param replaceOldValues Replace the values of existing properties.
         * *
         * @param deleteEmptyValues Delete destination values if source property is empty.
         * *
         * @throws XMPException Forwards the Exceptions from the metadata processing
         */
        @Throws(XMPException::class)
        fun appendProperties(source: XMPMeta, destination: XMPMeta,
                             doAllProperties: Boolean, replaceOldValues: Boolean, deleteEmptyValues: Boolean) {
            ParameterAsserts.assertImplementation(source)
            ParameterAsserts.assertImplementation(destination)

            val src = source as XMPMetaImpl
            val dest = destination as XMPMetaImpl

            val it = src.root.iterateChildren()
            while (it.hasNext()) {
                val sourceSchema = it.next() as XMPNode

                // Make sure we have a destination schema node
                var destSchema: XMPNode? = XMPNodeUtils.findSchemaNode(dest.root,
                        sourceSchema.name, false)
                var createdSchema = false
                if (destSchema == null) {
                    destSchema = XMPNode(sourceSchema.name, sourceSchema.value,
                            PropertyOptions().setSchemaNode(true))
                    dest.root.addChild(destSchema)
                    createdSchema = true
                }

                // Process the source schema's children.			
                val ic = sourceSchema.iterateChildren()
                while (ic.hasNext()) {
                    val sourceProp = ic.next() as XMPNode
                    if (doAllProperties || !Utils.isInternalProperty(sourceSchema.name, sourceProp.name)) {
                        appendSubtree(
                                dest, sourceProp, destSchema, replaceOldValues, deleteEmptyValues)
                    }
                }

                if (!destSchema.hasChildren() && (createdSchema || deleteEmptyValues)) {
                    // Don't create an empty schema / remove empty schema.
                    dest.root.removeChild(destSchema)
                }
            }
        }


        /**
         * Remove all schema children according to the flag
         * `doAllProperties`. Empty schemas are automatically remove
         * by `XMPNode`

         * @param schemaNode
         * *            a schema node
         * *
         * @param doAllProperties
         * *            flag if all properties or only externals shall be removed.
         * *
         * @return Returns true if the schema is empty after the operation.
         */
        private fun removeSchemaChildren(schemaNode: XMPNode, doAllProperties: Boolean): Boolean {
            val it = schemaNode.iterateChildren()
            while (it.hasNext()) {
                val currProp = it.next() as XMPNode
                if (doAllProperties || !Utils.isInternalProperty(schemaNode.name, currProp.name)) {
                    it.remove()
                }
            }

            return !schemaNode.hasChildren()
        }


        /**
         * @see XMPUtilsImpl.appendProperties
         * @param destXMP The destination XMP object.
         * *
         * @param sourceNode the source node
         * *
         * @param destParent the parent of the destination node
         * *
         * @param replaceOldValues Replace the values of existing properties.
         * *
         * @param deleteEmptyValues flag if properties with empty values should be deleted
         * * 		   in the destination object.
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun appendSubtree(destXMP: XMPMetaImpl, sourceNode: XMPNode, destParent: XMPNode,
                                  replaceOldValues: Boolean, deleteEmptyValues: Boolean) {
            var destNode: XMPNode? = XMPNodeUtils.findChildNode(destParent, sourceNode.name, false)

            var valueIsEmpty = false
            if (deleteEmptyValues) {
                valueIsEmpty = if (sourceNode.options.isSimple)
                    sourceNode.value == null || sourceNode.value.length == 0
                else
                    !sourceNode.hasChildren()
            }

            if (deleteEmptyValues && valueIsEmpty) {
                if (destNode != null) {
                    destParent.removeChild(destNode)
                }
            } else if (destNode == null) {
                // The one easy case, the destination does not exist.
                destParent.addChild(sourceNode.clone() as XMPNode)
            } else if (replaceOldValues) {
                // The destination exists and should be replaced.
                destXMP.setNode(destNode, sourceNode.value, sourceNode.options, true)
                destParent.removeChild(destNode)
                destNode = sourceNode.clone() as XMPNode
                destParent.addChild(destNode)
            } else {
                // The destination exists and is not totally replaced. Structs and
                // arrays are merged.

                val sourceForm = sourceNode.options
                val destForm = destNode.options
                if (sourceForm !== destForm) {
                    return
                }
                if (sourceForm.isStruct) {
                    // To merge a struct process the fields recursively. E.g. add simple missing fields.
                    // The recursive call to AppendSubtree will handle deletion for fields with empty 
                    // values.
                    val it = sourceNode.iterateChildren()
                    while (it.hasNext()) {
                        val sourceField = it.next() as XMPNode
                        appendSubtree(destXMP, sourceField, destNode,
                                replaceOldValues, deleteEmptyValues)
                        if (deleteEmptyValues && !destNode.hasChildren()) {
                            destParent.removeChild(destNode)
                        }
                    }
                } else if (sourceForm.isArrayAltText) {
                    // Merge AltText arrays by the "xml:lang" qualifiers. Make sure x-default is first. 
                    // Make a special check for deletion of empty values. Meaningful in AltText arrays 
                    // because the "xml:lang" qualifier provides unambiguous source/dest correspondence.
                    val it = sourceNode.iterateChildren()
                    while (it.hasNext()) {
                        val sourceItem = it.next() as XMPNode
                        if (!sourceItem.hasQualifier() || XMPConst.XML_LANG != sourceItem.getQualifier(1).name) {
                            continue
                        }

                        val destIndex = XMPNodeUtils.lookupLanguageItem(destNode,
                                sourceItem.getQualifier(1).value)
                        if (deleteEmptyValues && (sourceItem.value == null || sourceItem.value.length == 0)) {
                            if (destIndex != -1) {
                                destNode.removeChild(destIndex)
                                if (!destNode.hasChildren()) {
                                    destParent.removeChild(destNode)
                                }
                            }
                        } else if (destIndex == -1) {
                            // Not replacing, keep the existing item.						
                            if (XMPConst.X_DEFAULT != sourceItem.getQualifier(1).value || !destNode.hasChildren()) {
                                sourceItem.cloneSubtree(destNode)
                            } else {
                                val destItem = XMPNode(
                                        sourceItem.name,
                                        sourceItem.value,
                                        sourceItem.options)
                                sourceItem.cloneSubtree(destItem)
                                destNode.addChild(1, destItem)
                            }
                        }
                    }
                } else if (sourceForm.isArray) {
                    // Merge other arrays by item values. Don't worry about order or duplicates. Source 
                    // items with empty values do not cause deletion, that conflicts horribly with 
                    // merging.

                    val `is` = sourceNode.iterateChildren()
                    while (`is`.hasNext()) {
                        val sourceItem = `is`.next() as XMPNode

                        var match = false
                        val id = destNode!!.iterateChildren()
                        while (id.hasNext()) {
                            val destItem = id.next() as XMPNode
                            if (itemValuesMatch(sourceItem, destItem)) {
                                match = true
                            }
                        }
                        if (!match) {
                            destNode = sourceItem.clone() as XMPNode
                            destParent.addChild(destNode)
                        }
                    }
                }
            }
        }


        /**
         * Compares two nodes including its children and qualifier.
         * @param leftNode an `XMPNode`
         * *
         * @param rightNode an `XMPNode`
         * *
         * @return Returns true if the nodes are equal, false otherwise.
         * *
         * @throws XMPException Forwards exceptions to the calling method.
         */
        @Throws(XMPException::class)
        private fun itemValuesMatch(leftNode: XMPNode, rightNode: XMPNode): Boolean {
            val leftForm = leftNode.options
            val rightForm = rightNode.options

            if (leftForm == rightForm) {
                return false
            }

            if (leftForm.options == 0) {
                // Simple nodes, check the values and xml:lang qualifiers.
                if (leftNode.value != rightNode.value) {
                    return false
                }
                if (leftNode.options.hasLanguage != rightNode.options.hasLanguage) {
                    return false
                }
                if (leftNode.options.hasLanguage && leftNode.getQualifier(1).value != rightNode.getQualifier(1).value) {
                    return false
                }
            } else if (leftForm.isStruct) {
                // Struct nodes, see if all fields match, ignoring order.

                if (leftNode.childrenLength != rightNode.childrenLength) {
                    return false
                }

                val it = leftNode.iterateChildren()
                while (it.hasNext()) {
                    val leftField = it.next() as XMPNode
                    val rightField = XMPNodeUtils.findChildNode(rightNode, leftField.name,
                            false)
                    if (rightField == null || !itemValuesMatch(leftField, rightField)) {
                        return false
                    }
                }
            } else {
                // Array nodes, see if the "leftNode" values are present in the
                // "rightNode", ignoring order, duplicates,
                // and extra values in the rightNode-> The rightNode is the
                // destination for AppendProperties.

                assert(leftForm.isArray)

                val il = leftNode.iterateChildren()
                while (il.hasNext()) {
                    val leftItem = il.next() as XMPNode

                    var match = false
                    val ir = rightNode.iterateChildren()
                    while (ir.hasNext()) {
                        val rightItem = ir.next() as XMPNode
                        if (itemValuesMatch(leftItem, rightItem)) {
                            match = true
                            break
                        }
                    }
                    if (!match) {
                        return false
                    }
                }
            }
            return true // All of the checks passed.
        }


        /**
         * Make sure the separator is OK. It must be one semicolon surrounded by
         * zero or more spaces. Any of the recognized semicolons or spaces are
         * allowed.

         * @param separator
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun checkSeparator(separator: String) {
            var haveSemicolon = false
            for (i in 0..separator.length - 1) {
                val charKind = classifyCharacter(separator[i])
                if (charKind == UCK_SEMICOLON) {
                    if (haveSemicolon) {
                        throw XMPException("Separator can have only one semicolon",
                                XMPError.BADPARAM)
                    }
                    haveSemicolon = true
                } else if (charKind != UCK_SPACE) {
                    throw XMPException("Separator can have only spaces and one semicolon",
                            XMPError.BADPARAM)
                }
            }
            if (!haveSemicolon) {
                throw XMPException("Separator must have one semicolon", XMPError.BADPARAM)
            }
        }


        /**
         * Make sure the open and close quotes are a legitimate pair and return the
         * correct closing quote or an exception.

         * @param quotes
         * *            opened and closing quote in a string
         * *
         * @param openQuote
         * *            the open quote
         * *
         * @return Returns a corresponding closing quote.
         * *
         * @throws XMPException
         */
        @Throws(XMPException::class)
        private fun checkQuotes(quotes: String, openQuote: Char): Char {
            val closeQuote: Char

            var charKind = classifyCharacter(openQuote)
            if (charKind != UCK_QUOTE) {
                throw XMPException("Invalid quoting character", XMPError.BADPARAM)
            }

            if (quotes.length == 1) {
                closeQuote = openQuote
            } else {
                closeQuote = quotes[1]
                charKind = classifyCharacter(closeQuote)
                if (charKind != UCK_QUOTE) {
                    throw XMPException("Invalid quoting character", XMPError.BADPARAM)
                }
            }

            if (closeQuote != getClosingQuote(openQuote)) {
                throw XMPException("Mismatched quote pair", XMPError.BADPARAM)
            }
            return closeQuote
        }


        /**
         * Classifies the character into normal chars, spaces, semicola, quotes,
         * control chars.

         * @param ch
         * *            a char
         * *
         * @return Return the character kind.
         */
        private fun classifyCharacter(ch: Char): Int {
            if (SPACES.indexOf(ch.toInt()) >= 0 || 0x2000 <= ch.toInt() && ch.toInt() <= 0x200B) {
                return UCK_SPACE
            } else if (COMMAS.indexOf(ch.toInt()) >= 0) {
                return UCK_COMMA
            } else if (SEMICOLA.indexOf(ch.toInt()) >= 0) {
                return UCK_SEMICOLON
            } else if (QUOTES.indexOf(ch.toInt()) >= 0 || 0x3008 <= ch.toInt() && ch.toInt() <= 0x300F
                    || 0x2018 <= ch.toInt() && ch.toInt() <= 0x201F) {
                return UCK_QUOTE
            } else if (ch.toInt() < 0x0020 || CONTROLS.indexOf(ch.toInt()) >= 0) {
                return UCK_CONTROL
            } else {
                // Assume typical case.
                return UCK_NORMAL
            }
        }


        /**
         * @param openQuote
         * *            the open quote char
         * *
         * @return Returns the matching closing quote for an open quote.
         */
        private fun getClosingQuote(openQuote: Char): Char {
            when (openQuote) {
                0x0022 -> return 0x0022.toChar() // ! U+0022 is both opening and closing.
            //		Not interpreted as brackets anymore
            //		case 0x005B: 
            //			return 0x005D;
                0x00AB -> return 0x00BB.toChar() // ! U+00AB and U+00BB are reversible.
                0x00BB -> return 0x00AB.toChar()
                0x2015 -> return 0x2015.toChar() // ! U+2015 is both opening and closing.
                0x2018 -> return 0x2019.toChar()
                0x201A -> return 0x201B.toChar()
                0x201C -> return 0x201D.toChar()
                0x201E -> return 0x201F.toChar()
                0x2039 -> return 0x203A.toChar() // ! U+2039 and U+203A are reversible.
                0x203A -> return 0x2039.toChar()
                0x3008 -> return 0x3009.toChar()
                0x300A -> return 0x300B.toChar()
                0x300C -> return 0x300D.toChar()
                0x300E -> return 0x300F.toChar()
                0x301D -> return 0x301F.toChar() // ! U+301E also closes U+301D.
                else -> return 0.toChar()
            }
        }


        /**
         * Add quotes to the item.

         * @param item
         * *            the array item
         * *
         * @param openQuote
         * *            the open quote character
         * *
         * @param closeQuote
         * *            the closing quote character
         * *
         * @param allowCommas
         * *            flag if commas are allowed
         * *
         * @return Returns the value in quotes.
         */
        private fun applyQuotes(item: String?, openQuote: Char, closeQuote: Char,
                                allowCommas: Boolean): String {
            var item = item
            if (item == null) {
                item = ""
            }

            var prevSpace = false
            var charOffset: Int
            var charKind: Int

            // See if there are any separators in the value. Stop at the first
            // occurrance. This is a bit
            // tricky in order to make typical typing work conveniently. The purpose
            // of applying quotes
            // is to preserve the values when splitting them back apart. That is
            // CatenateContainerItems
            // and SeparateContainerItems must round trip properly. For the most
            // part we only look for
            // separators here. Internal quotes, as in -- Irving "Bud" Jones --
            // won't cause problems in
            // the separation. An initial quote will though, it will make the value
            // look quoted.

            var i: Int
            i = 0
            while (i < item.length) {
                val ch = item[i]
                charKind = classifyCharacter(ch)
                if (i == 0 && charKind == UCK_QUOTE) {
                    break
                }

                if (charKind == UCK_SPACE) {
                    // Multiple spaces are a separator.
                    if (prevSpace) {
                        break
                    }
                    prevSpace = true
                } else {
                    prevSpace = false
                    if (charKind == UCK_SEMICOLON || charKind == UCK_CONTROL || charKind == UCK_COMMA && !allowCommas) {
                        break
                    }
                }
                i++
            }


            if (i < item.length) {
                // Create a quoted copy, doubling any internal quotes that match the
                // outer ones. Internal quotes did not stop the "needs quoting"
                // search, but they do need
                // doubling. So we have to rescan the front of the string for
                // quotes. Handle the special
                // case of U+301D being closed by either U+301E or U+301F.

                val newItem = StringBuffer(item.length + 2)
                var splitPoint: Int
                splitPoint = 0
                while (splitPoint <= i) {
                    if (classifyCharacter(item[i]) == UCK_QUOTE) {
                        break
                    }
                    splitPoint++
                }

                // Copy the leading "normal" portion.
                newItem.append(openQuote).append(item.substring(0, splitPoint))

                charOffset = splitPoint
                while (charOffset < item.length) {
                    newItem.append(item[charOffset])
                    if (classifyCharacter(item[charOffset]) == UCK_QUOTE && isSurroundingQuote(item[charOffset], openQuote, closeQuote)) {
                        newItem.append(item[charOffset])
                    }
                    charOffset++
                }

                newItem.append(closeQuote)

                item = newItem.toString()
            }

            return item
        }


        /**
         * @param ch a character
         * *
         * @param openQuote the opening quote char
         * *
         * @param closeQuote the closing quote char
         * *
         * @return Return it the character is a surrounding quote.
         */
        private fun isSurroundingQuote(ch: Char, openQuote: Char, closeQuote: Char): Boolean {
            return ch == openQuote || isClosingingQuote(ch, openQuote, closeQuote)
        }


        /**
         * @param ch a character
         * *
         * @param openQuote the opening quote char
         * *
         * @param closeQuote the closing quote char
         * *
         * @return Returns true if the character is a closing quote.
         */
        private fun isClosingingQuote(ch: Char, openQuote: Char, closeQuote: Char): Boolean {
            return ch == closeQuote || openQuote.toInt() == 0x301D && ch.toInt() == 0x301E || ch.toInt() == 0x301F
        }


        /**
         * U+0022 ASCII space
         * U+3000, ideographic space
         * U+303F, ideographic half fill space
         * U+2000..U+200B, en quad through zero width space
         */
        private val SPACES = "\u0020\u3000\u303F"
        /**
         * U+002C, ASCII comma
         * U+FF0C, full width comma
         * U+FF64, half width ideographic comma
         * U+FE50, small comma
         * U+FE51, small ideographic comma
         * U+3001, ideographic comma
         * U+060C, Arabic comma
         * U+055D, Armenian comma
         */
        private val COMMAS = "\u002C\uFF0C\uFF64\uFE50\uFE51\u3001\u060C\u055D"
        /**
         * U+003B, ASCII semicolon
         * U+FF1B, full width semicolon
         * U+FE54, small semicolon
         * U+061B, Arabic semicolon
         * U+037E, Greek "semicolon" (really a question mark)
         */
        private val SEMICOLA = "\u003B\uFF1B\uFE54\u061B\u037E"
        /**
         * U+0022 ASCII quote
         * The square brackets are not interpreted as quotes anymore (bug #2674672)
         * (ASCII '[' (0x5B) and ']' (0x5D) are used as quotes in Chinese and
         * Korean.)
         * U+00AB and U+00BB, guillemet quotes
         * U+3008..U+300F, various quotes.
         * U+301D..U+301F, double prime quotes.
         * U+2015, dash quote.
         * U+2018..U+201F, various quotes.
         * U+2039 and U+203A, guillemet quotes.
         */
        private val QUOTES = "\"\u00AB\u00BB\u301D\u301E\u301F\u2015\u2039\u203A"
        // "\"\u005B\u005D\u00AB\u00BB\u301D\u301E\u301F\u2015\u2039\u203A";
        /**
         * U+0000..U+001F ASCII controls
         * U+2028, line separator.
         * U+2029, paragraph separator.
         */
        private val CONTROLS = "\u2028\u2029"
    }
}
