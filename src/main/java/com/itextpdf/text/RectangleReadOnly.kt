/*
 * $Id: 79e431efac6378009b1ee02d85d916d69eb46f71 $
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
import com.itextpdf.text.BaseColor

/**
 * A RectangleReadOnly is the representation of a geometric figure.
 * It's the same as a Rectangle but immutable.
 * Rectangles support constant width borders using
 * [.setBorderWidth]and [.setBorder].
 * They also support borders that vary in width/color on each side using
 * methods like [.setBorderWidthLeft]or
 * [.setBorderColorLeft].

 * @see Element

 * @since 2.1.2
 */

class RectangleReadOnly : Rectangle {

    // CONSTRUCTORS

    /**
     * Constructs a RectangleReadOnly -object.

     * @param llx    lower left x
     * *
     * @param lly    lower left y
     * *
     * @param urx    upper right x
     * *
     * @param ury    upper right y
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float) : super(llx, lly, urx, ury) {
    }

    /**
     * Constructs a RectangleReadOnly -object.

     * @param llx    lower left x
     * *
     * @param lly    lower left y
     * *
     * @param urx    upper right x
     * *
     * @param ury    upper right y
     * *
     * @param rotation    the rotation of the Rectangle (0, 90, 180, 270)
     * *
     * @since iText 5.0.6
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, rotation: Int) : super(llx, lly, urx, ury) {
        super.rotation = rotation
    }

    /**
     * Constructs a RectangleReadOnly-object starting from the origin
     * (0, 0).

     * @param urx    upper right x
     * *
     * @param ury    upper right y
     */
    constructor(urx: Float, ury: Float) : super(0f, 0f, urx, ury) {
    }

    /**
     * Constructs a RectangleReadOnly-object starting from the origin
     * (0, 0) and with a specific rotation (valid values are 0, 90, 180, 270).

     * @param urx    upper right x
     * *
     * @param ury    upper right y
     * *
     * @param rotation the rotation
     * *
     * @since iText 5.0.6
     */
    constructor(urx: Float, ury: Float, rotation: Int) : super(0f, 0f, urx, ury) {
        super.rotation = rotation
    }

    /**
     * Constructs a RectangleReadOnly -object.

     * @param rect    another Rectangle
     */
    constructor(rect: Rectangle) : super(rect.left, rect.bottom, rect.right, rect.top) {
        super.cloneNonPositionParameters(rect)
    }

