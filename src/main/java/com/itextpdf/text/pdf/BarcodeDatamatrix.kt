/*
 * $Id: 31a358f64342109524f1bf06a6a041113c1f1cb0 $
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

import com.itextpdf.text.BadElementException
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.codec.CCITTG4Encoder

import java.io.UnsupportedEncodingException
import java.util.Arrays
import java.util.Hashtable

/**
 * A DataMatrix 2D barcode generator.
 */
class BarcodeDatamatrix {
    private var extOut: Int = 0
    private var place: ShortArray? = null
    /**
     * Gets the generated image. The image is represented as a stream of bytes, each byte representing
     * 8 pixels, 0 for white and 1 for black, with the high-order bit of each byte first. Each row
     * is aligned at byte boundaries. The dimensions of the image are defined by height and width
     * plus 2 * ws.
     * @return the generated image
     */
    var image: ByteArray? = null
        private set
    /**
     * Gets the height of the barcode. Will contain the real height used after a successful call
     * to generate(). This height doesn't include the whitespace border, if any.
     * @return the height of the barcode
     */
    /**
     * Sets the height of the barcode. If the height is zero it will be calculated. This height doesn't include the whitespace border, if any.
     *
     *
     * The allowed dimensions are (height, width):
     *
     *
     * 10, 10
     * 12, 12
     * 8, 18
     * 14, 14
     * 8, 32
     * 16, 16
     * 12, 26
     * 18, 18
     * 20, 20
     * 12, 36
     * 22, 22
     * 16, 36
     * 24, 24
     * 26, 26
     * 16, 48
     * 32, 32
     * 36, 36
     * 40, 40
     * 44, 44
     * 48, 48
     * 52, 52
     * 64, 64
     * 72, 72
     * 80, 80
     * 88, 88
     * 96, 96
     * 104, 104
     * 120, 120
     * 132, 132
     * 144, 144
     * @param height the height of the barcode
     */
    var height: Int = 0
    /**
     * Gets the width of the barcode. Will contain the real width used after a successful call
     * to generate(). This width doesn't include the whitespace border, if any.
     * @return the width of the barcode
     */
    /**
     * Sets the width of the barcode. If the width is zero it will be calculated. This width doesn't include the whitespace border, if any.
     *
     *
     * The allowed dimensions are (height, width):
     *
     *
     * 10, 10
     * 12, 12
     * 8, 18
     * 14, 14
     * 8, 32
     * 16, 16
     * 12, 26
     * 18, 18
     * 20, 20
     * 12, 36
     * 22, 22
     * 16, 36
     * 24, 24
     * 26, 26
     * 16, 48
     * 32, 32
     * 36, 36
     * 40, 40
     * 44, 44
     * 48, 48
     * 52, 52
     * 64, 64
     * 72, 72
     * 80, 80
     * 88, 88
     * 96, 96
     * 104, 104
     * 120, 120
     * 132, 132
     * 144, 144
     * @param width the width of the barcode
     */
    var width: Int = 0
    /**
     * Gets the whitespace border around the barcode.
     * @return the whitespace border around the barcode
     */
    /**
     * Sets the whitespace border around the barcode.
     * @param ws the whitespace border around the barcode
     */
    var ws: Int = 0
    /**
     * Gets the barcode options.
     * @return the barcode options
     */
    /**
     * Sets the options for the barcode generation. The options can be:
     *
     *
     * One of:
     * DM_AUTO - the best encodation will be used
     * DM_ASCII - ASCII encodation
     * DM_C40 - C40 encodation
     * DM_TEXT - TEXT encodation
     * DM_B256 - binary encodation
     * DM_X21 - X21 encodation
     * DM_EDIFACT - EDIFACT encodation
     * DM_RAW - no encodation. The bytes provided are already encoded and will be added directly to the barcode, using padding if needed. It assumes that the encodation state is left at ASCII after the last byte.
     *
     *
     * One of:
     * DM_EXTENSION - allows extensions to be embedded at the start of the text:
     *
     *
     * exxxxxx - ECI number xxxxxx
     * m5 - macro 5
     * m6 - macro 6
     * f - FNC1
     * saabbccccc - Structured Append, aa symbol position (1-16), bb total number of symbols (2-16), ccccc file identification (0-64515)
     * p - Reader programming
     * . - extension terminator
     *
     *
     * Example for a structured append, symbol 2 of 6, with FNC1 and ECI 000005. The actual text is "Hello".
     *
     *
     * s020600075fe000005.Hello
     *
     *
     * One of:
     * DM_TEST - doesn't generate the image but returns all the other information.
     * @param options the barcode options
     */
    var options: Int = 0

    private fun setBit(x: Int, y: Int, xByte: Int) {
        image[y * xByte + x / 8] = image[y * xByte + x / 8] or (128 shr (x and 7)).toByte()
    }

    private fun draw(data: ByteArray, dataSize: Int, dm: DmParams) {
        var i: Int
        var j: Int
        var p: Int
        var x: Int
        var y: Int
        var xs: Int
        var ys: Int
        var z: Int
        val xByte = (dm.width + ws * 2 + 7) / 8
        Arrays.fill(image, 0.toByte())
        //alignment patterns
        //dotted horizontal line
        i = ws
        while (i < dm.height + ws) {
            j = ws
            while (j < dm.width + ws) {
                setBit(j, i, xByte)
                j += 2
            }
            i += dm.heightSection
        }
        //solid horizontal line
        i = dm.heightSection - 1 + ws
        while (i < dm.height + ws) {
            j = ws
            while (j < dm.width + ws) {
                setBit(j, i, xByte)
                ++j
            }
            i += dm.heightSection
        }
        //solid vertical line
        i = ws
        while (i < dm.width + ws) {
            j = ws
            while (j < dm.height + ws) {
                setBit(i, j, xByte)
                ++j
            }
            i += dm.widthSection
        }
        //dotted vertical line
        i = dm.widthSection - 1 + ws
        while (i < dm.width + ws) {
            j = 1 + ws
            while (j < dm.height + ws) {
                setBit(i, j, xByte)
                j += 2
            }
            i += dm.widthSection
        }
        p = 0
        ys = 0
        while (ys < dm.height) {
            y = 1
            while (y < dm.heightSection - 1) {
                xs = 0
                while (xs < dm.width) {
                    x = 1
                    while (x < dm.widthSection - 1) {
                        z = place!![p++].toInt()
                        if (z == 1 || z > 1 && data[z / 8 - 1].toInt() and 0xff and 128 shr z % 8 != 0)
                            setBit(x + xs + ws, y + ys + ws, xByte)
                        ++x
                    }
                    xs += dm.widthSection
                }
                ++y
            }
            ys += dm.heightSection
        }
    }

