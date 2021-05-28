package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.InterlocutoryApplicant;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantsListGenerator {

    public static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";
    private final DynamicListService dynamicLists;

    public DynamicList buildApplicantsList(CaseData caseData) {

        List<InterlocutoryApplicant> applicantsFullNames = new ArrayList<>();

        // Main applicant
        if (isNotEmpty(caseData.getCaseLocalAuthorityName())) {
            applicantsFullNames.add(InterlocutoryApplicant.builder()
                .code("applicant")
                .name(caseData.getCaseLocalAuthorityName() + ", Applicant").build());
        }

        applicantsFullNames.addAll(buildRespondentNameElements(caseData.getAllRespondents()));
        applicantsFullNames.addAll(buildOthersElements(caseData.getAllOthers())); // Others to give notice

        applicantsFullNames.add(
            InterlocutoryApplicant.builder().code(APPLICANT_SOMEONE_ELSE).name("Someone else").build());

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
                .name(other.getValue().getName() + ", Other to be given notice " + i.getAndIncrement())
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

}
