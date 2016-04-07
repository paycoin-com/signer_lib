/*
 * $Id: 09268fe7772ef537af04708b02ea0efd76ded6df $
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

import com.itextpdf.text.*
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.events.PdfPCellEventForwarder
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.ArrayList
import java.util.HashMap

/**
 * A cell in a PdfPTable.
 */
open class PdfPCell : Rectangle, IAccessibleElement {

    /**
     * Gets the ColumnText with the content of the cell.

     * @return a columntext object
     */
    /**
     * Sets the columntext in the cell.

     * @param column
     */
    var column: ColumnText? = ColumnText(null)

    /**
     * Vertical alignment of the cell.
     */
    /**
     * Gets the vertical alignment for the cell.

     * @return the vertical alignment for the cell
     */
    /**
     * Sets the vertical alignment for the cell. It could be
     * Element.ALIGN_MIDDLE for example.

     * @param verticalAlignment The vertical alignment
     */
    var verticalAlignment = Element.ALIGN_TOP
        set(verticalAlignment) {
            if (table != null) {
                table!!.isExtendLastRow = verticalAlignment == Element.ALIGN_TOP
            }
            this.verticalAlignment = verticalAlignment
        }

    /**
     * Left padding of the cell.
     */
    /**
     * @return Value of property paddingLeft.
     */
    /**
     * Setter for property paddingLeft.

     * @param paddingLeft New value of property paddingLeft.
     */
    var paddingLeft = 2f

    /**
     * Right padding of the cell.
     */
    /**
     * Getter for property paddingRight.

     * @return Value of property paddingRight.
     */
    /**
     * Setter for property paddingRight.

     * @param paddingRight New value of property paddingRight.
     */
    var paddingRight = 2f

    /**
     * Top padding of the cell.
     */
    /**
     * Getter for property paddingTop.

     * @return Value of property paddingTop.
     */
    /**
     * Setter for property paddingTop.

     * @param paddingTop New value of property paddingTop.
     */
    var paddingTop = 2f

    /**
     * Bottom padding of the cell.
     */
    /**
     * Getter for property paddingBottom.

     * @return Value of property paddingBottom.
     */
    /**
     * Setter for property paddingBottom.

     * @param paddingBottom New value of property paddingBottom.
     */
    var paddingBottom = 2f

    /**
     * Fixed height of the cell.
     */
    /**
     * Get the fixed height of the cell.

     * @return Value of property fixedHeight.
     */
    /**
     * Set a fixed height for the cell. This will automatically unset
     * minimumHeight, if set.

     * @param fixedHeight New value of property fixedHeight.
     */
    var fixedHeight = 0f
        set(fixedHeight) {
            this.fixedHeight = fixedHeight
            minimumHeight = 0f
        }

    /**
     * Fixed height of the cell.
     */
    /**
     * Get the calculated height of the cell.

     * @return Value of property calculatedHeight.
     */
    /**
     * Set a calculated height for the cell.

     * @param calculatedHeight New value of property calculatedHeight.
     */
    var calculatedHeight = 0f

    /**
     * Minimum height of the cell.
     */
    /**
     * Get the minimum height of the cell.

     * @return Value of property minimumHeight.
     */
    /**
     * Set a minimum height for the cell. This will automatically unset
     * fixedHeight, if set.

     * @param minimumHeight New value of property minimumHeight.
     */
    var minimumHeight: Float = 0.toFloat()
        set(minimumHeight) {
            this.minimumHeight = minimumHeight
            fixedHeight = 0f
        }

    /**
     * This field is used to cache the height which is calculated on getMaxHeight() method call;
     * this helps to avoid unnecessary recalculations on table drawing.
     */
    /**
     * Gets the height which was calculated on last call of getMaxHeight().
     * If cell's bBox and content wasn't changed this value is actual maxHeight of the cell.
     * @return max height which was calculated on last call of getMaxHeight(); if getMaxHeight() wasn't called the return value is 0
     */
    var cachedMaxHeight: Float = 0.toFloat()
        private set

    /**
     * Holds value of property noWrap.
     */
    /**
     * Getter for property noWrap.

     * @return Value of property noWrap.
     */
    /**
     * Setter for property noWrap.

     * @param noWrap New value of property noWrap.
     */
    var isNoWrap = false

    /**
     * Holds value of property table.
     */
    /**
     * Getter for property table.

     * @return Value of property table.
     * *
     * @since 2.x
     */
    var table: PdfPTable? = null
        internal set(table) {
            this.table = table
            column.setText(null)
            image = null
            if (table != null) {
                table.isExtendLastRow = verticalAlignment == Element.ALIGN_TOP
                column.addElement(table)
                table.widthPercentage = 100
            }
        }

