/*
 * $Id: 4037b42e7123d5a5991064c252a520c0877387be $
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
package com.itextpdf.text.pdf

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.io.*
import java.util.*

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.xml.XmlDomWriter
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException

/**
 * Processes XFA forms.
 * @author Paulo Soares
 */
class XfaForm {

    /**
     * Gets the class that contains the template processing section of the XFA.
     * @return the class that contains the template processing section of the XFA
     */
    /**
     * Sets the class that contains the template processing section of the XFA
     * @param templateSom the class that contains the template processing section of the XFA
     */
    var templateSom: Xml2SomTemplate? = null
    private var templateNode: Node? = null
    /**
     * Gets the class that contains the datasets processing section of the XFA.
     * @return the class that contains the datasets processing section of the XFA
     */
    /**
     * Sets the class that contains the datasets processing section of the XFA.
     * @param datasetsSom the class that contains the datasets processing section of the XFA
     */
    var datasetsSom: Xml2SomDatasets? = null
    /**
     * Gets the Node that corresponds to the datasets part.
     * @return the Node that corresponds to the datasets part
     */
    var datasetsNode: Node? = null
        private set
    /**
     * Gets the class that contains the "classic" fields processing.
     * @return the class that contains the "classic" fields processing
     */
    /**
     * Sets the class that contains the "classic" fields processing.
     * @param acroFieldsSom the class that contains the "classic" fields processing
     */
    var acroFieldsSom: AcroFieldsSearch? = null
    /**
     * Gets the PdfReader used by this instance.
     * @return the PdfReader used by this instance
     */
    /**
     * Sets the PdfReader to be used by this instance.
     * @param reader the PdfReader to be used by this instance
     */
    var reader: PdfReader? = null
    /**
     * Returns true if it is a XFA form.
     * @return true if it is a XFA form
     */
    /**
     * Sets the XFA form flag signaling that this is a valid XFA form.
     * @param xfaPresent the XFA form flag signaling that this is a valid XFA form
     */
    var isXfaPresent: Boolean = false
    /**
     * Gets the top level DOM document.
     * @return the top level DOM document
     */
    /**
     * Sets the top DOM document.
     * @param domDocument the top DOM document
     */
    var domDocument: org.w3c.dom.Document? = null
        set(domDocument) {
            this.domDocument = domDocument
            extractNodes()
        }
    /**
     * Checks if this XFA form was changed.
     * @return true if this XFA form was changed
     */
    /**
     * Sets the changed status of this XFA instance.
     * @param changed the changed status of this XFA instance
     */
    var isChanged: Boolean = false

    /**
     * An empty constructor to build on.
     */
    constructor() {
    }

    /**
     * A constructor from a PdfReader. It basically does everything
     * from finding the XFA stream to the XML parsing.
     * @param reader the reader
     * *
     * @throws java.io.IOException on error
     * *
     * @throws javax.xml.parsers.ParserConfigurationException on error
     * *
     * @throws org.xml.sax.SAXException on error
     */
    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    constructor(reader: PdfReader) {
        this.reader = reader
        val xfa = getXfaObject(reader)
        if (xfa == null) {
            isXfaPresent = false
            return
        }
        isXfaPresent = true
        val bout = ByteArrayOutputStream()
        if (xfa.isArray) {
            val ar = xfa as PdfArray?
            var k = 1
            while (k < ar.size()) {
                val ob = ar.getDirectObject(k)
                if (ob is PRStream) {
                    val b = PdfReader.getStreamBytes(ob)
                    bout.write(b)
                }
                k += 2
            }
        } else if (xfa is PRStream) {
            val b = PdfReader.getStreamBytes(xfa as PRStream?)
            bout.write(b)
        }
        bout.close()
        val fact = DocumentBuilderFactory.newInstance()
        fact.isNamespaceAware = true
        val db = fact.newDocumentBuilder()
        domDocument = db.parse(ByteArrayInputStream(bout.toByteArray()))
        extractNodes()
    }

