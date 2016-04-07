/*
 * $Id: 5caf3ceb3bf6216a03fa5e9094e3bb16483c369e $
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
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.DrawInterface
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.net.URL
import java.util.ArrayList
import java.util.HashMap

/**
 * This is the smallest significant part of text that can be added to a
 * document.
 *
 * Most elements can be divided in one or more Chunks. A chunk
 * is a String with a certain Font. All other
 * layout parameters should be defined in the object to which this chunk of text
 * is added.
 *
 * Example:

 *

 * Chunk chunk = new Chunk("Hello world",
 * FontFactory.getFont(FontFactory.COURIER, 20, Font.ITALIC, new BaseColor(255, 0,
 * 0)));  document.add(chunk);

 *

 *
 */

class Chunk : Element, IAccessibleElement {

    // member variables

    /** This is the content of this chunk of text.  */
    protected var content: StringBuffer? = null

    /** This is the Font of this chunk of text.  */
    // methods to retrieve information

    /**
     * Gets the font of this Chunk.

     * @return a Font
     */
    /**
     * Sets the font of this Chunk.

     * @param font
     * *            a Font
     */
    var font: Font? = null

    /** Contains some of the attributes for this Chunk.  */
    /**
     * Gets the attributes for this Chunk.
     *
     * It may be null.

     * @return the attributes for this Chunk
     */

    /**
     * Sets the attributes all at once.
     * @param    attributes    the attributes of a Chunk
     */
    var attributes: HashMap<String, Any>? = null

