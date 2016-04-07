package org.bouncycastle.asn1.eac

import java.util.Enumeration
import java.util.Hashtable


class Flags {

    var flags = 0
        internal set

    constructor() {

    }

    constructor(v: Int) {
        flags = v
    }

    fun set(flag: Int) {
        flags = flags or flag
    }

    fun isSet(flag: Int): Boolean {
        return flags and flag != 0
    }

    /* Java 1.5
     String decode(Map<Integer, String> decodeMap)
     {
         StringJoiner joiner = new StringJoiner(" ");
         for (int i : decodeMap.keySet())
         {
             if (isSet(i))
                 joiner.add(decodeMap.get(i));
         }
         return joiner.toString();
     }
     */

    internal fun decode(decodeMap: Hashtable<Any, Any>): String {
        val joiner = StringJoiner(" ")
        val e = decodeMap.keys()
        while (e.hasMoreElements()) {
            val i = e.nextElement() as Int
            if (isSet(i.toInt())) {
                joiner.add(decodeMap[i] as String)
            }
        }
        return joiner.toString()
    }

    private inner class StringJoiner(internal var mSeparator:

                                     String) {
        internal var First = true
        internal var b = StringBuffer()

        fun add(str: String) {
            if (First) {
                First = false
            } else {
                b.append(mSeparator)
            }

            b.append(str)
        }

        override fun toString(): String {
            return b.toString()
        }
    }
}
