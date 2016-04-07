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

import com.itextpdf.xmp.impl.XMPMetaImpl
import com.itextpdf.xmp.impl.XMPMetaParser
import com.itextpdf.xmp.impl.XMPSchemaRegistryImpl
import com.itextpdf.xmp.impl.XMPSerializerHelper
import com.itextpdf.xmp.options.ParseOptions
import com.itextpdf.xmp.options.SerializeOptions

import java.io.InputStream
import java.io.OutputStream


/**
 * Creates `XMPMeta`-instances from an `InputStream`

 * @since 30.01.2006
 */
object XMPMetaFactory {
    /**
     * The singleton instance of the `XMPSchemaRegistry`.
     */
    /**
     * @return Returns the singleton instance of the `XMPSchemaRegistry`.
     */
    var schemaRegistry: XMPSchemaRegistry = XMPSchemaRegistryImpl()
        private set
    /**
     * cache for version info
     */
    private var versionInfo: XMPVersionInfo? = null

    /**
     * @return Returns an empty `XMPMeta`-object.
     */
    fun create(): XMPMeta {
        return XMPMetaImpl()
    }

    /**
     * These functions support parsing serialized RDF into an XMP object, and serailizing an XMP
     * object into RDF. The input for parsing may be any valid Unicode
     * encoding. ISO Latin-1 is also recognized, but its use is strongly discouraged. Serialization
     * is always as UTF-8.
     *
     *
     * `parseFromBuffer()` parses RDF from an `InputStream`. The encoding
     * is recognized automatically.

     * @param in      an `InputStream`
     * *
     * @param options Options controlling the parsing.
     * *                The available options are:
     * *
     * *                 *  XMP_REQUIRE_XMPMETA - The &lt;x:xmpmeta&gt; XML element is required around
     * *                &lt;rdf:RDF&gt;.
     * *                 *  XMP_STRICT_ALIASING - Do not reconcile alias differences, throw an exception.
     * *
     * *                *Note:*The XMP_STRICT_ALIASING option is not yet implemented.
     * *
     * @return Returns the `XMPMeta`-object created from the input.
     * *
     * @throws XMPException If the file is not well-formed XML or if the parsing fails.
     */
    @Throws(XMPException::class)
    @JvmOverloads fun parse(`in`: InputStream, options: ParseOptions? = null): XMPMeta {
        return XMPMetaParser.parse(`in`, options)
    }

    /**
     * Creates an `XMPMeta`-object from a string.

     * @param packet  a String contain an XMP-file.
     * *
     * @param options Options controlling the parsing.
     * *
     * @return Returns the `XMPMeta`-object created from the input.
     * *
     * @throws XMPException If the file is not well-formed XML or if the parsing fails.
     * *
     * @see XMPMetaFactory.parseFromString
     */
    @Throws(XMPException::class)
    @JvmOverloads fun parseFromString(packet: String, options: ParseOptions? = null): XMPMeta {
        return XMPMetaParser.parse(packet, options)
    }

    /**
     * Creates an `XMPMeta`-object from a byte-buffer.

     * @param buffer  a String contain an XMP-file.
     * *
     * @param options Options controlling the parsing.
     * *
     * @return Returns the `XMPMeta`-object created from the input.
     * *
     * @throws XMPException If the file is not well-formed XML or if the parsing fails.
     * *
     * @see XMPMetaFactory.parse
     */
    @Throws(XMPException::class)
    @JvmOverloads fun parseFromBuffer(buffer: ByteArray,
                                      options: ParseOptions? = null): XMPMeta {
        return XMPMetaParser.parse(buffer, options)
    }

    /**
     * Serializes an `XMPMeta`-object as RDF into an `OutputStream`.

     * @param xmp     a metadata object
     * *
     * @param options Options to control the serialization (see [SerializeOptions]).
     * *
     * @param out     an `OutputStream` to write the serialized RDF to.
     * *
     * @throws XMPException on serializsation errors.
     */
    @Throws(XMPException::class)
    @JvmOverloads fun serialize(xmp: XMPMeta, out: OutputStream, options: SerializeOptions? = null) {
        assertImplementation(xmp)
        XMPSerializerHelper.serialize(xmp as XMPMetaImpl, out, options)
    }

