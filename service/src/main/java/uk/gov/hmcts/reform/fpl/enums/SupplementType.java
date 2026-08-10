package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@AllArgsConstructor
@Getter
public enum SupplementType {
    @CCD(label = "C13A - Special guardianship order")
    C13A_SPECIAL_GUARDIANSHIP("C13A - Special guardianship order"),
    @CCD(label = "C14 - Authority to refuse contact with a child in care")
    C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD("C14 - Authority to refuse contact with a child in care"),
    @CCD(label = "C15 - Contact with a child in care")
    C15_CONTACT_WITH_CHILD_IN_CARE("C15 - Contact with a child in care"),
    @CCD(label = "C16 - Child assessment")
    C16_CHILD_ASSESSMENT("C16 - Child assessment"),
    @CCD(label = "C18 - Recovery order")
    C18_RECOVERY_ORDER("C18 - Recovery order"),
    @CCD(label = "C20 - Secure accommodation")
    C20_SECURE_ACCOMMODATION("C20 - Secure accommodation");

    private final String label;
}
