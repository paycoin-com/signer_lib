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

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPDateTime
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPIterator
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPPathFactory
import com.itextpdf.xmp.XMPUtils
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathParser
import com.itextpdf.xmp.options.IteratorOptions
import com.itextpdf.xmp.options.ParseOptions
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.properties.XMPProperty


/**
 * Implementation for [XMPMeta].

 * @since 17.02.2006
 */
class XMPMetaImpl : XMPMeta, XMPConst {

    /** root of the metadata tree  */
    /**
     * @return Returns the root node of the XMP tree.
     */
    var root: XMPNode? = null
        private set
    /** the xpacket processing instructions content  */
    private var packetHeader: String? = null


    /**
     * Constructor for an empty metadata object.
     */
    constructor() {
        // create root node
        root = XMPNode(null, null, null)
    }


    /**
     * Constructor for a cloned metadata tree.

     * @param tree
     * *            an prefilled metadata tree which fulfills all
     * *            `XMPNode` contracts.
     */
    constructor(tree: XMPNode) {
        this.root = tree
    }


    /**
     * @see XMPMeta.appendArrayItem
     */
    @Throws(XMPException::class)
    override fun appendArrayItem(schemaNS: String, arrayName: String, arrayOptions: PropertyOptions?,
                                 itemValue: String, itemOptions: PropertyOptions?) {
        var arrayOptions = arrayOptions
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(arrayName)

        if (arrayOptions == null) {
            arrayOptions = PropertyOptions()
        }
        if (!arrayOptions.isOnlyArrayOptions) {
            throw XMPException("Only array form flags allowed for arrayOptions",
                    XMPError.BADOPTIONS)
        }

        // Check if array options are set correctly.
        arrayOptions = XMPNodeUtils.verifySetOptions(arrayOptions, null)


        // Locate or create the array. If it already exists, make sure the array
        // form from the options
        // parameter is compatible with the current state.
        val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)


        // Just lookup, don't try to create.
        var arrayNode = XMPNodeUtils.findNode(root, arrayPath, false, null)

        if (arrayNode != null) {
            // The array exists, make sure the form is compatible. Zero
            // arrayForm means take what exists.
            if (!arrayNode.options.isArray) {
                throw XMPException("The named property is not an array", XMPError.BADXPATH)
            }
            // if (arrayOptions != null && !arrayOptions.equalArrayTypes(arrayNode.getOptions()))
            // {
            // throw new XMPException("Mismatch of existing and specified array form", BADOPTIONS);
            // }
        } else {
            // The array does not exist, try to create it.
            if (arrayOptions!!.isArray) {
                arrayNode = XMPNodeUtils.findNode(root, arrayPath, true, arrayOptions)
                if (arrayNode == null) {
                    throw XMPException("Failure creating array node", XMPError.BADXPATH)
                }
            } else {
                // array options missing
                throw XMPException("Explicit arrayOptions required to create new array",
                        XMPError.BADOPTIONS)
            }
        }

