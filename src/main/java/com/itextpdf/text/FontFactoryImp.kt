/*
 * $Id: 6de09f08fa21fe6e447ed7f5030838e848e84831 $
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

import com.itextpdf.text.Font.FontFamily
import com.itextpdf.text.log.Level
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.BaseFont

import java.io.File
import java.io.IOException
import java.util.*

/**
 * If you are using True Type fonts, you can declare the paths of the different ttf- and ttc-files
 * to this class first and then create fonts in your code using one of the getFont method
 * without having to enter a path as parameter.

 * @author  Bruno Lowagie
 */

class FontFactoryImp : FontProvider {
    /** This is a map of postscriptfontnames of True Type fonts and the path of their ttf- or ttc-file.  */
    private val trueTypeFonts = Hashtable<String, String>()

    /** This is a map of fontfamilies.  */
    private val fontFamilies = Hashtable<String, ArrayList<String>>()

    /** This is the default encoding to use.  */
    var defaultEncoding = BaseFont.WINANSI

    /** This is the default value of the embedded variable.  */
    var defaultEmbedding = BaseFont.NOT_EMBEDDED

    init {
        trueTypeFonts.put(FontFactory.COURIER.toLowerCase(), FontFactory.COURIER)
        trueTypeFonts.put(FontFactory.COURIER_BOLD.toLowerCase(), FontFactory.COURIER_BOLD)
        trueTypeFonts.put(FontFactory.COURIER_OBLIQUE.toLowerCase(), FontFactory.COURIER_OBLIQUE)
        trueTypeFonts.put(FontFactory.COURIER_BOLDOBLIQUE.toLowerCase(), FontFactory.COURIER_BOLDOBLIQUE)
        trueTypeFonts.put(FontFactory.HELVETICA.toLowerCase(), FontFactory.HELVETICA)
        trueTypeFonts.put(FontFactory.HELVETICA_BOLD.toLowerCase(), FontFactory.HELVETICA_BOLD)
        trueTypeFonts.put(FontFactory.HELVETICA_OBLIQUE.toLowerCase(), FontFactory.HELVETICA_OBLIQUE)
        trueTypeFonts.put(FontFactory.HELVETICA_BOLDOBLIQUE.toLowerCase(), FontFactory.HELVETICA_BOLDOBLIQUE)
        trueTypeFonts.put(FontFactory.SYMBOL.toLowerCase(), FontFactory.SYMBOL)
        trueTypeFonts.put(FontFactory.TIMES_ROMAN.toLowerCase(), FontFactory.TIMES_ROMAN)
        trueTypeFonts.put(FontFactory.TIMES_BOLD.toLowerCase(), FontFactory.TIMES_BOLD)
        trueTypeFonts.put(FontFactory.TIMES_ITALIC.toLowerCase(), FontFactory.TIMES_ITALIC)
        trueTypeFonts.put(FontFactory.TIMES_BOLDITALIC.toLowerCase(), FontFactory.TIMES_BOLDITALIC)
        trueTypeFonts.put(FontFactory.ZAPFDINGBATS.toLowerCase(), FontFactory.ZAPFDINGBATS)

        var tmp: ArrayList<String>
        tmp = ArrayList<String>()
        tmp.add(FontFactory.COURIER)
        tmp.add(FontFactory.COURIER_BOLD)
        tmp.add(FontFactory.COURIER_OBLIQUE)
        tmp.add(FontFactory.COURIER_BOLDOBLIQUE)
        fontFamilies.put(FontFactory.COURIER.toLowerCase(), tmp)
        tmp = ArrayList<String>()
        tmp.add(FontFactory.HELVETICA)
        tmp.add(FontFactory.HELVETICA_BOLD)
        tmp.add(FontFactory.HELVETICA_OBLIQUE)
        tmp.add(FontFactory.HELVETICA_BOLDOBLIQUE)
        fontFamilies.put(FontFactory.HELVETICA.toLowerCase(), tmp)
        tmp = ArrayList<String>()
        tmp.add(FontFactory.SYMBOL)
        fontFamilies.put(FontFactory.SYMBOL.toLowerCase(), tmp)
        tmp = ArrayList<String>()
        tmp.add(FontFactory.TIMES_ROMAN)
        tmp.add(FontFactory.TIMES_BOLD)
        tmp.add(FontFactory.TIMES_ITALIC)
        tmp.add(FontFactory.TIMES_BOLDITALIC)
        fontFamilies.put(FontFactory.TIMES.toLowerCase(), tmp)
        fontFamilies.put(FontFactory.TIMES_ROMAN.toLowerCase(), tmp)
        tmp = ArrayList<String>()
        tmp.add(FontFactory.ZAPFDINGBATS)
        fontFamilies.put(FontFactory.ZAPFDINGBATS.toLowerCase(), tmp)
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
     * @return the Font constructed based on the parameters
     */
    override fun getFont(fontname: String, encoding: String, embedded: Boolean, size: Float, style: Int, color: BaseColor?): Font {
        return getFont(fontname, encoding, embedded, size, style, color, true)
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
    fun getFont(fontname: String?, encoding: String, embedded: Boolean, size: Float, style: Int, color: BaseColor, cached: Boolean): Font {
        var fontname = fontname
        var style = style
        if (fontname == null) return Font(FontFamily.UNDEFINED, size, style, color)
        val lowercasefontname = fontname.toLowerCase()
        val tmp = fontFamilies[lowercasefontname]
        if (tmp != null) {
            synchronized (tmp) {
                // some bugs were fixed here by Daniel Marczisovszky
                val s = if (style == Font.UNDEFINED) Font.NORMAL else style
                var fs = Font.NORMAL
                var found = false
                for (f in tmp) {
                    val lcf = f.toLowerCase()
                    fs = Font.NORMAL
                    if (lcf.indexOf("bold") != -1) fs = fs or Font.BOLD
                    if (lcf.indexOf("italic") != -1 || lcf.indexOf("oblique") != -1) fs = fs or Font.ITALIC
                    if (s and Font.BOLDITALIC == fs) {
                        fontname = f
                        found = true
                        break
                    }
                }
                if (style != Font.UNDEFINED && found) {
                    style = style and fs.inv()
                }
            }
        }
        var basefont: BaseFont? = null
        try {
            basefont = getBaseFont(fontname, encoding, embedded, cached)
            if (basefont == null) {
                // the font is not registered as truetype font
                return Font(FontFamily.UNDEFINED, size, style, color)
            }
        } catch (de: DocumentException) {
            // this shouldn't happen
            throw ExceptionConverter(de)
        } catch (ioe: IOException) {
            // the font is registered as a true type font, but the path was wrong
            return Font(FontFamily.UNDEFINED, size, style, color)
        } catch (npe: NullPointerException) {
            // null was entered as fontname and/or encoding
            return Font(FontFamily.UNDEFINED, size, style, color)
        }

        return Font(basefont, size, style, color)
    }

    @Throws(IOException::class, DocumentException::class)
    protected fun getBaseFont(fontname: String?, encoding: String, embedded: Boolean, cached: Boolean): BaseFont {
        var fontname = fontname
        var basefont: BaseFont? = null
        try {
            // the font is a type 1 font or CJK font
            basefont = BaseFont.createFont(fontname, encoding, embedded, cached, null, null, true)
        } catch (de: DocumentException) {
        }

        if (basefont == null) {
            // the font is a true type font or an unknown font
            fontname = trueTypeFonts[fontname!!.toLowerCase()]
            // the font is not registered as truetype font
            if (fontname != null)
                basefont = BaseFont.createFont(fontname, encoding, embedded, cached, null, null)
        }

        return basefont
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
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, embedded: Boolean, size: Float, style: Int): Font {
        return getFont(fontname, encoding, embedded, size, style, null)
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
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String, embedded: Boolean, size: Float): Font {
        return getFont(fontname, encoding, embedded, size, Font.UNDEFINED, null)
    }

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

    fun getFont(fontname: String, encoding: String, embedded: Boolean): Font {
        return getFont(fontname, encoding, embedded, Font.UNDEFINED.toFloat(), Font.UNDEFINED, null)
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
     * @param    encoding    the encoding of the font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String, encoding: String): Font {
        return getFont(fontname, encoding, defaultEmbedding, Font.UNDEFINED.toFloat(), Font.UNDEFINED, null)
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
     * Constructs a Font-object.

     * @param    fontname    the name of the font
     * *
     * @return the Font constructed based on the parameters
     */

    fun getFont(fontname: String): Font {
        return getFont(fontname, defaultEncoding, defaultEmbedding, Font.UNDEFINED.toFloat(), Font.UNDEFINED, null)
    }

    /**
     * Register a font by giving explicitly the font family and name.
     * @param familyName the font family
     * *
     * @param fullName the font name
     * *
     * @param path the font path
     */
    fun registerFamily(familyName: String, fullName: String, path: String?) {
        if (path != null)
            trueTypeFonts.put(fullName, path)
        var tmp: ArrayList<String>?
        synchronized (fontFamilies) {
            tmp = fontFamilies[familyName]
            if (tmp == null) {
                tmp = ArrayList<String>()
                fontFamilies.put(familyName, tmp)
            }
        }
        synchronized (tmp) {
            if (!tmp!!.contains(fullName)) {
                val fullNameLength = fullName.length
                var inserted = false
                for (j in tmp.indices) {
                    if (tmp[j].length >= fullNameLength) {
                        tmp.add(j, fullName)
                        inserted = true
                        break
                    }
                }
                if (!inserted) {
                    tmp.add(fullName)
                    var newFullName = fullName.toLowerCase()
                    if (newFullName.endsWith("regular")) {
                        //remove "regular" at the end of the font name
                        newFullName = newFullName.substring(0, newFullName.length - 7).trim { it <= ' ' }
                        //insert this font name at the first position for higher priority
                        tmp.add(0, fullName.substring(0, newFullName.length))
                    }
                }
            }
        }
    }

    /**
     * Register a font file and use an alias for the font contained in it.

     * @param   path    the path to a font file
     * *
     * @param   alias   the alias you want to use for the font
     */

    @JvmOverloads fun register(path: String, alias: String? = null) {
        try {
            if (path.toLowerCase().endsWith(".ttf") || path.toLowerCase().endsWith(".otf") || path.toLowerCase().indexOf(".ttc,") > 0) {
                val allNames = BaseFont.getAllFontNames(path, BaseFont.WINANSI, null)
                trueTypeFonts.put((allNames[0] as String).toLowerCase(), path)
                if (alias != null) {
                    val lcAlias = alias.toLowerCase()
                    trueTypeFonts.put(lcAlias, path)
                    if (lcAlias.endsWith("regular")) {
                        //do this job to give higher priority to regular fonts in comparison with light, narrow, etc
                        saveCopyOfRegularFont(lcAlias, path)
                    }
                }
                // register all the font names with all the locales
                var names = allNames[2] as Array<Array<String>> //full name
                for (name in names) {
                    val lcName = name[3].toLowerCase()
                    trueTypeFonts.put(lcName, path)
                    if (lcName.endsWith("regular")) {
                        //do this job to give higher priority to regular fonts in comparison with light, narrow, etc
                        saveCopyOfRegularFont(lcName, path)
                    }
                }
                var fullName: String? = null
                var familyName: String? = null
                names = allNames[1] as Array<Array<String>> //family name
                run {
                    var k = 0
                    while (k < TTFamilyOrder.size) {
                        for (name in names) {
                            if (TTFamilyOrder[k] == name[0] && TTFamilyOrder[k + 1] == name[1] && TTFamilyOrder[k + 2] == name[2]) {
                                familyName = name[3].toLowerCase()
                                k = TTFamilyOrder.size
                                break
                            }
                        }
                        k += 3
                    }
                }
                if (familyName != null) {
                    var lastName = ""
                    names = allNames[2] as Array<Array<String>> //full name
                    for (name in names) {
                        var k = 0
                        while (k < TTFamilyOrder.size) {
                            if (TTFamilyOrder[k] == name[0] && TTFamilyOrder[k + 1] == name[1] && TTFamilyOrder[k + 2] == name[2]) {
                                fullName = name[3]
                                if (fullName == lastName) {
                                    k += 3
                                    continue
                                }
                                lastName = fullName
                                registerFamily(familyName, fullName, null)
                                break
                            }
                            k += 3
                        }
                    }
                }
            } else if (path.toLowerCase().endsWith(".ttc")) {
                if (alias != null)
                    LOGGER.error("You can't define an alias for a true type collection.")
                val names = BaseFont.enumerateTTCNames(path)
                for (i in names.indices) {
                    register(path + "," + i)
                }
            } else if (path.toLowerCase().endsWith(".afm") || path.toLowerCase().endsWith(".pfm")) {
                val bf = BaseFont.createFont(path, BaseFont.CP1252, false)
                val fullName = bf.fullFontName[0][3].toLowerCase()
                val familyName = bf.familyFontName[0][3].toLowerCase()
                val psName = bf.postscriptFontName.toLowerCase()
                registerFamily(familyName, fullName, null)
                trueTypeFonts.put(psName, path)
                trueTypeFonts.put(fullName, path)
            }
            if (LOGGER.isLogging(Level.TRACE)) {
                LOGGER.trace(String.format("Registered %s", path))
            }
        } catch (de: DocumentException) {
            // this shouldn't happen
            throw ExceptionConverter(de)
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

    }

    // remove regular and correct last symbol
    // do this job to give higher priority to regular fonts in comparison with light, narrow, etc
    // Don't use this method for not regular fonts!
    protected fun saveCopyOfRegularFont(regularFontName: String, path: String): Boolean {
        //remove "regular" at the end of the font name
        val alias = regularFontName.substring(0, regularFontName.length - 7).trim { it <= ' ' }
        if (!trueTypeFonts.containsKey(alias)) {
            trueTypeFonts.put(alias, path)
            return true
        }
        return false
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
    @JvmOverloads fun registerDirectory(dir: String, scanSubdirectories: Boolean = false): Int {
        if (LOGGER.isLogging(Level.DEBUG)) {
            LOGGER.debug(String.format("Registering directory %s, looking for fonts", dir))
        }
        var count = 0
        try {
            var file = File(dir)
            if (!file.exists() || !file.isDirectory)
                return 0
            val files = file.list() ?: return 0
            for (k in files.indices) {
                try {
                    file = File(dir, files[k])
                    if (file.isDirectory) {
                        if (scanSubdirectories) {
                            count += registerDirectory(file.absolutePath, true)
                        }
                    } else {
                        val name = file.path
                        val suffix = if (name.length < 4) null else name.substring(name.length - 4).toLowerCase()
                        if (".afm" == suffix || ".pfm" == suffix) {
                            /* Only register Type 1 fonts with matching .pfb files */
                            val pfb = File(name.substring(0, name.length - 4) + ".pfb")
                            if (pfb.exists()) {
                                register(name, null)
                                ++count
                            }
                        } else if (".ttf" == suffix || ".otf" == suffix || ".ttc" == suffix) {
                            register(name, null)
                            ++count
                        }
                    }
                } catch (e: Exception) {
                    //empty on purpose
                }

            }
        } catch (e: Exception) {
            //empty on purpose
        }

        return count
    }

    /** Register fonts in some probable directories. It usually works in Windows,
     * Linux and Solaris.
     * @return the number of fonts registered
     */
    fun registerDirectories(): Int {
        var count = 0
        val windir = System.getenv("windir")
        val fileseparator = System.getProperty("file.separator")
        if (windir != null && fileseparator != null) {
            count += registerDirectory(windir + fileseparator + "fonts")
        }
        count += registerDirectory("/usr/share/X11/fonts", true)
        count += registerDirectory("/usr/X/lib/X11/fonts", true)
        count += registerDirectory("/usr/openwin/lib/X11/fonts", true)
        count += registerDirectory("/usr/share/fonts", true)
        count += registerDirectory("/usr/X11R6/lib/X11/fonts", true)
        count += registerDirectory("/Library/Fonts")
        count += registerDirectory("/System/Library/Fonts")
        return count
    }

    /**
     * Gets a set of registered fontnames.
     * @return a set of registered fonts
     */

    val registeredFonts: Set<String>
        get() = trueTypeFonts.keys

    /**
     * Gets a set of registered fontnames.
     * @return a set of registered font families
     */

    val registeredFamilies: Set<String>
        get() = fontFamilies.keys

    /**
     * Checks if a certain font is registered.

     * @param   fontname    the name of the font that has to be checked.
     * *
     * @return  true if the font is found
     */
    override fun isRegistered(fontname: String): Boolean {
        return trueTypeFonts.containsKey(fontname.toLowerCase())
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(FontFactoryImp::class.java)

        private val TTFamilyOrder = arrayOf("3", "1", "1033", "3", "0", "1033", "1", "0", "0", "0", "3", "0")
    }
}
/** Creates new FontFactory  */
/**
 * Register a ttf- or a ttc-file.

 * @param   path    the path to a ttf- or ttc-file
 */
/** Register all the fonts in a directory.
 * @param dir the directory
 * *
 * @return the number of fonts registered
 */
