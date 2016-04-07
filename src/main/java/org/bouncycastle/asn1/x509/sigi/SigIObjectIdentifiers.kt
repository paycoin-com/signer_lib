package org.bouncycastle.asn1.x509.sigi

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * Object Identifiers of SigI specifciation (German Signature Law
 * Interoperability specification).
 */
interface SigIObjectIdentifiers {
    companion object {
        /**
         * OID: 1.3.36.8
         */
        val id_sigi = ASN1ObjectIdentifier("1.3.36.8")

        /**
         * Key purpose IDs for German SigI (Signature Interoperability
         * Specification)
         *
         *
         * OID: 1.3.36.8.2
         */
        val id_sigi_kp = ASN1ObjectIdentifier("1.3.36.8.2")

        /**
         * Certificate policy IDs for German SigI (Signature Interoperability
         * Specification)
         *
         *
         * OID: 1.3.36.8.1
         */
        val id_sigi_cp = ASN1ObjectIdentifier("1.3.36.8.1")

        /**
         * Other Name IDs for German SigI (Signature Interoperability Specification)
         *
         *
         * OID: 1.3.36.8.4
         */
        val id_sigi_on = ASN1ObjectIdentifier("1.3.36.8.4")

        /**
         * To be used for for the generation of directory service certificates.
         *
         *
         * OID: 1.3.36.8.2.1
         */
        val id_sigi_kp_directoryService = ASN1ObjectIdentifier("1.3.36.8.2.1")

        /**
         * ID for PersonalData
         *
         *
         * OID: 1.3.36.8.4.1
         */
        val id_sigi_on_personalData = ASN1ObjectIdentifier("1.3.36.8.4.1")

        /**
         * Certificate is conformant to german signature law.
         *
         *
         * OID: 1.3.36.8.1.1
         */
        val id_sigi_cp_sigconform = ASN1ObjectIdentifier("1.3.36.8.1.1")
    }

}