    /**
     * Extracts the nodes from the domDocument.
     * @since    2.1.5
     */
    private fun extractNodes() {
        val xfaNodes = extractXFANodes(domDocument)

        if (xfaNodes.containsKey("template")) {
            templateNode = xfaNodes["template"]
            templateSom = Xml2SomTemplate(templateNode)
        }
        if (xfaNodes.containsKey("datasets")) {
            datasetsNode = xfaNodes["datasets"]
            datasetsSom = Xml2SomDatasets(datasetsNode!!.firstChild)
        }
        if (datasetsNode == null)
            createDatasetsNode(domDocument!!.firstChild)
    }

    /**
     * Some XFA forms don't have a datasets node.
     * If this is the case, we have to add one.
     */
    private fun createDatasetsNode(n: Node?) {
        var n = n
        while (n!!.childNodes.length == 0) {
            n = n.nextSibling
        }
        if (n != null) {
            val e = n.ownerDocument.createElement("xfa:datasets")
            e.setAttribute("xmlns:xfa", XFA_DATA_SCHEMA)
            datasetsNode = e
            n.appendChild(datasetsNode)
        }
    }

    /**
     * Sets the XFA key from the instance data. The old XFA is erased.
     * @param writer the writer
     * *
     * @throws java.io.IOException on error
     */
    @Throws(IOException::class)
    fun setXfa(writer: PdfWriter) {
        setXfa(this, reader, writer)
    }

    /**
     * Finds the complete field name contained in the "classic" forms from a partial
     * name.
     * @param name the complete or partial name
     * *
     * @param af the fields
     * *
     * @return the complete name or null if not found
     */
    fun findFieldName(name: String, af: AcroFields): String {
        val items = af.getFields()
        if (items.containsKey(name))
            return name
        if (acroFieldsSom == null) {
            if (items.isEmpty() && isXfaPresent)
                acroFieldsSom = AcroFieldsSearch(datasetsSom!!.name2Node.keys)
            else
                acroFieldsSom = AcroFieldsSearch(items.keys)
        }
        if (acroFieldsSom!!.acroShort2LongName.containsKey(name))
            return acroFieldsSom!!.acroShort2LongName.get(name)
        return acroFieldsSom!!.inverseSearchGlobal(Xml2Som.splitParts(name))
    }

    /**
     * Finds the complete SOM name contained in the datasets section from a
     * possibly partial name.
     * @param name the complete or partial name
     * *
     * @return the complete name or null if not found
     */
    fun findDatasetsName(name: String): String {
        if (datasetsSom!!.name2Node.containsKey(name))
            return name
        return datasetsSom!!.inverseSearchGlobal(Xml2Som.splitParts(name))
    }

    /**
     * Finds the Node contained in the datasets section from a
     * possibly partial name.
     * @param name the complete or partial name
     * *
     * @return the Node or null if not found
     */
    fun findDatasetsNode(name: String?): Node? {
        var name: String? = name ?: return null
        name = findDatasetsName(name)
        if (name == null)
            return null
        return datasetsSom!!.name2Node[name]
    }

    /**
     * Sets the text of this node. All the child's node are deleted and a new
     * child text node is created.
     * @param n the Node to add the text to
     * *
     * @param text the text to add
     */
    fun setNodeText(n: Node?, text: String) {
        if (n == null)
            return
        var nc: Node? = null
        while ((nc = n.firstChild) != null) {
            n.removeChild(nc)
        }
        if (n.attributes.getNamedItemNS(XFA_DATA_SCHEMA, "dataNode") != null)
            n.attributes.removeNamedItemNS(XFA_DATA_SCHEMA, "dataNode")
        n.appendChild(domDocument!!.createTextNode(text))
        isChanged = true
    }

    /**
     * A structure to store each part of a SOM name and link it to the next part
     * beginning from the lower hierarchy.
     */
    class InverseStore {
        protected var part = ArrayList<String>()
        protected var follow = ArrayList<Any>()

