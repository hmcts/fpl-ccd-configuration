package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFactorsParenting;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Service
public class CafcassApiFactorsParentingConverter implements CafcassApiCaseDataConverter {
    private static final CafcassApiFactorsParenting EMPTY = CafcassApiFactorsParenting.builder().build();
    private static final List<String> SOURCE = List.of("data.factorsParenting");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.factorsParenting(getCafcassApiFactorsParenting(caseData));
    }

    private CafcassApiFactorsParenting getCafcassApiFactorsParenting(CaseData caseData) {
        CafcassApiFactorsParenting.CafcassApiFactorsParentingBuilder builder = CafcassApiFactorsParenting.builder();

        FactorsParenting factorsParenting = caseData.getFactorsParenting();
        if (factorsParenting != null) {
            builder = builder.alcoholDrugAbuse(isYes(factorsParenting.getAlcoholDrugAbuse()))
                .alcoholDrugAbuseReason(factorsParenting.getAlcoholDrugAbuseReason())
                .domesticViolence(isYes(factorsParenting.getDomesticViolence()))
                .domesticViolenceReason(factorsParenting.getDomesticViolenceReason())
                .anythingElse(isYes(factorsParenting.getAnythingElse()))
                .anythingElseReason(factorsParenting.getAnythingElseReason());
        }

        CafcassApiFactorsParenting cafcassApiFactorsParenting = builder.build();
        return EMPTY.equals(cafcassApiFactorsParenting) ? null : cafcassApiFactorsParenting;
    }
}
