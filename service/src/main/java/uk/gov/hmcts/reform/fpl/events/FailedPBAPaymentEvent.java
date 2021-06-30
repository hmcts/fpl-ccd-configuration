package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class FailedPBAPaymentEvent {
    private final CaseData caseData;
    private final List<ApplicationType> applicationTypes;
    private final String applicantName;
}
