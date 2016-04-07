package org.bouncycastle.asn1.smime

import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.Attribute

class SMIMECapabilitiesAttribute(
        capabilities: SMIMECapabilityVector) : Attribute(SMIMEAttributes.smimeCapabilities, DERSet(DERSequence(capabilities.toASN1EncodableVector())))