        doSetArrayItem(arrayNode, XMPConst.ARRAY_LAST_ITEM, itemValue, itemOptions, true)
    }


    /**
     * @see XMPMeta.appendArrayItem
     */
    @Throws(XMPException::class)
    override fun appendArrayItem(schemaNS: String, arrayName: String, itemValue: String) {
        appendArrayItem(schemaNS, arrayName, null, itemValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.countArrayItems
     */
    @Throws(XMPException::class)
    override fun countArrayItems(schemaNS: String, arrayName: String): Int {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(arrayName)

        val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)
        val arrayNode = XMPNodeUtils.findNode(root, arrayPath, false, null) ?: return 0

        if (arrayNode.options.isArray) {
            return arrayNode.childrenLength
        } else {
            throw XMPException("The named property is not an array", XMPError.BADXPATH)
        }
    }


    /**
     * @see XMPMeta.deleteArrayItem
     */
    override fun deleteArrayItem(schemaNS: String, arrayName: String, itemIndex: Int) {
        try {
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertArrayName(arrayName)

            val itemPath = XMPPathFactory.composeArrayItemPath(arrayName, itemIndex)
            deleteProperty(schemaNS, itemPath)
        } catch (e: XMPException) {
            // EMPTY, exceptions are ignored within delete
        }

    }


    /**
     * @see XMPMeta.deleteProperty
     */
    override fun deleteProperty(schemaNS: String, propName: String) {
        try {
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertPropName(propName)

            val expPath = XMPPathParser.expandXPath(schemaNS, propName)

            val propNode = XMPNodeUtils.findNode(root, expPath, false, null)
            if (propNode != null) {
                XMPNodeUtils.deleteNode(propNode)
            }
        } catch (e: XMPException) {
            // EMPTY, exceptions are ignored within delete
        }

    }


    /**
     * @see XMPMeta.deleteQualifier
     */
    override fun deleteQualifier(schemaNS: String, propName: String, qualNS: String, qualName: String) {
        try {
            // Note: qualNS and qualName are checked inside composeQualfierPath
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertPropName(propName)

            val qualPath = propName + XMPPathFactory.composeQualifierPath(qualNS, qualName)
            deleteProperty(schemaNS, qualPath)
        } catch (e: XMPException) {
            // EMPTY, exceptions within delete are ignored
        }

    }


    /**
     * @see XMPMeta.deleteStructField
     */
    override fun deleteStructField(schemaNS: String, structName: String, fieldNS: String,
                                   fieldName: String) {
        try {
            // fieldNS and fieldName are checked inside composeStructFieldPath
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertStructName(structName)

            val fieldPath = structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName)
            deleteProperty(schemaNS, fieldPath)
        } catch (e: XMPException) {
            // EMPTY, exceptions within delete are ignored
        }

    }


    /**
     * @see XMPMeta.doesPropertyExist
     */
    override fun doesPropertyExist(schemaNS: String, propName: String): Boolean {
        try {
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertPropName(propName)

            val expPath = XMPPathParser.expandXPath(schemaNS, propName)
            val propNode = XMPNodeUtils.findNode(root, expPath, false, null)
            return propNode != null
        } catch (e: XMPException) {
            return false
        }

    }


    /**
     * @see XMPMeta.doesArrayItemExist
     */
    override fun doesArrayItemExist(schemaNS: String, arrayName: String, itemIndex: Int): Boolean {
        try {
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertArrayName(arrayName)

            val path = XMPPathFactory.composeArrayItemPath(arrayName, itemIndex)
            return doesPropertyExist(schemaNS, path)
        } catch (e: XMPException) {
            return false
        }

    }


    /**
     * @see XMPMeta.doesStructFieldExist
     */
    override fun doesStructFieldExist(schemaNS: String, structName: String, fieldNS: String,
                                      fieldName: String): Boolean {
        try {
            // fieldNS and fieldName are checked inside composeStructFieldPath()
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertStructName(structName)

            val path = XMPPathFactory.composeStructFieldPath(fieldNS, fieldName)
            return doesPropertyExist(schemaNS, structName + path)
        } catch (e: XMPException) {
            return false
        }

    }


    /**
     * @see XMPMeta.doesQualifierExist
     */
    override fun doesQualifierExist(schemaNS: String, propName: String, qualNS: String,
                                    qualName: String): Boolean {
        try {
            // qualNS and qualName are checked inside composeQualifierPath()
            ParameterAsserts.assertSchemaNS(schemaNS)
            ParameterAsserts.assertPropName(propName)

            val path = XMPPathFactory.composeQualifierPath(qualNS, qualName)
            return doesPropertyExist(schemaNS, propName + path)
        } catch (e: XMPException) {
            return false
        }

    }


    /**
     * @see XMPMeta.getArrayItem
     */
    @Throws(XMPException::class)
    override fun getArrayItem(schemaNS: String, arrayName: String, itemIndex: Int): XMPProperty {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(arrayName)

        val itemPath = XMPPathFactory.composeArrayItemPath(arrayName, itemIndex)
        return getProperty(schemaNS, itemPath)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.getLocalizedText
     */
    @Throws(XMPException::class)
    override fun getLocalizedText(schemaNS: String, altTextName: String, genericLang: String?,
                                  specificLang: String): XMPProperty? {
        var genericLang = genericLang
        var specificLang = specificLang
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(altTextName)
        ParameterAsserts.assertSpecificLang(specificLang)

        genericLang = if (genericLang != null) Utils.normalizeLangValue(genericLang) else null
        specificLang = Utils.normalizeLangValue(specificLang)

        val arrayPath = XMPPathParser.expandXPath(schemaNS, altTextName)
        val arrayNode = XMPNodeUtils.findNode(root, arrayPath, false, null) ?: return null

        val result = XMPNodeUtils.chooseLocalizedText(arrayNode, genericLang, specificLang)
        val match = (result[0] as Int).toInt()
        val itemNode = result[1] as XMPNode

        if (match != XMPNodeUtils.CLT_NO_VALUES) {
            return object : XMPProperty {
                override fun getValue(): String {
                    return itemNode.value
                }


                override fun getOptions(): PropertyOptions {
                    return itemNode.options
                }


                override fun getLanguage(): String {
                    return itemNode.getQualifier(1).value
                }


                override fun toString(): String {
                    return itemNode.value.toString()
                }
            }
        } else {
            return null
        }
    }


    /**
     * @see XMPMeta.setLocalizedText
     */
    @Throws(XMPException::class)
    override fun setLocalizedText(schemaNS: String, altTextName: String, genericLang: String?,
                                  specificLang: String, itemValue: String, options: PropertyOptions?) {
        var genericLang = genericLang
        var specificLang = specificLang
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(altTextName)
        ParameterAsserts.assertSpecificLang(specificLang)

        genericLang = if (genericLang != null) Utils.normalizeLangValue(genericLang) else null
        specificLang = Utils.normalizeLangValue(specificLang)

        val arrayPath = XMPPathParser.expandXPath(schemaNS, altTextName)

        // Find the array node and set the options if it was just created.
        val arrayNode = XMPNodeUtils.findNode(root, arrayPath, true, PropertyOptions(
                PropertyOptions.ARRAY or PropertyOptions.ARRAY_ORDERED
                        or PropertyOptions.ARRAY_ALTERNATE or PropertyOptions.ARRAY_ALT_TEXT))

        if (arrayNode == null) {
            throw XMPException("Failed to find or create array node", XMPError.BADXPATH)
        } else if (!arrayNode.options.isArrayAltText) {
            if (!arrayNode.hasChildren() && arrayNode.options.isArrayAlternate) {
                arrayNode.options.isArrayAltText = true
            } else {
                throw XMPException(
                        "Specified property is no alt-text array", XMPError.BADXPATH)
            }
        }

        // Make sure the x-default item, if any, is first.
        var haveXDefault = false
        var xdItem: XMPNode? = null

        run {
            val it = arrayNode.iterateChildren()
            while (it.hasNext()) {
                val currItem = it.next() as XMPNode
                if (!currItem.hasQualifier() || XMPConst.XML_LANG != currItem.getQualifier(1).name) {
                    throw XMPException("Language qualifier must be first", XMPError.BADXPATH)
                } else if (XMPConst.X_DEFAULT == currItem.getQualifier(1).value) {
                    xdItem = currItem
                    haveXDefault = true
                    break
                }
            }
        }

        // Moves x-default to the beginning of the array
        if (xdItem != null && arrayNode.childrenLength > 1) {
            arrayNode.removeChild(xdItem)
            arrayNode.addChild(1, xdItem)
        }

        // Find the appropriate item.
        // chooseLocalizedText will make sure the array is a language
        // alternative.
        val result = XMPNodeUtils.chooseLocalizedText(arrayNode, genericLang, specificLang)
        val match = (result[0] as Int).toInt()
        val itemNode = result[1] as XMPNode

        val specificXDefault = XMPConst.X_DEFAULT == specificLang

        when (match) {
            XMPNodeUtils.CLT_NO_VALUES -> {

                // Create the array items for the specificLang and x-default, with
                // x-default first.
                XMPNodeUtils.appendLangItem(arrayNode, XMPConst.X_DEFAULT, itemValue)
                haveXDefault = true
                if (!specificXDefault) {
                    XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue)
                }
            }

            XMPNodeUtils.CLT_SPECIFIC_MATCH ->

                if (!specificXDefault) {
                    // Update the specific item, update x-default if it matches the
                    // old value.
                    if (haveXDefault && xdItem !== itemNode && xdItem != null
                            && xdItem!!.value == itemNode.value) {
                        xdItem!!.value = itemValue
                    }
                    // ! Do this after the x-default check!
                    itemNode.value = itemValue
                } else {
                    // Update all items whose values match the old x-default value.
                    assert(haveXDefault && xdItem === itemNode)
                    val it = arrayNode.iterateChildren()
                    while (it.hasNext()) {
                        val currItem = it.next() as XMPNode
                        if (currItem === xdItem || currItem.value != (if (xdItem != null) xdItem!!.value else null)) {
                            continue
                        }
                        currItem.value = itemValue
                    }
                    // And finally do the x-default item.
                    if (xdItem != null) {
                        xdItem!!.value = itemValue
                    }
                }

            XMPNodeUtils.CLT_SINGLE_GENERIC -> {

                // Update the generic item, update x-default if it matches the old
                // value.
                if (haveXDefault && xdItem !== itemNode && xdItem != null
                        && xdItem!!.value == itemNode.value) {
                    xdItem!!.value = itemValue
                }
                itemNode.value = itemValue // ! Do this after
            }

            XMPNodeUtils.CLT_MULTIPLE_GENERIC -> {

                // Create the specific language, ignore x-default.
                XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue)
                if (specificXDefault) {
                    haveXDefault = true
                }
            }

            XMPNodeUtils.CLT_XDEFAULT -> {

                // Create the specific language, update x-default if it was the only
                // item.
                if (xdItem != null && arrayNode.childrenLength == 1) {
                    xdItem!!.value = itemValue
                }
                XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue)
            }

            XMPNodeUtils.CLT_FIRST_ITEM -> {

                // Create the specific language, don't add an x-default item.
                XMPNodeUtils.appendLangItem(arrayNode, specificLang, itemValue)
                if (specificXDefault) {
                    haveXDefault = true
                }
            }

            else -> // does not happen under normal circumstances
                throw XMPException("Unexpected result from ChooseLocalizedText",
                        XMPError.INTERNALFAILURE)
        }// the x-default
        // check!

        // Add an x-default at the front if needed.
        if (!haveXDefault && arrayNode.childrenLength == 1) {
            XMPNodeUtils.appendLangItem(arrayNode, XMPConst.X_DEFAULT, itemValue)
        }
    }


    /**
     * @see XMPMeta.setLocalizedText
     */
    @Throws(XMPException::class)
    override fun setLocalizedText(schemaNS: String, altTextName: String, genericLang: String,
                                  specificLang: String, itemValue: String) {
        setLocalizedText(schemaNS, altTextName, genericLang, specificLang, itemValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.getProperty
     */
    @Throws(XMPException::class)
    override fun getProperty(schemaNS: String, propName: String): XMPProperty {
        return getProperty(schemaNS, propName, VALUE_STRING)
    }


    /**
     * Returns a property, but the result value can be requested. It can be one
     * of [XMPMetaImpl.VALUE_STRING], [XMPMetaImpl.VALUE_BOOLEAN],
     * [XMPMetaImpl.VALUE_INTEGER], [XMPMetaImpl.VALUE_LONG],
     * [XMPMetaImpl.VALUE_DOUBLE], [XMPMetaImpl.VALUE_DATE],
     * [XMPMetaImpl.VALUE_CALENDAR], [XMPMetaImpl.VALUE_BASE64].

     * @see XMPMeta.getProperty
     * @param schemaNS
     * *            a schema namespace
     * *
     * @param propName
     * *            a property name or path
     * *
     * @param valueType
     * *            the type of the value, see VALUE_...
     * *
     * @return Returns an `XMPProperty`
     * *
     * @throws XMPException
     * *             Collects any exception that occurs.
     */
    @Throws(XMPException::class)
    protected fun getProperty(schemaNS: String, propName: String, valueType: Int): XMPProperty? {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertPropName(propName)

        val expPath = XMPPathParser.expandXPath(schemaNS, propName)
        val propNode = XMPNodeUtils.findNode(root, expPath, false, null)

        if (propNode != null) {
            if (valueType != VALUE_STRING && propNode.options.isCompositeProperty) {
                throw XMPException("Property must be simple when a value type is requested",
                        XMPError.BADXPATH)
            }

            val value = evaluateNodeValue(valueType, propNode)

            return object : XMPProperty {
                override fun getValue(): String? {
                    return value?.toString()
                }


                override fun getOptions(): PropertyOptions {
                    return propNode.options
                }


                override fun getLanguage(): String? {
                    return null
                }


                override fun toString(): String {
                    return value!!.toString()
                }
            }
        } else {
            return null
        }
    }


    /**
     * Returns a property, but the result value can be requested.

     * @see XMPMeta.getProperty
     * @param schemaNS
     * *            a schema namespace
     * *
     * @param propName
     * *            a property name or path
     * *
     * @param valueType
     * *            the type of the value, see VALUE_...
     * *
     * @return Returns the node value as an object according to the
     * *         `valueType`.
     * *
     * @throws XMPException
     * *             Collects any exception that occurs.
     */
    @Throws(XMPException::class)
    protected fun getPropertyObject(schemaNS: String, propName: String, valueType: Int): Any? {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertPropName(propName)

        val expPath = XMPPathParser.expandXPath(schemaNS, propName)
        val propNode = XMPNodeUtils.findNode(root, expPath, false, null)

        if (propNode != null) {
            if (valueType != VALUE_STRING && propNode.options.isCompositeProperty) {
                throw XMPException("Property must be simple when a value type is requested",
                        XMPError.BADXPATH)
            }

            return evaluateNodeValue(valueType, propNode)
        } else {
            return null
        }
    }


    /**
     * @see XMPMeta.getPropertyBoolean
     */
    @Throws(XMPException::class)
    override fun getPropertyBoolean(schemaNS: String, propName: String): Boolean? {
        return getPropertyObject(schemaNS, propName, VALUE_BOOLEAN) as Boolean?
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.setPropertyBoolean
     */
    @Throws(XMPException::class)
    override fun setPropertyBoolean(schemaNS: String, propName: String, propValue: Boolean,
                                    options: PropertyOptions) {
        setProperty(schemaNS, propName, if (propValue) XMPConst.TRUESTR else XMPConst.FALSESTR, options)
    }


    /**
     * @see XMPMeta.setPropertyBoolean
     */
    @Throws(XMPException::class)
    override fun setPropertyBoolean(schemaNS: String, propName: String, propValue: Boolean) {
        setProperty(schemaNS, propName, if (propValue) XMPConst.TRUESTR else XMPConst.FALSESTR, null)
    }


    /**
     * @see XMPMeta.getPropertyInteger
     */
    @Throws(XMPException::class)
    override fun getPropertyInteger(schemaNS: String, propName: String): Int? {
        return getPropertyObject(schemaNS, propName, VALUE_INTEGER) as Int?
    }


    /**
     * @see XMPMeta.setPropertyInteger
     */
    @Throws(XMPException::class)
    override fun setPropertyInteger(schemaNS: String, propName: String, propValue: Int,
                                    options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyInteger
     */
    @Throws(XMPException::class)
    override fun setPropertyInteger(schemaNS: String, propName: String, propValue: Int) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @see XMPMeta.getPropertyLong
     */
    @Throws(XMPException::class)
    override fun getPropertyLong(schemaNS: String, propName: String): Long? {
        return getPropertyObject(schemaNS, propName, VALUE_LONG) as Long?
    }


    /**
     * @see XMPMeta.setPropertyLong
     */
    @Throws(XMPException::class)
    override fun setPropertyLong(schemaNS: String, propName: String, propValue: Long,
                                 options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyLong
     */
    @Throws(XMPException::class)
    override fun setPropertyLong(schemaNS: String, propName: String, propValue: Long) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @see XMPMeta.getPropertyDouble
     */
    @Throws(XMPException::class)
    override fun getPropertyDouble(schemaNS: String, propName: String): Double? {
        return getPropertyObject(schemaNS, propName, VALUE_DOUBLE) as Double?
    }


    /**
     * @see XMPMeta.setPropertyDouble
     */
    @Throws(XMPException::class)
    override fun setPropertyDouble(schemaNS: String, propName: String, propValue: Double,
                                   options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyDouble
     */
    @Throws(XMPException::class)
    override fun setPropertyDouble(schemaNS: String, propName: String, propValue: Double) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @see XMPMeta.getPropertyDate
     */
    @Throws(XMPException::class)
    override fun getPropertyDate(schemaNS: String, propName: String): XMPDateTime {
        return getPropertyObject(schemaNS, propName, VALUE_DATE) as XMPDateTime?
    }


    /**
     * @see XMPMeta.setPropertyDate
     */
    @Throws(XMPException::class)
    override fun setPropertyDate(schemaNS: String, propName: String, propValue: XMPDateTime,
                                 options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyDate
     */
    @Throws(XMPException::class)
    override fun setPropertyDate(schemaNS: String, propName: String, propValue: XMPDateTime) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @see XMPMeta.getPropertyCalendar
     */
    @Throws(XMPException::class)
    override fun getPropertyCalendar(schemaNS: String, propName: String): Calendar {
        return getPropertyObject(schemaNS, propName, VALUE_CALENDAR) as Calendar?
    }


    /**
     * @see XMPMeta.setPropertyCalendar
     */
    @Throws(XMPException::class)
    override fun setPropertyCalendar(schemaNS: String, propName: String, propValue: Calendar,
                                     options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyCalendar
     */
    @Throws(XMPException::class)
    override fun setPropertyCalendar(schemaNS: String, propName: String, propValue: Calendar) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @see XMPMeta.getPropertyBase64
     */
    @Throws(XMPException::class)
    override fun getPropertyBase64(schemaNS: String, propName: String): ByteArray {
        return getPropertyObject(schemaNS, propName, VALUE_BASE64) as ByteArray?
    }


    /**
     * @see XMPMeta.getPropertyString
     */
    @Throws(XMPException::class)
    override fun getPropertyString(schemaNS: String, propName: String): String {
        return getPropertyObject(schemaNS, propName, VALUE_STRING) as String?
    }


    /**
     * @see XMPMeta.setPropertyBase64
     */
    @Throws(XMPException::class)
    override fun setPropertyBase64(schemaNS: String, propName: String, propValue: ByteArray,
                                   options: PropertyOptions) {
        setProperty(schemaNS, propName, propValue, options)
    }


    /**
     * @see XMPMeta.setPropertyBase64
     */
    @Throws(XMPException::class)
    override fun setPropertyBase64(schemaNS: String, propName: String, propValue: ByteArray) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.getQualifier
     */
    @Throws(XMPException::class)
    override fun getQualifier(schemaNS: String, propName: String, qualNS: String,
                              qualName: String): XMPProperty {
        // qualNS and qualName are checked inside composeQualfierPath
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertPropName(propName)

        val qualPath = propName + XMPPathFactory.composeQualifierPath(qualNS, qualName)
        return getProperty(schemaNS, qualPath)
    }


    /**
     * @see XMPMeta.getStructField
     */
    @Throws(XMPException::class)
    override fun getStructField(schemaNS: String, structName: String, fieldNS: String,
                                fieldName: String): XMPProperty {
        // fieldNS and fieldName are checked inside composeStructFieldPath
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertStructName(structName)

        val fieldPath = structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName)
        return getProperty(schemaNS, fieldPath)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.iterator
     */
    @Throws(XMPException::class)
    override fun iterator(): XMPIterator {
        return iterator(null, null, null)
    }


    /**
     * @see XMPMeta.iterator
     */
    @Throws(XMPException::class)
    override fun iterator(options: IteratorOptions): XMPIterator {
        return iterator(null, null, options)
    }


    /**
     * @see XMPMeta.iterator
     */
    @Throws(XMPException::class)
    override fun iterator(schemaNS: String?, propName: String?, options: IteratorOptions?): XMPIterator {
        return XMPIteratorImpl(this, schemaNS, propName, options)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.setArrayItem
     */
    @Throws(XMPException::class)
    override fun setArrayItem(schemaNS: String, arrayName: String, itemIndex: Int, itemValue: String,
                              options: PropertyOptions?) {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(arrayName)

        // Just lookup, don't try to create.
        val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)
        val arrayNode = XMPNodeUtils.findNode(root, arrayPath, false, null)

        if (arrayNode != null) {
            doSetArrayItem(arrayNode, itemIndex, itemValue, options, false)
        } else {
            throw XMPException("Specified array does not exist", XMPError.BADXPATH)
        }
    }


    /**
     * @see XMPMeta.setArrayItem
     */
    @Throws(XMPException::class)
    override fun setArrayItem(schemaNS: String, arrayName: String, itemIndex: Int, itemValue: String) {
        setArrayItem(schemaNS, arrayName, itemIndex, itemValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.insertArrayItem
     */
    @Throws(XMPException::class)
    override fun insertArrayItem(schemaNS: String, arrayName: String, itemIndex: Int, itemValue: String,
                                 options: PropertyOptions?) {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertArrayName(arrayName)

        // Just lookup, don't try to create.
        val arrayPath = XMPPathParser.expandXPath(schemaNS, arrayName)
        val arrayNode = XMPNodeUtils.findNode(root, arrayPath, false, null)

        if (arrayNode != null) {
            doSetArrayItem(arrayNode, itemIndex, itemValue, options, true)
        } else {
            throw XMPException("Specified array does not exist", XMPError.BADXPATH)
        }
    }


    /**
     * @see XMPMeta.insertArrayItem
     */
    @Throws(XMPException::class)
    override fun insertArrayItem(schemaNS: String, arrayName: String, itemIndex: Int, itemValue: String) {
        insertArrayItem(schemaNS, arrayName, itemIndex, itemValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.setProperty
     */
    @Throws(XMPException::class)
    override fun setProperty(schemaNS: String, propName: String, propValue: Any,
                             options: PropertyOptions?) {
        var options = options
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertPropName(propName)

        options = XMPNodeUtils.verifySetOptions(options, propValue)

        val expPath = XMPPathParser.expandXPath(schemaNS, propName)

        val propNode = XMPNodeUtils.findNode(root, expPath, true, options)
        if (propNode != null) {
            setNode(propNode, propValue, options, false)
        } else {
            throw XMPException("Specified property does not exist", XMPError.BADXPATH)
        }
    }


    /**
     * @see XMPMeta.setProperty
     */
    @Throws(XMPException::class)
    override fun setProperty(schemaNS: String, propName: String, propValue: Any) {
        setProperty(schemaNS, propName, propValue, null)
    }


    /**
     * @throws XMPException
     * *
     * @see XMPMeta.setQualifier
     */
    @Throws(XMPException::class)
    override fun setQualifier(schemaNS: String, propName: String, qualNS: String, qualName: String,
                              qualValue: String, options: PropertyOptions?) {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertPropName(propName)

        if (!doesPropertyExist(schemaNS, propName)) {
            throw XMPException("Specified property does not exist!", XMPError.BADXPATH)
        }

        val qualPath = propName + XMPPathFactory.composeQualifierPath(qualNS, qualName)
        setProperty(schemaNS, qualPath, qualValue, options)
    }


    /**
     * @see XMPMeta.setQualifier
     */
    @Throws(XMPException::class)
    override fun setQualifier(schemaNS: String, propName: String, qualNS: String, qualName: String,
                              qualValue: String) {
        setQualifier(schemaNS, propName, qualNS, qualName, qualValue, null)

    }


    /**
     * @see XMPMeta.setStructField
     */
    @Throws(XMPException::class)
    override fun setStructField(schemaNS: String, structName: String, fieldNS: String,
                                fieldName: String, fieldValue: String, options: PropertyOptions?) {
        ParameterAsserts.assertSchemaNS(schemaNS)
        ParameterAsserts.assertStructName(structName)

        val fieldPath = structName + XMPPathFactory.composeStructFieldPath(fieldNS, fieldName)
        setProperty(schemaNS, fieldPath, fieldValue, options)
    }


    /**
     * @see XMPMeta.setStructField
     */
    @Throws(XMPException::class)
    override fun setStructField(schemaNS: String, structName: String, fieldNS: String,
                                fieldName: String, fieldValue: String) {
        setStructField(schemaNS, structName, fieldNS, fieldName, fieldValue, null)
    }


    /**
     * @see XMPMeta.getObjectName
     */
    override fun getObjectName(): String {
        return if (root!!.name != null) root!!.name else ""
    }


    /**
     * @see XMPMeta.setObjectName
     */
    override fun setObjectName(name: String) {
        root!!.name = name
    }


    /**
     * @see XMPMeta.getPacketHeader
     */
    override fun getPacketHeader(): String {
        return packetHeader
    }


    /**
     * Sets the packetHeader attributes, only used by the parser.
     * @param packetHeader the processing instruction content
     */
    fun setPacketHeader(packetHeader: String) {
        this.packetHeader = packetHeader
    }


    /**
     * Performs a deep clone of the XMPMeta-object

     * @see java.lang.Object.clone
     */
    override fun clone(): Any {
        val clonedTree = root!!.clone() as XMPNode
        return XMPMetaImpl(clonedTree)
    }


    /**
     * @see XMPMeta.dumpObject
     */
    override fun dumpObject(): String {
        // renders tree recursively
        return root.dumpNode(true)
    }


    /**
     * @see XMPMeta.sort
     */
    override fun sort() {
        this.root!!.sort()
    }


    /**
     * @see XMPMeta.normalize
     */
    @Throws(XMPException::class)
    override fun normalize(options: ParseOptions?) {
        var options = options
        if (options == null) {
            options = ParseOptions()
        }
        XMPNormalizer.process(this, options)
    }



    // -------------------------------------------------------------------------------------
    // private


    /**
     * Locate or create the item node and set the value. Note the index
     * parameter is one-based! The index can be in the range [1..size + 1] or
     * "last()", normalize it and check the insert flags. The order of the
     * normalization checks is important. If the array is empty we end up with
     * an index and location to set item size + 1.

     * @param arrayNode an array node
     * *
     * @param itemIndex the index where to insert the item
     * *
     * @param itemValue the item value
     * *
     * @param itemOptions the options for the new item
     * *
     * @param insert insert oder overwrite at index position?
     * *
     * @throws XMPException
     */
    @Throws(XMPException::class)
    private fun doSetArrayItem(arrayNode: XMPNode, itemIndex: Int, itemValue: String,
                               itemOptions: PropertyOptions, insert: Boolean) {
        var itemIndex = itemIndex
        var itemOptions = itemOptions
        val itemNode = XMPNode(XMPConst.ARRAY_ITEM_NAME, null)
        itemOptions = XMPNodeUtils.verifySetOptions(itemOptions, itemValue)

        // in insert mode the index after the last is allowed,
        // even ARRAY_LAST_ITEM points to the index *after* the last.
        val maxIndex = if (insert) arrayNode.childrenLength + 1 else arrayNode.childrenLength
        if (itemIndex == XMPConst.ARRAY_LAST_ITEM) {
            itemIndex = maxIndex
        }

        if (1 <= itemIndex && itemIndex <= maxIndex) {
            if (!insert) {
                arrayNode.removeChild(itemIndex)
            }
            arrayNode.addChild(itemIndex, itemNode)
            setNode(itemNode, itemValue, itemOptions, false)
        } else {
            throw XMPException("Array index out of bounds", XMPError.BADINDEX)
        }
    }


    /**
     * The internals for setProperty() and related calls, used after the node is
     * found or created.

     * @param node
     * *            the newly created node
     * *
     * @param value
     * *            the node value, can be `null`
     * *
     * @param newOptions
     * *            options for the new node, must not be `null`.
     * *
     * @param deleteExisting flag if the existing value is to be overwritten
     * *
     * @throws XMPException thrown if options and value do not correspond
     */
    @Throws(XMPException::class)
    internal fun setNode(node: XMPNode, value: Any?, newOptions: PropertyOptions, deleteExisting: Boolean) {
        if (deleteExisting) {
            node.clear()
        }

        // its checked by setOptions(), if the merged result is a valid options set
        node.options.mergeWith(newOptions)

        if (!node.options.isCompositeProperty) {
            // This is setting the value of a leaf node.
            XMPNodeUtils.setNodeValue(node, value)
        } else {
            if (value != null && value.toString().length > 0) {
                throw XMPException("Composite nodes can't have values", XMPError.BADXPATH)
            }

            node.removeChildren()
        }

    }


    /**
     * Evaluates a raw node value to the given value type, apply special
     * conversions for defined types in XMP.

     * @param valueType
     * *            an int indicating the value type
     * *
     * @param propNode
     * *            the node containing the value
     * *
     * @return Returns a literal value for the node.
     * *
     * @throws XMPException
     */
    @Throws(XMPException::class)
    private fun evaluateNodeValue(valueType: Int, propNode: XMPNode): Any? {
        val value: Any
        val rawValue = propNode.value
        when (valueType) {
            VALUE_BOOLEAN -> value = XMPUtils.convertToBoolean(rawValue)
            VALUE_INTEGER -> value = XMPUtils.convertToInteger(rawValue)
            VALUE_LONG -> value = XMPUtils.convertToLong(rawValue)
            VALUE_DOUBLE -> value = XMPUtils.convertToDouble(rawValue)
            VALUE_DATE -> value = XMPUtils.convertToDate(rawValue)
            VALUE_CALENDAR -> {
                val dt = XMPUtils.convertToDate(rawValue)
                value = dt.calendar
            }
            VALUE_BASE64 -> value = XMPUtils.decodeBase64(rawValue)
            VALUE_STRING,
            else -> // leaf values return empty string instead of null
                // for the other cases the converter methods provides a "null"
                // value.
                // a default value can only occur if this method is made public.
                value = if (rawValue != null || propNode.options.isCompositeProperty) rawValue else ""
        }
        return value
    }

    companion object {
        /** Property values are Strings by default  */
        private val VALUE_STRING = 0
        /**  */
        private val VALUE_BOOLEAN = 1
        /**  */
        private val VALUE_INTEGER = 2
        /**  */
        private val VALUE_LONG = 3
        /**  */
        private val VALUE_DOUBLE = 4
        /**  */
        private val VALUE_DATE = 5
        /**  */
        private val VALUE_CALENDAR = 6
        /**  */
        private val VALUE_BASE64 = 7
    }
}
