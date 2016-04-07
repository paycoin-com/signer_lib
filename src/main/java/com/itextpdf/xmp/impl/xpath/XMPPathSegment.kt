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

package com.itextpdf.xmp.impl.xpath


/**
 * A segment of a parsed `XMPPath`.

 * @since   23.06.2006
 */
class XMPPathSegment {
    /** name of the path segment  */
    /**
     * @return Returns the name.
     */
    /**
     * @param name The name to set.
     */
    var name: String? = null
    /** kind of the path segment  */
    /**
     * @return Returns the kind.
     */
    /**
     * @param kind The kind to set.
     */
    var kind: Int = 0
    /** flag if segment is an alias  */
    /**
     * @return Returns the alias.
     */
    /**
     * @param alias the flag to set
     */
    var isAlias: Boolean = false
    /** alias form if applicable  */
    /**
     * @return Returns the aliasForm if this segment has been created by an alias.
     */
    /**
     * @param aliasForm the aliasForm to set
     */
    var aliasForm: Int = 0


    /**
     * Constructor with initial values.

     * @param name the name of the segment
     */
    constructor(name: String) {
        this.name = name
    }


    /**
     * Constructor with initial values.

     * @param name the name of the segment
     * *
     * @param kind the kind of the segment
     */
    constructor(name: String, kind: Int) {
        this.name = name
        this.kind = kind
    }


    /**
     * @see Object.toString
     */
    override fun toString(): String {
        when (kind) {
            XMPPath.STRUCT_FIELD_STEP, XMPPath.ARRAY_INDEX_STEP, XMPPath.QUALIFIER_STEP, XMPPath.ARRAY_LAST_STEP -> return name
            XMPPath.QUAL_SELECTOR_STEP, XMPPath.FIELD_SELECTOR_STEP -> return name

            else -> // no defined step
                return name
        }
    }
}
