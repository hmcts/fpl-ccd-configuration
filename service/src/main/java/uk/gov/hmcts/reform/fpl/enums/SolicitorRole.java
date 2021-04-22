package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[SOLICITORA]", 0),
    SOLICITORB("[SOLICITORB]", 1),
    SOLICITORC("[SOLICITORC]", 2),
    SOLICITORD("[SOLICITORD]", 3),
    SOLICITORE("[SOLICITORE]", 4),
    SOLICITORF("[SOLICITORF]", 5),
    SOLICITORG("[SOLICITORG]", 6),
    SOLICITORH("[SOLICITORH]", 6),
    SOLICITORI("[SOLICITORI]", 8),
    SOLICITORJ("[SOLICITORJ]", 9);

    private final String caseRoleLabel;
    private final int index;

    public static SolicitorRole from(String label) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst()
            .orElseThrow();
    }
}
