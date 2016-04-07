package org.bouncycastle.asn1.icao

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**

 * { ISOITU(2) intorgs(23) icao(136) }
 */
interface ICAOObjectIdentifiers {
    companion object {
        //
        // base id
        //
        /**  2.23.136   */
        val id_icao = ASN1ObjectIdentifier("2.23.136")

        /**  2.23.136.1   */
        val id_icao_mrtd = id_icao.branch("1")
        /**  2.23.136.1.1   */
        val id_icao_mrtd_security = id_icao_mrtd.branch("1")

        /** LDS security object, see ICAO Doc 9303-Volume 2-Section IV-A3.2
         *
         *
         * 2.23.136.1.1.1   */
        val id_icao_ldsSecurityObject = id_icao_mrtd_security.branch("1")

        /** CSCA master list, see TR CSCA Countersigning and Master List issuance
         *
         *
         * 2.23.136.1.1.2
         */
        val id_icao_cscaMasterList = id_icao_mrtd_security.branch("2")
        /** 2.23.136.1.1.3  */
        val id_icao_cscaMasterListSigningKey = id_icao_mrtd_security.branch("3")

        /** document type list, see draft TR LDS and PKI Maintenance, par. 3.2.1
         *
         *
         * 2.23.136.1.1.4
         */
        val id_icao_documentTypeList = id_icao_mrtd_security.branch("4")

        /** Active Authentication protocol, see draft TR LDS and PKI Maintenance, par. 5.2.2
         *
         *
         * 2.23.136.1.1.5
         */
        val id_icao_aaProtocolObject = id_icao_mrtd_security.branch("5")

        /** CSCA name change and key reoll-over, see draft TR LDS and PKI Maintenance, par. 3.2.1
         *
         *
         * 2.23.136.1.1.6
         */
        val id_icao_extensions = id_icao_mrtd_security.branch("6")
        /** 2.23.136.1.1.6.1  */
        val id_icao_extensions_namechangekeyrollover = id_icao_extensions.branch("1")
    }
}
