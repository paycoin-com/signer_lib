package org.bouncycastle.asn1.eac

import org.bouncycastle.asn1.ASN1ApplicationSpecific
import org.bouncycastle.asn1.BERTags

object EACTags {
    val OBJECT_IDENTIFIER = 0x06
    val COUNTRY_CODE_NATIONAL_DATA = 0x41
    val ISSUER_IDENTIFICATION_NUMBER = 0x02 //0x42;
    val CARD_SERVICE_DATA = 0x43
    val INITIAL_ACCESS_DATA = 0x44
    val CARD_ISSUER_DATA = 0x45
    val PRE_ISSUING_DATA = 0x46
    val CARD_CAPABILITIES = 0x47
    val STATUS_INFORMATION = 0x48
    val EXTENDED_HEADER_LIST = 0x4D
    val APPLICATION_IDENTIFIER = 0x4F
    val APPLICATION_LABEL = 0x50
    val FILE_REFERENCE = 0x51
    val COMMAND_TO_PERFORM = 0x52
    val DISCRETIONARY_DATA = 0x53
    val OFFSET_DATA_OBJECT = 0x54
    val TRACK1_APPLICATION = 0x56
    val TRACK2_APPLICATION = 0x57
    val TRACK3_APPLICATION = 0x58
    val CARD_EXPIRATION_DATA = 0x59
    val PRIMARY_ACCOUNT_NUMBER = 0x5A// PAN
    val NAME = 0x5B
    val TAG_LIST = 0x5C
    val HEADER_LIST = 0x5D
    val LOGIN_DATA = 0x5E
    val CARDHOLDER_NAME = 0x20 // 0x5F20;
    val TRACK1_CARD = 0x5F21
    val TRACK2_CARD = 0x5F22
    val TRACK3_CARD = 0x5F23
    val APPLICATION_EXPIRATION_DATE = 0x24 // 0x5F24;
    val APPLICATION_EFFECTIVE_DATE = 0x25 // 0x5F25;
    val CARD_EFFECTIVE_DATE = 0x5F26
    val INTERCHANGE_CONTROL = 0x5F27
    val COUNTRY_CODE = 0x5F28
    val INTERCHANGE_PROFILE = 0x29 // 0x5F29;
    val CURRENCY_CODE = 0x5F2A
    val DATE_OF_BIRTH = 0x5F2B
    val CARDHOLDER_NATIONALITY = 0x5F2C
    val LANGUAGE_PREFERENCES = 0x5F2D
    val CARDHOLDER_BIOMETRIC_DATA = 0x5F2E
    val PIN_USAGE_POLICY = 0x5F2F
    val SERVICE_CODE = 0x5F30
    val TRANSACTION_COUNTER = 0x5F32
    val TRANSACTION_DATE = 0x5F33
    val CARD_SEQUENCE_NUMBER = 0x5F34
    val SEX = 0x5F35
    val CURRENCY_EXPONENT = 0x5F36
    val STATIC_INTERNAL_AUTHENTIFICATION_ONE_STEP = 0x37 // 0x5F37;
    val SIGNATURE = 0x5F37
    val STATIC_INTERNAL_AUTHENTIFICATION_FIRST_DATA = 0x5F38
    val STATIC_INTERNAL_AUTHENTIFICATION_SECOND_DATA = 0x5F39
    val DYNAMIC_INTERNAL_AUTHENTIFICATION = 0x5F3A
    val DYNAMIC_EXTERNAL_AUTHENTIFICATION = 0x5F3B
    val DYNAMIC_MUTUAL_AUTHENTIFICATION = 0x5F3C
    val CARDHOLDER_PORTRAIT_IMAGE = 0x5F40
    val ELEMENT_LIST = 0x5F41
    val ADDRESS = 0x5F42
    val CARDHOLDER_HANDWRITTEN_SIGNATURE = 0x5F43
    val APPLICATION_IMAGE = 0x5F44
    val DISPLAY_IMAGE = 0x5F45
    val TIMER = 0x5F46
    val MESSAGE_REFERENCE = 0x5F47
    val CARDHOLDER_PRIVATE_KEY = 0x5F48
    val CARDHOLDER_PUBLIC_KEY = 0x5F49
    val CERTIFICATION_AUTHORITY_PUBLIC_KEY = 0x5F4A
    val DEPRECATED = 0x5F4B
    val CERTIFICATE_HOLDER_AUTHORIZATION = 0x5F4C// Not yet defined in iso7816. The allocation is requested
    val INTEGRATED_CIRCUIT_MANUFACTURER_ID = 0x5F4D
    val CERTIFICATE_CONTENT = 0x5F4E
    val UNIFORM_RESOURCE_LOCATOR = 0x5F50
    val ANSWER_TO_RESET = 0x5F51
    val HISTORICAL_BYTES = 0x5F52
    val DIGITAL_SIGNATURE = 0x5F3D
    val APPLICATION_TEMPLATE = 0x61
    val FCP_TEMPLATE = 0x62
    val WRAPPER = 0x63
    val FMD_TEMPLATE = 0x64
    val CARDHOLDER_RELATIVE_DATA = 0x65
    val CARD_DATA = 0x66
    val AUTHENTIFICATION_DATA = 0x67
    val SPECIAL_USER_REQUIREMENTS = 0x68
    val LOGIN_TEMPLATE = 0x6A
    val QUALIFIED_NAME = 0x6B
    val CARDHOLDER_IMAGE_TEMPLATE = 0x6C
    val APPLICATION_IMAGE_TEMPLATE = 0x6D
    val APPLICATION_RELATED_DATA = 0x6E
    val FCI_TEMPLATE = 0x6F
    val DISCRETIONARY_DATA_OBJECTS = 0x73
    val COMPATIBLE_TAG_ALLOCATION_AUTHORITY = 0x78
    val COEXISTANT_TAG_ALLOCATION_AUTHORITY = 0x79
    val SECURITY_SUPPORT_TEMPLATE = 0x7A
    val SECURITY_ENVIRONMENT_TEMPLATE = 0x7B
    val DYNAMIC_AUTHENTIFICATION_TEMPLATE = 0x7C
    val SECURE_MESSAGING_TEMPLATE = 0x7D
    val NON_INTERINDUSTRY_DATA_OBJECT_NESTING_TEMPLATE = 0x7E
    val DISPLAY_CONTROL = 0x7F20
    val CARDHOLDER_CERTIFICATE = 0x21 // 0x7F21;
    val CV_CERTIFICATE = 0x7F21
    val CARDHOLER_REQUIREMENTS_INCLUDED_FEATURES = 0x7F22
    val CARDHOLER_REQUIREMENTS_EXCLUDED_FEATURES = 0x7F23
    val BIOMETRIC_DATA_TEMPLATE = 0x7F2E
    val DIGITAL_SIGNATURE_BLOCK = 0x7F3D
    val CARDHOLDER_PRIVATE_KEY_TEMPLATE = 0x7F48
    val CARDHOLDER_PUBLIC_KEY_TEMPLATE = 0x49 // 0x7F49;
    val CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE = 0x4C // 0x7F4C;
    val CERTIFICATE_CONTENT_TEMPLATE = 0x4E // 0x7F4E;
    val CERTIFICATE_BODY = 0x4E // 0x7F4E;
    val BIOMETRIC_INFORMATION_TEMPLATE = 0x7F60
    val BIOMETRIC_INFORMATION_GROUP_TEMPLATE = 0x7F61

