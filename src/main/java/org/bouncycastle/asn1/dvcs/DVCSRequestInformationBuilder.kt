package org.bouncycastle.asn1.dvcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.util.BigIntegers

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
class DVCSRequestInformationBuilder {
    private var version = DEFAULT_VERSION

    private val service: ServiceType
    private val initialInfo: DVCSRequestInformation?

    private var nonce: BigInteger? = null
    private var requestTime: DVCSTime? = null
    private var requester: GeneralNames? = null
    private var requestPolicy: PolicyInformation? = null
    private var dvcs: GeneralNames? = null
    private var dataLocations: GeneralNames? = null
    private var extensions: Extensions? = null

    constructor(service: ServiceType) {
        this.service = service
    }

    constructor(initialInfo: DVCSRequestInformation) {
        this.initialInfo = initialInfo
        this.service = initialInfo.service
        this.version = initialInfo.version
        this.nonce = initialInfo.nonce
        this.requestTime = initialInfo.requestTime
        this.requestPolicy = initialInfo.requestPolicy
        this.dvcs = initialInfo.dvcs
        this.dataLocations = initialInfo.dataLocations
    }

    fun build(): DVCSRequestInformation {
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

        return DVCSRequestInformation.getInstance(DERSequence(v))
    }

    fun setVersion(version: Int) {
        if (initialInfo != null) {
            throw IllegalStateException("cannot change version in existing DVCSRequestInformation")
        }

        this.version = version
    }

    fun setNonce(nonce: BigInteger) {
        // RFC 3029, 9.1: The DVCS MAY modify the fields
        // 'dvcs', 'requester', 'dataLocations', and 'nonce' of the ReqInfo structure

        // RFC 3029, 9.1: The only modification
        // allowed to a 'nonce' is the inclusion of a new field if it was not
        // present, or to concatenate other data to the end (right) of an
        // existing value.
        if (initialInfo != null) {
            if (initialInfo.nonce == null) {
                this.nonce = nonce
            } else {
                val initialBytes = initialInfo.nonce.toByteArray()
                val newBytes = BigIntegers.asUnsignedByteArray(nonce)
                val nonceBytes = ByteArray(initialBytes.size + newBytes.size)

                System.arraycopy(initialBytes, 0, nonceBytes, 0, initialBytes.size)
                System.arraycopy(newBytes, 0, nonceBytes, initialBytes.size, newBytes.size)

                this.nonce = BigInteger(nonceBytes)
            }
        }

        this.nonce = nonce
    }

    fun setRequestTime(requestTime: DVCSTime) {
        if (initialInfo != null) {
            throw IllegalStateException("cannot change request time in existing DVCSRequestInformation")
        }

        this.requestTime = requestTime
    }

    fun setRequester(requester: GeneralName) {
        this.setRequester(GeneralNames(requester))
    }

    fun setRequester(requester: GeneralNames) {
        // RFC 3029, 9.1: The DVCS MAY modify the fields
        // 'dvcs', 'requester', 'dataLocations', and 'nonce' of the ReqInfo structure

        this.requester = requester
    }

    fun setRequestPolicy(requestPolicy: PolicyInformation) {
        if (initialInfo != null) {
            throw IllegalStateException("cannot change request policy in existing DVCSRequestInformation")
        }

        this.requestPolicy = requestPolicy
    }

    fun setDVCS(dvcs: GeneralName) {
        this.setDVCS(GeneralNames(dvcs))
    }

    fun setDVCS(dvcs: GeneralNames) {
        // RFC 3029, 9.1: The DVCS MAY modify the fields
        // 'dvcs', 'requester', 'dataLocations', and 'nonce' of the ReqInfo structure

        this.dvcs = dvcs
    }

    fun setDataLocations(dataLocation: GeneralName) {
        this.setDataLocations(GeneralNames(dataLocation))
    }

    fun setDataLocations(dataLocations: GeneralNames) {
        // RFC 3029, 9.1: The DVCS MAY modify the fields
        // 'dvcs', 'requester', 'dataLocations', and 'nonce' of the ReqInfo structure

        this.dataLocations = dataLocations
    }

    fun setExtensions(extensions: Extensions) {
        if (initialInfo != null) {
            throw IllegalStateException("cannot change extensions in existing DVCSRequestInformation")
        }

        this.extensions = extensions
    }

    companion object {

        private val DEFAULT_VERSION = 1
        private val TAG_REQUESTER = 0
        private val TAG_REQUEST_POLICY = 1
        private val TAG_DVCS = 2
        private val TAG_DATA_LOCATIONS = 3
        private val TAG_EXTENSIONS = 4
    }
}
