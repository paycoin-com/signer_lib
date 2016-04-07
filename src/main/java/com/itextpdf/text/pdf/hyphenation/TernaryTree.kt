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
import java.util.Enumeration
import java.util.Stack

/**
 * Ternary Search Tree.

 *
 * A ternary search tree is a hybrid between a binary tree and
 * a digital search tree (trie). Keys are limited to strings.
 * A data value of type char is stored in each leaf node.
 * It can be used as an index (or pointer) to the data.
 * Branches that only contain one key are compressed to one node
 * by storing a pointer to the trailer substring of the key.
 * This class is intended to serve as base class or helper class
 * to implement Dictionary collections or the like. Ternary trees
 * have some nice properties as the following: the tree can be
 * traversed in sorted order, partial matches (wildcard) can be
 * implemented, retrieval of all keys within a given distance
 * from the target, etc. The storage requirements are higher than
 * a binary tree but a lot less than a trie. Performance is
 * comparable with a hash table, sometimes it outperforms a hash
 * function (most of the time can determine a miss faster than a hash).

 *
 * The main purpose of this java port is to serve as a base for
 * implementing TeX's hyphenation algorithm (see The TeXBook,
 * appendix H). Each language requires from 5000 to 15000 hyphenation
 * patterns which will be keys in this tree. The strings patterns
 * are usually small (from 2 to 5 characters), but each char in the
 * tree is stored in a node. Thus memory usage is the main concern.
 * We will sacrifice 'elegance' to keep memory requirements to the
 * minimum. Using java's char type as pointer (yes, I know pointer
 * it is a forbidden word in java) we can keep the size of the node
 * to be just 8 bytes (3 pointers and the data char). This gives
 * room for about 65000 nodes. In my tests the English patterns
 * took 7694 nodes and the German patterns 10055 nodes,
 * so I think we are safe.

 *
 * All said, this is a map with strings as keys and char as value.
 * Pretty limited!. It can be extended to a general map by
 * using the string representation of an object and using the
 * char value as an index to an array that contains the object
 * values.

 * @author cav@uniscope.co.jp
 */

open class TernaryTree internal constructor() : Cloneable, Serializable {

    /**
     * Pointer to low branch and to rest of the key when it is
     * stored directly in this node, we don't have unions in java!
     */
    protected var lo: CharArray

    /**
     * Pointer to high branch.
     */
    protected var hi: CharArray

    /**
     * Pointer to equal branch and to data when this node is a string terminator.
     */
    protected var eq: CharArray

    /**
     * The character stored in this node: splitchar.
     * Two special values are reserved:
     *  * 0x0000 as string terminator
     *  * 0xFFFF to indicate that the branch starting at
     * this node is compressed
     *
     * This shouldn't be a problem if we give the usual semantics to
     * strings since 0xFFFF is guaranteed not to be an Unicode character.
     */
    protected var sc: CharArray

    /**
     * This vector holds the trailing of the keys when the branch is compressed.
     */
    protected var kv: CharVector

    protected var root: Char = ' '
    protected var freenode: Char = ' '
    protected var length: Int = 0    // number of items in tree

    init {
        init()
    }

    protected fun init() {
        root = 0.toChar()
        freenode = 1.toChar()
        length = 0
        lo = CharArray(BLOCK_SIZE)
        hi = CharArray(BLOCK_SIZE)
        eq = CharArray(BLOCK_SIZE)
        sc = CharArray(BLOCK_SIZE)
        kv = CharVector()
    }

    /**
     * Branches are initially compressed, needing
     * one node per key plus the size of the string
     * key. They are decompressed as needed when
     * another key with same prefix
     * is inserted. This saves a lot of space,
     * specially for long keys.
     */
    fun insert(key: String, `val`: Char) {
        // make sure we have enough room in the arrays
        var len = key.length + 1    // maximum number of nodes that may be generated
        if (freenode.toInt() + len > eq.size) {
            redimNodeArrays(eq.size + BLOCK_SIZE)
        }
        val strkey = CharArray(len--)
        key.toCharArray(strkey, 0, 0, len)
        strkey[len] = 0.toChar()
        root = insert(root, strkey, 0, `val`)
    }

    fun insert(key: CharArray, start: Int, `val`: Char) {
        val len = strlen(key) + 1
        if (freenode.toInt() + len > eq.size) {
            redimNodeArrays(eq.size + BLOCK_SIZE)
        }
        root = insert(root, key, start, `val`)
    }

