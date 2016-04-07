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

import java.util.HashMap

import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException

/**
 * The base class for a collection of 32 flag bits. Individual flags are defined as enum value bit
 * masks. Inheriting classes add convenience accessor methods.

 * @since 24.01.2006
 */
abstract class Options {
    /** the internal int containing all options  */
    /**
     * Is friendly to access it during the tests.
     * @return Returns the options.
     */
    /**
     * @param options The options to set.
     * *
     * @throws XMPException
     */
    var options = 0
        @Throws(XMPException::class)
        set(options) {
            assertOptionsValid(options)
            this.options = options
        }
    /** a map containing the bit names  */
    private var optionNames: Map<Any, Any>? = null


    /**
     * The default constructor.
     */
    constructor() {
        // EMTPY
    }


    /**
     * Constructor with the options bit mask.

     * @param options the options bit mask
     * *
     * @throws XMPException If the options are not correct
     */
    @Throws(XMPException::class)
    constructor(options: Int) {
        assertOptionsValid(options)
        options = options
    }


    /**
     * Resets the options.
     */
    fun clear() {
        options = 0
    }


    /**
     * @param optionBits an option bitmask
     * *
     * @return Returns true, if this object is equal to the given options.
     */
    fun isExactly(optionBits: Int): Boolean {
        return options == optionBits
    }


    /**
     * @param optionBits an option bitmask
     * *
     * @return Returns true, if this object contains all given options.
     */
    fun containsAllOptions(optionBits: Int): Boolean {
        return options and optionBits == optionBits
    }


    /**
     * @param optionBits an option bitmask
     * *
     * @return Returns true, if this object contain at least one of the given options.
     */
    fun containsOneOf(optionBits: Int): Boolean {
        return options and optionBits != 0
    }


    /**
     * @param optionBit the binary bit or bits that are requested
     * *
     * @return Returns if all of the requested bits are set or not.
     */
    protected fun getOption(optionBit: Int): Boolean {
        return options and optionBit != 0
    }


    /**
     * @param optionBits the binary bit or bits that shall be set to the given value
     * *
     * @param value the boolean value to set
     */
    fun setOption(optionBits: Int, value: Boolean) {
        options = if (value) options or optionBits else options and optionBits.inv()
    }


    /**
     * @see Object.equals
     */
    override fun equals(obj: Any?): Boolean {
        return options == (obj as Options).options
    }


    /**
     * @see java.lang.Object.hashCode
     */
    override fun hashCode(): Int {
        return options
    }


    /**
     * Creates a human readable string from the set options. *Note:* This method is quite
     * expensive and should only be used within tests or as
     * @return Returns a String listing all options that are set to `true` by their name,
     * * like &quot;option1 | option4&quot;.
     */
    // clear rightmost one bit
    val optionsString: String
        get() {
            if (options != 0) {
                val sb = StringBuffer()
                var theBits = options
                while (theBits != 0) {
                    val oneLessBit = theBits and theBits - 1
                    val singleBit = theBits xor oneLessBit
                    val bitName = getOptionName(singleBit)
                    sb.append(bitName)
                    if (oneLessBit != 0) {
                        sb.append(" | ")
                    }
                    theBits = oneLessBit
                }
                return sb.toString()
            } else {
                return "<none>"
            }
        }


    /**
     * @return Returns the options as hex bitmask.
     */
    override fun toString(): String {
        return "0x" + Integer.toHexString(options)
    }


    /**
     * To be implemeted by inheritants.
     * @return Returns a bit mask where all valid option bits are set.
     */
    protected abstract val validOptions: Int


    /**
     * To be implemeted by inheritants.
     * @param option a single, valid option bit.
     * *
     * @return Returns a human readable name for an option bit.
     */
    protected abstract fun defineOptionName(option: Int): String


    /**
     * The inheriting option class can do additional checks on the options.
     * *Note:* For performance reasons this method is only called
     * when setting bitmasks directly.
     * When get- and set-methods are used, this method must be called manually,
     * normally only when the Options-object has been created from a client
     * (it has to be made public therefore).

     * @param options the bitmask to check.
     * *
     * @throws XMPException Thrown if the options are not consistent.
     */
    @Throws(XMPException::class)
    protected open fun assertConsistency(options: Int) {
        // empty, no checks
    }


    /**
     * Checks options before they are set.
     * First it is checked if only defined options are used,
     * second the additional [Options.assertConsistency]-method is called.

     * @param options the options to check
     * *
     * @throws XMPException Thrown if the options are invalid.
     */
    @Throws(XMPException::class)
    private fun assertOptionsValid(options: Int) {
        val invalidOptions = options and validOptions.inv()
        if (invalidOptions == 0) {
            assertConsistency(options)
        } else {
            throw XMPException("The option bit(s) 0x" + Integer.toHexString(invalidOptions)
                    + " are invalid!", XMPError.BADOPTIONS)
        }
    }


    /**
     * Looks up or asks the inherited class for the name of an option bit.
     * Its save that there is only one valid option handed into the method.
     * @param option a single option bit
     * *
     * @return Returns the option name or undefined.
     */
    private fun getOptionName(option: Int): String {
        val optionsNames = procureOptionNames()

        val key = option
        var result: String? = optionsNames[key] as String
        if (result == null) {
            result = defineOptionName(option)
            if (result != null) {
                optionsNames.put(key, result)
            } else {
                result = "<option name not defined>"
            }
        }

        return result
    }


    /**
     * @return Returns the optionNames map and creates it if required.
     */
    private fun procureOptionNames(): MutableMap<Any, Any> {
        if (optionNames == null) {
            optionNames = HashMap()
        }
        return optionNames
    }
}
