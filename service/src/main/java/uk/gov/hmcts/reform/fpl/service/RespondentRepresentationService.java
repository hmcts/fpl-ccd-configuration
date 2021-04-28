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
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentRepresentationService {

    private final NoticeOfChangeAnswersConverter noticeOfChangeRespondentConverter;
    private final RespondentPolicyConverter respondentPolicyConverter;
    private final ChangeOfRepresentationService changeOfRepresentationService;

    public Map<String, Object> generateForSubmission(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        Applicant firstApplicant = caseData.getAllApplicants().get(0).getValue();

        List<Element<Respondent>> respondents = caseData.getRespondents1();
        int numOfRespondents = respondents.size();

        for (int i = 0; i < SolicitorRole.values().length; i++) {
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

    public Map<String, Object> updateRepresentation(CaseData caseData, UserDetails solicitor) {
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole solicitorRole = SolicitorRole.from(change.getCaseRoleId().getValueCode());

        final List<Element<Respondent>> respondents = defaultIfNull(caseData.getRespondents1(), emptyList());

        final Respondent respondent = respondents.get(solicitorRole.getIndex()).getValue();

        RespondentSolicitor removedSolicitor = respondent.getSolicitor();

        RespondentSolicitor addedSolicitor = RespondentSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getForename())
            .lastName(solicitor.getSurname().orElse(EMPTY))
            .organisation(change.getOrganisationToAdd())
            .build();

        respondent.setLegalRepresentation(YesNo.YES.getValue());
        respondent.setSolicitor(addedSolicitor);

        return Map.of(
            "respondents1", caseData.getRespondents1(),
            "changeOfRepresentatives", changeOfRepresentationService.changeRepresentative(
                ChangeOfRepresentationRequest.builder()
                    .method(ChangeOfRepresentationMethod.NOC)
                    .by(solicitor.getEmail())
                    .respondent(respondent)
                    .current(caseData.getChangeOfRepresentatives())
                    .addedRepresentative(addedSolicitor)
                    .removedRepresentative(removedSolicitor)
                    .build()
            )
        );

    }
}
