package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlacementNoticeRecipientType {

    LOCAL_AUTHORITY("Local authority"),
    RESPONDENTS("Respondents (Parents)"),
    CAFCASS("Cafcass");

    private final String name;
}
