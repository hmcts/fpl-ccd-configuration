package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum DirectionType {
    REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE,
    REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS,
    ASK_FOR_DISCLOSURE,
    ATTEND_HEARING,
    CONTACT_ALTERNATIVE_CARERS,
    SEND_DOCUMENTS_TO_ALL_PARTIES,
    SEND_MISSING_ANNEX,
    IDENTIFY_ALTERNATIVE_CARERS,
    SEND_TRANSLATED_DOCUMENTS,
    LODGE_BUNDLE,
    SEND_CASE_SUMMARY,
    CONSIDER_JURISDICTION,
    SEND_RESPONSE_TO_THRESHOLD_STATEMENT,
    ARRANGE_ADVOCATES_MEETING,
    SEND_GUARDIANS_ANALYSIS,
    APPOINT_CHILDREN_GUARDIAN,
    OBJECT_TO_REQUEST_FOR_DISCLOSURE,
    ARRANGE_INTERPRETERS,
    CUSTOM(false);

    private final boolean isStandard;

    DirectionType(boolean standard) {
        this.isStandard = standard;
    }

    DirectionType() {
        this(true);
    }

    @JsonIgnore
    public boolean isStandard() {
        return isStandard;
    }

    @JsonIgnore
    public String getFieldName() {
        return "direction-" + this.name();
    }

}
