package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum C2ApplicationType {
    @CCD(label = "Application with notice.")
    WITH_NOTICE("With notice"),
    @CCD(label = "Application by consent. Parties will be notified of this application.")
    WITHOUT_NOTICE("By consent");

    private final String label;
}
