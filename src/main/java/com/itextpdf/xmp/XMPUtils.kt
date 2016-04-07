// =================================================================================================
// ADOBE SYSTEMS INCORPORATED
// Copyright 2006 Adobe Systems Incorporated
// All Rights Reserved
//
// NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the terms
// of the Adobe license agreement accompanying it.
// =================================================================================================

package com.itextpdf.xmp

import com.itextpdf.xmp.impl.Base64
import com.itextpdf.xmp.impl.ISO8601Converter
import com.itextpdf.xmp.impl.XMPUtilsImpl
import com.itextpdf.xmp.options.PropertyOptions


/**
 * Utility methods for XMP. I included only those that are different from the
 * Java default conversion utilities.

 * @since 21.02.2006
 */
object XMPUtils {


    /**
     * Create a single edit string from an array of strings.

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
     * @throws XMPException Forwards the Exceptions from the metadata processing
     */
    @Throws(XMPException::class)
    fun catenateArrayItems(xmp: XMPMeta, schemaNS: String, arrayName: String,
                           separator: String, quotes: String, allowCommas: Boolean): String {
        return XMPUtilsImpl.catenateArrayItems(xmp, schemaNS, arrayName, separator, quotes, allowCommas)
    }


    /**
     * Separate a single edit string into an array of strings.

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
     * @param arrayOptions Option flags to control the separation.
     * *
     * @param preserveCommas Flag if commas shall be preserved
     * *
     * @throws XMPException Forwards the Exceptions from the metadata processing
     */
    @Throws(XMPException::class)
    fun separateArrayItems(xmp: XMPMeta, schemaNS: String, arrayName: String,
                           catedStr: String, arrayOptions: PropertyOptions, preserveCommas: Boolean) {
        XMPUtilsImpl.separateArrayItems(xmp, schemaNS, arrayName, catedStr, arrayOptions,
                preserveCommas)
    }


    /**
     * Remove multiple properties from an XMP object.

     * RemoveProperties was created to support the File Info dialog's Delete
     * button, and has been been generalized somewhat from those specific needs.
     * It operates in one of three main modes depending on the schemaNS and
     * propName parameters:

     *
     *  *  Non-empty `schemaNS` and `propName` - The named property is
     * removed if it is an external property, or if the
     * flag `doAllProperties` option is true. It does not matter whether the
     * named property is an actual property or an alias.

     *  *  Non-empty `schemaNS` and empty `propName` - The all external
     * properties in the named schema are removed. Internal properties are also
     * removed if the flag `doAllProperties` option is set. In addition,
     * aliases from the named schema will be removed if the flag `includeAliases`
     * option is set.

     *  *  Empty `schemaNS` and empty `propName` - All external properties in
     * all schema are removed. Internal properties are also removed if the
     * flag `doAllProperties` option is passed. Aliases are implicitly handled
     * because the associated actuals are internal if the alias is.
     *

     * It is an error to pass an empty `schemaNS` and non-empty `propName`.

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
     * @param doAllProperties Option flag to control the deletion: do internal properties in
     * *          addition to external properties.
     * *
     * *
     * @param includeAliases Option flag to control the deletion:
     * * 			Include aliases in the "named schema" case above.
     * * 			*Note:* Currently not supported.
     * *
     * @throws XMPException Forwards the Exceptions from the metadata processing
     */
    @Throws(XMPException::class)
    fun removeProperties(xmp: XMPMeta, schemaNS: String, propName: String,
                         doAllProperties: Boolean, includeAliases: Boolean) {
        XMPUtilsImpl.removeProperties(xmp, schemaNS, propName, doAllProperties, includeAliases)
    }


