package uk.gov.hmcts.reform.fpl.service.noc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final List<NoticeOfChangeUpdateAction> updateActions;

    public Map<String, Object> updateRepresentation(CaseData caseData, UserDetails solicitor) {
        final ChangeOrganisationRequest change = getChangeOrganisationRequest(caseData);

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getValueCode()).orElseThrow();

        final Representing representing = role.getRepresenting();

        final List<Element<WithSolicitor>> elements = defaultIfNull(
            representing.getTarget().apply(caseData), Collections.emptyList()
        );

        final WithSolicitor container = elements.get(role.getIndex()).getValue();

        RespondentSolicitor removedSolicitor = container.getSolicitor();

        RespondentSolicitor addedSolicitor = generateRespondentSolicitor(solicitor, change);

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

    public Map<String, Object> updateRepresentationThirdPartyOutsourcing(CaseData caseData, UserDetails solicitor) {
        HashMap<String, Object> data = new HashMap<>();
        final ChangeOrganisationRequest change = getChangeOrganisationRequest(caseData);

        RespondentSolicitor removedSolicitor = RespondentSolicitor.builder()
            .organisation(caseData.getOutsourcingPolicy().getOrganisation())
            .build();

        RespondentSolicitor addedSolicitor = generateRespondentSolicitor(solicitor, change);

        List<Element<ChangeOfRepresentation>> auditList = changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(ChangeOfRepresentationMethod.NOC)
                .by(solicitor.getEmail())
                .current(caseData.getChangeOfRepresentatives())
                .addedRepresentative(addedSolicitor)
                .removedRepresentative(removedSolicitor)
                .build()
        );

        data.put("changeOfRepresentatives", auditList);
        return data;
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseData caseData) {
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        return change;
    }

    private RespondentSolicitor generateRespondentSolicitor(UserDetails solicitor, ChangeOrganisationRequest change) {
        return RespondentSolicitor.builder()
            .email(solicitor.getEmail() == null ? EMPTY : solicitor.getEmail())
            .firstName(solicitor.getForename())
            .lastName(solicitor.getSurname().orElse(EMPTY))
            .organisation(change.getOrganisationToAdd())
            .build();
    }
}
