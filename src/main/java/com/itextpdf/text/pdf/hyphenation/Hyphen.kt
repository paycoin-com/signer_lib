/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itextpdf.text.pdf.hyphenation

import java.io.Serializable

/**
 * This class represents a hyphen. A 'full' hyphen is made of 3 parts:
 * the pre-break text, post-break text and no-break. If no line-break
 * is generated at this position, the no-break text is used, otherwise,
 * pre-break and post-break are used. Typically, pre-break is equal to
 * the hyphen character and the others are empty. However, this general
 * scheme allows support for cases in some languages where words change
 * spelling if they're split across lines, like german's 'backen' which
 * hyphenates 'bak-ken'. BTW, this comes from TeX.

 * @author Carlos Villegas @uniscope.co.jp>
 */

class Hyphen : Serializable {
    var preBreak: String? = null
    var noBreak: String? = null
    var postBreak: String? = null

    internal constructor(pre: String, no: String, post: String) {
        preBreak = pre
        noBreak = no
        postBreak = post
    }

    internal constructor(pre: String) {
        preBreak = pre
        noBreak = null
        postBreak = null
    }

    override fun toString(): String {
        if (noBreak == null
                && postBreak == null
                && preBreak != null
                && preBreak == "-") {
            return "-"
        }
        val res = StringBuffer("{")
        res.append(preBreak)
        res.append("}{")
        res.append(postBreak)
        res.append("}{")
        res.append(noBreak)
        res.append('}')
        return res.toString()
    }

    companion object {
        private val serialVersionUID = -7666138517324763063L
    }

}
