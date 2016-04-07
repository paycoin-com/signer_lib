/*
 * $Id: 320bddea1e0d9620089563d77b8e5f450f3bcaf6 $
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
package com.itextpdf.text.pdf.languages

import com.itextpdf.text.pdf.BidiLine
import com.itextpdf.text.pdf.BidiOrder
import com.itextpdf.text.pdf.PdfWriter
import java.util.HashMap

/**
 * Shape arabic characters. This code was inspired by an LGPL'ed C library:
 * Pango ( see http://www.pango.com/ ). Note that the code of this class is
 * the original work of Paulo Soares.

 * @author Paulo Soares
 */
class ArabicLigaturizer : LanguageProcessor {

    internal class charstruct {
        var basechar: Char = ' '
        var mark1: Char = ' '               /* has to be initialized to zero */
        var vowel: Char = ' '
        var lignum: Int = 0           /* is a ligature with lignum aditional characters */
        var numshapes = 1
    }


    protected var options = 0
    protected var runDirection = PdfWriter.RUN_DIRECTION_RTL

    constructor() {
    }

    constructor(runDirection: Int, options: Int) {
        this.runDirection = runDirection
        this.options = options
    }

    override fun process(s: String): String {
        return BidiLine.processLTR(s, runDirection, options)
    }

    /**
     * Arabic is written from right to left.
     * @return true
     * *
     * @see com.itextpdf.text.pdf.languages.LanguageProcessor.isRTL
     */
    override val isRTL: Boolean
        get() = true

