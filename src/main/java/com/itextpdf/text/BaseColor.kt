/*
 * $Id: 0554329475b6135abcf72f767bf64453ef11f1fb $
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

import com.itextpdf.text.error_messages.MessageLocalization

/**

 * @author psoares
 */
class BaseColor {
    /**
     * @return the combined color value
     */
    var rgb: Int = 0
        private set

    /**
     * Construct a new BaseColor.
     * @param red the value for the red gamma
     * *
     * @param green the value for the green gamma
     * *
     * @param blue the value for the blue gamma
     * *
     * @param alpha the value for the alpha gamma
     */
    @JvmOverloads constructor(red: Int, green: Int, blue: Int, alpha: Int = 255) {
        setValue(red, green, blue, alpha)
    }

    /**
     * Construct a BaseColor with float values.
     * @param red
     * *
     * @param green
     * *
     * @param blue
     * *
     * @param alpha
     */
    @JvmOverloads constructor(red: Float, green: Float, blue: Float, alpha: Float = 1f) : this((red * 255 + .5).toInt(), (green * 255 + .5).toInt(), (blue * 255 + .5).toInt(), (alpha * 255 + .5).toInt()) {
    }

    /**
     * Construct a BaseColor by setting the combined value.
     * @param argb
     */
    constructor(argb: Int) {
        rgb = argb
    }

    /**

     * @return the value for red
     */
    val red: Int
        get() = rgb shr 16 and 0xFF
    /**

     * @return the value for green
     */
    val green: Int
        get() = rgb shr 8 and 0xFF
    /**

     * @return the value for blue
     */
    val blue: Int
        get() = rgb shr 0 and 0xFF
    /**

     * @return the value for the alpha channel
     */
    val alpha: Int
        get() = rgb shr 24 and 0xff

    /**
     * Make this BaseColor brighter. Factor used is 0.7.
     * @return the new BaseColor
     */
    fun brighter(): BaseColor {
        var r = red
        var g = green
        var b = blue

        val i = (1.0 / (1.0 - FACTOR)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return BaseColor(i, i, i)
        }
        if (r > 0 && r < i)
            r = i
        if (g > 0 && g < i)
            g = i
        if (b > 0 && b < i)
            b = i

        return BaseColor(Math.min((r / FACTOR).toInt(), 255),
                Math.min((g / FACTOR).toInt(), 255),
                Math.min((b / FACTOR).toInt(), 255))
    }

    /**
     * Make this color darker. Factor used is 0.7
     * @return the new BaseColor
     */
    fun darker(): BaseColor {
        return BaseColor(Math.max((red * FACTOR).toInt(), 0),
                Math.max((green * FACTOR).toInt(), 0),
                Math.max((blue * FACTOR).toInt(), 0))
    }

    override fun equals(obj: Any?): Boolean {
        return obj is BaseColor && obj.rgb == this.rgb
    }

    override fun hashCode(): Int {
        return rgb
    }

    protected fun setValue(red: Int, green: Int, blue: Int, alpha: Int) {
        validate(red)
        validate(green)
        validate(blue)
        validate(alpha)
        rgb = alpha and 0xFF shl 24 or (red and 0xFF shl 16) or (green and 0xFF shl 8) or (blue and 0xFF shl 0)
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    override fun toString(): String {
        return "Color value[" + Integer.toString(rgb, 16) + "]"
    }

    companion object {
        val WHITE = BaseColor(255, 255, 255)
        val LIGHT_GRAY = BaseColor(192, 192, 192)
        val GRAY = BaseColor(128, 128, 128)
        val DARK_GRAY = BaseColor(64, 64, 64)
        val BLACK = BaseColor(0, 0, 0)
        val RED = BaseColor(255, 0, 0)
        val PINK = BaseColor(255, 175, 175)
        val ORANGE = BaseColor(255, 200, 0)
        val YELLOW = BaseColor(255, 255, 0)
        val GREEN = BaseColor(0, 255, 0)
        val MAGENTA = BaseColor(255, 0, 255)
        val CYAN = BaseColor(0, 255, 255)
        val BLUE = BaseColor(0, 0, 255)
        private val FACTOR = 0.7


        private fun validate(value: Int) {
            if (value < 0 || value > 255)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("color.value.outside.range.0.255"))
        }
    }
}
/**
 * @param red
 * *
 * @param green
 * *
 * @param blue
 */
/**
 * Construct a BaseColor with float values.
 * @param red
 * *
 * @param green
 * *
 * @param blue
 */
