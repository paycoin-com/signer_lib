/*
 * $Id: 3981a0968c53447583439d8acc65072389b0b9ef $
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
import com.itextpdf.text.pdf.BaseFont

/**
 * If you are using True Type fonts, you can declare the paths of the different ttf- and ttc-files
 * to this static class first and then create fonts in your code using one of the static getFont-method
 * without having to enter a path as parameter.

 * @author  Bruno Lowagie
 */

object FontFactory {

    /** This is a possible value of a base 14 type 1 font  */
    val COURIER = BaseFont.COURIER

    /** This is a possible value of a base 14 type 1 font  */
    val COURIER_BOLD = BaseFont.COURIER_BOLD

    /** This is a possible value of a base 14 type 1 font  */
    val COURIER_OBLIQUE = BaseFont.COURIER_OBLIQUE

    /** This is a possible value of a base 14 type 1 font  */
    val COURIER_BOLDOBLIQUE = BaseFont.COURIER_BOLDOBLIQUE

    /** This is a possible value of a base 14 type 1 font  */
    val HELVETICA = BaseFont.HELVETICA

    /** This is a possible value of a base 14 type 1 font  */
    val HELVETICA_BOLD = BaseFont.HELVETICA_BOLD

    /** This is a possible value of a base 14 type 1 font  */
    val HELVETICA_OBLIQUE = BaseFont.HELVETICA_OBLIQUE

    /** This is a possible value of a base 14 type 1 font  */
    val HELVETICA_BOLDOBLIQUE = BaseFont.HELVETICA_BOLDOBLIQUE

    /** This is a possible value of a base 14 type 1 font  */
    val SYMBOL = BaseFont.SYMBOL

    /** This is a possible value of a base 14 type 1 font  */
    val TIMES = "Times"

    /** This is a possible value of a base 14 type 1 font  */
    val TIMES_ROMAN = BaseFont.TIMES_ROMAN

    /** This is a possible value of a base 14 type 1 font  */
    val TIMES_BOLD = BaseFont.TIMES_BOLD

    /** This is a possible value of a base 14 type 1 font  */
    val TIMES_ITALIC = BaseFont.TIMES_ITALIC

    /** This is a possible value of a base 14 type 1 font  */
    val TIMES_BOLDITALIC = BaseFont.TIMES_BOLDITALIC

    /** This is a possible value of a base 14 type 1 font  */
    val ZAPFDINGBATS = BaseFont.ZAPFDINGBATS

    /**
     * Gets the font factory implementation.
     * @return the font factory implementation
     */
    /**
     * Sets the font factory implementation.
     * @param fontImp the font factory implementation
     */
    var fontImp: FontFactoryImp? = FontFactoryImp()
        set(fontImp) {
            if (fontImp == null)
                throw NullPointerException(MessageLocalization.getComposedMessage("fontfactoryimp.cannot.be.null"))
            FontFactory.fontImp = fontImp
        }

    /** This is the default encoding to use.  */
    var defaultEncoding = BaseFont.WINANSI

    /** This is the default value of the embedded variable.  */
    var defaultEmbedding = BaseFont.NOT_EMBEDDED

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    encoding    the encoding of the font
     * *
     * @param       embedded    true if the font is to be embedded in the PDF
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @param    color        the BaseColor of this font.
     * *
     * @return the Font constructed based on the parameters
     */

    @JvmOverloads fun getFont(fontname: String, encoding: String = defaultEncoding, embedded: Boolean = defaultEmbedding, size: Float = Font.UNDEFINED.toFloat(), style: Int = Font.UNDEFINED, color: BaseColor? = null): Font {
        return fontImp.getFont(fontname, encoding, embedded, size, style, color)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    encoding    the encoding of the font
     * *
     * @param       embedded    true if the font is to be embedded in the PDF
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @param    color        the BaseColor of this font.
     * *
     * @param    cached        true if the font comes from the cache or is added to
     * * 				the cache if new, false if the font is always created new
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, embedded: Boolean, size: Float, style: Int, color: BaseColor, cached: Boolean): Font {
        return fontImp.getFont(fontname, encoding, embedded, size, style, color, cached)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    encoding    the encoding of the font
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @param    color        the BaseColor of this font.
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, size: Float, style: Int, color: BaseColor): Font {
        return getFont(fontname, encoding, defaultEmbedding, size, style, color)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    encoding    the encoding of the font
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, size: Float, style: Int): Font {
        return getFont(fontname, encoding, defaultEmbedding, size, style, null)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    encoding    the encoding of the font
     * *
     * @param    size        the size of this font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, size: Float): Font {
        return getFont(fontname, encoding, defaultEmbedding, size, Font.UNDEFINED, null)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @param    color        the BaseColor of this font.
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, size: Float, style: Int, color: BaseColor): Font {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, color)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    size        the size of this font
     * *
     * @param    color        the BaseColor of this font.
     * *
     * @return the Font constructed based on the parameters
     * *
     * @since 2.1.0
     */

