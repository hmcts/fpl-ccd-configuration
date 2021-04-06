package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[solicitorA]"),
    SOLICITORB("[solicitorB]"),
    SOLICITORC("[solicitorC]"),
    SOLICITORD("[solicitorD]"),
    SOLICITORE("[solicitorE]"),
    SOLICITORF("[solicitorF]"),
    SOLICITORG("[solicitorG]"),
    SOLICITORH("[solicitorH]"),
    SOLICITORI("[solicitorI]"),
    SOLICITORJ("[solicitorJ]");

    private final String caseRoleLabel;
}
