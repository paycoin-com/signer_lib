/*
 * $Id: 7cfe9a14fc4f1c29fea8178db7f9e1db53638063 $
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

import com.itextpdf.awt.FontMapper
import com.itextpdf.awt.PdfGraphics2D
import com.itextpdf.awt.PdfPrinterGraphics2D
import com.itextpdf.awt.geom.AffineTransform
import com.itextpdf.awt.geom.Point2D
import com.itextpdf.text.Annotation
import com.itextpdf.text.BaseColor
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.ImgJBIG2
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.IllegalPdfSyntaxException
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.internal.PdfAnnotationsImp
import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

/**
   * PdfContentByte is an object containing the user positioned
   * text and graphic contents of a page. It knows how to apply the proper
   * font encoding.
  */

open class PdfContentByte// constructors

    /**
       * Constructs a new PdfContentByte-object.
     
       * @param wr the writer associated to this content
 */

    (wr:PdfWriter?) {

/**
       * This class keeps the graphic state of the current page
      */

     class GraphicState {

/** This is the font in use  */
        internal var fontDetails:FontDetails? = null

/** This is the color in use  */
        internal var colorDetails:ColorDetails

/** This is the font size in use  */
        internal var size:Float = 0.toFloat()

/** The x position of the text line matrix.  */
        protected var xTLM = 0f
/** The y position of the text line matrix.  */
        protected var yTLM = 0f

protected var aTLM = 1f
protected var bTLM = 0f
protected var cTLM = 0f
protected var dTLM = 1f

protected var tx = 0f

/** The current text leading.  */
        protected var leading = 0f

/** The current horizontal scaling  */
        protected var scale = 100f

/** The current character spacing  */
        protected var charSpace = 0f

/** The current word spacing  */
        protected var wordSpace = 0f

protected var colorFill:BaseColor = GrayColor(0)
protected var colorStroke:BaseColor = GrayColor(0)
protected var textRenderMode = TEXT_RENDER_MODE_FILL
protected var CTM = AffineTransform()
protected var extGState:PdfObject? = null

internal constructor() {}

internal constructor(cp:GraphicState) {
copyParameters(cp)
}

internal fun copyParameters(cp:GraphicState) {
fontDetails = cp.fontDetails
colorDetails = cp.colorDetails
size = cp.size
xTLM = cp.xTLM
yTLM = cp.yTLM
aTLM = cp.aTLM
bTLM = cp.bTLM
cTLM = cp.cTLM
dTLM = cp.dTLM
tx = cp.tx
leading = cp.leading
scale = cp.scale
charSpace = cp.charSpace
wordSpace = cp.wordSpace
colorFill = cp.colorFill
colorStroke = cp.colorStroke
CTM = AffineTransform(cp.CTM)
textRenderMode = cp.textRenderMode
extGState = cp.extGState
}

internal fun restore(restore:GraphicState) {
copyParameters(restore)
}
}
// membervariables

    /** This is the actual content  */
    /**
       * Gets the internal buffer.
       * @return the internal buffer
 */
     var internalBuffer = ByteBuffer()
protected set

protected var markedContentSize = 0

/** This is the writer  */
    /**
       * Gets the PdfWriter in use by this object.
       * @return the PdfWriter in use by this object
 */
     var pdfWriter:PdfWriter? = null
protected set

/** This is the PdfDocument  */
    /**
       * Gets the PdfDocument in use by this object.
       * @return the PdfDocument in use by this object
 */
     var pdfDocument:PdfDocument
protected set

/** This is the GraphicState in use  */
    protected var state = GraphicState()

/** The list were we save/restore the state  */
    protected var stateList = ArrayList<GraphicState>()

/** The list were we save/restore the layer depth  */
    protected var layerDepth:ArrayList<Int>? = null

/** The separator between commands.
      */
    protected var separator:Int = '\n'

private var mcDepth = 0
protected var inText = false
private set

private var mcElements = ArrayList<IAccessibleElement>()

protected var duplicatedFrom:PdfContentByte? = null
init{
if (wr != null)
{
pdfWriter = wr
pdfDocument = pdfWriter!!.pdfDocument
}
}

// methods to get the content of this object

    /**
       * Returns the String representation of this PdfContentByte-object.
     
       * @return      a String
 */

    override fun toString():String {
return internalBuffer.toString()
}

/**
       * Checks if the content needs to be tagged.
       * @return false if no tags need to be added
 */
    open val isTagged:Boolean
get() =pdfWriter != null && pdfWriter!!.isTagged

/** Returns the PDF representation of this PdfContentByte-object.
     
       * @param writer the PdfWriter
      * * 
 * @return a byte array with the representation
 */

     fun toPdf(writer:PdfWriter):ByteArray {
sanityCheck()
return internalBuffer.toByteArray()
}

// methods to add graphical content

    /**
       * Adds the content of another PdfContentByte-object to this object.
     
       * @param       other       another PdfByteContent-object
 */

     fun add(other:PdfContentByte) {
if (other.pdfWriter != null && pdfWriter !== other.pdfWriter)
throw RuntimeException(MessageLocalization.getComposedMessage("inconsistent.writers.are.you.mixing.two.documents"))
internalBuffer.append(other.internalBuffer)
markedContentSize += other.markedContentSize
}

/**
       * Gets the x position of the text line matrix.
     
       * @return the x position of the text line matrix
 */
     val xtlm:Float
get() =state.xTLM

/**
       * Gets the y position of the text line matrix.
     
       * @return the y position of the text line matrix
 */
     val ytlm:Float
get() =state.yTLM

/**
       * Gets the current text leading.
     
       * @return the current text leading
 */
    /**
       * Sets the text leading parameter.
       * 
       * The leading parameter is measured in text space units. It specifies the vertical distance
       * between the baselines of adjacent lines of text.
     
       * @param       leading         the new leading
 */
     var leading:Float
get() =state.leading
set(leading) {
if (!inText && isTagged)
{
beginText(true)
}
state.leading = leading
internalBuffer.append(leading).append(" TL").append_i(separator)
}

/**
       * Gets the current character spacing.
     
       * @return the current character spacing
 */
    /**
       * Sets the character spacing parameter.
     
       * @param       charSpace           a parameter
 */
     var characterSpacing:Float
get() =state.charSpace
set(charSpace) {
if (!inText && isTagged)
{
beginText(true)
}
state.charSpace = charSpace
internalBuffer.append(charSpace).append(" Tc").append_i(separator)
}

/**
       * Gets the current word spacing.
     
       * @return the current word spacing
 */
    /**
       * Sets the word spacing parameter.
     
       * @param       wordSpace           a parameter
 */
     var wordSpacing:Float
get() =state.wordSpace
set(wordSpace) {
if (!inText && isTagged)
{
beginText(true)
}
state.wordSpace = wordSpace
internalBuffer.append(wordSpace).append(" Tw").append_i(separator)
}

/**
       * Gets the current character spacing.
     
       * @return the current character spacing
 */
    /**
       * Sets the horizontal scaling parameter.
     
       * @param       scale               a parameter
 */
     var horizontalScaling:Float
get() =state.scale
set(scale) {
if (!inText && isTagged)
{
beginText(true)
}
state.scale = scale
internalBuffer.append(scale).append(" Tz").append_i(separator)
}

/**
       * Changes the Flatness.
       * 
       * Flatness sets the maximum permitted distance in device pixels between the
       * mathematically correct path and an approximation constructed from straight line segments.
     
       * @param       flatness        a value
 */

     fun setFlatness(flatness:Float) {
setFlatness(flatness.toDouble())
}

/**
       * Changes the Flatness.
       * 
       * Flatness sets the maximum permitted distance in device pixels between the
       * mathematically correct path and an approximation constructed from straight line segments.
     
       * @param       flatness        a value
 */

     fun setFlatness(flatness:Double) {
if (flatness >= 0 && flatness <= 100)
{
internalBuffer.append(flatness).append(" i").append_i(separator)
}
}

/**
       * Changes the Line cap style.
       * 
       * The line cap style specifies the shape to be used at the end of open subpaths
       * when they are stroked.
       * Allowed values are LINE_CAP_BUTT, LINE_CAP_ROUND and LINE_CAP_PROJECTING_SQUARE.
     
       * @param       style       a value
 */

     fun setLineCap(style:Int) {
if (style >= 0 && style <= 2)
{
internalBuffer.append(style).append(" J").append_i(separator)
}
}

/**
       * Set the rendering intent, possible values are: PdfName.ABSOLUTECOLORIMETRIC,
       * PdfName.RELATIVECOLORIMETRIC, PdfName.SATURATION, PdfName.PERCEPTUAL.
       * @param ri
 */
     fun setRenderingIntent(ri:PdfName) {
internalBuffer.append(ri.bytes).append(" ri").append_i(separator)
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
 */

     fun setLineDash(phase:Float) {
setLineDash(phase.toDouble())
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
 */

     fun setLineDash(phase:Double) {
internalBuffer.append("[] ").append(phase).append(" d").append_i(separator)
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
      * * 
 * @param       unitsOn     the number of units that must be 'on' (equals the number of units that must be 'off').
 */

     fun setLineDash(unitsOn:Float, phase:Float) {
setLineDash(unitsOn.toDouble(), phase.toDouble())
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
      * * 
 * @param       unitsOn     the number of units that must be 'on' (equals the number of units that must be 'off').
 */

     fun setLineDash(unitsOn:Double, phase:Double) {
internalBuffer.append("[").append(unitsOn).append("] ").append(phase).append(" d").append_i(separator)
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
      * * 
 * @param       unitsOn     the number of units that must be 'on'
      * * 
 * @param       unitsOff    the number of units that must be 'off'
 */

     fun setLineDash(unitsOn:Float, unitsOff:Float, phase:Float) {
setLineDash(unitsOn.toDouble(), unitsOff.toDouble(), phase.toDouble())
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       phase       the value of the phase
      * * 
 * @param       unitsOn     the number of units that must be 'on'
      * * 
 * @param       unitsOff    the number of units that must be 'off'
 */

     fun setLineDash(unitsOn:Double, unitsOff:Double, phase:Double) {
internalBuffer.append("[").append(unitsOn).append(' ').append(unitsOff).append("] ").append(phase).append(" d").append_i(separator)
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       array       length of the alternating dashes and gaps
      * * 
 * @param       phase       the value of the phase
 */

     fun setLineDash(array:FloatArray, phase:Float) {
internalBuffer.append("[")
for (i in array.indices)
{
internalBuffer.append(array[i])
if (i < array.size - 1) internalBuffer.append(' ')
}
internalBuffer.append("] ").append(phase).append(" d").append_i(separator)
}

/**
       * Changes the value of the line dash pattern.
       * 
       * The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
       * It is specified by an array and a phase. The array specifies the length
       * of the alternating dashes and gaps. The phase specifies the distance into the dash
       * pattern to start the dash.
     
       * @param       array       length of the alternating dashes and gaps
      * * 
 * @param       phase       the value of the phase
 */

     fun setLineDash(array:DoubleArray, phase:Double) {
internalBuffer.append("[")
for (i in array.indices)
{
internalBuffer.append(array[i])
if (i < array.size - 1) internalBuffer.append(' ')
}
internalBuffer.append("] ").append(phase).append(" d").append_i(separator)
}

/**
       * Changes the Line join style.
       * 
       * The line join style specifies the shape to be used at the corners of paths
       * that are stroked.
       * Allowed values are LINE_JOIN_MITER (Miter joins), LINE_JOIN_ROUND (Round joins) and LINE_JOIN_BEVEL (Bevel joins).
     
       * @param       style       a value
 */

     fun setLineJoin(style:Int) {
if (style >= 0 && style <= 2)
{
internalBuffer.append(style).append(" j").append_i(separator)
}
}

/**
       * Changes the line width.
       * 
       * The line width specifies the thickness of the line used to stroke a path and is measured
       * in user space units.
     
       * @param       w           a width
 */

     fun setLineWidth(w:Float) {
setLineWidth(w.toDouble())
}

/**
       * Changes the line width.
       * 
       * The line width specifies the thickness of the line used to stroke a path and is measured
       * in user space units.
     
       * @param       w           a width
 */

     fun setLineWidth(w:Double) {
internalBuffer.append(w).append(" w").append_i(separator)
}

/**
       * Changes the Miter limit.
       * 
       * When two line segments meet at a sharp angle and mitered joins have been specified as the
       * line join style, it is possible for the miter to extend far beyond the thickness of the line
       * stroking path. The miter limit imposes a maximum on the ratio of the miter length to the line
       * witdh. When the limit is exceeded, the join is converted from a miter to a bevel.
     
       * @param       miterLimit      a miter limit
 */

     fun setMiterLimit(miterLimit:Float) {
setMiterLimit(miterLimit.toDouble())
}

/**
       * Changes the Miter limit.
       * 
       * When two line segments meet at a sharp angle and mitered joins have been specified as the
       * line join style, it is possible for the miter to extend far beyond the thickness of the line
       * stroking path. The miter limit imposes a maximum on the ratio of the miter length to the line
       * witdh. When the limit is exceeded, the join is converted from a miter to a bevel.
     
       * @param       miterLimit      a miter limit
 */

     fun setMiterLimit(miterLimit:Double) {
if (miterLimit > 1)
{
internalBuffer.append(miterLimit).append(" M").append_i(separator)
}
}

/**
       * Modify the current clipping path by intersecting it with the current path, using the
       * nonzero winding number rule to determine which regions lie inside the clipping
       * path.
      */

     fun clip() {
if (inText && isTagged)
{
endText()
}
internalBuffer.append("W").append_i(separator)
}

/**
       * Modify the current clipping path by intersecting it with the current path, using the
       * even-odd rule to determine which regions lie inside the clipping path.
      */

     fun eoClip() {
if (inText && isTagged)
{
endText()
}
internalBuffer.append("W*").append_i(separator)
}

/**
       * Changes the currentgray tint for filling paths (device dependent colors!).
       * 
       * Sets the color space to DeviceGray (or the DefaultGray color space),
       * and sets the gray tint to use for filling paths.
     
       * @param   gray    a value between 0 (black) and 1 (white)
 */

    open fun setGrayFill(gray:Float) {
saveColor(GrayColor(gray), true)
internalBuffer.append(gray).append(" g").append_i(separator)
}

/**
       * Changes the current gray tint for filling paths to black.
      */

    open fun resetGrayFill() {
saveColor(GrayColor(0), true)
internalBuffer.append("0 g").append_i(separator)
}

/**
       * Changes the currentgray tint for stroking paths (device dependent colors!).
       * 
       * Sets the color space to DeviceGray (or the DefaultGray color space),
       * and sets the gray tint to use for stroking paths.
     
       * @param   gray    a value between 0 (black) and 1 (white)
 */

    open fun setGrayStroke(gray:Float) {
saveColor(GrayColor(gray), false)
internalBuffer.append(gray).append(" G").append_i(separator)
}

/**
       * Changes the current gray tint for stroking paths to black.
      */

    open fun resetGrayStroke() {
saveColor(GrayColor(0), false)
internalBuffer.append("0 G").append_i(separator)
}

/**
       * Helper to validate and write the RGB color components
       * @param   red     the intensity of red. A value between 0 and 1
      * * 
 * @param   green   the intensity of green. A value between 0 and 1
      * * 
 * @param   blue    the intensity of blue. A value between 0 and 1
 */
    private fun HelperRGB(red:Float, green:Float, blue:Float) {
var red = red
var green = green
var blue = blue
if (red < 0)
red = 0.0f
else if (red > 1.0f)
red = 1.0f
if (green < 0)
green = 0.0f
else if (green > 1.0f)
green = 1.0f
if (blue < 0)
blue = 0.0f
else if (blue > 1.0f)
blue = 1.0f
internalBuffer.append(red).append(' ').append(green).append(' ').append(blue)
}

/**
       * Changes the current color for filling paths (device dependent colors!).
       * 
       * Sets the color space to DeviceRGB (or the DefaultRGB color space),
       * and sets the color to use for filling paths.
       * 
       * Following the PDF manual, each operand must be a number between 0 (minimum intensity) and
       * 1 (maximum intensity).
     
       * @param   red     the intensity of red. A value between 0 and 1
      * * 
 * @param   green   the intensity of green. A value between 0 and 1
      * * 
 * @param   blue    the intensity of blue. A value between 0 and 1
 */

    open fun setRGBColorFillF(red:Float, green:Float, blue:Float) {
saveColor(BaseColor(red, green, blue), true)
HelperRGB(red, green, blue)
internalBuffer.append(" rg").append_i(separator)
}

/**
       * Changes the current color for filling paths to black.
      */

    open fun resetRGBColorFill() {
resetGrayFill()
}

/**
       * Changes the current color for stroking paths (device dependent colors!).
       * 
       * Sets the color space to DeviceRGB (or the DefaultRGB color space),
       * and sets the color to use for stroking paths.
       * 
       * Following the PDF manual, each operand must be a number between 0 (miniumum intensity) and
       * 1 (maximum intensity).
     
       * @param   red     the intensity of red. A value between 0 and 1
      * * 
 * @param   green   the intensity of green. A value between 0 and 1
      * * 
 * @param   blue    the intensity of blue. A value between 0 and 1
 */

    open fun setRGBColorStrokeF(red:Float, green:Float, blue:Float) {
saveColor(BaseColor(red, green, blue), false)
HelperRGB(red, green, blue)
internalBuffer.append(" RG").append_i(separator)
}

/**
       * Changes the current color for stroking paths to black.
     
      */

    open fun resetRGBColorStroke() {
resetGrayStroke()
}

/**
       * Helper to validate and write the CMYK color components.
     
       * @param   cyan    the intensity of cyan. A value between 0 and 1
      * * 
 * @param   magenta the intensity of magenta. A value between 0 and 1
      * * 
 * @param   yellow  the intensity of yellow. A value between 0 and 1
      * * 
 * @param   black   the intensity of black. A value between 0 and 1
 */
    private fun HelperCMYK(cyan:Float, magenta:Float, yellow:Float, black:Float) {
var cyan = cyan
var magenta = magenta
var yellow = yellow
var black = black
if (cyan < 0)
cyan = 0.0f
else if (cyan > 1.0f)
cyan = 1.0f
if (magenta < 0)
magenta = 0.0f
else if (magenta > 1.0f)
magenta = 1.0f
if (yellow < 0)
yellow = 0.0f
else if (yellow > 1.0f)
yellow = 1.0f
if (black < 0)
black = 0.0f
else if (black > 1.0f)
black = 1.0f
internalBuffer.append(cyan).append(' ').append(magenta).append(' ').append(yellow).append(' ').append(black)
}

/**
       * Changes the current color for filling paths (device dependent colors!).
       * 
       * Sets the color space to DeviceCMYK (or the DefaultCMYK color space),
       * and sets the color to use for filling paths.
       * 
       * Following the PDF manual, each operand must be a number between 0 (no ink) and
       * 1 (maximum ink).
     
       * @param   cyan    the intensity of cyan. A value between 0 and 1
      * * 
 * @param   magenta the intensity of magenta. A value between 0 and 1
      * * 
 * @param   yellow  the intensity of yellow. A value between 0 and 1
      * * 
 * @param   black   the intensity of black. A value between 0 and 1
 */

    open fun setCMYKColorFillF(cyan:Float, magenta:Float, yellow:Float, black:Float) {
saveColor(CMYKColor(cyan, magenta, yellow, black), true)
HelperCMYK(cyan, magenta, yellow, black)
internalBuffer.append(" k").append_i(separator)
}

/**
       * Changes the current color for filling paths to black.
     
      */

    open fun resetCMYKColorFill() {
saveColor(CMYKColor(0, 0, 0, 1), true)
internalBuffer.append("0 0 0 1 k").append_i(separator)
}

/**
       * Changes the current color for stroking paths (device dependent colors!).
       * 
       * Sets the color space to DeviceCMYK (or the DefaultCMYK color space),
       * and sets the color to use for stroking paths.
       * 
       * Following the PDF manual, each operand must be a number between 0 (miniumum intensity) and
       * 1 (maximum intensity).
     
       * @param   cyan    the intensity of cyan. A value between 0 and 1
      * * 
 * @param   magenta the intensity of magenta. A value between 0 and 1
      * * 
 * @param   yellow  the intensity of yellow. A value between 0 and 1
      * * 
 * @param   black   the intensity of black. A value between 0 and 1
 */

    open fun setCMYKColorStrokeF(cyan:Float, magenta:Float, yellow:Float, black:Float) {
saveColor(CMYKColor(cyan, magenta, yellow, black), false)
HelperCMYK(cyan, magenta, yellow, black)
internalBuffer.append(" K").append_i(separator)
}

/**
       * Changes the current color for stroking paths to black.
     
      */

    open fun resetCMYKColorStroke() {
saveColor(CMYKColor(0, 0, 0, 1), false)
internalBuffer.append("0 0 0 1 K").append_i(separator)
}

/**
       * Move the current point (x, y), omitting any connecting line segment.
     
       * @param       x               new x-coordinate
      * * 
 * @param       y               new y-coordinate
 */

     fun moveTo(x:Float, y:Float) {
moveTo(x.toDouble(), y.toDouble())
}

/**
       * Move the current point (x, y), omitting any connecting line segment.
     
       * @param       x               new x-coordinate
      * * 
 * @param       y               new y-coordinate
 */

     fun moveTo(x:Double, y:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x).append(' ').append(y).append(" m").append_i(separator)
}

/**
       * Appends a straight line segment from the current point (x, y). The new current
       * point is (x, y).
     
       * @param       x               new x-coordinate
      * * 
 * @param       y               new y-coordinate
 */

     fun lineTo(x:Float, y:Float) {
lineTo(x.toDouble(), y.toDouble())
}

/**
       * Appends a straight line segment from the current point (x, y). The new current
       * point is (x, y).
     
       * @param       x               new x-coordinate
      * * 
 * @param       y               new y-coordinate
 */

     fun lineTo(x:Double, y:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x).append(' ').append(y).append(" l").append_i(separator)
}

/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x1      x-coordinate of the first control point
      * * 
 * @param       y1      y-coordinate of the first control point
      * * 
 * @param       x2      x-coordinate of the second control point
      * * 
 * @param       y2      y-coordinate of the second control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveTo(x1:Float, y1:Float, x2:Float, y2:Float, x3:Float, y3:Float) {
curveTo(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), x3.toDouble(), y3.toDouble())
}

/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x1      x-coordinate of the first control point
      * * 
 * @param       y1      y-coordinate of the first control point
      * * 
 * @param       x2      x-coordinate of the second control point
      * * 
 * @param       y2      y-coordinate of the second control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveTo(x1:Double, y1:Double, x2:Double, y2:Double, x3:Double, y3:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x1).append(' ').append(y1).append(' ').append(x2).append(' ').append(y2).append(' ').append(x3).append(' ').append(y3).append(" c").append_i(separator)
}

/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x2      x-coordinate of the second control point
      * * 
 * @param       y2      y-coordinate of the second control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveTo(x2:Float, y2:Float, x3:Float, y3:Float) {
curveTo(x2.toDouble(), y2.toDouble(), x3.toDouble(), y3.toDouble())
}
/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x2      x-coordinate of the second control point
      * * 
 * @param       y2      y-coordinate of the second control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveTo(x2:Double, y2:Double, x3:Double, y3:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x2).append(' ').append(y2).append(' ').append(x3).append(' ').append(y3).append(" v").append_i(separator)
}

/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x1      x-coordinate of the first control point
      * * 
 * @param       y1      y-coordinate of the first control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveFromTo(x1:Float, y1:Float, x3:Float, y3:Float) {
curveFromTo(x1.toDouble(), y1.toDouble(), x3.toDouble(), y3.toDouble())
}

/**
       * Appends a B&#xea;zier curve to the path, starting from the current point.
     
       * @param       x1      x-coordinate of the first control point
      * * 
 * @param       y1      y-coordinate of the first control point
      * * 
 * @param       x3      x-coordinate of the ending point (= new current point)
      * * 
 * @param       y3      y-coordinate of the ending point (= new current point)
 */

     fun curveFromTo(x1:Double, y1:Double, x3:Double, y3:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x1).append(' ').append(y1).append(' ').append(x3).append(' ').append(y3).append(" y").append_i(separator)
}

/** Draws a circle. The endpoint will (x+r, y).
     
       * @param x x center of circle
      * * 
 * @param y y center of circle
      * * 
 * @param r radius of circle
 */
     fun circle(x:Float, y:Float, r:Float) {
circle(x.toDouble(), y.toDouble(), r.toDouble())
}

/** Draws a circle. The endpoint will (x+r, y).
     
       * @param x x center of circle
      * * 
 * @param y y center of circle
      * * 
 * @param r radius of circle
 */
     fun circle(x:Double, y:Double, r:Double) {
val b = 0.5523f
moveTo(x + r, y)
curveTo(x + r, y + r * b, x + r * b, y + r, x, y + r)
curveTo(x - r * b, y + r, x - r, y + r * b, x - r, y)
curveTo(x - r, y - r * b, x - r * b, y - r, x, y - r)
curveTo(x + r * b, y - r, x + r, y - r * b, x + r, y)
}

/**
       * Adds a rectangle to the current path.
     
       * @param       x       x-coordinate of the starting point
      * * 
 * @param       y       y-coordinate of the starting point
      * * 
 * @param       w       width
      * * 
 * @param       h       height
 */

     fun rectangle(x:Float, y:Float, w:Float, h:Float) {
rectangle(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
}

/**
       * Adds a rectangle to the current path.
     
       * @param       x       x-coordinate of the starting point
      * * 
 * @param       y       y-coordinate of the starting point
      * * 
 * @param       w       width
      * * 
 * @param       h       height
 */

     fun rectangle(x:Double, y:Double, w:Double, h:Double) {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re").append_i(separator)
}

private fun compareColors(c1:BaseColor?, c2:BaseColor?):Boolean {
if (c1 == null && c2 == null)
return true
if (c1 == null || c2 == null)
return false
if (c1 is ExtendedColor)
return c1 == c2
return c2 == c1
}

/**
       * Adds a variable width border to the current path.
       * Only use if [Rectangle.isUseVariableBorders][com.itextpdf.text.Rectangle.isUseVariableBorders]
       * = true.
       * @param rect a Rectangle
 */
     fun variableRectangle(rect:Rectangle) {
val t = rect.top
val b = rect.bottom
val r = rect.right
val l = rect.left
val wt = rect.borderWidthTop
val wb = rect.borderWidthBottom
val wr = rect.borderWidthRight
val wl = rect.borderWidthLeft
val ct = rect.borderColorTop
val cb = rect.borderColorBottom
val cr = rect.borderColorRight
val cl = rect.borderColorLeft
saveState()
setLineCap(PdfContentByte.LINE_CAP_BUTT)
setLineJoin(PdfContentByte.LINE_JOIN_MITER)
var clw = 0f
var cdef = false
var ccol:BaseColor? = null
var cdefi = false
var cfil:BaseColor? = null
// draw top
        if (wt > 0)
{
setLineWidth(clw = wt)
cdef = true
if (ct == null)
resetRGBColorStroke()
else
setColorStroke(ct)
ccol = ct
moveTo(l, t - wt / 2f)
lineTo(r, t - wt / 2f)
stroke()
}

// Draw bottom
        if (wb > 0)
{
if (wb != clw)
setLineWidth(clw = wb)
if (!cdef || !compareColors(ccol, cb))
{
cdef = true
if (cb == null)
resetRGBColorStroke()
else
setColorStroke(cb)
ccol = cb
}
moveTo(r, b + wb / 2f)
lineTo(l, b + wb / 2f)
stroke()
}

// Draw right
        if (wr > 0)
{
if (wr != clw)
setLineWidth(clw = wr)
if (!cdef || !compareColors(ccol, cr))
{
cdef = true
if (cr == null)
resetRGBColorStroke()
else
setColorStroke(cr)
ccol = cr
}
val bt = compareColors(ct, cr)
val bb = compareColors(cb, cr)
moveTo(r - wr / 2f, if (bt) t else t - wt)
lineTo(r - wr / 2f, if (bb) b else b + wb)
stroke()
if (!bt || !bb)
{
cdefi = true
if (cr == null)
resetRGBColorFill()
else
setColorFill(cr)
cfil = cr
if (!bt)
{
moveTo(r, t)
lineTo(r, t - wt)
lineTo(r - wr, t - wt)
fill()
}
if (!bb)
{
moveTo(r, b)
lineTo(r, b + wb)
lineTo(r - wr, b + wb)
fill()
}
}
}

// Draw Left
        if (wl > 0)
{
if (wl != clw)
setLineWidth(wl)
if (!cdef || !compareColors(ccol, cl))
{
if (cl == null)
resetRGBColorStroke()
else
setColorStroke(cl)
}
val bt = compareColors(ct, cl)
val bb = compareColors(cb, cl)
moveTo(l + wl / 2f, if (bt) t else t - wt)
lineTo(l + wl / 2f, if (bb) b else b + wb)
stroke()
if (!bt || !bb)
{
if (!cdefi || !compareColors(cfil, cl))
{
if (cl == null)
resetRGBColorFill()
else
setColorFill(cl)
}
if (!bt)
{
moveTo(l, t)
lineTo(l, t - wt)
lineTo(l + wl, t - wt)
fill()
}
if (!bb)
{
moveTo(l, b)
lineTo(l, b + wb)
lineTo(l + wl, b + wb)
fill()
}
}
}
restoreState()
}

/**
       * Adds a border (complete or partially) to the current path..
     
       * @param       rectangle       a Rectangle
 */

     fun rectangle(rectangle:Rectangle) {
// the coordinates of the border are retrieved
        val x1 = rectangle.left
val y1 = rectangle.bottom
val x2 = rectangle.right
val y2 = rectangle.top

// the backgroundcolor is set
        val background = rectangle.backgroundColor
if (background != null)
{
saveState()
setColorFill(background)
rectangle(x1, y1, x2 - x1, y2 - y1)
fill()
restoreState()
}

// if the element hasn't got any borders, nothing is added
        if (!rectangle.hasBorders())
{
return 
}

// if any of the individual border colors are set
        // we draw the borders all around using the
        // different colors
        if (rectangle.isUseVariableBorders)
{
variableRectangle(rectangle)
}
else
{
// the width is set to the width of the element
            if (rectangle.borderWidth != Rectangle.UNDEFINED.toFloat())
{
setLineWidth(rectangle.borderWidth)
}

// the color is set to the color of the element
            val color = rectangle.borderColor
if (color != null)
{
setColorStroke(color)
}

// if the box is a rectangle, it is added as a rectangle
            if (rectangle.hasBorder(Rectangle.BOX))
{
rectangle(x1, y1, x2 - x1, y2 - y1)
}
else
{
if (rectangle.hasBorder(Rectangle.RIGHT))
{
moveTo(x2, y1)
lineTo(x2, y2)
}
if (rectangle.hasBorder(Rectangle.LEFT))
{
moveTo(x1, y1)
lineTo(x1, y2)
}
if (rectangle.hasBorder(Rectangle.BOTTOM))
{
moveTo(x1, y1)
lineTo(x2, y1)
}
if (rectangle.hasBorder(Rectangle.TOP))
{
moveTo(x1, y2)
lineTo(x2, y2)
}
}// if the border isn't a rectangle, the different sides are added apart

stroke()

if (color != null)
{
resetRGBColorStroke()
}
}
}

/**
       * Closes the current subpath by appending a straight line segment from the current point
       * to the starting point of the subpath.
      */

     fun closePath() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append("h").append_i(separator)
}

/**
       * Ends the path without filling or stroking it.
      */

     fun newPath() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
internalBuffer.append("n").append_i(separator)
}

/**
       * Strokes the path.
      */

     fun stroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("S").append_i(separator)
}

