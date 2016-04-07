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

import java.util.Calendar
import java.util.HashMap

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPDateTime
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.XMPUtils
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathParser
import com.itextpdf.xmp.options.ParseOptions
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.properties.XMPAliasInfo

/**
 * @since   Aug 18, 2006
 */
object XMPNormalizer {
    /** caches the correct dc-property array forms  */
    private var dcArrayForms: MutableMap<Any, Any>? = null

    /** init char tables  */
    init {
        initDCArrays()
    }


    /**
     * Normalizes a raw parsed XMPMeta-Object
     * @param xmp the raw metadata object
     * *
     * @param options the parsing options
     * *
     * @return Returns the normalized metadata object
     * *
     * @throws XMPException Collects all severe processing errors.
     */
    @Throws(XMPException::class)
    internal fun process(xmp: XMPMetaImpl, options: ParseOptions): XMPMeta {
        val tree = xmp.root

        touchUpDataModel(xmp)
        moveExplicitAliases(tree, options)

        tweakOldXMP(tree)

        deleteEmptySchemas(tree)

        return xmp
    }


    /**
     * Tweak old XMP: Move an instance ID from rdf:about to the
     * *xmpMM:InstanceID* property. An old instance ID usually looks
     * like &quot;uuid:bac965c4-9d87-11d9-9a30-000d936b79c4&quot;, plus InDesign
     * 3.0 wrote them like &quot;bac965c4-9d87-11d9-9a30-000d936b79c4&quot;. If
     * the name looks like a UUID simply move it to *xmpMM:InstanceID*,
     * don't worry about any existing *xmpMM:InstanceID*. Both will
     * only be present when a newer file with the *xmpMM:InstanceID*
     * property is updated by an old app that uses *rdf:about*.

     * @param tree the root of the metadata tree
     * *
     * @throws XMPException Thrown if tweaking fails.
     */
    @Throws(XMPException::class)
    private fun tweakOldXMP(tree: XMPNode) {
        if (tree.name != null && tree.name.length >= Utils.UUID_LENGTH) {
            var nameStr = tree.name.toLowerCase()
            if (nameStr.startsWith("uuid:")) {
                nameStr = nameStr.substring(5)
            }

            if (Utils.checkUUIDFormat(nameStr)) {
                // move UUID to xmpMM:InstanceID and remove it from the root node
                val path = XMPPathParser.expandXPath(XMPConst.NS_XMP_MM, "InstanceID")
                val idNode = XMPNodeUtils.findNode(tree, path, true, null)
                if (idNode != null) {
                    idNode.options = null    // Clobber any existing xmpMM:InstanceID.
                    idNode.value = "uuid:" + nameStr
                    idNode.removeChildren()
                    idNode.removeQualifiers()
                    tree.name = null
                } else {
                    throw XMPException("Failure creating xmpMM:InstanceID",
                            XMPError.INTERNALFAILURE)
                }
            }
        }
    }


    /**
     * Visit all schemas to do general fixes and handle special cases.

     * @param xmp the metadata object implementation
     * *
     * @throws XMPException Thrown if the normalisation fails.
     */
    @Throws(XMPException::class)
    private fun touchUpDataModel(xmp: XMPMetaImpl) {
        // make sure the DC schema is existing, because it might be needed within the normalization
        // if not touched it will be removed by removeEmptySchemas
        XMPNodeUtils.findSchemaNode(xmp.root, XMPConst.NS_DC, true)

        // Do the special case fixes within each schema.
        val it = xmp.root.iterateChildren()
        while (it.hasNext()) {
            val currSchema = it.next() as XMPNode
            if (XMPConst.NS_DC == currSchema.name) {
                normalizeDCArrays(currSchema)
            } else if (XMPConst.NS_EXIF == currSchema.name) {
                // Do a special case fix for exif:GPSTimeStamp.
                fixGPSTimeStamp(currSchema)
                val arrayNode = XMPNodeUtils.findChildNode(currSchema, "exif:UserComment",
                        false)
                if (arrayNode != null) {
                    repairAltText(arrayNode)
                }
            } else if (XMPConst.NS_DM == currSchema.name) {
                // Do a special case migration of xmpDM:copyright to
                // dc:rights['x-default'].
                val dmCopyright = XMPNodeUtils.findChildNode(currSchema, "xmpDM:copyright",
                        false)
                if (dmCopyright != null) {
                    migrateAudioCopyright(xmp, dmCopyright)
                }
            } else if (XMPConst.NS_XMP_RIGHTS == currSchema.name) {
                val arrayNode = XMPNodeUtils.findChildNode(currSchema, "xmpRights:UsageTerms",
                        false)
                if (arrayNode != null) {
                    repairAltText(arrayNode)
                }
            }
        }
    }


