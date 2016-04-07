/*
 * $Id: 4600a6d4c061f83a6860d846b2bd13ba916d2e3c $
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

import java.util.ArrayList

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Element
import com.itextpdf.text.Phrase
import com.itextpdf.text.error_messages.MessageLocalization

/** Writes text vertically. Note that the naming is done according
 * to horizontal text although it refers to vertical text.
 * A line with the alignment Element.LEFT_ALIGN will actually
 * be top aligned.
 */
class VerticalText
/** Creates new VerticalText
 * @param text the place where the text will be written to. Can
 * * be a template.
 */
(
        /** The PdfContent where the text will be written to.  */
        protected var text: PdfContentByte?) {

    /** The chunks that form the text.  */
    protected var chunks = ArrayList<PdfChunk>()

    /** The column alignment. Default is left alignment.  */
    /**
     * Gets the alignment.
     * @return the alignment
     */
    /**
     * Sets the alignment.
     * @param alignment the alignment
     */
    var alignment = Element.ALIGN_LEFT

    /** Marks the chunks to be eliminated when the line is written.  */
    protected var currentChunkMarker = -1

    /** The chunk created by the splitting.  */
    protected var currentStandbyChunk: PdfChunk? = null

    /** The chunk created by the splitting.  */
    protected var splittedChunkText: String? = null

    /** The leading
     */
    /** Gets the separation between the vertical lines.
     * @return the vertical line separation
     */
    /** Sets the separation between the vertical lines.
     * @param leading the vertical line separation
     */
    var leading: Float = 0.toFloat()

    /** The X coordinate.
     */
    /** Gets the X coordinate where the next line will be written. This value will change
     * after each call to `go()`.
     * @return  the X coordinate
     */
    var originX: Float = 0.toFloat()
        protected set

    /** The Y coordinate.
     */
    /** Gets the Y coordinate where the next line will be written.
     * @return  the Y coordinate
     */
    var originY: Float = 0.toFloat()
        protected set

    /** The maximum number of vertical lines.
     */
    /** Gets the maximum number of available lines. This value will change
     * after each call to `go()`.
     * @return Value of property maxLines.
     */
    /** Sets the maximum number of lines.
     * @param maxLines the maximum number of lines
     */
    var maxLines: Int = 0

    /** The height of the text.
     */
    /** Gets the height of the line
     * @return the height
     */
    /** Sets the height of the line
     * @param height the new height
     */
    var height: Float = 0.toFloat()

    /**
     * Adds a Phrase to the current text array.
     * @param phrase the text
     */
    fun addText(phrase: Phrase) {
        for (c in phrase.chunks) {
            chunks.add(PdfChunk(c, null))
        }
    }

    /**
     * Adds a Chunk to the current text array.
     * @param chunk the text
     */
    fun addText(chunk: Chunk) {
        chunks.add(PdfChunk(chunk, null))
    }

    /** Sets the layout.
     * @param startX the top right X line position
     * *
     * @param startY the top right Y line position
     * *
     * @param height the height of the lines
     * *
     * @param maxLines the maximum number of lines
     * *
     * @param leading the separation between the lines
     */
    fun setVerticalLayout(startX: Float, startY: Float, height: Float, maxLines: Int, leading: Float) {
        this.originX = startX
        this.originY = startY
        this.height = height
        this.maxLines = maxLines
        leading = leading
    }

    /**
     * Creates a line from the chunk array.
     * @param width the width of the line
     * *
     * @return the line or null if no more chunks
     */
    protected fun createLine(width: Float): PdfLine? {
        if (chunks.isEmpty())
            return null
        splittedChunkText = null
        currentStandbyChunk = null
        val line = PdfLine(0f, width, alignment, 0f)
        var total: String
        currentChunkMarker = 0
        while (currentChunkMarker < chunks.size) {
            val original = chunks[currentChunkMarker]
            total = original.toString()
            currentStandbyChunk = line.add(original)
            if (currentStandbyChunk != null) {
                splittedChunkText = original.toString()
                original.setValue(total)
                return line
            }
            ++currentChunkMarker
        }
        return line
    }

    /**
     * Normalizes the list of chunks when the line is accepted.
     */
    protected fun shortenChunkArray() {
        if (currentChunkMarker < 0)
            return
        if (currentChunkMarker >= chunks.size) {
            chunks.clear()
            return
        }
        val split = chunks[currentChunkMarker]
        split.setValue(splittedChunkText)
        chunks.set(currentChunkMarker, currentStandbyChunk)
        for (j in currentChunkMarker - 1 downTo 0)
            chunks.removeAt(j)
    }

    /**
     * Outputs the lines to the document. The output can be simulated.
     * @param simulate true to simulate the writing to the document
     * *
     * @return returns the result of the operation. It can be NO_MORE_TEXT
     * * and/or NO_MORE_COLUMN
     */
    @JvmOverloads fun go(simulate: Boolean = false): Int {
        var dirty = false
        var graphics: PdfContentByte? = null
        if (text != null) {
            graphics = text!!.duplicate
        } else if (!simulate)
            throw NullPointerException(MessageLocalization.getComposedMessage("verticaltext.go.with.simulate.eq.eq.false.and.text.eq.eq.null"))
        var status = 0
        while (true) {
            if (maxLines <= 0) {
                status = NO_MORE_COLUMN
                if (chunks.isEmpty())
                    status = status or NO_MORE_TEXT
                break
            }
            if (chunks.isEmpty()) {
                status = NO_MORE_TEXT
                break
            }
            val line = createLine(height)
            if (!simulate && !dirty) {
                text!!.beginText()
                dirty = true
            }
            shortenChunkArray()
            if (!simulate) {
                text!!.setTextMatrix(originX, originY - line.indentLeft())
                writeLine(line, text, graphics)
            }
            --maxLines
            originX -= leading
        }
        if (dirty) {
            text!!.endText()
            text!!.add(graphics)
        }
        return status
    }

    private var curCharSpace: Float? = 0f

    internal fun writeLine(line: PdfLine, text: PdfContentByte, graphics: PdfContentByte) {
        var currentFont: PdfFont? = null
        var chunk: PdfChunk
        val j = line.iterator()
        while (j.hasNext()) {
            chunk = j.next()

            if (!chunk.isImage && chunk.font().compareTo(currentFont) != 0) {
                currentFont = chunk.font()
                text.setFontAndSize(currentFont!!.font, currentFont.size())
            }
            val textRender = chunk.getAttribute(Chunk.TEXTRENDERMODE) as Array<Any>
            var tr = 0
            var strokeWidth = 1f
            val color = chunk.color()
            var strokeColor: BaseColor? = null
            if (textRender != null) {
                tr = (textRender[0] as Int).toInt() and 3
                if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                    text.setTextRenderingMode(tr)
                if (tr == PdfContentByte.TEXT_RENDER_MODE_STROKE || tr == PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE) {
                    strokeWidth = (textRender[1] as Float).toFloat()
                    if (strokeWidth != 1f)
                        text.setLineWidth(strokeWidth)
                    strokeColor = textRender[2] as BaseColor
                    if (strokeColor == null)
                        strokeColor = color
                    if (strokeColor != null)
                        text.setColorStroke(strokeColor)
                }
            }

            val charSpace = chunk.getAttribute(Chunk.CHAR_SPACING) as Float
            // no char space setting means "leave it as is".
            if (charSpace != null && curCharSpace != charSpace) {
                curCharSpace = charSpace.toFloat()
                text.characterSpacing = curCharSpace
            }
            if (color != null)
                text.setColorFill(color)

            text.showText(chunk.toString())

            if (color != null)
                text.resetRGBColorFill()
            if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                text.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL)
            if (strokeColor != null)
                text.resetRGBColorStroke()
            if (strokeWidth != 1f)
                text.setLineWidth(1f)
        }
    }

    /** Sets the new text origin.
     * @param startX the X coordinate
     * *
     * @param startY the Y coordinate
     */
    fun setOrigin(startX: Float, startY: Float) {
        this.originX = startX
        this.originY = startY
    }

    companion object {

        /** Signals that there are no more text available.  */
        val NO_MORE_TEXT = 1

        /** Signals that there is no more column.  */
        val NO_MORE_COLUMN = 2
    }
}
/**
 * Outputs the lines to the document. It is equivalent to go(false).
 * @return returns the result of the operation. It can be NO_MORE_TEXT
 * * and/or NO_MORE_COLUMN
 */
