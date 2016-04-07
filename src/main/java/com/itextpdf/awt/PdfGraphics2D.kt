/*
 * $Id: e9677d0bf2225e179e4f8bb7f2f92dae648c1af4 $
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
package com.itextpdf.awt

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Composite
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Image
import java.awt.MediaTracker
import java.awt.Paint
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Stroke
import java.awt.TexturePaint
import java.awt.Transparency
import java.awt.RenderingHints.Key
import java.awt.color.ColorSpace
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ColorModel
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.WritableRaster
import java.awt.image.renderable.RenderableImage
import java.io.ByteArrayOutputStream
import java.text.AttributedCharacterIterator
import java.util.ArrayList
import java.util.HashMap
import java.util.Hashtable
import java.util.Locale

import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import javax.imageio.stream.ImageOutputStream

import com.itextpdf.awt.geom.PolylineShape
import com.itextpdf.text.BaseColor
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ByteBuffer
import com.itextpdf.text.pdf.CMYKColor
import com.itextpdf.text.pdf.PdfAction
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfGState
import com.itextpdf.text.pdf.PdfPatternPainter
import com.itextpdf.text.pdf.PdfShading
import com.itextpdf.text.pdf.PdfShadingPattern

open class PdfGraphics2D : Graphics2D {
    private var strokeOne = BasicStroke(1f)

    protected var font: Font
    protected var baseFont: BaseFont
    protected var fontSize: Float = 0.toFloat()
    protected var transform: AffineTransform
    protected var paint: Paint
    protected var background: Color
    protected var width: Float = 0.toFloat()
    protected var height: Float = 0.toFloat()

    protected var clip: Area? = null

    protected var rhints = RenderingHints(null)

    protected var stroke: Stroke
    protected var originalStroke: Stroke

    var content: PdfContentByte
        protected set

    /** Storage for BaseFont objects created.  */
    protected var baseFonts: HashMap<String, BaseFont>

    protected var disposeCalled = false

    protected var fontMapper: FontMapper? = null

    private class Kid internal constructor(internal val pos: Int, internal val graphics: PdfGraphics2D)

    private var kids: ArrayList<Kid>? = null

    private var kid = false

    private var dg2: Graphics2D? = null

    private var onlyShapes = false

    private var oldStroke: Stroke? = null
    private var paintFill: Paint? = null
    private var paintStroke: Paint? = null

    private var mediaTracker: MediaTracker? = null

    // Added by Jurij Bilas
    protected var underline: Boolean = false          // indicates if the font style is underlined
    // Added by Peter Severin
    /** @since 5.0.3
     */
    protected var strikethrough: Boolean = false

    protected var fillGState: Array<PdfGState>
    protected var strokeGState: Array<PdfGState>
    protected var currentFillGState = 255
    protected var currentStrokeGState = 255

    private var convertImagesToJPEG = false
    private var jpegQuality = .95f

    // Added by Alexej Suchov
    private var alpha: Float = 0.toFloat()

    // Added by Alexej Suchov
    private var composite: Composite? = null

    // Added by Alexej Suchov
    private var realPaint: Paint? = null

    /**
     * Method that creates a Graphics2D object.
     * Contributed by Peter Harvey: he moved code from the constructor to a separate method
     * @since 5.0.2
     */
    private val dG2: Graphics2D
        get() {
            if (dg2 == null) {
                dg2 = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).createGraphics()
                dg2!!.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
                setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
                setRenderingHint(HyperLinkKey.KEY_INSTANCE, HyperLinkKey.VALUE_HYPERLINKKEY_OFF)
            }
            return dg2
        }

    private constructor() {
    }

    constructor(cb: PdfContentByte, width: Float, height: Float, onlyShapes: Boolean) : this(cb, width, height, null, onlyShapes, false, 0f) {
    }

    /**
     * Constructor for PDFGraphics2D.
     */
    @JvmOverloads constructor(cb: PdfContentByte, width: Float, height: Float, fontMapper: FontMapper? = null, onlyShapes: Boolean = false, convertImagesToJPEG: Boolean = false, quality: Float = 0f) : super() {
        this.fillGState = arrayOfNulls<PdfGState>(256)
        this.strokeGState = arrayOfNulls<PdfGState>(256)
        this.convertImagesToJPEG = convertImagesToJPEG
        this.jpegQuality = quality
        this.onlyShapes = onlyShapes
        this.transform = AffineTransform()
        this.baseFonts = HashMap<String, BaseFont>()
        if (!onlyShapes) {
            this.fontMapper = fontMapper
            if (this.fontMapper == null)
                this.fontMapper = DefaultFontMapper()
        }
        paint = Color.black
        background = Color.white
        setFont(Font("sanserif", Font.PLAIN, 12))
        this.content = cb
        cb.saveState()
        this.width = width
        this.height = height
        clip = Area(Rectangle2D.Float(0f, 0f, width, height))
        clip(clip)
        originalStroke = stroke = oldStroke = strokeOne
        setStrokeDiff(stroke, null)
        cb.saveState()
    }

    /**
     * @see Graphics2D.draw
     */
    override fun draw(s: Shape) {
        followPath(s, STROKE)
    }

    /**
     * @see Graphics2D.drawImage
     */
    override fun drawImage(img: Image, xform: AffineTransform, obs: ImageObserver?): Boolean {
        return drawImage(img, null, xform, null, obs)
    }

    /**
     * @see Graphics2D.drawImage
     */
    override fun drawImage(img: BufferedImage, op: BufferedImageOp?, x: Int, y: Int) {
        var result = img
        if (op != null) {
            result = op.createCompatibleDestImage(img, img.colorModel)
            result = op.filter(img, result)
        }
        drawImage(result, x, y, null)
    }

    /**
     * @see Graphics2D.drawRenderedImage
     */
    override fun drawRenderedImage(img: RenderedImage, xform: AffineTransform) {
        var image: BufferedImage? = null
        if (img is BufferedImage) {
            image = img
        } else {
            val cm = img.colorModel
            val width = img.width
            val height = img.height
            val raster = cm.createCompatibleWritableRaster(width, height)
            val isAlphaPremultiplied = cm.isAlphaPremultiplied
            val properties = Hashtable<String, Any>()
            val keys = img.propertyNames
            if (keys != null) {
                for (key in keys) {
                    properties.put(key, img.getProperty(key))
                }
            }
            val result = BufferedImage(cm, raster, isAlphaPremultiplied, properties)
            img.copyData(raster)
            image = result
        }
        drawImage(image, xform, null)
    }

    /**
     * @see Graphics2D.drawRenderableImage
     */
    override fun drawRenderableImage(img: RenderableImage, xform: AffineTransform) {
        drawRenderedImage(img.createDefaultRendering(), xform)
    }

    /**
     * @see Graphics.drawString
     */
    override fun drawString(s: String, x: Int, y: Int) {
        drawString(s, x.toFloat(), y.toFloat())
    }

    /**
     * This routine goes through the attributes and sets the font
     * before calling the actual string drawing routine
     * @param iter
     */
    @SuppressWarnings("unchecked")
    protected fun doAttributes(iter: AttributedCharacterIterator) {
        underline = false
        strikethrough = false
        for (attribute in iter.attributes.keys) {
            if (attribute !is TextAttribute)
                continue
            if (attribute == TextAttribute.FONT) {
                val font = iter.attributes[attribute] as Font
                setFont(font)
            } else if (attribute == TextAttribute.UNDERLINE) {
                if (iter.attributes[attribute] === TextAttribute.UNDERLINE_ON)
                    underline = true
            } else if (attribute == TextAttribute.STRIKETHROUGH) {
                if (iter.attributes[attribute] === TextAttribute.STRIKETHROUGH_ON)
                    strikethrough = true
            } else if (attribute == TextAttribute.SIZE) {
                val obj = iter.attributes[attribute]
                if (obj is Int) {
                    val i = obj.toInt()
                    setFont(getFont().deriveFont(getFont().style, i.toFloat()))
                } else if (obj is Float) {
                    val f = obj.toFloat()
                    setFont(getFont().deriveFont(getFont().style, f))
                }
            } else if (attribute == TextAttribute.FOREGROUND) {
                color = iter.attributes[attribute] as Color
            } else if (attribute == TextAttribute.FAMILY) {
                val font = getFont()
                val fontAttributes = font.attributes
                fontAttributes.put(TextAttribute.FAMILY, iter.attributes[attribute])
                setFont(font.deriveFont(fontAttributes))
            } else if (attribute == TextAttribute.POSTURE) {
                val font = getFont()
                val fontAttributes = font.attributes
                fontAttributes.put(TextAttribute.POSTURE, iter.attributes[attribute])
                setFont(font.deriveFont(fontAttributes))
            } else if (attribute == TextAttribute.WEIGHT) {
                val font = getFont()
                val fontAttributes = font.attributes
                fontAttributes.put(TextAttribute.WEIGHT, iter.attributes[attribute])
                setFont(font.deriveFont(fontAttributes))
            }
        }
    }

    /**
     * @see Graphics2D.drawString
     */
    override fun drawString(s: String, x: Float, y: Float) {
        var y = y
        if (s.length == 0)
            return
        setFillPaint()
        if (onlyShapes) {
            drawGlyphVector(this.font.layoutGlyphVector(fontRenderContext, s.toCharArray(), 0, s.length, java.awt.Font.LAYOUT_LEFT_TO_RIGHT), x, y)
            //            Use the following line to compile in JDK 1.3
            //            drawGlyphVector(this.font.createGlyphVector(getFontRenderContext(), s), x, y);
        } else {
            var restoreTextRenderingMode = false
            // we want an untarnished clone of the transformation for use with
            // underline & strikethrough
            val at = getTransform()
            // this object will be manipulated in case of rotation, skewing, etc.
            val at2 = getTransform()
            at2.translate(x.toDouble(), y.toDouble())
            at2.concatenate(font.transform)
            setTransform(at2)
            val inverse = this.normalizeMatrix()
            val flipper = AffineTransform.getScaleInstance(1.0, -1.0)
            inverse.concatenate(flipper)
            content.beginText()
            content.setFontAndSize(baseFont, fontSize)
            // Check if we need to simulate an italic font.
            if (font.isItalic) {
                val angle = baseFont.getFontDescriptor(BaseFont.ITALICANGLE, 1000f)
                var angle2 = font.italicAngle
                // When there are different fonts for italic, bold, italic bold
                // the font.getName() will be different from the font.getFontName()
                // value. When they are the same value then we are normally dealing
                // with a single font that has been made into an italic or bold
                // font. When there are only a plain and a bold font available,
                // we need to enter this logic too. This should be identifiable
                // by the baseFont's and font's italic angles being 0.
                if (font.fontName == font.name || angle == 0f && angle2 == 0f) {
                    // We don't have an italic version of this font, so we need
                    // to set the font angle ourselves to produce an italic font.
                    if (angle2 == 0f) {
                        // The JavaVM didn't find an angle setting for making
                        // the font an italic font so use a default italic
                        // angle of 10 degrees.
                        angle2 = 10.0f
                    } else {
                        // This sign of the angle for Java and PDF
                        // seems to be reversed.
                        angle2 = -angle2
                    }
                    if (angle == 0f) {
                        // We need to concatenate the skewing transformation to
                        // the original ones.
                        val skewing = AffineTransform()
                        skewing.setTransform(1.0, 0.0, Math.tan(angle2 * Math.PI / 180).toFloat().toDouble(), 1.0, 0.0, 0.0)
                        inverse.concatenate(skewing)
                    }
                }
            }
            // We must wait to fetch the transformation matrix until after the
            // potential skewing transformation
            val mx = DoubleArray(6)
            inverse.getMatrix(mx)
            content.setTextMatrix(mx[0].toFloat(), mx[1].toFloat(), mx[2].toFloat(), mx[3].toFloat(), mx[4].toFloat(), mx[5].toFloat())
            var fontTextAttributeWidth: Float? = font.attributes[TextAttribute.WIDTH] as Float
            fontTextAttributeWidth = if (fontTextAttributeWidth == null)
                TextAttribute.WIDTH_REGULAR
            else
                fontTextAttributeWidth
            if (TextAttribute.WIDTH_REGULAR != fontTextAttributeWidth)
                content.horizontalScaling = 100.0f / fontTextAttributeWidth!!.toFloat()

            // Check if we need to simulate a bold font.
            // Do nothing if the BaseFont is already bold. This test is not foolproof but it will work most of the times.
            if (baseFont.postscriptFontName.toLowerCase().indexOf("bold") < 0) {
                // Get the weight of the font so we can detect fonts with a weight
                // that makes them bold, while there is only a single font file.
                var weight: Float? = font.attributes[TextAttribute.WEIGHT] as Float
                if (weight == null) {
                    weight = if (font.isBold)
                        TextAttribute.WEIGHT_BOLD
                    else
                        TextAttribute.WEIGHT_REGULAR
                }
                if (font.isBold && (weight!!.toFloat() >= TextAttribute.WEIGHT_SEMIBOLD!!.toFloat() || font.fontName == font.name)) {
                    // Simulate a bold font.
                    val strokeWidth = font.size2D * (weight.toFloat() - TextAttribute.WEIGHT_REGULAR!!.toFloat()) / 20f
                    if (realPaint is Color) {
                        content.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE)
                        content.setLineWidth(strokeWidth)
                        val color = realPaint as Color?
                        val alpha = color.getAlpha()
                        if (alpha != currentStrokeGState) {
                            currentStrokeGState = alpha
                            var gs: PdfGState? = strokeGState[alpha]
                            if (gs == null) {
                                gs = PdfGState()
                                gs.setStrokeOpacity(alpha / 255f)
                                strokeGState[alpha] = gs
                            }
                            content.setGState(gs)
                        }
                        content.setColorStroke(prepareColor(color))
                        restoreTextRenderingMode = true
                    }
                }
            }

            var width = 0.0
            if (font.size2D > 0) {
                val scale = 1000 / font.size2D
                val derivedFont = font.deriveFont(AffineTransform.getScaleInstance(scale.toDouble(), scale.toDouble()))
                width = derivedFont.getStringBounds(s, fontRenderContext).width
                if (derivedFont.isTransformed)
                    width /= scale.toDouble()
            }
            // if the hyperlink flag is set add an action to the text
            val url = getRenderingHint(HyperLinkKey.KEY_INSTANCE)
            if (url != null && url != HyperLinkKey.VALUE_HYPERLINKKEY_OFF) {
                val scale = 1000 / font.size2D
                val derivedFont = font.deriveFont(AffineTransform.getScaleInstance(scale.toDouble(), scale.toDouble()))
                var height = derivedFont.getStringBounds(s, fontRenderContext).height
                if (derivedFont.isTransformed)
                    height /= scale.toDouble()
                val leftX = content.xtlm.toDouble()
                val leftY = content.ytlm.toDouble()
                val action = PdfAction(url.toString())
                content.setAction(action, leftX.toFloat(), leftY.toFloat(), (leftX + width).toFloat(), (leftY + height).toFloat())
            }
            if (s.length > 1) {
                val adv = (width.toFloat() - baseFont.getWidthPoint(s, fontSize)) / (s.length - 1)
                content.characterSpacing = adv
            }
            content.showText(s)
            if (s.length > 1) {
                content.characterSpacing = 0f
            }
            if (TextAttribute.WIDTH_REGULAR != fontTextAttributeWidth)
                content.horizontalScaling = 100f

            // Restore the original TextRenderingMode if needed.
            if (restoreTextRenderingMode) {
                content.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL)
            }

            content.endText()
            setTransform(at)
            if (underline) {
                // These two are supposed to be taken from the .AFM file
                //int UnderlinePosition = -100;
                val UnderlineThickness = 50
                //
                val d = asPoints(UnderlineThickness.toDouble(), fontSize.toInt())
                val savedStroke = originalStroke
                setStroke(BasicStroke(d.toFloat()))
                // Setting of the underline must be 2 times the d-value, 
                // otherwise it might be too close to the text
                // esp. in case of a manually created bold font.
                val lineY = (y + d * 2).toFloat()
                val line = Line2D.Double(x.toDouble(), lineY.toDouble(), width + x, lineY.toDouble())
                draw(line)
                setStroke(savedStroke)
            }
            if (strikethrough) {
                // These two are supposed to be taken from the .AFM file
                val StrikethroughThickness = 50
                val StrikethroughPosition = 350
                //
                val d = asPoints(StrikethroughThickness.toDouble(), fontSize.toInt())
                val p = asPoints(StrikethroughPosition.toDouble(), fontSize.toInt())
                val savedStroke = originalStroke
                setStroke(BasicStroke(d.toFloat()))
                y = (y + asPoints(StrikethroughThickness.toDouble(), fontSize.toInt())).toFloat()
                val line = Line2D.Double(x.toDouble(), y - p, width + x, y - p)
                draw(line)
                setStroke(savedStroke)
            }
        }
    }

    /**
     * @see Graphics.drawString
     */
    override fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int) {
        drawString(iterator, x.toFloat(), y.toFloat())
    }

    /**
     * @see Graphics2D.drawString
     */
    override fun drawString(iter: AttributedCharacterIterator, x: Float, y: Float) {
        var x = x
        /*
        StringBuffer sb = new StringBuffer();
        for(char c = iter.first(); c != AttributedCharacterIterator.DONE; c = iter.next()) {
            sb.append(c);
        }
        drawString(sb.toString(),x,y);
*/
        val stringbuffer = StringBuffer(iter.endIndex)
        var c = iter.first()
        while (c != '\uFFFF') {
            if (iter.index == iter.runStart) {
                if (stringbuffer.length > 0) {
                    drawString(stringbuffer.toString(), x, y)
                    val fontmetrics = fontMetrics
                    x = (x + fontmetrics.getStringBounds(stringbuffer.toString(), this).width).toFloat()
                    stringbuffer.delete(0, stringbuffer.length)
                }
                doAttributes(iter)
            }
            stringbuffer.append(c)
            c = iter.next()
        }

        drawString(stringbuffer.toString(), x, y)
        underline = false
        strikethrough = false
    }

    /**
     * @see Graphics2D.drawGlyphVector
     */
    override fun drawGlyphVector(g: GlyphVector, x: Float, y: Float) {
        val s = g.getOutline(x, y)
        fill(s)
    }

    /**
     * @see Graphics2D.fill
     */
    override fun fill(s: Shape) {
        followPath(s, FILL)
    }

    /**
     * @see Graphics2D.hit
     */
    override fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean {
        var s = s
        if (onStroke) {
            s = stroke.createStrokedShape(s)
        }
        s = transform.createTransformedShape(s)
        val area = Area(s)
        if (clip != null)
            area.intersect(clip)
        return area.intersects(rect.x.toDouble(), rect.y.toDouble(), rect.width.toDouble(), rect.height.toDouble())
    }

    /**
     * @see Graphics2D.getDeviceConfiguration
     */
    override fun getDeviceConfiguration(): GraphicsConfiguration {
        return dG2.deviceConfiguration
    }

    /**
     * Method contributed by Alexej Suchov
     * @see Graphics2D.setComposite
     */
    override fun setComposite(comp: Composite) {

        if (comp is AlphaComposite) {

            if (comp.rule == 3) {

                alpha = comp.alpha
                this.composite = comp

                if (realPaint != null && realPaint is Color) {

                    val c = realPaint as Color?
                    paint = Color(c.getRed(), c.getGreen(), c.getBlue(),
                            (c.getAlpha() * alpha).toInt())
                }
                return
            }
        }

        this.composite = comp
        alpha = 1.0f

    }

    /**
     * Method contributed by Alexej Suchov
     * @see Graphics2D.setPaint
     */
    override fun setPaint(paint: Paint?) {
        if (paint == null)
            return
        this.paint = paint
        realPaint = paint

        if (composite is AlphaComposite && paint is Color) {

            val co = composite as AlphaComposite?

            if (co.getRule() == 3) {
                this.paint = Color(paint.red, paint.green, paint.blue, (paint.alpha * alpha).toInt())
                realPaint = paint
            }
        }

    }

    private fun transformStroke(stroke: Stroke): Stroke {
        if (stroke !is BasicStroke)
            return stroke
        val scale = Math.sqrt(Math.abs(transform.determinant)).toFloat()
        val dash = stroke.dashArray
        if (dash != null) {
            for (k in dash.indices)
                dash[k] *= scale
        }
        return BasicStroke(stroke.lineWidth * scale, stroke.endCap, stroke.lineJoin, stroke.miterLimit, dash, stroke.dashPhase * scale)
    }

    private fun setStrokeDiff(newStroke: Stroke, oldStroke: Stroke?) {
        if (newStroke === oldStroke)
            return
        if (newStroke !is BasicStroke)
            return
        val oldOk = oldStroke is BasicStroke
        var oStroke: BasicStroke? = null
        if (oldOk)
            oStroke = oldStroke as BasicStroke?
        if (!oldOk || newStroke.lineWidth != oStroke!!.lineWidth)
            content.setLineWidth(newStroke.lineWidth)
        if (!oldOk || newStroke.endCap != oStroke!!.endCap) {
            when (newStroke.endCap) {
                BasicStroke.CAP_BUTT -> content.setLineCap(0)
                BasicStroke.CAP_SQUARE -> content.setLineCap(2)
                else -> content.setLineCap(1)
            }
        }
        if (!oldOk || newStroke.lineJoin != oStroke!!.lineJoin) {
            when (newStroke.lineJoin) {
                BasicStroke.JOIN_MITER -> content.setLineJoin(0)
                BasicStroke.JOIN_BEVEL -> content.setLineJoin(2)
                else -> content.setLineJoin(1)
            }
        }
        if (!oldOk || newStroke.miterLimit != oStroke!!.miterLimit)
            content.setMiterLimit(newStroke.miterLimit)
        val makeDash: Boolean
        if (oldOk) {
            if (newStroke.dashArray != null) {
                if (newStroke.dashPhase != oStroke!!.dashPhase) {
                    makeDash = true
                } else if (!java.util.Arrays.equals(newStroke.dashArray, oStroke.dashArray)) {
                    makeDash = true
                } else
                    makeDash = false
            } else if (oStroke!!.dashArray != null) {
                makeDash = true
            } else
                makeDash = false
        } else {
            makeDash = true
        }
        if (makeDash) {
            val dash = newStroke.dashArray
            if (dash == null)
                content.setLiteral("[]0 d\n")
            else {
                content.setLiteral('[')
                val lim = dash.size
                for (k in 0..lim - 1) {
                    content.setLiteral(dash[k])
                    content.setLiteral(' ')
                }
                content.setLiteral(']')
                content.setLiteral(newStroke.dashPhase)
                content.setLiteral(" d\n")
            }
        }
    }

    /**
     * @see Graphics2D.setStroke
     */
    override fun setStroke(s: Stroke) {
        originalStroke = s
        this.stroke = transformStroke(s)
    }


    /**
     * Sets a rendering hint
     * @param arg0
     * *
     * @param arg1
     */
    override fun setRenderingHint(arg0: Key, arg1: Any?) {
        if (arg1 != null) {
            rhints.put(arg0, arg1)
        } else {
            if (arg0 is HyperLinkKey) {
                rhints.put(arg0, HyperLinkKey.VALUE_HYPERLINKKEY_OFF)
            } else {
                rhints.remove(arg0)
            }
        }
    }

    /**
     * @param arg0 a key
     * *
     * @return the rendering hint
     */
    override fun getRenderingHint(arg0: Key): Any? {
        return rhints[arg0]
    }

    /**
     * @see Graphics2D.setRenderingHints
     */
    override fun setRenderingHints(hints: Map<*, *>) {
        rhints.clear()
        rhints.putAll(hints)
    }

    /**
     * @see Graphics2D.addRenderingHints
     */
    override fun addRenderingHints(hints: Map<*, *>) {
        rhints.putAll(hints)
    }

    /**
     * @see Graphics2D.getRenderingHints
     */
    override fun getRenderingHints(): RenderingHints {
        return rhints
    }

    /**
     * @see Graphics.translate
     */
    override fun translate(x: Int, y: Int) {
        translate(x.toDouble(), y.toDouble())
    }

    /**
     * @see Graphics2D.translate
     */
    override fun translate(tx: Double, ty: Double) {
        transform.translate(tx, ty)
    }

    /**
     * @see Graphics2D.rotate
     */
    override fun rotate(theta: Double) {
        transform.rotate(theta)
    }

    /**
     * @see Graphics2D.rotate
     */
    override fun rotate(theta: Double, x: Double, y: Double) {
        transform.rotate(theta, x, y)
    }

    /**
     * @see Graphics2D.scale
     */
    override fun scale(sx: Double, sy: Double) {
        transform.scale(sx, sy)
        this.stroke = transformStroke(originalStroke)
    }

    /**
     * @see Graphics2D.shear
     */
    override fun shear(shx: Double, shy: Double) {
        transform.shear(shx, shy)
    }

    /**
     * @see Graphics2D.transform
     */
    override fun transform(tx: AffineTransform) {
        transform.concatenate(tx)
        this.stroke = transformStroke(originalStroke)
    }

    /**
     * @see Graphics2D.setTransform
     */
    override fun setTransform(t: AffineTransform) {
        transform = AffineTransform(t)
        this.stroke = transformStroke(originalStroke)
    }

    /**
     * @see Graphics2D.getTransform
     */
    override fun getTransform(): AffineTransform {
        return AffineTransform(transform)
    }

    /**
     * Method contributed by Alexej Suchov
     * @see Graphics2D.getPaint
     */
    override fun getPaint(): Paint {
        if (realPaint != null) {
            return realPaint
        } else {
            return paint
        }
    }

    /**
     * @see Graphics2D.getComposite
     */
    override fun getComposite(): Composite {
        return composite
    }

    /**
     * @see Graphics2D.setBackground
     */
    override fun setBackground(color: Color) {
        background = color
    }

    /**
     * @see Graphics2D.getBackground
     */
    override fun getBackground(): Color {
        return background
    }

    /**
     * @see Graphics2D.getStroke
     */
    override fun getStroke(): Stroke {
        return originalStroke
    }


    /**
     * @see Graphics2D.getFontRenderContext
     */
    override fun getFontRenderContext(): FontRenderContext {
        val antialias = RenderingHints.VALUE_TEXT_ANTIALIAS_ON == getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)
        val fractions = RenderingHints.VALUE_FRACTIONALMETRICS_ON == getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS)
        return FontRenderContext(AffineTransform(), antialias, fractions)
    }

    /**
     * @see Graphics.create
     */
    override fun create(): Graphics {
        val g2 = PdfGraphics2D()
        g2.rhints.putAll(this.rhints)
        g2.onlyShapes = this.onlyShapes
        g2.transform = AffineTransform(this.transform)
        g2.baseFonts = this.baseFonts
        g2.fontMapper = this.fontMapper
        g2.paint = this.paint
        g2.fillGState = this.fillGState
        g2.currentFillGState = this.currentFillGState
        g2.strokeGState = this.strokeGState
        g2.background = this.background
        g2.mediaTracker = this.mediaTracker
        g2.convertImagesToJPEG = this.convertImagesToJPEG
        g2.jpegQuality = this.jpegQuality
        g2.setFont(this.font)
        g2.content = this.content.duplicate
        g2.content.saveState()
        g2.width = this.width
        g2.height = this.height
        g2.followPath(Area(Rectangle2D.Float(0f, 0f, width, height)), CLIP)
        if (this.clip != null)
            g2.clip = Area(this.clip)
        g2.composite = composite
        g2.stroke = stroke
        g2.originalStroke = originalStroke
        g2.strokeOne = g2.transformStroke(g2.strokeOne) as BasicStroke
        g2.oldStroke = g2.strokeOne
        g2.setStrokeDiff(g2.oldStroke, null)
        g2.content.saveState()
        if (g2.clip != null)
            g2.followPath(g2.clip, CLIP)
        g2.kid = true
        if (this.kids == null)
            this.kids = ArrayList<Kid>()
        this.kids!!.add(Kid(content.internalBuffer.size(), g2))
        return g2
    }

    /**
     * @see Graphics.getColor
     */
    override fun getColor(): Color {
        if (paint is Color) {
            return paint as Color
        } else {
            return Color.black
        }
    }

    /**
     * @see Graphics.setColor
     */
    override fun setColor(color: Color) {
        setPaint(color)
    }

    /**
     * @see Graphics.setPaintMode
     */
    override fun setPaintMode() {
    }

    /**
     * @see Graphics.setXORMode
     */
    override fun setXORMode(c1: Color) {

    }

    /**
     * @see Graphics.getFont
     */
    override fun getFont(): Font {
        return font
    }

    /**
     * @see Graphics.setFont
     */
    /**
     * Sets the current font.
     */
    override fun setFont(f: Font?) {
        if (f == null)
            return
        if (onlyShapes) {
            font = f
            return
        }
        if (f === font)
            return
        font = f
        fontSize = f.size2D
        baseFont = getCachedBaseFont(f)
    }

    private fun getCachedBaseFont(f: Font): BaseFont {
        synchronized (baseFonts) {
            var bf: BaseFont? = baseFonts[f.fontName]
            if (bf == null) {
                bf = fontMapper!!.awtToPdf(f)
                baseFonts.put(f.fontName, bf)
            }
            return bf
        }
    }

    /**
     * @see Graphics.getFontMetrics
     */
    override fun getFontMetrics(f: Font): FontMetrics {
        return dG2.getFontMetrics(f)
    }

    /**
     * @see Graphics.getClipBounds
     */
    override fun getClipBounds(): Rectangle? {
        if (clip == null)
            return null
        return getClip()!!.bounds
    }

    /**
     * @see Graphics.clipRect
     */
    override fun clipRect(x: Int, y: Int, width: Int, height: Int) {
        val rect = Rectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        clip(rect)
    }

    /**
     * @see Graphics.setClip
     */
    override fun setClip(x: Int, y: Int, width: Int, height: Int) {
        val rect = Rectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        setClip(rect)
    }

    /**
     * @see Graphics2D.clip
     */
    override fun clip(s: Shape?) {
        var s = s
        if (s == null) {
            setClip(null)
            return
        }
        s = transform.createTransformedShape(s)
        if (clip == null)
            clip = Area(s)
        else
            clip!!.intersect(Area(s))
        followPath(s, CLIP)
    }

    /**
     * @see Graphics.getClip
     */
    override fun getClip(): Shape? {
        try {
            return transform.createInverse().createTransformedShape(clip)
        } catch (e: NoninvertibleTransformException) {
            return null
        }

    }

    /**
     * @see Graphics.setClip
     */
    override fun setClip(s: Shape?) {
        var s = s
        content.restoreState()
        content.saveState()
        if (s != null)
            s = transform.createTransformedShape(s)
        if (s == null) {
            clip = null
        } else {
            clip = Area(s)
            followPath(s, CLIP)
        }
        paintFill = paintStroke = null
        currentFillGState = currentStrokeGState = -1
        oldStroke = strokeOne
    }

    /**
     * @see Graphics.copyArea
     */
    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {

    }

    /**
     * @see Graphics.drawLine
     */
    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        val line = Line2D.Double(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        draw(line)
    }

    /**
     * @see Graphics.fillRect
     */
    override fun drawRect(x: Int, y: Int, width: Int, height: Int) {
        draw(Rectangle(x, y, width, height))
    }

    /**
     * @see Graphics.fillRect
     */
    override fun fillRect(x: Int, y: Int, width: Int, height: Int) {
        fill(Rectangle(x, y, width, height))
    }

    /**
     * @see Graphics.clearRect
     */
    override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
        val temp = paint
        setPaint(background)
        fillRect(x, y, width, height)
        setPaint(temp)
    }

    /**
     * @see Graphics.drawRoundRect
     */
    override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
        val rect = RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble())
        draw(rect)
    }

    /**
     * @see Graphics.fillRoundRect
     */
    override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
        val rect = RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble())
        fill(rect)
    }

    /**
     * @see Graphics.drawOval
     */
    override fun drawOval(x: Int, y: Int, width: Int, height: Int) {
        val oval = Ellipse2D.Float(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        draw(oval)
    }

    /**
     * @see Graphics.fillOval
     */
    override fun fillOval(x: Int, y: Int, width: Int, height: Int) {
        val oval = Ellipse2D.Float(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        fill(oval)
    }

    /**
     * @see Graphics.drawArc
     */
    override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
        val arc = Arc2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.OPEN)
        draw(arc)

    }

    /**
     * @see Graphics.fillArc
     */
    override fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
        val arc = Arc2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.PIE)
        fill(arc)
    }

    /**
     * @see Graphics.drawPolyline
     */
    override fun drawPolyline(x: IntArray, y: IntArray, nPoints: Int) {
        val polyline = PolylineShape(x, y, nPoints)
        draw(polyline)
    }

    /**
     * @see Graphics.drawPolygon
     */
    override fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
        val poly = Polygon(xPoints, yPoints, nPoints)
        draw(poly)
    }

    /**
     * @see Graphics.fillPolygon
     */
    override fun fillPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
        val poly = Polygon()
        for (i in 0..nPoints - 1) {
            poly.addPoint(xPoints[i], yPoints[i])
        }
        fill(poly)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, x: Int, y: Int, observer: ImageObserver?): Boolean {
        return drawImage(img, x, y, null, observer)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver): Boolean {
        return drawImage(img, x, y, width, height, null, observer)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color?, observer: ImageObserver): Boolean {
        waitForImage(img)
        return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, x: Int, y: Int, width: Int, height: Int, bgcolor: Color?, observer: ImageObserver): Boolean {
        waitForImage(img)
        val scalex = width / img.getWidth(observer).toDouble()
        val scaley = height / img.getHeight(observer).toDouble()
        val tx = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
        tx.scale(scalex, scaley)
        return drawImage(img, null, tx, bgcolor, observer)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver): Boolean {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer)
    }

    /**
     * @see Graphics.drawImage
     */
    override fun drawImage(img: Image, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color?, observer: ImageObserver): Boolean {
        waitForImage(img)
        val dwidth = dx2.toDouble() - dx1
        val dheight = dy2.toDouble() - dy1
        val swidth = sx2.toDouble() - sx1
        val sheight = sy2.toDouble() - sy1

        //if either width or height is 0, then there is nothing to draw
        if (dwidth == 0.0 || dheight == 0.0 || swidth == 0.0 || sheight == 0.0) return true

        val scalex = dwidth / swidth
        val scaley = dheight / sheight

        val transx = sx1 * scalex
        val transy = sy1 * scaley
        val tx = AffineTransform.getTranslateInstance(dx1 - transx, dy1 - transy)
        tx.scale(scalex, scaley)

        val mask = BufferedImage(img.getWidth(observer), img.getHeight(observer), BufferedImage.TYPE_BYTE_BINARY)
        val g = mask.graphics
        g.fillRect(sx1, sy1, swidth.toInt(), sheight.toInt())
        drawImage(img, mask, tx, null, observer)
        g.dispose()
        return true
    }

    /**
     * @see Graphics.dispose
     */
    override fun dispose() {
        if (kid)
            return
        if (!disposeCalled) {
            disposeCalled = true
            content.restoreState()
            content.restoreState()
            if (dg2 != null) {
                dg2!!.dispose()
                dg2 = null
            }
            if (kids != null) {
                val buf = ByteBuffer()
                internalDispose(buf)
                val buf2 = content.internalBuffer
                buf2.reset()
                buf2.append(buf)
            }
        }
    }

    private fun internalDispose(buf: ByteBuffer) {
        var last = 0
        var pos = 0
        val buf2 = content.internalBuffer
        if (kids != null) {
            for (kid in kids!!) {
                pos = kid.pos
                val g2 = kid.graphics
                g2.content.restoreState()
                g2.content.restoreState()
                buf.append(buf2.buffer, last, pos - last)
                if (g2.dg2 != null) {
                    g2.dg2!!.dispose()
                    g2.dg2 = null
                }
                g2.internalDispose(buf)
                last = pos
            }
        }
        buf.append(buf2.buffer, last, buf2.size() - last)
    }

    ///////////////////////////////////////////////
    //
    //
    //		implementation specific methods
    //
    //


    private fun followPath(s: Shape?, drawType: Int) {
        var s: Shape? = s ?: return
        if (drawType == STROKE) {
            if (stroke !is BasicStroke) {
                s = stroke.createStrokedShape(s)
                followPath(s, FILL)
                return
            }
        }
        if (drawType == STROKE) {
            setStrokeDiff(stroke, oldStroke)
            oldStroke = stroke
            setStrokePaint()
        } else if (drawType == FILL)
            setFillPaint()
        val points: PathIterator
        var traces = 0
        if (drawType == CLIP)
            points = s.getPathIterator(IDENTITY)
        else
            points = s.getPathIterator(transform)
        val coords = FloatArray(6)
        val dcoords = DoubleArray(6)
        while (!points.isDone) {
            ++traces
            // Added by Peter Harvey (start)
            val segtype = points.currentSegment(dcoords)
            val numpoints = if (segtype == PathIterator.SEG_CLOSE)
                0
            else
                if (segtype == PathIterator.SEG_QUADTO)
                    2
                else
                    if (segtype == PathIterator.SEG_CUBICTO)
                        3
                    else
                        1
            for (i in 0..numpoints * 2 - 1) {
                coords[i] = dcoords[i].toFloat()
            }
            // Added by Peter Harvey (end)
            normalizeY(coords)
            when (segtype) {
                PathIterator.SEG_CLOSE -> content.closePath()

                PathIterator.SEG_CUBICTO -> content.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])

                PathIterator.SEG_LINETO -> content.lineTo(coords[0], coords[1])

                PathIterator.SEG_MOVETO -> content.moveTo(coords[0], coords[1])

                PathIterator.SEG_QUADTO -> content.curveTo(coords[0], coords[1], coords[2], coords[3])
            }
            points.next()
        }
        when (drawType) {
            FILL -> if (traces > 0) {
                if (points.windingRule == PathIterator.WIND_EVEN_ODD)
                    content.eoFill()
                else
                    content.fill()
            }
            STROKE -> if (traces > 0)
                content.stroke()
            else //drawType==CLIP
            -> {
                if (traces == 0)
                    content.rectangle(0f, 0f, 0f, 0f)
                if (points.windingRule == PathIterator.WIND_EVEN_ODD)
                    content.eoClip()
                else
                    content.clip()
                content.newPath()
            }
        }
    }

    private fun normalizeY(y: Float): Float {
        return this.height - y
    }

    private fun normalizeY(coords: FloatArray) {
        coords[1] = normalizeY(coords[1])
        coords[3] = normalizeY(coords[3])
        coords[5] = normalizeY(coords[5])
    }

    protected fun normalizeMatrix(): AffineTransform {
        val mx = DoubleArray(6)
        var result = AffineTransform.getTranslateInstance(0.0, 0.0)
        result.getMatrix(mx)
        mx[3] = -1.0
        mx[5] = height.toDouble()
        result = AffineTransform(mx)
        result.concatenate(transform)
        return result
    }

    private fun drawImage(img: Image, mask: Image?, xform: AffineTransform?, bgColor: Color?, obs: ImageObserver): Boolean {
        var xform = xform
        if (xform == null)
            xform = AffineTransform()
        else
            xform = AffineTransform(xform)
        xform.translate(0.0, img.getHeight(obs).toDouble())
        xform.scale(img.getWidth(obs).toDouble(), img.getHeight(obs).toDouble())

        val inverse = this.normalizeMatrix()
        val flipper = AffineTransform.getScaleInstance(1.0, -1.0)
        inverse.concatenate(xform)
        inverse.concatenate(flipper)

        val mx = DoubleArray(6)
        inverse.getMatrix(mx)
        if (currentFillGState != 255) {
            var gs: PdfGState? = fillGState[255]
            if (gs == null) {
                gs = PdfGState()
                gs.setFillOpacity(1f)
                fillGState[255] = gs
            }
            content.setGState(gs)
        }

        try {
            var image: com.itextpdf.text.Image? = null
            if (!convertImagesToJPEG) {
                image = com.itextpdf.text.Image.getInstance(img, bgColor)
            } else {
                var scaled: BufferedImage? = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB)
                val g3 = scaled!!.createGraphics()
                g3.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null), null)
                g3.dispose()

                val baos = ByteArrayOutputStream()
                val iwparam = JPEGImageWriteParam(Locale.getDefault())
                iwparam.compressionMode = ImageWriteParam.MODE_EXPLICIT
                iwparam.compressionQuality = jpegQuality//Set here your compression rate
                val iw = ImageIO.getImageWritersByFormatName("jpg").next()
                val ios = ImageIO.createImageOutputStream(baos)
                iw.output = ios
                iw.write(null, IIOImage(scaled, null, null), iwparam)
                iw.dispose()
                ios.close()

                scaled.flush()
                scaled = null
                image = com.itextpdf.text.Image.getInstance(baos.toByteArray())

            }
            if (mask != null) {
                val msk = com.itextpdf.text.Image.getInstance(mask, null, true)
                msk.makeMask()
                msk.isInverted = true
                image!!.imageMask = msk
            }
            content.addImage(image, mx[0].toFloat(), mx[1].toFloat(), mx[2].toFloat(), mx[3].toFloat(), mx[4].toFloat(), mx[5].toFloat())
            val url = getRenderingHint(HyperLinkKey.KEY_INSTANCE)
            if (url != null && url != HyperLinkKey.VALUE_HYPERLINKKEY_OFF) {
                val action = PdfAction(url.toString())
                content.setAction(action, mx[4].toFloat(), mx[5].toFloat(), (mx[0] + mx[4]).toFloat(), (mx[3] + mx[5]).toFloat())
            }
        } catch (ex: Exception) {
            throw IllegalArgumentException(ex)
        }

        if (currentFillGState >= 0 && currentFillGState != 255) {
            val gs = fillGState[currentFillGState]
            content.setGState(gs)
        }
        return true
    }

    private fun checkNewPaint(oldPaint: Paint): Boolean {
        if (paint === oldPaint)
            return false
        return !(paint is Color && paint == oldPaint)
    }

    private fun setFillPaint() {
        if (checkNewPaint(paintFill)) {
            paintFill = paint
            setPaint(false, 0.0, 0.0, true)
        }
    }

    private fun setStrokePaint() {
        if (checkNewPaint(paintStroke)) {
            paintStroke = paint
            setPaint(false, 0.0, 0.0, false)
        }
    }

    private fun setPaint(invert: Boolean, xoffset: Double, yoffset: Double, fill: Boolean) {
        if (paint is Color) {
            val color = paint as Color
            val alpha = color.alpha
            if (fill) {
                if (alpha != currentFillGState) {
                    currentFillGState = alpha
                    var gs: PdfGState? = fillGState[alpha]
                    if (gs == null) {
                        gs = PdfGState()
                        gs.setFillOpacity(alpha / 255f)
                        fillGState[alpha] = gs
                    }
                    content.setGState(gs)
                }
                content.setColorFill(prepareColor(color))
            } else {
                if (alpha != currentStrokeGState) {
                    currentStrokeGState = alpha
                    var gs: PdfGState? = strokeGState[alpha]
                    if (gs == null) {
                        gs = PdfGState()
                        gs.setStrokeOpacity(alpha / 255f)
                        strokeGState[alpha] = gs
                    }
                    content.setGState(gs)
                }
                content.setColorStroke(prepareColor(color))
            }
        } else if (paint is GradientPaint) {
            val gp = paint as GradientPaint
            val p1 = gp.point1
            transform.transform(p1, p1)
            val p2 = gp.point2
            transform.transform(p2, p2)
            val c1 = gp.color1
            val c2 = gp.color2
            val shading = PdfShading.simpleAxial(content.pdfWriter, p1.x.toFloat(), normalizeY(p1.y.toFloat()), p2.x.toFloat(), normalizeY(p2.y.toFloat()), BaseColor(c1.rgb), BaseColor(c2.rgb))
            val pat = PdfShadingPattern(shading)
            if (fill)
                content.setShadingFill(pat)
            else
                content.setShadingStroke(pat)
        } else if (paint is TexturePaint) {
            try {
                val tp = paint as TexturePaint
                val img = tp.image
                val rect = tp.anchorRect
                val image = com.itextpdf.text.Image.getInstance(img, null)
                val pattern = content.createPattern(image.width, image.height)
                val inverse = this.normalizeMatrix()
                inverse.translate(rect.x, rect.y)
                inverse.scale(rect.width / image.width, -rect.height / image.height)
                val mx = DoubleArray(6)
                inverse.getMatrix(mx)
                pattern.setPatternMatrix(mx[0].toFloat(), mx[1].toFloat(), mx[2].toFloat(), mx[3].toFloat(), mx[4].toFloat(), mx[5].toFloat())
                image.setAbsolutePosition(0f, 0f)
                pattern.addImage(image)
                if (fill)
                    content.setPatternFill(pattern)
                else
                    content.setPatternStroke(pattern)
            } catch (ex: Exception) {
                if (fill)
                    content.setColorFill(BaseColor.GRAY)
                else
                    content.setColorStroke(BaseColor.GRAY)
            }

        } else {
            try {
                var img: BufferedImage? = null
                var type = BufferedImage.TYPE_4BYTE_ABGR
                if (paint.transparency == Transparency.OPAQUE) {
                    type = BufferedImage.TYPE_3BYTE_BGR
                }
                img = BufferedImage(width.toInt(), height.toInt(), type)
                var g: Graphics2D? = img.graphics as Graphics2D
                g!!.transform(transform)
                val inv = transform.createInverse()
                var fillRect: Shape = Rectangle2D.Double(0.0, 0.0, img.width.toDouble(), img.height.toDouble())
                fillRect = inv.createTransformedShape(fillRect)
                g.paint = paint
                g.fill(fillRect)
                if (invert) {
                    val tx = AffineTransform()
                    tx.scale(1.0, -1.0)
                    tx.translate(-xoffset, -yoffset)
                    g.drawImage(img, tx, null)
                }
                g.dispose()
                g = null
                val image = com.itextpdf.text.Image.getInstance(img, null)
                val pattern = content.createPattern(width, height)
                image.setAbsolutePosition(0f, 0f)
                pattern.addImage(image)
                if (fill)
                    content.setPatternFill(pattern)
                else
                    content.setPatternStroke(pattern)
            } catch (ex: Exception) {
                if (fill)
                    content.setColorFill(BaseColor.GRAY)
                else
                    content.setColorStroke(BaseColor.GRAY)
            }

        }
    }

    @Synchronized private fun waitForImage(image: java.awt.Image) {
        if (mediaTracker == null)
            mediaTracker = MediaTracker(PdfGraphics2D.FakeComponent())
        mediaTracker!!.addImage(image, 0)
        try {
            mediaTracker!!.waitForID(0)
        } catch (e: InterruptedException) {
            // empty on purpose
        }

        mediaTracker!!.removeImage(image)
    }

    private class FakeComponent : Component() {
        companion object {

            private val serialVersionUID = 6450197945596086638L
        }
    }

    /**
     * @since 2.0.8
     */
    class HyperLinkKey protected constructor(arg0: Int) : RenderingHints.Key(arg0) {

        override fun isCompatibleValue(`val`: Any): Boolean {
            return true
        }

        override fun toString(): String {
            return "HyperLinkKey"
        }

        companion object {
            val KEY_INSTANCE = HyperLinkKey(9999)
            val VALUE_HYPERLINKKEY_OFF: Any = "0"
        }
    }

    companion object {

        private val FILL = 1
        private val STROKE = 2
        private val CLIP = 3

        private val IDENTITY = AffineTransform()

        val AFM_DIVISOR = 1000 // used to calculate coordinates

        /**
         * Calculates position and/or stroke thickness depending on the font size
         * @param d value to be converted
         * *
         * @param i font size
         * *
         * @return position and/or stroke thickness depending on the font size
         */
        fun asPoints(d: Double, i: Int): Double {
            return d * i / AFM_DIVISOR
        }

        fun prepareColor(color: Color): BaseColor {
            if (color.colorSpace.type == ColorSpace.TYPE_CMYK) {
                val comp = color.getColorComponents(null)
                return CMYKColor(comp[0], comp[1], comp[2], comp[3])
            } else {
                return BaseColor(color.rgb)
            }
        }
    }

}
