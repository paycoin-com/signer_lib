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

import com.itextpdf.xmp.XMPException


/**
 * Options for XMPSchemaRegistryImpl#registerAlias.

 * @since 20.02.2006
 */
class AliasOptions : Options {


    /**
     * @see Options.Options
     */
    constructor() {
        // EMPTY
    }


    /**
     * @param options the options to init with
     * *
     * @throws XMPException If options are not consistant
     */
    @Throws(XMPException::class)
    constructor(options: Int) : super(options) {
    }


    /**
     * @return Returns if the alias is of the simple form.
     */
    val isSimple: Boolean
        get() = options == PROP_DIRECT


    /**
     * @return Returns the option.
     */
    val isArray: Boolean
        get() = getOption(PROP_ARRAY)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setArray(value: Boolean): AliasOptions {
        setOption(PROP_ARRAY, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val isArrayOrdered: Boolean
        get() = getOption(PROP_ARRAY_ORDERED)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setArrayOrdered(value: Boolean): AliasOptions {
        setOption(PROP_ARRAY or PROP_ARRAY_ORDERED, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val isArrayAlternate: Boolean
        get() = getOption(PROP_ARRAY_ALTERNATE)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setArrayAlternate(value: Boolean): AliasOptions {
        setOption(PROP_ARRAY or PROP_ARRAY_ORDERED or PROP_ARRAY_ALTERNATE, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val isArrayAltText: Boolean
        get() = getOption(PROP_ARRAY_ALT_TEXT)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setArrayAltText(value: Boolean): AliasOptions {
        setOption(PROP_ARRAY or PROP_ARRAY_ORDERED or
                PROP_ARRAY_ALTERNATE or PROP_ARRAY_ALT_TEXT, value)
        return this
    }


    /**
     * @return returns a [PropertyOptions]s object
     * *
     * @throws XMPException If the options are not consistant.
     */
    @Throws(XMPException::class)
    fun toPropertyOptions(): PropertyOptions {
        return PropertyOptions(options)
    }


    /**
     * @see Options.defineOptionName
     */
    override fun defineOptionName(option: Int): String? {
        when (option) {
            PROP_DIRECT -> return "PROP_DIRECT"
            PROP_ARRAY -> return "ARRAY"
            PROP_ARRAY_ORDERED -> return "ARRAY_ORDERED"
            PROP_ARRAY_ALTERNATE -> return "ARRAY_ALTERNATE"
            PROP_ARRAY_ALT_TEXT -> return "ARRAY_ALT_TEXT"
            else -> return null
        }
    }


    /**
     * @see Options.getValidOptions
     */
    protected override val validOptions: Int
        get() = PROP_DIRECT or
                PROP_ARRAY or
                PROP_ARRAY_ORDERED or
                PROP_ARRAY_ALTERNATE or
                PROP_ARRAY_ALT_TEXT

    companion object {
        /** This is a direct mapping. The actual data type does not matter.  */
        val PROP_DIRECT = 0
        /** The actual is an unordered array, the alias is to the first element of the array.  */
        val PROP_ARRAY = PropertyOptions.ARRAY
        /** The actual is an ordered array, the alias is to the first element of the array.  */
        val PROP_ARRAY_ORDERED = PropertyOptions.ARRAY_ORDERED
        /** The actual is an alternate array, the alias is to the first element of the array.  */
        val PROP_ARRAY_ALTERNATE = PropertyOptions.ARRAY_ALTERNATE
        /**
         * The actual is an alternate text array, the alias is to the 'x-default' element of the array.
         */
        val PROP_ARRAY_ALT_TEXT = PropertyOptions.ARRAY_ALT_TEXT
    }
}	