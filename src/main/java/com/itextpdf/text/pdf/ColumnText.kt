/*
 * $Id: ce4b9fecdc4c91a356530dd21e9dadf9c0fbe2f0 $
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
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.PdfPTable.FittingRows
import com.itextpdf.text.pdf.draw.DrawInterface
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.languages.ArabicLigaturizer

import java.util.ArrayList
import java.util.LinkedList
import java.util.Stack

/**
 * Formats text in a columnwise form. The text is bound on the left and on the
 * right by a sequence of lines. This allows the column to have any shape, not
 * only rectangular.
 *
 * Several parameters can be set like the first paragraph line indent and extra
 * space between paragraphs.
 *
 * A call to the method go will return one of the following
 * situations: the column ended or the text ended.
 *
 * If the column ended, a new column definition can be loaded with the method
 * setColumns and the method go can be called again.
 *
 * If the text ended, more text can be loaded with addText and the
 * method go can be called again.
 * The only limitation is that one or more complete paragraphs must be loaded
 * each time.
 *
 * Full bidirectional reordering is supported. If the run direction is
 * PdfWriter.RUN_DIRECTION_RTL the meaning of the horizontal
 * alignments and margins is mirrored.

 * @author Paulo Soares
 */
class ColumnText
/**
 * Creates a ColumnText.

 * @param canvas the place where the text will be written to. Can be a
 * * template.
 */
