/*
 * $Id: efb6a9e3f2182413722aacdb79ee39c991df468a $
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
package com.itextpdf.text

import java.io.IOException
import java.io.InputStream
import java.net.URL

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.ICC_Profile

/**
 * An Jpeg is the representation of a graphic element (JPEG)
 * that has to be inserted into the document

 * @see Element

 * @see Image
 */

class Jpeg : Image {

    private var icc: Array<ByteArray>? = null
    // Constructors

    internal constructor(image: Image) : super(image) {
    }

    /**
     * Constructs a Jpeg-object, using an url.

     * @param        url            the URL where the image can be found
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */
    @Throws(BadElementException::class, IOException::class)
    constructor(url: URL) : super(url) {
        processParameters()
    }

    /**
     * Constructs a Jpeg-object from memory.

     * @param        img        the memory image
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(img: ByteArray) : super(null as URL) {
        rawData = img
        originalData = img
        processParameters()
    }

    /**
     * Constructs a Jpeg-object from memory.

     * @param        img            the memory image.
     * *
     * @param        width        the width you want the image to have
     * *
     * @param        height        the height you want the image to have
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(img: ByteArray, width: Float, height: Float) : this(img) {
        scaledWidth = width
        scaledHeight = height
    }

    // private methods

    /**
     * This method checks if the image is a valid JPEG and processes some parameters.
     * @throws BadElementException
     * *
     * @throws IOException
     */
    @Throws(BadElementException::class, IOException::class)
    private fun processParameters() {
        type = Element.JPEG
        originalType = Image.ORIGINAL_JPEG
        var `is`: InputStream? = null
        try {
            val errorID: String
            if (rawData == null) {
                `is` = url.openStream()
                errorID = url.toString()
            } else {
                `is` = java.io.ByteArrayInputStream(rawData)
                errorID = "Byte array"
            }
            if (`is`!!.read() != 0xFF || `is`.read() != 0xD8) {
                throw BadElementException(MessageLocalization.getComposedMessage("1.is.not.a.valid.jpeg.file", errorID))
            }
            var firstPass = true
            var len: Int
            while (true) {
                val v = `is`.read()
                if (v < 0)
                    throw IOException(MessageLocalization.getComposedMessage("premature.eof.while.reading.jpg"))
                if (v == 0xFF) {
                    val marker = `is`.read()
                    if (firstPass && marker == M_APP0) {
                        firstPass = false
                        len = getShort(`is`)
                        if (len < 16) {
                            Utilities.skip(`is`, len - 2)
                            continue
                        }
                        val bcomp = ByteArray(JFIF_ID.size)
                        val r = `is`.read(bcomp)
                        if (r != bcomp.size)
                            throw BadElementException(MessageLocalization.getComposedMessage("1.corrupted.jfif.marker", errorID))
                        var found = true
                        for (k in bcomp.indices) {
                            if (bcomp[k] != JFIF_ID[k]) {
                                found = false
                                break
                            }
                        }
                        if (!found) {
                            Utilities.skip(`is`, len - 2 - bcomp.size)
                            continue
                        }
                        Utilities.skip(`is`, 2)
                        val units = `is`.read()
                        val dx = getShort(`is`)
                        val dy = getShort(`is`)
                        if (units == 1) {
                            dpiX = dx
                            dpiY = dy
                        } else if (units == 2) {
                            dpiX = (dx * 2.54f + 0.5f).toInt()
                            dpiY = (dy * 2.54f + 0.5f).toInt()
                        }
                        Utilities.skip(`is`, len - 2 - bcomp.size - 7)
                        continue
                    }
                    if (marker == M_APPE) {
                        len = getShort(`is`) - 2
                        val byteappe = ByteArray(len)
                        for (k in 0..len - 1) {
                            byteappe[k] = `is`.read().toByte()
                        }
                        if (byteappe.size >= 12) {
                            val appe = String(byteappe, 0, 5, "ISO-8859-1")
                            if (appe == "Adobe") {
                                isInverted = true
                            }
                        }
                        continue
                    }
                    if (marker == M_APP2) {
                        len = getShort(`is`) - 2
                        val byteapp2 = ByteArray(len)
                        for (k in 0..len - 1) {
                            byteapp2[k] = `is`.read().toByte()
                        }
                        if (byteapp2.size >= 14) {
                            val app2 = String(byteapp2, 0, 11, "ISO-8859-1")
                            if (app2 == "ICC_PROFILE") {
                                var order = byteapp2[12] and 0xff
                                var count = byteapp2[13] and 0xff
                                // some jpeg producers don't know how to count to 1
                                if (order < 1)
                                    order = 1
                                if (count < 1)
                                    count = 1
                                if (icc == null)
                                    icc = arrayOfNulls<ByteArray>(count)
                                icc[order - 1] = byteapp2
                            }
                        }
                        continue
                    }
                    if (marker == M_APPD) {
                        len = getShort(`is`) - 2
                        val byteappd = ByteArray(len)
                        for (k in 0..len - 1) {
                            byteappd[k] = `is`.read().toByte()
                        }
                        // search for '8BIM Resolution' marker
                        var k = 0
                        k = 0
                        while (k < len - PS_8BIM_RESO.size) {
                            var found = true
                            for (j in PS_8BIM_RESO.indices) {
                                if (byteappd[k + j] != PS_8BIM_RESO[j]) {
                                    found = false
                                    break
                                }
                            }
                            if (found)
                                break
                            k++
                        }

                        k += PS_8BIM_RESO.size
                        if (k < len - PS_8BIM_RESO.size) {
                            // "PASCAL String" for name, i.e. string prefix with length byte
                            // padded to be even length; 2 null bytes if empty
                            var namelength = byteappd[k]
                            // add length byte
                            namelength++
                            // add padding
                            if (namelength % 2 == 1)
                                namelength++
                            // just skip name
                            k += namelength.toInt()
                            // size of the resolution data
                            val resosize = (byteappd[k] shl 24) + (byteappd[k + 1] shl 16) + (byteappd[k + 2] shl 8) + byteappd[k + 3].toInt()
                            // should be 16
                            if (resosize != 16) {
                                // fail silently, for now
                                //System.err.println("DEBUG: unsupported resolution IRB size");
                                continue
                            }
                            k += 4
                            var dx = (byteappd[k] shl 8) + (byteappd[k + 1] and 0xff)
                            k += 2
                            // skip 2 unknown bytes
                            k += 2
                            val unitsx = (byteappd[k] shl 8) + (byteappd[k + 1] and 0xff)
                            k += 2
                            // skip 2 unknown bytes
                            k += 2
                            var dy = (byteappd[k] shl 8) + (byteappd[k + 1] and 0xff)
                            k += 2
                            // skip 2 unknown bytes
                            k += 2
                            val unitsy = (byteappd[k] shl 8) + (byteappd[k + 1] and 0xff)

                            if (unitsx == 1 || unitsx == 2) {
                                dx = if (unitsx == 2) (dx * 2.54f + 0.5f).toInt() else dx
                                // make sure this is consistent with JFIF data
                                if (dpiX != 0 && dpiX != dx) {
                                    //System.err.println("DEBUG: inconsistent metadata (dpiX: " + dpiX + " vs " + dx + ")");
                                } else
                                    dpiX = dx
                            }
                            if (unitsy == 1 || unitsy == 2) {
                                dy = if (unitsy == 2) (dy * 2.54f + 0.5f).toInt() else dy
                                // make sure this is consistent with JFIF data
                                if (dpiY != 0 && dpiY != dy) {
                                    //System.err.println("DEBUG: inconsistent metadata (dpiY: " + dpiY + " vs " + dy + ")");
                                } else
                                    dpiY = dy
                            }
                        }
                        continue
                    }
                    firstPass = false
                    val markertype = marker(marker)
                    if (markertype == VALID_MARKER) {
                        Utilities.skip(`is`, 2)
                        if (`is`.read() != 0x08) {
                            throw BadElementException(MessageLocalization.getComposedMessage("1.must.have.8.bits.per.component", errorID))
                        }
                        scaledHeight = getShort(`is`).toFloat()
                        top = scaledHeight
                        scaledWidth = getShort(`is`).toFloat()
                        right = scaledWidth
                        colorspace = `is`.read()
                        bpc = 8
                        break
                    } else if (markertype == UNSUPPORTED_MARKER) {
                        throw BadElementException(MessageLocalization.getComposedMessage("1.unsupported.jpeg.marker.2", errorID, marker.toString()))
                    } else if (markertype != NOPARAM_MARKER) {
                        Utilities.skip(`is`, getShort(`is`) - 2)
                    }
                }
            }
        } finally {
            if (`is` != null) {
                `is`.close()
            }
        }
        plainWidth = width
        plainHeight = height
        if (icc != null) {
            var total = 0
            for (k in icc!!.indices) {
                if (icc!![k] == null) {
                    icc = null
                    return
                }
                total += icc!![k].size - 14
            }
            val ficc = ByteArray(total)
            total = 0
            for (k in icc!!.indices) {
                System.arraycopy(icc!![k], 14, ficc, total, icc!![k].size - 14)
                total += icc!![k].size - 14
            }
            try {
                val icc_prof = ICC_Profile.getInstance(ficc, colorspace)
                tagICC(icc_prof)
            } catch (e: IllegalArgumentException) {
                // ignore ICC profile if it's invalid.
            }

            icc = null
        }
    }