/**
       * Closes the path and strokes it.
      */

     fun closePathStroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("s").append_i(separator)
}

/**
       * Fills the path, using the non-zero winding number rule to determine the region to fill.
      */

     fun fill() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("f").append_i(separator)
}

/**
       * Fills the path, using the even-odd rule to determine the region to fill.
      */

     fun eoFill() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("f*").append_i(separator)
}

/**
       * Fills the path using the non-zero winding number rule to determine the region to fill and strokes it.
      */

     fun fillStroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("B").append_i(separator)
}

/**
       * Closes the path, fills it using the non-zero winding number rule to determine the region to fill and strokes it.
      */

     fun closePathFillStroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("b").append_i(separator)
}

/**
       * Fills the path, using the even-odd rule to determine the region to fill and strokes it.
      */

     fun eoFillStroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("B*").append_i(separator)
}

/**
       * Closes the path, fills it using the even-odd rule to determine the region to fill and strokes it.
      */

     fun closePathEoFillStroke() {
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("path.construction.operator.inside.text.object"))
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
internalBuffer.append("b*").append_i(separator)
}

/**
       * Adds an Image to the page. The Image must have
       * absolute positioning. The image can be placed inline.
       * @param image the Image object
      * * 
 * @param inlineImage true to place this image inline, false otherwise
      * * 
 * @throws DocumentException if the Image does not have absolute positioning
 */
    @Throws(DocumentException::class)
