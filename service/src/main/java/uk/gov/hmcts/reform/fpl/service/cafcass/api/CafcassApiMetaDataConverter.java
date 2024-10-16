package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

import java.util.List;
import java.util.Optional;

@Service
public class CafcassApiMetaDataConverter implements CafcassApiCaseDataConverter {
    private static final List<String> SOURCE = List.of(
        "data.familyManCaseNumber", "data.dateSubmitted", "data.ordersSolicitor", "data.orders", "data.dateOfIssue",
        "data.isLocalAuthority", "data.relatingLA");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        final boolean citizenIsApplicant = YesNo.NO.equals(caseData.getIsLocalAuthority());
        return builder
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateSubmitted(caseData.getDateSubmitted())
            .applicationType(caseData.isC1Application() ? "C1" : "C110A")
            .ordersSought(Optional.ofNullable(caseData.getOrders()).orElse(Orders.builder().build()).getOrderType())
            .dateOfCourtIssue(caseData.getDateOfIssue())
            .citizenIsApplicant(citizenIsApplicant)
            .applicantLA(citizenIsApplicant ? null : caseData.getCaseLocalAuthority())
            .respondentLA(citizenIsApplicant ? caseData.getRelatingLA() : null);
    }
}