    /**
     * Throws an error because of the read only nature of this object.
     */
    private fun throwReadOnlyError() {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("rectanglereadonly.this.rectangle.is.read.only"))
    }

    /**
     * Sets the rotation of the rectangle. Valid values are 0, 90, 180, and 270.
     * @param rotation the new rotation value
     * *
     * @since iText 5.0.6
     */
    override var rotation: Int
        get() = super.rotation
        set(rotation) = throwReadOnlyError()

    // OVERWRITE METHODS SETTING THE DIMENSIONS:

    /**
     * Sets the lower left x-coordinate.

     * @param llx    the new value
     */
    override var left: Float
        get() = super.left
        set(llx) = throwReadOnlyError()

    /**
     * Sets the upper right x-coordinate.

     * @param urx    the new value
     */

    override var right: Float
        get() = super.right
        set(urx) = throwReadOnlyError()

    /**
     * Sets the upper right y-coordinate.

     * @param ury    the new value
     */
    override var top: Float
        get() = super.top
        set(ury) = throwReadOnlyError()

    /**
     * Sets the lower left y-coordinate.

     * @param lly    the new value
     */
    override var bottom: Float
        get() = super.bottom
        set(lly) = throwReadOnlyError()

    /**
     * Normalizes the rectangle.
     * Switches lower left with upper right if necessary.
     */
    override fun normalize() {
        throwReadOnlyError()
    }

    // OVERWRITE METHODS SETTING THE BACKGROUND COLOR:

    /**
     * Sets the backgroundcolor of the rectangle.

     * @param value    the new value
     */
    override var backgroundColor: BaseColor
        get() = super.backgroundColor
        set(value) = throwReadOnlyError()

    /**
     * Sets the grayscale of the rectangle.

     * @param value    the new value
     */
    override var grayFill: Float
        get() = super.grayFill
        set(value) = throwReadOnlyError()

    // OVERWRITE METHODS SETTING THE BORDER:

    /**
     * Enables/Disables the border on the specified sides.
     * The border is specified as an integer bitwise combination of
     * the constants: LEFT, RIGHT, TOP, BOTTOM.

     * @see .enableBorderSide
     * @see .disableBorderSide
     * @param border    the new value
     */
    override var border: Int
        get() = super.border
        set(border) = throwReadOnlyError()

    /**
     * Sets a parameter indicating if the rectangle has variable borders

     * @param useVariableBorders    indication if the rectangle has variable borders
     */
    override var isUseVariableBorders: Boolean
        get() = super.isUseVariableBorders
        set(useVariableBorders) = throwReadOnlyError()

    /**
     * Enables the border on the specified side.

     * @param side    the side to enable.
     * * One of LEFT, RIGHT, TOP, BOTTOM
     */
    override fun enableBorderSide(side: Int) {
        throwReadOnlyError()
    }

    /**
     * Disables the border on the specified side.

     * @param side    the side to disable.
     * * One of LEFT, RIGHT, TOP, BOTTOM
     */
    override fun disableBorderSide(side: Int) {
        throwReadOnlyError()
    }

    // OVERWRITE METHODS SETTING THE BORDER WIDTH:

    /**
     * Sets the borderwidth of the table.

     * @param borderWidth    the new value
     */

    override var borderWidth: Float
        get() = super.borderWidth
        set(borderWidth) = throwReadOnlyError()

    /**
     * Sets the width of the left border

     * @param borderWidthLeft    a width
     */
    override fun setBorderWidthLeft(borderWidthLeft: Float) {
        throwReadOnlyError()
    }

    /**
     * Sets the width of the right border

     * @param borderWidthRight    a width
     */
    override fun setBorderWidthRight(borderWidthRight: Float) {
        throwReadOnlyError()
    }

    /**
     * Sets the width of the top border

     * @param borderWidthTop    a width
     */
    override fun setBorderWidthTop(borderWidthTop: Float) {
        throwReadOnlyError()
    }

    /**
     * Sets the width of the bottom border

     * @param borderWidthBottom    a width
     */
    override fun setBorderWidthBottom(borderWidthBottom: Float) {
        throwReadOnlyError()
    }

    // METHODS TO GET/SET THE BORDER COLOR:

    /**
     * Sets the color of the border.

     * @param borderColor    a BaseColor
     */

    override var borderColor: BaseColor
        get() = super.borderColor
        set(borderColor) = throwReadOnlyError()

    /**
     * Sets the color of the left border.

     * @param borderColorLeft    a BaseColor
     */
    override var borderColorLeft: BaseColor
        get() = super.borderColorLeft
        set(borderColorLeft) = throwReadOnlyError()

    /**
     * Sets the color of the right border

     * @param borderColorRight    a BaseColor
     */
    override var borderColorRight: BaseColor
        get() = super.borderColorRight
        set(borderColorRight) = throwReadOnlyError()

    /**
     * Sets the color of the top border.

     * @param borderColorTop    a BaseColor
     */
    override var borderColorTop: BaseColor
        get() = super.borderColorTop
        set(borderColorTop) = throwReadOnlyError()

    /**
     * Sets the color of the bottom border.

     * @param borderColorBottom    a BaseColor
     */
    override var borderColorBottom: BaseColor
        get() = super.borderColorBottom
        set(borderColorBottom) = throwReadOnlyError()

    // SPECIAL METHODS:

    /**
     * Copies each of the parameters, except the position, from a
     * Rectangle object

     * @param rect    Rectangle to copy from
     */
    override fun cloneNonPositionParameters(rect: Rectangle) {
        throwReadOnlyError()
    }

    /**
     * Copies each of the parameters, except the position, from a
     * Rectangle object if the value is set there.

     * @param rect    Rectangle to copy from
     */
    override fun softCloneNonPositionParameters(rect: Rectangle) {
        throwReadOnlyError()
    }

    /**
     * @return    String version of the most important rectangle properties
     * *
     * @see java.lang.Object.toString
     */
    override fun toString(): String {
        val buf = StringBuffer("RectangleReadOnly: ")
        buf.append(width)
        buf.append('x')
        buf.append(height)
        buf.append(" (rot: ")
        buf.append(rotation)
        buf.append(" degrees)")
        return buf.toString()
    }
}
