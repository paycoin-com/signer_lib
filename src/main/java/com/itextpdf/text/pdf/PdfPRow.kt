/*
 * $Id: b85dfe2c293ef8dbf873a51707fa62badace2db3 $
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

import java.util.HashMap

import com.itextpdf.text.*
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

/**
 * A row in a PdfPTable.

 * @author Paulo Soares
 */
class PdfPRow : IAccessibleElement {

    private val LOGGER = LoggerFactory.getLogger(PdfPRow::class.java)

    /**
     * True if the table may not break after this row.
     */
    /**
     * Getter for the mayNotbreak variable.
     */
    /**
     * Setter for the mayNotBreak variable.
     */
    var isMayNotBreak = false

    /**
     * Returns the array of cells in the row. Please be extremely careful with
     * this method. Use the cells as read only objects.

     * @return    an array of cells
     * *
     * @since    2.1.1
     */
    var cells: Array<PdfPCell>
        protected set

    protected var widths: FloatArray

    /**
     * extra heights that needs to be added to a cell because of rowspans.

     * @since    2.1.6
     */
    protected var extraHeights: FloatArray

    /**
     * Gets the maximum height of the row (i.e. of the 'highest' cell).

     * @return the maximum height of the row
     */
    /**
     * Changes the maximum height of the row (to make it higher). (added by
     * Jin-Hsia Yang)

     * @param maxHeight the new maximum height
     */
    var maxHeights = 0f
        get() {
            if (!isCalculated) {
                calculateHeights()
            }
            return maxHeights
        }

    /**
     * Checks if the dimensions of the columns were calculated.

     * @return true if the dimensions of the columns were calculated
     */
    var isCalculated = false
        protected set
    var isAdjusted = false

    private var canvasesPos: IntArray? = null

    override var role = PdfName.TR
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()

    @JvmOverloads constructor(cells: Array<PdfPCell>, source: PdfPRow? = null) {
        this.cells = cells
        widths = FloatArray(cells.size)
        initExtraHeights()
        if (source != null) {
            this.id = source.id
            this.role = source.role
            if (source.accessibleAttributes != null) {
                this.accessibleAttributes = HashMap(source.accessibleAttributes)
            }
        }
    }

    /**
     * Makes a copy of an existing row.

     * @param row
     */
    constructor(row: PdfPRow) {
        isMayNotBreak = row.isMayNotBreak
        maxHeights = row.maxHeights
        isCalculated = row.isCalculated
        cells = arrayOfNulls<PdfPCell>(row.cells.size)
        for (k in cells.indices) {
            if (row.cells[k] != null) {
                if (row.cells[k] is PdfPHeaderCell) {
                    cells[k] = PdfPHeaderCell(row.cells[k] as PdfPHeaderCell)
                } else {
                    cells[k] = PdfPCell(row.cells[k])
                }
            }
        }
        widths = FloatArray(cells.size)
        System.arraycopy(row.widths, 0, widths, 0, cells.size)
        initExtraHeights()
        this.id = row.id
        this.role = row.role
        if (row.accessibleAttributes != null) {
            this.accessibleAttributes = HashMap(row.accessibleAttributes)
        }
    }

    /**
     * Sets the widths of the columns in the row.

     * @param widths
     * *
     * @return true if everything went right
     */
    fun setWidths(widths: FloatArray): Boolean {
        if (widths.size != cells.size) {
            return false
        }
        System.arraycopy(widths, 0, this.widths, 0, cells.size)
        var total = 0f
        isCalculated = false
        var k = 0
        while (k < widths.size) {
            val cell = cells[k]

            if (cell == null) {
                total += widths[k]
                ++k
                continue
            }

            cell.left = total
            val last = k + cell.colspan
            while (k < last) {
                total += widths[k]
                ++k
            }
            --k
            cell.right = total
            cell.top = 0f
            ++k
        }
        return true
    }

    /**
     * Initializes the extra heights array.

     * @since    2.1.6
     */
    protected fun initExtraHeights() {
        extraHeights = FloatArray(cells.size)
        for (i in extraHeights.indices) {
            extraHeights[i] = 0f
        }
    }

