package uk.gov.hmcts.reform.fpl.enums.orders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageOrdersChildAssessmentType {
    MEDICAL_ASSESSMENT("Medical Assessment"),
    PSYCHIATRIC_ASSESSMENT("Psychiatric Assessment");

    private final String title;
}
