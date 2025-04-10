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
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                strategy, solicitorContainer
            );

            if (possibleAnswer.isPresent()) {
                data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
            }
        }

        return data;
    }

    public Map<String, Object> generateApplicantAnswer(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        NoticeOfChangeAnswers nocAnswers = NoticeOfChangeAnswers.builder().build();

        if (caseData.isOutsourced()) {
            LocalAuthority localAuthority = caseData.getLocalAuthorities().stream()
                .map(Element::getValue)
                .filter(la -> la.getId().equals(caseData.getOutsourcingPolicy().getOrganisation().getOrganisationID()))
                .findFirst().orElse(null);

            if (localAuthority != null) {
                nocAnswers = NoticeOfChangeAnswers.builder()
                    .respondentFirstName(localAuthority.getRepresentingDetails().getFirstName())
                    .respondentLastName(localAuthority.getRepresentingDetails().getLastName())
                    .build();
            }
            data.put("noticeOfChangeAnswersThirdPartyRespondent", nocAnswers);
        } else {
            data.put("noticeOfChangeAnswersApplicant", nocAnswers);
        }

        return data;
    }

    private Optional<NoticeOfChangeAnswers> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           Optional<Element<WithSolicitor>> element) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeAnswers.builder().build());
        }

        return element.map(e -> answersConverter.generateForSubmission(e));
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }
}
