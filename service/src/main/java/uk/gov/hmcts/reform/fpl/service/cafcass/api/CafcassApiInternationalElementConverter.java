package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiInternationalElement;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiCaseDataConverter;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.isYes;

@Component
public class CafcassApiInternationalElementConverter implements CafcassApiCaseDataConverter {
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

        return builder.build();
    }
}