    /**
     * Undo the denormalization performed by the XMP used in Acrobat 5.
     * If a Dublin Core array had only one item, it was serialized as a simple
     * property.
     * The `xml:lang` attribute was dropped from an
     * `alt-text` item if the language was `x-default`.

     * @param dcSchema the DC schema node
     * *
     * @throws XMPException Thrown if normalization fails
     */
    @Throws(XMPException::class)
    private fun normalizeDCArrays(dcSchema: XMPNode) {
        for (i in 1..dcSchema.childrenLength) {
            val currProp = dcSchema.getChild(i)

            val arrayForm = dcArrayForms!![currProp.name] as PropertyOptions
            if (arrayForm == null) {
                continue
            } else if (currProp.options.isSimple) {
                // create a new array and add the current property as child, 
                // if it was formerly simple 
                val newArray = XMPNode(currProp.name, arrayForm)
                currProp.name = XMPConst.ARRAY_ITEM_NAME
                newArray.addChild(currProp)
                dcSchema.replaceChild(i, newArray)

                // fix language alternatives
                if (arrayForm.isArrayAltText && !currProp.options.hasLanguage) {
                    val newLang = XMPNode(XMPConst.XML_LANG, XMPConst.X_DEFAULT, null)
                    currProp.addQualifier(newLang)
                }
            } else {
                // clear array options and add corrected array form if it has been an array before
                currProp.options.setOption(
                        PropertyOptions.ARRAY or
                                PropertyOptions.ARRAY_ORDERED or
                                PropertyOptions.ARRAY_ALTERNATE or
                                PropertyOptions.ARRAY_ALT_TEXT,
                        false)
                currProp.options.mergeWith(arrayForm)

                if (arrayForm.isArrayAltText) {
                    // applying for "dc:description", "dc:rights", "dc:title"
                    repairAltText(currProp)
                }
            }

        }
    }


    /**
     * Make sure that the array is well-formed AltText. Each item must be simple
     * and have an "xml:lang" qualifier. If repairs are needed, keep simple
     * non-empty items by adding the "xml:lang" with value "x-repair".
     * @param arrayNode the property node of the array to repair.
     * *
     * @throws XMPException Forwards unexpected exceptions.
     */
    @Throws(XMPException::class)
    private fun repairAltText(arrayNode: XMPNode?) {
        if (arrayNode == null || !arrayNode.options.isArray) {
            // Already OK or not even an array.
            return
        }

        // fix options
        arrayNode.options.setArrayOrdered(true).setArrayAlternate(true).isArrayAltText = true

        val it = arrayNode.iterateChildren()
        while (it.hasNext()) {
            val currChild = it.next() as XMPNode
            if (currChild.options.isCompositeProperty) {
                // Delete non-simple children.
                it.remove()
            } else if (!currChild.options.hasLanguage) {
                val childValue = currChild.value
                if (childValue == null || childValue.length == 0) {
                    // Delete empty valued children that have no xml:lang.
                    it.remove()
                } else {
                    // Add an xml:lang qualifier with the value "x-repair".
                    val repairLang = XMPNode(XMPConst.XML_LANG, "x-repair", null)
                    currChild.addQualifier(repairLang)
                }
            }
        }
    }