    /**
     * Holds value of property colspan.
     */
    /**
     * Getter for property colspan.

     * @return Value of property colspan.
     */
    /**
     * Setter for property colspan.

     * @param colspan New value of property colspan.
     */
    var colspan = 1

    /**
     * Holds value of property rowspan.

     * @since    2.1.6
     */
    /**
     * Getter for property rowspan.

     * @return Value of property rowspan.
     * *
     * @since    2.1.6
     */
    /**
     * Setter for property rowspan.

     * @param rowspan New value of property rowspan.
     * *
     * @since    2.1.6
     */
    var rowspan = 1

    /**
     * Holds value of property image.
     */
    /**
     * Getter for property image.

     * @return Value of property image.
     */
    /**
     * Setter for property image.

     * @param image New value of property image.
     */
    var image: Image? = null
        set(image) {
            column.setText(null)
            table = null
            this.image = image
        }

    /**
     * Holds value of property cellEvent.
     */
    /**
     * Gets the cell event for this cell.

     * @return the cell event
     */
    /**
     * Sets the cell event for this cell.

     * @param cellEvent the cell event
     */
    var cellEvent: PdfPCellEvent? = null
        set(cellEvent) = if (cellEvent == null) {
            this.cellEvent = null
        } else if (this.cellEvent == null) {
            this.cellEvent = cellEvent
        } else if (this.cellEvent is PdfPCellEventForwarder) {
            (this.cellEvent as PdfPCellEventForwarder).addCellEvent(cellEvent)
        } else {
            val forward = PdfPCellEventForwarder()
            forward.addCellEvent(this.cellEvent)
            forward.addCellEvent(cellEvent)
            this.cellEvent = forward
        }

    /**
     * Holds value of property useDescender.
     */
    /**
     * Getter for property useDescender.

     * @return Value of property useDescender.
     */
    /**
     * Setter for property useDescender.

     * @param useDescender New value of property useDescender.
     */
    var isUseDescender = false

    /**
     * Increases padding to include border if true
     */
    /**
     * If true, then effective padding will include border widths

     * @return true if effective padding includes border widths
     */
    /**
     * Adjusts effective padding to include border widths.

     * @param use adjust effective padding if true
     */
    var isUseBorderPadding = false

    /**
     * The text in the cell.
     */
    /**
     * Gets the Phrase from this cell.

     * @return the Phrase
     */
    /**
     * Sets the Phrase for this cell.

     * @param phrase the Phrase
     */
    var phrase: Phrase
        set(phrase) {
            table = null
            image = null
            column.setText(this.phrase = phrase)
        }

    /**
     * The rotation of the cell. Possible values are 0, 90, 180 and 270.
     */
    private var rotation: Int = 0

    override var role = PdfName.TD
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()

    var headers: ArrayList<PdfPHeaderCell>? = null
        protected set

    /**
     * Constructs an empty PdfPCell. The default padding is 2.
     */
    constructor() : super(0f, 0f, 0f, 0f) {
        borderWidth = 0.5f
        border = Rectangle.BOX
        column.setLeading(0f, 1f)
    }

    /**
     * Constructs a PdfPCell with a Phrase. The
     * default padding is 2.

     * @param phrase the text
     */
    constructor(phrase: Phrase) : super(0f, 0f, 0f, 0f) {
        borderWidth = 0.5f
        border = Rectangle.BOX
        column.addText(this.phrase = phrase)
        column.setLeading(0f, 1f)
    }

    /**
     * Constructs a PdfPCell with an Image. The
     * default padding is 0.25 for a border width of 0.5.

     * @param image the Image
     * *
     * @param fit true to fit the image to the cell
     */
    @JvmOverloads constructor(image: Image, fit: Boolean = false) : super(0f, 0f, 0f, 0f) {
        borderWidth = 0.5f
        border = Rectangle.BOX
        column.setLeading(0f, 1f)
        if (fit) {
            this.image = image
            setPadding(borderWidth / 2)
        } else {
            image.isScaleToFitLineWhenOverflow = false
            column.addText(this.phrase = Phrase(Chunk(image, 0f, 0f, true)))
            setPadding(0f)
        }
    }