    private fun processExtensions(text: ByteArray, textOffset: Int, textSize: Int, data: ByteArray): Int {
        var order: Int
        var ptrIn: Int
        var ptrOut: Int
        var eci: Int
        var fn: Int
        var ft: Int
        var fi: Int
        var c: Int
        if (options and DM_EXTENSION == 0)
            return 0
        order = 0
        ptrIn = 0
        ptrOut = 0
        while (ptrIn < textSize) {
            if (order > 20)
                return -1
            c = text[textOffset + ptrIn++] and 0xff
            ++order
            when (c) {
                '.' -> {
                    extOut = ptrIn
                    return ptrOut
                }
                'e' -> {
                    if (ptrIn + 6 > textSize)
                        return -1
                    eci = getNumber(text, textOffset + ptrIn, 6)
                    if (eci < 0)
                        return -1
                    ptrIn += 6
                    data[ptrOut++] = 241.toByte()
                    if (eci < 127)
                        data[ptrOut++] = (eci + 1).toByte()
                    else if (eci < 16383) {
                        data[ptrOut++] = ((eci - 127) / 254 + 128).toByte()
                        data[ptrOut++] = ((eci - 127) % 254 + 1).toByte()
                    } else {
                        data[ptrOut++] = ((eci - 16383) / 64516 + 192).toByte()
                        data[ptrOut++] = ((eci - 16383) / 254 % 254 + 1).toByte()
                        data[ptrOut++] = ((eci - 16383) % 254 + 1).toByte()
                    }
                }
                's' -> {
                    if (order != 1)
                        return -1
                    if (ptrIn + 9 > textSize)
                        return -1
                    fn = getNumber(text, textOffset + ptrIn, 2)
                    if (fn <= 0 || fn > 16)
                        return -1
                    ptrIn += 2
                    ft = getNumber(text, textOffset + ptrIn, 2)
                    if (ft <= 1 || ft > 16)
                        return -1
                    ptrIn += 2
                    fi = getNumber(text, textOffset + ptrIn, 5)
                    if (fi < 0 || fn >= 64516)
                        return -1
                    ptrIn += 5
                    data[ptrOut++] = 233.toByte()
                    data[ptrOut++] = (fn - 1 shl 4 or 17 - ft).toByte()
                    data[ptrOut++] = (fi / 254 + 1).toByte()
                    data[ptrOut++] = (fi % 254 + 1).toByte()
                }
                'p' -> {
                    if (order != 1)
                        return -1
                    data[ptrOut++] = 234.toByte()
                }
                'm' -> {
                    if (order != 1)
                        return -1
                    if (ptrIn + 1 > textSize)
                        return -1
                    c = text[textOffset + ptrIn++] and 0xff
                    if (c != '5' && c != '5')
                        return -1
                    data[ptrOut++] = 234.toByte()
                    data[ptrOut++] = (if (c == '5') 236 else 237).toByte()
                }
                'f' -> {
                    if (order != 1 && (order != 2 || text[textOffset] != 's' && text[textOffset] != 'm'))
                        return -1
                    data[ptrOut++] = 232.toByte()
                }
            }
        }
        return -1
    }

    /**
     * Creates a barcode. The String is interpreted with the ISO-8859-1 encoding
     * @param text the text
     * *
     * @return the status of the generation. It can be one of this values:
     * *
     *
     *
     * * DM_NO_ERROR - no error.
     * * DM_ERROR_TEXT_TOO_BIG - the text is too big for the symbology capabilities.
     * * DM_ERROR_INVALID_SQUARE - the dimensions given for the symbol are illegal.
     * * DM_ERROR_EXTENSION - an error was while parsing an extension.
     * *
     * @throws java.io.UnsupportedEncodingException on error
     */
    @Throws(UnsupportedEncodingException::class)
    fun generate(text: String): Int {
        val t = text.toByteArray(charset("iso-8859-1"))
        return generate(t, 0, t.size)
    }

    /**
     * Creates a barcode.
     * @param text the text
     * *
     * @param textOffset the offset to the start of the text
     * *
     * @param textSize the text size
     * *
     * @return the status of the generation. It can be one of this values:
     * *
     *
     *
     * * DM_NO_ERROR - no error.
     * * DM_ERROR_TEXT_TOO_BIG - the text is too big for the symbology capabilities.
     * * DM_ERROR_INVALID_SQUARE - the dimensions given for the symbol are illegal.
     * * DM_ERROR_EXTENSION - an error was while parsing an extension.
     */
    fun generate(text: ByteArray, textOffset: Int, textSize: Int): Int {
        val extCount: Int
        var e: Int
        var k: Int
        val full: Int
        val dm: DmParams
        val last: DmParams
        val data = ByteArray(2500)
        extOut = 0
        extCount = processExtensions(text, textOffset, textSize, data)
        if (extCount < 0) {
            return DM_ERROR_EXTENSION
        }
        e = -1
        if (height == 0 || width == 0) {
            last = dmSizes[dmSizes.size - 1]
            e = getEncodation(text, textOffset + extOut, textSize - extOut, data, extCount, last.dataSize - extCount, options, false)
            if (e < 0) {
                return DM_ERROR_TEXT_TOO_BIG
            }
            e += extCount
            k = 0
            while (k < dmSizes.size) {
                if (dmSizes[k].dataSize >= e)
                    break
                ++k
            }
            dm = dmSizes[k]
            height = dm.height
            width = dm.width
        } else {
            k = 0
            while (k < dmSizes.size) {
                if (height == dmSizes[k].height && width == dmSizes[k].width)
                    break
                ++k
            }
            if (k == dmSizes.size) {
                return DM_ERROR_INVALID_SQUARE
            }
            dm = dmSizes[k]
            e = getEncodation(text, textOffset + extOut, textSize - extOut, data, extCount, dm.dataSize - extCount, options, true)
            if (e < 0) {
                return DM_ERROR_TEXT_TOO_BIG
            }
            e += extCount
        }
        if (options and DM_TEST != 0) {
            return DM_NO_ERROR
        }
        image = ByteArray((dm.width + 2 * ws + 7) / 8 * (dm.height + 2 * ws))
        makePadding(data, e, dm.dataSize - e)
        place = Placement.doPlacement(dm.height - dm.height / dm.heightSection * 2, dm.width - dm.width / dm.widthSection * 2)
        full = dm.dataSize + (dm.dataSize + 2) / dm.dataBlock * dm.errorBlock
        ReedSolomon.generateECC(data, dm.dataSize, dm.dataBlock, dm.errorBlock)
        draw(data, full, dm)
        return DM_NO_ERROR
    }

