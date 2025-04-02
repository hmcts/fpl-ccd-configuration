package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.InterlocutoryApplicant;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantsListGenerator {

    private static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";
    private static final String SEPARATOR = ", ";
    private final DynamicListService dynamicLists;

    public OrderApplicant getApplicant(CaseData caseData, AdditionalApplicationsBundle bundle) {
        String applicantName = getApplicantNameFromBundle(bundle);

        Map<String, String> applicants = new HashMap<>();
        buildApplicantsList(caseData).getListItems().forEach(
            applicant -> applicants.put(
                StringUtils.substringBefore(applicant.getLabel(), SEPARATOR),
                StringUtils.substringAfter(applicant.getLabel(), SEPARATOR)));

        ApplicantType type = isNotEmpty(applicants.get(applicantName))
            ? ApplicantType.fromType(StringUtils.substringBefore(applicants.get(applicantName), SPACE))
            : ApplicantType.OTHER;

        return OrderApplicant.builder().name(applicantName).type(type).build();
    }

    private String getApplicantNameFromBundle(AdditionalApplicationsBundle bundle) {
        C2DocumentBundle c2DocumentBundle = defaultIfNull(bundle.getC2DocumentBundle(),
            bundle.getC2DocumentBundleConfidential());
        return !isNull(c2DocumentBundle)
            ? StringUtils.substringBefore(
            defaultIfNull(c2DocumentBundle.getApplicantName(), EMPTY), SEPARATOR)
            : StringUtils.substringBefore(
            defaultIfNull(bundle.getOtherApplicationsBundle().getApplicantName(), EMPTY), SEPARATOR);
    }

    public DynamicList buildApplicantsList(CaseData caseData) {
        return buildApplicantsList(caseData, true);
    }

    public DynamicList buildApplicantsList(CaseData caseData, boolean withOthersOption) {

        List<InterlocutoryApplicant> applicantsFullNames = new ArrayList<>();

        // Main applicant
        if (isNotEmpty(caseData.getCaseLocalAuthorityName())) {
            applicantsFullNames.add(InterlocutoryApplicant.builder()
                .code("applicant")
                .name(caseData.getCaseLocalAuthorityName() + ", Applicant").build());
        }

        caseData.getSecondaryLocalAuthority()
            .map(LocalAuthority::getName)
            .map(localAuthorityName -> InterlocutoryApplicant.builder()
                .code("secondaryLocalAuthority")
                .name(localAuthorityName + ", Secondary LA").build())
            .ifPresent(applicantsFullNames::add);

        applicantsFullNames.addAll(buildRespondentNameElements(caseData.getAllRespondents()));
        applicantsFullNames.addAll(buildChildNameElements(caseData.getAllChildren()));

        if (withOthersOption) {
            applicantsFullNames.addAll(buildOthersElements(caseData.getOthersV2())); // Others to give notice
            applicantsFullNames.add(
                InterlocutoryApplicant.builder().code(APPLICANT_SOMEONE_ELSE).name("Someone else").build());
        }

        return dynamicLists.asDynamicList(
            applicantsFullNames,
            InterlocutoryApplicant::getCode,
            InterlocutoryApplicant::getName);
    }

    private List<InterlocutoryApplicant> buildOthersElements(List<Element<Other>> others) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> applicants = new ArrayList<>();

        others.forEach(other -> applicants.add(
            InterlocutoryApplicant.builder()
                .code(String.valueOf(other.getId()))
                .name(other.getValue().getFullName() + ", Other to be given notice " + i.getAndIncrement())
                .build())
        );

        return applicants;
    }

    private List<InterlocutoryApplicant> buildRespondentNameElements(List<Element<Respondent>> respondents) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> applicants = new ArrayList<>();

        respondents.forEach(respondent -> applicants.add(
            InterlocutoryApplicant.builder().code(respondent.getId().toString())
                .name(respondent.getValue().getParty().getFullName() + ", Respondent " + i.getAndIncrement())
                .build())
        );

        return applicants;
    }

    private List<InterlocutoryApplicant> buildChildNameElements(List<Element<Child>> chilren) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> applicants = new ArrayList<>();

        chilren.forEach(child -> applicants.add(
            InterlocutoryApplicant.builder().code(child.getId().toString())
                .name(child.getValue().getParty().getFullName() + ", Child " + i.getAndIncrement())
                .build())
        );

        return applicants;
    }

}