    /**
     *
     * Append properties from one XMP object to another.

     *
     * XMPUtils#appendProperties was created to support the File Info dialog's Append button, and
     * has been been generalized somewhat from those specific needs. It appends information from one
     * XMP object (source) to another (dest). The default operation is to append only external
     * properties that do not already exist in the destination. The flag
     * `doAllProperties` can be used to operate on all properties, external and internal.
     * The flag `replaceOldValues` option can be used to replace the values
     * of existing properties. The notion of external
     * versus internal applies only to top level properties. The keep-or-replace-old notion applies
     * within structs and arrays as described below.
     *
     *  * If `replaceOldValues` is true then the processing is restricted to the top
     * level properties. The processed properties from the source (according to
     * `doAllProperties`) are propagated to the destination,
     * replacing any existing values.Properties in the destination that are not in the source
     * are left alone.

     *  * If `replaceOldValues` is not passed then the processing is more complicated.
     * Top level properties are added to the destination if they do not already exist.
     * If they do exist but differ in form (simple/struct/array) then the destination is left alone.
     * If the forms match, simple properties are left unchanged while structs and arrays are merged.

     *  * If `deleteEmptyValues` is passed then an empty value in the source XMP causes
     * the corresponding destination XMP property to be deleted. The default is to treat empty
     * values the same as non-empty values. An empty value is any of a simple empty string, an array
     * with no items, or a struct with no fields. Qualifiers are ignored.
     *

     *
     * The detailed behavior is defined by the following pseudo-code:
     *
     *
     * appendProperties ( sourceXMP, destXMP, doAllProperties,
     * replaceOldValues, deleteEmptyValues ):
     * for all source schema (top level namespaces):
     * for all top level properties in sourceSchema:
     * if doAllProperties or prop is external:
     * appendSubtree ( sourceNode, destSchema, replaceOldValues, deleteEmptyValues )

     * appendSubtree ( sourceNode, destParent, replaceOldValues, deleteEmptyValues ):
     * if deleteEmptyValues and source value is empty:
     * delete the corresponding child from destParent
     * else if sourceNode not in destParent (by name):
     * copy sourceNode's subtree to destParent
     * else if replaceOld:
     * delete subtree from destParent
     * copy sourceNode's subtree to destParent
     * else:
     * // Already exists in dest and not replacing, merge structs and arrays
     * if sourceNode and destNode forms differ:
     * return, leave the destNode alone
     * else if form is a struct:
     * for each field in sourceNode:
     * AppendSubtree ( sourceNode.field, destNode, replaceOldValues )
     * else if form is an alt-text array:
     * copy new items by "xml:lang" value into the destination
     * else if form is an array:
     * copy new items by value into the destination, ignoring order and duplicates
     *
     *

     *
     * *Note:* appendProperties can be expensive if replaceOldValues is not passed and
     * the XMP contains large arrays. The array item checking described above is n-squared.
     * Each source item is checked to see if it already exists in the destination,
     * without regard to order or duplicates.
     *
     * Simple items are compared by value and "xml:lang" qualifier, other qualifiers are ignored.
     * Structs are recursively compared by field names, without regard to field order. Arrays are
     * compared by recursively comparing all items.

     * @param source The source XMP object.
     * *
     * @param dest The destination XMP object.
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
    @JvmOverloads fun appendProperties(source: XMPMeta, dest: XMPMeta, doAllProperties: Boolean,
                                       replaceOldValues: Boolean, deleteEmptyValues: Boolean = false) {
        XMPUtilsImpl.appendProperties(source, dest, doAllProperties, replaceOldValues,
                deleteEmptyValues)
    }


    /**
     * Convert from string to Boolean.

     * @param value
     * *            The string representation of the Boolean.
     * *
     * @return The appropriate boolean value for the string. The checked values
     * *         for `true` and `false` are:
     * *
     * *    	    	 * [XMPConst.TRUESTR] and [XMPConst.FALSESTR]
     * *    		     * &quot;t&quot; and &quot;f&quot;
     * *    		     * &quot;on&quot; and &quot;off&quot;
     * *    		     * &quot;yes&quot; and &quot;no&quot;
     * *   		  	 * &quot;value <> 0&quot; and &quot;value == 0&quot;
     * *
     * *
     * @throws XMPException If an empty string is passed.
     */
    @Throws(XMPException::class)
    fun convertToBoolean(value: String?): Boolean {
        var value = value
        if (value == null || value.length == 0) {
            throw XMPException("Empty convert-string", XMPError.BADVALUE)
        }
        value = value.toLowerCase()

        try {
            // First try interpretation as Integer (anything not 0 is true)
            return Integer.parseInt(value) != 0
        } catch (e: NumberFormatException) {
            return "true" == value ||
                    "t" == value ||
                    "on" == value ||
                    "yes" == value
        }

    }


    /**
     * Convert from boolean to string.

     * @param value
     * *            a boolean value
     * *
     * @return The XMP string representation of the boolean. The values used are
     * *         given by the constnts [XMPConst.TRUESTR] and
     * *         [XMPConst.FALSESTR].
     */
    fun convertFromBoolean(value: Boolean): String {
        return if (value) XMPConst.TRUESTR else XMPConst.FALSESTR
    }