@JvmOverloads  fun addImage(image:Image, inlineImage:Boolean = false) {
if (!image.hasAbsoluteY())
throw DocumentException(MessageLocalization.getComposedMessage("the.image.must.have.absolute.positioning"))
val matrix = image.matrix()
matrix[Image.CX] = image.absoluteX - matrix[Image.CX]
matrix[Image.CY] = image.absoluteY - matrix[Image.CY]
addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5], inlineImage)
}

/**
       * Adds an Image to the page. The positioning of the Image
       * is done with the transformation matrix. To position an image at (x,y)
       * use addImage(image, image_width, 0, 0, image_height, x, y).
       * @param image the Image object
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @throws DocumentException on error
 */
    @Throws(DocumentException::class)
open fun addImage(image:Image, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float) {
addImage(image, a, b, c, d, e, f, false)
}

/**
       * adds an image with the given matrix.
       * @param image image to add
      * * 
 * @param transform transform to apply to the template prior to adding it.
 */
    @Throws(DocumentException::class)
 fun addImage(image:Image, transform:AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
addImage(image, matrix[0], matrix[1], matrix[2], 
matrix[3], matrix[4], matrix[5], false)
}

/**
       * Adds an Image to the page. The positioning of the Image
       * is done with the transformation matrix. To position an image at (x,y)
       * use addImage(image, image_width, 0, 0, image_height, x, y). The image can be placed inline.
       * @param image the Image object
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param inlineImage true to place this image inline, false otherwise
      * * 
 * @throws DocumentException on error
 */
    @Throws(DocumentException::class)
open fun addImage(image:Image, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float, inlineImage:Boolean) {
addImage(image, a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble(), f.toDouble(), inlineImage)
}

/**
       * Adds an Image to the page. The positioning of the Image
       * is done with the transformation matrix. To position an image at (x,y)
       * use addImage(image, image_width, 0, 0, image_height, x, y). The image can be placed inline.
       * @param image the Image object
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param inlineImage true to place this image inline, false otherwise
      * * 
 * @throws DocumentException on error
 */
    @Throws(DocumentException::class)
@JvmOverloads  fun addImage(image:Image, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double, inlineImage:Boolean = false) {
addImage(image, a, b, c, d, e, f, inlineImage, false)
}


/**
       * Adds an Image to the page. The positioning of the Image
       * is done with the transformation matrix. To position an image at (x,y)
       * The image can be placed inline.
       * @param image the Image object
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param inlineImage true to place this image inline, false otherwise
      * * 
 * @param isMCBlockOpened true not to open MCBlock, false otherwise
      * * 
 * @throws DocumentException on error
 */
    @Throws(DocumentException::class)
protected fun addImage(image:Image?, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double, inlineImage:Boolean, isMCBlockOpened:Boolean) {
try
{
val transform = AffineTransform(a, b, c, d, e, f)

if (image!!.layer != null)
beginLayer(image.layer)
if (isTagged)
{
if (inText)
endText()
val src = arrayOf(Point2D.Float(0, 0), Point2D.Float(1, 0), Point2D.Float(1, 1), Point2D.Float(0, 1))
val dst = arrayOfNulls<Point2D.Float>(4)
transform.transform(src, 0, dst, 0, 4)
var left = java.lang.Float.MAX_VALUE
var right = -java.lang.Float.MAX_VALUE
var bottom = java.lang.Float.MAX_VALUE
var top = -java.lang.Float.MAX_VALUE
for (i in 0..3)
{
if (dst[i].x < left)
left = dst[i].x as Float
if (dst[i].x > right)
right = dst[i].x as Float
if (dst[i].y < bottom)
bottom = dst[i].y as Float
if (dst[i].y > top)
top = dst[i].y as Float
}
image.setAccessibleAttribute(PdfName.BBOX, PdfArray(floatArrayOf(left, bottom, right, top)))
}
if (pdfWriter != null && image.isImgTemplate)
{
pdfWriter!!.addDirectImageSimple(image)
val template = image.templateData
if (image.getAccessibleAttributes() != null)
{
for (key in image.getAccessibleAttributes().keys)
{
template.setAccessibleAttribute(key, image.getAccessibleAttribute(key))
}
}
val w = template.width
val h = template.height
addTemplate(template, a / w, b / w, c / h, d / h, e, f, false, false)
}
else
{
internalBuffer.append("q ")

if (!transform.isIdentity)
{
internalBuffer.append(a).append(' ')
internalBuffer.append(b).append(' ')
internalBuffer.append(c).append(' ')
internalBuffer.append(d).append(' ')
internalBuffer.append(e).append(' ')
internalBuffer.append(f).append(" cm")
}

if (inlineImage)
{
internalBuffer.append("\nBI\n")
val pimage = PdfImage(image, "", null)
if (image is ImgJBIG2)
{
val globals = image.globalBytes
if (globals != null)
{
val decodeparms = PdfDictionary()
decodeparms.put(PdfName.JBIG2GLOBALS, pdfWriter!!.getReferenceJBIG2Globals(globals))
pimage.put(PdfName.DECODEPARMS, decodeparms)
}
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_INLINE_IMAGE, pimage)
for (element in pimage.keys)
{
var value:PdfObject = pimage.get(element)
val s = abrev[element] ?: continue
internalBuffer.append(s)
var check = true
if (element == PdfName.COLORSPACE && value.isArray)
{
val ar = value as PdfArray
if (ar.size() == 4 
&& PdfName.INDEXED == ar.getAsName(0) 
&& ar.getPdfObject(1).isName 
&& ar.getPdfObject(2).isNumber 
&& ar.getPdfObject(3).isString)
{
check = false
}

}
if (check && element == PdfName.COLORSPACE && !value.isName)
{
val cs = pdfWriter!!.colorspaceName
val prs = pageResources
prs.addColor(cs, pdfWriter!!.addToBody(value).indirectReference)
value = cs
}
value.toPdf(null, internalBuffer)
internalBuffer.append('\n')
}
val baos = ByteArrayOutputStream()
pimage.writeContent(baos)
val imageBytes = baos.toByteArray()
internalBuffer.append(String.format("/L %s\n", imageBytes.size))
/*
                    // The following restriction will be normative in PDF 2.0
                    if (imageBytes.length > 4096)
                    	throw new DocumentException("Inline images must be 4 KB or less");
                    */
                    internalBuffer.append("ID\n")
internalBuffer.append(imageBytes)
internalBuffer.append("\nEI\nQ").append_i(separator)
}
else
{
var name:PdfName
val prs = pageResources
val maskImage = image.imageMask
if (maskImage != null)
{
name = pdfWriter!!.addDirectImageSimple(maskImage)
prs.addXObject(name, pdfWriter!!.getImageReference(name))
}
name = pdfWriter!!.addDirectImageSimple(image)
name = prs.addXObject(name, pdfWriter!!.getImageReference(name))
internalBuffer.append(' ').append(name.bytes).append(" Do Q").append_i(separator)
}
}
if (image.hasBorders())
{
saveState()
val w = image.width
val h = image.height
concatCTM(a / w, b / w, c / h, d / h, e, f)
rectangle(image)
restoreState()
}
if (image.layer != null)
endLayer()
var annot:Annotation? = image.annotation ?: return
val r = DoubleArray(unitRect.size)
run{
            var k = 0
            while (k < unitRect.size) {
                r[k] = a * unitRect[k] + c * unitRect[k + 1] + e
                r[k + 1] = b * unitRect[k] + d * unitRect[k + 1] + f
                k += 2
            }
        }
var llx = r[0]
var lly = r[1]
var urx = llx
var ury = lly
var k = 2
while (k < r.size)
{
llx = Math.min(llx, r[k])
lly = Math.min(lly, r[k + 1])
urx = Math.max(urx, r[k])
ury = Math.max(ury, r[k + 1])
k += 2
}
annot = Annotation(annot)
annot.setDimensions(llx.toFloat(), lly.toFloat(), urx.toFloat(), ury.toFloat())
val an = PdfAnnotationsImp.convertAnnotation(pdfWriter, annot, Rectangle(llx.toFloat(), lly.toFloat(), urx.toFloat(), ury.toFloat())) ?: return
addAnnotation(an)
}
catch (ioe:IOException) {
val path = if (image != null && image.url != null)
image.url.path
else
MessageLocalization.getComposedMessage("unknown")
throw DocumentException(MessageLocalization.getComposedMessage("add.image.exception", path), ioe)
}

}

/**
       * Makes this PdfContentByte empty.
       * @param validateContent will call `sanityCheck()` if true.
      * * 
 * @since 2.1.6
 */
    @JvmOverloads  fun reset(validateContent:Boolean = true) {
internalBuffer.reset()
markedContentSize = 0
if (validateContent)
{
sanityCheck()
}
state = GraphicState()
stateList = ArrayList<GraphicState>()
}


/**
       * Starts the writing of text.
       * @param restoreTM indicates if to restore text matrix of the previous text block.
 */
    protected fun beginText(restoreTM:Boolean) {
if (inText)
{
if (isTagged)
{

}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.begin.end.text.operators"))
}
}
else
{
inText = true
internalBuffer.append("BT").append_i(separator)
if (restoreTM)
{
val xTLM = state.xTLM
val tx = state.tx
setTextMatrix(state.aTLM, state.bTLM, state.cTLM, state.dTLM, state.tx, state.yTLM)
state.xTLM = xTLM
state.tx = tx
}
else
{
state.xTLM = 0f
state.yTLM = 0f
state.tx = 0f
}
}
}

/**
       * Starts the writing of text.
      */
     fun beginText() {
beginText(false)
}

/**
       * Ends the writing of text and makes the current font invalid.
      */
     fun endText() {
if (!inText)
{
if (isTagged)
{

}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.begin.end.text.operators"))
}
}
else
{
inText = false
internalBuffer.append("ET").append_i(separator)
}
}

/**
       * Saves the graphic state. saveState and
       * restoreState must be balanced.
      */
     fun saveState() {
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_CANVAS, "q")
if (inText && isTagged)
{
endText()
}
internalBuffer.append("q").append_i(separator)
stateList.add(GraphicState(state))
}

/**
       * Restores the graphic state. saveState and
       * restoreState must be balanced.
      */
     fun restoreState() {
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_CANVAS, "Q")
if (inText && isTagged)
{
endText()
}
internalBuffer.append("Q").append_i(separator)
val idx = stateList.size - 1
if (idx < 0)
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.save.restore.state.operators"))
state.restore(stateList[idx])
stateList.removeAt(idx)
}

/**
       * Set the font and the size for the subsequent text writing.
     
       * @param bf the font
      * * 
 * @param size the font size in points
 */
    open fun setFontAndSize(bf:BaseFont, size:Float) {
if (!inText && isTagged)
{
beginText(true)
}
checkWriter()
if (size < 0.0001f && size > -0.0001f)
throw IllegalArgumentException(MessageLocalization.getComposedMessage("font.size.too.small.1", size.toString()))
state.size = size
state.fontDetails = pdfWriter!!.addSimple(bf)
val prs = pageResources
var name = state.fontDetails!!.fontName
name = prs.addFont(name, state.fontDetails!!.indirectReference)
internalBuffer.append(name.bytes).append(' ').append(size).append(" Tf").append_i(separator)
}

/**
       * Sets the text rendering parameter.
     
       * @param       rendering               a parameter
 */
     fun setTextRenderingMode(rendering:Int) {
if (!inText && isTagged)
{
beginText(true)
}
state.textRenderMode = rendering
internalBuffer.append(rendering).append(" Tr").append_i(separator)
}

/**
       * Sets the text rise parameter.
       * 
       * This allows to write text in subscript or superscript mode.
     
       * @param       rise                a parameter
 */
     fun setTextRise(rise:Float) {
setTextRise(rise.toDouble())
}

/**
       * Sets the text rise parameter.
       * 
       * This allows to write text in subscript or superscript mode.
     
       * @param       rise                a parameter
 */
     fun setTextRise(rise:Double) {
if (!inText && isTagged)
{
beginText(true)
}
internalBuffer.append(rise).append(" Ts").append_i(separator)
}

/**
       * A helper to insert into the content stream the text
       * converted to bytes according to the font's encoding.
     
       * @param text the text to write
 */
    private fun showText2(text:String) {
if (state.fontDetails == null)
throw NullPointerException(MessageLocalization.getComposedMessage("font.and.size.must.be.set.before.writing.any.text"))
val b = state.fontDetails!!.convertToBytes(text)
StringUtils.escapeString(b, internalBuffer)
}

/**
       * Shows the text.
     
       * @param text the text to write
 */
     fun showText(text:String) {
checkState()
if (!inText && isTagged)
{
beginText(true)
}
showText2(text)
updateTx(text, 0f)
internalBuffer.append("Tj").append_i(separator)
}

 fun showTextGid(gids:String) {
checkState()
if (!inText && isTagged)
{
beginText(true)
}
if (state.fontDetails == null)
throw NullPointerException(MessageLocalization.getComposedMessage("font.and.size.must.be.set.before.writing.any.text"))
val objs = state.fontDetails!!.convertToBytesGid(gids)
StringUtils.escapeString(objs[0] as ByteArray, internalBuffer)
state.tx += (objs[2] as Int).toInt().toFloat() * 0.001f * state.size
internalBuffer.append("Tj").append_i(separator)
}

/**
       * Shows the text kerned.
     
       * @param text the text to write
 */
     fun showTextKerned(text:String) {
if (state.fontDetails == null)
throw NullPointerException(MessageLocalization.getComposedMessage("font.and.size.must.be.set.before.writing.any.text"))
val bf = state.fontDetails!!.baseFont
if (bf.hasKernPairs())
showText(getKernArray(text, bf))
else
{
showText(text)
}
}

/**
       * Moves to the next line and shows text.
     
       * @param text the text to write
 */
     fun newlineShowText(text:String) {
checkState()
if (!inText && isTagged)
{
beginText(true)
}
state.yTLM -= state.leading
showText2(text)
internalBuffer.append("'").append_i(separator)
state.tx = state.xTLM
updateTx(text, 0f)
}

/**
       * Moves to the next line and shows text string, using the given values of the character and word spacing parameters.
     
       * @param       wordSpacing     a parameter
      * * 
 * @param       charSpacing     a parameter
      * * 
 * @param text the text to write
 */
     fun newlineShowText(wordSpacing:Float, charSpacing:Float, text:String) {
checkState()
if (!inText && isTagged)
{
beginText(true)
}
state.yTLM -= state.leading
internalBuffer.append(wordSpacing).append(' ').append(charSpacing)
showText2(text)
internalBuffer.append("\"").append_i(separator)
// The " operator sets charSpace and wordSpace into graphics state
        // (cfr PDF reference v1.6, table 5.6)
        state.charSpace = charSpacing
state.wordSpace = wordSpacing
state.tx = state.xTLM
updateTx(text, 0f)
}

