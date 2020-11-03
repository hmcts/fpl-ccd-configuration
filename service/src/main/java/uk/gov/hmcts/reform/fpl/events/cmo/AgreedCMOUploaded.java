package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Getter
@RequiredArgsConstructor
public class AgreedCMOUploaded implements UploadCMOEvent {
    private final CaseData caseData;
    private final HearingBooking hearing;
}
