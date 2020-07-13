package uk.gov.hmcts.reform.fpl.enums;

public enum CMOStatus {
    /**
     * Indicates that the CMO is awaiting approval from the judge.
     */
    SEND_TO_JUDGE,
    /**
     * Indicates that the CMO needs to be reviewed by other parties before progressing.
     *
     * @deprecated no longer used in the new CMO status model but preserved for old cases.
     */
    @Deprecated
    PARTIES_REVIEW,
    /**
     * Indicates that the CMO is still being worked on by the LA or has been sent back to the LA by the judge.
     *
     * @deprecated no longer used in the new CMO status model but preserved for old cases.
     */
    @Deprecated
    SELF_REVIEW,
    /**
     * Indicates that the CMO has been approved.
     */
    APPROVED,

}