/**
       * Changes the text matrix.
       * 
       * Remark: this operation also initializes the current point position.
     
       * @param       a           operand 1,1 in the matrix
      * * 
 * @param       b           operand 1,2 in the matrix
      * * 
 * @param       c           operand 2,1 in the matrix
      * * 
 * @param       d           operand 2,2 in the matrix
      * * 
 * @param       x           operand 3,1 in the matrix
      * * 
 * @param       y           operand 3,2 in the matrix
 */
     fun setTextMatrix(a:Float, b:Float, c:Float, d:Float, x:Float, y:Float) {
if (!inText && isTagged)
{
beginText(true)
}
state.xTLM = x
state.yTLM = y
state.aTLM = a
state.bTLM = b
state.cTLM = c
state.dTLM = d
state.tx = state.xTLM
internalBuffer.append(a).append(' ').append(b).append_i(' ').append(c).append_i(' ').append(d).append_i(' ').append(x).append_i(' ').append(y).append(" Tm").append_i(separator)
}

/**
       * Changes the text matrix.
       * 
       * @param transform overwrite the current text matrix with this one
 */
     fun setTextMatrix(transform:AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
setTextMatrix(matrix[0].toFloat(), matrix[1].toFloat(), matrix[2].toFloat(), 
matrix[3].toFloat(), matrix[4].toFloat(), matrix[5].toFloat())
}

/**
       * Changes the text matrix. The first four parameters are {1,0,0,1}.
       * 
       * Remark: this operation also initializes the current point position.
     
       * @param       x           operand 3,1 in the matrix
      * * 
 * @param       y           operand 3,2 in the matrix
 */
     fun setTextMatrix(x:Float, y:Float) {
setTextMatrix(1f, 0f, 0f, 1f, x, y)
}

/**
       * Moves to the start of the next line, offset from the start of the current line.
     
       * @param       x           x-coordinate of the new current point
      * * 
 * @param       y           y-coordinate of the new current point
 */
     fun moveText(x:Float, y:Float) {
if (!inText && isTagged)
{
beginText(true)
}
state.xTLM += x
state.yTLM += y
if (isTagged && state.xTLM != state.tx)
{
setTextMatrix(state.aTLM, state.bTLM, state.cTLM, state.dTLM, state.xTLM, state.yTLM)
}
else
{
internalBuffer.append(x).append(' ').append(y).append(" Td").append_i(separator)
}
}

/**
       * Moves to the start of the next line, offset from the start of the current line.
       * 
       * As a side effect, this sets the leading parameter in the text state.
     
       * @param       x           offset of the new current point
      * * 
 * @param       y           y-coordinate of the new current point
 */
     fun moveTextWithLeading(x:Float, y:Float) {
if (!inText && isTagged)
{
beginText(true)
}
state.xTLM += x
state.yTLM += y
state.leading = -y
if (isTagged && state.xTLM != state.tx)
{
setTextMatrix(state.aTLM, state.bTLM, state.cTLM, state.dTLM, state.xTLM, state.yTLM)
}
else
{
internalBuffer.append(x).append(' ').append(y).append(" TD").append_i(separator)
}
}

/**
       * Moves to the start of the next line.
      */
     fun newlineText() {
if (!inText && isTagged)
{
beginText(true)
}
if (isTagged && state.xTLM != state.tx)
{
setTextMatrix(state.aTLM, state.bTLM, state.cTLM, state.dTLM, state.xTLM, state.yTLM)
}
state.yTLM -= state.leading
internalBuffer.append("T*").append_i(separator)
}

@JvmOverloads internal fun size(includeMarkedContentSize:Boolean = true):Int {
if (includeMarkedContentSize)
return internalBuffer.size()
else
return internalBuffer.size() - markedContentSize
}

/**
       * Adds a named outline to the document.
     
       * @param outline the outline
      * * 
 * @param name the name for the local destination
 */
     fun addOutline(outline:PdfOutline, name:String) {
checkWriter()
pdfDocument.addOutline(outline, name)
}
/**
       * Gets the root outline.
     
       * @return the root outline
 */
     val rootOutline:PdfOutline
get() {
checkWriter()
return pdfDocument.rootOutline
}

/**
       * Computes the width of the given string taking in account
       * the current values of "Character spacing", "Word Spacing"
       * and "Horizontal Scaling".
       * The additional spacing is not computed for the last character
       * of the string.
       * @param text the string to get width of
      * * 
 * @param kerned the kerning option
      * * 
 * @return the width
 */
     fun getEffectiveStringWidth(text:String, kerned:Boolean):Float {
val bf = state.fontDetails!!.baseFont

var w:Float
if (kerned)
w = bf.getWidthPointKerned(text, state.size)
else
w = bf.getWidthPoint(text, state.size)

if (state.charSpace != 0.0f && text.length > 1)
{
w += state.charSpace * (text.length - 1)
}

if (state.wordSpace != 0.0f && !bf.isVertical)
{
for (i in 0..text.length - 1 - 1)
{
if (text[i] == ' ')
w += state.wordSpace
}
}
if (state.scale.toDouble() != 100.0)
w = w * state.scale / 100.0f

//System.out.println("String width = " + Float.toString(w));
        return w
}

/**
       * Computes the width of the given string taking in account
       * the current values of "Character spacing", "Word Spacing"
       * and "Horizontal Scaling".
       * The spacing for the last character is also computed.
       * It also takes into account kerning that can be specified within TJ operator (e.g. [(Hello) 123 (World)] TJ)
       * @param text the string to get width of
      * * 
 * @param kerned the kerning option
      * * 
 * @param kerning the kerning option from TJ array
      * * 
 * @return the width
 */
    private fun getEffectiveStringWidth(text:String, kerned:Boolean, kerning:Float):Float {
val bf = state.fontDetails!!.baseFont
var w:Float
if (kerned)
w = bf.getWidthPointKerned(text, state.size)
else
w = bf.getWidthPoint(text, state.size)
if (state.charSpace != 0.0f && text.length > 0)
{
w += state.charSpace * text.length
}
if (state.wordSpace != 0.0f && !bf.isVertical)
{
for (i in 0..text.length - 1)
{
if (text[i] == ' ')
w += state.wordSpace
}
}
w -= kerning / 1000.f * state.size
if (state.scale.toDouble() != 100.0)
w = w * state.scale / 100.0f
return w
}

/**
       * Shows text right, left or center aligned with rotation.
       * @param alignment the alignment can be ALIGN_CENTER, ALIGN_RIGHT or ALIGN_LEFT
      * * 
 * @param text the text to show
      * * 
 * @param x the x pivot position
      * * 
 * @param y the y pivot position
      * * 
 * @param rotation the rotation to be applied in degrees counterclockwise
 */
     fun showTextAligned(alignment:Int, text:String, x:Float, y:Float, rotation:Float) {
showTextAligned(alignment, text, x, y, rotation, false)
}

private fun showTextAligned(alignment:Int, text:String, x:Float, y:Float, rotation:Float, kerned:Boolean) {
var x = x
var y = y
if (state.fontDetails == null)
throw NullPointerException(MessageLocalization.getComposedMessage("font.and.size.must.be.set.before.writing.any.text"))
if (rotation == 0f)
{
when (alignment) {
ALIGN_CENTER -> x -= getEffectiveStringWidth(text, kerned) / 2
ALIGN_RIGHT -> x -= getEffectiveStringWidth(text, kerned)
}
setTextMatrix(x, y)
if (kerned)
showTextKerned(text)
else
showText(text)
}
else
{
val alpha = rotation * Math.PI / 180.0
val cos = Math.cos(alpha).toFloat()
val sin = Math.sin(alpha).toFloat()
val len:Float
when (alignment) {
ALIGN_CENTER -> {
len = getEffectiveStringWidth(text, kerned) / 2
x -= len * cos
y -= len * sin
}
ALIGN_RIGHT -> {
len = getEffectiveStringWidth(text, kerned)
x -= len * cos
y -= len * sin
}
}
setTextMatrix(cos, sin, -sin, cos, x, y)
if (kerned)
showTextKerned(text)
else
showText(text)
setTextMatrix(0f, 0f)
}
}

/**
       * Shows text kerned right, left or center aligned with rotation.
       * @param alignment the alignment can be ALIGN_CENTER, ALIGN_RIGHT or ALIGN_LEFT
      * * 
 * @param text the text to show
      * * 
 * @param x the x pivot position
      * * 
 * @param y the y pivot position
      * * 
 * @param rotation the rotation to be applied in degrees counterclockwise
 */
     fun showTextAlignedKerned(alignment:Int, text:String, x:Float, y:Float, rotation:Float) {
showTextAligned(alignment, text, x, y, rotation, true)
}

/**
       * Concatenate a matrix to the current transformation matrix.
     
       * Common transformations:
     
       * 
         *  * Translation: [1 0 0 1 tx ty]
         *  * Scaling: [sx 0 0 sy 0 0] (if sx or sy is negative, it will flip the coordinate system)
         *  * Rotation: [cos(q) sin(q) -sin(q) cos(q) 0 0] where q is angle of counter-clockwise rotation (rotated around positive z-axis - use Right Hand Rule)
           * 
               *  * Rotate 90 degrees CCW: [0 1 -1 0 0 0]
      		    *  * Rotate 180 degrees: [-1 0 0 -1 0 0]
     		    *  * Rotate 270 degrees: [0 -1 1 0 0 0]
         * 
         *  * Skew: [1 tan(a) tan(b) 1 0 0] where a is x-axis skew angle and b is y-axis skew angle
	  * 
     
       * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
 */
     fun concatCTM(a:Float, b:Float, c:Float, d:Float, e:Float, f:Float) {
concatCTM(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble(), f.toDouble())
}

/**
       * Concatenate a matrix to the current transformation matrix.
     
       * Common transformations:
     
       * 
         *  * Translation: [1 0 0 1 tx ty]
         *  * Scaling: [sx 0 0 sy 0 0] (if sx or sy is negative, it will flip the coordinate system)
         *  * Rotation: [cos(q) sin(q) -sin(q) cos(q) 0 0] where q is angle of counter-clockwise rotation (rotated around positive z-axis - use Right Hand Rule)
           * 
               *  * Rotate 90 degrees CCW: [0 1 -1 0 0 0]
      		    *  * Rotate 180 degrees: [-1 0 0 -1 0 0]
     		    *  * Rotate 270 degrees: [0 -1 1 0 0 0]
         * 
         *  * Skew: [1 tan(a) tan(b) 1 0 0] where a is x-axis skew angle and b is y-axis skew angle
	  * 
     
       * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
 */
     fun concatCTM(a:Double, b:Double, c:Double, d:Double, e:Double, f:Double) {
if (inText && isTagged)
{
endText()
}
state.CTM.concatenate(AffineTransform(a, b, c, d, e, f))
internalBuffer.append(a).append(' ').append(b).append(' ').append(c).append(' ')
internalBuffer.append(d).append(' ').append(e).append(' ').append(f).append(" cm").append_i(separator)
}

/**
       * Concatenate a matrix to the current transformation matrix.
       * @param transform added to the Current Transformation Matrix
 */
     fun concatCTM(transform:AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
concatCTM(matrix[0], matrix[1], matrix[2], 
matrix[3], matrix[4], matrix[5])
}

/**
       * Draws a partial ellipse inscribed within the rectangle x1,y1,x2,y2,
       * starting at startAng degrees and covering extent degrees. Angles
       * start with 0 to the right (+x) and increase counter-clockwise.
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
      * * 
 * @param startAng starting angle in degrees
      * * 
 * @param extent angle extent in degrees
 */
     fun arc(x1:Float, y1:Float, x2:Float, y2:Float, startAng:Float, extent:Float) {
arc(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), startAng.toDouble(), extent.toDouble())
}

/**
       * Draws a partial ellipse inscribed within the rectangle x1,y1,x2,y2,
       * starting at startAng degrees and covering extent degrees. Angles
       * start with 0 to the right (+x) and increase counter-clockwise.
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
      * * 
 * @param startAng starting angle in degrees
      * * 
 * @param extent angle extent in degrees
 */
     fun arc(x1:Double, y1:Double, x2:Double, y2:Double, startAng:Double, extent:Double) {
val ar = bezierArc(x1, y1, x2, y2, startAng, extent)
if (ar.isEmpty())
return 
var pt = ar[0]
moveTo(pt[0], pt[1])
for (k in ar.indices)
{
pt = ar[k]
curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7])
}
}

