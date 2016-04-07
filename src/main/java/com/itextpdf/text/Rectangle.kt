/*
 * $Id: 4a8f033551f2a55419be148465854c2b4e48062b $
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

import com.itextpdf.text.pdf.GrayColor

import java.util.ArrayList

/**
 * A Rectangle is the representation of a geometric figure.

 * Rectangles support constant width borders using
 * [.setBorderWidth]and [.setBorder].
 * They also support borders that vary in width/color on each side using
 * methods like [.setBorderWidthLeft]or
 * [.setBorderColorLeft].

 * @see Element
 */
open class Rectangle// CONSTRUCTORS:

/**
 * Constructs a Rectangle -object.

 * @param llx    lower left x
 * *
 * @param lly    lower left y
 * *
 * @param urx    upper right x
 * *
 * @param ury    upper right y
 */
(// MEMBER VARIABLES:

        /** the lower left x-coordinate.  */
        /**
         * Returns the lower left x-coordinate.

         * @return the lower left x-coordinate
         */
        // METHODS TO GET/SET THE DIMENSIONS:

        /**
         * Sets the lower left x-coordinate.

         * @param llx    the new value
         */
        open var left: Float,
        /** the lower left y-coordinate.  */
        /**
         * Returns the lower left y-coordinate.

         * @return the lower left y-coordinate
         */
        /**
         * Sets the lower left y-coordinate.

         * @param lly    the new value
         */
        open var bottom: Float,
        /** the upper right x-coordinate.  */
        /**
         * Returns the upper right x-coordinate.

         * @return the upper right x-coordinate
         */
        /**
         * Sets the upper right x-coordinate.

         * @param urx    the new value
         */
        open var right: Float,
        /** the upper right y-coordinate.  */
        /**
         * Returns the upper right y-coordinate.

         * @return the upper right y-coordinate
         */
        /**
         * Sets the upper right y-coordinate.

         * @param ury    the new value
         */
        open var top: Float) : Element {

    /** The rotation of the Rectangle  */
    // METHODS TO GET/SET THE ROTATION:

    /**
     * Gets the rotation of the rectangle

     * @return a rotation value
     */
    /**
     * Sets the rotation of the rectangle. Valid values are 0, 90, 180, and 270.
     * @param rotation the new rotation value
     * *
     * @since iText 5.0.6
     */
    open var rotation = 0
        set(rotation) {
            this.rotation = rotation % 360
            when (this.rotation) {
                90, 180, 270 -> {
                }
                else -> this.rotation = 0
            }
        }

    /** This is the color of the background of this rectangle.  */
    // METHODS TO GET/SET THE BACKGROUND COLOR:

    /**
     * Gets the backgroundcolor.

     * @return a BaseColor
     */
    /**
     * Sets the backgroundcolor of the rectangle.

     * @param backgroundColor    a BaseColor
     */

    open var backgroundColor: BaseColor? = null

    /** This represents the status of the 4 sides of the rectangle.  */
    //	 METHODS TO GET/SET THE BORDER:

    /**
     * Returns the exact type of the border.

     * @return a value
     */
    /**
     * Enables/Disables the border on the specified sides.
     * The border is specified as an integer bitwise combination of
     * the constants: LEFT, RIGHT, TOP, BOTTOM.

     * @see .enableBorderSide
     * @see .disableBorderSide
     * @param border    the new value
     */
    open var border = UNDEFINED

    /** Whether variable width/color borders are used.  */
    /**
     * Indicates whether variable width borders are being used.
     * Returns true if setBorderWidthLeft, setBorderWidthRight,
     * setBorderWidthTop, or setBorderWidthBottom has been called.

     * @return true if variable width borders are in use
     */
    /**
     * Sets a parameter indicating if the rectangle has variable borders

     * @param useVariableBorders indication if the rectangle has variable borders
     */
    open var isUseVariableBorders = false

    /** This is the width of the border around this rectangle.  */
    // METHODS TO GET/SET THE BORDER WIDTH:

    /**
     * Gets the borderwidth.

     * @return a value
     */
    /**
     * Sets the borderwidth of the table.

     * @param borderWidth the new value
     */
    open var borderWidth = UNDEFINED.toFloat()

    /** The width of the left border of this rectangle.  */
    protected var borderWidthLeft = UNDEFINED.toFloat()

    /** The width of the right border of this rectangle.  */
    protected var borderWidthRight = UNDEFINED.toFloat()

    /** The width of the top border of this rectangle.  */
    protected var borderWidthTop = UNDEFINED.toFloat()

    /** The width of the bottom border of this rectangle.  */
    protected var borderWidthBottom = UNDEFINED.toFloat()

    /** The color of the border of this rectangle.  */
    // METHODS TO GET/SET THE BORDER COLOR:

    /**
     * Gets the color of the border.

     * @return    a BaseColor
     */
    /**
     * Sets the color of the border.

     * @param borderColor a BaseColor
     */
    open var borderColor: BaseColor? = null

    /** The color of the left border of this rectangle.  */
    /**
     * Gets the color of the left border.

     * @return a BaseColor
     */
    /**
     * Sets the color of the left border.

     * @param borderColorLeft a BaseColor
     */
    open var borderColorLeft: BaseColor? = null
        get() {
            if (borderColorLeft == null)
                return borderColor
            return borderColorLeft
        }

    /** The color of the right border of this rectangle.  */
    /**
     * Gets the color of the right border.

     * @return a BaseColor
     */
    /**
     * Sets the color of the right border.

     * @param borderColorRight a BaseColor
     */
    open var borderColorRight: BaseColor? = null
        get() {
            if (borderColorRight == null)
                return borderColor
            return borderColorRight
        }

    /** The color of the top border of this rectangle.  */
    /**
     * Gets the color of the top border.

     * @return a BaseColor
     */
    /**
     * Sets the color of the top border.

     * @param borderColorTop a BaseColor
     */
    open var borderColorTop: BaseColor? = null
        get() {
            if (borderColorTop == null)
                return borderColor
            return borderColorTop
        }

    /** The color of the bottom border of this rectangle.  */
    /**
     * Gets the color of the bottom border.

     * @return a BaseColor
     */
    /**
     * Sets the color of the bottom border.

     * @param borderColorBottom a BaseColor
     */
    open var borderColorBottom: BaseColor? = null
        get() {
            if (borderColorBottom == null)
                return borderColor
            return borderColorBottom
        }

    /**
     * Constructs a Rectangle-object.

     * @param llx    lower left x
     * *
     * @param lly    lower left y
     * *
     * @param urx    upper right x
     * *
     * @param ury    upper right y
     * *
     * @param rotation the rotation (0, 90, 180, or 270)
     * *
     * @since iText 5.0.6
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, rotation: Int) : this(llx, lly, urx, ury) {
        rotation = rotation
    }

    /**
     * Constructs a Rectangle -object starting from the origin
     * (0, 0).

     * @param urx    upper right x
     * *
     * @param ury    upper right y
     */
    constructor(urx: Float, ury: Float) : this(0f, 0f, urx, ury) {
    }

    /**
     * Constructs a Rectangle-object starting from the origin
     * (0, 0) and with a specific rotation (valid values are 0, 90, 180, 270).

     * @param urx    upper right x
     * *
     * @param ury    upper right y
     * *
     * @param rotation the rotation of the rectangle
     * *
     * @since iText 5.0.6
     */
    constructor(urx: Float, ury: Float, rotation: Int) : this(0f, 0f, urx, ury, rotation) {
    }

    /**
     * Constructs a Rectangle -object.

     * @param rect    another Rectangle
     */
    constructor(rect: Rectangle) : this(rect.left, rect.bottom, rect.right, rect.top) {
        cloneNonPositionParameters(rect)
    }

    /**
     * Constructs a Rectangle-object based on a com.itextpdf.awt.geom.Rectangle object
     * @param rect com.itextpdf.awt.geom.Rectangle
     */
    constructor(rect: com.itextpdf.awt.geom.Rectangle) : this(rect.x as Float, rect.y as Float, (rect.x + rect.width).toFloat(), (rect.y + rect.height).toFloat()) {
    }

    // IMPLEMENTATION OF THE ELEMENT INTERFACE:e

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param listener    an ElementListener
     * *
     * @return true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            return listener.add(this)
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets the type of the text element.

     * @return a type
     */
    override fun type(): Int {
        return Element.RECTANGLE
    }

    /**
     * Gets all the chunks in this element.

     * @return an ArrayList
     */
    override val chunks: List<Chunk>
        get() = ArrayList<Chunk>()

    /**
     * @see com.itextpdf.text.Element.isContent
     * @since    iText 2.0.8
     */
    override val isContent: Boolean
        get() = true

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override val isNestable: Boolean
        get() = false

    /**
     * Returns the lower left x-coordinate, considering a given margin.

     * @param margin    a margin
     * *
     * @return the lower left x-coordinate
     */
    fun getLeft(margin: Float): Float {
        return left + margin
    }

    /**
     * Returns the upper right x-coordinate, considering a given margin.

     * @param margin    a margin
     * *
     * @return the upper right x-coordinate
     */
    fun getRight(margin: Float): Float {
        return right - margin
    }

    /**
     * Returns the width of the rectangle.

     * @return    the width
     */
    val width: Float
        get() = right - left

    /**
     * Returns the upper right y-coordinate, considering a given margin.

     * @param margin    a margin
     * *
     * @return the upper right y-coordinate
     */
    fun getTop(margin: Float): Float {
        return top - margin
    }

    /**
     * Returns the lower left y-coordinate, considering a given margin.

     * @param margin    a margin
     * *
     * @return the lower left y-coordinate
     */
    fun getBottom(margin: Float): Float {
        return bottom + margin
    }

    /**
     * Returns the height of the rectangle.

     * @return the height
     */
    val height: Float
        get() = top - bottom

    /**
     * Normalizes the rectangle.
     * Switches lower left with upper right if necessary.
     */
    open fun normalize() {
        if (left > right) {
            val a = left
            left = right
            right = a
        }
        if (bottom > top) {
            val a = bottom
            bottom = top
            top = a
        }
    }

    /**
     * Rotates the rectangle.
     * Swaps the values of llx and lly and of urx and ury.

     * @return the rotated Rectangle
     */
    fun rotate(): Rectangle {
        val rect = Rectangle(bottom, left, top, right)
        rect.rotation = rotation + 90
        return rect
    }

    /**
     * Gets the grayscale.

     * @return the grayscale color of the background
     * * or 0 if the background has no grayscale color.
     */
    /**
     * Sets the the background color to a grayscale value.

     * @param value    the new grayscale value
     */
    open var grayFill: Float
        get() {
            if (backgroundColor is GrayColor)
                return (backgroundColor as GrayColor).gray
            return 0f
        }
        set(value) {
            backgroundColor = GrayColor(value)
        }

    /**
     * Indicates whether some type of border is set.

     * @return a boolean
     */
    fun hasBorders(): Boolean {
        when (border) {
            UNDEFINED, NO_BORDER -> return false
            else -> return borderWidth > 0 || borderWidthLeft > 0
                    || borderWidthRight > 0 || borderWidthTop > 0 || borderWidthBottom > 0
        }
    }

    /**
     * Indicates whether the specified type of border is set.

     * @param type    the type of border
     * *
     * @return a boolean
     */
    fun hasBorder(type: Int): Boolean {
        if (border == UNDEFINED)
            return false
        return border and type == type
    }

    /**
     * Enables the border on the specified side.

     * @param side    the side to enable.
     * * One of LEFT, RIGHT, TOP, BOTTOM
     */
    open fun enableBorderSide(side: Int) {
        if (border == UNDEFINED)
            border = 0
        border = border or side
    }

    /**
     * Disables the border on the specified side.

     * @param side    the side to disable.
     * * One of LEFT, RIGHT, TOP, BOTTOM
     */
    open fun disableBorderSide(side: Int) {
        if (border == UNDEFINED)
            border = 0
        border = border and side.inv()
    }

    /**
     * Helper function returning the border width of a specific side.

     * @param    variableWidthValue    a variable width (could be undefined)
     * *
     * @param    side    the border you want to check
     * *
     * @return    the variableWidthValue if not undefined, otherwise the borderWidth
     */
    private fun getVariableBorderWidth(variableWidthValue: Float, side: Int): Float {
        if (border and side != 0)
            return if (variableWidthValue != UNDEFINED.toFloat()) variableWidthValue else borderWidth
        return 0f
    }

    /**
     * Helper function updating the border flag for a side
     * based on the specified width.
     * A width of 0 will disable the border on that side.
     * Any other width enables it.

     * @param width    width of border
     * *
     * @param side    border side constant
     */
    private fun updateBorderBasedOnWidth(width: Float, side: Int) {
        isUseVariableBorders = true
        if (width > 0)
            enableBorderSide(side)
        else
            disableBorderSide(side)
    }

    /**
     * Gets the width of the left border.

     * @return a width
     */
    fun getBorderWidthLeft(): Float {
        return getVariableBorderWidth(borderWidthLeft, LEFT)
    }

    /**
     * Sets the width of the left border.

     * @param borderWidthLeft a width
     */
    open fun setBorderWidthLeft(borderWidthLeft: Float) {
        this.borderWidthLeft = borderWidthLeft
        updateBorderBasedOnWidth(borderWidthLeft, LEFT)
    }

    /**
     * Gets the width of the right border.

     * @return a width
     */
    fun getBorderWidthRight(): Float {
        return getVariableBorderWidth(borderWidthRight, RIGHT)
    }

    /**
     * Sets the width of the right border.

     * @param borderWidthRight a width
     */
    open fun setBorderWidthRight(borderWidthRight: Float) {
        this.borderWidthRight = borderWidthRight
        updateBorderBasedOnWidth(borderWidthRight, RIGHT)
    }

    /**
     * Gets the width of the top border.

     * @return a width
     */
    fun getBorderWidthTop(): Float {
        return getVariableBorderWidth(borderWidthTop, TOP)
    }

    /**
     * Sets the width of the top border.

     * @param borderWidthTop a width
     */
    open fun setBorderWidthTop(borderWidthTop: Float) {
        this.borderWidthTop = borderWidthTop
        updateBorderBasedOnWidth(borderWidthTop, TOP)
    }

    /**
     * Gets the width of the bottom border.

     * @return a width
     */
    fun getBorderWidthBottom(): Float {
        return getVariableBorderWidth(borderWidthBottom, BOTTOM)
    }

    /**
     * Sets the width of the bottom border.

     * @param borderWidthBottom a width
     */
    open fun setBorderWidthBottom(borderWidthBottom: Float) {
        this.borderWidthBottom = borderWidthBottom
        updateBorderBasedOnWidth(borderWidthBottom, BOTTOM)
    }

    // SPECIAL METHODS:

    /**
     * Gets a Rectangle that is altered to fit on the page.

     * @param top        the top position
     * *
     * @param bottom    the bottom position
     * *
     * @return a Rectangle
     */
    fun rectangle(top: Float, bottom: Float): Rectangle {
        val tmp = Rectangle(this)
        if (top > top) {
            tmp.top = top
            tmp.disableBorderSide(TOP)
        }
        if (bottom < bottom) {
            tmp.bottom = bottom
            tmp.disableBorderSide(BOTTOM)
        }
        return tmp
    }

    /**
     * Copies each of the parameters, except the position, from a
     * Rectangle object

     * @param rect    Rectangle to copy from
     */
    open fun cloneNonPositionParameters(rect: Rectangle) {
        this.rotation = rect.rotation
        this.backgroundColor = rect.backgroundColor
        this.border = rect.border
        this.isUseVariableBorders = rect.isUseVariableBorders
        this.borderWidth = rect.borderWidth
        this.borderWidthLeft = rect.borderWidthLeft
        this.borderWidthRight = rect.borderWidthRight
        this.borderWidthTop = rect.borderWidthTop
        this.borderWidthBottom = rect.borderWidthBottom
        this.borderColor = rect.borderColor
        this.borderColorLeft = rect.borderColorLeft
        this.borderColorRight = rect.borderColorRight
        this.borderColorTop = rect.borderColorTop
        this.borderColorBottom = rect.borderColorBottom
    }

    /**
     * Copies each of the parameters, except the position, from a
     * Rectangle object if the value is set there

     * @param rect Rectangle to copy from
     */
    open fun softCloneNonPositionParameters(rect: Rectangle) {
        if (rect.rotation != 0)
            this.rotation = rect.rotation
        if (rect.backgroundColor != null)
            this.backgroundColor = rect.backgroundColor
        if (rect.border != UNDEFINED)
            this.border = rect.border
        if (isUseVariableBorders)
            this.isUseVariableBorders = rect.isUseVariableBorders
        if (rect.borderWidth != UNDEFINED.toFloat())
            this.borderWidth = rect.borderWidth
        if (rect.borderWidthLeft != UNDEFINED.toFloat())
            this.borderWidthLeft = rect.borderWidthLeft
        if (rect.borderWidthRight != UNDEFINED.toFloat())
            this.borderWidthRight = rect.borderWidthRight
        if (rect.borderWidthTop != UNDEFINED.toFloat())
            this.borderWidthTop = rect.borderWidthTop
        if (rect.borderWidthBottom != UNDEFINED.toFloat())
            this.borderWidthBottom = rect.borderWidthBottom
        if (rect.borderColor != null)
            this.borderColor = rect.borderColor
        if (rect.borderColorLeft != null)
            this.borderColorLeft = rect.borderColorLeft
        if (rect.borderColorRight != null)
            this.borderColorRight = rect.borderColorRight
        if (rect.borderColorTop != null)
            this.borderColorTop = rect.borderColorTop
        if (rect.borderColorBottom != null)
            this.borderColorBottom = rect.borderColorBottom
    }

    /**
     * @return    a String representation of the rectangle
     * *
     * @see java.lang.Object.toString
     */
    override fun toString(): String {
        val buf = StringBuffer("Rectangle: ")
        buf.append(width)
        buf.append('x')
        buf.append(height)
        buf.append(" (rot: ")
        buf.append(rotation)
        buf.append(" degrees)")
        return buf.toString()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is Rectangle) {
            // should we normalize here?
            // normalization changes the structure and coordinates of the rectangle, so I'm inclined not to call normalize()
            // but it needs to be considered ~ Michaël D.
            return obj.left == this.left && obj.bottom == this.bottom && obj.right == this.right && obj.top == this.top && obj.rotation == this.rotation
        } else {
            return false
        }
    }

    companion object {

        // CONSTANTS:

        /** This is the value that will be used as undefined .  */
        val UNDEFINED = -1

        /** This represents one side of the border of the Rectangle.  */
        val TOP = 1

        /** This represents one side of the border of the Rectangle.  */
        val BOTTOM = 2

        /** This represents one side of the border of the Rectangle.  */
        val LEFT = 4

        /** This represents one side of the border of the Rectangle.  */
        val RIGHT = 8

        /** This represents a rectangle without borders.  */
        val NO_BORDER = 0

        /** This represents a type of border.  */
        val BOX = TOP + BOTTOM + LEFT + RIGHT
    }
}
