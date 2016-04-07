/*
 * $Id: cf66328380e91d390daf1a48513a7131a3ea57da $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Kevin Day, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.HashMap

import javax.imageio.ImageIO

import com.itextpdf.text.Version
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.UnsupportedPdfException
import com.itextpdf.text.pdf.FilterHandlers
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.codec.PngWriter
import com.itextpdf.text.pdf.codec.TIFFConstants
import com.itextpdf.text.pdf.codec.TiffWriter

/**
 * An object that contains an image dictionary and image bytes.
 * @since 5.0.2
 */
class PdfImageObject
/**
 * Creats a PdfImage object using an explicitly provided dictionary and image bytes
 * @param dictionary the dictionary for the image
 * *
 * @param samples the samples
 * *
 * @param colorSpaceDic    a color space dictionary
 * *
 * @since 5.0.3
 */
@Throws(IOException::class)
protected constructor(
        /** The image dictionary.  */
        /**
         * Returns the image dictionary.
         * @return the dictionary
         */
        val dictionary: PdfDictionary, samples: ByteArray, private val colorSpaceDic: PdfDictionary?) {

    /**
     * Different types of data that can be stored in the bytes of a [PdfImageObject]
     * @since 5.0.4
     */
    enum class ImageBytesType
    /**
     * @param fileExtension the recommended file extension for use with data of this type (for example, if the bytes were just saved to a file, what extension should the file have)
     */
    private constructor(
            /**
             * the recommended file extension for streams of this type
             */
            /**
             * @return the file extension registered when this type was created
             */
            val fileExtension: String) {
        PNG("png"), // the stream contains png encoded data
        JPG("jpg"), // the stream contains jpg encoded data
        JP2("jp2"), // the stream contains jp2 encoded data
        CCITT("tif"), // the stream contains ccitt encoded data
        JBIG2("jbig2") // the stream contains JBIG2 encoded data
    }

    /**
     * A filter that does nothing, but keeps track of the filter type that was used
     * @since 5.0.4
     */
    private class TrackingFilter : FilterHandlers.FilterHandler {
        var lastFilterName: PdfName? = null

        @Throws(IOException::class)
        override fun decode(b: ByteArray, filterName: PdfName, decodeParams: PdfObject, streamDictionary: PdfDictionary): ByteArray {
            lastFilterName = filterName
            return b
        }

    }
    /** The decoded image bytes (after applying filters), or the raw image bytes if unable to decode  */
    /**
     * @return the bytes of the image (the format will be as specified in [PdfImageObject.getImageBytesType]
     * *
     * @throws IOException
     * *
     * @since 5.0.4
     */
    var imageAsBytes: ByteArray? = null
        private set

    private var pngColorType = -1
    private var pngBitDepth: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var bpc: Int = 0
    private var palette: ByteArray? = null
    private var icc: ByteArray? = null
    private var stride: Int = 0

    /**
     * Tracks the type of data that is actually stored in the streamBytes member
     */
    /**
     * @return the type of image data that is returned by getImageBytes()
     */
    var imageBytesType: ImageBytesType? = null
        private set

    val fileType: String
        get() = imageBytesType!!.fileExtension

    /**
     * Creates a PdfImage object.
     * @param stream a PRStream
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(stream: PRStream) : this(stream, PdfReader.getStreamBytesRaw(stream), null) {
    }

    /**
     * Creates a PdfImage object.
     * @param stream a PRStream
     * *
     * @param colorSpaceDic    a color space dictionary
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(stream: PRStream, colorSpaceDic: PdfDictionary) : this(stream, PdfReader.getStreamBytesRaw(stream), colorSpaceDic) {
    }


    init {
        val trackingFilter = TrackingFilter()
        val handlers = HashMap(FilterHandlers.getDefaultFilterHandlers())
        handlers.put(PdfName.JBIG2DECODE, trackingFilter)
        handlers.put(PdfName.DCTDECODE, trackingFilter)
        handlers.put(PdfName.JPXDECODE, trackingFilter)

        imageAsBytes = PdfReader.decodeBytes(samples, dictionary, handlers)

        if (trackingFilter.lastFilterName != null) {
            if (PdfName.JBIG2DECODE == trackingFilter.lastFilterName)
                imageBytesType = ImageBytesType.JBIG2
            else if (PdfName.DCTDECODE == trackingFilter.lastFilterName)
                imageBytesType = ImageBytesType.JPG
            else if (PdfName.JPXDECODE == trackingFilter.lastFilterName)
                imageBytesType = ImageBytesType.JP2
        } else {
            decodeImageBytes()
        }
    }

    /**
     * Returns an entry from the image dictionary.
     * @param key a key
     * *
     * @return the value
     */
    operator fun get(key: PdfName): PdfObject {
        return dictionary.get(key)
    }

    /**
     * Sets state of this object according to the color space
     * @param colorspace the colorspace to use
     * *
     * @param allowIndexed whether indexed color spaces will be resolved (used for recursive call)
     * *
     * @throws IOException if there is a problem with reading from the underlying stream
     */
    @Throws(IOException::class)
    private fun findColorspace(colorspace: PdfObject?, allowIndexed: Boolean) {
        if (colorspace == null && bpc == 1) {
            // handle imagemasks
            stride = (width * bpc + 7) / 8
            pngColorType = 0
        } else if (PdfName.DEVICEGRAY == colorspace) {
            stride = (width * bpc + 7) / 8
            pngColorType = 0
        } else if (PdfName.DEVICERGB == colorspace) {
            if (bpc == 8 || bpc == 16) {
                stride = (width * bpc * 3 + 7) / 8
                pngColorType = 2
            }
        } else if (colorspace is PdfArray) {
            val tyca = colorspace.getDirectObject(0)
            if (PdfName.CALGRAY == tyca) {
                stride = (width * bpc + 7) / 8
                pngColorType = 0
            } else if (PdfName.CALRGB == tyca) {
                if (bpc == 8 || bpc == 16) {
                    stride = (width * bpc * 3 + 7) / 8
                    pngColorType = 2
                }
            } else if (PdfName.ICCBASED == tyca) {
                val pr = colorspace.getDirectObject(1) as PRStream
                val n = pr.getAsNumber(PdfName.N).intValue()
                if (n == 1) {
                    stride = (width * bpc + 7) / 8
                    pngColorType = 0
                    icc = PdfReader.getStreamBytes(pr)
                } else if (n == 3) {
                    stride = (width * bpc * 3 + 7) / 8
                    pngColorType = 2
                    icc = PdfReader.getStreamBytes(pr)
                }
            } else if (allowIndexed && PdfName.INDEXED == tyca) {
                findColorspace(colorspace.getDirectObject(1), false)
                if (pngColorType == 2) {
                    val id2 = colorspace.getDirectObject(3)
                    if (id2 is PdfString) {
                        palette = id2.bytes
                    } else if (id2 is PRStream) {
                        palette = PdfReader.getStreamBytes(id2)
                    }
                    stride = (width * bpc + 7) / 8
                    pngColorType = 3
                }
            }
        }
    }

    /**
     * decodes the bytes currently captured in the streamBytes and replaces it with an image representation of the bytes
     * (this will either be a png or a tiff, depending on the color depth of the image)
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun decodeImageBytes() {
        if (imageBytesType != null)
            throw IllegalStateException(MessageLocalization.getComposedMessage("Decoding.can't.happen.on.this.type.of.stream.(.1.)", imageBytesType))

        pngColorType = -1
        val decode = dictionary.getAsArray(PdfName.DECODE)
        width = dictionary.getAsNumber(PdfName.WIDTH).intValue()
        height = dictionary.getAsNumber(PdfName.HEIGHT).intValue()
        bpc = dictionary.getAsNumber(PdfName.BITSPERCOMPONENT).intValue()
        pngBitDepth = bpc
        var colorspace = dictionary.getDirectObject(PdfName.COLORSPACE)
        if (colorspace is PdfName && colorSpaceDic != null) {
            val csLookup = colorSpaceDic.getDirectObject(colorspace)
            if (csLookup != null)
                colorspace = csLookup
        }

        palette = null
        icc = null
        stride = 0
        findColorspace(colorspace, true)
        val ms = ByteArrayOutputStream()
        if (pngColorType < 0) {
            if (bpc != 8)
                throw UnsupportedPdfException(MessageLocalization.getComposedMessage("the.color.depth.1.is.not.supported", bpc))

            if (PdfName.DEVICECMYK == colorspace) {
            } else if (colorspace is PdfArray) {
                val tyca = colorspace.getDirectObject(0)
                if (PdfName.ICCBASED != tyca)
                    throw UnsupportedPdfException(MessageLocalization.getComposedMessage("the.color.space.1.is.not.supported", colorspace))
                val pr = colorspace.getDirectObject(1) as PRStream
                val n = pr.getAsNumber(PdfName.N).intValue()
                if (n != 4) {
                    throw UnsupportedPdfException(MessageLocalization.getComposedMessage("N.value.1.is.not.supported", n))
                }
                icc = PdfReader.getStreamBytes(pr)
            } else
                throw UnsupportedPdfException(MessageLocalization.getComposedMessage("the.color.space.1.is.not.supported", colorspace))
            stride = 4 * width
            val wr = TiffWriter()
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_SAMPLESPERPIXEL, 4))
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_BITSPERSAMPLE, intArrayOf(8, 8, 8, 8)))
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_PHOTOMETRIC, TIFFConstants.PHOTOMETRIC_SEPARATED))
            wr.addField(TiffWriter.FieldLong(TIFFConstants.TIFFTAG_IMAGEWIDTH, width))
            wr.addField(TiffWriter.FieldLong(TIFFConstants.TIFFTAG_IMAGELENGTH, height))
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_COMPRESSION, TIFFConstants.COMPRESSION_LZW))
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_PREDICTOR, TIFFConstants.PREDICTOR_HORIZONTAL_DIFFERENCING))
            wr.addField(TiffWriter.FieldLong(TIFFConstants.TIFFTAG_ROWSPERSTRIP, height))
            wr.addField(TiffWriter.FieldRational(TIFFConstants.TIFFTAG_XRESOLUTION, intArrayOf(300, 1)))
            wr.addField(TiffWriter.FieldRational(TIFFConstants.TIFFTAG_YRESOLUTION, intArrayOf(300, 1)))
            wr.addField(TiffWriter.FieldShort(TIFFConstants.TIFFTAG_RESOLUTIONUNIT, TIFFConstants.RESUNIT_INCH))
            wr.addField(TiffWriter.FieldAscii(TIFFConstants.TIFFTAG_SOFTWARE, Version.getInstance().version))
            val comp = ByteArrayOutputStream()
            TiffWriter.compressLZW(comp, 2, imageAsBytes, height, 4, stride)
            val buf = comp.toByteArray()
            wr.addField(TiffWriter.FieldImage(buf))
            wr.addField(TiffWriter.FieldLong(TIFFConstants.TIFFTAG_STRIPBYTECOUNTS, buf.size))
            if (icc != null)
                wr.addField(TiffWriter.FieldUndefined(TIFFConstants.TIFFTAG_ICCPROFILE, icc))
            wr.writeFile(ms)
            imageBytesType = ImageBytesType.CCITT
            imageAsBytes = ms.toByteArray()
            return
        } else {
            val png = PngWriter(ms)
            if (decode != null) {
                if (pngBitDepth == 1) {
                    // if the decode array is 1,0, then we need to invert the image
                    if (decode.getAsNumber(0).intValue() == 1 && decode.getAsNumber(1).intValue() == 0) {
                        val len = imageAsBytes!!.size
                        for (t in 0..len - 1) {
                            imageAsBytes[t] = imageAsBytes[t] xor 0xff
                        }
                    } else {
                        // if the decode array is 0,1, do nothing.  It's possible that the array could be 0,0 or 1,1 - but that would be silly, so we'll just ignore that case
                    }
                } else {
                    // todo: add decode transformation for other depths
                }
            }
            png.writeHeader(width, height, pngBitDepth, pngColorType)
            if (icc != null)
                png.writeIccProfile(icc)
            if (palette != null)
                png.writePalette(palette)
            png.writeData(imageAsBytes, stride)
            png.writeEnd()
            imageBytesType = ImageBytesType.PNG
            imageAsBytes = ms.toByteArray()
        }
    }

    // AWT related methods (remove this if you port to Android / GAE)

    /**
     * @since 5.0.3 renamed from getAwtImage()
     */
    val bufferedImage: java.awt.image.BufferedImage?
        @Throws(IOException::class)
        get() {
            val img = imageAsBytes ?: return null
            return ImageIO.read(ByteArrayInputStream(img))
        }
}
