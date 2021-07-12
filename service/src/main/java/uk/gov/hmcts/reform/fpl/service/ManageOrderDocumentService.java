package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageOrderDocumentService {
    private final ChildrenSmartSelector childrenSmartSelector;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    public Map<String, String> commonContextElements(CaseData caseData) {
        Map<String, String> context = new HashMap<>();
        context.put("childOrChildren", getChildGrammar(childrenSmartSelector.getSelectedChildren(caseData).size()));
        context.put("childIsOrAre", getChildIsOrAreGrammar(childrenSmartSelector.getSelectedChildren(caseData).size()));
        context.put("localAuthorityName", laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        return context;
    }

    private String getChildGrammar(int numOfChildren) {
        return (numOfChildren == 1) ? "child" : "children";
    }

    private String getChildIsOrAreGrammar(int numOfChildren) {
        return (numOfChildren == 1) ? "is" : "are";
    }
}
