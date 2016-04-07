/*
 * $Id: 318fb7b5774348d8ed1efb93fec9c5c5661746d2 $
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

import com.itextpdf.awt.geom.AffineTransform
import com.itextpdf.text.*
import com.itextpdf.text.api.Spaceable
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.ArrayList
import java.util.HashMap

/**
 * A special element to put a collection of elements at an absolute position.
 */
class PdfDiv : Element, Spaceable, IAccessibleElement {
    enum class FloatType {
        NONE, LEFT, RIGHT
    }

    enum class PositionType {
        STATIC, ABSOLUTE, FIXED, RELATIVE
    }

    enum class DisplayType {
        NONE, BLOCK, INLINE, INLINE_BLOCK, INLINE_TABLE, LIST_ITEM, RUN_IN, TABLE, TABLE_CAPTION, TABLE_CELL, TABLE_COLUMN_GROUP, TABLE_COLUMN, TABLE_FOOTER_GROUP,
        TABLE_HEADER_GROUP, TABLE_ROW, TABLE_ROW_GROUP
    }

    enum class BorderTopStyle {
        DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET
    }

    var content: ArrayList<Element>? = null

    var left: Float? = null

    var top: Float? = null

    var right: Float? = null

    var bottom: Float? = null

    var width: Float? = null

    var height: Float? = null

    var percentageHeight: Float? = null

    var percentageWidth: Float? = null

    var contentWidth = 0f

    var contentHeight = 0f

    /**
     * Gets the alignment of this paragraph.

     * @return textAlignment
     */
    /**
     * Sets the alignment of this paragraph.

     * @param    textAlignment        the new alignment
     */
    var textAlignment = Element.ALIGN_UNDEFINED

    var paddingLeft = 0f

    var paddingRight = 0f

    override var paddingTop = 0f

    var paddingBottom = 0f

    var floatType = FloatType.NONE

    var position = PositionType.STATIC

    var display: DisplayType? = null

    private var floatLayout: FloatLayout? = null

    var borderTopStyle: BorderTopStyle? = null

    var yLine: Float = 0.toFloat()
        private set

    var runDirection = PdfWriter.RUN_DIRECTION_DEFAULT

    /**
     * Defines if the div should be kept on one page if possible
     */
    var keepTogether: Boolean = false

    override var role = PdfName.DIV
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()

    /**
     * IMPROTANT NOTE: be careful with this method because it would return correct result
     * only in case if [PdfDiv.layout]
     * was already called.
     * @return the actual height the div would require to layout it's content
     */

    val actualHeight: Float
        get() = if (height != null && height >= contentHeight) height else contentHeight

    /**
     * IMPROTANT NOTE: be careful with this method because it would return correct result
     * only in case if [PdfDiv.layout]
     * was already called.
     * @return the actual width the div would require to layout it's content
     */
    val actualWidth: Float
        get() = if (width != null && width >= contentWidth) width else contentWidth

    /**
     * Image will be scaled to fit in the div occupied area.
     */
    fun setBackgroundImage(image: Image) {
        this.backgroundImage = image
    }

    /**
     * Image will be scaled to fit in the div occupied area.
     */
    fun setBackgroundImage(image: Image, width: Float, height: Float) {
        this.backgroundImage = image
        this.backgroundImageWidth = width
        this.backgroundImageHeight = height
    }

    var backgroundColor: BaseColor? = null

    private var backgroundImage: Image? = null
    private var backgroundImageWidth: Float? = null
    private var backgroundImageHeight: Float? = null

    /**
     * The spacing before the table.
     */
    /**
     * Gets the spacing before this table.

     * @return    the spacing
     */
    /**
     * Sets the spacing before this table.

     * @param    spacing        the new spacing
     */
    override var spacingBefore: Float = 0.toFloat()

    /**
     * The spacing after the table.
     */
    /**
     * Gets the spacing after this table.

     * @return    the spacing
     */
    /**
     * Sets the spacing after this table.

     * @param    spacing        the new spacing
     */
    override var spacingAfter: Float = 0.toFloat()