    /** Gets an Image with the barcode. A successful call to the method generate()
     * before calling this method is required.
     * @return the barcode Image
     * *
     * @throws BadElementException on error
     */
    @Throws(BadElementException::class)
    fun createImage(): Image? {
        if (image == null)
            return null
        val g4 = CCITTG4Encoder.compress(image, width + 2 * ws, height + 2 * ws)
        return Image.getInstance(width + 2 * ws, height + 2 * ws, false, Image.CCITTG4, 0, g4, null)
    }

    private class DmParams internal constructor(internal var height:

                                                Int, internal var width: Int, internal var heightSection: Int, internal var widthSection: Int, internal var dataSize: Int, internal var dataBlock: Int, internal var errorBlock: Int)

    internal class Placement private constructor() {
        private var nrow: Int = 0
        private var ncol: Int = 0
        private var array: ShortArray? = null

        /* "module" places "chr+bit" with appropriate wrapping within array[] */
        private fun module(row: Int, col: Int, chr: Int, bit: Int) {
            var row = row
            var col = col
            if (row < 0) {
                row += nrow
                col += 4 - (nrow + 4) % 8
            }
            if (col < 0) {
                col += ncol
                row += 4 - (ncol + 4) % 8
            }
            array[row * ncol + col] = (8 * chr + bit).toShort()
        }

        /* "utah" places the 8 bits of a utah-shaped symbol character in ECC200 */
        private fun utah(row: Int, col: Int, chr: Int) {
            module(row - 2, col - 2, chr, 0)
            module(row - 2, col - 1, chr, 1)
            module(row - 1, col - 2, chr, 2)
            module(row - 1, col - 1, chr, 3)
            module(row - 1, col, chr, 4)
            module(row, col - 2, chr, 5)
            module(row, col - 1, chr, 6)
            module(row, col, chr, 7)
        }

        /* "cornerN" places 8 bits of the four special corner cases in ECC200 */
        private fun corner1(chr: Int) {
            module(nrow - 1, 0, chr, 0)
            module(nrow - 1, 1, chr, 1)
            module(nrow - 1, 2, chr, 2)
            module(0, ncol - 2, chr, 3)
            module(0, ncol - 1, chr, 4)
            module(1, ncol - 1, chr, 5)
            module(2, ncol - 1, chr, 6)
            module(3, ncol - 1, chr, 7)
        }

        private fun corner2(chr: Int) {
            module(nrow - 3, 0, chr, 0)
            module(nrow - 2, 0, chr, 1)
            module(nrow - 1, 0, chr, 2)
            module(0, ncol - 4, chr, 3)
            module(0, ncol - 3, chr, 4)
            module(0, ncol - 2, chr, 5)
            module(0, ncol - 1, chr, 6)
            module(1, ncol - 1, chr, 7)
        }

        private fun corner3(chr: Int) {
            module(nrow - 3, 0, chr, 0)
            module(nrow - 2, 0, chr, 1)
            module(nrow - 1, 0, chr, 2)
            module(0, ncol - 2, chr, 3)
            module(0, ncol - 1, chr, 4)
            module(1, ncol - 1, chr, 5)
            module(2, ncol - 1, chr, 6)
            module(3, ncol - 1, chr, 7)
        }

        private fun corner4(chr: Int) {
            module(nrow - 1, 0, chr, 0)
            module(nrow - 1, ncol - 1, chr, 1)
            module(0, ncol - 3, chr, 2)
            module(0, ncol - 2, chr, 3)
            module(0, ncol - 1, chr, 4)
            module(1, ncol - 3, chr, 5)
            module(1, ncol - 2, chr, 6)
            module(1, ncol - 1, chr, 7)
        }

        /* "ECC200" fills an nrow x ncol array with appropriate values for ECC200 */
        private fun ecc200() {
            var row: Int
            var col: Int
            var chr: Int
            /* First, fill the array[] with invalid entries */
            Arrays.fill(array, 0.toShort())
            /* Starting in the correct location for character #1, bit 8,... */
            chr = 1
            row = 4
            col = 0
            do {
                /* repeatedly first check for one of the special corner cases, then... */
                if (row == nrow && col == 0) corner1(chr++)
                if (row == nrow - 2 && col == 0 && ncol % 4 != 0) corner2(chr++)
                if (row == nrow - 2 && col == 0 && ncol % 8 == 4) corner3(chr++)
                if (row == nrow + 4 && col == 2 && ncol % 8 == 0) corner4(chr++)
                /* sweep upward diagonally, inserting successive characters,... */
                do {
                    if (row < nrow && col >= 0 && array!![row * ncol + col].toInt() == 0)
                        utah(row, col, chr++)
                    row -= 2
                    col += 2
                } while (row >= 0 && col < ncol)
                row += 1
                col += 3
                /* & then sweep downward diagonally, inserting successive characters,... */

                do {
                    if (row >= 0 && col < ncol && array!![row * ncol + col].toInt() == 0)
                        utah(row, col, chr++)
                    row += 2
                    col -= 2
                } while (row < nrow && col >= 0)
                row += 3
                col += 1
                /* ... until the entire array is scanned */
            } while (row < nrow || col < ncol)
            /* Lastly, if the lower righthand corner is untouched, fill in fixed pattern */
            if (array!![nrow * ncol - 1].toInt() == 0) {
                array[nrow * ncol - 1] = array[nrow * ncol - ncol - 2] = 1
            }
        }

