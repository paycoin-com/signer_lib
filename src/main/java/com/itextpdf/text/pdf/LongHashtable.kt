/*
 * Copyright The Apache Software Foundation.
 * This class is based on org.apache.IntHashMap.commons.lang
 * http://jakarta.apache.org/commons/lang/xref/org/apache/commons/lang/IntHashMap.html
 * It was adapted by Bruno Lowagie for use in iText,
 * reusing methods that were written by Paulo Soares.
 * Instead of being a hashtable that stores objects with an int as key,
 * it stores int values with an int as key.
 *
 * This is the original license of the original class IntHashMap:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Note: originally released under the GNU LGPL v2.1,
 * but rereleased by the original author under the ASF license (above).
 */
package com.itextpdf.text.pdf

import java.util.Arrays
import java.util.NoSuchElementException

import com.itextpdf.text.error_messages.MessageLocalization

/***
 *
 * A hash map that uses primitive ints for the key rather than objects.

 *
 * Note that this class is for internal optimization purposes only, and may
 * not be supported in future releases of Jakarta Commons Lang.  Utilities of
 * this sort may be included in future releases of Jakarta Commons Collections.

 * @author Justin Couch
 * *
 * @author Alex Chaffee (alex@apache.org)
 * *
 * @author Stephen Colebourne
 * *
 * @author Bruno Lowagie (change Objects as keys into int values)
 * *
 * @author Paulo Soares (added extra methods)
 */
class LongHashtable
/***
 *
 * Constructs a new, empty hashtable with the specified initial
 * capacity and the specified load factor.

 * @param initialCapacity the initial capacity of the hashtable.
 * *
 * @param loadFactor the load factor of the hashtable.
 * *
 * @throws IllegalArgumentException  if the initial capacity is less
 * *             than zero, or if the load factor is nonpositive.
 */
