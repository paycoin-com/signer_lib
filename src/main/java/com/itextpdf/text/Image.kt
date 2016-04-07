/*
 * $Id: 2b2cffd3b2dacfabf26f3899e97763bbae64aa8c $
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

import com.itextpdf.awt.PdfGraphics2D
import com.itextpdf.text.api.Indentable
import com.itextpdf.text.api.Spaceable
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.ICC_Profile
import com.itextpdf.text.pdf.PRIndirectReference
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfIndirectReference
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfOCG
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStream
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.PdfTemplate
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.RandomAccessFileOrArray
import com.itextpdf.text.pdf.codec.BmpImage
import com.itextpdf.text.pdf.codec.CCITTG4Encoder
import com.itextpdf.text.pdf.codec.GifImage
import com.itextpdf.text.pdf.codec.JBIG2Image
import com.itextpdf.text.pdf.codec.PngImage
import com.itextpdf.text.pdf.codec.TiffImage
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.interfaces.IAlternateDescription

import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Constructor
import java.net.MalformedURLException
import java.net.URL
import java.util.HashMap

/**
 * An Image is the representation of a graphic element (JPEG, PNG
 * or GIF) that has to be inserted into the document

 * @see Element

 * @see Rectangle
 */

abstract class Image : Rectangle, Indentable, Spaceable, IAccessibleElement, IAlternateDescription {

    // member variables

    /** The image type.  */
    protected var type: Int = 0

    /** The URL of the image.  */
    // getters and setters

    /**
     * Gets the String -representation of the reference to the
     * image.

     * @return a String
     */

    /**
     * Sets the url of the image

     * @param url
     * *            the url of the image
     */
    var url: URL

    /** The raw data of the image.  */
    /**
     * Gets the raw data for the image.
     *
     * Remark: this only makes sense for Images of the type RawImage
     * .

     * @return the raw data
     */
    var rawData: ByteArray
        protected set

    /** The bits per component of the raw image. It also flags a CCITT image.  */
    /**
     * Gets the bpc for the image.
     *
     * Remark: this only makes sense for Images of the type RawImage
     * .

     * @return a bpc value
     */
    var bpc = 1
        protected set

    /** The template to be treated as an image.  */
    protected var template = arrayOfNulls<PdfTemplate>(1)

    /** The alignment of the Image.  */
    /**
     * Gets the alignment for the image.

     * @return a value
     */
    /**
     * Sets the alignment for the image.

     * @param alignment
     * *            the alignment
     */

    var alignment: Int = 0

    /** Text that can be shown instead of the image.  */
    /**
     * Gets the alternative text for the image.

     * @return a String
     */

    /**
     * Sets the alternative information for the image.

     * @param alt
     * *            the alternative information
     */

    override var alt: String
        set(alt) {
            this.alt = alt
            setAccessibleAttribute(PdfName.ALT, PdfString(alt))
        }

    /** This is the absolute X-position of the image.  */
    /**
     * Returns the absolute X position.

     * @return a position
     */
    var absoluteX = java.lang.Float.NaN
        protected set

    /** This is the absolute Y-position of the image.  */
    /**
     * Returns the absolute Y position.

     * @return a position
     */
    var absoluteY = java.lang.Float.NaN
        protected set

    /** This is the width of the image without rotation.  */
    /**
     * Gets the plain width of the image.

     * @return a value
     */
    var plainWidth: Float = 0.toFloat()
        protected set

    /** This is the width of the image without rotation.  */
    /**
     * Gets the plain height of the image.

     * @return a value
     */
    var plainHeight: Float = 0.toFloat()
        protected set

    /** This is the scaled width of the image taking rotation into account.  */
    // width and height

    /**
     * Gets the scaled width of the image.

     * @return a value
     */
    var scaledWidth: Float = 0.toFloat()
        protected set

    /** This is the original height of the image taking rotation into account.  */
    /**
     * Gets the scaled height of the image.

     * @return a value
     */
    var scaledHeight: Float = 0.toFloat()
        protected set

    /**
     * The compression level of the content streams.
     * @since    2.1.3
     */
    /**
     * Returns the compression level used for images written as a compressed stream.
     * @return the compression level (0 = best speed, 9 = best compression, -1 is default)
     * *
     * @since    2.1.3
     */
    /**
     * Sets the compression level to be used if the image is written as a compressed stream.
     * @param compressionLevel a value between 0 (best speed) and 9 (best compression)
     * *
     * @since    2.1.3
     */
    var compressionLevel = PdfStream.DEFAULT_COMPRESSION
        set(compressionLevel) = if (compressionLevel < PdfStream.NO_COMPRESSION || compressionLevel > PdfStream.BEST_COMPRESSION)
            this.compressionLevel = PdfStream.DEFAULT_COMPRESSION
        else
            this.compressionLevel = compressionLevel

    /** an iText attributed unique id for this image.  */
    /**
     * Returns a serial id for the Image (reuse the same image more than once)

     * @return a serialId
     */
    var mySerialId = getSerialId()
        protected set

