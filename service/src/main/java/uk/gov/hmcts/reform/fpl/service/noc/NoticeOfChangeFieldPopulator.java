package uk.gov.hmcts.reform.fpl.service.noc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.POPULATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeFieldPopulator {
    public final NoticeOfChangeAnswersConverter answersConverter;
    public final RespondentPolicyConverter policyConverter;

    public Map<String, Object> generate(CaseData caseData, Representing representing) {
        return generate(caseData, representing, POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();

        String applicant = getApplicantName(caseData);

        List<Element<WithSolicitor>> elements = representing.getTarget().apply(caseData);
        int numElements = elements.size();

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<Element<WithSolicitor>> solicitorContainer = i < numElements
                                                                  ? Optional.of(elements.get(i))
                                                                  : Optional.empty();

            OrganisationPolicy organisationPolicy = policyConverter.generate(
                solicitorRole, solicitorContainer
            );

            data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

            Optional<NoticeOfChangeAnswers> possibleAnswer = populateAnswer(
                strategy, applicant, solicitorContainer
            );

            if (possibleAnswer.isPresent()) {
                data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
            }
        }

        return data;
    }

    private Optional<NoticeOfChangeAnswers> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           String applicantName,
                                                           Optional<Element<WithSolicitor>> element) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeAnswers.builder().build());
        }

        return element.map(e -> answersConverter.generateForSubmission(e, applicantName));
    }

    private String getApplicantName(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return caseData.getLocalAuthorities().get(0).getValue().getName();
        }

        return caseData.getAllApplicants().get(0).getValue().getParty().getOrganisationName();
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }
}
