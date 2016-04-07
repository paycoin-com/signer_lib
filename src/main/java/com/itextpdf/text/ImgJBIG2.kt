/*
 * $Id: 3810ef90118fb992b83fd2a482f2ea538cae90dd $
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

import java.net.URL
import java.security.MessageDigest

/**
 * Support for JBIG2 images.
 * @since 2.1.5
 */
class ImgJBIG2 : Image {

    /** JBIG2 globals  */
    /**
     * Getter for the JBIG2 global data.
     * @return    an array of bytes
     */
    var globalBytes: ByteArray? = null
        private set
    /** A unique hash  */
    /**
     * Getter for the unique hash.
     * @return    an array of bytes
     */
    var globalHash: ByteArray? = null
        private set

    /**
     * Copy contstructor.
     * @param    image another Image
     */
    internal constructor(image: Image) : super(image) {
    }

    /**
     * Empty constructor.
     */
    constructor() : super(null as Image) {
    }

    /**
     * Actual constructor for ImgJBIG2 images.
     * @param    width    the width of the image
     * *
     * @param    height    the height of the image
     * *
     * @param    data    the raw image data
     * *
     * @param    globals    JBIG2 globals
     */
    constructor(width: Int, height: Int, data: ByteArray, globals: ByteArray?) : super(null as URL) {
        type = Element.JBIG2
        originalType = Image.ORIGINAL_JBIG2
        scaledHeight = height.toFloat()
        top = scaledHeight
        scaledWidth = width.toFloat()
        right = scaledWidth
        bpc = 1
        colorspace = 1
        rawData = data
        plainWidth = width
        plainHeight = height
        if (globals != null) {
            this.globalBytes = globals
            val md: MessageDigest
            try {
                md = MessageDigest.getInstance("MD5")
                md.update(this.globalBytes)
                this.globalHash = md.digest()
            } catch (e: Exception) {
                //ignore
            }

        }
    }

}
