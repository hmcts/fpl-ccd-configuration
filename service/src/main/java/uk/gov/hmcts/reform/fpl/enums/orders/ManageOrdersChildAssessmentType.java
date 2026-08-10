package uk.gov.hmcts.reform.fpl.enums.orders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ManageOrdersChildAssessmentType {
    @CCD(label = "Medical Assessment")
    MEDICAL_ASSESSMENT("Medical Assessment"),
    @CCD(label = "Psychiatric Assessment")
    PSYCHIATRIC_ASSESSMENT("Psychiatric Assessment");

    private final String title;
}
