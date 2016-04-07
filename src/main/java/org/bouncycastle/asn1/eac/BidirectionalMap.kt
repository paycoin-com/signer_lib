package org.bouncycastle.asn1.eac

import java.util.Hashtable

class BidirectionalMap : Hashtable<Any, Any>() {

    internal var reverseMap = Hashtable()

    fun getReverse(o: Any): Any {
        return reverseMap.get(o)
    }

    override fun put(key: Any, o: Any): Any {
        reverseMap.put(o, key)
        return super.put(key, o)
    }

    companion object {
        private val serialVersionUID = -7457289971962812909L
    }

}
