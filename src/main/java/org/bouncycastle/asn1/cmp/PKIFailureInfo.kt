package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.DERBitString

/**
 *
 * PKIFailureInfo ::= BIT STRING {
 * badAlg               (0),
 * -- unrecognized or unsupported Algorithm Identifier
 * badMessageCheck      (1), -- integrity check failed (e.g., signature did not verify)
 * badRequest           (2),
 * -- transaction not permitted or supported
 * badTime              (3), -- messageTime was not sufficiently close to the system time, as defined by local policy
 * badCertId            (4), -- no certificate could be found matching the provided criteria
 * badDataFormat        (5),
 * -- the data submitted has the wrong format
 * wrongAuthority       (6), -- the authority indicated in the request is different from the one creating the response token
 * incorrectData        (7), -- the requester's data is incorrect (for notary services)
 * missingTimeStamp     (8), -- when the timestamp is missing but should be there (by policy)
 * badPOP               (9)  -- the proof-of-possession failed
 * certRevoked         (10),
 * certConfirmed       (11),
 * wrongIntegrity      (12),
 * badRecipientNonce   (13),
 * timeNotAvailable    (14),
 * -- the TSA's time source is not available
 * unacceptedPolicy    (15),
 * -- the requested TSA policy is not supported by the TSA
 * unacceptedExtension (16),
 * -- the requested extension is not supported by the TSA
 * addInfoNotAvailable (17)
 * -- the additional information requested could not be understood
 * -- or is not available
 * badSenderNonce      (18),
 * badCertTemplate     (19),
 * signerNotTrusted    (20),
 * transactionIdInUse  (21),
 * unsupportedVersion  (22),
 * notAuthorized       (23),
 * systemUnavail       (24),
 * systemFailure       (25),
 * -- the request cannot be handled due to system failure
 * duplicateCertReq    (26)
 *
 */
class PKIFailureInfo : DERBitString {
    /**
     * Basic constructor.
     */
    constructor(
            info: Int) : super(ASN1BitString.getBytes(info), ASN1BitString.getPadBits(info)) {
    }

    constructor(
            info: DERBitString) : super(info.bytes, info.getPadBits()) {
    }

    override fun toString(): String {
        return "PKIFailureInfo: 0x" + Integer.toHexString(this.intValue())
    }

    companion object {
        val badAlg = 1 shl 7 // unrecognized or unsupported Algorithm Identifier
        val badMessageCheck = 1 shl 6 // integrity check failed (e.g., signature did not verify)
        val badRequest = 1 shl 5
        val badTime = 1 shl 4 // -- messageTime was not sufficiently close to the system time, as defined by local policy
        val badCertId = 1 shl 3 // no certificate could be found matching the provided criteria
        val badDataFormat = 1 shl 2
        val wrongAuthority = 1 shl 1 // the authority indicated in the request is different from the one creating the response token
        val incorrectData = 1        // the requester's data is incorrect (for notary services)
        val missingTimeStamp = 1 shl 15 // when the timestamp is missing but should be there (by policy)
        val badPOP = 1 shl 14 // the proof-of-possession failed
        val certRevoked = 1 shl 13
        val certConfirmed = 1 shl 12
        val wrongIntegrity = 1 shl 11
        val badRecipientNonce = 1 shl 10
        val timeNotAvailable = 1 shl 9 // the TSA's time source is not available
        val unacceptedPolicy = 1 shl 8 // the requested TSA policy is not supported by the TSA
        val unacceptedExtension = 1 shl 23 //the requested extension is not supported by the TSA
        val addInfoNotAvailable = 1 shl 22 //the additional information requested could not be understood or is not available
        val badSenderNonce = 1 shl 21
        val badCertTemplate = 1 shl 20
        val signerNotTrusted = 1 shl 19
        val transactionIdInUse = 1 shl 18
        val unsupportedVersion = 1 shl 17
        val notAuthorized = 1 shl 16
        val systemUnavail = 1 shl 31
        val systemFailure = 1 shl 30 //the request cannot be handled due to system failure
        val duplicateCertReq = 1 shl 29


        @Deprecated("use lower case version ")
        val BAD_ALG = badAlg // unrecognized or unsupported Algorithm Identifier

        @Deprecated("use lower case version ")
        val BAD_MESSAGE_CHECK = badMessageCheck

        @Deprecated("use lower case version ")
        val BAD_REQUEST = badRequest // transaction not permitted or supported

        @Deprecated("use lower case version ")
        val BAD_TIME = badTime

        @Deprecated("use lower case version ")
        val BAD_CERT_ID = badCertId

        @Deprecated("use lower case version ")
        val BAD_DATA_FORMAT = badDataFormat // the data submitted has the wrong format

        @Deprecated("use lower case version ")
        val WRONG_AUTHORITY = wrongAuthority

        @Deprecated("use lower case version ")
        val INCORRECT_DATA = incorrectData

        @Deprecated("use lower case version ")
        val MISSING_TIME_STAMP = missingTimeStamp

        @Deprecated("use lower case version ")
        val BAD_POP = badPOP

        @Deprecated("use lower case version ")
        val TIME_NOT_AVAILABLE = timeNotAvailable

        @Deprecated("use lower case version ")
        val UNACCEPTED_POLICY = unacceptedPolicy

        @Deprecated("use lower case version ")
        val UNACCEPTED_EXTENSION = unacceptedExtension

        @Deprecated("use lower case version ")
        val ADD_INFO_NOT_AVAILABLE = addInfoNotAvailable

        @Deprecated("use lower case version ")
        val SYSTEM_FAILURE = systemFailure
    }
}
