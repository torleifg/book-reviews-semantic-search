package com.github.torleifg.semanticsearch.book.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MetadataDTO {
    private String externalId;
    private boolean deleted;

    private String isbn;
    private String title;
    private String publisher;

    private List<Contributor> contributors = new ArrayList<>();

    private String publishedYear;
    private String description;

    private List<Classification> about = new ArrayList<>();
    private List<Classification> genreAndForm = new ArrayList<>();

    public record Classification(String id, String source, List<LocalizedString> names) {
    }

    public record LocalizedString(String language, String text) {
    }

    private URI thumbnailUrl;

    public record Contributor(List<Role> roles, String name) {

        public enum Role {
            ABR,
            ACT,
            ADP,
            RCP,
            ANL,
            ANM,
            ANN,
            ANC,
            APL,
            APE,
            APP,
            ARC,
            ARR,
            ACP,
            ADI,
            ART,
            ARD,
            ASG,
            ASN,
            ATT,
            AUC,
            AUE,
            AUP,
            AUT,
            AQT,
            AFT,
            AUD,
            AUI,
            ATO,
            ANT,
            BND,
            BDD,
            BLW,
            BKA,
            BKD,
            BKP,
            BJD,
            BPD,
            BSL,
            BRL,
            BRD,
            CLL,
            COP,
            CTG,
            CAS,
            CAD,
            CNS,
            CHR,
            CNG,
            CLI,
            COR,
            COL,
            CLT,
            CLR,
            CMM,
            CWT,
            COM,
            CPL,
            CPT,
            CPE,
            CMP,
            CMT,
            CCP,
            CND,
            CON,
            CSL,
            CSP,
            COS,
            COT,
            COE,
            CTS,
            CTT,
            CTE,
            CTR,
            CTB,
            CPC,
            CPH,
            CRR,
            CRP,
            CST,
            COU,
            CRT,
            COV,
            CRE,
            CUR,
            DNC,
            DTC,
            DTM,
            DTE,
            DTO,
            DFD,
            DFT,
            DFE,
            DGC,
            DGG,
            DGS,
            DLN,
            DPC,
            DPT,
            DSR,
            DRT,
            DIS,
            DBP,
            DST,
            DJO,
            DNR,
            DRM,
            DBD,
            DUB,
            EDT,
            EDC,
            EDM,
            EDD,
            ELG,
            ELT,
            ENJ,
            ENG,
            EGR,
            ETR,
            EVP,
            EXP,
            FAC,
            FLD,
            FMD,
            FDS,
            FLM,
            FMP,
            FMK,
            FPY,
            FRG,
            FMO,
            FON,
            FND,
            GDV,
            GIS,
            HNR,
            HST,
            HIS,
            ILU,
            ILL,
            INS,
            ITR,
            IVE,
            IVR,
            INV,
            ISB,
            JUD,
            JUG,
            LBR,
            LDR,
            LSA,
            LED,
            LEN,
            LTR,
            LIL,
            LBT,
            LSE,
            LSO,
            LGD,
            LTG,
            LYR,
            MKA,
            MFP,
            MFR,
            MRB,
            MRK,
            MDC,
            MTE,
            MTK,
            MXE,
            MOD,
            MON,
            MCP,
            MUP,
            MSD,
            MUS,
            NRT,
            NAN,
            OPN,
            ORM,
            ORG,
            OTH,
            OWN,
            PAN,
            PPM,
            PTA,
            PTH,
            PAT,
            PRF,
            PMA,
            PHT,
            PRT,
            POP,
            PRM,
            PRC,
            PRO,
            PRN,
            PRS,
            PMN,
            PRD,
            PRP,
            PRG,
            PDR,
            PFR,
            PRV,
            PUP,
            PBL,
            PBD,
            PPT,
            RDD,
            RPC,
            RAP,
            RCE,
            RCD,
            RED,
            RXA,
            REN,
            RPT,
            RPS,
            RTH,
            RTM,
            RES,
            RSP,
            RST,
            RSE,
            RSF,
            RSR,
            REV,
            RBR,
            SCE,
            SAD,
            AUS,
            SCR,
            SCL,
            SPY,
            SEC,
            SLL,
            STD,
            SGN,
            SNG,
            SWD,
            SDS,
            SDE,
            SPK,
            SFX,
            SPN,
            SGD,
            STM,
            STN,
            STR,
            STL,
            SHT,
            SRV,
            TCH,
            TAD,
            TCD,
            TLD,
            TLG,
            TLH,
            TLP,
            TAU,
            THS,
            TRC,
            TRL,
            TYD,
            TYG,
            UVP,
            VDG,
            VFX,
            VAC,
            WIT,
            WDE,
            WDC,
            WAM,
            WAC,
            WAL,
            WAT,
            WIN,
            WPR,
            WST,
            WTS
        }
    }
}
