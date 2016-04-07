/*
 * $Id: 2108832fc1b7a854be83c9c26e40969ecbd9ee12 $
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

import java.util.ArrayList

/**
 * PdfLine defines an array with PdfChunk-objects
 * that fit into 1 line.
 */

class PdfLine {

    // membervariables

    /** The arraylist containing the chunks.  */
    protected var line: ArrayList<PdfChunk>

    /** The left indentation of the line.  */
    protected var left: Float = 0.toFloat()

    /** The width of the line.  */
    protected var width: Float = 0.toFloat()

    /** The alignment of the line.  */
    protected var alignment: Int = 0

    /** The height of the line.  */
    protected var height: Float = 0.toFloat()

    /** The listsymbol (if necessary).  */
    //    protected Chunk listSymbol = null;

    /** The listsymbol (if necessary).  */
    //    protected float symbolIndent;

    /** true if the chunk splitting was caused by a newline.  */
    protected var newlineSplit = false

    /** The original width.  */
    /**
     * Gets the original width of the line.
     * @return the original width of the line
     */
    var originalWidth: Float = 0.toFloat()
        protected set

    internal var isRTL = false
        protected set

    protected var listItem: ListItem? = null

    protected var tabStop: TabStop? = null

    protected var tabStopAnchorPosition = java.lang.Float.NaN

    protected var tabPosition = java.lang.Float.NaN

    // constructors

    /**
     * Constructs a new PdfLine-object.

     * @param    left        the limit of the line at the left
     * *
     * @param    right        the limit of the line at the right
     * *
     * @param    alignment    the alignment of the line
     * *
     * @param    height        the height of the line
     */

    internal constructor(left: Float, right: Float, alignment: Int, height: Float) {
        this.left = left
        this.width = right - left
        this.originalWidth = this.width
        this.alignment = alignment
        this.height = height
        this.line = ArrayList<PdfChunk>()
    }

    /**
     * Creates a PdfLine object.
     * @param left                the left offset
     * *
     * @param originalWidth        the original width of the line
     * *
     * @param remainingWidth    bigger than 0 if the line isn't completely filled
     * *
     * @param alignment            the alignment of the line
     * *
     * @param newlineSplit        was the line splitted (or does the paragraph end with this line)
     * *
     * @param line                an array of PdfChunk objects
     * *
     * @param isRTL                do you have to read the line from Right to Left?
     */
    internal constructor(left: Float, originalWidth: Float, remainingWidth: Float, alignment: Int, newlineSplit: Boolean, line: ArrayList<PdfChunk>, isRTL: Boolean) {
        this.left = left
        this.originalWidth = originalWidth
        this.width = remainingWidth
        this.alignment = alignment
        this.line = line
        this.newlineSplit = newlineSplit
        this.isRTL = isRTL
    }

    // methods

    /**
     * Adds a PdfChunk to the PdfLine.

     * @param        chunk                the PdfChunk to add
     * *
     * @param        currentLeading        new value for the height of the line
     * *
     * @return        null if the chunk could be added completely; if not
     * *				a PdfChunk containing the part of the chunk that could
     * *				not be added is returned
     */

    internal fun add(chunk: PdfChunk?, currentLeading: Float): PdfChunk {
        //we set line height to correspond to the current leading
        if (chunk != null && chunk.toString() != "") {
            //whitespace shouldn't change leading
            if (chunk.toString() != " ") {
                if (this.height < currentLeading || this.line.isEmpty())
                    this.height = currentLeading
            }
        }
        return add(chunk)
    }

    /**
     * Adds a PdfChunk to the PdfLine.

     * @param        chunk        the PdfChunk to add
     * *
     * @return        null if the chunk could be added completely; if not
     * *				a PdfChunk containing the part of the chunk that could
     * *				not be added is returned
     */

