package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.ChildSolicitorPolicyConverter;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.fromString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildRepresentationService {

    private final OptionCountBuilder optionCountBuilder;
    private final ChildRepresentationDetailsFlattener childRepSerializer;
    private final ChildSolicitorPolicyConverter childSolicitorPolicyConverter;
    private final NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;

    public Map<String, Object> populateRepresentationDetails(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();

        if (NO == fromString(eventData.getChildrenHaveRepresentation())) {
            return cleanUpData();
        }

        Map<String, Object> data = new HashMap<>();
        data.put(OptionCountBuilder.CASE_FIELD, optionCountBuilder.generateCode(caseData.getAllChildren()));
        data.putAll(childRepSerializer.serialise(caseData.getAllChildren(), eventData.getChildrenMainRepresentative()));
        return data;
    }

    private Map<String, Object> cleanUpData() {
        Map<String, Object> data = new HashMap<>();
        data.put(OptionCountBuilder.CASE_FIELD, null);
        data.putAll(childRepSerializer.serialise(null, null));
        return data;
    }

    public Map<String, Object> finaliseRepresentationDetails(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();
        List<Element<Child>> children = caseData.getAllChildren();

        return Map.of(
            "children1", IntStream.range(0, children.size())
                .mapToObj(idx -> element(children.get(idx).getId(), children.get(idx).getValue().toBuilder()
                    .representative(selectSpecifiedRepresentative(eventData, idx))
                    .build()))
                .collect(Collectors.toList())
        );
    }

    private RespondentSolicitor selectSpecifiedRepresentative(ChildrenEventData eventData, int idx) {
        if (!YES.getValue().equals(eventData.getChildrenHaveRepresentation())) {
            return null;
        }

        RespondentSolicitor mainRepresentative = eventData.getChildrenMainRepresentative();
        ChildRepresentationDetails details = eventData.getAllRepresentationDetails().get(idx);

        if (YES.getValue().equals(eventData.getChildrenHaveSameRepresentation())
            || YES.getValue().equals(details.getUseMainSolicitor())) {
            return mainRepresentative;
        }

        return details.getSolicitor();
    }

    public Map<String, Object> generateCaseAccessFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        Applicant firstApplicant = caseData.getAllApplicants().get(0).getValue();

        List<Element<Child>> children = caseData.getAllChildren();
        int numOfChildren = children.size();

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(SolicitorRole.Representing.CHILD);

        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<Element<Child>> childElement
                = (i < numOfChildren) ? Optional.of(children.get(i)) : Optional.empty();

            OrganisationPolicy organisationPolicy
                = childSolicitorPolicyConverter.generate(solicitorRole, childElement);

            data.put(String.format("childPolicy%d", i), organisationPolicy);

            if (childElement.isPresent()) {
                NoticeOfChangeAnswers noticeOfChangeAnswer
                    = noticeOfChangeAnswersConverter.generateForSubmission(childElement.get(), firstApplicant);
                data.put(String.format("noticeOfChangeChildAnswers%d", i), noticeOfChangeAnswer);
            }
        }

        return data;
    }
}
