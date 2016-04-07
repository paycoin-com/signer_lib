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

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.TreeMap
import java.util.regex.Pattern

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPSchemaRegistry
import com.itextpdf.xmp.options.AliasOptions
import com.itextpdf.xmp.properties.XMPAliasInfo


/**
 * The schema registry handles the namespaces, aliases and global options for the XMP Toolkit. There
 * is only one single instance used by the toolkit.

 * @since 27.01.2006
 */
class XMPSchemaRegistryImpl : XMPSchemaRegistry, XMPConst {
    /** a map from a namespace URI to its registered prefix  */
    private val namespaceToPrefixMap = HashMap()

    /** a map from a prefix to the associated namespace URI  */
    private val prefixToNamespaceMap = HashMap()

    /** a map of all registered aliases.
     * The map is a relationship from a qname to an `XMPAliasInfo`-object.  */
    private val aliasMap = HashMap()
    /** The pattern that must not be contained in simple properties  */
    private val p = Pattern.compile("[/*?\\[\\]]")


    init {
        try {
            registerStandardNamespaces()
            registerStandardAliases()
        } catch (e: XMPException) {
            throw RuntimeException("The XMPSchemaRegistry cannot be initialized!")
        }

    }


    // ---------------------------------------------------------------------------------------------
    // Namespace Functions


    /**
     * @see XMPSchemaRegistry.registerNamespace
     */
    @Synchronized @Throws(XMPException::class)
    override fun registerNamespace(namespaceURI: String, suggestedPrefix: String): String {
        var suggestedPrefix = suggestedPrefix
        ParameterAsserts.assertSchemaNS(namespaceURI)
        ParameterAsserts.assertPrefix(suggestedPrefix)

        if (suggestedPrefix[suggestedPrefix.length - 1] != ':') {
            suggestedPrefix += ':'
        }

        if (!Utils.isXMLNameNS(suggestedPrefix.substring(0,
                suggestedPrefix.length - 1))) {
            throw XMPException("The prefix is a bad XML name", XMPError.BADXML)
        }

        if (namespaceToPrefixMap.get(namespaceURI) != null) {
            // Return the actual prefix
            return namespaceToPrefixMap.get(namespaceURI)
        } else {
            if (prefixToNamespaceMap.get(suggestedPrefix) != null) {
                // the namespace is new, but the prefix is already engaged,
                // we generate a new prefix out of the suggested
                var generatedPrefix = suggestedPrefix
                var i = 1
                while (prefixToNamespaceMap.containsKey(generatedPrefix)) {
                    generatedPrefix = suggestedPrefix.substring(0, suggestedPrefix.length - 1)
                    +"_" + i + "_:"
                    i++
                }
                suggestedPrefix = generatedPrefix
            }
            prefixToNamespaceMap.put(suggestedPrefix, namespaceURI)
            namespaceToPrefixMap.put(namespaceURI, suggestedPrefix)

            // Return the suggested prefix
            return suggestedPrefix
        }
    }


    /**
     * @see XMPSchemaRegistry.deleteNamespace
     */
    @Synchronized override fun deleteNamespace(namespaceURI: String) {
        val prefixToDelete = getNamespacePrefix(namespaceURI)
        if (prefixToDelete != null) {
            namespaceToPrefixMap.remove(namespaceURI)
            prefixToNamespaceMap.remove(prefixToDelete)
        }
    }


    /**
     * @see XMPSchemaRegistry.getNamespacePrefix
     */
    @Synchronized override fun getNamespacePrefix(namespaceURI: String): String? {
        return namespaceToPrefixMap.get(namespaceURI)
    }


    /**
     * @see XMPSchemaRegistry.getNamespaceURI
     */
    @Synchronized override fun getNamespaceURI(namespacePrefix: String?): String {
        var namespacePrefix = namespacePrefix
        if (namespacePrefix != null && !namespacePrefix.endsWith(":")) {
            namespacePrefix += ":"
        }
        return prefixToNamespaceMap.get(namespacePrefix)
    }