    /**
     * Sets an extra height for a cell.

     * @param    cell    the index of the cell that needs an extra height
     * *
     * @param    height    the extra height
     * *
     * @since    2.1.6
     */
    fun setExtraHeight(cell: Int, height: Float) {
        if (cell < 0 || cell >= cells.size) {
            return
        }
        extraHeights[cell] = height
    }

    /**
     * Calculates the heights of each cell in the row.
     */
    protected fun calculateHeights() {
        maxHeights = 0f
        LOGGER.info("calculateHeights")
        for (k in cells.indices) {
            val cell = cells[k]
            var height = 0f
            if (cell == null) {
                continue
            } else {
                if (cell.hasCalculatedHeight()) {
                    height = cell.calculatedHeight
                } else {
                    height = cell.maxHeight
                }
                if (height > maxHeights && cell.rowspan == 1) {
                    maxHeights = height
                }
            }
        }
        isCalculated = true
    }

    /**
     * Writes the border and background of one cell in the row.

     * @param xPos The x-coordinate where the table starts on the canvas
     * *
     * @param yPos The y-coordinate where the table starts on the canvas
     * *
     * @param currentMaxHeight The height of the cell to be drawn.
     * *
     * @param cell
     * *
     * @param canvases
     * *
     * @since    2.1.6	extra parameter currentMaxHeight
     */
    fun writeBorderAndBackground(xPos: Float, yPos: Float, currentMaxHeight: Float, cell: PdfPCell, canvases: Array<PdfContentByte>) {
        val background = cell.getBackgroundColor()
        if (background != null || cell.hasBorders()) {
            // Add xPos resp. yPos to the cell's coordinates for absolute coordinates
            val right = cell.right + xPos
            val top = cell.top + yPos
            val left = cell.left + xPos
            val bottom = top - currentMaxHeight

            if (background != null) {
                val backgr = canvases[PdfPTable.BACKGROUNDCANVAS]
                backgr.setColorFill(background)
                backgr.rectangle(left, bottom, right - left, top - bottom)
                backgr.fill()
            }
            if (cell.hasBorders()) {
                val newRect = Rectangle(left, bottom, right, top)
                // Clone non-position parameters except for the background color
                newRect.cloneNonPositionParameters(cell)
                newRect.backgroundColor = null
                // Write the borders on the line canvas
                val lineCanvas = canvases[PdfPTable.LINECANVAS]
                lineCanvas.rectangle(newRect)
            }
        }
    }

    /**
     * @since    2.1.6 private is now protected
     */
    protected fun saveAndRotateCanvases(canvases: Array<PdfContentByte>, a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        val last = PdfPTable.TEXTCANVAS + 1
        if (canvasesPos == null) {
            canvasesPos = IntArray(last * 2)
        }
        for (k in 0..last - 1) {
            val bb = canvases[k].internalBuffer
            canvasesPos[k * 2] = bb.size()
            canvases[k].saveState()
            canvases[k].concatCTM(a, b, c, d, e, f)
            canvasesPos[k * 2 + 1] = bb.size()
        }
    }

    /**
     * @since    2.1.6 private is now protected
     */
    protected fun restoreCanvases(canvases: Array<PdfContentByte>) {
        val last = PdfPTable.TEXTCANVAS + 1
        for (k in 0..last - 1) {
            val bb = canvases[k].internalBuffer
            val p1 = bb.size()
            canvases[k].restoreState()
            if (p1 == canvasesPos!![k * 2 + 1]) {
                bb.setSize(canvasesPos!![k * 2])
            }
        }
    }

