package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiInternationalElement;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Service
public class CafcassApiInternationalElementConverter implements CafcassApiCaseDataConverter {
    private static final CafcassApiInternationalElement EMPTY = CafcassApiInternationalElement.builder().build();
    private static final List<String> SOURCE = List.of("data.internationalElement");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.internationalElement(getCafcassApiInternationalElement(caseData));
    }

    private CafcassApiInternationalElement getCafcassApiInternationalElement(CaseData caseData) {
        CafcassApiInternationalElement.CafcassApiInternationalElementBuilder builder =
            CafcassApiInternationalElement.builder();

        final InternationalElement internationalElement = caseData.getInternationalElement();
        if (internationalElement != null) {
            builder = builder.possibleCarer(isYes(internationalElement.getPossibleCarer()))
                .possibleCarerReason(internationalElement.getPossibleCarerReason())
                .significantEvents(isYes(internationalElement.getSignificantEvents()))
                .significantEventsReason(internationalElement.getSignificantEventsReason())
                .issues(isYes(internationalElement.getIssues()))
                .issuesReason(internationalElement.getIssuesReason())
                .proceedings(isYes(internationalElement.getProceedings()))
                .proceedingsReason(internationalElement.getProceedingsReason())
                .internationalAuthorityInvolvement(isYes(internationalElement.getInternationalAuthorityInvolvement()))
                .internationalAuthorityInvolvementDetails(internationalElement
                    .getInternationalAuthorityInvolvementDetails());
        }

        CafcassApiInternationalElement cafcassApiInternationalElement = builder.build();
        return EMPTY.equals(cafcassApiInternationalElement) ? null : cafcassApiInternationalElement;
    }
}
