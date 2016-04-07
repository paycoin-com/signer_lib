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

import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.impl.Utils
import com.itextpdf.xmp.properties.XMPAliasInfo


/**
 * Parser for XMP XPaths.

 * @since   01.03.2006
 */
object XMPPathParser {


/**
 * Split an XMPPath expression apart at the conceptual steps, adding the
 * root namespace prefix to the first property component. The schema URI is
 * put in the first (0th) slot in the expanded XMPPath. Check if the top
 * level component is an alias, but don't resolve it.
 *
 *
 * In the most verbose case steps are separated by '/', and each step can be
 * of these forms:
 *
 * prefix:name
 *  A top level property or struct field.
 * [index]
 *  An element of an array.
 * [last()]
 *  The last element of an array.
 * [fieldName=&quot;value&quot;]
 *  An element in an array of structs, chosen by a field value.
 * [@xml:lang=&quot;value&quot;]
 *  An element in an alt-text array, chosen by the xml:lang qualifier.
 * [?qualName=&quot;value&quot;]
 *  An element in an array, chosen by a qualifier value.
 * @xml:lang
 *  An xml:lang qualifier.
 * ?qualName
 *  A general qualifier.
 *
 *
 *
 * The logic is complicated though by shorthand for arrays, the separating
 * '/' and leading '*' are optional. These are all equivalent: array/*[2]
 * array/[2] array*[2] array[2] All of these are broken into the 2 steps
 * "array" and "[2]".
 *
 *
 * The value portion in the array selector forms is a string quoted by '''
 * or '"'. The value may contain any character including a doubled quoting
 * character. The value may be empty.
 *
 *
 * The syntax isn't checked, but an XML name begins with a letter or '_',
 * and contains letters, digits, '.', '-', '_', and a bunch of special
 * non-ASCII Unicode characters. An XML qualified name is a pair of names
 * separated by a colon.
 * @param schemaNS
 * *            schema namespace
 * *
 * @param path
 * *            property name
 * *
 * @return Returns the expandet XMPPath.
 * *
 * @throws XMPException
 * *             Thrown if the format is not correct somehow.
*/
@Throws(XMPException::class)
fun expandXPath(schemaNS:String?, path:String?):XMPPath {
if (schemaNS == null || path == null)
{
throw XMPException("Parameter must not be null", XMPError.BADPARAM)
}

val expandedXPath = XMPPath()
val pos = PathPosition()
pos.path = path

// Pull out the first component and do some special processing on it: add the schema
// namespace prefix and and see if it is an alias. The start must be a "qualName".
parseRootNode(schemaNS, pos, expandedXPath)

// Now continue to process the rest of the XMPPath string.
while (pos.stepEnd < path!!.length)
{
pos.stepBegin = pos.stepEnd

skipPathDelimiter(path, pos)

pos.stepEnd = pos.stepBegin


val segment:XMPPathSegment
if (path!!.get(pos.stepBegin) != '[')
{
// A struct field or qualifier.
segment = parseStructSegment(pos)
}
else
{
// One of the array forms.
segment = parseIndexSegment(pos)
}


if (segment.kind == XMPPath.STRUCT_FIELD_STEP)
{
if (segment.name.get(0) == '@')
{
segment.name = "?" + segment.name.substring(1)
if ("?xml:lang" != segment.name)
{
throw XMPException("Only xml:lang allowed with '@'",
XMPError.BADXPATH)
}
}
if (segment.name.get(0) == '?')
{
pos.nameStart++
segment.kind = XMPPath.QUALIFIER_STEP
}

verifyQualName(pos.path!!.substring(pos.nameStart, pos.nameEnd))
}
else if (segment.kind == XMPPath.FIELD_SELECTOR_STEP)
{
if (segment.name.get(1) == '@')
{
segment.name = "[?" + segment.name.substring(2)
if (!segment.name.startsWith("[?xml:lang="))
{
throw XMPException("Only xml:lang allowed with '@'",
XMPError.BADXPATH)
}
}

if (segment.name.get(1) == '?')
{
pos.nameStart++
segment.kind = XMPPath.QUAL_SELECTOR_STEP
verifyQualName(pos.path!!.substring(pos.nameStart, pos.nameEnd))
}
}

expandedXPath.add(segment)
}
return expandedXPath
}


/**
 * @param path
 * *
 * @param pos
 * *
 * @throws XMPException
*/
@Throws(XMPException::class)
private fun skipPathDelimiter(path:String, pos:PathPosition) {
if (path.get(pos.stepBegin) == '/')
{
// skip slash

pos.stepBegin++

// added for Java
if (pos.stepBegin >= path.length)
{
throw XMPException("Empty XMPPath segment", XMPError.BADXPATH)
}
}

if (path.get(pos.stepBegin) == '*')
{
// skip asterisk

pos.stepBegin++
if (pos.stepBegin >= path.length || path.get(pos.stepBegin) != '[')
{
throw XMPException("Missing '[' after '*'", XMPError.BADXPATH)
}
}
}


/**
 * Parses a struct segment
 * @param pos the current position in the path
 * *
 * @return Retusn the segment or an errror
 * *
 * @throws XMPException If the sement is empty
*/
@Throws(XMPException::class)
private fun parseStructSegment(pos:PathPosition):XMPPathSegment {
pos.nameStart = pos.stepBegin
while (pos.stepEnd < pos.path!!.length && "/[*".indexOf(pos.path!!.get(pos.stepEnd).toInt()) < 0)
{
pos.stepEnd++
}
pos.nameEnd = pos.stepEnd

if (pos.stepEnd == pos.stepBegin)
{
throw XMPException("Empty XMPPath segment", XMPError.BADXPATH)
}

// ! Touch up later, also changing '@' to '?'.
val segment = XMPPathSegment(pos.path!!.substring(pos.stepBegin, pos.stepEnd),
XMPPath.STRUCT_FIELD_STEP)
return segment
}


/**
 * Parses an array index segment.

 * @param pos the xmp path
 * *
 * @return Returns the segment or an error
 * *
 * @throws XMPException thrown on xmp path errors
*/
@Throws(XMPException::class)
private fun parseIndexSegment(pos:PathPosition):XMPPathSegment {
val segment:XMPPathSegment
pos.stepEnd++ // Look at the character after the leading '['.

if ('0' <= pos.path!!.get(pos.stepEnd) && pos.path!!.get(pos.stepEnd) <= '9')
{
// A numeric (decimal integer) array index.
while (pos.stepEnd < pos.path!!.length && '0' <= pos.path!!.get(pos.stepEnd)
&& pos.path!!.get(pos.stepEnd) <= '9')
{
pos.stepEnd++
}

segment = XMPPathSegment(null, XMPPath.ARRAY_INDEX_STEP)
}
else
{
// Could be "[last()]" or one of the selector forms. Find the ']' or '='.

while (pos.stepEnd < pos.path!!.length && pos.path!!.get(pos.stepEnd) != ']'
&& pos.path!!.get(pos.stepEnd) != '=')
{
pos.stepEnd++
}

if (pos.stepEnd >= pos.path!!.length)
{
throw XMPException("Missing ']' or '=' for array index", XMPError.BADXPATH)
}

if (pos.path!!.get(pos.stepEnd) == ']')
{
if ("[last()" != pos.path!!.substring(pos.stepBegin, pos.stepEnd))
{
throw XMPException(
"Invalid non-numeric array index", XMPError.BADXPATH)
}
segment = XMPPathSegment(null, XMPPath.ARRAY_LAST_STEP)
}
else
{
pos.nameStart = pos.stepBegin + 1
pos.nameEnd = pos.stepEnd
pos.stepEnd++ // Absorb the '=', remember the quote.
val quote = pos.path!!.get(pos.stepEnd)
if (quote != '\'' && quote != '"')
{
throw XMPException(
"Invalid quote in array selector", XMPError.BADXPATH)
}

pos.stepEnd++ // Absorb the leading quote.
while (pos.stepEnd < pos.path!!.length)
{
if (pos.path!!.get(pos.stepEnd) == quote)
{
// check for escaped quote
if (pos.stepEnd + 1 >= pos.path!!.length || pos.path!!.get(pos.stepEnd + 1) != quote)
{
break
}
pos.stepEnd++
}
pos.stepEnd++
}

if (pos.stepEnd >= pos.path!!.length)
{
throw XMPException("No terminating quote for array selector",
XMPError.BADXPATH)
}
pos.stepEnd++ // Absorb the trailing quote.

// ! Touch up later, also changing '@' to '?'.
segment = XMPPathSegment(null, XMPPath.FIELD_SELECTOR_STEP)
}
}


if (pos.stepEnd >= pos.path!!.length || pos.path!!.get(pos.stepEnd) != ']')
{
throw XMPException("Missing ']' for array index", XMPError.BADXPATH)
}
pos.stepEnd++
segment.name = pos.path!!.substring(pos.stepBegin, pos.stepEnd)

return segment
}


/**
 * Parses the root node of an XMP Path, checks if namespace and prefix fit together
 * and resolve the property to the base property if it is an alias.
 * @param schemaNS the root namespace
 * *
 * @param pos the parsing position helper
 * *
 * @param expandedXPath  the path to contribute to
 * *
 * @throws XMPException If the path is not valid.
*/
@Throws(XMPException::class)
private fun parseRootNode(schemaNS:String, pos:PathPosition, expandedXPath:XMPPath) {
while (pos.stepEnd < pos.path!!.length && "/[*".indexOf(pos.path!!.get(pos.stepEnd).toInt()) < 0)
{
pos.stepEnd++
}

if (pos.stepEnd == pos.stepBegin)
{
throw XMPException("Empty initial XMPPath step", XMPError.BADXPATH)
}

val rootProp = verifyXPathRoot(schemaNS, pos.path!!.substring(pos.stepBegin, pos.stepEnd))
val aliasInfo = XMPMetaFactory.getSchemaRegistry().findAlias(rootProp)
if (aliasInfo == null)
{
// add schema xpath step
expandedXPath.add(XMPPathSegment(schemaNS, XMPPath.SCHEMA_NODE))
val rootStep = XMPPathSegment(rootProp, XMPPath.STRUCT_FIELD_STEP)
expandedXPath.add(rootStep)
}
else
{
// add schema xpath step and base step of alias
expandedXPath.add(XMPPathSegment(aliasInfo!!.getNamespace(), XMPPath.SCHEMA_NODE))
val rootStep = XMPPathSegment(verifyXPathRoot(aliasInfo!!.getNamespace(),
aliasInfo!!.getPropName()),
XMPPath.STRUCT_FIELD_STEP)
rootStep.isAlias = true
rootStep.aliasForm = aliasInfo!!.getAliasForm().getOptions()
expandedXPath.add(rootStep)

if (aliasInfo!!.getAliasForm().isArrayAltText())
{
val qualSelectorStep = XMPPathSegment("[?xml:lang='x-default']",
XMPPath.QUAL_SELECTOR_STEP)
qualSelectorStep.isAlias = true
qualSelectorStep.aliasForm = aliasInfo!!.getAliasForm().getOptions()
expandedXPath.add(qualSelectorStep)
}
else if (aliasInfo!!.getAliasForm().isArray())
{
val indexStep = XMPPathSegment("[1]",
XMPPath.ARRAY_INDEX_STEP)
indexStep.isAlias = true
indexStep.aliasForm = aliasInfo!!.getAliasForm().getOptions()
expandedXPath.add(indexStep)
}
}
}


/**
 * Verifies whether the qualifier name is not XML conformant or the
 * namespace prefix has not been registered.

 * @param qualName
 * *            a qualifier name
 * *
 * @throws XMPException
 * *             If the name is not conformant
*/
@Throws(XMPException::class)
private fun verifyQualName(qualName:String) {
val colonPos = qualName.indexOf(':')
if (colonPos > 0)
{
val prefix = qualName.substring(0, colonPos)
if (Utils.isXMLNameNS(prefix))
{
val regURI = XMPMetaFactory.getSchemaRegistry().getNamespaceURI(
prefix)
if (regURI != null)
{
return
}

throw XMPException("Unknown namespace prefix for qualified name",
XMPError.BADXPATH)
}
}

throw XMPException("Ill-formed qualified name", XMPError.BADXPATH)
}


/**
 * Verify if an XML name is conformant.

 * @param name
 * *            an XML name
 * *
 * @throws XMPException
 * *             When the name is not XML conformant
*/
@Throws(XMPException::class)
private fun verifySimpleXMLName(name:String) {
if (!Utils.isXMLName(name))
{
throw XMPException("Bad XML name", XMPError.BADXPATH)
}
}


/**
 * Set up the first 2 components of the expanded XMPPath. Normalizes the various cases of using
 * the full schema URI and/or a qualified root property name. Returns true for normal
 * processing. If allowUnknownSchemaNS is true and the schema namespace is not registered, false
 * is returned. If allowUnknownSchemaNS is false and the schema namespace is not registered, an
 * exception is thrown
 *
 * (Should someday check the full syntax:)

 * @param schemaNS schema namespace
 * *
 * @param rootProp the root xpath segment
 * *
 * @return Returns root QName.
 * *
 * @throws XMPException Thrown if the format is not correct somehow.
*/
@Throws(XMPException::class)
private fun verifyXPathRoot(schemaNS:String?, rootProp:String):String {
// Do some basic checks on the URI and name. Try to lookup the URI. See if the name is
// qualified.

if (schemaNS == null || schemaNS!!.length == 0)
{
throw XMPException(
"Schema namespace URI is required", XMPError.BADSCHEMA)
}

if ((rootProp.get(0) == '?') || (rootProp.get(0) == '@'))
{
throw XMPException("Top level name must not be a qualifier", XMPError.BADXPATH)
}

if (rootProp.indexOf('/') >= 0 || rootProp.indexOf('[') >= 0)
{
throw XMPException("Top level name must be simple", XMPError.BADXPATH)
}

var prefix:String? = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(schemaNS)
if (prefix == null)
{
throw XMPException("Unregistered schema namespace URI", XMPError.BADSCHEMA)
}

// Verify the various URI and prefix combinations. Initialize the
// expanded XMPPath.
val colonPos = rootProp.indexOf(':')
if (colonPos < 0)
{
// The propName is unqualified, use the schemaURI and associated
// prefix.
verifySimpleXMLName(rootProp) // Verify the part before any colon
return prefix!! + rootProp
}
else
{
// The propName is qualified. Make sure the prefix is legit. Use the associated URI and
// qualified name.

// Verify the part before any colon
verifySimpleXMLName(rootProp.substring(0, colonPos))
verifySimpleXMLName(rootProp.substring(colonPos))

prefix = rootProp.substring(0, colonPos + 1)

val regPrefix = XMPMetaFactory.getSchemaRegistry().getNamespacePrefix(schemaNS)
if (regPrefix == null)
{
throw XMPException("Unknown schema namespace prefix", XMPError.BADSCHEMA)
}
if (prefix != regPrefix)
{
throw XMPException("Schema namespace URI and prefix mismatch",
XMPError.BADSCHEMA)
}

return rootProp
}
}
}/**
 * Private constructor
*/// empty





/**
 * This objects contains all needed char positions to parse.
*/
internal class PathPosition {
/** the complete path  */
var path:String? = null
/** the start of a segment name  */
var nameStart = 0
/** the end of a segment name  */
var nameEnd = 0
/** the begin of a step  */
var stepBegin = 0
/** the end of a step  */
var stepEnd = 0
}

