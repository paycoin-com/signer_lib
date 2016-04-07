/*
 * $Id: fee2ea52b0c97e488c125f0205768790902070bd $
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
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.HashMap
import java.util.HashSet

/**
 * A PdfChunk is the PDF translation of a Chunk.
 *
 * A PdfChunk is a PdfString in a certain
 * PdfFont and BaseColor.

 * @see PdfString

 * @see com.itextpdf.text.Chunk

 * @see com.itextpdf.text.Font
 */

class PdfChunk {

    // membervariables

    /** The value of this object.  */
    protected var value = PdfObject.NOTHING

    /** The encoding.  */
    /**
     * Gets the encoding of this string.

     * @return        a String
     */

    internal var encoding = BaseFont.WINANSI
        protected set


    /** The font for this PdfChunk.  */
    protected var font: PdfFont

    protected var baseFont: BaseFont? = null

    protected var splitCharacter: SplitCharacter? = null
    /**
     * Metric attributes.
     *
     * This attributes require the measurement of characters widths when rendering
     * such as underline.
     */
    protected var attributes = HashMap<String, Any>()

    /**
     * Non metric attributes.
     *
     * This attributes do not require the measurement of characters widths when rendering
     * such as BaseColor.
     */
    protected var noStroke = HashMap<String, Any>()

    /** true if the chunk split was cause by a newline.  */
    /**
     * Checks if the PdfChunk split was caused by a newline.
     * @return true if the PdfChunk split was caused by a newline.
     */

    var isNewlineSplit: Boolean = false
        protected set

    /** The image in this PdfChunk, if it has one  */
    /**
     * Gets the image in the PdfChunk.
     * @return the image or null
     */

    internal var image: Image? = null
        protected set
    /**
     * Returns a scalePercentage in case the image needs to be scaled.
     * @return the imageScalePercentage
     */
    /**
     * Sets a scale percentage in case the image needs to be scaled.
     * @param imageScalePercentage the imageScalePercentage to set
     */
    var imageScalePercentage = 1.0f

    /** The offset in the x direction for the image  */
    /**
     * Gets the image offset in the x direction
     * @return the image offset in the x direction
     */

    /**
     * Sets the image offset in the x direction
     * @param  offsetX the image offset in the x direction
     */

    internal var imageOffsetX: Float = 0.toFloat()

    /** The offset in the y direction for the image  */
    /**
     * Gets the image offset in the y direction
     * @return Gets the image offset in the y direction
     */

    /**
     * Sets the image offset in the y direction
     * @param  offsetY the image offset in the y direction
     */

    internal var imageOffsetY: Float = 0.toFloat()

    /** Indicates if the height and offset of the Image has to be taken into account  */
    protected var changeLeading = false

    /** The leading that can overrule the existing leading.  */
    var leading = 0f
        protected set

    protected var accessibleElement: IAccessibleElement? = null
    // constructors

    /**
     * Constructs a PdfChunk-object.

     * @param string the content of the PdfChunk-object
     * *
     * @param other Chunk with the same style you want for the new Chunk
     */

    internal constructor(string: String, other: PdfChunk) {
        value = string
        this.font = other.font
        this.attributes = other.attributes
        this.noStroke = other.noStroke
        this.baseFont = other.baseFont
        this.changeLeading = other.changeLeading
        this.leading = other.leading
        val obj = attributes[Chunk.IMAGE] as Array<Any>
        if (obj == null)
            image = null
        else {
            image = obj[0] as Image
            imageOffsetX = (obj[1] as Float).toFloat()
            imageOffsetY = (obj[2] as Float).toFloat()
            changeLeading = (obj[3] as Boolean).booleanValue()
        }
        encoding = font.font.encoding
        splitCharacter = noStroke[Chunk.SPLITCHARACTER] as SplitCharacter
        if (splitCharacter == null)
            splitCharacter = DefaultSplitCharacter.DEFAULT
        accessibleElement = other.accessibleElement
    }

    /**
     * Constructs a PdfChunk-object.

     * @param chunk the original Chunk-object
     * *
     * @param action the PdfAction if the Chunk comes from an Anchor
     */