    /**
     * Writes a number of cells (not necessarily all cells).

     * @param    colStart The first column to be written. Remember that the column
     * * index starts with 0.
     * *
     * @param    colEnd The last column to be written. Remember that the column
     * * index starts with 0. If -1, all the columns to the end are written.
     * *
     * @param    xPos The x-coordinate where the table starts on the canvas
     * *
     * @param    yPos The y-coordinate where the table starts on the canvas
     * *
     * @param    reusable if set to false, the content in the cells is "consumed";
     * * if true, you can reuse the cells, the row, the parent table as many times
     * * you want.
     * *
     * @since 5.1.0 added the reusable parameter
     */
    fun writeCells(colStart: Int, colEnd: Int, xPos: Float, yPos: Float, canvases: Array<PdfContentByte>, reusable: Boolean) {
        var colStart = colStart
        var colEnd = colEnd
        var xPos = xPos
        if (!isCalculated) {
            calculateHeights()
        }
        if (colEnd < 0) {
            colEnd = cells.size
        } else {
            colEnd = Math.min(colEnd, cells.size)
        }
        if (colStart < 0) {
            colStart = 0
        }
        if (colStart >= colEnd) {
            return
        }

        var newStart: Int
        newStart = colStart
        while (newStart >= 0) {
            if (cells[newStart] != null) {
                break
            }
            if (newStart > 0) {
                xPos -= widths[newStart - 1]
            }
            --newStart
        }

        if (newStart < 0) {
            newStart = 0
        }
        if (cells[newStart] != null) {
            xPos -= cells[newStart].left
        }

        if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
            canvases[PdfPTable.TEXTCANVAS].openMCBlock(this)
        }
        for (k in newStart..colEnd - 1) {
            val cell = cells[k] ?: continue
            if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
                canvases[PdfPTable.TEXTCANVAS].openMCBlock(cell)
            }
            val currentMaxHeight = maxHeights + extraHeights[k]

            writeBorderAndBackground(xPos, yPos, currentMaxHeight, cell, canvases)

            var img = cell.image

