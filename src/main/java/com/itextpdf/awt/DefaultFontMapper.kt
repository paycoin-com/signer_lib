/*
 * $Id: 8ca97bc7e2b7832601737c8d0980a8cf6ad0273e $
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

import java.awt.Font
import java.io.File
import java.util.HashMap

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.pdf.BaseFont

/** Default class to map awt fonts to BaseFont.
 * @author Paulo Soares
 */

open class DefaultFontMapper : FontMapper {

    /** A representation of BaseFont parameters.
     */
    class BaseFontParameters
    /** Constructs default BaseFont parameters.
     * @param fontName the font name or location
     */
    (
            /** The font name.
             */
            var fontName: String) {
        /** The encoding for that font.
         */
        var encoding: String
        /** The embedding for that font.
         */
        var embedded: Boolean = false
        /** Whether the font is cached of not.
         */
        var cached: Boolean = false
        /** The font bytes for ttf and afm.
         */
        var ttfAfm: ByteArray
        /** The font bytes for pfb.
         */
        var pfb: ByteArray

        init {
            encoding = BaseFont.CP1252
            embedded = BaseFont.EMBEDDED
            cached = BaseFont.CACHED
        }
    }

    /** Maps aliases to names.
     */
    val aliases = HashMap<String, String>()
    /** Maps names to BaseFont parameters.
     */
    val mapper = HashMap<String, BaseFontParameters>()

    /**
     * Returns a BaseFont which can be used to represent the given AWT Font

     * @param    font        the font to be converted
     * *
     * @return    a BaseFont which has similar properties to the provided Font
     */

    override fun awtToPdf(font: Font): BaseFont {
        try {
            val p = getBaseFontParameters(font.fontName)
            if (p != null)
                return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, p.ttfAfm, p.pfb)
            var fontKey: String? = null
            val logicalName = font.name

            if (logicalName.equals("DialogInput", ignoreCase = true) || logicalName.equals("Monospaced", ignoreCase = true) || logicalName.equals("Courier", ignoreCase = true)) {

                if (font.isItalic) {
                    if (font.isBold) {
                        fontKey = BaseFont.COURIER_BOLDOBLIQUE

                    } else {
                        fontKey = BaseFont.COURIER_OBLIQUE
                    }

                } else {
                    if (font.isBold) {
                        fontKey = BaseFont.COURIER_BOLD

                    } else {
                        fontKey = BaseFont.COURIER
                    }
                }

            } else if (logicalName.equals("Serif", ignoreCase = true) || logicalName.equals("TimesRoman", ignoreCase = true)) {

                if (font.isItalic) {
                    if (font.isBold) {
                        fontKey = BaseFont.TIMES_BOLDITALIC

                    } else {
                        fontKey = BaseFont.TIMES_ITALIC
                    }

                } else {
                    if (font.isBold) {
                        fontKey = BaseFont.TIMES_BOLD

                    } else {
                        fontKey = BaseFont.TIMES_ROMAN
                    }
                }

            } else {
                // default, this catches Dialog and SansSerif

                if (font.isItalic) {
                    if (font.isBold) {
                        fontKey = BaseFont.HELVETICA_BOLDOBLIQUE

                    } else {
                        fontKey = BaseFont.HELVETICA_OBLIQUE
                    }

                } else {
                    if (font.isBold) {
                        fontKey = BaseFont.HELVETICA_BOLD
                    } else {
                        fontKey = BaseFont.HELVETICA
                    }
                }
            }
            return BaseFont.createFont(fontKey, BaseFont.CP1252, false)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Returns an AWT Font which can be used to represent the given BaseFont

     * @param    font        the font to be converted
     * *
     * @param    size        the desired point size of the resulting font
     * *
     * @return    a Font which has similar properties to the provided BaseFont
     */

    override fun pdfToAwt(font: BaseFont, size: Int): Font {
        val names = font.fullFontName
        if (names.size == 1)
            return Font(names[0][3], 0, size)
        var name10: String? = null
        var name3x: String? = null
        for (k in names.indices) {
            val name = names[k]
            if (name[0] == "1" && name[1] == "0")
                name10 = name[3]
            else if (name[2] == "1033") {
                name3x = name[3]
                break
            }
        }
        var finalName = name3x
        if (finalName == null)
            finalName = name10
        if (finalName == null)
            finalName = names[0][3]
        return Font(finalName, 0, size)
    }

    /** Maps a name to a BaseFont parameter.
     * @param awtName the name
     * *
     * @param parameters the BaseFont parameter
     */
    fun putName(awtName: String, parameters: BaseFontParameters) {
        mapper.put(awtName, parameters)
    }

    /** Maps an alias to a name.
     * @param alias the alias
     * *
     * @param awtName the name
     */
    fun putAlias(alias: String, awtName: String) {
        aliases.put(alias, awtName)
    }

    /** Looks for a BaseFont parameter associated with a name.
     * @param name the name
     * *
     * @return the BaseFont parameter or null if not found.
     */
    fun getBaseFontParameters(name: String): BaseFontParameters? {
        val alias = aliases[name] ?: return mapper[name]
        val p = mapper[alias]
        if (p == null)
            return mapper[name]
        else
            return p
    }

    /**
     * Inserts the names in this map.
     * @param allNames the returned value of calling [BaseFont.getAllFontNames]
     * *
     * @param path the full path to the font
     */
    fun insertNames(allNames: Array<Any>, path: String) {
        val names = allNames[2] as Array<Array<String>>
        var main: String? = null
        for (k in names.indices) {
            val name = names[k]
            if (name[2] == "1033") {
                main = name[3]
                break
            }
        }
        if (main == null)
            main = names[0][3]
        val p = BaseFontParameters(path)
        mapper.put(main, p)
        for (k in names.indices) {
            aliases.put(names[k][3], main)
        }
        aliases.put(allNames[0] as String, main)
    }

    /** Inserts one font file into the map. The encoding
     * will be BaseFont.CP1252 but can be
     * changed later.
     * @param file the file to insert
     * *
     * @return the number of files inserted
     * *
     * @since 5.0.5
     */
    fun insertFile(file: File): Int {
        val name = file.path.toLowerCase()
        try {
            if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".afm")) {
                val allNames = BaseFont.getAllFontNames(file.path, BaseFont.CP1252, null)
                insertNames(allNames, file.path)
                return 1
            } else if (name.endsWith(".ttc")) {
                val ttcs = BaseFont.enumerateTTCNames(file.path)
                for (j in ttcs.indices) {
                    val nt = file.path + "," + j
                    val allNames = BaseFont.getAllFontNames(nt, BaseFont.CP1252, null)
                    insertNames(allNames, nt)
                }
                return 1
            }
        } catch (e: Exception) {
        }

        return 0
    }

    /** Inserts all the fonts recognized by iText in the
     * directory into the map. The encoding
     * will be BaseFont.CP1252 but can be
     * changed later.
     * @param dir the directory to scan
     * *
     * @return the number of files processed
     */
    fun insertDirectory(dir: String): Int {
        val file = File(dir)
        if (!file.exists() || !file.isDirectory)
            return 0
        val files = file.listFiles() ?: return 0
        var count = 0
        for (k in files.indices) {
            count += insertFile(files[k])
        }
        return count
    }
}