    /**
     * The actual insertion function, recursive version.
     */
    private fun insert(p: Char, key: CharArray, start: Int, `val`: Char): Char {
        var p = p
        val len = strlen(key, start)
        if (p.toInt() == 0) {
            // this means there is no branch, this node will start a new branch.
            // Instead of doing that, we store the key somewhere else and create
            // only one node with a pointer to the key
            p = freenode++
            eq[p] = `val`           // holds data
            length++
            hi[p] = 0.toChar()
            if (len > 0) {
                sc[p] = 0xFFFF.toChar()    // indicates branch is compressed
                lo[p] = kv.alloc(len + 1).toChar()    // use 'lo' to hold pointer to key
                strcpy(kv.array, lo[p].toInt(), key, start)
            } else {
                sc[p] = 0.toChar()
                lo[p] = 0.toChar()
            }
            return p
        }

        if (sc[p].toInt() == 0xFFFF) {
            // branch is compressed: need to decompress
            // this will generate garbage in the external key array
            // but we can do some garbage collection later
            val pp = freenode++
            lo[pp] = lo[p]    // previous pointer to key
            eq[pp] = eq[p]    // previous pointer to data
            lo[p] = 0.toChar()
            if (len > 0) {
                sc[p] = kv.get(lo[pp].toInt())
                eq[p] = pp
                lo[pp]++
                if (kv.get(lo[pp].toInt()).toInt() == 0) {
                    // key completely decompressed leaving garbage in key array
                    lo[pp] = 0.toChar()
                    sc[pp] = 0.toChar()
                    hi[pp] = 0.toChar()
                } else {
                    // we only got first char of key, rest is still there
                    sc[pp] = 0xFFFF.toChar()
                }
            } else {
                // In this case we can save a node by swapping the new node
                // with the compressed node
                sc[pp] = 0xFFFF.toChar()
                hi[p] = pp
                sc[p] = 0.toChar()
                eq[p] = `val`
                length++
                return p
            }
        }
        val s = key[start]
        if (s < sc[p]) {
            lo[p] = insert(lo[p], key, start, `val`)
        } else if (s == sc[p]) {
            if (s.toInt() != 0) {
                eq[p] = insert(eq[p], key, start + 1, `val`)
            } else {
                // key already in tree, overwrite data
                eq[p] = `val`
            }
        } else {
            hi[p] = insert(hi[p], key, start, `val`)
        }
        return p
    }

    fun find(key: String): Int {
        val len = key.length
        val strkey = CharArray(len + 1)
        key.toCharArray(strkey, 0, 0, len)
        strkey[len] = 0.toChar()

        return find(strkey, 0)
    }

    fun find(key: CharArray, start: Int): Int {
        var d: Int
        var p = root
        var i = start
        var c: Char

        while (p.toInt() != 0) {
            if (sc[p].toInt() == 0xFFFF) {
                if (strcmp(key, i, kv.array, lo[p].toInt()) == 0) {
                    return eq[p].toInt()
                } else {
                    return -1
                }
            }
            c = key[i]
            d = c - sc[p]
            if (d == 0) {
                if (c.toInt() == 0) {
                    return eq[p].toInt()
                }
                i++
                p = eq[p]
            } else if (d < 0) {
                p = lo[p]
            } else {
                p = hi[p]
            }
        }
        return -1
    }

    fun knows(key: String): Boolean {
        return find(key) >= 0
    }

    // redimension the arrays
    private fun redimNodeArrays(newsize: Int) {
        val len = if (newsize < lo.size) newsize else lo.size
        var na = CharArray(newsize)
        System.arraycopy(lo, 0, na, 0, len)
        lo = na
        na = CharArray(newsize)
        System.arraycopy(hi, 0, na, 0, len)
        hi = na
        na = CharArray(newsize)
        System.arraycopy(eq, 0, na, 0, len)
        eq = na
        na = CharArray(newsize)
        System.arraycopy(sc, 0, na, 0, len)
        sc = na
    }

    fun size(): Int {
        return length
    }

    public override fun clone(): Any {
        val t = TernaryTree()
        t.lo = this.lo.clone()
        t.hi = this.hi.clone()
        t.eq = this.eq.clone()
        t.sc = this.sc.clone()
        t.kv = this.kv.clone() as CharVector
        t.root = this.root
        t.freenode = this.freenode
        t.length = this.length

        return t
    }

