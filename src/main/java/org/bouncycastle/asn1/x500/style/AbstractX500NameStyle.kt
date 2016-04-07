package org.bouncycastle.asn1.x500.style

import java.io.IOException
import java.util.Enumeration
import java.util.Hashtable

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1ParsingException
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameStyle

/**
 * This class provides some default behavior and common implementation for a
 * X500NameStyle. It should be easily extendable to support implementing the
 * desired X500NameStyle.
 */
abstract class AbstractX500NameStyle : X500NameStyle {

    private fun calcHashCode(enc: ASN1Encodable): Int {
        var value = IETFUtils.valueToString(enc)
        value = IETFUtils.canonicalize(value)
        return value.hashCode()
    }

    override fun calculateHashCode(name: X500Name): Int {
        var hashCodeValue = 0
        val rdns = name.rdNs

        // this needs to be order independent, like equals
        for (i in rdns.indices) {
            if (rdns[i].isMultiValued) {
                val atv = rdns[i].typesAndValues

                for (j in atv.indices) {
                    hashCodeValue = hashCodeValue xor atv[j].type.hashCode()
                    hashCodeValue = hashCodeValue xor calcHashCode(atv[j].value)
                }
            } else {
                hashCodeValue = hashCodeValue xor rdns[i].first.type.hashCode()
                hashCodeValue = hashCodeValue xor calcHashCode(rdns[i].first.value)
            }
        }

        return hashCodeValue
    }


    /**
     * For all string values starting with '#' is assumed, that these are
     * already valid ASN.1 objects encoded in hex.
     *
     *
     * All other string values are send to
     * [AbstractX500NameStyle.encodeStringValue].
     *
     * Subclasses should overwrite
     * [AbstractX500NameStyle.encodeStringValue]
     * to change the encoding of specific types.

     * @param oid the DN name of the value.
     * *
     * @param value the String representation of the value.
     */
    override fun stringToValue(oid: ASN1ObjectIdentifier, value: String): ASN1Encodable {
        var value = value
        if (value.length != 0 && value[0] == '#') {
            try {
                return IETFUtils.valueFromHexString(value, 1)
            } catch (e: IOException) {
                throw ASN1ParsingException("can't recode value for oid " + oid.id)
            }

        }

        if (value.length != 0 && value[0] == '\\') {
            value = value.substring(1)
        }

        return encodeStringValue(oid, value)
    }

    /**
     * Encoded every value into a UTF8String.
     *
     *
     * Subclasses should overwrite
     * this method to change the encoding of specific types.
     *

     * @param oid the DN oid of the value
     * *
     * @param value the String representation of the value
     * *
     * @return a the value encoded into a ASN.1 object. Never returns `null`.
     */
    protected open fun encodeStringValue(oid: ASN1ObjectIdentifier, value: String): ASN1Encodable {
        return DERUTF8String(value)
    }

    override fun areEqual(name1: X500Name, name2: X500Name): Boolean {
        val rdns1 = name1.rdNs
        val rdns2 = name2.rdNs

        if (rdns1.size != rdns2.size) {
            return false
        }

        var reverse = false

        if (rdns1[0].first != null && rdns2[0].first != null) {
            reverse = rdns1[0].first.type != rdns2[0].first.type  // guess forward
        }

        for (i in rdns1.indices) {
            if (!foundMatch(reverse, rdns1[i], rdns2)) {
                return false
            }
        }

        return true
    }

    private fun foundMatch(reverse: Boolean, rdn: RDN, possRDNs: Array<RDN>): Boolean {
        if (reverse) {
            for (i in possRDNs.indices.reversed()) {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i])) {
                    possRDNs[i] = null
                    return true
                }
            }
        } else {
            for (i in possRDNs.indices) {
                if (possRDNs[i] != null && rdnAreEqual(rdn, possRDNs[i])) {
                    possRDNs[i] = null
                    return true
                }
            }
        }

        return false
    }

    protected fun rdnAreEqual(rdn1: RDN, rdn2: RDN): Boolean {
        return IETFUtils.rDNAreEqual(rdn1, rdn2)
    }

    companion object {

        /**
         * Tool function to shallow copy a Hashtable.

         * @param paramsMap table to copy
         * *
         * @return the copy of the table
         */
        fun copyHashTable(paramsMap: Hashtable<Any, Any>): Hashtable<Any, Any> {
            val newTable = Hashtable()

            val keys = paramsMap.keys()
            while (keys.hasMoreElements()) {
                val key = keys.nextElement()
                newTable.put(key, paramsMap[key])
            }

            return newTable
        }
    }
}