    internal fun add(chunk: PdfChunk?): PdfChunk? {
        var chunk = chunk
        // nothing happens if the chunk is null.
        if (chunk == null || chunk.toString() == "") {
            return null
        }

        // we split the chunk to be added
        var overflow = chunk.split(width)
        newlineSplit = chunk.isNewlineSplit || overflow == null
        if (chunk.isTab) {
            val tab = chunk.getAttribute(Chunk.TAB) as Array<Any>
            if (chunk.isAttribute(Chunk.TABSETTINGS)) {
                val isWhiteSpace = tab[1] as Boolean
                if (!isWhiteSpace || !line.isEmpty()) {
                    flush()
                    tabStopAnchorPosition = java.lang.Float.NaN
                    tabStop = PdfChunk.getTabStop(chunk, originalWidth - width)
                    if (tabStop!!.position > originalWidth) {
                        if (isWhiteSpace)
                            overflow = null
                        else if (Math.abs(originalWidth - width) < 0.001) {
                            addToLine(chunk)
                            overflow = null
                        } else {
                            overflow = chunk
                        }
                        width = 0f
                    } else {
                        chunk.tabStop = tabStop
                        if (!isRTL && tabStop!!.alignment == TabStop.Alignment.LEFT) {
                            width = originalWidth - tabStop!!.position
                            tabStop = null
                            tabPosition = java.lang.Float.NaN
                        } else
                            tabPosition = originalWidth - width
                        addToLine(chunk)
                    }
                } else
                    return null
            } else {
                //Keep deprecated tab logic for backward compatibility...
                val tabStopPosition = (tab[1] as Float).toFloat()
                val newline = (tab[2] as Boolean).booleanValue()
                if (newline && tabStopPosition < originalWidth - width) {
                    return chunk
                }
                chunk.adjustLeft(left)
                width = originalWidth - tabStopPosition
                addToLine(chunk)
            }
        } else if (chunk.length() > 0 || chunk.isImage) {
            if (overflow != null)
                chunk.trimLastSpace()
            width -= chunk.width()
            addToLine(chunk)
        } else if (line.size < 1) {
            chunk = overflow
            overflow = chunk!!.truncate(width)
            width -= chunk.width()
            if (chunk.length() > 0) {
                addToLine(chunk)
                return overflow
            } else {
                if (overflow != null)
                    addToLine(overflow)
                return null
            }// if the chunk couldn't even be truncated, we add everything, so be it
        } else {
            width += line[line.size - 1].trimLastSpace()
        }// if the length == 0 and there were no other chunks added to the line yet,
        // we risk to end up in an endless loop trying endlessly to add the same chunk
        // if the length of the chunk > 0 we add it to the line
        return overflow
    }

    private fun addToLine(chunk: PdfChunk) {
        if (chunk.changeLeading) {
            val f: Float
            if (chunk.isImage) {
                val img = chunk.image
                f = chunk.imageHeight + chunk.imageOffsetY
                +img.borderWidthTop + img.getSpacingBefore()
            } else {
                f = chunk.leading
            }
            if (f > height) height = f
        }
        if (tabStop != null && tabStop!!.alignment == TabStop.Alignment.ANCHOR && java.lang.Float.isNaN(tabStopAnchorPosition)) {
            val value = chunk.toString()
            val anchorIndex = value.indexOf(tabStop!!.anchorChar.toInt())
            if (anchorIndex != -1) {
                val subWidth = chunk.width(value.substring(anchorIndex, value.length))
                tabStopAnchorPosition = originalWidth - width - subWidth
            }
        }
        line.add(chunk)
    }

    // methods to retrieve information

    /**
     * Returns the number of chunks in the line.

     * @return    a value
     */

    fun size(): Int {
        return line.size
    }

    /**
     * Returns an iterator of PdfChunks.

     * @return    an Iterator
     */

    operator fun iterator(): Iterator<PdfChunk> {
        return line.iterator()
    }

    /**
     * Returns the height of the line.

     * @return    a value
     */

    internal fun height(): Float {
        return height
    }

    /**
     * Returns the left indentation of the line taking the alignment of the line into account.

     * @return    a value
     */

    internal fun indentLeft(): Float {
        if (isRTL) {
            when (alignment) {
                Element.ALIGN_CENTER -> return left + width / 2f
                Element.ALIGN_RIGHT -> return left
                Element.ALIGN_JUSTIFIED -> return left + if (hasToBeJustified()) 0 else width
                Element.ALIGN_LEFT,
                else -> return left + width
            }
        } else if (this.separatorCount <= 0) {
            when (alignment) {
                Element.ALIGN_RIGHT -> return left + width
                Element.ALIGN_CENTER -> return left + width / 2f
            }
        }
        return left
    }

