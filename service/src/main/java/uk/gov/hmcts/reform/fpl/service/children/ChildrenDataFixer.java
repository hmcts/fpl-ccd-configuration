package uk.gov.hmcts.reform.fpl.service.children;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInReturnedState;

@Component
public class ChildrenDataFixer {

    private final ObjectMapper mapper;

    @Autowired
    public ChildrenDataFixer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public CaseDetails fix(final CaseDetails caseDetails) {
        if (isInOpenState(caseDetails) || isInReturnedState(caseDetails)) {
            return caseDetails;
        }

        Map<String, Object> data = caseDetails.getData();
        final List<Element<Child>> children = mapper.convertValue(data.get("children1"), new TypeReference<>() {});

        if (1 == children.size()) {
            // directly adding to the map to ensure that it is persisted in the case data when returning to ccd
            data.put("childrenHaveSameRepresentation", "Yes");
        }

        return caseDetails;
    }
}
