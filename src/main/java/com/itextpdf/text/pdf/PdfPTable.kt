/*
 * $Id: 313a98f789d246dc968334fcf5504b8c44c80fb9 $
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

import com.itextpdf.text.AccessibleElementId
import com.itextpdf.text.Chunk
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.ElementListener
import com.itextpdf.text.Image
import com.itextpdf.text.LargeElement
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.api.Spaceable
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.events.PdfPTableEventForwarder
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.ArrayList
import java.util.HashMap

/**
 * This is a table that can be put at an absolute position but can also be added
 * to the document as the class Table.
 *
 *
 * A PdfPTableEvent can be associated to the table to do custom drawing when the
 * table is rendered.

 * @author Paulo Soares
 */
class PdfPTable : LargeElement, Spaceable, IAccessibleElement {

    private val LOGGER = LoggerFactory.getLogger(PdfPTable::class.java)

    /**
     * Gets an arraylist with all the rows in the table.

     * @return an arraylist
     */
    var rows = ArrayList<PdfPRow>()
        protected set
    /**
     * Gets the total height of the table.

     * @return the total height of the table
     */
    var totalHeight = 0f
        protected set
    protected var currentRow: Array<PdfPCell>
    /**
     * The current column index.

     * @since 5.1.0 renamed from currentRowIdx
     */
    protected var currentColIdx = 0
    /**
     * Gets the default PdfPCell that will be used as reference for
     * all the addCell methods except
     * addCell(PdfPCell).

     * @return default PdfPCell
     */
    var defaultCell = PdfPCell(null as Phrase)
        protected set
    /**
     * Gets the full width of the table.

     * @return the full width of the table
     */
    /**
     * Sets the full width of the table.

     * @param totalWidth the full width of the table.
     */
    var totalWidth = 0f
        set(totalWidth) {
            if (this.totalWidth == totalWidth) {
                return
            }
            this.totalWidth = totalWidth
            totalHeight = 0f
            calculateWidths()
            calculateHeights()
        }
    protected var relativeWidths: FloatArray
    /**
     * Gets the absolute sizes of each column width.

     * @return he absolute sizes of each column width
     */
    var absoluteWidths: FloatArray
        protected set
    /**
     * Gets the table event for this page.

     * @return the table event for this page
     */
    /**
     * Sets the table event for this table.

     * @param event the table event for this table
     */
    var tableEvent: PdfPTableEvent? = null
        set(event) = if (event == null) {
            this.tableEvent = null
        } else if (this.tableEvent == null) {
            this.tableEvent = event
        } else if (this.tableEvent is PdfPTableEventForwarder) {
            (this.tableEvent as PdfPTableEventForwarder).addTableEvent(event)
        } else {
            val forward = PdfPTableEventForwarder()
            forward.addTableEvent(this.tableEvent)
            forward.addTableEvent(event)
            this.tableEvent = forward
        }

    /**
     * Holds value of property headerRows.
     */
    /**
     * Gets the number of the rows that constitute the header.

     * @return the number of the rows that constitute the header
     */
    /**
     * Sets the number of the top rows that constitute the header. This header
     * has only meaning if the table is added to Document and the
     * table crosses pages.

     * @param headerRows the number of the top rows that constitute the header
     */
    var headerRows: Int = 0
        set(headerRows) {
            var headerRows = headerRows
            if (headerRows < 0) {
                headerRows = 0
            }
            this.headerRows = headerRows
        }

    /**
     * Holds value of property widthPercentage.
     */
    /**
     * Gets the width percentage that the table will occupy in the page.

     * @return the width percentage that the table will occupy in the page
     */
    /**
     * Sets the width percentage that the table will occupy in the page.

     * @param widthPercentage the width percentage that the table will occupy in
     * * the page
     */
    var widthPercentage = 80f

    /**
     * Holds value of property horizontalAlignment.
     */
    /**
     * Gets the horizontal alignment of the table relative to the page.

     * @return the horizontal alignment of the table relative to the page
     */
    /**
     * Sets the horizontal alignment of the table relative to the page. It only
     * has meaning if the width percentage is less than 100%.

     * @param horizontalAlignment the horizontal alignment of the table relative
     * * to the page
     */
    var horizontalAlignment = Element.ALIGN_CENTER

    /**
     * Holds value of property skipFirstHeader.
     */
    /**
     * Tells you if the first header needs to be skipped (for instance if the
     * header says "continued from the previous page").

     * @return Value of property skipFirstHeader.
     */
    /**
     * Skips the printing of the first header. Used when printing tables in
     * succession belonging to the same printed table aspect.

     * @param skipFirstHeader New value of property skipFirstHeader.
     */
    var isSkipFirstHeader = false
    /**
     * Holds value of property skipLastFooter.

     * @since 2.1.6
     */
    /**
     * Tells you if the last footer needs to be skipped (for instance if the
     * footer says "continued on the next page")

     * @return Value of property skipLastFooter.
     * *
     * @since 2.1.6
     */
    /**
     * Skips the printing of the last footer. Used when printing tables in
     * succession belonging to the same printed table aspect.

     * @param skipLastFooter New value of property skipLastFooter.
     * *
     * @since 2.1.6
     */
    var isSkipLastFooter = false

    protected var isColspan = false

