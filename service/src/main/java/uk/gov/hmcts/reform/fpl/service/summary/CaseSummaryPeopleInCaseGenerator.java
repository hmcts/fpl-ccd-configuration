package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class CaseSummaryPeopleInCaseGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryNumberOfChildren(generateSummaryNumberOfChildren(caseData))
            .caseSummaryLASolicitorName(generateSummaryLASolicitorName(caseData))
            .caseSummaryLASolicitorEmail(generateSummaryLASolicitorEmail(caseData))
            .caseSummaryFirstRespondentLastName(generateSummaryFirstRespondentLastName(caseData))
            .caseSummaryFirstRespondentLegalRep(getFirstRespondentRepresentativeFullName(caseData))
            .caseSummaryCafcassGuardian(generateSummaryCafcassGuardian(caseData))
            .build();
    }

    private String generateSummaryFirstRespondentLastName(CaseData caseData) {
        String firstRespondentLastName = getFirstRespondentLastName(caseData.getRespondents1());
        return firstRespondentLastName.isEmpty() ? null : firstRespondentLastName;
    }

    private String generateSummaryCafcassGuardian(CaseData caseData) {
        List<Representative> cafcasGuardians = unwrapElements(caseData.getRepresentatives()).stream()
            .filter(representative -> representative.getRole().equals(RepresentativeRole.CAFCASS_GUARDIAN))
            .collect(Collectors.toList());

        if (cafcasGuardians.isEmpty()) {
            return null;
        }

        return cafcasGuardians.stream().map(Representative::getFullName).collect(Collectors.joining(", "));
    }

    private String generateSummaryLASolicitorEmail(CaseData caseData) {
        return Optional.ofNullable(caseData.getSolicitor()).map(Solicitor::getEmail).orElse(null);
    }

    private String generateSummaryLASolicitorName(CaseData caseData) {
        return Optional.ofNullable(caseData.getSolicitor()).map(Solicitor::getName).orElse(null);
    }

    private Integer generateSummaryNumberOfChildren(CaseData caseData) {
        return Optional.ofNullable(caseData.getChildren1())
            .map(children -> children.isEmpty() ? null : children.size())
            .orElse(null);
    }

    private String getFirstRespondentRepresentativeFullName(CaseData caseData) {
        if (isNull(caseData.getRepresentatives())) {
            return null;
        }

        return unwrapElements(caseData.getRepresentatives()).stream()
            .filter(representative -> REPRESENTING_RESPONDENT_1.equals(representative.getRole()))
            .findFirst()
            .map(Representative::getFullName)
            .orElse(null);
    }
}