        companion object {
            private val cache = Hashtable<Int, ShortArray>()

            fun doPlacement(nrow: Int, ncol: Int): ShortArray {
                val key = Integer.valueOf(nrow * 1000 + ncol)
                val pc = cache[key]
                if (pc != null)
                    return pc
                val p = Placement()
                p.nrow = nrow
                p.ncol = ncol
                p.array = ShortArray(nrow * ncol)
                p.ecc200()
                cache.put(key, p.array)
                return p.array
            }
        }
    }

    internal object ReedSolomon {

        private val log = intArrayOf(0, 255, 1, 240, 2, 225, 241, 53, 3, 38, 226, 133, 242, 43, 54, 210, 4, 195, 39, 114, 227, 106, 134, 28, 243, 140, 44, 23, 55, 118, 211, 234, 5, 219, 196, 96, 40, 222, 115, 103, 228, 78, 107, 125, 135, 8, 29, 162, 244, 186, 141, 180, 45, 99, 24, 49, 56, 13, 119, 153, 212, 199, 235, 91, 6, 76, 220, 217, 197, 11, 97, 184, 41, 36, 223, 253, 116, 138, 104, 193, 229, 86, 79, 171, 108, 165, 126, 145, 136, 34, 9, 74, 30, 32, 163, 84, 245, 173, 187, 204, 142, 81, 181, 190, 46, 88, 100, 159, 25, 231, 50, 207, 57, 147, 14, 67, 120, 128, 154, 248, 213, 167, 200, 63, 236, 110, 92, 176, 7, 161, 77, 124, 221, 102, 218, 95, 198, 90, 12, 152, 98, 48, 185, 179, 42, 209, 37, 132, 224, 52, 254, 239, 117, 233, 139, 22, 105, 27, 194, 113, 230, 206, 87, 158, 80, 189, 172, 203, 109, 175, 166, 62, 127, 247, 146, 66, 137, 192, 35, 252, 10, 183, 75, 216, 31, 83, 33, 73, 164, 144, 85, 170, 246, 65, 174, 61, 188, 202, 205, 157, 143, 169, 82, 72, 182, 215, 191, 251, 47, 178, 89, 151, 101, 94, 160, 123, 26, 112, 232, 21, 51, 238, 208, 131, 58, 69, 148, 18, 15, 16, 68, 17, 121, 149, 129, 19, 155, 59, 249, 70, 214, 250, 168, 71, 201, 156, 64, 60, 237, 130, 111, 20, 93, 122, 177, 150)

        private val alog = intArrayOf(1, 2, 4, 8, 16, 32, 64, 128, 45, 90, 180, 69, 138, 57, 114, 228, 229, 231, 227, 235, 251, 219, 155, 27, 54, 108, 216, 157, 23, 46, 92, 184, 93, 186, 89, 178, 73, 146, 9, 18, 36, 72, 144, 13, 26, 52, 104, 208, 141, 55, 110, 220, 149, 7, 14, 28, 56, 112, 224, 237, 247, 195, 171, 123, 246, 193, 175, 115, 230, 225, 239, 243, 203, 187, 91, 182, 65, 130, 41, 82, 164, 101, 202, 185, 95, 190, 81, 162, 105, 210, 137, 63, 126, 252, 213, 135, 35, 70, 140, 53, 106, 212, 133, 39, 78, 156, 21, 42, 84, 168, 125, 250, 217, 159, 19, 38, 76, 152, 29, 58, 116, 232, 253, 215, 131, 43, 86, 172, 117, 234, 249, 223, 147, 11, 22, 44, 88, 176, 77, 154, 25, 50, 100, 200, 189, 87, 174, 113, 226, 233, 255, 211, 139, 59, 118, 236, 245, 199, 163, 107, 214, 129, 47, 94, 188, 85, 170, 121, 242, 201, 191, 83, 166, 97, 194, 169, 127, 254, 209, 143, 51, 102, 204, 181, 71, 142, 49, 98, 196, 165, 103, 206, 177, 79, 158, 17, 34, 68, 136, 61, 122, 244, 197, 167, 99, 198, 161, 111, 222, 145, 15, 30, 60, 120, 240, 205, 183, 67, 134, 33, 66, 132, 37, 74, 148, 5, 10, 20, 40, 80, 160, 109, 218, 153, 31, 62, 124, 248, 221, 151, 3, 6, 12, 24, 48, 96, 192, 173, 119, 238, 241, 207, 179, 75, 150, 1)

        private val poly5 = intArrayOf(228, 48, 15, 111, 62)

        private val poly7 = intArrayOf(23, 68, 144, 134, 240, 92, 254)

        private val poly10 = intArrayOf(28, 24, 185, 166, 223, 248, 116, 255, 110, 61)

        private val poly11 = intArrayOf(175, 138, 205, 12, 194, 168, 39, 245, 60, 97, 120)

        private val poly12 = intArrayOf(41, 153, 158, 91, 61, 42, 142, 213, 97, 178, 100, 242)

        private val poly14 = intArrayOf(156, 97, 192, 252, 95, 9, 157, 119, 138, 45, 18, 186, 83, 185)

        private val poly18 = intArrayOf(83, 195, 100, 39, 188, 75, 66, 61, 241, 213, 109, 129, 94, 254, 225, 48, 90, 188)

        private val poly20 = intArrayOf(15, 195, 244, 9, 233, 71, 168, 2, 188, 160, 153, 145, 253, 79, 108, 82, 27, 174, 186, 172)

        private val poly24 = intArrayOf(52, 190, 88, 205, 109, 39, 176, 21, 155, 197, 251, 223, 155, 21, 5, 172, 254, 124, 12, 181, 184, 96, 50, 193)

