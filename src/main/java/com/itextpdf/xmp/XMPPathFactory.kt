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

package com.itextpdf.xmp

import com.itextpdf.xmp.impl.Utils
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathParser

/**
 * Utility services for the metadata object. It has only public static functions, you cannot create
 * an object. These are all functions that layer cleanly on top of the core XMP toolkit.
 *
 *
 * These functions provide support for composing path expressions to deeply nested properties. The
 * functions `XMPMeta` such as `getProperty()`,
 * `getArrayItem()` and `getStructField()` provide easy access to top
 * level simple properties, items in top level arrays, and fields of top level structs. They do not
 * provide convenient access to more complex things like fields several levels deep in a complex
 * struct, or fields within an array of structs, or items of an array that is a field of a struct.
 * These functions can also be used to compose paths to top level array items or struct fields so
 * that you can use the binary accessors like `getPropertyAsInteger()`.
 *
 *
 * You can use these functions is to compose a complete path expression, or all but the last
 * component. Suppose you have a property that is an array of integers within a struct. You can
 * access one of the array items like this:
 *
 *
 *

 *
 * String path = XMPPathFactory.composeStructFieldPath (schemaNS, &quot;Struct&quot;, fieldNS,
 * &quot;Array&quot;);
 * String path += XMPPathFactory.composeArrayItemPath (schemaNS, &quot;Array&quot; index);
 * PropertyInteger result = xmpObj.getPropertyAsInteger(schemaNS, path);
 *

 *  You could also use this code if you want the string form of the integer:
 *

 *
 * String path = XMPPathFactory.composeStructFieldPath (schemaNS, &quot;Struct&quot;, fieldNS,
 * &quot;Array&quot;);
 * PropertyText xmpObj.getArrayItem (schemaNS, path, index);
 *

 *
 *
 *
 * *Note:* It might look confusing that the schemaNS is passed in all of the calls above.
 * This is because the XMP toolkit keeps the top level &quot;schema&quot; namespace separate from
 * the rest of the path expression.
 * *Note:* These methods are much simpler than in the C++-API, they don't check the given
 * path or array indices.

 * @since 25.01.2006
 */
object XMPPathFactory {


    /**
     * Compose the path expression for an item in an array.

     * @param arrayName The name of the array. May be a general path expression, must not be
     * *        `null` or the empty string.
     * *
     * @param itemIndex The index of the desired item. Arrays in XMP are indexed from 1.
     * * 		  0 and below means last array item and renders as `[last()]`.
     * *
     * *
     * @return Returns the composed path basing on fullPath. This will be of the form
     * *         ns:arrayName[i], where &quot;ns&quot; is the prefix for schemaNS and
     * *         &quot;i&quot; is the decimal representation of itemIndex.
     * *
     * @throws XMPException Throws exeption if index zero is used.
     */
    @Throws(XMPException::class)
    fun composeArrayItemPath(arrayName: String, itemIndex: Int): String {
        if (itemIndex > 0) {
            return "$arrayName[$itemIndex]"
        } else if (itemIndex == XMPConst.ARRAY_LAST_ITEM) {
            return arrayName + "[last()]"
        } else {
            throw XMPException("Array index must be larger than zero", XMPError.BADINDEX)
        }
    }


    /**
     * Compose the path expression for a field in a struct. The result can be added to the
     * path of


     * @param fieldNS The namespace URI for the field. Must not be `null` or the empty
     * *        string.
     * *
     * @param fieldName The name of the field. Must be a simple XML name, must not be
     * *        `null` or the empty string.
     * *
     * @return Returns the composed path. This will be of the form
     * *         ns:structName/fNS:fieldName, where &quot;ns&quot; is the prefix for
     * *         schemaNS and &quot;fNS&quot; is the prefix for fieldNS.
     * *
     * @throws XMPException Thrown if the path to create is not valid.
     */
    @Throws(XMPException::class)
    fun composeStructFieldPath(fieldNS: String,
                               fieldName: String): String {
        assertFieldNS(fieldNS)
        assertFieldName(fieldName)

        val fieldPath = XMPPathParser.expandXPath(fieldNS, fieldName)
        if (fieldPath.size() != 2) {
            throw XMPException("The field name must be simple", XMPError.BADXPATH)
        }

        return '/' + fieldPath.getSegment(XMPPath.STEP_ROOT_PROP).name!!
    }


    /**
     * Compose the path expression for a qualifier.

     * @param qualNS The namespace URI for the qualifier. May be `null` or the empty
     * *        string if the qualifier is in the XML empty namespace.
     * *
     * @param qualName The name of the qualifier. Must be a simple XML name, must not be
     * *        `null` or the empty string.
     * *
     * @return Returns the composed path. This will be of the form
     * *         ns:propName/?qNS:qualName, where &quot;ns&quot; is the prefix for
     * *         schemaNS and &quot;qNS&quot; is the prefix for qualNS.
     * *
     * @throws XMPException Thrown if the path to create is not valid.
     */
    @Throws(XMPException::class)
    fun composeQualifierPath(
            qualNS: String,
            qualName: String): String {
        assertQualNS(qualNS)
        assertQualName(qualName)

        val qualPath = XMPPathParser.expandXPath(qualNS, qualName)
        if (qualPath.size() != 2) {
            throw XMPException("The qualifier name must be simple", XMPError.BADXPATH)
        }

        return "/?" + qualPath.getSegment(XMPPath.STEP_ROOT_PROP).name!!
    }


