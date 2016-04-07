/*
 * $Id: 0627b24d711fa080aa1e1862d2ba41e2828e8a1b $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Utilities
import java.io.IOException

import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.pdf.fonts.cmaps.CMapByteCid
import com.itextpdf.text.pdf.fonts.cmaps.CMapCache
import com.itextpdf.text.pdf.fonts.cmaps.CMapCidUni
import com.itextpdf.text.pdf.fonts.cmaps.CMapParserEx
import com.itextpdf.text.pdf.fonts.cmaps.CMapSequence
import com.itextpdf.text.pdf.fonts.cmaps.CMapToUnicode
import com.itextpdf.text.pdf.fonts.cmaps.CidLocationFromByte
import com.itextpdf.text.pdf.fonts.cmaps.IdentityToUnicode//import java.util.ArrayList;


/**
 * Implementation of DocumentFont used while parsing PDF streams.
 * @since 2.1.4
 */
class CMapAwareDocumentFont : DocumentFont {

    /** The font dictionary.  */
    private var fontDic: PdfDictionary? = null
    /** the width of a space for this font, in normalized 1000 point units  */
    private var spaceWidth: Int = 0
    /** The CMap constructed from the ToUnicode map from the font's dictionary, if present.
     * This CMap transforms CID values into unicode equivalent
     */
    private var toUnicodeCmap: CMapToUnicode? = null
    private var byteCid: CMapByteCid? = null
    private var cidUni: CMapCidUni? = null
    /**
     * Mapping between CID code (single byte only for now) and unicode equivalent
     * as derived by the font's encoding.  Only needed if the ToUnicode CMap is not provided.
     */
    private var cidbyte2uni: CharArray? = null

    private var uni2cid: Map<Int, Int>? = null

    constructor(font: PdfDictionary) : super(font) {
        fontDic = font
        initFont()
    }

    /**
     * Creates an instance of a CMapAwareFont based on an indirect reference to a font.
     * @param refFont    the indirect reference to a font
     */
    constructor(refFont: PRIndirectReference) : super(refFont) {
        fontDic = PdfReader.getPdfObjectRelease(refFont) as PdfDictionary?
        initFont()
    }

    private fun initFont() {
        processToUnicode()
        try {
            //if (toUnicodeCmap == null)
            processUni2Byte()

            spaceWidth = super.getWidth(' ')
            if (spaceWidth == 0) {
                spaceWidth = computeAverageWidth()
            }
            if (cjkEncoding != null) {
                byteCid = CMapCache.getCachedCMapByteCid(cjkEncoding)
                cidUni = CMapCache.getCachedCMapCidUni(uniMap)
            }
        } catch (ex: Exception) {
            throw ExceptionConverter(ex)
        }

    }

    /**
     * Parses the ToUnicode entry, if present, and constructs a CMap for it
     * @since 2.1.7
     */
    private fun processToUnicode() {
        val toUni = PdfReader.getPdfObjectRelease(fontDic!!.get(PdfName.TOUNICODE))
        if (toUni is PRStream) {
            try {
                val touni = PdfReader.getStreamBytes(toUni)
                val lb = CidLocationFromByte(touni)
                toUnicodeCmap = CMapToUnicode()
                CMapParserEx.parseCid("", toUnicodeCmap, lb)
                uni2cid = toUnicodeCmap!!.createReverseMapping()
            } catch (e: IOException) {
                toUnicodeCmap = null
                uni2cid = null
                // technically, we should log this or provide some sort of feedback... but sometimes the cmap will be junk, but it's still possible to get text, so we don't want to throw an exception
                //throw new IllegalStateException("Unable to process ToUnicode map - " + e.getMessage(), e);
            }

        } else if (isType0) {
            // fake a ToUnicode for CJK Identity-H fonts
            try {
                val encodingName = fontDic!!.getAsName(PdfName.ENCODING) ?: return
                val enc = PdfName.decodeName(encodingName.toString())
                if (enc != "Identity-H")
                    return
                val df = PdfReader.getPdfObjectRelease(fontDic!!.get(PdfName.DESCENDANTFONTS)) as PdfArray?
                val cidft = PdfReader.getPdfObjectRelease(df.getPdfObject(0)) as PdfDictionary?
                val cidinfo = cidft.getAsDict(PdfName.CIDSYSTEMINFO) ?: return
                val ordering = cidinfo.getAsString(PdfName.ORDERING) ?: return
                val touni = IdentityToUnicode.GetMapFromOrdering(ordering.toUnicodeString()) ?: return
                toUnicodeCmap = touni
                uni2cid = toUnicodeCmap!!.createReverseMapping()
            } catch (e: IOException) {
                toUnicodeCmap = null
                uni2cid = null
            }

        }
    }

