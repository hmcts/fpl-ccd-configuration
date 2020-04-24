package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocmosisImages {

    DRAFT_WATERMARK("[userImage:draft-watermark.png]"),
    COURT_SEAL("[userImage:familycourtseal.png]"),
    CREST("[userImage:crest.png]"),
    HMCTS_LOGO_LARGE("[userImage:hmcts-logo-large.png]"),
    HMCTS_LOGO_SMALL("[userImage:hmcts-logo-small.png]");

    private final String value;
}