    init {
        content = ArrayList<Element>()
        keepTogether = false
    }

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override fun getChunks(): List<Chunk> {
        return ArrayList()
    }

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.DIV
    }

    /**
     * @see com.itextpdf.text.Element.isContent
     * @since    iText 2.0.8
     */
    override fun isContent(): Boolean {
        return true
    }

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override fun isNestable(): Boolean {
        return true
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param    listener    an ElementListener
     * *
     * @return    true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            return listener.add(this)
        } catch (de: DocumentException) {
            return false
        }

    }

    fun addElement(element: Element) {
        content!!.add(element)
    }

    @Throws(DocumentException::class)
    fun layout(canvas: PdfContentByte, useAscender: Boolean, simulate: Boolean, llx: Float, lly: Float, urx: Float, ury: Float): Int {

        var leftX = Math.min(llx, urx)
        val maxY = Math.max(lly, ury)
        var minY = Math.min(lly, ury)
        var rightX = Math.max(llx, urx)
        yLine = maxY
        var contentCutByFixedHeight = false

        if (width != null && width > 0) {
            if (width < rightX - leftX) {
                rightX = leftX + width!!
            } else if (width > rightX - leftX) {
                return ColumnText.NO_MORE_COLUMN
            }
        } else if (percentageWidth != null) {
            contentWidth = (rightX - leftX) * percentageWidth!!
            rightX = leftX + contentWidth
        } else if (percentageWidth == null) {
            if (this.floatType == FloatType.NONE && (this.display == null ||
                    this.display == DisplayType.BLOCK || this.display == DisplayType.LIST_ITEM ||
                    this.display == DisplayType.RUN_IN)) {
                contentWidth = rightX - leftX
            }
        }

        if (height != null && height > 0) {
            if (height < maxY - minY) {
                minY = maxY - height!!
                contentCutByFixedHeight = true
            } else if (height > maxY - minY) {
                return ColumnText.NO_MORE_COLUMN
            }
        } else if (percentageHeight != null) {
            if (percentageHeight < 1.0) {
                contentCutByFixedHeight = true
            }
            contentHeight = (maxY - minY) * percentageHeight!!
            minY = maxY - contentHeight
        }

        if (!simulate && position == PdfDiv.PositionType.RELATIVE) {
            var translationX: Float? = null
            if (left != null) {
                translationX = left
            } else if (right != null) {
                translationX = (-right)!!
            } else {
                translationX = 0f
            }

            var translationY: Float? = null
            if (top != null) {
                translationY = (-top)!!
            } else if (bottom != null) {
                translationY = bottom
            } else {
                translationY = 0f
            }
            canvas.saveState()
            canvas.transform(AffineTransform(1f, 0f, 0f, 1f, translationX!!, translationY!!))
        }

        if (!simulate) {
            if ((backgroundColor != null || backgroundImage != null) && actualWidth > 0 && actualHeight > 0) {
                var backgroundWidth = actualWidth
                var backgroundHeight = actualHeight
                if (width != null) {
                    backgroundWidth = if (width > 0) width else 0
                }

                if (height != null) {
                    backgroundHeight = if (height > 0) height else 0
                }
                if (backgroundWidth > 0 && backgroundHeight > 0) {
                    val background = Rectangle(leftX, maxY - backgroundHeight, backgroundWidth + leftX, maxY)
                    if (backgroundColor != null) {
                        background.backgroundColor = backgroundColor
                        val artifact = PdfArtifact()
                        canvas.openMCBlock(artifact)
                        canvas.rectangle(background)
                        canvas.closeMCBlock(artifact)
                    }
                    if (backgroundImage != null) {
                        if (backgroundImageWidth == null) {
                            backgroundImage!!.scaleToFit(background)
                        } else {
                            backgroundImage!!.scaleAbsolute(backgroundImageWidth!!, backgroundImageHeight!!)
                        }
                        backgroundImage!!.setAbsolutePosition(background.left, background.bottom)
                        canvas.openMCBlock(backgroundImage)
                        canvas.addImage(backgroundImage)
                        canvas.closeMCBlock(backgroundImage)
                    }
                }
            }
        }

        if (percentageWidth == null) {
            contentWidth = 0f
        }
        if (percentageHeight == null) {
            contentHeight = 0f
        }

        minY += paddingBottom
        leftX += paddingLeft
        rightX -= paddingRight

        yLine -= paddingTop

        var status = ColumnText.NO_MORE_TEXT

        if (!content!!.isEmpty()) {
            if (this.floatLayout == null) {
                val floatingElements = ArrayList(content)
                floatLayout = FloatLayout(floatingElements, useAscender)
                floatLayout!!.runDirection = runDirection
            }

            floatLayout!!.setSimpleColumn(leftX, minY, rightX, yLine)
            if (borderTopStyle != null) {
                floatLayout!!.compositeColumn.isIgnoreSpacingBefore = false
            }

            status = floatLayout!!.layout(canvas, simulate)
            yLine = floatLayout!!.yLine
            if (percentageWidth == null && contentWidth < floatLayout!!.filledWidth) {
                contentWidth = floatLayout!!.filledWidth
            }
        }


        if (!simulate && position == PdfDiv.PositionType.RELATIVE) {
            canvas.restoreState()
        }

        yLine -= paddingBottom
        if (percentageHeight == null) {
            contentHeight = maxY - yLine
        }

        if (percentageWidth == null) {
            contentWidth += paddingLeft + paddingRight
        }

        return if (contentCutByFixedHeight) ColumnText.NO_MORE_TEXT else status
    }

    fun getAccessibleAttribute(key: PdfName): PdfObject? {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = false
}
