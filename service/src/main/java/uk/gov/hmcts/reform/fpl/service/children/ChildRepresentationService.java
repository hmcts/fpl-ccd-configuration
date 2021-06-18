package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildRepresentationService {

    private final OptionCountBuilder optionCountBuilder;
    private final ChildRepresentationDetailsFlattener childRepSerializer;

    public Map<String, Object> populateRepresentationDetails(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();

        if (YesNo.NO == YesNo.fromString(eventData.getChildrenHaveRepresentation())) {
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

}