(canvas: PdfContentByte) {

    private val LOGGER = LoggerFactory.getLogger(ColumnText::class.java)

    /**
     * Gets the run direction.

     * @return the run direction
     */
    /**
     * Sets the run direction.

     * @param runDirection the run direction
     */
    var runDirection = PdfWriter.RUN_DIRECTION_DEFAULT
        set(runDirection) {
            if (runDirection < PdfWriter.RUN_DIRECTION_DEFAULT || runDirection > PdfWriter.RUN_DIRECTION_RTL) {
                throw RuntimeException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection))
            }
            this.runDirection = runDirection
        }

    /**
     * Upper bound of the column.
     */
    protected var maxY: Float = 0.toFloat()

    /**
     * Lower bound of the column.
     */
    protected var minY: Float = 0.toFloat()

    protected var leftX: Float = 0.toFloat()

    protected var rightX: Float = 0.toFloat()

    /**
     * The column alignment. Default is left alignment.
     */
    /**
     * Gets the alignment.

     * @return the alignment
     */
    /**
     * Sets the alignment.

     * @param alignment the alignment
     */
    var alignment = Element.ALIGN_LEFT

    /**
     * The left column bound.
     */
    protected var leftWall: ArrayList<FloatArray>? = null

    /**
     * The right column bound.
     */
    protected var rightWall: ArrayList<FloatArray>? = null

    /**
     * The chunks that form the text.
     */
    //    protected ArrayList chunks = new ArrayList();
    protected var bidiLine: BidiLine? = null

    /**
     * Call this after go() to know if any word was split into several lines.

     * @return
     */
    var isWordSplit: Boolean = false
        protected set

    /**
     * The current y line location. Text will be written at this line minus the
     * leading.
     */
    /**
     * Gets the yLine.

     * @return the yLine
     */
    /**
     * Sets the yLine. The line will be written to yLine-leading.

     * @param yLine the yLine
     */
    var yLine: Float = 0.toFloat()

    /**
     * The X position after the last line that has been written.

     * @since 5.0.3
     */
    /**
     * Gets the X position of the end of the last line that has been written
     * (will not work in simulation mode!).

     * @since 5.0.3
     */
    var lastX: Float = 0.toFloat()
        protected set

    /**
     * The leading for the current line.
     */
    /**
     * Gets the currentLeading.

     * @return the currentLeading
     */
    var currentLeading = 16f
        protected set

    /**
     * The fixed text leading.
     */
    /**
     * Gets the fixed leading.

     * @return the leading
     */
    /**
     * Sets the leading to fixed.

     * @param leading the leading
     */
    var leading = 16f
        set(leading) {
            this.leading = leading
            multipliedLeading = 0f
        }

    /**
     * The text leading that is multiplied by the biggest font size in the line.
     */
    /**
     * Gets the variable leading.

     * @return the leading
     */
    var multipliedLeading = 0f
        protected set

    /**
     * The PdfContent where the text will be written to.
     */
    /**
     * Gets the canvas. If a set of four canvases exists, the TEXTCANVAS is
     * returned.

     * @return a PdfContentByte.
     */
    /**
     * Sets the canvas. If before a set of four canvases was set, it is being
     * unset.

     * @param canvas
     */
    var canvas: PdfContentByte? = null
        set(canvas) {
            this.canvas = canvas
            this.canvases = null
            if (compositeColumn != null) {
                compositeColumn!!.canvas = canvas
            }
        }

    /**
     * Gets the canvases.

     * @return an array of PdfContentByte
     */
    /**
     * Sets the canvases.

     * @param canvases
     */
    var canvases: Array<PdfContentByte>? = null
        set(canvases) {
            this.canvases = canvases
            this.canvas = canvases[PdfPTable.TEXTCANVAS]
            if (compositeColumn != null) {
                compositeColumn!!.canvases = canvases
            }
        }

    /**
     * The line status when trying to fit a line to a column.
     */
    protected var lineStatus: Int = 0

    /**
     * The first paragraph line indent.
     */
    /**
     * Gets the first paragraph line indent.

     * @return the indent
     */
    /**
     * Sets the first paragraph line indent.

     * @param indent the indent
     */
    var indent = 0f
        set(indent) = setIndent(indent, true)

    /**
     * The following paragraph lines indent.
     */
    /**
     * Gets the following paragraph lines indent.

     * @return the indent
     */
    /**
     * Sets the following paragraph lines indent.

     * @param indent the indent
     */
    var followingIndent = 0f
        set(indent) {
            this.followingIndent = indent
            lastWasNewline = true
        }

    /**
     * The right paragraph lines indent.
     */
    /**
     * Gets the right paragraph lines indent.

     * @return the indent
     */
    /**
     * Sets the right paragraph lines indent.

     * @param indent the indent
     */
    var rightIndent = 0f
        set(indent) {
            this.rightIndent = indent
            lastWasNewline = true
        }

    /**
     * The extra space between paragraphs.
     */
    /**
     * Sets the extra space between paragraphs.

     * @return the extra space between paragraphs
     */
    /**
     * Sets the extra space between paragraphs.

     * @param extraParagraphSpace the extra space between paragraphs
     */
    var extraParagraphSpace = 0f

    /**
     * The width of the line when the column is defined as a simple rectangle.
     */
    protected var rectangularWidth = -1f

    protected var rectangularMode = false
    /**
     * Holds value of property spaceCharRatio.
     */
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
    var spaceCharRatio = GLOBAL_SPACE_CHAR_RATIO

    private var lastWasNewline = true
    private var repeatFirstLineIndent = true

    /**
     * Holds value of property linesWritten.
     */
    /**
     * Gets the number of lines written.

     * @return the number of lines written
     */
    var linesWritten: Int = 0
        private set

    private var firstLineY: Float = 0.toFloat()
    private var firstLineYDone = false

    /**
     * Holds value of property arabicOptions.
     */
    /**
     * Gets the arabic shaping options.

     * @return the arabic shaping options
     */
    /**
     * Sets the arabic shaping options. The option can be AR_NOVOWEL,
     * AR_COMPOSEDTASHKEEL and AR_LIG.

     * @param arabicOptions the arabic shaping options
     */
    var arabicOptions = 0

    /**
     * Gets the biggest descender value of the last line written.

     * @return the biggest descender value of the last line written
     */
    var descender: Float = 0.toFloat()
        protected set

    protected var composite = false

    protected var compositeColumn: ColumnText? = null

    protected var compositeElements: LinkedList<Element>? = null

    protected var listIdx = 0
    /**
     * Pointer for the row in a table that is being dealt with

     * @since 5.1.0
     */
    /**
     * Gets the number of rows that were drawn when a table is involved.
     */
    var rowsDrawn = 0
        protected set

    /**
     * The index of the last row that needed to be splitted.
     * -2 value mean it is the first attempt to split the first row.
     * -1 means that we try to avoid splitting current row.

     * @since 5.0.1 changed a boolean into an int
     */
    private var splittedRow = -2

    protected var waitPhrase: Phrase? = null

    /**
     * if true, first line height is adjusted so that the max ascender touches
     * the top
     */
    /**
     * Checks if UseAscender is enabled/disabled.

     * @return true is the adjustment of the first line height is based on max
     * * ascender.
     */
    /**
     * Enables/Disables adjustment of first line height based on max ascender.

     * @param useAscender    enable adjustment if true
     */
    var isUseAscender = false

    /**
     * Holds value of property filledWidth.
     */
    /**
     * Gets the real width used by the largest line.

     * @return the real width used by the largest line
     */
    /**
     * Sets the real width used by the largest line. Only used to set it to zero
     * to start another measurement.

     * @param filledWidth the real width used by the largest line
     */
    var filledWidth: Float = 0.toFloat()

    /**
     * Gets the first line adjustment property.

     * @return the first line adjustment property.
     */
    /**
     * Sets the first line adjustment. Some objects have properties, like
     * spacing before, that behave differently if the object is the first to be
     * written after go() or not. The first line adjustment is true
     * by default but can be changed if several objects are to be placed one
     * after the other in the same column calling go() several times.

     * @param adjustFirstLine true to adjust the first line,
     * * false otherwise
     */
    var isAdjustFirstLine = true

    /**
     * @since 5.4.2
     */
    var inheritGraphicState = false

    var isIgnoreSpacingBefore = true

    init {
        this.canvas = canvas
    }

    /**
     * Makes this instance an independent copy of org.

     * @param org the original ColumnText
     * *
     * @return itself
     */
    fun setACopy(org: ColumnText?): ColumnText {
        if (org != null) {
            setSimpleVars(org)
            if (org.bidiLine != null) {
                bidiLine = BidiLine(org.bidiLine)
            }
        }
        return this
    }

    protected fun setSimpleVars(org: ColumnText) {
        maxY = org.maxY
        minY = org.minY
        alignment = org.alignment
        leftWall = null
        if (org.leftWall != null) {
            leftWall = ArrayList(org.leftWall)
        }
        rightWall = null
        if (org.rightWall != null) {
            rightWall = ArrayList(org.rightWall)
        }
        yLine = org.yLine
        currentLeading = org.currentLeading
        leading = org.leading
        multipliedLeading = org.multipliedLeading
        canvas = org.canvas
        canvases = org.canvases
        lineStatus = org.lineStatus
        indent = org.indent
        followingIndent = org.followingIndent
        rightIndent = org.rightIndent
        extraParagraphSpace = org.extraParagraphSpace
        rectangularWidth = org.rectangularWidth
        rectangularMode = org.rectangularMode
        spaceCharRatio = org.spaceCharRatio
        lastWasNewline = org.lastWasNewline
        repeatFirstLineIndent = org.repeatFirstLineIndent
        linesWritten = org.linesWritten
        arabicOptions = org.arabicOptions
        runDirection = org.runDirection
        descender = org.descender
        composite = org.composite
        splittedRow = org.splittedRow
        if (org.composite) {
            compositeElements = LinkedList<Element>()
            for (element in org.compositeElements!!) {
                if (element is PdfPTable) {
                    compositeElements!!.add(PdfPTable(element))
                } else {
                    compositeElements!!.add(element)
                }
            }
            if (org.compositeColumn != null) {
                compositeColumn = duplicate(org.compositeColumn)
            }
        }
        listIdx = org.listIdx
        rowsDrawn = org.rowsDrawn
        firstLineY = org.firstLineY
        leftX = org.leftX
        rightX = org.rightX
        firstLineYDone = org.firstLineYDone
        waitPhrase = org.waitPhrase
        isUseAscender = org.isUseAscender
        filledWidth = org.filledWidth
        isAdjustFirstLine = org.isAdjustFirstLine
        inheritGraphicState = org.inheritGraphicState
        isIgnoreSpacingBefore = org.isIgnoreSpacingBefore
    }

    private fun addWaitingPhrase() {
        if (bidiLine == null && waitPhrase != null) {
            bidiLine = BidiLine()
            for (c in waitPhrase!!.chunks) {
                bidiLine!!.addChunk(PdfChunk(c, null, waitPhrase!!.tabSettings))
            }
            waitPhrase = null
        }
    }

    /**
     * Adds a Phrase to the current text array. Will not have any
     * effect if addElement() was called before.

     * @param phrase the text
     */
    fun addText(phrase: Phrase?) {
        if (phrase == null || composite) {
            return
        }
        addWaitingPhrase()
        if (bidiLine == null) {
            waitPhrase = phrase
            return
        }
        for (element in phrase.chunks) {
            bidiLine!!.addChunk(PdfChunk(element as Chunk, null, phrase.tabSettings))
        }
    }

    /**
     * Replaces the current text array with this Phrase. Anything
     * added previously with addElement() is lost.

     * @param phrase the text
     */
    fun setText(phrase: Phrase) {
        bidiLine = null
        composite = false
        compositeColumn = null
        compositeElements = null
        listIdx = 0
        rowsDrawn = 0
        splittedRow = -1
        waitPhrase = phrase
    }

    /**
     * Adds a Chunk to the current text array. Will not have any
     * effect if addElement() was called before.

     * @param chunk the text
     */
    fun addText(chunk: Chunk?) {
        if (chunk == null || composite) {
            return
        }
        addText(Phrase(chunk))
    }

    /**
     * Adds an element. Elements supported are Paragraph,
     * List, PdfPTable and Image. Also
     * accepts a `Chunk` and a `Phrase`, they are placed
     * in a new `Paragraph`.
     *
     *
     * It removes all the text placed with addText().

     * @param element the Element
    `` */
    fun addElement(element: Element?) {
        var element: Element? = element ?: return
        if (element is Image) {
            val t = PdfPTable(1)
            val w = element.widthPercentage
            if (w == 0f) {
                t.totalWidth = element.scaledWidth
                t.isLockedWidth = true
            } else {
                t.widthPercentage = w
            }
            t.spacingAfter = element.getSpacingAfter()
            t.spacingBefore = element.getSpacingBefore()
            when (element.alignment) {
                Image.LEFT -> t.horizontalAlignment = Element.ALIGN_LEFT
                Image.RIGHT -> t.horizontalAlignment = Element.ALIGN_RIGHT
                else -> t.horizontalAlignment = Element.ALIGN_CENTER
            }
            val c = PdfPCell(element, true)
            c.setPadding(0f)
            c.setBorder(element.border)
            c.setBorderColor(element.borderColor)
            c.setBorderWidth(element.borderWidth)
            c.setBackgroundColor(element.backgroundColor)
            t.addCell(c)
            element = t
        }
        if (element.type() == Element.CHUNK) {
            element = Paragraph(element as Chunk?)
        } else if (element.type() == Element.PHRASE) {
            element = Paragraph(element as Phrase?)
        } else if (element.type() == Element.PTABLE) {
            (element as PdfPTable).init()
        }
        if (element.type() != Element.PARAGRAPH && element.type() != Element.LIST && element.type() != Element.PTABLE && element.type() != Element.YMARK && element.type() != Element.DIV) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("element.not.allowed"))
        }
        if (!composite) {
            composite = true
            compositeElements = LinkedList<Element>()
            bidiLine = null
            waitPhrase = null
        }
        if (element.type() == Element.PARAGRAPH) {
            val p = element as Paragraph?
            compositeElements!!.addAll(p.breakUp())
            return
        }
        compositeElements!!.add(element)
    }

    /**
     * Converts a sequence of lines representing one of the column bounds into
     * an internal format.
     *
     *
     * Each array element will contain a float[4] representing the
     * line x = ax + b.

     * @param cLine the column array
     * *
     * @return the converted array
     */
    protected fun convertColumn(cLine: FloatArray): ArrayList<FloatArray> {
        if (cLine.size < 4) {
            throw RuntimeException(MessageLocalization.getComposedMessage("no.valid.column.line.found"))
        }
        val cc = ArrayList<FloatArray>()
        var k = 0
        while (k < cLine.size - 2) {
            val x1 = cLine[k]
            val y1 = cLine[k + 1]
            val x2 = cLine[k + 2]
            val y2 = cLine[k + 3]
            if (y1 == y2) {
                k += 2
                continue
            }
            // x = ay + b
            val a = (x1 - x2) / (y1 - y2)
            val b = x1 - a * y1
            val r = FloatArray(4)
            r[0] = Math.min(y1, y2)
            r[1] = Math.max(y1, y2)
            r[2] = a
            r[3] = b
            cc.add(r)
            maxY = Math.max(maxY, r[1])
            minY = Math.min(minY, r[0])
            k += 2
        }
        if (cc.isEmpty()) {
            throw RuntimeException(MessageLocalization.getComposedMessage("no.valid.column.line.found"))
        }
        return cc
    }

    /**
     * Finds the intersection between the yLine and the column. It
     * will set the lineStatus appropriately.

     * @param wall the column to intersect
     * *
     * @return the x coordinate of the intersection
     */
    protected fun findLimitsPoint(wall: ArrayList<FloatArray>): Float {
        lineStatus = LINE_STATUS_OK
        if (yLine < minY || yLine > maxY) {
            lineStatus = LINE_STATUS_OFFLIMITS
            return 0f
        }
        for (k in wall.indices) {
            val r = wall[k]
            if (yLine < r[0] || yLine > r[1]) {
                continue
            }
            return r[2] * yLine + r[3]
        }
        lineStatus = LINE_STATUS_NOLINE
        return 0f
    }

    /**
     * Finds the intersection between the yLine and the two column
     * bounds. It will set the lineStatus appropriately.

     * @return a float[2]with the x coordinates of the intersection
     */
    protected fun findLimitsOneLine(): FloatArray? {
        val x1 = findLimitsPoint(leftWall)
        if (lineStatus == LINE_STATUS_OFFLIMITS || lineStatus == LINE_STATUS_NOLINE) {
            return null
        }
        val x2 = findLimitsPoint(rightWall)
        if (lineStatus == LINE_STATUS_NOLINE) {
            return null
        }
        return floatArrayOf(x1, x2)
    }

    /**
     * Finds the intersection between the yLine, the
     * yLine-leadingand the two column bounds. It will set the
     * lineStatus appropriately.

     * @return a float[4]with the x coordinates of the intersection
     */
    protected fun findLimitsTwoLines(): FloatArray? {
        var repeat = false
        while (true) {
            if (repeat && currentLeading == 0f) {
                return null
            }
            repeat = true
            val x1 = findLimitsOneLine()
            if (lineStatus == LINE_STATUS_OFFLIMITS) {
                return null
            }
            yLine -= currentLeading
            if (lineStatus == LINE_STATUS_NOLINE) {
                continue
            }
            val x2 = findLimitsOneLine()
            if (lineStatus == LINE_STATUS_OFFLIMITS) {
                return null
            }
            if (lineStatus == LINE_STATUS_NOLINE) {
                yLine -= currentLeading
                continue
            }
            if (x1[0] >= x2[1] || x2[0] >= x1[1]) {
                continue
            }
            return floatArrayOf(x1[0], x1[1], x2[0], x2[1])
        }
    }

    /**
     * Sets the columns bounds. Each column bound is described by a
     * float[] with the line points [x1,y1,x2,y2,...]. The array
     * must have at least 4 elements.

     * @param leftLine the left column bound
     * *
     * @param rightLine the right column bound
     */
    fun setColumns(leftLine: FloatArray, rightLine: FloatArray) {
        maxY = -10e20f
        minY = 10e20f
        yLine = Math.max(leftLine[1], leftLine[leftLine.size - 1])
        rightWall = convertColumn(rightLine)
        leftWall = convertColumn(leftLine)
        rectangularWidth = -1f
        rectangularMode = false
    }

    /**
     * Simplified method for rectangular columns.

     * @param phrase a Phrase
     * *
     * @param llx the lower left x corner
     * *
     * @param lly the lower left y corner
     * *
     * @param urx the upper right x corner
     * *
     * @param ury the upper right y corner
     * *
     * @param leading the leading
     * *
     * @param alignment the column alignment
     */
    fun setSimpleColumn(phrase: Phrase, llx: Float, lly: Float, urx: Float, ury: Float, leading: Float, alignment: Int) {
        addText(phrase)
        setSimpleColumn(llx, lly, urx, ury, leading, alignment)
    }

    /**
     * Simplified method for rectangular columns.

     * @param llx the lower left x corner
     * *
     * @param lly the lower left y corner
     * *
     * @param urx the upper right x corner
     * *
     * @param ury the upper right y corner
     * *
     * @param leading the leading
     * *
     * @param alignment the column alignment
     */
    fun setSimpleColumn(llx: Float, lly: Float, urx: Float, ury: Float, leading: Float, alignment: Int) {
        leading = leading
        this.alignment = alignment
        setSimpleColumn(llx, lly, urx, ury)
    }

    /**
     * Simplified method for rectangular columns.

     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun setSimpleColumn(llx: Float, lly: Float, urx: Float, ury: Float) {
        leftX = Math.min(llx, urx)
        maxY = Math.max(lly, ury)
        minY = Math.min(lly, ury)
        rightX = Math.max(llx, urx)
        yLine = maxY
        rectangularWidth = rightX - leftX
        if (rectangularWidth < 0) {
            rectangularWidth = 0f
        }
        rectangularMode = true
    }

    /**
     * Simplified method for rectangular columns.

     * @param rect    the rectangle for the column
     */
    fun setSimpleColumn(rect: Rectangle) {
        setSimpleColumn(rect.left, rect.bottom, rect.right, rect.top)
    }

    /**
     * Sets the leading fixed and variable. The resultant leading will be
     * fixedLeading+multipliedLeading*maxFontSize where maxFontSize is the size
     * of the biggest font in the line.

     * @param fixedLeading the fixed leading
     * *
     * @param multipliedLeading the variable leading
     */
    fun setLeading(fixedLeading: Float, multipliedLeading: Float) {
        this.leading = fixedLeading
        this.multipliedLeading = multipliedLeading
    }

    /**
     * Sets the first paragraph line indent.

     * @param indent the indent
     * *
     * @param    repeatFirstLineIndent    do we need to repeat the indentation of the
     * * first line after a newline?
     */
    fun setIndent(indent: Float, repeatFirstLineIndent: Boolean) {
        this.indent = indent
        lastWasNewline = true
        this.repeatFirstLineIndent = repeatFirstLineIndent
    }

    @Throws(DocumentException::class)
    @JvmOverloads fun go(simulate: Boolean = false, elementToGo: IAccessibleElement? = null): Int {
        isWordSplit = false
        if (composite) {
            return goComposite(simulate)
        }

        var lBody: ListBody? = null
        if (isTagged(canvas) && elementToGo is ListItem) {
            lBody = elementToGo.listBody
        }

        addWaitingPhrase()
        if (bidiLine == null) {
            return NO_MORE_TEXT
        }
        descender = 0f
        linesWritten = 0
        lastX = 0f
        var dirty = false
        var ratio = spaceCharRatio
        val currentValues = arrayOfNulls<Any>(2)
        var currentFont: PdfFont? = null
        val lastBaseFactor = 0
        currentValues[1] = lastBaseFactor
        var pdf: PdfDocument? = null
        var graphics: PdfContentByte? = null
        var text: PdfContentByte? = null
        firstLineY = java.lang.Float.NaN
        var localRunDirection = PdfWriter.RUN_DIRECTION_NO_BIDI
        if (runDirection != PdfWriter.RUN_DIRECTION_DEFAULT) {
            localRunDirection = runDirection
        }
        if (canvas != null) {
            graphics = canvas
            pdf = canvas!!.pdfDocument
            if (!isTagged(canvas)) {
                text = canvas!!.getDuplicate(inheritGraphicState)
            } else {
                text = canvas
            }
        } else if (!simulate) {
            throw NullPointerException(MessageLocalization.getComposedMessage("columntext.go.with.simulate.eq.eq.false.and.text.eq.eq.null"))
        }
        if (!simulate) {
            if (ratio == GLOBAL_SPACE_CHAR_RATIO) {
                ratio = text!!.pdfWriter.spaceCharRatio
            } else if (ratio < 0.001f) {
                ratio = 0.001f
            }
        }
        if (!rectangularMode) {
            var max = 0f
            for (c in bidiLine!!.chunks) {
                max = Math.max(max, c.height())
            }
            currentLeading = leading + max * multipliedLeading
        }
        var firstIndent = 0f
        var line: PdfLine?
        var x1: Float
        var status = 0
        var rtl = false
        while (true) {
            firstIndent = if (lastWasNewline) indent else followingIndent //
            if (rectangularMode) {
                if (rectangularWidth <= firstIndent + rightIndent) {
                    status = NO_MORE_COLUMN
                    if (bidiLine!!.isEmpty) {
                        status = status or NO_MORE_TEXT
                    }
                    break
                }
                if (bidiLine!!.isEmpty) {
                    status = NO_MORE_TEXT
                    break
                }
                line = bidiLine!!.processLine(leftX, rectangularWidth - firstIndent - rightIndent, alignment, localRunDirection, arabicOptions, minY, yLine, descender)
                isWordSplit = isWordSplit or bidiLine!!.isWordSplit
                if (line == null) {
                    status = NO_MORE_TEXT
                    break
                }
                val maxSize = line.getMaxSize(leading, multipliedLeading)
                if (isUseAscender && java.lang.Float.isNaN(firstLineY)) {
                    currentLeading = line.ascender
                } else {
                    currentLeading = Math.max(maxSize[0], maxSize[1] - descender)
                }
                if (yLine > maxY || yLine - currentLeading < minY) {
                    status = NO_MORE_COLUMN
                    bidiLine!!.restore()
                    break
                }
                yLine -= currentLeading
                if (!simulate && !dirty) {
                    if (line.isRTL && canvas!!.isTagged) {
                        canvas!!.beginMarkedContentSequence(PdfName.REVERSEDCHARS)
                        rtl = true
                    }
                    text!!.beginText()
                    dirty = true
                }
                if (java.lang.Float.isNaN(firstLineY)) {
                    firstLineY = yLine
                }
                updateFilledWidth(rectangularWidth - line.widthLeft())
                x1 = leftX
            } else {
                val yTemp = yLine - currentLeading
                val xx = findLimitsTwoLines()
                if (xx == null) {
                    status = NO_MORE_COLUMN
                    if (bidiLine!!.isEmpty) {
                        status = status or NO_MORE_TEXT
                    }
                    yLine = yTemp
                    break
                }
                if (bidiLine!!.isEmpty) {
                    status = NO_MORE_TEXT
                    yLine = yTemp
                    break
                }
                x1 = Math.max(xx[0], xx[2])
                val x2 = Math.min(xx[1], xx[3])
                if (x2 - x1 <= firstIndent + rightIndent) {
                    continue
                }
                line = bidiLine!!.processLine(x1, x2 - x1 - firstIndent - rightIndent, alignment, localRunDirection, arabicOptions, minY, yLine, descender)
                if (!simulate && !dirty) {
                    if (line!!.isRTL && canvas!!.isTagged) {
                        canvas!!.beginMarkedContentSequence(PdfName.REVERSEDCHARS)
                        rtl = true
                    }
                    text!!.beginText()
                    dirty = true
                }
                if (line == null) {
                    status = NO_MORE_TEXT
                    yLine = yTemp
                    break
                }
            }
            if (isTagged(canvas) && elementToGo is ListItem) {
                if (!java.lang.Float.isNaN(firstLineY) && !firstLineYDone) {
                    if (!simulate) {
                        val lbl = elementToGo.listLabel
                        canvas!!.openMCBlock(lbl)
                        val symbol = Chunk(elementToGo.listSymbol)
                        symbol.setRole(null)
                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, Phrase(symbol), leftX + lbl.indentation, firstLineY, 0f)
                        canvas!!.closeMCBlock(lbl)
                    }
                    firstLineYDone = true
                }
            }
            if (!simulate) {
                if (lBody != null) {
                    canvas!!.openMCBlock(lBody)
                    lBody = null
                }
                currentValues[0] = currentFont
                text!!.setTextMatrix(x1 + if (line.isRTL) rightIndent else firstIndent + line.indentLeft(), yLine)
                lastX = pdf!!.writeLineToContent(line, text, graphics, currentValues, ratio)
                currentFont = currentValues[0] as PdfFont
            }
            lastWasNewline = repeatFirstLineIndent && line.isNewlineSplit
            yLine -= if (line.isNewlineSplit) extraParagraphSpace else 0
            ++linesWritten
            descender = line.descender
        }
        if (dirty) {
            text!!.endText()
            if (canvas !== text) {
                canvas!!.add(text)
            }
            if (rtl && canvas!!.isTagged) {
                canvas!!.endMarkedContentSequence()
            }
        }
        return status
    }

    /**
     * Clears the chunk array. A call to go() will always return
     * NO_MORE_TEXT.
     */
    fun clearChunks() {
        if (bidiLine != null) {
            bidiLine!!.clearChunks()
        }
    }

    @Throws(DocumentException::class)
    protected fun goComposite(simulate: Boolean): Int {
        var pdf: PdfDocument? = null
        if (canvas != null) {
            pdf = canvas!!.pdfDocument
        }
        if (!rectangularMode) {
            throw DocumentException(MessageLocalization.getComposedMessage("irregular.columns.are.not.supported.in.composite.mode"))
        }
        linesWritten = 0
        descender = 0f
        var firstPass = true
        val isRTL = runDirection == PdfWriter.RUN_DIRECTION_RTL
        while (true) {
            if (compositeElements!!.isEmpty()) {
                return NO_MORE_TEXT
            }
            var element: Element? = compositeElements!!.first
            if (element!!.type() == Element.PARAGRAPH) {
                val para = element as Paragraph?
                var status = 0
                for (keep in 0..1) {
                    val lastY = yLine
                    var createHere = false
                    if (compositeColumn == null) {
                        compositeColumn = ColumnText(canvas)
                        compositeColumn!!.alignment = para.getAlignment()
                        compositeColumn!!.setIndent(para.getIndentationLeft() + para.getFirstLineIndent(), false)
                        compositeColumn!!.extraParagraphSpace = para.getExtraParagraphSpace()
                        compositeColumn!!.followingIndent = para.getIndentationLeft()
                        compositeColumn!!.rightIndent = para.getIndentationRight()
                        compositeColumn!!.setLeading(para.getLeading(), para.getMultipliedLeading())
                        compositeColumn!!.runDirection = runDirection
                        compositeColumn!!.arabicOptions = arabicOptions
                        compositeColumn!!.spaceCharRatio = spaceCharRatio
                        compositeColumn!!.addText(para)
                        if (!(firstPass && isAdjustFirstLine)) {
                            yLine -= para.getSpacingBefore()
                        }
                        createHere = true
                    }
                    compositeColumn!!.isUseAscender = if ((firstPass || descender == 0f) && isAdjustFirstLine) isUseAscender else false
                    compositeColumn!!.inheritGraphicState = inheritGraphicState
                    compositeColumn!!.leftX = leftX
                    compositeColumn!!.rightX = rightX
                    compositeColumn!!.yLine = yLine
                    compositeColumn!!.rectangularWidth = rectangularWidth
                    compositeColumn!!.rectangularMode = rectangularMode
                    compositeColumn!!.minY = minY
                    compositeColumn!!.maxY = maxY
                    val keepCandidate = para.getKeepTogether() && createHere && !(firstPass && isAdjustFirstLine)
                    val s = simulate || keepCandidate && keep == 0
                    if (isTagged(canvas) && !s) {
                        canvas!!.openMCBlock(para)
                    }
                    status = compositeColumn!!.go(s)
                    if (isTagged(canvas) && !s) {
                        canvas!!.closeMCBlock(para)
                    }
                    lastX = compositeColumn!!.lastX
                    updateFilledWidth(compositeColumn!!.filledWidth)
                    if (status and NO_MORE_TEXT == 0 && keepCandidate) {
                        compositeColumn = null
                        yLine = lastY
                        return NO_MORE_COLUMN
                    }
                    if (simulate || !keepCandidate) {
                        break
                    }
                    if (keep == 0) {
                        compositeColumn = null
                        yLine = lastY
                    }
                }
                firstPass = false
                if (compositeColumn!!.linesWritten > 0) {
                    yLine = compositeColumn!!.yLine
                    linesWritten += compositeColumn!!.linesWritten
                    descender = compositeColumn!!.descender
                    isWordSplit = isWordSplit or compositeColumn!!.isWordSplit
                }
                currentLeading = compositeColumn!!.currentLeading
                if (status and NO_MORE_TEXT != 0) {
                    compositeColumn = null
                    compositeElements!!.removeFirst()
                    yLine -= para.getSpacingAfter()
                }
                if (status and NO_MORE_COLUMN != 0) {
                    return NO_MORE_COLUMN
                }
            } else if (element.type() == Element.LIST) {
                var list = element as com.itextpdf.text.List?
                var items = list.getItems()
                var item: ListItem? = null
                var listIndentation = list.getIndentationLeft()
                var count = 0
                val stack = Stack<Array<Any>>()
                var k = 0
                while (k < items.size) {
                    val obj = items[k]
                    if (obj is ListItem) {
                        if (count == listIdx) {
                            item = obj
                            break
                        } else {
                            ++count
                        }
                    } else if (obj is com.itextpdf.text.List) {
                        stack.push(arrayOf<Any>(list, Integer.valueOf(k), listIndentation))
                        list = obj
                        items = list.items
                        listIndentation += list.getIndentationLeft()
                        k = -1
                        ++k
                        continue
                    }
                    while (k == items.size - 1 && !stack.isEmpty()) {
                        val objs = stack.pop()
                        list = objs[0] as com.itextpdf.text.List
                        items = list.items
                        k = (objs[1] as Int).toInt()
                        listIndentation = (objs[2] as Float).toFloat()
                    }
                    ++k
                }
                var status = 0
                var keepTogetherAndDontFit = false
                for (keep in 0..1) {
                    val lastY = yLine
                    var createHere = false
                    if (compositeColumn == null) {
                        if (item == null) {
                            listIdx = 0
                            compositeElements!!.removeFirst()
                            break
                        }
                        compositeColumn = ColumnText(canvas)
                        compositeColumn!!.isUseAscender = if ((firstPass || descender == 0f) && isAdjustFirstLine) isUseAscender else false
                        compositeColumn!!.inheritGraphicState = inheritGraphicState
                        compositeColumn!!.alignment = item.alignment
                        compositeColumn!!.setIndent(item.getIndentationLeft() + listIndentation + item.firstLineIndent, false)
                        compositeColumn!!.extraParagraphSpace = item.extraParagraphSpace
                        compositeColumn!!.followingIndent = compositeColumn!!.indent
                        compositeColumn!!.rightIndent = item.getIndentationRight() + list.getIndentationRight()
                        compositeColumn!!.setLeading(item.leading, item.multipliedLeading)
                        compositeColumn!!.runDirection = runDirection
                        compositeColumn!!.arabicOptions = arabicOptions
                        compositeColumn!!.spaceCharRatio = spaceCharRatio
                        compositeColumn!!.addText(item)
                        if (!(firstPass && isAdjustFirstLine)) {
                            yLine -= item.getSpacingBefore()
                        }
                        createHere = true
                    }
                    compositeColumn!!.leftX = leftX
                    compositeColumn!!.rightX = rightX
                    compositeColumn!!.yLine = yLine
                    compositeColumn!!.rectangularWidth = rectangularWidth
                    compositeColumn!!.rectangularMode = rectangularMode
                    compositeColumn!!.minY = minY
                    compositeColumn!!.maxY = maxY
                    val keepCandidate = item!!.keepTogether && createHere && !(firstPass && isAdjustFirstLine)
                    val s = simulate || keepCandidate && keep == 0
                    if (isTagged(canvas) && !s) {
                        item.listLabel.indentation = listIndentation
                        if (list.getFirstItem() === item || compositeColumn != null && compositeColumn!!.bidiLine != null) {
                            canvas!!.openMCBlock(list)
                        }
                        canvas!!.openMCBlock(item)
                    }
                    status = compositeColumn!!.go(s, item)
                    if (isTagged(canvas) && !s) {
                        canvas!!.closeMCBlock(item.listBody)
                        canvas!!.closeMCBlock(item)
                    }
                    lastX = compositeColumn!!.lastX
                    updateFilledWidth(compositeColumn!!.filledWidth)
                    if (status and NO_MORE_TEXT == 0 && keepCandidate) {
                        keepTogetherAndDontFit = true
                        compositeColumn = null
                        yLine = lastY
                    }
                    if (simulate || !keepCandidate || keepTogetherAndDontFit) {
                        break
                    }
                    if (keep == 0) {
                        compositeColumn = null
                        yLine = lastY
                    }
                }

                if (isTagged(canvas) && !simulate) {
                    if (item == null || list.getLastItem() === item && status and NO_MORE_TEXT != 0 || status and NO_MORE_COLUMN != 0) {
                        canvas!!.closeMCBlock(list)
                    }
                }
                if (keepTogetherAndDontFit) {
                    return NO_MORE_COLUMN
                }
                if (item == null) {
                    continue
                }

                firstPass = false
                yLine = compositeColumn!!.yLine
                linesWritten += compositeColumn!!.linesWritten
                descender = compositeColumn!!.descender
                currentLeading = compositeColumn!!.currentLeading
                if (!isTagged(canvas)) {
                    if (!java.lang.Float.isNaN(compositeColumn!!.firstLineY) && !compositeColumn!!.firstLineYDone) {
                        if (!simulate) {
                            if (isRTL) {
                                showTextAligned(canvas, Element.ALIGN_RIGHT, Phrase(item.listSymbol), compositeColumn!!.lastX + item.getIndentationLeft(), compositeColumn!!.firstLineY, 0f, runDirection, arabicOptions)
                            } else {
                                showTextAligned(canvas, Element.ALIGN_LEFT, Phrase(item.listSymbol), compositeColumn!!.leftX + listIndentation, compositeColumn!!.firstLineY, 0f)
                            }

                        }
                        compositeColumn!!.firstLineYDone = true
                    }
                }
                if (status and NO_MORE_TEXT != 0) {
                    compositeColumn = null
                    ++listIdx
                    yLine -= item.getSpacingAfter()
                }
                if (status and NO_MORE_COLUMN != 0) {
                    return NO_MORE_COLUMN
                }
            } else if (element.type() == Element.PTABLE) {

                // INITIALISATIONS
                // get the PdfPTable element
                var table = element as PdfPTable?

                // tables without a body are dismissed
                if (table.size() <= table.headerRows) {
                    compositeElements!!.removeFirst()
                    continue
                }

                // Y-offset
                var yTemp = yLine
                yTemp += descender
                if (rowsDrawn == 0 && isAdjustFirstLine) {
                    yTemp -= table.spacingBefore()
                }

                // if there's no space left, ask for new column
                if (yTemp < minY || yTemp > maxY) {
                    return NO_MORE_COLUMN
                }

                // mark start of table
                val yLineWrite = yTemp
                var x1 = leftX
                currentLeading = 0f
                // get the width of the table
                val tableWidth: Float
                if (table.isLockedWidth) {
                    tableWidth = table.totalWidth
                    updateFilledWidth(tableWidth)
                } else {
                    tableWidth = rectangularWidth * table.widthPercentage / 100f
                    table.totalWidth = tableWidth
                }

                // HEADERS / FOOTERS
                // how many header rows are real header rows; how many are footer rows?
                table.normalizeHeadersFooters()
                val headerRows = table.headerRows
                var footerRows = table.footerRows
                val realHeaderRows = headerRows - footerRows
                val footerHeight = table.footerHeight
                val headerHeight = table.headerHeight - footerHeight

                // do we need to skip the header?
                val skipHeader = table.isSkipFirstHeader && rowsDrawn <= realHeaderRows && (table.isComplete() || rowsDrawn != realHeaderRows)

                if (!skipHeader) {
                    yTemp -= headerHeight
                }

                // MEASURE NECESSARY SPACE
                // how many real rows (not header or footer rows) fit on a page?
                var k = 0
                if (rowsDrawn < headerRows) {
                    rowsDrawn = headerRows
                }
                var fittingRows: FittingRows? = null
                //if we skip the last header, firstly, we want to check if table is wholly fit to the page
                if (table.isSkipLastFooter) {
                    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
                    fittingRows = table.getFittingRows(yTemp - minY, rowsDrawn)
                }
                //if we skip the last footer, but the table doesn't fit to the page - we reserve space for footer
                //and recalculate fitting rows
                if (!table.isSkipLastFooter || fittingRows!!.lastRow < table.size() - 1) {
                    yTemp -= footerHeight
                    fittingRows = table.getFittingRows(yTemp - minY, rowsDrawn)
                }

                //we want to be able to add more than just a header and a footer
                if (yTemp < minY || yTemp > maxY) {
                    return NO_MORE_COLUMN
                }

                // k will be the first row that doesn't fit
                k = fittingRows!!.lastRow + 1
                yTemp -= fittingRows.height
                // splitting row spans

                LOGGER.info("Want to split at row " + k)
                var kTemp = k
                while (kTemp > rowsDrawn && kTemp < table.size() && table.getRow(kTemp).isMayNotBreak) {
                    kTemp--
                }

                if (kTemp < table.size() - 1 && !table.getRow(kTemp).isMayNotBreak) {
                    kTemp++
                }

                if (kTemp > rowsDrawn && kTemp < k || kTemp == headerRows && table.getRow(headerRows).isMayNotBreak && table.isLoopCheck) {
                    yTemp = minY
                    k = kTemp
                    table.isLoopCheck = false
                }
                LOGGER.info("Will split at row " + k)

                // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
                if (table.isSplitLate && k > 0) {
                    fittingRows.correctLastRowChosen(table, k - 1)
                }
                // splitting row spans

                // only for incomplete tables:
                if (!table.isComplete()) {
                    yTemp += footerHeight
                }

                // IF ROWS MAY NOT BE SPLIT
                if (!table.isSplitRows) {
                    splittedRow = -1
                    if (k == rowsDrawn) {
                        // drop the whole table
                        if (k == table.size()) {
                            compositeElements!!.removeFirst()
                            continue
                        } // or drop the row
                        else {
                            // don't drop the row if the table is incomplete and if there's only one row (not counting the header rows)
                            // if there's only one row and this check wasn't here the row would have been deleted and not added at all
                            if (!(!table.isComplete() && k == 1)) {
                                table.rows.removeAt(k)
                            }
                            return NO_MORE_COLUMN
                        }
                    }
                } // IF ROWS SHOULD NOT BE SPLIT
                else if (table.isSplitLate && (rowsDrawn < k || splittedRow == -2 && (table.headerRows == 0 || table.isSkipFirstHeader))) {
                    splittedRow = -1
                } // SPLIT ROWS (IF WANTED AND NECESSARY)
                else if (k < table.size()) {
                    // we calculate the remaining vertical space
                    // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
                    // correct yTemp to only take completed rows into account
                    yTemp -= fittingRows.completedRowsHeight - fittingRows.height
                    // splitting row spans

                    val h = yTemp - minY
                    // we create a new row with the remaining content
                    val newRow = table.getRow(k).splitRow(table, k, h)
                    // if the row isn't null add it as an extra row
                    if (newRow == null) {
                        LOGGER.info("Didn't split row!")
                        splittedRow = -1
                        if (rowsDrawn == k) {
                            return NO_MORE_COLUMN
                        }
                    } else {
                        // if the row hasn't been split before, we duplicate (part of) the table
                        if (k != splittedRow) {
                            splittedRow = k + 1
                            table = PdfPTable(table)
                            compositeElements!![0] = table
                            val rows = table.rows
                            for (i in headerRows..rowsDrawn - 1) {
                                rows[i] = null
                            }
                        }
                        yTemp = minY
                        table.rows.add(++k, newRow)
                        LOGGER.info("Inserting row at position " + k)
                    }
                }// Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
                //else if (table.isSplitLate() && !table.hasRowspan(k) && rowIdx < k) {
                //if first row do not fit, splittedRow has value of -2, so in this case we try to avoid split.
                // Separate constant for the first attempt of splitting first row save us from infinite loop.
                // Also we check header rows, because in other case we may split right after header row,
                // while header row can't split before regular rows.

                // We're no longer in the first pass
                firstPass = false

                // if not in simulation mode, draw the table
                if (!simulate) {
                    // set the alignment
                    when (table.horizontalAlignment) {
                        Element.ALIGN_RIGHT -> if (!isRTL) {
                            x1 += rectangularWidth - tableWidth
                        }
                        Element.ALIGN_CENTER -> x1 += (rectangularWidth - tableWidth) / 2f
                        Element.ALIGN_LEFT,
                        else -> if (isRTL) {
                            x1 += rectangularWidth - tableWidth
                        }
                    }
                    // copy the rows that fit on the page in a new table nt
                    val nt = PdfPTable.shallowCopy(table)
                    val sub = nt.rows
                    // first we add the real header rows (if necessary)
                    if (!skipHeader && realHeaderRows > 0) {
                        val rows = table.getRows(0, realHeaderRows)
                        if (isTagged(canvas)) {
                            nt.header.rows = rows
                        }
                        sub.addAll(rows)
                    } else {
                        nt.headerRows = footerRows
                    }
                    // then we add the real content

                    run {
                        val rows = table.getRows(rowsDrawn, k)
                        if (isTagged(canvas)) {
                            nt.body.rows = rows
                        }
                        sub.addAll(rows)
                    }
                    // do we need to show a footer?
                    var showFooter = !table.isSkipLastFooter
                    var newPageFollows = false
                    if (k < table.size()) {
                        nt.isComplete = true
                        showFooter = true
                        newPageFollows = true
                    }
                    // we add the footer rows if necessary (not for incomplete tables)
                    if (footerRows > 0 && nt.isComplete && showFooter) {
                        val rows = table.getRows(realHeaderRows, realHeaderRows + footerRows)
                        if (isTagged(canvas)) {
                            nt.footer.rows = rows
                        }
                        sub.addAll(rows)
                    } else {
                        footerRows = 0
                    }

                    if (sub.size > 0) {
                        // we need a correction if the last row needs to be extended
                        var rowHeight = 0f
                        val lastIdx = sub.size - 1 - footerRows
                        val last = sub[lastIdx]
                        if (table.isExtendLastRow(newPageFollows)) {
                            rowHeight = last.maxHeights
                            last.maxHeights = yTemp - minY + rowHeight
                            yTemp = minY
                        }


                        // newPageFollows indicates that this table is being split
                        if (newPageFollows) {
                            val tableEvent = table.tableEvent
                            if (tableEvent is PdfPTableEventSplit) {
                                tableEvent.splitTable(table)
                            }
                        }

                        // now we render the rows of the new table
                        if (canvases != null) {
                            if (isTagged(canvases!![PdfPTable.TEXTCANVAS])) {
                                canvases!![PdfPTable.TEXTCANVAS].openMCBlock(table)
                            }
                            nt.writeSelectedRows(0, -1, 0, -1, x1, yLineWrite, canvases, false)
                            if (isTagged(canvases!![PdfPTable.TEXTCANVAS])) {
                                canvases!![PdfPTable.TEXTCANVAS].closeMCBlock(table)
                            }
                        } else {
                            if (isTagged(canvas)) {
                                canvas!!.openMCBlock(table)
                            }
                            nt.writeSelectedRows(0, -1, 0, -1, x1, yLineWrite, canvas, false)
                            if (isTagged(canvas)) {
                                canvas!!.closeMCBlock(table)
                            }
                        }

                        if (!table.isComplete()) {
                            table.addNumberOfRowsWritten(k)
                        }

                        // if the row was split, we copy the content of the last row
                        // that was consumed into the first row shown on the next page
                        if (splittedRow == k && k < table.size()) {
                            val splitted = table.rows[k]
                            splitted.copyRowContent(nt, lastIdx)
                        } // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz), splitting row spans
                        else if (k > 0 && k < table.size()) {
                            // continue rowspans on next page
                            // (as the row was not split there is no content to copy)
                            val row = table.getRow(k)
                            row.splitRowspans(table, k - 1, nt, lastIdx)
                        }
                        // splitting row spans

                        // reset the row height of the last row
                        if (table.isExtendLastRow(newPageFollows)) {
                            last.maxHeights = rowHeight
                        }

                        // Contributed by Deutsche Bahn Systel GmbH (Thorsten Seitz)
                        // newPageFollows indicates that this table is being split
                        if (newPageFollows) {
                            val tableEvent = table.tableEvent
                            if (tableEvent is PdfPTableEventAfterSplit) {
                                val row = table.getRow(k)
                                tableEvent.afterSplitTable(table, row, k)
                            }
                        }
                    }
                } // in simulation mode, we need to take extendLastRow into account
                else if (table.isExtendLastRow && minY > PdfPRow.BOTTOM_LIMIT) {
                    yTemp = minY
                }

                yLine = yTemp
                descender = 0f
                currentLeading = 0f
                if (!(skipHeader || table.isComplete())) {
                    yLine += footerHeight
                }
                while (k < table.size()) {
                    if (table.getRowHeight(k) > 0 || table.hasRowspan(k)) {
                        break
                    }
                    k++
                }
                if (k >= table.size()) {
                    // Use up space no more than left
                    if (yLine - table.spacingAfter() < minY) {
                        yLine = minY
                    } else {
                        yLine -= table.spacingAfter()
                    }
                    compositeElements!!.removeFirst()
                    splittedRow = -1
                    rowsDrawn = 0
                } else {
                    if (splittedRow > -1) {
                        val rows = table.rows
                        for (i in rowsDrawn..k - 1) {
                            rows[i] = null
                        }
                    }
                    rowsDrawn = k
                    return NO_MORE_COLUMN
                }
            } else if (element.type() == Element.YMARK) {
                if (!simulate) {
                    val zh = element as DrawInterface?
                    zh.draw(canvas, leftX, minY, rightX, maxY, yLine)
                }
                compositeElements!!.removeFirst()
            } else if (element.type() == Element.DIV) {
                val floatingElements = ArrayList<Element>()
                do {
                    floatingElements.add(element)
                    compositeElements!!.removeFirst()
                    element = if (!compositeElements!!.isEmpty()) compositeElements!!.first else null
                } while (element != null && element.type() == Element.DIV)

                val fl = FloatLayout(floatingElements, isUseAscender)
                fl.setSimpleColumn(leftX, minY, rightX, yLine)
                fl.compositeColumn.isIgnoreSpacingBefore = isIgnoreSpacingBefore
                val status = fl.layout(canvas, simulate)

                //firstPass = false;
                yLine = fl.yLine
                descender = 0f
                if (status and NO_MORE_TEXT == 0) {
                    compositeElements!!.addAll(floatingElements)
                    return status
                }
            } else {
                compositeElements!!.removeFirst()
            }
        }
    }

    /**
     * Checks if the element has a height of 0.

     * @return true or false
     * *
     * @since 2.1.2
     */
    fun zeroHeightElement(): Boolean {
        return composite && !compositeElements!!.isEmpty() && compositeElements!!.first.type() == Element.YMARK
    }

    fun getCompositeElements(): List<Element> {
        return compositeElements
    }

    /**
     * Replaces the filledWidth if greater than the existing one.

     * @param w the new filledWidth if greater than the existing
     * * one
     */
    fun updateFilledWidth(w: Float) {
        if (w > filledWidth) {
            filledWidth = w
        }
    }

    companion object {

        /**
         * Eliminate the arabic vowels
         */
        val AR_NOVOWEL = ArabicLigaturizer.ar_novowel
        /**
         * Compose the tashkeel in the ligatures.
         */
        val AR_COMPOSEDTASHKEEL = ArabicLigaturizer.ar_composedtashkeel
        /**
         * Do some extra double ligatures.
         */
        val AR_LIG = ArabicLigaturizer.ar_lig
        /**
         * Digit shaping option: Replace European digits (U+0030...U+0039) by
         * Arabic-Indic digits.
         */
        val DIGITS_EN2AN = ArabicLigaturizer.DIGITS_EN2AN

        /**
         * Digit shaping option: Replace Arabic-Indic digits by European digits
         * (U+0030...U+0039).
         */
        val DIGITS_AN2EN = ArabicLigaturizer.DIGITS_AN2EN

        /**
         * Digit shaping option: Replace European digits (U+0030...U+0039) by
         * Arabic-Indic digits if the most recent strongly directional character is
         * an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC). The
         * initial state at the start of the text is assumed to be not an Arabic,
         * letter, so European digits at the start of the text will not change.
         * Compare to DIGITS_ALEN2AN_INIT_AL.
         */
        val DIGITS_EN2AN_INIT_LR = ArabicLigaturizer.DIGITS_EN2AN_INIT_LR

        /**
         * Digit shaping option: Replace European digits (U+0030...U+0039) by
         * Arabic-Indic digits if the most recent strongly directional character is
         * an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC). The
         * initial state at the start of the text is assumed to be an Arabic,
         * letter, so European digits at the start of the text will change. Compare
         * to DIGITS_ALEN2AN_INT_LR.
         */
        val DIGITS_EN2AN_INIT_AL = ArabicLigaturizer.DIGITS_EN2AN_INIT_AL

        /**
         * Digit type option: Use Arabic-Indic digits (U+0660...U+0669).
         */
        val DIGIT_TYPE_AN = ArabicLigaturizer.DIGIT_TYPE_AN

        /**
         * Digit type option: Use Eastern (Extended) Arabic-Indic digits
         * (U+06f0...U+06f9).
         */
        val DIGIT_TYPE_AN_EXTENDED = ArabicLigaturizer.DIGIT_TYPE_AN_EXTENDED

        /**
         * the space char ratio
         */
        val GLOBAL_SPACE_CHAR_RATIO = 0f

        /**
         * Initial value of the status.
         */
        val START_COLUMN = 0

        /**
         * Signals that there is no more text available.
         */
        val NO_MORE_TEXT = 1

        /**
         * Signals that there is no more column.
         */
        val NO_MORE_COLUMN = 2

        /**
         * The column is valid.
         */
        protected val LINE_STATUS_OK = 0

        /**
         * The line is out the column limits.
         */
        protected val LINE_STATUS_OFFLIMITS = 1

        /**
         * The line cannot fit this column position.
         */
        protected val LINE_STATUS_NOLINE = 2

        /**
         * Creates an independent duplicated of the instance org.

         * @param org the original ColumnText
         * *
         * @return the duplicated
         */
        fun duplicate(org: ColumnText): ColumnText {
            val ct = ColumnText(null)
            ct.setACopy(org)
            return ct
        }

        fun isAllowedElement(element: Element): Boolean {
            val type = element.type()
            if (type == Element.CHUNK || type == Element.PHRASE || type == Element.DIV
                    || type == Element.PARAGRAPH || type == Element.LIST
                    || type == Element.YMARK || type == Element.PTABLE) {
                return true
            }
            if (element is Image) {
                return true
            }
            return false
        }

        /**
         * Gets the width that the line will occupy after writing. Only the width of
         * the first line is returned.

         * @param phrase the Phrase containing the line
         * *
         * @param runDirection the run direction
         * *
         * @param arabicOptions the options for the arabic shaping
         * *
         * @return the width of the line
         */
        @JvmOverloads fun getWidth(phrase: Phrase, runDirection: Int = PdfWriter.RUN_DIRECTION_NO_BIDI, arabicOptions: Int = 0): Float {
            val ct = ColumnText(null)
            ct.addText(phrase)
            ct.addWaitingPhrase()
            val line = ct.bidiLine!!.processLine(0f, 20000f, Element.ALIGN_LEFT, runDirection, arabicOptions, 0f, 0f, 0f)
            if (line == null) {
                return 0f
            } else {
                return 20000 - line.widthLeft()
            }
        }

        /**
         * Shows a line of text. Only the first line is written.

         * @param canvas where the text is to be written to
         * *
         * @param alignment the alignment. It is not influenced by the run direction
         * *
         * @param phrase the Phrase with the text
         * *
         * @param x the x reference position
         * *
         * @param y the y reference position
         * *
         * @param rotation the rotation to be applied in degrees counterclockwise
         * *
         * @param runDirection the run direction
         * *
         * @param arabicOptions the options for the arabic shaping
         */
        @JvmOverloads fun showTextAligned(canvas: PdfContentByte, alignment: Int, phrase: Phrase, x: Float, y: Float, rotation: Float, runDirection: Int = PdfWriter.RUN_DIRECTION_NO_BIDI, arabicOptions: Int = 0) {
            var alignment = alignment
            if (alignment != Element.ALIGN_LEFT && alignment != Element.ALIGN_CENTER
                    && alignment != Element.ALIGN_RIGHT) {
                alignment = Element.ALIGN_LEFT
            }
            canvas.saveState()
            val ct = ColumnText(canvas)
            var lly = -1f
            var ury = 2f
            var llx: Float
            var urx: Float
            when (alignment) {
                Element.ALIGN_LEFT -> {
                    llx = 0f
                    urx = 20000f
                }
                Element.ALIGN_RIGHT -> {
                    llx = -20000f
                    urx = 0f
                }
                else -> {
                    llx = -20000f
                    urx = 20000f
                }
            }
            if (rotation == 0f) {
                llx += x
                lly += y
                urx += x
                ury += y
            } else {
                val alpha = rotation * Math.PI / 180.0
                val cos = Math.cos(alpha).toFloat()
                val sin = Math.sin(alpha).toFloat()
                canvas.concatCTM(cos, sin, -sin, cos, x, y)
            }
            ct.setSimpleColumn(phrase, llx, lly, urx, ury, 2f, alignment)
            if (runDirection == PdfWriter.RUN_DIRECTION_RTL) {
                if (alignment == Element.ALIGN_LEFT) {
                    alignment = Element.ALIGN_RIGHT
                } else if (alignment == Element.ALIGN_RIGHT) {
                    alignment = Element.ALIGN_LEFT
                }
            }
            ct.alignment = alignment
            ct.arabicOptions = arabicOptions
            ct.runDirection = runDirection
            try {
                ct.go()
            } catch (e: DocumentException) {
                throw ExceptionConverter(e)
            }

            canvas.restoreState()
        }

        /**
         * Fits the text to some rectangle adjusting the font size as needed.

         * @param font the font to use
         * *
         * @param text the text
         * *
         * @param rect the rectangle where the text must fit
         * *
         * @param maxFontSize the maximum font size
         * *
         * @param runDirection the run direction
         * *
         * @return the calculated font size that makes the text fit
         */
        fun fitText(font: Font, text: String, rect: Rectangle, maxFontSize: Float, runDirection: Int): Float {
            var maxFontSize = maxFontSize
            try {
                var ct: ColumnText? = null
                var status = 0
                if (maxFontSize <= 0) {
                    var cr = 0
                    var lf = 0
                    val t = text.toCharArray()
                    for (k in t.indices) {
                        if (t[k] == '\n') {
                            ++lf
                        } else if (t[k] == '\r') {
                            ++cr
                        }
                    }
                    val minLines = Math.max(cr, lf) + 1
                    maxFontSize = Math.abs(rect.height) / minLines - 0.001f
                }
                font.size = maxFontSize
                val ph = Phrase(text, font)
                ct = ColumnText(null)
                ct.setSimpleColumn(ph, rect.left, rect.bottom, rect.right, rect.top, maxFontSize, Element.ALIGN_LEFT)
                ct.runDirection = runDirection
                status = ct.go(true)
                if (status and NO_MORE_TEXT != 0) {
                    return maxFontSize
                }
                val precision = 0.1f
                var min = 0f
                var max = maxFontSize
                var size = maxFontSize
                for (k in 0..49) {
                    //just in case it doesn't converge
                    size = (min + max) / 2
                    ct = ColumnText(null)
                    font.size = size
                    ct.setSimpleColumn(Phrase(text, font), rect.left, rect.bottom, rect.right, rect.top, size, Element.ALIGN_LEFT)
                    ct.runDirection = runDirection
                    status = ct.go(true)
                    if (status and NO_MORE_TEXT != 0) {
                        if (max - min < size * precision) {
                            return size
                        }
                        min = size
                    } else {
                        max = size
                    }
                }
                return size
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

        /**
         * Checks the status variable and looks if there's still some text.
         */
        fun hasMoreText(status: Int): Boolean {
            return status and ColumnText.NO_MORE_TEXT == 0
        }

        private fun isTagged(canvas: PdfContentByte?): Boolean {
            return canvas != null && canvas.pdfDocument != null && canvas.pdfWriter != null && canvas.pdfWriter!!.isTagged
        }
    }

}
/**
 * Outputs the lines to the document. It is equivalent to
 * go(false).

 * @return returns the result of the operation. It can be
 * * NO_MORE_TEXT and/or NO_MORE_COLUMN
 * *
 * @throws DocumentException on error
 */
/**
 * Outputs the lines to the document. The output can be simulated.

 * @param simulate true to simulate the writing to the document
 * *
 * @return returns the result of the operation. It can be
 * * NO_MORE_TEXT and/or NO_MORE_COLUMN
 * *
 * @throws DocumentException on error
 */
/**
 * Gets the width that the line will occupy after writing. Only the width of
 * the first line is returned.

 * @param phrase the Phrase containing the line
 * *
 * @return the width of the line
 */
/**
 * Shows a line of text. Only the first line is written.

 * @param canvas where the text is to be written to
 * *
 * @param alignment the alignment
 * *
 * @param phrase the Phrase with the text
 * *
 * @param x the x reference position
 * *
 * @param y the y reference position
 * *
 * @param rotation the rotation to be applied in degrees counterclockwise
 */
