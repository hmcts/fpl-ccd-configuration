package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildRepresentationService {

    private final OptionCountBuilder optionCountBuilder;
    private final ChildRepresentationDetailsSerializer childRepSerializer;

    public Map<String, Object> populateRepresentationDetails(CaseData caseData) {

        if (YesNo.NO == YesNo.fromString(caseData.getChildrenEventData().getChildrenHaveRepresentation())) {
            return cleanUpData();
        }

        Map<String, Object> data = new HashMap<>();
        data.put(OptionCountBuilder.CASE_FIELD, optionCountBuilder.generateCode(caseData.getAllChildren()));
        data.putAll(childRepSerializer.serialise(caseData.getAllChildren()));
        return data;
    }

    private Map<String, Object> cleanUpData() {
        Map<String, Object> data = new HashMap<>();
        data.put(OptionCountBuilder.CASE_FIELD, null);
        data.putAll(childRepSerializer.serialise(null));
        return data;
    }

}