@JvmOverloads constructor(initialCapacity: Int = 150,
                          /***
                           * The load factor for the hashtable.

                           * @serial
                           */
                          private val loadFactor: Float = 0.75f) : Cloneable {

    /***
     * The hash table data.
     */
    @Transient private var table: Array<Entry>? = null

    /***
     * The total number of entries in the hash table.
     */
    @Transient private var count: Int = 0

    /***
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)

     * @serial
     */
    private var threshold: Int = 0

    init {
        var initialCapacity = initialCapacity
        if (initialCapacity < 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("illegal.capacity.1", initialCapacity))
        }
        if (loadFactor <= 0) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("illegal.load.1", loadFactor.toString()))
        }
        if (initialCapacity == 0) {
            initialCapacity = 1
        }
        table = arrayOfNulls<Entry>(initialCapacity)
        threshold = (initialCapacity * loadFactor).toInt()
    }

    /***
     *
     * Returns the number of keys in this hashtable.

     * @return  the number of keys in this hashtable.
     */
    fun size(): Int {
        return count
    }

    /***
     *
     * Tests if this hashtable maps no keys to values.

     * @return  `true` if this hashtable maps no keys to values;
     * *          `false` otherwise.
     */
    val isEmpty: Boolean
        get() = count == 0

    /***
     *
     * Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the `containsKey`
     * method.

     *
     * Note that this method is identical in functionality to containsValue,
     * (which is part of the Map interface in the collections framework).

     * @param      value   a value to search for.
     * *
     * @return     `true` if and only if some key maps to the
     * *             `value` argument in this hashtable as
     * *             determined by the equals method;
     * *             `false` otherwise.
     * *
     * @throws  NullPointerException  if the value is `null`.
     * *
     * @see .containsKey
     * @see .containsValue
     * @see java.util.Map
     */
    operator fun contains(value: Long): Boolean {

        val tab = table
        var i = tab.size
        while (i-- > 0) {
            var e: Entry? = tab[i]
            while (e != null) {
                if (e.value == value) {
                    return true
                }
                e = e.next
            }
        }
        return false
    }

    /***
     *
     * Returns `true` if this HashMap maps one or more keys
     * to this value.

     *
     * Note that this method is identical in functionality to contains
     * (which predates the Map interface).

     * @param value value whose presence in this HashMap is to be tested.
     * *
     * @return boolean `true` if the value is contained
     * *
     * @see java.util.Map

     * @since JDK1.2
     */
    fun containsValue(value: Long): Boolean {
        return contains(value)
    }

    /***
     *
     * Tests if the specified int is a key in this hashtable.

     * @param  key  possible key.
     * *
     * @return `true` if and only if the specified int is a
     * *    key in this hashtable, as determined by the equals
     * *    method; `false` otherwise.
     * *
     * @see .contains
     */
    fun containsKey(key: Long): Boolean {
        val tab = table
        val hash = (key xor key.ushr(32)).toInt()
        val index = (hash and 0x7FFFFFFF) % tab.size
        var e: Entry? = tab[index]
        while (e != null) {
            if (e.hash == hash && e.key == key) {
                return true
            }
            e = e.next
        }
        return false
    }

    /***
     *
     * Returns the value to which the specified key is mapped in this map.

     * @param   key   a key in the hashtable.
     * *
     * @return  the value to which the key is mapped in this hashtable;
     * *          `null` if the key is not mapped to any value in
     * *          this hashtable.
     * *
     * @see .put
     */
    operator fun get(key: Long): Long {
        val tab = table
        val hash = (key xor key.ushr(32)).toInt()
        val index = (hash and 0x7FFFFFFF) % tab.size
        var e: Entry? = tab[index]
        while (e != null) {
            if (e.hash == hash && e.key == key) {
                return e.value
            }
            e = e.next
        }
        return 0
    }

    /***
     *
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.

     *
     * This method is called automatically when the number of keys
     * in the hashtable exceeds this hashtable's capacity and load
     * factor.
     */
    protected fun rehash() {
        val oldCapacity = table!!.size
        val oldMap = table

        val newCapacity = oldCapacity * 2 + 1
        val newMap = arrayOfNulls<Entry>(newCapacity)

        threshold = (newCapacity * loadFactor).toInt()
        table = newMap

        var i = oldCapacity
        while (i-- > 0) {
            var old: Entry? = oldMap[i]
            while (old != null) {
                val e = old
                old = old.next

                val index = (e.hash and 0x7FFFFFFF) % newCapacity
                e.next = newMap[index]
                newMap[index] = e
            }
        }
    }

    /***
     *
     * Maps the specified `key` to the specified
     * `value` in this hashtable. The key cannot be
     * `null`.

     *
     * The value can be retrieved by calling the `get` method
     * with a key that is equal to the original key.

     * @param key     the hashtable key.
     * *
     * @param value   the value.
     * *
     * @return the previous value of the specified key in this hashtable,
     * *         or `null` if it did not have one.
     * *
     * @throws  NullPointerException  if the key is `null`.
     * *
     * @see .get
     */
    fun put(key: Long, value: Long): Long {
        // Makes sure the key is not already in the hashtable.
        var tab: Array<Entry> = table
        val hash = (key xor key.ushr(32)).toInt()
        var index = (hash and 0x7FFFFFFF) % tab.size
        run {
            var e: Entry? = tab[index]
            while (e != null) {
                if (e.hash == hash && e.key == key) {
                    val old = e.value
                    e.value = value
                    return old
                }
                e = e.next
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash()

            tab = table
            index = (hash and 0x7FFFFFFF) % tab.size
        }

        // Creates the new entry.
        val e = Entry(hash, key, value, tab[index])
        tab[index] = e
        count++
        return 0
    }

    /***
     *
     * Removes the key (and its corresponding value) from this
     * hashtable.

     *
     * This method does nothing if the key is not present in the
     * hashtable.

     * @param   key   the key that needs to be removed.
     * *
     * @return  the value to which the key had been mapped in this hashtable,
     * *          or `null` if the key did not have a mapping.
     */
    fun remove(key: Long): Long {
        val tab = table
        val hash = (key xor key.ushr(32)).toInt()
        val index = (hash and 0x7FFFFFFF) % tab.size
        var e: Entry? = tab[index]
        var prev: Entry? = null
        while (e != null) {
            if (e.hash == hash && e.key == key) {
                if (prev != null) {
                    prev.next = e.next
                } else {
                    tab[index] = e.next
                }
                count--
                val oldValue = e.value
                e.value = 0
                return oldValue
            }
            prev = e
            e = e.next
        }
        return 0
    }

    /***
     *
     * Clears this hashtable so that it contains no keys.
     */
    fun clear() {
        val tab = table
        var index = tab.size
        while (--index >= 0) {
            tab[index] = null
        }
        count = 0
    }

    /***
     *
     * Innerclass that acts as a datastructure to create a new entry in the
     * table.
     */
    internal class Entry
    /***
     *
     * Create a new entry with the given values.

     * @param hash The code used to hash the int with
     * *
     * @param key The key used to enter this in the table
     * *
     * @param value The value for this key
     * *
     * @param next A reference to the next entry in the table
     */
    protected constructor(var hash: Int, // extra methods for inner class Entry by Paulo
                          var key: Long, var value: Long, var next: Entry?) {
        protected fun clone(): Any {
            val entry = Entry(hash, key, value, if (next != null) next!!.clone() as Entry else null)
            return entry
        }
    }

    // extra inner class by Paulo
    internal class LongHashtableIterator(var table: Array<Entry>) : Iterator<Entry> {
        var index: Int = 0
        var entry: Entry? = null

        init {
            this.index = table.size
        }

        override fun hasNext(): Boolean {
            if (entry != null) {
                return true
            }
            while (index-- > 0) {
                if ((entry = table[index]) != null) {
                    return true
                }
            }
            return false
        }

        override fun next(): Entry {
            if (entry == null) {
                while (index-- > 0 && (entry = table[index]) == null)
            }
            if (entry != null) {
                val e = entry
                entry = e.next
                return e
            }
            throw NoSuchElementException(MessageLocalization.getComposedMessage("inthashtableiterator"))
        }

        override fun remove() {
            throw UnsupportedOperationException(MessageLocalization.getComposedMessage("remove.not.supported"))
        }
    }

    // extra methods by Paulo Soares:

    val entryIterator: Iterator<Entry>
        get() = LongHashtableIterator(table)

    fun toOrderedKeys(): LongArray {
        val res = keys
        Arrays.sort(res)
        return res
    }

    val keys: LongArray
        get() {
            val res = LongArray(count)
            var ptr = 0
            var index = table!!.size
            var entry: Entry? = null
            while (true) {
                if (entry == null)
                    while (index-- > 0 && (entry = table!![index]) == null)
                        if (entry == null)
                            break
                val e = entry
                entry = e.next
                res[ptr++] = e.key
            }
            return res
        }

    val oneKey: Long
        get() {
            if (count == 0)
                return 0
            var index = table!!.size
            var entry: Entry? = null
            while (index-- > 0 && (entry = table!![index]) == null)
                if (entry == null)
                    return 0
            return entry!!.key
        }

    public override fun clone(): Any {
        try {
            val t = super.clone() as LongHashtable
            t.table = arrayOfNulls<Entry>(table!!.size)
            var i = table!!.size
            while (i-- > 0) {
                t.table[i] = if (table!![i] != null)
                    table!![i].clone() as Entry
                else
                    null
            }
            return t
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }

    }
}
/***
 *
 * Constructs a new, empty hashtable with a default capacity and load
 * factor, which is `20` and `0.75` respectively.
 */
/***
 *
 * Constructs a new, empty hashtable with the specified initial capacity
 * and default load factor, which is `0.75`.

 * @param  initialCapacity the initial capacity of the hashtable.
 * *
 * @throws IllegalArgumentException if the initial capacity is less
 * *   than zero.
 */
