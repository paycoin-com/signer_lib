/*
 * $Id: b0318aebcd0be1811579281731be9b10fd985e8b $
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
package com.itextpdf.text.pdf.hyphenation

import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.StringTokenizer

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser

/** Parses the xml hyphenation pattern.

 * @author Paulo Soares
 */
class SimplePatternParser : SimpleXMLDocHandler, PatternConsumer {
    internal var currElement: Int = 0

    internal var consumer: PatternConsumer

    internal var token: StringBuffer

    internal var exception: ArrayList<Any>

    internal var hyphenChar: Char = ' '

    internal var parser: SimpleXMLParser

    init {
        token = StringBuffer()
        hyphenChar = '-' // default
    }

    fun parse(stream: InputStream, consumer: PatternConsumer) {
        this.consumer = consumer
        try {
            SimpleXMLParser.parse(this, stream)
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        } finally {
            try {
                stream.close()
            } catch (e: Exception) {
            }

        }
    }

    protected fun normalizeException(ex: ArrayList<Any>): ArrayList<Any> {
        val res = ArrayList<Any>()
        for (i in ex.indices) {
            val item = ex[i]
            if (item is String) {
                val buf = StringBuffer()
                for (j in 0..item.length - 1) {
                    val c = item[j]
                    if (c != hyphenChar) {
                        buf.append(c)
                    } else {
                        res.add(buf.toString())
                        buf.setLength(0)
                        val h = CharArray(1)
                        h[0] = hyphenChar
                        // we use here hyphenChar which is not necessarily
                        // the one to be printed
                        res.add(Hyphen(String(h), null, null))
                    }
                }
                if (buf.length > 0) {
                    res.add(buf.toString())
                }
            } else {
                res.add(item)
            }
        }
        return res
    }

    protected fun getExceptionWord(ex: ArrayList<Any>): String {
        val res = StringBuffer()
        for (i in ex.indices) {
            val item = ex[i]
            if (item is String) {
                res.append(item)
            } else {
                if ((item as Hyphen).noBreak != null) {
                    res.append(item.noBreak)
                }
            }
        }
        return res.toString()
    }

    override fun endDocument() {
    }

    @SuppressWarnings("unchecked")
    override fun endElement(tag: String) {
        if (token.length > 0) {
            val word = token.toString()
            when (currElement) {
                ELEM_CLASSES -> consumer.addClass(word)
                ELEM_EXCEPTIONS -> {
                    exception.add(word)
                    exception = normalizeException(exception)
                    consumer.addException(getExceptionWord(exception),
                            exception.clone() as ArrayList<Any>)
                }
                ELEM_PATTERNS -> consumer.addPattern(getPattern(word),
                        getInterletterValues(word))
                ELEM_HYPHEN -> {
                }
            }// nothing to do
            if (currElement != ELEM_HYPHEN) {
                token.setLength(0)
            }
        }
        if (currElement == ELEM_HYPHEN) {
            currElement = ELEM_EXCEPTIONS
        } else {
            currElement = 0
        }
    }

    override fun startDocument() {
    }

    override fun startElement(tag: String, h: Map<String, String>) {
        if (tag == "hyphen-char") {
            val hh = h["value"]
            if (hh != null && hh.length == 1) {
                hyphenChar = hh[0]
            }
        } else if (tag == "classes") {
            currElement = ELEM_CLASSES
        } else if (tag == "patterns") {
            currElement = ELEM_PATTERNS
        } else if (tag == "exceptions") {
            currElement = ELEM_EXCEPTIONS
            exception = ArrayList<Any>()
        } else if (tag == "hyphen") {
            if (token.length > 0) {
                exception.add(token.toString())
            }
            exception.add(Hyphen(h["pre"], h["no"], h["post"]))
            currElement = ELEM_HYPHEN
        }
        token.setLength(0)
    }

    @SuppressWarnings("unchecked")
    override fun text(str: String) {
        val tk = StringTokenizer(str)
        while (tk.hasMoreTokens()) {
            val word = tk.nextToken()
            // System.out.println("\"" + word + "\"");
            when (currElement) {
                ELEM_CLASSES -> consumer.addClass(word)
                ELEM_EXCEPTIONS -> {
                    exception.add(word)
                    exception = normalizeException(exception)
                    consumer.addException(getExceptionWord(exception),
                            exception.clone() as ArrayList<Any>)
                    exception.clear()
                }
                ELEM_PATTERNS -> consumer.addPattern(getPattern(word),
                        getInterletterValues(word))
            }
        }
    }

    // PatternConsumer implementation for testing purposes
    override fun addClass(c: String) {
        println("class: " + c)
    }

    override fun addException(w: String, e: ArrayList<Any>) {
        println("exception: " + w + " : " + e.toString())
    }

    override fun addPattern(p: String, v: String) {
        println("pattern: $p : $v")
    }

    companion object {

        internal val ELEM_CLASSES = 1

        internal val ELEM_EXCEPTIONS = 2

        internal val ELEM_PATTERNS = 3

        internal val ELEM_HYPHEN = 4

        protected fun getPattern(word: String): String {
            val pat = StringBuffer()
            val len = word.length
            for (i in 0..len - 1) {
                if (!Character.isDigit(word[i])) {
                    pat.append(word[i])
                }
            }
            return pat.toString()
        }

        protected fun getInterletterValues(pat: String): String {
            val il = StringBuffer()
            val word = pat + "a" // add dummy letter to serve as sentinel
            val len = word.length
            var i = 0
            while (i < len) {
                val c = word[i]
                if (Character.isDigit(c)) {
                    il.append(c)
                    i++
                } else {
                    il.append('0')
                }
                i++
            }
            return il.toString()
        }
    }
    /*
	public static void main(String[] args) throws Exception {
		try {
			if (args.length > 0) {
				SimplePatternParser pp = new SimplePatternParser();
				pp.parse(new FileInputStream(args[0]), pp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
/** Creates a new instance of PatternParser2  */