    /**
     * Constructs a PdfPCell with a PdfPtable. This
     * constructor allows nested tables.

     * @param table The PdfPTable
     * *
     * @param style    The style to apply to the cell (you could use
     * * getDefaultCell())
     * *
     * @since 2.1.0
     */
    @JvmOverloads constructor(table: PdfPTable, style: PdfPCell? = null) : super(0f, 0f, 0f, 0f) {
        borderWidth = 0.5f
        border = Rectangle.BOX
        column.setLeading(0f, 1f)
        this.table = table
        table.widthPercentage = 100
        table.isExtendLastRow = true
        column.addElement(table)
        if (style != null) {
            cloneNonPositionParameters(style)
            verticalAlignment = style.verticalAlignment
            paddingLeft = style.paddingLeft
            paddingRight = style.paddingRight
            paddingTop = style.paddingTop
            paddingBottom = style.paddingBottom
            colspan = style.colspan
            rowspan = style.rowspan
            cellEvent = style.cellEvent
            isUseDescender = style.isUseDescender
            isUseBorderPadding = style.isUseBorderPadding
            rotation = style.rotation
        } else {
            setPadding(0f)
        }
    }

    /**
     * Constructs a deep copy of a PdfPCell.

     * @param cell the PdfPCell to duplicate
     */
    constructor(cell: PdfPCell) : super(cell.llx, cell.lly, cell.urx, cell.ury) {
        cloneNonPositionParameters(cell)
        verticalAlignment = cell.verticalAlignment
        paddingLeft = cell.paddingLeft
        paddingRight = cell.paddingRight
        paddingTop = cell.paddingTop
        paddingBottom = cell.paddingBottom
        phrase = cell.phrase
        fixedHeight = cell.fixedHeight
        minimumHeight = cell.minimumHeight
        isNoWrap = cell.isNoWrap
        colspan = cell.colspan
        rowspan = cell.rowspan
        if (cell.table != null) {
            table = PdfPTable(cell.table)
        }
        image = Image.getInstance(cell.image)
        cellEvent = cell.cellEvent
        isUseDescender = cell.isUseDescender
        column = ColumnText.duplicate(cell.column)
        isUseBorderPadding = cell.isUseBorderPadding
        rotation = cell.rotation
        id = cell.id
        role = cell.role
        if (cell.accessibleAttributes != null) {
            accessibleAttributes = HashMap(cell.accessibleAttributes)
        }
        headers = cell.headers
    }

    /**
     * Adds an iText element to the cell.

     * @param element
     */
    fun addElement(element: Element) {
        if (table != null) {
            table = null
            column.setText(null)
        }
        if (element is PdfPTable) {
            element.isSplitLate = false
        } else if (element is PdfDiv) {
            for (divChildElement in element.content) {
                if (divChildElement is PdfPTable) {
                    divChildElement.isSplitLate = false
                }
            }
        }
        column.addElement(element)
    }

    /**
     * Gets the horizontal alignment for the cell.

     * @return the horizontal alignment for the cell
     */
    /**
     * Sets the horizontal alignment for the cell. It could be
     * Element.ALIGN_CENTER for example.

     * @param horizontalAlignment The horizontal alignment
     */
    var horizontalAlignment: Int
        get() = column.alignment
        set(horizontalAlignment) {
            column.alignment = horizontalAlignment
        }

    /**
     * Gets the effective left padding. This will include the left border width
     * if [.isUseBorderPadding] is true.

     * @return effective value of property paddingLeft.
     */
    val effectivePaddingLeft: Float
        get() {
            if (isUseBorderPadding) {
                val border = getBorderWidthLeft() / if (isUseVariableBorders) 1f else 2f
                return paddingLeft + border
            }
            return paddingLeft
        }

    /**
     * Gets the effective right padding. This will include the right border
     * width if [.isUseBorderPadding] is true.

     * @return effective value of property paddingRight.
     */
    val effectivePaddingRight: Float
        get() {
            if (isUseBorderPadding) {
                val border = getBorderWidthRight() / if (isUseVariableBorders) 1f else 2f
                return paddingRight + border
            }
            return paddingRight
        }

    /**
     * Gets the effective top padding. This will include the top border width if
     * [.isUseBorderPadding] is true.

     * @return effective value of property paddingTop.
     */
    val effectivePaddingTop: Float
        get() {
            if (isUseBorderPadding) {
                val border = getBorderWidthTop() / if (isUseVariableBorders) 1f else 2f
                return paddingTop + border
            }
            return paddingTop
        }

    /**
     * Gets the effective bottom padding. This will include the bottom border
     * width if [.isUseBorderPadding] is true.

     * @return effective value of property paddingBottom.
     */
    val effectivePaddingBottom: Float
        get() {
            if (isUseBorderPadding) {
                val border = getBorderWidthBottom() / if (isUseVariableBorders) 1f else 2f
                return paddingBottom + border
            }
            return paddingBottom
        }

