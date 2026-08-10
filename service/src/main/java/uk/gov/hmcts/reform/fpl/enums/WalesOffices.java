package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CafcassWalesOffices", generate = true)
@AllArgsConstructor
@Getter
public enum WalesOffices {
    @CCD(label = "Aberystwyth")
    ABERYSTWYTH("Aberystwyth"),
    @CCD(label = "Caernarfon")
    CAERNARFON("Caernarfon"),
    @CCD(label = "Cardiff")
    CARDIFF("Cardiff"),
    @CCD(label = "Carmarthen")
    CARMARTHEN("Carmarthen"),
    @CCD(label = "Llandrindod Wells")
    LLANDRINDOD_WELLS("Llandrindod Wells"),
    @CCD(label = "Llandudno Junction")
    LLANDUDNO_JUNCTION("Llandudno Junction"),
    @CCD(label = "Merthyr Tydfil")
    MERTHYR_TYDFIL("Merthyr Tydfil"),
    @CCD(label = "Newport")
    NEWPORT("Newport"),
    @CCD(label = "Newton")
    NEWTON("Newton"),
    @CCD(label = "Swansea")
    SWANSEA("Swansea"),
    @CCD(label = "Wrexham")
    WREXHAM("Wrexham");

    private final String label;
}