    /**
     * Serializes an `XMPMeta`-object as RDF into a byte buffer.

     * @param xmp     a metadata object
     * *
     * @param options Options to control the serialization (see [SerializeOptions]).
     * *
     * @return Returns a byte buffer containing the serialized RDF.
     * *
     * @throws XMPException on serializsation errors.
     */
    @Throws(XMPException::class)
    fun serializeToBuffer(xmp: XMPMeta, options: SerializeOptions): ByteArray {
        assertImplementation(xmp)
        return XMPSerializerHelper.serializeToBuffer(xmp as XMPMetaImpl, options)
    }

    /**
     * Serializes an `XMPMeta`-object as RDF into a string. *Note:* Encoding
     * is ignored when serializing to a string.

     * @param xmp     a metadata object
     * *
     * @param options Options to control the serialization (see [SerializeOptions]).
     * *
     * @return Returns a string containing the serialized RDF.
     * *
     * @throws XMPException on serializsation errors.
     */
    @Throws(XMPException::class)
    fun serializeToString(xmp: XMPMeta, options: SerializeOptions): String {
        assertImplementation(xmp)
        return XMPSerializerHelper.serializeToString(xmp as XMPMetaImpl, options)
    }

    /**
     * @param xmp Asserts that xmp is compatible to `XMPMetaImpl`.s
     */
    private fun assertImplementation(xmp: XMPMeta) {
        if (xmp !is XMPMetaImpl) {
            throw UnsupportedOperationException("The serializing service works only" + "with the XMPMeta implementation of this library")
        }
    }

    /**
     * Resets the schema registry to its original state (creates a new one).
     * Be careful this might break all existing XMPMeta-objects and should be used
     * only for testing purpurses.
     */
    fun reset() {
        schemaRegistry = XMPSchemaRegistryImpl()
    }

    /**
     * Obtain version information. The XMPVersionInfo singleton is created the first time
     * its requested.

     * @return Returns the version information.
     */
    @Synchronized fun getVersionInfo(): XMPVersionInfo {
        if (versionInfo == null) {
            try {
                val major = 5
                val minor = 1
                val micro = 0
                val engBuild = 3
                val debug = false

                // Adobe XMP Core 5.0-jc001 DEBUG-<branch>.<changelist>, 2009 Jan 28 15:22:38-CET
                val message = "Adobe XMP Core 5.1.0-jc003"


                versionInfo = object : XMPVersionInfo {
                    override val major: Int
                        get() = major

                    override val minor: Int
                        get() = minor

                    override val micro: Int
                        get() = micro

                    override val isDebug: Boolean
                        get() = debug

                    override val build: Int
                        get() = engBuild

                    override val message: String
                        get() = message

                    override fun toString(): String {
                        return message
                    }
                }

            } catch (e: Throwable) {
                // EMTPY, severe error would be detected during the tests
                println(e)
            }

        }
        return versionInfo
    }
}
/**
 * Hides public constructor
 */
// EMPTY
/**
 * Parsing with default options.

 * @param in an `InputStream`
 * *
 * @return Returns the `XMPMeta`-object created from the input.
 * *
 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
 * *
 * @see XMPMetaFactory.parse
 */
/**
 * Parsing with default options.

 * @param packet a String contain an XMP-file.
 * *
 * @return Returns the `XMPMeta`-object created from the input.
 * *
 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
 * *
 * @see XMPMetaFactory.parse
 */
/**
 * Parsing with default options.

 * @param buffer a String contain an XMP-file.
 * *
 * @return Returns the `XMPMeta`-object created from the input.
 * *
 * @throws XMPException If the file is not well-formed XML or if the parsing fails.
 * *
 * @see XMPMetaFactory.parseFromBuffer
 */
/**
 * Serializes an `XMPMeta`-object as RDF into an `OutputStream`
 * with default options.

 * @param xmp a metadata object
 * *
 * @param out an `OutputStream` to write the serialized RDF to.
 * *
 * @throws XMPException on serializsation errors.
 */