    fun getFont(fontname: String, size: Float, color: BaseColor): Font {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, color)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    size        the size of this font
     * *
     * @param    style        the style of this font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, size: Float, style: Int): Font {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, null)
    }

    /**
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @param    size        the size of this font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, size: Float): Font {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, null)
    }

    /**
     * Register a font by giving explicitly the font family and name.
     * @param familyName the font family
     * *
     * @param fullName the font name
     * *
     * @param path the font path
     */
    fun registerFamily(familyName: String, fullName: String, path: String) {
        fontImp.registerFamily(familyName, fullName, path)
    }

    /**
     * Register a font file and use an alias for the font contained in it.

     * @param   path    the path to a font file
     * *
     * @param   alias   the alias you want to use for the font
     */

    @JvmOverloads fun register(path: String, alias: String? = null) {
        fontImp.register(path, alias)
    }

    /** Register all the fonts in a directory.
     * @param dir the directory
     * *
     * @return the number of fonts registered
     */
    fun registerDirectory(dir: String): Int {
        return fontImp.registerDirectory(dir)
    }

    /**
     * Register all the fonts in a directory and possibly its subdirectories.
     * @param dir the directory
     * *
     * @param scanSubdirectories recursively scan subdirectories if `true
     * *
     * @return the number of fonts registered
     * *
     * @since 2.1.2
    ` */
    fun registerDirectory(dir: String, scanSubdirectories: Boolean): Int {
        return fontImp.registerDirectory(dir, scanSubdirectories)
    }

    /** Register fonts in some probable directories. It usually works in Windows,
     * Linux and Solaris.
     * @return the number of fonts registered
     */
    fun registerDirectories(): Int {
        return fontImp.registerDirectories()
    }

    /**
     * Gets a set of registered fontnames.
     * @return a set of registered fonts
     */

    val registeredFonts: Set<String>
        get() = fontImp.registeredFonts

    /**
     * Gets a set of registered fontnames.
     * @return a set of registered font families
     */

    val registeredFamilies: Set<String>
        get() = fontImp.registeredFamilies

    /**
     * Gets a set of registered fontnames.
     * @param fontname of a font that may or may not be registered
     * *
     * @return true if a given font is registered
     */

    operator fun contains(fontname: String): Boolean {
        return fontImp.isRegistered(fontname)
    }

    /**
     * Checks if a certain font is registered.

     * @param   fontname    the name of the font that has to be checked.
     * *
     * @return  true if the font is found
     */

    fun isRegistered(fontname: String): Boolean {
        return fontImp.isRegistered(fontname)
    }
}
/** Creates new FontFactory  */
/**
 * Constructs a Font-object.

 * @param    fontname    the name of the font
 * *
 * @param    encoding    the encoding of the font
 * *
 * @param       embedded    true if the font is to be embedded in the PDF
 * *
 * @param    size        the size of this font
 * *
 * @param    style        the style of this font
 * *
 * @return the Font constructed based on the parameters
 */
/**
 * Constructs a Font-object.

 * @param    fontname    the name of the font
 * *
 * @param    encoding    the encoding of the font
 * *
 * @param       embedded    true if the font is to be embedded in the PDF
 * *
 * @param    size        the size of this font
 * *
 * @return the Font constructed based on the parameters
 */
/**
 * Constructs a Font-object.

 * @param    fontname    the name of the font
 * *
 * @param    encoding    the encoding of the font
 * *
 * @param       embedded    true if the font is to be embedded in the PDF
 * *
 * @return the Font constructed based on the parameters
 */
/**
 * Constructs a Font-object.

 * @param    fontname    the name of the font
 * *
 * @param    encoding    the encoding of the font
 * *
 * @return the Font constructed based on the parameters
 */
/**
 * Constructs a Font-object.

 * @param    fontname    the name of the font
 * *
 * @return the Font constructed based on the parameters
 */
/**
 * Register a ttf- or a ttc-file.

 * @param   path    the path to a ttf- or ttc-file
 */
