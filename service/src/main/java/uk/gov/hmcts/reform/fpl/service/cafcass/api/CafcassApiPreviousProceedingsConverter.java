package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ProceedingStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiProceeding;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class CafcassApiPreviousProceedingsConverter implements CafcassApiCaseDataConverter {
    private static final CafcassApiProceeding EMPTY = CafcassApiProceeding.builder().build();
    private static final List<String> SOURCE = List.of("data.proceeding");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.previousProceedings(getCafcassApiProceeding(caseData));
    }

    private List<CafcassApiProceeding> getCafcassApiProceeding(CaseData caseData) {
        return unwrapElements(caseData.getProceedings()).stream()
            .map(proceeding -> CafcassApiProceeding.builder()
                .proceedingStatus(ofNullable(proceeding.getProceedingStatus())
                    .map(ProceedingStatus::getValue).orElse(null))
                .caseNumber(proceeding.getCaseNumber())
                .started(proceeding.getStarted())
                .ended(proceeding.getEnded())
                .ordersMade(proceeding.getOrdersMade())
                .judge(proceeding.getJudge())
                .children(proceeding.getChildren())
                .guardian(proceeding.getGuardian())
                .sameGuardianNeeded(ofNullable(proceeding.getSameGuardianNeeded()).map(YES::equals).orElse(null))
                .sameGuardianDetails(proceeding.getSameGuardianDetails())
                .build())
            .filter(proceeding -> !EMPTY.equals(proceeding))
            .toList();
    }
}