    override var role = PdfName.FIGURE
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }


    // image from file or URL

    /**
     * Constructs an Image -object, using an url .

     * @param url
     * *            the URL where the image can be found.
     */
    constructor(url: URL) : super(0f, 0f) {
        this.url = url
        this.alignment = DEFAULT
        rotationRadians = 0f
    }

    // image from indirect reference

    /**
     * Holds value of property directReference.
     * An image is embedded into a PDF as an Image XObject.
     * This object is referenced by a PdfIndirectReference object.
     */
    /**
     * Getter for property directReference.
     * @return Value of property directReference.
     */
    /**
     * Setter for property directReference.
     * @param directReference New value of property directReference.
     */
    var directReference: PdfIndirectReference? = null

    // copy constructor

    /**
     * Constructs an Image -object, using an url .

     * @param image
     * *            another Image object.
     */
    protected constructor(image: Image) : super(image) {
        this.type = image.type
        this.url = image.url
        this.rawData = image.rawData
        this.bpc = image.bpc
        this.template = image.template
        this.alignment = image.alignment
        this.alt = image.alt
        this.absoluteX = image.absoluteX
        this.absoluteY = image.absoluteY
        this.plainWidth = image.plainWidth
        this.plainHeight = image.plainHeight
        this.scaledWidth = image.scaledWidth
        this.scaledHeight = image.scaledHeight
        this.mySerialId = image.mySerialId

        this.directReference = image.directReference

        this.rotationRadians = image.rotationRadians
        this.initialRotation = image.initialRotation
        this.indentationLeft = image.indentationLeft
        this.indentationRight = image.indentationRight
        this.spacingBefore = image.spacingBefore
        this.spacingAfter = image.spacingAfter

        this.widthPercentage = image.widthPercentage
        this.isScaleToFitLineWhenOverflow = image.isScaleToFitLineWhenOverflow
        this.isScaleToFitHeight = image.isScaleToFitHeight
        this.annotation = image.annotation
        this.layer = image.layer
        this.isInterpolation = image.isInterpolation
        this.originalType = image.originalType
        this.originalData = image.originalData
        this.isDeflated = image.isDeflated
        this.dpiX = image.dpiX
        this.dpiY = image.dpiY
        this.xyRatio = image.xyRatio

        this.colorspace = image.colorspace
        this.isInverted = image.isInverted
        this.iccProfile = image.iccProfile
        this.additional = image.additional
        this.isMask = image.isMask
        this.imageMask = image.imageMask
        this.isSmask = image.isSmask
        this.transparency = image.transparency
        this.role = image.role
        if (image.accessibleAttributes != null)
            this.accessibleAttributes = HashMap(image.accessibleAttributes)
        id = image.id
    }

    // implementation of the Element interface

    /**
     * Returns the type.

     * @return a type
     */

    override fun type(): Int {
        return type
    }

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override val isNestable: Boolean
        get() = true

    // checking the type of Image

    /**
     * Returns true if the image is a Jpeg
     * -object.

     * @return a boolean
     */

    val isJpeg: Boolean
        get() = type == Element.JPEG

    /**
     * Returns true if the image is a ImgRaw
     * -object.

     * @return a boolean
     */

    val isImgRaw: Boolean
        get() = type == Element.IMGRAW

    /**
     * Returns true if the image is an ImgTemplate
     * -object.

     * @return a boolean
     */

    val isImgTemplate: Boolean
        get() = type == Element.IMGTEMPLATE

    /**
     * Gets the template to be used as an image.
     *
     * Remark: this only makes sense for Images of the type ImgTemplate
     * .

     * @return the template
     */
    /**
     * Sets data from a PdfTemplate

     * @param template
     * *            the template with the content
     */
    var templateData: PdfTemplate
        get() = template[0]
        set(template) {
            this.template[0] = template
        }

    /**
     * Sets the absolute position of the Image.

     * @param absoluteX
     * *
     * @param absoluteY
     */

    fun setAbsolutePosition(absoluteX: Float, absoluteY: Float) {
        this.absoluteX = absoluteX
        this.absoluteY = absoluteY
    }

    /**
     * Checks if the Images has to be added at an absolute X
     * position.

     * @return a boolean
     */
    fun hasAbsoluteX(): Boolean {
        return !java.lang.Float.isNaN(absoluteX)
    }

    /**
     * Checks if the Images has to be added at an absolute
     * position.

     * @return a boolean
     */
    fun hasAbsoluteY(): Boolean {
        return !java.lang.Float.isNaN(absoluteY)
    }

    /**
     * Scale the image to the dimensions of the rectangle

     * @param rectangle dimensions to scale the Image
     */
    fun scaleAbsolute(rectangle: Rectangle) {
        scaleAbsolute(rectangle.width, rectangle.height)
    }

    /**
     * Scale the image to an absolute width and an absolute height.

     * @param newWidth
     * *            the new width
     * *
     * @param newHeight
     * *            the new height
     */
    fun scaleAbsolute(newWidth: Float, newHeight: Float) {
        plainWidth = newWidth
        plainHeight = newHeight
        val matrix = matrix()
        scaledWidth = matrix[DX] - matrix[CX]
        scaledHeight = matrix[DY] - matrix[CY]
        widthPercentage = 0
    }

    /**
     * Scale the image to an absolute width.

     * @param newWidth
     * *            the new width
     */
    fun scaleAbsoluteWidth(newWidth: Float) {
        plainWidth = newWidth
        val matrix = matrix()
        scaledWidth = matrix[DX] - matrix[CX]
        scaledHeight = matrix[DY] - matrix[CY]
        widthPercentage = 0
    }

    /**
     * Scale the image to an absolute height.

     * @param newHeight
     * *            the new height
     */
    fun scaleAbsoluteHeight(newHeight: Float) {
        plainHeight = newHeight
        val matrix = matrix()
        scaledWidth = matrix[DX] - matrix[CX]
        scaledHeight = matrix[DY] - matrix[CY]
        widthPercentage = 0
    }

    /**
     * Scale the image to a certain percentage.

     * @param percent
     * *            the scaling percentage
     */
    fun scalePercent(percent: Float) {
        scalePercent(percent, percent)
    }

    /**
     * Scale the width and height of an image to a certain percentage.

     * @param percentX
     * *            the scaling percentage of the width
     * *
     * @param percentY
     * *            the scaling percentage of the height
     */
    fun scalePercent(percentX: Float, percentY: Float) {
        plainWidth = width * percentX / 100f
        plainHeight = height * percentY / 100f
        val matrix = matrix()
        scaledWidth = matrix[DX] - matrix[CX]
        scaledHeight = matrix[DY] - matrix[CY]
        widthPercentage = 0
    }

    /**
     * Scales the images to the dimensions of the rectangle.

     * @param rectangle the dimensions to fit
     */
    fun scaleToFit(rectangle: Rectangle) {
        scaleToFit(rectangle.width, rectangle.height)
    }

    /**
     * Scales the image so that it fits a certain width and height.

     * @param fitWidth
     * *            the width to fit
     * *
     * @param fitHeight
     * *            the height to fit
     */
    fun scaleToFit(fitWidth: Float, fitHeight: Float) {
        scalePercent(100f)
        val percentX = fitWidth * 100 / scaledWidth
        val percentY = fitHeight * 100 / scaledHeight
        scalePercent(if (percentX < percentY) percentX else percentY)
        widthPercentage = 0
    }

    /**
     * Returns the transformation matrix of the image.

     * @return an array [AX, AY, BX, BY, CX, CY, DX, DY]
     */
    @JvmOverloads fun matrix(scalePercentage: Float = 1f): FloatArray {
        val matrix = FloatArray(8)
        val cosX = Math.cos(rotationRadians.toDouble()).toFloat()
        val sinX = Math.sin(rotationRadians.toDouble()).toFloat()
        matrix[AX] = plainWidth * cosX * scalePercentage
        matrix[AY] = plainWidth * sinX * scalePercentage
        matrix[BX] = -plainHeight * sinX * scalePercentage
        matrix[BY] = plainHeight * cosX * scalePercentage
        if (rotationRadians < Math.PI / 2f) {
            matrix[CX] = matrix[BX]
            matrix[CY] = 0f
            matrix[DX] = matrix[AX]
            matrix[DY] = matrix[AY] + matrix[BY]
        } else if (rotationRadians < Math.PI) {
            matrix[CX] = matrix[AX] + matrix[BX]
            matrix[CY] = matrix[BY]
            matrix[DX] = 0f
            matrix[DY] = matrix[AY]
        } else if (rotationRadians < Math.PI * 1.5f) {
            matrix[CX] = matrix[AX]
            matrix[CY] = matrix[AY] + matrix[BY]
            matrix[DX] = matrix[BX]
            matrix[DY] = 0f
        } else {
            matrix[CX] = 0f
            matrix[CY] = matrix[AY]
            matrix[DX] = matrix[AX] + matrix[BX]
            matrix[DY] = matrix[BY]
        }
        return matrix
    }

    // rotation, note that the superclass also has a rotation value.

    /** This is the rotation of the image in radians.  */
    protected var rotationRadians: Float = 0.toFloat()

    /** Holds value of property initialRotation.  */
    /**
     * Getter for property initialRotation.
     * @return Value of property initialRotation.
     */
    /**
     * Some image formats, like TIFF may present the images rotated that have
     * to be compensated.
     * @param initialRotation New value of property initialRotation.
     */
    var initialRotation: Float = 0.toFloat()
        set(initialRotation) {
            val old_rot = rotationRadians - this.initialRotation
            this.initialRotation = initialRotation
            setRotation(old_rot)
        }

    /**
     * Gets the current image rotation in radians.
     * @return the current image rotation in radians
     */
    val imageRotation: Float
        get() {
            val d = 2.0 * Math.PI
            var rot = ((rotationRadians - initialRotation) % d).toFloat()
            if (rot < 0) {
                rot += d.toFloat()
            }
            return rot
        }

    /**
     * Sets the rotation of the image in radians.

     * @param r
     * *            rotation in radians
     */
    fun setRotation(r: Float) {
        val d = 2.0 * Math.PI
        rotationRadians = ((r + initialRotation) % d).toFloat()
        if (rotationRadians < 0) {
            rotationRadians += d.toFloat()
        }
        val matrix = matrix()
        scaledWidth = matrix[DX] - matrix[CX]
        scaledHeight = matrix[DY] - matrix[CY]
    }

    /**
     * Sets the rotation of the image in degrees.

     * @param deg
     * *            rotation in degrees
     */
    fun setRotationDegrees(deg: Float) {
        val d = Math.PI
        setRotation(deg / 180 * d.toFloat())
    }

    // indentations

    /** the indentation to the left.  */
    /**
     * Gets the left indentation.

     * @return the left indentation
     */
    /**
     * Sets the left indentation.

     * @param f
     */
    override var indentationLeft = 0f

    /** the indentation to the right.  */
    /**
     * Gets the right indentation.

     * @return the right indentation
     */
    /**
     * Sets the right indentation.

     * @param f
     */
    override var indentationRight = 0f

    /** The spacing before the image.  */
    /**
     * Gets the spacing before this image.

     * @return the spacing
     */
    /**
     * Sets the spacing before this image.

     * @param spacing
     * *            the new spacing
     */

    override var spacingBefore: Float = 0.toFloat()

    /** The spacing after the image.  */
    /**
     * Gets the spacing before this image.

     * @return the spacing
     */
    /**
     * Sets the spacing after this image.

     * @param spacing
     * *            the new spacing
     */

    override var spacingAfter: Float = 0.toFloat()

    /** Padding top  */
    override var paddingTop: Float = 0.toFloat()

    // widthpercentage (for the moment only used in ColumnText)

    /**
     * Holds value of property widthPercentage.
     */
    /**
     * Getter for property widthPercentage.

     * @return Value of property widthPercentage.
     */
    /**
     * Setter for property widthPercentage.

     * @param widthPercentage
     * *            New value of property widthPercentage.
     */
    var widthPercentage = 100f

    // scaling the image to the available width (or not)

    /**
     * Indicates if the image should be scaled to fit the line
     * when the image exceeds the available width.
     * @since iText 5.0.6
     */
    /**
     * Gets the value of scaleToFitLineWhenOverflow.
     * @return true if the image size has to scale to the available width
     * *
     * @since iText 5.0.6
     */
    /**
     * Sets the value of scaleToFitLineWhenOverflow
     * @param scaleToFitLineWhenOverflow true if you want the image to scale to the available width
     * *
     * @since iText 5.0.6
     */
    var isScaleToFitLineWhenOverflow: Boolean = false

    // scaling the image to the available height (or not)

    /**
     * Indicates if the image should be scaled to fit
     * when the image exceeds the available height.
     * @since iText 5.4.2
     */
    /**
     * Gets the value of scaleToFitHeight.
     * @return true if the image size has to scale to the available height
     * *
     * @since iText 5.4.2
     */
    /**
     * Sets the value of scaleToFitHeight
     * @param scaleToFitHeight true if you want the image to scale to the available height
     * *
     * @since iText 5.4.2
     */
    var isScaleToFitHeight = true

    // annotation

    /** if the annotation is not null the image will be clickable.  */
    /**
     * Gets the annotation.

     * @return the annotation that is linked to this image
     */
    /**
     * Sets the annotation of this Image.

     * @param annotation
     * *            the annotation
     */
    var annotation: Annotation? = null

    // Optional Content

    /** Optional Content layer to which we want this Image to belong.  */
    /**
     * Gets the layer this image belongs to.

     * @return the layer this image belongs to or `null` for no
     * *         layer defined
     */
    /**
     * Sets the layer this image belongs to.

     * @param layer
     * *            the layer this image belongs to
     */
    var layer: PdfOCG

    // interpolation

    /** Holds value of property interpolation.  */
    /**
     * Getter for property interpolation.

     * @return Value of property interpolation.
     */
    /**
     * Sets the image interpolation. Image interpolation attempts to produce a
     * smooth transition between adjacent sample values.

     * @param interpolation
     * *            New value of property interpolation.
     */
    var isInterpolation: Boolean = false

    // original type and data

    /** Holds value of property originalType.  */
    /**
     * Getter for property originalType.

     * @return Value of property originalType.
     */
    /**
     * Setter for property originalType.

     * @param originalType
     * *            New value of property originalType.
     */
    var originalType = ORIGINAL_NONE

    /** Holds value of property originalData.  */
    /**
     * Getter for property originalData.

     * @return Value of property originalData.
     */
    /**
     * Setter for property originalData.

     * @param originalData
     * *            New value of property originalData.
     */
    var originalData: ByteArray? = null

    // the following values are only set for specific types of images.

    /** Holds value of property deflated.  */
    /**
     * Getter for property deflated.

     * @return Value of property deflated.
     */
    /**
     * Setter for property deflated.

     * @param deflated
     * *            New value of property deflated.
     */
    var isDeflated = false

    // DPI info

    /** Holds value of property dpiX.  */
    /**
     * Gets the dots-per-inch in the X direction. Returns 0 if not available.

     * @return the dots-per-inch in the X direction
     */
    var dpiX = 0
        protected set

    /** Holds value of property dpiY.  */
    /**
     * Gets the dots-per-inch in the Y direction. Returns 0 if not available.

     * @return the dots-per-inch in the Y direction
     */
    var dpiY = 0
        protected set

    /**
     * Sets the dots per inch value

     * @param dpiX
     * *            dpi for x coordinates
     * *
     * @param dpiY
     * *            dpi for y coordinates
     */
    fun setDpi(dpiX: Int, dpiY: Int) {
        this.dpiX = dpiX
        this.dpiY = dpiY
    }

    // XY Ratio

    /** Holds value of property XYRatio.  */
    /**
     * Gets the X/Y pixel dimensionless aspect ratio.

     * @return the X/Y pixel dimensionless aspect ratio
     */
    /**
     * Sets the X/Y pixel dimensionless aspect ratio.

     * @param XYRatio
     * *            the X/Y pixel dimensionless aspect ratio
     */
    var xyRatio = 0f

    // color, colorspaces and transparency

    /** this is the colorspace of a jpeg-image.  */
    /**
     * Gets the colorspace for the image.
     *
     * Remark: this only makes sense for Images of the type Jpeg.

     * @return a colorspace value
     */
    var colorspace = -1
        protected set

    var colorTransform = 1

    /** Image color inversion  */
    /**
     * Getter for the inverted value

     * @return true if the image is inverted
     */
    /**
     * Sets inverted true or false

     * @param invert
     * *            true or false
     */
    var isInverted = false

    /** ICC Profile attached  */
    /**
     * Gets the images ICC profile.

     * @return the ICC profile
     */
    var iccProfile: ICC_Profile? = null
        protected set

    /**
     * Tags this image with an ICC profile.

     * @param profile
     * *            the profile
     */
    fun tagICC(profile: ICC_Profile) {
        this.iccProfile = profile
    }

    /**
     * Checks is the image has an ICC profile.

     * @return the ICC profile or null
     */
    fun hasICCProfile(): Boolean {
        return this.iccProfile != null
    }

    /** a dictionary with additional information  */
    /**
     * Getter for the dictionary with additional information.

     * @return a PdfDictionary with additional information.
     */
    /**
     * Sets the /Colorspace key.

     * @param additional
     * *            a PdfDictionary with additional information.
     */
    var additional: PdfDictionary? = null

    /**
     * Replaces CalRGB and CalGray colorspaces with DeviceRGB and DeviceGray.
     */
    fun simplifyColorspace() {
        if (additional == null)
            return
        val value = additional!!.getAsArray(PdfName.COLORSPACE) ?: return
        val cs = simplifyColorspace(value)
        val newValue: PdfObject
        if (cs.isName)
            newValue = cs
        else {
            newValue = value
            val first = value.getAsName(0)
            if (PdfName.INDEXED.equals(first)) {
                if (value.size() >= 2) {
                    val second = value.getAsArray(1)
                    if (second != null) {
                        value[1] = simplifyColorspace(second)
                    }
                }
            }
        }
        additional!!.put(PdfName.COLORSPACE, newValue)
    }

    /**
     * Gets a PDF Name from an array or returns the object that was passed.
     */
    private fun simplifyColorspace(obj: PdfArray?): PdfObject {
        if (obj == null)
            return obj
        val first = obj.getAsName(0)
        if (PdfName.CALGRAY == first)
            return PdfName.DEVICEGRAY
        else if (PdfName.CALRGB == first)
            return PdfName.DEVICERGB
        else
            return obj
    }

    /** Is this image a mask?  */
    /**
     * Returns true if this Image is a mask.

     * @return true if this Image is a mask
     */
    var isMask = false
        protected set

    /** The image that serves as a mask for this image.  */
    /**
     * Gets the explicit masking.

     * @return the explicit masking
     */
    /**
     * Sets the explicit masking.

     * @param mask
     * *            the mask to be applied
     * *
     * @throws DocumentException
     * *             on error
     */
    var imageMask: Image
        @Throws(DocumentException::class)
        set(mask) {
            if (this.isMask)
                throw DocumentException(MessageLocalization.getComposedMessage("an.image.mask.cannot.contain.another.image.mask"))
            if (!mask.isMask)
                throw DocumentException(MessageLocalization.getComposedMessage("the.image.mask.is.not.a.mask.did.you.do.makemask"))
            imageMask = mask
            isSmask = mask.bpc > 1 && mask.bpc <= 8
        }

    /** Holds value of property smask.  */
    /**
     * Getter for property smask.

     * @return Value of property smask.
     */
    /**
     * Setter for property smask.

     * @param smask
     * *            New value of property smask.
     */
    var isSmask: Boolean = false

    /**
     * Make this Image a mask.

     * @throws DocumentException
     * *             if this Image can not be a mask
     */
    @Throws(DocumentException::class)
    fun makeMask() {
        if (!isMaskCandidate)
            throw DocumentException(MessageLocalization.getComposedMessage("this.image.can.not.be.an.image.mask"))
        isMask = true
    }

    /**
     * Returns true if this Image has the
     * requisites to be a mask.

     * @return true if this Image can be a mask
     */
    val isMaskCandidate: Boolean
        get() {
            if (type == Element.IMGRAW) {
                if (bpc > 0xff)
                    return true
            }
            return colorspace == 1
        }

    /** this is the transparency information of the raw image  */
    /**
     * Returns the transparency.

     * @return the transparency values
     */

    /**
     * Sets the transparency values

     * @param transparency
     * *            the transparency values
     */
    var transparency: IntArray

    override fun getAccessibleAttribute(key: PdfName): PdfObject {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    override fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = true

    companion object {

        // static final membervariables

        /** this is a kind of image alignment.  */
        val DEFAULT = 0

        /** this is a kind of image alignment.  */
        val RIGHT = 2

        /** this is a kind of image alignment.  */
        val LEFT = 0

        /** this is a kind of image alignment.  */
        val MIDDLE = 1

        /** this is a kind of image alignment.  */
        val TEXTWRAP = 4

        /** this is a kind of image alignment.  */
        val UNDERLYING = 8

        /** This represents a coordinate in the transformation matrix.  */
        val AX = 0

        /** This represents a coordinate in the transformation matrix.  */
        val AY = 1

        /** This represents a coordinate in the transformation matrix.  */
        val BX = 2

        /** This represents a coordinate in the transformation matrix.  */
        val BY = 3

        /** This represents a coordinate in the transformation matrix.  */
        val CX = 4

        /** This represents a coordinate in the transformation matrix.  */
        val CY = 5

        /** This represents a coordinate in the transformation matrix.  */
        val DX = 6

        /** This represents a coordinate in the transformation matrix.  */
        val DY = 7

        /** type of image  */
        val ORIGINAL_NONE = 0

        /** type of image  */
        val ORIGINAL_JPEG = 1

        /** type of image  */
        val ORIGINAL_PNG = 2

        /** type of image  */
        val ORIGINAL_GIF = 3

        /** type of image  */
        val ORIGINAL_BMP = 4

        /** type of image  */
        val ORIGINAL_TIFF = 5

        /** type of image  */
        val ORIGINAL_WMF = 6

        /** type of image  */
        val ORIGINAL_PS = 7

        /** type of image  */
        val ORIGINAL_JPEG2000 = 8

        /**
         * type of image
         * @since    2.1.5
         */
        val ORIGINAL_JBIG2 = 9

        @Throws(BadElementException::class, MalformedURLException::class, IOException::class)
        fun getInstance(url: URL): Image {
            return Image.getInstance(url, false)
        }

        /**
         * Gets an instance of an Image.

         * @param url
         * *            an URL
         * *
         * @return an Image
         * *
         * @throws BadElementException
         * *
         * @throws MalformedURLException
         * *
         * @throws IOException
         */
        @Throws(BadElementException::class, MalformedURLException::class, IOException::class)
        fun getInstance(url: URL, recoverFromImageError: Boolean): Image {
            var `is`: InputStream? = null
            val randomAccessSourceFactory = RandomAccessSourceFactory()

            try {
                `is` = url.openStream()
                val c1 = `is`!!.read()
                val c2 = `is`.read()
                val c3 = `is`.read()
                val c4 = `is`.read()
                // jbig2
                val c5 = `is`.read()
                val c6 = `is`.read()
                val c7 = `is`.read()
                val c8 = `is`.read()
                `is`.close()

                `is` = null
                if (c1 == 'G' && c2 == 'I' && c3 == 'F') {
                    val gif = GifImage(url)
                    val img = gif.getImage(1)
                    return img
                }
                if (c1 == 0xFF && c2 == 0xD8) {
                    return Jpeg(url)
                }
                if (c1 == 0x00 && c2 == 0x00 && c3 == 0x00 && c4 == 0x0c) {
                    return Jpeg2000(url)
                }
                if (c1 == 0xff && c2 == 0x4f && c3 == 0xff && c4 == 0x51) {
                    return Jpeg2000(url)
                }
                if (c1 == PngImage.PNGID[0] && c2 == PngImage.PNGID[1]
                        && c3 == PngImage.PNGID[2] && c4 == PngImage.PNGID[3]) {
                    return PngImage.getImage(url)
                }
                if (c1 == 0xD7 && c2 == 0xCD) {
                    return ImgWMF(url)
                }
                if (c1 == 'B' && c2 == 'M') {
                    return BmpImage.getImage(url)
                }
                if (c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42 || c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0) {
                    var ra: RandomAccessFileOrArray? = null
                    try {
                        if (url.protocol == "file") {
                            var file = url.file
                            file = Utilities.unEscapeURL(file)
                            ra = RandomAccessFileOrArray(randomAccessSourceFactory.createBestSource(file))
                        } else
                            ra = RandomAccessFileOrArray(randomAccessSourceFactory.createSource(url))
                        val img = TiffImage.getTiffImage(ra, 1)
                        img.url = url
                        return img
                    } catch (e: RuntimeException) {
                        if (recoverFromImageError) {
                            // reruns the getTiffImage() with several error recovering workarounds in place
                            // not guaranteed to work with every TIFF
                            val img = TiffImage.getTiffImage(ra, recoverFromImageError.toInt(), 1)
                            img.url = url
                            return img
                        }
                        throw e
                    } finally {
                        if (ra != null)
                            ra.close()
                    }

                }
                if (c1 == 0x97 && c2 == 'J' && c3 == 'B' && c4 == '2' &&
                        c5 == '\r' && c6 == '\n' && c7 == 0x1a && c8 == '\n') {
                    var ra: RandomAccessFileOrArray? = null
                    try {
                        if (url.protocol == "file") {
                            var file = url.file
                            file = Utilities.unEscapeURL(file)
                            ra = RandomAccessFileOrArray(randomAccessSourceFactory.createBestSource(file))
                        } else
                            ra = RandomAccessFileOrArray(randomAccessSourceFactory.createSource(url))
                        val img = JBIG2Image.getJbig2Image(ra, 1)
                        img.url = url
                        return img
                    } finally {
                        if (ra != null)
                            ra.close()
                    }
                }
                throw IOException(MessageLocalization.getComposedMessage("unknown.image.format", url.toString()))
            } finally {
                if (`is` != null) {
                    `is`.close()
                }
            }
        }

        /**
         * Gets an instance of an Image.

         * @param filename
         * *            a filename
         * *
         * @return an object of type Gif,Jpeg or
         * *         Png
         * *
         * @throws BadElementException
         * *
         * @throws MalformedURLException
         * *
         * @throws IOException
         */
        @Throws(BadElementException::class, MalformedURLException::class, IOException::class)
        fun getInstance(filename: String): Image {
            return getInstance(Utilities.toURL(filename))
        }

        @Throws(IOException::class, BadElementException::class)
        fun getInstance(filename: String, recoverFromImageError: Boolean): Image {
            return getInstance(Utilities.toURL(filename), recoverFromImageError)
        }

        /**
         * gets an instance of an Image

         * @param imgb
         * *            raw image date
         * *
         * @return an Image object
         * *
         * @throws BadElementException
         * *
         * @throws MalformedURLException
         * *
         * @throws IOException
         */
        @Throws(BadElementException::class, MalformedURLException::class, IOException::class)
        @JvmOverloads fun getInstance(imgb: ByteArray, recoverFromImageError: Boolean = false): Image {
            var `is`: InputStream? = null
            val randomAccessSourceFactory = RandomAccessSourceFactory()
            try {
                `is` = java.io.ByteArrayInputStream(imgb)
                val c1 = `is`.read()
                val c2 = `is`.read()
                val c3 = `is`.read()
                val c4 = `is`.read()
                `is`.close()

                `is` = null
                if (c1 == 'G' && c2 == 'I' && c3 == 'F') {
                    val gif = GifImage(imgb)
                    return gif.getImage(1)
                }
                if (c1 == 0xFF && c2 == 0xD8) {
                    return Jpeg(imgb)
                }
                if (c1 == 0x00 && c2 == 0x00 && c3 == 0x00 && c4 == 0x0c) {
                    return Jpeg2000(imgb)
                }
                if (c1 == 0xff && c2 == 0x4f && c3 == 0xff && c4 == 0x51) {
                    return Jpeg2000(imgb)
                }
                if (c1 == PngImage.PNGID[0] && c2 == PngImage.PNGID[1]
                        && c3 == PngImage.PNGID[2] && c4 == PngImage.PNGID[3]) {
                    return PngImage.getImage(imgb)
                }
                if (c1 == 0xD7 && c2 == 0xCD) {
                    return ImgWMF(imgb)
                }
                if (c1 == 'B' && c2 == 'M') {
                    return BmpImage.getImage(imgb)
                }
                if (c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42 || c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0) {
                    var ra: RandomAccessFileOrArray? = null
                    try {
                        ra = RandomAccessFileOrArray(randomAccessSourceFactory.createSource(imgb))
                        val img = TiffImage.getTiffImage(ra, 1)
                        if (img.originalData == null)
                            img.originalData = imgb
                        return img
                    } catch (e: RuntimeException) {
                        if (recoverFromImageError) {
                            // reruns the getTiffImage() with several error recovering workarounds in place
                            // not guaranteed to work with every TIFF
                            val img = TiffImage.getTiffImage(ra, recoverFromImageError.toInt(), 1)
                            if (img.originalData == null)
                                img.originalData = imgb
                            return img
                        }
                        throw e
                    } finally {
                        if (ra != null)
                            ra.close()
                    }

                }
                if (c1 == 0x97 && c2 == 'J' && c3 == 'B' && c4 == '2') {
                    `is` = java.io.ByteArrayInputStream(imgb)
                    `is`.skip(4)
                    val c5 = `is`.read()
                    val c6 = `is`.read()
                    val c7 = `is`.read()
                    val c8 = `is`.read()
                    `is`.close()
                    if (c5 == '\r' && c6 == '\n' && c7 == 0x1a && c8 == '\n') {
                        // a jbig2 file with a file header.  the header is the only way we know here.
                        // embedded jbig2s don't have a header, have to create them by explicit use of Jbig2Image?
                        // nkerr, 2008-12-05  see also the getInstance(URL)
                        var ra: RandomAccessFileOrArray? = null
                        try {
                            ra = RandomAccessFileOrArray(randomAccessSourceFactory.createSource(imgb))
                            val img = JBIG2Image.getJbig2Image(ra, 1)
                            if (img.originalData == null)
                                img.originalData = imgb
                            return img
                        } finally {
                            if (ra != null)
                                ra.close()
                        }
                    }
                }
                throw IOException(MessageLocalization.getComposedMessage("the.byte.array.is.not.a.recognized.imageformat"))
            } finally {
                if (`is` != null) {
                    `is`.close()
                }
            }
        }

        /**
         * Gets an instance of an Image in raw mode.

         * @param width
         * *            the width of the image in pixels
         * *
         * @param height
         * *            the height of the image in pixels
         * *
         * @param components
         * *            1,3 or 4 for GrayScale, RGB and CMYK
         * *
         * @param data
         * *            the image data
         * *
         * @param bpc
         * *            bits per component
         * *
         * @return an object of type ImgRaw
         * *
         * @throws BadElementException
         * *             on error
         */
        @Throws(BadElementException::class)
        fun getInstance(width: Int, height: Int, components: Int,
                        bpc: Int, data: ByteArray): Image {
            return Image.getInstance(width, height, components, bpc, data, null)
        }

        /**
         * Creates a JBIG2 Image.
         * @param    width    the width of the image
         * *
         * @param    height    the height of the image
         * *
         * @param    data    the raw image data
         * *
         * @param    globals    JBIG2 globals
         * *
         * @return the Image
         * *
         * @since    2.1.5
         */
        fun getInstance(width: Int, height: Int, data: ByteArray, globals: ByteArray): Image {
            return ImgJBIG2(width, height, data, globals)
        }

        /**
         * Creates an Image with CCITT G3 or G4 compression. It assumes that the
         * data bytes are already compressed.

         * @param width
         * *            the exact width of the image
         * *
         * @param height
         * *            the exact height of the image
         * *
         * @param reverseBits
         * *            reverses the bits in `data`. Bit 0 is swapped
         * *            with bit 7 and so on
         * *
         * @param typeCCITT
         * *            the type of compression in `data`. It can be
         * *            CCITTG4, CCITTG31D, CCITTG32D
         * *
         * @param parameters
         * *            parameters associated with this stream. Possible values are
         * *            CCITT_BLACKIS1, CCITT_ENCODEDBYTEALIGN, CCITT_ENDOFLINE and
         * *            CCITT_ENDOFBLOCK or a combination of them
         * *
         * @param data
         * *            the image data
         * *
         * @return an Image object
         * *
         * @throws BadElementException
         * *             on error
         */
        @Throws(BadElementException::class)
        fun getInstance(width: Int, height: Int, reverseBits: Boolean,
                        typeCCITT: Int, parameters: Int, data: ByteArray): Image {
            return Image.getInstance(width, height, reverseBits, typeCCITT,
                    parameters, data, null)
        }

        /**
         * Creates an Image with CCITT G3 or G4 compression. It assumes that the
         * data bytes are already compressed.

         * @param width
         * *            the exact width of the image
         * *
         * @param height
         * *            the exact height of the image
         * *
         * @param reverseBits
         * *            reverses the bits in `data`. Bit 0 is swapped
         * *            with bit 7 and so on
         * *
         * @param typeCCITT
         * *            the type of compression in `data`. It can be
         * *            CCITTG4, CCITTG31D, CCITTG32D
         * *
         * @param parameters
         * *            parameters associated with this stream. Possible values are
         * *            CCITT_BLACKIS1, CCITT_ENCODEDBYTEALIGN, CCITT_ENDOFLINE and
         * *            CCITT_ENDOFBLOCK or a combination of them
         * *
         * @param data
         * *            the image data
         * *
         * @param transparency
         * *            transparency information in the Mask format of the image
         * *            dictionary
         * *
         * @return an Image object
         * *
         * @throws BadElementException
         * *             on error
         */
        @Throws(BadElementException::class)
        fun getInstance(width: Int, height: Int, reverseBits: Boolean,
                        typeCCITT: Int, parameters: Int, data: ByteArray, transparency: IntArray?): Image {
            if (transparency != null && transparency.size != 2)
                throw BadElementException(MessageLocalization.getComposedMessage("transparency.length.must.be.equal.to.2.with.ccitt.images"))
            val img = ImgCCITT(width, height, reverseBits, typeCCITT,
                    parameters, data)
            img.transparency = transparency
            return img
        }

        /**
         * Gets an instance of an Image in raw mode.

         * @param width
         * *            the width of the image in pixels
         * *
         * @param height
         * *            the height of the image in pixels
         * *
         * @param components
         * *            1,3 or 4 for GrayScale, RGB and CMYK
         * *
         * @param data
         * *            the image data
         * *
         * @param bpc
         * *            bits per component
         * *
         * @param transparency
         * *            transparency information in the Mask format of the image
         * *            dictionary
         * *
         * @return an object of type ImgRaw
         * *
         * @throws BadElementException
         * *             on error
         */
        @Throws(BadElementException::class)
        fun getInstance(width: Int, height: Int, components: Int,
                        bpc: Int, data: ByteArray, transparency: IntArray?): Image {
            if (transparency != null && transparency.size != components * 2)
                throw BadElementException(MessageLocalization.getComposedMessage("transparency.length.must.be.equal.to.componentes.2"))
            if (components == 1 && bpc == 1) {
                val g4 = CCITTG4Encoder.compress(data, width, height)
                return Image.getInstance(width, height, false, Image.CCITTG4,
                        Image.CCITT_BLACKIS1, g4, transparency)
            }
            val img = ImgRaw(width, height, components, bpc, data)
            img.transparency = transparency
            return img
        }

        // images from a PdfTemplate

        /**
         * gets an instance of an Image

         * @param template
         * *            a PdfTemplate that has to be wrapped in an Image object
         * *
         * @return an Image object
         * *
         * @throws BadElementException
         */
        @Throws(BadElementException::class)
        fun getInstance(template: PdfTemplate): Image {
            return ImgTemplate(template)
        }

        /**
         * Reuses an existing image.
         * @param ref the reference to the image dictionary
         * *
         * @throws BadElementException on error
         * *
         * @return the image
         */
        @Throws(BadElementException::class)
        fun getInstance(ref: PRIndirectReference): Image {
            val dic = PdfReader.getPdfObjectRelease(ref.toInt()) as PdfDictionary?
            val width = (PdfReader.getPdfObjectRelease(dic.get(PdfName.WIDTH)!!.toInt()) as PdfNumber).intValue()
            val height = (PdfReader.getPdfObjectRelease(dic.get(PdfName.HEIGHT)!!.toInt()) as PdfNumber).intValue()
            var imask: Image? = null
            var obj = dic.get(PdfName.SMASK)
            if (obj != null && obj.isIndirect) {
                imask = getInstance(obj as PRIndirectReference?)
            } else {
                obj = dic.get(PdfName.MASK)
                if (obj != null && obj.isIndirect) {
                    val obj2 = PdfReader.getPdfObjectRelease(obj.toInt())
                    if (obj2 is PdfDictionary)
                        imask = getInstance(obj as PRIndirectReference?)
                }
            }
            val img = ImgRaw(width, height, 1, 1, null)
            img.imageMask = imask
            img.directReference = ref
            return img
        }

        /**
         * gets an instance of an Image

         * @param image
         * *            an Image object
         * *
         * @return a new Image object
         */
        fun getInstance(image: Image?): Image? {
            if (image == null)
                return null
            try {
                val cs = image.javaClass
                val constructor = cs.getDeclaredConstructor(*arrayOf<Class<Any>>(Image::class.java))
                return constructor.newInstance(*arrayOf<Any>(image))
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

        // serial stamping

        /** a static that is used for attributing a unique id to each image.  */
        internal var serialId: Long = 0

        /** Creates a new serial id.
         * @return the new serialId
         */
        @Synchronized protected fun getSerialId(): Long? {
            ++serialId
            return java.lang.Long.valueOf(serialId)
        }

        // AWT related methods (remove this if you port to Android / GAE)

        /**
         * Gets an instance of an Image from a java.awt.Image.

         * @param image
         * *            the java.awt.Image to convert
         * *
         * @param color
         * *            if different from null the transparency pixels
         * *            are replaced by this color
         * *
         * @param forceBW
         * *            if true the image is treated as black and white
         * *
         * @return an object of type ImgRaw
         * *
         * @throws BadElementException
         * *             on error
         * *
         * @throws IOException
         * *             on error
         */
        @Throws(BadElementException::class, IOException::class)
        fun getInstance(image: java.awt.Image, color: java.awt.Color?,
                        forceBW: Boolean): Image {
            var forceBW = forceBW

            if (image is java.awt.image.BufferedImage) {
                if (image.type == java.awt.image.BufferedImage.TYPE_BYTE_BINARY && image.colorModel.pixelSize == 1) {
                    forceBW = true
                }
            }

            val pg = java.awt.image.PixelGrabber(image,
                    0, 0, -1, -1, true)
            try {
                pg.grabPixels()
            } catch (e: InterruptedException) {
                throw IOException(MessageLocalization.getComposedMessage("java.awt.image.interrupted.waiting.for.pixels"))
            }

            if (pg.status and java.awt.image.ImageObserver.ABORT != 0) {
                throw IOException(MessageLocalization.getComposedMessage("java.awt.image.fetch.aborted.or.errored"))
            }
            val w = pg.width
            val h = pg.height
            val pixels = pg.pixels as IntArray
            if (forceBW) {
                val byteWidth = w / 8 + if (w and 7 != 0) 1 else 0
                val pixelsByte = ByteArray(byteWidth * h)

                var index = 0
                val size = h * w
                var transColor = 1
                if (color != null) {
                    transColor = if (color.red + color.green
                            + color.blue < 384)
                        0
                    else
                        1
                }
                var transparency: IntArray? = null
                var cbyte = 0x80
                var wMarker = 0
                var currByte = 0
                if (color != null) {
                    for (j in 0..size - 1) {
                        val alpha = pixels[j] shr 24 and 0xff
                        if (alpha < 250) {
                            if (transColor == 1)
                                currByte = currByte or cbyte
                        } else {
                            if (pixels[j] and 0x888 != 0)
                                currByte = currByte or cbyte
                        }
                        cbyte = cbyte shr 1
                        if (cbyte == 0 || wMarker + 1 >= w) {
                            pixelsByte[index++] = currByte.toByte()
                            cbyte = 0x80
                            currByte = 0
                        }
                        ++wMarker
                        if (wMarker >= w)
                            wMarker = 0
                    }
                } else {
                    for (j in 0..size - 1) {
                        if (transparency == null) {
                            val alpha = pixels[j] shr 24 and 0xff
                            if (alpha == 0) {
                                transparency = IntArray(2)
                                /* bugfix by M.P. Liston, ASC, was: ... ? 1: 0; */
                                transparency[0] = transparency[1] = if (pixels[j] and 0x888 != 0) 0xff else 0
                            }
                        }
                        if (pixels[j] and 0x888 != 0)
                            currByte = currByte or cbyte
                        cbyte = cbyte shr 1
                        if (cbyte == 0 || wMarker + 1 >= w) {
                            pixelsByte[index++] = currByte.toByte()
                            cbyte = 0x80
                            currByte = 0
                        }
                        ++wMarker
                        if (wMarker >= w)
                            wMarker = 0
                    }
                }
                return Image.getInstance(w, h, 1, 1, pixelsByte, transparency)
            } else {
                val pixelsByte = ByteArray(w * h * 3)
                var smask: ByteArray? = null

                var index = 0
                val size = h * w
                var red = 255
                var green = 255
                var blue = 255
                if (color != null) {
                    red = color.red
                    green = color.green
                    blue = color.blue
                }
                var transparency: IntArray? = null
                if (color != null) {
                    for (j in 0..size - 1) {
                        val alpha = pixels[j] shr 24 and 0xff
                        if (alpha < 250) {
                            pixelsByte[index++] = red.toByte()
                            pixelsByte[index++] = green.toByte()
                            pixelsByte[index++] = blue.toByte()
                        } else {
                            pixelsByte[index++] = (pixels[j] shr 16 and 0xff).toByte()
                            pixelsByte[index++] = (pixels[j] shr 8 and 0xff).toByte()
                            pixelsByte[index++] = (pixels[j] and 0xff).toByte()
                        }
                    }
                } else {
                    var transparentPixel = 0
                    smask = ByteArray(w * h)
                    var shades = false
                    for (j in 0..size - 1) {
                        val alpha = smask[j] = (pixels[j] shr 24 and 0xff).toByte()
                        /* bugfix by Chris Nokleberg */
                        if (!shades) {
                            if (alpha.toInt() != 0 && alpha.toInt() != -1) {
                                shades = true
                            } else if (transparency == null) {
                                if (alpha.toInt() == 0) {
                                    transparentPixel = pixels[j] and 0xffffff
                                    transparency = IntArray(6)
                                    transparency[0] = transparency[1] = transparentPixel shr 16 and 0xff
                                    transparency[2] = transparency[3] = transparentPixel shr 8 and 0xff
                                    transparency[4] = transparency[5] = transparentPixel and 0xff
                                }
                            } else if (pixels[j] and 0xffffff != transparentPixel) {
                                shades = true
                            }
                        }
                        pixelsByte[index++] = (pixels[j] shr 16 and 0xff).toByte()
                        pixelsByte[index++] = (pixels[j] shr 8 and 0xff).toByte()
                        pixelsByte[index++] = (pixels[j] and 0xff).toByte()
                    }
                    if (shades)
                        transparency = null
                    else
                        smask = null
                }
                val img = Image.getInstance(w, h, 3, 8, pixelsByte, transparency)
                if (smask != null) {
                    val sm = Image.getInstance(w, h, 1, 8, smask)
                    try {
                        sm.makeMask()
                        img.imageMask = sm
                    } catch (de: DocumentException) {
                        throw ExceptionConverter(de)
                    }

                }
                return img
            }
        }

        /**
         * Gets an instance of an Image from a java.awt.Image.

         * @param image
         * *            the java.awt.Image to convert
         * *
         * @param color
         * *            if different from null the transparency pixels
         * *            are replaced by this color
         * *
         * @return an object of type ImgRaw
         * *
         * @throws BadElementException
         * *             on error
         * *
         * @throws IOException
         * *             on error
         */
        @Throws(BadElementException::class, IOException::class)
        fun getInstance(image: java.awt.Image, color: java.awt.Color): Image {
            return Image.getInstance(image, color, false)
        }

        /**
         * Gets an instance of a Image from a java.awt.Image.
         * The image is added as a JPEG with a user defined quality.

         * @param writer
         * *            the PdfWriter object to which the image will be added
         * *
         * @param awtImage
         * *            the java.awt.Image to convert
         * *
         * @param quality
         * *            a float value between 0 and 1
         * *
         * @return an object of type PdfTemplate
         * *
         * @throws BadElementException
         * *             on error
         * *
         * @throws IOException
         */
        @Throws(BadElementException::class, IOException::class)
        fun getInstance(writer: PdfWriter, awtImage: java.awt.Image, quality: Float): Image {
            return getInstance(PdfContentByte(writer), awtImage, quality)
        }

        /**
         * Gets an instance of a Image from a java.awt.Image.
         * The image is added as a JPEG with a user defined quality.

         * @param cb
         * *            the PdfContentByte object to which the image will be added
         * *
         * @param awtImage
         * *            the java.awt.Image to convert
         * *
         * @param quality
         * *            a float value between 0 and 1
         * *
         * @return an object of type PdfTemplate
         * *
         * @throws BadElementException
         * *             on error
         * *
         * @throws IOException
         */
        @Throws(BadElementException::class, IOException::class)
        fun getInstance(cb: PdfContentByte, awtImage: java.awt.Image, quality: Float): Image {
            val pg = java.awt.image.PixelGrabber(awtImage,
                    0, 0, -1, -1, true)
            try {
                pg.grabPixels()
            } catch (e: InterruptedException) {
                throw IOException(MessageLocalization.getComposedMessage("java.awt.image.interrupted.waiting.for.pixels"))
            }

            if (pg.status and java.awt.image.ImageObserver.ABORT != 0) {
                throw IOException(MessageLocalization.getComposedMessage("java.awt.image.fetch.aborted.or.errored"))
            }
            val w = pg.width
            val h = pg.height
            val tp = cb.createTemplate(w.toFloat(), h.toFloat())
            val g2d = PdfGraphics2D(tp, w.toFloat(), h.toFloat(), null, false, true, quality)
            g2d.drawImage(awtImage, 0, 0, null)
            g2d.dispose()
            return getInstance(tp)
        }
    }
}
/**
 * Returns the transformation matrix of the image.

 * @return an array [AX, AY, BX, BY, CX, CY, DX, DY]
 */
