package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocmosisImages {

    DRAFT_WATERMARK("[userImage:draft-watermark.png]"),
    COURT_SEAL("[userImage:family-court-seal.png]"),
    CREST("[userImage:crest.png]");

    private final String value;
}
