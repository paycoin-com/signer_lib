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

package com.itextpdf.xmp.options

import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException


/**
 * The property flags are used when properties are fetched from the `XMPMeta`-object
 * and provide more detailed information about the property.

 * @since   03.07.2006
 */
class PropertyOptions : Options {



    /**
     * Default constructor
     */
    constructor() {
        // reveal default constructor
    }


    /**
     * Intialization constructor

     * @param options the initialization options
     * *
     * @throws XMPException If the options are not valid
     */
    @Throws(XMPException::class)
    constructor(options: Int) : super(options) {
    }


    /**
     * @return Return whether the property value is a URI. It is serialized to RDF using the
     * *         rdf:resource attribute. Not mandatory for URIs, but considered RDF-savvy.
     */
    val isURI: Boolean
        get() = getOption(URI)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setURI(value: Boolean): PropertyOptions {
        setOption(URI, value)
        return this
    }


    /**
     * @return Return whether the property has qualifiers. These could be an xml:lang
     * *         attribute, an rdf:type property, or a general qualifier. See the
     * *         introductory discussion of qualified properties for more information.
     */
    val hasQualifiers: Boolean
        get() = getOption(HAS_QUALIFIERS)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setHasQualifiers(value: Boolean): PropertyOptions {
        setOption(HAS_QUALIFIERS, value)
        return this
    }


    /**
     * @return Return whether this property is a qualifier for some other property. Note that if the
     * *         qualifier itself has a structured value, this flag is only set for the top node of
     * *         the qualifier's subtree. Qualifiers may have arbitrary structure, and may even have
     * *         qualifiers.
     */
    val isQualifier: Boolean
        get() = getOption(QUALIFIER)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setQualifier(value: Boolean): PropertyOptions {
        setOption(QUALIFIER, value)
        return this
    }


    /** @return Return whether this property has an xml:lang qualifier.
     */
    val hasLanguage: Boolean
        get() = getOption(HAS_LANGUAGE)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setHasLanguage(value: Boolean): PropertyOptions {
        setOption(HAS_LANGUAGE, value)
        return this
    }


    /** @return Return whether this property has an rdf:type qualifier.
     */
    val hasType: Boolean
        get() = getOption(HAS_TYPE)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setHasType(value: Boolean): PropertyOptions {
        setOption(HAS_TYPE, value)
        return this
    }


    /** @return Return whether this property contains nested fields.
     */
    val isStruct: Boolean
        get() = getOption(STRUCT)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setStruct(value: Boolean): PropertyOptions {
        setOption(STRUCT, value)
        return this
    }


    /**
     * @return Return whether this property is an array. By itself this indicates a general
     * *         unordered array. It is serialized using an rdf:Bag container.
     */
    val isArray: Boolean
        get() = getOption(ARRAY)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setArray(value: Boolean): PropertyOptions {
        setOption(ARRAY, value)
        return this
    }


    /**
     * @return Return whether this property is an ordered array. Appears in conjunction with
     * *         getPropValueIsArray(). It is serialized using an rdf:Seq container.
     */
    val isArrayOrdered: Boolean
        get() = getOption(ARRAY_ORDERED)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setArrayOrdered(value: Boolean): PropertyOptions {
        setOption(ARRAY_ORDERED, value)
        return this
    }


    /**
     * @return Return whether this property is an alternative array. Appears in conjunction with
     * *         getPropValueIsArray(). It is serialized using an rdf:Alt container.
     */
    val isArrayAlternate: Boolean
        get() = getOption(ARRAY_ALTERNATE)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setArrayAlternate(value: Boolean): PropertyOptions {
        setOption(ARRAY_ALTERNATE, value)
        return this
    }


    /**
     * @return Return whether this property is an alt-text array. Appears in conjunction with
     * *         getPropArrayIsAlternate(). It is serialized using an rdf:Alt container.
     * *         Each array element is a simple property with an xml:lang attribute.
     */
    val isArrayAltText: Boolean
        get() = getOption(ARRAY_ALT_TEXT)


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setArrayAltText(value: Boolean): PropertyOptions {
        setOption(ARRAY_ALT_TEXT, value)
        return this
    }


    /**
     * @param value the value to set
     * *
     * @return Returns this to enable cascaded options.
     */


    /**
     * @return Returns whether the SCHEMA_NODE option is set.
     */
    val isSchemaNode: Boolean
        get() = getOption(SCHEMA_NODE)