        /**
         * Gets the full name by traversing the hierarchy using only the
         * index 0.
         * @return the full name
         */
        val defaultName: String
            get() {
                var store = this
                while (true) {
                    val obj = store.follow[0]
                    if (obj is String)
                        return obj
                    store = obj as InverseStore
                }
            }

        /**
         * Search the current node for a similar name. A similar name starts
         * with the same name but has a different index. For example, "detail[3]"
         * is similar to "detail[9]". The main use is to discard names that
         * correspond to out of bounds records.
         * @param name the name to search
         * *
         * @return true if a similitude was found
         */
        fun isSimilar(name: String): Boolean {
            var name = name
            val idx = name.indexOf('[')
            name = name.substring(0, idx + 1)
            for (k in part.indices) {
                if (part[k].startsWith(name))
                    return true
            }
            return false
        }
    }

    /**
     * Another stack implementation. The main use is to facilitate
     * the porting to other languages.
     */
    class Stack2<T> : ArrayList<T>() {

        /**
         * Looks at the object at the top of this stack without removing it from the stack.
         * @return the object at the top of this stack
         */
        fun peek(): T {
            if (size == 0)
                throw EmptyStackException()
            return get(size - 1)
        }

        /**
         * Removes the object at the top of this stack and returns that object as the value of this function.
         * @return the object at the top of this stack
         */
        fun pop(): T {
            if (size == 0)
                throw EmptyStackException()
            val ret = get(size - 1)
            removeAt(size - 1)
            return ret
        }

        /**
         * Pushes an item onto the top of this stack.
         * @param item the item to be pushed onto this stack
         * *
         * @return the item argument
         */
        fun push(item: T): T {
            add(item)
            return item
        }

        /**
         * Tests if this stack is empty.
         * @return true if and only if this stack contains no items; false otherwise
         */
        fun empty(): Boolean {
            return size == 0
        }

        companion object {
            private val serialVersionUID = -7451476576174095212L
        }
    }

    /**
     * A class for some basic SOM processing.
     */
    open class Xml2Som {
        /**
         * The order the names appear in the XML, depth first.
         */
        /**
         * Gets the order the names appear in the XML, depth first.
         * @return the order the names appear in the XML, depth first
         */
        /**
         * Sets the order the names appear in the XML, depth first
         * @param order the order the names appear in the XML, depth first
         */
        var order: ArrayList<String>
        /**
         * The mapping of full names to nodes.
         */
        /**
         * Gets the mapping of full names to nodes.
         * @return the mapping of full names to nodes
         */
        /**
         * Sets the mapping of full names to nodes.
         * @param name2Node the mapping of full names to nodes
         */
        var name2Node: HashMap<String, Node>
        /**
         * The data to do a search from the bottom hierarchy.
         */
        /**
         * Gets the data to do a search from the bottom hierarchy.
         * @return the data to do a search from the bottom hierarchy
         */
        /**
         * Sets the data to do a search from the bottom hierarchy.
         * @param inverseSearch the data to do a search from the bottom hierarchy
         */
        var inverseSearch: HashMap<String, InverseStore>
        /**
         * A stack to be used when parsing.
         */
        protected var stack: Stack2<String>
        /**
         * A temporary store for the repetition count.
         */
        protected var anform: Int = 0

        /**
         * Outputs the stack as the sequence of elements separated
         * by '.'.
         * @return the stack as the sequence of elements separated by '.'
         */
        protected fun printStack(): String {
            if (stack.empty())
                return ""
            val s = StringBuffer()
            for (k in stack.indices)
                s.append('.').append(stack[k])
            return s.substring(1)
        }

        /**
         * Adds a SOM name to the search node chain.
         * @param unstack the SOM name
         */
        fun inverseSearchAdd(unstack: String) {
            inverseSearchAdd(inverseSearch, stack, unstack)
        }

