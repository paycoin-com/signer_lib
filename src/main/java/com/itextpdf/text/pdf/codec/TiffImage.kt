/*
 * $Id: a32d3dab97facbb0db079a17b140ededad01f63c $
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
package com.itextpdf.text.pdf.codec

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.ImgRaw
import com.itextpdf.text.Jpeg
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.InvalidImageException
import com.itextpdf.text.pdf.ICC_Profile
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.RandomAccessFileOrArray

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater

/** Reads TIFF images
 * @author Paulo Soares
 */
object TiffImage {

    /** Gets the number of pages the TIFF document has.
     * @param s the file source
     * *
     * @return the number of pages
     */
    fun getNumberOfPages(s: RandomAccessFileOrArray): Int {
        try {
            return TIFFDirectory.getNumDirectories(s)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    internal fun getDpi(fd: TIFFField?, resolutionUnit: Int): Int {
        if (fd == null)
            return 0
        val res = fd.getAsRational(0)
        val frac = res[0].toFloat() / res[1].toFloat()
        var dpi = 0
        when (resolutionUnit) {
            TIFFConstants.RESUNIT_INCH, TIFFConstants.RESUNIT_NONE -> dpi = (frac + 0.5).toInt()
            TIFFConstants.RESUNIT_CENTIMETER -> dpi = (frac * 2.54 + 0.5).toInt()
        }
        return dpi
    }

    @JvmOverloads fun getTiffImage(s: RandomAccessFileOrArray, recoverFromImageError: Boolean, page: Int, direct: Boolean = false): Image {
        if (page < 1)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.page.number.must.be.gt.eq.1"))
        try {
            val dir = TIFFDirectory(s, page - 1)
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_TILEWIDTH))
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("tiles.are.not.supported"))
            val compression = dir.getFieldAsLong(TIFFConstants.TIFFTAG_COMPRESSION).toInt()
            when (compression) {
                TIFFConstants.COMPRESSION_CCITTRLEW, TIFFConstants.COMPRESSION_CCITTRLE, TIFFConstants.COMPRESSION_CCITTFAX3, TIFFConstants.COMPRESSION_CCITTFAX4 -> {
                }
                else -> return getTiffImageColor(dir, s)
            }
            var rotation = 0f
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ORIENTATION)) {
                val rot = dir.getFieldAsLong(TIFFConstants.TIFFTAG_ORIENTATION).toInt()
                if (rot == TIFFConstants.ORIENTATION_BOTRIGHT || rot == TIFFConstants.ORIENTATION_BOTLEFT)
                    rotation = Math.PI.toFloat()
                else if (rot == TIFFConstants.ORIENTATION_LEFTTOP || rot == TIFFConstants.ORIENTATION_LEFTBOT)
                    rotation = (Math.PI / 2.0).toFloat()
                else if (rot == TIFFConstants.ORIENTATION_RIGHTTOP || rot == TIFFConstants.ORIENTATION_RIGHTBOT)
                    rotation = -(Math.PI / 2.0).toFloat()
            }

            var img: Image? = null
            var tiffT4Options: Long = 0
            var tiffT6Options: Long = 0
            var fillOrder: Long = 1
            val h = dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGELENGTH).toInt()
            val w = dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGEWIDTH).toInt()
            var dpiX = 0
            var dpiY = 0
            var XYRatio = 0f
            var resolutionUnit = TIFFConstants.RESUNIT_INCH
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_RESOLUTIONUNIT))
                resolutionUnit = dir.getFieldAsLong(TIFFConstants.TIFFTAG_RESOLUTIONUNIT).toInt()
            dpiX = getDpi(dir.getField(TIFFConstants.TIFFTAG_XRESOLUTION), resolutionUnit)
            dpiY = getDpi(dir.getField(TIFFConstants.TIFFTAG_YRESOLUTION), resolutionUnit)
            if (resolutionUnit == TIFFConstants.RESUNIT_NONE) {
                if (dpiY != 0)
                    XYRatio = dpiX.toFloat() / dpiY.toFloat()
                dpiX = 0
                dpiY = 0
            }
            var rowsStrip = h
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ROWSPERSTRIP))
                rowsStrip = dir.getFieldAsLong(TIFFConstants.TIFFTAG_ROWSPERSTRIP).toInt()
            if (rowsStrip <= 0 || rowsStrip > h)
                rowsStrip = h
            val offset = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPOFFSETS)
            var size = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPBYTECOUNTS)
            if ((size == null || size.size == 1 && (size[0] == 0 || size[0] + offset[0] > s.length())) && h == rowsStrip) {
                // some TIFF producers are really lousy, so...
                size = longArrayOf(s.length() - offset[0].toInt())
            }
            var reverse = false
            val fillOrderField = dir.getField(TIFFConstants.TIFFTAG_FILLORDER)
            if (fillOrderField != null)
                fillOrder = fillOrderField.getAsLong(0)
            reverse = fillOrder == TIFFConstants.FILLORDER_LSB2MSB.toLong()
            var params = 0
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_PHOTOMETRIC)) {
                val photo = dir.getFieldAsLong(TIFFConstants.TIFFTAG_PHOTOMETRIC)
                if (photo == TIFFConstants.PHOTOMETRIC_MINISBLACK.toLong())
                    params = params or Image.CCITT_BLACKIS1
            }
            var imagecomp = 0
            when (compression) {
                TIFFConstants.COMPRESSION_CCITTRLEW, TIFFConstants.COMPRESSION_CCITTRLE -> {
                    imagecomp = Image.CCITTG3_1D
                    params = params or (Image.CCITT_ENCODEDBYTEALIGN or Image.CCITT_ENDOFBLOCK)
                }
                TIFFConstants.COMPRESSION_CCITTFAX3 -> {
                    imagecomp = Image.CCITTG3_1D
                    params = params or (Image.CCITT_ENDOFLINE or Image.CCITT_ENDOFBLOCK)
                    val t4OptionsField = dir.getField(TIFFConstants.TIFFTAG_GROUP3OPTIONS)
                    if (t4OptionsField != null) {
                        tiffT4Options = t4OptionsField.getAsLong(0)
                        if (tiffT4Options and TIFFConstants.GROUP3OPT_2DENCODING != 0)
                            imagecomp = Image.CCITTG3_2D
                        if (tiffT4Options and TIFFConstants.GROUP3OPT_FILLBITS != 0)
                            params = params or Image.CCITT_ENCODEDBYTEALIGN
                    }
                }
                TIFFConstants.COMPRESSION_CCITTFAX4 -> {
                    imagecomp = Image.CCITTG4
                    val t6OptionsField = dir.getField(TIFFConstants.TIFFTAG_GROUP4OPTIONS)
                    if (t6OptionsField != null)
                        tiffT6Options = t6OptionsField.getAsLong(0)
                }
            }
            if (direct && rowsStrip == h) {
                //single strip, direct
                val im = ByteArray(size!![0].toInt())
                s.seek(offset[0])
                s.readFully(im)
                img = Image.getInstance(w, h, false, imagecomp, params, im)
                img!!.isInverted = true
            } else {
                var rowsLeft = h
                val g4 = CCITTG4Encoder(w)
                for (k in offset.indices) {
                    var im = ByteArray(size!![k].toInt())
                    s.seek(offset[k])
                    s.readFully(im)
                    val height = Math.min(rowsStrip, rowsLeft)
                    val decoder = TIFFFaxDecoder(fillOrder, w, height)
                    decoder.setRecoverFromImageError(recoverFromImageError)
                    val outBuf = ByteArray((w + 7) / 8 * height)
                    when (compression) {
                        TIFFConstants.COMPRESSION_CCITTRLEW, TIFFConstants.COMPRESSION_CCITTRLE -> {
                            decoder.decode1D(outBuf, im, 0, height)
                            g4.fax4Encode(outBuf, height)
                        }
                        TIFFConstants.COMPRESSION_CCITTFAX3 -> {
                            try {
                                decoder.decode2D(outBuf, im, 0, height, tiffT4Options)
                            } catch (e: RuntimeException) {
                                // let's flip the fill bits and try again...
                                tiffT4Options = tiffT4Options xor TIFFConstants.GROUP3OPT_FILLBITS.toLong()
                                try {
                                    decoder.decode2D(outBuf, im, 0, height, tiffT4Options)
                                } catch (e2: RuntimeException) {
                                    if (!recoverFromImageError)
                                        throw e
                                    if (rowsStrip == 1)
                                        throw e
                                    // repeat of reading the tiff directly (the if section of this if else structure)
                                    // copy pasted to avoid making a method with 10 parameters
                                    im = ByteArray(size[0].toInt())
                                    s.seek(offset[0])
                                    s.readFully(im)
                                    img = Image.getInstance(w, h, false, imagecomp, params, im)
                                    img!!.isInverted = true
                                    img.setDpi(dpiX, dpiY)
                                    img.xyRatio = XYRatio
                                    img.originalType = Image.ORIGINAL_TIFF
                                    if (rotation != 0f)
                                        img.initialRotation = rotation
                                    return img
                                }

                            }

                            g4.fax4Encode(outBuf, height)
                        }
                        TIFFConstants.COMPRESSION_CCITTFAX4 -> {
                            try {
                                decoder.decodeT6(outBuf, im, 0, height, tiffT6Options)
                            } catch (e: InvalidImageException) {
                                if (!recoverFromImageError) {
                                    throw e
                                }
                            }

                            g4.fax4Encode(outBuf, height)
                        }
                    }
                    rowsLeft -= rowsStrip
                }
                val g4pic = g4.close()
                img = Image.getInstance(w, h, false, Image.CCITTG4, params and Image.CCITT_BLACKIS1, g4pic)
            }
            img!!.setDpi(dpiX, dpiY)
            img.xyRatio = XYRatio
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ICCPROFILE)) {
                try {
                    val fd = dir.getField(TIFFConstants.TIFFTAG_ICCPROFILE)
                    val icc_prof = ICC_Profile.getInstance(fd.asBytes)
                    if (icc_prof.numComponents == 1)
                        img.tagICC(icc_prof)
                } catch (e: RuntimeException) {
                    //empty
                }

            }
            img.originalType = Image.ORIGINAL_TIFF
            if (rotation != 0f)
                img.initialRotation = rotation
            return img
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /** Reads a page from a TIFF image.
     * @param s the file source
     * *
     * @param page the page to get. The first page is 1
     * *
     * @param direct for single strip, CCITT images, generate the image
     * * by direct byte copying. It's faster but may not work
     * * every time
     * *
     * @return the Image
     */
    @JvmOverloads fun getTiffImage(s: RandomAccessFileOrArray, page: Int, direct: Boolean = false): Image {
        return getTiffImage(s, false, page, direct)
    }

    protected fun getTiffImageColor(dir: TIFFDirectory, s: RandomAccessFileOrArray): Image {
        try {
            val compression = dir.getFieldAsLong(TIFFConstants.TIFFTAG_COMPRESSION).toInt()
            var predictor = 1
            var lzwDecoder: TIFFLZWDecoder? = null
            when (compression) {
                TIFFConstants.COMPRESSION_NONE, TIFFConstants.COMPRESSION_LZW, TIFFConstants.COMPRESSION_PACKBITS, TIFFConstants.COMPRESSION_DEFLATE, TIFFConstants.COMPRESSION_ADOBE_DEFLATE, TIFFConstants.COMPRESSION_OJPEG, TIFFConstants.COMPRESSION_JPEG -> {
                }
                else -> throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.compression.1.is.not.supported", compression))
            }
            val photometric = dir.getFieldAsLong(TIFFConstants.TIFFTAG_PHOTOMETRIC).toInt()
            when (photometric) {
                TIFFConstants.PHOTOMETRIC_MINISWHITE, TIFFConstants.PHOTOMETRIC_MINISBLACK, TIFFConstants.PHOTOMETRIC_RGB, TIFFConstants.PHOTOMETRIC_SEPARATED, TIFFConstants.PHOTOMETRIC_PALETTE -> {
                }
                else -> if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.photometric.1.is.not.supported", photometric))
            }
            var rotation = 0f
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ORIENTATION)) {
                val rot = dir.getFieldAsLong(TIFFConstants.TIFFTAG_ORIENTATION).toInt()
                if (rot == TIFFConstants.ORIENTATION_BOTRIGHT || rot == TIFFConstants.ORIENTATION_BOTLEFT)
                    rotation = Math.PI.toFloat()
                else if (rot == TIFFConstants.ORIENTATION_LEFTTOP || rot == TIFFConstants.ORIENTATION_LEFTBOT)
                    rotation = (Math.PI / 2.0).toFloat()
                else if (rot == TIFFConstants.ORIENTATION_RIGHTTOP || rot == TIFFConstants.ORIENTATION_RIGHTBOT)
                    rotation = -(Math.PI / 2.0).toFloat()
            }
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_PLANARCONFIG) && dir.getFieldAsLong(TIFFConstants.TIFFTAG_PLANARCONFIG) == TIFFConstants.PLANARCONFIG_SEPARATE.toLong())
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("planar.images.are.not.supported"))
            var extraSamples = 0
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_EXTRASAMPLES))
                extraSamples = 1
            var samplePerPixel = 1
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_SAMPLESPERPIXEL))
            // 1,3,4
                samplePerPixel = dir.getFieldAsLong(TIFFConstants.TIFFTAG_SAMPLESPERPIXEL).toInt()
            var bitsPerSample = 1
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_BITSPERSAMPLE))
                bitsPerSample = dir.getFieldAsLong(TIFFConstants.TIFFTAG_BITSPERSAMPLE).toInt()
            when (bitsPerSample) {
                1, 2, 4, 8 -> {
                }
                else -> throw IllegalArgumentException(MessageLocalization.getComposedMessage("bits.per.sample.1.is.not.supported", bitsPerSample))
            }
            var img: Image? = null

            val h = dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGELENGTH).toInt()
            val w = dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGEWIDTH).toInt()
            var dpiX = 0
            var dpiY = 0
            var resolutionUnit = TIFFConstants.RESUNIT_INCH
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_RESOLUTIONUNIT))
                resolutionUnit = dir.getFieldAsLong(TIFFConstants.TIFFTAG_RESOLUTIONUNIT).toInt()
            dpiX = getDpi(dir.getField(TIFFConstants.TIFFTAG_XRESOLUTION), resolutionUnit)
            dpiY = getDpi(dir.getField(TIFFConstants.TIFFTAG_YRESOLUTION), resolutionUnit)
            var fillOrder = 1
            var reverse = false
            val fillOrderField = dir.getField(TIFFConstants.TIFFTAG_FILLORDER)
            if (fillOrderField != null)
                fillOrder = fillOrderField.getAsInt(0)
            reverse = fillOrder == TIFFConstants.FILLORDER_LSB2MSB
            var rowsStrip = h
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ROWSPERSTRIP))
            //another hack for broken tiffs
                rowsStrip = dir.getFieldAsLong(TIFFConstants.TIFFTAG_ROWSPERSTRIP).toInt()
            if (rowsStrip <= 0 || rowsStrip > h)
                rowsStrip = h
            val offset = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPOFFSETS)
            var size = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPBYTECOUNTS)
            if ((size == null || size.size == 1 && (size[0] == 0 || size[0] + offset[0] > s.length())) && h == rowsStrip) {
                // some TIFF producers are really lousy, so...
                size = longArrayOf(s.length() - offset[0].toInt())
            }
            if (compression == TIFFConstants.COMPRESSION_LZW || compression == TIFFConstants.COMPRESSION_DEFLATE || compression == TIFFConstants.COMPRESSION_ADOBE_DEFLATE) {
                val predictorField = dir.getField(TIFFConstants.TIFFTAG_PREDICTOR)
                if (predictorField != null) {
                    predictor = predictorField.getAsInt(0)
                    if (predictor != 1 && predictor != 2) {
                        throw RuntimeException(MessageLocalization.getComposedMessage("illegal.value.for.predictor.in.tiff.file"))
                    }
                    if (predictor == 2 && bitsPerSample != 8) {
                        throw RuntimeException(MessageLocalization.getComposedMessage("1.bit.samples.are.not.supported.for.horizontal.differencing.predictor", bitsPerSample))
                    }
                }
            }
            if (compression == TIFFConstants.COMPRESSION_LZW) {
                lzwDecoder = TIFFLZWDecoder(w, predictor, samplePerPixel)
            }
            var rowsLeft = h
            var stream: ByteArrayOutputStream? = null
            var mstream: ByteArrayOutputStream? = null
            var zip: DeflaterOutputStream? = null
            var mzip: DeflaterOutputStream? = null
            if (extraSamples > 0) {
                mstream = ByteArrayOutputStream()
                mzip = DeflaterOutputStream(mstream)
            }

            var g4: CCITTG4Encoder? = null
            if (bitsPerSample == 1 && samplePerPixel == 1 && photometric != TIFFConstants.PHOTOMETRIC_PALETTE) {
                g4 = CCITTG4Encoder(w)
            } else {
                stream = ByteArrayOutputStream()
                if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG)
                    zip = DeflaterOutputStream(stream)
            }
            if (compression == TIFFConstants.COMPRESSION_OJPEG) {

                // Assume that the TIFFTAG_JPEGIFBYTECOUNT tag is optional, since it's obsolete and 
                // is often missing

                if (!dir.isTagPresent(TIFFConstants.TIFFTAG_JPEGIFOFFSET)) {
                    throw IOException(MessageLocalization.getComposedMessage("missing.tag.s.for.ojpeg.compression"))
                }
                val jpegOffset = dir.getFieldAsLong(TIFFConstants.TIFFTAG_JPEGIFOFFSET).toInt()
                var jpegLength = s.length().toInt() - jpegOffset

                if (dir.isTagPresent(TIFFConstants.TIFFTAG_JPEGIFBYTECOUNT)) {
                    jpegLength = dir.getFieldAsLong(TIFFConstants.TIFFTAG_JPEGIFBYTECOUNT).toInt() + size!![0].toInt()
                }

                val jpeg = ByteArray(Math.min(jpegLength, s.length().toInt() - jpegOffset))

                // tiff files work with offsets based on absolute positioning with regards to the start of the file
                s.seek(jpegOffset.toLong())
                s.readFully(jpeg)
                img = Jpeg(jpeg)
            } else if (compression == TIFFConstants.COMPRESSION_JPEG) {
                if (size!!.size > 1)
                    throw IOException(MessageLocalization.getComposedMessage("compression.jpeg.is.only.supported.with.a.single.strip.this.image.has.1.strips", size.size))
                var jpeg = ByteArray(size[0].toInt())
                s.seek(offset[0])
                s.readFully(jpeg)
                // if quantization and/or Huffman tables are stored separately in the tiff,
                // we need to add them to the jpeg data
                val jpegtables = dir.getField(TIFFConstants.TIFFTAG_JPEGTABLES)
                if (jpegtables != null) {
                    val temp = jpegtables.asBytes
                    var tableoffset = 0
                    var tablelength = temp.size
                    // remove FFD8 from start
                    if (temp[0] == 0xFF.toByte() && temp[1] == 0xD8.toByte()) {
                        tableoffset = 2
                        tablelength -= 2
                    }
                    // remove FFD9 from end
                    if (temp[temp.size - 2] == 0xFF.toByte() && temp[temp.size - 1] == 0xD9.toByte())
                        tablelength -= 2
                    val tables = ByteArray(tablelength)
                    System.arraycopy(temp, tableoffset, tables, 0, tablelength)
                    // TODO insert after JFIF header, instead of at the start
                    val jpegwithtables = ByteArray(jpeg.size + tables.size)
                    System.arraycopy(jpeg, 0, jpegwithtables, 0, 2)
                    System.arraycopy(tables, 0, jpegwithtables, 2, tables.size)
                    System.arraycopy(jpeg, 2, jpegwithtables, tables.size + 2, jpeg.size - 2)
                    jpeg = jpegwithtables
                }
                img = Jpeg(jpeg)
                if (photometric == TIFFConstants.PHOTOMETRIC_RGB) {
                    img.colorTransform = 0
                }
            } else {
                for (k in offset.indices) {
                    val im = ByteArray(size!![k].toInt())
                    s.seek(offset[k])
                    s.readFully(im)
                    val height = Math.min(rowsStrip, rowsLeft)
                    var outBuf: ByteArray? = null
                    if (compression != TIFFConstants.COMPRESSION_NONE)
                        outBuf = ByteArray((w * bitsPerSample * samplePerPixel + 7) / 8 * height)
                    if (reverse)
                        TIFFFaxDecoder.reverseBits(im)
                    when (compression) {
                        TIFFConstants.COMPRESSION_DEFLATE, TIFFConstants.COMPRESSION_ADOBE_DEFLATE -> {
                            inflate(im, outBuf)
                            applyPredictor(outBuf, predictor, w, height, samplePerPixel)
                        }
                        TIFFConstants.COMPRESSION_NONE -> outBuf = im
                        TIFFConstants.COMPRESSION_PACKBITS -> decodePackbits(im, outBuf)
                        TIFFConstants.COMPRESSION_LZW -> lzwDecoder!!.decode(im, outBuf, height)
                    }
                    if (bitsPerSample == 1 && samplePerPixel == 1 && photometric != TIFFConstants.PHOTOMETRIC_PALETTE) {
                        g4!!.fax4Encode(outBuf, height)
                    } else {
                        if (extraSamples > 0)
                            ProcessExtraSamples(zip, mzip, outBuf, samplePerPixel, bitsPerSample, w, height)
                        else
                            zip!!.write(outBuf)
                    }
                    rowsLeft -= rowsStrip
                }
                if (bitsPerSample == 1 && samplePerPixel == 1 && photometric != TIFFConstants.PHOTOMETRIC_PALETTE) {
                    img = Image.getInstance(w, h, false, Image.CCITTG4,
                            if (photometric == TIFFConstants.PHOTOMETRIC_MINISBLACK) Image.CCITT_BLACKIS1 else 0, g4!!.close())
                } else {
                    zip!!.close()
                    img = ImgRaw(w, h, samplePerPixel - extraSamples, bitsPerSample, stream!!.toByteArray())
                    img.isDeflated = true
                }
            }
            img!!.setDpi(dpiX, dpiY)
            if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG) {
                if (dir.isTagPresent(TIFFConstants.TIFFTAG_ICCPROFILE)) {
                    try {
                        val fd = dir.getField(TIFFConstants.TIFFTAG_ICCPROFILE)
                        val icc_prof = ICC_Profile.getInstance(fd.asBytes)
                        if (samplePerPixel - extraSamples == icc_prof.numComponents)
                            img.tagICC(icc_prof)
                    } catch (e: RuntimeException) {
                        //empty
                    }

                }
                if (dir.isTagPresent(TIFFConstants.TIFFTAG_COLORMAP)) {
                    val fd = dir.getField(TIFFConstants.TIFFTAG_COLORMAP)
                    val rgb = fd.asChars
                    val palette = ByteArray(rgb.size)
                    val gColor = rgb.size / 3
                    val bColor = gColor * 2
                    for (k in 0..gColor - 1) {
                        palette[k * 3] = rgb[k].toInt().ushr(8).toByte()
                        palette[k * 3 + 1] = rgb[k + gColor].toInt().ushr(8).toByte()
                        palette[k * 3 + 2] = rgb[k + bColor].toInt().ushr(8).toByte()
                    }
                    // Colormap components are supposed to go from 0 to 655535 but,
                    // as usually, some tiff producers just put values from 0 to 255.
                    // Let's check for these broken tiffs.
                    var colormapBroken = true
                    for (k in palette.indices) {
                        if (palette[k].toInt() != 0) {
                            colormapBroken = false
                            break
                        }
                    }
                    if (colormapBroken) {
                        for (k in 0..gColor - 1) {
                            palette[k * 3] = rgb[k].toByte()
                            palette[k * 3 + 1] = rgb[k + gColor].toByte()
                            palette[k * 3 + 2] = rgb[k + bColor].toByte()
                        }
                    }
                    val indexed = PdfArray()
                    indexed.add(PdfName.INDEXED)
                    indexed.add(PdfName.DEVICERGB)
                    indexed.add(PdfNumber(gColor - 1))
                    indexed.add(PdfString(palette))
                    val additional = PdfDictionary()
                    additional.put(PdfName.COLORSPACE, indexed)
                    img.additional = additional
                }
                img.originalType = Image.ORIGINAL_TIFF
            }
            if (photometric == TIFFConstants.PHOTOMETRIC_MINISWHITE)
                img.isInverted = true
            if (rotation != 0f)
                img.initialRotation = rotation
            if (extraSamples > 0) {
                mzip!!.close()
                val mimg = Image.getInstance(w, h, 1, bitsPerSample, mstream!!.toByteArray())
                mimg.makeMask()
                mimg.isDeflated = true
                img.imageMask = mimg
            }
            return img
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    @Throws(IOException::class)
    internal fun ProcessExtraSamples(zip: DeflaterOutputStream, mzip: DeflaterOutputStream, outBuf: ByteArray, samplePerPixel: Int, bitsPerSample: Int, width: Int, height: Int): Image? {
        if (bitsPerSample == 8) {
            val mask = ByteArray(width * height)
            var mptr = 0
            var optr = 0
            val total = width * height * samplePerPixel
            var k = 0
            while (k < total) {
                for (s in 0..samplePerPixel - 1 - 1) {
                    outBuf[optr++] = outBuf[k + s]
                }
                mask[mptr++] = outBuf[k + samplePerPixel - 1]
                k += samplePerPixel
            }
            zip.write(outBuf, 0, optr)
            mzip.write(mask, 0, mptr)
        } else
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("extra.samples.are.not.supported"))
        return null
    }

    internal fun getArrayLongShort(dir: TIFFDirectory, tag: Int): LongArray? {
        val field = dir.getField(tag) ?: return null
        val offset: LongArray
        if (field.type == TIFFField.TIFF_LONG)
            offset = field.asLongs
        else {
            // must be short
            val temp = field.asChars
            offset = LongArray(temp.size)
            for (k in temp.indices)
                offset[k] = temp[k].toLong()
        }
        return offset
    }

    // Uncompress packbits compressed image data.
    fun decodePackbits(data: ByteArray, dst: ByteArray) {
        var srcCount = 0
        var dstCount = 0
        var repeat: Byte
        var b: Byte

        try {
            while (dstCount < dst.size) {
                b = data[srcCount++]
                if (b >= 0 && b <= 127) {
                    // literal run packet
                    for (i in 0..b + 1 - 1) {
                        dst[dstCount++] = data[srcCount++]
                    }

                } else if (b <= -1 && b >= -127) {
                    // 2 byte encoded run packet
                    repeat = data[srcCount++]
                    for (i in 0..-b + 1 - 1) {
                        dst[dstCount++] = repeat
                    }
                } else {
                    // no-op packet. Do nothing
                    srcCount++
                }
            }
        } catch (e: Exception) {
            // do nothing
        }

    }

    fun inflate(deflated: ByteArray, inflated: ByteArray) {
        val inflater = Inflater()
        inflater.setInput(deflated)
        try {
            inflater.inflate(inflated)
        } catch (dfe: DataFormatException) {
            throw ExceptionConverter(dfe)
        }

    }

    fun applyPredictor(uncompData: ByteArray, predictor: Int, w: Int, h: Int, samplesPerPixel: Int) {
        if (predictor != 2)
            return
        var count: Int
        for (j in 0..h - 1) {
            count = samplesPerPixel * (j * w + 1)
            for (i in samplesPerPixel..w * samplesPerPixel - 1) {
                uncompData[count] += uncompData[count - samplesPerPixel]
                count++
            }
        }
    }
}
/** Reads a page from a TIFF image. Direct mode is not used.
 * @param s the file source
 * *
 * @param page the page to get. The first page is 1
 * *
 * @return the Image
 */
