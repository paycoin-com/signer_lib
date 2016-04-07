/*
 * $Id: aff26a945e7c66ec28a255b849ff20ae3f154d07 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import com.itextpdf.text.error_messages.MessageLocalization

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.HashMap

/**
 * PdfName is an object that can be used as a name in a PDF-file.
 *
 * A name, like a string, is a sequence of characters.
 * It must begin with a slash followed by a sequence of ASCII characters in
 * the range 32 through 136 except %, (, ), [, ], &lt;, &gt;, {, }, / and #.
 * Any character except 0x00 may be included in a name by writing its
 * two character hex code, preceded by #. The maximum number of characters
 * in a name is 127.
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.4 (page 56-58).
 *

 * @see PdfObject

 * @see PdfDictionary

 * @see BadPdfFormatException
 */

class PdfName : PdfObject, Comparable<PdfName> {
    // CLASS VARIABLES

    private var hash = 0

    /**
     * Constructs a new PdfName.
     * @param name the new name
     * *
     * @param lengthCheck if true check the length validity,
     * * if false the name can have any length
     */
    @JvmOverloads constructor(name: String, lengthCheck: Boolean = true) : super(PdfObject.NAME) {
        // The minimum number of characters in a name is 0, the maximum is 127 (the '/' not included)
        val length = name.length
        if (lengthCheck && length > 127)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.name.1.is.too.long.2.characters", name, length.toString()))
        bytes = encodeName(name)
    }

    /**
     * Constructs a PdfName.

     * @param bytes the byte representation of the name
     */
    constructor(bytes: ByteArray) : super(PdfObject.NAME, bytes) {
    }

    // CLASS METHODS

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *
     *

     * @param name the PdfName to be compared.
     * *
     * @return a negative integer, zero, or a positive integer as this object
     * * is less than, equal to, or greater than the specified object.
     * *
     * @throws ClassCastException if the specified object's type prevents it
     * * from being compared to this Object.
     */
    override fun compareTo(name: PdfName): Int {
        val myBytes = bytes
        val objBytes = name.bytes
        val len = Math.min(myBytes.size, objBytes.size)
        for (i in 0..len - 1) {
            if (myBytes[i] > objBytes[i])
                return 1
            if (myBytes[i] < objBytes[i])
                return -1
        }
        if (myBytes.size < objBytes.size)
            return -1
        if (myBytes.size > objBytes.size)
            return 1
        return 0
    }

    /**
     * Indicates whether some other object is "equal to" this one.

     * @param   obj   the reference object with which to compare.
     * *
     * @return  `true` if this object is the same as the obj
     * * argument; `false` otherwise.
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj is PdfName)
            return compareTo(obj as PdfName?) == 0
        return false
    }

    /**
     * Returns a hash code value for the object.
     * This method is supported for the benefit of hashtables such as those provided by
     * `java.util.Hashtable`.

     * @return  a hash code value for this object.
     */
    override fun hashCode(): Int {
        var h = hash
        if (h == 0) {
            var ptr = 0
            val len = bytes!!.size
            for (i in 0..len - 1)
                h = 31 * h + (bytes!![ptr++] and 0xff)
            hash = h
        }
        return h
    }