    internal constructor(chunk: Chunk, action: PdfAction?) {
        value = chunk.content

        val f = chunk.font
        var size = f.size
        if (size == Font.UNDEFINED.toFloat())
            size = 12f
        baseFont = f.baseFont
        var style = f.style
        if (style == Font.UNDEFINED) {
            style = Font.NORMAL
        }
        if (baseFont == null) {
            // translation of the font-family to a PDF font-family
            baseFont = f.getCalculatedBaseFont(false)
        } else {
            // bold simulation
            if (style and Font.BOLD != 0)
                attributes.put(Chunk.TEXTRENDERMODE, arrayOf<Any>(Integer.valueOf(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE), size / 30f, null))
            // italic simulation
            if (style and Font.ITALIC != 0)
                attributes.put(Chunk.SKEW, floatArrayOf(0f, ITALIC_ANGLE))
        }
        font = PdfFont(baseFont, size)
        // other style possibilities
        val attr = chunk.attributes
        if (attr != null) {
            for (entry in attr.entries) {
                val name = entry.key
                if (keysAttributes.contains(name)) {
                    attributes.put(name, entry.value)
                } else if (keysNoStroke.contains(name)) {
                    noStroke.put(name, entry.value)
                }
            }
            if ("" == attr[Chunk.GENERICTAG]) {
                attributes.put(Chunk.GENERICTAG, chunk.content)
            }
        }
        if (f.isUnderlined) {
            val obj = arrayOf<Any>(null, floatArrayOf(0f, UNDERLINE_THICKNESS, 0f, UNDERLINE_OFFSET, 0f))
            val unders = Utilities.addToArray(attributes[Chunk.UNDERLINE] as Array<Array<Any>>, obj)
            attributes.put(Chunk.UNDERLINE, unders)
        }
        if (f.isStrikethru) {
            val obj = arrayOf<Any>(null, floatArrayOf(0f, 1f / 15, 0f, 1f / 3, 0f))
            val unders = Utilities.addToArray(attributes[Chunk.UNDERLINE] as Array<Array<Any>>, obj)
            attributes.put(Chunk.UNDERLINE, unders)
        }
        if (action != null)
            attributes.put(Chunk.ACTION, action)
        // the color can't be stored in a PdfFont
        noStroke.put(Chunk.COLOR, f.color)
        noStroke.put(Chunk.ENCODING, font.font.encoding)

        val lh = attributes[Chunk.LINEHEIGHT] as Float
        if (lh != null) {
            changeLeading = true
            leading = lh
        }

        val obj = attributes[Chunk.IMAGE] as Array<Any>
        if (obj == null) {
            image = null
        } else {
            attributes.remove(Chunk.HSCALE) // images are scaled in other ways
            image = obj[0] as Image
            imageOffsetX = (obj[1] as Float).toFloat()
            imageOffsetY = (obj[2] as Float).toFloat()
            changeLeading = (obj[3] as Boolean).booleanValue()
        }
        val hs = attributes[Chunk.HSCALE] as Float
        if (hs != null)
            font.horizontalScaling = hs.toFloat()
        encoding = font.font.encoding
        splitCharacter = noStroke[Chunk.SPLITCHARACTER] as SplitCharacter
        if (splitCharacter == null)
            splitCharacter = DefaultSplitCharacter.DEFAULT
        accessibleElement = chunk
    }

    /**
     * Constructs a PdfChunk-object.

     * @param chunk     the original Chunk-object
     * *
     * @param action    the PdfAction if the Chunk comes from an Anchor
     * *
     * @param tabSettings  the Phrase tab settings
     */
    internal constructor(chunk: Chunk, action: PdfAction, tabSettings: TabSettings?) : this(chunk, action) {
        if (tabSettings != null && attributes[Chunk.TABSETTINGS] == null)
            attributes.put(Chunk.TABSETTINGS, tabSettings)
    }

    // methods

    /** Gets the Unicode equivalent to a CID.
     * The (inexistent) CID  is translated as '\n'.
     * It has only meaning with CJK fonts with Identity encoding.
     * @param c the CID code
     * *
     * @return the Unicode equivalent
     */
    fun getUnicodeEquivalent(c: Int): Int {
        return baseFont!!.getUnicodeEquivalent(c)
    }

    protected fun getWord(text: String, start: Int): Int {
        var start = start
        val len = text.length
        while (start < len) {
            if (!Character.isLetter(text[start]))
                break
            ++start
        }
        return start
    }

    /**
     * Splits this PdfChunk if it's too long for the given width.
     *
     * Returns null if the PdfChunk wasn't truncated.

     * @param        width        a given width
     * *
     * @return        the PdfChunk that doesn't fit into the width.
     */

