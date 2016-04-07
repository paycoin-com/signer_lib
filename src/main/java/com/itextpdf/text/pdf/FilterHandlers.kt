/*
 * $Id: 86657a2d8f3332f955bf556dec5f336c407f76a7 $
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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Collections
import java.util.HashMap

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.UnsupportedPdfException
import com.itextpdf.text.pdf.codec.TIFFConstants
import com.itextpdf.text.pdf.codec.TIFFFaxDecoder
import com.itextpdf.text.pdf.codec.TIFFFaxDecompressor

/**
 * Encapsulates filter behavior for PDF streams.  Classes generally interace with this
 * using the static getDefaultFilterHandlers() method, then obtain the desired [FilterHandler]
 * via a lookup.
 * @since 5.0.4
 */
// Dev note:  we eventually want to refactor PdfReader so all of the existing filter functionality is moved into this class
// it may also be better to split the sub-classes out into a separate package 
object FilterHandlers {

    /**
     * The main interface for creating a new [FilterHandler]
     */
    interface FilterHandler {
        @Throws(IOException::class)
        fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray
    }

    /** The default [FilterHandler]s used by iText  */
    /**
     * @return the default [FilterHandler]s used by iText
     */
    val defaultFilterHandlers: Map<PdfName, FilterHandler>

    init {
        val map = HashMap<PdfName, FilterHandler>()

        map.put(PdfName.FLATEDECODE, Filter_FLATEDECODE())
        map.put(PdfName.FL, Filter_FLATEDECODE())
        map.put(PdfName.ASCIIHEXDECODE, Filter_ASCIIHEXDECODE())
        map.put(PdfName.AHX, Filter_ASCIIHEXDECODE())
        map.put(PdfName.ASCII85DECODE, Filter_ASCII85DECODE())
        map.put(PdfName.A85, Filter_ASCII85DECODE())
        map.put(PdfName.LZWDECODE, Filter_LZWDECODE())
        map.put(PdfName.CCITTFAXDECODE, Filter_CCITTFAXDECODE())
        map.put(PdfName.CRYPT, Filter_DoNothing())
        map.put(PdfName.RUNLENGTHDECODE, Filter_RUNLENGTHDECODE())

        defaultFilterHandlers = Collections.unmodifiableMap(map)
    }

    /**
     * Handles FLATEDECODE filter
     */
    private class Filter_FLATEDECODE : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            var b = b
            b = PdfReader.FlateDecode(b)
            b = PdfReader.decodePredictor(b, decodeParams)
            return b
        }
    }

    /**
     * Handles ASCIIHEXDECODE filter
     */
    private class Filter_ASCIIHEXDECODE : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            var b = b
            b = PdfReader.ASCIIHexDecode(b)
            return b
        }
    }

    /**
     * Handles ASCIIHEXDECODE filter
     */
    private class Filter_ASCII85DECODE : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            var b = b
            b = PdfReader.ASCII85Decode(b)
            return b
        }
    }

    /**
     * Handles LZWDECODE filter
     */
    private class Filter_LZWDECODE : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            var b = b
            b = PdfReader.LZWDecode(b)
            b = PdfReader.decodePredictor(b, decodeParams)
            return b
        }
    }


    /**
     * Handles CCITTFAXDECODE filter
     */
    private class Filter_CCITTFAXDECODE : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            var b = b
            val wn = PdfReader.getPdfObjectRelease(streamDictionary.get(PdfName.WIDTH)) as PdfNumber?
            val hn = PdfReader.getPdfObjectRelease(streamDictionary.get(PdfName.HEIGHT)) as PdfNumber?
            if (wn == null || hn == null)
                throw UnsupportedPdfException(MessageLocalization.getComposedMessage("filter.ccittfaxdecode.is.only.supported.for.images"))
            val width = wn.intValue()
            val height = hn.intValue()

            val param = if (decodeParams is PdfDictionary) decodeParams else null
            var k = 0
            var blackIs1 = false
            var byteAlign = false
            if (param != null) {
                val kn = param.getAsNumber(PdfName.K)
                if (kn != null)
                    k = kn.intValue()
                var bo: PdfBoolean? = param.getAsBoolean(PdfName.BLACKIS1)
                if (bo != null)
                    blackIs1 = bo.booleanValue()
                bo = param.getAsBoolean(PdfName.ENCODEDBYTEALIGN)
                if (bo != null)
                    byteAlign = bo.booleanValue()
            }
            var outBuf = ByteArray((width + 7) / 8 * height)
            val decoder = TIFFFaxDecompressor()
            if (k == 0 || k > 0) {
                var tiffT4Options = if (k > 0) TIFFConstants.GROUP3OPT_2DENCODING else 0
                tiffT4Options = tiffT4Options or if (byteAlign) TIFFConstants.GROUP3OPT_FILLBITS else 0
                decoder.SetOptions(1, TIFFConstants.COMPRESSION_CCITTFAX3, tiffT4Options, 0)
                decoder.decodeRaw(outBuf, b, width, height)
                if (decoder.fails > 0) {
                    val outBuf2 = ByteArray((width + 7) / 8 * height)
                    val oldFails = decoder.fails
                    decoder.SetOptions(1, TIFFConstants.COMPRESSION_CCITTRLE, tiffT4Options, 0)
                    decoder.decodeRaw(outBuf2, b, width, height)
                    if (decoder.fails < oldFails) {
                        outBuf = outBuf2
                    }
                }
            } else {
                val deca = TIFFFaxDecoder(1, width, height)
                deca.decodeT6(outBuf, b, 0, height, 0)
            }
            if (!blackIs1) {
                val len = outBuf.size
                for (t in 0..len - 1) {
                    outBuf[t] = outBuf[t] xor 0xff
                }
            }
            b = outBuf
            return b
        }
    }

    /**
     * A filter that doesn't modify the stream at all
     */
    private class Filter_DoNothing : FilterHandler {
        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            return b
        }
    }

    /**
     * Handles RUNLENGTHDECODE filter
     */
    private class Filter_RUNLENGTHDECODE : FilterHandler {

        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            // allocate the output buffer
            val baos = ByteArrayOutputStream()
            var dupCount: Byte = -1
            var i = 0
            while (i < b.size) {
                dupCount = b[i]
                if (dupCount.toInt() == -128) break // this is implicit end of data

                if (dupCount >= 0 && dupCount <= 127) {
                    val bytesToCopy = dupCount + 1
                    baos.write(b, i, bytesToCopy)
                    i += bytesToCopy
                } else {
                    // make dupcount copies of the next byte
                    i++
                    for (j in 0..1 - dupCount.toInt() - 1) {
                        baos.write(b[i].toInt())
                    }
                }
                i++
            }

            return baos.toByteArray()
        }
    }

}