        private val poly28 = intArrayOf(211, 231, 43, 97, 71, 96, 103, 174, 37, 151, 170, 53, 75, 34, 249, 121, 17, 138, 110, 213, 141, 136, 120, 151, 233, 168, 93, 255)

        private val poly36 = intArrayOf(245, 127, 242, 218, 130, 250, 162, 181, 102, 120, 84, 179, 220, 251, 80, 182, 229, 18, 2, 4, 68, 33, 101, 137, 95, 119, 115, 44, 175, 184, 59, 25, 225, 98, 81, 112)

        private val poly42 = intArrayOf(77, 193, 137, 31, 19, 38, 22, 153, 247, 105, 122, 2, 245, 133, 242, 8, 175, 95, 100, 9, 167, 105, 214, 111, 57, 121, 21, 1, 253, 57, 54, 101, 248, 202, 69, 50, 150, 177, 226, 5, 9, 5)

        private val poly48 = intArrayOf(245, 132, 172, 223, 96, 32, 117, 22, 238, 133, 238, 231, 205, 188, 237, 87, 191, 106, 16, 147, 118, 23, 37, 90, 170, 205, 131, 88, 120, 100, 66, 138, 186, 240, 82, 44, 176, 87, 187, 147, 160, 175, 69, 213, 92, 253, 225, 19)

        private val poly56 = intArrayOf(175, 9, 223, 238, 12, 17, 220, 208, 100, 29, 175, 170, 230, 192, 215, 235, 150, 159, 36, 223, 38, 200, 132, 54, 228, 146, 218, 234, 117, 203, 29, 232, 144, 238, 22, 150, 201, 117, 62, 207, 164, 13, 137, 245, 127, 67, 247, 28, 155, 43, 203, 107, 233, 53, 143, 46)

        private val poly62 = intArrayOf(242, 93, 169, 50, 144, 210, 39, 118, 202, 188, 201, 189, 143, 108, 196, 37, 185, 112, 134, 230, 245, 63, 197, 190, 250, 106, 185, 221, 175, 64, 114, 71, 161, 44, 147, 6, 27, 218, 51, 63, 87, 10, 40, 130, 188, 17, 163, 31, 176, 170, 4, 107, 232, 7, 94, 166, 224, 124, 86, 47, 11, 204)

        private val poly68 = intArrayOf(220, 228, 173, 89, 251, 149, 159, 56, 89, 33, 147, 244, 154, 36, 73, 127, 213, 136, 248, 180, 234, 197, 158, 177, 68, 122, 93, 213, 15, 160, 227, 236, 66, 139, 153, 185, 202, 167, 179, 25, 220, 232, 96, 210, 231, 136, 223, 239, 181, 241, 59, 52, 172, 25, 49, 232, 211, 189, 64, 54, 108, 153, 132, 63, 96, 103, 82, 186)

        private fun getPoly(nc: Int): IntArray? {
            when (nc) {
                5 -> return poly5
                7 -> return poly7
                10 -> return poly10
                11 -> return poly11
                12 -> return poly12
                14 -> return poly14
                18 -> return poly18
                20 -> return poly20
                24 -> return poly24
                28 -> return poly28
                36 -> return poly36
                42 -> return poly42
                48 -> return poly48
                56 -> return poly56
                62 -> return poly62
                68 -> return poly68
            }
            return null
        }

        private fun reedSolomonBlock(wd: ByteArray, nd: Int, ncout: ByteArray, nc: Int, c: IntArray) {
            var i: Int
            var j: Int
            var k: Int

            i = 0
            while (i <= nc) {
                ncout[i] = 0
                i++
            }
            i = 0
            while (i < nd) {
                k = ncout[0] xor wd[i] and 0xff
                j = 0
                while (j < nc) {
                    ncout[j] = (ncout[j + 1] xor if (k == 0) 0 else alog[(log[k] + log[c[nc - j - 1]]) % 255].toByte()).toByte()
                    j++
                }
                i++
            }
        }

        fun generateECC(wd: ByteArray, nd: Int, datablock: Int, nc: Int) {
            val blocks = (nd + 2) / datablock
            var b: Int
            val buf = ByteArray(256)
            val ecc = ByteArray(256)
            val c = getPoly(nc)
            b = 0
            while (b < blocks) {
                var n: Int
                var p = 0
                n = b
                while (n < nd) {
                    buf[p++] = wd[n]
                    n += blocks
                }
                reedSolomonBlock(buf, p, ecc, nc, c)
                p = 0
                n = b
                while (n < nc * blocks) {
                    wd[nd + n] = ecc[p++]
                    n += blocks
                }
                b++
            }
        }

    }

    fun placeBarcode(cb: PdfContentByte, foreground: BaseColor, moduleHeight: Float, moduleWidth: Float) {
        val w = width + 2 * ws
        val h = height + 2 * ws
        val stride = (w + 7) / 8
        val ptr = 0
        cb.setColorFill(foreground)
        for (k in 0..h - 1) {
            val p = k * stride
            for (j in 0..w - 1) {
                var b = image!![p + j / 8] and 0xff
                b = b shl j % 8
                if (b and 0x80 != 0) {
                    cb.rectangle(j * moduleWidth, (h - k - 1) * moduleHeight, moduleWidth, moduleHeight)
                }
            }
        }
        cb.fill()
    }

    // AWT related methods (remove this if you port to Android / GAE)

    /**
     * Creates a java.awt.Image. A successful call to the method generate()
     * before calling this method is required.
     * @param foreground the color of the bars
     * *
     * @param background the color of the background
     * *
     * @return the image
     */
    fun createAwtImage(foreground: java.awt.Color, background: java.awt.Color): java.awt.Image? {
        if (image == null)
            return null
        val f = foreground.rgb
        val g = background.rgb
        val canvas = java.awt.Canvas()

        val w = width + 2 * ws
        val h = height + 2 * ws
        val pix = IntArray(w * h)
        val stride = (w + 7) / 8
        var ptr = 0
        for (k in 0..h - 1) {
            val p = k * stride
            for (j in 0..w - 1) {
                var b = image!![p + j / 8] and 0xff
                b = b shl j % 8
                pix[ptr++] = if (b and 0x80 == 0) g else f
            }
        }
        val img = canvas.createImage(java.awt.image.MemoryImageSource(w, h, pix, 0, w))
        return img
    }