    /**
     * @param value the option DELETE_EXISTING to set
     * *
     * @return Returns this to enable cascaded options.
     */
    fun setSchemaNode(value: Boolean): PropertyOptions {
        setOption(SCHEMA_NODE, value)
        return this
    }


    //-------------------------------------------------------------------------- convenience methods

    /**
     * @return Returns whether the property is of composite type - an array or a struct.
     */
    val isCompositeProperty: Boolean
        get() = options and (ARRAY or STRUCT) > 0


    /**
     * @return Returns whether the property is of composite type - an array or a struct.
     */
    val isSimple: Boolean
        get() = options and (ARRAY or STRUCT) == 0


    /**
     * Compares two options set for array compatibility.

     * @param options other options
     * *
     * @return Returns true if the array options of the sets are equal.
     */
    fun equalArrayTypes(options: PropertyOptions): Boolean {
        return isArray == options.isArray &&
                isArrayOrdered == options.isArrayOrdered &&
                isArrayAlternate == options.isArrayAlternate &&
                isArrayAltText == options.isArrayAltText
    }


    /**
     * Merges the set options of a another options object with this.
     * If the other options set is null, this objects stays the same.
     * @param options other options
     * *
     * @throws XMPException If illegal options are provided
     */
    @Throws(XMPException::class)
    fun mergeWith(options: PropertyOptions?) {
        if (options != null) {
            options = options or options.options
        }
    }


    /**
     * @return Returns true if only array options are set.
     */
    val isOnlyArrayOptions: Boolean
        get() = options and (ARRAY or ARRAY_ORDERED or ARRAY_ALTERNATE or ARRAY_ALT_TEXT).inv() == 0


    /**
     * @see Options.getValidOptions
     */
    protected override val validOptions: Int
        get() = URI or
                HAS_QUALIFIERS or
                QUALIFIER or
                HAS_LANGUAGE or
                HAS_TYPE or
                STRUCT or
                ARRAY or
                ARRAY_ORDERED or
                ARRAY_ALTERNATE or
                ARRAY_ALT_TEXT or
                SCHEMA_NODE or
                SEPARATE_NODE


    /**
     * @see Options.defineOptionName
     */
    override fun defineOptionName(option: Int): String? {
        when (option) {
            URI -> return "URI"
            HAS_QUALIFIERS -> return "HAS_QUALIFIER"
            QUALIFIER -> return "QUALIFIER"
            HAS_LANGUAGE -> return "HAS_LANGUAGE"
            HAS_TYPE -> return "HAS_TYPE"
            STRUCT -> return "STRUCT"
            ARRAY -> return "ARRAY"
            ARRAY_ORDERED -> return "ARRAY_ORDERED"
            ARRAY_ALTERNATE -> return "ARRAY_ALTERNATE"
            ARRAY_ALT_TEXT -> return "ARRAY_ALT_TEXT"
            SCHEMA_NODE -> return "SCHEMA_NODE"
            else -> return null
        }
    }


    /**
     * Checks that a node not a struct and array at the same time;
     * and URI cannot be a struct.

     * @param options the bitmask to check.
     * *
     * @throws XMPException Thrown if the options are not consistent.
     */
    @Throws(XMPException::class)
    public override fun assertConsistency(options: Int) {
        if (options and STRUCT > 0 && options and ARRAY > 0) {
            throw XMPException("IsStruct and IsArray options are mutually exclusive",
                    XMPError.BADOPTIONS)
        } else if (options and URI > 0 && options and (ARRAY or STRUCT) > 0) {
            throw XMPException("Structs and arrays can't have \"value\" options",
                    XMPError.BADOPTIONS)
        }
    }

    companion object {
        /**  */
        val NO_OPTIONS = 0x00000000
        /**  */
        val URI = 0x00000002
        /**  */
        val HAS_QUALIFIERS = 0x00000010
        /**  */
        val QUALIFIER = 0x00000020
        /**  */
        val HAS_LANGUAGE = 0x00000040
        /**  */
        val HAS_TYPE = 0x00000080
        /**  */
        val STRUCT = 0x00000100
        /**  */
        val ARRAY = 0x00000200
        /**  */
        val ARRAY_ORDERED = 0x00000400
        /**  */
        val ARRAY_ALTERNATE = 0x00000800
        /**  */
        val ARRAY_ALT_TEXT = 0x00001000
        /**  */
        val SCHEMA_NODE = 0x80000000.toInt()
        /** may be used in the future  */
        val DELETE_EXISTING = 0x20000000
        /** Updated by iText. Indicates if the property should be writted as a separate node  */
        val SEPARATE_NODE = 0x40000000
    }
}