/**
       * Draws an ellipse inscribed within the rectangle x1,y1,x2,y2.
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
 */
     fun ellipse(x1:Float, y1:Float, x2:Float, y2:Float) {
ellipse(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
}

/**
       * Draws an ellipse inscribed within the rectangle x1,y1,x2,y2.
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
 */
     fun ellipse(x1:Double, y1:Double, x2:Double, y2:Double) {
arc(x1, y1, x2, y2, 0.0, 360.0)
}

/**
       * Create a new colored tiling pattern.
     
       * @param width the width of the pattern
      * * 
 * @param height the height of the pattern
      * * 
 * @param xstep the desired horizontal spacing between pattern cells.
      * * May be either positive or negative, but not zero.
      * * 
 * @param ystep the desired vertical spacing between pattern cells.
      * * May be either positive or negative, but not zero.
      * * 
 * @return the PdfPatternPainter where the pattern will be created
 */
    @JvmOverloads  fun createPattern(width:Float, height:Float, xstep:Float = width, ystep:Float = height):PdfPatternPainter {
checkWriter()
if (xstep == 0.0f || ystep == 0.0f)
throw RuntimeException(MessageLocalization.getComposedMessage("xstep.or.ystep.can.not.be.zero"))
val painter = PdfPatternPainter(pdfWriter)
painter.width = width
painter.height = height
painter.xStep = xstep
painter.yStep = ystep
pdfWriter!!.addSimplePattern(painter)
return painter
}

/**
       * Create a new uncolored tiling pattern.
     
       * @param width the width of the pattern
      * * 
 * @param height the height of the pattern
      * * 
 * @param xstep the desired horizontal spacing between pattern cells.
      * * May be either positive or negative, but not zero.
      * * 
 * @param ystep the desired vertical spacing between pattern cells.
      * * May be either positive or negative, but not zero.
      * * 
 * @param color the default color. Can be null
      * * 
 * @return the PdfPatternPainter where the pattern will be created
 */
     fun createPattern(width:Float, height:Float, xstep:Float, ystep:Float, color:BaseColor):PdfPatternPainter {
checkWriter()
if (xstep == 0.0f || ystep == 0.0f)
throw RuntimeException(MessageLocalization.getComposedMessage("xstep.or.ystep.can.not.be.zero"))
val painter = PdfPatternPainter(pdfWriter, color)
painter.width = width
painter.height = height
painter.xStep = xstep
painter.yStep = ystep
pdfWriter!!.addSimplePattern(painter)
return painter
}

/**
       * Create a new uncolored tiling pattern.
       * Variables xstep and ystep are set to the same values
       * of width and height.
       * @param width the width of the pattern
      * * 
 * @param height the height of the pattern
      * * 
 * @param color the default color. Can be null
      * * 
 * @return the PdfPatternPainter where the pattern will be created
 */
     fun createPattern(width:Float, height:Float, color:BaseColor):PdfPatternPainter {
return createPattern(width, height, width, height, color)
}

/**
       * Creates a new template.
       * 
       * Creates a new template that is nothing more than a form XObject. This template can be included
       * in this PdfContentByte or in another template. Templates are only written
       * to the output when the document is closed permitting things like showing text in the first page
       * that is only defined in the last page.
     
       * @param width the bounding box width
      * * 
 * @param height the bounding box height
      * * 
 * @return the created template
 */
     fun createTemplate(width:Float, height:Float):PdfTemplate {
return createTemplate(width, height, null)
}

internal fun createTemplate(width:Float, height:Float, forcedName:PdfName?):PdfTemplate {
checkWriter()
val template = PdfTemplate(pdfWriter)
template.width = width
template.height = height
pdfWriter!!.addDirectTemplateSimple(template, forcedName)
return template
}

/**
       * Creates a new appearance to be used with form fields.
     
       * @param width the bounding box width
      * * 
 * @param height the bounding box height
      * * 
 * @return the appearance created
 */
     fun createAppearance(width:Float, height:Float):PdfAppearance {
return createAppearance(width, height, null)
}

internal fun createAppearance(width:Float, height:Float, forcedName:PdfName?):PdfAppearance {
checkWriter()
val template = PdfAppearance(pdfWriter)
template.width = width
template.height = height
pdfWriter!!.addDirectTemplateSimple(template, forcedName)
return template
}

/**
       * Adds a PostScript XObject to this content.
     
       * @param psobject the object
 */
     fun addPSXObject(psobject:PdfPSXObject) {
if (inText && isTagged)
{
endText()
}
checkWriter()
var name = pdfWriter!!.addDirectTemplateSimple(psobject, null)
val prs = pageResources
name = prs.addXObject(name, psobject.indirectReference)
internalBuffer.append(name.bytes).append(" Do").append_i(separator)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
 */
    open fun addTemplate(template:PdfTemplate, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float) {
addTemplate(template, a, b, c, d, e, f, false)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param tagContent `true` - template content will be tagged(all that will be added after), `false` - only a Do operator will be tagged.
      * *                   taken into account only if `isTagged()` - `true`.
 */
     fun addTemplate(template:PdfTemplate, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float, tagContent:Boolean) {
addTemplate(template, a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble(), f.toDouble(), tagContent)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param tagContent `true` - template content will be tagged(all that will be added after), `false` - only a Do operator will be tagged.
      * *                   taken into account only if `isTagged()` - `true`.
 */
    @JvmOverloads  fun addTemplate(template:PdfTemplate, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double, tagContent:Boolean = false) {
addTemplate(template, a, b, c, d, e, f, true, tagContent)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @param tagTemplate defines if template is to be tagged; `true` by default, `false` used when template is a part of `ImgTemplate`.
      * * 
 * @param tagContent `true` - template content will be tagged(all that will be added after), `false` - only a Do operator will be tagged.
      * *                   taken into account only if `isTagged()` and `tagTemplate` parameter - both `true`.
 */
    private fun addTemplate(template:PdfTemplate, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double, tagTemplate:Boolean, tagContent:Boolean) {
checkWriter()
checkNoPattern(template)
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_FORM_XOBJ, template)
var name = pdfWriter!!.addDirectTemplateSimple(template, null)
val prs = pageResources
name = prs.addXObject(name, template.indirectReference)
if (isTagged && tagTemplate)
{
if (inText)
endText()
if (template.isContentTagged || template.pageReference != null && tagContent)
{
throw RuntimeException(MessageLocalization.getComposedMessage("template.with.tagged.could.not.be.used.more.than.once"))
}

template.pageReference = pdfWriter!!.currentPage

if (tagContent)
{
template.isContentTagged = true
ensureDocumentTagIsOpen()
val allMcElements = getMcElements()
if (allMcElements != null && allMcElements.size > 0)
template.mcElements!!.add(allMcElements[allMcElements.size - 1])
}
else
{
openMCBlock(template)
}
}

internalBuffer.append("q ")
internalBuffer.append(a).append(' ')
internalBuffer.append(b).append(' ')
internalBuffer.append(c).append(' ')
internalBuffer.append(d).append(' ')
internalBuffer.append(e).append(' ')
internalBuffer.append(f).append(" cm ")
internalBuffer.append(name.bytes).append(" Do Q").append_i(separator)

if (isTagged && tagTemplate && !tagContent)
{
closeMCBlock(template)
template.setId(null)
}
}

/**
       * Adds a form XObject to this content.
     
       * @param formXObj the form XObject
      * * 
 * @param name the name of form XObject in content stream. The name is changed, if if it already exists in page resources
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * *
      * * 
 * @return Name under which XObject was stored in resources. See `name` parameter
 */
    @Throws(IOException::class)
 fun addFormXObj(formXObj:PdfStream, name:PdfName, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float):PdfName {
return addFormXObj(formXObj, name, a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble(), f.toDouble())
}

/**
       * Adds a form XObject to this content.
     
       * @param formXObj the form XObject
      * * 
 * @param name the name of form XObject in content stream. The name is changed, if if it already exists in page resources
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * *
      * * 
 * @return Name under which XObject was stored in resources. See `name` parameter
 */
    @Throws(IOException::class)
 fun addFormXObj(formXObj:PdfStream, name:PdfName, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double):PdfName {
checkWriter()
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_STREAM, formXObj)
val prs = pageResources
val translatedName = prs.addXObject(name, pdfWriter!!.addToBody(formXObj).indirectReference)
var artifact:PdfArtifact? = null
if (isTagged)
{
if (inText)
endText()
artifact = PdfArtifact()
openMCBlock(artifact)
}

internalBuffer.append("q ")
internalBuffer.append(a).append(' ')
internalBuffer.append(b).append(' ')
internalBuffer.append(c).append(' ')
internalBuffer.append(d).append(' ')
internalBuffer.append(e).append(' ')
internalBuffer.append(f).append(" cm ")
internalBuffer.append(translatedName.bytes).append(" Do Q").append_i(separator)

if (isTagged)
{
closeMCBlock(artifact)
}

return translatedName
}

/**
       * adds a template with the given matrix.
       * @param template template to add
      * * 
 * @param transform transform to apply to the template prior to adding it.
      * * 
 * @param tagContent `true` - template content will be tagged(all that will be added after), `false` - only a Do operator will be tagged.
      * *                   taken into account only if `isTagged()` - `true`.
 */
    @JvmOverloads  fun addTemplate(template:PdfTemplate, transform:AffineTransform, tagContent:Boolean = false) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
addTemplate(template, matrix[0], matrix[1], matrix[2], 
matrix[3], matrix[4], matrix[5], tagContent)
}

internal fun addTemplateReference(template:PdfIndirectReference, name:PdfName, a:Float, b:Float, c:Float, d:Float, e:Float, f:Float) {
addTemplateReference(template, name, a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble(), f.toDouble())
}

internal fun addTemplateReference(template:PdfIndirectReference, name:PdfName, a:Double, b:Double, c:Double, d:Double, e:Double, f:Double) {
var name = name
if (inText && isTagged)
{
endText()
}
checkWriter()
val prs = pageResources
name = prs.addXObject(name, template)
internalBuffer.append("q ")
internalBuffer.append(a).append(' ')
internalBuffer.append(b).append(' ')
internalBuffer.append(c).append(' ')
internalBuffer.append(d).append(' ')
internalBuffer.append(e).append(' ')
internalBuffer.append(f).append(" cm ")
internalBuffer.append(name.bytes).append(" Do Q").append_i(separator)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param x the x location of this template
      * * 
 * @param y the y location of this template
 */
     fun addTemplate(template:PdfTemplate, x:Float, y:Float) {
addTemplate(template, 1f, 0f, 0f, 1f, x, y)
}

/**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param x the x location of this template
      * * 
 * @param y the y location of this template
 */
     fun addTemplate(template:PdfTemplate, x:Double, y:Double) {
addTemplate(template, 1.0, 0.0, 0.0, 1.0, x, y)
}

 fun addTemplate(template:PdfTemplate, x:Float, y:Float, tagContent:Boolean) {
addTemplate(template, 1f, 0f, 0f, 1f, x, y, tagContent)
}

 fun addTemplate(template:PdfTemplate, x:Double, y:Double, tagContent:Boolean) {
addTemplate(template, 1.0, 0.0, 0.0, 1.0, x, y, tagContent)
}

/**
       * Changes the current color for filling paths (device dependent colors!).
       * 
       * Sets the color space to DeviceCMYK (or the DefaultCMYK color space),
       * and sets the color to use for filling paths.
       * 
       * This method is described in the 'Portable Document Format Reference Manual version 1.3'
       * section 8.5.2.1 (page 331).
       * 
       * Following the PDF manual, each operand must be a number between 0 (no ink) and
       * 1 (maximum ink). This method however accepts only integers between 0x00 and 0xFF.
     
       * @param cyan the intensity of cyan
      * * 
 * @param magenta the intensity of magenta
      * * 
 * @param yellow the intensity of yellow
      * * 
 * @param black the intensity of black
 */

    open fun setCMYKColorFill(cyan:Int, magenta:Int, yellow:Int, black:Int) {
saveColor(CMYKColor(cyan, magenta, yellow, black), true)
internalBuffer.append((cyan and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((magenta and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((yellow and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((black and 0xFF).toFloat() / 0xFF)
internalBuffer.append(" k").append_i(separator)
}
/**
       * Changes the current color for stroking paths (device dependent colors!).
       * 
       * Sets the color space to DeviceCMYK (or the DefaultCMYK color space),
       * and sets the color to use for stroking paths.
       * 
       * This method is described in the 'Portable Document Format Reference Manual version 1.3'
       * section 8.5.2.1 (page 331).
       * Following the PDF manual, each operand must be a number between 0 (minimum intensity) and
       * 1 (maximum intensity). This method however accepts only integers between 0x00 and 0xFF.
     
       * @param cyan the intensity of red
      * * 
 * @param magenta the intensity of green
      * * 
 * @param yellow the intensity of blue
      * * 
 * @param black the intensity of black
 */

    open fun setCMYKColorStroke(cyan:Int, magenta:Int, yellow:Int, black:Int) {
saveColor(CMYKColor(cyan, magenta, yellow, black), false)
internalBuffer.append((cyan and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((magenta and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((yellow and 0xFF).toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append((black and 0xFF).toFloat() / 0xFF)
internalBuffer.append(" K").append_i(separator)
}

/**
       * Changes the current color for filling paths (device dependent colors!).
       * 
       * Sets the color space to DeviceRGB (or the DefaultRGB color space),
       * and sets the color to use for filling paths.
       * 
       * This method is described in the 'Portable Document Format Reference Manual version 1.3'
       * section 8.5.2.1 (page 331).
       * 
       * Following the PDF manual, each operand must be a number between 0 (minimum intensity) and
       * 1 (maximum intensity). This method however accepts only integers between 0x00 and 0xFF.
     
       * @param red the intensity of red
      * * 
 * @param green the intensity of green
      * * 
 * @param blue the intensity of blue
 */

    open fun setRGBColorFill(red:Int, green:Int, blue:Int) {
saveColor(BaseColor(red, green, blue), true)
HelperRGB((red and 0xFF).toFloat() / 0xFF, (green and 0xFF).toFloat() / 0xFF, (blue and 0xFF).toFloat() / 0xFF)
internalBuffer.append(" rg").append_i(separator)
}

/**
       * Changes the current color for stroking paths (device dependent colors!).
       * 
       * Sets the color space to DeviceRGB (or the DefaultRGB color space),
       * and sets the color to use for stroking paths.
       * 
       * This method is described in the 'Portable Document Format Reference Manual version 1.3'
       * section 8.5.2.1 (page 331).
       * Following the PDF manual, each operand must be a number between 0 (minimum intensity) and
       * 1 (maximum intensity). This method however accepts only integers between 0x00 and 0xFF.
     
       * @param red the intensity of red
      * * 
 * @param green the intensity of green
      * * 
 * @param blue the intensity of blue
 */

    open fun setRGBColorStroke(red:Int, green:Int, blue:Int) {
saveColor(BaseColor(red, green, blue), false)
HelperRGB((red and 0xFF).toFloat() / 0xFF, (green and 0xFF).toFloat() / 0xFF, (blue and 0xFF).toFloat() / 0xFF)
internalBuffer.append(" RG").append_i(separator)
}

/** Sets the stroke color. color can be an
       * ExtendedColor.
       * @param color the color
 */
    open fun setColorStroke(color:BaseColor) {
val type = ExtendedColor.getType(color)
when (type) {
ExtendedColor.TYPE_GRAY -> {
setGrayStroke((color as GrayColor).gray)
}
ExtendedColor.TYPE_CMYK -> {
val cmyk = color as CMYKColor
setCMYKColorStrokeF(cmyk.cyan, cmyk.magenta, cmyk.yellow, cmyk.black)
}
ExtendedColor.TYPE_SEPARATION -> {
val spot = color as SpotColor
setColorStroke(spot.pdfSpotColor, spot.tint)
}
ExtendedColor.TYPE_PATTERN -> {
val pat = color as PatternColor
setPatternStroke(pat.painter)
}
ExtendedColor.TYPE_SHADING -> {
val shading = color as ShadingColor
setShadingStroke(shading.pdfShadingPattern)
}
ExtendedColor.TYPE_DEVICEN -> {
val devicen = color as DeviceNColor
setColorStroke(devicen.pdfDeviceNColor, devicen.tints)
}
ExtendedColor.TYPE_LAB -> {
val lab = color as LabColor
setColorStroke(lab.labColorSpace, lab.l, lab.a, lab.b)
}
else -> setRGBColorStroke(color.red, color.green, color.blue)
}

val alpha = color.alpha
if (alpha < 255)
{
val gState = PdfGState()
gState.setStrokeOpacity(alpha / 255f)
setGState(gState)
}
}

/** Sets the fill color. color can be an
       * ExtendedColor.
       * @param color the color
 */
    open fun setColorFill(color:BaseColor) {
val type = ExtendedColor.getType(color)
when (type) {
ExtendedColor.TYPE_GRAY -> {
setGrayFill((color as GrayColor).gray)
}
ExtendedColor.TYPE_CMYK -> {
val cmyk = color as CMYKColor
setCMYKColorFillF(cmyk.cyan, cmyk.magenta, cmyk.yellow, cmyk.black)
}
ExtendedColor.TYPE_SEPARATION -> {
val spot = color as SpotColor
setColorFill(spot.pdfSpotColor, spot.tint)
}
ExtendedColor.TYPE_PATTERN -> {
val pat = color as PatternColor
setPatternFill(pat.painter)
}
ExtendedColor.TYPE_SHADING -> {
val shading = color as ShadingColor
setShadingFill(shading.pdfShadingPattern)
}
ExtendedColor.TYPE_DEVICEN -> {
val devicen = color as DeviceNColor
setColorFill(devicen.pdfDeviceNColor, devicen.tints)
}
ExtendedColor.TYPE_LAB -> {
val lab = color as LabColor
setColorFill(lab.labColorSpace, lab.l, lab.a, lab.b)
}
else -> setRGBColorFill(color.red, color.green, color.blue)
}

val alpha = color.alpha
if (alpha < 255)
{
val gState = PdfGState()
gState.setFillOpacity(alpha / 255f)
setGState(gState)
}
}

/** Sets the fill color to a spot color.
       * @param sp the spot color
      * * 
 * @param tint the tint for the spot color. 0 is no color and 1
      * * is 100% color
 */
    open fun setColorFill(sp:PdfSpotColor, tint:Float) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(sp)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(SpotColor(sp, tint), true)
internalBuffer.append(name.bytes).append(" cs ").append(tint).append(" scn").append_i(separator)
}

 fun setColorFill(dn:PdfDeviceNColor, tints:FloatArray) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(dn)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(DeviceNColor(dn, tints), true)
internalBuffer.append(name.bytes).append(" cs ")
for (tint in tints)
internalBuffer.append(tint + " ")
internalBuffer.append("scn").append_i(separator)
}

 fun setColorFill(lab:PdfLabColor, l:Float, a:Float, b:Float) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(lab)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(LabColor(lab, l, a, b), true)
internalBuffer.append(name.bytes).append(" cs ")
internalBuffer.append(l + " " + a + " " + b + " ")
internalBuffer.append("scn").append_i(separator)
}

/** Sets the stroke color to a spot color.
       * @param sp the spot color
      * * 
 * @param tint the tint for the spot color. 0 is no color and 1
      * * is 100% color
 */
    open fun setColorStroke(sp:PdfSpotColor, tint:Float) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(sp)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(SpotColor(sp, tint), false)
internalBuffer.append(name.bytes).append(" CS ").append(tint).append(" SCN").append_i(separator)
}

 fun setColorStroke(sp:PdfDeviceNColor, tints:FloatArray) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(sp)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(DeviceNColor(sp, tints), true)
internalBuffer.append(name.bytes).append(" CS ")
for (tint in tints)
internalBuffer.append(tint + " ")
internalBuffer.append("SCN").append_i(separator)
}

 fun setColorStroke(lab:PdfLabColor, l:Float, a:Float, b:Float) {
checkWriter()
state.colorDetails = pdfWriter!!.addSimple(lab)
val prs = pageResources
var name = state.colorDetails.colorSpaceName
name = prs.addColor(name, state.colorDetails.indirectReference)
saveColor(LabColor(lab, l, a, b), true)
internalBuffer.append(name.bytes).append(" CS ")
internalBuffer.append(l + " " + a + " " + b + " ")
internalBuffer.append("SCN").append_i(separator)
}

/** Sets the fill color to a pattern. The pattern can be
       * colored or uncolored.
       * @param p the pattern
 */
    open fun setPatternFill(p:PdfPatternPainter) {
if (p.isStencil)
{
setPatternFill(p, p.defaultColor)
return 
}
checkWriter()
val prs = pageResources
var name = pdfWriter!!.addSimplePattern(p)
name = prs.addPattern(name, p.indirectReference)
saveColor(PatternColor(p), true)
internalBuffer.append(PdfName.PATTERN.bytes).append(" cs ").append(name.bytes).append(" scn").append_i(separator)
}

/** Outputs the color values to the content.
       * @param color The color
      * * 
 * @param tint the tint if it is a spot color, ignored otherwise
 */
    internal fun outputColorNumbers(color:BaseColor, tint:Float) {
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, color)
val type = ExtendedColor.getType(color)
when (type) {
ExtendedColor.TYPE_RGB -> {
internalBuffer.append(color.red.toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append(color.green.toFloat() / 0xFF)
internalBuffer.append(' ')
internalBuffer.append(color.blue.toFloat() / 0xFF)
}
ExtendedColor.TYPE_GRAY -> internalBuffer.append((color as GrayColor).gray)
ExtendedColor.TYPE_CMYK -> {
val cmyk = color as CMYKColor
internalBuffer.append(cmyk.cyan).append(' ').append(cmyk.magenta)
internalBuffer.append(' ').append(cmyk.yellow).append(' ').append(cmyk.black)
}
ExtendedColor.TYPE_SEPARATION -> internalBuffer.append(tint)
else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"))
}
}

/** Sets the fill color to an uncolored pattern.
       * @param p the pattern
      * * 
 * @param color the color of the pattern
 */
     fun setPatternFill(p:PdfPatternPainter, color:BaseColor) {
if (ExtendedColor.getType(color) == ExtendedColor.TYPE_SEPARATION)
setPatternFill(p, color, (color as SpotColor).tint)
else
setPatternFill(p, color, 0f)
}

/** Sets the fill color to an uncolored pattern.
       * @param p the pattern
      * * 
 * @param color the color of the pattern
      * * 
 * @param tint the tint if the color is a spot color, ignored otherwise
 */
    open fun setPatternFill(p:PdfPatternPainter, color:BaseColor, tint:Float) {
checkWriter()
if (!p.isStencil)
throw RuntimeException(MessageLocalization.getComposedMessage("an.uncolored.pattern.was.expected"))
val prs = pageResources
var name = pdfWriter!!.addSimplePattern(p)
name = prs.addPattern(name, p.indirectReference)
val csDetail = pdfWriter!!.addSimplePatternColorspace(color)
val cName = prs.addColor(csDetail.colorSpaceName, csDetail.indirectReference)
saveColor(UncoloredPattern(p, color, tint), true)
internalBuffer.append(cName.bytes).append(" cs").append_i(separator)
outputColorNumbers(color, tint)
internalBuffer.append(' ').append(name.bytes).append(" scn").append_i(separator)
}

/** Sets the stroke color to an uncolored pattern.
       * @param p the pattern
      * * 
 * @param color the color of the pattern
 */
     fun setPatternStroke(p:PdfPatternPainter, color:BaseColor) {
if (ExtendedColor.getType(color) == ExtendedColor.TYPE_SEPARATION)
setPatternStroke(p, color, (color as SpotColor).tint)
else
setPatternStroke(p, color, 0f)
}

/** Sets the stroke color to an uncolored pattern.
       * @param p the pattern
      * * 
 * @param color the color of the pattern
      * * 
 * @param tint the tint if the color is a spot color, ignored otherwise
 */
    open fun setPatternStroke(p:PdfPatternPainter, color:BaseColor, tint:Float) {
checkWriter()
if (!p.isStencil)
throw RuntimeException(MessageLocalization.getComposedMessage("an.uncolored.pattern.was.expected"))
val prs = pageResources
var name = pdfWriter!!.addSimplePattern(p)
name = prs.addPattern(name, p.indirectReference)
val csDetail = pdfWriter!!.addSimplePatternColorspace(color)
val cName = prs.addColor(csDetail.colorSpaceName, csDetail.indirectReference)
saveColor(UncoloredPattern(p, color, tint), false)
internalBuffer.append(cName.bytes).append(" CS").append_i(separator)
outputColorNumbers(color, tint)
internalBuffer.append(' ').append(name.bytes).append(" SCN").append_i(separator)
}

/** Sets the stroke color to a pattern. The pattern can be
       * colored or uncolored.
       * @param p the pattern
 */
    open fun setPatternStroke(p:PdfPatternPainter) {
if (p.isStencil)
{
setPatternStroke(p, p.defaultColor)
return 
}
checkWriter()
val prs = pageResources
var name = pdfWriter!!.addSimplePattern(p)
name = prs.addPattern(name, p.indirectReference)
saveColor(PatternColor(p), false)
internalBuffer.append(PdfName.PATTERN.bytes).append(" CS ").append(name.bytes).append(" SCN").append_i(separator)
}

/**
       * Paints using a shading object.
       * @param shading the shading object
 */
     fun paintShading(shading:PdfShading) {
pdfWriter!!.addSimpleShading(shading)
val prs = pageResources
val name = prs.addShading(shading.shadingName, shading.getShadingReference())
internalBuffer.append(name.bytes).append(" sh").append_i(separator)
val details = shading.colorDetails
if (details != null)
prs.addColor(details.colorSpaceName, details.indirectReference)
}

/**
       * Paints using a shading pattern.
       * @param shading the shading pattern
 */
     fun paintShading(shading:PdfShadingPattern) {
paintShading(shading.shading)
}

/**
       * Sets the shading fill pattern.
       * @param shading the shading pattern
 */
     fun setShadingFill(shading:PdfShadingPattern) {
pdfWriter!!.addSimpleShadingPattern(shading)
val prs = pageResources
val name = prs.addPattern(shading.patternName, shading.getPatternReference())
saveColor(ShadingColor(shading), true)
internalBuffer.append(PdfName.PATTERN.bytes).append(" cs ").append(name.bytes).append(" scn").append_i(separator)
val details = shading.colorDetails
if (details != null)
prs.addColor(details.colorSpaceName, details.indirectReference)
}

/**
       * Sets the shading stroke pattern
       * @param shading the shading pattern
 */
     fun setShadingStroke(shading:PdfShadingPattern) {
pdfWriter!!.addSimpleShadingPattern(shading)
val prs = pageResources
val name = prs.addPattern(shading.patternName, shading.getPatternReference())
saveColor(ShadingColor(shading), false)
internalBuffer.append(PdfName.PATTERN.bytes).append(" CS ").append(name.bytes).append(" SCN").append_i(separator)
val details = shading.colorDetails
if (details != null)
prs.addColor(details.colorSpaceName, details.indirectReference)
}

/** Check if we have a valid PdfWriter.
     
      */
    protected fun checkWriter() {
if (pdfWriter == null)
throw NullPointerException(MessageLocalization.getComposedMessage("the.writer.in.pdfcontentbyte.is.null"))
}

/**
       * Show an array of text.
       * @param text array of text
 */
     fun showText(text:PdfTextArray) {
checkState()
if (!inText && isTagged)
{
beginText(true)
}
if (state.fontDetails == null)
throw NullPointerException(MessageLocalization.getComposedMessage("font.and.size.must.be.set.before.writing.any.text"))
internalBuffer.append("[")
val arrayList = text.arrayList
var lastWasNumber = false
for (obj in arrayList)
{
if (obj is String)
{
showText2(obj)
updateTx(obj, 0f)
lastWasNumber = false
}
else
{
if (lastWasNumber)
internalBuffer.append(' ')
else
lastWasNumber = true
internalBuffer.append((obj as Float).toFloat())
updateTx("", obj.toFloat())
}
}
internalBuffer.append("]TJ").append_i(separator)
}

/**
       * Implements a link to other part of the document. The jump will
       * be made to a local destination with the same name, that must exist.
       * @param name the name for this link
      * * 
 * @param llx the lower left x corner of the activation area
      * * 
 * @param lly the lower left y corner of the activation area
      * * 
 * @param urx the upper right x corner of the activation area
      * * 
 * @param ury the upper right y corner of the activation area
 */
     fun localGoto(name:String, llx:Float, lly:Float, urx:Float, ury:Float) {
pdfDocument.localGoto(name, llx, lly, urx, ury)
}

/**
       * The local destination to where a local goto with the same
       * name will jump.
       * @param name the name of this local destination
      * * 
 * @param destination the PdfDestination with the jump coordinates
      * * 
 * @return true if the local destination was added,
      * * false if a local destination with the same name
      * * already exists
 */
     fun localDestination(name:String, destination:PdfDestination):Boolean {
return pdfDocument.localDestination(name, destination)
}

/**
       * Gets a duplicate of this PdfContentByte. All
       * the members are copied by reference but the buffer stays different.
     
       * @return a copy of this PdfContentByte
 */
    open val duplicate:PdfContentByte
get() {
val cb = PdfContentByte(pdfWriter)
cb.duplicatedFrom = this
return cb
}

 fun getDuplicate(inheritGraphicState:Boolean):PdfContentByte {
val cb = this.duplicate
if (inheritGraphicState)
{
cb.state = state
cb.stateList = stateList
}
return cb
}

 fun inheritGraphicState(parentCanvas:PdfContentByte) {
this.state = parentCanvas.state
this.stateList = parentCanvas.stateList
}

/**
       * Implements a link to another document.
       * @param filename the filename for the remote document
      * * 
 * @param name the name to jump to
      * * 
 * @param llx the lower left x corner of the activation area
      * * 
 * @param lly the lower left y corner of the activation area
      * * 
 * @param urx the upper right x corner of the activation area
      * * 
 * @param ury the upper right y corner of the activation area
 */
     fun remoteGoto(filename:String, name:String, llx:Float, lly:Float, urx:Float, ury:Float) {
pdfDocument.remoteGoto(filename, name, llx, lly, urx, ury)
}

/**
       * Implements a link to another document.
       * @param filename the filename for the remote document
      * * 
 * @param page the page to jump to
      * * 
 * @param llx the lower left x corner of the activation area
      * * 
 * @param lly the lower left y corner of the activation area
      * * 
 * @param urx the upper right x corner of the activation area
      * * 
 * @param ury the upper right y corner of the activation area
 */
     fun remoteGoto(filename:String, page:Int, llx:Float, lly:Float, urx:Float, ury:Float) {
pdfDocument.remoteGoto(filename, page, llx, lly, urx, ury)
}

/**
       * Adds a round rectangle to the current path.
     
       * @param x x-coordinate of the starting point
      * * 
 * @param y y-coordinate of the starting point
      * * 
 * @param w width
      * * 
 * @param h height
      * * 
 * @param r radius of the arc corner
 */
     fun roundRectangle(x:Float, y:Float, w:Float, h:Float, r:Float) {
roundRectangle(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), r.toDouble())
}

/**
       * Adds a round rectangle to the current path.
     
       * @param x x-coordinate of the starting point
      * * 
 * @param y y-coordinate of the starting point
      * * 
 * @param w width
      * * 
 * @param h height
      * * 
 * @param r radius of the arc corner
 */
     fun roundRectangle(x:Double, y:Double, w:Double, h:Double, r:Double) {
var x = x
var y = y
var w = w
var h = h
var r = r
if (w < 0)
{
x += w
w = -w
}
if (h < 0)
{
y += h
h = -h
}
if (r < 0)
r = -r
val b = 0.4477f
moveTo(x + r, y)
lineTo(x + w - r, y)
curveTo(x + w - r * b, y, x + w, y + r * b, x + w, y + r)
lineTo(x + w, y + h - r)
curveTo(x + w, y + h - r * b, x + w - r * b, y + h, x + w - r, y + h)
lineTo(x + r, y + h)
curveTo(x + r * b, y + h, x, y + h - r * b, x, y + h - r)
lineTo(x, y + r)
curveTo(x, y + r * b, x + r * b, y, x + r, y)
}

/** Implements an action in an area.
       * @param action the PdfAction
      * * 
 * @param llx the lower left x corner of the activation area
      * * 
 * @param lly the lower left y corner of the activation area
      * * 
 * @param urx the upper right x corner of the activation area
      * * 
 * @param ury the upper right y corner of the activation area
 */
    open fun setAction(action:PdfAction, llx:Float, lly:Float, urx:Float, ury:Float) {
pdfDocument.setAction(action, llx, lly, urx, ury)
}

/** Outputs a String directly to the content.
       * @param s the String
 */
     fun setLiteral(s:String) {
internalBuffer.append(s)
}

/** Outputs a char directly to the content.
       * @param c the char
 */
     fun setLiteral(c:Char) {
internalBuffer.append(c)
}

/** Outputs a float directly to the content.
       * @param n the float
 */
     fun setLiteral(n:Float) {
internalBuffer.append(n)
}

/** Throws an error if it is a pattern.
       * @param t the object to check
 */
    internal fun checkNoPattern(t:PdfTemplate) {
if (t.type == PdfTemplate.TYPE_PATTERN)
throw RuntimeException(MessageLocalization.getComposedMessage("invalid.use.of.a.pattern.a.template.was.expected"))
}

/**
       * Draws a TextField.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param on
 */
     fun drawRadioField(llx:Float, lly:Float, urx:Float, ury:Float, on:Boolean) {
drawRadioField(llx.toDouble(), lly.toDouble(), urx.toDouble(), ury.toDouble(), on)
}

/**
       * Draws a TextField.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param on
 */
     fun drawRadioField(llx:Double, lly:Double, urx:Double, ury:Double, on:Boolean) {
var llx = llx
var lly = lly
var urx = urx
var ury = ury
if (llx > urx) {
val x = llx
llx = urx
urx = x
}
if (lly > ury) {
val y = lly
lly = ury
ury = y
}
saveState()
// silver circle
        setLineWidth(1f)
setLineCap(1)
setColorStroke(BaseColor(0xC0, 0xC0, 0xC0))
arc(llx + 1f, lly + 1f, urx - 1f, ury - 1f, 0.0, 360.0)
stroke()
// gray circle-segment
        setLineWidth(1f)
setLineCap(1)
setColorStroke(BaseColor(0xA0, 0xA0, 0xA0))
arc(llx + 0.5f, lly + 0.5f, urx - 0.5f, ury - 0.5f, 45.0, 180.0)
stroke()
// black circle-segment
        setLineWidth(1f)
setLineCap(1)
setColorStroke(BaseColor(0x00, 0x00, 0x00))
arc(llx + 1.5f, lly + 1.5f, urx - 1.5f, ury - 1.5f, 45.0, 180.0)
stroke()
if (on)
{
// gray circle
            setLineWidth(1f)
setLineCap(1)
setColorFill(BaseColor(0x00, 0x00, 0x00))
arc(llx + 4f, lly + 4f, urx - 4f, ury - 4f, 0.0, 360.0)
fill()
}
restoreState()
}

/**
       * Draws a TextField.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
 */
     fun drawTextField(llx:Float, lly:Float, urx:Float, ury:Float) {
drawTextField(llx.toDouble(), lly.toDouble(), urx.toDouble(), ury.toDouble())
}

/**
       * Draws a TextField.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
 */
     fun drawTextField(llx:Double, lly:Double, urx:Double, ury:Double) {
var llx = llx
var lly = lly
var urx = urx
var ury = ury
if (llx > urx) {
val x = llx
llx = urx
urx = x
}
if (lly > ury) {
val y = lly
lly = ury
ury = y
}
// silver rectangle not filled
        saveState()
setColorStroke(BaseColor(0xC0, 0xC0, 0xC0))
setLineWidth(1f)
setLineCap(0)
rectangle(llx, lly, urx - llx, ury - lly)
stroke()
// white rectangle filled
        setLineWidth(1f)
setLineCap(0)
setColorFill(BaseColor(0xFF, 0xFF, 0xFF))
rectangle(llx + 0.5f, lly + 0.5f, urx - llx - 1.0, ury - lly - 1.0)
fill()
// silver lines
        setColorStroke(BaseColor(0xC0, 0xC0, 0xC0))
setLineWidth(1f)
setLineCap(0)
moveTo(llx + 1f, lly + 1.5f)
lineTo(urx - 1.5f, lly + 1.5f)
lineTo(urx - 1.5f, ury - 1f)
stroke()
// gray lines
        setColorStroke(BaseColor(0xA0, 0xA0, 0xA0))
setLineWidth(1f)
setLineCap(0)
moveTo(llx + 1f, lly + 1)
lineTo(llx + 1f, ury - 1f)
lineTo(urx - 1f, ury - 1f)
stroke()
// black lines
        setColorStroke(BaseColor(0x00, 0x00, 0x00))
setLineWidth(1f)
setLineCap(0)
moveTo(llx + 2f, lly + 2f)
lineTo(llx + 2f, ury - 2f)
lineTo(urx - 2f, ury - 2f)
stroke()
restoreState()
}

/**
       * Draws a button.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param text
      * * 
 * @param bf
      * * 
 * @param size
 */
     fun drawButton(llx:Float, lly:Float, urx:Float, ury:Float, text:String, bf:BaseFont, size:Float) {
drawButton(llx.toDouble(), lly.toDouble(), urx.toDouble(), ury.toDouble(), text, bf, size)
}

/**
       * Draws a button.
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param text
      * * 
 * @param bf
      * * 
 * @param size
 */
     fun drawButton(llx:Double, lly:Double, urx:Double, ury:Double, text:String, bf:BaseFont, size:Float) {
var llx = llx
var lly = lly
var urx = urx
var ury = ury
if (llx > urx) {
val x = llx
llx = urx
urx = x
}
if (lly > ury) {
val y = lly
lly = ury
ury = y
}
// black rectangle not filled
        saveState()
setColorStroke(BaseColor(0x00, 0x00, 0x00))
setLineWidth(1f)
setLineCap(0)
rectangle(llx, lly, urx - llx, ury - lly)
stroke()
// silver rectangle filled
        setLineWidth(1f)
setLineCap(0)
setColorFill(BaseColor(0xC0, 0xC0, 0xC0))
rectangle(llx + 0.5f, lly + 0.5f, urx - llx - 1.0, ury - lly - 1.0)
fill()
// white lines
        setColorStroke(BaseColor(0xFF, 0xFF, 0xFF))
setLineWidth(1f)
setLineCap(0)
moveTo(llx + 1f, lly + 1f)
lineTo(llx + 1f, ury - 1f)
lineTo(urx - 1f, ury - 1f)
stroke()
// dark grey lines
        setColorStroke(BaseColor(0xA0, 0xA0, 0xA0))
setLineWidth(1f)
setLineCap(0)
moveTo(llx + 1f, lly + 1f)
lineTo(urx - 1f, lly + 1f)
lineTo(urx - 1f, ury - 1f)
stroke()
// text
        resetRGBColorFill()
beginText()
setFontAndSize(bf, size)
showTextAligned(PdfContentByte.ALIGN_CENTER, text, (llx + (urx - llx) / 2).toFloat(), (lly + (ury - lly - size.toDouble()) / 2).toFloat(), 0f)
endText()
restoreState()
}

internal open val pageResources:PageResources
get() =pdfDocument.pageResources

/** Sets the graphic state
       * @param gstate the graphic state
 */
     fun setGState(gstate:PdfGState) {
val obj = pdfWriter!!.addSimpleExtGState(gstate)
val prs = pageResources
val name = prs.addExtGState(obj[0] as PdfName, obj[1] as PdfIndirectReference)
state.extGState = gstate
internalBuffer.append(name.bytes).append(" gs").append_i(separator)
}

/**
       * Begins a graphic block whose visibility is controlled by the layer.
       * Blocks can be nested. Each block must be terminated by an [.endLayer].
 *
 *
       * Note that nested layers with [PdfLayer.addChild] only require a single
       * call to this method and a single call to [.endLayer]; all the nesting control
       * is built in.
       * @param layer the layer
 */
     fun beginLayer(layer:PdfOCG) {
if (layer is PdfLayer && layer.title != null)
throw IllegalArgumentException(MessageLocalization.getComposedMessage("a.title.is.not.a.layer"))
if (layerDepth == null)
layerDepth = ArrayList<Int>()
if (layer is PdfLayerMembership)
{
layerDepth!!.add(Integer.valueOf(1))
beginLayer2(layer)
return 
}
var n = 0
var la:PdfLayer? = layer as PdfLayer
while (la != null)
{
if (la.title == null)
{
beginLayer2(la)
++n
}
la = la.parent
}
layerDepth!!.add(Integer.valueOf(n))
}

private fun beginLayer2(layer:PdfOCG) {
var name = pdfWriter!!.addSimpleProperty(layer, layer.ref)[0] as PdfName
val prs = pageResources
name = prs.addProperty(name, layer.ref)
internalBuffer.append("/OC ").append(name.bytes).append(" BDC").append_i(separator)
}

/**
       * Ends a layer controlled graphic block. It will end the most recent open block.
      */
     fun endLayer() {
var n = 1
if (layerDepth != null && !layerDepth!!.isEmpty())
{
n = layerDepth!![layerDepth!!.size - 1].toInt()
layerDepth!!.removeAt(layerDepth!!.size - 1)
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.layer.operators"))
}
while (n-- > 0)
internalBuffer.append("EMC").append_i(separator)
}

/** Concatenates a transformation to the current transformation
       * matrix.
       * @param af the transformation
 */
     fun transform(af:AffineTransform) {
if (inText && isTagged)
{
endText()
}
val matrix = DoubleArray(6)
af.getMatrix(matrix)
state.CTM.concatenate(af)
internalBuffer.append(matrix[0]).append(' ').append(matrix[1]).append(' ').append(matrix[2]).append(' ')
internalBuffer.append(matrix[3]).append(' ').append(matrix[4]).append(' ').append(matrix[5]).append(" cm").append_i(separator)
}

internal open fun addAnnotation(annot:PdfAnnotation) {
val needToTag = isTagged && annot.role != null && (annot !is PdfFormField || annot.kids == null)
if (needToTag)
{
openMCBlock(annot)
}
pdfWriter!!.addAnnotation(annot)
if (needToTag)
{
val strucElem = pdfDocument.getStructElement(annot.id)
if (strucElem != null)
{
val structParent = pdfDocument.getStructParentIndex(annot)
annot.put(PdfName.STRUCTPARENT, PdfNumber(structParent))
strucElem.setAnnotation(annot, currentPage)
pdfWriter!!.getStructureTreeRoot().setAnnotationMark(structParent, strucElem.reference)
}
closeMCBlock(annot)
}
}

 fun addAnnotation(annot:PdfAnnotation, applyCTM:Boolean) {
if (applyCTM && state.CTM.getType() != AffineTransform.TYPE_IDENTITY)
{
annot.applyCTM(state.CTM)
}
addAnnotation(annot)
}

/**
       * Sets the default colorspace.
       * @param name the name of the colorspace. It can be PdfName.DEFAULTGRAY, PdfName.DEFAULTRGB
      * * or PdfName.DEFAULTCMYK
      * * 
 * @param obj the colorspace. A null or PdfNull removes any colorspace with the same name
 */
     fun setDefaultColorspace(name:PdfName, obj:PdfObject) {
val prs = pageResources
prs.addDefaultColor(name, obj)
}

/**
       * Begins a marked content sequence. This sequence will be tagged with the structure struc.
       * The same structure can be used several times to connect text that belongs to the same logical segment
       * but is in a different location, like the same paragraph crossing to another page, for example.
       * @param struc the tagging structure
 */
     fun beginMarkedContentSequence(struc:PdfStructureElement) {
beginMarkedContentSequence(struc, null)
}

/**
       * Begins a marked content sequence. This sequence will be tagged with the structure struc.
       * The same structure can be used several times to connect text that belongs to the same logical segment
       * but is in a different location, like the same paragraph crossing to another page, for example.
       * expansion  is token's expansion.
       * @param struc the tagging structure
      * * 
 * @param expansion the expansion
 */
    private fun beginMarkedContentSequence(struc:PdfStructureElement, expansion:String?) {
val obj = struc.get(PdfName.K)
val structParentMarkPoint = pdfDocument.getStructParentIndexAndNextMarkPoint(currentPage)
val structParent = structParentMarkPoint[0]
val mark = structParentMarkPoint[1]
if (obj != null)
{
var ar:PdfArray? = null
if (obj.isNumber)
{
ar = PdfArray()
ar.add(obj)
struc.put(PdfName.K, ar)
}
else if (obj.isArray)
{
ar = obj as PdfArray?
}
else
throw IllegalArgumentException(MessageLocalization.getComposedMessage("unknown.object.at.k.1", obj.javaClass.toString()))
if (ar!!.getAsNumber(0) != null)
{
val dic = PdfDictionary(PdfName.MCR)
dic.put(PdfName.PG, currentPage)
dic.put(PdfName.MCID, PdfNumber(mark))
ar.add(dic)
}
struc.setPageMark(pdfDocument.getStructParentIndex(currentPage), -1)
}
else
{
struc.setPageMark(structParent, mark)
struc.put(PdfName.PG, currentPage)
}
setMcDepth(getMcDepth() + 1)
val contentSize = internalBuffer.size()
internalBuffer.append(struc.get(PdfName.S)!!.bytes).append(" <</MCID ").append(mark)
if (null != expansion)
{
internalBuffer.append("/E (").append(expansion).append(")")
}
internalBuffer.append(">> BDC").append_i(separator)
markedContentSize += internalBuffer.size() - contentSize
}

protected open val currentPage:PdfIndirectReference
get() =pdfWriter!!.currentPage

/**
       * Ends a marked content sequence
      */
     fun endMarkedContentSequence() {
if (getMcDepth() == 0)
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.begin.end.marked.content.operators"))
}
val contentSize = internalBuffer.size()
setMcDepth(getMcDepth() - 1)
internalBuffer.append("EMC").append_i(separator)
markedContentSize += internalBuffer.size() - contentSize
}

/**
       * Begins a marked content sequence. If property is null the mark will be of the type
       * BMC otherwise it will be BDC.
       * @param tag the tag
      * * 
 * @param property the property
      * * 
 * @param inline true to include the property in the content or false
      * * to include the property in the resource dictionary with the possibility of reusing
 */
    @JvmOverloads  fun beginMarkedContentSequence(tag:PdfName, property:PdfDictionary? = null, inline:Boolean = false) {
val contentSize = internalBuffer.size()
if (property == null)
{
internalBuffer.append(tag.bytes).append(" BMC").append_i(separator)
setMcDepth(getMcDepth() + 1)
}
else
{
internalBuffer.append(tag.bytes).append(' ')
if (inline)
try
{
property.toPdf(pdfWriter, internalBuffer)
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

else
{
val objs:Array<PdfObject>
if (pdfWriter!!.propertyExists(property))
objs = pdfWriter!!.addSimpleProperty(property, null)
else
objs = pdfWriter!!.addSimpleProperty(property, pdfWriter!!.pdfIndirectReference)
var name = objs[0] as PdfName
val prs = pageResources
name = prs.addProperty(name, objs[1] as PdfIndirectReference)
internalBuffer.append(name.bytes)
}
internalBuffer.append(" BDC").append_i(separator)
setMcDepth(getMcDepth() + 1)
}
markedContentSize += internalBuffer.size() - contentSize
}

/**
       * Checks for any dangling state: Mismatched save/restore state, begin/end text,
       * begin/end layer, or begin/end marked content sequence.
       * If found, this function will throw.  This function is called automatically
       * during a reset() (from Document.newPage() for example), and before writing
       * itself out in toPdf().
       * One possible cause: not calling myPdfGraphics2D.dispose() will leave dangling
                           * saveState() calls.
       * @since 2.1.6
      * * 
 * @throws IllegalPdfSyntaxException (a runtime exception)
 */
     fun sanityCheck() {
if (getMcDepth() != 0)
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.marked.content.operators"))
}
if (inText)
{
if (isTagged)
{
endText()
}
else
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.begin.end.text.operators"))
}
}
if (layerDepth != null && !layerDepth!!.isEmpty())
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.layer.operators"))
}
if (!stateList.isEmpty())
{
throw IllegalPdfSyntaxException(MessageLocalization.getComposedMessage("unbalanced.save.restore.state.operators"))
}
}

 fun openMCBlock(element:IAccessibleElement?) {
if (isTagged)
{
ensureDocumentTagIsOpen()
if (element != null/* && element.getRole() != null*/)
{
if (!getMcElements()!!.contains(element))
{
val structureElement = openMCBlockInt(element)
getMcElements()!!.add(element)
if (structureElement != null)
{
pdfDocument.saveStructElement(element.id, structureElement)
}
}
}
}
}

private val parentStructureElement:PdfDictionary
get() {
var parent:PdfDictionary? = null
if (getMcElements()!!.size > 0)
parent = pdfDocument.getStructElement(getMcElements()!![getMcElements()!!.size - 1].id)
if (parent == null)
{
parent = pdfWriter!!.getStructureTreeRoot()
}
return parent
}

private fun openMCBlockInt(element:IAccessibleElement):PdfStructureElement? {
var structureElement:PdfStructureElement? = null
if (isTagged)
{
var parent:IAccessibleElement? = null
if (getMcElements()!!.size > 0)
parent = getMcElements()!![getMcElements()!!.size - 1]
pdfWriter!!.checkElementRole(element, parent)
if (element.role != null)
{
if (PdfName.ARTIFACT != element.role)
{
structureElement = pdfDocument.getStructElement(element.id)
if (structureElement == null)
{
structureElement = PdfStructureElement(parentStructureElement, element.role, element.id)
}
}
if (PdfName.ARTIFACT == element.role)
{
val properties = element.accessibleAttributes
var propertiesDict:PdfDictionary? = null
if (properties != null && !properties.isEmpty())
{
propertiesDict = PdfDictionary()
for (entry in properties.entries)
{
propertiesDict.put(entry.key, entry.value)
}
}
val inTextLocal = inText
if (inText)
endText()
beginMarkedContentSequence(element.role, propertiesDict, true)
if (inTextLocal)
beginText(true)
}
else
{
if (pdfWriter!!.needToBeMarkedInContent(element))
{
val inTextLocal = inText
if (inText)
endText()
if (null != element.accessibleAttributes && null != element.getAccessibleAttribute(PdfName.E))
{
beginMarkedContentSequence(structureElement, element.getAccessibleAttribute(PdfName.E).toString())
element.setAccessibleAttribute(PdfName.E, null)
}
else
{
beginMarkedContentSequence(structureElement)
}
if (inTextLocal)
beginText(true)
}
}
}
}
return structureElement
}

 fun closeMCBlock(element:IAccessibleElement?) {
if (isTagged && element != null/* && element.getRole() != null*/)
{
if (getMcElements()!!.contains(element))
{
closeMCBlockInt(element)
getMcElements()!!.remove(element)
}
}
}

private fun closeMCBlockInt(element:IAccessibleElement) {
if (isTagged && element.role != null)
{
val structureElement = pdfDocument.getStructElement(element.id)
structureElement?.writeAttributes(element)
if (pdfWriter!!.needToBeMarkedInContent(element))
{
val inTextLocal = inText
if (inText)
endText()
endMarkedContentSequence()
if (inTextLocal)
beginText(true)
}
}
}

private fun ensureDocumentTagIsOpen() {
if (pdfDocument.openMCDocument)
{
pdfDocument.openMCDocument = false
pdfWriter!!.getDirectContentUnder().openMCBlock(pdfDocument)
}
}

protected fun saveMCBlocks():ArrayList<IAccessibleElement> {
var mc = ArrayList<IAccessibleElement>()
if (isTagged)
{
mc = getMcElements()
for (i in mc.indices)
{
closeMCBlockInt(mc[i])
}
setMcElements(ArrayList<IAccessibleElement>())
}
return mc
}

protected fun restoreMCBlocks(mcElements:ArrayList<IAccessibleElement>?) {
if (isTagged && mcElements != null)
{
setMcElements(mcElements)
for (i in 0..this.getMcElements()!!.size - 1)
{
openMCBlockInt(this.getMcElements()!![i])
}
}
}

protected fun getMcDepth():Int {
if (duplicatedFrom != null)
return duplicatedFrom!!.getMcDepth()
else
return mcDepth
}

protected fun setMcDepth(value:Int) {
if (duplicatedFrom != null)
duplicatedFrom!!.setMcDepth(value)
else
mcDepth = value
}

protected fun getMcElements():ArrayList<IAccessibleElement>? {
if (duplicatedFrom != null)
return duplicatedFrom!!.getMcElements()
else
return mcElements
}

protected fun setMcElements(value:ArrayList<IAccessibleElement>) {
if (duplicatedFrom != null)
duplicatedFrom!!.setMcElements(value)
else
mcElements = value
}

protected fun updateTx(text:String, Tj:Float) {
state.tx += getEffectiveStringWidth(text, false, Tj)
}

private fun saveColor(color:BaseColor, fill:Boolean) {
if (fill)
{
state.colorFill = color
}
else
{
state.colorStroke = color
}
}

internal class UncoloredPattern protected constructor(p:PdfPatternPainter, protected var color:BaseColor, protected var tint:Float):PatternColor(p) {

override fun equals(obj:Any?):Boolean {
return obj is UncoloredPattern && obj.painter == this.painter && obj.color == this.color && obj.tint == this.tint
}

}

protected fun checkState() {
var stroke = false
var fill = false
if (state.textRenderMode == TEXT_RENDER_MODE_FILL)
{
fill = true
}
else if (state.textRenderMode == TEXT_RENDER_MODE_STROKE)
{
stroke = true
}
else if (state.textRenderMode == TEXT_RENDER_MODE_FILL_STROKE)
{
fill = true
stroke = true
}
if (fill)
{
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorFill)
}
if (stroke)
{
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_COLOR, state.colorStroke)
}
PdfWriter.checkPdfIsoConformance(pdfWriter, PdfIsoKeys.PDFISOKEY_GSTATE, state.extGState)
}

// AWT related methods (remove this if you port to Android / GAE)

    /** Gets a Graphics2D to write on. The graphics
       * are translated to PDF commands as shapes. No PDF fonts will appear.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfGraphics2D")
 fun createGraphicsShapes(width:Float, height:Float):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height, true)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands as shapes. No PDF fonts will appear.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param printerJob a printer job
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createPrinterGraphicsShapes(width:Float, height:Float, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, true, printerJob)
}

/** Gets a Graphics2D to write on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfGraphics2D")
 fun createGraphics(width:Float, height:Float):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param printerJob
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createPrinterGraphics(width:Float, height:Float, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, printerJob)
}

/** Gets a Graphics2D to write on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param convertImagesToJPEG
      * * 
 * @param quality
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfGraphics2D")
 fun createGraphics(width:Float, height:Float, convertImagesToJPEG:Boolean, quality:Float):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height, null, false, convertImagesToJPEG, quality)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param convertImagesToJPEG
      * * 
 * @param quality
      * * 
 * @param printerJob
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfGraphics2D")
 fun createPrinterGraphics(width:Float, height:Float, convertImagesToJPEG:Boolean, quality:Float, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, null, false, convertImagesToJPEG, quality, printerJob)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width
      * * 
 * @param height
      * * 
 * @param convertImagesToJPEG
      * * 
 * @param quality
      * * 
 * @return A Graphics2D object
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createGraphicsShapes(width:Float, height:Float, convertImagesToJPEG:Boolean, quality:Float):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height, null, true, convertImagesToJPEG, quality)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width
      * * 
 * @param height
      * * 
 * @param convertImagesToJPEG
      * * 
 * @param quality
      * * 
 * @param printerJob
      * * 
 * @return a Graphics2D object
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createPrinterGraphicsShapes(width:Float, height:Float, convertImagesToJPEG:Boolean, quality:Float, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, null, true, convertImagesToJPEG, quality, printerJob)
}

/** Gets a Graphics2D to write on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param fontMapper the mapping from awt fonts to BaseFont
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createGraphics(width:Float, height:Float, fontMapper:FontMapper):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height, fontMapper)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param fontMapper the mapping from awt fonts to BaseFont
      * * 
 * @param printerJob a printer job
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createPrinterGraphics(width:Float, height:Float, fontMapper:FontMapper, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, fontMapper, printerJob)
}

/** Gets a Graphics2D to write on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param fontMapper the mapping from awt fonts to BaseFont
      * * 
 * @param convertImagesToJPEG converts awt images to jpeg before inserting in pdf
      * * 
 * @param quality the quality of the jpeg
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createGraphics(width:Float, height:Float, fontMapper:FontMapper, convertImagesToJPEG:Boolean, quality:Float):java.awt.Graphics2D {
return PdfGraphics2D(this, width, height, fontMapper, false, convertImagesToJPEG, quality)
}

/** Gets a Graphics2D to print on. The graphics
       * are translated to PDF commands.
       * @param width the width of the panel
      * * 
 * @param height the height of the panel
      * * 
 * @param fontMapper the mapping from awt fonts to BaseFont
      * * 
 * @param convertImagesToJPEG converts awt images to jpeg before inserting in pdf
      * * 
 * @param quality the quality of the jpeg
      * * 
 * @param printerJob a printer job
      * * 
 * @return a Graphics2D
      * * 
 */
    @Deprecated("use the constructor in PdfPrinterGraphics2D")
 fun createPrinterGraphics(width:Float, height:Float, fontMapper:FontMapper, convertImagesToJPEG:Boolean, quality:Float, printerJob:java.awt.print.PrinterJob):java.awt.Graphics2D {
return PdfPrinterGraphics2D(this, width, height, fontMapper, false, convertImagesToJPEG, quality, printerJob)
}

/**
       * adds an image with the given matrix.
       * @param image image to add
      * * 
 * @param transform transform to apply to the template prior to adding it.
      * * 
 * @since 5.0.1
      * * 
 */
    @Deprecated("use com.itextpdf.text.geom.AffineTransform as parameter")
@Throws(DocumentException::class)
 fun addImage(image:Image, transform:java.awt.geom.AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
addImage(image, AffineTransform(matrix))
}

/**
       * adds a template with the given matrix.
       * @param template template to add
      * * 
 * @param transform transform to apply to the template prior to adding it.
      * * 
 */
    @Deprecated("use com.itextpdf.text.geom.AffineTransform as parameter")
 fun addTemplate(template:PdfTemplate, transform:java.awt.geom.AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
addTemplate(template, AffineTransform(matrix))
}

/**
       * Concatenate a matrix to the current transformation matrix.
       * @param transform added to the Current Transformation Matrix
      * * 
 */
    @Deprecated("use com.itextpdf.text.geom.AffineTransform as parameter")
 fun concatCTM(transform:java.awt.geom.AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
concatCTM(AffineTransform(matrix))
}

/**
       * Changes the text matrix.
       * 
       * @param transform overwrite the current text matrix with this one
      * * 
 */
    @Deprecated("use com.itextpdf.text.geom.AffineTransform as parameter")
 fun setTextMatrix(transform:java.awt.geom.AffineTransform) {
val matrix = DoubleArray(6)
transform.getMatrix(matrix)
setTextMatrix(AffineTransform(matrix))
}

/** Concatenates a transformation to the current transformation
       * matrix.
       * @param af the transformation
      * * 
 */
    @Deprecated("use com.itextpdf.text.geom.AffineTransform as parameter")
 fun transform(af:java.awt.geom.AffineTransform) {
val matrix = DoubleArray(6)
af.getMatrix(matrix)
transform(AffineTransform(matrix))
}

companion object {

/** The alignment is center  */
     val ALIGN_CENTER = Element.ALIGN_CENTER

/** The alignment is left  */
     val ALIGN_LEFT = Element.ALIGN_LEFT

/** The alignment is right  */
     val ALIGN_RIGHT = Element.ALIGN_RIGHT

/** A possible line cap value  */
     val LINE_CAP_BUTT = 0
/** A possible line cap value  */
     val LINE_CAP_ROUND = 1
/** A possible line cap value  */
     val LINE_CAP_PROJECTING_SQUARE = 2

/** A possible line join value  */
     val LINE_JOIN_MITER = 0
/** A possible line join value  */
     val LINE_JOIN_ROUND = 1
/** A possible line join value  */
     val LINE_JOIN_BEVEL = 2

/** A possible text rendering value  */
     val TEXT_RENDER_MODE_FILL = 0
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_STROKE = 1
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_FILL_STROKE = 2
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_INVISIBLE = 3
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_FILL_CLIP = 4
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_STROKE_CLIP = 5
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_FILL_STROKE_CLIP = 6
/** A possible text rendering value  */
     val TEXT_RENDER_MODE_CLIP = 7

private val unitRect = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)

private val abrev = HashMap<PdfName, String>()

init{
abrev.put(PdfName.BITSPERCOMPONENT, "/BPC ")
abrev.put(PdfName.COLORSPACE, "/CS ")
abrev.put(PdfName.DECODE, "/D ")
abrev.put(PdfName.DECODEPARMS, "/DP ")
abrev.put(PdfName.FILTER, "/F ")
abrev.put(PdfName.HEIGHT, "/H ")
abrev.put(PdfName.IMAGEMASK, "/IM ")
abrev.put(PdfName.INTENT, "/Intent ")
abrev.put(PdfName.INTERPOLATE, "/I ")
abrev.put(PdfName.WIDTH, "/W ")
}

/**
       * Constructs a kern array for a text in a certain font
       * @param text the text
      * * 
 * @param font the font
      * * 
 * @return a PdfTextArray
 */
     fun getKernArray(text:String, font:BaseFont):PdfTextArray {
val pa = PdfTextArray()
val acc = StringBuffer()
val len = text.length - 1
val c = text.toCharArray()
if (len >= 0)
acc.append(c, 0, 1)
for (k in 0..len - 1)
{
val c2 = c[k + 1]
val kern = font.getKerning(c[k].toInt(), c2.toInt())
if (kern == 0)
{
acc.append(c2)
}
else
{
pa.add(acc.toString())
acc.setLength(0)
acc.append(c, k + 1, 1)
pa.add((-kern).toFloat())
}
}
pa.add(acc.toString())
return pa
}

/**
       * Generates an array of bezier curves to draw an arc.
       * 
       * (x1, y1) and (x2, y2) are the corners of the enclosing rectangle.
       * Angles, measured in degrees, start with 0 to the right (the positive X
       * axis) and increase counter-clockwise.  The arc extends from startAng
       * to startAng+extent.  I.e. startAng=0 and extent=180 yields an openside-down
       * semi-circle.
       * 
       * The resulting coordinates are of the form float[]{x1,y1,x2,y2,x3,y3, x4,y4}
       * such that the curve goes from (x1, y1) to (x4, y4) with (x2, y2) and
       * (x3, y3) as their respective Bezier control points.
       * 
       * Note: this code was taken from ReportLab (www.reportlab.org), an excellent
       * PDF generator for Python (BSD license: http://www.reportlab.org/devfaq.html#1.3 ).
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
      * * 
 * @param startAng starting angle in degrees
      * * 
 * @param extent angle extent in degrees
      * * 
 * @return a list of float[] with the bezier curves
 */
     fun bezierArc(x1:Float, y1:Float, x2:Float, y2:Float, startAng:Float, extent:Float):ArrayList<DoubleArray> {
return bezierArc(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), startAng.toDouble(), extent.toDouble())
}

/**
       * Generates an array of bezier curves to draw an arc.
       * 
       * (x1, y1) and (x2, y2) are the corners of the enclosing rectangle.
       * Angles, measured in degrees, start with 0 to the right (the positive X
       * axis) and increase counter-clockwise.  The arc extends from startAng
       * to startAng+extent.  I.e. startAng=0 and extent=180 yields an openside-down
       * semi-circle.
       * 
       * The resulting coordinates are of the form float[]{x1,y1,x2,y2,x3,y3, x4,y4}
       * such that the curve goes from (x1, y1) to (x4, y4) with (x2, y2) and
       * (x3, y3) as their respective Bezier control points.
       * 
       * Note: this code was taken from ReportLab (www.reportlab.org), an excellent
       * PDF generator for Python (BSD license: http://www.reportlab.org/devfaq.html#1.3 ).
     
       * @param x1 a corner of the enclosing rectangle
      * * 
 * @param y1 a corner of the enclosing rectangle
      * * 
 * @param x2 a corner of the enclosing rectangle
      * * 
 * @param y2 a corner of the enclosing rectangle
      * * 
 * @param startAng starting angle in degrees
      * * 
 * @param extent angle extent in degrees
      * * 
 * @return a list of float[] with the bezier curves
 */
     fun bezierArc(x1:Double, y1:Double, x2:Double, y2:Double, startAng:Double, extent:Double):ArrayList<DoubleArray> {
var x1 = x1
var y1 = y1
var x2 = x2
var y2 = y2
var tmp:Double
if (x1 > x2)
{
tmp = x1
x1 = x2
x2 = tmp
}
if (y2 > y1)
{
tmp = y1
y1 = y2
y2 = tmp
}

val fragAngle:Double
val Nfrag:Int
if (Math.abs(extent) <= 90f)
{
fragAngle = extent
Nfrag = 1
}
else
{
Nfrag = Math.ceil(Math.abs(extent) / 90f).toInt()
fragAngle = extent / Nfrag
}
val x_cen = (x1 + x2) / 2f
val y_cen = (y1 + y2) / 2f
val rx = (x2 - x1) / 2f
val ry = (y2 - y1) / 2f
val halfAng = fragAngle * Math.PI / 360.
val kappa = Math.abs(4. / 3. * (1. - Math.coshalfAng) / Math.sin(halfAng))
val pointList = ArrayList<DoubleArray>()
for (i in 0..Nfrag - 1)
{
val theta0 = (startAng + i * fragAngle) * Math.PI / 180.
val theta1 = (startAng + (i + 1) * fragAngle) * Math.PI / 180.
val cos0 = Math.cos(theta0)
val cos1 = Math.cos(theta1)
val sin0 = Math.sin(theta0)
val sin1 = Math.sin(theta1)
if (fragAngle > 0f)
{
pointList.add(doubleArrayOf(x_cen + rx * cos0, y_cen - ry * sin0, x_cen + rx * (cos0 - kappa * sin0), y_cen - ry * (sin0 + kappa * cos0), x_cen + rx * (cos1 + kappa * sin1), y_cen - ry * (sin1 - kappa * cos1), x_cen + rx * cos1, y_cen - ry * sin1))
}
else
{
pointList.add(doubleArrayOf(x_cen + rx * cos0, y_cen - ry * sin0, x_cen + rx * (cos0 + kappa * sin0), y_cen - ry * (sin0 - kappa * cos0), x_cen + rx * (cos1 - kappa * sin1), y_cen - ry * (sin1 + kappa * cos1), x_cen + rx * cos1, y_cen - ry * sin1))
}
}
return pointList
}
}
}/**
       * Adds an Image to the page. The Image must have
       * absolute positioning.
       * @param image the Image object
      * * 
 * @throws DocumentException if the Image does not have absolute positioning
 *//**
       * Adds an Image to the page. The positioning of the Image
       * is done with the transformation matrix. To position an image at (x,y)
       * use addImage(image, image_width, 0, 0, image_height, x, y).
       * @param image the Image object
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
      * * 
 * @throws DocumentException on error
 *//**
       * Makes this PdfContentByte empty.
       * Calls `reset( true )`
      *//**
       * Gets the size of this content.
     
       * @return the size of the content
 *//**
       * Create a new colored tiling pattern. Variables xstep and ystep are set to the same values
       * of width and height.
       * @param width the width of the pattern
      * * 
 * @param height the height of the pattern
      * * 
 * @return the PdfPatternPainter where the pattern will be created
 *//**
       * Adds a template to this content.
     
       * @param template the template
      * * 
 * @param a an element of the transformation matrix
      * * 
 * @param b an element of the transformation matrix
      * * 
 * @param c an element of the transformation matrix
      * * 
 * @param d an element of the transformation matrix
      * * 
 * @param e an element of the transformation matrix
      * * 
 * @param f an element of the transformation matrix
 *//**
       * adds a template with the given matrix.
       * @param template template to add
      * * 
 * @param transform transform to apply to the template prior to adding it.
 *//**
       * This is just a shorthand to beginMarkedContentSequence(tag, null, false).
       * @param tag the tag
 */