    /**
     * Visit all of the top level nodes looking for aliases. If there is
     * no base, transplant the alias subtree. If there is a base and strict
     * aliasing is on, make sure the alias and base subtrees match.

     * @param tree the root of the metadata tree
     * *
     * @param options th parsing options
     * *
     * @throws XMPException Forwards XMP errors
     */
    @Throws(XMPException::class)
    private fun moveExplicitAliases(tree: XMPNode, options: ParseOptions) {
        if (!tree.hasAliases) {
            return
        }
        tree.hasAliases = false

        val strictAliasing = options.strictAliasing

        val schemaIt = tree.unmodifiableChildren.iterator()
        while (schemaIt.hasNext()) {
            val currSchema = schemaIt.next() as XMPNode
            if (!currSchema.hasAliases) {
                continue
            }

            val propertyIt = currSchema.iterateChildren()
            while (propertyIt.hasNext()) {
                val currProp = propertyIt.next() as XMPNode

                if (!currProp.isAlias) {
                    continue
                }

                currProp.isAlias = false

                // Find the base path, look for the base schema and root node.
                val info = XMPMetaFactory.getSchemaRegistry().findAlias(currProp.name)
                if (info != null) {
                    // find or create schema
                    val baseSchema = XMPNodeUtils.findSchemaNode(tree, info.namespace, null, true)
                    baseSchema.isImplicit = false

                    var baseNode: XMPNode? = XMPNodeUtils.findChildNode(baseSchema,
                            info.prefix + info.propName, false)
                    if (baseNode == null) {
                        if (info.aliasForm.isSimple) {
                            // A top-to-top alias, transplant the property.
                            // change the alias property name to the base name
                            val qname = info.prefix + info.propName
                            currProp.name = qname
                            baseSchema.addChild(currProp)
                            // remove the alias property
                            propertyIt.remove()
                        } else {
                            // An alias to an array item, 
                            // create the array and transplant the property.
                            baseNode = XMPNode(info.prefix + info.propName, info.aliasForm.toPropertyOptions())
                            baseSchema.addChild(baseNode)
                            transplantArrayItemAlias(propertyIt, currProp, baseNode)
                        }

                    } else if (info.aliasForm.isSimple) {
                        // The base node does exist and this is a top-to-top alias.
                        // Check for conflicts if strict aliasing is on. 
                        // Remove and delete the alias subtree.
                        if (strictAliasing) {
                            compareAliasedSubtrees(currProp, baseNode, true)
                        }

                        propertyIt.remove()
                    } else {
                        // This is an alias to an array item and the array exists.
                        // Look for the aliased item.
                        // Then transplant or check & delete as appropriate.

                        var itemNode: XMPNode? = null
                        if (info.aliasForm.isArrayAltText) {
                            val xdIndex = XMPNodeUtils.lookupLanguageItem(baseNode,
                                    XMPConst.X_DEFAULT)
                            if (xdIndex != -1) {
                                itemNode = baseNode.getChild(xdIndex)
                            }
                        } else if (baseNode.hasChildren()) {
                            itemNode = baseNode.getChild(1)
                        }

                        if (itemNode == null) {
                            transplantArrayItemAlias(propertyIt, currProp, baseNode)
                        } else {
                            if (strictAliasing) {
                                compareAliasedSubtrees(currProp, itemNode, true)
                            }

                            propertyIt.remove()
                        }
                    }
                }
            }
            currSchema.hasAliases = false
        }
    }


    /**
     * Moves an alias node of array form to another schema into an array
     * @param propertyIt the property iterator of the old schema (used to delete the property)
     * *
     * @param childNode the node to be moved
     * *
     * @param baseArray the base array for the array item
     * *
     * @throws XMPException Forwards XMP errors
     */
    @Throws(XMPException::class)
    private fun transplantArrayItemAlias(propertyIt: MutableIterator<Any>, childNode: XMPNode,
                                         baseArray: XMPNode) {
        if (baseArray.options.isArrayAltText) {
            if (childNode.options.hasLanguage) {
                throw XMPException("Alias to x-default already has a language qualifier",
                        XMPError.BADXMP)
            }

            val langQual = XMPNode(XMPConst.XML_LANG, XMPConst.X_DEFAULT, null)
            childNode.addQualifier(langQual)
        }

        propertyIt.remove()
        childNode.name = XMPConst.ARRAY_ITEM_NAME
        baseArray.addChild(childNode)
    }