        /**
         * Searches the SOM hierarchy from the bottom.
         * @param parts the SOM parts
         * *
         * @return the full name or null if not found
         */
        fun inverseSearchGlobal(parts: ArrayList<String>): String? {
            if (parts.isEmpty())
                return null
            var store: InverseStore? = inverseSearch[parts[parts.size - 1]] ?: return null
            for (k in parts.size - 2 downTo 0) {
                val part = parts[k]
                val idx = store!!.part.indexOf(part)
                if (idx < 0) {
                    if (store.isSimilar(part))
                        return null
                    return store.defaultName
                }
                store = store.follow[idx] as InverseStore
            }
            return store!!.defaultName
        }

        companion object {

            /**
             * Escapes a SOM string fragment replacing "." with "\.".
             * @param s the unescaped string
             * *
             * @return the escaped string
             */
            fun escapeSom(s: String?): String {
                if (s == null)
                    return ""
                var idx = s.indexOf('.')
                if (idx < 0)
                    return s
                val sb = StringBuffer()
                var last = 0
                while (idx >= 0) {
                    sb.append(s.substring(last, idx))
                    sb.append('\\')
                    last = idx
                    idx = s.indexOf('.', idx + 1)
                }
                sb.append(s.substring(last))
                return sb.toString()
            }

            /**
             * Unescapes a SOM string fragment replacing "\." with ".".
             * @param s the escaped string
             * *
             * @return the unescaped string
             */
            fun unescapeSom(s: String): String {
                var idx = s.indexOf('\\')
                if (idx < 0)
                    return s
                val sb = StringBuffer()
                var last = 0
                while (idx >= 0) {
                    sb.append(s.substring(last, idx))
                    last = idx + 1
                    idx = s.indexOf('\\', idx + 1)
                }
                sb.append(s.substring(last))
                return sb.toString()
            }

            /**
             * Gets the name with the #subform removed.
             * @param s the long name
             * *
             * @return the short name
             */
            fun getShortName(s: String): String {
                var idx = s.indexOf(".#subform[")
                if (idx < 0)
                    return s
                var last = 0
                val sb = StringBuffer()
                while (idx >= 0) {
                    sb.append(s.substring(last, idx))
                    idx = s.indexOf("]", idx + 10)
                    if (idx < 0)
                        return sb.toString()
                    last = idx + 1
                    idx = s.indexOf(".#subform[", last)
                }
                sb.append(s.substring(last))
                return sb.toString()
            }

            /**
             * Adds a SOM name to the search node chain.
             * @param inverseSearch the start point
             * *
             * @param stack the stack with the separated SOM parts
             * *
             * @param unstack the full name
             */
            fun inverseSearchAdd(inverseSearch: HashMap<String, InverseStore>, stack: Stack2<String>, unstack: String) {
                var last = stack.peek()
                var store: InverseStore? = inverseSearch[last]
                if (store == null) {
                    store = InverseStore()
                    inverseSearch.put(last, store)
                }
                for (k in stack.size - 2 downTo 0) {
                    last = stack[k]
                    val store2: InverseStore
                    val idx = store!!.part.indexOf(last)
                    if (idx < 0) {
                        store.part.add(last)
                        store2 = InverseStore()
                        store.follow.add(store2)
                    } else
                        store2 = store.follow[idx] as InverseStore
                    store = store2
                }
                store!!.part.add("")
                store.follow.add(unstack)
            }

            /**
             * Splits a SOM name in the individual parts.
             * @param name the full SOM name
             * *
             * @return the split name
             */
            fun splitParts(name: String): Stack2<String> {
                var name = name
                while (name.startsWith("."))
                    name = name.substring(1)
                val parts = Stack2<String>()
                var last = 0
                var pos = 0
                var part: String
                while (true) {
                    pos = last
                    while (true) {
                        pos = name.indexOf('.', pos)
                        if (pos < 0)
                            break
                        if (name[pos - 1] == '\\')
                            ++pos
                        else
                            break
                    }
                    if (pos < 0)
                        break
                    part = name.substring(last, pos)
                    if (!part.endsWith("]"))
                        part += "[0]"
                    parts.add(part)
                    last = pos + 1
                }
                part = name.substring(last)
                if (!part.endsWith("]"))
                    part += "[0]"
                parts.add(part)
                return parts
            }
        }
    }

