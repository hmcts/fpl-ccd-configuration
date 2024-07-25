package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiProceeding;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Component
public class CafcassApiPreviousProceedingsConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.previousProceedings(getCafcassApiProceeding(caseData));
    }

    private List<CafcassApiProceeding> getCafcassApiProceeding(CaseData caseData) {
        return caseData.getAllProceedings().stream()
            .map(Element::getValue)
            .map(proceeding -> CafcassApiProceeding.builder()
                .proceedingStatus(proceeding.getProceedingStatus())
                .caseNumber(proceeding.getCaseNumber())
                .started(proceeding.getStarted())
                .ended(proceeding.getEnded())
                .ordersMade(proceeding.getOrdersMade())
                .judge(proceeding.getJudge())
                .children(proceeding.getChildren())
                .guardian(proceeding.getGuardian())
                .sameGuardianNeeded(isYes(proceeding.getSameGuardianNeeded()))
                .sameGuardianDetails(proceeding.getSameGuardianDetails())
                .build())
            .toList();
    }
}
