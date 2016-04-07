package org.bouncycastle.asn1

/**
 * a general class for building up a vector of DER encodable objects -
 * this will eventually be superseded by ASN1EncodableVector so you should
 * use that class in preference.
 */
class DEREncodableVector
@Deprecated("use ASN1EncodableVector instead.")
constructor() : ASN1EncodableVector()
