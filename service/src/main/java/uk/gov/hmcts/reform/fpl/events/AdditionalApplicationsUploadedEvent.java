package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AdditionalApplicationsUploadedEvent {
    private final CaseData caseData;
    private final CaseData caseDataBefore;
    private final OrderApplicant applicant;
    private final List<String> recipientsOfConfidentialC2;
}