    /**
     * Checks if this line has to be justified.

     * @return    true if the alignment equals ALIGN_JUSTIFIED and there is some width left.
     */

    fun hasToBeJustified(): Boolean {
        return (alignment == Element.ALIGN_JUSTIFIED && !newlineSplit || alignment == Element.ALIGN_JUSTIFIED_ALL) && width != 0f
    }

    /**
     * Resets the alignment of this line.
     *
     * The alignment of the last line of for instance a Paragraph
     * that has to be justified, has to be reset to ALIGN_LEFT.
     */

    fun resetAlignment() {
        if (alignment == Element.ALIGN_JUSTIFIED) {
            alignment = Element.ALIGN_LEFT
        }
    }

    /** Adds extra indentation to the left (for Paragraph.setFirstLineIndent).  */
    internal fun setExtraIndent(extra: Float) {
        left += extra
        width -= extra
        originalWidth -= extra
    }

    /**
     * Returns the width that is left, after a maximum of characters is added to the line.

     * @return    a value
     */

    internal fun widthLeft(): Float {
        return width
    }

    /**
     * Returns the number of space-characters in this line.

     * @return    a value
     */

    internal fun numberOfSpaces(): Int {
        var numberOfSpaces = 0
        for (pdfChunk in line) {
            val tmp = pdfChunk.toString()
            val length = tmp.length
            for (i in 0..length - 1) {
                if (tmp[i] == ' ') {
                    numberOfSpaces++
                }
            }
        }
        return numberOfSpaces
    }

    /**
     * Sets the listsymbol of this line.
     *
     * This is only necessary for the first line of a ListItem.

     * @param listItem the list symbol
     */

    fun setListItem(listItem: ListItem) {
        this.listItem = listItem
        //        this.listSymbol = listItem.getListSymbol();
        //        this.symbolIndent = listItem.getIndentationLeft();
    }

    /**
     * Returns the listsymbol of this line.

     * @return    a PdfChunk if the line has a listsymbol; null otherwise
     */

    fun listSymbol(): Chunk? {
        return if (listItem != null) listItem!!.listSymbol else null
    }

    /**
     * Return the indentation needed to show the listsymbol.

     * @return    a value
     */

    fun listIndent(): Float {
        return if (listItem != null) listItem!!.getIndentationLeft() else 0
    }

    fun listItem(): ListItem {
        return listItem
    }

    /**
     * Get the string representation of what is in this line.

     * @return    a String
     */

    override fun toString(): String {
        val tmp = StringBuffer()
        for (pdfChunk in line) {
            tmp.append(pdfChunk.toString())
        }
        return tmp.toString()
    }

    /**
     * Returns the length of a line in UTF32 characters
     * @return    the length in UTF32 characters
     * *
     * @since    2.1.2; Get changed into get in 5.0.2
     */
    val lineLengthUtf32: Int
        get() {
            var total = 0
            for (element in line) {
                total += element.lengthUtf32()
            }
            return total
        }

    /**
     * Checks if a newline caused the line split.
     * @return true if a newline caused the line split
     */
    val isNewlineSplit: Boolean
        get() = newlineSplit && alignment != Element.ALIGN_JUSTIFIED_ALL

    /**
     * Gets the index of the last PdfChunk with metric attributes
     * @return the last PdfChunk with metric attributes
     */
    val lastStrokeChunk: Int
        get() {
            var lastIdx = line.size - 1
            while (lastIdx >= 0) {
                val chunk = line[lastIdx]
                if (chunk.isStroked)
                    break
                --lastIdx
            }
            return lastIdx
        }

    /**
     * Gets a PdfChunk by index.
     * @param idx the index
     * *
     * @return the PdfChunk or null if beyond the array
     */
    fun getChunk(idx: Int): PdfChunk? {
        if (idx < 0 || idx >= line.size)
            return null
        return line[idx]
    }

    /*
     * Gets the maximum size of all the fonts used in this line
     * including images.
     * @return maximum size of all the fonts used in this line
     float getMaxSizeSimple() {
        float maxSize = 0;
        PdfChunk chunk;
        for (int k = 0; k < line.size(); ++k) {
            chunk = (PdfChunk)line.get(k);
            if (!chunk.isImage()) {
                maxSize = Math.max(chunk.font().size(), maxSize);
            }
            else {
                maxSize = Math.max(chunk.getImage().getScaledHeight() + chunk.getImageOffsetY() , maxSize);
            }
        }
        return maxSize;
    }*/