    /**
     * Sets the padding of the contents in the cell (space between content and
     * border).

     * @param padding
     */
    fun setPadding(padding: Float) {
        paddingBottom = padding
        paddingTop = padding
        paddingLeft = padding
        paddingRight = padding
    }

    /**
     * Sets the leading fixed and variable. The resultant leading will be:
     * fixedLeading+multipliedLeading*maxFontSize where maxFontSize is the size
     * of the biggest font in the line.

     * @param fixedLeading the fixed leading
     * *
     * @param multipliedLeading the variable leading
     */
    fun setLeading(fixedLeading: Float, multipliedLeading: Float) {
        column.setLeading(fixedLeading, multipliedLeading)
    }

    /**
     * Gets the fixed leading.

     * @return the leading
     */
    val leading: Float
        get() = column.leading

    /**
     * Gets the variable leading.

     * @return the leading
     */
    val multipliedLeading: Float
        get() = column.multipliedLeading

    /**
     * Gets the first paragraph line indent.

     * @return the indent
     */
    /**
     * Sets the first paragraph line indent.

     * @param indent the indent
     */
    var indent: Float
        get() = column.indent
        set(indent) {
            column.indent = indent
        }

    /**
     * Gets the extra space between paragraphs.

     * @return the extra space between paragraphs
     */
    /**
     * Sets the extra space between paragraphs.

     * @param extraParagraphSpace the extra space between paragraphs
     */
    var extraParagraphSpace: Float
        get() = column.extraParagraphSpace
        set(extraParagraphSpace) {
            column.extraParagraphSpace = extraParagraphSpace
        }

    /**
     * Tells you whether the height was calculated.

     * @return    true if the height was calculated.
     */
    fun hasCalculatedHeight(): Boolean {
        return calculatedHeight > 0
    }

    /**
     * Tells you whether the cell has a fixed height.

     * @return    true is a fixed height was set.
     * *
     * @since 2.1.5
     */
    fun hasFixedHeight(): Boolean {
        return fixedHeight > 0
    }

    fun hasCachedMaxHeight(): Boolean {
        return cachedMaxHeight > 0
    }

    /**
     * Tells you whether the cell has a minimum height.

     * @return    true if a minimum height was set.
     * *
     * @since 2.1.5
     */
    fun hasMinimumHeight(): Boolean {
        return minimumHeight > 0
    }

    /**
     * Gets the following paragraph lines indent.

     * @return the indent
     */
    /**
     * Sets the following paragraph lines indent.

     * @param indent the indent
     */
    var followingIndent: Float
        get() = column.followingIndent
        set(indent) {
            column.followingIndent = indent
        }

    /**
     * Gets the right paragraph lines indent.

     * @return the indent
     */
    /**
     * Sets the right paragraph lines indent.

     * @param indent the indent
     */
    var rightIndent: Float
        get() = column.rightIndent
        set(indent) {
            column.rightIndent = indent
        }

    /**
     * Gets the space/character extra spacing ratio for fully justified text.

     * @return the space/character extra spacing ratio
     */
    /**
     * Sets the ratio between the extra word spacing and the extra character
     * spacing when the text is fully justified. Extra word spacing will grow
     * spaceCharRatio times more than extra character spacing. If
     * the ratio is PdfWriter.NO_SPACE_CHAR_RATIO then the extra
     * character spacing will be zero.

     * @param spaceCharRatio the ratio between the extra word spacing and the
     * * extra character spacing
     */
    var spaceCharRatio: Float
        get() = column.spaceCharRatio
        set(spaceCharRatio) {
            column.spaceCharRatio = spaceCharRatio
        }

    /**
     * Gets the run direction of the text content in the cell

     * @return One of the following values: PdfWriter.RUN_DIRECTION_DEFAULT,
     * * PdfWriter.RUN_DIRECTION_NO_BIDI, PdfWriter.RUN_DIRECTION_LTR or
     * * PdfWriter.RUN_DIRECTION_RTL.
     */
    /**
     * Sets the run direction of the text content in the cell. May be either of:
     * PdfWriter.RUN_DIRECTION_DEFAULT, PdfWriter.RUN_DIRECTION_NO_BIDI,
     * PdfWriter.RUN_DIRECTION_LTR or PdfWriter.RUN_DIRECTION_RTL.

     * @param runDirection
     */
    var runDirection: Int
        get() = column.runDirection
        set(runDirection) {
            column.runDirection = runDirection
        }

