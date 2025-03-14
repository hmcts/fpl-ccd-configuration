package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class CaseSummaryPeopleInCaseGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryNumberOfChildren(generateSummaryNumberOfChildren(caseData))
            .caseSummaryLASolicitorName(generateSummaryMainContactName(caseData))
            .caseSummaryLASolicitorEmail(generateSummaryMainContactEmail(caseData))
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
            .filter(representative -> RepresentativeRole.CAFCASS_GUARDIAN.equals(representative.getRole()))
            .collect(Collectors.toList());

        if (cafcasGuardians.isEmpty()) {
            return null;
        }

        return cafcasGuardians.stream().map(Representative::getFullName).collect(Collectors.joining(", "));
    }

    private String generateSummaryMainContactEmail(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            if (nonNull(caseData.getDesignatedLocalAuthority())) {
                return caseData.getDesignatedLocalAuthority().getMainContact()
                    .map(Colleague::getEmail)
                    .orElse(null);
            } else {
                LocalAuthority applicant = caseData.getLocalAuthorities().stream()
                    .map(Element::getValue)
                    .findFirst()
                    .orElse(null);

                return nonNull(applicant) ? applicant.getMainContact()
                    .map(Colleague::getEmail)
                    .orElse(null) : null;
            }
        }

        return ofNullable(caseData.getSolicitor()).map(Solicitor::getEmail).orElse(null);
    }

    private String generateSummaryMainContactName(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            if (nonNull(caseData.getDesignatedLocalAuthority())) {
                return caseData.getDesignatedLocalAuthority().getMainContact()
                    .map(Colleague::buildFullName)
                    .orElse(null);
            } else {
                LocalAuthority applicant = caseData.getLocalAuthorities().stream()
                    .map(Element::getValue)
                    .findFirst()
                    .orElse(null);

                return nonNull(applicant) ? applicant.getMainContact()
                    .map(Colleague::buildFullName)
                    .orElse(null) : null;
            }
        }

        return ofNullable(caseData.getSolicitor()).map(Solicitor::getName).orElse(null);
    }

    private Integer generateSummaryNumberOfChildren(CaseData caseData) {
        return ofNullable(caseData.getChildren1())
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
