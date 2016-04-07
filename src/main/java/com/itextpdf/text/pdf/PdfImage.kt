/*
 * $Id: e5700894697afe1644ddf1f1a7e1e12e23aee8bd $
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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.Image

/**
 * PdfImage is a PdfStream containing an image-Dictionary and -stream.
 */

class PdfImage// constructor

/**
 * Constructs a PdfImage-object.

 * @param image the Image-object
 * *
 * @param name the PdfName for this image
 * *
 * @throws BadPdfFormatException on error
 */
@Throws(BadPdfFormatException::class)
constructor(image: Image, name: String?, maskRef: PdfIndirectReference?) : PdfStream() {
    // membervariables

    /** This is the PdfName of the image.  */
    protected var name: PdfName? = null

    var image: Image? = null
        protected set

    init {
        this.image = image
        if (name == null)
            generateImgResName(image)
        else
            this.name = PdfName(name)
        put(PdfName.TYPE, PdfName.XOBJECT)
        put(PdfName.SUBTYPE, PdfName.IMAGE)
        put(PdfName.WIDTH, PdfNumber(image.width))
        put(PdfName.HEIGHT, PdfNumber(image.height))
        if (image.layer != null)
            put(PdfName.OC, image.layer.ref)
        if (image.isMask && (image.bpc == 1 || image.bpc > 0xff))
            put(PdfName.IMAGEMASK, PdfBoolean.PDFTRUE)
        if (maskRef != null) {
            if (image.isSmask)
                put(PdfName.SMASK, maskRef)
            else
                put(PdfName.MASK, maskRef)
        }
        if (image.isMask && image.isInverted)
            put(PdfName.DECODE, PdfLiteral("[1 0]"))
        if (image.isInterpolation)
            put(PdfName.INTERPOLATE, PdfBoolean.PDFTRUE)
        var `is`: InputStream? = null
        try {
            // deal with transparency
            val transparency = image.transparency
            if (transparency != null && !image.isMask && maskRef == null) {
                val s = StringBuilder("[")
                for (k in transparency.indices)
                    s.append(transparency[k]).append(" ")
                s.append("]")
                put(PdfName.MASK, PdfLiteral(s.toString()))
            }
            // Raw Image data
            if (image.isImgRaw) {
                // will also have the CCITT parameters
                val colorspace = image.colorspace
                bytes = image.rawData
                put(PdfName.LENGTH, PdfNumber(bytes!!.size))
                val bpc = image.bpc
                if (bpc > 0xff) {
                    if (!image.isMask)
                        put(PdfName.COLORSPACE, PdfName.DEVICEGRAY)
                    put(PdfName.BITSPERCOMPONENT, PdfNumber(1))
                    put(PdfName.FILTER, PdfName.CCITTFAXDECODE)
                    val k = bpc - Image.CCITTG3_1D
                    val decodeparms = PdfDictionary()
                    if (k != 0)
                        decodeparms.put(PdfName.K, PdfNumber(k))
                    if (colorspace and Image.CCITT_BLACKIS1 != 0)
                        decodeparms.put(PdfName.BLACKIS1, PdfBoolean.PDFTRUE)
                    if (colorspace and Image.CCITT_ENCODEDBYTEALIGN != 0)
                        decodeparms.put(PdfName.ENCODEDBYTEALIGN, PdfBoolean.PDFTRUE)
                    if (colorspace and Image.CCITT_ENDOFLINE != 0)
                        decodeparms.put(PdfName.ENDOFLINE, PdfBoolean.PDFTRUE)
                    if (colorspace and Image.CCITT_ENDOFBLOCK != 0)
                        decodeparms.put(PdfName.ENDOFBLOCK, PdfBoolean.PDFFALSE)
                    decodeparms.put(PdfName.COLUMNS, PdfNumber(image.width))
                    decodeparms.put(PdfName.ROWS, PdfNumber(image.height))
                    put(PdfName.DECODEPARMS, decodeparms)
                } else {
                    when (colorspace) {
                        1 -> {
                            put(PdfName.COLORSPACE, PdfName.DEVICEGRAY)
                            if (image.isInverted)
                                put(PdfName.DECODE, PdfLiteral("[1 0]"))
                        }
                        3 -> {
                            put(PdfName.COLORSPACE, PdfName.DEVICERGB)
                            if (image.isInverted)
                                put(PdfName.DECODE, PdfLiteral("[1 0 1 0 1 0]"))
                        }
                        4,
                        else -> {
                            put(PdfName.COLORSPACE, PdfName.DEVICECMYK)
                            if (image.isInverted)
                                put(PdfName.DECODE, PdfLiteral("[1 0 1 0 1 0 1 0]"))
                        }
                    }
                    val additional = image.additional
                    if (additional != null)
                        putAll(additional)
                    if (image.isMask && (image.bpc == 1 || image.bpc > 8))
                        remove(PdfName.COLORSPACE)
                    put(PdfName.BITSPERCOMPONENT, PdfNumber(image.bpc))
                    if (image.isDeflated)
                        put(PdfName.FILTER, PdfName.FLATEDECODE)
                    else {
                        flateCompress(image.compressionLevel)
                    }
                }
                return
            }
            // GIF, JPEG or PNG
            val errorID: String
            if (image.rawData == null) {
                `is` = image.url.openStream()
                errorID = image.url.toString()
            } else {
                `is` = java.io.ByteArrayInputStream(image.rawData)
                errorID = "Byte array"
            }
            when (image.type()) {
                Image.JPEG -> {
                    put(PdfName.FILTER, PdfName.DCTDECODE)
                    if (image.colorTransform == 0) {
                        val decodeparms = PdfDictionary()
                        decodeparms.put(PdfName.COLORTRANSFORM, PdfNumber(0))
                        put(PdfName.DECODEPARMS, decodeparms)
                    }
                    when (image.colorspace) {
                        1 -> put(PdfName.COLORSPACE, PdfName.DEVICEGRAY)
                        3 -> put(PdfName.COLORSPACE, PdfName.DEVICERGB)
                        else -> {
                            put(PdfName.COLORSPACE, PdfName.DEVICECMYK)
                            if (image.isInverted) {
                                put(PdfName.DECODE, PdfLiteral("[1 0 1 0 1 0 1 0]"))
                            }
                        }
                    }
                    put(PdfName.BITSPERCOMPONENT, PdfNumber(8))
                    if (image.rawData != null) {
                        bytes = image.rawData
                        put(PdfName.LENGTH, PdfNumber(bytes!!.size))
                        return
                    }
                    streamBytes = ByteArrayOutputStream()
                    transferBytes(`is`, streamBytes, -1)
                }
                Image.JPEG2000 -> {
                    put(PdfName.FILTER, PdfName.JPXDECODE)
                    if (image.colorspace > 0) {
                        when (image.colorspace) {
                            1 -> put(PdfName.COLORSPACE, PdfName.DEVICEGRAY)
                            3 -> put(PdfName.COLORSPACE, PdfName.DEVICERGB)
                            else -> put(PdfName.COLORSPACE, PdfName.DEVICECMYK)
                        }
                        put(PdfName.BITSPERCOMPONENT, PdfNumber(image.bpc))
                    }
                    if (image.rawData != null) {
                        bytes = image.rawData
                        put(PdfName.LENGTH, PdfNumber(bytes!!.size))
                        return
                    }
                    streamBytes = ByteArrayOutputStream()
                    transferBytes(`is`, streamBytes, -1)
                }
                Image.JBIG2 -> {
                    put(PdfName.FILTER, PdfName.JBIG2DECODE)
                    put(PdfName.COLORSPACE, PdfName.DEVICEGRAY)
                    put(PdfName.BITSPERCOMPONENT, PdfNumber(1))
                    if (image.rawData != null) {
                        bytes = image.rawData
                        put(PdfName.LENGTH, PdfNumber(bytes!!.size))
                        return
                    }
                    streamBytes = ByteArrayOutputStream()
                    transferBytes(`is`, streamBytes, -1)
                }
                else -> throw BadPdfFormatException(MessageLocalization.getComposedMessage("1.is.an.unknown.image.format", errorID))
            }
            if (image.compressionLevel > PdfStream.NO_COMPRESSION)
                flateCompress(image.compressionLevel)
            put(PdfName.LENGTH, PdfNumber(streamBytes!!.size()))
        } catch (ioe: IOException) {
            throw BadPdfFormatException(ioe.message)
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (ee: Exception) {
                    // empty on purpose
                }

            }
        }
    }

    /**
     * Returns the PdfName of the image.

     * @return        the name
     */

    fun name(): PdfName {
        return name
    }

    protected fun importAll(dup: PdfImage) {
        name = dup.name
        compressed = dup.compressed
        compressionLevel = dup.compressionLevel
        streamBytes = dup.streamBytes
        bytes = dup.bytes
        hashMap = dup.hashMap
    }

    /**
     * Called when no resource name is provided in our constructor.  This generates a
     * name that is required to be unique within a given resource dictionary.
     * @since 5.0.1
     */
    private fun generateImgResName(img: Image) {
        name = PdfName("img" + java.lang.Long.toHexString(img.mySerialId!!))
    }

    companion object {

        internal val TRANSFERSIZE = 4096

        @Throws(IOException::class)
        internal fun transferBytes(`in`: InputStream, out: OutputStream, len: Int) {
            var len = len
            val buffer = ByteArray(TRANSFERSIZE)
            if (len < 0)
                len = 0x7fff0000
            var size: Int
            while (len != 0) {
                size = `in`.read(buffer, 0, Math.min(len, TRANSFERSIZE))
                if (size < 0)
                    return
                out.write(buffer, 0, size)
                len -= size
            }
        }
    }
}
