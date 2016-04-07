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


/**
 * Options for `XMPIterator` construction.

 * @since 24.01.2006
 */
class IteratorOptions : Options() {


    /**
     * @return Returns whether the option is set.
     */
    val isJustChildren: Boolean
        get() = getOption(JUST_CHILDREN)


    /**
     * @return Returns whether the option is set.
     */
    val isJustLeafname: Boolean
        get() = getOption(JUST_LEAFNAME)


    /**
     * @return Returns whether the option is set.
     */
    val isJustLeafnodes: Boolean
        get() = getOption(JUST_LEAFNODES)


    /**
     * @return Returns whether the option is set.
     */
    val isOmitQualifiers: Boolean
        get() = getOption(OMIT_QUALIFIERS)


    /**
     * Sets the option and returns the instance.

     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setJustChildren(value: Boolean): IteratorOptions {
        setOption(JUST_CHILDREN, value)
        return this
    }


    /**
     * Sets the option and returns the instance.

     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setJustLeafname(value: Boolean): IteratorOptions {
        setOption(JUST_LEAFNAME, value)
        return this
    }


    /**
     * Sets the option and returns the instance.

     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setJustLeafnodes(value: Boolean): IteratorOptions {
        setOption(JUST_LEAFNODES, value)
        return this
    }


    /**
     * Sets the option and returns the instance.

     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setOmitQualifiers(value: Boolean): IteratorOptions {
        setOption(OMIT_QUALIFIERS, value)
        return this
    }


    /**
     * @see Options.defineOptionName
     */
    override fun defineOptionName(option: Int): String? {
        when (option) {
            JUST_CHILDREN -> return "JUST_CHILDREN"
            JUST_LEAFNODES -> return "JUST_LEAFNODES"
            JUST_LEAFNAME -> return "JUST_LEAFNAME"
            OMIT_QUALIFIERS -> return "OMIT_QUALIFIERS"
            else -> return null
        }
    }


    /**
     * @see Options.getValidOptions
     */
    protected override val validOptions: Int
        get() = JUST_CHILDREN or
                JUST_LEAFNODES or
                JUST_LEAFNAME or
                OMIT_QUALIFIERS

    companion object {
        /** Just do the immediate children of the root, default is subtree.  */
        val JUST_CHILDREN = 0x0100
        /** Just do the leaf nodes, default is all nodes in the subtree.
         * Bugfix #2658965: If this option is set the Iterator returns the namespace
         * of the leaf instead of the namespace of the base property.  */
        val JUST_LEAFNODES = 0x0200
        /** Return just the leaf part of the path, default is the full path.  */
        val JUST_LEAFNAME = 0x0400
        //	/** Include aliases, default is just actual properties. <em>Note:</em> Not supported. 
        //	 *  @deprecated it is commonly preferred to work with the base properties */
        //	public static final int INCLUDE_ALIASES = 0x0800;
        /** Omit all qualifiers.  */
        val OMIT_QUALIFIERS = 0x1000
    }
}