package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentPolicyService {

    private final ObjectMapper mapper;
    private final NoticeOfChangeAnswersConverter noticeOfChangeRespondentConverter;
    private final RespondentPolicyConverter respondentPolicyConverter;

    public Map<String, Object> generateForSubmission(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>();

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Applicant firstApplicant = caseData.getAllApplicants().get(0).getValue();

        List<Element<Respondent>> respondents = caseData.getRespondents1();
        int numOfRespondents = respondents.size();

        for (int i = 0; i < 10; i++) {
            SolicitorRole solicitorRole = SolicitorRole.values()[i];

            Optional<Element<Respondent>> respondentElement
                = (i < numOfRespondents) ? Optional.of(respondents.get(i)) : Optional.empty();

            OrganisationPolicy organisationPolicy
                = respondentPolicyConverter.generateForSubmission(solicitorRole, respondentElement);

            data.put(String.format("respondentPolicy%d", i), organisationPolicy);

            if (respondentElement.isPresent()) {
                NoticeOfChangeAnswers noticeOfChangeAnswer
                    = noticeOfChangeRespondentConverter.generateForSubmission(respondentElement.get(), firstApplicant);
                data.put(String.format("noticeOfChangeAnswers%d", i), noticeOfChangeAnswer);
            }
        }

        return data;
    }

    public List<Element<Respondent>> updateRespondentPolicies(CaseData caseData,
                                                              CaseData caseDataBefore,
                                                              UserDetails userDetails) {
        RespondentPolicyData respondentPolicyData = caseData.getRespondentPolicyData();
        RespondentPolicyData respondentPolicyDataBefore = caseDataBefore.getRespondentPolicyData();
        List<OrganisationPolicy> respondentPolicyDiff = respondentPolicyData.diff(respondentPolicyDataBefore);

        if (!respondentPolicyDiff.isEmpty()) {
            OrganisationPolicy policy = respondentPolicyDiff.get(0);
            int index = SolicitorRole.from(policy.getOrgPolicyCaseAssignedRole()).getIndex();
            Organisation organisation = policy.getOrganisation();

            return updateRespondents(caseData.getRespondents1(), index, userDetails, organisation);
        } else {
            throw new IllegalStateException("Could not find updated respondentPolicy");
        }
    }

    private List<Element<Respondent>> updateRespondents(List<Element<Respondent>> respondents,
                                                        Integer index,
                                                        UserDetails userDetails,
                                                        Organisation organisation) {
        Element<Respondent> respondentElement = respondents.get(index);

        Respondent updatedRespondent = respondentElement.getValue().toBuilder()
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .email(userDetails.getEmail())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().isPresent() ? userDetails.getSurname().get() : "")
                .organisation(organisation)
                .build())
            .build();

        respondents.set(index, element(respondentElement.getId(), updatedRespondent));

        return respondents;
    }
}
