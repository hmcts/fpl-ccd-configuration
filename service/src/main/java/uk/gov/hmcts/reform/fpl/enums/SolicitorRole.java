package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[SOLICITORA]"),
    SOLICITORB("[SOLICITORB]"),
    SOLICITORC("[SOLICITORC]"),
    SOLICITORD("[SOLICITORD]"),
    SOLICITORE("[SOLICITORE]"),
    SOLICITORF("[SOLICITORF]"),
    SOLICITORG("[SOLICITORG]"),
    SOLICITORH("[SOLICITORH]"),
    SOLICITORI("[SOLICITORI]"),
    SOLICITORJ("[SOLICITORJ]");

    private final String caseRoleLabel;
}
