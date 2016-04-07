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

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.options.PropertyOptions


/**
 * A node in the internally XMP tree, which can be a schema node, a property node, an array node,
 * an array item, a struct node or a qualifier node (without '?').

 * Possible improvements:

 * 1. The kind Node of node might be better represented by a class-hierarchy of different nodes.
 * 2. The array type should be an enum
 * 3. isImplicitNode should be removed completely and replaced by return values of fi.
 * 4. hasLanguage, hasType should be automatically maintained by XMPNode

 * @since 21.02.2006
 */
internal class XMPNode
/**
 * Creates an `XMPNode` with initial values.

 * @param name the name of the node
 * *
 * @param value the value of the node
 * *
 * @param options the options of the node
 */
(
        /** name of the node, contains different information depending of the node kind  */
        /**
         * @return Returns the name.
         */
        /**
         * @param name The name to set.
         */
        var name: String?,
        /** value of the node, contains different information depending of the node kind  */
        /**
         * @return Returns the value.
         */
        /**
         * @param value The value to set.
         */
        var value: String?, options: PropertyOptions) : Comparable<Any> {
    /** link to the parent node  */
    /**
     * @return Returns the parent node.
     */
    /**
     * Sets the parent node, this is solely done by `addChild(...)`
     * and `addQualifier()`.

     * @param parent
     * *            Sets the parent node.
     */
    var parent: XMPNode? = null
        protected set(parent) {
            this.parent = parent
        }
    /** list of child nodes, lazy initialized  */
    private var children: List<Any>? = null
    /** list of qualifier of the node, lazy initialized  */
    private var qualifier: List<Any>? = null
    /** options describing the kind of the node  */
    /**
     * @return Returns the options.
     */
    /**
     * Updates the options of the node.
     * @param options the options to set.
     */
    var options: PropertyOptions? = null
        get() {
            if (options == null) {
                options = PropertyOptions()
            }
            return options
        }

    // internal processing options

    /** flag if the node is implicitly created  */
    /**
     * @return Returns the implicit flag
     */
    /**
     * @param implicit Sets the implicit node flag
     */
    var isImplicit: Boolean = false
    /** flag if the node has aliases  */
    /**
     * @return Returns if the node contains aliases (applies only to schema nodes)
     */
    /**
     * @param hasAliases sets the flag that the node contains aliases
     */
    var hasAliases: Boolean = false
    /** flag if the node is an alias  */
    /**
     * @return Returns if the node contains aliases (applies only to schema nodes)
     */
    /**
     * @param alias sets the flag that the node is an alias
     */
    var isAlias: Boolean = false
    /** flag if the node has an "rdf:value" child node.  */
    /**
     * @return the hasValueChild
     */
    /**
     * @param hasValueChild the hasValueChild to set
     */
    var hasValueChild: Boolean = false


    init {
        this.options = options
    }


    /**
     * Constructor for the node without value.

     * @param name the name of the node
     * *
     * @param options the options of the node
     */
    constructor(name: String, options: PropertyOptions) : this(name, null, options) {
    }


    /**
     * Resets the node.
     */
    fun clear() {
        options = null
        name = null
        value = null
        children = null
        qualifier = null
    }


    /**
     * @param index an index [1..size]
     * *
     * @return Returns the child with the requested index.
     */
    fun getChild(index: Int): XMPNode {
        return getChildren()[index - 1] as XMPNode
    }


    /**
     * Adds a node as child to this node.
     * @param node an XMPNode
     * *
     * @throws XMPException
     */
    @Throws(XMPException::class)
    fun addChild(node: XMPNode) {
        // check for duplicate properties
        assertChildNotExisting(node.name)
        node.parent = this
        getChildren().add(node)
    }


    /**
     * Adds a node as child to this node.
     * @param index the index of the node *before* which the new one is inserted.
     * * *Note:* The node children are indexed from [1..size]!
     * * An index of size + 1 appends a node.
     * *
     * @param node an XMPNode
     * *
     * @throws XMPException
     */
    @Throws(XMPException::class)
    fun addChild(index: Int, node: XMPNode) {
        assertChildNotExisting(node.name)
        node.parent = this
        getChildren().add(index - 1, node)
    }


    /**
     * Replaces a node with another one.
     * @param index the index of the node that will be replaced.
     * * *Note:* The node children are indexed from [1..size]!
     * *
     * @param node the replacement XMPNode
     */
    fun replaceChild(index: Int, node: XMPNode) {
        node.parent = this
        getChildren()[index - 1] = node
    }


    /**
     * Removes a child at the requested index.
     * @param itemIndex the index to remove [1..size]
     */
    fun removeChild(itemIndex: Int) {
        getChildren().removeAt(itemIndex - 1)
        cleanupChildren()
    }


    /**
     * Removes a child node.
     * If its a schema node and doesn't have any children anymore, its deleted.

     * @param node the child node to delete.
     */
    fun removeChild(node: XMPNode) {
        getChildren().remove(node)
        cleanupChildren()
    }


    /**
     * Removes the children list if this node has no children anymore;
     * checks if the provided node is a schema node and doesn't have any children anymore,
     * its deleted.
     */
    protected fun cleanupChildren() {
        if (children!!.isEmpty()) {
            children = null
        }
    }


    /**
     * Removes all children from the node.
     */
    fun removeChildren() {
        children = null
    }


    /**
     * @return Returns the number of children without neccessarily creating a list.
     */
    val childrenLength: Int
        get() = if (children != null)
            children!!.size
        else
            0


    /**
     * @param expr child node name to look for
     * *
     * @return Returns an `XMPNode` if node has been found, `null` otherwise.
     */
    fun findChildByName(expr: String): XMPNode? {
        return find(getChildren(), expr)
    }


    /**
     * @param index an index [1..size]
     * *
     * @return Returns the qualifier with the requested index.
     */
    fun getQualifier(index: Int): XMPNode {
        return getQualifier()[index - 1] as XMPNode
    }


    /**
     * @return Returns the number of qualifier without neccessarily creating a list.
     */
    val qualifierLength: Int
        get() = if (qualifier != null)
            qualifier!!.size
        else
            0


    /**
     * Appends a qualifier to the qualifier list and sets respective options.
     * @param qualNode a qualifier node.
     * *
     * @throws XMPException
     */
    @Throws(XMPException::class)
    fun addQualifier(qualNode: XMPNode) {
        assertQualifierNotExisting(qualNode.name)
        qualNode.parent = this
        qualNode.options.setQualifier(true)
        options.setHasQualifiers(true)

        // contraints
        if (qualNode.isLanguageNode) {
            // "xml:lang" is always first and the option "hasLanguage" is set
            options!!.hasLanguage = true
            getQualifier().add(0, qualNode)
        } else if (qualNode.isTypeNode) {
            // "rdf:type" must be first or second after "xml:lang" and the option "hasType" is set
            options!!.hasType = true
            getQualifier().add(
                    if (!options!!.hasLanguage) 0 else 1,
                    qualNode)
        } else {
            // other qualifiers are appended
            getQualifier().add(qualNode)
        }
    }


    /**
     * Removes one qualifier node and fixes the options.
     * @param qualNode qualifier to remove
     */
    fun removeQualifier(qualNode: XMPNode) {
        val opts = options
        if (qualNode.isLanguageNode) {
            // if "xml:lang" is removed, remove hasLanguage-flag too
            opts.setHasLanguage(false)
        } else if (qualNode.isTypeNode) {
            // if "rdf:type" is removed, remove hasType-flag too
            opts.setHasType(false)
        }

        getQualifier().remove(qualNode)
        if (qualifier!!.isEmpty()) {
            opts.setHasQualifiers(false)
            qualifier = null
        }

    }


    /**
     * Removes all qualifiers from the node and sets the options appropriate.
     */
    fun removeQualifiers() {
        val opts = options
        // clear qualifier related options
        opts.setHasQualifiers(false)
        opts.setHasLanguage(false)
        opts.setHasType(false)
        qualifier = null
    }


    /**
     * @param expr qualifier node name to look for
     * *
     * @return Returns a qualifier `XMPNode` if node has been found,
     * * `null` otherwise.
     */
    fun findQualifierByName(expr: String): XMPNode? {
        return find(qualifier, expr)
    }


    /**
     * @return Returns whether the node has children.
     */
    fun hasChildren(): Boolean {
        return children != null && children!!.size > 0
    }


    /**
     * @return Returns an iterator for the children.
     * * *Note:* take care to use it.remove(), as the flag are not adjusted in that case.
     */
    fun iterateChildren(): Iterator<Any> {
        if (children != null) {
            return getChildren().iterator()
        } else {
            return Collections.EMPTY_LIST.listIterator()
        }
    }


    /**
     * @return Returns whether the node has qualifier attached.
     */
    fun hasQualifier(): Boolean {
        return qualifier != null && qualifier!!.size > 0
    }


    /**
     * @return Returns an iterator for the qualifier.
     * * *Note:* take care to use it.remove(), as the flag are not adjusted in that case.
     */
    fun iterateQualifier(): Iterator<Any> {
        if (qualifier != null) {
            val it = getQualifier().iterator()

            return object : Iterator {
                override operator fun hasNext(): Boolean {
                    return it.hasNext()
                }

                override operator fun next(): Any {
                    return it.next()
                }

                override fun remove() {
                    throw UnsupportedOperationException(
                            "remove() is not allowed due to the internal contraints")
                }

            }
        } else {
            return Collections.EMPTY_LIST.iterator()
        }
    }


    /**
     * Performs a **deep clone** of the node and the complete subtree.

     * @see java.lang.Object.clone
     */
    override fun clone(): Any {
        val newOptions: PropertyOptions
        try {
            newOptions = PropertyOptions(options.getOptions())
        } catch (e: XMPException) {
            // cannot happen
            newOptions = PropertyOptions()
        }

        val newNode = XMPNode(name, value, newOptions)
        cloneSubtree(newNode)

        return newNode
    }


    /**
     * Performs a **deep clone** of the complete subtree (children and
     * qualifier )into and add it to the destination node.

     * @param destination the node to add the cloned subtree
     */
    fun cloneSubtree(destination: XMPNode) {
        try {
            run {
                val it = iterateChildren()
                while (it.hasNext()) {
                    val child = it.next() as XMPNode
                    destination.addChild(child.clone() as XMPNode)
                }
            }

            val it = iterateQualifier()
            while (it.hasNext()) {
                val qualifier = it.next() as XMPNode
                destination.addQualifier(qualifier.clone() as XMPNode)
            }
        } catch (e: XMPException) {
            // cannot happen (duplicate childs/quals do not exist in this node)
            assert(false)
        }

    }


    /**
     * Renders this node and the tree unter this node in a human readable form.
     * @param recursive Flag is qualifier and child nodes shall be rendered too
     * *
     * @return Returns a multiline string containing the dump.
     */
    fun dumpNode(recursive: Boolean): String {
        val result = StringBuffer(512)
        this.dumpNode(result, recursive, 0, 0)
        return result.toString()
    }


    /**
     * @see Comparable.compareTo
     */
    override fun compareTo(xmpNode: Any): Int {
        if (options.isSchemaNode()) {
            return this.value!!.compareTo((xmpNode as XMPNode).value)
        } else {
            return this.name!!.compareTo((xmpNode as XMPNode).name)
        }
    }


    /**
     * Sorts the complete datamodel according to the following rules:
     *
     *  * Nodes at one level are sorted by name, that is prefix + local name
     *  * Starting at the root node the children and qualifier are sorted recursively,
     * which the following exceptions.
     *  * Sorting will not be used for arrays.
     *  * Within qualifier "xml:lang" and/or "rdf:type" stay at the top in that order,
     * all others are sorted.
     *
     */
    fun sort() {
        // sort qualifier
        if (hasQualifier()) {
            val quals = getQualifier().toArray(arrayOfNulls<XMPNode>(qualifierLength)) as Array<XMPNode>
            var sortFrom = 0
            while (quals.size > sortFrom && (XMPConst.XML_LANG == quals[sortFrom].name || "rdf:type" == quals[sortFrom].name)) {
                quals[sortFrom].sort()
                sortFrom++
            }

            Arrays.sort(quals, sortFrom, quals.size)
            val it = qualifier!!.listIterator()
            for (j in quals.indices) {
                it.next()
                it.set(quals[j])
                quals[j].sort()
            }
        }

        // sort children
        if (hasChildren()) {
            if (!options.isArray()) {
                Collections.sort(children)
            }
            val it = iterateChildren()
            while (it.hasNext()) {
                (it.next() as XMPNode).sort()

            }
        }
    }



    //------------------------------------------------------------------------------ private methods


    /**
     * Dumps this node and its qualifier and children recursively.
     * *Note:* It creats empty options on every node.

     * @param result the buffer to append the dump.
     * *
     * @param recursive Flag is qualifier and child nodes shall be rendered too
     * *
     * @param indent the current indent level.
     * *
     * @param index the index within the parent node (important for arrays)
     */
    private fun dumpNode(result: StringBuffer, recursive: Boolean, indent: Int, index: Int) {
        // write indent
        for (i in 0..indent - 1) {
            result.append('\t')
        }

        // render Node
        if (parent != null) {
            if (options.isQualifier()) {
                result.append('?')
                result.append(name)
            } else if (parent.options.isArray()) {
                result.append('[')
                result.append(index)
                result.append(']')
            } else {
                result.append(name)
            }
        } else {
            // applies only to the root node
            result.append("ROOT NODE")
            if (name != null && name!!.length > 0) {
                // the "about" attribute
                result.append(" (")
                result.append(name)
                result.append(')')
            }
        }

        if (value != null && value!!.length > 0) {
            result.append(" = \"")
            result.append(value)
            result.append('"')
        }

        // render options if at least one is set
        if (options.containsOneOf(0xffffffff.toInt())) {
            result.append("\t(")
            result.append(options.toString())
            result.append(" : ")
            result.append(options.getOptionsString())
            result.append(')')
        }

        result.append('\n')

        // render qualifier
        if (recursive && hasQualifier()) {
            val quals = getQualifier().toArray(arrayOfNulls<XMPNode>(qualifierLength)) as Array<XMPNode>
            var i = 0
            while (quals.size > i && (XMPConst.XML_LANG == quals[i].name || "rdf:type" == quals[i].name)) {
                i++
            }
            Arrays.sort(quals, i, quals.size)
            i = 0
            while (i < quals.size) {
                val qualifier = quals[i]
                qualifier.dumpNode(result, recursive, indent + 2, i + 1)
                i++
            }
        }

        // render children
        if (recursive && hasChildren()) {
            val children = getChildren().toArray(arrayOfNulls<XMPNode>(childrenLength)) as Array<XMPNode>
            if (!options.isArray()) {
                Arrays.sort(children)
            }
            for (i in children.indices) {
                val child = children[i]
                child.dumpNode(result, recursive, indent + 1, i + 1)
            }
        }
    }


    /**
     * @return Returns whether this node is a language qualifier.
     */
    private val isLanguageNode: Boolean
        get() = XMPConst.XML_LANG == name


    /**
     * @return Returns whether this node is a type qualifier.
     */
    private val isTypeNode: Boolean
        get() = "rdf:type" == name


    /**
     * *Note:* This method should always be called when accessing 'children' to be sure
     * that its initialized.
     * @return Returns list of children that is lazy initialized.
     */
    private fun getChildren(): MutableList<Any> {
        if (children == null) {
            children = ArrayList(0)
        }
        return children
    }


    /**
     * @return Returns a read-only copy of child nodes list.
     */
    val unmodifiableChildren: List<Any>
        get() = Collections.unmodifiableList(ArrayList(getChildren()))


    /**
     * @return Returns list of qualifier that is lazy initialized.
     */
    private fun getQualifier(): MutableList<Any> {
        if (qualifier == null) {
            qualifier = ArrayList(0)
        }
        return qualifier
    }


    /**
     * Internal find.
     * @param list the list to search in
     * *
     * @param expr the search expression
     * *
     * @return Returns the found node or `nulls`.
     */
    private fun find(list: List<Any>?, expr: String): XMPNode? {

        if (list != null) {
            val it = list.iterator()
            while (it.hasNext()) {
                val child = it.next() as XMPNode
                if (child.name == expr) {
                    return child
                }
            }
        }
        return null
    }


    /**
     * Checks that a node name is not existing on the same level, except for array items.
     * @param childName the node name to check
     * *
     * @throws XMPException Thrown if a node with the same name is existing.
     */
    @Throws(XMPException::class)
    private fun assertChildNotExisting(childName: String) {
        if (XMPConst.ARRAY_ITEM_NAME != childName && findChildByName(childName) != null) {
            throw XMPException("Duplicate property or field node '$childName'",
                    XMPError.BADXMP)
        }
    }


    /**
     * Checks that a qualifier name is not existing on the same level.
     * @param qualifierName the new qualifier name
     * *
     * @throws XMPException Thrown if a node with the same name is existing.
     */
    @Throws(XMPException::class)
    private fun assertQualifierNotExisting(qualifierName: String) {
        if (XMPConst.ARRAY_ITEM_NAME != qualifierName && findQualifierByName(qualifierName) != null) {
            throw XMPException("Duplicate '$qualifierName' qualifier", XMPError.BADXMP)
        }
    }
}