    /**
     * Returns the run direction of the contents in the table.

     * @return One of the following values: PdfWriter.RUN_DIRECTION_DEFAULT,
     * * PdfWriter.RUN_DIRECTION_NO_BIDI, PdfWriter.RUN_DIRECTION_LTR or
     * * PdfWriter.RUN_DIRECTION_RTL.
     */
    /**
     * Sets the run direction of the contents of the table.

     * @param runDirection One of the following values:
     * * PdfWriter.RUN_DIRECTION_DEFAULT, PdfWriter.RUN_DIRECTION_NO_BIDI,
     * * PdfWriter.RUN_DIRECTION_LTR or PdfWriter.RUN_DIRECTION_RTL.
     */
    var runDirection = PdfWriter.RUN_DIRECTION_DEFAULT
        set(runDirection) = when (runDirection) {
            PdfWriter.RUN_DIRECTION_DEFAULT, PdfWriter.RUN_DIRECTION_NO_BIDI, PdfWriter.RUN_DIRECTION_LTR, PdfWriter.RUN_DIRECTION_RTL -> this.runDirection = runDirection
            else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection))
        }

    /**
     * Holds value of property lockedWidth.
     */
    /**
     * Getter for property lockedWidth.

     * @return Value of property lockedWidth.
     */
    /**
     * Uses the value in setTotalWidth() in
     * Document.add().

     * @param lockedWidth true to use the value in
     * * setTotalWidth() in Document.add()
     */
    var isLockedWidth = false

    /**
     * Holds value of property splitRows.
     */
    /**
     * Gets the split value.

     * @return true to split; false otherwise
     */
    /**
     * When set the rows that won't fit in the page will be split. Note that it
     * takes at least twice the memory to handle a split table row than a normal
     * table. true by default.

     * @param splitRows true to split; false otherwise
     */
    var isSplitRows = true

    /**
     * The spacing before the table.
     */
    /* (non-Javadoc)
     * @see com.itextpdf.text.api.Spaceable#getSpacingBefore()
     */
    /**
     * Sets the spacing before this table.

     * @param spacing the new spacing
     */
    override var spacingBefore: Float = 0.toFloat()

    /**
     * The spacing after the table.
     */
    /* (non-Javadoc)
     * @see com.itextpdf.text.api.Spaceable#getSpacingAfter()
     */
    /**
     * Sets the spacing after this table.

     * @param spacing the new spacing
     */
    override var spacingAfter: Float = 0.toFloat()

    override var paddingTop: Float = 0.toFloat()

    /**
     * Holds value of property extendLastRow.
     */
    private var extendLastRow = booleanArrayOf(false, false)

    /**
     * Holds value of property headersInEvent.
     */
    /**
     * Gets the header status inclusion in PdfPTableEvent.

     * @return true if the headers are included; false otherwise
     */
    /**
     * When set the PdfPTableEvent will include the headers.

     * @param headersInEvent true to include the headers; false otherwise
     */
    var isHeadersInEvent: Boolean = false

    /**
     * Holds value of property splitLate.
     */
    /**
     * Gets the property splitLate.

     * @return the property splitLate
     */
    /**
     * If true the row will only split if it's the first one in an empty page.
     * It's true by default. It's only meaningful if setSplitRows(true).

     * @param splitLate the property value
     */
    var isSplitLate = true

    /**
     * Defines if the table should be kept on one page if possible
     */
    /**
     * Getter for property keepTogether

     * @return true if it is tried to keep the table on one page; false
     * * otherwise
     */
    /**
     * If true the table will be kept on one page if it fits, by forcing a new
     * page if it doesn't fit on the current page. The default is to split the
     * table over multiple pages.

     * @param keepTogether whether to try to keep the table on one page
     */
    var keepTogether: Boolean = false

    /**
     * Indicates if the PdfPTable is complete once added to the document.

     * @since iText 2.0.8
     */
    protected var complete = true

    /**
     * Holds value of property footerRows.
     */
    /**
     * Gets the number of rows in the footer.

     * @return the number of rows in the footer
     */
    /**
     * Sets the number of rows to be used for the footer. The number of footer
     * rows are subtracted from the header rows. For example, for a table with
     * two header rows and one footer row the code would be:
     *
     * table.setHeaderRows(3);
     * table.setFooterRows(1);
     *  Row 0 and 1 will be the header rows and row 2 will be the footer
     * row.

     * @param footerRows the number of rows to be used for the footer
     */
    var footerRows: Int = 0
        set(footerRows) {
            var footerRows = footerRows
            if (footerRows < 0) {
                footerRows = 0
            }
            this.footerRows = footerRows
        }

    /**
     * Keeps track of the completeness of the current row.

     * @since 2.1.6
     */
    protected var rowCompleted = true

    var isLoopCheck = true
    protected var rowsNotChecked = true

    override var role = PdfName.TABLE
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()
    private var header: PdfPTableHeader? = null
    private var body: PdfPTableBody? = null
    private var footer: PdfPTableFooter? = null

    private var numberOfWrittenRows: Int = 0

    protected constructor() {
    }

    /**
     * Constructs a PdfPTable with the relative column widths.

     * @param relativeWidths the relative column widths
     */
    constructor(relativeWidths: FloatArray?) {
        if (relativeWidths == null) {
            throw NullPointerException(MessageLocalization.getComposedMessage("the.widths.array.in.pdfptable.constructor.can.not.be.null"))
        }
        if (relativeWidths.size == 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.widths.array.in.pdfptable.constructor.can.not.have.zero.length"))
        }
        this.relativeWidths = FloatArray(relativeWidths.size)
        System.arraycopy(relativeWidths, 0, this.relativeWidths, 0, relativeWidths.size)
        absoluteWidths = FloatArray(relativeWidths.size)
        calculateWidths()
        currentRow = arrayOfNulls<PdfPCell>(absoluteWidths.size)
        keepTogether = false
    }

    /**
     * Constructs a PdfPTable with numColumns columns.

     * @param numColumns the number of columns
     */
    constructor(numColumns: Int) {
        if (numColumns <= 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.number.of.columns.in.pdfptable.constructor.must.be.greater.than.zero"))
        }
        relativeWidths = FloatArray(numColumns)
        for (k in 0..numColumns - 1) {
            relativeWidths[k] = 1f
        }
        absoluteWidths = FloatArray(relativeWidths.size)
        calculateWidths()
        currentRow = arrayOfNulls<PdfPCell>(absoluteWidths.size)
        keepTogether = false
    }

    /**
     * Constructs a copy of a PdfPTable.

     * @param table the PdfPTable to be copied
     */
    constructor(table: PdfPTable) {
        copyFormat(table)
        for (k in currentRow.indices) {
            if (table.currentRow[k] == null) {
                break
            }
            currentRow[k] = PdfPCell(table.currentRow[k])
        }
        for (k in table.rows.indices) {
            var row: PdfPRow? = table.rows[k]
            if (row != null) {
                row = PdfPRow(row)
            }
            rows.add(row)
        }
    }

    fun init() {
        LOGGER.info("Initialize row and cell heights")

        for (row in rows) {
            if (row == null) continue
            row.isCalculated = false
            for (cell in row.cells) {
                if (cell == null) continue
                cell.calculatedHeight = 0
            }
        }

    }

    /**
     * Copies the format of the sourceTable without copying the content.

     * @param sourceTable
     * *
     * @since 2.1.6 private is now protected
     */
    protected fun copyFormat(sourceTable: PdfPTable) {
        rowsNotChecked = sourceTable.rowsNotChecked
        relativeWidths = FloatArray(sourceTable.numberOfColumns)
        absoluteWidths = FloatArray(sourceTable.numberOfColumns)
        System.arraycopy(sourceTable.relativeWidths, 0, relativeWidths, 0, numberOfColumns)
        System.arraycopy(sourceTable.absoluteWidths, 0, absoluteWidths, 0, numberOfColumns)
        totalWidth = sourceTable.totalWidth
        totalHeight = sourceTable.totalHeight
        currentColIdx = 0
        tableEvent = sourceTable.tableEvent
        runDirection = sourceTable.runDirection
        if (sourceTable.defaultCell is PdfPHeaderCell) {
            defaultCell = PdfPHeaderCell(sourceTable.defaultCell as PdfPHeaderCell)
        } else {
            defaultCell = PdfPCell(sourceTable.defaultCell)
        }
        currentRow = arrayOfNulls<PdfPCell>(sourceTable.currentRow.size)
        isColspan = sourceTable.isColspan
        isSplitRows = sourceTable.isSplitRows
        spacingAfter = sourceTable.spacingAfter
        spacingBefore = sourceTable.spacingBefore
        headerRows = sourceTable.headerRows
        footerRows = sourceTable.footerRows
        isLockedWidth = sourceTable.isLockedWidth
        extendLastRow = sourceTable.extendLastRow
        isHeadersInEvent = sourceTable.isHeadersInEvent
        widthPercentage = sourceTable.widthPercentage
        isSplitLate = sourceTable.isSplitLate
        isSkipFirstHeader = sourceTable.isSkipFirstHeader
        isSkipLastFooter = sourceTable.isSkipLastFooter
        horizontalAlignment = sourceTable.horizontalAlignment
        keepTogether = sourceTable.keepTogether
        complete = sourceTable.complete
        isLoopCheck = sourceTable.isLoopCheck
        id = sourceTable.id
        role = sourceTable.role
        if (sourceTable.accessibleAttributes != null) {
            accessibleAttributes = HashMap(sourceTable.accessibleAttributes)
        }
        header = sourceTable.getHeader()
        body = sourceTable.getBody()
        footer = sourceTable.getFooter()
    }

    /**
     * Sets the relative widths of the table.

     * @param relativeWidths the relative widths of the table.
     * *
     * @throws DocumentException if the number of widths is different than the
     * * number of columns
     */
    @Throws(DocumentException::class)
    fun setWidths(relativeWidths: FloatArray) {
        if (relativeWidths.size != numberOfColumns) {
            throw DocumentException(MessageLocalization.getComposedMessage("wrong.number.of.columns"))
        }
        this.relativeWidths = FloatArray(relativeWidths.size)
        System.arraycopy(relativeWidths, 0, this.relativeWidths, 0, relativeWidths.size)
        absoluteWidths = FloatArray(relativeWidths.size)
        totalHeight = 0f
        calculateWidths()
        calculateHeights()
    }

    /**
     * Sets the relative widths of the table.

     * @param relativeWidths the relative widths of the table.
     * *
     * @throws DocumentException if the number of widths is different than the
     * * number of columns
     */
    @Throws(DocumentException::class)
    fun setWidths(relativeWidths: IntArray) {
        val tb = FloatArray(relativeWidths.size)
        for (k in relativeWidths.indices) {
            tb[k] = relativeWidths[k].toFloat()
        }
        setWidths(tb)
    }

    /**
     * @since 2.1.6 private is now protected
     */
    protected fun calculateWidths() {
        if (totalWidth <= 0) {
            return
        }
        var total = 0f
        val numCols = numberOfColumns
        for (k in 0..numCols - 1) {
            total += relativeWidths[k]
        }
        for (k in 0..numCols - 1) {
            absoluteWidths[k] = totalWidth * relativeWidths[k] / total
        }
    }

    /**
     * Sets the full width of the table from the absolute column width.

     * @param columnWidth the absolute width of each column
     * *
     * @throws DocumentException if the number of widths is different than the
     * * number of columns
     */
    @Throws(DocumentException::class)
    fun setTotalWidth(columnWidth: FloatArray) {
        if (columnWidth.size != numberOfColumns) {
            throw DocumentException(MessageLocalization.getComposedMessage("wrong.number.of.columns"))
        }
        totalWidth = 0f
        for (k in columnWidth.indices) {
            totalWidth += columnWidth[k]
        }
        setWidths(columnWidth)
    }

    /**
     * Sets the percentage width of the table from the absolute column width. Warning: Don't use this with setLockedWidth(true). These two settings don't mix.

     * @param columnWidth the absolute width of each column
     * *
     * @param pageSize the page size
     * *
     * @throws DocumentException
     */
    @Throws(DocumentException::class)
    fun setWidthPercentage(columnWidth: FloatArray, pageSize: Rectangle) {
        if (columnWidth.size != numberOfColumns) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("wrong.number.of.columns"))
        }
        setTotalWidth(columnWidth)
        widthPercentage = totalWidth / (pageSize.right - pageSize.left) * 100f
    }

    /**
     * Calculates the heights of the table.

     * @return the total height of the table. Note that it will be 0 if you
     * * didn't specify the width of the table with setTotalWidth(). and made it
     * * public
     */
    fun calculateHeights(): Float {
        if (totalWidth <= 0) {
            return 0f
        }
        totalHeight = 0f
        for (k in rows.indices) {
            totalHeight += getRowHeight(k, true)
        }
        return totalHeight
    }

    /**
     * Changes the number of columns. Any existing rows will be deleted.

     * @param newColCount the new number of columns
     * *
     * @since 5.0.2
     */
    fun resetColumnCount(newColCount: Int) {
        if (newColCount <= 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.number.of.columns.in.pdfptable.constructor.must.be.greater.than.zero"))
        }
        relativeWidths = FloatArray(newColCount)
        for (k in 0..newColCount - 1) {
            relativeWidths[k] = 1f
        }
        absoluteWidths = FloatArray(relativeWidths.size)
        calculateWidths()
        currentRow = arrayOfNulls<PdfPCell>(absoluteWidths.size)
        totalHeight = 0f
    }

    /**
     * Adds a cell element.

     * @param cell the cell element
     */
    fun addCell(cell: PdfPCell): PdfPCell {
        rowCompleted = false
        val ncell: PdfPCell
        if (cell is PdfPHeaderCell) {
            ncell = PdfPHeaderCell(cell)
        } else {
            ncell = PdfPCell(cell)
        }

        var colspan = ncell.colspan
        colspan = Math.max(colspan, 1)
        colspan = Math.min(colspan, currentRow.size - currentColIdx)
        ncell.colspan = colspan

        if (colspan != 1) {
            isColspan = true
        }
        val rdir = ncell.runDirection
        if (rdir == PdfWriter.RUN_DIRECTION_DEFAULT) {
            ncell.runDirection = runDirection
        }

        skipColsWithRowspanAbove()

        var cellAdded = false
        if (currentColIdx < currentRow.size) {
            currentRow[currentColIdx] = ncell
            currentColIdx += colspan
            cellAdded = true
        }

        skipColsWithRowspanAbove()

        while (currentColIdx >= currentRow.size) {
            val numCols = numberOfColumns
            if (runDirection == PdfWriter.RUN_DIRECTION_RTL) {
                val rtlRow = arrayOfNulls<PdfPCell>(numCols)
                var rev = currentRow.size
                var k = 0
                while (k < currentRow.size) {
                    val rcell = currentRow[k]
                    val cspan = rcell.colspan
                    rev -= cspan
                    rtlRow[rev] = rcell
                    k += cspan - 1
                    ++k
                }
                currentRow = rtlRow
            }
            val row = PdfPRow(currentRow)
            if (totalWidth > 0) {
                row.setWidths(absoluteWidths)
                totalHeight += row.maxHeights
            }
            rows.add(row)
            currentRow = arrayOfNulls<PdfPCell>(numCols)
            currentColIdx = 0
            skipColsWithRowspanAbove()
            rowCompleted = true
        }

        if (!cellAdded) {
            currentRow[currentColIdx] = ncell
            currentColIdx += colspan
        }
        return ncell
    }

    /**
     * When updating the row index, cells with rowspan should be taken into
     * account. This is what happens in this method.

     * @since 2.1.6
     */
    private fun skipColsWithRowspanAbove() {
        var direction = 1
        if (runDirection == PdfWriter.RUN_DIRECTION_RTL) {
            direction = -1
        }
        while (rowSpanAbove(rows.size, currentColIdx)) {
            currentColIdx += direction
        }
    }

    /**
     * Added by timmo3. This will return the correct cell taking it's cellspan
     * into account

     * @param row the row index
     * *
     * @param col the column index
     * *
     * @return PdfPCell at the given row and position or null otherwise
     */
    internal fun cellAt(row: Int, col: Int): PdfPCell? {
        val cells = rows[row].cells
        for (i in cells.indices) {
            if (cells[i] != null) {
                if (col >= i && col < i + cells[i].colspan) {
                    return cells[i]
                }
            }
        }
        return null
    }

    /**
     * Checks if there are rows above belonging to a rowspan.

     * @param currRow the current row to check
     * *
     * @param currCol the current column to check
     * *
     * @return true if there's a cell above that belongs to a rowspan
     * *
     * @since 2.1.6
     */
    internal fun rowSpanAbove(currRow: Int, currCol: Int): Boolean {
        if (currCol >= numberOfColumns
                || currCol < 0
                || currRow < 1) {
            return false
        }
        var row = currRow - 1
        var aboveRow: PdfPRow? = rows[row] ?: return false
        var aboveCell = cellAt(row, currCol)
        while (aboveCell == null && row > 0) {
            aboveRow = rows[--row]
            if (aboveRow == null) {
                return false
            }
            aboveCell = cellAt(row, currCol)
        }

        var distance = currRow - row

        if (aboveCell!!.rowspan == 1 && distance > 1) {
            var col = currCol - 1
            aboveRow = rows[row + 1]
            distance--
            aboveCell = aboveRow.cells[col]
            while (aboveCell == null && col > 0) {
                aboveCell = aboveRow.cells[--col]
            }
        }

        return aboveCell != null && aboveCell.rowspan > distance
    }

    /**
     * Adds a cell element.

     * @param text the text for the cell
     */
    fun addCell(text: String) {
        addCell(Phrase(text))
    }

    /**
     * Adds a nested table.

     * @param table the table to be added to the cell
     */
    fun addCell(table: PdfPTable) {
        defaultCell.table = table
        val newCell = addCell(defaultCell)
        newCell.id = AccessibleElementId()
        defaultCell.table = null
    }

    /**
     * Adds an Image as Cell.

     * @param image the Image to add to the table. This image will
     * * fit in the cell
     */
    fun addCell(image: Image) {
        defaultCell.image = image
        val newCell = addCell(defaultCell)
        newCell.id = AccessibleElementId()
        defaultCell.image = null
    }

    /**
     * Adds a cell element.

     * @param phrase the Phrase to be added to the cell
     */
    fun addCell(phrase: Phrase) {
        defaultCell.phrase = phrase
        val newCell = addCell(defaultCell)
        newCell.id = AccessibleElementId()
        defaultCell.phrase = null
    }

    /**
     * Writes the selected rows to the document. canvases is
     * obtained from beginWritingRows().

     * @param rowStart the first row to be written, zero index
     * *
     * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
     * * the end are written
     * *
     * @param xPos the x write coordinate
     * *
     * @param yPos the y write coordinate
     * *
     * @param canvases an array of 4 PdfContentByte obtained from
     * * beginWrittingRows()
     * *
     * @return the y coordinate position of the bottom of the last row
     * *
     * @see .beginWritingRows
     */
    fun writeSelectedRows(rowStart: Int, rowEnd: Int, xPos: Float, yPos: Float, canvases: Array<PdfContentByte>): Float {
        return writeSelectedRows(0, -1, rowStart, rowEnd, xPos, yPos, canvases)
    }

    /**
     * Writes the selected rows and columns to the document. This method does
     * not clip the columns; this is only important if there are columns with
     * colspan at boundaries. canvases is obtained from
     * beginWritingRows(). The table event is only fired for
     * complete rows.

     * @param colStart the first column to be written, zero index
     * *
     * @param colEnd the last column to be written + 1. If it is -1 all the
     * * columns to the end are written
     * *
     * @param rowStart the first row to be written, zero index
     * *
     * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
     * * the end are written
     * *
     * @param xPos the x write coordinate
     * *
     * @param yPos the y write coordinate
     * *
     * @param canvases an array of 4 PdfContentByte obtained from
     * * beginWritingRows()
     * *
     * @return the y coordinate position of the bottom of the last row
     * *
     * @param reusable if set to false, the content in the cells is "consumed";
     * * if true, you can reuse the cells, the row, the parent table as many times
     * * you want.
     * *
     * @see .beginWritingRows
     * @since 5.1.0 added the reusable parameter
     */
    @JvmOverloads fun writeSelectedRows(colStart: Int, colEnd: Int, rowStart: Int, rowEnd: Int, xPos: Float, yPos: Float, canvases: Array<PdfContentByte>, reusable: Boolean = true): Float {
        var colStart = colStart
        var colEnd = colEnd
        var rowStart = rowStart
        var rowEnd = rowEnd
        var yPos = yPos
        if (totalWidth <= 0) {
            throw RuntimeException(MessageLocalization.getComposedMessage("the.table.width.must.be.greater.than.zero"))
        }

        val totalRows = rows.size
        if (rowStart < 0) {
            rowStart = 0
        }
        if (rowEnd < 0) {
            rowEnd = totalRows
        } else {
            rowEnd = Math.min(rowEnd, totalRows)
        }
        if (rowStart >= rowEnd) {
            return yPos
        }

        val totalCols = numberOfColumns
        if (colStart < 0) {
            colStart = 0
        } else {
            colStart = Math.min(colStart, totalCols)
        }
        if (colEnd < 0) {
            colEnd = totalCols
        } else {
            colEnd = Math.min(colEnd, totalCols)
        }

        LOGGER.info(String.format("Writing row %s to %s; column %s to %s", rowStart, rowEnd, colStart, colEnd))

        val yPosStart = yPos

        var currentBlock: PdfPTableBody? = null
        if (rowsNotChecked) {
            getFittingRows(java.lang.Float.MAX_VALUE, rowStart)
        }
        val rows = getRows(rowStart, rowEnd)
        var k = rowStart
        for (row in rows) {
            if (getHeader().rows != null && getHeader().rows!!.contains(row) && currentBlock == null) {
                currentBlock = openTableBlock(getHeader(), canvases[TEXTCANVAS])
            } else if (getBody().rows != null && getBody().rows!!.contains(row) && currentBlock == null) {
                currentBlock = openTableBlock(getBody(), canvases[TEXTCANVAS])
            } else if (getFooter().rows != null && getFooter().rows!!.contains(row) && currentBlock == null) {
                currentBlock = openTableBlock(getFooter(), canvases[TEXTCANVAS])
            }
            if (row != null) {
                row.writeCells(colStart, colEnd, xPos, yPos, canvases, reusable)
                yPos -= row.maxHeights
            }
            if (getHeader().rows != null && getHeader().rows!!.contains(row) && (k == rowEnd - 1 || !getHeader().rows!!.contains(rows[k + 1]))) {
                currentBlock = closeTableBlock(getHeader(), canvases[TEXTCANVAS])
            } else if (getBody().rows != null && getBody().rows!!.contains(row) && (k == rowEnd - 1 || !getBody().rows!!.contains(rows[k + 1]))) {
                currentBlock = closeTableBlock(getBody(), canvases[TEXTCANVAS])
            } else if (getFooter().rows != null && getFooter().rows!!.contains(row) && (k == rowEnd - 1 || !getFooter().rows!!.contains(rows[k + 1]))) {
                currentBlock = closeTableBlock(getFooter(), canvases[TEXTCANVAS])
            }
            k++
        }

        if (tableEvent != null && colStart == 0 && colEnd == totalCols) {
            val heights = FloatArray(rowEnd - rowStart + 1)
            heights[0] = yPosStart
            k = rowStart
            while (k < rowEnd) {
                val row = rows[k]
                var hr = 0f
                if (row != null) {
                    hr = row.maxHeights
                }
                heights[k - rowStart + 1] = heights[k - rowStart] - hr
                ++k
            }
            tableEvent!!.tableLayout(this, getEventWidths(xPos, rowStart, rowEnd, isHeadersInEvent), heights, if (isHeadersInEvent) headerRows else 0, rowStart, canvases)
        }

        return yPos
    }

    private fun openTableBlock(block: PdfPTableBody, canvas: PdfContentByte): PdfPTableBody? {
        if (canvas.pdfWriter!!.standardStructElems.contains(block.role)) {
            canvas.openMCBlock(block)
            return block
        }
        return null
    }

    private fun closeTableBlock(block: PdfPTableBody, canvas: PdfContentByte): PdfPTableBody? {
        if (canvas.pdfWriter!!.standardStructElems.contains(block.role)) {
            canvas.closeMCBlock(block)
        }
        return null
    }

    /**
     * Writes the selected rows to the document.

     * @param rowStart the first row to be written, zero index
     * *
     * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
     * * the end are written
     * *
     * @param xPos the x write coordinate
     * *
     * @param yPos the y write coordinate
     * *
     * @param canvas the PdfContentByte where the rows will be
     * * written to
     * *
     * @return the y coordinate position of the bottom of the last row
     */
    fun writeSelectedRows(rowStart: Int, rowEnd: Int, xPos: Float, yPos: Float, canvas: PdfContentByte): Float {
        return writeSelectedRows(0, -1, rowStart, rowEnd, xPos, yPos, canvas)
    }

    /**
     * Writes the selected rows and columns to the document. This method clips
     * the columns; this is only important if there are columns with colspan at
     * boundaries. The table event is only fired for complete rows.

     * @param colStart the first column to be written, zero index
     * *
     * @param colEnd the last column to be written + 1. If it is -1 all the
     * * columns to the end are written
     * *
     * @param rowStart the first row to be written, zero index
     * *
     * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
     * * the end are written
     * *
     * @param xPos the x write coordinate
     * *
     * @param yPos the y write coordinate
     * *
     * @param canvas the PdfContentByte where the rows will be
     * * written to
     * *
     * @return the y coordinate position of the bottom of the last row
     * *
     * @param reusable if set to false, the content in the cells is "consumed";
     * * if true, you can reuse the cells, the row, the parent table as many times
     * * you want.
     * *
     * @since 5.1.0 added the reusable parameter
     */
    @JvmOverloads fun writeSelectedRows(colStart: Int, colEnd: Int, rowStart: Int, rowEnd: Int, xPos: Float, yPos: Float, canvas: PdfContentByte, reusable: Boolean = true): Float {
        var colStart = colStart
        var colEnd = colEnd
        val totalCols = numberOfColumns
        if (colStart < 0) {
            colStart = 0
        } else {
            colStart = Math.min(colStart, totalCols)
        }

        if (colEnd < 0) {
            colEnd = totalCols
        } else {
            colEnd = Math.min(colEnd, totalCols)
        }

        val clip = colStart != 0 || colEnd != totalCols

        if (clip) {
            var w = 0f
            for (k in colStart..colEnd - 1) {
                w += absoluteWidths[k]
            }
            canvas.saveState()
            val lx = (if (colStart == 0) 10000 else 0).toFloat()
            val rx = (if (colEnd == totalCols) 10000 else 0).toFloat()
            canvas.rectangle(xPos - lx, -10000f, w + lx + rx, PdfPRow.RIGHT_LIMIT)
            canvas.clip()
            canvas.newPath()
        }

        val canvases = beginWritingRows(canvas)
        val y = writeSelectedRows(colStart, colEnd, rowStart, rowEnd, xPos, yPos, canvases, reusable)
        endWritingRows(canvases)

        if (clip) {
            canvas.restoreState()
        }

        return y
    }

    /**
     * Gets the number of rows in this table.

     * @return the number of rows in this table
     */
    fun size(): Int {
        return rows.size
    }

    /**
     * Gets the height of a particular row.

     * @param idx the row index (starts at 0)
     * *
     * @return the height of a particular row
     */
    fun getRowHeight(idx: Int): Float {
        return getRowHeight(idx, false)
    }

    /**
     * Gets the height of a particular row.

     * @param idx the row index (starts at 0)
     * *
     * @param firsttime is this the first time the row heigh is calculated?
     * *
     * @return the height of a particular row
     * *
     * @since 5.0.0
     */
    protected fun getRowHeight(idx: Int, firsttime: Boolean): Float {
        if (totalWidth <= 0 || idx < 0 || idx >= rows.size) {
            return 0f
        }
        val row = rows[idx] ?: return 0f
        if (firsttime) {
            row.setWidths(absoluteWidths)
        }
        var height = row.maxHeights
        var cell: PdfPCell?
        var tmprow: PdfPRow
        for (i in relativeWidths.indices) {
            if (!rowSpanAbove(idx, i)) {
                continue
            }
            var rs = 1
            while (rowSpanAbove(idx - rs, i)) {
                rs++
            }
            tmprow = rows[idx - rs]
            cell = tmprow.cells[i]
            var tmp = 0f
            if (cell != null && cell.rowspan == rs + 1) {
                tmp = cell.maxHeight
                while (rs > 0) {
                    tmp -= getRowHeight(idx - rs)
                    rs--
                }
            }
            if (tmp > height) {
                height = tmp
            }
        }
        row.maxHeights = height
        return height
    }

    /**
     * Gets the maximum height of a cell in a particular row (will only be
     * different from getRowHeight is one of the cells in the row has a rowspan
     * > 1).

     * @return the height of a particular row including rowspan
     * *
     * @param rowIndex the row index
     * *
     * @param cellIndex the cell index
     * *
     * @since 2.1.6
     */
    fun getRowspanHeight(rowIndex: Int, cellIndex: Int): Float {
        if (totalWidth <= 0 || rowIndex < 0 || rowIndex >= rows.size) {
            return 0f
        }
        val row = rows[rowIndex]
        if (row == null || cellIndex >= row.cells.size) {
            return 0f
        }
        val cell = row.cells[cellIndex] ?: return 0f
        var rowspanHeight = 0f
        for (j in 0..cell.rowspan - 1) {
            rowspanHeight += getRowHeight(rowIndex + j)
        }
        return rowspanHeight
    }

    /**
     * Checks if a cell in a row has a rowspan greater than 1.

     * @since 5.1.0
     */
    fun hasRowspan(rowIdx: Int): Boolean {
        if (rowIdx < rows.size && getRow(rowIdx).hasRowspan()) {
            return true
        }
        val previousRow = if (rowIdx > 0) getRow(rowIdx - 1) else null
        if (previousRow != null && previousRow.hasRowspan()) {
            return true
        }
        for (i in 0..numberOfColumns - 1) {
            if (rowSpanAbove(rowIdx - 1, i)) {
                return true
            }
        }
        return false
    }

    /**
     * Makes sure the footers value is lower than the headers value.

     * @since 5.0.1
     */
    fun normalizeHeadersFooters() {
        if (footerRows > headerRows) {
            footerRows = headerRows
        }
    }

    /**
     * Gets the height of the rows that constitute the header as defined by
     * setHeaderRows().

     * @return the height of the rows that constitute the header and footer
     */
    val headerHeight: Float
        get() {
            var total = 0f
            val size = Math.min(rows.size, headerRows)
            for (k in 0..size - 1) {
                val row = rows[k]
                if (row != null) {
                    total += row.maxHeights
                }
            }
            return total
        }

    /**
     * Gets the height of the rows that constitute the footer as defined by
     * setFooterRows().

     * @return the height of the rows that constitute the footer
     * *
     * @since 2.1.1
     */
    val footerHeight: Float
        get() {
            var total = 0f
            val start = Math.max(0, headerRows - footerRows)
            val size = Math.min(rows.size, headerRows)
            for (k in start..size - 1) {
                val row = rows[k]
                if (row != null) {
                    total += row.maxHeights
                }
            }
            return total
        }

    /**
     * Deletes a row from the table.

     * @param rowNumber the row to be deleted
     * *
     * @return true if the row was deleted
     */
    fun deleteRow(rowNumber: Int): Boolean {
        if (rowNumber < 0 || rowNumber >= rows.size) {
            return false
        }
        if (totalWidth > 0) {
            val row = rows[rowNumber]
            if (row != null) {
                totalHeight -= row.maxHeights
            }
        }
        rows.removeAt(rowNumber)
        if (rowNumber < headerRows) {
            --headerRows
            if (rowNumber >= headerRows - footerRows) {
                --footerRows
            }
        }
        return true
    }

    /**
     * Deletes the last row in the table.

     * @return true if the last row was deleted
     */
    fun deleteLastRow(): Boolean {
        return deleteRow(rows.size - 1)
    }

    /**
     * Removes all of the rows except headers
     */
    fun deleteBodyRows() {
        val rows2 = ArrayList<PdfPRow>()
        for (k in 0..headerRows - 1) {
            rows2.add(rows[k])
        }
        rows = rows2
        totalHeight = 0f
        if (totalWidth > 0) {
            totalHeight = headerHeight
        }
    }

    /**
     * Returns the number of columns.

     * @return the number of columns.
     * *
     * @since 2.1.1
     */
    val numberOfColumns: Int
        get() = relativeWidths.size

    /**
     * Gets all the chunks in this element.

     * @return an ArrayList
     */
    override fun getChunks(): List<Chunk> {
        return ArrayList()
    }

    /**
     * Gets the type of the text element.

     * @return a type
     */
    override fun type(): Int {
        return Element.PTABLE
    }

    /**
     * @since iText 2.0.8
     * *
     * @see com.itextpdf.text.Element.isContent
     */
    override fun isContent(): Boolean {
        return true
    }

    /**
     * @since iText 2.0.8
     * *
     * @see com.itextpdf.text.Element.isNestable
     */
    override fun isNestable(): Boolean {
        return true
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param listener an ElementListener
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

    var summary: String
        get() = getAccessibleAttribute(PdfName.SUMMARY)!!.toString()
        set(summary) = setAccessibleAttribute(PdfName.SUMMARY, PdfString(summary))

    /**
     * Gets a row with a given index.

     * @param idx
     * *
     * @return the row at position idx
     */
    fun getRow(idx: Int): PdfPRow {
        return rows[idx]
    }

    /**
     * Returns the index of the last completed row.

     * @return the index of a row
     */
    val lastCompletedRowIndex: Int
        get() = rows.size - 1

    /**
     * Defines where the table may be broken (if necessary).

     * @param breakPoints int[]
     * *
     * @throws java.lang.IndexOutOfBoundsException if a row index is passed that
     * * is out of bounds
     */
    fun setBreakPoints(vararg breakPoints: Int) {
        keepRowsTogether(0, rows.size) // sets all rows as unbreakable

        for (i in breakPoints.indices) {
            getRow(breakPoints[i]).isMayNotBreak = false
        }
    }

    /**
     * Defines which rows should not allow a page break (if possible).

     * @param rows int[]
     * *
     * @throws java.lang.IndexOutOfBoundsException if a row index is passed that
     * * is out of bounds
     */
    fun keepRowsTogether(rows: IntArray) {
        for (i in rows.indices) {
            getRow(rows[i]).isMayNotBreak = true
        }
    }

    /**
     * Defines a range of rows that should not allow a page break (if possible).

     * @param start int
     * *
     * @param end int
     * *
     * @throws java.lang.IndexOutOfBoundsException if a row index is passed that
     * * is out of bounds
     */
    @JvmOverloads fun keepRowsTogether(start: Int, end: Int = rows.size) {
        var start = start
        if (start < end) {
            while (start < end) {
                getRow(start).isMayNotBreak = true
                start++
            }
        }
    }

    /**
     * Gets an arraylist with a selection of rows.

     * @param start the first row in the selection
     * *
     * @param end the first row that isn't part of the selection
     * *
     * @return a selection of rows
     * *
     * @since 2.1.6
     */
    fun getRows(start: Int, end: Int): ArrayList<PdfPRow> {
        val list = ArrayList<PdfPRow>()
        if (start < 0 || end > size()) {
            return list
        }
        for (i in start..end - 1) {
            list.add(adjustCellsInRow(i, end))
        }
        return list
    }

    /**
     * Calculates the extra height needed in a row because of rowspans.

     * @param start the index of the start row (the one to adjust)
     * *
     * @param end the index of the end row on the page
     * *
     * @since 2.1.6
     */
    protected fun adjustCellsInRow(start: Int, end: Int): PdfPRow {
        var row = getRow(start)
        if (row.isAdjusted) {
            return row
        }
        row = PdfPRow(row)
        var cell: PdfPCell?
        val cells = row.cells
        for (i in cells.indices) {
            cell = cells[i]
            if (cell == null || cell.rowspan == 1) {
                continue
            }
            val stop = Math.min(end, start + cell.rowspan)
            var extra = 0f
            for (k in start + 1..stop - 1) {
                extra += getRow(k).maxHeights
            }
            row.setExtraHeight(i, extra)
        }
        row.isAdjusted = true
        return row
    }

    internal fun getEventWidths(xPos: Float, firstRow: Int, lastRow: Int, includeHeaders: Boolean): Array<FloatArray> {
        var firstRow = firstRow
        var lastRow = lastRow
        if (includeHeaders) {
            firstRow = Math.max(firstRow, headerRows)
            lastRow = Math.max(lastRow, headerRows)
        }
        val widths = arrayOfNulls<FloatArray>((if (includeHeaders) headerRows else 0) + lastRow - firstRow)
        if (isColspan) {
            var n = 0
            if (includeHeaders) {
                for (k in 0..headerRows - 1) {
                    val row = rows[k]
                    if (row == null) {
                        ++n
                    } else {
                        widths[n++] = row.getEventWidth(xPos, absoluteWidths)
                    }
                }
            }
            while (firstRow < lastRow) {
                val row = rows[firstRow]
                if (row == null) {
                    ++n
                } else {
                    widths[n++] = row.getEventWidth(xPos, absoluteWidths)
                }
                ++firstRow
            }
        } else {
            val numCols = numberOfColumns
            val width = FloatArray(numCols + 1)
            width[0] = xPos
            for (k in 0..numCols - 1) {
                width[k + 1] = width[k] + absoluteWidths[k]
            }
            for (k in widths.indices) {
                widths[k] = width
            }
        }
        return widths
    }

    /**
     * Gets the spacing before this table.

     * @return the spacing
     */
    fun spacingBefore(): Float {
        return spacingBefore
    }

    /**
     * Gets the spacing after this table.

     * @return the spacing
     */
    fun spacingAfter(): Float {
        return spacingAfter
    }

    /**
     * Gets the value of the last row extension.

     * @return true if the last row will extend; false otherwise
     */
    /**
     * When set the last row on every page will be extended to fill all the
     * remaining space to the bottom boundary.

     * @param extendLastRows true to extend the last row; false otherwise
     */
    var isExtendLastRow: Boolean
        get() = extendLastRow[0]
        set(extendLastRows) {
            extendLastRow[0] = extendLastRows
            extendLastRow[1] = extendLastRows
        }

    /**
     * When set the last row on every page will be extended to fill all the
     * remaining space to the bottom boundary; except maybe the final row.

     * @param extendLastRows true to extend the last row on each page; false
     * * otherwise
     * *
     * @param extendFinalRow false if you don't want to extend the final row of
     * * the complete table
     * *
     * @since iText 5.0.0
     */
    fun setExtendLastRow(extendLastRows: Boolean, extendFinalRow: Boolean) {
        extendLastRow[0] = extendLastRows
        extendLastRow[1] = extendFinalRow
    }

    /**
     * Gets the value of the last row extension, taking into account if the
     * final row is reached or not.

     * @return true if the last row will extend; false otherwise
     * *
     * @since iText 5.0.0
     */
    fun isExtendLastRow(newPageFollows: Boolean): Boolean {
        if (newPageFollows) {
            return extendLastRow[0]
        }
        return extendLastRow[1]
    }

    /**
     * Completes the current row with the default cell. An incomplete row will
     * be dropped but calling this method will make sure that it will be present
     * in the table.
     */
    fun completeRow() {
        while (!rowCompleted) {
            addCell(defaultCell)
        }
    }

    /**
     * @since iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.flushContent
     */
    override fun flushContent() {
        deleteBodyRows()

        // setSkipFirstHeader(boolean) shouldn't be set to true if the table hasn't been added yet.
        if (this.numberOfWrittenRows > 0) {
            isSkipFirstHeader = true
        }
    }

    /**
     * Adds the number of written rows to the counter.

     * @param numberOfWrittenRows number of newly written rows
     * *
     * @since 5.5.4
     */
    internal fun addNumberOfRowsWritten(numberOfWrittenRows: Int) {
        this.numberOfWrittenRows += numberOfWrittenRows
    }

    /**
     * @since iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.isComplete
     */
    override fun isComplete(): Boolean {
        return complete
    }

    /**
     * @since iText 2.0.8
     * *
     * @see com.itextpdf.text.LargeElement.setComplete
     */
    override fun setComplete(complete: Boolean) {
        this.complete = complete
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

    fun getHeader(): PdfPTableHeader {
        if (header == null) {
            header = PdfPTableHeader()
        }
        return header
    }

    fun getBody(): PdfPTableBody {
        if (body == null) {
            body = PdfPTableBody()
        }
        return body
    }

    fun getFooter(): PdfPTableFooter {
        if (footer == null) {
            footer = PdfPTableFooter()
        }
        return footer
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    /**
     * Gets row index where cell overlapping (rowIdx, colIdx) starts

     * @param rowIdx
     * *
     * @param colIdx
     * *
     * @return row index
     * *
     * @since iText 5.4.3
     */
    fun getCellStartRowIndex(rowIdx: Int, colIdx: Int): Int {
        var lastRow = rowIdx
        while (getRow(lastRow).cells[colIdx] == null && lastRow > 0) {
            --lastRow
        }
        return lastRow
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    /**

     * @since iText 5.4.3
     */
    class FittingRows(val firstRow: Int, val lastRow: Int, val height: Float, val completedRowsHeight: Float,
                      private val correctedHeightsForLastRow: Map<Int, Float>) {

        /**
         * Correct chosen last fitting row so that the content of all cells with
         * open rowspans will fit on the page, i.e. the cell content won't be
         * split. (Only to be used with splitLate == true)
         */
        fun correctLastRowChosen(table: PdfPTable, k: Int) {
            val row = table.getRow(k)
            val value = correctedHeightsForLastRow[k]
            if (value != null) {
                row.setFinalMaxHeights(value)
                //System.out.printf("corrected chosen last fitting row: %6.0f\n\n", row.getMaxHeights());
            }
        }
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    /**

     * @since iText 5.4.3
     */
    class ColumnMeasurementState {

        var height = 0f

        var rowspan = 1
        var colspan = 1

        fun beginCell(cell: PdfPCell, completedRowsHeight: Float, rowHeight: Float) {
            rowspan = cell.rowspan
            colspan = cell.colspan
            height = completedRowsHeight + Math.max(if (cell.hasCachedMaxHeight()) cell.cachedMaxHeight else cell.maxHeight, rowHeight)
        }

        fun consumeRowspan(completedRowsHeight: Float, rowHeight: Float) {
            --rowspan
        }

        fun cellEnds(): Boolean {
            return rowspan == 1
        }
    }

    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
    /**
     * Determine which rows fit on the page, respecting isSplitLate(). Note:
     * sets max heights of the inspected rows as a side effect, just like
     * PdfPTable.getRowHeight(int, boolean) does. Respect row.getMaxHeights() if
     * it has been previously set (which might be independent of the height of
     * individual cells). The last row written on the page will be chosen by the
     * caller who might choose not the calculated one but an earlier one (due to
     * mayNotBreak settings on the rows). The height of the chosen last row has
     * to be corrected if splitLate == true by calling
     * FittingRows.correctLastRowChosen() by the caller to avoid splitting the
     * content of cells with open rowspans.

     * @since iText 5.4.3
     */
    fun getFittingRows(availableHeight: Float, startIdx: Int): FittingRows {
        LOGGER.info(String.format("getFittingRows(%s, %s)", availableHeight, startIdx))
        if (startIdx > 0 && startIdx < rows.size) {
            assert(getRow(startIdx).cells[0] != null) // top left cell of current page may not be null
        }
        val cols = numberOfColumns
        val states = arrayOfNulls<ColumnMeasurementState>(cols)
        for (i in 0..cols - 1) {
            states[i] = ColumnMeasurementState()
        }
        var completedRowsHeight = 0f // total height of all rows up to k only counting completed cells (with no open
        // rowspans)
        var totalHeight = 0f // total height needed to display all rows up to k, respecting rowspans
        val correctedHeightsForLastRow = HashMap<Int, Float>()
        var k: Int
        k = startIdx
        while (k < size()) {
            val row = getRow(k)
            val rowHeight = row.maxRowHeightsWithoutCalculating
            var maxCompletedRowsHeight = 0f
            var i = 0
            while (i < cols) {
                val cell = row.cells[i]
                val state = states[i]
                if (cell == null) {
                    state.consumeRowspan(completedRowsHeight, rowHeight)
                } else {
                    state.beginCell(cell, completedRowsHeight, rowHeight)
                    LOGGER.info(String.format("Height after beginCell: %s (cell: %s)", state.height, cell.cachedMaxHeight))
                }
                if (state.cellEnds() && state.height > maxCompletedRowsHeight) {
                    maxCompletedRowsHeight = state.height
                }
                for (j in 1..state.colspan - 1) {
                    states[i + j].height = state.height
                }
                i += state.colspan
                //System.out.printf("%6.0f", state.height);
            }
            var maxTotalHeight = 0f
            for (state in states) {
                if (state.height > maxTotalHeight) {
                    maxTotalHeight = state.height
                }
            }
            row.setFinalMaxHeights(maxCompletedRowsHeight - completedRowsHeight)
            //System.out.printf(" | %6.0f | %6.0f %6.0f | row: %6.0f\n", rowHeight, maxCompletedRowsHeight, maxTotalHeight, row.getMaxHeights());
            val remainingHeight = availableHeight - if (isSplitLate) maxTotalHeight else maxCompletedRowsHeight
            if (remainingHeight < 0) {
                break
            }
            correctedHeightsForLastRow.put(k, maxTotalHeight - completedRowsHeight)
            completedRowsHeight = maxCompletedRowsHeight
            totalHeight = maxTotalHeight
            ++k
        }
        rowsNotChecked = false
        return FittingRows(startIdx, k - 1, totalHeight, completedRowsHeight, correctedHeightsForLastRow)
    }

    companion object {
        /**
         * The index of the original PdfcontentByte.
         */
        val BASECANVAS = 0

        /**
         * The index of the duplicate PdfContentByte where the
         * background will be drawn.
         */
        val BACKGROUNDCANVAS = 1

        /**
         * The index of the duplicate PdfContentByte where the border
         * lines will be drawn.
         */
        val LINECANVAS = 2

        /**
         * The index of the duplicate PdfContentByte where the text
         * will be drawn.
         */
        val TEXTCANVAS = 3

        /**
         * Makes a shallow copy of a table (format without content).

         * @param table
         * *
         * @return a shallow copy of the table
         */
        fun shallowCopy(table: PdfPTable): PdfPTable {
            val nt = PdfPTable()
            nt.copyFormat(table)
            return nt
        }

        /**
         * Gets and initializes the 4 layers where the table is written to. The text
         * or graphics are added to one of the 4 PdfContentByte
         * returned with the following order:
         *
         *
         *
         *  * PdfPtable.BASECANVAS - the original
         * PdfContentByte. Anything placed here will be under the
         * table.
         *  * PdfPtable.BACKGROUNDCANVAS - the layer where the
         * background goes to.
         *  * PdfPtable.LINECANVAS - the layer where the lines go to.
         *  * PdfPtable.TEXTCANVAS - the layer where the text go to.
         * Anything placed here will be over the table.
         *
         *
         * The layers are placed in sequence on top of each other.

         * @param canvas the PdfContentByte where the rows will be
         * * written to
         * *
         * @return an array of 4 PdfContentByte
         * *
         * @see .writeSelectedRows
         */
        fun beginWritingRows(canvas: PdfContentByte): Array<PdfContentByte> {
            return arrayOf(canvas, canvas.duplicate, canvas.duplicate, canvas.duplicate)
        }

        /**
         * Finishes writing the table.

         * @param canvases the array returned by beginWritingRows()
         */
        fun endWritingRows(canvases: Array<PdfContentByte>) {
            val canvas = canvases[BASECANVAS]
            val artifact = PdfArtifact()
            canvas.openMCBlock(artifact)
            canvas.saveState()
            canvas.add(canvases[BACKGROUNDCANVAS])
            canvas.restoreState()
            canvas.saveState()
            canvas.setLineCap(2)
            canvas.resetRGBColorStroke()
            canvas.add(canvases[LINECANVAS])
            canvas.restoreState()
            canvas.closeMCBlock(artifact)
            canvas.add(canvases[TEXTCANVAS])
        }
    }
}
/**
 * Writes the selected rows and columns to the document. This method does
 * not clip the columns; this is only important if there are columns with
 * colspan at boundaries. canvases is obtained from
 * beginWritingRows(). The table event is only fired for
 * complete rows.

 * @param colStart the first column to be written, zero index
 * *
 * @param colEnd the last column to be written + 1. If it is -1 all the
 * * columns to the end are written
 * *
 * @param rowStart the first row to be written, zero index
 * *
 * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
 * * the end are written
 * *
 * @param xPos the x write coordinate
 * *
 * @param yPos the y write coordinate
 * *
 * @param canvases an array of 4 PdfContentByte obtained from
 * * beginWritingRows()
 * *
 * @return the y coordinate position of the bottom of the last row
 * *
 * @see .beginWritingRows
 */
/**
 * Writes the selected rows and columns to the document. This method clips
 * the columns; this is only important if there are columns with colspan at
 * boundaries. The table event is only fired for complete rows.

 * @param colStart the first column to be written, zero index
 * *
 * @param colEnd the last column to be written + 1. If it is -1 all the
 * * columns to the end are written
 * *
 * @param rowStart the first row to be written, zero index
 * *
 * @param rowEnd the last row to be written + 1. If it is -1 all the rows to
 * * the end are written
 * *
 * @param xPos the x write coordinate
 * *
 * @param yPos the y write coordinate
 * *
 * @param canvas the PdfContentByte where the rows will be
 * * written to
 * *
 * @return the y coordinate position of the bottom of the last row
 */
/**
 * Defines a range of rows (from the parameter to the last row) that should
 * not allow a page break (if possible). The equivalent of calling
 * [keepRowsTogether(start, rows.size()][.keepRowsTogether].

 * @param start int
 * *
 * @throws java.lang.IndexOutOfBoundsException if a row index is passed that
 * * is out of bounds
 */
