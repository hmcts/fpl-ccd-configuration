package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ChildSolicitorRole {
    CHILDSOLICITORA("[CHILDSOLICITORA]", 0),
    CHILDSOLICITORB("[CHILDSOLICITORB]", 1),
    CHILDSOLICITORC("[CHILDSOLICITORC]", 2),
    CHILDSOLICITORD("[CHILDSOLICITORD]", 3),
    CHILDSOLICITORE("[CHILDSOLICITORE]", 4),
    CHILDSOLICITORF("[CHILDSOLICITORF]", 5),
    CHILDSOLICITORG("[CHILDSOLICITORG]", 6),
    CHILDSOLICITORH("[CHILDSOLICITORH]", 7),
    CHILDSOLICITORI("[CHILDSOLICITORI]", 8),
    CHILDSOLICITORJ("[CHILDSOLICITORJ]", 9),
    CHILDSOLICITORK("[CHILDSOLICITORK]", 10),
    CHILDSOLICITORL("[CHILDSOLICITORL]", 11),
    CHILDSOLICITORM("[CHILDSOLICITORM]", 12),
    CHILDSOLICITORN("[CHILDSOLICITORN]", 13),
    CHILDSOLICITORO("[CHILDSOLICITORO]", 14);

    private final String caseRoleLabel;
    private final int index;

    public static ChildSolicitorRole from(String label) {
        return Arrays.stream(ChildSolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst()
            .orElseThrow();
    }
}
