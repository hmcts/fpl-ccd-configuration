package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod.RESPONDENTS_EVENT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentAfterSubmissionRepresentationService {

    private final RespondentService respondentService;
    private final NoticeOfChangeFieldPopulator nocFieldPopulator;
    private final ChangeOfRepresentationService changeOfRepresentationService;

    public Map<String, Object> updateRepresentation(CaseData caseData, CaseData caseDataBefore,
                                                    SolicitorRole.Representing representing,
                                                    boolean recordChangeOrRepresentation) {

        HashMap<String, Object> updatedFields = new HashMap<>(nocFieldPopulator.generate(caseData, representing));

        if (recordChangeOrRepresentation) {
            Function<CaseData, List<Element<WithSolicitor>>> target = representing.getTarget();
            final List<Element<WithSolicitor>> respondentsAfter = defaultIfNull(target.apply(caseData), emptyList());
            final List<Element<WithSolicitor>> respondentsBefore = defaultIfNull(target.apply(caseDataBefore),
                emptyList());

            List<ChangeOrganisationRequest> representationChanges = respondentService.getRepresentationChanges(
                respondentsAfter, respondentsBefore, representing
            );

            updatedFields.put("changeOfRepresentatives", representationChanges.stream().reduce(
                defaultIfNull(caseData.getChangeOfRepresentatives(), new ArrayList<>()),
                generateRequest(respondentsAfter, respondentsBefore, representing),
                (v, v2) -> v2
            ));
        }

        return updatedFields;
    }

    private BiFunction<List<Element<ChangeOfRepresentation>>, ChangeOrganisationRequest,
        List<Element<ChangeOfRepresentation>>> generateRequest(List<Element<WithSolicitor>> respondentsAfter,
                                                               List<Element<WithSolicitor>> respondentsBefore,
                                                               SolicitorRole.Representing representing) {
        return (changeOfRepresentations, changeOrganisationRequest) ->
            changeOfRepresentationService.changeRepresentative(
                ChangeOfRepresentationRequest.builder()
                    .method(RESPONDENTS_EVENT)
                    .by("HMCTS") // this event is only allowed from judicial users
                    .respondent(getPartyIf(respondentsAfter, representing, changeOrganisationRequest,
                        SolicitorRole.Representing.RESPONDENT))
                    .child(getPartyIf(respondentsAfter, representing, changeOrganisationRequest,
                        SolicitorRole.Representing.CHILD))
                    .current(changeOfRepresentations)
                    .addedRepresentative(getSolicitor(respondentsAfter, changeOrganisationRequest))
                    .removedRepresentative(getSolicitor(respondentsBefore, changeOrganisationRequest))
                    .build());
    }

    private ConfidentialParty<?> getPartyIf(List<Element<WithSolicitor>> respondentsAfter,
                                         SolicitorRole.Representing representing,
                                         ChangeOrganisationRequest changeOrganisationRequest,
                                         SolicitorRole.Representing representingMatch) {
        return representing == representingMatch
            ? (ConfidentialParty<?>) respondentsAfter.get(changeOrganisationRequest.getCaseRole().getIndex()).getValue()
            : null;
    }

    private RespondentSolicitor getSolicitor(List<Element<WithSolicitor>> respondents,
                                             ChangeOrganisationRequest changeOrganisationRequest) {
        int solicitorIdx = changeOrganisationRequest.getCaseRole().getIndex();

        if (solicitorIdx >= respondents.size()) {
            return null;
        }

        return respondents.get(solicitorIdx)
            .getValue()
            .getSolicitor();
    }
}