    internal fun split(width: Float): PdfChunk? {
        isNewlineSplit = false
        if (image != null) {
            if (image!!.scaledWidth > width) {
                val pc = PdfChunk(Chunk.OBJECT_REPLACEMENT_CHARACTER, this)
                value = ""
                attributes = HashMap<String, Any>()
                image = null
                font = PdfFont.defaultFont
                return pc
            } else
                return null
        }
        val hyphenationEvent = noStroke[Chunk.HYPHENATION] as HyphenationEvent
        var currentPosition = 0
        var splitPosition = -1
        var currentWidth = 0f

        // loop over all the characters of a string
        // or until the totalWidth is reached
        var lastSpace = -1
        var lastSpaceWidth = 0f
        val length = value.length
        val valueArray = value.toCharArray()
        var character: Char = 0.toChar()
        val ft = font.font
        var surrogate = false
        if (ft.fontType == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            while (currentPosition < length) {
                // the width of every character is added to the currentWidth
                val cidChar = valueArray[currentPosition]
                character = ft.getUnicodeEquivalent(cidChar.toInt()).toChar()
                // if a newLine or carriageReturn is encountered
                if (character == '\n') {
                    isNewlineSplit = true
                    val returnValue = value.substring(currentPosition + 1)
                    value = value.substring(0, currentPosition)
                    if (value.length < 1) {
                        value = "\u0001"
                    }
                    val pc = PdfChunk(returnValue, this)
                    return pc
                }
                currentWidth += getCharWidth(cidChar.toInt())
                if (character == ' ') {
                    lastSpace = currentPosition + 1
                    lastSpaceWidth = currentWidth
                }
                if (currentWidth > width)
                    break
                // if a split-character is encountered, the splitPosition is altered
                if (splitCharacter!!.isSplitCharacter(0, currentPosition, length, valueArray, arrayOf(this)))
                    splitPosition = currentPosition + 1
                currentPosition++
            }
        } else {
            while (currentPosition < length) {
                // the width of every character is added to the currentWidth
                character = valueArray[currentPosition]
                // if a newLine or carriageReturn is encountered
                if (character == '\r' || character == '\n') {
                    isNewlineSplit = true
                    var inc = 1
                    if (character == '\r' && currentPosition + 1 < length && valueArray[currentPosition + 1] == '\n')
                        inc = 2
                    val returnValue = value.substring(currentPosition + inc)
                    value = value.substring(0, currentPosition)
                    if (value.length < 1) {
                        value = " "
                    }
                    val pc = PdfChunk(returnValue, this)
                    return pc
                }
                surrogate = Utilities.isSurrogatePair(valueArray, currentPosition)
                if (surrogate)
                    currentWidth += getCharWidth(Utilities.convertToUtf32(valueArray[currentPosition], valueArray[currentPosition + 1]))
                else
                    currentWidth += getCharWidth(character.toInt())
                if (character == ' ') {
                    lastSpace = currentPosition + 1
                    lastSpaceWidth = currentWidth
                }
                if (surrogate)
                    currentPosition++
                if (currentWidth > width)
                    break
                // if a split-character is encountered, the splitPosition is altered
                if (splitCharacter!!.isSplitCharacter(0, currentPosition, length, valueArray, null))
                    splitPosition = currentPosition + 1
                currentPosition++
            }
        }

        // if all the characters fit in the total width, null is returned (there is no overflow)
        if (currentPosition == length) {
            return null
        }
        // otherwise, the string has to be truncated
        if (splitPosition < 0) {
            val returnValue = value
            value = ""
            val pc = PdfChunk(returnValue, this)
            return pc
        }
        if (lastSpace > splitPosition && splitCharacter!!.isSplitCharacter(0, 0, 1, singleSpace, null))
            splitPosition = lastSpace
        if (hyphenationEvent != null && lastSpace >= 0 && lastSpace < currentPosition) {
            val wordIdx = getWord(value, lastSpace)
            if (wordIdx > lastSpace) {
                val pre = hyphenationEvent.getHyphenatedWordPre(value.substring(lastSpace, wordIdx), font.font, font.size(), width - lastSpaceWidth)
                val post = hyphenationEvent.hyphenatedWordPost
                if (pre.length > 0) {
                    val returnValue = post + value.substring(wordIdx)
                    value = trim(value.substring(0, lastSpace) + pre)
                    val pc = PdfChunk(returnValue, this)
                    return pc
                }
            }
        }
        val returnValue = value.substring(splitPosition)
        value = trim(value.substring(0, splitPosition))
        val pc = PdfChunk(returnValue, this)
        return pc
    }