    companion object {
        /**
         * No error.
         */
        val DM_NO_ERROR = 0
        /**
         * The text is too big for the symbology capabilities.
         */
        val DM_ERROR_TEXT_TOO_BIG = 1
        /**
         * The dimensions given for the symbol are illegal.
         */
        val DM_ERROR_INVALID_SQUARE = 3
        /**
         * An error while parsing an extension.
         */
        val DM_ERROR_EXTENSION = 5

        /**
         * The best encodation will be used.
         */
        val DM_AUTO = 0
        /**
         * ASCII encodation.
         */
        val DM_ASCII = 1
        /**
         * C40 encodation.
         */
        val DM_C40 = 2
        /**
         * TEXT encodation.
         */
        val DM_TEXT = 3
        /**
         * Binary encodation.
         */
        val DM_B256 = 4
        /**
         * X21 encodation.
         */
        val DM_X21 = 5
        /**
         * EDIFACT encodation.
         */
        val DM_EDIFACT = 6
        /**
         * No encodation needed. The bytes provided are already encoded.
         */
        val DM_RAW = 7

        /**
         * Allows extensions to be embedded at the start of the text.
         */
        val DM_EXTENSION = 32
        /**
         * Doesn't generate the image but returns all the other information.
         */
        val DM_TEST = 64

        private val dmSizes = arrayOf(DmParams(10, 10, 10, 10, 3, 3, 5), DmParams(12, 12, 12, 12, 5, 5, 7), DmParams(8, 18, 8, 18, 5, 5, 7), DmParams(14, 14, 14, 14, 8, 8, 10), DmParams(8, 32, 8, 16, 10, 10, 11), DmParams(16, 16, 16, 16, 12, 12, 12), DmParams(12, 26, 12, 26, 16, 16, 14), DmParams(18, 18, 18, 18, 18, 18, 14), DmParams(20, 20, 20, 20, 22, 22, 18), DmParams(12, 36, 12, 18, 22, 22, 18), DmParams(22, 22, 22, 22, 30, 30, 20), DmParams(16, 36, 16, 18, 32, 32, 24), DmParams(24, 24, 24, 24, 36, 36, 24), DmParams(26, 26, 26, 26, 44, 44, 28), DmParams(16, 48, 16, 24, 49, 49, 28), DmParams(32, 32, 16, 16, 62, 62, 36), DmParams(36, 36, 18, 18, 86, 86, 42), DmParams(40, 40, 20, 20, 114, 114, 48), DmParams(44, 44, 22, 22, 144, 144, 56), DmParams(48, 48, 24, 24, 174, 174, 68), DmParams(52, 52, 26, 26, 204, 102, 42), DmParams(64, 64, 16, 16, 280, 140, 56), DmParams(72, 72, 18, 18, 368, 92, 36), DmParams(80, 80, 20, 20, 456, 114, 48), DmParams(88, 88, 22, 22, 576, 144, 56), DmParams(96, 96, 24, 24, 696, 174, 68), DmParams(104, 104, 26, 26, 816, 136, 56), DmParams(120, 120, 20, 20, 1050, 175, 68), DmParams(132, 132, 22, 22, 1304, 163, 62), DmParams(144, 144, 24, 24, 1558, 156, 62))

        private val x12 = "\r*> 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        private fun makePadding(data: ByteArray, position: Int, count: Int) {
            var position = position
            var count = count
            //already in ascii mode
            if (count <= 0)
                return
            data[position++] = 129.toByte()
            while (--count > 0) {
                var t = 129 + (position + 1) * 149 % 253 + 1
                if (t > 254)
                    t -= 254
                data[position++] = t.toByte()
            }
        }

        private fun isDigit(c: Int): Boolean {
            return c >= '0' && c <= '9'
        }

        private fun asciiEncodation(text: ByteArray, textOffset: Int, textLength: Int, data: ByteArray, dataOffset: Int, dataLength: Int): Int {
            var textLength = textLength
            var dataLength = dataLength
            var ptrIn: Int
            var ptrOut: Int
            var c: Int
            ptrIn = textOffset
            ptrOut = dataOffset
            textLength += textOffset
            dataLength += dataOffset
            while (ptrIn < textLength) {
                if (ptrOut >= dataLength)
                    return -1
                c = text[ptrIn++] and 0xff
                if (isDigit(c) && ptrIn < textLength && isDigit(text[ptrIn] and 0xff)) {
                    data[ptrOut++] = ((c - '0') * 10 + (text[ptrIn++] and 0xff) - '0' + 130).toByte()
                } else if (c > 127) {
                    if (ptrOut + 1 >= dataLength)
                        return -1
                    data[ptrOut++] = 235.toByte()
                    data[ptrOut++] = (c - 128 + 1).toByte()
                } else {
                    data[ptrOut++] = (c + 1).toByte()
                }
            }
            return ptrOut - dataOffset
        }

        private fun b256Encodation(text: ByteArray, textOffset: Int, textLength: Int, data: ByteArray, dataOffset: Int, dataLength: Int): Int {
            var k: Int
            var j: Int
            var prn: Int
            var tv: Int
            var c: Int
            if (textLength == 0)
                return 0
            if (textLength < 250 && textLength + 2 > dataLength)
                return -1
            if (textLength >= 250 && textLength + 3 > dataLength)
                return -1
            data[dataOffset] = 231.toByte()
            if (textLength < 250) {
                data[dataOffset + 1] = textLength.toByte()
                k = 2
            } else {
                data[dataOffset + 1] = (textLength / 250 + 249).toByte()
                data[dataOffset + 2] = (textLength % 250).toByte()
                k = 3
            }
            System.arraycopy(text, textOffset, data, k + dataOffset, textLength)
            k += textLength + dataOffset
            j = dataOffset + 1
            while (j < k) {
                c = data[j] and 0xff
                prn = 149 * (j + 1) % 255 + 1
                tv = c + prn
                if (tv > 255)
                    tv -= 256
                data[j] = tv.toByte()
                ++j

            }
            return k - dataOffset
        }