    /**
     * Processes the datasets section in the XFA form.
     */
    class Xml2SomDatasets
    /**
     * Creates a new instance from the datasets node. This expects
     * not the datasets but the data node that comes below.
     * @param n the datasets node
     */
    (n: Node) : Xml2Som() {
        init {
            order = ArrayList<String>()
            name2Node = HashMap<String, Node>()
            stack = Stack2<String>()
            anform = 0
            inverseSearch = HashMap<String, InverseStore>()
            processDatasetsInternal(n)
        }

        /**
         * Inserts a new Node that will match the short name.
         * @param n the datasets top Node
         * *
         * @param shortName the short name
         * *
         * @return the new Node of the inserted name
         */
        fun insertNode(n: Node, shortName: String): Node {
            var n = n
            val stack = XfaForm.Xml2Som.splitParts(shortName)
            val doc = n.ownerDocument
            var n2: Node? = null
            n = n.firstChild
            while (n.nodeType != Node.ELEMENT_NODE)
                n = n.nextSibling
            for (k in stack.indices) {
                val part = stack[k]
                var idx = part.lastIndexOf('[')
                val name = part.substring(0, idx)
                idx = Integer.parseInt(part.substring(idx + 1, part.length - 1))
                var found = -1
                n2 = n.firstChild
                while (n2 != null) {
                    if (n2.nodeType == Node.ELEMENT_NODE) {
                        val s = XfaForm.Xml2Som.escapeSom(n2.localName)
                        if (s == name) {
                            ++found
                            if (found == idx)
                                break
                        }
                    }
                    n2 = n2.nextSibling
                }
                while (found < idx) {
                    n2 = doc.createElementNS(null, name)
                    n2 = n.appendChild(n2)
                    val attr = doc.createAttributeNS(XFA_DATA_SCHEMA, "dataNode")
                    attr.nodeValue = "dataGroup"
                    n2!!.attributes.setNamedItemNS(attr)
                    ++found
                }
                n = n2
            }
            XfaForm.Xml2Som.inverseSearchAdd(inverseSearch, stack, shortName)
            name2Node.put(shortName, n2)
            order.add(shortName)
            return n2
        }

        private fun hasChildren(n: Node): Boolean {
            val dataNodeN = n.attributes.getNamedItemNS(XFA_DATA_SCHEMA, "dataNode")
            if (dataNodeN != null) {
                val dataNode = dataNodeN.nodeValue
                if ("dataGroup" == dataNode)
                    return true
                else if ("dataValue" == dataNode)
                    return false
            }
            if (!n.hasChildNodes())
                return false
            var n2: Node? = n.firstChild
            while (n2 != null) {
                if (n2.nodeType == Node.ELEMENT_NODE) {
                    return true
                }
                n2 = n2.nextSibling
            }
            return false
        }

        private fun processDatasetsInternal(n: Node?) {
            if (n != null) {
                val ss = HashMap<String, Int>()
                var n2: Node? = n.firstChild
                while (n2 != null) {
                    if (n2.nodeType == Node.ELEMENT_NODE) {
                        val s = XfaForm.Xml2Som.escapeSom(n2.localName)
                        var i: Int? = ss[s]
                        if (i == null)
                            i = Integer.valueOf(0)
                        else
                            i = Integer.valueOf(i.toInt() + 1)
                        ss.put(s, i)
                        if (hasChildren(n2)) {
                            stack.push(s + "[" + i!!.toString() + "]")
                            processDatasetsInternal(n2)
                            stack.pop()
                        } else {
                            stack.push(s + "[" + i!!.toString() + "]")
                            val unstack = printStack()
                            order.add(unstack)
                            inverseSearchAdd(unstack)
                            name2Node.put(unstack, n2)
                            stack.pop()
                        }
                    }
                    n2 = n2.nextSibling
                }
            }
        }
    }

