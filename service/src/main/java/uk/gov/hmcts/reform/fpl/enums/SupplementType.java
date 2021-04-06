package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SupplementType {
    C13A_SPECIAL_GUARDIANSHIP("C13A - Special guardianship order"),
    C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD("C14 - Authority to refuse contact with a child in care"),
    C15_CONTACT_WITH_CHILD_IN_CARE("C15 - Contact with a child in care"),
    C16_CHILD_ASSESSMENT("C16 - Child assessment"),
    C18_RECOVERY_ORDER("C18 - Recovery order"),
    C20_SECURE_ACCOMMODATION("C20 - Secure accommodation");

    private final String label;
}