    protected var role: PdfName? = null
    protected var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }

    // constructors

    /**
     * Empty constructor.
     */
    constructor() {
        this.content = StringBuffer()
        this.font = Font()
        this.role = PdfName.SPAN
    }

    /**
     * A Chunk copy constructor.
     * @param ck the Chunk to be copied
     */
    constructor(ck: Chunk) {
        if (ck.content != null) {
            content = StringBuffer(ck.content!!.toString())
        }
        if (ck.font != null) {
            font = Font(ck.font)
        }
        if (ck.attributes != null) {
            attributes = HashMap(ck.attributes)
        }
        role = ck.role
        if (ck.accessibleAttributes != null) {
            accessibleAttributes = HashMap(ck.accessibleAttributes)
        }
        id = ck.id
    }

    /**
     * Constructs a chunk of text with a certain content and a certain
     * Font.

     * @param content
     * *            the content
     * *
     * @param font
     * *            the font
     */
    @JvmOverloads constructor(content: String, font: Font = Font()) {
        this.content = StringBuffer(content)
        this.font = font
        this.role = PdfName.SPAN
    }

    /**
     * Constructs a chunk of text with a char and a certain Font.

     * @param c
     * *            the content
     * *
     * @param font
     * *            the font
     */
    @JvmOverloads constructor(c: Char, font: Font = Font()) {
        this.content = StringBuffer()
        this.content!!.append(c)
        this.font = font
        this.role = PdfName.SPAN
    }

    /**
     * Constructs a chunk containing an Image.

     * @param image
     * *            the image
     * *
     * @param offsetX
     * *            the image offset in the x direction
     * *
     * @param offsetY
     * *            the image offset in the y direction
     */
    constructor(image: Image, offsetX: Float, offsetY: Float) : this(OBJECT_REPLACEMENT_CHARACTER, Font()) {
        val copyImage = Image.getInstance(image)
        copyImage.setAbsolutePosition(java.lang.Float.NaN, java.lang.Float.NaN)
        setAttribute(IMAGE, arrayOf(copyImage, offsetX, offsetY, java.lang.Boolean.FALSE))
        this.role = null
    }

    /**
     * Creates a separator Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
     * @param    separator    the drawInterface to use to draw the separator.
     * *
     * @param    vertical    true if this is a vertical separator
     * *
     * @since    2.1.2
     */
    @JvmOverloads constructor(separator: DrawInterface, vertical: Boolean = false) : this(OBJECT_REPLACEMENT_CHARACTER, Font()) {
        setAttribute(SEPARATOR, arrayOf(separator, java.lang.Boolean.valueOf(vertical)))
        this.role = null
    }

    private var contentWithNoTabs: String? = null

    /**
     * Creates a tab Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
     * @param    separator    the drawInterface to use to draw the tab.
     * *
     * @param    tabPosition    an X coordinate that will be used as start position for the next Chunk.
     * *
     * @param    newline        if true, a newline will be added if the tabPosition has already been reached.
     * *
     * @since    2.1.2
     */
    @Deprecated("")
    @JvmOverloads constructor(separator: DrawInterface, tabPosition: Float, newline: Boolean = false) : this(OBJECT_REPLACEMENT_CHARACTER, Font()) {
        if (tabPosition < 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("a.tab.position.may.not.be.lower.than.0.yours.is.1", tabPosition.toString()))
        }
        setAttribute(TAB, arrayOf(separator, tabPosition, java.lang.Boolean.valueOf(newline), 0))
        this.role = PdfName.ARTIFACT
    }

    /**
     * Creates a tab Chunk.

     * @param   tabInterval     an interval that will be used if tab stops are omitted.
     * *
     * @param   isWhitespace    if true, the current tab is treated as white space.
     * *
     * @since 5.4.1
     */
    private constructor(tabInterval: Float?, isWhitespace: Boolean) : this(OBJECT_REPLACEMENT_CHARACTER, Font()) {
        if (tabInterval < 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("a.tab.position.may.not.be.lower.than.0.yours.is.1", tabInterval.toString()))
        }
        setAttribute(TAB, arrayOf<Any>(tabInterval, java.lang.Boolean.valueOf(isWhitespace)))
        setAttribute(SPLITCHARACTER, TabSplitCharacter.TAB)

        setAttribute(TABSETTINGS, null)
        this.role = PdfName.ARTIFACT
    }

    /**
     * Constructs a chunk containing an Image.

     * @param image
     * *            the image
     * *
     * @param offsetX
     * *            the image offset in the x direction
     * *
     * @param offsetY
     * *            the image offset in the y direction
     * *
     * @param changeLeading
     * *            true if the leading has to be adapted to the image
     */
    constructor(image: Image, offsetX: Float, offsetY: Float,
                changeLeading: Boolean) : this(OBJECT_REPLACEMENT_CHARACTER, Font()) {
        setAttribute(IMAGE, arrayOf(image, offsetX, offsetY, java.lang.Boolean.valueOf(changeLeading)))
        this.role = PdfName.ARTIFACT
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param listener
     * *            an ElementListener
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
        return Element.CHUNK
    }

    /**
     * Gets all the chunks in this element.

     * @return an ArrayList
     */
    override val chunks: List<Chunk>
        get() {
            val tmp = ArrayList<Chunk>()
            tmp.add(this)
            return tmp
        }

    // methods that change the member variables

    /**
     * appends some text to this Chunk.

     * @param string
     * *            String
     * *
     * @return a StringBuffer
     */
    fun append(string: String): StringBuffer {
        contentWithNoTabs = null
        return content!!.append(string)
    }

    /**
     * Returns the content of this Chunk.

     * @return a String
     */
    fun getContent(): String {
        if (contentWithNoTabs == null)
            contentWithNoTabs = content!!.toString().replace("\t".toRegex(), "")
        return contentWithNoTabs
    }

    /**
     * Returns the content of this Chunk.

     * @return a String
     */
    override fun toString(): String {
        return getContent()
    }

    /**
     * Checks is this Chunk is empty.

     * @return false if the Chunk contains other characters than
     * *         space.
     */
    val isEmpty: Boolean
        get() = content!!.toString().trim { it <= ' ' }.length == 0
                && content!!.toString().indexOf("\n") == -1
                && attributes == null

    /**
     * Gets the width of the Chunk in points.

     * @return a width in points
     */
    val widthPoint: Float
        get() {
            if (image != null) {
                return image!!.scaledWidth
            }
            return font!!.getCalculatedBaseFont(true).getWidthPoint(getContent(),
                    font!!.calculatedSize) * horizontalScaling
        }

    // attributes

    /**
     * Checks the attributes of this Chunk.

     * @return false if there aren't any.
     */

    fun hasAttributes(): Boolean {
        return attributes != null && !attributes!!.isEmpty()
    }

    /**
     * Checks  the accessible attributes of this Chunk.

     * @return false if there aren't any.
     */
    fun hasAccessibleAttributes(): Boolean {
        return accessibleAttributes != null && !accessibleAttributes!!.isEmpty()
    }

    /**
     * Sets an arbitrary attribute.

     * @param name
     * *            the key for the attribute
     * *
     * @param obj
     * *            the value of the attribute
     * *
     * @return this Chunk
     */

    private fun setAttribute(name: String, obj: Any?): Chunk {
        if (attributes == null)
            attributes = HashMap<String, Any>()
        attributes!!.put(name, obj)
        return this
    }

    /**
     * Sets the text horizontal scaling. A value of 1 is normal and a value of
     * 0.5f shrinks the text to half it's width.

     * @param scale
     * *            the horizontal scaling factor
     * *
     * @return this Chunk
     */
    fun setHorizontalScaling(scale: Float): Chunk {
        return setAttribute(HSCALE, scale)
    }

    /**
     * Gets the horizontal scaling.

     * @return a percentage in float
     */
    val horizontalScaling: Float
        get() {
            if (attributes == null)
                return 1f
            val f = attributes!![HSCALE] as Float ?: return 1f
            return f.toFloat()
        }

    /**
     * Sets an horizontal line that can be an underline or a strikethrough.
     * Actually, the line can be anywhere vertically and has always the
     * Chunk width. Multiple call to this method will produce multiple
     * lines.

     * @param thickness
     * *            the absolute thickness of the line
     * *
     * @param yPosition
     * *            the absolute y position relative to the baseline
     * *
     * @return this Chunk
     */
    fun setUnderline(thickness: Float, yPosition: Float): Chunk {
        return setUnderline(null, thickness, 0f, yPosition, 0f,
                PdfContentByte.LINE_CAP_BUTT)
    }

    /**
     * Sets an horizontal line that can be an underline or a strikethrough.
     * Actually, the line can be anywhere vertically and has always the
     * Chunk width. Multiple call to this method will produce multiple
     * lines.

     * @param color
     * *            the color of the line or null to follow the
     * *            text color
     * *
     * @param thickness
     * *            the absolute thickness of the line
     * *
     * @param thicknessMul
     * *            the thickness multiplication factor with the font size
     * *
     * @param yPosition
     * *            the absolute y position relative to the baseline
     * *
     * @param yPositionMul
     * *            the position multiplication factor with the font size
     * *
     * @param cap
     * *            the end line cap. Allowed values are
     * *            PdfContentByte.LINE_CAP_BUTT, PdfContentByte.LINE_CAP_ROUND
     * *            and PdfContentByte.LINE_CAP_PROJECTING_SQUARE
     * *
     * @return this Chunk
     */
    fun setUnderline(color: BaseColor?, thickness: Float, thicknessMul: Float,
                     yPosition: Float, yPositionMul: Float, cap: Int): Chunk {
        if (attributes == null)
            attributes = HashMap<String, Any>()
        val obj = arrayOf<Any>(color, floatArrayOf(thickness, thicknessMul, yPosition, yPositionMul, cap.toFloat()))
        val unders = Utilities.addToArray(attributes!![UNDERLINE] as Array<Array<Any>>,
                obj)
        return setAttribute(UNDERLINE, unders)
    }

    /**
     * Sets the text displacement relative to the baseline. Positive values rise
     * the text, negative values lower the text.
     *
     * It can be used to implement sub/superscript.

     * @param rise
     * *            the displacement in points
     * *
     * @return this Chunk
     */

    fun setTextRise(rise: Float): Chunk {
        return setAttribute(SUBSUPSCRIPT, rise)
    }

    /**
     * Gets the text displacement relative to the baseline.

     * @return a displacement in points
     */
    val textRise: Float
        get() {
            if (attributes != null && attributes!!.containsKey(SUBSUPSCRIPT)) {
                val f = attributes!![SUBSUPSCRIPT] as Float
                return f.toFloat()
            }
            return 0.0f
        }

    /**
     * Skews the text to simulate italic and other effects. Try alpha=0
     *  and beta=12.

     * @param alpha
     * *            the first angle in degrees
     * *
     * @param beta
     * *            the second angle in degrees
     * *
     * @return this Chunk
     */
    fun setSkew(alpha: Float, beta: Float): Chunk {
        var alpha = alpha
        var beta = beta
        alpha = Math.tan(alpha * Math.PI / 180).toFloat()
        beta = Math.tan(beta * Math.PI / 180).toFloat()
        return setAttribute(SKEW, floatArrayOf(alpha, beta))
    }

    /**
     * Sets the color and the size of the background Chunk.

     * @param color
     * *            the color of the background
     * *
     * @param extraLeft
     * *            increase the size of the rectangle in the left
     * *
     * @param extraBottom
     * *            increase the size of the rectangle in the bottom
     * *
     * @param extraRight
     * *            increase the size of the rectangle in the right
     * *
     * @param extraTop
     * *            increase the size of the rectangle in the top
     * *
     * @return this Chunk
     */
    @JvmOverloads fun setBackground(color: BaseColor, extraLeft: Float = 0f, extraBottom: Float = 0f,
                                    extraRight: Float = 0f, extraTop: Float = 0f): Chunk {
        return setAttribute(BACKGROUND, arrayOf(color, floatArrayOf(extraLeft, extraBottom, extraRight, extraTop)))
    }

    /**
     * Sets the text rendering mode. It can outline text, simulate bold and make
     * text invisible.

     * @param mode
     * *            the text rendering mode. It can be
     * *            PdfContentByte.TEXT_RENDER_MODE_FILL,
     * *            PdfContentByte.TEXT_RENDER_MODE_STROKE,
     * *            PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE and
     * *            PdfContentByte.TEXT_RENDER_MODE_INVISIBLE.
     * *
     * @param strokeWidth
     * *            the stroke line width for the modes
     * *            PdfContentByte.TEXT_RENDER_MODE_STROKE and
     * *            PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE.
     * *
     * @param strokeColor
     * *            the stroke color or null to follow the text
     * *            color
     * *
     * @return this Chunk
     */
    fun setTextRenderMode(mode: Int, strokeWidth: Float,
                          strokeColor: BaseColor): Chunk {
        return setAttribute(TEXTRENDERMODE, arrayOf(Integer.valueOf(mode), strokeWidth, strokeColor))
    }

    /**
     * Sets the split characters.

     * @param splitCharacter
     * *            the SplitCharacter interface
     * *
     * @return this Chunk
     */

    fun setSplitCharacter(splitCharacter: SplitCharacter): Chunk {
        return setAttribute(SPLITCHARACTER, splitCharacter)
    }

    /**
     * sets the hyphenation engine to this Chunk.

     * @param hyphenation
     * *            the hyphenation engine
     * *
     * @return this Chunk
     */
    fun setHyphenation(hyphenation: HyphenationEvent): Chunk {
        return setAttribute(HYPHENATION, hyphenation)
    }

    /**
     * Sets a goto for a remote destination for this Chunk.

     * @param filename
     * *            the file name of the destination document
     * *
     * @param name
     * *            the name of the destination to go to
     * *
     * @return this Chunk
     */

    fun setRemoteGoto(filename: String, name: String): Chunk {
        return setAttribute(REMOTEGOTO, arrayOf<Any>(filename, name))
    }

    /**
     * Sets a goto for a remote destination for this Chunk.

     * @param filename
     * *            the file name of the destination document
     * *
     * @param page
     * *            the page of the destination to go to. First page is 1
     * *
     * @return this Chunk
     */

    fun setRemoteGoto(filename: String, page: Int): Chunk {
        return setAttribute(REMOTEGOTO, arrayOf(filename, Integer.valueOf(page)))
    }

    /**
     * Sets a local goto for this Chunk.
     *
     * There must be a local destination matching the name.

     * @param name
     * *            the name of the destination to go to
     * *
     * @return this Chunk
     */

    fun setLocalGoto(name: String): Chunk {
        return setAttribute(LOCALGOTO, name)
    }

    /**
     * Sets a local destination for this Chunk.

     * @param name
     * *            the name for this destination
     * *
     * @return this Chunk
     */
    fun setLocalDestination(name: String): Chunk {
        return setAttribute(LOCALDESTINATION, name)
    }

    /**
     * Sets the generic tag Chunk.
     *
     * The text for this tag can be retrieved with PdfPageEvent.

     * @param text
     * *            the text for the tag
     * *
     * @return this Chunk
     */

    fun setGenericTag(text: String): Chunk {
        return setAttribute(GENERICTAG, text)
    }

    /**
     * Sets a line height tag.

     * @return this Chunk
     */

    fun setLineHeight(lineheight: Float): Chunk {
        return setAttribute(LINEHEIGHT, lineheight)
    }

    /**
     * Returns the image.

     * @return the image
     */

    val image: Image?
        get() {
            if (attributes == null)
                return null
            val obj = attributes!![Chunk.IMAGE] as Array<Any>
            if (obj == null)
                return null
            else {
                return obj[0] as Image
            }
        }

    /**
     * Sets an action for this Chunk.

     * @param action
     * *            the action
     * *
     * @return this Chunk
     */

    fun setAction(action: PdfAction): Chunk {
        setRole(PdfName.LINK)
        return setAttribute(ACTION, action)
    }

    /**
     * Sets an anchor for this Chunk.

     * @param url
     * *            the URL to link to
     * *
     * @return this Chunk
     */

    fun setAnchor(url: URL): Chunk {
        setRole(PdfName.LINK)
        val urlStr = url.toExternalForm()
        setAccessibleAttribute(PdfName.ALT, PdfString(urlStr))
        return setAttribute(ACTION, PdfAction(urlStr))
    }

    /**
     * Sets an anchor for this Chunk.

     * @param url
     * *            the url to link to
     * *
     * @return this Chunk
     */

    fun setAnchor(url: String): Chunk {
        setRole(PdfName.LINK)
        setAccessibleAttribute(PdfName.ALT, PdfString(url))
        return setAttribute(ACTION, PdfAction(url))
    }

    /**
     * Sets a new page tag..

     * @return this Chunk
     */

    fun setNewPage(): Chunk {
        return setAttribute(NEWPAGE, null)
    }

    /**
     * Sets a generic annotation to this Chunk.

     * @param annotation
     * *            the annotation
     * *
     * @return this Chunk
     */
    fun setAnnotation(annotation: PdfAnnotation): Chunk {
        return setAttribute(PDFANNOTATION, annotation)
    }

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
        get() = true

    /**
     * Returns the hyphenation (if present).
     * @return the HypenationEvent of this Chunk
     * *
     * @since    2.1.2
     */
    val hyphenation: HyphenationEvent?
        get() {
            if (attributes == null) return null
            return attributes!![Chunk.HYPHENATION] as HyphenationEvent
        }

    /**
     * Sets the character spacing.

     * @param charSpace the character spacing value
     * *
     * @return this Chunk
     */
    fun setCharacterSpacing(charSpace: Float): Chunk {
        return setAttribute(CHAR_SPACING, charSpace)
    }

    /**
     * Gets the character spacing.

     * @return a value in float
     */
    val characterSpacing: Float
        get() {
            if (attributes != null && attributes!!.containsKey(CHAR_SPACING)) {
                val f = attributes!![CHAR_SPACING] as Float
                return f.toFloat()
            }
            return 0.0f
        }

    /**
     * Sets the word spacing.

     * @param wordSpace the word spacing value
     * *
     * @return this Chunk
     */
    fun setWordSpacing(wordSpace: Float): Chunk {
        return setAttribute(WORD_SPACING, wordSpace)
    }

    /**
     * Gets the word spacing.

     * @return a value in float
     */
    val wordSpacing: Float
        get() {
            if (attributes != null && attributes!!.containsKey(WORD_SPACING)) {
                val f = attributes!![WORD_SPACING] as Float
                return f.toFloat()
            }
            return 0.0f
        }

    val isWhitespace: Boolean
        get() = attributes != null && attributes!!.containsKey(WHITESPACE)

    val isTabspace: Boolean
        @Deprecated("")
        get() = attributes != null && attributes!!.containsKey(TAB)

    override fun getAccessibleAttribute(key: PdfName): PdfObject {
        if (image != null) {
            return image!!.getAccessibleAttribute(key)
        } else if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    override fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (image != null) {
            image!!.setAccessibleAttribute(key, value)
        } else {
            if (accessibleAttributes == null)
                accessibleAttributes = HashMap<PdfName, PdfObject>()
            accessibleAttributes!!.put(key, value)
        }
    }

    override fun getAccessibleAttributes(): HashMap<PdfName, PdfObject> {
        if (image != null)
            return image!!.accessibleAttributes
        else
            return accessibleAttributes
    }

    override fun getRole(): PdfName {
        if (image != null)
            return image!!.role
        else
            return role
    }

    override fun setRole(role: PdfName) {
        if (image != null)
            image!!.role = role
        else
            this.role = role
    }

    override val isInline: Boolean
        get() = true

    /**
     * Sets the textual expansion of the abbreviation or acronym.
     * It is highly recommend to set textuual expansion when generating PDF/UA documents.
     * @param value
     */
    var textExpansion: String?
        get() {
            val o = getAccessibleAttribute(PdfName.E)
            if (o is PdfString)
                return o.toUnicodeString()
            return null
        }
        set(value) = setAccessibleAttribute(PdfName.E, PdfString(value))

    companion object {

        // public static membervariables

        /** The character stand in for an image or a separator.  */
        val OBJECT_REPLACEMENT_CHARACTER = "\ufffc"

        /** This is a Chunk containing a newline.  */
        val NEWLINE = Chunk("\n")

        init {
            NEWLINE.setRole(PdfName.P)
        }

        /** This is a Chunk containing a newpage.  */
        val NEXTPAGE = Chunk("")

        init {
            NEXTPAGE.setNewPage()
        }

        val TABBING = Chunk(java.lang.Float.NaN, false)

        val SPACETABBING = Chunk(java.lang.Float.NaN, true)

        /**
         * Key for drawInterface of the Separator.
         * @since    2.1.2
         */
        val SEPARATOR = "SEPARATOR"

        /**
         * Key for drawInterface of the tab.
         * @since    2.1.2
         */
        val TAB = "TAB"
        /**
         * Key for tab stops of the tab.
         * @since    5.4.1
         */
        val TABSETTINGS = "TABSETTINGS"

        // the attributes are ordered as they appear in the book 'iText in Action'

        /** Key for text horizontal scaling.  */
        val HSCALE = "HSCALE"

        /** Key for underline.  */
        val UNDERLINE = "UNDERLINE"

        /** Key for sub/superscript.  */
        val SUBSUPSCRIPT = "SUBSUPSCRIPT"

        /** Key for text skewing.  */
        val SKEW = "SKEW"

        /** Key for background.  */
        val BACKGROUND = "BACKGROUND"

        /** Key for text rendering mode.  */
        val TEXTRENDERMODE = "TEXTRENDERMODE"

        /** Key for split character.  */
        val SPLITCHARACTER = "SPLITCHARACTER"

        /** Key for hyphenation.  */
        val HYPHENATION = "HYPHENATION"

        /** Key for remote goto.  */
        val REMOTEGOTO = "REMOTEGOTO"

        /** Key for local goto.  */
        val LOCALGOTO = "LOCALGOTO"

        /** Key for local destination.  */
        val LOCALDESTINATION = "LOCALDESTINATION"

        /** Key for generic tag.  */
        val GENERICTAG = "GENERICTAG"

        /** Key for line-height (alternative for leading in Phrase).  */
        val LINEHEIGHT = "LINEHEIGHT"


        /** Key for image.  */
        val IMAGE = "IMAGE"

        /** Key for Action.  */
        val ACTION = "ACTION"

        /** Key for newpage.  */
        val NEWPAGE = "NEWPAGE"

        /** Key for annotation.  */
        val PDFANNOTATION = "PDFANNOTATION"

        // keys used in PdfChunk

        /** Key for color.  */
        val COLOR = "COLOR"

        /** Key for encoding.  */
        val ENCODING = "ENCODING"

        /**
         * Key for character spacing.
         */
        val CHAR_SPACING = "CHAR_SPACING"

        /**
         * Key for word spacing.
         */
        val WORD_SPACING = "WORD_SPACING"

        val WHITESPACE = "WHITESPACE"

        @JvmOverloads fun createWhitespace(content: String, preserve: Boolean = false): Chunk {
            var whitespace: Chunk? = null
            if (!preserve) {
                whitespace = Chunk(' ')
                whitespace.setAttribute(WHITESPACE, content)
            } else {
                whitespace = Chunk(content)
            }

            return whitespace
        }

        @Deprecated("")
        @JvmOverloads fun createTabspace(spacing: Float = 60f): Chunk {
            val tabspace = Chunk(spacing, true)
            return tabspace
        }
    }

}
/**
 * Constructs a chunk of text with a certain content, without specifying a
 * Font.

 * @param content
 * *            the content
 */
/**
 * Constructs a chunk of text with a char, without specifying a Font
 * .

 * @param c
 * *            the content
 */
/**
 * Creates a separator Chunk.
 * Note that separator chunks can't be used in combination with tab chunks!
 * @param    separator    the drawInterface to use to draw the separator.
 * *
 * @since    2.1.2
 */
/**
 * Creates a tab Chunk.
 * Note that separator chunks can't be used in combination with tab chunks!
 * @param    separator    the drawInterface to use to draw the tab.
 * *
 * @param    tabPosition    an X coordinate that will be used as start position for the next Chunk.
 * *
 * @since    2.1.2
 */
/**
 * Sets the color of the background Chunk.

 * @param color
 * *            the color of the background
 * *
 * @return this Chunk
 */