    /**
     * A class to process "classic" fields.
     */
    class AcroFieldsSearch
    /**
     * Creates a new instance from a Collection with the full names.
     * @param items the Collection
     */
    (items: Collection<String>) : Xml2Som() {
        /**
         * Gets the mapping from short names to long names. A long
         * name may contain the #subform name part.
         * @return the mapping from short names to long names
         */
        /**
         * Sets the mapping from short names to long names. A long
         * name may contain the #subform name part.
         * @param acroShort2LongName the mapping from short names to long names
         */
        var acroShort2LongName: HashMap<String, String>? = null

        init {
            inverseSearch = HashMap<String, InverseStore>()
            acroShort2LongName = HashMap<String, String>()
            for (string in items) {
                val itemName = string
                val itemShort = XfaForm.Xml2Som.getShortName(itemName)
                acroShort2LongName!!.put(itemShort, itemName)
                XfaForm.Xml2Som.inverseSearchAdd(inverseSearch, XfaForm.Xml2Som.splitParts(itemShort), itemName)
            }
        }
    }

    /**
     * Processes the template section in the XFA form.
     */
    class Xml2SomTemplate
    /**
     * Creates a new instance from the datasets node.
     * @param n the template node
     */
    (n: Node) : Xml2Som() {
        /**
         * true if it's a dynamic form; false
         * if it's a static form.
         * @return true if it's a dynamic form; false
         * * if it's a static form
         */
        /**
         * Sets the dynamic form flag. It doesn't change the template.
         * @param dynamicForm the dynamic form flag
         */
        var isDynamicForm: Boolean = false
        private var templateLevel: Int = 0

        init {
            order = ArrayList<String>()
            name2Node = HashMap<String, Node>()
            stack = Stack2<String>()
            anform = 0
            templateLevel = 0
            inverseSearch = HashMap<String, InverseStore>()
            processTemplate(n, null)
        }

        /**
         * Gets the field type as described in the template section of the XFA.
         * @param s the exact template name
         * *
         * @return the field type or null if not found
         */
        fun getFieldType(s: String): String? {
            val n = name2Node[s] ?: return null
            if ("exclGroup" == n.localName)
                return "exclGroup"
            var ui: Node? = n.firstChild
            while (ui != null) {
                if (ui.nodeType == Node.ELEMENT_NODE && "ui" == ui.localName) {
                    break
                }
                ui = ui.nextSibling
            }
            if (ui == null)
                return null
            var type: Node? = ui.firstChild
            while (type != null) {
                if (type.nodeType == Node.ELEMENT_NODE && !("extras" == type.localName && "picture" == type.localName)) {
                    return type.localName
                }
                type = type.nextSibling
            }
            return null
        }

        private fun processTemplate(n: Node, ff: HashMap<String, Int>?) {
            var ff = ff
            if (ff == null)
                ff = HashMap<String, Int>()
            val ss = HashMap<String, Int>()
            var n2: Node? = n.firstChild
            while (n2 != null) {
                if (n2.nodeType == Node.ELEMENT_NODE) {
                    val s = n2.localName
                    if ("subform" == s) {
                        val name = n2.attributes.getNamedItem("name")
                        var nn = "#subform"
                        var annon = true
                        if (name != null) {
                            nn = XfaForm.Xml2Som.escapeSom(name.nodeValue)
                            annon = false
                        }
                        var i: Int?
                        if (annon) {
                            i = Integer.valueOf(anform)
                            ++anform
                        } else {
                            i = ss[nn]
                            if (i == null)
                                i = Integer.valueOf(0)
                            else
                                i = Integer.valueOf(i.toInt() + 1)
                            ss.put(nn, i)
                        }
                        stack.push(nn + "[" + i!!.toString() + "]")
                        ++templateLevel
                        if (annon)
                            processTemplate(n2, ff)
                        else
                            processTemplate(n2, null)
                        --templateLevel
                        stack.pop()
                    } else if ("field" == s || "exclGroup" == s) {
                        val name = n2.attributes.getNamedItem("name")
                        if (name != null) {
                            val nn = XfaForm.Xml2Som.escapeSom(name.nodeValue)
                            var i: Int? = ff[nn]
                            if (i == null)
                                i = Integer.valueOf(0)
                            else
                                i = Integer.valueOf(i.toInt() + 1)
                            ff.put(nn, i)
                            stack.push(nn + "[" + i!!.toString() + "]")
                            val unstack = printStack()
                            order.add(unstack)
                            inverseSearchAdd(unstack)
                            name2Node.put(unstack, n2)
                            stack.pop()
                        }
                    } else if (!isDynamicForm && templateLevel > 0 && "occur" == s) {
                        var initial = 1
                        var min = 1
                        var max = 1
                        var a: Node? = n2.attributes.getNamedItem("initial")
                        if (a != null)
                            try {
                                initial = Integer.parseInt(a.nodeValue.trim { it <= ' ' })
                            } catch (e: Exception) {
                            }

                        a = n2.attributes.getNamedItem("min")
                        if (a != null)
                            try {
                                min = Integer.parseInt(a.nodeValue.trim { it <= ' ' })
                            } catch (e: Exception) {
                            }

                        a = n2.attributes.getNamedItem("max")
                        if (a != null)
                            try {
                                max = Integer.parseInt(a.nodeValue.trim { it <= ' ' })
                            } catch (e: Exception) {
                            }

                        if (initial != min || min != max)
                            isDynamicForm = true
                    }
                }
                n2 = n2.nextSibling
            }
        }
    }