    companion object {

        private val maptable = HashMap<Char, CharArray>()
        /**
         * Some fonts do not implement ligaturized variations on Arabic characters
         * e.g. Simplified Arabic has got code point 0xFEED but not 0xFEEE
         */
        private val reverseLigatureMapTable = HashMap<Char, Char>()

        internal fun isVowel(s: Char): Boolean {
            return s.toInt() >= 0x064B && s.toInt() <= 0x0655 || s.toInt() == 0x0670
        }

        internal fun charshape(s: Char, which: Int): Char /* which 0=isolated 1=final 2=initial 3=medial */ {
            if (s.toInt() >= 0x0621 && s.toInt() <= 0x06D3) {
                val c = maptable[Character.valueOf(s)]
                if (c != null)
                    return c[which + 1]
            } else if (s.toInt() >= 0xfef5 && s.toInt() <= 0xfefb)
                return (s.toInt() + which).toChar()
            return s
        }

        internal fun shapecount(s: Char): Int {
            if (s.toInt() >= 0x0621 && s.toInt() <= 0x06D3 && !isVowel(s)) {
                val c = maptable[Character.valueOf(s)]
                if (c != null)
                    return c.size - 1
            } else if (s == ZWJ) {
                return 4
            }
            return 1
        }

        internal fun ligature(newchar: Char, oldchar: charstruct): Int {
            /* 0 == no ligature possible; 1 == vowel; 2 == two chars; 3 == Lam+Alef */
            var retval = 0

            if (oldchar.basechar.toInt() == 0)
                return 0
            if (isVowel(newchar)) {
                retval = 1
                if (oldchar.vowel.toInt() != 0 && newchar != SHADDA) {
                    retval = 2           /* we eliminate the old vowel .. */
                }
                when (newchar) {
                    SHADDA -> if (oldchar.mark1.toInt() == 0) {
                        oldchar.mark1 = SHADDA
                    } else {
                        return 0         /* no ligature possible */
                    }
                    HAMZABELOW -> when (oldchar.basechar) {
                        ALEF -> {
                            oldchar.basechar = ALEFHAMZABELOW
                            retval = 2
                        }
                        LAM_ALEF -> {
                            oldchar.basechar = LAM_ALEFHAMZABELOW
                            retval = 2
                        }
                        else -> oldchar.mark1 = HAMZABELOW
                    }
                    HAMZAABOVE -> when (oldchar.basechar) {
                        ALEF -> {
                            oldchar.basechar = ALEFHAMZA
                            retval = 2
                        }
                        LAM_ALEF -> {
                            oldchar.basechar = LAM_ALEFHAMZA
                            retval = 2
                        }
                        WAW -> {
                            oldchar.basechar = WAWHAMZA
                            retval = 2
                        }
                        YEH, ALEFMAKSURA, FARSIYEH -> {
                            oldchar.basechar = YEHHAMZA
                            retval = 2
                        }
                        else           /* whatever sense this may make .. */ -> oldchar.mark1 = HAMZAABOVE
                    }
                    MADDA -> when (oldchar.basechar) {
                        ALEF -> {
                            oldchar.basechar = ALEFMADDA
                            retval = 2
                        }
                    }
                    else -> oldchar.vowel = newchar
                }
                if (retval == 1) {
                    oldchar.lignum++
                }
                return retval
            }
            if (oldchar.vowel.toInt() != 0) {
                /* if we already joined a vowel, we can't join a Hamza */
                return 0
            }

            when (oldchar.basechar) {
                LAM -> when (newchar) {
                    ALEF -> {
                        oldchar.basechar = LAM_ALEF
                        oldchar.numshapes = 2
                        retval = 3
                    }
                    ALEFHAMZA -> {
                        oldchar.basechar = LAM_ALEFHAMZA
                        oldchar.numshapes = 2
                        retval = 3
                    }
                    ALEFHAMZABELOW -> {
                        oldchar.basechar = LAM_ALEFHAMZABELOW
                        oldchar.numshapes = 2
                        retval = 3
                    }
                    ALEFMADDA -> {
                        oldchar.basechar = LAM_ALEFMADDA
                        oldchar.numshapes = 2
                        retval = 3
                    }
                }
                0 -> {
                    oldchar.basechar = newchar
                    oldchar.numshapes = shapecount(newchar)
                    retval = 1
                }
            }
            return retval
        }

        internal fun copycstostring(string: StringBuffer, s: charstruct, level: Int) {
            /* s is a shaped charstruct; i is the index into the string */
            if (s.basechar.toInt() == 0)
                return

            string.append(s.basechar)
            s.lignum--
            if (s.mark1.toInt() != 0) {
                if (level and ar_novowel == 0) {
                    string.append(s.mark1)
                    s.lignum--
                } else {
                    s.lignum--
                }
            }
            if (s.vowel.toInt() != 0) {
                if (level and ar_novowel == 0) {
                    string.append(s.vowel)
                    s.lignum--
                } else {
                    /* vowel elimination */
                    s.lignum--
                }
            }
            //        while (s.lignum > 0) {                           /* NULL-insertion for Langbox-font */
            //            string[i] = 0;
            //            i++;
            //            (s.lignum)--;
            //        }
            //        return i;
        }

        // return len
        internal fun doublelig(string: StringBuffer, level: Int) /* Ok. We have presentation ligatures in our font. */ {
            var len: Int
            val olen = len = string.length
            var j = 0
            var si = 1
            var lapresult: Char

            while (si < olen) {
                lapresult = 0.toChar()
                if (level and ar_composedtashkeel != 0) {
                    when (string[j]) {
                        SHADDA -> when (string[si]) {
                            KASRA -> lapresult = 0xFC62.toChar()
                            FATHA -> lapresult = 0xFC60.toChar()
                            DAMMA -> lapresult = 0xFC61.toChar()
                            0x064C -> lapresult = 0xFC5E.toChar()
                            0x064D -> lapresult = 0xFC5F.toChar()
                        }
                        KASRA -> if (string[si] == SHADDA)
                            lapresult = 0xFC62.toChar()
                        FATHA -> if (string[si] == SHADDA)
                            lapresult = 0xFC60.toChar()
                        DAMMA -> if (string[si] == SHADDA)
                            lapresult = 0xFC61.toChar()
                    }
                }

                if (level and ar_lig != 0) {
                    when (string[j]) {
                        0xFEDF       /* LAM initial */ -> when (string[si]) {
                            0xFE9E -> lapresult = 0xFC3F.toChar()
                            0xFEA0 -> lapresult = 0xFCC9.toChar()
                            0xFEA2 -> lapresult = 0xFC40.toChar()
                            0xFEA4 -> lapresult = 0xFCCA.toChar()
                            0xFEA6 -> lapresult = 0xFC41.toChar()
                            0xFEA8 -> lapresult = 0xFCCB.toChar()
                            0xFEE2 -> lapresult = 0xFC42.toChar()
                            0xFEE4 -> lapresult = 0xFCCC.toChar()
                        }/* JEEM final *//* JEEM medial *//* HAH final *//* HAH medial *//* KHAH final *//* KHAH medial *//* MEEM final *//* MEEM medial */
                        0xFE97       /* TEH inital */ -> when (string[si]) {
                            0xFEA0 -> lapresult = 0xFCA1.toChar()
                            0xFEA4 -> lapresult = 0xFCA2.toChar()
                            0xFEA8 -> lapresult = 0xFCA3.toChar()
                        }/* JEEM medial *//* HAH medial *//* KHAH medial */
                        0xFE91       /* BEH inital */ -> when (string[si]) {
                            0xFEA0 -> lapresult = 0xFC9C.toChar()
                            0xFEA4 -> lapresult = 0xFC9D.toChar()
                            0xFEA8 -> lapresult = 0xFC9E.toChar()
                        }/* JEEM medial *//* HAH medial *//* KHAH medial */
                        0xFEE7       /* NOON inital */ -> when (string[si]) {
                            0xFEA0 -> lapresult = 0xFCD2.toChar()
                            0xFEA4 -> lapresult = 0xFCD3.toChar()
                            0xFEA8 -> lapresult = 0xFCD4.toChar()
                        }/* JEEM initial *//* HAH medial *//* KHAH medial */

                        0xFEE8       /* NOON medial */ -> when (string[si]) {
                            0xFEAE -> lapresult = 0xFC8A.toChar()
                            0xFEB0 -> lapresult = 0xFC8B.toChar()
                        }/* REH final  *//* ZAIN final */
                        0xFEE3       /* MEEM initial */ -> when (string[si]) {
                            0xFEA0 -> lapresult = 0xFCCE.toChar()
                            0xFEA4 -> lapresult = 0xFCCF.toChar()
                            0xFEA8 -> lapresult = 0xFCD0.toChar()
                            0xFEE4 -> lapresult = 0xFCD1.toChar()
                        }/* JEEM medial *//* HAH medial *//* KHAH medial *//* MEEM medial */

                        0xFED3       /* FEH initial */ -> when (string[si]) {
                            0xFEF2 -> lapresult = 0xFC32.toChar()
                        }/* YEH final */

                        else -> {
                        }
                    }                   /* end switch string[si] */
                }
                if (lapresult.toInt() != 0) {
                    string.setCharAt(j, lapresult)
                    len--
                    si++                 /* jump over one character */
                    /* we'll have to change this, too. */
                } else {
                    j++
                    string.setCharAt(j, string[si])
                    si++
                }
            }
            string.setLength(len)
        }

        internal fun connects_to_left(a: charstruct): Boolean {
            return a.numshapes > 2
        }

        internal fun shape(text: CharArray, string: StringBuffer, level: Int) {
            /* string is assumed to be empty and big enough.
   * text is the original text.
   * This routine does the basic arabic reshaping.
   * *len the number of non-null characters.
   *
   * Note: We have to unshape each character first!
   */
            var join: Int
            var which: Int
            var nextletter: Char

            var p = 0                     /* initialize for output */
            var oldchar = charstruct()
            var curchar = charstruct()
            while (p < text.size) {
                nextletter = text[p++]
                //nextletter = unshape (nextletter);

                join = ligature(nextletter, curchar)
                if (join == 0) {
                    /* shape curchar */
                    val nc = shapecount(nextletter)
                    //(*len)++;
                    if (nc == 1) {
                        which = 0        /* final or isolated */
                    } else {
                        which = 2        /* medial or initial */
                    }
                    if (connects_to_left(oldchar)) {
                        which++
                    }

                    which = which % curchar.numshapes
                    curchar.basechar = charshape(curchar.basechar, which)

                    /* get rid of oldchar */
                    copycstostring(string, oldchar, level)
                    oldchar = curchar    /* new values in oldchar */

                    /* init new curchar */
                    curchar = charstruct()
                    curchar.basechar = nextletter
                    curchar.numshapes = nc
                    curchar.lignum++
                    //          (*len) += unligature (&curchar, level);
                } else if (join == 1) {
                }
                //      else
                //        {
                //          (*len) += unligature (&curchar, level);
                //        }
                //      p = g_utf8_next_char (p);
            }

            /* Handle last char */
            if (connects_to_left(oldchar))
                which = 1
            else
                which = 0
            which = which % curchar.numshapes
            curchar.basechar = charshape(curchar.basechar, which)

            /* get rid of oldchar */
            copycstostring(string, oldchar, level)
            copycstostring(string, curchar, level)
        }

        fun arabic_shape(src: CharArray, srcoffset: Int, srclength: Int, dest: CharArray, destoffset: Int, destlength: Int, level: Int): Int {
            val str = CharArray(srclength)
            for (k in srclength + srcoffset - 1 downTo srcoffset)
                str[k - srcoffset] = src[k]
            val string = StringBuffer(srclength)
            shape(str, string, level)
            if (level and (ar_composedtashkeel or ar_lig) != 0)
                doublelig(string, level)
            //        string.reverse();
            System.arraycopy(string.toString().toCharArray(), 0, dest, destoffset, string.length)
            return string.length
        }

        fun processNumbers(text: CharArray, offset: Int, length: Int, options: Int) {
            val limit = offset + length
            if (options and DIGITS_MASK != 0) {
                var digitBase = '\u0030' // European digits
                when (options and DIGIT_TYPE_MASK) {
                    DIGIT_TYPE_AN -> digitBase = '\u0660'  // Arabic-Indic digits

                    DIGIT_TYPE_AN_EXTENDED -> digitBase = '\u06f0'  // Eastern Arabic-Indic digits (Persian and Urdu)

                    else -> {
                    }
                }

                when (options and DIGITS_MASK) {
                    DIGITS_EN2AN -> {
                        val digitDelta = digitBase - '\u0030'
                        for (i in offset..limit - 1) {
                            val ch = text[i]
                            if (ch <= '\u0039' && ch >= '\u0030') {
                                text[i] += digitDelta.toChar()
                            }
                        }
                    }

                    DIGITS_AN2EN -> {
                        val digitTop = (digitBase.toInt() + 9).toChar()
                        val digitDelta = '\u0030' - digitBase
                        for (i in offset..limit - 1) {
                            val ch = text[i]
                            if (ch <= digitTop && ch >= digitBase) {
                                text[i] += digitDelta.toChar()
                            }
                        }
                    }

                    DIGITS_EN2AN_INIT_LR -> shapeToArabicDigitsWithContext(text, 0, length, digitBase, false)

                    DIGITS_EN2AN_INIT_AL -> shapeToArabicDigitsWithContext(text, 0, length, digitBase, true)

                    else -> {
                    }
                }
            }
        }

        internal fun shapeToArabicDigitsWithContext(dest: CharArray, start: Int, length: Int, digitBase: Char, lastStrongWasAL: Boolean) {
            var digitBase = digitBase
            var lastStrongWasAL = lastStrongWasAL
            digitBase -= '0' // move common adjustment out of loop

            val limit = start + length
            for (i in start..limit - 1) {
                val ch = dest[i]
                when (BidiOrder.getDirection(ch)) {
                    BidiOrder.L, BidiOrder.R -> lastStrongWasAL = false
                    BidiOrder.AL -> lastStrongWasAL = true
                    BidiOrder.EN -> if (lastStrongWasAL && ch <= '\u0039') {
                        dest[i] = (ch + digitBase).toChar()
                    }
                    else -> {
                    }
                }
            }
        }

        fun getReverseMapping(c: Char): Char? {
            return reverseLigatureMapTable[c]
        }

        private val ALEF: Char = 0x0627.toChar()
        private val ALEFHAMZA: Char = 0x0623.toChar()
        private val ALEFHAMZABELOW: Char = 0x0625.toChar()
        private val ALEFMADDA: Char = 0x0622.toChar()
        private val LAM: Char = 0x0644.toChar()
        private val HAMZA: Char = 0x0621.toChar()
        private val TATWEEL: Char = 0x0640.toChar()
        private val ZWJ: Char = 0x200D.toChar()

        private val HAMZAABOVE: Char = 0x0654.toChar()
        private val HAMZABELOW: Char = 0x0655.toChar()

        private val WAWHAMZA: Char = 0x0624.toChar()
        private val YEHHAMZA: Char = 0x0626.toChar()
        private val WAW: Char = 0x0648.toChar()
        private val ALEFMAKSURA: Char = 0x0649.toChar()
        private val YEH: Char = 0x064A.toChar()
        private val FARSIYEH: Char = 0x06CC.toChar()

        private val SHADDA: Char = 0x0651.toChar()
        private val KASRA: Char = 0x0650.toChar()
        private val FATHA: Char = 0x064E.toChar()
        private val DAMMA: Char = 0x064F.toChar()
        private val MADDA: Char = 0x0653.toChar()

        private val LAM_ALEF: Char = 0xFEFB.toChar()
        private val LAM_ALEFHAMZA: Char = 0xFEF7.toChar()
        private val LAM_ALEFHAMZABELOW: Char = 0xFEF9.toChar()
        private val LAM_ALEFMADDA: Char = 0xFEF5.toChar()

        private val chartable = arrayOf(charArrayOf(0x0621.toChar(), 0xFE80.toChar()), /* HAMZA */
                charArrayOf(0x0622.toChar(), 0xFE81.toChar(), 0xFE82.toChar()), /* ALEF WITH MADDA ABOVE */
                charArrayOf(0x0623.toChar(), 0xFE83.toChar(), 0xFE84.toChar()), /* ALEF WITH HAMZA ABOVE */
                charArrayOf(0x0624.toChar(), 0xFE85.toChar(), 0xFE86.toChar()), /* WAW WITH HAMZA ABOVE */
                charArrayOf(0x0625.toChar(), 0xFE87.toChar(), 0xFE88.toChar()), /* ALEF WITH HAMZA BELOW */
                charArrayOf(0x0626.toChar(), 0xFE89.toChar(), 0xFE8A.toChar(), 0xFE8B.toChar(), 0xFE8C.toChar()), /* YEH WITH HAMZA ABOVE */
                charArrayOf(0x0627.toChar(), 0xFE8D.toChar(), 0xFE8E.toChar()), /* ALEF */
                charArrayOf(0x0628.toChar(), 0xFE8F.toChar(), 0xFE90.toChar(), 0xFE91.toChar(), 0xFE92.toChar()), /* BEH */
                charArrayOf(0x0629.toChar(), 0xFE93.toChar(), 0xFE94.toChar()), /* TEH MARBUTA */
                charArrayOf(0x062A.toChar(), 0xFE95.toChar(), 0xFE96.toChar(), 0xFE97.toChar(), 0xFE98.toChar()), /* TEH */
                charArrayOf(0x062B.toChar(), 0xFE99.toChar(), 0xFE9A.toChar(), 0xFE9B.toChar(), 0xFE9C.toChar()), /* THEH */
                charArrayOf(0x062C.toChar(), 0xFE9D.toChar(), 0xFE9E.toChar(), 0xFE9F.toChar(), 0xFEA0.toChar()), /* JEEM */
                charArrayOf(0x062D.toChar(), 0xFEA1.toChar(), 0xFEA2.toChar(), 0xFEA3.toChar(), 0xFEA4.toChar()), /* HAH */
                charArrayOf(0x062E.toChar(), 0xFEA5.toChar(), 0xFEA6.toChar(), 0xFEA7.toChar(), 0xFEA8.toChar()), /* KHAH */
                charArrayOf(0x062F.toChar(), 0xFEA9.toChar(), 0xFEAA.toChar()), /* DAL */
                charArrayOf(0x0630.toChar(), 0xFEAB.toChar(), 0xFEAC.toChar()), /* THAL */
                charArrayOf(0x0631.toChar(), 0xFEAD.toChar(), 0xFEAE.toChar()), /* REH */
                charArrayOf(0x0632.toChar(), 0xFEAF.toChar(), 0xFEB0.toChar()), /* ZAIN */
                charArrayOf(0x0633.toChar(), 0xFEB1.toChar(), 0xFEB2.toChar(), 0xFEB3.toChar(), 0xFEB4.toChar()), /* SEEN */
                charArrayOf(0x0634.toChar(), 0xFEB5.toChar(), 0xFEB6.toChar(), 0xFEB7.toChar(), 0xFEB8.toChar()), /* SHEEN */
                charArrayOf(0x0635.toChar(), 0xFEB9.toChar(), 0xFEBA.toChar(), 0xFEBB.toChar(), 0xFEBC.toChar()), /* SAD */
                charArrayOf(0x0636.toChar(), 0xFEBD.toChar(), 0xFEBE.toChar(), 0xFEBF.toChar(), 0xFEC0.toChar()), /* DAD */
                charArrayOf(0x0637.toChar(), 0xFEC1.toChar(), 0xFEC2.toChar(), 0xFEC3.toChar(), 0xFEC4.toChar()), /* TAH */
                charArrayOf(0x0638.toChar(), 0xFEC5.toChar(), 0xFEC6.toChar(), 0xFEC7.toChar(), 0xFEC8.toChar()), /* ZAH */
                charArrayOf(0x0639.toChar(), 0xFEC9.toChar(), 0xFECA.toChar(), 0xFECB.toChar(), 0xFECC.toChar()), /* AIN */
                charArrayOf(0x063A.toChar(), 0xFECD.toChar(), 0xFECE.toChar(), 0xFECF.toChar(), 0xFED0.toChar()), /* GHAIN */
                charArrayOf(0x0640.toChar(), 0x0640.toChar(), 0x0640.toChar(), 0x0640.toChar(), 0x0640.toChar()), /* TATWEEL */
                charArrayOf(0x0641.toChar(), 0xFED1.toChar(), 0xFED2.toChar(), 0xFED3.toChar(), 0xFED4.toChar()), /* FEH */
                charArrayOf(0x0642.toChar(), 0xFED5.toChar(), 0xFED6.toChar(), 0xFED7.toChar(), 0xFED8.toChar()), /* QAF */
                charArrayOf(0x0643.toChar(), 0xFED9.toChar(), 0xFEDA.toChar(), 0xFEDB.toChar(), 0xFEDC.toChar()), /* KAF */
                charArrayOf(0x0644.toChar(), 0xFEDD.toChar(), 0xFEDE.toChar(), 0xFEDF.toChar(), 0xFEE0.toChar()), /* LAM */
                charArrayOf(0x0645.toChar(), 0xFEE1.toChar(), 0xFEE2.toChar(), 0xFEE3.toChar(), 0xFEE4.toChar()), /* MEEM */
                charArrayOf(0x0646.toChar(), 0xFEE5.toChar(), 0xFEE6.toChar(), 0xFEE7.toChar(), 0xFEE8.toChar()), /* NOON */
                charArrayOf(0x0647.toChar(), 0xFEE9.toChar(), 0xFEEA.toChar(), 0xFEEB.toChar(), 0xFEEC.toChar()), /* HEH */
                charArrayOf(0x0648.toChar(), 0xFEED.toChar(), 0xFEEE.toChar()), /* WAW */
                charArrayOf(0x0649.toChar(), 0xFEEF.toChar(), 0xFEF0.toChar(), 0xFBE8.toChar(), 0xFBE9.toChar()), /* ALEF MAKSURA */
                charArrayOf(0x064A.toChar(), 0xFEF1.toChar(), 0xFEF2.toChar(), 0xFEF3.toChar(), 0xFEF4.toChar()), /* YEH */
                charArrayOf(0x0671.toChar(), 0xFB50.toChar(), 0xFB51.toChar()), /* ALEF WASLA */
                charArrayOf(0x0679.toChar(), 0xFB66.toChar(), 0xFB67.toChar(), 0xFB68.toChar(), 0xFB69.toChar()), /* TTEH */
                charArrayOf(0x067A.toChar(), 0xFB5E.toChar(), 0xFB5F.toChar(), 0xFB60.toChar(), 0xFB61.toChar()), /* TTEHEH */
                charArrayOf(0x067B.toChar(), 0xFB52.toChar(), 0xFB53.toChar(), 0xFB54.toChar(), 0xFB55.toChar()), /* BEEH */
                charArrayOf(0x067E.toChar(), 0xFB56.toChar(), 0xFB57.toChar(), 0xFB58.toChar(), 0xFB59.toChar()), /* PEH */
                charArrayOf(0x067F.toChar(), 0xFB62.toChar(), 0xFB63.toChar(), 0xFB64.toChar(), 0xFB65.toChar()), /* TEHEH */
                charArrayOf(0x0680.toChar(), 0xFB5A.toChar(), 0xFB5B.toChar(), 0xFB5C.toChar(), 0xFB5D.toChar()), /* BEHEH */
                charArrayOf(0x0683.toChar(), 0xFB76.toChar(), 0xFB77.toChar(), 0xFB78.toChar(), 0xFB79.toChar()), /* NYEH */
                charArrayOf(0x0684.toChar(), 0xFB72.toChar(), 0xFB73.toChar(), 0xFB74.toChar(), 0xFB75.toChar()), /* DYEH */
                charArrayOf(0x0686.toChar(), 0xFB7A.toChar(), 0xFB7B.toChar(), 0xFB7C.toChar(), 0xFB7D.toChar()), /* TCHEH */
                charArrayOf(0x0687.toChar(), 0xFB7E.toChar(), 0xFB7F.toChar(), 0xFB80.toChar(), 0xFB81.toChar()), /* TCHEHEH */
                charArrayOf(0x0688.toChar(), 0xFB88.toChar(), 0xFB89.toChar()), /* DDAL */
                charArrayOf(0x068C.toChar(), 0xFB84.toChar(), 0xFB85.toChar()), /* DAHAL */
                charArrayOf(0x068D.toChar(), 0xFB82.toChar(), 0xFB83.toChar()), /* DDAHAL */
                charArrayOf(0x068E.toChar(), 0xFB86.toChar(), 0xFB87.toChar()), /* DUL */
                charArrayOf(0x0691.toChar(), 0xFB8C.toChar(), 0xFB8D.toChar()), /* RREH */
                charArrayOf(0x0698.toChar(), 0xFB8A.toChar(), 0xFB8B.toChar()), /* JEH */
                charArrayOf(0x06A4.toChar(), 0xFB6A.toChar(), 0xFB6B.toChar(), 0xFB6C.toChar(), 0xFB6D.toChar()), /* VEH */
                charArrayOf(0x06A6.toChar(), 0xFB6E.toChar(), 0xFB6F.toChar(), 0xFB70.toChar(), 0xFB71.toChar()), /* PEHEH */
                charArrayOf(0x06A9.toChar(), 0xFB8E.toChar(), 0xFB8F.toChar(), 0xFB90.toChar(), 0xFB91.toChar()), /* KEHEH */
                charArrayOf(0x06AD.toChar(), 0xFBD3.toChar(), 0xFBD4.toChar(), 0xFBD5.toChar(), 0xFBD6.toChar()), /* NG */
                charArrayOf(0x06AF.toChar(), 0xFB92.toChar(), 0xFB93.toChar(), 0xFB94.toChar(), 0xFB95.toChar()), /* GAF */
                charArrayOf(0x06B1.toChar(), 0xFB9A.toChar(), 0xFB9B.toChar(), 0xFB9C.toChar(), 0xFB9D.toChar()), /* NGOEH */
                charArrayOf(0x06B3.toChar(), 0xFB96.toChar(), 0xFB97.toChar(), 0xFB98.toChar(), 0xFB99.toChar()), /* GUEH */
                charArrayOf(0x06BA.toChar(), 0xFB9E.toChar(), 0xFB9F.toChar()), /* NOON GHUNNA */
                charArrayOf(0x06BB.toChar(), 0xFBA0.toChar(), 0xFBA1.toChar(), 0xFBA2.toChar(), 0xFBA3.toChar()), /* RNOON */
                charArrayOf(0x06BE.toChar(), 0xFBAA.toChar(), 0xFBAB.toChar(), 0xFBAC.toChar(), 0xFBAD.toChar()), /* HEH DOACHASHMEE */
                charArrayOf(0x06C0.toChar(), 0xFBA4.toChar(), 0xFBA5.toChar()), /* HEH WITH YEH ABOVE */
                charArrayOf(0x06C1.toChar(), 0xFBA6.toChar(), 0xFBA7.toChar(), 0xFBA8.toChar(), 0xFBA9.toChar()), /* HEH GOAL */
                charArrayOf(0x06C5.toChar(), 0xFBE0.toChar(), 0xFBE1.toChar()), /* KIRGHIZ OE */
                charArrayOf(0x06C6.toChar(), 0xFBD9.toChar(), 0xFBDA.toChar()), /* OE */
                charArrayOf(0x06C7.toChar(), 0xFBD7.toChar(), 0xFBD8.toChar()), /* U */
                charArrayOf(0x06C8.toChar(), 0xFBDB.toChar(), 0xFBDC.toChar()), /* YU */
                charArrayOf(0x06C9.toChar(), 0xFBE2.toChar(), 0xFBE3.toChar()), /* KIRGHIZ YU */
                charArrayOf(0x06CB.toChar(), 0xFBDE.toChar(), 0xFBDF.toChar()), /* VE */
                charArrayOf(0x06CC.toChar(), 0xFBFC.toChar(), 0xFBFD.toChar(), 0xFBFE.toChar(), 0xFBFF.toChar()), /* FARSI YEH */
                charArrayOf(0x06D0.toChar(), 0xFBE4.toChar(), 0xFBE5.toChar(), 0xFBE6.toChar(), 0xFBE7.toChar()), /* E */
                charArrayOf(0x06D2.toChar(), 0xFBAE.toChar(), 0xFBAF.toChar()), /* YEH BARREE */
                charArrayOf(0x06D3.toChar(), 0xFBB0.toChar(), 0xFBB1.toChar()) /* YEH BARREE WITH HAMZA ABOVE */)

        val ar_nothing = 0x0
        val ar_novowel = 0x1
        val ar_composedtashkeel = 0x4
        val ar_lig = 0x8
        /**
         * Digit shaping option: Replace European digits (U+0030...U+0039) by Arabic-Indic digits.
         */
        val DIGITS_EN2AN = 0x20

        /**
         * Digit shaping option: Replace Arabic-Indic digits by European digits (U+0030...U+0039).
         */
        val DIGITS_AN2EN = 0x40

        /**
         * Digit shaping option:
         * Replace European digits (U+0030...U+0039) by Arabic-Indic digits
         * if the most recent strongly directional character
         * is an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC).
         * The initial state at the start of the text is assumed to be not an Arabic,
         * letter, so European digits at the start of the text will not change.
         * Compare to DIGITS_ALEN2AN_INIT_AL.
         */
        val DIGITS_EN2AN_INIT_LR = 0x60

        /**
         * Digit shaping option:
         * Replace European digits (U+0030...U+0039) by Arabic-Indic digits
         * if the most recent strongly directional character
         * is an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC).
         * The initial state at the start of the text is assumed to be an Arabic,
         * letter, so European digits at the start of the text will change.
         * Compare to DIGITS_ALEN2AN_INT_LR.
         */
        val DIGITS_EN2AN_INIT_AL = 0x80

        /** Not a valid option value.  */
        private val DIGITS_RESERVED = 0xa0

        /**
         * Bit mask for digit shaping options.
         */
        val DIGITS_MASK = 0xe0

        /**
         * Digit type option: Use Arabic-Indic digits (U+0660...U+0669).
         */
        val DIGIT_TYPE_AN = 0

        /**
         * Digit type option: Use Eastern (Extended) Arabic-Indic digits (U+06f0...U+06f9).
         */
        val DIGIT_TYPE_AN_EXTENDED = 0x100

        /**
         * Bit mask for digit type options.
         */
        val DIGIT_TYPE_MASK = 0x0100 // 0x3f00?

        init {
            for (c in chartable) {
                maptable.put(c[0], c)
                when (c.size) {
                // only store the 2->1 and 4->3 mapping, if they are there
                    5 -> {
                        reverseLigatureMapTable.put(c[4], c[3])
                        reverseLigatureMapTable.put(c[2], c[1])
                    }
                    3 -> reverseLigatureMapTable.put(c[2], c[1])
                }
                if (c[0].toInt() == 0x0637 || c[0].toInt() == 0x0638) {
                    reverseLigatureMapTable.put(c[4], c[1])
                    reverseLigatureMapTable.put(c[3], c[1])
                }
            }
        }
    }
}
