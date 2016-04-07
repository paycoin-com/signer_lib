package org.bouncycastle.asn1.dvcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.PolicyInformation

/**
 *
 * DVCSRequestInformation ::= SEQUENCE  {
 * version                      INTEGER DEFAULT 1 ,
 * service                      ServiceType,
 * nonce                        Nonce OPTIONAL,
 * requestTime                  DVCSTime OPTIONAL,
 * requester                    [0] GeneralNames OPTIONAL,
 * requestPolicy                [1] PolicyInformation OPTIONAL,
 * dvcs                         [2] GeneralNames OPTIONAL,
 * dataLocations                [3] GeneralNames OPTIONAL,
 * extensions                   [4] IMPLICIT Extensions OPTIONAL
 * }
 *
 */

class DVCSRequestInformation private constructor(seq: ASN1Sequence) : ASN1Object() {
    var version = DEFAULT_VERSION
        private set
    val service: ServiceType
    var nonce: BigInteger? = null
        private set
    var requestTime: DVCSTime? = null
        private set
    var requester: GeneralNames? = null
        private set
    var requestPolicy: PolicyInformation? = null
        private set
    var dvcs: GeneralNames? = null
        private set
    var dataLocations: GeneralNames? = null
        private set
    var extensions: Extensions? = null
        private set

    init {
        var i = 0

        if (seq.getObjectAt(0) is ASN1Integer) {
            val encVersion = ASN1Integer.getInstance(seq.getObjectAt(i++))
            this.version = encVersion.value.toInt()
        } else {
            this.version = 1
        }

        this.service = ServiceType.getInstance(seq.getObjectAt(i++))

        while (i < seq.size()) {
            val x = seq.getObjectAt(i)

            if (x is ASN1Integer) {
                this.nonce = ASN1Integer.getInstance(x).value
            } else if (x is ASN1GeneralizedTime) {
                this.requestTime = DVCSTime.getInstance(x)
            } else if (x is ASN1TaggedObject) {
                val t = ASN1TaggedObject.getInstance(x)
                val tagNo = t.tagNo

                when (tagNo) {
                    TAG_REQUESTER -> this.requester = GeneralNames.getInstance(t, false)
                    TAG_REQUEST_POLICY -> this.requestPolicy = PolicyInformation.getInstance(ASN1Sequence.getInstance(t, false))
                    TAG_DVCS -> this.dvcs = GeneralNames.getInstance(t, false)
                    TAG_DATA_LOCATIONS -> this.dataLocations = GeneralNames.getInstance(t, false)
                    TAG_EXTENSIONS -> this.extensions = Extensions.getInstance(t, false)
                }
            } else {
                this.requestTime = DVCSTime.getInstance(x)
            }

            i++
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (version != DEFAULT_VERSION) {
            v.add(ASN1Integer(version.toLong()))
        }
        v.add(service)
        if (nonce != null) {
            v.add(ASN1Integer(nonce))
        }
        if (requestTime != null) {
            v.add(requestTime)
        }

        val tags = intArrayOf(TAG_REQUESTER, TAG_REQUEST_POLICY, TAG_DVCS, TAG_DATA_LOCATIONS, TAG_EXTENSIONS)
        val taggedObjects = arrayOf<ASN1Encodable>(requester, requestPolicy, dvcs, dataLocations, extensions)
        for (i in tags.indices) {
            val tag = tags[i]
            val taggedObject = taggedObjects[i]
            if (taggedObject != null) {
                v.add(DERTaggedObject(false, tag, taggedObject))
            }
        }

        return DERSequence(v)
    }

    override fun toString(): String {

        val s = StringBuffer()

        s.append("DVCSRequestInformation {\n")

        if (version != DEFAULT_VERSION) {
            s.append("version: " + version + "\n")
        }
        s.append("service: " + service + "\n")
        if (nonce != null) {
            s.append("nonce: " + nonce + "\n")
        }
        if (requestTime != null) {
            s.append("requestTime: " + requestTime + "\n")
        }
        if (requester != null) {
            s.append("requester: " + requester + "\n")
        }
        if (requestPolicy != null) {
            s.append("requestPolicy: " + requestPolicy + "\n")
        }
        if (dvcs != null) {
            s.append("dvcs: " + dvcs + "\n")
        }
        if (dataLocations != null) {
            s.append("dataLocations: " + dataLocations + "\n")
        }
        if (extensions != null) {
            s.append("extensions: " + extensions + "\n")
        }

        s.append("}\n")
        return s.toString()
    }

    companion object {

        private val DEFAULT_VERSION = 1
        private val TAG_REQUESTER = 0
        private val TAG_REQUEST_POLICY = 1
        private val TAG_DVCS = 2
        private val TAG_DATA_LOCATIONS = 3
        private val TAG_EXTENSIONS = 4

        fun getInstance(obj: Any?): DVCSRequestInformation? {
            if (obj is DVCSRequestInformation) {
                return obj
            } else if (obj != null) {
                return DVCSRequestInformation(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSRequestInformation {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