    @Throws(IOException::class)
    @JvmOverloads fun fillXfaForm(file: File, readOnly: Boolean = false) {
        fillXfaForm(FileInputStream(file), readOnly)
    }

    @Throws(IOException::class)
    @JvmOverloads fun fillXfaForm(`is`: InputStream, readOnly: Boolean = false) {
        fillXfaForm(InputSource(`is`), readOnly)
    }

    @Throws(IOException::class)
    @JvmOverloads fun fillXfaForm(`is`: InputSource, readOnly: Boolean = false) {
        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            val newdoc = db.parse(`is`)
            fillXfaForm(newdoc.documentElement, readOnly)
        } catch (e: ParserConfigurationException) {
            throw ExceptionConverter(e)
        } catch (e: SAXException) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Replaces the data under datasets/data.
     * @since    iText 5.0.0
     */
    @JvmOverloads fun fillXfaForm(node: Node, readOnly: Boolean = false) {
        if (readOnly) {
            val nodeList = domDocument!!.getElementsByTagName("field")
            for (i in 0..nodeList.length - 1) {
                (nodeList.item(i) as Element).setAttribute("access", "readOnly")
            }
        }
        val allChilds = datasetsNode!!.childNodes
        val len = allChilds.length
        var data: Node? = null
        for (k in 0..len - 1) {
            val n = allChilds.item(k)
            if (n.nodeType == Node.ELEMENT_NODE && n.localName == "data" && XFA_DATA_SCHEMA == n.namespaceURI) {
                data = n
                break
            }
        }
        if (data == null) {
            data = datasetsNode!!.ownerDocument.createElementNS(XFA_DATA_SCHEMA, "xfa:data")
            datasetsNode!!.appendChild(data)
        }
        val list = data!!.childNodes
        if (list.length == 0) {
            data.appendChild(domDocument!!.importNode(node, true))
        } else {
            // There's a possibility that first child node of XFA data is not an ELEMENT but simply a TEXT. In this case data will be duplicated.
            //			data.replaceChild(domDocument.importNode(node, true), data.getFirstChild());
            val firstNode = getFirstElementNode(data)
            if (firstNode != null)
                data.replaceChild(domDocument!!.importNode(node, true), firstNode)
        }
        extractNodes()
        isChanged = true
    }

    private fun getFirstElementNode(src: Node): Node? {
        var result: Node? = null
        val list = src.childNodes
        for (i in 0..list.length - 1) {
            if (list.item(i).nodeType == Node.ELEMENT_NODE) {
                result = list.item(i)
                break
            }
        }
        return result
    }