    /**
     * Recursively insert the median first and then the median of the
     * lower and upper halves, and so on in order to get a balanced
     * tree. The array of keys is assumed to be sorted in ascending
     * order.
     */
    protected fun insertBalanced(k: Array<String>, v: CharArray, offset: Int, n: Int) {
        val m: Int
        if (n < 1) {
            return
        }
        m = n shr 1

        insert(k[m + offset], v[m + offset])
        insertBalanced(k, v, offset, m)

        insertBalanced(k, v, offset + m + 1, n - m - 1)
    }


    /**
     * Balance the tree for best search performance
     */
    fun balance() {
        // System.out.print("Before root splitchar = "); System.out.println(sc[root]);

        var i = 0
        val n = length
        val k = arrayOfNulls<String>(n)
        val v = CharArray(n)
        val iter = Iterator()
        while (iter.hasMoreElements()) {
            v[i] = iter.value
            k[i++] = iter.nextElement()
        }
        init()
        insertBalanced(k, v, 0, n)

        // With uniform letter distribution sc[root] should be around 'm'
        // System.out.print("After root splitchar = "); System.out.println(sc[root]);
    }

    /**
     * Each node stores a character (splitchar) which is part of
     * some key(s). In a compressed branch (one that only contain
     * a single string key) the trailer of the key which is not
     * already in nodes is stored  externally in the kv array.
     * As items are inserted, key substrings decrease.
     * Some substrings may completely  disappear when the whole
     * branch is totally decompressed.
     * The tree is traversed to find the key substrings actually
     * used. In addition, duplicate substrings are removed using
     * a map (implemented with a TernaryTree!).

     */
    fun trimToSize() {
        // first balance the tree for best performance
        balance()

        // redimension the node arrays
        redimNodeArrays(freenode.toInt())

        // ok, compact kv array
        val kx = CharVector()
        kx.alloc(1)
        val map = TernaryTree()
        compact(kx, map, root)
        kv = kx
        kv.trimToSize()
    }

    private fun compact(kx: CharVector, map: TernaryTree, p: Char) {
        var k: Int
        if (p.toInt() == 0) {
            return
        }
        if (sc[p].toInt() == 0xFFFF) {
            k = map.find(kv.array, lo[p].toInt())
            if (k < 0) {
                k = kx.alloc(strlen(kv.array, lo[p].toInt()) + 1)
                strcpy(kx.array, k, kv.array, lo[p].toInt())
                map.insert(kx.array, k, k.toChar())
            }
            lo[p] = k.toChar()
        } else {
            compact(kx, map, lo[p])
            if (sc[p].toInt() != 0) {
                compact(kx, map, eq[p])
            }
            compact(kx, map, hi[p])
        }
    }


    fun keys(): Enumeration<String> {
        return Iterator()
    }

    inner class Iterator : Enumeration<String> {

        /**
         * current node index
         */
        internal var cur: Int = 0

        /**
         * current key
         */
        internal var curkey: String

        private inner class Item : Cloneable {
            internal var parent: Char = ' '
            internal var child: Char = ' '

            constructor() {
                parent = 0.toChar()
                child = 0.toChar()
            }

            constructor(p: Char, c: Char) {
                parent = p
                child = c
            }

            public override fun clone(): Item {
                return Item(parent, child)
            }

        }

        /**
         * Node stack
         */
        internal var ns: Stack<Item>

        /**
         * key stack implemented with a StringBuffer
         */
        internal var ks: StringBuffer

        init {
            cur = -1
            ns = Stack<Item>()
            ks = StringBuffer()
            rewind()
        }

        fun rewind() {
            ns.removeAllElements()
            ks.setLength(0)
            cur = root.toInt()
            run()
        }

        override fun nextElement(): String {
            val res = curkey
            cur = up()
            run()
            return res
        }

        val value: Char
            get() {
                if (cur >= 0) {
                    return eq[cur]
                }
                return 0.toChar()
            }

        override fun hasMoreElements(): Boolean {
            return cur != -1
        }

        /**
         * traverse upwards
         */
        private fun up(): Int {
            var i = Item()
            var res = 0

            if (ns.empty()) {
                return -1
            }

            if (cur != 0 && sc[cur].toInt() == 0) {
                return lo[cur].toInt()
            }

            var climb = true

            while (climb) {
                i = ns.pop()
                i.child++
                when (i.child) {
                    1 -> {
                        if (sc[i.parent].toInt() != 0) {
                            res = eq[i.parent].toInt()
                            ns.push(i.clone())
                            ks.append(sc[i.parent])
                        } else {
                            i.child++
                            ns.push(i.clone())
                            res = hi[i.parent].toInt()
                        }
                        climb = false
                    }

                    2 -> {
                        res = hi[i.parent].toInt()
                        ns.push(i.clone())
                        if (ks.length > 0) {
                            ks.setLength(ks.length - 1)    // pop
                        }
                        climb = false
                    }

                    else -> {
                        if (ns.empty()) {
                            return -1
                        }
                        climb = true
                    }
                }
            }
            return res
        }

        /**
         * traverse the tree to find next key
         */
        private fun run(): Int {
            if (cur == -1) {
                return -1
            }

            var leaf = false
            while (true) {
                // first go down on low branch until leaf or compressed branch
                while (cur != 0) {
                    if (sc[cur].toInt() == 0xFFFF) {
                        leaf = true
                        break
                    }
                    ns.push(Item(cur.toChar(), '\u0000'))
                    if (sc[cur].toInt() == 0) {
                        leaf = true
                        break
                    }
                    cur = lo[cur].toInt()
                }
                if (leaf) {
                    break
                }
                // nothing found, go up one node and try again
                cur = up()
                if (cur == -1) {
                    return -1
                }
            }
            // The current node should be a data node and
            // the key should be in the key stack (at least partially)
            val buf = StringBuffer(ks.toString())
            if (sc[cur].toInt() == 0xFFFF) {
                var p = lo[cur].toInt()
                while (kv.get(p).toInt() != 0) {
                    buf.append(kv.get(p++))
                }
            }
            curkey = buf.toString()
            return 0
        }

    }

