/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This code was originally part of the Apache Harmony project.
 *  The Apache Harmony project has been discontinued.
 *  That's why we imported the code into iText.
 */
/**
 * @author Alexey A. Petrenko
 */
package com.itextpdf.awt.geom.misc

import java.util.HashMap

/**
 * RenderingHints

 */
class RenderingHints : Map<Any, Any>, Cloneable {

    private var map = HashMap<Any, Any>()

    constructor(map: Map<Key, *>?) : super() {
        if (map != null) {
            putAll(map)
        }
    }

    constructor(key: Key, value: Any) : super() {
        put(key, value)
    }

    fun add(hints: RenderingHints) {
        map.putAll(hints.map)
    }

    override fun put(key: Any, value: Any): Any {
        if (!(key as Key).isCompatibleValue(value)) {
            throw IllegalArgumentException()
        }

        return map.put(key, value)
    }

    override fun remove(key: Any): Any {
        return map.remove(key)
    }

    override fun get(key: Any): Any? {
        return map[key]
    }

    override fun keySet(): Set<Any> {
        return map.keys
    }

    override fun entrySet(): Set<Entry<Any, Any>> {
        return map.entries
    }

    override fun putAll(m: Map<*, *>) {
        if (m is RenderingHints) {
            map.putAll(m.map)
        } else {
            val entries = m.entries

            if (entries != null) {
                val it = entries.iterator()
                while (it.hasNext()) {
                    val entry = it.next() as Entry<*, *>
                    val `val` = entry.value
                    put(entry.key, `val`)
                }
            }
        }
    }

    override fun values(): Collection<Any> {
        return map.values
    }

    override fun containsValue(value: Any): Boolean {
        return map.containsValue(value)
    }

    override fun containsKey(key: Any?): Boolean {
        if (key == null) {
            throw NullPointerException()
        }

        return map.containsKey(key)
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun clear() {
        map.clear()
    }

    override fun size(): Int {
        return map.size
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Map<*, *>) {
            return false
        }

        val keys = keys
        if (keys != o.keys) {
            return false
        }

        val it = keys.iterator()
        while (it.hasNext()) {
            val key = it.next() as Key
            val v1 = get(key)
            val v2 = o[key]
            if (!(if (v1 == null) v2 == null else v1 == v2)) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    @SuppressWarnings("unchecked")
    public override fun clone(): Any {
        val clone = RenderingHints(null)
        clone.map = this.map.clone() as HashMap<Any, Any>
        return clone
    }

    override fun toString(): String {
        return "RenderingHints[" + map.toString() + "]" //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Key
     */
    abstract class Key protected constructor(private val key: Int) {

        override fun equals(o: Any?): Boolean {
            return this === o
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }

        protected fun intKey(): Int {
            return key
        }

        abstract fun isCompatibleValue(`val`: Any): Boolean
    }

    /**
     * Private implementation of Key class
     */
    private class KeyImpl protected constructor(key: Int) : Key(key) {

        override fun isCompatibleValue(`val`: Any): Boolean {
            if (`val` !is KeyValue) {
                return false
            }

            return `val`.key === this
        }
    }

    /**
     * Private class KeyValue is used as value for Key class instance.
     */
    private class KeyValue protected constructor(private val key: Key)

    companion object {
        val KEY_ALPHA_INTERPOLATION: Key = KeyImpl(1)
        val VALUE_ALPHA_INTERPOLATION_DEFAULT: Any = KeyValue(KEY_ALPHA_INTERPOLATION)
        val VALUE_ALPHA_INTERPOLATION_SPEED: Any = KeyValue(KEY_ALPHA_INTERPOLATION)
        val VALUE_ALPHA_INTERPOLATION_QUALITY: Any = KeyValue(KEY_ALPHA_INTERPOLATION)

        val KEY_ANTIALIASING: Key = KeyImpl(2)
        val VALUE_ANTIALIAS_DEFAULT: Any = KeyValue(KEY_ANTIALIASING)
        val VALUE_ANTIALIAS_ON: Any = KeyValue(KEY_ANTIALIASING)
        val VALUE_ANTIALIAS_OFF: Any = KeyValue(KEY_ANTIALIASING)

        val KEY_COLOR_RENDERING: Key = KeyImpl(3)
        val VALUE_COLOR_RENDER_DEFAULT: Any = KeyValue(KEY_COLOR_RENDERING)
        val VALUE_COLOR_RENDER_SPEED: Any = KeyValue(KEY_COLOR_RENDERING)
        val VALUE_COLOR_RENDER_QUALITY: Any = KeyValue(KEY_COLOR_RENDERING)

        val KEY_DITHERING: Key = KeyImpl(4)
        val VALUE_DITHER_DEFAULT: Any = KeyValue(KEY_DITHERING)
        val VALUE_DITHER_DISABLE: Any = KeyValue(KEY_DITHERING)
        val VALUE_DITHER_ENABLE: Any = KeyValue(KEY_DITHERING)

        val KEY_FRACTIONALMETRICS: Key = KeyImpl(5)
        val VALUE_FRACTIONALMETRICS_DEFAULT: Any = KeyValue(KEY_FRACTIONALMETRICS)
        val VALUE_FRACTIONALMETRICS_ON: Any = KeyValue(KEY_FRACTIONALMETRICS)
        val VALUE_FRACTIONALMETRICS_OFF: Any = KeyValue(KEY_FRACTIONALMETRICS)

        val KEY_INTERPOLATION: Key = KeyImpl(6)
        val VALUE_INTERPOLATION_BICUBIC: Any = KeyValue(KEY_INTERPOLATION)
        val VALUE_INTERPOLATION_BILINEAR: Any = KeyValue(KEY_INTERPOLATION)
        val VALUE_INTERPOLATION_NEAREST_NEIGHBOR: Any = KeyValue(KEY_INTERPOLATION)

        val KEY_RENDERING: Key = KeyImpl(7)
        val VALUE_RENDER_DEFAULT: Any = KeyValue(KEY_RENDERING)
        val VALUE_RENDER_SPEED: Any = KeyValue(KEY_RENDERING)
        val VALUE_RENDER_QUALITY: Any = KeyValue(KEY_RENDERING)

        val KEY_STROKE_CONTROL: Key = KeyImpl(8)
        val VALUE_STROKE_DEFAULT: Any = KeyValue(KEY_STROKE_CONTROL)
        val VALUE_STROKE_NORMALIZE: Any = KeyValue(KEY_STROKE_CONTROL)
        val VALUE_STROKE_PURE: Any = KeyValue(KEY_STROKE_CONTROL)

        val KEY_TEXT_ANTIALIASING: Key = KeyImpl(9)
        val VALUE_TEXT_ANTIALIAS_DEFAULT: Any = KeyValue(KEY_TEXT_ANTIALIASING)
        val VALUE_TEXT_ANTIALIAS_ON: Any = KeyValue(KEY_TEXT_ANTIALIASING)
        val VALUE_TEXT_ANTIALIAS_OFF: Any = KeyValue(KEY_TEXT_ANTIALIASING)
    }
}
