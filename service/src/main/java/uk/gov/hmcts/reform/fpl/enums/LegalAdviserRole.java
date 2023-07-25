package uk.gov.hmcts.reform.fpl.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LegalAdviserRole {

    ALLOCATED_LEGAL_ADVISER("allocated-legal-adviser"),
    HEARING_LEGAL_ADVISER("hearing-legal-adviser");

    private final String roleName;

}