    /**
     * Gets the arabic shaping options.

     * @return the arabic shaping options
     */
    /**
     * Sets the arabic shaping options. The option can be AR_NOVOWEL,
     * AR_COMPOSEDTASHKEEL and AR_LIG.

     * @param arabicOptions the arabic shaping options
     */
    var arabicOptions: Int
        get() = column.arabicOptions
        set(arabicOptions) {
            column.arabicOptions = arabicOptions
        }

    /**
     * Gets state of first line height based on max ascender

     * @return true if an ascender is to be used.
     */
    /**
     * Enables/ Disables adjustment of first line height based on max ascender.

     * @param useAscender adjust height if true
     */
    var isUseAscender: Boolean
        get() = column.isUseAscender
        set(useAscender) {
            column.isUseAscender = useAscender
        }

    /**
     * Returns the list of composite elements of the column.

     * @return    a List object.
     * *
     * @since    2.1.1
     */
    val compositeElements: List<Element>
        get() = column!!.compositeElements

    /**
     * Gets the rotation of the cell.

     * @return the rotation of the cell.
     */
    override fun getRotation(): Int {
        return rotation
    }

    /**
     * Sets the rotation of the cell. Possible values are 0, 90, 180 and 270.

     * @param rotation the rotation of the cell
     */
    override fun setRotation(rotation: Int) {
        var rotation = rotation
        rotation %= 360
        if (rotation < 0) {
            rotation += 360
        }
        if (rotation % 90 != 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("rotation.must.be.a.multiple.of.90"))
        }
        this.rotation = rotation
    }

    /**
     * Returns the height of the cell.

     * @return    the height of the cell
     * *
     * @since    3.0.0
     */
    val maxHeight: Float
        get() {
            val pivoted = getRotation() == 90 || getRotation() == 270
            val img = image
            if (img != null) {
                img.scalePercent(100f)
                val refWidth = if (pivoted) img.scaledHeight else img.scaledWidth
                val scale = (right - effectivePaddingRight
                        - effectivePaddingLeft - left) / refWidth
                img.scalePercent(scale * 100)
                val refHeight = if (pivoted) img.scaledWidth else img.scaledHeight
                bottom = top - effectivePaddingTop - effectivePaddingBottom - refHeight
            } else {
                if (pivoted && hasFixedHeight() || column == null) {
                    bottom = top - fixedHeight
                } else {
                    val ct = ColumnText.duplicate(column)
                    val right: Float
                    val top: Float
                    val left: Float
                    val bottom: Float
                    if (pivoted) {
                        right = PdfPRow.RIGHT_LIMIT
                        top = getRight() - effectivePaddingRight
                        left = 0f
                        bottom = getLeft() + effectivePaddingLeft
                    } else {
                        right = if (isNoWrap) PdfPRow.RIGHT_LIMIT else getRight() - effectivePaddingRight
                        top = getTop() - effectivePaddingTop
                        left = getLeft() + effectivePaddingLeft
                        bottom = if (hasCalculatedHeight()) getTop() + effectivePaddingBottom - calculatedHeight else PdfPRow.BOTTOM_LIMIT
                    }
                    PdfPRow.setColumn(ct, left, bottom, right, top)
                    try {
                        ct.go(true)
                    } catch (e: DocumentException) {
                        throw ExceptionConverter(e)
                    }

                    if (pivoted) {
                        setBottom(getTop() - effectivePaddingTop - effectivePaddingBottom - ct.filledWidth)
                    } else {
                        var yLine = ct.yLine
                        if (isUseDescender) {
                            yLine += ct.descender
                        }
                        setBottom(yLine - effectivePaddingBottom)
                    }
                }
            }
            var height = height
            if (height == effectivePaddingTop + effectivePaddingBottom) {
                height = 0f
            }
            if (hasFixedHeight()) {
                height = fixedHeight
            } else if (hasMinimumHeight() && height < minimumHeight) {
                height = minimumHeight
            }
            cachedMaxHeight = height
            return height
        }

    fun getAccessibleAttribute(key: PdfName): PdfObject? {
        if (accessibleAttributes != null) {
            return accessibleAttributes!![key]
        } else {
            return null
        }
    }

    fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null) {
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        }
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = false

    fun addHeader(header: PdfPHeaderCell) {
        if (headers == null) {
            headers = ArrayList<PdfPHeaderCell>()
        }
        headers!!.add(header)
    }
}
/**
 * Constructs a PdfPCell with an Image. The
 * default padding is 0.

 * @param image the Image
 */
/**
 * Constructs a PdfPCell with a PdfPtable. This
 * constructor allows nested tables. The default padding is 0.

 * @param table The PdfPTable
 */
