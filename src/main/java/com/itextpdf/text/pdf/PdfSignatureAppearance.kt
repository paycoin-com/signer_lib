/*
 * $Id: 0ef3d88a9bd71c4772171ecc4326c7374c696a62 $
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

import com.itextpdf.text.Chunk
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.Version
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.RASInputStream
import com.itextpdf.text.io.RandomAccessSource
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.AcroFields.Item
import com.itextpdf.text.pdf.interfaces.PdfVersion
import com.itextpdf.text.pdf.security.CertificateInfo
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name

import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.HashMap

/**
 * Class that takes care of the cryptographic options
 * and appearances that form a signature.
 */
class PdfSignatureAppearance
/**
 * Constructs a PdfSignatureAppearance object.
 * @param writer    the writer to which the signature will be written.
 */
internal constructor(
        /** The PdfStamperImp object corresponding with the stamper.  */
        private val writer: PdfStamperImp) {

    init {
        signDate = GregorianCalendar()
        fieldName = newSigName
        signatureCreator = Version.getInstance().version
    }

    /** The certification level  */
    /**
     * Gets the certified status of this document.
     * @return the certified status
     */
    /**
     * Sets the document type to certified instead of simply signed.
     * @param certificationLevel the values can be: `NOT_CERTIFIED`, `CERTIFIED_NO_CHANGES_ALLOWED`,
     * * `CERTIFIED_FORM_FILLING` and `CERTIFIED_FORM_FILLING_AND_ANNOTATIONS`
     */
    var certificationLevel = NOT_CERTIFIED

    // signature info

    /** The caption for the reason for signing.  */
    private var reasonCaption = "Reason: "

    /** The caption for the location of signing.  */
    private var locationCaption = "Location: "

    /** The reason for signing.  */
    /**
     * Gets the signing reason.
     * @return the signing reason
     */
    /**
     * Sets the signing reason.
     * @param reason the signing reason
     */
    var reason: String? = null

    /** Holds value of property location.  */
    /**
     * Gets the signing location.
     * @return the signing location
     */
    /**
     * Sets the signing location.
     * @param location the signing location
     */
    var location: String? = null

    /** Holds value of property signDate.  */
    /**
     * Gets the signature date.
     * @return the signature date
     */
    /**
     * Sets the signature date.
     * @param signDate the signature date
     */
    var signDate: java.util.Calendar? = null

    /**
     * Sets the caption for signing reason.
     * @param reasonCaption the signing reason caption
     */
    fun setReasonCaption(reasonCaption: String) {
        this.reasonCaption = reasonCaption
    }

    /**
     * Sets the caption for the signing location.
     * @param locationCaption the signing location caption
     */
    fun setLocationCaption(locationCaption: String) {
        this.locationCaption = locationCaption
    }

    /** Holds value of the application that creates the signature  */
    /**
     * Gets the signature creator.
     * @return the signature creator
     */
    /**
     * Sets the name of the application used to create the signature.
     * @param signatureCreator the name of the signature creating application
     */
    var signatureCreator: String? = null

    /** The contact name of the signer.  */
    /**
     * Gets the signing contact.
     * @return the signing contact
     */
    /**
     * Sets the signing contact.
     * @param contact the signing contact
     */
    var contact: String? = null

    // the PDF file

    /** The file right before the signature is added (can be null).  */
    private var raf: RandomAccessFile? = null
    /** The bytes of the file right before the signature is added (if raf is null)  */
    private var bout: ByteArray? = null
    /** Array containing the byte positions of the bytes that need to be hashed.  */
    private var range: LongArray? = null

    /**
     * Gets the document bytes that are hashable when using external signatures. The general sequence is:
     * preClose(), getRangeStream() and close().
     *
     *
     * @return the document bytes that are hashable
     */
    val rangeStream: InputStream
        @Throws(IOException::class)
        get() {
            val fac = RandomAccessSourceFactory()
            return RASInputStream(fac.createRanged(underlyingSource, range))
        }

    /**
     * @return the underlying source
     * *
     * @throws IOException
     */
    private //TODO: get rid of separate byte[] and RandomAccessFile objects and just store a RandomAccessSource
    val underlyingSource: RandomAccessSource
        @Throws(IOException::class)
        get() {
            val fac = RandomAccessSourceFactory()
            return if (raf == null) fac.createSource(bout) else fac.createSource(raf)
        }

    /** The signing certificate  */
    /**
     * Sets the certificate used to provide the text in the appearance.
     * This certificate doesn't take part in the actual signing process.
     * @param signCertificate the certificate
     */
    var certificate: Certificate? = null

    // Developer extenstion

    /**
     * Adds the appropriate developer extension.
     */
    fun addDeveloperExtension(de: PdfDeveloperExtension) {
        writer.addDeveloperExtension(de)
    }

    // Crypto dictionary

    /** The crypto dictionary  */
    /**
     * Gets the user made signature dictionary. This is the dictionary at the /V key.
     * @return the user made signature dictionary
     */
    /**
     * Sets a user made signature dictionary. This is the dictionary at the /V key.
     * @param cryptoDictionary a user made signature dictionary
     */
    var cryptoDictionary: com.itextpdf.text.pdf.PdfDictionary? = null

    // Signature event

    /**
     * An interface to retrieve the signature dictionary for modification.
     */
    interface SignatureEvent {
        /**
         * Allows modification of the signature dictionary.
         * @param sig the signature dictionary
         */
        fun getSignatureDictionary(sig: PdfDictionary)
    }

    /**
     * Holds value of property signatureEvent.
     */
    /**
     * Getter for property signatureEvent.
     * @return Value of property signatureEvent.
     */
    /**
     * Sets the signature event to allow modification of the signature dictionary.
     * @param signatureEvent the signature event
     */
    var signatureEvent: SignatureEvent? = null

    /*
	 * SIGNATURE FIELD
	 */

    /** The name of the field  */
    /**
     * Gets the field name.
     * @return the field name
     */
    var fieldName: java.lang.String? = null
        private set

    /**
     * Gets a new signature field name that
     * doesn't clash with any existing name.
     * @return a new signature field name
     */
    val newSigName: String
        get() {
            val af = writer.getAcroFields()
            var name = "Signature"
            var step = 0
            var found = false
            while (!found) {
                ++step
                var n1 = name + step
                if (af.getFieldItem(n1) != null)
                    continue
                n1 += "."
                found = true
                for (element in af.getFields().keys) {
                    if (element.startsWith(n1)) {
                        found = false
                        break
                    }
                }
            }
            name += step
            return name
        }

    /**
     * The page where the signature will appear.
     */
    /**
     * Gets the page number of the field.
     * @return the page number of the field
     */
    var page = 1
        private set

    /**
     * The coordinates of the rectangle for a visible signature,
     * or a zero-width, zero-height rectangle for an invisible signature.
     */
    /**
     * Gets the rectangle representing the signature dimensions.
     * @return the rectangle representing the signature dimensions. It may be null
     * * or have zero width or height for invisible signatures
     */
    var rect: Rectangle? = null
        private set

    /** rectangle that represent the position and dimension of the signature in the page.  */
    /**
     * Gets the rectangle that represent the position and dimension of the signature in the page.
     * @return the rectangle that represent the position and dimension of the signature in the page
     */
    var pageRect: Rectangle? = null
        private set

    /**
     * Gets the visibility status of the signature.
     * @return the visibility status of the signature
     */
    val isInvisible: Boolean
        get() = rect == null || rect!!.width == 0f || rect!!.height == 0f

    /**
     * Sets the signature to be visible. It creates a new visible signature field.
     * @param pageRect the position and dimension of the field in the page
     * *
     * @param page the page to place the field. The fist page is 1
     * *
     * @param fieldName the field name or null to generate automatically a new field name
     */
    fun setVisibleSignature(pageRect: Rectangle, page: Int, fieldName: String?) {
        if (fieldName != null) {
            if (fieldName.indexOf('.') >= 0)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("field.names.cannot.contain.a.dot"))
            val af = writer.getAcroFields()
            val item = af.getFieldItem(fieldName)
            if (item != null)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.field.1.already.exists", fieldName))
            this.fieldName = fieldName
        }
        if (page < 1 || page > writer.pdfReader.numberOfPages)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.page.number.1", page))
        this.pageRect = Rectangle(pageRect)
        this.pageRect!!.normalize()
        rect = Rectangle(this.pageRect!!.width, this.pageRect!!.height)
        this.page = page
    }

    /**
     * Sets the signature to be visible. An empty signature field with the same name must already exist.
     * @param fieldName the existing empty signature field name
     */
    fun setVisibleSignature(fieldName: String) {
        val af = writer.getAcroFields()
        val item = af.getFieldItem(fieldName) ?: throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.field.1.does.not.exist", fieldName))
        val merged = item.getMerged(0)
        if (PdfName.SIG != PdfReader.getPdfObject(merged.get(PdfName.FT)))
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.field.1.is.not.a.signature.field", fieldName))
        this.fieldName = fieldName
        val r = merged.getAsArray(PdfName.RECT)
        val llx = r.getAsNumber(0).floatValue()
        val lly = r.getAsNumber(1).floatValue()
        val urx = r.getAsNumber(2).floatValue()
        val ury = r.getAsNumber(3).floatValue()
        pageRect = Rectangle(llx, lly, urx, ury)
        pageRect!!.normalize()
        page = item.getPage(0)!!.toInt()
        val rotation = writer.pdfReader.getPageRotation(page)
        val pageSize = writer.pdfReader.getPageSizeWithRotation(page)
        when (rotation) {
            90 -> pageRect = Rectangle(
                    pageRect!!.bottom,
                    pageSize.top - pageRect!!.left,
                    pageRect!!.top,
                    pageSize.top - pageRect!!.right)
            180 -> pageRect = Rectangle(
                    pageSize.right - pageRect!!.left,
                    pageSize.top - pageRect!!.bottom,
                    pageSize.right - pageRect!!.right,
                    pageSize.top - pageRect!!.top)
            270 -> pageRect = Rectangle(
                    pageSize.right - pageRect!!.bottom,
                    pageRect!!.left,
                    pageSize.right - pageRect!!.top,
                    pageRect!!.right)
        }
        if (rotation != 0)
            pageRect!!.normalize()
        rect = Rectangle(this.pageRect!!.width, this.pageRect!!.height)
    }

    /*
	 * SIGNATURE APPEARANCE
	 */

    /**
     * Signature rendering modes
     * @since 5.0.1
     */
    enum class RenderingMode {
        /**
         * The rendering mode is just the description.
         */
        DESCRIPTION,
        /**
         * The rendering mode is the name of the signer and the description.
         */
        NAME_AND_DESCRIPTION,
        /**
         * The rendering mode is an image and the description.
         */
        GRAPHIC_AND_DESCRIPTION,
        /**
         * The rendering mode is just an image.
         */
        GRAPHIC
    }

    /** The rendering mode chosen for visible signatures  */
    /**
     * Gets the rendering mode for this signature.
     * @return the rendering mode for this signature
     * *
     * @since 5.0.1
     */
    /**
     * Sets the rendering mode for this signature.
     * @param renderingMode the rendering mode
     * *
     * @since 5.0.1
     */
    var renderingMode = RenderingMode.DESCRIPTION

    /** The image that needs to be used for a visible signature  */
    /**
     * Gets the Image object to render.
     * @return the image
     */
    /**
     * Sets the Image object to render when Render is set to RenderingMode.GRAPHIC
     * or RenderingMode.GRAPHIC_AND_DESCRIPTION.
     * @param signatureGraphic image rendered. If null the mode is defaulted
     * * to RenderingMode.DESCRIPTION
     */
    var signatureGraphic: Image? = null

    /** Appearance compliant with the recommendations introduced in Acrobat 6?  */
    /**
     * Gets the Acrobat 6.0 layer mode.
     * @return the Acrobat 6.0 layer mode
     */
    /**
     * Acrobat 6.0 and higher recommends that only layer n0 and n2 be present.
     * Use this method with value `false` if you want to ignore this recommendation.
     * @param acro6Layers if `true` only the layers n0 and n2 will be present
     * *
     */
    var isAcro6Layers = true

    /** Layers for a visible signature.  */
    private val app = arrayOfNulls<PdfTemplate>(5)

    /**
     * Gets a template layer to create a signature appearance. The layers can go from 0 to 4,
     * but only layer 0 and 2 will be used if acro6Layers is true.
     *
     *
     * Consult PPKAppearances.pdf
     * for further details.
     * @param layer the layer
     * *
     * @return a template
     */
    fun getLayer(layer: Int): PdfTemplate? {
        if (layer < 0 || layer >= app.size)
            return null
        var t: PdfTemplate? = app[layer]
        if (t == null) {
            t = app[layer] = PdfTemplate(writer)
            t!!.boundingBox = rect
            writer.addDirectTemplateSimple(t, PdfName("n" + layer))
        }
        return t
    }

    /** Indicates if we need to reuse the existing appearance as layer 0.  */
    private var reuseAppearance = false

    /**
     * Indicates that the existing appearances needs to be reused as layer 0.
     */
    fun setReuseAppearance(reuseAppearance: Boolean) {
        this.reuseAppearance = reuseAppearance
    }

    // layer 2

    /** A background image for the text in layer 2.  */
    /**
     * Gets the background image for the layer 2.
     * @return the background image for the layer 2
     */
    /**
     * Sets the background image for the layer 2.
     * @param image the background image for the layer 2
     */
    var image: Image? = null

    /** the scaling to be applied to the background image.t   */
    /**
     * Gets the scaling to be applied to the background image.
     * @return the scaling to be applied to the background image
     */
    /**
     * Sets the scaling to be applied to the background image. If it's zero the image
     * will fully fill the rectangle. If it's less than zero the image will fill the rectangle but
     * will keep the proportions. If it's greater than zero that scaling will be applied.
     * In any of the cases the image will always be centered. It's zero by default.
     * @param imageScale the scaling to be applied to the background image
     */
    var imageScale: Float = 0.toFloat()

    /** The text that goes in Layer 2 of the signature appearance.  */
    /**
     * Gets the signature text identifying the signer if set by setLayer2Text().
     * @return the signature text identifying the signer
     */
    /**
     * Sets the signature text identifying the signer.
     * @param text the signature text identifying the signer. If null or not set
     * * a standard description will be used
     */
    var layer2Text: String? = null

    /** Font for the text in Layer 2.  */
    /**
     * Gets the n2 and n4 layer font.
     * @return the n2 and n4 layer font
     */
    /**
     * Sets the n2 and n4 layer font. If the font size is zero, auto-fit will be used.
     * @param layer2Font the n2 and n4 font
     */
    var layer2Font: Font? = null

    /** Run direction for the text in layers 2 and 4.  */
    /** Gets the run direction.
     * @return the run direction
     */
    /** Sets the run direction in the n2 and n4 layer.
     * @param runDirection the run direction
     */
    var runDirection = PdfWriter.RUN_DIRECTION_NO_BIDI
        set(runDirection) {
            if (runDirection < PdfWriter.RUN_DIRECTION_DEFAULT || runDirection > PdfWriter.RUN_DIRECTION_RTL)
                throw RuntimeException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection))
            this.runDirection = runDirection
        }

    // layer 4

    /** The text that goes in Layer 4 of the appearance.  */
    /**
     * Gets the text identifying the signature status if set by setLayer4Text().
     * @return the text identifying the signature status
     */
    /**
     * Sets the text identifying the signature status. Will be ignored if acro6Layers is true.
     * @param text the text identifying the signature status. If null or not set
     * * the description "Signature Not Verified" will be used
     */
    var layer4Text: String? = null

    // all layers

    /** Template containing all layers drawn on top of each other.  */
    private var frm: PdfTemplate? = null

    /**
     * Gets the template that aggregates all appearance layers. This corresponds to the /FRM resource.
     *
     *
     * Consult PPKAppearances.pdf
     * for further details.
     * @return the template that aggregates all appearance layers
     */
    val topLayer: PdfTemplate
        get() {
            if (frm == null) {
                frm = PdfTemplate(writer)
                frm!!.boundingBox = rect
                writer.addDirectTemplateSimple(frm, PdfName("FRM"))
            }
            return frm
        }

    /**
     * Gets the main appearance layer.
     *
     *
     * Consult PPKAppearances.pdf
     * for further details.
     * @return the main appearance layer
     * *
     * @throws DocumentException on error
     */
    // origin is the bottom-left
    // take all space available
    // must calculate the point to draw from to make image appear in middle of column
    // experimentation found this magic number to counteract Adobe's signature graphic, which
    // offsets the y co-ordinate by 15 units
    // must calculate the point to draw from to make image appear in middle of column
    //float size = font.getSize();
    val appearance: PdfTemplate
        @Throws(DocumentException::class)
        get() {
            if (isInvisible) {
                val t = PdfTemplate(writer)
                t.boundingBox = Rectangle(0f, 0f)
                writer.addDirectTemplateSimple(t, null)
                return t
            }

            if (app[0] == null && !reuseAppearance) {
                createBlankN0()
            }
            if (app[1] == null && !isAcro6Layers) {
                val t = app[1] = PdfTemplate(writer)
                t.boundingBox = Rectangle(100f, 100f)
                writer.addDirectTemplateSimple(t, PdfName("n1"))
                t.setLiteral(questionMark)
            }
            if (app[2] == null) {
                val text: String
                if (layer2Text == null) {
                    val buf = StringBuilder()
                    buf.append("Digitally signed by ")
                    var name: String? = null
                    val x500name = CertificateInfo.getSubjectFields(certificate as X509Certificate?)
                    if (x500name != null) {
                        name = x500name.getField("CN")
                        if (name == null)
                            name = x500name.getField("E")
                    }
                    if (name == null)
                        name = ""
                    buf.append(name).append('\n')
                    val sd = SimpleDateFormat("yyyy.MM.dd HH:mm:ss z")
                    buf.append("Date: ").append(sd.format(signDate!!.time))
                    if (reason != null)
                        buf.append('\n').append(reasonCaption).append(reason)
                    if (location != null)
                        buf.append('\n').append(locationCaption).append(location)
                    text = buf.toString()
                } else
                    text = layer2Text
                val t = app[2] = PdfTemplate(writer)
                t.boundingBox = rect
                writer.addDirectTemplateSimple(t, PdfName("n2"))
                if (image != null) {
                    if (imageScale == 0f) {
                        t.addImage(image, rect!!.width, 0f, 0f, rect!!.height, 0f, 0f)
                    } else {
                        var usableScale = imageScale
                        if (imageScale < 0)
                            usableScale = Math.min(rect!!.width / image!!.width, rect!!.height / image!!.height)
                        val w = image!!.width * usableScale
                        val h = image!!.height * usableScale
                        val x = (rect!!.width - w) / 2
                        val y = (rect!!.height - h) / 2
                        t.addImage(image, w, 0f, 0f, h, x, y)
                    }
                }
                val font: Font
                if (layer2Font == null)
                    font = Font()
                else
                    font = Font(layer2Font)
                var size = font.size

                var dataRect: Rectangle? = null
                var signatureRect: Rectangle? = null

                if (renderingMode == RenderingMode.NAME_AND_DESCRIPTION || renderingMode == RenderingMode.GRAPHIC_AND_DESCRIPTION && this.signatureGraphic != null) {
                    signatureRect = Rectangle(
                            MARGIN,
                            MARGIN,
                            rect!!.width / 2 - MARGIN,
                            rect!!.height - MARGIN)
                    dataRect = Rectangle(
                            rect!!.width / 2 + MARGIN / 2,
                            MARGIN,
                            rect!!.width - MARGIN / 2,
                            rect!!.height - MARGIN)

                    if (rect!!.height > rect!!.width) {
                        signatureRect = Rectangle(
                                MARGIN,
                                rect!!.height / 2,
                                rect!!.width - MARGIN,
                                rect!!.height)
                        dataRect = Rectangle(
                                MARGIN,
                                MARGIN,
                                rect!!.width - MARGIN,
                                rect!!.height / 2 - MARGIN)
                    }
                } else if (renderingMode == RenderingMode.GRAPHIC) {
                    if (signatureGraphic == null) {
                        throw IllegalStateException(MessageLocalization.getComposedMessage("a.signature.image.should.be.present.when.rendering.mode.is.graphic.only"))
                    }
                    signatureRect = Rectangle(
                            MARGIN,
                            MARGIN,
                            rect!!.width - MARGIN,
                            rect!!.height - MARGIN)
                } else {
                    dataRect = Rectangle(
                            MARGIN,
                            MARGIN,
                            rect!!.width - MARGIN,
                            rect!!.height * (1 - TOP_SECTION) - MARGIN)
                }

                when (renderingMode) {
                    PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION -> {
                        var signedBy = CertificateInfo.getSubjectFields(certificate as X509Certificate?)!!.getField("CN")
                        if (signedBy == null)
                            signedBy = CertificateInfo.getSubjectFields(certificate as X509Certificate?)!!.getField("E")
                        if (signedBy == null)
                            signedBy = ""
                        val sr2 = Rectangle(signatureRect!!.width - MARGIN, signatureRect.height - MARGIN)
                        val signedSize = ColumnText.fitText(font, signedBy, sr2, -1f, runDirection)

                        var ct2 = ColumnText(t)
                        ct2.runDirection = runDirection
                        ct2.setSimpleColumn(Phrase(signedBy, font), signatureRect.left, signatureRect.bottom, signatureRect.right, signatureRect.top, signedSize, Element.ALIGN_LEFT)

                        ct2.go()
                    }
                    PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION -> {
                        if (signatureGraphic == null) {
                            throw IllegalStateException(MessageLocalization.getComposedMessage("a.signature.image.should.be.present.when.rendering.mode.is.graphic.and.description"))
                        }
                        ct2 = ColumnText(t)
                        ct2.runDirection = runDirection
                        ct2.setSimpleColumn(signatureRect!!.left, signatureRect.bottom, signatureRect.right, signatureRect.top, 0f, Element.ALIGN_RIGHT)

                        var im = Image.getInstance(signatureGraphic)
                        im.scaleToFit(signatureRect.width, signatureRect.height)

                        var p = Paragraph()
                        var x = 0f
                        var y = -im.scaledHeight + 15

                        x = x + (signatureRect.width - im.scaledWidth) / 2
                        y = y - (signatureRect.height - im.scaledHeight) / 2
                        p.add(Chunk(im, x + (signatureRect.width - im.scaledWidth) / 2, y, false))
                        ct2.addElement(p)
                        ct2.go()
                    }
                    PdfSignatureAppearance.RenderingMode.GRAPHIC -> {
                        ct2 = ColumnText(t)
                        ct2.runDirection = runDirection
                        ct2.setSimpleColumn(signatureRect!!.left, signatureRect.bottom, signatureRect.right, signatureRect.top, 0f, Element.ALIGN_RIGHT)

                        im = Image.getInstance(signatureGraphic)
                        im.scaleToFit(signatureRect.width, signatureRect.height)

                        p = Paragraph(signatureRect.height)
                        x = (signatureRect.width - im.getScaledWidth()) / 2
                        y = (signatureRect.height - im.getScaledHeight()) / 2
                        p.add(Chunk(im, x, y, false))
                        ct2.addElement(p)
                        ct2.go()
                    }
                }

                if (renderingMode != RenderingMode.GRAPHIC) {
                    if (size <= 0) {
                        val sr = Rectangle(dataRect!!.width, dataRect.height)
                        size = ColumnText.fitText(font, text, sr, 12f, runDirection)
                    }
                    val ct = ColumnText(t)
                    ct.runDirection = runDirection
                    ct.setSimpleColumn(Phrase(text, font), dataRect!!.left, dataRect.bottom, dataRect.right, dataRect.top, size, Element.ALIGN_LEFT)
                    ct.go()
                }
            }
            if (app[3] == null && !isAcro6Layers) {
                val t = app[3] = PdfTemplate(writer)
                t.boundingBox = Rectangle(100f, 100f)
                writer.addDirectTemplateSimple(t, PdfName("n3"))
                t.setLiteral("% DSBlank\n")
            }
            if (app[4] == null && !isAcro6Layers) {
                val t = app[4] = PdfTemplate(writer)
                t.boundingBox = Rectangle(0f, rect!!.height * (1 - TOP_SECTION), rect!!.right, rect!!.top)
                writer.addDirectTemplateSimple(t, PdfName("n4"))
                val font: Font
                if (layer2Font == null)
                    font = Font()
                else
                    font = Font(layer2Font)
                var text = "Signature Not Verified"
                if (layer4Text != null)
                    text = layer4Text
                val sr = Rectangle(rect!!.width - 2 * MARGIN, rect!!.height * TOP_SECTION - 2 * MARGIN)
                val size = ColumnText.fitText(font, text, sr, 15f, runDirection)
                val ct = ColumnText(t)
                ct.runDirection = runDirection
                ct.setSimpleColumn(Phrase(text, font), MARGIN, 0f, rect!!.width - MARGIN, rect!!.height - MARGIN, size, Element.ALIGN_LEFT)
                ct.go()
            }
            val rotation = writer.pdfReader.getPageRotation(page)
            var rotated = Rectangle(rect)
            var n = rotation
            while (n > 0) {
                rotated = rotated.rotate()
                n -= 90
            }
            if (frm == null) {
                frm = PdfTemplate(writer)
                frm!!.boundingBox = rotated
                writer.addDirectTemplateSimple(frm, PdfName("FRM"))
                var scale = Math.min(rect!!.width, rect!!.height) * 0.9f
                val x = (rect!!.width - scale) / 2
                val y = (rect!!.height - scale) / 2
                scale /= 100f
                if (rotation == 90)
                    frm!!.concatCTM(0f, 1f, -1f, 0f, rect!!.height, 0f)
                else if (rotation == 180)
                    frm!!.concatCTM(-1f, 0f, 0f, -1f, rect!!.width, rect!!.height)
                else if (rotation == 270)
                    frm!!.concatCTM(0f, -1f, 1f, 0f, 0f, rect!!.width)
                if (reuseAppearance) {
                    val af = writer.getAcroFields()
                    val ref = af.getNormalAppearance(fieldName)
                    if (ref != null) {
                        frm!!.addTemplateReference(ref, PdfName("n0"), 1f, 0f, 0f, 1f, 0f, 0f)
                    } else {
                        reuseAppearance = false
                        if (app[0] == null) {
                            createBlankN0()
                        }
                    }
                }
                if (!reuseAppearance) {
                    frm!!.addTemplate(app[0], 0f, 0f)
                }
                if (!isAcro6Layers)
                    frm!!.addTemplate(app[1], scale, 0f, 0f, scale, x, y)
                frm!!.addTemplate(app[2], 0f, 0f)
                if (!isAcro6Layers) {
                    frm!!.addTemplate(app[3], scale, 0f, 0f, scale, x, y)
                    frm!!.addTemplate(app[4], 0f, 0f)
                }
            }
            val napp = PdfTemplate(writer)
            napp.boundingBox = rotated
            writer.addDirectTemplateSimple(napp, null)
            napp.addTemplate(frm, 0f, 0f)
            return napp
        }

    private fun createBlankN0() {
        val t = app[0] = PdfTemplate(writer)
        t.boundingBox = Rectangle(100f, 100f)
        writer.addDirectTemplateSimple(t, PdfName("n0"))
        t.setLiteral("% DSBlank\n")
    }

    /*
     * Creating the signed file.
     */

    /** The PdfStamper that creates the signed PDF.  */
    /**
     * Gets the PdfStamper associated with this instance.
     * @return the PdfStamper associated with this instance
     */
    /**
     * Sets the PdfStamper
     * @param stamper PdfStamper
     */
    var stamper: PdfStamper? = null
        internal set(stamper) {
            this.stamper = stamper
        }

    /** A byte buffer containing the bytes of the Stamper.  */
    /**
     * Getter for the byte buffer.
     */
    /**
     * Setter for the byte buffer.
     */
    internal var sigout: ByteBuffer? = null

    /** OutputStream for the bytes of the stamper.  */
    /**
     * Getter for the OutputStream.
     */
    /**
     * Setter for the OutputStream.
     */
    internal var originalout: OutputStream? = null

    /** Temporary file in case you don't want to sign in memory.  */
    /**
     * Gets the temporary file.
     * @return the temporary file or null is the document is created in memory
     */
    /**
     * Setter for the temporary file.
     * @param tempFile
     */
    var tempFile: File? = null
        internal set(tempFile) {
            this.tempFile = tempFile
        }

    /** Name and content of keys that can only be added in the close() method.  */
    private var exclusionLocations: HashMap<PdfName, PdfLiteral>? = null

    /** Length of the output.  */
    private var boutLen: Int = 0

    /** Indicates if the stamper has already been pre-closed.  */
    /**
     * Checks if the document is in the process of closing.
     * @return true if the document is in the process of closing,
     * * false otherwise
     */
    var isPreClosed = false
        private set

    /** Signature field lock dictionary  */
    /**
     * Getter for the field lock dictionary.
     * @return Field lock dictionary.
     */
    /**
     * Setter for the field lock dictionary.
     *
     * **Be aware:** if a signature is created on an existing signature field,
     * then its /Lock dictionary takes the precedence (if it exists).

     * @param fieldLock Field lock dictionary.
     */
    var fieldLockDict: PdfSigLockDictionary? = null

    /**
     * This is the first method to be called when using external signatures. The general sequence is:
     * preClose(), getDocumentBytes() and close().
     *
     *
     * If calling preClose() dont't call PdfStamper.close().
     *
     *
     * exclusionSizes must contain at least
     * the PdfName.CONTENTS key with the size that it will take in the
     * document. Note that due to the hex string coding this size should be
     * byte_size*2+2.
     * @param exclusionSizes a HashMap with names and sizes to be excluded in the signature
     * * calculation. The key is a PdfName and the value an
     * * Integer. At least the PdfName.CONTENTS must be present
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     */
    @Throws(IOException::class, DocumentException::class)
    fun preClose(exclusionSizes: HashMap<PdfName, Int>) {
        if (isPreClosed)
            throw DocumentException(MessageLocalization.getComposedMessage("document.already.pre.closed"))
        stamper!!.mergeVerification()
        isPreClosed = true
        val af = writer.getAcroFields()
        val name = fieldName
        val fieldExists = af.doesSignatureFieldExist(name)
        val refSig = writer.pdfIndirectReference
        writer.setSigFlags(3)
        var fieldLock: PdfDictionary? = null
        if (fieldExists) {
            val widget = af.getFieldItem(name)!!.getWidget(0)
            writer.markUsed(widget)
            fieldLock = widget.getAsDict(PdfName.LOCK)

            if (fieldLock == null && this.fieldLockDict != null) {
                widget.put(PdfName.LOCK, writer.addToBody(this.fieldLockDict).indirectReference)
                fieldLock = this.fieldLockDict
            }

            widget.put(PdfName.P, writer.getPageReference(page))
            widget.put(PdfName.V, refSig)
            val obj = PdfReader.getPdfObjectRelease(widget.get(PdfName.F))
            var flags = 0
            if (obj != null && obj.isNumber)
                flags = (obj as PdfNumber).intValue()
            flags = flags or PdfAnnotation.FLAGS_LOCKED
            widget.put(PdfName.F, PdfNumber(flags))
            val ap = PdfDictionary()
            ap.put(PdfName.N, appearance.indirectReference)
            widget.put(PdfName.AP, ap)
        } else {
            val sigField = PdfFormField.createSignature(writer)
            sigField.setFieldName(name)
            sigField.put(PdfName.V, refSig)
            sigField.setFlags(PdfAnnotation.FLAGS_PRINT or PdfAnnotation.FLAGS_LOCKED)

            if (this.fieldLockDict != null) {
                sigField.put(PdfName.LOCK, writer.addToBody(this.fieldLockDict).indirectReference)
                fieldLock = this.fieldLockDict
            }

            val pagen = page
            if (!isInvisible)
                sigField.setWidget(pageRect, null)
            else
                sigField.setWidget(Rectangle(0f, 0f), null)
            //sigField.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, getAppearance());
            sigField.setPage(pagen)
            writer.addAnnotation(sigField, pagen)
        }

        exclusionLocations = HashMap<PdfName, PdfLiteral>()
        if (cryptoDictionary == null) {
            throw DocumentException("No crypto dictionary defined.")
        } else {
            var lit = PdfLiteral(80)
            exclusionLocations!!.put(PdfName.BYTERANGE, lit)
            cryptoDictionary!!.put(PdfName.BYTERANGE, lit)
            for ((key, v) in exclusionSizes) {
                lit = PdfLiteral(v.toInt())
                exclusionLocations!!.put(key, lit)
                cryptoDictionary!!.put(key, lit)
            }
            if (certificationLevel > 0)
                addDocMDP(cryptoDictionary)
            if (fieldLock != null)
                addFieldMDP(cryptoDictionary, fieldLock)
            if (signatureEvent != null)
                signatureEvent!!.getSignatureDictionary(cryptoDictionary)
            writer.addToBody(cryptoDictionary, refSig, false)
        }
        if (certificationLevel > 0) {
            // add DocMDP entry to root
            val docmdp = PdfDictionary()
            docmdp.put(PdfName("DocMDP"), refSig)
            writer.pdfReader.catalog.put(PdfName("Perms"), docmdp)
        }
        writer.close(stamper!!.moreInfo)

        range = LongArray(exclusionLocations!!.size * 2)
        val byteRangePosition = exclusionLocations!![PdfName.BYTERANGE].position
        exclusionLocations!!.remove(PdfName.BYTERANGE)
        var idx = 1
        for (lit in exclusionLocations!!.values) {
            val n = lit.position
            range[idx++] = n
            range[idx++] = lit.posLength + n
        }
        Arrays.sort(range, 1, range!!.size - 1)
        run {
            var k = 3
            while (k < range!!.size - 2) {
                range[k] -= range!![k - 1]
                k += 2
            }
        }

        if (tempFile == null) {
            bout = sigout!!.buffer
            boutLen = sigout!!.size()
            range[range!!.size - 1] = boutLen - range!![range!!.size - 2]
            val bf = ByteBuffer()
            bf.append('[')
            for (k in range!!.indices)
                bf.append(range!![k]).append(' ')
            bf.append(']')
            System.arraycopy(bf.buffer, 0, bout, byteRangePosition.toInt(), bf.size())
        } else {
            try {
                raf = RandomAccessFile(tempFile, "rw")
                val len = raf!!.length()
                range[range!!.size - 1] = len - range!![range!!.size - 2]
                val bf = ByteBuffer()
                bf.append('[')
                for (k in range!!.indices)
                    bf.append(range!![k]).append(' ')
                bf.append(']')
                raf!!.seek(byteRangePosition)
                raf!!.write(bf.buffer, 0, bf.size())
            } catch (e: IOException) {
                try {
                    raf!!.close()
                } catch (ee: Exception) {
                }

                try {
                    tempFile!!.delete()
                } catch (ee: Exception) {
                }

                throw e
            }

        }
    }

    /**
     * Adds keys to the signature dictionary that define
     * the certification level and the permissions.
     * This method is only used for Certifying signatures.
     * @param crypto the signature dictionary
     */
    private fun addDocMDP(crypto: PdfDictionary) {
        val reference = PdfDictionary()
        val transformParams = PdfDictionary()
        transformParams.put(PdfName.P, PdfNumber(certificationLevel))
        transformParams.put(PdfName.V, PdfName("1.2"))
        transformParams.put(PdfName.TYPE, PdfName.TRANSFORMPARAMS)
        reference.put(PdfName.TRANSFORMMETHOD, PdfName.DOCMDP)
        reference.put(PdfName.TYPE, PdfName.SIGREF)
        reference.put(PdfName.TRANSFORMPARAMS, transformParams)
        if (writer.pdfVersion.version < PdfWriter.VERSION_1_6) {
            reference.put(PdfName("DigestValue"), PdfString("aa"))
            val loc = PdfArray()
            loc.add(PdfNumber(0))
            loc.add(PdfNumber(0))
            reference.put(PdfName("DigestLocation"), loc)
            reference.put(PdfName("DigestMethod"), PdfName("MD5"))
        }
        reference.put(PdfName.DATA, writer.pdfReader.trailer.get(PdfName.ROOT))
        val types = PdfArray()
        types.add(reference)
        crypto.put(PdfName.REFERENCE, types)
    }

    /**
     * Adds keys to the signature dictionary that define
     * the field permissions.
     * This method is only used for signatures that lock fields.
     * @param crypto the signature dictionary
     */
    private fun addFieldMDP(crypto: PdfDictionary, fieldLock: PdfDictionary) {
        val reference = PdfDictionary()
        val transformParams = PdfDictionary()
        transformParams.putAll(fieldLock)
        transformParams.put(PdfName.TYPE, PdfName.TRANSFORMPARAMS)
        transformParams.put(PdfName.V, PdfName("1.2"))
        reference.put(PdfName.TRANSFORMMETHOD, PdfName.FIELDMDP)
        reference.put(PdfName.TYPE, PdfName.SIGREF)
        reference.put(PdfName.TRANSFORMPARAMS, transformParams)
        reference.put(PdfName("DigestValue"), PdfString("aa"))
        val loc = PdfArray()
        loc.add(PdfNumber(0))
        loc.add(PdfNumber(0))
        reference.put(PdfName("DigestLocation"), loc)
        reference.put(PdfName("DigestMethod"), PdfName("MD5"))
        reference.put(PdfName.DATA, writer.pdfReader.trailer.get(PdfName.ROOT))
        var types: PdfArray? = crypto.getAsArray(PdfName.REFERENCE)
        if (types == null)
            types = PdfArray()
        types.add(reference)
        crypto.put(PdfName.REFERENCE, types)
    }

    /**
     * This is the last method to be called when using external signatures. The general sequence is:
     * preClose(), getDocumentBytes() and close().
     *
     *
     * update is a PdfDictionary that must have exactly the
     * same keys as the ones provided in [.preClose].
     * @param update a PdfDictionary with the key/value that will fill the holes defined
     * * in [.preClose]
     * *
     * @throws DocumentException on error
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class, DocumentException::class)
    fun close(update: PdfDictionary) {
        try {
            if (!isPreClosed)
                throw DocumentException(MessageLocalization.getComposedMessage("preclose.must.be.called.first"))
            val bf = ByteBuffer()
            for (key in update.keys) {
                val obj = update.get(key)
                val lit = exclusionLocations!![key] ?: throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.key.1.didn.t.reserve.space.in.preclose", key.toString()))
                bf.reset()
                obj.toPdf(null, bf)
                if (bf.size() > lit.posLength)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.key.1.is.too.big.is.2.reserved.3", key.toString(), bf.size().toString(), lit.posLength.toString()))
                if (tempFile == null)
                    System.arraycopy(bf.buffer, 0, bout, lit.position.toInt(), bf.size())
                else {
                    raf!!.seek(lit.position)
                    raf!!.write(bf.buffer, 0, bf.size())
                }
            }
            if (update.size() != exclusionLocations!!.size)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.update.dictionary.has.less.keys.than.required"))
            if (tempFile == null) {
                originalout!!.write(bout, 0, boutLen)
            } else {
                if (originalout != null) {
                    raf!!.seek(0)
                    var length = raf!!.length()
                    val buf = ByteArray(8192)
                    while (length > 0) {
                        val r = raf!!.read(buf, 0, Math.min(buf.size.toLong(), length).toInt())
                        if (r < 0)
                            throw EOFException(MessageLocalization.getComposedMessage("unexpected.eof"))
                        originalout!!.write(buf, 0, r)
                        length -= r.toLong()
                    }
                }
            }
        } finally {
            writer.pdfReader.close()
            if (tempFile != null) {
                try {
                    raf!!.close()
                } catch (ee: Exception) {
                }

                if (originalout != null)
                    try {
                        tempFile!!.delete()
                    } catch (ee: Exception) {
                    }

            }
            if (originalout != null)
                try {
                    originalout!!.close()
                } catch (e: Exception) {
                }

        }
    }

    companion object {

        /*
	 * SIGNATURE
	 */

        // signature types

        /** Approval signature  */
        val NOT_CERTIFIED = 0

        /** Author signature, no changes allowed  */
        val CERTIFIED_NO_CHANGES_ALLOWED = 1

        /** Author signature, form filling allowed  */
        val CERTIFIED_FORM_FILLING = 2

        /** Author signature, form filling and annotations allowed  */
        val CERTIFIED_FORM_FILLING_AND_ANNOTATIONS = 3

        // layer 1

        /** An appearance that can be used for layer 1 (if acro6Layers is false).  */
        val questionMark =
                "% DSUnknown\n" +
                        "q\n" +
                        "1 G\n" +
                        "1 g\n" +
                        "0.1 0 0 0.1 9 0 cm\n" +
                        "0 J 0 j 4 M []0 d\n" +
                        "1 i \n" +
                        "0 g\n" +
                        "313 292 m\n" +
                        "313 404 325 453 432 529 c\n" +
                        "478 561 504 597 504 645 c\n" +
                        "504 736 440 760 391 760 c\n" +
                        "286 760 271 681 265 626 c\n" +
                        "265 625 l\n" +
                        "100 625 l\n" +
                        "100 828 253 898 381 898 c\n" +
                        "451 898 679 878 679 650 c\n" +
                        "679 555 628 499 538 435 c\n" +
                        "488 399 467 376 467 292 c\n" +
                        "313 292 l\n" +
                        "h\n" +
                        "308 214 170 -164 re\n" +
                        "f\n" +
                        "0.44 G\n" +
                        "1.2 w\n" +
                        "1 1 0.4 rg\n" +
                        "287 318 m\n" +
                        "287 430 299 479 406 555 c\n" +
                        "451 587 478 623 478 671 c\n" +
                        "478 762 414 786 365 786 c\n" +
                        "260 786 245 707 239 652 c\n" +
                        "239 651 l\n" +
                        "74 651 l\n" +
                        "74 854 227 924 355 924 c\n" +
                        "425 924 653 904 653 676 c\n" +
                        "653 581 602 525 512 461 c\n" +
                        "462 425 441 402 441 318 c\n" +
                        "287 318 l\n" +
                        "h\n" +
                        "282 240 170 -164 re\n" +
                        "B\n" +
                        "Q\n"

        // creating the appearance

        /** extra space at the top.  */
        private val TOP_SECTION = 0.3f

        /** margin for the content inside the signature rectangle.  */
        private val MARGIN = 2f
    }
}
