package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.LIVE_IN_REFUGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.fromString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildRepresentationService {

    private final OptionCountBuilder optionCountBuilder;
    private final ChildRepresentationDetailsFlattener childRepSerializer;

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

    public Map<String, Object> finaliseChildrenAndRepresentationDetails(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();
        List<Element<Child>> children = caseData.getAllChildren();

        return Map.of(
            "children1", IntStream.range(0, children.size())
                .mapToObj(idx -> {
                    final Element<Child> childElement = children.get(idx);
                    Child.ChildBuilder childBuilder = childElement.getValue().toBuilder()
                        .solicitor(selectSpecifiedRepresentative(eventData, idx));

                    if (childElement.getValue().getParty() != null
                        && LIVE_IN_REFUGE.getValue().equalsIgnoreCase(
                            childElement.getValue().getParty().getLivingSituation())) {
                        childBuilder = childBuilder.party(childElement.getValue().getParty().toBuilder()
                            .detailsHidden(YES.getValue()).build());
                    }

                    return element(childElement.getId(), childBuilder.build());
                })
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

}
