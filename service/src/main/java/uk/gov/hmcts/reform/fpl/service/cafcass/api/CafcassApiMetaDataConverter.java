package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

@Component
public class CafcassApiMetaDataConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateSubmitted(caseData.getDateSubmitted())
            .applicationType(caseData.isC1Application() ? "C1" : "C110A")
            .ordersSought(caseData.getOrders().getOrderType())
            .dateOfCourtIssue(caseData.getDateOfIssue())
            .citizenIsApplicant(YesNo.NO.equals(caseData.getIsLocalAuthority()))
            .applicantLA(caseData.getCaseLocalAuthority())
            .respondentLA(caseData.getRelatingLA());
    }
}