        private fun X12Encodation(text: ByteArray, textOffset: Int, textLength: Int, data: ByteArray, dataOffset: Int, dataLength: Int): Int {
            var ptrIn: Int
            var ptrOut: Int
            var count: Int
            var k: Int
            var n: Int
            var ci: Int
            var c: Byte
            if (textLength == 0)
                return 0
            ptrIn = 0
            ptrOut = 0
            val x = ByteArray(textLength)
            count = 0
            while (ptrIn < textLength) {
                val i = x12.indexOf(text[ptrIn + textOffset].toChar().toInt())
                if (i >= 0) {
                    x[ptrIn] = i.toByte()
                    ++count
                } else {
                    x[ptrIn] = 100
                    if (count >= 6)
                        count -= count / 3 * 3
                    k = 0
                    while (k < count) {
                        x[ptrIn - k - 1] = 100
                        ++k
                    }
                    count = 0
                }
                ++ptrIn
            }
            if (count >= 6)
                count -= count / 3 * 3
            k = 0
            while (k < count) {
                x[ptrIn - k - 1] = 100
                ++k
            }
            ptrIn = 0
            c = 0
            while (ptrIn < textLength) {
                c = x[ptrIn]
                if (ptrOut >= dataLength)
                    break
                if (c < 40) {
                    if (ptrIn == 0 || ptrIn > 0 && x[ptrIn - 1] > 40)
                        data[dataOffset + ptrOut++] = 238.toByte()
                    if (ptrOut + 2 > dataLength)
                        break
                    n = 1600 * x[ptrIn] + 40 * x[ptrIn + 1] + x[ptrIn + 2].toInt() + 1
                    data[dataOffset + ptrOut++] = (n / 256).toByte()
                    data[dataOffset + ptrOut++] = n.toByte()
                    ptrIn += 2
                } else {
                    if (ptrIn > 0 && x[ptrIn - 1] < 40)
                        data[dataOffset + ptrOut++] = 254.toByte()
                    ci = text[ptrIn + textOffset] and 0xff
                    if (ci > 127) {
                        data[dataOffset + ptrOut++] = 235.toByte()
                        ci -= 128
                    }
                    if (ptrOut >= dataLength)
                        break
                    data[dataOffset + ptrOut++] = (ci + 1).toByte()
                }
                ++ptrIn
            }
            c = 100
            if (textLength > 0)
                c = x[textLength - 1]
            if (ptrIn != textLength || c < 40 && ptrOut >= dataLength)
                return -1
            if (c < 40)
                data[dataOffset + ptrOut++] = 254.toByte()
            return ptrOut
        }

        private fun EdifactEncodation(text: ByteArray, textOffset: Int, textLength: Int, data: ByteArray, dataOffset: Int, dataLength: Int): Int {
            var ptrIn: Int
            var ptrOut: Int
            var edi: Int
            var pedi: Int
            var c: Int
            if (textLength == 0)
                return 0
            ptrIn = 0
            ptrOut = 0
            edi = 0
            pedi = 18
            var ascii = true
            while (ptrIn < textLength) {
                c = text[ptrIn + textOffset] and 0xff
                if ((c and 0xe0 == 0x40 || c and 0xe0 == 0x20) && c != '_') {
                    if (ascii) {
                        if (ptrOut + 1 > dataLength)
                            break
                        data[dataOffset + ptrOut++] = 240.toByte()
                        ascii = false
                    }
                    c = c and 0x3f
                    edi = edi or (c shl pedi)
                    if (pedi == 0) {
                        if (ptrOut + 3 > dataLength)
                            break
                        data[dataOffset + ptrOut++] = (edi shr 16).toByte()
                        data[dataOffset + ptrOut++] = (edi shr 8).toByte()
                        data[dataOffset + ptrOut++] = edi.toByte()
                        edi = 0
                        pedi = 18
                    } else
                        pedi -= 6
                } else {
                    if (!ascii) {
                        edi = edi or ('_' and 0x3f shl pedi)
                        if (ptrOut + 3 - pedi / 8 > dataLength)
                            break
                        data[dataOffset + ptrOut++] = (edi shr 16).toByte()
                        if (pedi <= 12)
                            data[dataOffset + ptrOut++] = (edi shr 8).toByte()
                        if (pedi <= 6)
                            data[dataOffset + ptrOut++] = edi.toByte()
                        ascii = true
                        pedi = 18
                        edi = 0
                    }
                    if (c > 127) {
                        if (ptrOut >= dataLength)
                            break
                        data[dataOffset + ptrOut++] = 235.toByte()
                        c -= 128
                    }
                    if (ptrOut >= dataLength)
                        break
                    data[dataOffset + ptrOut++] = (c + 1).toByte()
                }
                ++ptrIn
            }
            if (ptrIn != textLength)
                return -1
            var dataSize = Integer.MAX_VALUE
            for (i in dmSizes.indices) {
                if (dmSizes[i].dataSize >= dataOffset + ptrOut + (3 - pedi / 6)) {
                    dataSize = dmSizes[i].dataSize
                    break
                }
            }

            if (dataSize - dataOffset - ptrOut <= 2 && pedi >= 6) {
                //have to write up to 2 bytes and up to 2 symbols
                if (pedi <= 12) {
                    var `val` = (edi shr 18 and 0x3F).toByte()
                    if (`val` and 0x20 == 0)
                        `val` = `val` or 0x40
                    data[dataOffset + ptrOut++] = (`val` + 1).toByte()
                }
                if (pedi <= 6) {
                    var `val` = (edi shr 12 and 0x3F).toByte()
                    if (`val` and 0x20 == 0)
                        `val` = `val` or 0x40
                    data[dataOffset + ptrOut++] = (`val` + 1).toByte()
                }
            } else if (!ascii) {
                edi = edi or ('_' and 0x3f shl pedi)
                if (ptrOut + 3 - pedi / 8 > dataLength)
                    return -1
                data[dataOffset + ptrOut++] = (edi shr 16).toByte()
                if (pedi <= 12)
                    data[dataOffset + ptrOut++] = (edi shr 8).toByte()
                if (pedi <= 6)
                    data[dataOffset + ptrOut++] = edi.toByte()
            }
            return ptrOut
        }