    /**
     * Truncates this PdfChunk if it's too long for the given width.
     *
     * Returns null if the PdfChunk wasn't truncated.

     * @param        width        a given width
     * *
     * @return        the PdfChunk that doesn't fit into the width.
     */
    internal fun truncate(width: Float): PdfChunk? {
        if (image != null) {
            if (image!!.scaledWidth > width) {
                // Image does not fit the line, resize if requested
                if (image!!.isScaleToFitLineWhenOverflow) {
                    //float scalePercent = width / image.getWidth() * 100;
                    //image.scalePercent(scalePercent);
                    this.imageScalePercentage = width / image!!.width
                    return null
                }
                val pc = PdfChunk("", this)
                value = ""
                attributes.remove(Chunk.IMAGE)
                image = null
                font = PdfFont.defaultFont
                return pc
            } else
                return null
        }

        var currentPosition = 0
        var currentWidth = 0f

        // it's no use trying to split if there isn't even enough place for a space
        if (width < font.width()) {
            val returnValue = value.substring(1)
            value = value.substring(0, 1)
            val pc = PdfChunk(returnValue, this)
            return pc
        }

        // loop over all the characters of a string
        // or until the totalWidth is reached
        val length = value.length
        var surrogate = false
        while (currentPosition < length) {
            // the width of every character is added to the currentWidth
            surrogate = Utilities.isSurrogatePair(value, currentPosition)
            if (surrogate)
                currentWidth += getCharWidth(Utilities.convertToUtf32(value, currentPosition))
            else
                currentWidth += getCharWidth(value[currentPosition].toInt())
            if (currentWidth > width)
                break
            if (surrogate)
                currentPosition++
            currentPosition++
        }

        // if all the characters fit in the total width, null is returned (there is no overflow)
        if (currentPosition == length) {
            return null
        }

        // otherwise, the string has to be truncated
        //currentPosition -= 2;
        // we have to chop off minimum 1 character from the chunk
        if (currentPosition == 0) {
            currentPosition = 1
            if (surrogate)
                ++currentPosition
        }
        val returnValue = value.substring(currentPosition)
        value = value.substring(0, currentPosition)
        val pc = PdfChunk(returnValue, this)
        return pc
    }

    // methods to retrieve the membervariables

    /**
     * Returns the font of this Chunk.

     * @return    a PdfFont
     */

    internal fun font(): PdfFont {
        return font
    }

    /**
     * Returns the color of this Chunk.

     * @return    a BaseColor
     */

    internal fun color(): BaseColor {
        return noStroke[Chunk.COLOR] as BaseColor
    }

    @JvmOverloads internal fun width(str: String = value): Float {
        if (isAttribute(Chunk.SEPARATOR)) {
            return 0f
        }
        if (isImage) {
            return imageWidth
        }

        var width = font.width(str)

        if (isAttribute(Chunk.CHAR_SPACING)) {
            val cs = getAttribute(Chunk.CHAR_SPACING) as Float
            width += str.length * cs.toFloat()
        }
        if (isAttribute(Chunk.WORD_SPACING)) {
            var numberOfSpaces = 0
            var idx = -1
            while ((idx = str.indexOf(' ', idx + 1)) >= 0)
                ++numberOfSpaces
            val ws = getAttribute(Chunk.WORD_SPACING) as Float
            width += numberOfSpaces * ws
        }
        return width
    }

    internal fun height(): Float {
        if (isImage) {
            return imageHeight
        } else {
            return font.size()
        }
    }

    /**
     * Gets the width of the PdfChunk taking into account the
     * extra character and word spacing.
     * @param charSpacing the extra character spacing
     * *
     * @param wordSpacing the extra word spacing
     * *
     * @return the calculated width
     */

    fun getWidthCorrected(charSpacing: Float, wordSpacing: Float): Float {
        if (image != null) {
            return image!!.scaledWidth + charSpacing
        }
        var numberOfSpaces = 0
        var idx = -1
        while ((idx = value.indexOf(' ', idx + 1)) >= 0)
            ++numberOfSpaces
        return font.width(value) + value.length * charSpacing + numberOfSpaces * wordSpacing
    }

