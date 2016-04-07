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
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory


/**
 * Options for [XMPMetaFactory.serializeToBuffer].

 * @since 24.01.2006
 */
class SerializeOptions : Options {

    /**
     * The amount of padding to be added if a writeable XML packet is created. If zero is passed
     * (the default) an appropriate amount of padding is computed.
     */
    private var padding = 2048
    /**
     * The string to be used as a line terminator. If empty it defaults to; linefeed, U+000A, the
     * standard XML newline.
     */
    private var newline = "\n"
    /**
     * The string to be used for each level of indentation in the serialized
     * RDF. If empty it defaults to two ASCII spaces, U+0020.
     */
    private var indent = "  "
    /**
     * The number of levels of indentation to be used for the outermost XML element in the
     * serialized RDF. This is convenient when embedding the RDF in other text, defaults to 0.
     */
    private var baseIndent = 0
    /** Omits the Toolkit version attribute, not published, only used for Unit tests.  */
    /**
     * @return Returns whether the Toolkit version attribute shall be omitted.
     * * *Note:* This options can only be set by unit tests.
     */
    val omitVersionAttribute = false


    /**
     * Default constructor.
     */
    constructor() {
        // reveal default constructor
    }


    /**
     * Constructor using inital options
     * @param options the inital options
     * *
     * @throws XMPException Thrown if options are not consistant.
     */
    @Throws(XMPException::class)
    constructor(options: Int) : super(options) {
    }