    companion object {
        val XFA_DATA_SCHEMA = "http://www.xfa.org/schema/xfa-data/1.0/"

        /**
         * Return the XFA Object, could be an array, could be a Stream.
         * Returns null f no XFA Object is present.
         * @param    reader    a PdfReader instance
         * *
         * @return    the XFA object
         * *
         * @since    2.1.3
         */
        fun getXfaObject(reader: PdfReader): PdfObject? {
            val af = PdfReader.getPdfObjectRelease(reader.catalog.get(PdfName.ACROFORM)) as PdfDictionary? ?: return null
            return PdfReader.getPdfObjectRelease(af.get(PdfName.XFA))
        }

        fun extractXFANodes(domDocument: Document): Map<String, Node> {
            val xfaNodes = HashMap<String, Node>()
            var n: Node? = domDocument.firstChild
            while (n!!.childNodes.length == 0) {
                n = n.nextSibling
            }
            n = n.firstChild
            while (n != null) {
                if (n.nodeType == Node.ELEMENT_NODE) {
                    val s = n.localName
                    xfaNodes.put(s, n)
                }
                n = n.nextSibling
            }

            return xfaNodes
        }

        /**
         * Sets the XFA key from a byte array. The old XFA is erased.
         * @param form the data
         * *
         * @param reader the reader
         * *
         * @param writer the writer
         * *
         * @throws java.io.IOException on error
         */
        @Throws(IOException::class)
        fun setXfa(form: XfaForm, reader: PdfReader, writer: PdfWriter) {
            val af = PdfReader.getPdfObjectRelease(reader.catalog.get(PdfName.ACROFORM)) as PdfDictionary? ?: return
            val xfa = getXfaObject(reader)
            if (xfa.isArray) {
                val ar = xfa as PdfArray
                var t = -1
                var d = -1
                var k = 0
                while (k < ar.size()) {
                    val s = ar.getAsString(k)
                    if ("template" == s.toString()) {
                        t = k + 1
                    }
                    if ("datasets" == s.toString()) {
                        d = k + 1
                    }
                    k += 2
                }
                if (t > -1 && d > -1) {
                    reader.killXref(ar.getAsIndirectObject(t))
                    reader.killXref(ar.getAsIndirectObject(d))
                    val tStream = PdfStream(serializeDoc(form.templateNode))
                    tStream.flateCompress(writer.compressionLevel)
                    ar.set(t, writer.addToBody(tStream).indirectReference)
                    val dStream = PdfStream(serializeDoc(form.datasetsNode))
                    dStream.flateCompress(writer.compressionLevel)
                    ar.set(d, writer.addToBody(dStream).indirectReference)
                    af.put(PdfName.XFA, PdfArray(ar))
                    return
                }
            }
            reader.killXref(af.get(PdfName.XFA))
            val str = PdfStream(serializeDoc(form.domDocument))
            str.flateCompress(writer.compressionLevel)
            val ref = writer.addToBody(str).indirectReference
            af.put(PdfName.XFA, ref)
        }

        /**
         * Serializes a XML document to a byte array.
         * @param n the XML document
         * *
         * @throws java.io.IOException on error
         * *
         * @return the serialized XML document
         */
        @Throws(IOException::class)
        fun serializeDoc(n: Node): ByteArray {
            val xw = XmlDomWriter()
            val fout = ByteArrayOutputStream()
            xw.setOutput(fout, null)
            xw.setCanonical(false)
            xw.write(n)
            fout.close()
            return fout.toByteArray()
        }

        /**
         * Gets all the text contained in the child nodes of this node.
         * @param n the Node
         * *
         * @return the text found or "" if no text was found
         */
        fun getNodeText(n: Node?): String {
            if (n == null)
                return ""
            return getNodeText(n, "")

        }

        private fun getNodeText(n: Node, name: String): String {
            var name = name
            var n2: Node? = n.firstChild
            while (n2 != null) {
                if (n2.nodeType == Node.ELEMENT_NODE) {
                    name = getNodeText(n2, name)
                } else if (n2.nodeType == Node.TEXT_NODE) {
                    name += n2.nodeValue
                }
                n2 = n2.nextSibling
            }
            return name
        }
    }


}
