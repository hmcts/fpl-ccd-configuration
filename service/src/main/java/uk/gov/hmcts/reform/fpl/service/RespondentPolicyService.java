package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            OrganisationPolicy organisationPolicy;
            SolicitorRole solicitorRole = SolicitorRole.values()[i];

            if (i < numOfRespondents) {
                Element<Respondent> respondentElement = respondents.get(i);
                organisationPolicy = respondentPolicyConverter.generateForSubmission(solicitorRole, respondentElement);

                NoticeOfChangeAnswers noticeOfChangeAnswer
                    = noticeOfChangeRespondentConverter.generateForSubmission(respondentElement, firstApplicant);

                data.put(String.format("noticeOfChangeAnswers%d", i), noticeOfChangeAnswer);
            } else {
                organisationPolicy = respondentPolicyConverter.generateForSubmission(solicitorRole);
            }

            data.put(String.format("respondentPolicy%d", i), organisationPolicy);
        }

        return data;
    }
}