    companion object {

        // CLASS CONSTANTS (a variety of standard names used in PDF))
        /**
         * A name.
         * @since 2.1.6
         */
        val _3D = PdfName("3D")
        /** A name  */
        val A = PdfName("A")
        /**
         * A name
         * @since 5.0.3
         */
        val A85 = PdfName("A85")
        /** A name  */
        val AA = PdfName("AA")
        /**
         * A name
         * @since 2.1.5 renamed from ABSOLUTECALORIMETRIC
         */
        val ABSOLUTECOLORIMETRIC = PdfName("AbsoluteColorimetric")
        /** A name  */
        val AC = PdfName("AC")
        /** A name  */
        val ACROFORM = PdfName("AcroForm")
        /** A name  */
        val ACTION = PdfName("Action")
        /**
         * A name.
         * @since 2.1.6
         */
        val ACTIVATION = PdfName("Activation")
        /**
         * A name.
         * @since 2.1.6
         */
        val ADBE = PdfName("ADBE")
        /**
         * a name used in PDF structure
         * @since 2.1.6
         */
        val ACTUALTEXT = PdfName("ActualText")
        /** A name  */
        val ADBE_PKCS7_DETACHED = PdfName("adbe.pkcs7.detached")
        /** A name  */
        val ADBE_PKCS7_S4 = PdfName("adbe.pkcs7.s4")
        /** A name  */
        val ADBE_PKCS7_S5 = PdfName("adbe.pkcs7.s5")
        /** A name  */
        val ADBE_PKCS7_SHA1 = PdfName("adbe.pkcs7.sha1")
        /** A name  */
        val ADBE_X509_RSA_SHA1 = PdfName("adbe.x509.rsa_sha1")
        /** A name  */
        val ADOBE_PPKLITE = PdfName("Adobe.PPKLite")
        /** A name  */
        val ADOBE_PPKMS = PdfName("Adobe.PPKMS")
        /** A name  */
        val AESV2 = PdfName("AESV2")
        /** A name  */
        val AESV3 = PdfName("AESV3")
        /**
         * A name
         * @since 5.5.2
         */
        val AF = PdfName("AF")
        /**
         * A name
         * @since 5.4.5
         */
        val AFRELATIONSHIP = PdfName("AFRelationship")
        /**
         * A name
         * @since 5.0.3
         */
        val AHX = PdfName("AHx")
        /** A name  */
        val AIS = PdfName("AIS")
        /** A name  */
        val ALL = PdfName("All")
        /** A name  */
        val ALLPAGES = PdfName("AllPages")
        /**
         * Use ALT to specify alternate texts in Tagged PDF.
         * For alternate ICC profiles, use [.ALTERNATE]
         */
        val ALT = PdfName("Alt")
        /**
         * Use ALTERNATE only in ICC profiles. It specifies an alternative color
         * space, in case the primary one is not supported, for legacy purposes.
         * For various types of alternate texts in Tagged PDF, use [.ALT]
         */
        val ALTERNATE = PdfName("Alternate")
        /**
         * A name
         * @since 5.4.5
         */
        val ALTERNATEPRESENTATION = PdfName("AlternatePresentations")
        /**
         * A name.
         * @since 5.4.3
         */
        val ALTERNATES = PdfName("Alternates")
        /**
         * A name.
         * @since 5.0.2
         */
        val AND = PdfName("And")
        /**
         * A name.
         * @since 2.1.6
         */
        val ANIMATION = PdfName("Animation")
        /** A name  */
        val ANNOT = PdfName("Annot")
        /** A name  */
        val ANNOTS = PdfName("Annots")
        /** A name  */
        val ANTIALIAS = PdfName("AntiAlias")
        /** A name  */
        val AP = PdfName("AP")
        /** A name  */
        val APP = PdfName("App")
        /** A name  */
        val APPDEFAULT = PdfName("AppDefault")
        /**
         * A name
         * @since 2.1.6
         */
        val ART = PdfName("Art")
        /** A name  */
        val ARTBOX = PdfName("ArtBox")
        /**
         * A name
         * @since 5.4.2
         */
        val ARTIFACT = PdfName("Artifact")
        /** A name  */
        val ASCENT = PdfName("Ascent")
        /** A name  */
        val AS = PdfName("AS")
        /** A name  */
        val ASCII85DECODE = PdfName("ASCII85Decode")
        /** A name  */
        val ASCIIHEXDECODE = PdfName("ASCIIHexDecode")
        /**
         * A name.
         * @since 2.1.6
         */
        val ASSET = PdfName("Asset")
        /**
         * A name.
         * @since 2.1.6
         */
        val ASSETS = PdfName("Assets")
        /**
         * A name
         * @since 5.4.2
         */
        val ATTACHED = PdfName("Attached")
        /** A name  */
        val AUTHEVENT = PdfName("AuthEvent")
        /** A name  */
        val AUTHOR = PdfName("Author")
        /** A name  */
        val B = PdfName("B")
        /**
         * A name
         * @since    2.1.6
         */
        val BACKGROUND = PdfName("Background")
        /**
         * A name
         * @since    5.3.5
         */
        val BACKGROUNDCOLOR = PdfName("BackgroundColor")
        /** A name  */
        val BASEENCODING = PdfName("BaseEncoding")
        /** A name  */
        val BASEFONT = PdfName("BaseFont")
        /**
         * A name
         * @since    2.1.6
         */
        val BASEVERSION = PdfName("BaseVersion")
        /** A name  */
        val BBOX = PdfName("BBox")
        /** A name  */
        val BC = PdfName("BC")
        /** A name  */
        val BG = PdfName("BG")
        /**
         * A name
         * @since 2.1.6
         */
        val BIBENTRY = PdfName("BibEntry")
        /** A name  */
        val BIGFIVE = PdfName("BigFive")
        /**
         * A name.
         * @since 2.1.6
         */
        val BINDING = PdfName("Binding")
        /**
         * A name.
         * @since 2.1.6
         */
        val BINDINGMATERIALNAME = PdfName("BindingMaterialName")
        /** A name  */
        val BITSPERCOMPONENT = PdfName("BitsPerComponent")
        /** A name  */
        val BITSPERSAMPLE = PdfName("BitsPerSample")
        /** A name  */
        val BL = PdfName("Bl")
        /** A name  */
        val BLACKIS1 = PdfName("BlackIs1")
        /** A name  */
        val BLACKPOINT = PdfName("BlackPoint")
        /**
         * A name
         * @since 2.1.6
         */
        val BLOCKQUOTE = PdfName("BlockQuote")
        /** A name  */
        val BLEEDBOX = PdfName("BleedBox")
        /** A name  */
        val BLINDS = PdfName("Blinds")
        /** A name  */
        val BM = PdfName("BM")
        /** A name  */
        val BORDER = PdfName("Border")
        /**
         * A name
         * @since 5.4.0
         */
        val BOTH = PdfName("Both")
        /** A name  */
        val BOUNDS = PdfName("Bounds")
        /** A name  */
        val BOX = PdfName("Box")
        /** A name  */
        val BS = PdfName("BS")
        /** A name  */
        val BTN = PdfName("Btn")
        /** A name  */
        val BYTERANGE = PdfName("ByteRange")
        /** A name  */
        val C = PdfName("C")
        /** A name  */
        val C0 = PdfName("C0")
        /** A name  */
        val C1 = PdfName("C1")
        /** A name  */
        val CA = PdfName("CA")
        /** A name  */
        val ca = PdfName("ca")
        /** A name  */
        val CALGRAY = PdfName("CalGray")
        /** A name  */
        val CALRGB = PdfName("CalRGB")
        /** A name  */
        val CAPHEIGHT = PdfName("CapHeight")
        /**
         * A name
         * @since 5.4.5
         */
        val CARET = PdfName("Caret")
        /**
         * A name
         * @since 2.1.6
         */
        val CAPTION = PdfName("Caption")
        /** A name  */
        val CATALOG = PdfName("Catalog")
        /** A name  */
        val CATEGORY = PdfName("Category")
        /**
         * A name
         * @since 5.4.4
         */
        val CB = PdfName("cb")
        /** A name  */
        val CCITTFAXDECODE = PdfName("CCITTFaxDecode")
        /**
         * A name.
         * @since 2.1.6
         */
        val CENTER = PdfName("Center")
        /** A name  */
        val CENTERWINDOW = PdfName("CenterWindow")
        /** A name  */
        val CERT = PdfName("Cert")
        /** A name
         * @since 5.1.3
         */
        val CERTS = PdfName("Certs")
        /** A name  */
        val CF = PdfName("CF")
        /** A name  */
        val CFM = PdfName("CFM")
        /** A name  */
        val CH = PdfName("Ch")
        /** A name  */
        val CHARPROCS = PdfName("CharProcs")
        /** A name  */
        val CHECKSUM = PdfName("CheckSum")
        /** A name  */
        val CI = PdfName("CI")
        /** A name  */
        val CIDFONTTYPE0 = PdfName("CIDFontType0")
        /** A name  */
        val CIDFONTTYPE2 = PdfName("CIDFontType2")
        /**
         * A name
         * @since 2.0.7
         */
        val CIDSET = PdfName("CIDSet")
        /** A name  */
        val CIDSYSTEMINFO = PdfName("CIDSystemInfo")
        /** A name  */
        val CIDTOGIDMAP = PdfName("CIDToGIDMap")
        /** A name  */
        val CIRCLE = PdfName("Circle")
        /**
         * A name.
         * @since 5.3.4
         */
        val CLASSMAP = PdfName("ClassMap")
        /**
         * A name.
         * @since 5.1.0
         */
        val CLOUD = PdfName("Cloud")
        /**
         * A name.
         * @since 2.1.6
         */
        val CMD = PdfName("CMD")
        /** A name  */
        val CO = PdfName("CO")
        /**
         * A name
         * @since 2.1.6
         */
        val CODE = PdfName("Code")
        /**
         * A name
         * @since 5.3.4
         */
        val COLOR = PdfName("Color")
        val COLORANTS = PdfName("Colorants")
        /** A name  */
        val COLORS = PdfName("Colors")
        /** A name  */
        val COLORSPACE = PdfName("ColorSpace")
        /**
         * A name
         * @since 5.4.4
         */
        val COLORTRANSFORM = PdfName("ColorTransform")
        /** A name  */
        val COLLECTION = PdfName("Collection")
        /** A name  */
        val COLLECTIONFIELD = PdfName("CollectionField")
        /** A name  */
        val COLLECTIONITEM = PdfName("CollectionItem")
        /** A name  */
        val COLLECTIONSCHEMA = PdfName("CollectionSchema")
        /** A name  */
        val COLLECTIONSORT = PdfName("CollectionSort")
        /** A name  */
        val COLLECTIONSUBITEM = PdfName("CollectionSubitem")
        /**
         * A name.
         * @since 5.4.0
         */
        val COLSPAN = PdfName("ColSpan")
        /**
         * A name.
         * @since 5.4.0
         */
        val COLUMN = PdfName("Column")
        /** A name  */
        val COLUMNS = PdfName("Columns")
        /**
         * A name.
         * @since 2.1.6
         */
        val CONDITION = PdfName("Condition")
        /**
         * A name.
         * @since 5.4.2
         */
        val CONFIGS = PdfName("Configs")
        /**
         * A name.
         * @since 2.1.6
         */
        val CONFIGURATION = PdfName("Configuration")
        /**
         * A name.
         * @since 2.1.6
         */
        val CONFIGURATIONS = PdfName("Configurations")
        /** A name  */
        val CONTACTINFO = PdfName("ContactInfo")
        /** A name  */
        val CONTENT = PdfName("Content")
        /** A name  */
        val CONTENTS = PdfName("Contents")
        /** A name  */
        val COORDS = PdfName("Coords")
        /** A name  */
        val COUNT = PdfName("Count")
        /** A name of a base 14 type 1 font  */
        val COURIER = PdfName("Courier")
        /** A name of a base 14 type 1 font  */
        val COURIER_BOLD = PdfName("Courier-Bold")
        /** A name of a base 14 type 1 font  */
        val COURIER_OBLIQUE = PdfName("Courier-Oblique")
        /** A name of a base 14 type 1 font  */
        val COURIER_BOLDOBLIQUE = PdfName("Courier-BoldOblique")
        /** A name  */
        val CREATIONDATE = PdfName("CreationDate")
        /** A name  */
        val CREATOR = PdfName("Creator")
        /** A name  */
        val CREATORINFO = PdfName("CreatorInfo")
        /** A name
         * @since 5.1.3
         */
        val CRL = PdfName("CRL")
        /** A name
         * @since 5.1.3
         */
        val CRLS = PdfName("CRLs")
        /** A name  */
        val CROPBOX = PdfName("CropBox")
        /** A name  */
        val CRYPT = PdfName("Crypt")
        /** A name  */
        val CS = PdfName("CS")
        /**
         * A name.
         * @since 2.1.6
         */
        val CUEPOINT = PdfName("CuePoint")
        /**
         * A name.
         * @since 2.1.6
         */
        val CUEPOINTS = PdfName("CuePoints")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val CYX = PdfName("CYX")
        /** A name  */
        val D = PdfName("D")
        /** A name  */
        val DA = PdfName("DA")
        /** A name  */
        val DATA = PdfName("Data")
        /** A name  */
        val DC = PdfName("DC")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val DCS = PdfName("DCS")
        /** A name  */
        val DCTDECODE = PdfName("DCTDecode")
        /**
         * A name.
         * @since 5.4.0
         */
        val DECIMAL = PdfName("Decimal")
        /**
         * A name.
         * @since 2.1.6
         */
        val DEACTIVATION = PdfName("Deactivation")
        /** A name  */
        val DECODE = PdfName("Decode")
        /** A name  */
        val DECODEPARMS = PdfName("DecodeParms")
        /**
         * A name
         * @since    2.1.6
         */
        val DEFAULT = PdfName("Default")
        /**
         * A name
         * @since    2.1.5 renamed from DEFAULTCRYPTFILER
         */
        val DEFAULTCRYPTFILTER = PdfName("DefaultCryptFilter")
        /** A name  */
        val DEFAULTCMYK = PdfName("DefaultCMYK")
        /** A name  */
        val DEFAULTGRAY = PdfName("DefaultGray")
        /** A name  */
        val DEFAULTRGB = PdfName("DefaultRGB")
        /** A name  */
        val DESC = PdfName("Desc")
        /** A name  */
        val DESCENDANTFONTS = PdfName("DescendantFonts")
        /** A name  */
        val DESCENT = PdfName("Descent")
        /** A name  */
        val DEST = PdfName("Dest")
        /** A name  */
        val DESTOUTPUTPROFILE = PdfName("DestOutputProfile")
        /** A name  */
        val DESTS = PdfName("Dests")
        /** A name  */
        val DEVICEGRAY = PdfName("DeviceGray")
        /** A name  */
        val DEVICERGB = PdfName("DeviceRGB")
        /** A name  */
        val DEVICECMYK = PdfName("DeviceCMYK")
        /**
         * A name
         * @since 5.2.1
         */
        val DEVICEN = PdfName("DeviceN")
        /** A name  */
        val DI = PdfName("Di")
        /** A name  */
        val DIFFERENCES = PdfName("Differences")
        /** A name  */
        val DISSOLVE = PdfName("Dissolve")
        /** A name  */
        val DIRECTION = PdfName("Direction")
        /** A name  */
        val DISPLAYDOCTITLE = PdfName("DisplayDocTitle")
        /** A name  */
        val DIV = PdfName("Div")
        /** A name  */
        val DL = PdfName("DL")
        /** A name  */
        val DM = PdfName("Dm")
        /** A name  */
        val DOCMDP = PdfName("DocMDP")
        /** A name  */
        val DOCOPEN = PdfName("DocOpen")
        /**
         * A name.
         * @since 5.1.3
         */
        val DOCTIMESTAMP = PdfName("DocTimeStamp")
        /**
         * A name.
         * @since 2.1.6
         */
        val DOCUMENT = PdfName("Document")
        /** A name  */
        val DOMAIN = PdfName("Domain")
        /**
         * A name.
         * @since 5.2.1
         */
        val DOS = PdfName("DOS")
        /** A name  */
        val DP = PdfName("DP")
        /** A name  */
        val DR = PdfName("DR")
        /** A name  */
        val DS = PdfName("DS")
        /** A name
         * @since 5.1.3
         */
        val DSS = PdfName("DSS")
        /** A name  */
        val DUR = PdfName("Dur")
        /** A name  */
        val DUPLEX = PdfName("Duplex")
        /** A name  */
        val DUPLEXFLIPSHORTEDGE = PdfName("DuplexFlipShortEdge")
        /** A name  */
        val DUPLEXFLIPLONGEDGE = PdfName("DuplexFlipLongEdge")
        /** A name  */
        val DV = PdfName("DV")
        /** A name  */
        val DW = PdfName("DW")
        /** A name  */
        val E = PdfName("E")
        /** A name  */
        val EARLYCHANGE = PdfName("EarlyChange")
        /** A name  */
        val EF = PdfName("EF")
        /**
         * A name
         * @since    2.1.3
         */
        val EFF = PdfName("EFF")
        /**
         * A name
         * @since    2.1.3
         */
        val EFOPEN = PdfName("EFOpen")
        /**
         * A name
         * @since    2.1.6
         */
        val EMBEDDED = PdfName("Embedded")
        /** A name  */
        val EMBEDDEDFILE = PdfName("EmbeddedFile")
        /** A name  */
        val EMBEDDEDFILES = PdfName("EmbeddedFiles")
        /** A name  */
        val ENCODE = PdfName("Encode")
        /** A name  */
        val ENCODEDBYTEALIGN = PdfName("EncodedByteAlign")
        /** A name  */
        val ENCODING = PdfName("Encoding")
        /** A name  */
        val ENCRYPT = PdfName("Encrypt")
        /** A name  */
        val ENCRYPTMETADATA = PdfName("EncryptMetadata")
        /**
         * A name
         * @since 5.3.4
         */
        val END = PdfName("End")
        /**
         * A name
         * @since 5.3.4
         */
        val ENDINDENT = PdfName("EndIndent")
        /** A name  */
        val ENDOFBLOCK = PdfName("EndOfBlock")
        /** A name  */
        val ENDOFLINE = PdfName("EndOfLine")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val EPSG = PdfName("EPSG")
        /**
         * A name
         * @since 5.4.3
         * *
         */
        val ESIC = PdfName("ESIC")
        /** A name
         * @since 5.1.3
         */
        val ETSI_CADES_DETACHED = PdfName("ETSI.CAdES.detached")
        /** A name  */
        val ETSI_RFC3161 = PdfName("ETSI.RFC3161")
        /** A name  */
        val EXCLUDE = PdfName("Exclude")
        /** A name  */
        val EXTEND = PdfName("Extend")
        /**
         * A name
         * @since    2.1.6
         */
        val EXTENSIONS = PdfName("Extensions")
        /**
         * A name
         * @since    2.1.6
         */
        val EXTENSIONLEVEL = PdfName("ExtensionLevel")
        /** A name  */
        val EXTGSTATE = PdfName("ExtGState")
        /** A name  */
        val EXPORT = PdfName("Export")
        /** A name  */
        val EXPORTSTATE = PdfName("ExportState")
        /** A name  */
        val EVENT = PdfName("Event")
        /** A name  */
        val F = PdfName("F")
        /**
         * A name.
         * @since 2.1.6
         */
        val FAR = PdfName("Far")
        /** A name  */
        val FB = PdfName("FB")
        /**
         * A name
         * @since 5.1.0
         */
        val FD = PdfName("FD")
        /** A name  */
        val FDECODEPARMS = PdfName("FDecodeParms")
        /** A name  */
        val FDF = PdfName("FDF")
        /** A name  */
        val FF = PdfName("Ff")
        /** A name  */
        val FFILTER = PdfName("FFilter")
        /**
         * A name
         * @since 5.0.2
         */
        val FG = PdfName("FG")
        /** A name  */
        val FIELDMDP = PdfName("FieldMDP")
        /** A name  */
        val FIELDS = PdfName("Fields")
        /**
         * A name
         * @since 2.1.6
         */
        val FIGURE = PdfName("Figure")
        /** A name  */
        val FILEATTACHMENT = PdfName("FileAttachment")
        /** A name  */
        val FILESPEC = PdfName("Filespec")
        /** A name  */
        val FILTER = PdfName("Filter")
        /** A name  */
        val FIRST = PdfName("First")
        /** A name  */
        val FIRSTCHAR = PdfName("FirstChar")
        /** A name  */
        val FIRSTPAGE = PdfName("FirstPage")
        /** A name  */
        val FIT = PdfName("Fit")
        /** A name  */
        val FITH = PdfName("FitH")
        /** A name  */
        val FITV = PdfName("FitV")
        /** A name  */
        val FITR = PdfName("FitR")
        /** A name  */
        val FITB = PdfName("FitB")
        /** A name  */
        val FITBH = PdfName("FitBH")
        /** A name  */
        val FITBV = PdfName("FitBV")
        /** A name  */
        val FITWINDOW = PdfName("FitWindow")
        /**
         * A name
         * @since 5.0.3
         */
        val FL = PdfName("Fl")
        /** A name  */
        val FLAGS = PdfName("Flags")
        /**
         * A name.
         * @since 2.1.6
         */
        val FLASH = PdfName("Flash")
        /**
         * A name.
         * @since 2.1.6
         */
        val FLASHVARS = PdfName("FlashVars")
        /** A name  */
        val FLATEDECODE = PdfName("FlateDecode")
        /** A name  */
        val FO = PdfName("Fo")
        /** A name  */
        val FONT = PdfName("Font")
        /** A name  */
        val FONTBBOX = PdfName("FontBBox")
        /** A name  */
        val FONTDESCRIPTOR = PdfName("FontDescriptor")
        /** A name  */
        val FONTFAMILY = PdfName("FontFamily")
        /** A name  */
        val FONTFILE = PdfName("FontFile")
        /** A name  */
        val FONTFILE2 = PdfName("FontFile2")
        /** A name  */
        val FONTFILE3 = PdfName("FontFile3")
        /** A name  */
        val FONTMATRIX = PdfName("FontMatrix")
        /** A name  */
        val FONTNAME = PdfName("FontName")
        /** A name  */
        val FONTWEIGHT = PdfName("FontWeight")
        /**
         * A name
         * @since    2.1.6
         */
        val FOREGROUND = PdfName("Foreground")
        /** A name  */
        val FORM = PdfName("Form")
        /** A name  */
        val FORMTYPE = PdfName("FormType")
        /**
         * A name
         * @since 2.1.6
         */
        val FORMULA = PdfName("Formula")
        /** A name  */
        val FREETEXT = PdfName("FreeText")
        /** A name  */
        val FRM = PdfName("FRM")
        /** A name  */
        val FS = PdfName("FS")
        /** A name  */
        val FT = PdfName("FT")
        /** A name  */
        val FULLSCREEN = PdfName("FullScreen")
        /** A name  */
        val FUNCTION = PdfName("Function")
        /** A name  */
        val FUNCTIONS = PdfName("Functions")
        /** A name  */
        val FUNCTIONTYPE = PdfName("FunctionType")
        /** A name of an attribute.  */
        val GAMMA = PdfName("Gamma")
        /** A name of an attribute.  */
        val GBK = PdfName("GBK")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val GCS = PdfName("GCS")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val GEO = PdfName("GEO")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val GEOGCS = PdfName("GEOGCS")
        /** A name of an attribute.  */
        val GLITTER = PdfName("Glitter")
        /** A name of an attribute.  */
        val GOTO = PdfName("GoTo")
        /**
         * A name
         * @since 5.4.5
         */
        val GOTO3DVIEW = PdfName("GoTo3DView")
        /** A name of an attribute.  */
        val GOTOE = PdfName("GoToE")
        /** A name of an attribute.  */
        val GOTOR = PdfName("GoToR")
        /**
         * A name of an attribute.
         * @since 5.1.0
         */
        val GPTS = PdfName("GPTS")
        /** A name of an attribute.  */
        val GROUP = PdfName("Group")
        /** A name of an attribute.  */
        val GTS_PDFA1 = PdfName("GTS_PDFA1")
        /** A name of an attribute.  */
        val GTS_PDFX = PdfName("GTS_PDFX")
        /** A name of an attribute.  */
        val GTS_PDFXVERSION = PdfName("GTS_PDFXVersion")
        /** A name of an attribute.  */
        val H = PdfName("H")
        /**
         * A name
         * @since 2.1.6
         */
        val H1 = PdfName("H1")
        /**
         * A name
         * @since 2.1.6
         */
        val H2 = PdfName("H2")
        /**
         * A name
         * @since 2.1.6
         */
        val H3 = PdfName("H3")
        /**
         * A name
         * @since 2.1.6
         */
        val H4 = PdfName("H4")
        /**
         * A name
         * @since 2.1.6
         */
        val H5 = PdfName("H5")
        /**
         * A name
         * @since 2.1.6
         */
        val H6 = PdfName("H6")
        /**
         * A name
         * @since 5.4.5
         */
        val HALFTONENAME = PdfName("HalftoneName")
        /**
         * A name
         * @since 5.4.5
         */
        val HALFTONETYPE = PdfName("HalftoneType")
/**
 * /**
 * A name.
 * @since 2.1.6
*/
val HALIGN = PdfName("HAlign")
/**
 * A name.
 * @since 5.4.0
*/
val HEADERS = PdfName("Headers")
/** A name of an attribute.  */
val HEIGHT = PdfName("Height")
/** A name  */
val HELV = PdfName("Helv")
/** A name of a base 14 type 1 font  */
val HELVETICA = PdfName("Helvetica")
/** A name of a base 14 type 1 font  */
val HELVETICA_BOLD = PdfName("Helvetica-Bold")
/** A name of a base 14 type 1 font  */
val HELVETICA_OBLIQUE = PdfName("Helvetica-Oblique")
/** A name of a base 14 type 1 font  */
val HELVETICA_BOLDOBLIQUE = PdfName("Helvetica-BoldOblique")
/**
 * A name
 * @since 5.0.2
*/
val HF = PdfName("HF")
/** A name  */
val HID = PdfName("Hid")
/** A name  */
val HIDE = PdfName("Hide")
/** A name  */
val HIDEMENUBAR = PdfName("HideMenubar")
/** A name  */
val HIDETOOLBAR = PdfName("HideToolbar")
/** A name  */
val HIDEWINDOWUI = PdfName("HideWindowUI")
/** A name  */
val HIGHLIGHT = PdfName("Highlight")
/**
 * A name
 * @since 2.1.6
*/
val HOFFSET = PdfName("HOffset")
/**
 * A name
 * @since 5.4.5
*/
val HT = PdfName("HT")
/**
 * A name
 * @since 5.4.5
*/
val HTP = PdfName("HTP")
/** A name  */
val I = PdfName("I")
/**
 * A name
 * @since 5.4.3
*/
val IC = PdfName("IC")
/** A name  */
val ICCBASED = PdfName("ICCBased")
/** A name  */
val ID = PdfName("ID")
/** A name  */
val IDENTITY = PdfName("Identity")
/** A name  */
val IDTREE = PdfName("IDTree")
/** A name  */
val IF = PdfName("IF")
/**
 * A name
 * @since 5.5.3
*/
val IM = PdfName("IM")
/** A name  */
val IMAGE = PdfName("Image")
/** A name  */
val IMAGEB = PdfName("ImageB")
/** A name  */
val IMAGEC = PdfName("ImageC")
/** A name  */
val IMAGEI = PdfName("ImageI")
/** A name  */
val IMAGEMASK = PdfName("ImageMask")
/** A name  */
val INCLUDE = PdfName("Include")
/**
 * A name
 * @since 5.0.2
*/
val IND = PdfName("Ind")
/** A name  */
val INDEX = PdfName("Index")
/** A name  */
val INDEXED = PdfName("Indexed")
/** A name  */
val INFO = PdfName("Info")
/** A name  */
val INK = PdfName("Ink")
/** A name  */
val INKLIST = PdfName("InkList")
/**
 * A name.
 * @since 2.1.6
*/
val INSTANCES = PdfName("Instances")
/** A name  */
val IMPORTDATA = PdfName("ImportData")
/** A name  */
val INTENT = PdfName("Intent")
/** A name  */
val INTERPOLATE = PdfName("Interpolate")
/** A name  */
val ISMAP = PdfName("IsMap")
/** A name  */
val IRT = PdfName("IRT")
/** A name  */
val ITALICANGLE = PdfName("ItalicAngle")
/**
 * A name
 * @since    2.1.6
*/
val ITXT = PdfName("ITXT")
/** A name  */
val IX = PdfName("IX")
/** A name  */
val JAVASCRIPT = PdfName("JavaScript")
/**
 * A name
 * @since    2.1.5
*/
val JBIG2DECODE = PdfName("JBIG2Decode")
/**
 * A name
 * @since    2.1.5
*/
val JBIG2GLOBALS = PdfName("JBIG2Globals")
/** A name  */
val JPXDECODE = PdfName("JPXDecode")
/** A name  */
val JS = PdfName("JS")
/**
 * A name
 * @since 5.3.4
*/
val JUSTIFY = PdfName("Justify")
/** A name  */
val K = PdfName("K")
/** A name  */
val KEYWORDS = PdfName("Keywords")
/** A name  */
val KIDS = PdfName("Kids")
/** A name  */
val L = PdfName("L")
/** A name  */
val L2R = PdfName("L2R")
/**
 * A name
 * @since 5.2.1
*/
val LAB = PdfName("Lab")
/**
 * An entry specifying the natural language, and optionally locale. Use this
 * to specify the Language attribute on a Tagged Pdf element.
 * For the content usage dictionary, use [.LANGUAGE]
*/
val LANG = PdfName("Lang")
/**
 * A dictionary type, strictly for use in the content usage dictionary. For
 * dictionary entries in Tagged Pdf, use [.LANG]
*/
val LANGUAGE = PdfName("Language")
/** A name  */
val LAST = PdfName("Last")
/** A name  */
val LASTCHAR = PdfName("LastChar")
/** A name  */
val LASTPAGE = PdfName("LastPage")
/** A name  */
val LAUNCH = PdfName("Launch")
/**
 * A name
 * @since 5.5.0
*/
val LAYOUT = PdfName("Layout")
/**
 * A name
 * @since 2.1.6
*/
val LBL = PdfName("Lbl")
/**
 * A name
 * @since 2.1.6
*/
val LBODY = PdfName("LBody")
/** A name  */
val LENGTH = PdfName("Length")
/** A name  */
val LENGTH1 = PdfName("Length1")
/**
 * A name
 * @since 2.1.6
*/
val LI = PdfName("LI")
/** A name  */
val LIMITS = PdfName("Limits")
/** A name  */
val LINE = PdfName("Line")
/**
 * A name.
 * @since 2.1.6
*/
val LINEAR = PdfName("Linear")
/**
 * A name.
 * @since 5.3.5
*/
val LINEHEIGHT = PdfName("LineHeight")
/** A name  */
val LINK = PdfName("Link")
/**
 * A name
 * @since 5.4.0
*/
val LIST = PdfName("List")
/** A name  */
val LISTMODE = PdfName("ListMode")
/** A name  */
val LISTNUMBERING = PdfName("ListNumbering")
/** A name  */
val LOCATION = PdfName("Location")
/** A name  */
val LOCK = PdfName("Lock")
/**
 * A name
 * @since    2.1.2
*/
val LOCKED = PdfName("Locked")
/**
 * A name
 * @since    5.4.0
*/
val LOWERALPHA = PdfName("LowerAlpha")
/**
 * A name
 * @since    5.4.0
*/
val LOWERROMAN = PdfName("LowerRoman")
/**
 * A name of an attribute.
 * @since 5.1.0
*/
val LPTS = PdfName("LPTS")
/** A name  */
val LZWDECODE = PdfName("LZWDecode")
/** A name  */
val M = PdfName("M")
/**
 * A name.
 * @since 5.2.1
*/
val MAC = PdfName("Mac")
/**
 * A name
 * @since    2.1.6
*/
val MATERIAL = PdfName("Material")
/** A name  */
val MATRIX = PdfName("Matrix")
/** A name of an encoding  */
val MAC_EXPERT_ENCODING = PdfName("MacExpertEncoding")
/** A name of an encoding  */
val MAC_ROMAN_ENCODING = PdfName("MacRomanEncoding")
/** A name  */
val MARKED = PdfName("Marked")
/** A name  */
val MARKINFO = PdfName("MarkInfo")
/** A name  */
val MASK = PdfName("Mask")
/**
 * A name
 * @since    2.1.6 renamed from MAX
*/
val MAX_LOWER_CASE = PdfName("max")
/**
 * A name
 * @since    2.1.6
*/
val MAX_CAMEL_CASE = PdfName("Max")
/** A name  */
val MAXLEN = PdfName("MaxLen")
/** A name  */
val MEDIABOX = PdfName("MediaBox")
/** A name  */
val MCID = PdfName("MCID")
/** A name  */
val MCR = PdfName("MCR")
/**
 * A name
 * @since    5.1.0
*/
val MEASURE = PdfName("Measure")
/** A name  */
val METADATA = PdfName("Metadata")
/**
 * A name
 * @since    2.1.6 renamed from MIN
*/
val MIN_LOWER_CASE = PdfName("min")
/**
 * A name
 * @since    2.1.6
*/
val MIN_CAMEL_CASE = PdfName("Min")
/** A name  */
val MK = PdfName("MK")
/** A name  */
val MMTYPE1 = PdfName("MMType1")
/** A name  */
val MODDATE = PdfName("ModDate")
/**
 * A name
 * @since    5.4.3
*/
val MOVIE = PdfName("Movie")
/** A name  */
val N = PdfName("N")
/** A name  */
val N0 = PdfName("n0")
/** A name  */
val N1 = PdfName("n1")
/** A name  */
val N2 = PdfName("n2")
/** A name  */
val N3 = PdfName("n3")
/** A name  */
val N4 = PdfName("n4")
/** A name  */
val NAME = PdfName("Name")
/** A name  */
val NAMED = PdfName("Named")
/** A name  */
val NAMES = PdfName("Names")
/**
 * A name.
 * @since 2.1.6
*/
val NAVIGATION = PdfName("Navigation")
/**
 * A name.
 * @since 2.1.6
*/
val NAVIGATIONPANE = PdfName("NavigationPane")
val NCHANNEL = PdfName("NChannel")
/**
 * A name.
 * @since 2.1.6
*/
val NEAR = PdfName("Near")
/** A name  */
val NEEDAPPEARANCES = PdfName("NeedAppearances")
/**
 * A name.
 * @since 5.4.5
*/
val NEEDRENDERING = PdfName("NeedsRendering")
/** A name  */
val NEWWINDOW = PdfName("NewWindow")
/** A name  */
val NEXT = PdfName("Next")
/** A name  */
val NEXTPAGE = PdfName("NextPage")
/** A name  */
val NM = PdfName("NM")
/** A name  */
val NONE = PdfName("None")
/** A name  */
val NONFULLSCREENPAGEMODE = PdfName("NonFullScreenPageMode")
/**
 * A name
 * @since 2.1.6
*/
val NONSTRUCT = PdfName("NonStruct")
/**
 * A name.
 * @since 5.0.2
*/
val NOT = PdfName("Not")
/**
 * A name
 * @since 2.1.6
*/
val NOTE = PdfName("Note")
/**
 * A name
 * @since 5.1.0
*/
val NUMBERFORMAT = PdfName("NumberFormat")
/** A name  */
val NUMCOPIES = PdfName("NumCopies")
/** A name  */
val NUMS = PdfName("Nums")
/** A name  */
val O = PdfName("O")
/**
 * A name used with Document Structure
 * @since 2.1.5
*/
val OBJ = PdfName("Obj")
/**
 * a name used with Document Structure
 * @since 2.1.5
*/
val OBJR = PdfName("OBJR")
/** A name  */
val OBJSTM = PdfName("ObjStm")
/** A name  */
val OC = PdfName("OC")
/** A name  */
val OCG = PdfName("OCG")
/** A name  */
val OCGS = PdfName("OCGs")
/** A name  */
val OCMD = PdfName("OCMD")
/** A name  */
val OCPROPERTIES = PdfName("OCProperties")
/** A name
 * @since 5.1.3
*/
val OCSP = PdfName("OCSP")
/** A name
 * @since 5.1.3
*/
val OCSPS = PdfName("OCSPs")
/** A name  */
val OE = PdfName("OE")
/** A name  */
val Off = PdfName("Off")
/** A name  */
val OFF = PdfName("OFF")
/** A name  */
val ON = PdfName("ON")
/** A name  */
val ONECOLUMN = PdfName("OneColumn")
/** A name  */
val OPEN = PdfName("Open")
/** A name  */
val OPENACTION = PdfName("OpenAction")
/** A name  */
val OP = PdfName("OP")
/** A name  */
val op = PdfName("op")
/** A name
 * @since 5.4.3
*/
val OPI = PdfName("OPI")
/** A name  */
val OPM = PdfName("OPM")
/** A name  */
val OPT = PdfName("Opt")
/**
 * A name.
 * @since 5.0.2
*/
val OR = PdfName("Or")
/** A name  */
val ORDER = PdfName("Order")
/** A name  */
val ORDERING = PdfName("Ordering")
/**
 * A name
 * @since 5.0.2
*/
val ORG = PdfName("Org")
/**
 * A name.
 * @since 2.1.6
*/
val OSCILLATING = PdfName("Oscillating")

/** A name  */
val OUTLINES = PdfName("Outlines")
/** A name  */
val OUTPUTCONDITION = PdfName("OutputCondition")
/** A name  */
val OUTPUTCONDITIONIDENTIFIER = PdfName("OutputConditionIdentifier")
/** A name  */
val OUTPUTINTENT = PdfName("OutputIntent")
/** A name  */
val OUTPUTINTENTS = PdfName("OutputIntents")
/**
 * A name
 * @since 5.5.4
*/
val OVERLAYTEXT = PdfName("OverlayText")
/** A name  */
val P = PdfName("P")
/** A name  */
val PAGE = PdfName("Page")
/**
 * A name
 * @since 5.0.2
*/
val PAGEELEMENT = PdfName("PageElement")
/** A name  */
val PAGELABELS = PdfName("PageLabels")
/** A name  */
val PAGELAYOUT = PdfName("PageLayout")
/** A name  */
val PAGEMODE = PdfName("PageMode")
/** A name  */
val PAGES = PdfName("Pages")
/** A name  */
val PAINTTYPE = PdfName("PaintType")
/** A name  */
val PANOSE = PdfName("Panose")
/** A name  */
val PARAMS = PdfName("Params")
/** A name  */
val PARENT = PdfName("Parent")
/** A name  */
val PARENTTREE = PdfName("ParentTree")
/**
 * A name used in defining Document Structure.
 * @since 2.1.5
*/
val PARENTTREENEXTKEY = PdfName("ParentTreeNextKey")
/**
 * A name
 * @since 2.1.6
*/
val PART = PdfName("Part")
/**
 * A name.
 * @since 2.1.6
*/
val PASSCONTEXTCLICK = PdfName("PassContextClick")
/** A name  */
val PATTERN = PdfName("Pattern")
/** A name  */
val PATTERNTYPE = PdfName("PatternType")
/**
 * A name
 * @since 5.4.4
*/
val PB = PdfName("pb")
/**
 * A name.
 * @since 2.1.6
*/
val PC = PdfName("PC")
/** A name  */
val PDF = PdfName("PDF")
/** A name  */
val PDFDOCENCODING = PdfName("PDFDocEncoding")
/**
 * A name of an attribute.
 * @since 5.1.0
*/
val PDU = PdfName("PDU")
/** A name  */
val PERCEPTUAL = PdfName("Perceptual")
/** A name  */
val PERMS = PdfName("Perms")
/** A name  */
val PG = PdfName("Pg")
/**
 * A name.
 * @since 2.1.6
*/
val PI = PdfName("PI")
/** A name  */
val PICKTRAYBYPDFSIZE = PdfName("PickTrayByPDFSize")
/**
 * A name
 * @since 5.5.0
*/
val PIECEINFO = PdfName("PieceInfo")
/**
 * A name.
 * @since 2.1.6
*/
val PLAYCOUNT = PdfName("PlayCount")
/**
 * A name.
 * @since 2.1.6
*/
val PO = PdfName("PO")
/**
 * A name.
 * @since 5.0.2
*/
val POLYGON = PdfName("Polygon")
/**
 * A name.
 * @since 5.0.2
*/
val POLYLINE = PdfName("PolyLine")
/** A name  */
val POPUP = PdfName("Popup")
/**
 * A name.
 * @since 2.1.6
*/
val POSITION = PdfName("Position")
/** A name  */
val PREDICTOR = PdfName("Predictor")
/** A name  */
val PREFERRED = PdfName("Preferred")
/**
 * A name.
 * @since 2.1.6
*/
val PRESENTATION = PdfName("Presentation")
/** A name  */
val PRESERVERB = PdfName("PreserveRB")
/**
 * A name.
 * @since 5.4.5
*/
val PRESSTEPS = PdfName("PresSteps")
/** A name  */
val PREV = PdfName("Prev")
/** A name  */
val PREVPAGE = PdfName("PrevPage")
/** A name  */
val PRINT = PdfName("Print")
/** A name  */
val PRINTAREA = PdfName("PrintArea")
/** A name  */
val PRINTCLIP = PdfName("PrintClip")
/**
 * A name
 * @since 5.4.3
*/
val PRINTERMARK = PdfName("PrinterMark")
/**
 * A name
 * @since 5.4.4
*/
val PRINTFIELD = PdfName("PrintField")
/** A name  */
val PRINTPAGERANGE = PdfName("PrintPageRange")
/** A name  */
val PRINTSCALING = PdfName("PrintScaling")
/** A name  */
val PRINTSTATE = PdfName("PrintState")
/**
 * A name
 * @since 2.1.6
*/
val PRIVATE = PdfName("Private")
/** A name  */
val PROCSET = PdfName("ProcSet")
/** A name  */
val PRODUCER = PdfName("Producer")
/**
 * A name of an attribute.
 * @since 5.1.0
*/
val PROJCS = PdfName("PROJCS")
/** A name  */
val PROP_BUILD = PdfName("Prop_Build")
/** A name  */
val PROPERTIES = PdfName("Properties")
/** A name  */
val PS = PdfName("PS")
/**
 * A name
 * @since 5.1.0
*/
val PTDATA = PdfName("PtData")
/** A name  */
val PUBSEC = PdfName("Adobe.PubSec")
/**
 * A name.
 * @since 2.1.6
*/
val PV = PdfName("PV")
/** A name  */
val Q = PdfName("Q")
/** A name  */
val QUADPOINTS = PdfName("QuadPoints")
/**
 * A name
 * @since 2.1.6
*/
val QUOTE = PdfName("Quote")
/** A name  */
val R = PdfName("R")
/** A name  */
val R2L = PdfName("R2L")
/** A name  */
val RANGE = PdfName("Range")
/**
 * A name
 * @since 5.4.3
*/
val RB = PdfName("RB")
/**
 * A name
 * @since 5.4.4
*/
val rb = PdfName("rb")
/** A name  */
val RBGROUPS = PdfName("RBGroups")
/** A name  */
val RC = PdfName("RC")
/**
 * A name
 * @since 5.1.0
*/
val RD = PdfName("RD")
/** A name  */
val REASON = PdfName("Reason")
/** A name  */
val RECIPIENTS = PdfName("Recipients")
/** A name  */
val RECT = PdfName("Rect")
/**
 * A name
 * @since 5.4.4
*/
val REDACT = PdfName("Redact")
/** A name  */
val REFERENCE = PdfName("Reference")
/** A name  */
val REGISTRY = PdfName("Registry")
/** A name  */
val REGISTRYNAME = PdfName("RegistryName")
/**
 * A name
 * @since    2.1.5 renamed from RELATIVECALORIMETRIC
*/
val RELATIVECOLORIMETRIC = PdfName("RelativeColorimetric")
/** A name  */
val RENDITION = PdfName("Rendition")
/**
 * A name
 * @since 5.5.4
*/
val REPEAT = PdfName("Repeat")
/** A name  */
val RESETFORM = PdfName("ResetForm")
/** A name  */
val RESOURCES = PdfName("Resources")
val REQUIREMENTS = PdfName("Requirements")
val REVERSEDCHARS = PdfName("ReversedChars")
/** A name  */
val RI = PdfName("RI")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIA = PdfName("RichMedia")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAACTIVATION = PdfName("RichMediaActivation")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAANIMATION = PdfName("RichMediaAnimation")
/**
 * A name
 * @since    2.1.6
*/
val RICHMEDIACOMMAND = PdfName("RichMediaCommand")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIACONFIGURATION = PdfName("RichMediaConfiguration")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIACONTENT = PdfName("RichMediaContent")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIADEACTIVATION = PdfName("RichMediaDeactivation")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAEXECUTE = PdfName("RichMediaExecute")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAINSTANCE = PdfName("RichMediaInstance")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAPARAMS = PdfName("RichMediaParams")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAPOSITION = PdfName("RichMediaPosition")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAPRESENTATION = PdfName("RichMediaPresentation")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIASETTINGS = PdfName("RichMediaSettings")
/**
 * A name.
 * @since 2.1.6
*/
val RICHMEDIAWINDOW = PdfName("RichMediaWindow")
/**
 * A name of an attribute.
 * @since 5.1.0
*/
val RL = PdfName("RL")
/**
 * A name
 * @since 5.4.4
*/
val ROLE = PdfName("Role")
/**
 * A name
 * @since 5.4.4
*/
val RO = PdfName("RO")
/** A name  */
val ROLEMAP = PdfName("RoleMap")
/** A name  */
val ROOT = PdfName("Root")
/** A name  */
val ROTATE = PdfName("Rotate")
/**
 * A name
 * @since 5.4.0
*/
val ROW = PdfName("Row")
/** A name  */
val ROWS = PdfName("Rows")
/**
 * A name
 * @since 5.4.0
*/
val ROWSPAN = PdfName("RowSpan")
/**
 * A name
 * @since 5.4.3
*/
val RP = PdfName("RP")
/**
 * A name
 * @since 5.1.0
*/
val RT = PdfName("RT")
/**
 * A name
 * @since 2.1.6
*/
val RUBY = PdfName("Ruby")
/** A name  */
val RUNLENGTHDECODE = PdfName("RunLengthDecode")
/** A name  */
val RV = PdfName("RV")
/** A name  */
val S = PdfName("S")
/** A name  */
val SATURATION = PdfName("Saturation")
/** A name  */
val SCHEMA = PdfName("Schema")
/**
 * A name.
 * @since 5.4.0
*/
val SCOPE = PdfName("Scope")
/** A name  */
val SCREEN = PdfName("Screen")
/**
 * A name.
 * @since 2.1.6
*/
val SCRIPTS = PdfName("Scripts")
/** A name  */
val SECT = PdfName("Sect")
/** A name  */
val SEPARATION = PdfName("Separation")
/** A name  */
val SETOCGSTATE = PdfName("SetOCGState")
/**
 * A name.
 * @since 2.1.6
*/
val SETTINGS = PdfName("Settings")
/** A name  */
val SHADING = PdfName("Shading")
/** A name  */
val SHADINGTYPE = PdfName("ShadingType")
/** A name  */
val SHIFT_JIS = PdfName("Shift-JIS")
/** A name  */
val SIG = PdfName("Sig")
/** A name  */
val SIGFIELDLOCK = PdfName("SigFieldLock")
/** A name  */
val SIGFLAGS = PdfName("SigFlags")
/** A name  */
val SIGREF = PdfName("SigRef")
/** A name  */
val SIMPLEX = PdfName("Simplex")
/** A name  */
val SINGLEPAGE = PdfName("SinglePage")
/** A name  */
val SIZE = PdfName("Size")
/** A name  */
val SMASK = PdfName("SMask")

val SMASKINDATA = PdfName("SMaskInData")
/** A name  */
val SORT = PdfName("Sort")
/**
 * A name.
 * @since 2.1.6
*/
val SOUND = PdfName("Sound")
/**
 * A name
 * @since 5.3.4
*/
val SPACEAFTER = PdfName("SpaceAfter")
/**
 * A name
 * @since 5.3.4
*/
val SPACEBEFORE = PdfName("SpaceBefore")
/** A name  */
val SPAN = PdfName("Span")
/**
 * A name.
 * @since 2.1.6
*/
val SPEED = PdfName("Speed")
/** A name  */
val SPLIT = PdfName("Split")
/** A name  */
val SQUARE = PdfName("Square")
/**
 * A name
 * @since 2.1.3
*/
val SQUIGGLY = PdfName("Squiggly")
/**
 * A name
 * @since 5.1.0
*/
val SS = PdfName("SS")
/** A name  */
val ST = PdfName("St")
/** A name  */
val STAMP = PdfName("Stamp")
/** A name  */
val STATUS = PdfName("Status")
/** A name  */
val STANDARD = PdfName("Standard")
/**
 * A name
 * @since 5.3.4
*/
val START = PdfName("Start")
/**
 * A name
 * @since 5.3.4
*/
val STARTINDENT = PdfName("StartIndent")
/** A name  */
val STATE = PdfName("State")
/** A name  */
val STDCF = PdfName("StdCF")
/** A name  */
val STEMV = PdfName("StemV")
/** A name  */
val STMF = PdfName("StmF")
/** A name  */
val STRF = PdfName("StrF")
/** A name  */
val STRIKEOUT = PdfName("StrikeOut")
/**
 * A name.
 * @since iText 5.0.6
*/
val STRUCTELEM = PdfName("StructElem")
/** A name  */
val STRUCTPARENT = PdfName("StructParent")
/** A name  */
val STRUCTPARENTS = PdfName("StructParents")
/** A name  */
val STRUCTTREEROOT = PdfName("StructTreeRoot")
/** A name  */
val STYLE = PdfName("Style")
/** A name  */
val SUBFILTER = PdfName("SubFilter")
/** A name  */
val SUBJECT = PdfName("Subject")
/** A name  */
val SUBMITFORM = PdfName("SubmitForm")
/** A name  */
val SUBTYPE = PdfName("Subtype")
/**
 * A name
*/
val SUMMARY = PdfName("Summary")
/** A name  */
val SUPPLEMENT = PdfName("Supplement")
/** A name  */
val SV = PdfName("SV")
/** A name  */
val SW = PdfName("SW")
/** A name of a base 14 type 1 font  */
val SYMBOL = PdfName("Symbol")
/**
 * T is very commonly used for various dictionary entries, including title
 * entries in a Tagged PDF element dictionary, and target dictionaries.
*/
val T = PdfName("T")
/**
 * A name
 * @since    2.1.6
*/
val TA = PdfName("TA")
/**
 * A name
 * @since 2.1.6
*/
val TABLE = PdfName("Table")
/**
 * A name
 * @since    2.1.5
*/
val TABS = PdfName("Tabs")
/**
 * A name
 * @since 2.1.6
*/
val TBODY = PdfName("TBody")
/**
 * A name
 * @since 2.1.6
*/
val TD = PdfName("TD")
/**
 * A name
 * @since 5.3.5
*/
val TR = PdfName("TR")
/**
 * A name
 * @since 5.4.3
*/
val TR2 = PdfName("TR2")
/** A name  */
val TEXT = PdfName("Text")
/**
 * A name
 * @since 5.3.4
*/
val TEXTALIGN = PdfName("TextAlign")
/**
 * A name
 * @since 5.3.5
*/
val TEXTDECORATIONCOLOR = PdfName("TextDecorationColor")
/**
 * A name
 * @since 5.3.5
*/
val TEXTDECORATIONTHICKNESS = PdfName("TextDecorationThickness")
/**
 * A name
 * @since 5.3.5
*/
val TEXTDECORATIONTYPE = PdfName("TextDecorationType")
/**
 * A name
 * @since 5.3.4
*/
val TEXTINDENT = PdfName("TextIndent")
/**
 * A name
 * @since 2.1.6
*/
val TFOOT = PdfName("TFoot")
/**
 * A name
 * @since 2.1.6
*/
val TH = PdfName("TH")
/**
 * A name
 * @since 2.1.6
*/
val THEAD = PdfName("THead")
/** A name  */
val THUMB = PdfName("Thumb")
/** A name  */
val THREADS = PdfName("Threads")
/** A name  */
val TI = PdfName("TI")
/**
 * A name
 * @since    2.1.6
*/
val TIME = PdfName("Time")
/** A name  */
val TILINGTYPE = PdfName("TilingType")
/** A name of a base 14 type 1 font  */
val TIMES_ROMAN = PdfName("Times-Roman")
/** A name of a base 14 type 1 font  */
val TIMES_BOLD = PdfName("Times-Bold")
/** A name of a base 14 type 1 font  */
val TIMES_ITALIC = PdfName("Times-Italic")
/** A name of a base 14 type 1 font  */
val TIMES_BOLDITALIC = PdfName("Times-BoldItalic")
/**
 * Use Title for the document's top level title (optional), and for document
 * outline dictionaries, which can store bookmarks.
 * For all other uses of a title entry, including Tagged PDF, use [.T]
*/
val TITLE = PdfName("Title")
/** A name  */
val TK = PdfName("TK")
/** A name  */
val TM = PdfName("TM")
/**
 * A name
 * @since 2.1.6
*/
val TOC = PdfName("TOC")
/**
 * A name
 * @since 2.1.6
*/
val TOCI = PdfName("TOCI")
/** A name  */
val TOGGLE = PdfName("Toggle")
/**
 * A name.
 * @since 2.1.6
*/
val TOOLBAR = PdfName("Toolbar")
/** A name  */
val TOUNICODE = PdfName("ToUnicode")
/** A name  */
val TP = PdfName("TP")
/**
 * A name
 * @since 2.1.6
*/
val TABLEROW = PdfName("TR")
/** A name  */
val TRANS = PdfName("Trans")
/** A name  */
val TRANSFORMPARAMS = PdfName("TransformParams")
/** A name  */
val TRANSFORMMETHOD = PdfName("TransformMethod")
/** A name  */
val TRANSPARENCY = PdfName("Transparency")
/**
 * A name.
 * @since 2.1.6
*/
val TRANSPARENT = PdfName("Transparent")
/**
 * A name
 * @since 5.4.3
*/
val TRAPNET = PdfName("TrapNet")
/** A name  */
val TRAPPED = PdfName("Trapped")
/** A name  */
val TRIMBOX = PdfName("TrimBox")
/** A name  */
val TRUETYPE = PdfName("TrueType")
/**
 * A name
 * @since 5.1.3
*/
val TS = PdfName("TS")
/**
 * A name
 * @since 5.0.2
*/
val TTL = PdfName("Ttl")
/** A name  */
val TU = PdfName("TU")
/**
 * A name
 * @since 5.4.4
*/
val TV = PdfName("tv")
/** A name  */
val TWOCOLUMNLEFT = PdfName("TwoColumnLeft")
/** A name  */
val TWOCOLUMNRIGHT = PdfName("TwoColumnRight")
/** A name  */
val TWOPAGELEFT = PdfName("TwoPageLeft")
/** A name  */
val TWOPAGERIGHT = PdfName("TwoPageRight")
/** A name  */
val TX = PdfName("Tx")
/** A name  */
val TYPE = PdfName("Type")
/** A name  */
val TYPE0 = PdfName("Type0")
/** A name  */
val TYPE1 = PdfName("Type1")
/** A name of an attribute.  */
val TYPE3 = PdfName("Type3")
/** A name of an attribute.  */
val U = PdfName("U")
/** A name  */
val UE = PdfName("UE")
/** A name of an attribute.  */
val UF = PdfName("UF")
/** A name of an attribute.  */
val UHC = PdfName("UHC")
/** A name of an attribute.  */
val UNDERLINE = PdfName("Underline")
/**
 * A name.
 * @since 5.2.1
*/
val UNIX = PdfName("Unix")
/**
 * A name.
 * @since 5.4.0
*/
val UPPERALPHA = PdfName("UpperAlpha")
/**
 * A name.
 * @since 5.4.0
*/
val UPPERROMAN = PdfName("UpperRoman")
/** A name  */
val UR = PdfName("UR")
/** A name  */
val UR3 = PdfName("UR3")
/** A name  */
val URI = PdfName("URI")
/** A name  */
val URL = PdfName("URL")
/** A name  */
val USAGE = PdfName("Usage")
/** A name  */
val USEATTACHMENTS = PdfName("UseAttachments")
/** A name  */
val USENONE = PdfName("UseNone")
/** A name  */
val USEOC = PdfName("UseOC")
/** A name  */
val USEOUTLINES = PdfName("UseOutlines")
/** A name  */
val USER = PdfName("User")
/** A name  */
val USERPROPERTIES = PdfName("UserProperties")
/** A name  */
val USERUNIT = PdfName("UserUnit")
/** A name  */
val USETHUMBS = PdfName("UseThumbs")
/**
 * A name.
 * @since 5.4.0
*/
val UTF_8 = PdfName("utf_8")
/** A name  */
val V = PdfName("V")
/** A name  */
val V2 = PdfName("V2")
/**
 * A name.
 * @since 2.1.6
*/
val VALIGN = PdfName("VAlign")
/**
 * A name.
 * @since 5.0.2
*/
val VE = PdfName("VE")
/** A name  */
val VERISIGN_PPKVS = PdfName("VeriSign.PPKVS")
/** A name  */
val VERSION = PdfName("Version")
/**
 * A name.
 * @since 5.0.2
*/
val VERTICES = PdfName("Vertices")
/**
 * A name.
 * @since 2.1.6
*/
val VIDEO = PdfName("Video")
/** A name  */
val VIEW = PdfName("View")
/**
 * A name.
 * @since 2.1.6
*/
val VIEWS = PdfName("Views")
/** A name  */
val VIEWAREA = PdfName("ViewArea")
/** A name  */
val VIEWCLIP = PdfName("ViewClip")
/** A name  */
val VIEWERPREFERENCES = PdfName("ViewerPreferences")
/**
 * A name
 * @since 5.1.0
*/
val VIEWPORT = PdfName("Viewport")
/** A name  */
val VIEWSTATE = PdfName("ViewState")
/** A name  */
val VISIBLEPAGES = PdfName("VisiblePages")
/**
 * A name.
 * @since 2.1.6
*/
val VOFFSET = PdfName("VOffset")
/**
 * A name
 * @since 5.1.0
*/
val VP = PdfName("VP")
/**
 * A name
 * @since 5.1.3
*/
val VRI = PdfName("VRI")
/** A name of an attribute.  */
val W = PdfName("W")
/** A name of an attribute.  */
val W2 = PdfName("W2")
/**
 * A name
 * @since 2.1.6
*/
val WARICHU = PdfName("Warichu")
/**
 * A name
 * @since 5.4.5
*/
val WATERMARK = PdfName("Watermark")
/** A name of an attribute.  */
val WC = PdfName("WC")
/** A name of an attribute.  */
val WIDGET = PdfName("Widget")
/** A name of an attribute.  */
val WIDTH = PdfName("Width")
/** A name  */
val WIDTHS = PdfName("Widths")
/** A name of an encoding  */
val WIN = PdfName("Win")
/** A name of an encoding  */
val WIN_ANSI_ENCODING = PdfName("WinAnsiEncoding")
/**
 * A name.
 * @since 2.1.6
*/
val WINDOW = PdfName("Window")
/**
 * A name.
 * @since 2.1.6
*/
val WINDOWED = PdfName("Windowed")
/** A name of an encoding  */
val WIPE = PdfName("Wipe")
/** A name  */
val WHITEPOINT = PdfName("WhitePoint")
/**
 * A name of an attribute.
 * @since 5.1.0
*/
val WKT = PdfName("WKT")
/** A name  */
val WP = PdfName("WP")
/** A name of an encoding  */
val WS = PdfName("WS")
/**
 * A name
 * @since 5.4.3
*/
val WT = PdfName("WT")
/** A name  */
val X = PdfName("X")
/**
 * A name.
 * @since 2.1.6
*/
val XA = PdfName("XA")
/**
 * A name.
 * @since 2.1.6
*/
val XD = PdfName("XD")
/** A name  */
val XFA = PdfName("XFA")
/** A name  */
val XML = PdfName("XML")
/** A name  */
val XOBJECT = PdfName("XObject")
/**
 * A name
 * @since 5.1.0
*/
val XPTS = PdfName("XPTS")
/** A name  */
val XREF = PdfName("XRef")
/** A name  */
val XREFSTM = PdfName("XRefStm")
/** A name  */
val XSTEP = PdfName("XStep")
/** A name  */
val XYZ = PdfName("XYZ")
/** A name  */
val YSTEP = PdfName("YStep")
/** A name  */
val ZADB = PdfName("ZaDb")
/** A name of a base 14 type 1 font  */
val ZAPFDINGBATS = PdfName("ZapfDingbats")
/** A name  */
val ZOOM = PdfName("Zoom")

/**
 * map strings to all known static names
 * @since 2.1.6
*/
var staticNames:MutableMap<String, PdfName>

/**
 * Use reflection to cache all the static public final names so
 * future `PdfName` additions don't have to be "added twice".
 * A bit less efficient (around 50ms spent here on a 2.2ghz machine),
 * but Much Less error prone.
 * @since 2.1.6
*/

init{
val fields = PdfName::class.java!!.getDeclaredFields()
staticNames = HashMap<String, PdfName>(fields.size)
val flags = Modifier.STATIC or Modifier.PUBLIC or Modifier.FINAL
try
{
for (fldIdx in fields.indices)
{
val curFld = fields[fldIdx]
if ((curFld.getModifiers() and flags) == flags && curFld.getType() == PdfName::class.java)
{
val name = curFld.get(null) as PdfName
staticNames.put(decodeName(name.toString()), name)
}
}
}
catch (e:Exception) {
e.printStackTrace()
}

}

/**
 * Encodes a plain name given in the unescaped form "AB CD" into "/AB#20CD".

 * @param name the name to encode
 * *
 * @return the encoded name
 * *
 * @since    2.1.5
*/
fun encodeName(name:String):ByteArray {
val length = name.length
val buf = ByteBuffer(length + 20)
buf.append('/')
val c:Char
val chars = name.toCharArray()
for (k in 0..length - 1)
{
c = (chars[k].toInt() and 0xff).toChar()
// Escape special characters
when (c) {
' ', '%', '(', ')', '<', '>', '[', ']', '{', '}', '/', '#' -> {
buf.append('#')
buf.append(Integer.toString(c.toInt(), 16))
}
else -> if (c.toInt() >= 32 && c.toInt() <= 126)
buf.append(c)
else
{
buf.append('#')
if (c.toInt() < 16)
buf.append('0')
buf.append(Integer.toString(c.toInt(), 16))
}
}
}
return buf.toByteArray()
}

/**
 * Decodes an escaped name given in the form "/AB#20CD" into "AB CD".

 * @param name the name to decode
 * *
 * @return the decoded name
*/
fun decodeName(name:String):String {
val buf = StringBuffer()
try
{
val len = name.length
var k = 1
while (k < len)
{
var c = name.get(k)
if (c == '#')
{
val c1 = name.get(k + 1)
val c2 = name.get(k + 2)
c = ((PRTokeniser.getHex(c1.toInt()) shl 4) + PRTokeniser.getHex(c2.toInt())).toChar()
k += 2
}
buf.append(c)
++k
}
}
catch (e:IndexOutOfBoundsException) {// empty on purpose
}

return buf.toString()
}
}
}// CONSTRUCTORS
/**
 * Constructs a new PdfName. The name length will be checked.

 * @param name the new name
*/