    /**
     * Compose the path expression to select an alternate item by language. The
     * path syntax allows two forms of &quot;content addressing&quot; that may
     * be used to select an item in an array of alternatives. The form used in
     * ComposeLangSelector lets you select an item in an alt-text array based on
     * the value of its xml:lang qualifier. The other form of content
     * addressing is shown in ComposeFieldSelector. \note ComposeLangSelector
     * does not supplant SetLocalizedText or GetLocalizedText. They should
     * generally be used, as they provide extra logic to choose the appropriate
     * language and maintain consistency with the 'x-default' value.
     * ComposeLangSelector gives you an path expression that is explicitly and
     * only for the language given in the langName parameter.

     * @param arrayName
     * *            The name of the array. May be a general path expression, must
     * *            not be `null` or the empty string.
     * *
     * @param langName
     * *            The RFC 3066 code for the desired language.
     * *
     * @return Returns the composed path. This will be of the form
     * *         ns:arrayName[@xml:lang='langName'], where
     * *         &quot;ns&quot; is the prefix for schemaNS.
     */
    fun composeLangSelector(arrayName: String,
                            langName: String): String {
        return arrayName + "[?xml:lang=\"" + Utils.normalizeLangValue(langName) + "\"]"
    }


    /**
     * Compose the path expression to select an alternate item by a field's value. The path syntax
     * allows two forms of &quot;content addressing&quot; that may be used to select an item in an
     * array of alternatives. The form used in ComposeFieldSelector lets you select an item in an
     * array of structs based on the value of one of the fields in the structs. The other form of
     * content addressing is shown in ComposeLangSelector. For example, consider a simple struct
     * that has two fields, the name of a city and the URI of an FTP site in that city. Use this to
     * create an array of download alternatives. You can show the user a popup built from the values
     * of the city fields. You can then get the corresponding URI as follows:
     *
     *
     *

     *
     * String path = composeFieldSelector ( schemaNS, &quot;Downloads&quot;, fieldNS,
     * &quot;City&quot;, chosenCity );
     * XMPProperty prop = xmpObj.getStructField ( schemaNS, path, fieldNS, &quot;URI&quot; );
     *

     *

     * @param arrayName The name of the array. May be a general path expression, must not be
     * *        `null` or the empty string.
     * *
     * @param fieldNS The namespace URI for the field used as the selector. Must not be
     * *        `null` or the empty string.
     * *
     * @param fieldName The name of the field used as the selector. Must be a simple XML name, must
     * *        not be `null` or the empty string. It must be the name of a field that is
     * *        itself simple.
     * *
     * @param fieldValue The desired value of the field.
     * *
     * @return Returns the composed path. This will be of the form
     * *         ns:arrayName[fNS:fieldName='fieldValue'], where &quot;ns&quot; is the
     * *         prefix for schemaNS and &quot;fNS&quot; is the prefix for fieldNS.
     * *
     * @throws XMPException Thrown if the path to create is not valid.
     */
    @Throws(XMPException::class)
    fun composeFieldSelector(arrayName: String, fieldNS: String,
                             fieldName: String, fieldValue: String): String {
        val fieldPath = XMPPathParser.expandXPath(fieldNS, fieldName)
        if (fieldPath.size() != 2) {
            throw XMPException("The fieldName name must be simple", XMPError.BADXPATH)
        }

        return arrayName + '[' + fieldPath.getSegment(XMPPath.STEP_ROOT_PROP).name +
                "=\"" + fieldValue + "\"]"
    }


    /**
     * ParameterAsserts that a qualifier namespace is set.
     * @param qualNS a qualifier namespace
     * *
     * @throws XMPException Qualifier schema is null or empty
     */
    @Throws(XMPException::class)
    private fun assertQualNS(qualNS: String?) {
        if (qualNS == null || qualNS.length == 0) {
            throw XMPException("Empty qualifier namespace URI", XMPError.BADSCHEMA)
        }

    }


    /**
     * ParameterAsserts that a qualifier name is set.
     * @param qualName a qualifier name or path
     * *
     * @throws XMPException Qualifier name is null or empty
     */
    @Throws(XMPException::class)
    private fun assertQualName(qualName: String?) {
        if (qualName == null || qualName.length == 0) {
            throw XMPException("Empty qualifier name", XMPError.BADXPATH)
        }
    }


    /**
     * ParameterAsserts that a struct field namespace is set.
     * @param fieldNS a struct field namespace
     * *
     * @throws XMPException Struct field schema is null or empty
     */
    @Throws(XMPException::class)
    private fun assertFieldNS(fieldNS: String?) {
        if (fieldNS == null || fieldNS.length == 0) {
            throw XMPException("Empty field namespace URI", XMPError.BADSCHEMA)
        }

    }


    /**
     * ParameterAsserts that a struct field name is set.
     * @param fieldName a struct field name or path
     * *
     * @throws XMPException Struct field name is null or empty
     */
    @Throws(XMPException::class)
    private fun assertFieldName(fieldName: String?) {
        if (fieldName == null || fieldName.length == 0) {
            throw XMPException("Empty f name", XMPError.BADXPATH)
        }
    }
}
/** Private constructor  */
// EMPTY