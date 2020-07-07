package uk.gov.hmcts.reform.fpl.enums;

public enum CMOStatus {
    SEND_TO_JUDGE,
    /**
     * Indicates that the CMO needs to be reviewed by other parties before progressing.
     *
     * @deprecated no longer used in the new CMO status model but preserved for old cases.
     */
    PARTIES_REVIEW,
    SELF_REVIEW
}
