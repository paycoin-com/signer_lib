/*
 * $Id: b509231b26e9fa5d76075187ccde6bacce505972 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2015 iText Group NV
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

/*
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageioimpl.plugins.bmp.BMPImageReader.java)
 * using the BSD license in a specific wording. In a mail dating from
 * January 23, 2008, Brian Burkhalter (@sun.com) gave us permission
 * to use the code under the following version of the BSD license:
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this  list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for
 * use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
package com.itextpdf.text.pdf.codec

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.HashMap

import com.itextpdf.text.BadElementException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.ImgRaw
import com.itextpdf.text.Utilities
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfString

/** Reads a BMP image. All types of BMP can be read.
 *
 *
 * It is based in the JAI codec.

 * @author  Paulo Soares
 */
class BmpImage @Throws(IOException::class)
internal constructor(`is`: InputStream, noHeader: Boolean, size: Int) {

    // BMP variables
    private var inputStream: InputStream? = null
    private var bitmapFileSize: Long = 0
    private var bitmapOffset: Long = 0
    private var compression: Long = 0
    private var imageSize: Long = 0
    private var palette: ByteArray? = null
    private var imageType: Int = 0
    private var numBands: Int = 0
    private var isBottomUp: Boolean = false
    private var bitsPerPixel: Int = 0
    private var redMask: Int = 0
    private var greenMask: Int = 0
    private var blueMask: Int = 0
    private var alphaMask: Int = 0
    var properties = HashMap<String, Any>()
    private var xPelsPerMeter: Long = 0
    private var yPelsPerMeter: Long = 0

    internal var width: Int = 0
    internal var height: Int = 0

    init {
        bitmapFileSize = size.toLong()
        bitmapOffset = 0
        process(`is`, noHeader)
    }


    @Throws(IOException::class)
    protected fun process(stream: InputStream, noHeader: Boolean) {
        if (noHeader || stream is BufferedInputStream) {
            inputStream = stream
        } else {
            inputStream = BufferedInputStream(stream)
        }
        if (!noHeader) {
            // Start File Header
            if (!(readUnsignedByte(inputStream) == 'B' && readUnsignedByte(inputStream) == 'M')) {
                throw RuntimeException(MessageLocalization.getComposedMessage("invalid.magic.value.for.bmp.file"))
            }

            // Read file size
            bitmapFileSize = readDWord(inputStream)

            // Read the two reserved fields
            readWord(inputStream)
            readWord(inputStream)

            // Offset to the bitmap from the beginning
            bitmapOffset = readDWord(inputStream)

            // End File Header
        }
        // Start BitmapCoreHeader
        val size = readDWord(inputStream)

        if (size == 12) {
            width = readWord(inputStream)
            height = readWord(inputStream)
        } else {
            width = readLong(inputStream)
            height = readLong(inputStream)
        }

        val planes = readWord(inputStream)
        bitsPerPixel = readWord(inputStream)

        properties.put("color_planes", Integer.valueOf(planes))
        properties.put("bits_per_pixel", Integer.valueOf(bitsPerPixel))

        // As BMP always has 3 rgb bands, except for Version 5,
        // which is bgra
        numBands = 3
        if (bitmapOffset == 0)
            bitmapOffset = size
        if (size == 12) {
            // Windows 2.x and OS/2 1.x
            properties.put("bmp_version", "BMP v. 2.x")

            // Classify the image type
            if (bitsPerPixel == 1) {
                imageType = VERSION_2_1_BIT
            } else if (bitsPerPixel == 4) {
                imageType = VERSION_2_4_BIT
            } else if (bitsPerPixel == 8) {
                imageType = VERSION_2_8_BIT
            } else if (bitsPerPixel == 24) {
                imageType = VERSION_2_24_BIT
            }

            // Read in the palette
            val numberOfEntries = ((bitmapOffset - 14 - size) / 3).toInt()
            var sizeOfPalette = numberOfEntries * 3
            if (bitmapOffset == size) {
                when (imageType) {
                    VERSION_2_1_BIT -> sizeOfPalette = 2 * 3
                    VERSION_2_4_BIT -> sizeOfPalette = 16 * 3
                    VERSION_2_8_BIT -> sizeOfPalette = 256 * 3
                    VERSION_2_24_BIT -> sizeOfPalette = 0
                }
                bitmapOffset = size + sizeOfPalette
            }
            readPalette(sizeOfPalette)
        } else {

            compression = readDWord(inputStream)
            imageSize = readDWord(inputStream)
            xPelsPerMeter = readLong(inputStream).toLong()
            yPelsPerMeter = readLong(inputStream).toLong()
            val colorsUsed = readDWord(inputStream)
            val colorsImportant = readDWord(inputStream)

            when (compression.toInt()) {
                BI_RGB -> properties.put("compression", "BI_RGB")

                BI_RLE8 -> properties.put("compression", "BI_RLE8")

                BI_RLE4 -> properties.put("compression", "BI_RLE4")

                BI_BITFIELDS -> properties.put("compression", "BI_BITFIELDS")
            }

            properties.put("x_pixels_per_meter", java.lang.Long.valueOf(xPelsPerMeter))
            properties.put("y_pixels_per_meter", java.lang.Long.valueOf(yPelsPerMeter))
            properties.put("colors_used", java.lang.Long.valueOf(colorsUsed))
            properties.put("colors_important", java.lang.Long.valueOf(colorsImportant))

            if (size == 40 || size == 52 || size == 56) {
                // Windows 3.x and Windows NT
                when (compression.toInt()) {

                    BI_RGB  // No compression
                        , BI_RLE8  // 8-bit RLE compression
                        , BI_RLE4  // 4-bit RLE compression
                    -> {

                        if (bitsPerPixel == 1) {
                            imageType = VERSION_3_1_BIT
                        } else if (bitsPerPixel == 4) {
                            imageType = VERSION_3_4_BIT
                        } else if (bitsPerPixel == 8) {
                            imageType = VERSION_3_8_BIT
                        } else if (bitsPerPixel == 24) {
                            imageType = VERSION_3_24_BIT
                        } else if (bitsPerPixel == 16) {
                            imageType = VERSION_3_NT_16_BIT
                            redMask = 0x7C00
                            greenMask = 0x3E0
                            blueMask = 0x1F
                            properties.put("red_mask", Integer.valueOf(redMask))
                            properties.put("green_mask", Integer.valueOf(greenMask))
                            properties.put("blue_mask", Integer.valueOf(blueMask))
                        } else if (bitsPerPixel == 32) {
                            imageType = VERSION_3_NT_32_BIT
                            redMask = 0x00FF0000
                            greenMask = 0x0000FF00
                            blueMask = 0x000000FF
                            properties.put("red_mask", Integer.valueOf(redMask))
                            properties.put("green_mask", Integer.valueOf(greenMask))
                            properties.put("blue_mask", Integer.valueOf(blueMask))
                        }

                        // 52 and 56 byte header have mandatory R, G and B masks
                        if (size >= 52) {
                            redMask = readDWord(inputStream).toInt()
                            greenMask = readDWord(inputStream).toInt()
                            blueMask = readDWord(inputStream).toInt()
                            properties.put("red_mask", Integer.valueOf(redMask))
                            properties.put("green_mask", Integer.valueOf(greenMask))
                            properties.put("blue_mask", Integer.valueOf(blueMask))
                        }
                        // 56 byte header has mandatory alpha mask
                        if (size == 56) {
                            alphaMask = readDWord(inputStream).toInt()
                            properties.put("alpha_mask", Integer.valueOf(alphaMask))
                        }

                        // Read in the palette
                        val numberOfEntries = ((bitmapOffset - 14 - size) / 4).toInt()
                        var sizeOfPalette = numberOfEntries * 4
                        if (bitmapOffset == size) {
                            when (imageType) {
                                VERSION_3_1_BIT -> sizeOfPalette = (if (colorsUsed == 0) 2 else colorsUsed).toInt() * 4
                                VERSION_3_4_BIT -> sizeOfPalette = (if (colorsUsed == 0) 16 else colorsUsed).toInt() * 4
                                VERSION_3_8_BIT -> sizeOfPalette = (if (colorsUsed == 0) 256 else colorsUsed).toInt() * 4
                                else -> sizeOfPalette = 0
                            }
                            bitmapOffset = size + sizeOfPalette
                        }
                        readPalette(sizeOfPalette)

                        properties.put("bmp_version", "BMP v. 3.x")
                    }

                    BI_BITFIELDS -> {

                        if (bitsPerPixel == 16) {
                            imageType = VERSION_3_NT_16_BIT
                        } else if (bitsPerPixel == 32) {
                            imageType = VERSION_3_NT_32_BIT
                        }

                        // BitsField encoding
                        redMask = readDWord(inputStream).toInt()
                        greenMask = readDWord(inputStream).toInt()
                        blueMask = readDWord(inputStream).toInt()

                        // 56 byte header has mandatory alpha mask
                        if (size == 56) {
                            alphaMask = readDWord(inputStream).toInt()
                            properties.put("alpha_mask", Integer.valueOf(alphaMask))
                        }

                        properties.put("red_mask", Integer.valueOf(redMask))
                        properties.put("green_mask", Integer.valueOf(greenMask))
                        properties.put("blue_mask", Integer.valueOf(blueMask))

                        if (colorsUsed != 0) {
                            // there is a palette
                            sizeOfPalette = colorsUsed.toInt() * 4
                            readPalette(sizeOfPalette)
                        }

                        properties.put("bmp_version", "BMP v. 3.x NT")
                    }

                    else -> throw RuntimeException("Invalid compression specified in BMP file.")
                }
            } else if (size == 108) {
                // Windows 4.x BMP

                properties.put("bmp_version", "BMP v. 4.x")

                // rgb masks, valid only if comp is BI_BITFIELDS
                redMask = readDWord(inputStream).toInt()
                greenMask = readDWord(inputStream).toInt()
                blueMask = readDWord(inputStream).toInt()
                // Only supported for 32bpp BI_RGB argb
                alphaMask = readDWord(inputStream).toInt()
                val csType = readDWord(inputStream)
                val redX = readLong(inputStream)
                val redY = readLong(inputStream)
                val redZ = readLong(inputStream)
                val greenX = readLong(inputStream)
                val greenY = readLong(inputStream)
                val greenZ = readLong(inputStream)
                val blueX = readLong(inputStream)
                val blueY = readLong(inputStream)
                val blueZ = readLong(inputStream)
                val gammaRed = readDWord(inputStream)
                val gammaGreen = readDWord(inputStream)
                val gammaBlue = readDWord(inputStream)

                if (bitsPerPixel == 1) {
                    imageType = VERSION_4_1_BIT
                } else if (bitsPerPixel == 4) {
                    imageType = VERSION_4_4_BIT
                } else if (bitsPerPixel == 8) {
                    imageType = VERSION_4_8_BIT
                } else if (bitsPerPixel == 16) {
                    imageType = VERSION_4_16_BIT
                    if (compression.toInt() == BI_RGB) {
                        redMask = 0x7C00
                        greenMask = 0x3E0
                        blueMask = 0x1F
                    }
                } else if (bitsPerPixel == 24) {
                    imageType = VERSION_4_24_BIT
                } else if (bitsPerPixel == 32) {
                    imageType = VERSION_4_32_BIT
                    if (compression.toInt() == BI_RGB) {
                        redMask = 0x00FF0000
                        greenMask = 0x0000FF00
                        blueMask = 0x000000FF
                    }
                }

                properties.put("red_mask", Integer.valueOf(redMask))
                properties.put("green_mask", Integer.valueOf(greenMask))
                properties.put("blue_mask", Integer.valueOf(blueMask))
                properties.put("alpha_mask", Integer.valueOf(alphaMask))

                // Read in the palette
                val numberOfEntries = ((bitmapOffset - 14 - size) / 4).toInt()
                var sizeOfPalette = numberOfEntries * 4
                if (bitmapOffset == size) {
                    when (imageType) {
                        VERSION_4_1_BIT -> sizeOfPalette = (if (colorsUsed == 0) 2 else colorsUsed).toInt() * 4
                        VERSION_4_4_BIT -> sizeOfPalette = (if (colorsUsed == 0) 16 else colorsUsed).toInt() * 4
                        VERSION_4_8_BIT -> sizeOfPalette = (if (colorsUsed == 0) 256 else colorsUsed).toInt() * 4
                        else -> sizeOfPalette = 0
                    }
                    bitmapOffset = size + sizeOfPalette
                }
                readPalette(sizeOfPalette)

                when (csType.toInt()) {
                    LCS_CALIBRATED_RGB -> {
                        // All the new fields are valid only for this case
                        properties.put("color_space", "LCS_CALIBRATED_RGB")
                        properties.put("redX", Integer.valueOf(redX))
                        properties.put("redY", Integer.valueOf(redY))
                        properties.put("redZ", Integer.valueOf(redZ))
                        properties.put("greenX", Integer.valueOf(greenX))
                        properties.put("greenY", Integer.valueOf(greenY))
                        properties.put("greenZ", Integer.valueOf(greenZ))
                        properties.put("blueX", Integer.valueOf(blueX))
                        properties.put("blueY", Integer.valueOf(blueY))
                        properties.put("blueZ", Integer.valueOf(blueZ))
                        properties.put("gamma_red", java.lang.Long.valueOf(gammaRed))
                        properties.put("gamma_green", java.lang.Long.valueOf(gammaGreen))
                        properties.put("gamma_blue", java.lang.Long.valueOf(gammaBlue))

                        // break;
                        throw RuntimeException("Not implemented yet.")
                    }

                    LCS_sRGB -> // Default Windows color space
                        properties.put("color_space", "LCS_sRGB")

                    LCS_CMYK -> {
                        properties.put("color_space", "LCS_CMYK")
                        //		    break;
                        throw RuntimeException("Not implemented yet.")
                    }
                }

            } else {
                properties.put("bmp_version", "BMP v. 5.x")
                throw RuntimeException("BMP version 5 not implemented yet.")
            }
        }

        if (height > 0) {
            // bottom up image
            isBottomUp = true
        } else {
            // top down image
            isBottomUp = false
            height = Math.abs(height)
        }
        // When number of bitsPerPixel is <= 8, we use IndexColorModel.
        if (bitsPerPixel == 1 || bitsPerPixel == 4 || bitsPerPixel == 8) {

            numBands = 1


            // Create IndexColorModel from the palette.
            val r: ByteArray
            val g: ByteArray
            val b: ByteArray
            var sizep: Int
            if (imageType == VERSION_2_1_BIT ||
                    imageType == VERSION_2_4_BIT ||
                    imageType == VERSION_2_8_BIT) {

                sizep = palette!!.size / 3

                if (sizep > 256) {
                    sizep = 256
                }

                var off: Int
                r = ByteArray(sizep)
                g = ByteArray(sizep)
                b = ByteArray(sizep)
                for (i in 0..sizep - 1) {
                    off = 3 * i
                    b[i] = palette!![off]
                    g[i] = palette!![off + 1]
                    r[i] = palette!![off + 2]
                }
            } else {
                sizep = palette!!.size / 4

                if (sizep > 256) {
                    sizep = 256
                }

                var off: Int
                r = ByteArray(sizep)
                g = ByteArray(sizep)
                b = ByteArray(sizep)
                for (i in 0..sizep - 1) {
                    off = 4 * i
                    b[i] = palette!![off]
                    g[i] = palette!![off + 1]
                    r[i] = palette!![off + 2]
                }
            }

        } else if (bitsPerPixel == 16) {
            numBands = 3
        } else if (bitsPerPixel == 32) {
            numBands = if (alphaMask == 0) 3 else 4

            // The number of bands in the SampleModel is determined by
            // the length of the mask array passed in.
        } else {
            numBands = 3
        }
    }

    private fun getPalette(group: Int): ByteArray? {
        if (palette == null)
            return null
        val np = ByteArray(palette!!.size / group * 3)
        val e = palette!!.size / group
        for (k in 0..e - 1) {
            var src = k * group
            val dest = k * 3
            np[dest + 2] = palette!![src++]
            np[dest + 1] = palette!![src++]
            np[dest] = palette!![src]
        }
        return np
    }

    private // buffer for byte data
            //	if (sampleModel.getDataType() == DataBuffer.TYPE_BYTE)
            //	    bdata = (byte[])((DataBufferByte)tile.getDataBuffer()).getData();
            //	else if (sampleModel.getDataType() == DataBuffer.TYPE_USHORT)
            //	    sdata = (short[])((DataBufferUShort)tile.getDataBuffer()).getData();
            //	else if (sampleModel.getDataType() == DataBuffer.TYPE_INT)
            //	    idata = (int[])((DataBufferInt)tile.getDataBuffer()).getData();
            // There should only be one tile.
            // no compression
            // no compression
            // no compression
            // no compression
            // 1-bit images cannot be compressed.
            // 24-bit images are not compressed
    val image: Image?
        @Throws(IOException::class, BadElementException::class)
        get() {
            var bdata: ByteArray? = null
            when (imageType) {

                VERSION_2_1_BIT -> return read1Bit(3)

                VERSION_2_4_BIT -> return read4Bit(3)

                VERSION_2_8_BIT -> return read8Bit(3)

                VERSION_2_24_BIT -> {
                    bdata = ByteArray(width * height * 3)
                    read24Bit(bdata)
                    return ImgRaw(width, height, 3, 8, bdata)
                }

                VERSION_3_1_BIT -> return read1Bit(4)

                VERSION_3_4_BIT -> {
                    when (compression.toInt()) {
                        BI_RGB -> return read4Bit(4)

                        BI_RLE4 -> return readRLE4()

                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    when (compression.toInt()) {
                        BI_RGB -> return read8Bit(4)

                        BI_RLE8 -> return readRLE8()

                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    bdata = ByteArray(width * height * 3)
                    read24Bit(bdata)
                    return ImgRaw(width, height, 3, 8, bdata)
                }

                VERSION_3_8_BIT -> {
                    when (compression.toInt()) {
                        BI_RGB -> return read8Bit(4)
                        BI_RLE8 -> return readRLE8()
                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    bdata = ByteArray(width * height * 3)
                    read24Bit(bdata)
                    return ImgRaw(width, height, 3, 8, bdata)
                }

                VERSION_3_24_BIT -> {
                    bdata = ByteArray(width * height * 3)
                    read24Bit(bdata)
                    return ImgRaw(width, height, 3, 8, bdata)
                }

                VERSION_3_NT_16_BIT -> return read1632Bit(false)

                VERSION_3_NT_32_BIT -> return read1632Bit(true)

                VERSION_4_1_BIT -> return read1Bit(4)

                VERSION_4_4_BIT -> {
                    when (compression.toInt()) {

                        BI_RGB -> return read4Bit(4)

                        BI_RLE4 -> return readRLE4()

                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    when (compression.toInt()) {

                        BI_RGB -> return read8Bit(4)

                        BI_RLE8 -> return readRLE8()

                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    return read1632Bit(false)
                }

                VERSION_4_8_BIT -> {
                    when (compression.toInt()) {
                        BI_RGB -> return read8Bit(4)
                        BI_RLE8 -> return readRLE8()
                        else -> throw RuntimeException("Invalid compression specified for BMP file.")
                    }
                    return read1632Bit(false)
                }

                VERSION_4_16_BIT -> return read1632Bit(false)

                VERSION_4_24_BIT -> {
                    bdata = ByteArray(width * height * 3)
                    read24Bit(bdata)
                    return ImgRaw(width, height, 3, 8, bdata)
                }

                VERSION_4_32_BIT -> return read1632Bit(true)
            }
            return null
        }

    @Throws(BadElementException::class)
    private fun indexedModel(bdata: ByteArray, bpc: Int, paletteEntries: Int): Image {
        val img = ImgRaw(width, height, 1, bpc, bdata)
        val colorspace = PdfArray()
        colorspace.add(PdfName.INDEXED)
        colorspace.add(PdfName.DEVICERGB)
        val np = getPalette(paletteEntries)
        val len = np.size
        colorspace.add(PdfNumber(len / 3 - 1))
        colorspace.add(PdfString(np))
        val ad = PdfDictionary()
        ad.put(PdfName.COLORSPACE, colorspace)
        img.additional = ad
        return img
    }

    @Throws(IOException::class)
    private fun readPalette(sizeOfPalette: Int) {
        if (sizeOfPalette == 0) {
            return
        }

        palette = ByteArray(sizeOfPalette)
        var bytesRead = 0
        while (bytesRead < sizeOfPalette) {
            val r = inputStream!!.read(palette, bytesRead, sizeOfPalette - bytesRead)
            if (r < 0) {
                throw RuntimeException(MessageLocalization.getComposedMessage("incomplete.palette"))
            }
            bytesRead += r
        }
        properties.put("palette", palette)
    }

    // Deal with 1 Bit images using IndexColorModels
    @Throws(IOException::class, BadElementException::class)
    private fun read1Bit(paletteEntries: Int): Image {
        val bdata = ByteArray((width + 7) / 8 * height)
        var padding = 0
        val bytesPerScanline = Math.ceil(width / 8.0).toInt()

        val remainder = bytesPerScanline % 4
        if (remainder != 0) {
            padding = 4 - remainder
        }

        val imSize = (bytesPerScanline + padding) * height

        // Read till we have the whole image
        val values = ByteArray(imSize)
        var bytesRead = 0
        while (bytesRead < imSize) {
            bytesRead += inputStream!!.read(values, bytesRead,
                    imSize - bytesRead)
        }

        if (isBottomUp) {

            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.

            for (i in 0..height - 1) {
                System.arraycopy(values,
                        imSize - (i + 1) * (bytesPerScanline + padding),
                        bdata,
                        i * bytesPerScanline, bytesPerScanline)
            }
        } else {

            for (i in 0..height - 1) {
                System.arraycopy(values,
                        i * (bytesPerScanline + padding),
                        bdata,
                        i * bytesPerScanline,
                        bytesPerScanline)
            }
        }
        return indexedModel(bdata, 1, paletteEntries)
    }

    // Method to read a 4 bit BMP image data
    @Throws(IOException::class, BadElementException::class)
    private fun read4Bit(paletteEntries: Int): Image {
        val bdata = ByteArray((width + 1) / 2 * height)

        // Padding bytes at the end of each scanline
        var padding = 0

        val bytesPerScanline = Math.ceil(width / 2.0).toInt()
        val remainder = bytesPerScanline % 4
        if (remainder != 0) {
            padding = 4 - remainder
        }

        val imSize = (bytesPerScanline + padding) * height

        // Read till we have the whole image
        val values = ByteArray(imSize)
        var bytesRead = 0
        while (bytesRead < imSize) {
            bytesRead += inputStream!!.read(values, bytesRead,
                    imSize - bytesRead)
        }

        if (isBottomUp) {

            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            for (i in 0..height - 1) {
                System.arraycopy(values,
                        imSize - (i + 1) * (bytesPerScanline + padding),
                        bdata,
                        i * bytesPerScanline,
                        bytesPerScanline)
            }
        } else {
            for (i in 0..height - 1) {
                System.arraycopy(values,
                        i * (bytesPerScanline + padding),
                        bdata,
                        i * bytesPerScanline,
                        bytesPerScanline)
            }
        }
        return indexedModel(bdata, 4, paletteEntries)
    }

    // Method to read 8 bit BMP image data
    @Throws(IOException::class, BadElementException::class)
    private fun read8Bit(paletteEntries: Int): Image {
        val bdata = ByteArray(width * height)
        // Padding bytes at the end of each scanline
        var padding = 0

        // width * bitsPerPixel should be divisible by 32
        val bitsPerScanline = width * 8
        if (bitsPerScanline % 32 != 0) {
            padding = (bitsPerScanline / 32 + 1) * 32 - bitsPerScanline
            padding = Math.ceil(padding / 8.0).toInt()
        }

        val imSize = (width + padding) * height

        // Read till we have the whole image
        val values = ByteArray(imSize)
        var bytesRead = 0
        while (bytesRead < imSize) {
            bytesRead += inputStream!!.read(values, bytesRead, imSize - bytesRead)
        }

        if (isBottomUp) {

            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            for (i in 0..height - 1) {
                System.arraycopy(values,
                        imSize - (i + 1) * (width + padding),
                        bdata,
                        i * width,
                        width)
            }
        } else {
            for (i in 0..height - 1) {
                System.arraycopy(values,
                        i * (width + padding),
                        bdata,
                        i * width,
                        width)
            }
        }
        return indexedModel(bdata, 8, paletteEntries)
    }

    // Method to read 24 bit BMP image data
    private fun read24Bit(bdata: ByteArray) {
        // Padding bytes at the end of each scanline
        var padding = 0

        // width * bitsPerPixel should be divisible by 32
        val bitsPerScanline = width * 24
        if (bitsPerScanline % 32 != 0) {
            padding = (bitsPerScanline / 32 + 1) * 32 - bitsPerScanline
            padding = Math.ceil(padding / 8.0).toInt()
        }


        val imSize = (width * 3 + 3) / 4 * 4 * height
        // Read till we have the whole image
        val values = ByteArray(imSize)
        try {
            var bytesRead = 0
            while (bytesRead < imSize) {
                val r = inputStream!!.read(values, bytesRead,
                        imSize - bytesRead)
                if (r < 0)
                    break
                bytesRead += r
            }
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

        var l = 0
        var count: Int

        if (isBottomUp) {
            val max = width * height * 3 - 1

            count = -padding
            for (i in 0..height - 1) {
                l = max - (i + 1) * width * 3 + 1
                count += padding
                for (j in 0..width - 1) {
                    bdata[l + 2] = values[count++]
                    bdata[l + 1] = values[count++]
                    bdata[l] = values[count++]
                    l += 3
                }
            }
        } else {
            count = -padding
            for (i in 0..height - 1) {
                count += padding
                for (j in 0..width - 1) {
                    bdata[l + 2] = values[count++]
                    bdata[l + 1] = values[count++]
                    bdata[l] = values[count++]
                    l += 3
                }
            }
        }
    }

    private fun findMask(mask: Int): Int {
        var mask = mask
        var k = 0
        while (k < 32) {
            if (mask and 1 == 1)
                break
            mask = mask ushr 1
            ++k
        }
        return mask
    }

    private fun findShift(mask: Int): Int {
        var mask = mask
        var k = 0
        while (k < 32) {
            if (mask and 1 == 1)
                break
            mask = mask ushr 1
            ++k
        }
        return k
    }

    @Throws(IOException::class, BadElementException::class)
    private fun read1632Bit(is32: Boolean): Image {

        val red_mask = findMask(redMask)
        val red_shift = findShift(redMask)
        val red_factor = red_mask + 1
        val green_mask = findMask(greenMask)
        val green_shift = findShift(greenMask)
        val green_factor = green_mask + 1
        val blue_mask = findMask(blueMask)
        val blue_shift = findShift(blueMask)
        val blue_factor = blue_mask + 1
        val bdata = ByteArray(width * height * 3)
        // Padding bytes at the end of each scanline
        var padding = 0

        if (!is32) {
            // width * bitsPerPixel should be divisible by 32
            val bitsPerScanline = width * 16
            if (bitsPerScanline % 32 != 0) {
                padding = (bitsPerScanline / 32 + 1) * 32 - bitsPerScanline
                padding = Math.ceil(padding / 8.0).toInt()
            }
        }

        var imSize = imageSize.toInt()
        if (imSize == 0) {
            imSize = (bitmapFileSize - bitmapOffset).toInt()
        }

        var l = 0
        var v: Int
        if (isBottomUp) {
            for (i in height - 1 downTo 0) {
                l = width * 3 * i
                for (j in 0..width - 1) {
                    if (is32)
                        v = readDWord(inputStream).toInt()
                    else
                        v = readWord(inputStream)
                    bdata[l++] = ((v.ushr(red_shift) and red_mask) * 256 / red_factor).toByte()
                    bdata[l++] = ((v.ushr(green_shift) and green_mask) * 256 / green_factor).toByte()
                    bdata[l++] = ((v.ushr(blue_shift) and blue_mask) * 256 / blue_factor).toByte()
                }
                for (m in 0..padding - 1) {
                    inputStream!!.read()
                }
            }
        } else {
            for (i in 0..height - 1) {
                for (j in 0..width - 1) {
                    if (is32)
                        v = readDWord(inputStream).toInt()
                    else
                        v = readWord(inputStream)
                    bdata[l++] = ((v.ushr(red_shift) and red_mask) * 256 / red_factor).toByte()
                    bdata[l++] = ((v.ushr(green_shift) and green_mask) * 256 / green_factor).toByte()
                    bdata[l++] = ((v.ushr(blue_shift) and blue_mask) * 256 / blue_factor).toByte()
                }
                for (m in 0..padding - 1) {
                    inputStream!!.read()
                }
            }
        }
        return ImgRaw(width, height, 3, 8, bdata)
    }

    @Throws(IOException::class, BadElementException::class)
    private fun readRLE8(): Image {

        // If imageSize field is not provided, calculate it.
        var imSize = imageSize.toInt()
        if (imSize == 0) {
            imSize = (bitmapFileSize - bitmapOffset).toInt()
        }

        // Read till we have the whole image
        val values = ByteArray(imSize)
        var bytesRead = 0
        while (bytesRead < imSize) {
            bytesRead += inputStream!!.read(values, bytesRead,
                    imSize - bytesRead)
        }

        // Since data is compressed, decompress it
        var `val` = decodeRLE(true, values)

        // Uncompressed data does not have any padding
        imSize = width * height

        if (isBottomUp) {

            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            // int bytesPerScanline = (int)Math.ceil((double)width/8.0);
            val temp = ByteArray(`val`.size)
            val bytesPerScanline = width
            for (i in 0..height - 1) {
                System.arraycopy(`val`,
                        imSize - (i + 1) * bytesPerScanline,
                        temp,
                        i * bytesPerScanline, bytesPerScanline)
            }
            `val` = temp
        }
        return indexedModel(`val`, 8, 4)
    }

    @Throws(IOException::class, BadElementException::class)
    private fun readRLE4(): Image {

        // If imageSize field is not specified, calculate it.
        var imSize = imageSize.toInt()
        if (imSize == 0) {
            imSize = (bitmapFileSize - bitmapOffset).toInt()
        }

        // Read till we have the whole image
        val values = ByteArray(imSize)
        var bytesRead = 0
        while (bytesRead < imSize) {
            bytesRead += inputStream!!.read(values, bytesRead,
                    imSize - bytesRead)
        }

        // Decompress the RLE4 compressed data.
        var `val` = decodeRLE(false, values)

        // Invert it as it is bottom up format.
        if (isBottomUp) {

            val inverted = `val`
            `val` = ByteArray(width * height)
            var l = 0
            var index: Int
            var lineEnd: Int

            for (i in height - 1 downTo 0) {
                index = i * width
                lineEnd = l + width
                while (l != lineEnd) {
                    `val`[l++] = inverted[index++]
                }
            }
        }
        val stride = (width + 1) / 2
        val bdata = ByteArray(stride * height)
        var ptr = 0
        var sh = 0
        for (h in 0..height - 1) {
            for (w in 0..width - 1) {
                if (w and 1 == 0)
                    bdata[sh + w / 2] = (`val`[ptr++] shl 4).toByte()
                else
                    bdata[sh + w / 2] = bdata[sh + w / 2] or (`val`[ptr++] and 0x0f).toByte()
            }
            sh += stride
        }
        return indexedModel(bdata, 4, 4)
    }

    private fun decodeRLE(is8: Boolean, values: ByteArray): ByteArray {
        val `val` = ByteArray(width * height)
        try {
            var ptr = 0
            var x = 0
            var q = 0
            var y = 0
            while (y < height && ptr < values.size) {
                var count = values[ptr++] and 0xff
                if (count != 0) {
                    // encoded mode
                    val bt = values[ptr++] and 0xff
                    if (is8) {
                        for (i in count downTo 1) {
                            `val`[q++] = bt.toByte()
                        }
                    } else {
                        for (i in 0..count - 1) {
                            `val`[q++] = (if (i and 1 == 1) bt and 0x0f else bt.ushr(4) and 0x0f).toByte()
                        }
                    }
                    x += count
                } else {
                    // escape mode
                    count = values[ptr++] and 0xff
                    if (count == 1)
                        break
                    when (count) {
                        0 -> {
                            x = 0
                            ++y
                            q = y * width
                        }
                        2 -> {
                            // delta mode
                            x += values[ptr++] and 0xff
                            y += values[ptr++] and 0xff
                            q = y * width + x
                        }
                        else -> {
                            // absolute mode
                            if (is8) {
                                for (i in count downTo 1)
                                    `val`[q++] = (values[ptr++] and 0xff).toByte()
                            } else {
                                var bt = 0
                                for (i in 0..count - 1) {
                                    if (i and 1 == 0)
                                        bt = values[ptr++] and 0xff
                                    `val`[q++] = (if (i and 1 == 1) bt and 0x0f else bt.ushr(4) and 0x0f).toByte()
                                }
                            }
                            x += count
                            // read pad byte
                            if (is8) {
                                if (count and 1 == 1)
                                    ++ptr
                            } else {
                                if (count and 3 == 1 || count and 3 == 2)
                                    ++ptr
                            }
                        }
                    }
                }
            }
        } catch (e: RuntimeException) {
            //empty on purpose
        }

        return `val`
    }

    // Windows defined data type reading methods - everything is little endian

    // Unsigned 8 bits
    @Throws(IOException::class)
    private fun readUnsignedByte(stream: InputStream): Int {
        return stream.read() and 0xff
    }

    // Unsigned 2 bytes
    @Throws(IOException::class)
    private fun readUnsignedShort(stream: InputStream): Int {
        val b1 = readUnsignedByte(stream)
        val b2 = readUnsignedByte(stream)
        return b2 shl 8 or b1 and 0xffff
    }

    // Signed 16 bits
    @Throws(IOException::class)
    private fun readShort(stream: InputStream): Int {
        val b1 = readUnsignedByte(stream)
        val b2 = readUnsignedByte(stream)
        return b2 shl 8 or b1
    }

    // Unsigned 16 bits
    @Throws(IOException::class)
    private fun readWord(stream: InputStream): Int {
        return readUnsignedShort(stream)
    }

    // Unsigned 4 bytes
    @Throws(IOException::class)
    private fun readUnsignedInt(stream: InputStream): Long {
        val b1 = readUnsignedByte(stream)
        val b2 = readUnsignedByte(stream)
        val b3 = readUnsignedByte(stream)
        val b4 = readUnsignedByte(stream)
        val l = b4 shl 24 or b3 shl 16 or b2 shl 8 or b1.toLong()
        return l and 0xffffffff.toInt()
    }

    // Signed 4 bytes
    @Throws(IOException::class)
    private fun readInt(stream: InputStream): Int {
        val b1 = readUnsignedByte(stream)
        val b2 = readUnsignedByte(stream)
        val b3 = readUnsignedByte(stream)
        val b4 = readUnsignedByte(stream)
        return b4 shl 24 or b3 shl 16 or b2 shl 8 or b1
    }

    // Unsigned 4 bytes
    @Throws(IOException::class)
    private fun readDWord(stream: InputStream): Long {
        return readUnsignedInt(stream)
    }

    // 32 bit signed value
    @Throws(IOException::class)
    private fun readLong(stream: InputStream): Int {
        return readInt(stream)
    }

    companion object {
        // BMP Image types
        private val VERSION_2_1_BIT = 0
        private val VERSION_2_4_BIT = 1
        private val VERSION_2_8_BIT = 2
        private val VERSION_2_24_BIT = 3

        private val VERSION_3_1_BIT = 4
        private val VERSION_3_4_BIT = 5
        private val VERSION_3_8_BIT = 6
        private val VERSION_3_24_BIT = 7

        private val VERSION_3_NT_16_BIT = 8
        private val VERSION_3_NT_32_BIT = 9

        private val VERSION_4_1_BIT = 10
        private val VERSION_4_4_BIT = 11
        private val VERSION_4_8_BIT = 12
        private val VERSION_4_16_BIT = 13
        private val VERSION_4_24_BIT = 14
        private val VERSION_4_32_BIT = 15

        // Color space types
        private val LCS_CALIBRATED_RGB = 0
        private val LCS_sRGB = 1
        private val LCS_CMYK = 2

        // Compression Types
        private val BI_RGB = 0
        private val BI_RLE8 = 1
        private val BI_RLE4 = 2
        private val BI_BITFIELDS = 3

        /** Reads a BMP from an url.
         * @param url the url
         * *
         * @throws IOException on error
         * *
         * @return the image
         */
        @Throws(IOException::class)
        fun getImage(url: URL): Image {
            var `is`: InputStream? = null
            try {
                `is` = url.openStream()
                val img = getImage(`is`)
                img.url = url
                return img
            } finally {
                if (`is` != null) {
                    `is`.close()
                }
            }
        }

        /** Reads a BMP from a stream. The stream is not closed.
         * The BMP may not have a header and be considered as a plain DIB.
         * @param is the stream
         * *
         * @param noHeader true to process a plain DIB
         * *
         * @param size the size of the DIB. Not used for a BMP
         * *
         * @throws IOException on error
         * *
         * @return the image
         */
        @Throws(IOException::class)
        @JvmOverloads fun getImage(`is`: InputStream, noHeader: Boolean = false, size: Int = 0): Image {
            val bmp = BmpImage(`is`, noHeader, size)
            try {
                val img = bmp.image
                img.setDpi((bmp.xPelsPerMeter * 0.0254 + 0.5).toInt(), (bmp.yPelsPerMeter * 0.0254 + 0.5).toInt())
                img.setOriginalType(Image.ORIGINAL_BMP)
                return img
            } catch (be: BadElementException) {
                throw ExceptionConverter(be)
            }

        }

        /** Reads a BMP from a file.
         * @param file the file
         * *
         * @throws IOException on error
         * *
         * @return the image
         */
        @Throws(IOException::class)
        fun getImage(file: String): Image {
            return getImage(Utilities.toURL(file))
        }

        /** Reads a BMP from a byte array.
         * @param data the byte array
         * *
         * @throws IOException on error
         * *
         * @return the image
         */
        @Throws(IOException::class)
        fun getImage(data: ByteArray): Image {
            val `is` = ByteArrayInputStream(data)
            val img = getImage(`is`)
            img.originalData = data
            return img
        }
    }
}
/** Reads a BMP from a stream. The stream is not closed.
 * @param is the stream
 * *
 * @throws IOException on error
 * *
 * @return the image
 */
