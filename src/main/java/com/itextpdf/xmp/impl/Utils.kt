//Copyright (c) 2006, Adobe Systems Incorporated
//All rights reserved.
//
//        Redistribution and use in source and binary forms, with or without
//        modification, are permitted provided that the following conditions are met:
//        1. Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//        2. Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//        3. All advertising materials mentioning features or use of this software
//        must display the following acknowledgement:
//        This product includes software developed by the Adobe Systems Incorporated.
//        4. Neither the name of the Adobe Systems Incorporated nor the
//        names of its contributors may be used to endorse or promote products
//        derived from this software without specific prior written permission.
//
//        THIS SOFTWARE IS PROVIDED BY ADOBE SYSTEMS INCORPORATED ''AS IS'' AND ANY
//        EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//        WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//        DISCLAIMED. IN NO EVENT SHALL ADOBE SYSTEMS INCORPORATED BE LIABLE FOR ANY
//        DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//        (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//        LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//        ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//        (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//        http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

package com.itextpdf.xmp.impl


import com.itextpdf.xmp.XMPConst


/**
 * Utility functions for the XMPToolkit implementation.

 * @since 06.06.2006
 */
class Utils
/**
 * Private constructor
 */
private constructor()// EMPTY
: XMPConst {
    companion object {
        /** segments of a UUID  */
        val UUID_SEGMENT_COUNT = 4
        /** length of a UUID  */
        val UUID_LENGTH = 32 + UUID_SEGMENT_COUNT
        /** table of XML name start chars (<= 0xFF)  */
        private var xmlNameStartChars: BooleanArray? = null
        /** table of XML name chars (<= 0xFF)  */
        private var xmlNameChars: BooleanArray? = null

        /** init char tables  */
        init {
            initCharTables()
        }


        /**
         * Normalize an xml:lang value so that comparisons are effectively case
         * insensitive as required by RFC 3066 (which superceeds RFC 1766). The
         * normalization rules:
         *
         *  *  The primary subtag is lower case, the suggested practice of ISO 639.
         *  *  All 2 letter secondary subtags are upper case, the suggested
         * practice of ISO 3166.
         *  *  All other subtags are lower case.
         *

         * @param value
         * *            raw value
         * *
         * @return Returns the normalized value.
         */
        fun normalizeLangValue(value: String): String {
            // don't normalize x-default
            if (XMPConst.X_DEFAULT == value) {
                return value
            }

            var subTag = 1
            val buffer = StringBuffer()

            for (i in 0..value.length - 1) {
                when (value[i]) {
                    '-', '_' -> {
                        // move to next subtag and convert underscore to hyphen
                        buffer.append('-')
                        subTag++
                    }
                    ' ' -> {
                    }
                    else -> // convert second subtag to uppercase, all other to lowercase
                        if (subTag != 2) {
                            buffer.append(Character.toLowerCase(value[i]))
                        } else {
                            buffer.append(Character.toUpperCase(value[i]))
                        }
                }// remove spaces

            }
            return buffer.toString()
        }


        /**
         * Split the name and value parts for field and qualifier selectors:
         *
         *  * [qualName="value"] - An element in an array of structs, chosen by a
         * field value.
         *  * [?qualName="value"] - An element in an array, chosen by a qualifier
         * value.
         *
         * The value portion is a string quoted by ''' or '"'. The value may contain
         * any character including a doubled quoting character. The value may be
         * empty. *Note:* It is assumed that the expression is formal
         * correct

         * @param selector
         * *            the selector
         * *
         * @return Returns an array where the first entry contains the name and the
         * *         second the value.
         */
        internal fun splitNameAndValue(selector: String): Array<String> {
            // get the name
            val eq = selector.indexOf('=')
            var pos = 1
            if (selector[pos] == '?') {
                pos++
            }
            val name = selector.substring(pos, eq)

            // get the value
            pos = eq + 1
            val quote = selector[pos]
            pos++
            val end = selector.length - 2 // quote and ]
            val value = StringBuffer(end - eq)
            while (pos < end) {
                value.append(selector[pos])
                pos++
                if (selector[pos] == quote) {
                    // skip one quote in value
                    pos++
                }
            }
            return arrayOf(name, value.toString())
        }


        /**

         * @param schema
         * *            a schema namespace
         * *
         * @param prop
         * *            an XMP Property
         * *
         * @return Returns true if the property is defined as &quot;Internal
         * *         Property&quot;, see XMP Specification.
         */
        internal fun isInternalProperty(schema: String, prop: String): Boolean {
            var isInternal = false

            if (XMPConst.NS_DC == schema) {
                if ("dc:format" == prop || "dc:language" == prop) {
                    isInternal = true
                }
            } else if (XMPConst.NS_XMP == schema) {
                if ("xmp:BaseURL" == prop || "xmp:CreatorTool" == prop
                        || "xmp:Format" == prop || "xmp:Locale" == prop
                        || "xmp:MetadataDate" == prop || "xmp:ModifyDate" == prop) {
                    isInternal = true
                }
            } else if (XMPConst.NS_PDF == schema) {
                if ("pdf:BaseURL" == prop || "pdf:Creator" == prop
                        || "pdf:ModDate" == prop || "pdf:PDFVersion" == prop
                        || "pdf:Producer" == prop) {
                    isInternal = true
                }
            } else if (XMPConst.NS_TIFF == schema) {
                isInternal = true
                if ("tiff:ImageDescription" == prop || "tiff:Artist" == prop
                        || "tiff:Copyright" == prop) {
                    isInternal = false
                }
            } else if (XMPConst.NS_EXIF == schema) {
                isInternal = true
                if ("exif:UserComment" == prop) {
                    isInternal = false
                }
            } else if (XMPConst.NS_EXIF_AUX == schema) {
                isInternal = true
            } else if (XMPConst.NS_PHOTOSHOP == schema) {
                if ("photoshop:ICCProfile" == prop) {
                    isInternal = true
                }
            } else if (XMPConst.NS_CAMERARAW == schema) {
                if ("crs:Version" == prop || "crs:RawFileName" == prop
                        || "crs:ToneCurveName" == prop) {
                    isInternal = true
                }
            } else if (XMPConst.NS_ADOBESTOCKPHOTO == schema) {
                isInternal = true
            } else if (XMPConst.NS_XMP_MM == schema) {
                isInternal = true
            } else if (XMPConst.TYPE_TEXT == schema) {
                isInternal = true
            } else if (XMPConst.TYPE_PAGEDFILE == schema) {
                isInternal = true
            } else if (XMPConst.TYPE_GRAPHICS == schema) {
                isInternal = true
            } else if (XMPConst.TYPE_IMAGE == schema) {
                isInternal = true
            } else if (XMPConst.TYPE_FONT == schema) {
                isInternal = true
            }

            return isInternal
        }


        /**
         * Check some requirements for an UUID:
         *
         *  * Length of the UUID is 32
         *  * The Delimiter count is 4 and all the 4 delimiter are on their right
         * position (8,13,18,23)
         *


         * @param uuid uuid to test
         * *
         * @return true - this is a well formed UUID, false - UUID has not the expected format
         */

        internal fun checkUUIDFormat(uuid: String?): Boolean {
            var result = true
            var delimCnt = 0
            var delimPos = 0

            if (uuid == null) {
                return false
            }

            delimPos = 0
            while (delimPos < uuid.length) {
                if (uuid[delimPos] == '-') {
                    delimCnt++
                    result = result && (delimPos == 8 || delimPos == 13 || delimPos == 18 || delimPos == 23)
                }
                delimPos++
            }

            return result && UUID_SEGMENT_COUNT == delimCnt && UUID_LENGTH == delimPos
        }


        /**
         * Simple check for valid XMLNames. Within ASCII range
         * ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6]
         * are accepted, above all characters (which is not entirely
         * correct according to the XML Spec.

         * @param name an XML Name
         * *
         * @return Return `true` if the name is correct.
         */
        fun isXMLName(name: String): Boolean {
            if (name.length > 0 && !isNameStartChar(name[0])) {
                return false
            }
            for (i in 1..name.length - 1) {
                if (!isNameChar(name[i])) {
                    return false
                }
            }
            return true
        }


        /**
         * Checks if the value is a legal "unqualified" XML name, as
         * defined in the XML Namespaces proposed recommendation.
         * These are XML names, except that they must not contain a colon.
         * @param name the value to check
         * *
         * @return Returns true if the name is a valid "unqualified" XML name.
         */
        fun isXMLNameNS(name: String): Boolean {
            if (name.length > 0 && (!isNameStartChar(name[0]) || name[0] == ':')) {
                return false
            }
            for (i in 1..name.length - 1) {
                if (!isNameChar(name[i]) || name[i] == ':') {
                    return false
                }
            }
            return true
        }


        /**
         * @param c  a char
         * *
         * @return Returns true if the char is an ASCII control char.
         */
        internal fun isControlChar(c: Char): Boolean {
            return (c.toInt() <= 0x1F || c.toInt() == 0x7F) &&
                    c.toInt() != 0x09 && c.toInt() != 0x0A && c.toInt() != 0x0D
        }


        /**
         * Serializes the node value in XML encoding. Its used for tag bodies and
         * attributes.
         * *Note:* The attribute is always limited by quotes,
         * thats why `&amp;apos;` is never serialized.
         * *Note:* Control chars are written unescaped, but if the user uses others than tab, LF
         * and CR the resulting XML will become invalid.
         * @param value a string
         * *
         * @param forAttribute flag if string is attribute value (need to additional escape quotes)
         * *
         * @param escapeWhitespaces Decides if LF, CR and TAB are escaped.
         * *
         * @return Returns the value ready for XML output.
         */
        fun escapeXML(value: String, forAttribute: Boolean, escapeWhitespaces: Boolean): String {
            // quick check if character are contained that need special treatment
            var needsEscaping = false
            for (i in 0..value.length - 1) {
                val c = value[i]
                if (c == '<' || c == '>' || c == '&' || // XML chars

                        escapeWhitespaces && (c == '\t' || c == '\n' || c == '\r') ||
                        forAttribute && c == '"') {
                    needsEscaping = true
                    break
                }
            }

            if (!needsEscaping) {
                // fast path
                return value
            } else {
                // slow path with escaping
                val buffer = StringBuffer(value.length * 4 / 3)
                for (i in 0..value.length - 1) {
                    val c = value[i]
                    if (!(escapeWhitespaces && (c == '\t' || c == '\n' || c == '\r'))) {
                        when (c) {
                        // we do what "Canonical XML" expects
                        // AUDIT: &apos; not serialized as only outer qoutes are used
                            '<' -> {
                                buffer.append("&lt;")
                                continue
                            }
                            '>' -> {
                                buffer.append("&gt;")
                                continue
                            }
                            '&' -> {
                                buffer.append("&amp;")
                                continue
                            }
                            '"' -> {
                                buffer.append(if (forAttribute) "&quot;" else "\"")
                                continue
                            }
                            else -> {
                                buffer.append(c)
                                continue
                            }
                        }
                    } else {
                        // write control chars escaped,
                        // if there are others than tab, LF and CR the xml will become invalid.
                        buffer.append("&#x")
                        buffer.append(Integer.toHexString(c.toInt()).toUpperCase())
                        buffer.append(';')
                    }
                }
                return buffer.toString()
            }
        }


        /**
         * Replaces the ASCII control chars with a space.

         * @param value
         * *            a node value
         * *
         * @return Returns the cleaned up value
         */
        internal fun removeControlChars(value: String): String {
            val buffer = StringBuffer(value)
            for (i in 0..buffer.length - 1) {
                if (isControlChar(buffer[i])) {
                    buffer.setCharAt(i, ' ')
                }
            }
            return buffer.toString()
        }


        /**
         * Simple check if a character is a valid XML start name char.
         * All characters according to the XML Spec 1.1 are accepted:
         * http://www.w3.org/TR/xml11/#NT-NameStartChar

         * @param ch a character
         * *
         * @return Returns true if the character is a valid first char of an XML name.
         */
        private fun isNameStartChar(ch: Char): Boolean {
            return ch.toInt() <= 0xFF && xmlNameStartChars!![ch] ||
                    ch.toInt() >= 0x100 && ch.toInt() <= 0x2FF ||
                    ch.toInt() >= 0x370 && ch.toInt() <= 0x37D ||
                    ch.toInt() >= 0x37F && ch.toInt() <= 0x1FFF ||
                    ch.toInt() >= 0x200C && ch.toInt() <= 0x200D ||
                    ch.toInt() >= 0x2070 && ch.toInt() <= 0x218F ||
                    ch.toInt() >= 0x2C00 && ch.toInt() <= 0x2FEF ||
                    ch.toInt() >= 0x3001 && ch.toInt() <= 0xD7FF ||
                    ch.toInt() >= 0xF900 && ch.toInt() <= 0xFDCF ||
                    ch.toInt() >= 0xFDF0 && ch.toInt() <= 0xFFFD ||
                    ch.toInt() >= 0x10000 && ch.toInt() <= 0xEFFFF
        }


        /**
         * Simple check if a character is a valid XML name char
         * (every char except the first one), according to the XML Spec 1.1:
         * http://www.w3.org/TR/xml11/#NT-NameChar

         * @param ch a character
         * *
         * @return Returns true if the character is a valid char of an XML name.
         */
        private fun isNameChar(ch: Char): Boolean {
            return ch.toInt() <= 0xFF && xmlNameChars!![ch] ||
                    isNameStartChar(ch) ||
                    ch.toInt() >= 0x300 && ch.toInt() <= 0x36F ||
                    ch.toInt() >= 0x203F && ch.toInt() <= 0x2040
        }


        /**
         * Initializes the char tables for the chars 0x00-0xFF for later use,
         * according to the XML 1.1 specification
         * http://www.w3.org/TR/xml11
         */
        private fun initCharTables() {
            xmlNameChars = BooleanArray(0x0100)
            xmlNameStartChars = BooleanArray(0x0100)

            for (ch in xmlNameChars!!.indices) {
                xmlNameStartChars[ch] = ch == ':' ||
                        'A' <= ch && ch <= 'Z' ||
                        ch == '_' ||
                        'a' <= ch && ch <= 'z' ||
                        0xC0 <= ch.toInt() && ch.toInt() <= 0xD6 ||
                        0xD8 <= ch.toInt() && ch.toInt() <= 0xF6 ||
                        0xF8 <= ch.toInt() && ch.toInt() <= 0xFF

                xmlNameChars[ch] = xmlNameStartChars!![ch] ||
                        ch == '-' ||
                        ch == '.' ||
                        '0' <= ch && ch <= '9' ||
                        ch.toInt() == 0xB7
            }
        }
    }
}