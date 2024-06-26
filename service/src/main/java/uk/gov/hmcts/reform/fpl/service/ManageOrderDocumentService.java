package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.utils.GrammarHelper;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageOrderDocumentService {
    private final ChildrenSmartSelector childrenSmartSelector;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    public Map<String, String> commonContextElements(CaseData caseData) {
        Map<String, String> context = new HashMap<>();
        final int numOfChildren = childrenSmartSelector.getSelectedChildren(caseData).size();
        context.put("childOrChildren", getChildGrammar(numOfChildren));
        context.put("childIsOrAre", getChildIsOrAreGrammar(numOfChildren));
        context.put("childWasOrWere", getChildWasOrWereGrammar(numOfChildren));
        context.put("localAuthorityName", nonNull(caseData.getCaseLocalAuthority())
            ? laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority())
            : caseData.getApplicantName().orElse(null));
        context.put("courtName", caseData.getCourt() != null ? caseData.getCourt().getName() : null);
        return context;
    }

    public String getChildGrammar(int numOfChildren) {
        return GrammarHelper.getChildGrammar(numOfChildren);
    }

    private String getChildIsOrAreGrammar(int numOfChildren) {
        return GrammarHelper.getIsOrAreGrammar(numOfChildren);
    }

    private String getChildWasOrWereGrammar(int numOfChildren) {
        return GrammarHelper.getWasOrWereGrammar(numOfChildren);
    }
}
