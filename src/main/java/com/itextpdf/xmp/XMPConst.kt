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


/**
 * Common constants for the XMP Toolkit.

 * @since 20.01.2006
 */
interface XMPConst {
    companion object {
        // ---------------------------------------------------------------------------------------------
        // Standard namespace URI constants


        // Standard namespaces

        /** The XML namespace for XML.  */
        val NS_XML = "http://www.w3.org/XML/1998/namespace"
        /** The XML namespace for RDF.  */
        val NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        /** The XML namespace for the Dublin Core schema.  */
        val NS_DC = "http://purl.org/dc/elements/1.1/"
        /** The XML namespace for the IPTC Core schema.  */
        val NS_IPTCCORE = "http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/"
        /** The XML namespace for the IPTC Extension schema.  */
        val NS_IPTCEXT = "http://iptc.org/std/Iptc4xmpExt/2008-02-29/"
        /** The XML namespace for the DICOM medical schema.  */
        val NS_DICOM = "http://ns.adobe.com/DICOM/"
        /** The XML namespace for the PLUS (Picture Licensing Universal System, http://www.useplus.org)  */
        val NS_PLUS = "http://ns.useplus.org/ldf/xmp/1.0/"

        // Adobe standard namespaces

        /** The XML namespace Adobe XMP Metadata.  */
        val NS_X = "adobe:ns:meta/"
        /**  */
        val NS_IX = "http://ns.adobe.com/iX/1.0/"
        /** The XML namespace for the XMP "basic" schema.  */
        val NS_XMP = "http://ns.adobe.com/xap/1.0/"
        /** The XML namespace for the XMP copyright schema.  */
        val NS_XMP_RIGHTS = "http://ns.adobe.com/xap/1.0/rights/"
        /** The XML namespace for the XMP digital asset management schema.  */
        val NS_XMP_MM = "http://ns.adobe.com/xap/1.0/mm/"
        /** The XML namespace for the job management schema.  */
        val NS_XMP_BJ = "http://ns.adobe.com/xap/1.0/bj/"
        /** The XML namespace for the job management schema.  */
        val NS_XMP_NOTE = "http://ns.adobe.com/xmp/note/"

        /** The XML namespace for the PDF schema.  */
        val NS_PDF = "http://ns.adobe.com/pdf/1.3/"
        /** The XML namespace for the PDF schema.  */
        val NS_PDFX = "http://ns.adobe.com/pdfx/1.3/"
        /**  */
        val NS_PDFX_ID = "http://www.npes.org/pdfx/ns/id/"
        /**  */
        val NS_PDFA_SCHEMA = "http://www.aiim.org/pdfa/ns/schema#"
        /**  */
        val NS_PDFA_PROPERTY = "http://www.aiim.org/pdfa/ns/property#"
        /**  */
        val NS_PDFA_TYPE = "http://www.aiim.org/pdfa/ns/type#"
        /**  */
        val NS_PDFA_FIELD = "http://www.aiim.org/pdfa/ns/field#"
        /**  */
        val NS_PDFA_ID = "http://www.aiim.org/pdfa/ns/id/"
        /**  */
        val NS_PDFUA_ID = "http://www.aiim.org/pdfua/ns/id/"
        /**  */
        val NS_PDFA_EXTENSION = "http://www.aiim.org/pdfa/ns/extension/"
        /** The XML namespace for the Photoshop custom schema.  */
        val NS_PHOTOSHOP = "http://ns.adobe.com/photoshop/1.0/"
        /** The XML namespace for the Photoshop Album schema.  */
        val NS_PSALBUM = "http://ns.adobe.com/album/1.0/"
        /** The XML namespace for Adobe's EXIF schema.  */
        val NS_EXIF = "http://ns.adobe.com/exif/1.0/"
        /** NS for the CIPA XMP for Exif document v1.1  */
        val NS_EXIFX = "http://cipa.jp/exif/1.0/"
        /**  */
        val NS_EXIF_AUX = "http://ns.adobe.com/exif/1.0/aux/"
        /** The XML namespace for Adobe's TIFF schema.  */
        val NS_TIFF = "http://ns.adobe.com/tiff/1.0/"
        /**  */
        val NS_PNG = "http://ns.adobe.com/png/1.0/"
        /**  */
        val NS_JPEG = "http://ns.adobe.com/jpeg/1.0/"
        /**  */
        val NS_JP2K = "http://ns.adobe.com/jp2k/1.0/"
        /**  */
        val NS_CAMERARAW = "http://ns.adobe.com/camera-raw-settings/1.0/"
        /**  */
        val NS_ADOBESTOCKPHOTO = "http://ns.adobe.com/StockPhoto/1.0/"
        /**  */
        val NS_CREATOR_ATOM = "http://ns.adobe.com/creatorAtom/1.0/"
        /**  */
        val NS_ASF = "http://ns.adobe.com/asf/1.0/"
        /**  */
        val NS_WAV = "http://ns.adobe.com/xmp/wav/1.0/"
        /** BExt Schema  */
        val NS_BWF = "http://ns.adobe.com/bwf/bext/1.0/"
        /** RIFF Info Schema  */
        val NS_RIFFINFO = "http://ns.adobe.com/riff/info/"
        /**  */
        val NS_SCRIPT = "http://ns.adobe.com/xmp/1.0/Script/"
        /** Transform XMP  */
        val NS_TXMP = "http://ns.adobe.com/TransformXMP/"
        /** Adobe Flash SWF  */
        val NS_SWF = "http://ns.adobe.com/swf/1.0/"


        // XMP namespaces that are Adobe private

        /**  */
        val NS_DM = "http://ns.adobe.com/xmp/1.0/DynamicMedia/"
        /**  */
        val NS_TRANSIENT = "http://ns.adobe.com/xmp/transient/1.0/"
        /** legacy Dublin Core NS, will be converted to NS_DC  */
        val NS_DC_DEPRECATED = "http://purl.org/dc/1.1/"


        // XML namespace constants for qualifiers and structured property fields.

        /** The XML namespace for qualifiers of the xmp:Identifier property.  */
        val TYPE_IDENTIFIERQUAL = "http://ns.adobe.com/xmp/Identifier/qual/1.0/"
        /** The XML namespace for fields of the Dimensions type.  */
        val TYPE_DIMENSIONS = "http://ns.adobe.com/xap/1.0/sType/Dimensions#"
        /**  */
        val TYPE_TEXT = "http://ns.adobe.com/xap/1.0/t/"
        /**  */
        val TYPE_PAGEDFILE = "http://ns.adobe.com/xap/1.0/t/pg/"
        /**  */
        val TYPE_GRAPHICS = "http://ns.adobe.com/xap/1.0/g/"
        /** The XML namespace for fields of a graphical image. Used for the Thumbnail type.  */
        val TYPE_IMAGE = "http://ns.adobe.com/xap/1.0/g/img/"
        /**  */
        val TYPE_FONT = "http://ns.adobe.com/xap/1.0/sType/Font#"
        /** The XML namespace for fields of the ResourceEvent type.  */
        val TYPE_RESOURCEEVENT = "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#"
        /** The XML namespace for fields of the ResourceRef type.  */
        val TYPE_RESOURCEREF = "http://ns.adobe.com/xap/1.0/sType/ResourceRef#"
        /** The XML namespace for fields of the Version type.  */
        val TYPE_ST_VERSION = "http://ns.adobe.com/xap/1.0/sType/Version#"
        /** The XML namespace for fields of the JobRef type.  */
        val TYPE_ST_JOB = "http://ns.adobe.com/xap/1.0/sType/Job#"
        /**  */
        val TYPE_MANIFESTITEM = "http://ns.adobe.com/xap/1.0/sType/ManifestItem#"



        // ---------------------------------------------------------------------------------------------
        // Basic types and constants

        /**
         * The canonical true string value for Booleans in serialized XMP. Code that converts from the
         * string to a bool should be case insensitive, and even allow "1".
         */
        val TRUESTR = "True"
        /**
         * The canonical false string value for Booleans in serialized XMP. Code that converts from the
         * string to a bool should be case insensitive, and even allow "0".
         */
        val FALSESTR = "False"
        /** Index that has the meaning to be always the last item in an array.  */
        val ARRAY_LAST_ITEM = -1
        /** Node name of an array item.  */
        val ARRAY_ITEM_NAME = "[]"
        /** The x-default string for localized properties  */
        val X_DEFAULT = "x-default"
        /** xml:lang qualfifier  */
        val XML_LANG = "xml:lang"
        /** rdf:type qualfifier  */
        val RDF_TYPE = "rdf:type"

        /** Processing Instruction (PI) for xmp packet  */
        val XMP_PI = "xpacket"
        /** XMP meta tag version new  */
        val TAG_XMPMETA = "xmpmeta"
        /** XMP meta tag version old  */
        val TAG_XAPMETA = "xapmeta"
    }
}