            var tly = cell.top + yPos - cell.effectivePaddingTop
            if (cell.height <= currentMaxHeight) {
                when (cell.verticalAlignment) {
                    Element.ALIGN_BOTTOM -> tly = cell.top + yPos - currentMaxHeight + cell.height - cell.effectivePaddingTop
                    Element.ALIGN_MIDDLE -> tly = cell.top + yPos + (cell.height - currentMaxHeight) / 2 - cell.effectivePaddingTop
                    else -> {
                    }
                }
            }
            if (img != null) {
                if (cell.rotation != 0) {
                    img = Image.getInstance(img)
                    img!!.setRotation(img.imageRotation + (cell.rotation * Math.PI / 180.0).toFloat())
                }
                var vf = false
                if (cell.height > currentMaxHeight) {
                    if (!img.isScaleToFitHeight) {
                        continue
                    }
                    img.scalePercent(100f)
                    val scale = (currentMaxHeight - cell.effectivePaddingTop - cell.effectivePaddingBottom) / img.scaledHeight
                    img.scalePercent(scale * 100)
                    vf = true
                }
                var left = cell.left + xPos
                +cell.effectivePaddingLeft
                if (vf) {
                    when (cell.horizontalAlignment) {
                        Element.ALIGN_CENTER -> left = xPos + (cell.left + cell.effectivePaddingLeft
                                + cell.right
                                - cell.effectivePaddingRight - img.scaledWidth) / 2
                        Element.ALIGN_RIGHT -> left = xPos + cell.right
                        -cell.effectivePaddingRight
                                - img.scaledWidth
                        else -> {
                        }
                    }
                    tly = cell.top + yPos - cell.effectivePaddingTop
                }
                img.setAbsolutePosition(left, tly - img.scaledHeight)
                try {
                    if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
                        canvases[PdfPTable.TEXTCANVAS].openMCBlock(img)
                    }
                    canvases[PdfPTable.TEXTCANVAS].addImage(img)
                    if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
                        canvases[PdfPTable.TEXTCANVAS].closeMCBlock(img)
                    }
                } catch (e: DocumentException) {
                    throw ExceptionConverter(e)
                }

            } else {
                // rotation sponsored by Connection GmbH
                if (cell.rotation == 90 || cell.rotation == 270) {
                    val netWidth = currentMaxHeight - cell.effectivePaddingTop - cell.effectivePaddingBottom
                    val netHeight = cell.width - cell.effectivePaddingLeft - cell.effectivePaddingRight
                    var ct = ColumnText.duplicate(cell.column)
                    ct.canvases = canvases
                    ct.setSimpleColumn(0f, 0f, netWidth + 0.001f, -netHeight)
                    try {
                        ct.go(true)
                    } catch (e: DocumentException) {
                        throw ExceptionConverter(e)
                    }

                    var calcHeight = -ct.yLine
                    if (netWidth <= 0 || netHeight <= 0) {
                        calcHeight = 0f
                    }
                    if (calcHeight > 0) {
                        if (cell.isUseDescender) {
                            calcHeight -= ct.descender
                        }
                        if (reusable) {
                            ct = ColumnText.duplicate(cell.column)
                        } else {
                            ct = cell.column
                        }
                        ct.canvases = canvases
                        ct.setSimpleColumn(-0.003f, -0.001f, netWidth + 0.003f, calcHeight)
                        val pivotX: Float
                        val pivotY: Float
                        if (cell.rotation == 90) {
                            pivotY = cell.top + yPos - currentMaxHeight + cell.effectivePaddingBottom
                            when (cell.verticalAlignment) {
                                Element.ALIGN_BOTTOM -> pivotX = cell.left + xPos + cell.width - cell.effectivePaddingRight
                                Element.ALIGN_MIDDLE -> pivotX = cell.left + xPos + (cell.width + cell.effectivePaddingLeft - cell.effectivePaddingRight + calcHeight) / 2
                                else //top
                                -> pivotX = cell.left + xPos + cell.effectivePaddingLeft + calcHeight
                            }
                            saveAndRotateCanvases(canvases, 0f, 1f, -1f, 0f, pivotX, pivotY)
                        } else {
                            pivotY = cell.top + yPos - cell.effectivePaddingTop
                            when (cell.verticalAlignment) {
                                Element.ALIGN_BOTTOM -> pivotX = cell.left + xPos + cell.effectivePaddingLeft
                                Element.ALIGN_MIDDLE -> pivotX = cell.left + xPos + (cell.width + cell.effectivePaddingLeft - cell.effectivePaddingRight - calcHeight) / 2
                                else //top
                                -> pivotX = cell.left + xPos + cell.width - cell.effectivePaddingRight - calcHeight
                            }
                            saveAndRotateCanvases(canvases, 0f, -1f, 1f, 0f, pivotX, pivotY)
                        }
                        try {
                            ct.go()
                        } catch (e: DocumentException) {
                            throw ExceptionConverter(e)
                        } finally {
                            restoreCanvases(canvases)
                        }
                    }
                } else {
                    val fixedHeight = cell.fixedHeight
                    var rightLimit = cell.right + xPos - cell.effectivePaddingRight
                    var leftLimit = cell.left + xPos
                    +cell.effectivePaddingLeft
                    if (cell.isNoWrap) {
                        when (cell.horizontalAlignment) {
                            Element.ALIGN_CENTER -> {
                                rightLimit += 10000f
                                leftLimit -= 10000f
                            }
                            Element.ALIGN_RIGHT -> if (cell.rotation == 180) {
                                rightLimit += RIGHT_LIMIT
                            } else {
                                leftLimit -= RIGHT_LIMIT
                            }
                            else -> if (cell.rotation == 180) {
                                leftLimit -= RIGHT_LIMIT
                            } else {
                                rightLimit += RIGHT_LIMIT
                            }
                        }
                    }
                    val ct: ColumnText
                    if (reusable) {
                        ct = ColumnText.duplicate(cell.column)
                    } else {
                        ct = cell.column
                    }
                    ct.canvases = canvases
                    var bry = tly - (currentMaxHeight
                            - cell.effectivePaddingTop - cell.effectivePaddingBottom)
                    if (fixedHeight > 0) {
                        if (cell.height > currentMaxHeight) {
                            tly = cell.top + yPos - cell.effectivePaddingTop
                            bry = cell.top + yPos - currentMaxHeight + cell.effectivePaddingBottom
                        }
                    }
                    if ((tly > bry || ct.zeroHeightElement()) && leftLimit < rightLimit) {
                        ct.setSimpleColumn(leftLimit, bry - 0.001f, rightLimit, tly)
                        if (cell.rotation == 180) {
                            val shx = leftLimit + rightLimit
                            val shy = yPos + yPos - currentMaxHeight + cell.effectivePaddingBottom - cell.effectivePaddingTop
                            saveAndRotateCanvases(canvases, -1f, 0f, 0f, -1f, shx, shy)
                        }
                        try {
                            ct.go()
                        } catch (e: DocumentException) {
                            throw ExceptionConverter(e)
                        } finally {
                            if (cell.rotation == 180) {
                                restoreCanvases(canvases)
                            }
                        }
                    }
                }
            }
            val evt = cell.cellEvent
            if (evt != null) {
                val rect = Rectangle(cell.left + xPos, cell.top + yPos - currentMaxHeight, cell.right + xPos, cell.top + yPos)
                evt.cellLayout(cell, rect, canvases)
            }
            if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
                canvases[PdfPTable.TEXTCANVAS].closeMCBlock(cell)
            }
        }
        if (isTagged(canvases[PdfPTable.TEXTCANVAS])) {
            canvases[PdfPTable.TEXTCANVAS].closeMCBlock(this)
        }
    }

    //end add
    internal fun getEventWidth(xPos: Float, absoluteWidths: FloatArray): FloatArray {
        var n = 1
        run {
            var k = 0
            while (k < cells.size) {
                if (cells[k] != null) {
                    n++
                    k += cells[k].colspan
                } else {
                    while (k < cells.size && cells[k] == null) {
                        n++
                        k++
                    }
                }
            }
        }
        val width = FloatArray(n)
        width[0] = xPos
        n = 1
        var k = 0
        while (k < cells.size && n < width.size) {
            if (cells[k] != null) {
                val colspan = cells[k].colspan
                width[n] = width[n - 1]
                var i = 0
                while (i < colspan && k < absoluteWidths.size) {
                    width[n] += absoluteWidths[k++]
                    i++
                }
                n++
            } else {
                width[n] = width[n - 1]
                while (k < cells.size && cells[k] == null) {
                    width[n] += absoluteWidths[k++]
                }
                n++
            }
        }
        return width
    }

    /**
     * Copies the content of a specific row in a table to this row. Don't do
     * this if the rows have a different number of cells.

     * @param table    the table from which you want to copy a row
     * *
     * @param idx    the index of the row that needs to be copied
     * *
     * @since 5.1.0
     */
    fun copyRowContent(table: PdfPTable?, idx: Int) {
        if (table == null) {
            return
        }
        var copy: PdfPCell?
        for (i in cells.indices) {
            var lastRow = idx
            copy = table.getRow(lastRow).cells[i]
            while (copy == null && lastRow > 0) {
                copy = table.getRow(--lastRow).cells[i]
            }
            if (cells[i] != null && copy != null) {
                cells[i].column = copy.column
                this.isCalculated = false
            }
        }
    }

    /**
     * Splits a row to newHeight. The returned row is the remainder. It will
     * return null if the newHeight was so small that only an empty row would
     * result.

     * @param new_height    the new height
     * *
     * @return the remainder row or null if the newHeight was so small that only
     * * an empty row would result
     */
    fun splitRow(table: PdfPTable, rowIndex: Int, new_height: Float): PdfPRow? {
        LOGGER.info(String.format("Splitting row %s available height: %s", rowIndex, new_height))
        // second part of the row
        val newCells = arrayOfNulls<PdfPCell>(cells.size)
        val calHs = FloatArray(cells.size)
        val fixHs = FloatArray(cells.size)
        val minHs = FloatArray(cells.size)
        var allEmpty = true
        // loop over all the cells
        for (k in cells.indices) {
            var newHeight = new_height
            val cell = cells[k]
            if (cell == null) {
                var index = rowIndex
                if (table.rowSpanAbove(index, k)) {
                    while (table.rowSpanAbove(--index, k)) {
                        newHeight += table.getRow(index).maxHeights
                    }
                    val row = table.getRow(index)
                    if (row != null && row.cells[k] != null) {
                        newCells[k] = PdfPCell(row.cells[k])
                        newCells[k].column = null
                        newCells[k].rowspan = row.cells[k].rowspan - rowIndex + index
                        allEmpty = false
                    }
                }
                continue
            }
            calHs[k] = cell.calculatedHeight
            fixHs[k] = cell.fixedHeight
            minHs[k] = cell.minimumHeight
            val img = cell.image
            val newCell = PdfPCell(cell)
            if (img != null) {
                val padding = cell.effectivePaddingBottom + cell.effectivePaddingTop + 2f
                if ((img.isScaleToFitHeight || img.scaledHeight + padding < newHeight) && newHeight > padding) {
                    newCell.phrase = null
                    allEmpty = false
                }
            } else {
                val y: Float
                val ct = ColumnText.duplicate(cell.column)
                val left = cell.left + cell.effectivePaddingLeft
                val bottom = cell.top + cell.effectivePaddingBottom - newHeight
                val right = cell.right - cell.effectivePaddingRight
                val top = cell.top - cell.effectivePaddingTop
                when (cell.rotation) {
                    90, 270 -> y = setColumn(ct, bottom, left, top, right)
                    else -> y = setColumn(ct, left, bottom + 0.00001f, if (cell.isNoWrap) RIGHT_LIMIT else right, top)
                }
                val status: Int
                try {
                    status = ct.go(true)
                } catch (e: DocumentException) {
                    throw ExceptionConverter(e)
                }

                val thisEmpty = ct.yLine == y
                if (thisEmpty) {
                    newCell.column = ColumnText.duplicate(cell.column)
                    ct.filledWidth = 0
                } else if (status and ColumnText.NO_MORE_TEXT == 0) {
                    newCell.column = ct
                    ct.filledWidth = 0
                } else {
                    newCell.phrase = null
                }
                allEmpty = allEmpty && thisEmpty
            }
            newCells[k] = newCell
            cell.calculatedHeight = newHeight
        }
        if (allEmpty) {
            for (k in cells.indices) {
                val cell = cells[k] ?: continue
                cell.calculatedHeight = calHs[k]
                if (fixHs[k] > 0) {
                    cell.fixedHeight = fixHs[k]
                } else {
                    cell.minimumHeight = minHs[k]
                }
            }
            return null
        }
        calculateHeights()
        val split = PdfPRow(newCells, this)
        split.widths = widths.clone()
        return split
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    val maxRowHeightsWithoutCalculating: Float
        get() = maxHeights

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    fun setFinalMaxHeights(maxHeight: Float) {
        maxHeights = maxHeight
        isCalculated = true // otherwise maxHeight would be recalculated in getter
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    /**
     * Split rowspan of cells with rowspan on next page by inserting copies with
     * the remaining rowspan and reducing the previous rowspan appropriately,
     * i.e. if a cell with rowspan 7 gets split after 3 rows of that rowspan
     * have been laid out, its column on the next page should start with an
     * empty cell having the same attributes and rowspan 7 - 3 = 4.

     * @since iText 5.4.3
     */
    fun splitRowspans(original: PdfPTable?, originalIdx: Int, part: PdfPTable?, partIdx: Int) {
        if (original == null || part == null) {
            return
        }
        var i = 0
        while (i < cells.size) {
            if (cells[i] == null) {
                val splittedRowIdx = original.getCellStartRowIndex(originalIdx, i)
                val copyRowIdx = part.getCellStartRowIndex(partIdx, i)
                val splitted = original.getRow(splittedRowIdx).cells[i] // need this to reduce its rowspan
                val copy = part.getRow(copyRowIdx).cells[i] // need this for (partially) consumed ColumnText
                if (splitted != null) {
                    assert(copy != null) // both null or none
                    cells[i] = PdfPCell(copy)
                    val rowspanOnPreviousPage = partIdx - copyRowIdx + 1
                    cells[i].rowspan = copy!!.rowspan - rowspanOnPreviousPage
                    splitted.rowspan = rowspanOnPreviousPage
                    this.isCalculated = false
                }
                ++i
            } else {
                i += cells[i].colspan
            }
        }
    }

    /**
     * Checks if a cell in the row has a rowspan greater than 1.

     * @since 5.1.0
     */
    fun hasRowspan(): Boolean {
        for (i in cells.indices) {
            if (cells[i] != null && cells[i].rowspan > 1) {
                return true
            }
        }
        return false
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

    companion object {

        /**
         * the bottom limit (bottom right y)
         */
        val BOTTOM_LIMIT = (-(1 shl 30)).toFloat()
        /**
         * the right limit

         * @since    2.1.5
         */
        val RIGHT_LIMIT = 20000f

        /**
         * @since    3.0.0 protected is now public static
         */
        fun setColumn(ct: ColumnText, left: Float, bottom: Float, right: Float, top: Float): Float {
            var right = right
            var top = top
            if (left > right) {
                right = left
            }
            if (bottom > top) {
                top = bottom
            }
            ct.setSimpleColumn(left, bottom, right, top)
            return top
        }

        private fun isTagged(canvas: PdfContentByte?): Boolean {
            return canvas != null && canvas.pdfWriter != null && canvas.pdfWriter!!.isTagged
        }
    }
}
/**
 * Constructs a new PdfPRow with the cells in the array that was passed as a
 * parameter.

 * @param cells
 */