    fun getTag(encodedTag: Int): Int {
        /*
        int i;
        for (i = 24; i>=0; i-=8) {
            if (((0xFF<<i) & tag) != 0)
                return (((0xFF<<i) & tag) >> i);
        }
        return 0;
        */
        return decodeTag(encodedTag)
    }

    fun getTagNo(tag: Int): Int {
        var i: Int
        i = 24
        while (i >= 0) {
            if (0xFF shl i and tag != 0) {
                return (0xFF shl i).inv() and tag
            }
            i -= 8
        }
        return 0
    }

    fun encodeTag(spec: ASN1ApplicationSpecific): Int {
        var retValue = BERTags.APPLICATION
        val constructed = spec.isConstructed
        if (constructed) {
            retValue = retValue or BERTags.CONSTRUCTED
        }

        var tag = spec.applicationTag

        if (tag > 31) {
            retValue = retValue or 0x1F
            retValue = retValue shl 8

            var currentByte = tag and 0x7F
            retValue = retValue or currentByte
            tag = tag shr 7

            while (tag > 0) {
                retValue = retValue or 0x80
                retValue = retValue shl 8

                currentByte = tag and 0x7F
                tag = tag shr 7
            }
        } else {
            retValue = retValue or tag
        }

        return retValue
    }

    fun decodeTag(tag: Int): Int {
        var retValue = 0
        var multiBytes = false
        var i = 24
        while (i >= 0) {
            val currentByte = tag shr i and 0xFF
            if (currentByte == 0) {
                i -= 8
                continue
            }

            if (multiBytes) {
                retValue = retValue shl 7
                retValue = retValue or (currentByte and 0x7F)
            } else if (currentByte and 0x1F == 0x1F) {
                multiBytes = true
            } else {
                return currentByte and 0x1F // higher order bit are for DER.Constructed and type
            }
            i -= 8
        }
        return retValue
    }
}