    /**
     * Inverts DocumentFont's uni2byte mapping to obtain a cid-to-unicode mapping based
     * on the font's encoding
     * @throws IOException
     * *
     * @since 2.1.7
     */
    @Throws(IOException::class)
    private fun processUni2Byte() {
        //IntHashtable uni2byte = getUni2Byte();
        //int e[] = uni2byte.toOrderedKeys();
        //if (e.length == 0)
        //    return;

        val byte2uni = byte2Uni
        var e = byte2uni.toOrderedKeys()
        if (e.size == 0)
            return

        cidbyte2uni = CharArray(256)
        for (k in e.indices) {
            val key = e[k]
            cidbyte2uni[key] = byte2uni.get(key).toChar()
        }
        if (toUnicodeCmap != null) {
            /*
        	for (int k = 0; k < e.length; ++k) {
        		// Kevin Day:
        		// this is messy, messy - an encoding can have multiple unicode values mapping to the same cid - we are going to arbitrarily choose the first one
        		// what we really need to do is to parse the encoding, and handle the differences info ourselves.  This is a huge duplication of code of what is already
        		// being done in DocumentFont, so I really hate to go down that path without seriously thinking about a change in the organization of the Font class hierarchy
        		
        		// Bruno Lowagie:
        		// I wish I could fix this in a better way, for instance by creating a uni2byte intHashtable in DocumentFont.
        		// However, I chose a quick & dirty solution, allowing intHashtable to store an array of int values.
        		ArrayList<Integer> nList = uni2byte.getValues(e[k]);
        		for (int n : nList) {
        			if (n < 256 && cidbyte2uni[n] == 0)
        				cidbyte2uni[n] = (char)e[k];
        		}
        	}
        	*/
            val dm = toUnicodeCmap!!.createDirectMapping()
            for (kv in dm.entries) {
                if (kv.key < 256) {
                    cidbyte2uni[kv.key.toInt()] = kv.value.toInt().toChar()
                }
            }
        }
        val diffmap = diffmap
        if (diffmap != null) {
            // the difference array overrides the existing encoding
            e = diffmap.toOrderedKeys()
            for (k in e.indices) {
                val n = diffmap.get(e[k])
                if (n < 256)
                    cidbyte2uni[n] = e[k].toChar()
            }
        }
    }


    /**
     * For all widths of all glyphs, compute the average width in normalized 1000 point units.
     * This is used to give some meaningful width in cases where we need an average font width
     * (such as if the width of a space isn't specified by a given font)
     * @return the average width of all non-zero width glyphs in the font
     */
    private fun computeAverageWidth(): Int {
        var count = 0
        var total = 0
        for (i in super.widths.indices) {
            if (super.widths[i] != 0) {
                total += super.widths[i]
                count++
            }
        }
        return if (count != 0) total / count else 0
    }

    /**
     * @since 2.1.5
     * * Override to allow special handling for fonts that don't specify width of space character
     * *
     * @see com.itextpdf.text.pdf.DocumentFont.getWidth
     */
    override fun getWidth(char1: Int): Int {
        if (char1 == ' ')
            return if (spaceWidth != 0) spaceWidth else defaultWidth
        return super.getWidth(char1)
    }

    /**
     * Decodes a single CID (represented by one or two bytes) to a unicode String.
     * @param bytes        the bytes making up the character code to convert
     * *
     * @param offset    an offset
     * *
     * @param len        a length
     * *
     * @return    a String containing the encoded form of the input bytes using the font's encoding.
     */
    private fun decodeSingleCID(bytes: ByteArray, offset: Int, len: Int): String? {
        if (toUnicodeCmap != null) {
            if (offset + len > bytes.size)
                throw ArrayIndexOutOfBoundsException(MessageLocalization.getComposedMessage("invalid.index.1", offset + len))
            val s = toUnicodeCmap!!.lookup(bytes, offset, len)
            if (s != null)
                return s
            if (len != 1 || cidbyte2uni == null)
                return null
        }

        if (len == 1) {
            if (cidbyte2uni == null)
                return ""
            else
                return String(cidbyte2uni, 0xff and bytes[offset], 1)
        }

        throw Error("Multi-byte glyphs not implemented yet")
    }

    /**
     * Decodes a string of bytes (encoded in the font's encoding) into a unicode string
     * This will use the ToUnicode map of the font, if available, otherwise it uses
     * the font's encoding
     * @param cidbytes    the bytes that need to be decoded
     * *
     * @return  the unicode String that results from decoding
     * *
     * @since 2.1.7
     */
    fun decode(cidbytes: ByteArray, offset: Int, len: Int): String {
        val sb = StringBuilder()
        if (toUnicodeCmap == null && byteCid != null) {
            val seq = CMapSequence(cidbytes, offset, len)
            val cid = byteCid!!.decodeSequence(seq)
            for (k in 0..cid.length - 1) {
                val c = cidUni!!.lookup(cid[k].toInt())
                if (c > 0)
                    sb.append(Utilities.convertFromUtf32(c))
            }
        } else {
            var i = offset
            while (i < offset + len) {
                var rslt = decodeSingleCID(cidbytes, i, 1)
                if (rslt == null && i < offset + len - 1) {
                    rslt = decodeSingleCID(cidbytes, i, 2)
                    i++
                }
                if (rslt != null)
                    sb.append(rslt)
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Encodes bytes to a String.
     * @param bytes        the bytes from a stream
     * *
     * @param offset    an offset
     * *
     * @param len        a length
     * *
     * @return    a String encoded taking into account if the bytes are in unicode or not.
     * *
     */
    @Deprecated("method name is not indicative of what it does.  Use <code>decode</code> instead.")
    fun encode(bytes: ByteArray, offset: Int, len: Int): String {
        return decode(bytes, offset, len)
    }
}