    companion object {

        // public static final membervariables

        /** This is a type of marker.  */
        val NOT_A_MARKER = -1

        /** This is a type of marker.  */
        val VALID_MARKER = 0

        /** Acceptable Jpeg markers.  */
        val VALID_MARKERS = intArrayOf(0xC0, 0xC1, 0xC2)

        /** This is a type of marker.  */
        val UNSUPPORTED_MARKER = 1

        /** Unsupported Jpeg markers.  */
        val UNSUPPORTED_MARKERS = intArrayOf(0xC3, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF)

        /** This is a type of marker.  */
        val NOPARAM_MARKER = 2

        /** Jpeg markers without additional parameters.  */
        val NOPARAM_MARKERS = intArrayOf(0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0x01)

        /** Marker value  */
        val M_APP0 = 0xE0
        /** Marker value  */
        val M_APP2 = 0xE2
        /** Marker value  */
        val M_APPE = 0xEE
        /** Marker value for Photoshop IRB  */
        val M_APPD = 0xED

        /** sequence that is used in all Jpeg files  */
        val JFIF_ID = byteArrayOf(0x4A, 0x46, 0x49, 0x46, 0x00)

        /** sequence preceding Photoshop resolution data  */
        val PS_8BIM_RESO = byteArrayOf(0x38, 0x42, 0x49, 0x4d, 0x03, 0xed.toByte())

        // private static methods

        /**
         * Reads a short from the InputStream.

         * @param    is        the InputStream
         * *
         * @return    an int
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun getShort(`is`: InputStream): Int {
            return (`is`.read() shl 8) + `is`.read()
        }

        /**
         * Returns a type of marker.

         * @param    marker      an int
         * *
         * @return    a type: VALID_MARKER, UNSUPPORTED_MARKER or NOPARAM_MARKER
         */
        private fun marker(marker: Int): Int {
            for (i in VALID_MARKERS.indices) {
                if (marker == VALID_MARKERS[i]) {
                    return VALID_MARKER
                }
            }
            for (i in NOPARAM_MARKERS.indices) {
                if (marker == NOPARAM_MARKERS[i]) {
                    return NOPARAM_MARKER
                }
            }
            for (i in UNSUPPORTED_MARKERS.indices) {
                if (marker == UNSUPPORTED_MARKERS[i]) {
                    return UNSUPPORTED_MARKER
                }
            }
            return NOT_A_MARKER
        }
    }
}
