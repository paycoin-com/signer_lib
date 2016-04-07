/*
 * $Id: 45c7b9072f4c50bb481302e1cab2d59507d55e07 $
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
package com.itextpdf.text.xml.simpleparser

import java.util.HashMap

import com.itextpdf.text.Chunk
import com.itextpdf.text.Font
import com.itextpdf.text.Font.FontFamily

/**
 * This class contains entities that can be used in an entity tag.
 */

object EntitiesToSymbol {

    /**
     * This is a map that contains all possible id values of the entity tag
     * that can be translated to a character in font Symbol.
     */
    private val MAP: MutableMap<String, Char>

    init {
        MAP = HashMap<String, Char>()
        MAP.put("169", Character.valueOf(227.toChar()))
        MAP.put("172", Character.valueOf(216.toChar()))
        MAP.put("174", Character.valueOf(210.toChar()))
        MAP.put("177", Character.valueOf(177.toChar()))
        MAP.put("215", Character.valueOf(180.toChar()))
        MAP.put("247", Character.valueOf(184.toChar()))
        MAP.put("8230", Character.valueOf(188.toChar()))
        MAP.put("8242", Character.valueOf(162.toChar()))
        MAP.put("8243", Character.valueOf(178.toChar()))
        MAP.put("8260", Character.valueOf(164.toChar()))
        MAP.put("8364", Character.valueOf(240.toChar()))
        MAP.put("8465", Character.valueOf(193.toChar()))
        MAP.put("8472", Character.valueOf(195.toChar()))
        MAP.put("8476", Character.valueOf(194.toChar()))
        MAP.put("8482", Character.valueOf(212.toChar()))
        MAP.put("8501", Character.valueOf(192.toChar()))
        MAP.put("8592", Character.valueOf(172.toChar()))
        MAP.put("8593", Character.valueOf(173.toChar()))
        MAP.put("8594", Character.valueOf(174.toChar()))
        MAP.put("8595", Character.valueOf(175.toChar()))
        MAP.put("8596", Character.valueOf(171.toChar()))
        MAP.put("8629", Character.valueOf(191.toChar()))
        MAP.put("8656", Character.valueOf(220.toChar()))
        MAP.put("8657", Character.valueOf(221.toChar()))
        MAP.put("8658", Character.valueOf(222.toChar()))
        MAP.put("8659", Character.valueOf(223.toChar()))
        MAP.put("8660", Character.valueOf(219.toChar()))
        MAP.put("8704", Character.valueOf(34.toChar()))
        MAP.put("8706", Character.valueOf(182.toChar()))
        MAP.put("8707", Character.valueOf(36.toChar()))
        MAP.put("8709", Character.valueOf(198.toChar()))
        MAP.put("8711", Character.valueOf(209.toChar()))
        MAP.put("8712", Character.valueOf(206.toChar()))
        MAP.put("8713", Character.valueOf(207.toChar()))
        MAP.put("8717", Character.valueOf(39.toChar()))
        MAP.put("8719", Character.valueOf(213.toChar()))
        MAP.put("8721", Character.valueOf(229.toChar()))
        MAP.put("8722", Character.valueOf(45.toChar()))
        MAP.put("8727", Character.valueOf(42.toChar()))
        MAP.put("8729", Character.valueOf(183.toChar()))
        MAP.put("8730", Character.valueOf(214.toChar()))
        MAP.put("8733", Character.valueOf(181.toChar()))
        MAP.put("8734", Character.valueOf(165.toChar()))
        MAP.put("8736", Character.valueOf(208.toChar()))
        MAP.put("8743", Character.valueOf(217.toChar()))
        MAP.put("8744", Character.valueOf(218.toChar()))
        MAP.put("8745", Character.valueOf(199.toChar()))
        MAP.put("8746", Character.valueOf(200.toChar()))
        MAP.put("8747", Character.valueOf(242.toChar()))
        MAP.put("8756", Character.valueOf(92.toChar()))
        MAP.put("8764", Character.valueOf(126.toChar()))
        MAP.put("8773", Character.valueOf(64.toChar()))
        MAP.put("8776", Character.valueOf(187.toChar()))
        MAP.put("8800", Character.valueOf(185.toChar()))
        MAP.put("8801", Character.valueOf(186.toChar()))
        MAP.put("8804", Character.valueOf(163.toChar()))
        MAP.put("8805", Character.valueOf(179.toChar()))
        MAP.put("8834", Character.valueOf(204.toChar()))
        MAP.put("8835", Character.valueOf(201.toChar()))
        MAP.put("8836", Character.valueOf(203.toChar()))
        MAP.put("8838", Character.valueOf(205.toChar()))
        MAP.put("8839", Character.valueOf(202.toChar()))
        MAP.put("8853", Character.valueOf(197.toChar()))
        MAP.put("8855", Character.valueOf(196.toChar()))
        MAP.put("8869", Character.valueOf(94.toChar()))
        MAP.put("8901", Character.valueOf(215.toChar()))
        MAP.put("8992", Character.valueOf(243.toChar()))
        MAP.put("8993", Character.valueOf(245.toChar()))
        MAP.put("9001", Character.valueOf(225.toChar()))
        MAP.put("9002", Character.valueOf(241.toChar()))
        MAP.put("913", Character.valueOf(65.toChar()))
        MAP.put("914", Character.valueOf(66.toChar()))
        MAP.put("915", Character.valueOf(71.toChar()))
        MAP.put("916", Character.valueOf(68.toChar()))
        MAP.put("917", Character.valueOf(69.toChar()))
        MAP.put("918", Character.valueOf(90.toChar()))
        MAP.put("919", Character.valueOf(72.toChar()))
        MAP.put("920", Character.valueOf(81.toChar()))
        MAP.put("921", Character.valueOf(73.toChar()))
        MAP.put("922", Character.valueOf(75.toChar()))
        MAP.put("923", Character.valueOf(76.toChar()))
        MAP.put("924", Character.valueOf(77.toChar()))
        MAP.put("925", Character.valueOf(78.toChar()))
        MAP.put("926", Character.valueOf(88.toChar()))
        MAP.put("927", Character.valueOf(79.toChar()))
        MAP.put("928", Character.valueOf(80.toChar()))
        MAP.put("929", Character.valueOf(82.toChar()))
        MAP.put("931", Character.valueOf(83.toChar()))
        MAP.put("932", Character.valueOf(84.toChar()))
        MAP.put("933", Character.valueOf(85.toChar()))
        MAP.put("934", Character.valueOf(70.toChar()))
        MAP.put("935", Character.valueOf(67.toChar()))
        MAP.put("936", Character.valueOf(89.toChar()))
        MAP.put("937", Character.valueOf(87.toChar()))
        MAP.put("945", Character.valueOf(97.toChar()))
        MAP.put("946", Character.valueOf(98.toChar()))
        MAP.put("947", Character.valueOf(103.toChar()))
        MAP.put("948", Character.valueOf(100.toChar()))
        MAP.put("949", Character.valueOf(101.toChar()))
        MAP.put("950", Character.valueOf(122.toChar()))
        MAP.put("951", Character.valueOf(104.toChar()))
        MAP.put("952", Character.valueOf(113.toChar()))
        MAP.put("953", Character.valueOf(105.toChar()))
        MAP.put("954", Character.valueOf(107.toChar()))
        MAP.put("955", Character.valueOf(108.toChar()))
        MAP.put("956", Character.valueOf(109.toChar()))
        MAP.put("957", Character.valueOf(110.toChar()))
        MAP.put("958", Character.valueOf(120.toChar()))
        MAP.put("959", Character.valueOf(111.toChar()))
        MAP.put("960", Character.valueOf(112.toChar()))
        MAP.put("961", Character.valueOf(114.toChar()))
        MAP.put("962", Character.valueOf(86.toChar()))
        MAP.put("963", Character.valueOf(115.toChar()))
        MAP.put("964", Character.valueOf(116.toChar()))
        MAP.put("965", Character.valueOf(117.toChar()))
        MAP.put("966", Character.valueOf(102.toChar()))
        MAP.put("967", Character.valueOf(99.toChar()))
        MAP.put("9674", Character.valueOf(224.toChar()))
        MAP.put("968", Character.valueOf(121.toChar()))
        MAP.put("969", Character.valueOf(119.toChar()))
        MAP.put("977", Character.valueOf(74.toChar()))
        MAP.put("978", Character.valueOf(161.toChar()))
        MAP.put("981", Character.valueOf(106.toChar()))
        MAP.put("982", Character.valueOf(118.toChar()))
        MAP.put("9824", Character.valueOf(170.toChar()))
        MAP.put("9827", Character.valueOf(167.toChar()))
        MAP.put("9829", Character.valueOf(169.toChar()))
        MAP.put("9830", Character.valueOf(168.toChar()))
        MAP.put("Alpha", Character.valueOf(65.toChar()))
        MAP.put("Beta", Character.valueOf(66.toChar()))
        MAP.put("Chi", Character.valueOf(67.toChar()))
        MAP.put("Delta", Character.valueOf(68.toChar()))
        MAP.put("Epsilon", Character.valueOf(69.toChar()))
        MAP.put("Eta", Character.valueOf(72.toChar()))
        MAP.put("Gamma", Character.valueOf(71.toChar()))
        MAP.put("Iota", Character.valueOf(73.toChar()))
        MAP.put("Kappa", Character.valueOf(75.toChar()))
        MAP.put("Lambda", Character.valueOf(76.toChar()))
        MAP.put("Mu", Character.valueOf(77.toChar()))
        MAP.put("Nu", Character.valueOf(78.toChar()))
        MAP.put("Omega", Character.valueOf(87.toChar()))
        MAP.put("Omicron", Character.valueOf(79.toChar()))
        MAP.put("Phi", Character.valueOf(70.toChar()))
        MAP.put("Pi", Character.valueOf(80.toChar()))
        MAP.put("Prime", Character.valueOf(178.toChar()))
        MAP.put("Psi", Character.valueOf(89.toChar()))
        MAP.put("Rho", Character.valueOf(82.toChar()))
        MAP.put("Sigma", Character.valueOf(83.toChar()))
        MAP.put("Tau", Character.valueOf(84.toChar()))
        MAP.put("Theta", Character.valueOf(81.toChar()))
        MAP.put("Upsilon", Character.valueOf(85.toChar()))
        MAP.put("Xi", Character.valueOf(88.toChar()))
        MAP.put("Zeta", Character.valueOf(90.toChar()))
        MAP.put("alefsym", Character.valueOf(192.toChar()))
        MAP.put("alpha", Character.valueOf(97.toChar()))
        MAP.put("and", Character.valueOf(217.toChar()))
        MAP.put("ang", Character.valueOf(208.toChar()))
        MAP.put("asymp", Character.valueOf(187.toChar()))
        MAP.put("beta", Character.valueOf(98.toChar()))
        MAP.put("cap", Character.valueOf(199.toChar()))
        MAP.put("chi", Character.valueOf(99.toChar()))
        MAP.put("clubs", Character.valueOf(167.toChar()))
        MAP.put("cong", Character.valueOf(64.toChar()))
        MAP.put("copy", Character.valueOf(211.toChar()))
        MAP.put("crarr", Character.valueOf(191.toChar()))
        MAP.put("cup", Character.valueOf(200.toChar()))
        MAP.put("dArr", Character.valueOf(223.toChar()))
        MAP.put("darr", Character.valueOf(175.toChar()))
        MAP.put("delta", Character.valueOf(100.toChar()))
        MAP.put("diams", Character.valueOf(168.toChar()))
        MAP.put("divide", Character.valueOf(184.toChar()))
        MAP.put("empty", Character.valueOf(198.toChar()))
        MAP.put("epsilon", Character.valueOf(101.toChar()))
        MAP.put("equiv", Character.valueOf(186.toChar()))
        MAP.put("eta", Character.valueOf(104.toChar()))
        MAP.put("euro", Character.valueOf(240.toChar()))
        MAP.put("exist", Character.valueOf(36.toChar()))
        MAP.put("forall", Character.valueOf(34.toChar()))
        MAP.put("frasl", Character.valueOf(164.toChar()))
        MAP.put("gamma", Character.valueOf(103.toChar()))
        MAP.put("ge", Character.valueOf(179.toChar()))
        MAP.put("hArr", Character.valueOf(219.toChar()))
        MAP.put("harr", Character.valueOf(171.toChar()))
        MAP.put("hearts", Character.valueOf(169.toChar()))
        MAP.put("hellip", Character.valueOf(188.toChar()))
        MAP.put("horizontal arrow extender", Character.valueOf(190.toChar()))
        MAP.put("image", Character.valueOf(193.toChar()))
        MAP.put("infin", Character.valueOf(165.toChar()))
        MAP.put("int", Character.valueOf(242.toChar()))
        MAP.put("iota", Character.valueOf(105.toChar()))
        MAP.put("isin", Character.valueOf(206.toChar()))
        MAP.put("kappa", Character.valueOf(107.toChar()))
        MAP.put("lArr", Character.valueOf(220.toChar()))
        MAP.put("lambda", Character.valueOf(108.toChar()))
        MAP.put("lang", Character.valueOf(225.toChar()))
        MAP.put("large brace extender", Character.valueOf(239.toChar()))
        MAP.put("large integral extender", Character.valueOf(244.toChar()))
        MAP.put("large left brace (bottom)", Character.valueOf(238.toChar()))
        MAP.put("large left brace (middle)", Character.valueOf(237.toChar()))
        MAP.put("large left brace (top)", Character.valueOf(236.toChar()))
        MAP.put("large left bracket (bottom)", Character.valueOf(235.toChar()))
        MAP.put("large left bracket (extender)", Character.valueOf(234.toChar()))
        MAP.put("large left bracket (top)", Character.valueOf(233.toChar()))
        MAP.put("large left parenthesis (bottom)", Character.valueOf(232.toChar()))
        MAP.put("large left parenthesis (extender)", Character.valueOf(231.toChar()))
        MAP.put("large left parenthesis (top)", Character.valueOf(230.toChar()))
        MAP.put("large right brace (bottom)", Character.valueOf(254.toChar()))
        MAP.put("large right brace (middle)", Character.valueOf(253.toChar()))
        MAP.put("large right brace (top)", Character.valueOf(252.toChar()))
        MAP.put("large right bracket (bottom)", Character.valueOf(251.toChar()))
        MAP.put("large right bracket (extender)", Character.valueOf(250.toChar()))
        MAP.put("large right bracket (top)", Character.valueOf(249.toChar()))
        MAP.put("large right parenthesis (bottom)", Character.valueOf(248.toChar()))
        MAP.put("large right parenthesis (extender)", Character.valueOf(247.toChar()))
        MAP.put("large right parenthesis (top)", Character.valueOf(246.toChar()))
        MAP.put("larr", Character.valueOf(172.toChar()))
        MAP.put("le", Character.valueOf(163.toChar()))
        MAP.put("lowast", Character.valueOf(42.toChar()))
        MAP.put("loz", Character.valueOf(224.toChar()))
        MAP.put("minus", Character.valueOf(45.toChar()))
        MAP.put("mu", Character.valueOf(109.toChar()))
        MAP.put("nabla", Character.valueOf(209.toChar()))
        MAP.put("ne", Character.valueOf(185.toChar()))
        MAP.put("not", Character.valueOf(216.toChar()))
        MAP.put("notin", Character.valueOf(207.toChar()))
        MAP.put("nsub", Character.valueOf(203.toChar()))
        MAP.put("nu", Character.valueOf(110.toChar()))
        MAP.put("omega", Character.valueOf(119.toChar()))
        MAP.put("omicron", Character.valueOf(111.toChar()))
        MAP.put("oplus", Character.valueOf(197.toChar()))
        MAP.put("or", Character.valueOf(218.toChar()))
        MAP.put("otimes", Character.valueOf(196.toChar()))
        MAP.put("part", Character.valueOf(182.toChar()))
        MAP.put("perp", Character.valueOf(94.toChar()))
        MAP.put("phi", Character.valueOf(102.toChar()))
        MAP.put("pi", Character.valueOf(112.toChar()))
        MAP.put("piv", Character.valueOf(118.toChar()))
        MAP.put("plusmn", Character.valueOf(177.toChar()))
        MAP.put("prime", Character.valueOf(162.toChar()))
        MAP.put("prod", Character.valueOf(213.toChar()))
        MAP.put("prop", Character.valueOf(181.toChar()))
        MAP.put("psi", Character.valueOf(121.toChar()))
        MAP.put("rArr", Character.valueOf(222.toChar()))
        MAP.put("radic", Character.valueOf(214.toChar()))
        MAP.put("radical extender", Character.valueOf(96.toChar()))
        MAP.put("rang", Character.valueOf(241.toChar()))
        MAP.put("rarr", Character.valueOf(174.toChar()))
        MAP.put("real", Character.valueOf(194.toChar()))
        MAP.put("reg", Character.valueOf(210.toChar()))
        MAP.put("rho", Character.valueOf(114.toChar()))
        MAP.put("sdot", Character.valueOf(215.toChar()))
        MAP.put("sigma", Character.valueOf(115.toChar()))
        MAP.put("sigmaf", Character.valueOf(86.toChar()))
        MAP.put("sim", Character.valueOf(126.toChar()))
        MAP.put("spades", Character.valueOf(170.toChar()))
        MAP.put("sub", Character.valueOf(204.toChar()))
        MAP.put("sube", Character.valueOf(205.toChar()))
        MAP.put("sum", Character.valueOf(229.toChar()))
        MAP.put("sup", Character.valueOf(201.toChar()))
        MAP.put("supe", Character.valueOf(202.toChar()))
        MAP.put("tau", Character.valueOf(116.toChar()))
        MAP.put("there4", Character.valueOf(92.toChar()))
        MAP.put("theta", Character.valueOf(113.toChar()))
        MAP.put("thetasym", Character.valueOf(74.toChar()))
        MAP.put("times", Character.valueOf(180.toChar()))
        MAP.put("trade", Character.valueOf(212.toChar()))
        MAP.put("uArr", Character.valueOf(221.toChar()))
        MAP.put("uarr", Character.valueOf(173.toChar()))
        MAP.put("upsih", Character.valueOf(161.toChar()))
        MAP.put("upsilon", Character.valueOf(117.toChar()))
        MAP.put("vertical arrow extender", Character.valueOf(189.toChar()))
        MAP.put("weierp", Character.valueOf(195.toChar()))
        MAP.put("xi", Character.valueOf(120.toChar()))
        MAP.put("zeta", Character.valueOf(122.toChar()))
    }

    /**
     * Gets a chunk with a symbol character.
     * @param e a symbol value (see Entities class: alfa is greek alfa,...)
     * *
     * @param font the font if the symbol isn't found (otherwise Font.SYMBOL)
     * *
     * @return a Chunk
     */
    operator fun get(e: String, font: Font): Chunk {
        val s = getCorrespondingSymbol(e)
        if (s == 0.toChar()) {
            try {
                return Chunk(Integer.parseInt(e).toChar().toString(), font)
            } catch (exception: Exception) {
                return Chunk(e, font)
            }

        }
        val symbol = Font(FontFamily.SYMBOL, font.size, font.style, font.color)
        return Chunk(s.toString(), symbol)
    }

    /**
     * Looks for the corresponding symbol in the font Symbol.

     * @param    name    the name of the entity
     * *
     * @return    the corresponding character in font Symbol
     */
    fun getCorrespondingSymbol(name: String): Char {
        val symbol = MAP[name] ?: return 0.toChar()
        return symbol.charValue()
    }
}