    /**
     * @see XMPSchemaRegistry.getNamespaces
     */
    @Synchronized override fun getNamespaces(): Map<Any, Any> {
        return Collections.unmodifiableMap(TreeMap(namespaceToPrefixMap))
    }


    /**
     * @see XMPSchemaRegistry.getPrefixes
     */
    @Synchronized override fun getPrefixes(): Map<Any, Any> {
        return Collections.unmodifiableMap(TreeMap(prefixToNamespaceMap))
    }


    /**
     * Register the standard namespaces of schemas and types that are included in the XMP
     * Specification and some other Adobe private namespaces.
     * Note: This method is not lock because only called by the constructor.

     * @throws XMPException Forwards processing exceptions
     */
    @Throws(XMPException::class)
    private fun registerStandardNamespaces() {
        // register standard namespaces
        registerNamespace(XMPConst.NS_XML, "xml")
        registerNamespace(XMPConst.NS_RDF, "rdf")
        registerNamespace(XMPConst.NS_DC, "dc")
        registerNamespace(XMPConst.NS_IPTCCORE, "Iptc4xmpCore")
        registerNamespace(XMPConst.NS_IPTCEXT, "Iptc4xmpExt")
        registerNamespace(XMPConst.NS_DICOM, "DICOM")
        registerNamespace(XMPConst.NS_PLUS, "plus")

        // register Adobe standard namespaces
        registerNamespace(XMPConst.NS_X, "x")
        registerNamespace(XMPConst.NS_IX, "iX")

        registerNamespace(XMPConst.NS_XMP, "xmp")
        registerNamespace(XMPConst.NS_XMP_RIGHTS, "xmpRights")
        registerNamespace(XMPConst.NS_XMP_MM, "xmpMM")
        registerNamespace(XMPConst.NS_XMP_BJ, "xmpBJ")
        registerNamespace(XMPConst.NS_XMP_NOTE, "xmpNote")

        registerNamespace(XMPConst.NS_PDF, "pdf")
        registerNamespace(XMPConst.NS_PDFX, "pdfx")
        registerNamespace(XMPConst.NS_PDFX_ID, "pdfxid")
        registerNamespace(XMPConst.NS_PDFA_SCHEMA, "pdfaSchema")
        registerNamespace(XMPConst.NS_PDFA_PROPERTY, "pdfaProperty")
        registerNamespace(XMPConst.NS_PDFA_TYPE, "pdfaType")
        registerNamespace(XMPConst.NS_PDFA_FIELD, "pdfaField")
        registerNamespace(XMPConst.NS_PDFA_ID, "pdfaid")
        registerNamespace(XMPConst.NS_PDFUA_ID, "pdfuaid")
        registerNamespace(XMPConst.NS_PDFA_EXTENSION, "pdfaExtension")
        registerNamespace(XMPConst.NS_PHOTOSHOP, "photoshop")
        registerNamespace(XMPConst.NS_PSALBUM, "album")
        registerNamespace(XMPConst.NS_EXIF, "exif")
        registerNamespace(XMPConst.NS_EXIFX, "exifEX")
        registerNamespace(XMPConst.NS_EXIF_AUX, "aux")
        registerNamespace(XMPConst.NS_TIFF, "tiff")
        registerNamespace(XMPConst.NS_PNG, "png")
        registerNamespace(XMPConst.NS_JPEG, "jpeg")
        registerNamespace(XMPConst.NS_JP2K, "jp2k")
        registerNamespace(XMPConst.NS_CAMERARAW, "crs")
        registerNamespace(XMPConst.NS_ADOBESTOCKPHOTO, "bmsp")
        registerNamespace(XMPConst.NS_CREATOR_ATOM, "creatorAtom")
        registerNamespace(XMPConst.NS_ASF, "asf")
        registerNamespace(XMPConst.NS_WAV, "wav")
        registerNamespace(XMPConst.NS_BWF, "bext")
        registerNamespace(XMPConst.NS_RIFFINFO, "riffinfo")
        registerNamespace(XMPConst.NS_SCRIPT, "xmpScript")
        registerNamespace(XMPConst.NS_TXMP, "txmp")
        registerNamespace(XMPConst.NS_SWF, "swf")

        // register Adobe private namespaces
        registerNamespace(XMPConst.NS_DM, "xmpDM")
        registerNamespace(XMPConst.NS_TRANSIENT, "xmpx")

        // register Adobe standard type namespaces
        registerNamespace(XMPConst.TYPE_TEXT, "xmpT")
        registerNamespace(XMPConst.TYPE_PAGEDFILE, "xmpTPg")
        registerNamespace(XMPConst.TYPE_GRAPHICS, "xmpG")
        registerNamespace(XMPConst.TYPE_IMAGE, "xmpGImg")
        registerNamespace(XMPConst.TYPE_FONT, "stFnt")
        registerNamespace(XMPConst.TYPE_DIMENSIONS, "stDim")
        registerNamespace(XMPConst.TYPE_RESOURCEEVENT, "stEvt")
        registerNamespace(XMPConst.TYPE_RESOURCEREF, "stRef")
        registerNamespace(XMPConst.TYPE_ST_VERSION, "stVer")
        registerNamespace(XMPConst.TYPE_ST_JOB, "stJob")
        registerNamespace(XMPConst.TYPE_MANIFESTITEM, "stMfs")
        registerNamespace(XMPConst.TYPE_IDENTIFIERQUAL, "xmpidq")
    }



