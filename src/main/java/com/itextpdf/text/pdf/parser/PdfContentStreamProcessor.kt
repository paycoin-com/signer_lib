/*
 * $Id: 058eeefd3934590b5e4f2c04d5d81db3c54525a5 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import com.itextpdf.text.BaseColor
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.*

import java.io.IOException
import java.util.*

/**
 * Processor for a PDF content Stream.
 * @since    2.1.4
 */
class PdfContentStreamProcessor
/**
 * Creates a new PDF Content Stream Processor that will send it's output to the
 * designated render listener.

 * @param renderListener the [RenderListener] that will receive rendering notifications
 */
(
        /** Listener that will be notified of render events  */
        /**
         * Accessor method for the RenderListener object maintained in this class.
         * Necessary for implementing custom ContentOperator implementations.
         * @return the renderListener
         */
        val renderListener: RenderListener) {

    /** A map with all supported operators (PDF syntax).  */
    private val operators: MutableMap<String, ContentOperator>
    /** Resources for the content stream.  */
    private var resources: ResourceDictionary? = null
    /** Stack keeping track of the graphics state.  */
    private val gsStack = Stack<GraphicsState>()
    /** Text matrix.  */
    private var textMatrix: Matrix? = null
    /** Text line matrix.  */
    private var textLineMatrix: Matrix? = null
    /** A map with all supported XObject handlers  */
    private val xobjectDoHandlers: MutableMap<PdfName, XObjectDoHandler>
    /**
     * The font cache.
     * @since 5.0.6
     */
    /**   */
    private val cachedFonts = HashMap<Int, CMapAwareDocumentFont>()
    /**
     * A stack containing marked content info.
     * @since 5.0.2
     */
    private val markedContentStack = Stack<MarkedContentInfo>()

    init {
        operators = HashMap<String, ContentOperator>()
        populateOperators()
        xobjectDoHandlers = HashMap<PdfName, XObjectDoHandler>()
        populateXObjectDoHandlers()
        reset()
    }

    private fun populateXObjectDoHandlers() {
        registerXObjectDoHandler(PdfName.DEFAULT, IgnoreXObjectDoHandler())
        registerXObjectDoHandler(PdfName.FORM, FormXObjectDoHandler())
        registerXObjectDoHandler(PdfName.IMAGE, ImageXObjectDoHandler())
    }

    /**
     * Registers a Do handler that will be called when Do for the provided XObject subtype is encountered during content processing.
     *
     * If you register a handler, it is a very good idea to pass the call on to the existing registered handler (returned by this call), otherwise you
     * may inadvertently change the internal behavior of the processor.
     * @param xobjectSubType the XObject subtype this handler will process, or PdfName.DEFAULT for a catch-all handler
     * *
     * @param handler the handler that will receive notification when the Do operator for the specified subtype is encountered
     * *
     * @return the existing registered handler, if any
     * *
     * @since 5.0.1
     */
    fun registerXObjectDoHandler(xobjectSubType: PdfName, handler: XObjectDoHandler): XObjectDoHandler {
        return xobjectDoHandlers.put(xobjectSubType, handler)
    }

    /**
     * Gets the font pointed to by the indirect reference. The font may have been cached.
     * @param ind the indirect reference ponting to the font
     * *
     * @return the font
     * *
     * @since 5.0.6
     */
    private fun getFont(ind: PRIndirectReference): CMapAwareDocumentFont {
        val n = Integer.valueOf(ind.number)
        var font: CMapAwareDocumentFont? = cachedFonts[n]
        if (font == null) {
            font = CMapAwareDocumentFont(ind)
            cachedFonts.put(n, font)
        }
        return font
    }

    private fun getFont(fontResource: PdfDictionary): CMapAwareDocumentFont {
        return CMapAwareDocumentFont(fontResource)
    }

    /**
     * Loads all the supported graphics and text state operators in a map.
     */
    private fun populateOperators() {

        registerContentOperator(DEFAULTOPERATOR, IgnoreOperatorContentOperator())

        registerContentOperator("q", PushGraphicsState())
        registerContentOperator("Q", PopGraphicsState())
        registerContentOperator("g", SetGrayFill())
        registerContentOperator("G", SetGrayStroke())
        registerContentOperator("rg", SetRGBFill())
        registerContentOperator("RG", SetRGBStroke())
        registerContentOperator("k", SetCMYKFill())
        registerContentOperator("K", SetCMYKStroke())
        registerContentOperator("cs", SetColorSpaceFill())
        registerContentOperator("CS", SetColorSpaceStroke())
        registerContentOperator("sc", SetColorFill())
        registerContentOperator("SC", SetColorStroke())
        registerContentOperator("scn", SetColorFill())
        registerContentOperator("SCN", SetColorStroke())
        registerContentOperator("cm", ModifyCurrentTransformationMatrix())
        registerContentOperator("gs", ProcessGraphicsStateResource())

        val tcOperator = SetTextCharacterSpacing()
        registerContentOperator("Tc", tcOperator)
        val twOperator = SetTextWordSpacing()
        registerContentOperator("Tw", twOperator)
        registerContentOperator("Tz", SetTextHorizontalScaling())
        val tlOperator = SetTextLeading()
        registerContentOperator("TL", tlOperator)
        registerContentOperator("Tf", SetTextFont())
        registerContentOperator("Tr", SetTextRenderMode())
        registerContentOperator("Ts", SetTextRise())

        registerContentOperator("BT", BeginText())
        registerContentOperator("ET", EndText())
        registerContentOperator("BMC", BeginMarkedContent())
        registerContentOperator("BDC", BeginMarkedContentDictionary())
        registerContentOperator("EMC", EndMarkedContent())

        val tdOperator = TextMoveStartNextLine()
        registerContentOperator("Td", tdOperator)
        registerContentOperator("TD", TextMoveStartNextLineWithLeading(tdOperator, tlOperator))
        registerContentOperator("Tm", TextSetTextMatrix())
        val tstarOperator = TextMoveNextLine(tdOperator)
        registerContentOperator("T*", tstarOperator)

        val tjOperator = ShowText()
        registerContentOperator("Tj", tjOperator)
        val tickOperator = MoveNextLineAndShowText(tstarOperator, tjOperator)
        registerContentOperator("'", tickOperator)
        registerContentOperator("\"", MoveNextLineAndShowTextWithSpacing(twOperator, tcOperator, tickOperator))
        registerContentOperator("TJ", ShowTextArray())

        registerContentOperator("Do", Do())

        registerContentOperator("w", SetLineWidth())
        registerContentOperator("J", SetLineCap())
        registerContentOperator("j", SetLineJoin())
        registerContentOperator("M", SetMiterLimit())
        registerContentOperator("d", SetLineDashPattern())

        // Path construction and painting operators
        if (renderListener is ExtRenderListener) {
            val fillStroke = PathPaintingRenderInfo.FILL or PathPaintingRenderInfo.STROKE
            registerContentOperator("m", MoveTo())
            registerContentOperator("l", LineTo())
            registerContentOperator("c", Curve())
            registerContentOperator("v", CurveFirstPointDuplicated())
            registerContentOperator("y", CurveFourhPointDuplicated())
            registerContentOperator("h", CloseSubpath())
            registerContentOperator("re", Rectangle())
            registerContentOperator("S", PaintPath(PathPaintingRenderInfo.STROKE, -1, false))
            registerContentOperator("s", PaintPath(PathPaintingRenderInfo.STROKE, -1, true))
            registerContentOperator("f", PaintPath(PathPaintingRenderInfo.FILL, PathPaintingRenderInfo.NONZERO_WINDING_RULE, false))
            registerContentOperator("F", PaintPath(PathPaintingRenderInfo.FILL, PathPaintingRenderInfo.NONZERO_WINDING_RULE, false))
            registerContentOperator("f*", PaintPath(PathPaintingRenderInfo.FILL, PathPaintingRenderInfo.EVEN_ODD_RULE, false))
            registerContentOperator("B", PaintPath(fillStroke, PathPaintingRenderInfo.NONZERO_WINDING_RULE, false))
            registerContentOperator("B*", PaintPath(fillStroke, PathPaintingRenderInfo.EVEN_ODD_RULE, false))
            registerContentOperator("b", PaintPath(fillStroke, PathPaintingRenderInfo.NONZERO_WINDING_RULE, true))
            registerContentOperator("b*", PaintPath(fillStroke, PathPaintingRenderInfo.EVEN_ODD_RULE, true))
            registerContentOperator("n", PaintPath(PathPaintingRenderInfo.NO_OP, -1, false))
            registerContentOperator("W", ClipPath(PathPaintingRenderInfo.NONZERO_WINDING_RULE))
            registerContentOperator("W*", ClipPath(PathPaintingRenderInfo.EVEN_ODD_RULE))
        }
    }

    /**
     * Registers a content operator that will be called when the specified operator string is encountered during content processing.
     *
     * If you register an operator, it is a very good idea to pass the call on to the existing registered operator (returned by this call), otherwise you
     * may inadvertently change the internal behavior of the processor.
     * @param operatorString the operator id, or DEFAULTOPERATOR for a catch-all operator
     * *
     * @param operator the operator that will receive notification when the operator is encountered
     * *
     * @return the existing registered operator, if any
     * *
     * @since 2.1.7
     */
    fun registerContentOperator(operatorString: String, operator: ContentOperator): ContentOperator {
        return operators.put(operatorString, operator)
    }

    /**
     * @return [java.util.Collection] containing all the registered operators strings
     * *
     * @since 5.5.6
     */
    val registeredOperatorStrings: Collection<String>
        get() = ArrayList(operators.keys)

    /**
     * Resets the graphics state stack, matrices and resources.
     */
    fun reset() {
        gsStack.removeAllElements()
        gsStack.add(GraphicsState())
        textMatrix = null
        textLineMatrix = null
        resources = ResourceDictionary()
    }

    /**
     * Returns the current graphics state.
     * @return    the graphics state
     */
    fun gs(): GraphicsState {
        return gsStack.peek()
    }

    /**
     * Invokes an operator.
     * @param operator    the PDF Syntax of the operator
     * *
     * @param operands    a list with operands
     */
    @Throws(Exception::class)
    private fun invokeOperator(operator: PdfLiteral, operands: ArrayList<PdfObject>) {
        var op: ContentOperator? = operators[operator.toString()]
        if (op == null)
            op = operators[DEFAULTOPERATOR]
        op!!.invoke(this, operator, operands)
    }

    /**
     * Add to the marked content stack
     * @param tag the tag of the marked content
     * *
     * @param dict the PdfDictionary associated with the marked content
     * *
     * @since 5.0.2
     */
    private fun beginMarkedContent(tag: PdfName, dict: PdfDictionary) {
        markedContentStack.push(MarkedContentInfo(tag, dict))
    }

    /**
     * Remove the latest marked content from the stack.  Keeps track of the BMC, BDC and EMC operators.
     * @since 5.0.2
     */
    private fun endMarkedContent() {
        markedContentStack.pop()
    }

    /**
     * Used to trigger beginTextBlock on the renderListener
     */
    private fun beginText() {
        renderListener.beginTextBlock()
    }

    /**
     * Used to trigger endTextBlock on the renderListener
     */
    private fun endText() {
        renderListener.endTextBlock()
    }

    /**
     * Displays text.
     * @param string    the text to display
     */
    private fun displayPdfString(string: PdfString) {

        val renderInfo = TextRenderInfo(string, gs(), textMatrix, markedContentStack)

        renderListener.renderText(renderInfo)

        textMatrix = Matrix(renderInfo.unscaledWidth, 0f).multiply(textMatrix)
    }


    /**
     * Displays an XObject using the registered handler for this XObject's subtype
     * @param xobjectName the name of the XObject to retrieve from the resource dictionary
     */
    @Throws(IOException::class)
    private fun displayXObject(xobjectName: PdfName) {
        val xobjects = resources!!.getAsDict(PdfName.XOBJECT)
        val xobject = xobjects.getDirectObject(xobjectName)
        val xobjectStream = xobject as PdfStream

        val subType = xobjectStream.getAsName(PdfName.SUBTYPE)
        if (xobject.isStream()) {
            var handler: XObjectDoHandler? = xobjectDoHandlers[subType]
            if (handler == null)
                handler = xobjectDoHandlers[PdfName.DEFAULT]
            handler!!.handleXObject(this, xobjectStream, xobjects.getAsIndirectObject(xobjectName))
        } else {
            throw IllegalStateException(MessageLocalization.getComposedMessage("XObject.1.is.not.a.stream", xobjectName))
        }

    }

    /**
     * Displays the current path.

     * @param operation One of the possible combinations of [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.STROKE]
     * *                  and [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.FILL] values or
     * *                  [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.NO_OP]
     * *
     * @param rule      Either [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.NONZERO_WINDING_RULE] or
     * *                  [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.EVEN_ODD_RULE]
     * *                  In case it isn't applicable pass any byte value.
     * *
     * @param close     Indicates whether the path should be closed or not.
     * *
     * @since 5.5.6
     */
    private fun paintPath(operation: Int, rule: Int, close: Boolean) {
        if (close) {
            modifyPath(PathConstructionRenderInfo.CLOSE, null)
        }

        val renderInfo = PathPaintingRenderInfo(operation, rule, gs())
        (renderListener as ExtRenderListener).renderPath(renderInfo)
    }

    /**
     * Modifies the current path.

     * @param operation   Indicates which path-construction operation should be performed.
     * *
     * @param segmentData Contains x, y components of points of a new segment being added to the current path.
     * *                    E.g. x1 y1 x2 y2 x3 y3 etc. It's ignored for "close subpath" operarion (h).
     */
    private fun modifyPath(operation: Int, segmentData: List<Float>?) {
        val renderInfo = PathConstructionRenderInfo(operation, segmentData, gs().ctm)
        (renderListener as ExtRenderListener).modifyPath(renderInfo)
    }

    private fun clipPath(rule: Int) {
        (renderListener as ExtRenderListener).clipPath(rule)
    }

    /**
     * Adjusts the text matrix for the specified adjustment value (see TJ operator in the PDF spec for information)
     * @param tj the text adjustment
     */
    private fun applyTextAdjust(tj: Float) {
        val adjustBy = -tj / 1000f * gs().fontSize * gs().horizontalScaling

        textMatrix = Matrix(adjustBy, 0f).multiply(textMatrix)
    }

    /**
     * Processes PDF syntax.
     * **Note:** If you re-use a given [PdfContentStreamProcessor], you must call [PdfContentStreamProcessor.reset]
     * @param contentBytes    the bytes of a content stream
     * *
     * @param resources        the resources that come with the content stream
     */
    fun processContent(contentBytes: ByteArray, resources: PdfDictionary?) {
        this.resources!!.push(resources)
        try {
            val tokeniser = PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(contentBytes)))
            val ps = PdfContentParser(tokeniser)
            val operands = ArrayList<PdfObject>()
            while (ps.parse(operands).size > 0) {
                val operator = operands[operands.size - 1] as PdfLiteral
                if ("BI" == operator.toString()) {
                    // we don't call invokeOperator for embedded images - this is one area of the PDF spec that is particularly nasty and inconsistent
                    val colorSpaceDic = resources?.getAsDict(PdfName.COLORSPACE)
                    handleInlineImage(InlineImageUtils.parseInlineImage(ps, colorSpaceDic), colorSpaceDic)
                } else {
                    invokeOperator(operator, operands)
                }
            }

        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        this.resources!!.pop()

    }

    /**
     * Callback when an inline image is found.  This requires special handling because inline images don't follow the standard operator syntax
     * @param info the inline image
     * *
     * @param colorSpaceDic the color space for the inline immage
     */
    protected fun handleInlineImage(info: InlineImageInfo, colorSpaceDic: PdfDictionary) {
        val renderInfo = ImageRenderInfo.createForEmbeddedImage(gs(), info, colorSpaceDic)
        renderListener.renderImage(renderInfo)
    }

    /**
     * A resource dictionary that allows stack-like behavior to support resource dictionary inheritance
     */
    private class ResourceDictionary : PdfDictionary() {
        private val resourcesStack = ArrayList<PdfDictionary>()

        fun push(resources: PdfDictionary) {
            resourcesStack.add(resources)
        }

        fun pop() {
            resourcesStack.removeAt(resourcesStack.size - 1)
        }

        override fun getDirectObject(key: PdfName): PdfObject {
            for (i in resourcesStack.indices.reversed()) {
                val subResource = resourcesStack[i]
                if (subResource != null) {
                    val obj = subResource.getDirectObject(key)
                    if (obj != null) return obj
                }
            }
            return super.getDirectObject(key) // shouldn't be necessary, but just in case we've done something crazy
        }
    }

    /**
     * A content operator implementation (unregistered).
     */
    private class IgnoreOperatorContentOperator : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            // ignore the operator
        }
    }

    /**
     * A content operator implementation (TJ).
     */
    private class ShowTextArray : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val array = operands[0] as PdfArray
            var tj = 0f
            val i = array.listIterator()
            while (i.hasNext()) {
                val entryObj = i.next()
                if (entryObj is PdfString) {
                    processor.displayPdfString(entryObj)
                    tj = 0f
                } else {
                    tj = (entryObj as PdfNumber).floatValue()
                    processor.applyTextAdjust(tj)
                }
            }

        }
    }

    /**
     * A content operator implementation (").
     */
    private class MoveNextLineAndShowTextWithSpacing(private val setTextWordSpacing: SetTextWordSpacing, private val setTextCharacterSpacing: SetTextCharacterSpacing, private val moveNextLineAndShowText: MoveNextLineAndShowText) : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val aw = operands[0] as PdfNumber
            val ac = operands[1] as PdfNumber
            val string = operands[2] as PdfString

            val twOperands = ArrayList<PdfObject>(1)
            twOperands.add(0, aw)
            setTextWordSpacing.invoke(processor, null, twOperands)

            val tcOperands = ArrayList<PdfObject>(1)
            tcOperands.add(0, ac)
            setTextCharacterSpacing.invoke(processor, null, tcOperands)

            val tickOperands = ArrayList<PdfObject>(1)
            tickOperands.add(0, string)
            moveNextLineAndShowText.invoke(processor, null, tickOperands)
        }
    }

    /**
     * A content operator implementation (').
     */
    private class MoveNextLineAndShowText(private val textMoveNextLine: TextMoveNextLine, private val showText: ShowText) : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            textMoveNextLine.invoke(processor, null, ArrayList<PdfObject>(0))
            showText.invoke(processor, null, operands)
        }
    }

    /**
     * A content operator implementation (Tj).
     */
    private class ShowText : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val string = operands[0] as PdfString

            processor.displayPdfString(string)
        }
    }


    /**
     * A content operator implementation (T*).
     */
    private class TextMoveNextLine(private val moveStartNextLine: TextMoveStartNextLine) : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val tdoperands = ArrayList<PdfObject>(2)
            tdoperands.add(0, PdfNumber(0))
            tdoperands.add(1, PdfNumber(-processor.gs().leading))
            moveStartNextLine.invoke(processor, null, tdoperands)
        }
    }

    /**
     * A content operator implementation (Tm).
     */
    private class TextSetTextMatrix : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val a = (operands[0] as PdfNumber).floatValue()
            val b = (operands[1] as PdfNumber).floatValue()
            val c = (operands[2] as PdfNumber).floatValue()
            val d = (operands[3] as PdfNumber).floatValue()
            val e = (operands[4] as PdfNumber).floatValue()
            val f = (operands[5] as PdfNumber).floatValue()

            processor.textLineMatrix = Matrix(a, b, c, d, e, f)
            processor.textMatrix = processor.textLineMatrix
        }
    }

    /**
     * A content operator implementation (TD).
     */
    private class TextMoveStartNextLineWithLeading(private val moveStartNextLine: TextMoveStartNextLine, private val setTextLeading: SetTextLeading) : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val ty = (operands[1] as PdfNumber).floatValue()

            val tlOperands = ArrayList<PdfObject>(1)
            tlOperands.add(0, PdfNumber(-ty))
            setTextLeading.invoke(processor, null, tlOperands)
            moveStartNextLine.invoke(processor, null, operands)
        }
    }

    /**
     * A content operator implementation (Td).
     */
    private class TextMoveStartNextLine : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val tx = (operands[0] as PdfNumber).floatValue()
            val ty = (operands[1] as PdfNumber).floatValue()

            val translationMatrix = Matrix(tx, ty)
            processor.textMatrix = translationMatrix.multiply(processor.textLineMatrix)
            processor.textLineMatrix = processor.textMatrix
        }
    }

    /**
     * A content operator implementation (Tf).
     */
    private class SetTextFont : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val fontResourceName = operands[0] as PdfName
            val size = (operands[1] as PdfNumber).floatValue()

            val fontsDictionary = processor.resources!!.getAsDict(PdfName.FONT)
            val font: CMapAwareDocumentFont
            val fontObject = fontsDictionary.get(fontResourceName)
            if (fontObject is PdfDictionary)
                font = processor.getFont(fontObject)
            else
                font = processor.getFont(fontObject as PRIndirectReference)

            processor.gs().font = font
            processor.gs().fontSize = size

        }
    }

    /**
     * A content operator implementation (Tr).
     */
    private class SetTextRenderMode : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val render = operands[0] as PdfNumber
            processor.gs().renderMode = render.intValue()
        }
    }

    /**
     * A content operator implementation (Ts).
     */
    private class SetTextRise : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val rise = operands[0] as PdfNumber
            processor.gs().rise = rise.floatValue()
        }
    }

    /**
     * A content operator implementation (TL).
     */
    private class SetTextLeading : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val leading = operands[0] as PdfNumber
            processor.gs().leading = leading.floatValue()
        }
    }

    /**
     * A content operator implementation (Tz).
     */
    private class SetTextHorizontalScaling : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val scale = operands[0] as PdfNumber
            processor.gs().horizontalScaling = scale.floatValue() / 100f
        }
    }

    /**
     * A content operator implementation (Tc).
     */
    private class SetTextCharacterSpacing : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val charSpace = operands[0] as PdfNumber
            processor.gs().characterSpacing = charSpace.floatValue()
        }
    }

    /**
     * A content operator implementation (Tw).
     */
    private class SetTextWordSpacing : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>) {
            val wordSpace = operands[0] as PdfNumber
            processor.gs().wordSpacing = wordSpace.floatValue()
        }
    }

    /**
     * A content operator implementation (gs).
     */
    private class ProcessGraphicsStateResource : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {

            val dictionaryName = operands[0] as PdfName
            val extGState = processor.resources!!.getAsDict(PdfName.EXTGSTATE) ?: throw IllegalArgumentException(MessageLocalization.getComposedMessage("resources.do.not.contain.extgstate.entry.unable.to.process.operator.1", operator))
            val gsDic = extGState.getAsDict(dictionaryName) ?: throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.is.an.unknown.graphics.state.dictionary", dictionaryName))

            // at this point, all we care about is the FONT entry in the GS dictionary
            val fontParameter = gsDic.getAsArray(PdfName.FONT)
            if (fontParameter != null) {
                val font = processor.getFont(fontParameter.getPdfObject(0) as PRIndirectReference)
                val size = fontParameter.getAsNumber(1).floatValue()

                processor.gs().font = font
                processor.gs().fontSize = size
            }
        }
    }

    /**
     * A content operator implementation (q).
     */
    private class PushGraphicsState : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>?) {
            val gs = processor.gsStack.peek()
            val copy = GraphicsState(gs)
            processor.gsStack.push(copy)
        }
    }

    /**
     * A content operator implementation (cm).
     */
    private class ModifyCurrentTransformationMatrix : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val a = (operands[0] as PdfNumber).floatValue()
            val b = (operands[1] as PdfNumber).floatValue()
            val c = (operands[2] as PdfNumber).floatValue()
            val d = (operands[3] as PdfNumber).floatValue()
            val e = (operands[4] as PdfNumber).floatValue()
            val f = (operands[5] as PdfNumber).floatValue()
            val matrix = Matrix(a, b, c, d, e, f)
            val gs = processor.gsStack.peek()
            gs.ctm = matrix.multiply(gs.ctm)
        }
    }

    /**
     * A content operator implementation (g).
     */
    private class SetGrayFill : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().fillColor = getColor(1, operands)
        }
    }

    /**
     * A content operator implementation (G).
     */
    private class SetGrayStroke : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().strokeColor = getColor(1, operands)
        }
    }

    /**
     * A content operator implementation (rg).
     */
    private class SetRGBFill : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().fillColor = getColor(3, operands)
        }
    }

    /**
     * A content operator implementation (RG).
     */
    private class SetRGBStroke : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().strokeColor = getColor(3, operands)
        }
    }

    /**
     * A content operator implementation (rg).
     */
    private class SetCMYKFill : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().fillColor = getColor(4, operands)
        }
    }

    /**
     * A content operator implementation (RG).
     */
    private class SetCMYKStroke : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().strokeColor = getColor(4, operands)
        }
    }

    /**
     * A content operator implementation (cs).
     */
    private class SetColorSpaceFill : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().colorSpaceFill = operands[0] as PdfName
        }
    }

    /**
     * A content operator implementation (CS).
     */
    private class SetColorSpaceStroke : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().colorSpaceStroke = operands[0] as PdfName
        }
    }

    /**
     * A content operator implementation (sc / scn).
     */
    private class SetColorFill : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().fillColor = getColor(processor.gs().colorSpaceFill, operands)
        }
    }

    /**
     * A content operator implementation (SC / SCN).
     */
    private class SetColorStroke : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.gs().strokeColor = getColor(processor.gs().colorSpaceStroke, operands)
        }
    }

    /**
     * A content operator implementation (Q).
     */
    private class PopGraphicsState : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral?, operands: ArrayList<PdfObject>?) {
            processor.gsStack.pop()
        }
    }

    /**
     * A content operator implementation (BT).
     */
    private class BeginText : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.textMatrix = Matrix()
            processor.textLineMatrix = processor.textMatrix
            processor.beginText()
        }
    }

    /**
     * A content operator implementation (ET).
     */
    private class EndText : ContentOperator {
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.textMatrix = null
            processor.textLineMatrix = null
            processor.endText()
        }
    }

    /**
     * A content operator implementation (BMC).
     * @since 5.0.2
     */
    private class BeginMarkedContent : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor,
                            operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.beginMarkedContent(operands[0] as PdfName, PdfDictionary())
        }

    }

    /**
     * A content operator implementation (BDC).
     * @since 5.0.2
     */
    private class BeginMarkedContentDictionary : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor,
                            operator: PdfLiteral, operands: ArrayList<PdfObject>) {

            val properties = operands[1]

            processor.beginMarkedContent(operands[0] as PdfName, getPropertiesDictionary(properties, processor.resources))
        }

        private fun getPropertiesDictionary(operand1: PdfObject, resources: ResourceDictionary): PdfDictionary {
            if (operand1.isDictionary)
                return operand1 as PdfDictionary

            val dictionaryName = operand1 as PdfName
            return resources.getAsDict(dictionaryName)
        }
    }

    /**
     * A content operator implementation (EMC).
     * @since 5.0.2
     */
    private class EndMarkedContent : ContentOperator {
        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor,
                            operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.endMarkedContent()
        }
    }

    /**
     * A content operator implementation (Do).
     */
    private class Do : ContentOperator {
        @Throws(IOException::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val xobjectName = operands[0] as PdfName
            processor.displayXObject(xobjectName)
        }
    }

    /**
     * A content operator implementation (w).
     */
    private class SetLineWidth : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, oper: PdfLiteral, operands: ArrayList<PdfObject>) {
            val lineWidth = (operands[0] as PdfNumber).floatValue()
            processor.gs().lineWidth = lineWidth
        }
    }

    /**
     * A content operator implementation (J).
     */
    private inner class SetLineCap : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, oper: PdfLiteral, operands: ArrayList<PdfObject>) {
            val lineCap = (operands[0] as PdfNumber).intValue()
            processor.gs().lineCapStyle = lineCap
        }
    }

    /**
     * A content operator implementation (j).
     */
    private inner class SetLineJoin : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, oper: PdfLiteral, operands: ArrayList<PdfObject>) {
            val lineJoin = (operands[0] as PdfNumber).intValue()
            processor.gs().lineJoinStyle = lineJoin
        }
    }

    /**
     * A content operator implementation (M).
     */
    private inner class SetMiterLimit : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, oper: PdfLiteral, operands: ArrayList<PdfObject>) {
            val miterLimit = (operands[0] as PdfNumber).floatValue()
            processor.gs().miterLimit = miterLimit
        }
    }

    /**
     * A content operator implementation (d).
     */
    private inner class SetLineDashPattern : ContentOperator {

        override fun invoke(processor: PdfContentStreamProcessor, oper: PdfLiteral, operands: ArrayList<PdfObject>) {
            val pattern = LineDashPattern(operands[0] as PdfArray,
                    (operands[1] as PdfNumber).floatValue())
            processor.gs().lineDashPattern = pattern
        }
    }

    /**
     * A content operator implementation (m).

     * @since 5.5.6
     */
    private class MoveTo : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x = (operands[0] as PdfNumber).floatValue()
            val y = (operands[1] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.MOVETO, Arrays.asList(x, y))
        }
    }

    /**
     * A content operator implementation (l).

     * @since 5.5.6
     */
    private class LineTo : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x = (operands[0] as PdfNumber).floatValue()
            val y = (operands[1] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.LINETO, Arrays.asList(x, y))
        }
    }

    /**
     * A content operator implementation (c).

     * @since 5.5.6
     */
    private class Curve : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x1 = (operands[0] as PdfNumber).floatValue()
            val y1 = (operands[1] as PdfNumber).floatValue()
            val x2 = (operands[2] as PdfNumber).floatValue()
            val y2 = (operands[3] as PdfNumber).floatValue()
            val x3 = (operands[4] as PdfNumber).floatValue()
            val y3 = (operands[5] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.CURVE_123, Arrays.asList(x1, y1, x2, y2, x3, y3))
        }
    }

    /**
     * A content operator implementation (v).

     * @since 5.5.6
     */
    private class CurveFirstPointDuplicated : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x2 = (operands[0] as PdfNumber).floatValue()
            val y2 = (operands[1] as PdfNumber).floatValue()
            val x3 = (operands[2] as PdfNumber).floatValue()
            val y3 = (operands[3] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.CURVE_23, Arrays.asList(x2, y2, x3, y3))
        }
    }

    /**
     * A content operator implementation (y).

     * @since 5.5.6
     */
    private class CurveFourhPointDuplicated : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x1 = (operands[0] as PdfNumber).floatValue()
            val y1 = (operands[1] as PdfNumber).floatValue()
            val x3 = (operands[2] as PdfNumber).floatValue()
            val y3 = (operands[3] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.CURVE_13, Arrays.asList(x1, y1, x3, y3))
        }
    }

    /**
     * A content operator implementation (h).

     * @since 5.5.6
     */
    private class CloseSubpath : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.modifyPath(PathConstructionRenderInfo.CLOSE, null)
        }
    }

    /**
     * A content operator implementation (re).

     * @since 5.5.6
     */
    private class Rectangle : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            val x = (operands[0] as PdfNumber).floatValue()
            val y = (operands[1] as PdfNumber).floatValue()
            val w = (operands[2] as PdfNumber).floatValue()
            val h = (operands[3] as PdfNumber).floatValue()
            processor.modifyPath(PathConstructionRenderInfo.RECT, Arrays.asList(x, y, w, h))
        }
    }

    /**
     * A content operator implementation (S, s, f, F, f*, B, B*, b, b*).

     * @since 5.5.6
     */
    private class PaintPath
    /**
     * Constructs PainPath object.

     * @param operation One of the possible combinations of [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.STROKE]
     * *                  and [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.FILL] values or
     * *                  [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.NO_OP]
     * *
     * @param rule      Either [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.NONZERO_WINDING_RULE] or
     * *                  [com.itextpdf.text.pdf.parser.PathPaintingRenderInfo.EVEN_ODD_RULE]
     * *                  In case it isn't applicable pass any value.
     * *
     * @param close     Indicates whether the path should be closed or not.
     */
    (private val operation: Int, private val rule: Int, private val close: Boolean) : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.paintPath(operation, rule, close)
            // TODO: add logic for clipping path (before add it to the graphics state)
        }
    }

    /**
     * A content operator implementation (W, W*)

     * @since 5.5.6
     */
    private class ClipPath(private val rule: Int) : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.clipPath(rule)
        }
    }

    /**
     * A content operator implementation (n).

     * @since 5.5.6
     */
    private class EndPath : ContentOperator {

        @Throws(Exception::class)
        override fun invoke(processor: PdfContentStreamProcessor, operator: PdfLiteral, operands: ArrayList<PdfObject>) {
            processor.paintPath(PathPaintingRenderInfo.NO_OP, -1, false)
        }
    }

    /**
     * An XObject subtype handler for FORM
     */
    private class FormXObjectDoHandler : XObjectDoHandler {

        override fun handleXObject(processor: PdfContentStreamProcessor, stream: PdfStream, ref: PdfIndirectReference) {

            val resources = stream.getAsDict(PdfName.RESOURCES)

            // we read the content bytes up here so if it fails we don't leave the graphics state stack corrupted
            // this is probably not necessary (if we fail on this, probably the entire content stream processing
            // operation should be rejected
            val contentBytes: ByteArray
            try {
                contentBytes = ContentByteUtils.getContentBytesFromContentObject(stream)
            } catch (e1: IOException) {
                throw ExceptionConverter(e1)
            }

            val matrix = stream.getAsArray(PdfName.MATRIX)

            PushGraphicsState().invoke(processor, null, null)

            if (matrix != null) {
                val a = matrix.getAsNumber(0).floatValue()
                val b = matrix.getAsNumber(1).floatValue()
                val c = matrix.getAsNumber(2).floatValue()
                val d = matrix.getAsNumber(3).floatValue()
                val e = matrix.getAsNumber(4).floatValue()
                val f = matrix.getAsNumber(5).floatValue()
                val formMatrix = Matrix(a, b, c, d, e, f)

                processor.gs().ctm = formMatrix.multiply(processor.gs().ctm)
            }

            processor.processContent(contentBytes, resources)

            PopGraphicsState().invoke(processor, null, null)

        }

    }

    /**
     * An XObject subtype handler for IMAGE
     */
    private class ImageXObjectDoHandler : XObjectDoHandler {

        override fun handleXObject(processor: PdfContentStreamProcessor, xobjectStream: PdfStream, ref: PdfIndirectReference) {
            val colorSpaceDic = processor.resources!!.getAsDict(PdfName.COLORSPACE)
            val renderInfo = ImageRenderInfo.createForXObject(processor.gs(), ref, colorSpaceDic)
            processor.renderListener.renderImage(renderInfo)
        }
    }

    /**
     * An XObject subtype handler that does nothing
     */
    private class IgnoreXObjectDoHandler : XObjectDoHandler {
        override fun handleXObject(processor: PdfContentStreamProcessor, xobjectStream: PdfStream, ref: PdfIndirectReference) {
            // ignore XObject subtype
        }
    }

    companion object {
        /**
         * Default operator
         * @since 5.0.1
         */
        val DEFAULTOPERATOR = "DefaultOperator"

        /**
         * Gets a color based on a list of operands.
         */
        private fun getColor(colorSpace: PdfName, operands: List<PdfObject>): BaseColor? {
            if (PdfName.DEVICEGRAY == colorSpace) {
                return getColor(1, operands)
            }
            if (PdfName.DEVICERGB == colorSpace) {
                return getColor(3, operands)
            }
            if (PdfName.DEVICECMYK == colorSpace) {
                return getColor(4, operands)
            }
            return null
        }

        /**
         * Gets a color based on a list of operands.
         */
        private fun getColor(nOperands: Int, operands: List<PdfObject>): BaseColor? {
            val c = FloatArray(nOperands)
            for (i in 0..nOperands - 1) {
                c[i] = (operands[i] as PdfNumber).floatValue()
                // fallbacks for illegal values: handled as Acrobat and Foxit do
                if (c[i] > 1f) {
                    c[i] = 1f
                } else if (c[i] < 0f) {
                    c[i] = 0f
                }
            }
            when (nOperands) {
                1 -> return GrayColor(c[0])
                3 -> return BaseColor(c[0], c[1], c[2])
                4 -> return CMYKColor(c[0], c[1], c[2], c[3])
            }
            return null
        }
    }
}