    /**
     * Fixes the GPS Timestamp in EXIF.
     * @param exifSchema the EXIF schema node
     * *
     * @throws XMPException Thrown if the date conversion fails.
     */
    @Throws(XMPException::class)
    private fun fixGPSTimeStamp(exifSchema: XMPNode) {
        // Note: if dates are not found the convert-methods throws an exceptions,
        // 		 and this methods returns.
        val gpsDateTime = XMPNodeUtils.findChildNode(exifSchema, "exif:GPSTimeStamp", false) ?: return

        try {
            var binGPSStamp: XMPDateTime
            val binOtherDate: XMPDateTime

            binGPSStamp = XMPUtils.convertToDate(gpsDateTime.value)
            if (binGPSStamp.year != 0 ||
                    binGPSStamp.month != 0 ||
                    binGPSStamp.day != 0) {
                return
            }

            var otherDate: XMPNode? = XMPNodeUtils.findChildNode(exifSchema, "exif:DateTimeOriginal",
                    false)
            if (otherDate == null) {
                otherDate = XMPNodeUtils.findChildNode(exifSchema, "exif:DateTimeDigitized", false)
            }

            binOtherDate = XMPUtils.convertToDate(otherDate!!.value)
            val cal = binGPSStamp.calendar
            cal.set(Calendar.YEAR, binOtherDate.year)
            cal.set(Calendar.MONTH, binOtherDate.month)
            cal.set(Calendar.DAY_OF_MONTH, binOtherDate.day)
            binGPSStamp = XMPDateTimeImpl(cal)
            gpsDateTime.value = XMPUtils.convertFromDate(binGPSStamp)
        } catch (e: XMPException) {
            // Don't let a missing or bad date stop other things.
            return
        }

    }


    /**
     * Remove all empty schemas from the metadata tree that were generated during the rdf parsing.
     * @param tree the root of the metadata tree
     */
    private fun deleteEmptySchemas(tree: XMPNode) {
        // Delete empty schema nodes. Do this last, other cleanup can make empty
        // schema.

        val it = tree.iterateChildren()
        while (it.hasNext()) {
            val schema = it.next() as XMPNode
            if (!schema.hasChildren()) {
                it.remove()
            }
        }
    }


    /**
     * The outermost call is special. The names almost certainly differ. The
     * qualifiers (and hence options) will differ for an alias to the x-default
     * item of a langAlt array.

     * @param aliasNode the alias node
     * *
     * @param baseNode the base node of the alias
     * *
     * @param outerCall marks the outer call of the recursion
     * *
     * @throws XMPException Forwards XMP errors
     */
    @Throws(XMPException::class)
    private fun compareAliasedSubtrees(aliasNode: XMPNode, baseNode: XMPNode,
                                       outerCall: Boolean) {
        if (aliasNode.value != baseNode.value || aliasNode.childrenLength != baseNode.childrenLength) {
            throw XMPException("Mismatch between alias and base nodes", XMPError.BADXMP)
        }

        if (!outerCall && (aliasNode.name != baseNode.name ||
                aliasNode.options != baseNode.options ||
                aliasNode.qualifierLength != baseNode.qualifierLength)) {
            throw XMPException("Mismatch between alias and base nodes",
                    XMPError.BADXMP)
        }

        run {
            val an = aliasNode.iterateChildren()
            val bn = baseNode.iterateChildren()
            while (an.hasNext() && bn.hasNext()) {
                val aliasChild = an.next() as XMPNode
                val baseChild = bn.next() as XMPNode
                compareAliasedSubtrees(aliasChild, baseChild, false)
            }
        }


        val an = aliasNode.iterateQualifier()
        val bn = baseNode.iterateQualifier()
        while (an.hasNext() && bn.hasNext()) {
            val aliasQual = an.next() as XMPNode
            val baseQual = bn.next() as XMPNode
            compareAliasedSubtrees(aliasQual, baseQual, false)
        }
    }