    /**
     * Gets the text displacement relative to the baseline.
     * @return a displacement in points
     */
    val textRise: Float
        get() {
            val f = getAttribute(Chunk.SUBSUPSCRIPT) as Float
            if (f != null) {
                return f.toFloat()
            }
            return 0.0f
        }

    /**
     * Trims the last space.
     * @return the width of the space trimmed, otherwise 0
     */

    fun trimLastSpace(): Float {
        val ft = font.font
        if (ft.fontType == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length > 1 && value.endsWith("\u0001")) {
                value = value.substring(0, value.length - 1)
                return font.width('\u0001')
            }
        } else {
            if (value.length > 1 && value.endsWith(" ")) {
                value = value.substring(0, value.length - 1)
                return font.width(' ')
            }
        }
        return 0f
    }

    fun trimFirstSpace(): Float {
        val ft = font.font
        if (ft.fontType == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length > 1 && value.startsWith("\u0001")) {
                value = value.substring(1)
                return font.width('\u0001')
            }
        } else {
            if (value.length > 1 && value.startsWith(" ")) {
                value = value.substring(1)
                return font.width(' ')
            }
        }
        return 0f
    }

    /**
     * Gets an attribute. The search is made in attributes
     * and noStroke.
     * @param name the attribute key
     * *
     * @return the attribute value or null if not found
     */

    internal fun getAttribute(name: String): Any {
        if (attributes.containsKey(name))
            return attributes[name]
        return noStroke[name]
    }

    /**
     * Checks if the attribute exists.
     * @param name the attribute key
     * *
     * @return true if the attribute exists
     */

    internal fun isAttribute(name: String): Boolean {
        if (attributes.containsKey(name))
            return true
        return noStroke.containsKey(name)
    }

    /**
     * Checks if this PdfChunk needs some special metrics handling.
     * @return true if this PdfChunk needs some special metrics handling.
     */

    internal val isStroked: Boolean
        get() = !attributes.isEmpty()

    /**
     * Checks if this PdfChunk is a Separator Chunk.
     * @return    true if this chunk is a separator.
     * *
     * @since    2.1.2
     */
    internal val isSeparator: Boolean
        get() = isAttribute(Chunk.SEPARATOR)

    /**
     * Checks if this PdfChunk is a horizontal Separator Chunk.
     * @return    true if this chunk is a horizontal separator.
     * *
     * @since    2.1.2
     */
    internal val isHorizontalSeparator: Boolean
        get() {
            if (isAttribute(Chunk.SEPARATOR)) {
                val o = getAttribute(Chunk.SEPARATOR) as Array<Any>
                return !(o[1] as Boolean).booleanValue()
            }
            return false
        }

    /**
     * Checks if this PdfChunk is a tab Chunk.
     * @return    true if this chunk is a separator.
     * *
     * @since    2.1.2
     */
    internal val isTab: Boolean
        get() = isAttribute(Chunk.TAB)

    /**
     * Correction for the tab position based on the left starting position.
     * @param    newValue    the new value for the left X.
     * *
     * @since    2.1.2
     */
    @Deprecated("")
    internal fun adjustLeft(newValue: Float) {
        val o = attributes[Chunk.TAB] as Array<Any>
        if (o != null) {
            attributes.put(Chunk.TAB, arrayOf(o[0], o[1], o[2], newValue))
        }
    }

    internal var tabStop: TabStop
        get() = attributes[TABSTOP] as TabStop
        set(tabStop) {
            attributes.put(TABSTOP, tabStop)
        }

    /**
     * Checks if there is an image in the PdfChunk.
     * @return true if an image is present
     */

    internal val isImage: Boolean
        get() = image != null

    internal val imageHeight: Float
        get() = image!!.scaledHeight * imageScalePercentage

    internal val imageWidth: Float
        get() = image!!.scaledWidth * imageScalePercentage

    /**
     * sets the value.
     * @param value content of the Chunk
     */

    internal fun setValue(value: String) {
        this.value = value
    }

    /**
     * @see java.lang.Object.toString
     */
    override fun toString(): String {
        return value
    }

    /**
     * Tells you if this string is in Chinese, Japanese, Korean or Identity-H.
     * @return true if the Chunk has a special encoding
     */

    internal val isSpecialEncoding: Boolean
        get() = encoding == CJKFont.CJK_ENCODING || encoding == BaseFont.IDENTITY_H

    internal fun length(): Int {
        return value.length
    }

    internal fun lengthUtf32(): Int {
        if (BaseFont.IDENTITY_H != encoding)
            return value.length
        var total = 0
        val len = value.length
        var k = 0
        while (k < len) {
            if (Utilities.isSurrogateHigh(value[k]))
                ++k
            ++total
            ++k
        }
        return total
    }

    internal fun isExtSplitCharacter(start: Int, current: Int, end: Int, cc: CharArray, ck: Array<PdfChunk>): Boolean {
        return splitCharacter!!.isSplitCharacter(start, current, end, cc, ck)
    }

    /**
     * Removes all the ' ' and '-'-characters on the right of a String.
     *
     * @param    string        the String that has to be trimmed.
     * *
     * @return    the trimmed String
     */
    internal fun trim(string: String): String {
        var string = string
        val ft = font.font
        if (ft.fontType == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            while (string.endsWith("\u0001")) {
                string = string.substring(0, string.length - 1)
            }
        } else {
            while (string.endsWith(" ") || string.endsWith("\t")) {
                string = string.substring(0, string.length - 1)
            }
        }
        return string
    }

    fun changeLeading(): Boolean {
        return changeLeading
    }

    internal fun getCharWidth(c: Int): Float {
        if (noPrint(c))
            return 0f
        if (isAttribute(Chunk.CHAR_SPACING)) {
            val cs = getAttribute(Chunk.CHAR_SPACING) as Float
            return font.width(c) + cs.toFloat() * font.horizontalScaling
        }
        if (isImage) {
            return imageWidth
        }
        return font.width(c)
    }

    companion object {

        private val singleSpace = charArrayOf(' ')
        private val ITALIC_ANGLE = 0.21256f
        /** The allowed attributes in variable attributes.  */
        private val keysAttributes = HashSet<String>()

        /** The allowed attributes in variable noStroke.  */
        private val keysNoStroke = HashSet<String>()
        private val TABSTOP = "TABSTOP"

        init {
            keysAttributes.add(Chunk.ACTION)
            keysAttributes.add(Chunk.UNDERLINE)
            keysAttributes.add(Chunk.REMOTEGOTO)
            keysAttributes.add(Chunk.LOCALGOTO)
            keysAttributes.add(Chunk.LOCALDESTINATION)
            keysAttributes.add(Chunk.GENERICTAG)
            keysAttributes.add(Chunk.NEWPAGE)
            keysAttributes.add(Chunk.IMAGE)
            keysAttributes.add(Chunk.BACKGROUND)
            keysAttributes.add(Chunk.PDFANNOTATION)
            keysAttributes.add(Chunk.SKEW)
            keysAttributes.add(Chunk.HSCALE)
            keysAttributes.add(Chunk.SEPARATOR)
            keysAttributes.add(Chunk.TAB)
            keysAttributes.add(Chunk.TABSETTINGS)
            keysAttributes.add(Chunk.CHAR_SPACING)
            keysAttributes.add(Chunk.WORD_SPACING)
            keysAttributes.add(Chunk.LINEHEIGHT)
            keysNoStroke.add(Chunk.SUBSUPSCRIPT)
            keysNoStroke.add(Chunk.SPLITCHARACTER)
            keysNoStroke.add(Chunk.HYPHENATION)
            keysNoStroke.add(Chunk.TEXTRENDERMODE)
        }

        val UNDERLINE_THICKNESS = 1f / 15
        val UNDERLINE_OFFSET = -1f / 3

        internal fun getTabStop(tab: PdfChunk, tabPosition: Float): TabStop {
            var tabStop: TabStop? = null
            val o = tab.attributes[Chunk.TAB] as Array<Any>
            if (o != null) {
                val tabInterval = o[0] as Float
                if (java.lang.Float.isNaN(tabInterval)) {
                    tabStop = TabSettings.getTabStopNewInstance(tabPosition, tab.attributes[Chunk.TABSETTINGS] as TabSettings)
                } else {
                    tabStop = TabStop.newInstance(tabPosition, tabInterval)
                }
            }
            return tabStop
        }

        fun noPrint(c: Int): Boolean {
            return c >= 0x200b && c <= 0x200f || c >= 0x202a && c <= 0x202e
        }
    }


}
/**
 * Returns the width of this PdfChunk.

 * @return    a width
 */