    /**
     * Converts a string value to an `int`.

     * @param rawValue
     * *            the string value
     * *
     * @return Returns an int.
     * *
     * @throws XMPException
     * *             If the `rawValue` is `null` or empty or the
     * *             conversion fails.
     */
    @Throws(XMPException::class)
    fun convertToInteger(rawValue: String?): Int {
        try {
            if (rawValue == null || rawValue.length == 0) {
                throw XMPException("Empty convert-string", XMPError.BADVALUE)
            }
            if (rawValue.startsWith("0x")) {
                return Integer.parseInt(rawValue.substring(2), 16)
            } else {
                return Integer.parseInt(rawValue)
            }
        } catch (e: NumberFormatException) {
            throw XMPException("Invalid integer string", XMPError.BADVALUE)
        }

    }


    /**
     * Convert from int to string.

     * @param value
     * *            an int value
     * *
     * @return The string representation of the int.
     */
    fun convertFromInteger(value: Int): String {
        return value.toString()
    }


    /**
     * Converts a string value to a `long`.

     * @param rawValue
     * *            the string value
     * *
     * @return Returns a long.
     * *
     * @throws XMPException
     * *             If the `rawValue` is `null` or empty or the
     * *             conversion fails.
     */
    @Throws(XMPException::class)
    fun convertToLong(rawValue: String?): Long {
        try {
            if (rawValue == null || rawValue.length == 0) {
                throw XMPException("Empty convert-string", XMPError.BADVALUE)
            }
            if (rawValue.startsWith("0x")) {
                return java.lang.Long.parseLong(rawValue.substring(2), 16)
            } else {
                return java.lang.Long.parseLong(rawValue)
            }
        } catch (e: NumberFormatException) {
            throw XMPException("Invalid long string", XMPError.BADVALUE)
        }

    }


    /**
     * Convert from long to string.

     * @param value
     * *            a long value
     * *
     * @return The string representation of the long.
     */
    fun convertFromLong(value: Long): String {
        return value.toString()
    }


    /**
     * Converts a string value to a `double`.

     * @param rawValue
     * *            the string value
     * *
     * @return Returns a double.
     * *
     * @throws XMPException
     * *             If the `rawValue` is `null` or empty or the
     * *             conversion fails.
     */
    @Throws(XMPException::class)
    fun convertToDouble(rawValue: String?): Double {
        try {
            if (rawValue == null || rawValue.length == 0) {
                throw XMPException("Empty convert-string", XMPError.BADVALUE)
            } else {
                return java.lang.Double.parseDouble(rawValue)
            }
        } catch (e: NumberFormatException) {
            throw XMPException("Invalid double string", XMPError.BADVALUE)
        }

    }


    /**
     * Convert from long to string.

     * @param value
     * *            a long value
     * *
     * @return The string representation of the long.
     */
    fun convertFromDouble(value: Double): String {
        return value.toString()
    }


    /**
     * Converts a string value to an `XMPDateTime`.

     * @param rawValue
     * *            the string value
     * *
     * @return Returns an `XMPDateTime`-object.
     * *
     * @throws XMPException
     * *             If the `rawValue` is `null` or empty or the
     * *             conversion fails.
     */
    @Throws(XMPException::class)
    fun convertToDate(rawValue: String?): XMPDateTime {
        if (rawValue == null || rawValue.length == 0) {
            throw XMPException("Empty convert-string", XMPError.BADVALUE)
        } else {
            return ISO8601Converter.parse(rawValue)
        }
    }


    /**
     * Convert from `XMPDateTime` to string.

     * @param value
     * *            an `XMPDateTime`
     * *
     * @return The string representation of the long.
     */
    fun convertFromDate(value: XMPDateTime): String {
        return ISO8601Converter.render(value)
    }


    /**
     * Convert from a byte array to a base64 encoded string.

     * @param buffer
     * *            the byte array to be converted
     * *
     * @return Returns the base64 string.
     */
    fun encodeBase64(buffer: ByteArray): String {
        return String(Base64.encode(buffer))
    }


    /**
     * Decode from Base64 encoded string to raw data.

     * @param base64String
     * *            a base64 encoded string
     * *
     * @return Returns a byte array containg the decoded string.
     * *
     * @throws XMPException Thrown if the given string is not property base64 encoded
     */
    @Throws(XMPException::class)
    fun decodeBase64(base64String: String): ByteArray {
        try {
            return Base64.decode(base64String.toByteArray())
        } catch (e: Throwable) {
            throw XMPException("Invalid base64 string", XMPError.BADVALUE, e)
        }

    }
}
/** Private constructor  */
// EMPTY
/**
 * Alias without the new option `deleteEmptyValues`.
 * @param source The source XMP object.
 * *
 * @param dest The destination XMP object.
 * *
 * @param doAllProperties Do internal properties in addition to external properties.
 * *
 * @param replaceOldValues Replace the values of existing properties.
 * *
 * @throws XMPException Forwards the Exceptions from the metadata processing
 */