    // ---------------------------------------------------------------------------------------------
    // Alias Functions


    /**
     * @see XMPSchemaRegistry.resolveAlias
     */
    @Synchronized override fun resolveAlias(aliasNS: String, aliasProp: String): XMPAliasInfo? {
        val aliasPrefix = getNamespacePrefix(aliasNS) ?: return null

        return aliasMap.get(aliasPrefix + aliasProp)
    }


    /**
     * @see XMPSchemaRegistry.findAlias
     */
    @Synchronized override fun findAlias(qname: String): XMPAliasInfo {
        return aliasMap.get(qname)
    }


    /**
     * @see XMPSchemaRegistry.findAliases
     */
    @Synchronized override fun findAliases(aliasNS: String): Array<XMPAliasInfo> {
        val prefix = getNamespacePrefix(aliasNS)
        val result = ArrayList()
        if (prefix != null) {
            val it = aliasMap.keys.iterator()
            while (it.hasNext()) {
                if (it.next().startsWith(prefix)) {
                    result.add(findAlias(it.next()))
                }
            }

        }
        return result.toArray(arrayOfNulls<XMPAliasInfo>(result.size))
    }


    /**
     * Associates an alias name with an actual name.
     *
     *
     * Define a alias mapping from one namespace/property to another. Both
     * property names must be simple names. An alias can be a direct mapping,
     * where the alias and actual have the same data type. It is also possible
     * to map a simple alias to an item in an array. This can either be to the
     * first item in the array, or to the 'x-default' item in an alt-text array.
     * Multiple alias names may map to the same actual, as long as the forms
     * match. It is a no-op to reregister an alias in an identical fashion.
     * Note: This method is not locking because only called by registerStandardAliases
     * which is only called by the constructor.
     * Note2: The method is only package-private so that it can be tested with unittests

     * @param aliasNS
     * *            The namespace URI for the alias. Must not be null or the empty
     * *            string.
     * *
     * @param aliasProp
     * *            The name of the alias. Must be a simple name, not null or the
     * *            empty string and not a general path expression.
     * *
     * @param actualNS
     * *            The namespace URI for the actual. Must not be null or the
     * *            empty string.
     * *
     * @param actualProp
     * *            The name of the actual. Must be a simple name, not null or the
     * *            empty string and not a general path expression.
     * *
     * @param aliasForm
     * *            Provides options for aliases for simple aliases to array
     * *            items. This is needed to know what kind of array to create if
     * *            set for the first time via the simple alias. Pass
     * *            `XMP_NoOptions`, the default value, for all
     * *            direct aliases regardless of whether the actual data type is
     * *            an array or not (see [AliasOptions]).
     * *
     * @throws XMPException
     * *             for inconsistant aliases.
     */
    @Synchronized @Throws(XMPException::class)
    internal fun registerAlias(aliasNS: String, aliasProp: String, actualNS: String,
                               actualProp: String, aliasForm: AliasOptions?) {
        ParameterAsserts.assertSchemaNS(aliasNS)
        ParameterAsserts.assertPropName(aliasProp)
        ParameterAsserts.assertSchemaNS(actualNS)
        ParameterAsserts.assertPropName(actualProp)

        // Fix the alias options
        val aliasOpts = if (aliasForm != null)
            AliasOptions(XMPNodeUtils.verifySetOptions(
                    aliasForm.toPropertyOptions(), null).options)
        else
            AliasOptions()

        if (p.matcher(aliasProp).find() || p.matcher(actualProp).find()) {
            throw XMPException("Alias and actual property names must be simple",
                    XMPError.BADXPATH)
        }

        // check if both namespaces are registered
        val aliasPrefix = getNamespacePrefix(aliasNS)
        val actualPrefix = getNamespacePrefix(actualNS)
        if (aliasPrefix == null) {
            throw XMPException("Alias namespace is not registered", XMPError.BADSCHEMA)
        } else if (actualPrefix == null) {
            throw XMPException("Actual namespace is not registered",
                    XMPError.BADSCHEMA)
        }

        val key = aliasPrefix + aliasProp

        // check if alias is already existing
        if (aliasMap.containsKey(key)) {
            throw XMPException("Alias is already existing", XMPError.BADPARAM)
        } else if (aliasMap.containsKey(actualPrefix + actualProp)) {
            throw XMPException(
                    "Actual property is already an alias, use the base property",
                    XMPError.BADPARAM)
        }

        val aliasInfo = object : XMPAliasInfo {
            /**
             * @see XMPAliasInfo.getNamespace
             */
            override fun getNamespace(): String {
                return actualNS
            }

            /**
             * @see XMPAliasInfo.getPrefix
             */
            override fun getPrefix(): String {
                return actualPrefix
            }

            /**
             * @see XMPAliasInfo.getPropName
             */
            override fun getPropName(): String {
                return actualProp
            }

            /**
             * @see XMPAliasInfo.getAliasForm
             */
            override fun getAliasForm(): AliasOptions {
                return aliasOpts
            }

            override fun toString(): String {
                return "$actualPrefix$actualProp NS($actualNS), FORM ("
                +getAliasForm() + ")"
            }
        }

        aliasMap.put(key, aliasInfo)
    }


