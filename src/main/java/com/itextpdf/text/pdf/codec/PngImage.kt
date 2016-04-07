/*
 * $Id: af5f976075e3d212992b11d62884023a0aec3704 $
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
 * This code is based on a series of source files originally released
 * by SUN in the context of the JAI project. The original code was released 
 * under the BSD license in a specific wording. In a mail dating from
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

import com.itextpdf.text.pdf.ICC_Profile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.ImgRaw
import com.itextpdf.text.Utilities
import com.itextpdf.text.pdf.ByteBuffer
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfLiteral
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfString

/** Reads a PNG image. All types of PNG can be read.
 *
 *
 * It is based in part in the JAI codec.

 * @author  Paulo Soares
 */
class PngImage
/** Creates a new instance of PngImage  */
internal constructor(internal var `is`:

                     InputStream) {
    internal var dataStream: DataInputStream
    internal var width: Int = 0
    internal var height: Int = 0
    internal var bitDepth: Int = 0
    internal var colorType: Int = 0
    internal var compressionMethod: Int = 0
    internal var filterMethod: Int = 0
    internal var interlaceMethod: Int = 0
    internal var additional = PdfDictionary()
    internal var image: ByteArray? = null
    internal var smask: ByteArray
    internal var trans: ByteArray? = null
    internal var idat = NewByteArrayOutputStream()
    internal var dpiX: Int = 0
    internal var dpiY: Int = 0
    internal var XYRatio: Float = 0.toFloat()
    internal var genBWMask: Boolean = false
    internal var palShades: Boolean = false
    internal var transRedGray = -1
    internal var transGreen = -1
    internal var transBlue = -1
    internal var inputBands: Int = 0
    internal var bytesPerPixel: Int = 0 // number of bytes per input pixel
    internal var colorTable: ByteArray
    internal var gamma = 1f
    internal var hasCHRM = false
    internal var xW: Float = 0.toFloat()
    internal var yW: Float = 0.toFloat()
    internal var xR: Float = 0.toFloat()
    internal var yR: Float = 0.toFloat()
    internal var xG: Float = 0.toFloat()
    internal var yG: Float = 0.toFloat()
    internal var xB: Float = 0.toFloat()
    internal var yB: Float = 0.toFloat()
    internal var intent: PdfName? = null
    internal var icc_profile: ICC_Profile? = null

    internal fun checkMarker(s: String): Boolean {
        if (s.length != 4)
            return false
        for (k in 0..3) {
            val c = s[k]
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z'))
                return false
        }
        return true
    }

    @Throws(IOException::class)
    internal fun readPng() {
        for (i in PNGID.indices) {
            if (PNGID[i] != `is`.read()) {
                throw IOException(MessageLocalization.getComposedMessage("file.is.not.a.valid.png"))
            }
        }
        val buffer = ByteArray(TRANSFERSIZE)
        while (true) {
            var len = getInt(`is`)
            val marker = getString(`is`)
            if (len < 0 || !checkMarker(marker))
                throw IOException(MessageLocalization.getComposedMessage("corrupted.png.file"))
            if (IDAT == marker) {
                var size: Int
                while (len != 0) {
                    size = `is`.read(buffer, 0, Math.min(len, TRANSFERSIZE))
                    if (size < 0)
                        return
                    idat.write(buffer, 0, size)
                    len -= size
                }
            } else if (tRNS == marker) {
                when (colorType) {
                    0 -> if (len >= 2) {
                        len -= 2
                        val gray = getWord(`is`)
                        if (bitDepth == 16)
                            transRedGray = gray
                        else
                            additional.put(PdfName.MASK, PdfLiteral("[$gray $gray]"))
                    }
                    2 -> if (len >= 6) {
                        len -= 6
                        val red = getWord(`is`)
                        val green = getWord(`is`)
                        val blue = getWord(`is`)
                        if (bitDepth == 16) {
                            transRedGray = red
                            transGreen = green
                            transBlue = blue
                        } else
                            additional.put(PdfName.MASK, PdfLiteral("[$red $red $green $green $blue $blue]"))
                    }
                    3 -> if (len > 0) {
                        trans = ByteArray(len)
                        for (k in 0..len - 1)
                            trans[k] = `is`.read().toByte()
                        len = 0
                    }
                }
                Utilities.skip(`is`, len)
            } else if (IHDR == marker) {
                width = getInt(`is`)
                height = getInt(`is`)

                bitDepth = `is`.read()
                colorType = `is`.read()
                compressionMethod = `is`.read()
                filterMethod = `is`.read()
                interlaceMethod = `is`.read()
            } else if (PLTE == marker) {
                if (colorType == 3) {
                    val colorspace = PdfArray()
                    colorspace.add(PdfName.INDEXED)
                    colorspace.add(colorspace)
                    colorspace.add(PdfNumber(len / 3 - 1))
                    val colortable = ByteBuffer()
                    while (len-- > 0) {
                        colortable.append_i(`is`.read())
                    }
                    colorspace.add(PdfString(colorTable = colortable.toByteArray()))
                    additional.put(PdfName.COLORSPACE, colorspace)
                } else {
                    Utilities.skip(`is`, len)
                }
            } else if (pHYs == marker) {
                val dx = getInt(`is`)
                val dy = getInt(`is`)
                val unit = `is`.read()
                if (unit == 1) {
                    dpiX = (dx * 0.0254f + 0.5f).toInt()
                    dpiY = (dy * 0.0254f + 0.5f).toInt()
                } else {
                    if (dy != 0)
                        XYRatio = dx.toFloat() / dy.toFloat()
                }
            } else if (cHRM == marker) {
                xW = getInt(`is`) / 100000f
                yW = getInt(`is`) / 100000f
                xR = getInt(`is`) / 100000f
                yR = getInt(`is`) / 100000f
                xG = getInt(`is`) / 100000f
                yG = getInt(`is`) / 100000f
                xB = getInt(`is`) / 100000f
                yB = getInt(`is`) / 100000f
                hasCHRM = !(Math.abs(xW) < 0.0001f || Math.abs(yW) < 0.0001f || Math.abs(xR) < 0.0001f || Math.abs(yR) < 0.0001f || Math.abs(xG) < 0.0001f || Math.abs(yG) < 0.0001f || Math.abs(xB) < 0.0001f || Math.abs(yB) < 0.0001f)
            } else if (sRGB == marker) {
                val ri = `is`.read()
                intent = intents[ri]
                gamma = 2.2f
                xW = 0.3127f
                yW = 0.329f
                xR = 0.64f
                yR = 0.33f
                xG = 0.3f
                yG = 0.6f
                xB = 0.15f
                yB = 0.06f
                hasCHRM = true
            } else if (gAMA == marker) {
                val gm = getInt(`is`)
                if (gm != 0) {
                    gamma = 100000f / gm
                    if (!hasCHRM) {
                        xW = 0.3127f
                        yW = 0.329f
                        xR = 0.64f
                        yR = 0.33f
                        xG = 0.3f
                        yG = 0.6f
                        xB = 0.15f
                        yB = 0.06f
                        hasCHRM = true
                    }
                }
            } else if (iCCP == marker) {
                do {
                    --len
                } while (`is`.read() != 0)
                `is`.read()
                --len
                var icccom: ByteArray? = ByteArray(len)
                var p = 0
                while (len > 0) {
                    val r = `is`.read(icccom, p, len)
                    if (r < 0)
                        throw IOException(MessageLocalization.getComposedMessage("premature.end.of.file"))
                    p += r
                    len -= r
                }
                val iccp = PdfReader.FlateDecode(icccom, true)
                icccom = null
                try {
                    icc_profile = ICC_Profile.getInstance(iccp)
                } catch (e: RuntimeException) {
                    icc_profile = null
                }

            } else if (IEND == marker) {
                break
            } else {
                Utilities.skip(`is`, len)
            }
            Utilities.skip(`is`, 4)
        }
    }

    internal //YA+YB+YC;
    val colorspace: PdfObject
        get() {
            if (icc_profile != null) {
                if (colorType and 2 == 0)
                    return PdfName.DEVICEGRAY
                else
                    return PdfName.DEVICERGB
            }
            if (gamma == 1f && !hasCHRM) {
                if (colorType and 2 == 0)
                    return PdfName.DEVICEGRAY
                else
                    return PdfName.DEVICERGB
            } else {
                val array = PdfArray()
                val dic = PdfDictionary()
                if (colorType and 2 == 0) {
                    if (gamma == 1f)
                        return PdfName.DEVICEGRAY
                    array.add(PdfName.CALGRAY)
                    dic.put(PdfName.GAMMA, PdfNumber(gamma))
                    dic.put(PdfName.WHITEPOINT, PdfLiteral("[1 1 1]"))
                    array.add(dic)
                } else {
                    var wp: PdfObject = PdfLiteral("[1 1 1]")
                    array.add(PdfName.CALRGB)
                    if (gamma != 1f) {
                        val gm = PdfArray()
                        val n = PdfNumber(gamma)
                        gm.add(n)
                        gm.add(n)
                        gm.add(n)
                        dic.put(PdfName.GAMMA, gm)
                    }
                    if (hasCHRM) {
                        val z = yW * ((xG - xB) * yR - (xR - xB) * yG + (xR - xG) * yB)
                        val YA = yR * ((xG - xB) * yW - (xW - xB) * yG + (xW - xG) * yB) / z
                        val XA = YA * xR / yR
                        val ZA = YA * ((1 - xR) / yR - 1)
                        val YB = -yG * ((xR - xB) * yW - (xW - xB) * yR + (xW - xR) * yB) / z
                        val XB = YB * xG / yG
                        val ZB = YB * ((1 - xG) / yG - 1)
                        val YC = yB * ((xR - xG) * yW - (xW - xG) * yW + (xW - xR) * yG) / z
                        val XC = YC * xB / yB
                        val ZC = YC * ((1 - xB) / yB - 1)
                        val XW = XA + XB + XC
                        val YW = 1f
                        val ZW = ZA + ZB + ZC
                        val wpa = PdfArray()
                        wpa.add(PdfNumber(XW))
                        wpa.add(PdfNumber(YW))
                        wpa.add(PdfNumber(ZW))
                        wp = wpa
                        val matrix = PdfArray()
                        matrix.add(PdfNumber(XA))
                        matrix.add(PdfNumber(YA))
                        matrix.add(PdfNumber(ZA))
                        matrix.add(PdfNumber(XB))
                        matrix.add(PdfNumber(YB))
                        matrix.add(PdfNumber(ZB))
                        matrix.add(PdfNumber(XC))
                        matrix.add(PdfNumber(YC))
                        matrix.add(PdfNumber(ZC))
                        dic.put(PdfName.MATRIX, matrix)
                    }
                    dic.put(PdfName.WHITEPOINT, wp)
                    array.add(dic)
                }
                return array
            }
        }

    @Throws(IOException::class)
    internal fun getImage(): Image {
        readPng()
        try {
            var pal0 = 0
            var palIdx = 0
            palShades = false
            if (trans != null) {
                for (k in trans!!.indices) {
                    val n = trans!![k] and 0xff
                    if (n == 0) {
                        ++pal0
                        palIdx = k
                    }
                    if (n != 0 && n != 255) {
                        palShades = true
                        break
                    }
                }
            }
            if (colorType and 4 != 0)
                palShades = true
            genBWMask = !palShades && (pal0 > 1 || transRedGray >= 0)
            if (!palShades && !genBWMask && pal0 == 1) {
                additional.put(PdfName.MASK, PdfLiteral("[$palIdx $palIdx]"))
            }
            val needDecode = interlaceMethod == 1 || bitDepth == 16 || colorType and 4 != 0 || palShades || genBWMask
            when (colorType) {
                0 -> inputBands = 1
                2 -> inputBands = 3
                3 -> inputBands = 1
                4 -> inputBands = 2
                6 -> inputBands = 4
            }
            if (needDecode)
                decodeIdat()
            var components = inputBands
            if (colorType and 4 != 0)
                --components
            var bpc = bitDepth
            if (bpc == 16)
                bpc = 8
            val img: Image
            if (image != null) {
                if (colorType == 3)
                    img = ImgRaw(width, height, components, bpc, image)
                else
                    img = Image.getInstance(width, height, components, bpc, image)
            } else {
                img = ImgRaw(width, height, components, bpc, idat.toByteArray())
                img.setDeflated(true)
                val decodeparms = PdfDictionary()
                decodeparms.put(PdfName.BITSPERCOMPONENT, PdfNumber(bitDepth))
                decodeparms.put(PdfName.PREDICTOR, PdfNumber(15))
                decodeparms.put(PdfName.COLUMNS, PdfNumber(width))
                decodeparms.put(PdfName.COLORS, PdfNumber(if (colorType == 3 || colorType and 2 == 0) 1 else 3))
                additional.put(PdfName.DECODEPARMS, decodeparms)
            }
            if (additional.get(PdfName.COLORSPACE) == null)
                additional.put(PdfName.COLORSPACE, colorspace)
            if (intent != null)
                additional.put(PdfName.INTENT, intent)
            if (additional.size() > 0)
                img.additional = additional
            if (icc_profile != null)
                img.tagICC(icc_profile)
            if (palShades) {
                val im2 = Image.getInstance(width, height, 1, 8, smask)
                im2.makeMask()
                img.imageMask = im2
            }
            if (genBWMask) {
                val im2 = Image.getInstance(width, height, 1, 1, smask)
                im2.makeMask()
                img.imageMask = im2
            }
            img.setDpi(dpiX, dpiY)
            img.xyRatio = XYRatio
            img.originalType = Image.ORIGINAL_PNG
            return img
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    internal fun decodeIdat() {
        var nbitDepth = bitDepth
        if (nbitDepth == 16)
            nbitDepth = 8
        var size = -1
        bytesPerPixel = if (bitDepth == 16) 2 else 1
        when (colorType) {
            0 -> size = (nbitDepth * width + 7) / 8 * height
            2 -> {
                size = width * 3 * height
                bytesPerPixel *= 3
            }
            3 -> {
                if (interlaceMethod == 1)
                    size = (nbitDepth * width + 7) / 8 * height
                bytesPerPixel = 1
            }
            4 -> {
                size = width * height
                bytesPerPixel *= 2
            }
            6 -> {
                size = width * 3 * height
                bytesPerPixel *= 4
            }
        }
        if (size >= 0)
            image = ByteArray(size)
        if (palShades)
            smask = ByteArray(width * height)
        else if (genBWMask)
            smask = ByteArray((width + 7) / 8 * height)
        val bai = ByteArrayInputStream(idat.buf, 0, idat.size())
        val infStream = InflaterInputStream(bai, Inflater())
        dataStream = DataInputStream(infStream)

        if (interlaceMethod != 1) {
            decodePass(0, 0, 1, 1, width, height)
        } else {
            decodePass(0, 0, 8, 8, (width + 7) / 8, (height + 7) / 8)
            decodePass(4, 0, 8, 8, (width + 3) / 8, (height + 7) / 8)
            decodePass(0, 4, 4, 8, (width + 3) / 4, (height + 3) / 8)
            decodePass(2, 0, 4, 4, (width + 1) / 4, (height + 3) / 4)
            decodePass(0, 2, 2, 4, (width + 1) / 2, (height + 1) / 4)
            decodePass(1, 0, 2, 2, width / 2, (height + 1) / 2)
            decodePass(0, 1, 1, 2, width, height / 2)
        }

    }

    internal fun decodePass(xOffset: Int, yOffset: Int,
                            xStep: Int, yStep: Int,
                            passWidth: Int, passHeight: Int) {
        if (passWidth == 0 || passHeight == 0) {
            return
        }

        val bytesPerRow = (inputBands * passWidth * bitDepth + 7) / 8
        var curr = ByteArray(bytesPerRow)
        var prior = ByteArray(bytesPerRow)

        // Decode the (sub)image row-by-row
        var srcY: Int
        var dstY: Int
        srcY = 0
        dstY = yOffset
        while (srcY < passHeight) {
            // Read the filter type byte and a row of data
            var filter = 0
            try {
                filter = dataStream.read()
                dataStream.readFully(curr, 0, bytesPerRow)
            } catch (e: Exception) {
                // empty on purpose
            }

            when (filter) {
                PNG_FILTER_NONE -> {
                }
                PNG_FILTER_SUB -> decodeSubFilter(curr, bytesPerRow, bytesPerPixel)
                PNG_FILTER_UP -> decodeUpFilter(curr, prior, bytesPerRow)
                PNG_FILTER_AVERAGE -> decodeAverageFilter(curr, prior, bytesPerRow, bytesPerPixel)
                PNG_FILTER_PAETH -> decodePaethFilter(curr, prior, bytesPerRow, bytesPerPixel)
                else -> // Error -- uknown filter type
                    throw RuntimeException(MessageLocalization.getComposedMessage("png.filter.unknown"))
            }

            processPixels(curr, xOffset, xStep, dstY, passWidth)

            // Swap curr and prior
            val tmp = prior
            prior = curr
            curr = tmp
            srcY++
            dstY += yStep
        }
    }

    internal fun processPixels(curr: ByteArray, xOffset: Int, step: Int, y: Int, width: Int) {
        var srcX: Int
        var dstX: Int

        val out = getPixel(curr)
        var sizes = 0
        when (colorType) {
            0, 3, 4 -> sizes = 1
            2, 6 -> sizes = 3
        }
        if (image != null) {
            dstX = xOffset
            val yStride = (sizes * this.width * (if (bitDepth == 16) 8 else bitDepth) + 7) / 8
            srcX = 0
            while (srcX < width) {
                setPixel(image, out, inputBands * srcX, sizes, dstX, y, bitDepth, yStride)
                dstX += step
                srcX++
            }
        }
        if (palShades) {
            if (colorType and 4 != 0) {
                if (bitDepth == 16) {
                    for (k in 0..width - 1)
                        out[k * inputBands + sizes] = out[k * inputBands + sizes] ushr 8
                }
                val yStride = this.width
                dstX = xOffset
                srcX = 0
                while (srcX < width) {
                    setPixel(smask, out, inputBands * srcX + sizes, 1, dstX, y, 8, yStride)
                    dstX += step
                    srcX++
                }
            } else {
                //colorType 3
                val yStride = this.width
                val v = IntArray(1)
                dstX = xOffset
                srcX = 0
                while (srcX < width) {
                    val idx = out[srcX]
                    if (idx < trans!!.size)
                        v[0] = trans!![idx].toInt()
                    else
                        v[0] = 255 // Patrick Valsecchi
                    setPixel(smask, v, 0, 1, dstX, y, 8, yStride)
                    dstX += step
                    srcX++
                }
            }
        } else if (genBWMask) {
            when (colorType) {
                3 -> {
                    val yStride = (this.width + 7) / 8
                    val v = IntArray(1)
                    dstX = xOffset
                    srcX = 0
                    while (srcX < width) {
                        val idx = out[srcX]
                        v[0] = if (idx < trans!!.size && trans!![idx].toInt() == 0) 1 else 0
                        setPixel(smask, v, 0, 1, dstX, y, 1, yStride)
                        dstX += step
                        srcX++
                    }
                }
                0 -> {
                    val yStride = (this.width + 7) / 8
                    val v = IntArray(1)
                    dstX = xOffset
                    srcX = 0
                    while (srcX < width) {
                        val g = out[srcX]
                        v[0] = if (g == transRedGray) 1 else 0
                        setPixel(smask, v, 0, 1, dstX, y, 1, yStride)
                        dstX += step
                        srcX++
                    }
                }
                2 -> {
                    val yStride = (this.width + 7) / 8
                    val v = IntArray(1)
                    dstX = xOffset
                    srcX = 0
                    while (srcX < width) {
                        val markRed = inputBands * srcX
                        v[0] = if (out[markRed] == transRedGray && out[markRed + 1] == transGreen
                                && out[markRed + 2] == transBlue)
                            1
                        else
                            0
                        setPixel(smask, v, 0, 1, dstX, y, 1, yStride)
                        dstX += step
                        srcX++
                    }
                }
            }
        }
    }

    internal fun getPixel(curr: ByteArray): IntArray {
        when (bitDepth) {
            8 -> {
                val out = IntArray(curr.size)
                for (k in out.indices)
                    out[k] = curr[k] and 0xff
                return out
            }
            16 -> {
                val out = IntArray(curr.size / 2)
                for (k in out.indices)
                    out[k] = (curr[k * 2] and 0xff shl 8) + (curr[k * 2 + 1] and 0xff)
                return out
            }
            else -> {
                val out = IntArray(curr.size * 8 / bitDepth)
                var idx = 0
                val passes = 8 / bitDepth
                val mask = (1 shl bitDepth) - 1
                for (k in curr.indices) {
                    for (j in passes - 1 downTo 0) {
                        out[idx++] = curr[k].ushr(bitDepth * j) and mask
                    }
                }
                return out
            }
        }
    }

    internal class NewByteArrayOutputStream : ByteArrayOutputStream() {
        val buf: ByteArray
            get() = buf
    }

    companion object {
        /** Some PNG specific values.  */
        val PNGID = intArrayOf(137, 80, 78, 71, 13, 10, 26, 10)

        /** A PNG marker.  */
        val IHDR = "IHDR"

        /** A PNG marker.  */
        val PLTE = "PLTE"

        /** A PNG marker.  */
        val IDAT = "IDAT"

        /** A PNG marker.  */
        val IEND = "IEND"

        /** A PNG marker.  */
        val tRNS = "tRNS"

        /** A PNG marker.  */
        val pHYs = "pHYs"

        /** A PNG marker.  */
        val gAMA = "gAMA"

        /** A PNG marker.  */
        val cHRM = "cHRM"

        /** A PNG marker.  */
        val sRGB = "sRGB"

        /** A PNG marker.  */
        val iCCP = "iCCP"

        private val TRANSFERSIZE = 4096
        private val PNG_FILTER_NONE = 0
        private val PNG_FILTER_SUB = 1
        private val PNG_FILTER_UP = 2
        private val PNG_FILTER_AVERAGE = 3
        private val PNG_FILTER_PAETH = 4
        private val intents = arrayOf(PdfName.PERCEPTUAL, PdfName.RELATIVECOLORIMETRIC, PdfName.SATURATION, PdfName.ABSOLUTECOLORIMETRIC)

        /** Reads a PNG from an url.
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

        /** Reads a PNG from a stream.
         * @param is the stream
         * *
         * @throws IOException on error
         * *
         * @return the image
         */
        @Throws(IOException::class)
        fun getImage(`is`: InputStream): Image {
            val png = PngImage(`is`)
            return png.getImage()
        }

        /** Reads a PNG from a file.
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

        /** Reads a PNG from a byte array.
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

        internal fun getPixel(image: ByteArray, x: Int, y: Int, bitDepth: Int, bytesPerRow: Int): Int {
            if (bitDepth == 8) {
                val pos = bytesPerRow * y + x
                return image[pos] and 0xff
            } else {
                val pos = bytesPerRow * y + x / (8 / bitDepth)
                val v = image[pos] shr 8 - bitDepth * (x % (8 / bitDepth)) - bitDepth
                return v and (1 shl bitDepth) - 1
            }
        }

        internal fun setPixel(image: ByteArray, data: IntArray, offset: Int, size: Int, x: Int, y: Int, bitDepth: Int, bytesPerRow: Int) {
            if (bitDepth == 8) {
                val pos = bytesPerRow * y + size * x
                for (k in 0..size - 1)
                    image[pos + k] = data[k + offset].toByte()
            } else if (bitDepth == 16) {
                val pos = bytesPerRow * y + size * x
                for (k in 0..size - 1)
                    image[pos + k] = data[k + offset].ushr(8).toByte()
            } else {
                val pos = bytesPerRow * y + x / (8 / bitDepth)
                val v = data[offset] shl 8 - bitDepth * (x % (8 / bitDepth)) - bitDepth
                image[pos] = image[pos] or v.toByte()
            }
        }

        private fun decodeSubFilter(curr: ByteArray, count: Int, bpp: Int) {
            for (i in bpp..count - 1) {
                val `val`: Int

                `val` = curr[i] and 0xff
                `val` += curr[i - bpp] and 0xff

                curr[i] = `val`.toByte()
            }
        }

        private fun decodeUpFilter(curr: ByteArray, prev: ByteArray,
                                   count: Int) {
            for (i in 0..count - 1) {
                val raw = curr[i] and 0xff
                val prior = prev[i] and 0xff

                curr[i] = (raw + prior).toByte()
            }
        }

        private fun decodeAverageFilter(curr: ByteArray, prev: ByteArray,
                                        count: Int, bpp: Int) {
            var raw: Int
            var priorPixel: Int
            var priorRow: Int

            for (i in 0..bpp - 1) {
                raw = curr[i] and 0xff
                priorRow = prev[i] and 0xff

                curr[i] = (raw + priorRow / 2).toByte()
            }

            for (i in bpp..count - 1) {
                raw = curr[i] and 0xff
                priorPixel = curr[i - bpp] and 0xff
                priorRow = prev[i] and 0xff

                curr[i] = (raw + (priorPixel + priorRow) / 2).toByte()
            }
        }

        private fun paethPredictor(a: Int, b: Int, c: Int): Int {
            val p = a + b - c
            val pa = Math.abs(p - a)
            val pb = Math.abs(p - b)
            val pc = Math.abs(p - c)

            if (pa <= pb && pa <= pc) {
                return a
            } else if (pb <= pc) {
                return b
            } else {
                return c
            }
        }

        private fun decodePaethFilter(curr: ByteArray, prev: ByteArray,
                                      count: Int, bpp: Int) {
            var raw: Int
            var priorPixel: Int
            var priorRow: Int
            var priorRowPixel: Int

            for (i in 0..bpp - 1) {
                raw = curr[i] and 0xff
                priorRow = prev[i] and 0xff

                curr[i] = (raw + priorRow).toByte()
            }

            for (i in bpp..count - 1) {
                raw = curr[i] and 0xff
                priorPixel = curr[i - bpp] and 0xff
                priorRow = prev[i] and 0xff
                priorRowPixel = prev[i - bpp] and 0xff

                curr[i] = (raw + paethPredictor(priorPixel,
                        priorRow,
                        priorRowPixel)).toByte()
            }
        }

        /**
         * Gets an int from an InputStream.

         * @param        is      an InputStream
         * *
         * @return        the value of an int
         */

        @Throws(IOException::class)
        fun getInt(`is`: InputStream): Int {
            return (`is`.read() shl 24) + (`is`.read() shl 16) + (`is`.read() shl 8) + `is`.read()
        }

        /**
         * Gets a word from an InputStream.

         * @param        is      an InputStream
         * *
         * @return        the value of an int
         */

        @Throws(IOException::class)
        fun getWord(`is`: InputStream): Int {
            return (`is`.read() shl 8) + `is`.read()
        }

        /**
         * Gets a String from an InputStream.

         * @param        is      an InputStream
         * *
         * @return        the value of an int
         */

        @Throws(IOException::class)
        fun getString(`is`: InputStream): String {
            val buf = StringBuffer()
            for (i in 0..3) {
                buf.append(`is`.read().toChar())
            }
            return buf.toString()
        }
    }

}
