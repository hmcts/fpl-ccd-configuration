package uk.gov.hmcts.reform.fpl.service.document;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConfidentialDocumentsSplitter {

    public void updateConfidentialDocsInCaseDetails(CaseDetailsMap caseDetailsMap,
                                                    List<Element<SupportingEvidenceBundle>> documents,
                                                    String keyPrefix) {
        String nonConfidentialPrefix = keyPrefix + "NC";

        List<Element<SupportingEvidenceBundle>> nonConfidentialCopy = documents.stream()
            .filter(doc -> !doc.getValue().isConfidentialDocument())
            .collect(Collectors.toList());

        caseDetailsMap.putIfNotEmpty(nonConfidentialPrefix, nonConfidentialCopy);
    }
}
