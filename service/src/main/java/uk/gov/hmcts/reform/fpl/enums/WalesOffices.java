package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalesOffices {
    ABERYSTWYTH("Aberystwyth"),
    CAERNARFON("Caernarfon"),
    CARDIFF("Cardiff"),
    CARMARTHEN("Carmarthen"),
    LLANDRINDOD_WELLS("Llandrindod Wells"),
    LLANDUDNO_JUNCTION("Llandudno Junction"),
    MERTHYR_TYDFIL("Merthyr Tydfil"),
    NEWPORT("Newport"),
    NEWTON("Newton"),
    SWANSEA("Swansea"),
    WREXHAM("Wrexham");

    private final String label;
}