    /**
     * The initial support for WAV files mapped a legacy ID3 audio copyright
     * into a new xmpDM:copyright property. This is special case code to migrate
     * that into dc:rights['x-default']. The rules:

     *
     * 1. If there is no dc:rights array, or an empty array -
     * Create one with dc:rights['x-default'] set from double linefeed and xmpDM:copyright.

     * 2. If there is a dc:rights array but it has no x-default item -
     * Create an x-default item as a copy of the first item then apply rule #3.

     * 3. If there is a dc:rights array with an x-default item,
     * Look for a double linefeed in the value.
     * A. If no double linefeed, compare the x-default value to the xmpDM:copyright value.
     * A1. If they match then leave the x-default value alone.
     * A2. Otherwise, append a double linefeed and
     * the xmpDM:copyright value to the x-default value.
     * B. If there is a double linefeed, compare the trailing text to the xmpDM:copyright value.
     * B1. If they match then leave the x-default value alone.
     * B2. Otherwise, replace the trailing x-default text with the xmpDM:copyright value.

     * 4. In all cases, delete the xmpDM:copyright property.
     *

     * @param xmp the metadata object
     * *
     * @param dmCopyright the "dm:copyright"-property
     */
    private fun migrateAudioCopyright(xmp: XMPMeta, dmCopyright: XMPNode) {
        try {
            val dcSchema = XMPNodeUtils.findSchemaNode(
                    (xmp as XMPMetaImpl).root, XMPConst.NS_DC, true)

            var dmValue = dmCopyright.value
            val doubleLF = "\n\n"

            val dcRightsArray = XMPNodeUtils.findChildNode(dcSchema, "dc:rights", false)

            if (dcRightsArray == null || !dcRightsArray.hasChildren()) {
                // 1. No dc:rights array, create from double linefeed and xmpDM:copyright.
                dmValue = doubleLF + dmValue
                xmp.setLocalizedText(XMPConst.NS_DC, "rights", "", XMPConst.X_DEFAULT, dmValue,
                        null)
            } else {
                var xdIndex = XMPNodeUtils.lookupLanguageItem(dcRightsArray, XMPConst.X_DEFAULT)

                if (xdIndex < 0) {
                    // 2. No x-default item, create from the first item.
                    val firstValue = dcRightsArray.getChild(1).value
                    xmp.setLocalizedText(XMPConst.NS_DC, "rights", "", XMPConst.X_DEFAULT,
                            firstValue, null)
                    xdIndex = XMPNodeUtils.lookupLanguageItem(dcRightsArray, XMPConst.X_DEFAULT)
                }

                // 3. Look for a double linefeed in the x-default value.
                val defaultNode = dcRightsArray.getChild(xdIndex)
                val defaultValue = defaultNode.value
                val lfPos = defaultValue.indexOf(doubleLF)

                if (lfPos < 0) {
                    // 3A. No double LF, compare whole values.
                    if (dmValue != defaultValue) {
                        // 3A2. Append the xmpDM:copyright to the x-default
                        // item.
                        defaultNode.value = defaultValue + doubleLF + dmValue
                    }
                } else {
                    // 3B. Has double LF, compare the tail.
                    if (defaultValue.substring(lfPos + 2) != dmValue) {
                        // 3B2. Replace the x-default tail.
                        defaultNode.value = defaultValue.substring(0, lfPos + 2) + dmValue
                    }
                }

            }

            // 4. Get rid of the xmpDM:copyright.
            dmCopyright.parent.removeChild(dmCopyright)
        } catch (e: XMPException) {
            // Don't let failures (like a bad dc:rights form) stop other
            // cleanup.
        }

    }


    /**
     * Initializes the map that contains the known arrays, that are fixed by
     * [XMPNormalizer.normalizeDCArrays].
     */
    private fun initDCArrays() {
        dcArrayForms = HashMap()

        // Properties supposed to be a "Bag".
        val bagForm = PropertyOptions()
        bagForm.isArray = true
        dcArrayForms!!.put("dc:contributor", bagForm)
        dcArrayForms!!.put("dc:language", bagForm)
        dcArrayForms!!.put("dc:publisher", bagForm)
        dcArrayForms!!.put("dc:relation", bagForm)
        dcArrayForms!!.put("dc:subject", bagForm)
        dcArrayForms!!.put("dc:type", bagForm)

        // Properties supposed to be a "Seq".
        val seqForm = PropertyOptions()
        seqForm.isArray = true
        seqForm.isArrayOrdered = true
        dcArrayForms!!.put("dc:creator", seqForm)
        dcArrayForms!!.put("dc:date", seqForm)

        // Properties supposed to be an "Alt" in alternative-text form.
        val altTextForm = PropertyOptions()
        altTextForm.isArray = true
        altTextForm.isArrayOrdered = true
        altTextForm.isArrayAlternate = true
        altTextForm.isArrayAltText = true
        dcArrayForms!!.put("dc:description", altTextForm)
        dcArrayForms!!.put("dc:rights", altTextForm)
        dcArrayForms!!.put("dc:title", altTextForm)
    }
}
/**
 * Hidden constructor
 */
// EMPTY
