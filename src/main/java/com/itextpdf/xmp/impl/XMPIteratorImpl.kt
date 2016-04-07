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

import java.util.Collections
import java.util.NoSuchElementException

import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPIterator
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.impl.xpath.XMPPath
import com.itextpdf.xmp.impl.xpath.XMPPathParser
import com.itextpdf.xmp.options.IteratorOptions
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.properties.XMPPropertyInfo


/**
 * The `XMPIterator` implementation.
 * Iterates the XMP Tree according to a set of options.
 * During the iteration the XMPMeta-object must not be changed.
 * Calls to `skipSubtree()` / `skipSiblings()` will affect the iteration.

 * @since   29.06.2006
 */
class XMPIteratorImpl
/**
 * Constructor with optionsl initial values. If `propName` is provided,
 * `schemaNS` has also be provided.
 * @param xmp the iterated metadata object.
 * *
 * @param schemaNS the iteration is reduced to this schema (optional)
 * *
 * @param propPath the iteration is redurce to this property within the `schemaNS`
 * *
 * @param options advanced iteration options, see [IteratorOptions]
 * *
 * @throws XMPException If the node defined by the paramters is not existing.
 */
@Throws(XMPException::class)
constructor(xmp: XMPMetaImpl, schemaNS: String?, propPath: String?,
            options: IteratorOptions?) : XMPIterator {
    /** stores the iterator options  */
    /**
     * @return Exposes the options for inner class.
     */
    protected val options: IteratorOptions
    /** the base namespace of the property path, will be changed during the iteration  */
    /**
     * @return Exposes the options for inner class.
     */
    /**
     * @param baseNS sets the baseNS from the inner class.
     */
    protected var baseNS: String? = null
    /** flag to indicate that skipSiblings() has been called.  */
    protected var skipSiblings = false
    /** flag to indicate that skipSiblings() has been called.  */
    protected var skipSubtree = false
    /** the node iterator doing the work  */
    private var nodeIterator: Iterator<Any>? = null


    init {
        // make sure that options is defined at least with defaults
        this.options = options ?: IteratorOptions()

        // the start node of the iteration depending on the schema and property filter
        var startNode: XMPNode? = null
        var initialPath: String? = null
        val baseSchema = schemaNS != null && schemaNS.length > 0
        val baseProperty = propPath != null && propPath.length > 0

        if (!baseSchema && !baseProperty) {
            // complete tree will be iterated
            startNode = xmp.root
        } else if (baseSchema && baseProperty) {
            // Schema and property node provided
            val path = XMPPathParser.expandXPath(schemaNS, propPath)

            // base path is the prop path without the property leaf
            val basePath = XMPPath()
            for (i in 0..path.size() - 1 - 1) {
                basePath.add(path.getSegment(i))
            }

            startNode = XMPNodeUtils.findNode(xmp.root, path, false, null)
            baseNS = schemaNS
            initialPath = basePath.toString()
        } else if (baseSchema && !baseProperty) {
            // Only Schema provided
            startNode = XMPNodeUtils.findSchemaNode(xmp.root, schemaNS, false)
        } else
        // !baseSchema  &&  baseProperty
        {
            // No schema but property provided -> error
            throw XMPException("Schema namespace URI is required", XMPError.BADSCHEMA)
        }


        // create iterator
        if (startNode != null) {
            if (!this.options.isJustChildren) {
                nodeIterator = NodeIterator(startNode, initialPath, 1)
            } else {
                nodeIterator = NodeIteratorChildren(startNode, initialPath)
            }
        } else {
            // create null iterator
            nodeIterator = Collections.EMPTY_LIST.iterator()
        }
    }


    /**
     * @see XMPIterator.skipSubtree
     */
    override fun skipSubtree() {
        this.skipSubtree = true
    }


    /**
     * @see XMPIterator.skipSiblings
     */
    override fun skipSiblings() {
        skipSubtree()
        this.skipSiblings = true
    }


    /**
     * @see java.util.Iterator.hasNext
     */
    override fun hasNext(): Boolean {
        return nodeIterator!!.hasNext()
    }


    /**
     * @see java.util.Iterator.next
     */
    override fun next(): Any {
        return nodeIterator!!.next()
    }


    /**
     * @see java.util.Iterator.remove
     */
    override fun remove() {
        throw UnsupportedOperationException("The XMPIterator does not support remove().")
    }


    /**
     * The `XMPIterator` implementation.
     * It first returns the node itself, then recursivly the children and qualifier of the node.

     * @since   29.06.2006
     */
    private open inner class NodeIterator : Iterator<Any> {

        /** the state of the iteration  */
        private var state = ITERATE_NODE
        /** the currently visited node  */
        private val visitedNode: XMPNode
        /** the recursively accumulated path  */
        private val path: String
        /** the iterator that goes through the children and qualifier list  */
        /**
         * @return the childrenIterator
         */
        /**
         * @param childrenIterator the childrenIterator to set
         */
        protected var childrenIterator: Iterator<Any>? = null
        /** index of node with parent, only interesting for arrays  */
        private var index = 0
        /** the iterator for each child  */
        private var subIterator: Iterator<Any> = Collections.EMPTY_LIST.iterator()
        /** the cached `PropertyInfo` to return  */
        /**
         * @return Returns the returnProperty.
         */
        /**
         * @param returnProperty the returnProperty to set
         */
        protected var returnProperty: XMPPropertyInfo? = null


        /**
         * Default constructor
         */
        constructor() {
            // EMPTY
        }


        /**
         * Constructor for the node iterator.
         * @param visitedNode the currently visited node
         * *
         * @param parentPath the accumulated path of the node
         * *
         * @param index the index within the parent node (only for arrays)
         */
        constructor(visitedNode: XMPNode, parentPath: String, index: Int) {
            this.visitedNode = visitedNode
            this.state = NodeIterator.ITERATE_NODE
            if (visitedNode.options.isSchemaNode) {
                baseNS = visitedNode.name
            }

            // for all but the root node and schema nodes
            path = accumulatePath(visitedNode, parentPath, index)
        }


        /**
         * Prepares the next node to return if not already done.

         * @see Iterator.hasNext
         */
        override fun hasNext(): Boolean {
            if (returnProperty != null) {
                // hasNext has been called before
                return true
            }

            // find next node
            if (state == ITERATE_NODE) {
                return reportNode()
            } else if (state == ITERATE_CHILDREN) {
                if (childrenIterator == null) {
                    childrenIterator = visitedNode.iterateChildren()
                }

                var hasNext = iterateChildren(childrenIterator)

                if (!hasNext && visitedNode.hasQualifier() && !options.isOmitQualifiers) {
                    state = ITERATE_QUALIFIER
                    childrenIterator = null
                    hasNext = hasNext()
                }
                return hasNext
            } else {
                if (childrenIterator == null) {
                    childrenIterator = visitedNode.iterateQualifier()
                }

                return iterateChildren(childrenIterator)
            }
        }


        /**
         * Sets the returnProperty as next item or recurses into `hasNext()`.
         * @return Returns if there is a next item to return.
         */
        protected fun reportNode(): Boolean {
            state = ITERATE_CHILDREN
            if (visitedNode.parent != null && (!options.isJustLeafnodes || !visitedNode.hasChildren())) {
                returnProperty = createPropertyInfo(visitedNode, baseNS, path)
                return true
            } else {
                return hasNext()
            }
        }


        /**
         * Handles the iteration of the children or qualfier
         * @param iterator an iterator
         * *
         * @return Returns if there are more elements available.
         */
        private fun iterateChildren(iterator: Iterator<Any>): Boolean {
            if (skipSiblings) {
                // setSkipSiblings(false);
                skipSiblings = false
                subIterator = Collections.EMPTY_LIST.iterator()
            }

            // create sub iterator for every child,
            // if its the first child visited or the former child is finished 
            if (!subIterator.hasNext() && iterator.hasNext()) {
                val child = iterator.next() as XMPNode
                index++
                subIterator = NodeIterator(child, path, index)
            }

            if (subIterator.hasNext()) {
                returnProperty = subIterator.next() as XMPPropertyInfo
                return true
            } else {
                return false
            }
        }


        /**
         * Calls hasNext() and returnes the prepared node. Afterwards its set to null.
         * The existance of returnProperty indicates if there is a next node, otherwise
         * an exceptio is thrown.

         * @see Iterator.next
         */
        override fun next(): Any {
            if (hasNext()) {
                val result = returnProperty
                returnProperty = null
                return result
            } else {
                throw NoSuchElementException("There are no more nodes to return")
            }
        }


        /**
         * Not supported.
         * @see Iterator.remove
         */
        override fun remove() {
            throw UnsupportedOperationException()
        }


        /**
         * @param currNode the node that will be added to the path.
         * *
         * @param parentPath the path up to this node.
         * *
         * @param currentIndex the current array index if an arrey is traversed
         * *
         * @return Returns the updated path.
         */
        protected fun accumulatePath(currNode: XMPNode, parentPath: String?, currentIndex: Int): String? {
            val separator: String
            val segmentName: String
            if (currNode.parent == null || currNode.options.isSchemaNode) {
                return null
            } else if (currNode.parent.options.isArray) {
                separator = ""
                segmentName = "[" + currentIndex.toString() + "]"
            } else {
                separator = "/"
                segmentName = currNode.name
            }


            if (parentPath == null || parentPath.length == 0) {
                return segmentName
            } else if (options.isJustLeafname) {
                return if (!segmentName.startsWith("?"))
                    segmentName
                else
                    segmentName.substring(1) // qualifier
            } else {
                return parentPath + separator + segmentName
            }
        }


        /**
         * Creates a property info object from an `XMPNode`.
         * @param node an `XMPNode`
         * *
         * @param baseNS the base namespace to report
         * *
         * @param path the full property path
         * *
         * @return Returns a `XMPProperty`-object that serves representation of the node.
         */
        protected fun createPropertyInfo(node: XMPNode, baseNS: String,
                                         path: String): XMPPropertyInfo {
            val value = if (node.options.isSchemaNode) null else node.value

            return object : XMPPropertyInfo {
                override fun getNamespace(): String {
                    if (!node.options.isSchemaNode) {
                        // determine namespace of leaf node
                        val qname = QName(node.name)
                        return XMPMetaFactory.getSchemaRegistry().getNamespaceURI(qname.prefix)
                    } else {
                        return baseNS
                    }
                }

                override fun getPath(): String {
                    return path
                }

                override fun getValue(): String {
                    return value
                }

                override fun getOptions(): PropertyOptions {
                    return node.options
                }

                override fun getLanguage(): String? {
                    // the language is not reported
                    return null
                }
            }
        }

        companion object {
            /** iteration state  */
            protected val ITERATE_NODE = 0
            /** iteration state  */
            protected val ITERATE_CHILDREN = 1
            /** iteration state  */
            protected val ITERATE_QUALIFIER = 2
        }
    }


    /**
     * This iterator is derived from the default `NodeIterator`,
     * and is only used for the option [IteratorOptions.JUST_CHILDREN].

     * @since 02.10.2006
     */
    private inner class NodeIteratorChildren
    /**
     * Constructor
     * @param parentNode the node which children shall be iterated.
     * *
     * @param parentPath the full path of the former node without the leaf node.
     */
    (parentNode: XMPNode, parentPath: String) : NodeIterator() {
        /**  */
        private val parentPath: String
        /**  */
        private val childrenIterator: Iterator<Any>
        /**  */
        private var index = 0


        init {
            if (parentNode.options.isSchemaNode) {
                baseNS = parentNode.name
            }
            this.parentPath = accumulatePath(parentNode, parentPath, 1)

            childrenIterator = parentNode.iterateChildren()
        }


        /**
         * Prepares the next node to return if not already done.

         * @see Iterator.hasNext
         */
        override fun hasNext(): Boolean {
            if (returnProperty != null) {
                // hasNext has been called before
                return true
            } else if (skipSiblings) {
                return false
            } else if (childrenIterator.hasNext()) {
                val child = childrenIterator.next() as XMPNode
                index++

                var path: String? = null
                if (child.options.isSchemaNode) {
                    baseNS = child.name
                } else if (child.parent != null) {
                    // for all but the root node and schema nodes
                    path = accumulatePath(child, parentPath, index)
                }

                // report next property, skip not-leaf nodes in case options is set
                if (!options.isJustLeafnodes || !child.hasChildren()) {
                    returnProperty = createPropertyInfo(child, baseNS, path)
                    return true
                } else {
                    return hasNext()
                }
            } else {
                return false
            }
        }
    }
}