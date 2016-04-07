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
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta


/**
 * @since   11.08.2006
 */
internal class ParameterAsserts
/**
 * private constructor
 */
private constructor()// EMPTY
: XMPConst {
    companion object {


        /**
         * Asserts that an array name is set.
         * @param arrayName an array name
         * *
         * @throws XMPException Array name is null or empty
         */
        @Throws(XMPException::class)
        fun assertArrayName(arrayName: String?) {
            if (arrayName == null || arrayName.length == 0) {
                throw XMPException("Empty array name", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that a property name is set.
         * @param propName a property name or path
         * *
         * @throws XMPException Property name is null or empty
         */
        @Throws(XMPException::class)
        fun assertPropName(propName: String?) {
            if (propName == null || propName.length == 0) {
                throw XMPException("Empty property name", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that a schema namespace is set.
         * @param schemaNS a schema namespace
         * *
         * @throws XMPException Schema is null or empty
         */
        @Throws(XMPException::class)
        fun assertSchemaNS(schemaNS: String?) {
            if (schemaNS == null || schemaNS.length == 0) {
                throw XMPException("Empty schema namespace URI", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that a prefix is set.
         * @param prefix a prefix
         * *
         * @throws XMPException Prefix is null or empty
         */
        @Throws(XMPException::class)
        fun assertPrefix(prefix: String?) {
            if (prefix == null || prefix.length == 0) {
                throw XMPException("Empty prefix", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that a specific language is set.
         * @param specificLang a specific lang
         * *
         * @throws XMPException Specific language is null or empty
         */
        @Throws(XMPException::class)
        fun assertSpecificLang(specificLang: String?) {
            if (specificLang == null || specificLang.length == 0) {
                throw XMPException("Empty specific language", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that a struct name is set.
         * @param structName a struct name
         * *
         * @throws XMPException Struct name is null or empty
         */
        @Throws(XMPException::class)
        fun assertStructName(structName: String?) {
            if (structName == null || structName.length == 0) {
                throw XMPException("Empty array name", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that any string parameter is set.
         * @param param any string parameter
         * *
         * @throws XMPException Thrown if the parameter is null or has length 0.
         */
        @Throws(XMPException::class)
        fun assertNotNull(param: Any?) {
            if (param == null) {
                throw XMPException("Parameter must not be null", XMPError.BADPARAM)
            } else if (param is String && param.length == 0) {
                throw XMPException("Parameter must not be null or empty", XMPError.BADPARAM)
            }
        }


        /**
         * Asserts that the xmp object is of this implemention
         * ([XMPMetaImpl]).
         * @param xmp the XMP object
         * *
         * @throws XMPException A wrong implentaion is used.
         */
        @Throws(XMPException::class)
        fun assertImplementation(xmp: XMPMeta?) {
            if (xmp == null) {
                throw XMPException("Parameter must not be null",
                        XMPError.BADPARAM)
            } else if (xmp !is XMPMetaImpl) {
                throw XMPException("The XMPMeta-object is not compatible with this implementation",
                        XMPError.BADPARAM)
            }
        }
    }
}