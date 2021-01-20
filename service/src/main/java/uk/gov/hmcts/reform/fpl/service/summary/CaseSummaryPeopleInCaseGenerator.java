package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class CaseSummaryPeopleInCaseGenerator implements CaseSummaryFieldsGenerator {
    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryNumberOfChildren(caseData.getChildren1().size())
            .caseSummaryLASolicitorName(caseData.getSolicitor().getName())
            .caseSummaryLASolicitorEmail(caseData.getSolicitor().getEmail())
            .caseSummaryFirstRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .caseSummaryFirstRespondentLegalRep(getFirstRespondentRepresentativeFullName(caseData))
            .caseSummaryCafcassGuardian(
                unwrapElements(caseData.getRepresentatives()).stream()
                    .filter(representative -> representative.getRole().equals(RepresentativeRole.CAFCASS_GUARDIAN))
                    // can fullname be null?
                    .map(Representative::getFullName).collect(Collectors.joining(", "))
            ).build();
    }

    private String getFirstRespondentRepresentativeFullName(CaseData caseData) {
        return unwrapElements(caseData.getRepresentatives()).stream()
            .filter(representative -> REPRESENTING_RESPONDENT_1.equals(representative.getRole()))
            .findFirst()
            .map(Representative::getFullName)
            .orElse(null);
    }
}