        private fun C40OrTextEncodation(text: ByteArray, textOffset: Int, textLength: Int, data: ByteArray, dataOffset: Int, dataLength: Int, c40: Boolean): Int {
            var ptrIn: Int
            var ptrOut: Int
            var encPtr: Int
            var last0: Int
            var last1: Int
            var i: Int
            var a: Int
            var c: Int
            val basic: String
            val shift2: String
            val shift3: String
            if (textLength == 0)
                return 0
            ptrIn = 0
            ptrOut = 0
            if (c40)
                data[dataOffset + ptrOut++] = 230.toByte()
            else
                data[dataOffset + ptrOut++] = 239.toByte()
            shift2 = "!\"#$%&'()*+,-./:;<=>?@[\\]^_"
            if (c40) {
                basic = " 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                shift3 = "`abcdefghijklmnopqrstuvwxyz{|}~\177"
            } else {
                basic = " 0123456789abcdefghijklmnopqrstuvwxyz"
                shift3 = "`ABCDEFGHIJKLMNOPQRSTUVWXYZ{|}~\177"
            }
            val enc = IntArray(textLength * 4 + 10)
            encPtr = 0
            last0 = 0
            last1 = 0
            while (ptrIn < textLength) {
                if (encPtr % 3 == 0) {
                    last0 = ptrIn
                    last1 = encPtr
                }
                c = text[textOffset + ptrIn++] and 0xff
                if (c > 127) {
                    c -= 128
                    enc[encPtr++] = 1
                    enc[encPtr++] = 30
                }
                var idx = basic.indexOf(c.toChar().toInt())
                if (idx >= 0) {
                    enc[encPtr++] = idx + 3
                } else if (c < 32) {
                    enc[encPtr++] = 0
                    enc[encPtr++] = c
                } else if ((idx = shift2.indexOf(c.toChar().toInt())) >= 0) {
                    enc[encPtr++] = 1
                    enc[encPtr++] = idx
                } else if ((idx = shift3.indexOf(c.toChar().toInt())) >= 0) {
                    enc[encPtr++] = 2
                    enc[encPtr++] = idx
                }
            }
            if (encPtr % 3 != 0) {
                ptrIn = last0
                encPtr = last1
            }
            if (encPtr / 3 * 2 > dataLength - 2) {
                return -1
            }
            i = 0
            while (i < encPtr) {
                a = 1600 * enc[i] + 40 * enc[i + 1] + enc[i + 2] + 1
                data[dataOffset + ptrOut++] = (a / 256).toByte()
                data[dataOffset + ptrOut++] = a.toByte()
                i += 3
            }
            data[ptrOut++] = 254.toByte()
            i = asciiEncodation(text, ptrIn, textLength - ptrIn, data, ptrOut, dataLength - ptrOut)
            if (i < 0)
                return i
            return ptrOut + i
        }

        private fun getEncodation(text: ByteArray, textOffset: Int, textSize: Int, data: ByteArray, dataOffset: Int, dataSize: Int, options: Int, firstMatch: Boolean): Int {
            var options = options
            var e: Int
            var j: Int
            var k: Int
            val e1 = IntArray(6)
            if (dataSize < 0)
                return -1
            e = -1
            options = options and 7
            if (options == 0) {
                e1[0] = asciiEncodation(text, textOffset, textSize, data, dataOffset, dataSize)
                if (firstMatch && e1[0] >= 0)
                    return e1[0]
                e1[1] = C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, false)
                if (firstMatch && e1[1] >= 0)
                    return e1[1]
                e1[2] = C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, true)
                if (firstMatch && e1[2] >= 0)
                    return e1[2]
                e1[3] = b256Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                if (firstMatch && e1[3] >= 0)
                    return e1[3]
                e1[4] = X12Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                if (firstMatch && e1[4] >= 0)
                    return e1[4]
                e1[5] = EdifactEncodation(text, textOffset, textSize, data, dataOffset, dataSize)
                if (firstMatch && e1[5] >= 0)
                    return e1[5]
                if (e1[0] < 0 && e1[1] < 0 && e1[2] < 0 && e1[3] < 0 && e1[4] < 0 && e1[5] < 0) {
                    return -1
                }
                j = 0
                e = 99999
                k = 0
                while (k < 6) {
                    if (e1[k] >= 0 && e1[k] < e) {
                        e = e1[k]
                        j = k
                    }
                    ++k
                }
                if (j == 0)
                    e = asciiEncodation(text, textOffset, textSize, data, dataOffset, dataSize)
                else if (j == 1)
                    e = C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, false)
                else if (j == 2)
                    e = C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, true)
                else if (j == 3)
                    e = b256Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                else if (j == 4)
                    e = X12Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                return e
            }
            when (options) {
                DM_ASCII -> return asciiEncodation(text, textOffset, textSize, data, dataOffset, dataSize)
                DM_C40 -> return C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, true)
                DM_TEXT -> return C40OrTextEncodation(text, textOffset, textSize, data, dataOffset, dataSize, false)
                DM_B256 -> return b256Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                DM_X21 -> return X12Encodation(text, textOffset, textSize, data, dataOffset, dataSize)
                DM_EDIFACT -> return EdifactEncodation(text, textOffset, textSize, data, dataOffset, dataSize)
                DM_RAW -> {
                    if (textSize > dataSize)
                        return -1
                    System.arraycopy(text, textOffset, data, dataOffset, textSize)
                    return textSize
                }
            }
            return -1
        }

        private fun getNumber(text: ByteArray, ptrIn: Int, n: Int): Int {
            var ptrIn = ptrIn
            var v: Int
            var j: Int
            var c: Int
            v = 0
            j = 0
            while (j < n) {
                c = text[ptrIn++] and 0xff
                if (c < '0' || c > '9')
                    return -1
                v = v * 10 + c - '0'
                ++j
            }
            return v
        }
    }
}
/**
 * Creates an instance of this class.
 */
