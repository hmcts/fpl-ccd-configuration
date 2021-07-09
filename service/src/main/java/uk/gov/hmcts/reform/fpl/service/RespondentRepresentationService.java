package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentRepresentationService {

    private final NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;
    private final RespondentPolicyConverter respondentPolicyConverter;
    private final ChangeOfRepresentationService changeOfRepresentationService;

    @SuppressWarnings("unchecked")
    public Map<String, Object> generate(CaseData caseData,
                                        SolicitorRole.Representing representing) {
        Map<String, Object> data = new HashMap<>();

        Applicant firstApplicant = caseData.getAllApplicants().get(0).getValue();

        List<Element<WithSolicitor>> respondents = representing.getTarget().apply(caseData);
        int numOfRespondents = respondents.size();

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<Element<WithSolicitor>> respondentElement
                = (i < numOfRespondents) ? Optional.of(respondents.get(i)) : Optional.empty();

            OrganisationPolicy organisationPolicy
                = respondentPolicyConverter.generate(solicitorRole, respondentElement);

            data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

            if (respondentElement.isPresent()) {
                NoticeOfChangeAnswers noticeOfChangeAnswer = noticeOfChangeAnswersConverter.generateForSubmission(
                        (Element) respondentElement.get(), firstApplicant
                );
                data.put(String.format(representing.getNocAnswersTemplate(), i), noticeOfChangeAnswer);
            }
        }

        return data;
    }

    public Map<String, Object> updateRepresentation(CaseData caseData, UserDetails solicitor) {
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole solicitorRole = SolicitorRole.from(change.getCaseRoleId().getValueCode());

        final List<Element<WithSolicitor>> respondents = defaultIfNull(solicitorRole.getRepresenting()
            .getTarget().apply(caseData), Collections.emptyList()
        );

        final WithSolicitor respondent = respondents.get(solicitorRole.getIndex()).getValue();

        RespondentSolicitor removedSolicitor = respondent.getSolicitor();

        RespondentSolicitor addedSolicitor = RespondentSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getForename())
            .lastName(solicitor.getSurname().orElse(EMPTY))
            .organisation(change.getOrganisationToAdd())
            .build();

        //        TODO: FIX!!! FUNCTION SPECIFIC (strategy)
        if (SolicitorRole.Representing.RESPONDENT == solicitorRole.getRepresenting()) {
            ((Respondent) respondent).setLegalRepresentation(YesNo.YES.getValue());
        }

        respondent.setSolicitor(addedSolicitor);

        return Map.of(
            solicitorRole.getRepresenting().getCaseField(), solicitorRole.getRepresenting().getTarget().apply(caseData),
            "changeOfRepresentatives", changeOfRepresentationService.changeRepresentative(
                ChangeOfRepresentationRequest.builder()
                    .method(ChangeOfRepresentationMethod.NOC)
                    .by(solicitor.getEmail())
                    .respondent(SolicitorRole.Representing.RESPONDENT == solicitorRole.getRepresenting() ?
                        (ConfidentialParty) respondent : null)
                    .child(SolicitorRole.Representing.CHILD == solicitorRole.getRepresenting() ?
                        (ConfidentialParty) respondent : null)
                    .current(caseData.getChangeOfRepresentatives())
                    .addedRepresentative(addedSolicitor)
                    .removedRepresentative(removedSolicitor)
                    .build()
            )
        );

    }
}