    /**
     * @see XMPSchemaRegistry.getAliases
     */
    @Synchronized override fun getAliases(): Map<Any, Any> {
        return Collections.unmodifiableMap(TreeMap(aliasMap))
    }


    /**
     * Register the standard aliases.
     * Note: This method is not lock because only called by the constructor.

     * @throws XMPException If the registrations of at least one alias fails.
     */
    @Throws(XMPException::class)
    private fun registerStandardAliases() {
        val aliasToArrayOrdered = AliasOptions().setArrayOrdered(true)
        val aliasToArrayAltText = AliasOptions().setArrayAltText(true)


        // Aliases from XMP to DC.
        registerAlias(XMPConst.NS_XMP, "Author", XMPConst.NS_DC, "creator", aliasToArrayOrdered)
        registerAlias(XMPConst.NS_XMP, "Authors", XMPConst.NS_DC, "creator", null)
        registerAlias(XMPConst.NS_XMP, "Description", XMPConst.NS_DC, "description", null)
        registerAlias(XMPConst.NS_XMP, "Format", XMPConst.NS_DC, "format", null)
        registerAlias(XMPConst.NS_XMP, "Keywords", XMPConst.NS_DC, "subject", null)
        registerAlias(XMPConst.NS_XMP, "Locale", XMPConst.NS_DC, "language", null)
        registerAlias(XMPConst.NS_XMP, "Title", XMPConst.NS_DC, "title", null)
        registerAlias(XMPConst.NS_XMP_RIGHTS, "Copyright", XMPConst.NS_DC, "rights", null)

        // Aliases from PDF to DC and XMP.
        registerAlias(XMPConst.NS_PDF, "Author", XMPConst.NS_DC, "creator", aliasToArrayOrdered)
        registerAlias(XMPConst.NS_PDF, "BaseURL", XMPConst.NS_XMP, "BaseURL", null)
        registerAlias(XMPConst.NS_PDF, "CreationDate", XMPConst.NS_XMP, "CreateDate", null)
        registerAlias(XMPConst.NS_PDF, "Creator", XMPConst.NS_XMP, "CreatorTool", null)
        registerAlias(XMPConst.NS_PDF, "ModDate", XMPConst.NS_XMP, "ModifyDate", null)
        registerAlias(XMPConst.NS_PDF, "Subject", XMPConst.NS_DC, "description", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PDF, "Title", XMPConst.NS_DC, "title", aliasToArrayAltText)

        // Aliases from PHOTOSHOP to DC and XMP.
        registerAlias(XMPConst.NS_PHOTOSHOP, "Author", XMPConst.NS_DC, "creator", aliasToArrayOrdered)
        registerAlias(XMPConst.NS_PHOTOSHOP, "Caption", XMPConst.NS_DC, "description", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PHOTOSHOP, "Copyright", XMPConst.NS_DC, "rights", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PHOTOSHOP, "Keywords", XMPConst.NS_DC, "subject", null)
        registerAlias(XMPConst.NS_PHOTOSHOP, "Marked", XMPConst.NS_XMP_RIGHTS, "Marked", null)
        registerAlias(XMPConst.NS_PHOTOSHOP, "Title", XMPConst.NS_DC, "title", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PHOTOSHOP, "WebStatement", XMPConst.NS_XMP_RIGHTS, "WebStatement", null)

        // Aliases from TIFF and EXIF to DC and XMP.
        registerAlias(XMPConst.NS_TIFF, "Artist", XMPConst.NS_DC, "creator", aliasToArrayOrdered)
        registerAlias(XMPConst.NS_TIFF, "Copyright", XMPConst.NS_DC, "rights", null)
        registerAlias(XMPConst.NS_TIFF, "DateTime", XMPConst.NS_XMP, "ModifyDate", null)
        registerAlias(XMPConst.NS_TIFF, "ImageDescription", XMPConst.NS_DC, "description", null)
        registerAlias(XMPConst.NS_TIFF, "Software", XMPConst.NS_XMP, "CreatorTool", null)

        // Aliases from PNG (Acrobat ImageCapture) to DC and XMP.
        registerAlias(XMPConst.NS_PNG, "Author", XMPConst.NS_DC, "creator", aliasToArrayOrdered)
        registerAlias(XMPConst.NS_PNG, "Copyright", XMPConst.NS_DC, "rights", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PNG, "CreationTime", XMPConst.NS_XMP, "CreateDate", null)
        registerAlias(XMPConst.NS_PNG, "Description", XMPConst.NS_DC, "description", aliasToArrayAltText)
        registerAlias(XMPConst.NS_PNG, "ModificationTime", XMPConst.NS_XMP, "ModifyDate", null)
        registerAlias(XMPConst.NS_PNG, "Software", XMPConst.NS_XMP, "CreatorTool", null)
        registerAlias(XMPConst.NS_PNG, "Title", XMPConst.NS_DC, "title", aliasToArrayAltText)
    }
}
/**
 * Performs the initialisation of the registry with the default namespaces, aliases and global
 * options.
 */
