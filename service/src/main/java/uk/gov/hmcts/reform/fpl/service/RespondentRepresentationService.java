package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeUpdateAction;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentRepresentationService {

    private final NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;
    private final RespondentPolicyConverter respondentPolicyConverter;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final List<NoticeOfChangeUpdateAction> updateActions;

    public Map<String, Object> generate(CaseData caseData, Representing representing) {
        return generate(caseData, representing, NoticeOfChangeAnswersPopulationStrategy.POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();

        Applicant applicant = caseData.getAllApplicants().get(0).getValue();

        List<Element<WithSolicitor>> elements = representing.getTarget().apply(caseData);
        int numElements = elements.size();

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<Element<WithSolicitor>> solicitorContainer = i < numElements
                                                                  ? Optional.of(elements.get(i))
                                                                  : Optional.empty();

            OrganisationPolicy organisationPolicy = respondentPolicyConverter.generate(
                solicitorRole, solicitorContainer
            );

            data.put(format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

            Optional<NoticeOfChangeAnswers> possibleAnswer = populateAnswer(
                strategy, applicant, solicitorContainer
            );

            if (possibleAnswer.isPresent()) {
                data.put(format(representing.getNocAnswersTemplate(), i), possibleAnswer);
            }
        }

        return data;
    }

    public Map<String, Object> updateRepresentation(CaseData caseData, UserDetails solicitor) {
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getValueCode());

        final Representing representing = role.getRepresenting();

        final List<Element<WithSolicitor>> elements = defaultIfNull(
            representing.getTarget().apply(caseData), Collections.emptyList()
        );

        final WithSolicitor container = elements.get(role.getIndex()).getValue();

        RespondentSolicitor removedSolicitor = container.getSolicitor();

        RespondentSolicitor addedSolicitor = RespondentSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getForename())
            .lastName(solicitor.getSurname().orElse(EMPTY))
            .organisation(change.getOrganisationToAdd())
            .build();

        HashMap<String, Object> data = updateActions.stream()
            .filter(action -> action.accepts(representing))
            .findFirst()
            .map(action -> new HashMap<>(action.applyUpdates(container, caseData, addedSolicitor)))
            .orElse(new HashMap<>());

        List<Element<ChangeOfRepresentation>> auditList = changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(ChangeOfRepresentationMethod.NOC)
                .by(solicitor.getEmail())
                .respondent(RESPONDENT == representing ? (ConfidentialParty<?>) container : null)
                .child(CHILD == representing ? (ConfidentialParty<?>) container : null)
                .current(caseData.getChangeOfRepresentatives())
                .addedRepresentative(addedSolicitor)
                .removedRepresentative(removedSolicitor)
                .build()
        );

        data.put("changeOfRepresentatives", auditList);

        return data;
    }

    private Optional<NoticeOfChangeAnswers> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           Applicant applicant,
                                                           Optional<Element<WithSolicitor>> element) {
        if (NoticeOfChangeAnswersPopulationStrategy.BLANK == strategy) {
            return Optional.of(NoticeOfChangeAnswers.builder().build());
        }

        return element.map(e -> noticeOfChangeAnswersConverter.generateForSubmission(e, applicant));
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }
}