    /**
     * @return Returns the option.
     */
    val omitPacketWrapper: Boolean
        get() = getOption(OMIT_PACKET_WRAPPER)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setOmitPacketWrapper(value: Boolean): SerializeOptions {
        setOption(OMIT_PACKET_WRAPPER, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val omitXmpMetaElement: Boolean
        get() = getOption(OMIT_XMPMETA_ELEMENT)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setOmitXmpMetaElement(value: Boolean): SerializeOptions {
        setOption(OMIT_XMPMETA_ELEMENT, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val readOnlyPacket: Boolean
        get() = getOption(READONLY_PACKET)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setReadOnlyPacket(value: Boolean): SerializeOptions {
        setOption(READONLY_PACKET, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val useCompactFormat: Boolean
        get() = getOption(USE_COMPACT_FORMAT)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setUseCompactFormat(value: Boolean): SerializeOptions {
        setOption(USE_COMPACT_FORMAT, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val useCanonicalFormat: Boolean
        get() = getOption(USE_CANONICAL_FORMAT)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setUseCanonicalFormat(value: Boolean): SerializeOptions {
        setOption(USE_CANONICAL_FORMAT, value)
        return this
    }

    /**
     * @return Returns the option.
     */
    val includeThumbnailPad: Boolean
        get() = getOption(INCLUDE_THUMBNAIL_PAD)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setIncludeThumbnailPad(value: Boolean): SerializeOptions {
        setOption(INCLUDE_THUMBNAIL_PAD, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val exactPacketLength: Boolean
        get() = getOption(EXACT_PACKET_LENGTH)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setExactPacketLength(value: Boolean): SerializeOptions {
        setOption(EXACT_PACKET_LENGTH, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val sort: Boolean
        get() = getOption(SORT)


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setSort(value: Boolean): SerializeOptions {
        setOption(SORT, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val encodeUTF16BE: Boolean
        get() = options and ENCODING_MASK == ENCODE_UTF16BE


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setEncodeUTF16BE(value: Boolean): SerializeOptions {
        // clear unicode bits
        setOption(UTF16_BIT or LITTLEENDIAN_BIT, false)
        setOption(ENCODE_UTF16BE, value)
        return this
    }


    /**
     * @return Returns the option.
     */
    val encodeUTF16LE: Boolean
        get() = options and ENCODING_MASK == ENCODE_UTF16LE


    /**
     * @param value the value to set
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setEncodeUTF16LE(value: Boolean): SerializeOptions {
        // clear unicode bits
        setOption(UTF16_BIT or LITTLEENDIAN_BIT, false)
        setOption(ENCODE_UTF16LE, value)
        return this
    }


    /**
     * @return Returns the baseIndent.
     */
    fun getBaseIndent(): Int {
        return baseIndent
    }


    /**
     * @param baseIndent
     * *            The baseIndent to set.
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setBaseIndent(baseIndent: Int): SerializeOptions {
        this.baseIndent = baseIndent
        return this
    }


    /**
     * @return Returns the indent.
     */
    fun getIndent(): String {
        return indent
    }


    /**
     * @param indent
     * *            The indent to set.
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setIndent(indent: String): SerializeOptions {
        this.indent = indent
        return this
    }


    /**
     * @return Returns the newline.
     */
    fun getNewline(): String {
        return newline
    }


    /**
     * @param newline
     * *            The newline to set.
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setNewline(newline: String): SerializeOptions {
        this.newline = newline
        return this
    }


    /**
     * @return Returns the padding.
     */
    fun getPadding(): Int {
        return padding
    }


    /**
     * @param padding
     * *            The padding to set.
     * *
     * @return Returns the instance to call more set-methods.
     */
    fun setPadding(padding: Int): SerializeOptions {
        this.padding = padding
        return this
    }


    /**
     * @return Returns the encoding as Java encoding String.
     */
    val encoding: String
        get() {
            if (encodeUTF16BE) {
                return "UTF-16BE"
            } else if (encodeUTF16LE) {
                return "UTF-16LE"
            } else {
                return "UTF-8"
            }
        }


    /**

     * @return Returns clone of this SerializeOptions-object with the same options set.
     * *
     * @throws CloneNotSupportedException Cannot happen in this place.
     */
    @Throws(CloneNotSupportedException::class)
    fun clone(): Any? {
        val clone: SerializeOptions
        try {
            clone = SerializeOptions(options)
            clone.setBaseIndent(baseIndent)
            clone.setIndent(indent)
            clone.setNewline(newline)
            clone.setPadding(padding)
            return clone
        } catch (e: XMPException) {
            // This cannot happen, the options are already checked in "this" object.
            return null
        }

    }


    /**
     * @see Options.defineOptionName
     */
    override fun defineOptionName(option: Int): String? {
        when (option) {
            OMIT_PACKET_WRAPPER -> return "OMIT_PACKET_WRAPPER"
            READONLY_PACKET -> return "READONLY_PACKET"
            USE_COMPACT_FORMAT -> return "USE_COMPACT_FORMAT"
        //			case USE_CANONICAL_FORMAT :		return "USE_CANONICAL_FORMAT";
            INCLUDE_THUMBNAIL_PAD -> return "INCLUDE_THUMBNAIL_PAD"
            EXACT_PACKET_LENGTH -> return "EXACT_PACKET_LENGTH"
            OMIT_XMPMETA_ELEMENT -> return "OMIT_XMPMETA_ELEMENT"
            SORT -> return "NORMALIZED"
            else -> return null
        }
    }


    /**
     * @see Options.getValidOptions
     */
    protected override //		USE_CANONICAL_FORMAT |
    val validOptions: Int
        get() = OMIT_PACKET_WRAPPER or
                READONLY_PACKET or
                USE_COMPACT_FORMAT or
                INCLUDE_THUMBNAIL_PAD or
                OMIT_XMPMETA_ELEMENT or
                EXACT_PACKET_LENGTH or
                SORT

    companion object {
        /** Omit the XML packet wrapper.  */
        val OMIT_PACKET_WRAPPER = 0x0010
        /** Mark packet as read-only. Default is a writeable packet.  */
        val READONLY_PACKET = 0x0020
        /**
         * Use a compact form of RDF.
         * The compact form is the default serialization format (this flag is technically ignored).
         * To serialize to the canonical form, set the flag USE_CANONICAL_FORMAT.
         * If both flags &quot;compact&quot; and &quot;canonical&quot; are set, canonical is used.
         */
        val USE_COMPACT_FORMAT = 0x0040
        /** Use the canonical form of RDF if set. By default the compact form is used  */
        val USE_CANONICAL_FORMAT = 0x0080
        /**
         * Include a padding allowance for a thumbnail image. If no xmp:Thumbnails property
         * is present, the typical space for a JPEG thumbnail is used.
         */
        val INCLUDE_THUMBNAIL_PAD = 0x0100
        /**
         * The padding parameter provides the overall packet length. The actual amount of padding is
         * computed. An exception is thrown if the packet exceeds this length with no padding.
         */
        val EXACT_PACKET_LENGTH = 0x0200
        /** Omit the &lt;x:xmpmeta-tag  */
        val OMIT_XMPMETA_ELEMENT = 0x1000
        /** Sort the struct properties and qualifier before serializing  */
        val SORT = 0x2000

        // ---------------------------------------------------------------------------------------------
        // encoding bit constants

        /** Bit indicating little endian encoding, unset is big endian  */
        private val LITTLEENDIAN_BIT = 0x0001
        /** Bit indication UTF16 encoding.  */
        private val UTF16_BIT = 0x0002
        /** UTF8 encoding; this is the default  */
        val ENCODE_UTF8 = 0
        /** UTF16BE encoding  */
        val ENCODE_UTF16BE = UTF16_BIT
        /** UTF16LE encoding  */
        val ENCODE_UTF16LE = UTF16_BIT or LITTLEENDIAN_BIT
        /**  */
        private val ENCODING_MASK = UTF16_BIT or LITTLEENDIAN_BIT
    }
}