    /**
     * Gets the difference between the "normal" leading and the maximum
     * size (for instance when there are images in the chunk and the leading
     * has to be taken into account).
     * @return    an extra leading for images
     * *
     * @since    2.1.5
     */
    internal fun getMaxSize(fixedLeading: Float, multipliedLeading: Float): FloatArray {
        var normal_leading = 0f
        var image_leading = -10000f
        var chunk: PdfChunk
        for (k in line.indices) {
            chunk = line[k]
            if (chunk.isImage) {
                val img = chunk.image
                if (chunk.changeLeading()) {
                    val height = chunk.imageHeight + chunk.imageOffsetY + img.getSpacingBefore()
                    image_leading = Math.max(height, image_leading)
                }
            } else {
                if (chunk.changeLeading())
                    normal_leading = Math.max(chunk.leading, normal_leading)
                else
                    normal_leading = Math.max(fixedLeading + multipliedLeading * chunk.font().size(), normal_leading)
            }
        }
        return floatArrayOf(if (normal_leading > 0) normal_leading else fixedLeading, image_leading)
    }

    /**
     * Gets the number of separators in the line.
     * Returns -1 if there's a tab in the line.
     * @return    the number of separators in the line
     * *
     * @since    2.1.2
     */
    internal //It seems justification was forbidden in the deprecated tab logic!!!
    val separatorCount: Int
        get() {
            var s = 0
            var ck: PdfChunk
            for (element in line) {
                ck = element
                if (ck.isTab) {
                    if (ck.isAttribute(Chunk.TABSETTINGS))
                        continue
                    return -1
                }
                if (ck.isHorizontalSeparator) {
                    s++
                }
            }
            return s
        }

    /**
     * Gets a width corrected with a charSpacing and wordSpacing.
     * @param charSpacing
     * *
     * @param wordSpacing
     * *
     * @return a corrected width
     */
    fun getWidthCorrected(charSpacing: Float, wordSpacing: Float): Float {
        var total = 0f
        for (k in line.indices) {
            val ck = line[k]
            total += ck.getWidthCorrected(charSpacing, wordSpacing)
        }
        return total
    }

    /**
     * Gets the maximum size of the ascender for all the fonts used
     * in this line.
     * @return maximum size of all the ascenders used in this line
     */
    val ascender: Float
        get() {
            var ascender = 0f
            for (k in line.indices) {
                val ck = line[k]
                if (ck.isImage)
                    ascender = Math.max(ascender, ck.imageHeight + ck.imageOffsetY)
                else {
                    val font = ck.font()
                    val textRise = ck.textRise
                    ascender = Math.max(ascender, if (textRise > 0) textRise else 0 + font.font.getFontDescriptor(BaseFont.ASCENT, font.size()))
                }
            }
            return ascender
        }

    /**
     * Gets the biggest descender for all the fonts used
     * in this line.  Note that this is a negative number.
     * @return maximum size of all the descenders used in this line
     */
    val descender: Float
        get() {
            var descender = 0f
            for (k in line.indices) {
                val ck = line[k]
                if (ck.isImage)
                    descender = Math.min(descender, ck.imageOffsetY)
                else {
                    val font = ck.font()
                    val textRise = ck.textRise
                    descender = Math.min(descender, if (textRise < 0) textRise else 0 + font.font.getFontDescriptor(BaseFont.DESCENT, font.size()))
                }
            }
            return descender
        }

    fun flush() {
        if (tabStop != null) {
            val textWidth = originalWidth - width - tabPosition
            var tabStopPosition = tabStop!!.getPosition(tabPosition, originalWidth - width, tabStopAnchorPosition)
            width = originalWidth - tabStopPosition - textWidth
            if (width < 0)
                tabStopPosition += width
            if (!isRTL)
                tabStop!!.position = tabStopPosition
            else
                tabStop!!.position = originalWidth - width - tabPosition
            tabStop = null
            tabPosition = java.lang.Float.NaN
        }
    }
}