    open fun printStats() {
        println("Number of keys = " + Integer.toString(length))
        println("Node count = " + Integer.toString(freenode.toInt()))
        // System.out.println("Array length = " + Integer.toString(eq.length));
        println("Key Array length = " + Integer.toString(kv.length()))

        /*
         * for(int i=0; i<kv.length(); i++)
         * if ( kv.get(i) != 0 )
         * System.out.print(kv.get(i));
         * else
         * System.out.println("");
         * System.out.println("Keys:");
         * for(Enumeration enum = keys(); enum.hasMoreElements(); )
         * System.out.println(enum.nextElement());
         */

    }

    companion object {

        /**
         * We use 4 arrays to represent a node. I guess I should have created
         * a proper node class, but somehow Knuth's pascal code made me forget
         * we now have a portable language with virtual memory management and
         * automatic garbage collection! And now is kind of late, furthermore,
         * if it ain't broken, don't fix it.
         */

        private val serialVersionUID = 5313366505322983510L

        protected val BLOCK_SIZE = 2048    // allocation size for arrays

        /**
         * Compares 2 null terminated char arrays
         */
        fun strcmp(a: CharArray, startA: Int, b: CharArray, startB: Int): Int {
            var startA = startA
            var startB = startB
            while (a[startA] == b[startB]) {
                if (a[startA].toInt() == 0) {
                    return 0
                }
                startA++
                startB++
            }
            return a[startA] - b[startB]
        }

        /**
         * Compares a string with null terminated char array
         */
        fun strcmp(str: String, a: CharArray, start: Int): Int {
            var i: Int
            var d: Int
            val len = str.length
            i = 0
            while (i < len) {
                d = str[i] - a[start + i]
                if (d != 0) {
                    return d
                }
                if (a[start + i].toInt() == 0) {
                    return d
                }
                i++
            }
            if (a[start + i].toInt() != 0) {
                return -a[start + i]
            }
            return 0

        }

        fun strcpy(dst: CharArray, di: Int, src: CharArray, si: Int) {
            var di = di
            var si = si
            while (src[si].toInt() != 0) {
                dst[di++] = src[si++]
            }
            dst[di] = 0.toChar()
        }

        @JvmOverloads fun strlen(a: CharArray, start: Int = 0): Int {
            var len = 0
            var i = start
            while (i < a.size && a[i].toInt() != 0) {
                len++
                i++
            }
            return len
        }
    }

    /*    public static void main(String[] args) throws Exception {
        TernaryTree tt = new TernaryTree();
        tt.insert("Carlos", 'C');
        tt.insert("Car", 'r');
        tt.insert("palos", 'l');
        tt.insert("pa", 'p');
        tt.trimToSize();
        System.out.println((char)tt.find("Car"));
        System.out.println((char)tt.find("Carlos"));
        System.out.println((char)tt.find("alto"));
        tt.printStats();
    }*/

}

